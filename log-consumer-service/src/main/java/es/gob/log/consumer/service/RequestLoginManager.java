package es.gob.log.consumer.service;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpSession;

public class RequestLoginManager {

	public static byte[] process(final HttpSession session) {

		// Generamos un token de inicio de sesion y lo almacenamos en la misma
		final byte[] uuid = UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8);
		final Random r = new SecureRandom();
		final byte[] iv = new byte[16];
		r.nextBytes(iv);

		session.setAttribute(SessionParams.TOKEN, uuid);
		session.setAttribute(SessionParams.IV, iv);

		return buildResult(uuid, session.getId(), iv);
	}

	private static byte[] buildResult(final byte[] uuid, final String sessionId, final byte[] iv) {

		final JsonObjectBuilder builder = Json.createObjectBuilder();
		builder.add("tkn", Base64.encode(uuid)); //$NON-NLS-1$
		builder.add("ss", sessionId); //$NON-NLS-1$
		builder.add("iv", Base64.encode(iv)); //$NON-NLS-1$

		return builder.build().toString().getBytes();
	}
}
