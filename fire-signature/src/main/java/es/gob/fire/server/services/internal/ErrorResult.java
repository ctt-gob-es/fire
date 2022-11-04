package es.gob.fire.server.services.internal;

import java.nio.charset.Charset;

import es.gob.fire.server.connector.OperationResult;

public class ErrorResult extends OperationResult {

	private final int code;
	private final String message;

	public ErrorResult(final int code, final String message) {
		this.code = code;
		this.message = message;
	}

	public int getCode() {
		return this.code;
	}

	public String getMessage() {
		return this.message;
	}

	@Override
	public byte[] encodeResult(final Charset charset) {
		final String result = "{\"c\": " + this.code + ", \"m\": \"" + this.message + "\"}"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return charset != null ? result.getBytes(charset) : result.getBytes();
	}
}
