package es.gob.fire.client.weblogic;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import javax.net.ssl.SSLSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.gob.fire.client.HttpsConnection;
import weblogic.net.http.HttpsURLConnection;
import weblogic.security.SSL.HostnameVerifier;
import weblogic.security.SSL.SSLContext;
import weblogic.security.SSL.TrustManager;

public class WebLogicHttpsConnection extends HttpsConnection {

	static final Logger LOGGER = LoggerFactory.getLogger(WebLogicHttpsConnection.class);

	private SSLContext ctx;

	private Certificate[] clientCerts = null;
	private PrivateKey clientPk = null;

	private TrustManager trustManager = null;

	private static final TrustManager DUMMY_TRUST_MANAGER = new TrustManager() {

		@Override
		public boolean certificateCallback(final X509Certificate[] certificateChain, final int validateErr) {

			//LOGGER.info(" ==== TrustManager DUMMY: Verificar certificados: " + certificateChain[0].getSubjectDN().toString() + " (Error " + validateErr + ")");
			LOGGER.info(" ==== TrustManager DUMMY: Verificar certificados: " + certificateChain + " (Error " + validateErr + ")");

			return true;
		}
	};

	private static final HostnameVerifier NULL_HOSTNAME_VERIFIER = new HostnameVerifier() {

		@Override
		public boolean verify(final String hostname, final SSLSession sslSession) {

			LOGGER.info(" ==== HostnameVerifier NULL: Verificar dominio: " + hostname);


			return true;
		}
	};

	@Override
	protected void initContext(
			final KeyStore ks,
			final char[] ksPassword,
			final String ksAlias,
			final KeyStore ts,
			final boolean acceptAllCerts) throws
												NoSuchAlgorithmException,
												UnrecoverableKeyException,
												KeyStoreException,
												KeyManagementException {

		LOGGER.info(" =========== Inicializamos el contexto SSL del servidor weblogic");

		// Definimos como gestionar la confianza de los certificados SSL
		if (ts != null) {
			this.trustManager = new CustomTrustManager(ts);
			LOGGER.info(" ========= TrustManager Custom");
		}
		else if (acceptAllCerts) {
			this.trustManager = DUMMY_TRUST_MANAGER;
			LOGGER.info(" ========= TrustManager DUMMY");
		}

		// Definimos la informacion del certificado cliente SSL
		if (ks != null) {
			String certAlias = ksAlias;
			if (certAlias == null) {
				final Enumeration<String> aliases = ks.aliases();
				if (aliases.hasMoreElements()) {
					certAlias = aliases.nextElement();
				}
			}

			if (certAlias != null) {
				this.clientCerts = ks.getCertificateChain(certAlias);
				this.clientPk = (PrivateKey) ks.getKey(certAlias, ksPassword);
			}
		}

		this.ctx = SSLContext.getInstance("TLSv1.2"); //$NON-NLS-1$

		LOGGER.info(" =========== Contexto SSL: " + this.ctx.getClass());

		// Configuramos el certificado cliente SSL si se ha indicado
		if (this.clientCerts != null && this.clientPk != null) {
			LOGGER.info(" ============ Definimos el certificado SSL cliente en la inicializacion del contexto");
			this.ctx.loadLocalIdentity(this.clientCerts, this.clientPk);
		}
		// Configuramos el almacen de confianza si es uno distinto al por defecto
		if (this.trustManager != null) {
			LOGGER.info(" ============ Definimos el TrustManager a medida en la inicializacion del contexto");
			this.ctx.setTrustManager(this.trustManager);
		}

		// Desactivamos la verificacion de nombre de host si se indica asi
		if (!isVerifyHostNames()) {
			LOGGER.info(" ============ Indicamos que se ignore el nombre de dominio definido en el certificado SSL en la inicializacion del contexto");
			this.ctx.setHostnameVerifier(NULL_HOSTNAME_VERIFIER);
		}

		HttpsURLConnection.setDefaultSSLSocketFactory(this.ctx.getSocketFactory());
	}

	@Override
	protected HttpURLConnection getConnection(final URL url) throws IOException {



		HttpURLConnection con;
		if ("https".equalsIgnoreCase(url.getProtocol()) && this.ctx != null) {
			LOGGER.info(" ============ Vamos a preparar la conexion HTTPS para la URL: " + url);

			final HttpsURLConnection cons = new HttpsURLConnection(url);
			//final HttpsURLConnection cons = (HttpsURLConnection) url.openConnection();

			if (this.clientCerts != null && this.clientPk != null) {
				LOGGER.info(" ============ Definimos el certificado SSL cliente al configurar la conexion");
				cons.loadLocalIdentity(this.clientCerts, this.clientPk);
			}
			if (this.trustManager != null) {
				LOGGER.info(" ============ Definimos el TrustManager a medida al configurar la conexion");
				cons.setTrustManager(this.trustManager);

			}
			if (!isVerifyHostNames()) {
				LOGGER.info(" ============ Indicamos que se ignore el nombre de dominio definido en el certificado SSL al configurar la conexion");
				cons.setHostnameVerifier(NULL_HOSTNAME_VERIFIER);
			}
			cons.setSSLSocketFactory(this.ctx.getSocketFactory());
			con = cons;
		}
		else {
			con = (HttpURLConnection) url.openConnection();
		}


		return con;
	}

	static class CustomTrustManager implements TrustManager {

		private final KeyStore truststore;

		public CustomTrustManager(final KeyStore ks) {
			this.truststore = ks;
		}

		@Override
		public boolean certificateCallback(final X509Certificate[] certChain, final int validateErr) {



			// Comprobamos si en el almacen de confianza se encuentra alguno de los certificados
			// de la cadena de certificacion
			try {
				final Enumeration<String> aliases = this.truststore.aliases();
				while (aliases.hasMoreElements()) {
					final String alias = aliases.nextElement();
					for (final X509Certificate cert : certChain) {
						if (cert.equals(this.truststore.getCertificate(alias))) {
							return true;
						}
					}
				}
			}
			catch (final Exception e) {
				LOGGER.error("No se pudo verificar el certificado SSL de la conexion", e); //$NON-NLS-1$
				return false;
			}

			return false;
		}

	}
}
