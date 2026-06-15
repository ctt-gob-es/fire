package es.gob.fire.server.services;

import es.gob.afirma.core.ErrorCode;

public class TriServiceErrorCode {
	
	public static class Request {

		public static final ErrorCode CRYPTO_OPERATION_NOT_FOUND			= new ErrorCode("600115", "No se ha recibido el identificador de operacion criptografica para la operacion de firma"); //$NON-NLS-1$ //$NON-NLS-2$

	}

}
