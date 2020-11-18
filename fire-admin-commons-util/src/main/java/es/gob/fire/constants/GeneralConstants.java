package es.gob.fire.constants;

/** 
 * <p>Interface that contains general constants.</p>
 * <b>Project:</b><p>Application for monitoring the services of @firma suite systems.</p>
 * @version 1.0, 30/10/2020.
 */
public interface GeneralConstants {
	
	/**
	 * Constant attribute that represents the Monitoriza Logger name.
	 */
	public static final String LOGGER_NAME_FIRE_LOG = "Fire-Server";
	
	/**
	 * Constant attribute that represents the Monitoriza Web Logger name.
	 */
	public static final String LOGGER_NAME_FIRE_WEB_LOG = "Fire-Web";
	
	/**
	 * Constant attribute that represents HTTP secure mode.
	 */
	public static final String SECUREMODE_HTTP = "http";
	
	/**
	 * Constant attribute that represents HTTPS secure mode.
	 */
	public static final String SECUREMODE_HTTPS = "https";
		
	/**
	 * Constant that represents the En_dash character with spaces ' - '.
	 */
	public static final String EN_DASH_WITH_SPACES = " - ";
			
	/**
	 * Constant that represents the path separator character '//'.
	 */
	public static final String DOUBLE_PATH_SEPARATOR = "//";
			
	/**
	 * Attribute that represents the service identifier for SOAP services. 
	 */
	public static final String SOAP_SERVICE = "soap";
	
	/**
	 * Attribute that represents the service identifier for OCSP services. 
	 */
	public static final String OCSP_SERVICE = "ocsp";
	
	/**
	 * Attribute that represents the service identifier for RFC3161 services. 
	 */
	public static final String RFC3161_SERVICE = "rfc3161";
	
	/**
	 * Attribute that represents the string contained in the TimeStamp Services of TS@. 
	 */
	public static final String TIMESTAMP_SERVICE = "timestamp";
			
	/**
	 * Attribute that represents the property substring for timer configuration. 
	 */
	public static final String MONITORIZA_TIMER = "timer";
	
	/**
	 * Constant that represents the static property that indicates the frequency in which the soap requests
	 * are sent to @firma or ts@.
	 */
	public static final String FREQUENCY = "freq";	
	
	/**
	 * Constant attribute that identifies the provider Sun for X.509 content type.
	 */
	public static final String TRUST_MANAGER_FACTORY_SUN_X509 = "SunX509";
	
	/**
	 * Constant that represents the name of the truststore keystore at the database. 
	 */
	public static final String SSL_TRUST_STORE_NAME = "TrustStoreSSL";
	
	/**
	 * Constant that represents the name of the RFC3161 keystore at the database. 
	 */
	public static final String RFC3161_KEYSTORE_NAME = "AuthClientRFC3161";
	
	/**
	 * Constant that represents the string 'smtp'.
	 */		
	public static final String SMTP = "smtp";
		
	/**
	 * Constant that represents the string 'service'.
	 */
	public static final String SERVICE = "service";

	/**
	 * Constant that represents the string 'blockAlarmTime'.
	 */
	public static final String BLOCK_TIME_KEY = "blockAlarmTime";
	
	/**
	 * Constant that represents the string 'mailAddress'.
	 */
	public static final String MAIL_ADDRESS = "mailAddress";
	
	/**
	 * Constant that represents the string 'alarm'.
	 */
	public static final String ALARM = "alarm";

	/**
	 * Constant that represents the string 'subject'.
	 */
	public static final String SUBJECT = "subject";

	/**
	 * Constant that represents the string 'body'.
	 */
	public static final String BODY = "body";

	/**
	 * Constant that represents the string 'degraded'.
	 */
	public static final String DEGRADED = "degraded";

	/**
	 * Constant that represents the string 'downed'.
	 */
	public static final String DOWNED = "downed";
	
	/**
	 * Attribute that represents the name of the parameter that receives the response servlet
	 * for filtering the results by platform (@Firma or TS@). 
	 */
	public static final String OPERATION_CODE_PLATFORM_FILTER = "platform";
	
	/**
	 * Attribute that represents the name of the parameter that receives the response servlet
	 * for showing admin mode results, including details about each request file and times. 
	 */
	public static final String OPERATION_CODE_ADMIN_FILTER = "admin";
	
	/**
	 * Attribute that represents the string for identifying the @firma platform. 
	 */
	public static final String PLATFORM_AFIRMA = "@Firma";
	
	/**
	 * Attribute that represents the string for identifying the ts@ platform. 
	 */
	public static final String PLATFORM_TSA = "TS@";
	
	/**
	 * Attribute that represents the string for identifying the cl@ve platform. 
	 */
	public static final String PLATFORM_CLAVE = "Cl@ve";
	
	/**
	 * Attribute that represents the string for identifying the @firma filter parameter on calling the status servlet. 
	 */
	public static final String PARAMETER_AFIRMA = "afirma";
	
	/**
	 * Attribute that represents the string for identifying the ts@ filter parameter on calling the status servlet. 
	 */
	public static final String PARAMETER_TSA = "tsa";
	
	/**
	 * Attribute that represents the string for identifying the Cl@ve filter parameter on calling the status servlet. 
	 */
	public static final String PARAMETER_CLAVE = "clave";
	
	/**
	 * Attribute that represents the excetion if the certificate is not valid. 
	 */
	public static final String CERTIFICATE_NOT_VALID = "CertificateNotValid";
	
	/**
	 * Attribute that represents the excetion if the certificate is already stored in the keystore.
	 */
	public static final String CERTIFICATE_STORED = "CertificateStored";
	
	/**
	 * Attribute that represents the exception if the validation service is not configured.
	 */
	public static final String VALID_SERVICE_NOT_CONFIGURED = "ValidServiceNotConfigured";
	
	/**
	 * Attribute that represents the row index value for the datatable in case of error.
	 */
	public static final String ROW_INDEX_ERROR = "-1";

	/**
	 * Attribute that represents the service identifier for HTTP services. 
	 */
	public static final String HTTP_SERVICE = "http";
}