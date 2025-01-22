package es.gob.fire.web.clave.sp;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.engine.ProtocolEngineNoMetadata;
import eu.eidas.auth.engine.configuration.ProtocolConfigurationAccessorNoMetadata;
import eu.eidas.auth.engine.xml.opensaml.CorrelatedResponse;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;

/**
 * SpProtocolEngine
 */
public final class SpProtocolEngine extends ProtocolEngineNoMetadata implements SpProtocolEngineI {

    private static final Logger LOG = LoggerFactory.getLogger(SpProtocolEngine.class);

    public SpProtocolEngine(@Nonnull ProtocolConfigurationAccessorNoMetadata configurationAccessor) {
        super(configurationAccessor);
    }

    /**
     * Decrypt and validate saml respons
     *
     * @param responseBytes
     * @return
     * @throws EIDASSAMLEngineException
     */
    @Override
    @Nonnull
    public byte[] checkAndDecryptResponse(@Nonnull byte[] responseBytes) throws EIDASSAMLEngineException {
        // This decrypts the given responseBytes:
        CorrelatedResponse response = (CorrelatedResponse) unmarshallResponse(responseBytes);

        // validateUnmarshalledResponse(samlResponse, userIP, skewTimeInMillis);

        try {
            // re-transform the decrypted bytes to another byte array, without signing:
            return marshall(response.getResponse());
        } catch (EIDASSAMLEngineException e) {
            LOG.debug(SAML_EXCHANGE, "BUSINESS EXCEPTION : checkAndResignEIDASTokenSAML : Sign and Marshall.", e);
            LOG.info(SAML_EXCHANGE, "BUSINESS EXCEPTION : checkAndResignEIDASTokenSAML : Sign and Marshall.",
                     e.getMessage());
            throw new EIDASSAMLEngineException(EidasErrorKey.INTERNAL_ERROR.errorCode(),
                                               EidasErrorKey.INTERNAL_ERROR.errorMessage(), e);
        }
    }
}
