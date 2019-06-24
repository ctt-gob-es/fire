package es.gob.log.consumer.service;

/**
 * Clase que define los Par&aacute;metros utilizados por los servicios.
 * @author Adolfo.Navarro
 *
 */
class ServiceParams {

	/** Parametro operaci&oacute;n. */
	static final String OPERATION = "op"; //$NON-NLS-1$

	/** Parametro con el token cifrado de inicio de sesi&oacute;n. */
	static final String CIPHERED_TOKEN = "sc"; //$NON-NLS-1$

	/**Par&aacute;metro que indica el nombre del fichero log */
	public static final String LOG_FILE_NAME = "fname"; //$NON-NLS-1$

	/**Par&aacute;metro que indica el n&uacute;mero de l&iacute;neas a obtener del fichero log */
	static final String NUM_LINES = "nlines"; //$NON-NLS-1$

	/***Par&aacute;metro que indica el texto de b&uacute;squeda */
	static final String SEARCH_TEXT = "search_txt"; //$NON-NLS-1$

	/***Par&aacute;metro que indica la fecha y hora de b&uacute;squeda */
	static final String SEARCH_DATETIME = "search_date"; //$NON-NLS-1$

	/***Par&aacute;metro que indica la fecha y hora de b&uacute;squeda */
	static final String START_DATETIME = "start_date"; //$NON-NLS-1$

	/***Par&aacute;metro que indica la fecha y hora de b&uacute;squeda */
	static final String END_DATETIME = "end_date"; //$NON-NLS-1$

	/***Par&aacute;metro que indica el texto de b&uacute;squeda */
	static final String LEVEL = "level"; //$NON-NLS-1$

	/***Par&aacute;metro que indica si se tiene que reiniciar*/
	public static final String PARAM_RESET ="reset"; //$NON-NLS-1$


}
