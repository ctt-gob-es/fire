package es.gob.fire.web.clave.sp;

import javax.annotation.Nonnull;

import eu.eidas.auth.engine.ProtocolEngineNoMetadataI;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;

/**
 * SpProtocolEngineI
 */
public interface SpProtocolEngineI extends ProtocolEngineNoMetadataI {

    @Nonnull
    byte[] checkAndDecryptResponse(@Nonnull byte[] responseBytes) throws EIDASSAMLEngineException;
}
