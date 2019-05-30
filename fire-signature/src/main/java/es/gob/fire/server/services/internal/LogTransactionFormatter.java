package es.gob.fire.server.services.internal;

/** Utilidad para la simplificaci&oacute;n de la impresi&oacute;n de <i>logs</i> referentes a una
 * transacci&oacute;n. */
public final class LogTransactionFormatter {

	private final String appId;

	private String transactionId;

	LogTransactionFormatter(final String appId) {
		this(appId, null);
	}

	/** Construye la utilidad para la simplificaci&oacute;n de la impresi&oacute;n de <i>logs</i>
	 * referentes a una transacci&oacute;n.
	 * @param appId Identificador de aplicaci&oacute;n.
	 * @param transactionId Identificador de transacci&oacute;n. */
	public LogTransactionFormatter(final String appId, final String transactionId) {
		this.appId = appId;
		this.transactionId = transactionId;
	}

	String getAppId() {
		return this.appId;
	}

	String getTransactionId() {
		return this.transactionId;
	}

	void setTransactionId(final String transactionId) {
		this.transactionId = transactionId;
	}

	/** Devuelve un mensaje con los prefijos que indican el identificador de aplicaci&oacute;n y
	 * el de la transacci&oacute;n actual si se han configurado.
	 * @param message Mensaje al que agregar los prefijos.
	 * @return Cadena con los prefijos. */
	public String format(final String message) {
		if (this.appId != null && this.transactionId != null) {
			return String.format("App %1s, TrId %2s: %3s", this.appId, this.transactionId, message); //$NON-NLS-1$
		}
		else if (this.appId != null && this.transactionId == null) {
			return String.format("App %1s: %2s", this.appId, message); //$NON-NLS-1$
		}
		else if (this.appId == null && this.transactionId != null) {
			return String.format("TrId %1s: %2s", this.transactionId, message); //$NON-NLS-1$
		}
		return message;
	}
}
