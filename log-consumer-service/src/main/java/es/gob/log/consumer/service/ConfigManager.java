package es.gob.log.consumer.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gestor de la configuraci&oacute;n del servicio.
 */
public class ConfigManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigManager.class);

	private static final String CONFIG_FILENAME = "logconsumer.properties"; //$NON-NLS-1$

	private static final String ENVIRONMENT_VAR_CONFIG_DIR = "logconsumer.config.path"; //$NON-NLS-1$

	private static final String PROP_AUTHORIZATION_KEY = "auth.key"; //$NON-NLS-1$

	private static final String PROP_LOGS_DIR = "logs.dir"; //$NON-NLS-1$

	private static final String PROP_LOG_REGISTER_CLASS = "logs.register.class"; //$NON-NLS-1$

	private static final String PROP_LOG_REGISTER_URL = "logs.register.url"; //$NON-NLS-1$

	private static ConfigManager instance = null;

	private Properties config;

	/**
	 * Obtiene una instancia de la clase que gestiona la configuraci&oacute;n.
	 * @return Instancia de la clase que gestiona la configuraci&oacute;n.
	 * @throws IOException Cuabdi bi se oyede cargar el fichero de configuraci&oacute;n.
	 */
	public static ConfigManager getInstance() throws IOException {
		if (instance == null) {
			instance = new ConfigManager();
			try {
				instance.load();
			}
			catch (final Exception e) {
				instance = null;
				throw new IOException("No se ha podido cargar el fichero de configuracion del servicio de consulta de logs", e); //$NON-NLS-1$
			}
		}
		return instance;
	}

	private ConfigManager() {
		// Evitamos que se creen instancias desde fuera de la clase
	}

	private void load() throws FileNotFoundException, IOException {
		this.config = loadConfigFile(CONFIG_FILENAME);
	}

	/**
	 * Carga un fichero de configuraci&oacute;n del directorio configurado
	 * o del classpath si no se configur&oacute;.
	 * @param configFilename Nombre del fichero de configuraci&oacute;n.
	 * @return Propiedades de fichero de configuraci&oacute:n.
	 * @throws IOException Cuando no se puede cargar el fichero de configuraci&oacute;n.
	 * @throws FileNotFoundException Cuando no se encuentra el fichero de configuraci&oacute;n.
	 */
	private static Properties loadConfigFile(final String configFilename)
			throws IOException, FileNotFoundException {

		LOGGER.info("Cargamos fichero de configuracion");

		boolean loaded = false;
		final Properties config = new Properties();
		try {
			final String configDir = System.getProperty(ENVIRONMENT_VAR_CONFIG_DIR);
			if (configDir != null) {
				final File configFile = new File(configDir, configFilename).getCanonicalFile();
				// Comprobamos que se trate de un fichero sobre el que tengamos permisos y que no
				// nos hayamos salido del directorio de configuracion indicado
				if (configFile.isFile() && configFile.canRead() &&
						configFile.getCanonicalPath().startsWith(new File(configDir).getCanonicalPath())) {

					try (InputStream is = new FileInputStream(configFile);
							InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);) {
						config.load(isr);
						loaded = true;
					}
				}
				else {
					LOGGER.warn(
							"El fichero " + configFilename + " no existe o no pudo leerse del directorio configurado en la variable " + //$NON-NLS-1$ //$NON-NLS-2$
									ENVIRONMENT_VAR_CONFIG_DIR + ". El fichero debe encontrase dentro del directorio '" + configDir + //$NON-NLS-1$
							"'.\nSe buscara en el CLASSPATH."); //$NON-NLS-1$
				}
			}

			// Cargamos el fichero desde el classpath si no se cargo de otro sitio
			if (!loaded) {
				try (InputStream is = ConfigManager.class.getResourceAsStream('/' + configFilename);
						InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);) {
					if (is == null) {
						throw new FileNotFoundException("No se encontro en ningun sitio el fichero de configuracion " + configFilename); //$NON-NLS-1$
					}
					config.load(isr);
				}
			}
		}
		catch(final FileNotFoundException e){
			throw e;
		}
		catch(final Exception e){
			throw new IOException("No se ha podido cargar el fichero de configuracion: " + configFilename, e); //$NON-NLS-1$
		}

		return config;
	}

	/**
	 * Obtiene la clave de cifrado configurada.
	 * @return Clave de cifrado.
	 */
	public byte[] getCipherKey() {

		final String encodedKey = this.config.getProperty(PROP_AUTHORIZATION_KEY);
		try {
			return Base64.decode(encodedKey);
		} catch (final IOException e) {
			LOGGER.error("La clave de autorizacion no esta correctamente codificada: " + e); //$NON-NLS-1$
			return null;
		}
	}

	/**
	 * Obtiene la clave de cifrado configurada.
	 * @return Clave de cifrado.
	 */
	public File getLogsDir() {
		final String directory = this.config.getProperty(PROP_LOGS_DIR);
		return directory != null ? new File(directory) : null;
	}

	/**
	 * Recupera la clase para la notificaci&oacute;n del estado del servicio.
	 * @return Clase encargada de notificar el estado del servicio
	 */
	public String getLogServiceRegisterClass() {
		return this.config.getProperty(PROP_LOG_REGISTER_CLASS);
	}

	/**
	 * URL a la que notificar el estado del servicio.
	 * @return URL del servicio al que notificar.
	 */
	public String getLogServiceRegisterUrl() {
		return this.config.getProperty(PROP_LOG_REGISTER_URL);
	}

	/**
	 * Recupera una copia de todas las propiedades configuradas.
	 * @return Propiedades configuradas en la aplicaci&oacute;n.
	 */
	public Properties getProperties() {
		return (Properties) this.config.clone();
	}
}
