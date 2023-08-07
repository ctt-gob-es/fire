package es.gob.fire.server.services.internal;

/**
 * Clase con objetos y m&eacute;todos de ayuda para el tratamiento de una transacci&oacute;n concreta.
 */
public class TransactionAuxParams {

	private String appId;

	private String transactionId;

	private final LogTransactionFormatter logFormatter;

	/**
	 * Construye el objeto auxiliar.
	 */
	public TransactionAuxParams() {
		this(null, null);
	}

	/**
	 * Construye el objeto auxiliar.
	 * @param appId Identificador de la aplicaci&oacute;n que se procesa.
	 */
	public TransactionAuxParams(final String appId) {
		this(appId, null);
	}

	/**
	 * Construye el objeto auxiliar.
	 * @param appId Identificador de la aplicaci&oacute;n que se procesa.
	 * @param transactionId Identificador de la transacci&oacute;n que se procesa.
	 */
	public TransactionAuxParams(final String appId, final String transactionId) {
		this.appId = appId;
		this.transactionId = transactionId;

		this.logFormatter = new LogTransactionFormatter(appId, transactionId);
	}

	/**
	 * Establece el identificador de la transacci&oacute;n que se esta procesando.
	 * @param transactionId Identificador de la transacci&oacute;n que se procesa.
	 */
	public void setTransactionId(final String transactionId) {
		this.transactionId = transactionId;
		this.logFormatter.setTransactionId(transactionId);
	}

	/**
	 * Obtiene objeto para el formateo de las trazas de log relacionadas con la transacci&oacute;n.
	 * @return Objeto para el formateo de logs.
	 */
	public LogTransactionFormatter getLogFormatter() {
		return this.logFormatter;
	}

	public void setAppId(final String appId) {
		this.appId = appId;
		this.logFormatter.setAppId(appId);
	}

	/**
	 * Obtiene el identificador de aplicaci&oacute;n asociado.
	 * @return Identificador de aplicaci&oacute;n.
	 */
	public String getAppId() {
		return this.appId;
	}

	/**
	 * Obtiene el identificador de transacci&oacute;n asociado.
	 * @return Identificador de transacci&oacute;n.
	 */
	public String getTransactionId() {
		return this.transactionId;
	}
}
