package es.gob.fire.server.services.internal;

/**
 * Excepci&oacute;n lanzada al producirse un error durante la recuperaci&oacute;n
 * de los datos resultantes de una firma batch.
 */
public class BatchRecoverException extends Exception {

	/** Serial Id. */
	private static final long serialVersionUID = -8576751196938207048L;

	private final String resultState;

	public BatchRecoverException(final String resultState) {
		super();
		this.resultState = resultState;
	}

	public BatchRecoverException(final String msg, final String resultState) {
		super(msg);
		this.resultState = resultState;
	}

	public BatchRecoverException(final String msg, final Throwable cause, final String resultState) {
		super(msg, cause);
		this.resultState = resultState;
	}

	/**
	 * Estado resultante.
	 * @return  Estado.
	 */
	public String getResultState() {
		return this.resultState;
	}
}
