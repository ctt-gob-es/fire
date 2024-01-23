package es.gob.fire.server.services;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import es.gob.fire.server.services.internal.SessionCollector;
import es.gob.fire.server.services.internal.TempDocumentsManager;
import es.gob.fire.signature.DbManager;
import es.gob.fire.statistics.FireStatistics;

public class ContextListener implements ServletContextListener {

	private static final Logger LOGGER = Logger.getLogger(ContextListener.class.toString());

	@Override
	public void contextInitialized(final ServletContextEvent sce) {
		LOGGER.log(Level.INFO, "Se arranca el contexto del componente central de FIRe"); //$NON-NLS-1$
	}

	@Override
	public void contextDestroyed(final ServletContextEvent sce) {
		try {
			SessionCollector.release();
		} catch (final Throwable e) {
			LOGGER.log(Level.SEVERE, "No se pudieron liberar los recursos del gestor de sesiones", e); //$NON-NLS-1$
		}

		try {
			FireStatistics.release();
		} catch (final Throwable e) {
			LOGGER.log(Level.SEVERE, "No se pudieron liberar los recursos del gestor de estadisticas", e); //$NON-NLS-1$
		}

		try {
			DbManager.closeResources();
		} catch (final Throwable e) {
			LOGGER.log(Level.SEVERE, "No se pudieron liberar los recursos del gestor de bases de datos", e); //$NON-NLS-1$
		}

		try {
			TempDocumentsManager.release();
		} catch (final Throwable e) {
			LOGGER.log(Level.SEVERE, "No se pudieron liberar los recursos del gestor de ficheros temporales", e); //$NON-NLS-1$
		}
	}

}
