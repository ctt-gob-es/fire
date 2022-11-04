package es.gob.fire.client;

import java.nio.charset.Charset;

/**
 * Respuesta de una petici&oacute;n HTTP.
 * @author carlos.gamuci
 */
public class HttpResponse {

	private final boolean ok;
	private final int status;
	private final String mimeType;
	private final Charset charset;
	private final byte[] content;

	/**
	 * Crea la respuesta con el c&oacute;digo de estado y el contenido.
	 * @param statusCode C&oacute;digo de estado de la respuesta.
	 * @param content Contenido de la respuesta.
	 */
	public HttpResponse(final int statusCode, final byte[] content) {
		this.ok = statusCode == 200;
		this.mimeType = null;
		this.charset = null;
		this.status = statusCode;
		this.content = content;
	}

	/**
	 * Crea la respuesta con el c&oacute;digo de estado y el contenido.
	 * @param statusCode C&oacute;digo de estado de la respuesta.
	 * @param mimeType Tipo de dato.
	 * @param charset Juego de caracteres.
	 * @param content Contenido de la respuesta.
	 */
	public HttpResponse(final int statusCode, final String mimeType, final Charset charset, final byte[] content) {
		this.ok = statusCode == 200;
		this.mimeType = mimeType;
		this.charset = charset;
		this.status = statusCode;
		this.content = content;
	}

	/**
	 * Indica si la petici&oacute;n funcion&oacute; correctamente o, si
	 * en cambio, se produjo un error.
	 * @return {@code true} si la petici&oacute;n finaliz&oacute; correctamente,
	 * {@code false} en caso contrario.
	 */
	public boolean isOk() {
		return this.ok;
	}

	/**
	 * Recupera el c&oacute;digo de estado de la respuesta.
	 * @return C&oacute;digo de estado HTTP.
	 */
	public int getStatus() {
		return this.status;
	}

	/**
	 * Recupera el contenido de la respuesta.
	 * @return Contenido de la respuesta.
	 */
	public byte[] getContent() {
		return this.content;
	}

	/**
	 * Recupera el tipo de contenido de la respuesta.
	 * @return Tipo de contenido de la respuesta o {@code null} si se desconoce.
	 */
	public String getMimeType() {
		return this.mimeType;
	}

	/**
	 * Recupera el juego de caracteres de la respuesta.
	 * @return Juego de caracters de la respuesta o {@code null} si se desconoce
	 * o no aplica.
	 */
	public Charset getCharset() {
		return this.charset;
	}
}
