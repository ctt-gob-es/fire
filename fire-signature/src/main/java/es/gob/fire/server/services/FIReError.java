package es.gob.fire.server.services;

/**
 * Errores que pueden darse en FIRe.
 * @author carlos.gamuci
 */
public enum FIReError {

	// Errores de la peticion que devuelven informacion significativa

	READING_PARAMETERS(1, 400, "Error en la lectura de los par\u00E1metros de entrada."), //$NON-NLS-1$
	PARAMETER_APP_ID_NEEDED(2, 400, "No se ha indicado el identificador de la aplicaci\u00F3n."), //$NON-NLS-1$
	PARAMETER_OPERATION_NEEDED(3, 400, "No se ha indicado la operaci\u00F3n a realizar."), //$NON-NLS-1$
	PARAMETER_OPERATION_NOT_SUPPORTED(5, 400, "Se ha indicado un id de operaci\u00F3n no soportado."), //$NON-NLS-1$
	PARAMETER_AUTHENTICATION_CERTIFICATE_NEEDED(6, 400, "No se ha indicado el certificado de autenticaci\u00F3n."), //$NON-NLS-1$
	PARAMETER_AUTHENTICATION_CERTIFICATE_INVALID(7, 400, "Se ha indicado un certificado de autenticaci\u00F3n mal formado."), //$NON-NLS-1$
	PARAMETER_USER_ID_NEEDED(8, 400, "No se ha indicado el identificador de usuario."), //$NON-NLS-1$
	PARAMETER_SIGNATURE_ALGORITHM_NEEDED(9, 400, "No se ha indicado el algoritmo de firma."), //$NON-NLS-1$
	PARAMETER_SIGNATURE_OPERATION_NEEDED(10, 400, "No se ha indicado la operaci\u00F3n de firma."), //$NON-NLS-1$
	PARAMETER_SIGNATURE_FORMAT_NEEDED(11, 400, "No se ha indicado el formato de firma."), //$NON-NLS-1$
	PARAMETER_DATA_TO_SIGN_NEEDED(12, 400, "No se han indicado los datos que firmar."), //$NON-NLS-1$
	PARAMETER_DATA_TO_SIGN_INVALID(13, 400, "Se han indicado datos a firmar mal codificados."), //$NON-NLS-1$
	PARAMETER_DATA_TO_SIGN_NOT_FOUND(14, 404, "No se han encontrado los datos a firmar."), //$NON-NLS-1$
	PARAMETER_CONFIG_TRANSACTION_NEEDED(15, 400, "No se ha indicado la configuraci\u00F3n de transacci\u00F3n."), //$NON-NLS-1$
	PARAMETER_CONFIG_TRANSACTION_INVALID(16, 400, "Se ha indicado una configuraci\u00F3n de transacci\u00F3n mal formada."), //$NON-NLS-1$
	PARAMETER_URL_ERROR_REDIRECION_NEEDED(17, 400, "No se ha indicado en la configuraci\u00F3n de la transacci\u00F3n la URL de redirecci\u00F3n en caso de error."), //$NON-NLS-1$
	PARAMETER_TRANSACTION_ID_NEEDED(18, 400, "No se ha indicado el identificador de transacci\u00F3n"), //$NON-NLS-1$
	PARAMETER_SIGNATURE_PARAMS_INVALID(20, 400, "Se han indicado propiedades de configuraci\u00F3n de fima mal formadas."), //$NON-NLS-1$
	UNKNOWN_USER(21, 400, "El proveedor no tiene dado de alta al usuario indicado."), //$NON-NLS-1$
	CERTIFICATE_DUPLICATED(22, 500, "El usuario ya dispone de un certificado del tipo que se est\u00E1 solicitando generar."), //$NON-NLS-1$
	CERTIFICATE_ERROR(23, 500, "Error al obtener los certificados del usuario o al generar uno nuevo."), //$NON-NLS-1$
	CERTIFICATE_WEAK_REGISTRY(24, 500, "El usuario no puede poseer certificados de firma por haber realizado un registro no fehaciente."), //$NON-NLS-1$
	UNDEFINED_ERROR(25, 500, "Error desconocido durante la operaci\u00F3n."), //$NON-NLS-1$
	SIGNING(26, 500, "Error durante la firma."), //$NON-NLS-1$
	PROVIDER_NOT_SELECTED(27, 400, "No se seleccion\u00F3 un proveedor de firma."), //$NON-NLS-1$
	INVALID_SIGNATURE(31, 500, "La firma generada no es v\u00E1lida."), //$NON-NLS-1$
	UPGRADING_SIGNATURE(32, 500, "Error durante la actualizaci\u00F3n de firma."), //$NON-NLS-1$
	PARAMETER_ASYNC_ID_NEEDED(34, 400, "No se ha indicado el identificador de los datos as\u00EDncronos."), //$NON-NLS-1$
	PARAMETER_DOCUMENT_MANAGER_INVALID(35, 400, "Gestor de documentos no v\u00E1lido."), //$NON-NLS-1$
	CERTIFICATE_BLOCKED(38, 500, "Los certificados del usuario est\u00E1n bloqueados."), //$NON-NLS-1$
	CERTIFICATE_NO_CERTS(39, 500, "El usuario no dispone de certificados y el proveedor no le permite generarlos en este momento."), //$NON-NLS-1$
	BATCH_DUPLICATE_DOCUMENT(42, 400, "El identificador de documento ya existe en el lote."), //$NON-NLS-1$
	BATCH_NUM_DOCUMENTS_EXCEEDED(43, 400, "Se ha excedido el n\u00FAmero m\u00E1ximo de documentos permitidos en el lote."), //$NON-NLS-1$
	BATCH_NO_DOCUMENTS(44, 400, "Se intenta firmar un lote sin documentos"), //$NON-NLS-1$
	PARAMETER_DOCUMENT_ID_NEEDED(48, 400, "No se ha indicado el identificador del documento del lote."), //$NON-NLS-1$
	BATCH_NO_SIGNED(49, 500, "No se ha firmado previamente el lote."), //$NON-NLS-1$
	BATCH_SIGNING(50, 500, "Error al firmar el lote."), //$NON-NLS-1$
	BATCH_RECOVERED(51, 500, "La firma se recuper\u00F3 anteriormente."), //$NON-NLS-1$
	BATCH_DOCUMENT_GRACE_PERIOD(52, 500, "Se requiere esperar un periodo de gracia para recuperar el documento."), //$NON-NLS-1$
	BATCH_INVALID_DOCUMENT(53, 500, "El documento no estaba en el lote."), //$NON-NLS-1$
	BATCH_RESULT_RECOVERED(54, 500, "El resultado del lote se recuper\u00F3 anteriormente."), //$NON-NLS-1$
	PARAMETER_PROVIDERS_INVALID(55, 400, "El listado de proveedores indicado no permite seleccionar un proveedor v\u00E1lido."), //$NON-NLS-1$
	PARAMETER_DOCUMENT_ID_INVALID(56, 400, "Se ha indicado un identificador de documento con longitud o caracteres no soportados"), //$NON-NLS-1$


	// Errores que no devuelven informacion significativa a la aplicacion
	INTERNAL_ERROR(500, 500, "Error interno del servidor. Espere unos momentos antes de reintentar la operaci\u00F3n."), //$NON-NLS-1$
	FORBIDDEN(501, 403, "Petici\u00F3n rechazada."), //$NON-NLS-1$
	UNAUTHORIZED(502, 401, "No se proporcionaron los par\u00E1metros de autenticaci\u00F3n o no son correctos."), //$NON-NLS-1$
	INVALID_TRANSACTION(503, 403, "La transacci\u00F3n no se ha inicializado o ha caducado."), //$NON-NLS-1$
	EXTERNAL_SERVICE_ERROR_TO_LOGIN(504, 500, "Error detectado despues de llamar a la pasarela externa para autenticar al usuario."), //$NON-NLS-1$
	EXTERNAL_SERVICE_ERROR_TO_SIGN(505, 500, "Error detectado despues de llamar a la pasarela externa para firmar."), //$NON-NLS-1$
	EXTERNAL_SERVICE_ERROR(506, 500, "Error detectado despues de llamar a la pasarela externa."), //$NON-NLS-1$
	OPERATION_CANCELED(507, 400, "Operaci\u00F3n cancelada."), //$NON-NLS-1$
	PROVIDER_ERROR(508, 500, "El proveedor de firma devolvi\u00F3 un error."), //$NON-NLS-1$
	PROVIDER_INACCESIBLE_SERVICE(510, 500, "No se pudo conectar con el proveedor de firma."), //$NON-NLS-1$
	;


	int code;
	int httpStatus;
	String message;

	FIReError(final int code, final int httpStatus, final String message) {
		this.code = code;
		this.httpStatus = httpStatus;
		this.message = message;
	}

	/**
	 * Recupera el c&oacute;digo del error.
	 * @return C&oacute;digo de error.
	 */
	public int getCode() {
		return this.code;
	}

	/**
	 * Recupera el c&oacute;digo de estado HTTP.
	 * @return C&oacute;digo de estado HTTP.
	 */
	public int getHttpStatus() {
		return this.httpStatus;
	}

	/**
	 * Recupera el mensaje de error.
	 * @return Mensaje de error.
	 */
	public String getMessage() {
		return this.message;
	}

}

