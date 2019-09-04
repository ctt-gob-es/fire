package es.gob.fire.server.admin.service;

public class ServiceParams {

	/***Par&aacute;metro que indica el id de la aplicaci&oacute;n */
	public static final String PARAM_APPID = "appid"; //$NON-NLS-1$

	/***Par&aacute;metro que indica el nombre de la aplicaci&oacute;n */
	public static final String PARAM_NAME = "nombre-app"; //$NON-NLS-1$

	/***Par&aacute;metro que indica el id de la aplicaci&oacute;n */
	public static final String PARAM_ENABLED = "enabled"; //$NON-NLS-1$

	/***Par&aacute;metro que indica el nombre del responsable de la aplicaci&oacute;n */
	public static final String PARAM_RESP = "nombre-resp"; //$NON-NLS-1$

	public static final String PARAM_CERTID = "id-certificate"; //$NON-NLS-1$

	/***Par&aacute;metro que indica el tf del responsable de la aplicaci&oacute;n */
	public static final String PARAM_TEL = "telf"; //$NON-NLS-1$

	public static final String PARAM_USERID = "userid"; //$NON-NLS-1$

	public static final String PARAM_USERNAME = "user"; //$NON-NLS-1$

	/***Par&aacute;metro que indica el password del usuario */
	public static final String PARAM_PASSWORD = "password"; //$NON-NLS-1$

	/***Par&aacute;metro que indica el id de la aplicaci&oacute;n */
	public static final String PARAM_PASSWORD_COPY = "passwordcopy"; //$NON-NLS-1$

	public static final String PARAM_MAIL = "mail"; //$NON-NLS-1$

	public static final String PARAM_OP = "op"; //$NON-NLS-1$

	public static final String PARAM_NAMESRV = "name-srv"; //$NON-NLS-1$

	public static final String PARAM_URL = "url"; //$NON-NLS-1$

	public static final String PARAM_VERIFY_SSL = "verifyssl"; //$NON-NLS-1$

	public static final String PARAM_FILENAME = "fname";//$NON-NLS-1$

	public static final String PARAM_NLINES = "nlines";//$NON-NLS-1$

	public static final String PARAM_TXT2SEARCH = "search_txt";//$NON-NLS-1$

	public static final String PARAM_SEARCHDATE = "search_date";//$NON-NLS-1$

	public static final String PARAM_CHARSET = "Charset";//$NON-NLS-1$

	public static final String PARAM_PARAM_LEVELS = "Levels";//$NON-NLS-1$

	public static final String PARAM_DATE = "Date";//$NON-NLS-1$

	public static final String PARAM_TIME = "Time";//$NON-NLS-1$

	public static final String PARAM_DATETIME = "DateTimeFormat";//$NON-NLS-1$

	public static final String PARAM_CODE = "cod"; //$NON-NLS-1$

	/***Par&aacute;metro que indica el id del certificado */
	public static final String PARAM_ID_CERT = "id-cert"; //$NON-NLS-1$

	/***Par&aacute;metro que indica el nombre del certificado */
	public static final String PARAM_NAME_CERT = "nombre-cer";//$NON-NLS-1$

	/***Par&aacute;metro que indica el fichero pricipal del certificado */
	public static final String PARAM_CER_PRIN = "fichero-firma-prin";//$NON-NLS-1$

	/***Par&aacute;metro que indica el fichero backup del certificado */
	public static final String PARAM_CER_RESP = "fichero-firma-resp";//$NON-NLS-1$

	/***Par&aacute;metro que indica el fichero pricipal convertido en base64 del certificado */
	public static final String PARAM_CERB64PRIM = "b64CertPrin";//$NON-NLS-1$

	/***Par&aacute;metro que indica el fichero backup convertido en base64 del certificado */
	public static final String PARAM_CERB64RESP = "b64CertBkup";//$NON-NLS-1$

	/***Par&aacute;metro que indica la operaci&oacute;n del certificado */
	public static final String PARAM_OP_CERT = "op"; //$NON-NLS-1$

	/***Par&aacute;metro que indica el id del usuario */
	public static final String PARAM_IDUSER = "idUser";//$NON-NLS-1$

	/***Par&aacute;metro que indica el login del usuario */
	public static final String PARAM_LOGNAME = "login-usr"; //$NON-NLS-1$

	/***Par&aacute;metro que indica el password del usuario */
	public static final String PARAM_PASSWD = "passwd-usr"; //$NON-NLS-1$

	/***Par&aacute;metro que indica el rol del usuario */
	public static final String PARAM_USER_ROLE = "role-usr";//$NON-NLS-1$

	/***Par&aacute;metro que indica la operaci&oacute;n en el campo del usuario */
	public static final String PARAM_OP_USER = "op"; //$NON-NLS-1$

	/***Par&aacute;metro que indica el nombre del usuario */
	public static final String PARAM_USER_NAME = "usr-name";//$NON-NLS-1$

	/***Par&aacute;metro que indica el apellido del usuario */
	public static final String PARAM_USERSURNAME = "usr-surname";//$NON-NLS-1$

	/***Par&aacute;metro que indica el mail del usuario */
	public static final String PARAM_USEREMAIL = "email";//$NON-NLS-1$

	/***Par&aacute;metro que indica el tel&eacute; del usuario */
	public static final String PARAM_USERTELF = "telf-contact";//$NON-NLS-1$

	/***Par&aacute;metro que indica el hash del algoritmo */
	public static final String SHA_2 = "SHA-256"; //$NON-NLS-1$

	/***Par&aacute;metro que indica el texto de error */
	public static final String PARAM_ERR = "err"; //$NON-NLS-1$

	/***Par&aacute;metro que indica el texto de &eacute;xito */
	public static final String PARAM_SUCCESS = "succ"; //$NON-NLS-1$

	/***Par&aacute;metro que indica la fecha y hora de b&uacute;squeda */
	static final String PARAM_START_DATETIME = "start_date"; //$NON-NLS-1$

	/***Par&aacute;metro que indica la fecha y hora de b&uacute;squeda */
	static final String PARAM_END_DATETIME = "end_date"; //$NON-NLS-1$

	/***Par&aacute;metro que indica el texto de b&uacute;squeda */
	static final String PARAM_LEVEL = "level"; //$NON-NLS-1$

	/***Par&aacute;metro que indica si se tiene que reiniciar*/
	public static final String PARAM_RESET ="reset"; //$NON-NLS-1$

	/***Par&aacute;metro que indica menasaje de texto a mostrar*/
	public static final String PARAM_MSG ="msg"; //$NON-NLS-1$

	/***Par&aacute;metro que indica la consulta seleccionada para las estad&iacute;sticas */
	public static final String PARAM_SELECT_QUERY = "select_query";//$NON-NLS-1$

	/** Atributo de sesi&oacute;n para el guardado de un JSON de error. */
	public static final String SESSION_ATTR_ERROR_JSON = "ERROR_JSON"; //$NON-NLS-1$

	/** Atributo de sesi&oacute;n para el guardado de una respuesta JSON. */
	public static final String SESSION_ATTR_JSON = "JSON"; //$NON-NLS-1$

	/** Atributo de sesi&oacute;n para el guardado de un JSON con la informaci&oacute;n del log. */
	public static final String SESSION_ATTR_JSON_LOGINFO = "JSON_LOGINFO"; //$NON-NLS-1$

	/** Atributo de sesi&oacute;n para el guardado del objeto de consulta de logs. */
	public static final String SESSION_ATTR_LOG_CLIENT = "LOG_CLIENT"; //$NON-NLS-1$

	/** Atributo de sesi&oacute;n para el guardado del valor bandera que indica que el usuario
	 * completo correctamente la autenticaci&oacute;n. */
	public static final String SESSION_ATTR_INITIALIZED = "initializedSession"; //$NON-NLS-1$

	/** Atributo de sesi&oacute;n para el guardado del nombre de usuario logueado. */
	public static final String SESSION_ATTR_USER = "user"; //$NON-NLS-1$

	/** Atributo de sesi&oacute;n para el guardado la informaci&oacute;n de restauraci&oacute;n de contrase&ntilde;a
	 * del usuario. */
	public static final String SESSION_ATTR_RESTORATION = "restorationPass"; //$NON-NLS-1$


}
