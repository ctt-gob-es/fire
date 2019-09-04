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
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import es.gob.fire.server.services.internal.AddDocumentBatchManager;
import es.gob.fire.server.services.internal.CreateBatchManager;
import es.gob.fire.server.services.internal.LogTransactionFormatter;
import es.gob.fire.server.services.internal.RecoverBatchResultManager;
import es.gob.fire.server.services.internal.RecoverBatchSignatureManager;
import es.gob.fire.server.services.internal.RecoverBatchStateManager;
import es.gob.fire.server.services.internal.RecoverErrorManager;
import es.gob.fire.server.services.internal.RecoverSignManager;
import es.gob.fire.server.services.internal.RecoverSignResultManager;
import es.gob.fire.server.services.internal.ServiceParams;
import es.gob.fire.server.services.internal.SignBatchManager;
import es.gob.fire.server.services.internal.SignOperationManager;
import es.gob.fire.signature.AplicationsDAO;
import es.gob.fire.signature.ApplicationChecking;
import es.gob.fire.signature.ConfigManager;
import es.gob.fire.statistics.FireStatistics;

/** Servicio central de FIRe que integra las funciones de firma a traves del Cliente @firma
 * y proveedores de firma en la nube. */
public final class FIReService extends HttpServlet {

	/** Serial Id. */
	private static final long serialVersionUID = -2304782878707695769L;

	private static final Logger LOGGER = Logger.getLogger(FIReService.class.getName());

    @Override
    public void init() throws ServletException {

    	super.init();

    	// Configuramos los logs
    	try {
    		FireLogManager.configureLogs();
    	}
		catch(final Throwable e) {
			LOGGER.warning(
				"No se pudo configurar la salida de los logs de FIRe a un fichero externo: " + e //$NON-NLS-1$
			);
		}

    	// Codigo para programar el volcado de estadisticas a BD si procede
		try {
			final int configStatistic = Integer.valueOf(ConfigManager.getStatisticsPolicy()).intValue() ;
			final String statisticsDirPath = ConfigManager.getStatisticsDir();
			final String jdbcDriver = ConfigManager.getJdbcDriverString();
			final String dbConnectionString = ConfigManager.getDataBaseConnectionString();
			if (configStatistic == 2 &&
					statisticsDirPath != null && !statisticsDirPath.isEmpty() &&
					jdbcDriver != null && !jdbcDriver.isEmpty() &&
					dbConnectionString != null && !dbConnectionString.isEmpty()) {
				final String startTime = ConfigManager.getStatisticsDumpTime();
				FireStatistics.init(statisticsDirPath, startTime, jdbcDriver, dbConnectionString, false);
			}
		}
		catch (final Exception e) {
			LOGGER.warning("Error al cargar la configuracion de estadisticas. No se generaran: " + e); //$NON-NLS-1$
		}

    	LOGGER.info("Componente central de FIRe cargado correctamente"); //$NON-NLS-1$
    }

	@Override
	protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

		LOGGER.fine("Nueva peticion entrante"); //$NON-NLS-1$

		final RequestParameters params = RequestParameters.extractParameters(request);

    	final String appId     = params.getParameter(ServiceParams.HTTP_PARAM_APPLICATION_ID);
        final String operation = params.getParameter(ServiceParams.HTTP_PARAM_OPERATION);
        final String trId      = params.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID);

		final LogTransactionFormatter logF = new LogTransactionFormatter(appId, trId);

		String appName = null;

    	if (ConfigManager.isCheckApplicationNeeded()) {
        	LOGGER.fine(logF.format("Se realizara la validacion del Id de aplicacion")); //$NON-NLS-1$
        	if (appId == null || appId.isEmpty()) {
        		LOGGER.warning(logF.format("No se ha proporcionado el identificador de la aplicacion en una peticion entrante")); //$NON-NLS-1$
                response.sendError(
            		HttpServletResponse.SC_BAD_REQUEST,
                    "No se ha proporcionado el identificador de la aplicacion" //$NON-NLS-1$
        		);
                return;
            }

	        try {
	        	final ApplicationChecking appCheck = AplicationsDAO.checkApplicationId(appId);
	        	if (!appCheck.isValid()) {
	        		LOGGER.warning(logF.format("Se proporciono un identificador de aplicacion no valido. Se rechaza la peticion")); //$NON-NLS-1$
	        		response.sendError(HttpServletResponse.SC_FORBIDDEN);
	        		return;
	        	}
	        	if (!appCheck.isEnabled()) {
	        		LOGGER.warning(logF.format(String.format("La aplicacion %1s se encuentra desactivada. Se rechaza la peticion", appCheck.getName()))); //$NON-NLS-1$
	        		response.sendError(HttpServletResponse.SC_FORBIDDEN);
	        		return;
	        	}
	        	appName = appCheck.getName();
	        }
	        catch (final Exception e) {
	        	LOGGER.log(Level.SEVERE, logF.format("Ocurrio un error grave al validar el identificador de aplicacion enviado"), e); //$NON-NLS-1$
	        	response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	        	return;
	        }
        }
        else {
        	LOGGER.fine(logF.format("No se realiza la validacion del identificador de aplicacion")); //$NON-NLS-1$
        }


    	if (ConfigManager.isCheckCertificateNeeded()){
    		LOGGER.fine(logF.format("Se realizara la validacion del certificado")); //$NON-NLS-1$
    		final X509Certificate[] certificates = ServiceUtil.getCertificatesFromRequest(request);
	    	try {
				ServiceUtil.checkValidCertificate(appId, certificates);
			}
	    	catch (final CertificateValidationException e) {
				LOGGER.severe(logF.format("Error en la validacion del certificado: " + e)); //$NON-NLS-1$
				response.sendError(e.getHttpError(), e.getMessage());
				return;
			}
    	}
    	else {
    		LOGGER.fine(logF.format("No se valida el certificado"));//$NON-NLS-1$
    	}

    	LOGGER.fine(logF.format("Peticion autorizada")); //$NON-NLS-1$

        if (operation == null || operation.isEmpty()) {
            LOGGER.warning(logF.format("No se ha indicado la operacion a realizar en servidor")); //$NON-NLS-1$
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                "No se ha indicado la operacion a realizar" //$NON-NLS-1$
            );
            return;
        }

        final FIReServiceOperation op;
        try {
        	op = FIReServiceOperation.parse(operation);
        }
        catch (final Exception e) {
            LOGGER.warning(logF.format("Se ha indicado un id de operacion incorrecto: " + e)); //$NON-NLS-1$
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                "Se ha indicado un id de operacion incorrecto" //$NON-NLS-1$
            );
            return;
		}

    	LOGGER.info(logF.format("Peticion de tipo " + op.toString())); //$NON-NLS-1$

    	try {
    		switch (op) {
	    		case SIGN:
	    			SignOperationManager.sign(request, appName, params, response);
	    			break;
	    		case RECOVER_SIGN:
	    			RecoverSignManager.recoverSignature(params, response);
	    			break;
	    		case RECOVER_SIGN_RESULT:
	    			RecoverSignResultManager.recoverSignature(params, response);
	    			break;
	    		case CREATE_BATCH:
	    			CreateBatchManager.createBatch(request, appName, params, response);
	    			break;
	    		case ADD_DOCUMENT_TO_BATCH:
	    			AddDocumentBatchManager.addDocument(params, response);
	    			break;
	    		case SIGN_BATCH:
	    			SignBatchManager.signBatch(request, params, response);
	    			break;
	    		case RECOVER_BATCH:
	    			RecoverBatchResultManager.recoverResult(params, response);
	    			break;
	    		case RECOVER_BATCH_STATE:
	    			RecoverBatchStateManager.recoverState(params, response);
	    			break;
	    		case RECOVER_SIGN_BATCH:
	    			RecoverBatchSignatureManager.recoverSignature(params, response);
	    			break;
	    		case RECOVER_ERROR:
	    			RecoverErrorManager.recoverError(params, response);
	    			break;
	    		default:
	    			LOGGER.warning(logF.format("Se ha enviado una peticion con una operacion no soportada: " + op.name())); //$NON-NLS-1$
	    			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
	    			break;
    		}
    	}
    	catch (final Exception e) {
    		// Las operaciones solo lanzan una excepcion al exterior si no queda mas remedio.
    		// De norma, deberian responder directamente con su propio mensaje de error.
    		LOGGER.log(Level.WARNING, logF.format("Ocurrio un error no recuperable durante la ejecucion de la operacion"), e); //$NON-NLS-1$
    		response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    		return;
    	}
	}
}
