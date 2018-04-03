package es.gob.log.consumer;

/**
 * Indica que el patr&oacute;n indicado con la forma de los registros del log, no
 * es v&aacute;lido.
 */
public class InvalidPatternException extends Exception {

	/** Serial ID. */
	private static final long serialVersionUID = 6228834228066580806L;

	/**
	 * Construye una excepci&oacute;n con el detalle del problema.
	 * @param msg Mensaje con el detalle del problema.
	 */
	public InvalidPatternException(final String msg) {
		super(msg);
	}
}
