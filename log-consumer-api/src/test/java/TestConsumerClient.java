import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.junit.Ignore;
import org.junit.Test;

import es.gob.log.consumer.client.LogConsumerClient;
import es.gob.log.consumer.client.LogData;
import junit.framework.Assert;

public class TestConsumerClient {

	private static final String SERVICE_URL = "https://appprueba:8443/log-consumer-service/logservice";

	@Test
	@Ignore
	public void testEcho() {

		final LogConsumerClient client = new LogConsumerClient();
		client.setDisableSslChecks(true);
		try {
			client.echo(SERVICE_URL);
		} catch (final Exception e) {
			Assert.fail("No se pudo conectar con el servicio: " + e);
		}
	}

	@Test
	@Ignore
	public void testInitSession() {

		final LogConsumerClient client = new LogConsumerClient();
		client.setDisableSslChecks(true);

		try {
			client.init(SERVICE_URL, "I9lUuX+iEvzAD/hwaU2MbQ==");
		} catch (final IOException e) {
			Assert.fail("Ocurrio un error en la inicializacion de la sesion: " + e);
		}
	}

	@Test
	public void testSearhText() {

		final LogConsumerClient client = new LogConsumerClient();
		client.setDisableSslChecks(true);

		try {
			client.init(SERVICE_URL, "I9lUuX+iEvzAD/hwaU2MbQ==");
		} catch (final IOException e) {
			Assert.fail("Ocurrio un error en la inicializacion de la sesion: " + e);
		}

		System.out.println("Ficheros de log:\n" + new String(client.getLogFiles()));

		client.openFile("FIRe_2018-12-03.log");

		LogData data = client.searchText(5, "ser", 0, false);
		System.out.println(new String(data.getLog()));

		data = client.searchText(5, "ser", 0, false);
		System.out.println(new String(data.getLog()));

		data = client.searchText(5, "ser", 0, false);
		System.out.println(new String(data.getLog()));

		data = client.searchText(5, "ser", 0, false);
		System.out.println(new String(data.getLog()));
	}

	public static void main(final String[ ] args) throws NoSuchAlgorithmException {

		// Generamos clave de usuario
		final KeyGenerator keyGen = KeyGenerator.getInstance("AES");
		final SecureRandom random = new SecureRandom(); // cryptograph. secure random
		keyGen.init(random);
		final SecretKey secretKey = keyGen.generateKey();
		System.out.println(Base64.getEncoder().encodeToString(secretKey.getEncoded()));


	}
}
