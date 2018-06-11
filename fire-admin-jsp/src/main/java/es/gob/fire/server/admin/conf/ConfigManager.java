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

class ConfigManager {

	private static final Logger LOGGER = Logger.getLogger(ConfigManager.class.getName());

	private static final String PARAM_DB_DRIVER = "bbdd.driver"; //$NON-NLS-1$
	private static final String PARAM_DB_CONN = "bbdd.conn"; //$NON-NLS-1$
	private static final String PARAM_CIPHER_CLASS = "cipher.class"; //$NON-NLS-1$
	private static final String CONFIG_FILE = "admin_config.properties";//$NON-NLS-1$

	private static final String PREFIX_CIPHERED_TEXT = "{@ciphered:"; //$NON-NLS-1$
	private static final String SUFIX_CIPHERED_TEXT = "}"; //$NON-NLS-1$

	/** Variable de entorno que determina el directorio en el que buscar el fichero de configuraci&oacute;n. */
	private static final String ENVIRONMENT_VAR_CONFIG_DIR = "fire.config.path"; //$NON-NLS-1$

	/** Variable de entorno antigua que determinaba el directorio en el que buscar el fichero
	 * de configuraci&oacute;n. Se utiliza si no se ha establecido la nueva variable. */
	private static final String ENVIRONMENT_VAR_CONFIG_DIR_OLD = "clavefirma.config.path"; //$NON-NLS-1$

	private static Properties config = null;

	private static PropertyDecipher decipherImpl = null;


	/**
	 * Carga la configuraci&oacute;n del m&oacute;dulo.
	 * @throws AdminFilesNotFoundException Cuando no se encuentra o no se puede cargar el fichero de configuraci&oacute;n.
	 */
	static void initialize() throws AdminFilesNotFoundException {

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

		config = dbConfig;
	}

	/**
	 * Recupera la cadena del controlador JDBC configurado.
	 * @return Cadena del controlador JDBC o {@code null}
	 * si no se ha encontrado o si no se ha podido obetener.
	 */
	static String getDbDriverString() {

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
	static String getDbConnectionString() {

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
}
