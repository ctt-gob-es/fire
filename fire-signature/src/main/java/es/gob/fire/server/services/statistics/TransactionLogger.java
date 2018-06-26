package es.gob.fire.server.services.statistics;

import es.gob.fire.server.services.internal.FireSession;
import es.gob.fire.server.services.internal.ServiceParams;
import es.gob.fire.services.FireLogger;

public class TransactionLogger {


	private FireLogger fireLogger ;

	private static String LOGGER_NAME = "TRANSACTION"; //$NON-NLS-1$

	private static String PROV_OTRO = "OTRO"; //$NON-NLS-1$

	private static TransactionLogger transactlogger;

	private  TransactionCube transactCube;


	private TransactionLogger() {
		this.setFireLogger(new FireLogger(LOGGER_NAME));
	}


	public final void log(final FireSession fireSesion, final boolean result) {

		 String[] provsSession = null;
		 String prov =  null;
		 String provForced = null;

		if(getTransactCube() == null) {
			this.setTransactCube(new TransactionCube());
		}

		this.getTransactCube().setResultTransaction(result);

		if(fireSesion.getString(ServiceParams.SESSION_PARAM_APPLICATION_ID) != null && !"".equals(fireSesion.getString(ServiceParams.SESSION_PARAM_APPLICATION_ID))) { //$NON-NLS-1$
			final String idAplication = fireSesion.getString(ServiceParams.SESSION_PARAM_APPLICATION_ID);
			this.getTransactCube().setIdAplicacion(idAplication);
		}
		if(fireSesion.getString(ServiceParams.SESSION_PARAM_OPERATION) != null && !"".equals(fireSesion.getString(ServiceParams.SESSION_PARAM_OPERATION))) { //$NON-NLS-1$
			final Integer idOperation = Integer.valueOf(fireSesion.getString(ServiceParams.SESSION_PARAM_OPERATION));
			this.getTransactCube().setIdOperacion(idOperation);
		}

		if(fireSesion.getObject(ServiceParams.SESSION_PARAM_PROVIDERS) != null ) {
			 provsSession = (String []) fireSesion.getObject(ServiceParams.SESSION_PARAM_PROVIDERS);
		}
		if(fireSesion.getString(ServiceParams.SESSION_PARAM_CERT_ORIGIN) != null &&
				!"".equals(fireSesion.getString(ServiceParams.SESSION_PARAM_CERT_ORIGIN))) { //$NON-NLS-1$
			 prov = fireSesion.getString(ServiceParams.SESSION_PARAM_CERT_ORIGIN);
		}
		if(fireSesion.getString(ServiceParams.SESSION_PARAM_CERT_ORIGIN_FORCED) != null &&
				!"".equals(fireSesion.getString(ServiceParams.SESSION_PARAM_CERT_ORIGIN_FORCED))) { //$NON-NLS-1$
			provForced = fireSesion.getString(ServiceParams.SESSION_PARAM_CERT_ORIGIN_FORCED);
		}

		if(provForced != null && !"".equals(provForced)) { //$NON-NLS-1$
			this.getTransactCube().setProveedor(provForced);
			this.getTransactCube().setProveedorForzado(true);
		}
		else if(prov != null && !"".equals(prov)) { //$NON-NLS-1$
			this.getTransactCube().setProveedor(prov);
		}
		else if(provsSession != null && provsSession.length == 1) {
			this.getTransactCube().setProveedor(provsSession[0]);
			this.getTransactCube().setProveedorForzado(true);
		}
		else {
			this.getTransactCube().setProveedor(PROV_OTRO);
		}

		this.getFireLogger().getLogger().info(this.getTransactCube().toString());
	}


	public static final TransactionLogger getTransactLogger() {
		if(transactlogger == null) {
			transactlogger = new TransactionLogger();
		}
		return transactlogger;
	}



	private final FireLogger getFireLogger() {
		return this.fireLogger;
	}

	private final void setFireLogger(final FireLogger fireLogger) {
		this.fireLogger = fireLogger;
	}


	private final TransactionCube getTransactCube() {
		return this.transactCube;
	}


	private final void setTransactCube(final TransactionCube transactCube) {
		this.transactCube = transactCube;
	}



}
