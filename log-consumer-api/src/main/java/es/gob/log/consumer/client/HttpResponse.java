package es.gob.log.consumer.client;

/**
 * Respuesta HTTP.
 */
public class HttpResponse {

	byte[] content = null;
	int statusCode = 0;

	/**
	 * Construye un objeto con los datos de una respuesta HTTP.
	 * @param statusCode Resultado notificado en la respuesta.
	 */
	public HttpResponse(final int statusCode) {
		this.statusCode = statusCode;
	}

	/**
	 * Establece el mensaje remitido en la respuesta.
	 * @param content Mensaje de la respuesta.
	 */
	public void setContent(final byte[] content) {
		this.content = content;
	}

	/**
	 * Obtiene el mensaje remitido en la respuesta.
	 * @return Mensaje de la respuesta.
	 */
	public byte[] getContent() {
		return this.content;
	}
}
