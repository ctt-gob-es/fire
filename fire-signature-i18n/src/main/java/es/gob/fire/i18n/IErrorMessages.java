package es.gob.fire.i18n;

public interface IErrorMessages {

	/******************************/
	/** Mensajes de error **/
	/******************************/

	String READING_PARAMETERS = "reading.parameters";
	String PARAMETER_APP_ID_NEEDED = "parameter.app.id.needed";
	String PARAMETER_OPERATION_NEEDED = "parameter.op.needed";
	String PARAMETER_OPERATION_NOT_SUPPORTED = "parameter.op.not.supported";
	String PARAMETER_AUTHENTICATION_CERTIFICATE_NEEDED = "parameter.auth.cert.needed";
	String PARAMETER_AUTHENTICATION_CERTIFICATE_INVALID = "parameter.auth.cert.invalid";
	String PARAMETER_USER_ID_NEEDED = "parameter.user.id.needed";
	String PARAMETER_SIGNATURE_ALGORITHM_NEEDED = "parameter.signature.algorithm.needed";
	String PARAMETER_SIGNATURE_OPERATION_NEEDED = "parameter.signature.op.needed";
	String PARAMETER_SIGNATURE_FORMAT_NEEDED = "parameter.signature.format.needed";
	String PARAMETER_DATA_TO_SIGN_NEEDED = "parameter.data.sign.needed";
	String PARAMETER_DATA_TO_SIGN_INVALID = "parameter.data.sign.invalid";
	String PARAMETER_DATA_TO_SIGN_NOT_FOUND = "parameter.data.sign.not.found";
	String PARAMETER_CONFIG_TRANSACTION_NEEDED = "parameter.config.trans.needed";
	String PARAMETER_CONFIG_TRANSACTION_INVALID = "parameter.config.trans.invalid";
	String PARAMETER_URL_ERROR_REDIRECTION_NEEDED = "parameter.url.error.redirection.needed";
	String PARAMETER_TRANSACTION_ID_NEEDED = "parameter.trans.id.needed";
	String PARAMETER_SIGNATURE_PARAMS_INVALID = "parameter.signature.params.invalid";
	String UNKNOWN_USER = "unknown.user";
	String CERTIFICATE_DUPLICATED = "cert.duplicated";
	String CERTIFICATE_ERROR = "cert.error";
	String CERTIFICATE_WEAK_REGISTRY = "cert.weak.reg";
	String UNDEFINED_ERROR = "undefined.error";
	String SIGNING = "signing";
	String PROVIDER_NOT_SELECTED = "provider.not.selected";
	String INVALID_SIGNATURE = "invalid.signature";
	String UPGRADING_SIGNATURE = "upgrading.signature";
	String PARAMETER_ASYNC_ID_NEEDED = "parameter.async.id.needed";
	String PARAMETER_DOCUMENT_MANAGER_INVALID = "parameter.document.manager.invalid";
	String CERTIFICATE_BLOCKED = "cert.blocked";
	String CERTIFICATE_NO_CERTS = "cert.no.certs";
	String BATCH_DUPLICATE_DOCUMENT = "batch.duplicate.document";
	String BATCH_NUM_DOCUMENTS_EXCEEDED = "batch.num.documents.exceeded";
	String BATCH_NO_DOCUMENTS = "batch.no.documents";
	String PARAMETER_DOCUMENT_ID_NEEDED = "parameter.document.id.needed";
	String BATCH_NO_SIGNED = "batch.no.signed";
	String BATCH_SIGNING = "batch.signing";
	String BATCH_RECOVERED = "batch.recovered";
	String BATCH_DOCUMENT_GRACE_PERIOD = "batch.document.grace.period";
	String BATCH_INVALID_DOCUMENT = "batch.invalid.document";
	String BATCH_RESULT_RECOVERED = "batch.result.recovered";
	String PARAMETER_PROVIDERS_INVALID = "parameter.providers.invalid";
	String PARAMETER_DOCUMENT_ID_INVALID = "parameter.document.id.invalid";

	// Errores que no devuelven informacion significativa a la aplicacion
	String INTERNAL_ERROR = "internal.error";
	String FORBIDDEN = "forbidden";
	String UNAUTHORIZED = "unauthorized";
	String INVALID_TRANSACTION = "invalid.transaction";
	String EXTERNAL_SERVICE_ERROR_TO_LOGIN = "external.service.error.login";
	String EXTERNAL_SERVICE_ERROR_TO_SIGN = "external.service.error.sign";
	String EXTERNAL_SERVICE_ERROR = "external.service.error";
	String OPERATION_CANCELED = "operation.canceled";
	String PROVIDER_ERROR = "provider.error";
	String PROVIDER_INACCESIBLE_SERVICE = "provider.inaccesible.service";
	
}
