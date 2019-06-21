package es.gob.log.register;

/**
 * Excepci&oacute;n que se&ntilde;ala un problema al registrarse un nodo en el servicio de registro de logs.
 * @version 1.0, 19/06/2019.
 */
public class RegistrationException extends Exception {

	/**
	 * Serial Id.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Construye la excepci&oacute;n.
	 */
	public RegistrationException() {
		super();
	}

	/**
	 * Construye la excepci&oacute;n con el mensaje asociado.
	 * @param msg Mensaje que describe el error.
	 */
	public RegistrationException(final String msg) {
		super(msg);
	}

	/**
	 * Construye la excepci&oacute;n con la causa que lo origin&oacute;.
	 * @param cause Motivo del error.
	 */
	public RegistrationException(final Throwable cause) {
		super(cause);
	}

	/**
	 * Construye la excepci&oacute;n con el mensaje asociado y la causa que lo
	 * origin&oacute;.
	 * @param msg Mensaje que describe el error.
	 * @param cause Motivo del error.
	 */
	public RegistrationException(final String msg, final Throwable cause) {
		super(msg, cause);
	}
}
