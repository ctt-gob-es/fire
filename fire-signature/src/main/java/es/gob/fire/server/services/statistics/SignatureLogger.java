package es.gob.fire.server.services.statistics;

import es.gob.fire.server.services.internal.FireSession;
import es.gob.fire.server.services.internal.ServiceParams;
import es.gob.fire.services.FireLogger;

public class SignatureLogger {

	private static String LOGGER_NAME = "SIGNATURE"; //$NON-NLS-1$

	private FireLogger fireLogger ;

	private static SignatureLogger signlogger;

	private SignatureCube signCube;

	private static String PROV_OTRO = "OTRO"; //$NON-NLS-1$


	/**
	 * Constructor
	 */
	private SignatureLogger() {
		this.setFireLogger(new FireLogger(LOGGER_NAME));
	}


	public final static SignatureLogger getSignatureLogger() {
		if(signlogger == null) {
			signlogger =  new SignatureLogger();
		}
		return signlogger;
	}

	public final void log(final Browser browser,final String sSignFormat,final String sSignAlgorithm,final String provider ,final boolean result) {

		if(getSignCube() == null) {
			this.setSingCube(new SignatureCube());
		}

		if(browser != null) {
			this.getSignCube().setNavegador(browser);
		}

		if(sSignFormat != null && !"".equals(sSignFormat)) { //$NON-NLS-1$
			this.getSignCube().setSignFormat(SignatureFormats.getId(sSignFormat));
		}

		if(sSignAlgorithm != null && !"".equals(sSignAlgorithm)) { //$NON-NLS-1$
			this.getSignCube().setSignAlgorithm(SignatureAlgorithms.getId(sSignAlgorithm));
		}

		if(provider != null && !"".equals(provider)) { //$NON-NLS-1$
			this.getSignCube().setProveedor(provider);
		}

		this.getSignCube().setResultSign(result);


		this.getFireLogger().getLogger().info(this.getSignCube().toString());
	}

	public final void log(final FireSession fireSesion, final boolean result) {

		 String[] provsSession = null;
		 String prov =  null;

		if(getSignCube() == null) {
			this.setSingCube(new SignatureCube());
		}

		this.getSignCube().setResultSign(result);

		final Browser browser = (Browser) fireSesion.getObject(ServiceParams.SESSION_PARAM_BROWSER);
		this.getSignCube().setNavegador(browser);

		if(fireSesion.getString(ServiceParams.SESSION_PARAM_FORMAT) != null &&
				!"".equals(fireSesion.getString(ServiceParams.SESSION_PARAM_FORMAT))) { //$NON-NLS-1$
			 final String sSignFormat = fireSesion.getString(ServiceParams.SESSION_PARAM_FORMAT);
			 this.getSignCube().setSignFormat(SignatureFormats.getId(sSignFormat));
		}
		if(fireSesion.getString(ServiceParams.SESSION_PARAM_ALGORITHM) != null &&
				!"".equals(fireSesion.getString(ServiceParams.SESSION_PARAM_ALGORITHM))) { //$NON-NLS-1$
			final String sSignAlgorithm =  fireSesion.getString(ServiceParams.SESSION_PARAM_ALGORITHM);
			this.getSignCube().setSignAlgorithm(SignatureAlgorithms.getId(sSignAlgorithm));
		}

		if(fireSesion.getObject(ServiceParams.SESSION_PARAM_PROVIDERS) != null ) {
			 provsSession = (String []) fireSesion.getObject(ServiceParams.SESSION_PARAM_PROVIDERS);
		}
		if(fireSesion.getString(ServiceParams.SESSION_PARAM_CERT_ORIGIN) != null &&
				!"".equals(fireSesion.getString(ServiceParams.SESSION_PARAM_CERT_ORIGIN))) { //$NON-NLS-1$
			 prov = fireSesion.getString(ServiceParams.SESSION_PARAM_CERT_ORIGIN);
		}


		if(prov != null && !"".equals(prov)) { //$NON-NLS-1$
			this.getSignCube().setProveedor(prov);
		}
		else if(provsSession != null && provsSession.length == 1) {
			this.getSignCube().setProveedor(provsSession[0]);
		}
		else {
			this.getSignCube().setProveedor(PROV_OTRO);
		}

		this.getFireLogger().getLogger().info(this.getSignCube().toString());
	}


	public final FireLogger getFireLogger() {
		return this.fireLogger;
	}

	private final void setFireLogger(final FireLogger fireLogger) {
		this.fireLogger = fireLogger;
	}


	protected final SignatureCube getSignCube() {
		return this.signCube;
	}


	protected final void setSingCube(final SignatureCube signCube) {
		this.signCube = signCube;
	}







}
