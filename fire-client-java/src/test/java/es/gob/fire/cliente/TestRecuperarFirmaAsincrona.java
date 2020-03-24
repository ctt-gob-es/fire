package es.gob.fire.cliente;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import es.gob.fire.client.FireClient;
import es.gob.fire.client.GracePeriodInfo;
import es.gob.fire.client.TransactionResult;

public class TestRecuperarFirmaAsincrona {

	private Properties config = null;

	@Before
	public void init() {

		this.config = new Properties();
		this.config.setProperty("fireUrl", "https://127.0.0.1:8443/fire-signature/fireService"); //$NON-NLS-1$ //$NON-NLS-2$
		this.config.setProperty("javax.net.ssl.keyStore", "C:/Users/carlos.gamuci/Documents/FIRe/Ficheros_Despliegue/client_ssl_new.jks"); //$NON-NLS-1$ //$NON-NLS-2$
		this.config.setProperty("javax.net.ssl.keyStorePassword", "12341234"); //$NON-NLS-1$ //$NON-NLS-2$
		this.config.setProperty("javax.net.ssl.keyStoreType", "JKS"); //$NON-NLS-1$ //$NON-NLS-2$
		this.config.setProperty("javax.net.ssl.certAlias", ""); //$NON-NLS-1$ //$NON-NLS-2$
		this.config.setProperty("javax.net.ssl.trustStore", "all"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Test
	@Ignore
	@SuppressWarnings("static-method")
	public void testRecuperarFirmaAsincrona() {

		final FireClient client;
		try {
			client = new FireClient("B244E473466F", this.config); //$NON-NLS-1$
		}
		catch (final Exception e) {
			Assert.fail("No se construyo el cliente de firma: " + e); //$NON-NLS-1$
			return;
		}

		TransactionResult result;
		try {
			result = client.recoverAsyncSign("1573460420268258223", "ES-T", null, true); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (final Exception e) {
			Assert.fail("Error en la recuperacion de la firma: " + e); //$NON-NLS-1$
			return;
		}

		// Si se devolvio la firma resultante
		if (result.getResult() != null) {
			System.out.println("Se obtiene la firma"); //$NON-NLS-1$
			final File outputFile = new File(""); //$NON-NLS-1$
			try {
				final FileOutputStream fos = new FileOutputStream(outputFile);
				fos.write(result.getResult());
				fos.close();
			}
			catch (final Exception e) {
				Assert.fail("No se puede guardar el fichero: " + e); //$NON-NLS-1$
				return;
			}
		}
		// Si se devolvio un nuevo periodo de gracia
		else if (result.getGracePeriod() != null) {
			System.out.println("Se obtiene un nuevo periodo de gracia"); //$NON-NLS-1$
			final GracePeriodInfo grace = result.getGracePeriod();
			System.out.println("Id de recuperacion: " + grace.getResponseId()); //$NON-NLS-1$
			System.out.println("Fecha estimada: " + grace.getResolutionDate()); //$NON-NLS-1$
		}
		else {
			Assert.fail("No se devolvio un tipo de respuesta esperado"); //$NON-NLS-1$
		}
	}

	public static void main(final String[] args) {
		final TestRecuperarFirmaAsincrona test = new TestRecuperarFirmaAsincrona();
		test.init();
		test.testRecuperarFirmaAsincrona();
	}
}
