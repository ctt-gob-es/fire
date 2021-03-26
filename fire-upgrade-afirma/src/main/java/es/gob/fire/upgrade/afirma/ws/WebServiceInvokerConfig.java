package es.gob.fire.upgrade.afirma.ws;

import java.util.Properties;

public class WebServiceInvokerConfig {

	private static final String PROPERTY_APP_ID = "minhap.seap.dtic.clavefirma"; //$NON-NLS-1$

	private static final String PROPERTY_ENDPOINT = "webservices.endpoint"; //$NON-NLS-1$

	private static final String PROPERTY_SERVICE_VERIFY = "webservices.service.signupgrade"; //$NON-NLS-1$

	private static final String PROPERTY_SERVICE_RECOVERY = "webservices.service.recoversignature"; //$NON-NLS-1$

	private static final String PROPERTY_TIMEOUT = "webservices.timeout"; //$NON-NLS-1$

	private static final String PROPERTY_TRUSTSTORE_PATH = "com.trustedstore.path"; //$NON-NLS-1$

	private static final String PROPERTY_TRUSTSTORE_PASS = "com.trustedstore.password"; //$NON-NLS-1$

	private static final String PROPERTY_TRUSTSTORE_TYPE = "com.trustedstore.type"; //$NON-NLS-1$

	private static final String PROPERTY_TRUSTSTORE_CERT_ALIAS = "com.trustedstore.cert.alias"; //$NON-NLS-1$

	private static final String PROPERTY_AUTH_METHOD = "webservices.authorization.method"; //$NON-NLS-1$

	private static final String PROPERTY_KEYSTORE_PATH = "webservices.authorization.ks.path"; //$NON-NLS-1$

	private static final String PROPERTY_KEYSTORE_PASS = "webservices.authorization.ks.password"; //$NON-NLS-1$

	private static final String PROPERTY_KEYTSTORE_TYPE = "webservices.authorization.ks.type"; //$NON-NLS-1$

	private static final String PROPERTY_KEYSTORE_CERT_ALIAS = "webservices.authorization.ks.cert.alias"; //$NON-NLS-1$

	private static final String PROPERTY_KEYSTORE_CERT_PASS = "webservices.authorization.ks.cert.password"; //$NON-NLS-1$

	private static final String PROPERTY_USER_NAME = "webservices.authorization.user.name"; //$NON-NLS-1$

	private static final String PROPERTY_USER_PASS = "webservices.authorization.user.password"; //$NON-NLS-1$

	private static final String PROPERTY_USER_PASS_TYPE = "webservices.authorization.user.passwordType"; //$NON-NLS-1$

    private static final long DEFAULT_TIMEOUT = 25000;

    private static final String DEFAULT_AUTH_METHOD = "none"; //$NON-NLS-1$

	private final Properties config;

	public WebServiceInvokerConfig(final Properties config) {
		this.config = config;
	}

	public String getAppId() {
		return this.config.getProperty(PROPERTY_APP_ID);
	}

	public String getEndpoint() {
		return this.config.getProperty(PROPERTY_ENDPOINT);
	}

	public String getServiceVerify() {
		return this.config.getProperty(PROPERTY_SERVICE_VERIFY);
	}

	public String getServiceRecovery() {
		return this.config.getProperty(PROPERTY_SERVICE_RECOVERY);
	}

	public long getTimeout() {

		long timeout;
		try {
			timeout = Long.parseLong(this.config.getProperty(PROPERTY_TIMEOUT));
		}
		catch (final Exception e) {
			timeout = DEFAULT_TIMEOUT;
		}
		return timeout;
	}

	public String getTruststorePath() {
		return this.config.getProperty(PROPERTY_TRUSTSTORE_PATH);
	}

	public String getTruststorePass() {
		return this.config.getProperty(PROPERTY_TRUSTSTORE_PASS);
	}

	public String getTruststoreType() {
		return this.config.getProperty(PROPERTY_TRUSTSTORE_TYPE);
	}


	public String getTruststoreCertAlias() {
		return this.config.getProperty(PROPERTY_TRUSTSTORE_CERT_ALIAS);
	}

	public String getAuthMethod() {
		return this.config.getProperty(PROPERTY_AUTH_METHOD, DEFAULT_AUTH_METHOD);
	}

	public String getKeystorePath() {
		return this.config.getProperty(PROPERTY_KEYSTORE_PATH);
	}

	public String getKeystorePass() {
		return this.config.getProperty(PROPERTY_KEYSTORE_PASS);
	}

	public String getKeystoreType() {
		return this.config.getProperty(PROPERTY_KEYTSTORE_TYPE);
	}

	public String getKeystoreCertAlias() {
		return this.config.getProperty(PROPERTY_KEYSTORE_CERT_ALIAS);
	}

	public String getKeystoreCertPass() {
		return this.config.getProperty(PROPERTY_KEYSTORE_CERT_PASS);
	}

	public String getUserName() {
		return this.config.getProperty(PROPERTY_USER_NAME);
	}

	public String getUserPass() {
		return this.config.getProperty(PROPERTY_USER_PASS);
	}

	public String getUserPassType() {
		return this.config.getProperty(PROPERTY_USER_PASS_TYPE);
	}
}
