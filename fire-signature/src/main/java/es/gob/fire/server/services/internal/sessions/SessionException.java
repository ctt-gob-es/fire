package es.gob.fire.server.services.internal.sessions;

/**
 * Excepci&oacute;n que se&ntilde;ala un problema de aturaleza
 */
public class SessionException extends Exception {

	/** Sesion Id. */
	private static final long serialVersionUID = -1475752597406718059L;

	/**
	 * Construye la excepci&oacuate;n con el mensaje descriptivo.
	 * @param msg Mensaje descriptivo.
	 */
	public SessionException(final String msg) {
		super(msg);
	}

	/**
	 * Construye la excepci&oacuate;n con el mensaje descriptivo y su causa.
	 * @param msg Mensaje descriptivo.
	 * @param cause Motivo por el que se lanza la excepci&oacute;n.
	 */
	public SessionException(final String msg, final Throwable cause) {
		super(msg, cause);
	}
}
