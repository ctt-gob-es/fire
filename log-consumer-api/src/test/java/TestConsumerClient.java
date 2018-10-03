import java.io.IOException;

import org.junit.Test;

import es.gob.log.consumer.client.LogConsumerClient;
import junit.framework.Assert;

public class TestConsumerClient {

	private static final String SERVICE_URL = "https://appprueba:8443/log-consumer-service/logservice";

	@Test
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
	public void testInitSession() {

		final LogConsumerClient client = new LogConsumerClient();
		client.setDisableSslChecks(true);

		try {
			client.init(SERVICE_URL, "I9lUuX+iEvzAD/hwaU2MbQ==");
		} catch (final IOException e) {
			Assert.fail("Ocurrio un error en la inicializacion de la sesion: " + e);
		}
	}
}
