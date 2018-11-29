package es.gob.log.consumer.service;

/**
 * Excepci&oacute;n que determina cuando la operaci&oacute;n no hay resultados
 * y establece un mensaje legible del motivo de ello.
 */
class NoResultException extends Exception {

	/** Serial Id. */
	private static final long serialVersionUID = -8251624179631733594L;

	public NoResultException(final String msg) {
		super(msg);
	}

	public NoResultException(final String msg, final Throwable cause) {
		super(msg, cause);
	}

}
