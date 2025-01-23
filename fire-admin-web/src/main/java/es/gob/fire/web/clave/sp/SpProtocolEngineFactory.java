package es.gob.fire.web.clave.sp;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.gob.fire.web.clave.sp.utils.Constants;
import es.gob.fire.web.clave.sp.utils.ClaveConfigUtil;
import eu.eidas.auth.engine.ProtocolEngineFactoryNoMetadata;
import eu.eidas.auth.engine.ProtocolEngineNoMetadataI;
import eu.eidas.auth.engine.configuration.ProtocolConfigurationAccessorNoMetadata;
import eu.eidas.auth.engine.configuration.ProtocolEngineConfigurationException;
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

        private static final SpProtocolEngineFactory DEFAULT_SAML_ENGINE_FACTORY;

        private static final Exception INITIALIZATION_EXCEPTION;

        static {
            Exception initializationException = null;
            SpProtocolEngineFactory defaultProtocolEngineFactory = null;
            try {
                ProtocolEngineConfigurationFactoryNoMetadata protocolEngineConfigurationFactory = 
                		new ProtocolEngineConfigurationFactoryNoMetadata(Constants.SP_SAMLENGINE_FILE, null,
                				ClaveConfigUtil.getConfigFilePath());
                defaultProtocolEngineFactory =
                        new SpProtocolEngineFactory(protocolEngineConfigurationFactory);
            } catch (Exception ex) {
                initializationException = ex;
                LOG.error("Unable to instantiate default SAML engines: " + ex, ex);
            }
            DEFAULT_SAML_ENGINE_FACTORY = defaultProtocolEngineFactory;
            INITIALIZATION_EXCEPTION = initializationException;
        }

        static SpProtocolEngineFactory getDefaultSamlEngineFactory() {
            if (null == INITIALIZATION_EXCEPTION) {
                return DEFAULT_SAML_ENGINE_FACTORY;
            } else {
                throw new IllegalStateException(INITIALIZATION_EXCEPTION);
            }
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(SpProtocolEngineFactory.class);

    @Nonnull
    public static SpProtocolEngineFactory getInstance() {
        return LazyHolder.getDefaultSamlEngineFactory();
    }

    /**
     * Returns a default ProtocolEngine instance matching the given name retrieved from the configuration file.
     *
     * @param instanceName the instance name
     * @return the ProtocolEngine instance matching the given name retrieved from the configuration file
     */
    @Nullable
    public static SpProtocolEngineI getSpProtocolEngine(@Nonnull String instanceName) {
        Preconditions.checkNotBlank(instanceName, "instanceName");
        return (SpProtocolEngineI) getInstance().getProtocolEngine(instanceName);
    }

    private SpProtocolEngineFactory(@Nonnull ProtocolEngineConfigurationFactoryNoMetadata configurationFactory)
            throws ProtocolEngineConfigurationException {
        super(configurationFactory);
    }

    @Nonnull
    @Override
    protected ProtocolEngineNoMetadataI createProtocolEngine(@Nonnull ProtocolConfigurationAccessorNoMetadata configurationAccessor) {
        return new SpProtocolEngine(configurationAccessor);
    }
}
