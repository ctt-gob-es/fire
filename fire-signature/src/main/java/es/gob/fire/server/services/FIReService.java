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
import es.gob.fire.server.services.internal.RecoverBatchResultManager;
import es.gob.fire.server.services.internal.RecoverBatchSignatureManager;
import es.gob.fire.server.services.internal.RecoverBatchStateManager;
import es.gob.fire.server.services.internal.RecoverErrorManager;
import es.gob.fire.server.services.internal.RecoverSignManager;
import es.gob.fire.server.services.internal.RecoverSignResultManager;
import es.gob.fire.server.services.internal.SignBatchManager;
import es.gob.fire.server.services.internal.SignOperationManager;
import es.gob.fire.signature.AplicationsDAO;
import es.gob.fire.signature.ConfigFilesException;
import es.gob.fire.signature.ConfigManager;
import es.gob.fire.signature.GoogleAnalitycs;

/**
 * Servicio central de FIRe que integra las funciones de firma a traves del Cliente @firma
 * y proveedores de firma en la nube.
 */
public class FIReService extends HttpServlet {

	/** Serial Id. */
	private static final long serialVersionUID = -2304782878707695769L;

	private final static Logger LOGGER = Logger.getLogger(FIReService.class.getName());

    // Parametros que necesitamos de la URL.
    private static final String PARAMETER_NAME_APPLICATION_ID = "appid"; //$NON-NLS-1$
    private static final String PARAMETER_NAME_OPERATION = "op"; //$NON-NLS-1$

    private static GoogleAnalitycs analytics = null;

    @Override
    public void init() throws ServletException {
    	super.init();

    	try {
	    	ConfigManager.checkConfiguration();
		}
    	catch (final Exception e) {
    		LOGGER.severe("Error al cargar la configuracion: " + e); //$NON-NLS-1$
    		return;
    	}

    	if (analytics == null && ConfigManager.getGoogleAnalyticsTrackingId() != null) {
    		try {
	        	analytics = new GoogleAnalitycs(
	        			ConfigManager.getGoogleAnalyticsTrackingId(),
	        			FIReService.class.getSimpleName());
    		}
    		catch(final Throwable e) {
    			LOGGER.warning(
					"No ha podido inicializarse Google Analytics: " + e //$NON-NLS-1$
				);
    		}
    	}
    }

	@Override
	protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

		if (!ConfigManager.isInitialized()) {
			try {
				ConfigManager.checkConfiguration();
			}
			catch (final ConfigFilesException e) {
				LOGGER.severe("Error en la configuracion del servidor: " + e); //$NON-NLS-1$
				response.sendError(ConfigFilesException.getHttpError(), e.getMessage());
				return;
			}
		}

		final RequestParameters params = RequestParameters.extractParameters(request);

    	final String appId     = params.getParameter(PARAMETER_NAME_APPLICATION_ID);
        final String operation = params.getParameter(PARAMETER_NAME_OPERATION);

    	if (ConfigManager.isCheckApplicationNeeded()) {

        	if (appId == null || appId.isEmpty()) {
        		LOGGER.warning("No se ha proporcionado el identificador de la aplicacion"); //$NON-NLS-1$
                response.sendError(
            		HttpServletResponse.SC_BAD_REQUEST,
                    "No se ha proporcionado el identificador de la aplicacion" //$NON-NLS-1$
        		);
                return;
            }

        	LOGGER.info("Se realizara la validacion de aplicacion en la base de datos"); //$NON-NLS-1$
	        try {
	        	if (!AplicationsDAO.checkApplicationId(appId)) {
	        		LOGGER.warning("Se proporciono un identificador de aplicacion no valido. Se rechaza la peticion"); //$NON-NLS-1$
	        		response.sendError(HttpServletResponse.SC_FORBIDDEN);
	        		return;
	        	}
	        }
	        catch (final Exception e) {
	        	LOGGER.severe("Ocurrio un error grave al validar el identificador de la aplicacion :" + e); //$NON-NLS-1$
	        	response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	        	return;
	        }
        }
        else {
        	LOGGER.warning("No se realiza la validacion de aplicacion en la base de datos"); //$NON-NLS-1$
        }

    	if (ConfigManager.isCheckCertificateNeeded()){
    		LOGGER.info("Se realizara la validacion del certificado"); //$NON-NLS-1$
    		final X509Certificate[] certificates = ServiceUtil.getCertificatesFromRequest(request);
	    	try {
				ServiceUtil.checkValidCertificate(appId, certificates);
			} catch (final CertificateValidationException e) {
				LOGGER.severe("Error en la validacion del certificado: " + e); //$NON-NLS-1$
				response.sendError(e.getHttpError(), e.getMessage());
				return;
			}
    	}
    	else {
    		LOGGER.warning("No se validara el certificado");//$NON-NLS-1$
    	}

        if (operation == null || operation.isEmpty()) {
            LOGGER.warning("No se ha indicado la operacion a realizar en servidor"); //$NON-NLS-1$
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                "No se ha indicado la operacion a realizar" //$NON-NLS-1$
            );
            return;
        }

        FIReServiceOperation op;
        try {
        	op = FIReServiceOperation.parse(operation);
        }
        catch (final Exception e) {
            LOGGER.warning("Se ha indicado un id de operacion incorrecto: + e"); //$NON-NLS-1$
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                "Se ha indicado un id de operacion incorrecto" //$NON-NLS-1$
            );
            return;
		}

    	if (analytics != null) {
    		analytics.trackRequest(request.getRemoteHost(), op.name());
    	}

    	try {
    		switch (op) {
    		case SIGN:
    			SignOperationManager.sign(request, params, response);
    			break;
    		case RECOVER_SIGN:
    			RecoverSignManager.recoverSignature(params, response);
    			break;
    		case RECOVER_SIGN_RESULT:
    			RecoverSignResultManager.recoverSignature(params, response);
    			break;
    		case CREATE_BATCH:
    			CreateBatchManager.createBatch(request, params, response);
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
    			LOGGER.warning("Operacion no soportada: " + op.name()); //$NON-NLS-1$
    			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
    			break;
    		}
    	}
    	catch (final Exception e) {
    		//TODO: Comprobar que los manager solo lanzan la excepcion si no queda mas remedio
    		LOGGER.log(Level.WARNING, "Ocurrio un error no recuperable durante la ejecucion de la operacion: " + e, e); //$NON-NLS-1$
    		response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    		return;
    	}
	}
}
