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

	private static final String PARAM_DB_DRIVER = "bbdd.driver"; //$NON-NLS-1$
	private static final String PARAM_DB_CONN = "bbdd.conn"; //$NON-NLS-1$
	private static final String PARAM_TEMP_DIR = "tempdir";//$NON-NLS-1$
	private static final String PARAM_CIPHER_CLASS = "cipher.class"; //$NON-NLS-1$
	private static final String CONFIG_FILE = "admin_config.properties";//$NON-NLS-1$

	private static final String PARAN_MAIL_SENDER = "mail.sender.address"; //$NON-NLS-1$
	private static final String PARAM_MAIL_FROM = "mail.from"; //$NON-NLS-1$
	private static final String PARAM_MAIL_FROMNAME = "mail.fromname"; //$NON-NLS-1$
	private static final String PARAM_MAIL_HOST = "mail.host"; //$NON-NLS-1$
	private static final String PARAM_MAIL_PORT = "mail.port"; //$NON-NLS-1$
	private static final String PARAM_MAIL_SMTP_USERNAME = "mail.smtp.username"; //$NON-NLS-1$
	private static final String PARAM_MAIL_SMTP_PASSWORD = "mail.smtp.password"; //$NON-NLS-1$
	private static final String PARAM_MAIL_STARTTLS = "mail.starttls"; //$NON-NLS-1$
	private static final String PARAM_MAIL_AUTH = "mail.auth"; //$NON-NLS-1$

	private static final String PARAM_PASSWORD_EXPIRATION = "password.expiration"; //$NON-NLS-1$


	private static final String PREFIX_CIPHERED_TEXT = "{@ciphered:"; //$NON-NLS-1$
	private static final String SUFIX_CIPHERED_TEXT = "}"; //$NON-NLS-1$

	/** Variable de entorno que determina el directorio en el que buscar el fichero de configuraci&oacute;n. */
	private static final String ENVIRONMENT_VAR_CONFIG_DIR = "fire.config.path"; //$NON-NLS-1$

	/** Variable de entorno antigua que determinaba el directorio en el que buscar el fichero
	 * de configuraci&oacute;n. Se utiliza si no se ha establecido la nueva variable. */
	private static final String ENVIRONMENT_VAR_CONFIG_DIR_OLD = "clavefirma.config.path"; //$NON-NLS-1$

	private static final long DEFAULT_EXPIRED_TIME = 1800000;

	private static final int DEFAULT_MAIL_PORT = 587;

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

		InputStream is = null;
		final Properties dbConfig = new Properties();
		try {
			String configDir = System.getProperty(ENVIRONMENT_VAR_CONFIG_DIR);
			if (configDir == null) {
				configDir = System.getProperty(ENVIRONMENT_VAR_CONFIG_DIR_OLD);
			}
			if (configDir != null) {
				final File configFile = new File(configDir, CONFIG_FILE).getCanonicalFile();
				if (!configFile.isFile() || !configFile.canRead()) {
					LOGGER.warning(
							"No se encontro el fichero " + CONFIG_FILE + " en el directorio configurado en la variable " + //$NON-NLS-1$ //$NON-NLS-2$
									ENVIRONMENT_VAR_CONFIG_DIR + ": " + configFile.getAbsolutePath() + //$NON-NLS-1$
							"\nSe buscara en el CLASSPATH."); //$NON-NLS-1$
				}
				else {
					is = new FileInputStream(configFile);
				}
			}

			if (is == null) {
				is = ConfigManager.class.getResourceAsStream('/' + CONFIG_FILE);
				//is = new FileInputStream("C:\\Users\\carlos.j.raboso\\Documents\\RepositorioGit\\fire\\fire-admin-jsp\\src\\main\\resources\\" + CONFIG_FILE);
			}

			dbConfig.load(is);
			is.close();
		}
		catch(final NullPointerException e){
			LOGGER.severe("No se ha encontrado el fichero de configuracion de la base de datos: " + e); //$NON-NLS-1$
			if (is != null) {
				try { is.close(); } catch (final Exception ex) { ex.getStackTrace();/* No hacemos nada */ }
			}
			throw new AdminFilesNotFoundException("No se ha encontrado el fichero de propiedades" + CONFIG_FILE, CONFIG_FILE, e); //$NON-NLS-1$
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "No se pudo cargar el fichero de configuracion del modulo de administracion", e); //$NON-NLS-1$
			if (is != null) {
				try { is.close(); } catch (final Exception ex) { ex.getStackTrace();/* No hacemos nada */ }
			}
			throw new AdminFilesNotFoundException("No se pudo cargar el fichero de configuracion " + CONFIG_FILE, CONFIG_FILE, e); //$NON-NLS-1$
		}
		finally {
			if (is != null) {
				try { is.close(); } catch (final Exception ex) {ex.getStackTrace(); /* No hacemos nada */ }
			}
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
	 * Recupera la cadena del controlador JDBC configurado.
	 * @return Cadena del controlador JDBC o {@code null}
	 * si no se ha encontrado o si no se ha podido obetener.
	 */
	public static String getDbDriverString() {

		String driverString;
		try {
		driverString = getProperty(PARAM_DB_DRIVER);
		}
		catch (final Exception e) {
			LOGGER.severe(String.format("Error al descifrar la propiedad %1s", PARAM_DB_DRIVER)); //$NON-NLS-1$
			return null;
		}

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

		String connectionString;
		try {
			connectionString = getProperty(PARAM_DB_CONN);
		}
		catch (final Exception e) {
			LOGGER.severe(String.format("Error al descifrar la propiedad %1s", PARAM_DB_CONN)); //$NON-NLS-1$
			return null;
		}

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
	 * Recupera una propiedad del fichero de configuraci&oacute;n y devuelve su
	 * valor habi&eacute;ndolo descifrado si era necesario.
	 * @param key Clave de la propiedad.
	 * @return Valor descifrado de la propiedad o {@code null} si la propiedad no estaba definida.
	 * @throws IOException  Cuando ocurre un error al descifrar la propiedad.
	 */
	private static String getProperty(final String key) throws IOException {
		String value = config.getProperty(key);
		if (decipherImpl != null && value != null) {
			while (isCiphered(value)) {
				value = decipherFragment(value);
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
	 * Recupera el directorio temporal configurado.
	 * @return Ruta del directorio temporal o nulo si no se ha .
	 */
	public static String getTempDir() {

		String pathTempLogs;
		try {
			pathTempLogs = getProperty(PARAM_TEMP_DIR);
		}
		catch (final Exception e) {
			LOGGER.severe(String.format("Error al descifrar la propiedad %1s. Se usara el directorio temporal por defecto.", PARAM_TEMP_DIR)); //$NON-NLS-1$
			pathTempLogs = DEFAULT_TMP_DIR;
		}

		if (pathTempLogs == null) {
			pathTempLogs = DEFAULT_TMP_DIR;
			LOGGER.log(Level.WARNING, String.format(
							"No se ha configurado el directorio temporal (%1s). Se usara el del sistema: %2s", //$NON-NLS-1$
							PARAM_TEMP_DIR, DEFAULT_TMP_DIR));
		}
		return pathTempLogs;
	}

	/**
	 * Envia al mail el mensaje.
	 * @return env&iacute;o de mail del mensaje
	 * si no se ha encontrado o si no se ha podido obetener.
	 */
	public static String getSendMail() {

		String sendMail;
		try {
			sendMail = getProperty(PARAN_MAIL_SENDER);
		}
		catch (final Exception e) {
			LOGGER.severe(String.format("Error al realizar el env&iacute;o del mensaje", PARAN_MAIL_SENDER)); //$NON-NLS-1$
			return null;
		}

		if (sendMail == null) {
			LOGGER.log(
					Level.SEVERE,
					String.format(
							"No se ha podido realizar el enviacute del mensaje", //$NON-NLS-1$
							PARAN_MAIL_SENDER));
		}
		return sendMail;
	}
	/**
	 * Recupera el host para establecer la conexi&oacute;n.
	 * @return Host de conexi&oacute;n.
	 *
	 */
	public static String getMailHost() {

		String hostConecction;
		try {
			hostConecction = getProperty(PARAM_MAIL_HOST);
		}
		catch (final Exception e) {
			LOGGER.severe(String.format("Error al establecer la conexi&oacute;n.", PARAM_MAIL_HOST)); //$NON-NLS-1$
			return null;
		}

		if (hostConecction == null) {
			LOGGER.log(
					Level.SEVERE,
					String.format(
							"No se ha podido establecer la conexi&oacute;n", //$NON-NLS-1$
							PARAM_MAIL_HOST));
		}
		return hostConecction;
	}

	/**
	 * Recupera el puerto de conexi&oacute;n.
	 * @return Puerto de conexi&oacute;n
	 *
	 */
	public static int getMailPort() {

		int portConecction;
		try {
			final String portText = getProperty(PARAM_MAIL_PORT);
			portConecction = Integer.parseInt(portText);
		}
		catch (final Exception e) {
			LOGGER.severe(
					String.format(
							"No se ha configurado un puerto correcto en la propiedad '%1s' del fichero de configuracion", //$NON-NLS-1$
							PARAM_MAIL_PORT));
			return DEFAULT_MAIL_PORT;
		}
		return portConecction;
	}

	/**
	 * Recupera el nombre del usuario de la base de datos.
	 * @return Nombre del usuario logeado
	 *
	 */
	public static String getMailUsername() {

		String mailUsername;
		try {
			mailUsername = getProperty(PARAM_MAIL_SMTP_USERNAME);
		}
		catch (final Exception e) {
			LOGGER.severe(String.format("Error al recuperar el usuario", PARAM_MAIL_SMTP_USERNAME)); //$NON-NLS-1$
			return null;
		}

		if (mailUsername == null) {
			LOGGER.log(
					Level.SEVERE,
					String.format(
							"No se ha encontrado el usuario en la base de datos", //$NON-NLS-1$
							PARAM_MAIL_SMTP_USERNAME));
		}
		return mailUsername;
	}

	/**
	 * Recupera el password del usuario el cual va a enviar el mensaje.
	 * @return Password para realizar el env&iacute;o
	 *
	 */
	public static String getMailPassword() {

		String mailPassword;
		try {
			mailPassword = getProperty(PARAM_MAIL_SMTP_PASSWORD);
		}
		catch (final Exception e) {
			LOGGER.severe(String.format("Error al descifrar el password", PARAM_MAIL_SMTP_PASSWORD)); //$NON-NLS-1$
			return null;
		}

		if (mailPassword == null) {
			LOGGER.log(
					Level.SEVERE,
					String.format(
							"No se ha encontrado el password", //$NON-NLS-1$
							PARAM_MAIL_SMTP_PASSWORD));
		}
		return mailPassword;
	}

	/**
	 * Comienzo de la conexi&oacute;n la cual va a realizar el env&iacute;o del mensaje.
	 * @return Comienzo a la conexi&oacute;n
	 *
	 */
	public static String getMailStarttls() {

		String mailPassword;
		try {
			mailPassword = getProperty(PARAM_MAIL_STARTTLS);
		}
		catch (final Exception e) {
			LOGGER.severe(String.format("Error al comenzar la conexi&oacute;n", PARAM_MAIL_STARTTLS)); //$NON-NLS-1$
			return null;
		}

		if (mailPassword == null) {
			LOGGER.log(
					Level.SEVERE,
					String.format(
							"No se ha podido comenzar la conexi&oacute;n", //$NON-NLS-1$
							PARAM_MAIL_STARTTLS));
		}
		return mailPassword;
	}

	/**
	 * Autentificaci&nacute;n a la conexi&oacute;n el cual va a enviar el mensaje.
	 * @return autentificaci&nacute;n a la conexi&oacute;n para enviar mail
	 *
	 */
	public static String getMailAuth() {

		String mailAuth;
		try {
			mailAuth = getProperty(PARAM_MAIL_AUTH);
		}
		catch (final Exception e) {
			LOGGER.severe(String.format("Error al autentificarse", PARAM_MAIL_AUTH)); //$NON-NLS-1$
			return null;
		}

		if (mailAuth == null) {
			LOGGER.log(
					Level.SEVERE,
					String.format(
							"No se ha podidom realizar la autentificaci&oacute;n", //$NON-NLS-1$
							PARAM_MAIL_AUTH));
		}
		return mailAuth;
	}


	/**
	 * Recupera el emisor el cual va a enviar el mensaje.
	 * @return emisor del mensaje
	 *
	 */
	public static String getMailFrom() {

		String mailFrom;
		try {
			mailFrom = getProperty(PARAM_MAIL_FROM);
		}
		catch (final Exception e) {
			LOGGER.severe(String.format("Error al recuperar al emisor del mensaje", PARAM_MAIL_FROM)); //$NON-NLS-1$
			return null;
		}

		if (mailFrom == null) {
			LOGGER.log(
					Level.SEVERE,
					String.format(
							"No se ha encontrado al emisor del mensaje", //$NON-NLS-1$
							PARAM_MAIL_FROM));
		}
		return mailFrom;
	}


	/**
	 * Recupera el nombre de usuario emisor el cual va a enviar el mensaje.
	 * @return nombre del emisor del mensaje
	 *
	 */
	public static String getMailFromName() {

		String mailFromName;
		try {
			mailFromName = getProperty(PARAM_MAIL_FROMNAME);
		}
		catch (final Exception e) {
			LOGGER.severe(String.format("Error al recuperar el nombre del emisor del mensaje", PARAM_MAIL_FROMNAME)); //$NON-NLS-1$
			return null;
		}

		if (mailFromName == null) {
			LOGGER.log(
					Level.SEVERE,
					String.format(
							"Error al recuperar el nombre del usuario emisor", //$NON-NLS-1$
							PARAM_MAIL_FROMNAME));
		}
		return mailFromName;
	}

	/**
	 * Establece un tiempo de 30 minutos que es el que tiene el receptor para abrir el mensaje de lo contrario expirara.
	 * @return expira el mensaje
	 *
	 */
	public static long getExpiration() {

		long mailExpired;
		try {
			mailExpired = Long.parseLong(getProperty(PARAM_PASSWORD_EXPIRATION));
		}
		catch (final Exception e) {
			LOGGER.severe(String.format("No se ha podido expirar el mensaje", PARAM_PASSWORD_EXPIRATION)); //$NON-NLS-1$
			return DEFAULT_EXPIRED_TIME;
		}

		return mailExpired;
	}

}
