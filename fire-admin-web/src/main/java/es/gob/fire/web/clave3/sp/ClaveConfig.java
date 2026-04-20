package es.gob.fire.web.clave3.sp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.gob.fire.commons.utils.UtilsServer;

/**
 * Congiguraci&oacute;n para el uso de Cl@ve.
 */
public class ClaveConfig {

	private static final String PROPERTY_SERVICE_URL = "service.url"; //$NON-NLS-1$
	private static final String PROPERTY_SP_PROVIDER_NAME = "sp.providername"; //$NON-NLS-1$
	private static final String PROPERTY_SP_APPLICATION = "sp.applicationname"; //$NON-NLS-1$
	private static final String PROPERTY_SP_RETURN_URL = "sp.returnurl"; //$NON-NLS-1$
	private static final String PROPERTY_EIDAS_LEVELOFASSURANCE = "eidas.levelofassurance"; //$NON-NLS-1$

	private static final String PROPERTY_SERVICE_KEYSTORE_PATH = "service.keystore.path"; //$NON-NLS-1$
	private static final String PROPERTY_SERVICE_KEYSTORE_TYPE = "service.keystore.type"; //$NON-NLS-1$
	private static final String PROPERTY_SERVICE_KEYSTORE_PASSWORD = "service.keystore.password"; //$NON-NLS-1$
	private static final String PROPERTY_SERVICE_KEY_ALIAS = "service.key.alias"; //$NON-NLS-1$
	private static final String PROPERTY_SERVICE_KEY_PASSWORD = "service.key.password"; //$NON-NLS-1$

	/** Nombre del fichero de configuraci&oacute;n. */
	private static final String CONFIG_PROPERTIES_FILENAME = "clave_config.properties"; //$NON-NLS-1$

    /** Logger for this class. */
    private static final Logger LOGGER = LogManager.getLogger(ClaveConfig.class);

    private static Properties fileConfig = null;

    private final Properties config;

	/** Constructor para evitar la instanciaci&oacute;n. */
    public ClaveConfig(final Properties config) {
		this.config = new Properties();
    	if (config != null) {
    		this.config.putAll(config);
    	}
    }

    public String getServiceUrl() {
    	return this.config.getProperty(PROPERTY_SERVICE_URL);
    }

    public String getProviderName() {
    	return this.config.getProperty(PROPERTY_SP_PROVIDER_NAME);
    }

    public String getApplicationName() {
    	return this.config.getProperty(PROPERTY_SP_APPLICATION);
    }

    public String getReturnUrl() {
    	return this.config.getProperty(PROPERTY_SP_RETURN_URL);
    }

    public String getEidasAsseguranceLevel() {
    	return this.config.getProperty(PROPERTY_EIDAS_LEVELOFASSURANCE);
    }


    public String getKeyStorePath() {
    	return this.config.getProperty(PROPERTY_SERVICE_KEYSTORE_PATH);
    }

    public String getKeyStoreType() {
    	return this.config.getProperty(PROPERTY_SERVICE_KEYSTORE_TYPE);
    }

    public String getKeyStorePass() {
    	return this.config.getProperty(PROPERTY_SERVICE_KEYSTORE_PASSWORD, ""); //$NON-NLS-1$
    }

    public String getKeyAlias() {
    	return this.config.getProperty(PROPERTY_SERVICE_KEY_ALIAS);
    }

    public String getKeyPass() {
    	return this.config.getProperty(PROPERTY_SERVICE_KEY_PASSWORD);
    }

    /**
     * Carga la configuraci&oacute;n del fichero de configuracion de Clave.
     * @return Configuraci&oacute;n de Clave.
     */
    public static ClaveConfig loadConfigFromFile() {

    	if (fileConfig == null) {
    		fileConfig = loadConfig();
    	}

    	return new ClaveConfig(fileConfig);
    }

    /**
     * Loads configurations from a specified file.
     *
     * @param fileName The name of the configuration file.
     * @return A Properties object containing the configurations.
     */
    private static Properties loadConfig() {

    	final Properties properties = new Properties();

    	// Tratamos de cargar el fichero de configuracion del directorio de configuracion
    	final String configPath = ClaveConfig.getConfigFilePath();
    	final File f = new File(configPath);
    	if (f.isFile()) {
    		try (InputStream is = new FileInputStream(f);
    				InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8)) {
    			properties.load(isr);
    		} catch (final IOException e) {
                LOGGER.error("No se ha podido cargar el fichero de configuracion de Cl@ve: " + CONFIG_PROPERTIES_FILENAME, e); //$NON-NLS-1$
            }
    	}

        return properties;
    }

    /**
     * Retrieves the configuration file path.
     * @return The configuration file path.
     */
    public static String getConfigFilePath() {

    	String configDir = UtilsServer.getServerConfigDir();

    	// Si no existe la ruta o el directorio de Clave en ella, se intenta buscar en la ruta del ejecutable
    	if (configDir == null || !new File(configDir, CONFIG_PROPERTIES_FILENAME).isFile()) {
			try {
				final String alternativeDir = ClaveConfig.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
				if (new File(alternativeDir, CONFIG_PROPERTIES_FILENAME).isFile()) {
					configDir = alternativeDir;
				}
			} catch (final Exception e) {
				LOGGER.warn("No se pudo buscar la configuracion de Clave en el directorio del proyecto", e); //$NON-NLS-1$
			}
    	}

    	return UtilsServer.createAbsolutePath(configDir, CONFIG_PROPERTIES_FILENAME);
    }

}
