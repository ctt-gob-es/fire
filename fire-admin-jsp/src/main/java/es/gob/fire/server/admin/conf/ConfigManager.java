package es.gob.fire.server.admin.conf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import es.gob.fire.server.admin.message.AdminFilesNotFoundException;
import es.gob.fire.server.decipher.PropertyDecipher;

public class ConfigManager {

	private static final Logger LOGGER = Logger.getLogger(ConfigManager.class.getName());

	private static final String CONFIG_FILE = "admin_config.properties";//$NON-NLS-1$

	private static final String PARAM_DB_DRIVER = "bbdd.driver"; //$NON-NLS-1$
	private static final String PARAM_DB_CONN = "bbdd.conn"; //$NON-NLS-1$
	private static final String PARAM_TEMP_DIR = "tempdir";//$NON-NLS-1$
	private static final String PARAM_CIPHER_CLASS = "cipher.class"; //$NON-NLS-1$

	private static final String PARAM_MAIL_FROM_ADDRESS = "mail.from.address"; //$NON-NLS-1$
	private static final String PARAM_MAIL_FROM_NAME = "mail.from.name"; //$NON-NLS-1$
	private static final String PARAM_MAIL_HOST = "mail.host"; //$NON-NLS-1$
	private static final String PARAM_MAIL_PORT = "mail.port"; //$NON-NLS-1$
	private static final String PARAM_MAIL_USERNAME = "mail.username"; //$NON-NLS-1$
	private static final String PARAM_MAIL_PASSWORD = "mail.password"; //$NON-NLS-1$

	private static final String PARAM_PASSWORD_EXPIRATION = "password.expiration"; //$NON-NLS-1$


	private static final String PREFIX_CIPHERED_TEXT = "{@ciphered:"; //$NON-NLS-1$
	private static final String SUFIX_CIPHERED_TEXT = "}"; //$NON-NLS-1$

	/** Variable de entorno que determina el directorio en el que buscar el fichero de configuraci&oacute;n. */
	private static final String ENVIRONMENT_VAR_CONFIG_DIR = "fire.config.path"; //$NON-NLS-1$

	/** Variable de entorno antigua que determinaba el directorio en el que buscar el fichero
	 * de configuraci&oacute;n. Se utiliza si no se ha establecido la nueva variable. */
	private static final String ENVIRONMENT_VAR_CONFIG_DIR_OLD = "clavefirma.config.path"; //$NON-NLS-1$

	private static final int DEFAULT_EXPIRED_TIME = 1800000;

	private static final Integer DEFAULT_MAIL_PORT = new Integer(587);

	/** Ruta del directorio por defecto para el guardado de temporales (directorio temporal del sistema). */
	private static String DEFAULT_TMP_DIR;

	private static Properties config = null;

	private static PropertyDecipher decipherImpl = null;

	static {
		try {
			initialize();
		} catch (final AdminFilesNotFoundException e) {
			LOGGER.log(Level.SEVERE, "No se encuentra el fichero de configuracion del modulo de administracion", e); //$NON-NLS-1$
		}
	}


	/**
	 * Carga la configuraci&oacute;n del m&oacute;dulo.
	 * @throws AdminFilesNotFoundException Cuando no se encuentra o no se puede cargar el fichero de configuraci&oacute;n.
	 */
	private static void initialize() throws AdminFilesNotFoundException {

		// Si ya esta cargada, se evita repetir el proceso
		if (config != null) {
			return;
		}

		final Properties dbConfig = new Properties();
		try {
			String configDir = System.getProperty(ENVIRONMENT_VAR_CONFIG_DIR);
			if (configDir == null) {
				configDir = System.getProperty(ENVIRONMENT_VAR_CONFIG_DIR_OLD);
			}

			final File configFile = configDir != null ? new File(configDir, CONFIG_FILE).getCanonicalFile() : null;
			if (configFile == null || !configFile.isFile() || !configFile.canRead()) {
				LOGGER.warning(
						"No se encontro el fichero " + CONFIG_FILE + " en el directorio configurado en la variable " + //$NON-NLS-1$ //$NON-NLS-2$
								ENVIRONMENT_VAR_CONFIG_DIR + ": " + configDir + //$NON-NLS-1$
						"\nSe buscara en el CLASSPATH."); //$NON-NLS-1$
				try (InputStream is = ConfigManager.class.getResourceAsStream('/' + CONFIG_FILE);) {
					dbConfig.load(is);
				}
			}
			else {
				try (InputStream is = new FileInputStream(configFile);) {
					dbConfig.load(is);
				}
			}
		}
		catch(final NullPointerException e){
			LOGGER.severe("No se ha encontrado el fichero de configuracion del modulo de administracion: " + e); //$NON-NLS-1$
			throw new AdminFilesNotFoundException("No se ha encontrado el fichero de configuracion " + CONFIG_FILE, CONFIG_FILE, e); //$NON-NLS-1$
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "No se pudo cargar el fichero de configuracion del modulo de administracion", e); //$NON-NLS-1$
			throw new AdminFilesNotFoundException("No se pudo cargar el fichero de configuracion " + CONFIG_FILE, CONFIG_FILE, e); //$NON-NLS-1$
		}

		if (dbConfig.containsKey(PARAM_CIPHER_CLASS)) {
			final String decipherClassname = dbConfig.getProperty(PARAM_CIPHER_CLASS);
			if (decipherClassname != null && !decipherClassname.trim().isEmpty()) {
				try {
					final Class<?> decipherClass = Class.forName(decipherClassname);
					final Object decipher = decipherClass.newInstance();
					if (PropertyDecipher.class.isInstance(decipher)) {
						decipherImpl = (PropertyDecipher) decipher;
					}
				}
				catch (final Exception e) {
					LOGGER.log(Level.WARNING, "Se ha definido una clase de descifrado no valida", e); //$NON-NLS-1$
				}
			}
		}

		try {
			DEFAULT_TMP_DIR = System.getProperty("java.io.tmpdir"); //$NON-NLS-1$
		}
		catch (final Exception e) {
			try {
				DEFAULT_TMP_DIR = File.createTempFile("tmp", null).getParentFile().getAbsolutePath(); //$NON-NLS-1$
			}
			catch (final Exception e1) {
				DEFAULT_TMP_DIR = null;
				LOGGER.warning(
					"No se ha podido cargar un directorio temporal por defecto, se debera configurar expresamente en el fichero de propiedades: "  + e1 //$NON-NLS-1$
				);
			}
		}

		config = dbConfig;
	}

	/**
	 * Recupera una propiedad del fichero de configuraci&oacute;n y devuelve su
	 * valor habi&eacute;ndolo descifrado si era necesario.
	 * @param key Clave de la propiedad.
	 * @return Valor descifrado de la propiedad o {@code null} si la propiedad no estaba definida.
	 */
	private static String getProperty(final String key) {
		String value = config.getProperty(key);
		if (decipherImpl != null && value != null) {
			while (isCiphered(value)) {
				try {
					value = decipherFragment(value);
				}
				catch (final Exception e) {
					LOGGER.log(Level.WARNING, String.format("Error al descifrar la propiedad %1s", PARAM_TEMP_DIR), e); //$NON-NLS-1$
					return null;
				}
			}
		}
		return value;
	}

	/**
	 * Comprueba si una cadena de texto tiene alg&uacute;n fragmento cifrado.
	 * @param text Cadena de texto.
	 * @return {@code true} si la cadena contiene fragmentos cifrados. {@code false},
	 * en caso contrario.
	 */
	private static boolean isCiphered(final String text) {
		final int idx = text.indexOf(PREFIX_CIPHERED_TEXT);
		return idx != -1 && text.indexOf(SUFIX_CIPHERED_TEXT, idx + PREFIX_CIPHERED_TEXT.length()) != -1;
	}

	/**
	 * Texto cifrado del que descifrar un fragmento.
	 * @param text Texto con los marcadores que se&ntilde;alan que hay un fragmento cifrado.
	 * @return  Texto con un framento descifrado.
	 * @throws IOException Cuando ocurre un error al descifrar los datos.
	 */
	private static String decipherFragment(final String text) throws IOException {
		final int idx1 = text.indexOf(PREFIX_CIPHERED_TEXT);
		final int idx2 = text.indexOf(SUFIX_CIPHERED_TEXT, idx1);
		final String base64Text = text.substring(idx1 + PREFIX_CIPHERED_TEXT.length(), idx2).trim();

		return text.substring(0, idx1) +
				decipherImpl.decipher(Base64.decode(base64Text)) +
				text.substring(idx2 + SUFIX_CIPHERED_TEXT.length());
	}


	/**
	 * Recupera la cadena del controlador JDBC configurado.
	 * @return Cadena del controlador JDBC o {@code null}
	 * si no se ha encontrado o si no se ha podido obetener.
	 */
	public static String getDbDriverString() {
		final String driverString = getProperty(PARAM_DB_DRIVER);
		if (driverString == null) {
			LOGGER.log(
					Level.SEVERE,
					String.format(
							"No se ha encontrado la clase del driver JDBC ('%1s') en el fichero de configuracion", //$NON-NLS-1$
							PARAM_DB_DRIVER));
		}
		return driverString;
	}

	/**
	 * Recupera la cadena de conexi&oacute;n a base de datos configurada.
	 * @return Cadena de conexi&oacute;n a base de datos configurada o {@code null}
	 * si no se ha encontrado o si no se ha podido obetener.
	 */
	public static String getDbConnectionString() {

		final String connectionString = getProperty(PARAM_DB_CONN);
		if (connectionString == null) {
			LOGGER.log(
					Level.SEVERE,
					String.format(
							"No se ha encontrado la cadena de conexion ('%1s') a la BD en el fichero de configuracion", //$NON-NLS-1$
							PARAM_DB_CONN));
		}
		return connectionString;
	}

	/**
	 * Recupera el directorio temporal configurado.
	 * @return Ruta del directorio temporal o nulo si no se ha .
	 */
	public static String getTempDir() {
		String pathTempLogs = getProperty(PARAM_TEMP_DIR);
		if (pathTempLogs == null) {
			pathTempLogs = DEFAULT_TMP_DIR;
			LOGGER.log(Level.WARNING, String.format(
							"No se ha configurado el directorio temporal (%1s). Se usara el del sistema: %2s", //$NON-NLS-1$
							PARAM_TEMP_DIR, DEFAULT_TMP_DIR));
		}
		return pathTempLogs;
	}

	/**
	 * Recupera el host para establecer la conexi&oacute;n.
	 * @return Host de conexi&oacute;n.
	 *
	 */
	public static String getMailHost() {

		final String hostConecction = getProperty(PARAM_MAIL_HOST);
		if (hostConecction == null) {
			LOGGER.log(
					Level.SEVERE,
					String.format(
							"No se ha configurado el host del correo electronico (%1s)", //$NON-NLS-1$
							PARAM_MAIL_HOST));
		}
		return hostConecction;
	}

	/**
	 * Recupera el puerto de conexi&oacute;n.
	 * @return Puerto de conexi&oacute;n
	 */
	public static Integer getMailPort() {

		Integer portConecction;
		try {
			final String portText = getProperty(PARAM_MAIL_PORT);
			portConecction = new Integer(portText);
		}
		catch (final Exception e) {
			LOGGER.severe(
					String.format(
							"No se ha configurado un puerto correcto en la propiedad '%1s' del fichero de configuracion. Se usara '%2d'", //$NON-NLS-1$
							PARAM_MAIL_PORT,
							DEFAULT_MAIL_PORT));
			portConecction = DEFAULT_MAIL_PORT;
		}
		return portConecction;
	}

	/**
	 * Recupera el nombre del usuario de la base de datos.
	 * @return Nombre del usuario logeado
	 *
	 */
	public static String getMailUsername() {

		final String mailUsername = getProperty(PARAM_MAIL_USERNAME);
		if (mailUsername == null) {
			LOGGER.log(
					Level.SEVERE,
					String.format(
							"El usuario de la cuenta de correo (%1s) no puede ser nulo", //$NON-NLS-1$
							PARAM_MAIL_USERNAME));
		}
		return mailUsername;
	}

	/**
	 * Recupera el password del usuario el cual va a enviar el mensaje.
	 * @return Password para realizar el env&iacute;o
	 *
	 */
	public static String getMailPassword() {

		final String mailPassword = getProperty(PARAM_MAIL_PASSWORD);
		if (mailPassword == null) {
			LOGGER.log(
					Level.SEVERE,
					String.format(
							"La contrasena de la cuenta de correo (%1s) no puede ser nula", //$NON-NLS-1$
							PARAM_MAIL_PASSWORD));
		}
		return mailPassword;
	}

	/**
	 * Recupera el emisor el cual va a enviar el mensaje.
	 * @return emisor del mensaje
	 *
	 */
	public static String getMailFromAddress() {

		final String mailFrom = getProperty(PARAM_MAIL_FROM_ADDRESS);
		if (mailFrom == null) {
			LOGGER.log(
					Level.SEVERE,
					String.format(
							"La direcciones de correo desde la que se envia el mensaje (%1s) no puede ser nula", //$NON-NLS-1$
							PARAM_MAIL_FROM_ADDRESS));
		}
		return mailFrom;
	}


	/**
	 * Recupera el nombre de usuario emisor el cual va a enviar el mensaje.
	 * @return nombre del emisor del mensaje
	 *
	 */
	public static String getMailFromName() {

		final String mailFromName = getProperty(PARAM_MAIL_FROM_NAME);
		if (mailFromName == null) {
			LOGGER.log(
					Level.SEVERE,
					String.format(
							"El nombre del emisor del mensaje (%1s) no puede ser nulo", //$NON-NLS-1$
							PARAM_MAIL_FROM_NAME));
		}
		return mailFromName;
	}

	/**
	 * Recupera el tiempo que dura antes de caducar el enlace de restauraci&oacute;n de contrase&ntilde;a
	 * enviado por correo electr&acute;nico.
	 * @return Tiempo de expiraci&oacute;n en milisegundos.
	 */
	public static int getExpiration() {

		int passwordExpirationTime;
		try {
			passwordExpirationTime = Integer.parseInt(getProperty(PARAM_PASSWORD_EXPIRATION));
		}
		catch (final Exception e) {
			LOGGER.severe(String.format("No se ha podido identificar el tiempo de expiracion del " //$NON-NLS-1$
					+ "enlace de restauraci&oacute;n de contrasena (%1s). Se utilizara el tiempo " //$NON-NLS-1$
					+ "por defecto (%2d)", PARAM_PASSWORD_EXPIRATION, DEFAULT_EXPIRED_TIME)); //$NON-NLS-1$
			passwordExpirationTime = DEFAULT_EXPIRED_TIME;
		}
		return passwordExpirationTime;
	}
}
