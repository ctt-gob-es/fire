package es.gob.fire.alarms;

/**
 * Excepci&oacute;n lanzada al producirse un error durante la inicializaci&oacute;n
 * del notificador de alarmas.
 */
public class InitializationException extends Exception {

	/** Serial Id. */
	private static final long serialVersionUID = 250831824085760980L;

	/**
	 * Construye la excepci&oacute;n.
	 */
	public InitializationException() {
		super();
	}

	/**
	 * Construye un excepci&oacute;n.
	 * @param message Mensage descriptivo de la excepci&oacute;n.
	 */
	public InitializationException(final String message) {
		super(message);
	}

	/**
	 * Construye un excepci&oacute;n.
	 * @param message Mensage descriptivo de la excepci&oacute;n.
	 * @param cause Motivo por el que se lanza la excepci&oacute;n.
	 */
	public InitializationException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
