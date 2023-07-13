package es.gob.fire.server.services.internal;

/**
 * Clase para la simplificacion de la impresion de logs referentes a una transacci&oacute;n.
 */
public class LogTransactionFormatter {

	private String appId;

	private String transactionId;

	private String logHeader;

	public LogTransactionFormatter(final String appId) {
		this(appId, null);
	}

	public LogTransactionFormatter(final String appId, final String transactionId) {
		this.appId = appId;
		this.transactionId = transactionId;

		buildHeader();
	}

	/**
	 * Construye la cabecera que deben mostrar las trazas de log.
	 */
	private void buildHeader() {
		if (this.appId != null && this.transactionId != null) {
			this.logHeader = String.format("App %1s, TrId %2s: ", this.appId, this.transactionId); //$NON-NLS-1$
		}
		else if (this.appId != null && this.transactionId == null) {
			this.logHeader = String.format("App %1s: ", this.appId); //$NON-NLS-1$
		}
		else if (this.appId == null && this.transactionId != null) {
			this.logHeader = String.format("TrId %1s: ", this.transactionId); //$NON-NLS-1$
		}
		else {
			this.logHeader = null;
		}
	}

	/**
	 * Obtiene el identificador de aplicaci&oacute;n.
	 * @return Identificador de aplicaci&oacute;n.
	 */
	public String getAppId() {
		return this.appId;
	}

	/**
	 * Establece el identificador de aplicaci&oacute;n.
	 * @param appId Identificador de aplicaci&oacute;n.
	 */
	public void setAppId(final String appId) {
		this.appId = appId;

		buildHeader();
	}

	/**
	 * Obtiene el identificador de transacci&oacute;n.
	 * @return Identificador de transacci&oacute;n.
	 */
	public String getTransactionId() {
		return this.transactionId;
	}

	/**
	 * Establece el identificador de transacci&oacute;n.
	 * @param transactionId Identificador de transacci&oacute;n.
	 */
	public void setTransactionId(final String transactionId) {
		this.transactionId = transactionId;

		buildHeader();
	}

	/**
	 * Devuelve un mensaje con los prefijos que indican el identificador de aplicaci&oacute;n y
	 * el de la transacci&oacute;n actual si se han configurado.
	 * @param message Mensaje al que agregar los prefijos.
	 * @return Cadena con los prefijos.
	 */
	public String f(final String message) {
		return this.logHeader != null
				? this.logHeader + message
				: message;
	}

	/**
	 * Devuelve un mensaje con los prefijos que indican el identificador de aplicaci&oacute;n y
	 * el de la transacci&oacute;n actual si se han configurado.
	 * @param message Mensaje al que agregar los prefijos.
	 * @param subtexts Part&iacute;culas de texto que deben introducirse en el mensaje.
	 * @return Cadena con los prefijos.
	 */
	public String f(final String message, final Object... subtexts) {
		return f(subtexts != null && subtexts.length > 0
				? String.format(message, subtexts)
				: message);
	}

	/**
	 * Devuelve un mensaje formateado usando el identificador indicado como identificador de
	 * la transacci&oacute;n.
	 * @param trId Identificador de transacci&oacute;n.
	 * @param message Mensaje.
	 * @return Mensaje formateado.
	 */
	public String fTr(final String trId, final String message) {
		return format(this.appId, trId, message);
	}

	/**
	 * Devuelve un mensaje con los prefijos que indican el identificador de aplicaci&oacute;n y
	 * el de la transacci&oacute;n si se han configurado.
	 * @param appId Identificador de aplicaci&oacute;n.
	 * @param trId Identificador de transacci&oacute;n.
	 * @param message Mensaje al que agregar los prefijos.
	 * @return Cadena con los prefijos.
	 */
	public static String format(final String appId, final String trId, final String message) {
		if (appId != null && trId != null) {
			return String.format("App %1s, TrId %2s: %3s", appId, trId, message); //$NON-NLS-1$
		}
		else if (appId != null && trId == null) {
			return String.format("App %1s: %2s", appId, message); //$NON-NLS-1$
		}
		else if (appId == null && trId != null) {
			return String.format("TrId %1s: %2s", trId, message); //$NON-NLS-1$
		}
		return message;
	}
}
