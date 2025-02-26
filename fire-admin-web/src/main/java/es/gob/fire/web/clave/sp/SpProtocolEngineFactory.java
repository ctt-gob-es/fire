package es.gob.fire.web.clave.sp;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.eidas.auth.engine.ProtocolEngineFactoryNoMetadata;
import eu.eidas.auth.engine.ProtocolEngineNoMetadataI;
import eu.eidas.auth.engine.configuration.ProtocolConfigurationAccessorNoMetadata;
import eu.eidas.auth.engine.configuration.SamlEngineConfigurationException;
import eu.eidas.auth.engine.configuration.dom.ProtocolEngineConfigurationFactoryNoMetadata;
import eu.eidas.util.Preconditions;

/**
 * Sp ProtocolEngineFactory
 */
public final class SpProtocolEngineFactory extends ProtocolEngineFactoryNoMetadata {

	/**
     * Initialization-on-demand holder idiom.
     * <p/>
     * See item 71 of Effective Java 2nd Edition.
     * <p/>
     * See http://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom.
     */
    private static final class LazyHolder {

        private static SpProtocolEngineFactory DEFAULT_SAML_ENGINE_FACTORY;

        private static Exception INITIALIZATION_EXCEPTION;

        static SpProtocolEngineFactory getDefaultSamlEngineFactory(String configPath) {
        	
        	if (INITIALIZATION_EXCEPTION != null) {
        		throw new IllegalStateException(INITIALIZATION_EXCEPTION);
        	}
        	if (DEFAULT_SAML_ENGINE_FACTORY == null) {
        		try {
        			DEFAULT_SAML_ENGINE_FACTORY = initSamlEngineFactory(configPath);
        		}
        		catch (IllegalStateException e) {
        			INITIALIZATION_EXCEPTION = (Exception) e.getCause();
				}
        	}
        	return DEFAULT_SAML_ENGINE_FACTORY; 
        }
        
        private static SpProtocolEngineFactory initSamlEngineFactory(String configPath) {
            SpProtocolEngineFactory defaultProtocolEngineFactory = null;
            try {
                ProtocolEngineConfigurationFactoryNoMetadata protocolEngineConfigurationFactory = 
                		new ProtocolEngineConfigurationFactoryNoMetadata("SPSamlEngine.xml", null,
                				configPath);
                defaultProtocolEngineFactory =
                        new SpProtocolEngineFactory(protocolEngineConfigurationFactory);
            } catch (Exception ex) {
                LOG.error("Unable to instantiate default SAML engines: " + ex, ex);
                throw new IllegalStateException(ex);
            }
            return defaultProtocolEngineFactory; 
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(SpProtocolEngineFactory.class);

    @Nonnull
    public static SpProtocolEngineFactory getInstance(String configPath) {
        return LazyHolder.getDefaultSamlEngineFactory(configPath);
    }

    /**
     * Returns a default ProtocolEngine instance matching the given name retrieved from the configuration file.
     *
     * @param instanceName the instance name
     * @param configPath Configuraton path.
     * @return the ProtocolEngine instance matching the given name retrieved from the configuration file
     */
    @Nullable
    public static SpProtocolEngineI getSpProtocolEngine(@Nonnull String instanceName, String configPath) {
        Preconditions.checkNotBlank(instanceName, "instanceName");
        return (SpProtocolEngineI) getInstance(configPath).getProtocolEngine(instanceName);
    }

    private SpProtocolEngineFactory(@Nonnull ProtocolEngineConfigurationFactoryNoMetadata configurationFactory)
            throws SamlEngineConfigurationException {
        super(configurationFactory);
    }

    @Nonnull
    @Override
    protected ProtocolEngineNoMetadataI createProtocolEngine(@Nonnull ProtocolConfigurationAccessorNoMetadata configurationAccessor) {
        return new SpProtocolEngine(configurationAccessor);
    }
}