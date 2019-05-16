package es.gob.fire.server.services.internal.sessions;

/** Excepci&oacute;n relacionada con el DAO de sesiones.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
public final class SessionsDAOException extends Exception {

	private static final long serialVersionUID = 3193250236314821932L;

	SessionsDAOException(final String msg, final Throwable cause) {
		super(msg, cause);
	}

	SessionsDAOException(final String msg) {
		super(msg);
	}

}
