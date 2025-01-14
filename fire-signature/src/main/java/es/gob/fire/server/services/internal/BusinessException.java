package es.gob.fire.server.services.internal;

import es.gob.fire.server.services.FIReError;

/**
 * Excepci&oacute;n interna de operaci&oacute;n que se&ntilde;ala el error
 * concreto de FIRe que se ha producido.
 */
class BusinessException extends Exception {

	/** Serial Id. */
	private static final long serialVersionUID = -8310529118039752152L;
	private final FIReError fireError;

	public BusinessException(final FIReError fireError) {
		super();
		this.fireError = fireError;
	}

	public BusinessException(final FIReError fireError, final Throwable cause) {
		super(cause);
		this.fireError = fireError;
	}

	public FIReError getFireError() {
		return this.fireError;
	}
}
