package es.gob.fire.server.services.statistics;

import java.sql.SQLException;

import es.gob.fire.server.services.DocInfo;
import es.gob.fire.server.services.internal.BatchResult;
import es.gob.fire.server.services.internal.FireSession;
import es.gob.fire.server.services.internal.ServiceParams;
import es.gob.fire.server.services.internal.SignBatchConfig;
import es.gob.fire.services.FireLogger;
import es.gob.fire.services.statistics.Browser;
import es.gob.fire.services.statistics.config.ConfigManager;
import es.gob.fire.services.statistics.dao.AlgorithmsDAO;
import es.gob.fire.services.statistics.dao.FormatsDAO;
import es.gob.fire.services.statistics.dao.ImprovedFormatsDAO;
import es.gob.fire.services.statistics.dao.ProvidersDAO;
import es.gob.fire.services.statistics.entity.Algorithm;
import es.gob.fire.services.statistics.entity.Format;
import es.gob.fire.services.statistics.entity.ImprovedFormat;
import es.gob.fire.services.statistics.entity.Provider;
import es.gob.fire.services.statistics.entity.SignatureCube;

public class SignatureLogger {

	private static String LOGGER_NAME = "SIGNATURE"; //$NON-NLS-1$

	private static String ROLLDATE = "DIARIA"; //$NON-NLS-1$

	private FireLogger fireLogger ;

	private static SignatureLogger signlogger;

	private SignatureCube signCube;

	private static int OTRO = 99;


	/**
	 * Constructor
	 */
	private SignatureLogger() {
		this.setFireLogger(new FireLogger(LOGGER_NAME, ConfigManager.getStatisticsDir(),ROLLDATE));
	}


	public final static SignatureLogger getSignatureLogger(final String confStatistics) {
		int conf = 0;
		if(confStatistics != null && !"".equals(confStatistics)) {//$NON-NLS-1$
			conf = Integer.parseInt(confStatistics);
		}
		if (conf != 0) {
			if(signlogger == null) {
				signlogger =  new SignatureLogger();
			}
			return signlogger;
		}
		return null;
	}


	/**
	 *
	 * @param fireSesion
	 * @param result
	 * @param docId
	 */
	public final void log(final FireSession fireSesion, final boolean result, final String docId) {

		 String[] provsSession = null;
		 String prov =  null;

		if(getSignCube() == null) {
			this.setSingCube(new SignatureCube());
		}

		this.getSignCube().setResultSign(result);


		try {

			//Navegador
			final Browser browser = (Browser) fireSesion.getObject(ServiceParams.SESSION_PARAM_BROWSER);
			this.getSignCube().setNavegador(browser);
			// Se tratan los datos, de los docuemnto de firma por lotes
			if(docId != null) {
				final BatchResult batchResult = (BatchResult) fireSesion.getObject(ServiceParams.SESSION_PARAM_BATCH_RESULT);
				if(batchResult != null && batchResult.documentsCount() > 0) {
					final DocInfo docinf = batchResult.getDocInfo(docId);
			        if(docinf != null) {
			        	fireSesion.setAttribute(ServiceParams.SESSION_PARAM_DOCSIZE, docinf.getSize());
			        }
			        final SignBatchConfig signConfig = batchResult.getSignConfig(docId);
			        if(signConfig != null) {
			        	fireSesion.setAttribute(ServiceParams.SESSION_PARAM_FORMAT_CONFIG, signConfig.getFormat());
						if(signConfig.getUpgrade() != null) {
							fireSesion.setAttribute(ServiceParams.SESSION_PARAM_UPGRADE,signConfig.getUpgrade());
						}
					}
				}

			}

			if(fireSesion.getObject(ServiceParams.SESSION_PARAM_TRANSACTION_ID) != null
					&& !"".equals(fireSesion.getObject(ServiceParams.SESSION_PARAM_TRANSACTION_ID))) { //$NON-NLS-1$
					final String id_tr = fireSesion.getString(ServiceParams.SESSION_PARAM_TRANSACTION_ID);
					this.getSignCube().setId_transaccion(id_tr != null ? id_tr : "0"); //$NON-NLS-1$
			}

			if(fireSesion.getString(ServiceParams.SESSION_PARAM_FORMAT_CONFIG) != null &&
					!"".equals(fireSesion.getString(ServiceParams.SESSION_PARAM_FORMAT_CONFIG))) { //$NON-NLS-1$
				 final String sSignFormat = fireSesion.getString(ServiceParams.SESSION_PARAM_FORMAT_CONFIG);
				 final Format format = FormatsDAO.getFormatByName(sSignFormat);
				 this.getSignCube().setFormat(format);
			}
			else if(fireSesion.getString(ServiceParams.SESSION_PARAM_FORMAT) != null &&
					!"".equals(fireSesion.getString(ServiceParams.SESSION_PARAM_FORMAT))) { //$NON-NLS-1$
				 final String sSignFormat = fireSesion.getString(ServiceParams.SESSION_PARAM_FORMAT);
				 final Format format = FormatsDAO.getFormatByName(sSignFormat);
				 this.getSignCube().setFormat(format);
			}
			else {

			}

			if(fireSesion.getString(ServiceParams.SESSION_PARAM_UPGRADE) != null &&
					!"".equals(fireSesion.getString(ServiceParams.SESSION_PARAM_UPGRADE))) { //$NON-NLS-1$
				 final String sSignImprovedFormat = fireSesion.getString(ServiceParams.SESSION_PARAM_UPGRADE);
				 final ImprovedFormat impformat = ImprovedFormatsDAO.getFormatByName(sSignImprovedFormat);
				 this.getSignCube().setImprovedFormat(impformat);
			}
			else {
				 //this.getSignCube().setIdImprovedFormat(0);//Ninguno
			}


			if(fireSesion.getString(ServiceParams.SESSION_PARAM_ALGORITHM) != null &&
					!"".equals(fireSesion.getString(ServiceParams.SESSION_PARAM_ALGORITHM))) { //$NON-NLS-1$
				final String sSignAlgorithm =  fireSesion.getString(ServiceParams.SESSION_PARAM_ALGORITHM);
				final Algorithm algorithm = AlgorithmsDAO.getAlgorithmByName(sSignAlgorithm);
				this.getSignCube().setAlgorithm(algorithm);
			}
			else {
				//this.getSignCube().setIdAlgorithm(OTRO);
			}

			if(fireSesion.getObject(ServiceParams.SESSION_PARAM_PROVIDERS) != null ) {
				 provsSession = (String []) fireSesion.getObject(ServiceParams.SESSION_PARAM_PROVIDERS);
			}
			if(fireSesion.getString(ServiceParams.SESSION_PARAM_CERT_ORIGIN) != null &&
					!"".equals(fireSesion.getString(ServiceParams.SESSION_PARAM_CERT_ORIGIN))) { //$NON-NLS-1$
				 prov = fireSesion.getString(ServiceParams.SESSION_PARAM_CERT_ORIGIN);
			}

			if(prov != null && !"".equals(prov)) { //$NON-NLS-1$
				Provider provider = new Provider();
				provider = ProvidersDAO.getProviderByName(prov);
				this.getSignCube().setProveedor( provider);
			}
			else if(provsSession != null && provsSession.length == 1) {
				Provider provider = new Provider();
				provider = ProvidersDAO.getProviderByName(provsSession[0]);
				this.getSignCube().setProveedor( provider);
			}
			else {
				//this.getSignCube().setIdProveedor(OTRO);
			}

//			if(fireSesion.getObject(ServiceParams.SESSION_PARAM_DOCSIZE) != null) {
//				final Long size = (Long)fireSesion.getObject(ServiceParams.SESSION_PARAM_DOCSIZE);
//				this.getSignCube().setSize(size);
//			}else {
//				this.getSignCube().setSize(new Long(0L));
//			}

		}
		catch (final SQLException e) {
			// TODO: handle exception
		}
		catch (final es.gob.fire.services.statistics.config.DBConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.getFireLogger().getLogger().info(this.getSignCube().toString());

		fireSesion.setAttribute(ServiceParams.SESSION_PARAM_UPGRADE, null);
		fireSesion.setAttribute(ServiceParams.SESSION_PARAM_FORMAT_CONFIG, null);
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
