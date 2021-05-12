package es.gob.fire.signature;

/**
 * Excepci&oacute;n para se&ntilde;alar un error en una propiedad de un fichero de
 * configuraci&oacute;n. Puede usarse cuando no se ha configurado una propiedad obligatoria
 * o cuando se ha establecido un valor no v&aacute;lido. Permite obtener el nombre de la
 * propiedad problem&aacute;tica y el fichero de propiedades en el que debe encontrarse.
 */
public class InvalidConfigurationException extends Exception {

	/** Serial Id. */
	private static final long serialVersionUID = -1024655289147032345L;

	private static final int HTTP_INTERNAL_SERVER_ERROR = 500;

	private final String property;

	private final String filename;

	/**
	 * Construye la excepci&oacute;n.
	 * @param message Mensaje descriptivo del error.
	 * @param property Propiedad mal configurada.
	 * @param filename Fichero de configuraci&oacute;n al que pertenece la propiedad.
	 */
	public InvalidConfigurationException(final String message, final String property, final String filename) {
		super(message);
		this.property = property;
		this.filename = filename;
	}

	/**
	 * Construye la excepci&oacute;n.
	 * @param property Propiedad mal configurada.
	 * @param filename Fichero de configuraci&oacute;n al que pertenece la propiedad.
	 */
	public InvalidConfigurationException(final String property, final String filename) {
		super();
		this.property = property;
		this.filename = filename;
	}

	/**
	 * Obtiene el nombre de la propiedad que produjo el error por no estar configurada o
	 * por tener un valor no v&aacute;lido.
	 * @return Nombre de la propiedad.
	 */
	public String getProperty() {
		return this.property;
	}

	/**
	 * Obtiene el nombre del fichero de configuraci&oacute;n en el que se debe encontrar la
	 * propiedad que produjo el error.
	 * @return Nombre del fichero de configuraci&oacute;n.
	 */
	public String getFileName() {
		return this.filename;
	}

	/**
	 * Devuelve el error HTTP de la excepci&oacute;n.
	 * @return El httpError.
	 */
	public static int getHttpError(){
		return HTTP_INTERNAL_SERVER_ERROR;
	}
}
