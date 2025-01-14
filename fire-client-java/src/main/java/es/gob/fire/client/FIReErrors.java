package es.gob.fire.client;

/**
 * Errores que pueden darse en FIRe.
 * @author carlos.gamuci
 */
public class FIReErrors {

	/** Error en la lectura de los par&aacute;metros de entrada. */
	public static final int READING_PARAMETERS = 1;
	/** No se ha indicado el identificador de la aplicaci&oacute;n. */
	public static final int PARAMETER_APP_ID_NEEDED = 2;
	/** No se ha indicado la operaci&oacute;n a realizar. */
	public static final int PARAMETER_OPERATION_NEEDED = 3;
	/** Se ha indicado un id de operaci&oacute;n no soportado. */
	public static final int PARAMETER_OPERATION_NOT_SUPPORTED = 5;
	/** No se ha indicado el certificado de autenticaci&oacute;n. */
	public static final int PARAMETER_AUTHENTICATION_CERTIFICATE_NEEDED = 6;
	/** Se ha indicado un certificado de autenticaci&oacute;n mal formado. */
	public static final int PARAMETER_AUTHENTICATION_CERTIFICATE_INVALID = 7;
	/** No se ha indicado el identificador de usuario. */
	public static final int PARAMETER_USER_ID_NEEDED = 8;
	/** No se ha indicado el algoritmo de firma. */
	public static final int PARAMETER_SIGNATURE_ALGORITHM_NEEDED = 9;
	/** No se ha indicado la operaci&oacute;n de firma. */
	public static final int PARAMETER_SIGNATURE_OPERATION_NEEDED = 10;
	/** No se ha indicado el formato de firma. */
	public static final int PARAMETER_SIGNATURE_FORMAT_NEEDED = 11;
	/** No se han indicado los datos que firmar. */
	public static final int PARAMETER_DATA_TO_SIGN_NEEDED = 12;
	/** Se han indicado datos a firmar mal codificados. */
	public static final int PARAMETER_DATA_TO_SIGN_INVALID = 13;
	/** No se han encontrado los datos a firmar. */
	public static final int PARAMETER_DATA_TO_SIGN_NOT_FOUND = 14;
	/** No se ha indicado la configuraci&oacute;n de transacci&oacute;n. */
	public static final int PARAMETER_CONFIG_TRANSACTION_NEEDED = 15;
	/** Se ha indicado una configuraci&oacute;n de transacci&oacute;n mal formada. */
	public static final int PARAMETER_CONFIG_TRANSACTION_INVALID = 16;
	/** No se ha indicado la URL de redirecci&oacute;n en caso de error en la configuraci&oacute;n de transacci&oacute;n. */
	public static final int PARAMETER_URL_ERROR_REDIRECION_NEEDED = 17;
	/** No se ha indicado el identificador de transacci&oacute;n. */
	public static final int PARAMETER_TRANSACTION_ID_NEEDED = 18;
	/** Se han indicado propiedades de configuraci&oacute;n de fima mal formadas. */
	public static final int PARAMETER_SIGNATURE_PARAMS_INVALID = 20;
	/** El proveedor no tiene dado de alta al usuario indicado. */
	public static final int UNKNOWN_USER = 21;
	/** El usuario ya dispone de un certificado del tipo que se est&aacute; solicitando generar. */
	public static final int CERTIFICATE_DUPLICATED = 22;
	/** Error al obtener los certificados del usuario o al generar uno nuevo. */
	public static final int CERTIFICATE_ERROR = 23;
	/** El usuario no puede poseer certificados de firma por haber realizado un registro no fehaciente. */
	public static final int CERTIFICATE_WEAK_REGISTRY = 24;
	/** Error desconocido durante la operaci&oacute;n. */
	public static final int UNDEFINED_ERROR = 25;
	/** Error durante la firma. */
	public static final int SIGNING = 26;
	/** No se seleccion&oacute; un proveedor de firma. */
	public static final int PROVIDER_NOT_SELECTED = 27;
	/** La firma generada no es v&aacute;lida. */
	public static final int INVALID_SIGNATURE = 31;
	/** Error durante la actualizaci&oacute;n de firma. */
	public static final int UPGRADING_SIGNATURE = 32;
	/** No se ha indicado el identificador de los datos as&iacute;ncronos. */
	public static final int PARAMETER_ASYNC_ID_NEEDED = 34;
	/** Gestor de documentos no v&aacute;lido. */
	public static final int PARAMETER_DOCUMENT_MANAGER_INVALID = 35;
	/** Los certificados del usuario est&aacute;n bloqueados. */
	public static final int CERTIFICATE_BLOCKED = 38;
	/** El usuario no dispone de certificados y el proveedor no le permite generarlos en este momento. */
	public static final int CERTIFICATE_NO_CERTS = 39;
	/** El identificador de documento ya existe en el lote. */
	public static final int BATCH_DUPLICATE_DOCUMENT = 42;
	/** Se ha excedido el n&uacute;mero m&aacute;ximo de documentos permitidos en el lote. */
	public static final int BATCH_NUM_DOCUMENTS_EXCEEDED = 43;
	/** Se intenta firmar un lote sin documentos. */
	public static final int BATCH_NO_DOCUMENTS = 44;
	/** No se ha indicado el identificador del documento del lote. */
	public static final int PARAMETER_DOCUMENT_ID_NEEDED = 48;
	/** No se ha firmado previamente el lote. */
	public static final int BATCH_NO_SIGNED = 49;
	/** Error al firmar el lote. */
	public static final int BATCH_SIGNING = 50;
	/** La firma se recuper&oacute;n anteriormente. */
	public static final int BATCH_RECOVERED = 51;
	/** Se requiere esperar un periodo de gracia para recuperar el documento. */
	public static final int BATCH_DOCUMENT_GRACE_PERIOD = 52;
	/** El documento no estaba en el lote. */
	public static final int BATCH_INVALID_DOCUMENT = 53;
	/** El resultado del lote se recuper&oacute; anteriormente. */
	public static final int BATCH_RESULT_RECOVERED = 54;

	/** El contenido de la petici&oacute;n era demasiado grande. */
	public static final int TOO_LARGE_CONTENT = 413;
	/** Error interno del servidor. */
	public static final int INTERNAL_ERROR = 500;
	/** Petici&oacute;n rechazada. */
	public static final int FORBIDDEN = 501;
	/** No se proporcionaron los par&aacute;metros de autenticaci&oacute;n o no son correctos. */
	public static final int UNAUTHORIZED = 502;
	/** La transacci&oacute;n no se ha inicializado o ha caducado. */
	public static final int INVALID_TRANSACTION = 503;
	/** Error detectado despu&eacute;s de llamar a la pasarela externa para autenticar al usuario. */
	public static final int EXTERNAL_SERVICE_ERROR_TO_LOGIN = 504;
	/** Error detectado despu&eacute;s de llamar a la pasarela externa para firmar. */
	public static final int EXTERNAL_SERVICE_ERROR_TO_SIGN = 505;
	/** Operaci&oacute;n cancelada. */
	public static final int OPERATION_CANCELED = 507;
	/** El proveedor de firma devolvi&oacute; un error. */
	public static final int PROVIDER_ERROR = 508;
	/** No se pudo conectar con el proveedor de firma. */
	public static final int PROVIDER_INACCESIBLE_SERVICE = 510;

}

