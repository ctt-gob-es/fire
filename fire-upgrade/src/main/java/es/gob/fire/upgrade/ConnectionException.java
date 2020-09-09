package es.gob.fire.upgrade;

import java.io.IOException;

/**
 * Excepcion que identifica un error en la conexi&oacute;n con el servicio de actualizaci&oacute;n
 * y validaci&oacute;n de firmas.
 */
public class ConnectionException extends IOException {

	/** Serial Id. */
	private static final long serialVersionUID = 3160883336210013150L;

	/**
	 * Crea la excepci&oacute;n de conexi&oacute;n con una descripci&oacute;n asociada.
	 * @param desc Descripci&oacute;n del error.
	 */
	public ConnectionException(final String desc) {
		super(desc);
	}

	/**
	 * Crea la excepci&oacute;n de conexi&oacute;n con el origen del error.
	 * @param cause Origen del error.
	 */
	public ConnectionException(final Throwable cause) {
		super(cause);
	}

	/**
	 * Crea la excepci&oacute;n de conexi&oacute;n con una descripci&oacute;n asociada.
	 * @param desc Descripci&oacute;n del error.
	 * @param cause Origen del error.
	 */
	public ConnectionException(final String desc, final Throwable cause) {
		super(desc, cause);
	}

}
