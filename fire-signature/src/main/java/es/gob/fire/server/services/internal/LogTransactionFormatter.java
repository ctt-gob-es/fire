package es.gob.fire.server.services.internal;

/**
 * Clase para la simplificacion de la impresion de logs referentes a una transacci&oacute;n.
 */
public class LogTransactionFormatter {

	private String appId;

	private String transactionId;

	public LogTransactionFormatter(final String appId) {
		this(appId, null);
	}

	public LogTransactionFormatter(final String appId, final String transactionId) {
		this.appId = appId;
		this.transactionId = transactionId;
	}

	public String getAppId() {
		return this.appId;
	}

	public void setAppId(final String appId) {
		this.appId = appId;
	}

	public String getTransactionId() {
		return this.transactionId;
	}

	public void setTransactionId(final String transactionId) {
		this.transactionId = transactionId;
	}

	/**
	 * Devuelve un mensaje con los prefijos que indican el identificador de aplicaci&oacute;n y
	 * el de la transacci&oacute;n actual si se han configurado.
	 * @param message Mensaje al que agregar los prefijos.
	 * @return Cadena con los prefijos.
	 */
	public String f(final String message) {
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

	/**
	 * Devuelve un mensaje con los prefijos que indican el identificador de aplicaci&oacute;n y
	 * el de la transacci&oacute;n actual si se han configurado.
	 * @param message Mensaje al que agregar los prefijos.
	 * @param subtexts Part&iacute;culas de texto que deben introducirse en el mensaje.
	 * @return Cadena con los prefijos.
	 */
	public String f(final String message, final Object... subtexts) {
		return f(subtexts != null && subtexts.length > 0 ?
					String.format(message, subtexts) :
					message);
	}

	/**
	 * Devuelve un mensaje con el prefijo que indica el identificador de la transacci&oacute;n
	 * si se han configurado.
	 * @param trId Identificador de transacci&oacute;n.
	 * @param message Mensaje al que agregar el prefijo.
	 * @return Cadena con el prefijo.
	 */
	public static String format(final String trId, final String message) {
		return format(null, trId, message);
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
