package es.gob.fire.server.connector;

import java.nio.charset.Charset;

/**
 * Resultado de una operaci&oacute;n.
 * @author carlos.gamuci
 */
public abstract class OperationResult {

	/**
	 * Codifica el resultado de la operaci&oacute;n.
	 */
	public abstract byte[] encodeResult(Charset charset);
}
