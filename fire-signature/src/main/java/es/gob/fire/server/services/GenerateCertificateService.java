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
import es.gob.fire.server.services.internal.LogTransactionFormatter;
import es.gob.fire.server.services.internal.ServiceParams;
import es.gob.fire.signature.ConfigFilesException;
import es.gob.fire.signature.ConfigManager;
import es.gob.fire.signature.InvalidConfigurationException;

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

    	// Configuramos el modulo de alarmas
    	AlarmsManager.init(ModuleConstants.MODULE_NAME, ConfigManager.getAlarmsNotifierClassName());
    }

    /** Solicitud de un nuevo certificado de firma. */
    @Override
    protected void service(final HttpServletRequest request,
    		               final HttpServletResponse response) {

		LOGGER.fine("Peticion recibida"); //$NON-NLS-1$

		if (!ConfigManager.isInitialized()) {
			try {
				ConfigManager.checkConfiguration();
			}
	    	catch (final ConfigFilesException e) {
	    		LOGGER.log(Level.SEVERE, "No se encontro el fichero de configuracion del componente central: " + e.getFileName(), e); //$NON-NLS-1$
	    		AlarmsManager.notify(Alarm.RESOURCE_NOT_FOUND, e.getMessage());
	    		Responser.sendError(response, ConfigFilesException.getHttpError(), e.getMessage());
	    		return;
	    	}
	    	catch (final InvalidConfigurationException e) {
	    		LOGGER.log(Level.SEVERE, "Error en la configuracion de la propiedad " + e.getProperty() + " (" + e.getFileName() + ")", e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	    		AlarmsManager.notify(Alarm.RESOURCE_CONFIG, e.getProperty(), e.getFileName());
	    		Responser.sendError(response, InvalidConfigurationException.getHttpError(), e.getMessage());
	    		return;
	    	}
		}

    	final RequestParameters params;
    	try {
    		params = RequestParameters.extractParameters(request);
    	}
    	catch (final Exception e) {
    		LOGGER.log(Level.WARNING, "Error en la lectura de los parametros de entrada", e); //$NON-NLS-1$
    		Responser.sendError(response, HttpServletResponse.SC_BAD_REQUEST);
    		return;
		}

    	updateParamNames(params);

    	final String appId = params.getParameter(PARAMETER_NAME_APPLICATION_ID);

		final LogTransactionFormatter logF = new LogTransactionFormatter(appId, null);

    	// Comprobacion de la aplicacion solicitante
        if (ConfigManager.isCheckApplicationNeeded()) {
        	LOGGER.fine(logF.f("Se realizara la validacion del Id de aplicacion")); //$NON-NLS-1$
        	if (appId == null || appId.length() == 0) {
        		LOGGER.warning(logF.f("No se ha proporcionado el identificador de la aplicacion")); //$NON-NLS-1$
        		Responser.sendError(response,
            		HttpServletResponse.SC_BAD_REQUEST,
                    "No se ha proporcionado el identificador de la aplicacion" //$NON-NLS-1$
        		);
                return;
            }

        	try {
        		ServiceUtil.checkValidApplication(appId);
	        }
	        catch (final DBConnectionException e) {
	        	LOGGER.log(Level.SEVERE, logF.f("No se pudo conectar con la base de datos"), e); //$NON-NLS-1$
	        	AlarmsManager.notify(Alarm.CONNECTION_DB);
	        	Responser.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	        	return;
	        }
        	catch (final Exception e) {
				LOGGER.log(Level.SEVERE, logF.f("La aplicacion que solicita la peticion no es valida o esta desactivada"), e); //$NON-NLS-1$
				Responser.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}
        }
        else {
        	LOGGER.fine(logF.f("No se realiza la validacion de aplicacion en la base de datos")); //$NON-NLS-1$
        }

    	if (ConfigManager.isCheckCertificateNeeded()) {
    		LOGGER.fine(logF.f("Se realizara la validacion del certificado")); //$NON-NLS-1$
    		final X509Certificate[] certificates = ServiceUtil.getCertificatesFromRequest(request);
	    	try {
				ServiceUtil.checkValidCertificate(appId, certificates);
			}
	    	catch (final DBConnectionException e) {
				LOGGER.log(Level.SEVERE, logF.f("No se pudo conectar con la base de datos"), e); //$NON-NLS-1$
				AlarmsManager.notify(Alarm.CONNECTION_DB);
				Responser.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
				return;
			}
	    	catch (final CertificateValidationException e) {
				LOGGER.log(Level.SEVERE, logF.f("Error en la validacion del certificado: ") + e, e); //$NON-NLS-1$
				Responser.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
				return;
			}
    	}
    	else {
    		LOGGER.fine(logF.f("No se validara el certificado"));//$NON-NLS-1$
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
