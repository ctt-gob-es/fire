package es.gob.log.consumer.client;

public class HttpResponse {

	byte[] content = null;
	int statusCode = 0;

	public HttpResponse(final int statusCode) {
		this.statusCode = statusCode;
	}

	public void setContent(final byte[] content) {
		this.content = content;
	}

	public byte[] getContent() {
		return this.content;
	}
}
