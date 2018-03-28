package es.gob.log.consumer;

/**
 * Indica que se ha encontrado un registro dentro de un log que no cumple con el formato esperado.
 */
public class InvalidRegistryFormatException extends Exception {

	/** Serial Id. */
	private static final long serialVersionUID = -3858120939879352749L;

	private LogRegistry registry = null;

	/**
	 * Construye una excepci&oacute;n sin mensaje.
	 */
	public InvalidRegistryFormatException() {
		super();
	}

	/**
	 * Construye una excepci&oacute;n con el mensaje dado.
	 * @param message Detalle de la excepci&oacute;n.
	 */
	public InvalidRegistryFormatException(final String message) {
		super(message);
	}

	/**
	 * Construye una excepci&oacute;n indicando el motivo que lo origin&oacute;.
	 * @param cause Motivo que origin&oacute; la excepci&oacute;n.
	 */
	public InvalidRegistryFormatException(final Throwable cause) {
		super(cause);
	}

	/**
	 * Construye una excepci&oacute;n indicando el mensaje y el motivo
	 * que lo origin&oacute;.
	 * @param message Detalle de la excepci&oacute;n.
	 * @param cause Motivo que origin&oacute; la excepci&oacute;n.
	 */
	public InvalidRegistryFormatException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * Obtiene el registro con formato inv&aacute;lido.
	 * @return Registro de log.
	 */
	public LogRegistry getRegistry() {
		return this.registry;
	}

	/**
	 * Establece el registro con formato inv&aacute;lido.
	 * @param registry Registro de log.
	 */
	public void setRegistry(final LogRegistry registry) {
		this.registry = registry;
	}


}
