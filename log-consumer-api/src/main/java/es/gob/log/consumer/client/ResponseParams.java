package es.gob.log.consumer.client;

/**
 * Valores que pueden recibirse en las estructuras de respuesta del servidor.
 */
class ResponseParams {

	/** Token de inicio de sesi&oacute;n. */
	public static final String PARAM_TOKEN = "tkn"; //$NON-NLS-1$
	/** Salto que aplicar al cifrado del token de sesi&oacute;n. */
	public static final String PARAM_IV = "iv"; //$NON-NLS-1$
	/** Resultado de la operaci&oacute;n de login. */
	public static final String PARAM_RESULT = "ok"; //$NON-NLS-1$
}
