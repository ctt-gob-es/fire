/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.services;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import es.gob.fire.alarms.Alarm;
import es.gob.fire.server.services.internal.AddDocumentBatchManager;
import es.gob.fire.server.services.internal.AlarmsManager;
import es.gob.fire.server.services.internal.ApplicationInfo;
import es.gob.fire.server.services.internal.CreateBatchManager;
import es.gob.fire.server.services.internal.LogTransactionFormatter;
import es.gob.fire.server.services.internal.RecoverBatchResultManager;
import es.gob.fire.server.services.internal.RecoverBatchSignatureManager;
import es.gob.fire.server.services.internal.RecoverBatchStateManager;
import es.gob.fire.server.services.internal.RecoverErrorManager;
import es.gob.fire.server.services.internal.RecoverSignManager;
import es.gob.fire.server.services.internal.RecoverSignResultManager;
import es.gob.fire.server.services.internal.RecoverUpdatedSignManager;
import es.gob.fire.server.services.internal.RecoverUpdatedSignResultManager;
import es.gob.fire.server.services.internal.ServiceParams;
import es.gob.fire.server.services.internal.SessionCollector;
import es.gob.fire.server.services.internal.SignBatchManager;
import es.gob.fire.server.services.internal.SignOperationManager;
import es.gob.fire.server.services.internal.TempDocumentsManager;
import es.gob.fire.server.services.internal.TransactionAuxParams;
import es.gob.fire.signature.ConfigFilesException;
import es.gob.fire.signature.ConfigManager;
import es.gob.fire.signature.DbManager;
import es.gob.fire.signature.InvalidConfigurationException;
import es.gob.fire.statistics.FireStatistics;

/**
 * Servicio central de FIRe que integra las funciones de firma a traves del Cliente @firma
 * y proveedores de firma en la nube.
 */
public class FIReService extends HttpServlet {

	/** Serial Id. */
	private static final long serialVersionUID = -2304782878707695769L;
	private static final Logger LOGGER = Logger.getLogger(FIReService.class.getName());

    @Override
    public void init() throws ServletException {
    	super.init();

    	// Configuramos el modulo de alarmas
    	AlarmsManager.init(ModuleConstants.MODULE_NAME, ConfigManager.getAlarmsNotifierClassName());

    	// Comprobamos la configuracion
    	try {
	    	ConfigManager.checkConfiguration();
		}
    	catch (final InvalidConfigurationException e) {
    		LOGGER.log(Level.SEVERE, "Error en la configuracion de la/s propiedad/es " + e.getProperty() + " (" + e.getFileName() + ")", e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    		AlarmsManager.notify(Alarm.RESOURCE_CONFIG, e.getProperty(), e.getFileName());
    		return;
    	}
    	catch (final Exception e) {
    		LOGGER.log(Level.SEVERE, "Error al cargar la configuracion del componente central", e); //$NON-NLS-1$
    		final String configFile = e instanceof ConfigFilesException ?
    				((ConfigFilesException) e).getFileName() : "Fichero de configuracion principal del componente central"; //$NON-NLS-1$
    		AlarmsManager.notify(Alarm.RESOURCE_NOT_FOUND, configFile);
    		return;
    	}

    	// Configuramos los logs
    	try {
    		FireLogManager.configureLogs();
    	}
		catch(final Throwable e) {
			LOGGER.log(Level.WARNING,
				"No se pudo configurar la salida de los logs de FIRe a un fichero externo", e //$NON-NLS-1$
			);
		}

    	// Configuramos la autenticacion del proxy de red
    	try {
    		NetworkAuthenticator.configure();
    	}
    	catch(final Throwable e) {
    		LOGGER.log(Level.WARNING, "No se pudo configurar el autenticador para el proxy", e); //$NON-NLS-1$
    	}

    	// Codigo para programar el volcado de estadisticas a BD si procede
    	final int configStatistic = ConfigManager.getStatisticsPolicy();
    	if (configStatistic == 2) {
    		try {
    			final String statisticsDirPath = ConfigManager.getStatisticsDir();
    			final String jdbcDriver = ConfigManager.getJdbcDriverString();
    			final String dbConnectionString = ConfigManager.getDataBaseConnectionString();
    			final String dbUser = ConfigManager.getDataBaseUsername();
    			final String dbPass = ConfigManager.getDataBasePassword();
    			if (statisticsDirPath != null && !statisticsDirPath.isEmpty() &&
    					jdbcDriver != null && !jdbcDriver.isEmpty() &&
    					dbConnectionString != null && !dbConnectionString.isEmpty()) {
    				final String startTime = ConfigManager.getStatisticsDumpTime();
    				FireStatistics.init(statisticsDirPath, startTime, jdbcDriver, dbConnectionString, dbUser, dbPass, false);
    			}
    		}
    		catch (final Exception e) {
    			LOGGER.warning("Error al configurar el volcado de estadisticas. No se generaran: " + e); //$NON-NLS-1$
    		}
    	}

    	LOGGER.info("Componente central de FIRe cargado correctamente"); //$NON-NLS-1$
    }

	@Override
	protected void service(final HttpServletRequest request, final HttpServletResponse response) {

		LOGGER.fine("Nueva peticion entrante"); //$NON-NLS-1$

		if (!ConfigManager.isInitialized()) {
			try {
				ConfigManager.checkConfiguration();
			}
	    	catch (final ConfigFilesException e) {
	    		LOGGER.log(Level.SEVERE, "No se encontro el fichero de configuracion del componente central: " + e.getFileName(), e); //$NON-NLS-1$
	    		AlarmsManager.notify(Alarm.RESOURCE_NOT_FOUND, e.getMessage());
	    		Responser.sendError(response, FIReError.INTERNAL_ERROR);
	    		return;
	    	}
	    	catch (final InvalidConfigurationException e) {
	    		LOGGER.log(Level.SEVERE, "Error en la configuracion de la/s propiedad/es " + e.getProperty() + " (" + e.getFileName() + ")", e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	    		AlarmsManager.notify(Alarm.RESOURCE_CONFIG, e.getProperty(), e.getFileName());
	    		Responser.sendError(response, FIReError.INTERNAL_ERROR);
	    		return;
	    	}
		}

		RequestParameters params;
		try {
			params = RequestParameters.extractParameters(request);
		}
		catch (final Exception e) {
			LOGGER.log(Level.WARNING, "Error en la lectura de los parametros de entrada", e); //$NON-NLS-1$
			Responser.sendError(response, FIReError.READING_PARAMETERS);
			return;
		}

    	final String appId     = params.getParameter(ServiceParams.HTTP_PARAM_APPLICATION_ID);
        final String operation = params.getParameter(ServiceParams.HTTP_PARAM_OPERATION);
        final String trId      = params.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID);

        final TransactionAuxParams trAux = new TransactionAuxParams(appId, trId);
		final LogTransactionFormatter logF = trAux.getLogFormatter();

		// El identificador de aplicacion es obligatorio, incluso si no es necesario
		// validarlo posteriormente
    	if (appId == null || appId.isEmpty()) {
    		LOGGER.warning(logF.f("No se ha proporcionado el identificador de la aplicacion en una peticion entrante")); //$NON-NLS-1$
            Responser.sendError(response, FIReError.PARAMETER_APP_ID_NEEDED);
            return;
        }

    	// Comprobamos que la peticion este autorizada
    	ApplicationInfo appInfo;
    	try {
    		appInfo = ServiceUtil.checkAccess(appId, request, trAux);
    	}
    	catch (final IOException e) {
    		LOGGER.log(Level.SEVERE, logF.f("Error interno al validar la peticion"), e); //$NON-NLS-1$
            Responser.sendError(response, FIReError.INTERNAL_ERROR);
            return;
		}
    	catch (final CertificateValidationException e) {
    		LOGGER.log(Level.WARNING, logF.f("Error al validar el certificado cliente"), e); //$NON-NLS-1$
            Responser.sendError(response, e.getError());
            return;
		}
    	catch (final UnauthorizedApplicacionException e) {
    		LOGGER.log(Level.WARNING, logF.f("Acceso denegado: ") + e); //$NON-NLS-1$
            Responser.sendError(response, FIReError.UNAUTHORIZED);
            return;
		}
    	catch (final Exception e) {
    		LOGGER.log(Level.SEVERE, logF.f("Error desconocido al validar la peticion"), e); //$NON-NLS-1$
            Responser.sendError(response, FIReError.INTERNAL_ERROR);
            return;
		}

    	LOGGER.fine(logF.f("Peticion autorizada")); //$NON-NLS-1$

        if (operation == null || operation.isEmpty()) {
            LOGGER.warning(logF.f("No se ha indicado la operacion a realizar en servidor")); //$NON-NLS-1$
            Responser.sendError(response, FIReError.PARAMETER_OPERATION_NEEDED);
            return;
        }

        FIReServiceOperation op;
        try {
        	op = FIReServiceOperation.parse(operation);
        }
        catch (final Exception e) {
            LOGGER.warning(logF.f("Se ha indicado un id de operacion incorrecto: " + e)); //$NON-NLS-1$
            Responser.sendError(response, FIReError.PARAMETER_OPERATION_NOT_SUPPORTED);
            return;
		}

    	LOGGER.info(logF.f("Peticion de tipo " + op)); //$NON-NLS-1$

    	try {
    		switch (op) {
    		case SIGN:
    			SignOperationManager.sign(request, appInfo.getName(), params, trAux, response);
    			break;
    		case RECOVER_SIGN:
    			RecoverSignManager.recoverSignature(params, trAux, response);
    			break;
    		case RECOVER_SIGN_RESULT:
    			RecoverSignResultManager.recoverSignature(params, trAux, response);
    			break;
    		case CREATE_BATCH:
    			CreateBatchManager.createBatch(request, appInfo.getName(), params, trAux, response);
    			break;
    		case ADD_DOCUMENT_TO_BATCH:
    			AddDocumentBatchManager.addDocument(params, trAux, response);
    			break;
    		case SIGN_BATCH:
    			SignBatchManager.signBatch(request, params, trAux, response);
    			break;
    		case RECOVER_BATCH:
    			RecoverBatchResultManager.recoverResult(params, trAux, response);
    			break;
    		case RECOVER_BATCH_STATE:
    			RecoverBatchStateManager.recoverState(params, trAux, response);
    			break;
    		case RECOVER_SIGN_BATCH:
    			RecoverBatchSignatureManager.recoverSignature(params, trAux, response);
    			break;
    		case RECOVER_ERROR:
    			RecoverErrorManager.recoverError(params, trAux, response);
    			break;
    		case RECOVER_UPDATED_SIGN:
    			RecoverUpdatedSignManager.recoverSignature(params, trAux, response);
    			break;
    		case RECOVER_UPDATED_SIGN_RESULT:
    			RecoverUpdatedSignResultManager.recoverSignature(params, trAux, response);
    			break;
    		default:
    			LOGGER.warning(logF.f("Se ha enviado una peticion con una operacion no soportada: " + op.name())); //$NON-NLS-1$
    			Responser.sendError(response, FIReError.PARAMETER_OPERATION_NOT_SUPPORTED);
    			break;
    		}
    	}
    	catch (final Exception e) {
    		// Las operaciones solo lanzan una excepcion al exterior si no queda mas remedio.
    		// De norma, deberian responder directamente con su propio mensaje de error.
    		LOGGER.log(Level.WARNING, logF.f("Ocurrio un error no recuperable durante la ejecucion de la operacion"), e); //$NON-NLS-1$
    		Responser.sendError(response, FIReError.INTERNAL_ERROR);
    		return;
    	}
	}

	@Override
	public void destroy() {
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
