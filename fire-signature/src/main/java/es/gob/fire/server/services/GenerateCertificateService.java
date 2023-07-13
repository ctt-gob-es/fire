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
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import es.gob.fire.alarms.Alarm;
import es.gob.fire.server.connector.FIReCertificateAvailableException;
import es.gob.fire.server.connector.FIReCertificateException;
import es.gob.fire.server.connector.FIReConnectorFactoryException;
import es.gob.fire.server.connector.FIReConnectorNetworkException;
import es.gob.fire.server.connector.FIReConnectorUnknownUserException;
import es.gob.fire.server.connector.GenerateCertificateResult;
import es.gob.fire.server.connector.WeakRegistryException;
import es.gob.fire.server.services.internal.AlarmsManager;
import es.gob.fire.server.services.internal.GenerateCertificateManager;
import es.gob.fire.server.services.internal.LogTransactionFormatter;
import es.gob.fire.server.services.internal.ServiceParams;
import es.gob.fire.server.services.internal.TransactionAuxParams;
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
    	final String transactionId	= params.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID);

    	final TransactionAuxParams trAux = new TransactionAuxParams(appId, transactionId);
		final LogTransactionFormatter logF = trAux.getLogFormatter();

    	// Comprobamos que la peticion este autorizada
    	try {
    		ServiceUtil.checkAccess(appId, request, trAux);
    	}
    	catch (final IOException e) {
    		LOGGER.log(Level.SEVERE, logF.f("Error interno al validar la peticion"), e); //$NON-NLS-1$
            Responser.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
		}
    	catch (final CertificateValidationException | IllegalArgumentException e) {
    		LOGGER.log(Level.WARNING, logF.f("Error al validar la peticion"), e); //$NON-NLS-1$
            Responser.sendError(response, HttpServletResponse.SC_BAD_REQUEST);
            return;
		}
    	catch (final UnauthorizedApplicacionException e) {
    		LOGGER.log(Level.WARNING, logF.f("Acceso denegado: ") + e); //$NON-NLS-1$
            Responser.sendError(response, HttpServletResponse.SC_UNAUTHORIZED);
            return;
		}
    	catch (final Exception e) {
    		LOGGER.log(Level.SEVERE, logF.f("Error desconocido al validar la peticion"), e); //$NON-NLS-1$
            Responser.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
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
    	generateCertificate(params, response, trAux);
    }

    private static void updateParamNames(final RequestParameters params) {
    	params.replaceParamKey(OLD_PARAMETER_NAME_APPLICATION_ID, PARAMETER_NAME_APPLICATION_ID);
    	params.replaceParamKey(OLD_PARAMETER_NAME_SUBJECT_ID, PARAMETER_NAME_SUBJECT_ID);
    }

    /**
	 * Ejecuta una operacion de generaci&oacute;n de certificado en servidor.
	 * @param params Par&aacute;metros extra&iacute;dos de la petici&oacute;n.
	 * @param response Respuesta HTTP de generaci&oacute;n de certificado.
	 */
	private static void generateCertificate(
			final RequestParameters params,
            final HttpServletResponse response,
            final TransactionAuxParams trAux) {

        final String subjectId      = params.getParameter(ServiceParams.HTTP_PARAM_SUBJECT_ID);
		final String providerName	= params.getParameter(ServiceParams.HTTP_PARAM_CERT_ORIGIN);
		final String configB64      = params.getParameter(ServiceParams.HTTP_PARAM_CONFIG);

		final LogTransactionFormatter logF =  trAux.getLogFormatter();

		// Comprobamos del usuario
    	if (subjectId == null || subjectId.isEmpty()) {
        	LOGGER.warning(logF.f("No se ha proporcionado el identificador del usuario que solicita el certificado")); //$NON-NLS-1$
        	Responser.sendError(response, FIReError.PARAMETER_USER_ID_NEEDED);
        	return;
        }

		Properties config = null;
    	if (configB64 != null && !configB64.isEmpty()) {
    		try {
    			config = ServiceUtil.base642Properties(configB64);
    		}
    		catch (final Exception e) {
    			LOGGER.log(Level.SEVERE, logF.f("Error al decodificar las configuracion de los proveedores de firma"), e); //$NON-NLS-1$
    			Responser.sendError(response, FIReError.PARAMETER_CONFIG_TRANSACTION_INVALID);
    			return;
    		}
    	}

        final GenerateCertificateResult gcr;
        try {
        	gcr = GenerateCertificateManager.generateCertificate(providerName, subjectId, config);
        }
        catch (final FIReConnectorFactoryException e) {
        	LOGGER.log(Level.SEVERE, logF.f("No se ha podido cargar el conector del proveedor de firma: %1s", providerName), e); //$NON-NLS-1$
        	Responser.sendError(response, FIReError.INTERNAL_ERROR);
        	return;
        }
        catch (final FIReConnectorUnknownUserException e) {
        	LOGGER.log(Level.SEVERE, logF.f("El usuario %1s no esta dado de alta en el proveedor de firma en la nube %2s", subjectId, providerName), e); //$NON-NLS-1$
			AlarmsManager.notify(Alarm.CONNECTION_SIGNATURE_PROVIDER, providerName);
        	Responser.sendError(response, HttpCustomErrors.NO_USER.getErrorCode(), HttpCustomErrors.NO_USER.getErrorDescription());
        	return;
        }
        catch (final FIReConnectorNetworkException e) {
        	LOGGER.log(Level.SEVERE, logF.f("No se ha podido conectar con el proveedor de firma en la nube"), e); //$NON-NLS-1$
			AlarmsManager.notify(Alarm.CONNECTION_SIGNATURE_PROVIDER, providerName);
        	Responser.sendError(response, FIReError.PROVIDER_INACCESIBLE_SERVICE);
        	return;
        }
        catch (final WeakRegistryException e) {
        	LOGGER.log(Level.WARNING, logF.f("El usuario realizo un registro debil y no puede tener certificados de firma"), e); //$NON-NLS-1$
        	Responser.sendError(response, HttpCustomErrors.WEAK_REGISTRY.getErrorCode(), HttpCustomErrors.WEAK_REGISTRY.getErrorDescription());
    		return;
        }
        catch (final FIReCertificateAvailableException e) {
        	LOGGER.log(Level.SEVERE, logF.f("El usuario ya dispone de todos los certificados posibles de ese tipo"), e); //$NON-NLS-1$
       		Responser.sendError(response, HttpCustomErrors.CERTIFICATE_AVAILABLE.getErrorCode(), HttpCustomErrors.CERTIFICATE_AVAILABLE.getErrorDescription());
       		return;
        }
        catch (final FIReCertificateException e) {
        	LOGGER.log(Level.SEVERE, logF.f("El certificado obtenido no es valido"), e); //$NON-NLS-1$
        	Responser.sendError(response, FIReError.CERTIFICATE_ERROR);
        	return;
        }
        catch (final Exception e) {
        	LOGGER.log(Level.SEVERE, logF.f("Error desconocido en la generacion del certificado"), e);//$NON-NLS-1$
        	Responser.sendError(response, FIReError.PROVIDER_ERROR);
        	return;
        }

        Responser.sendResult(response, gcr);
	}
}
