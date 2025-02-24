package es.gob.fire.signature;

/**
 * Excepci&oacute;n que indica un error detectado en la configuraci&oacute;n.
 */
public class ConfigException extends Exception {

	/** Serial Id. */
	private static final long serialVersionUID = 4868632484888290733L;

	public ConfigException(final String msg) {
		super(msg);
	}

	public ConfigException(final String msg, final Throwable cause) {
		super(msg, cause);
	}
}
