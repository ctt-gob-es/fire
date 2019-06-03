package es.gob.log.consumer.client;

/**
 * Analizador para la gesti&oacute;n de las respuestas controladas del servidor de logs.
 */
public class ServerErrorParser {

	private static final int SEPARATOR = '|';

	private static final String DEFAULT_STATUS = "0"; //$NON-NLS-1$

	private String status;

	private final String message;

	/**
	 * Contruye el analizador para facilitar el tratamiento de una respuesta de error
	 * controlada del servidor de logs.
	 * @param content Contenido de la respuesta del servidor.
	 */
	public ServerErrorParser(final byte[] content) {
		this(new String(content));
	}

	/**
	 * Contruye el analizador para facilitar el tratamiento de una respuesta de error
	 * controlada del servidor de logs.
	 * @param content Contenido de la respuesta del servidor.
	 */
	public ServerErrorParser(final String content) {

		if (content == null) {
			throw new NullPointerException("No se admiten mensajes vacios"); //$NON-NLS-1$
		}

		final int sepPos = content.indexOf(SEPARATOR);
		if (sepPos > -1) {
			try {
				this.status = content.substring(0, sepPos);
			}
			catch (final Exception e) {
				this.status = DEFAULT_STATUS;
			}
			this.message = content.substring(sepPos + 1);
		}
		else {
			this.status = DEFAULT_STATUS;
			this.message = content;
		}
	}

	public String getStatus() {
		return this.status;
	}

	public String getMessage() {
		return this.message;
	}
}
