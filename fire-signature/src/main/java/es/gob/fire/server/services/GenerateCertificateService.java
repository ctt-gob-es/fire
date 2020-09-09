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

import es.gob.fire.alarms.Alarm;
import es.gob.fire.server.services.internal.AlarmsManager;
import es.gob.fire.server.services.internal.GenerateCertificateManager;
import es.gob.fire.server.services.internal.ServiceParams;
import es.gob.fire.signature.AplicationsDAO;
import es.gob.fire.signature.ApplicationChecking;
import es.gob.fire.signature.ConfigFilesException;
import es.gob.fire.signature.ConfigManager;
import es.gob.fire.signature.DBConnectionException;

/** Servicio para la solicitud de un nuevo certificado de firma. */
public final class GenerateCertificateService extends HttpServlet {

    /** Serial ID. */
	private static final long serialVersionUID = -6991704995319197156L;

	private static final Logger LOGGER = Logger.getLogger(GenerateCertificateService.class.getName());

    private static final String PARAMETER_NAME_APPLICATION_ID = "appid"; //$NON-NLS-1$
    private static final String OLD_PARAMETER_NAME_APPLICATION_ID = "appId"; //$NON-NLS-1$

    private static final String PARAMETER_NAME_SUBJECT_ID = "subjectid"; //$NON-NLS-1$
    private static final String OLD_PARAMETER_NAME_SUBJECT_ID = "subjectId"; //$NON-NLS-1$

    @Override
    public void init() throws ServletException {
    	super.init();

    	try {
	    	ConfigManager.checkConfiguration();
		}
    	catch (final Exception e) {
    		LOGGER.log(Level.SEVERE, "Error al cargar la configuracion", e); //$NON-NLS-1$
    		final String configFile = e instanceof ConfigFilesException ?
    				((ConfigFilesException) e).getFileName() : "Fichero de configuracion principal del componente central"; //$NON-NLS-1$
    		AlarmsManager.notify(Alarm.RESOURCE_CONFIG, configFile);
    		return;
    	}

    	// Configuramos el modulo de alarmas
    	AlarmsManager.init(ModuleConstants.MODULE_NAME, ConfigManager.getAlarmsNotifierClassName());
    }

    /** Solicitud de un nuevo certificado de firma. */
    @Override
    protected void service(final HttpServletRequest request,
    		               final HttpServletResponse response) throws ServletException, IOException {

		LOGGER.fine("Peticion recibida"); //$NON-NLS-1$

		if (!ConfigManager.isInitialized()) {
			try {
				ConfigManager.checkConfiguration();
			}
			catch (final ConfigFilesException e) {
				LOGGER.severe("Error en la configuracion del servidor: " + e); //$NON-NLS-1$
				AlarmsManager.notify(Alarm.RESOURCE_CONFIG, e.getFileName());
				response.sendError(ConfigFilesException.getHttpError(), e.getMessage());
				return;
			}
		}

    	final RequestParameters params = RequestParameters.extractParameters(request);

    	updateParamNames(params);

    	final String appId = params.getParameter(PARAMETER_NAME_APPLICATION_ID);

    	// Comprobacion de la aplicacion solicitante
        if (ConfigManager.isCheckApplicationNeeded()) {
        	LOGGER.fine("Se realizara la validacion del Id de aplicacion"); //$NON-NLS-1$
        	if (appId == null || appId.length() == 0) {
        		LOGGER.warning("No se ha proporcionado el identificador de la aplicacion"); //$NON-NLS-1$
                response.sendError(
            		HttpServletResponse.SC_BAD_REQUEST,
                    "No se ha proporcionado el identificador de la aplicacion" //$NON-NLS-1$
        		);
                return;
            }

	        try {
	        	final ApplicationChecking appCheck = AplicationsDAO.checkApplicationId(appId);
	        	if (!appCheck.isValid()) {
	        		LOGGER.warning("Se proporciono un identificador de aplicacion no valido. Se rechaza la peticion"); //$NON-NLS-1$
	        		response.sendError(HttpServletResponse.SC_FORBIDDEN);
	        		return;
	        	}
	        }
	        catch (final Exception e) {
	        	LOGGER.log(Level.SEVERE, "Error grave al validar el identificador de la aplicacion", e); //$NON-NLS-1$
	        	AlarmsManager.notify(Alarm.CONNECTION_DB);
	        	response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	        	return;
	        }
        }
        else {
        	LOGGER.fine("No se realiza la validacion de aplicacion en la base de datos"); //$NON-NLS-1$
        }

    	if (ConfigManager.isCheckCertificateNeeded()) {
    		LOGGER.fine("Se realizara la validacion del certificado"); //$NON-NLS-1$
    		final X509Certificate[] certificates = ServiceUtil.getCertificatesFromRequest(request);
	    	try {
				ServiceUtil.checkValidCertificate(appId, certificates);
			}
	    	catch (final DBConnectionException e) {
				LOGGER.log(Level.SEVERE, "No se pudo conectar con la base de datos", e); //$NON-NLS-1$
				AlarmsManager.notify(Alarm.CONNECTION_DB);
	        	response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
				return;
			}
	    	catch (final CertificateValidationException e) {
				LOGGER.severe("Error en la validacion del certificado: " + e); //$NON-NLS-1$
				response.sendError(e.getHttpError(), e.getMessage());
				return;
			}
    	}
    	else {
    		LOGGER.fine("No se validara el certificado");//$NON-NLS-1$
    	}

    	// Comprobamos si se indica un proveedor y, si no, se utiliza el
    	// por defecto de Clave Firma
    	String certOrigin = params.getParameter(ServiceParams.HTTP_PARAM_CERT_ORIGIN);
    	if (certOrigin == null) {
    		certOrigin = ProviderLegacy.PROVIDER_NAME_CLAVEFIRMA;
    	}
    	params.put(ServiceParams.HTTP_PARAM_CERT_ORIGIN, certOrigin);

    	// Una vez realizadas las comprobaciones de seguridad y envio de estadisticas,
    	// delegamos el procesado de la operacion
    	GenerateCertificateManager.generateCertificate(params, response);
    }

    private static void updateParamNames(final RequestParameters params) {
    	params.replaceParamKey(OLD_PARAMETER_NAME_APPLICATION_ID, PARAMETER_NAME_APPLICATION_ID);
    	params.replaceParamKey(OLD_PARAMETER_NAME_SUBJECT_ID, PARAMETER_NAME_SUBJECT_ID);
    }
}
