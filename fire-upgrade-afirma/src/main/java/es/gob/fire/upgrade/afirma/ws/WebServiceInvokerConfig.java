package es.gob.fire.upgrade.afirma.ws;

import java.util.Properties;

public class WebServiceInvokerConfig {

	private static final String PROPERTY_APP_ID = "minhap.seap.dtic.clavefirma"; //$NON-NLS-1$

	private static final String PROPERTY_ENDPOINT = "webservices.endpoint"; //$NON-NLS-1$

	private static final String PROPERTY_SERVICE_VERIFY = "webservices.service.signupgrade"; //$NON-NLS-1$

	private static final String PROPERTY_SERVICE_RECOVERY = "webservices.service.recoversignature"; //$NON-NLS-1$

	private static final String PROPERTY_TIMEOUT = "webservices.timeout"; //$NON-NLS-1$

	private static final String PROPERTY_SIGNING_CERTSTORE_PATH = "webservices.authentication.ts.path"; //$NON-NLS-1$

	private static final String PROPERTY_SIGNING_CERTSTORE_PASS = "webservices.authentication.ts.password"; //$NON-NLS-1$

	private static final String PROPERTY_SIGNING_CERTSTORE_TYPE = "webservices.authentication.ts.type"; //$NON-NLS-1$

	private static final String PROPERTY_SIGNING_CERT_ALIAS = "webservices.authentication.cert.alias"; //$NON-NLS-1$

	private static final String PROPERTY_TRUSTSTORE_PATH = "com.trustedstore.path"; //$NON-NLS-1$

	private static final String PROPERTY_TRUSTSTORE_PASS = "com.trustedstore.password"; //$NON-NLS-1$

	private static final String PROPERTY_TRUSTSTORE_TYPE = "com.trustedstore.type"; //$NON-NLS-1$

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

	public String getSigningCertStorePath() {
		final String value = this.config.getProperty(PROPERTY_SIGNING_CERTSTORE_PATH);
		return value == null || value.length() == 0 ? null : value;
	}

	public String getSigningCertStorePass() {
		final String value = this.config.getProperty(PROPERTY_SIGNING_CERTSTORE_PASS);
		return value == null || value.length() == 0 ? null : value;
	}

	public String getSigningCertStoreType() {
		final String value = this.config.getProperty(PROPERTY_SIGNING_CERTSTORE_TYPE);
		return value == null || value.length() == 0 ? null : value;
	}

	public String getSigningCertAlias() {
		final String value = this.config.getProperty(PROPERTY_SIGNING_CERT_ALIAS);
		return value == null || value.length() == 0 ? null : value;
	}

	public String getTruststorePath() {
		final String value = this.config.getProperty(PROPERTY_TRUSTSTORE_PATH);
		return value == null || value.length() == 0 ? null : value;
	}

	public String getTruststorePass() {
		final String value = this.config.getProperty(PROPERTY_TRUSTSTORE_PASS);
		return value == null || value.length() == 0 ? null : value;
	}

	public String getTruststoreType() {
		final String value = this.config.getProperty(PROPERTY_TRUSTSTORE_TYPE);
		return value == null || value.length() == 0 ? null : value;
	}

	public String getAuthMethod() {
		return this.config.getProperty(PROPERTY_AUTH_METHOD, DEFAULT_AUTH_METHOD);
	}

	public String getKeystorePath() {
		final String value = this.config.getProperty(PROPERTY_KEYSTORE_PATH);
		return value == null || value.length() == 0 ? null : value;
	}

	public String getKeystorePass() {
		final String value = this.config.getProperty(PROPERTY_KEYSTORE_PASS);
		return value == null || value.length() == 0 ? null : value;
	}

	public String getKeystoreType() {
		final String value = this.config.getProperty(PROPERTY_KEYTSTORE_TYPE);
		return value == null || value.length() == 0 ? null : value;
	}

	public String getKeystoreCertAlias() {
		final String value = this.config.getProperty(PROPERTY_KEYSTORE_CERT_ALIAS);
		return value == null || value.length() == 0 ? null : value;
	}

	public String getKeystoreCertPass() {
		final String value = this.config.getProperty(PROPERTY_KEYSTORE_CERT_PASS);
		return value == null || value.length() == 0 ? null : value;
	}

	public String getUserName() {
		final String value = this.config.getProperty(PROPERTY_USER_NAME);
		return value == null || value.length() == 0 ? null : value;
	}

	public String getUserPass() {
		final String value = this.config.getProperty(PROPERTY_USER_PASS);
		return value == null || value.length() == 0 ? null : value;
	}

	public String getUserPassType() {
		final String value = this.config.getProperty(PROPERTY_USER_PASS_TYPE);
		return value == null || value.length() == 0 ? null : value;
	}
}
