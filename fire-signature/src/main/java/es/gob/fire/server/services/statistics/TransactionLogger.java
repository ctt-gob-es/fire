package es.gob.fire.server.services.statistics;

import java.sql.SQLException;

import es.gob.fire.server.services.internal.FireSession;
import es.gob.fire.server.services.internal.ServiceParams;
import es.gob.fire.services.FireLogger;
import es.gob.fire.services.statistics.config.ConfigManager;
import es.gob.fire.services.statistics.config.DBConnectionException;
import es.gob.fire.services.statistics.dao.ProvidersDAO;
import es.gob.fire.services.statistics.entity.Provider;
import es.gob.fire.services.statistics.entity.TransactionCube;

public class TransactionLogger {


	private FireLogger fireLogger ;

	private static String LOGGER_NAME = "TRANSACTION"; //$NON-NLS-1$

	private static String ROLLDATE = "DIARIA"; //$NON-NLS-1$

	private static int OTRO = 99;

	private static TransactionLogger transactlogger;

	private  TransactionCube transactCube;


	private TransactionLogger() {
		this.setFireLogger(new FireLogger(LOGGER_NAME, ConfigManager.getStatisticsDir(),ROLLDATE));
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
		if(fireSesion.getString(ServiceParams.SESSION_PARAM_TYPE_OPERATION) != null && !"".equals(fireSesion.getString(ServiceParams.SESSION_PARAM_TYPE_OPERATION))) { //$NON-NLS-1$

			final Operations op = Operations.getOperation(fireSesion.getString(ServiceParams.SESSION_PARAM_TYPE_OPERATION));
			this.getTransactCube().setIdOperacion(new Integer(op.getId()));

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

		try {
			if(provForced != null && !"".equals(provForced)) { //$NON-NLS-1$
				final Provider provider =  ProvidersDAO.getProviderByName(provForced);
				this.getTransactCube().setIdProveedor(provider.getIdProveedor());
				this.getTransactCube().setProveedorForzado(true);
			}
			else if(prov != null && !"".equals(prov)) { //$NON-NLS-1$
				final Provider provider =  ProvidersDAO.getProviderByName(prov);
				this.getTransactCube().setIdProveedor(provider.getIdProveedor());
			}
			else if(provsSession != null && provsSession.length == 1) {
				final Provider provider =  ProvidersDAO.getProviderByName(provsSession[0]);
				this.getTransactCube().setIdProveedor(provider.getIdProveedor());
				this.getTransactCube().setProveedorForzado(true);
			}
			else {
				this.getTransactCube().setIdProveedor(OTRO);
			}
		}
		catch (final SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (final DBConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (final NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.getFireLogger().getLogger().info(this.getTransactCube().toString());
	}


	public static final TransactionLogger getTransactLogger(final String confStatistics) {
		int conf = 0;
		if(confStatistics != null && !"".equals(confStatistics)) {//$NON-NLS-1$
			conf = Integer.parseInt(confStatistics);
		}
		if (conf != 0) {
			if(transactlogger == null) {
				transactlogger = new TransactionLogger();
			}
			return transactlogger;
		}
		return null;
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
