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
import es.gob.fire.server.connector.FIReCertificateException;
import es.gob.fire.server.connector.FIReConnectorFactoryException;
import es.gob.fire.server.connector.FIReConnectorNetworkException;
import es.gob.fire.server.services.internal.AlarmsManager;
import es.gob.fire.server.services.internal.LogTransactionFormatter;
import es.gob.fire.server.services.internal.PropertiesUtils;
import es.gob.fire.server.services.internal.RecoverCertificateManager;
import es.gob.fire.server.services.internal.ServiceParams;
import es.gob.fire.server.services.internal.TransactionAuxParams;
import es.gob.fire.signature.ConfigFilesException;
import es.gob.fire.signature.ConfigManager;
import es.gob.fire.signature.InvalidConfigurationException;

/** Servlet que recupera un certificado recien creado. */
public final class RecoverCertificateService extends HttpServlet {

    /** Serial ID. */
	private static final long serialVersionUID = -2818829387993970068L;

	private static final Logger LOGGER = Logger.getLogger(RecoverCertificateService.class.getName());

    // Parametros que necesitamos de la URL. Se mantiene por compatibilidad ya que en las nuevas
	// versiones se utiliza "appid"
    private static final String PARAMETER_NAME_APPLICATION_ID = "appid"; //$NON-NLS-1$
    private static final String OLD_PARAMETER_NAME_APPLICATION_ID = "appId"; //$NON-NLS-1$

    private static final String PARAMETER_NAME_TRANSACTION_ID = "transactionid"; //$NON-NLS-1$
    private static final String OLD_PARAMETER_NAME_TRANSACTION_ID = "transactionId"; //$NON-NLS-1$

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

    /** Recepci&oacute;n de la petici&oacute;n GET y realizaci&oacute;n de la
     * firma. */
    @Override
    protected void service(final HttpServletRequest request,
    					   final HttpServletResponse response) {

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
	    		LOGGER.log(Level.SEVERE, "Error en la configuracion de la/s propiedad/es " + e.getProperty() + " (" + e.getFileName() + ")", e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	    		AlarmsManager.notify(Alarm.RESOURCE_CONFIG, e.getProperty(), e.getFileName());
	    		Responser.sendError(response, InvalidConfigurationException.getHttpError(), e.getMessage());
	    		return;
	    	}
		}

    	RequestParameters params;
    	try {
    		params = RequestParameters.extractParameters(request);
    	}
    	catch (final Exception e) {
			LOGGER.log(Level.WARNING, "Error en la lectura de los parametros de entrada", e); //$NON-NLS-1$
			Responser.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Error en la lectura de los parametros de entrada"); //$NON-NLS-1$
			return;
		}
		updateLegacyKeys(params);

    	final String appId = params.getParameter(PARAMETER_NAME_APPLICATION_ID);
    	final String trId = params.getParameter(PARAMETER_NAME_TRANSACTION_ID);

    	final TransactionAuxParams trAux = new TransactionAuxParams(LogUtils.limitText(appId), LogUtils.limitText(trId));
    	final LogTransactionFormatter logF = trAux.getLogFormatter();

    	LOGGER.fine(logF.f("Inicio de la llamada al servicio publico de recuperacion del certificado generado")); //$NON-NLS-1$

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
    	recoverCertificate(params, response);

        LOGGER.fine(logF.f("Fin de la llamada al servicio publico de recuperacion del certificado generado")); //$NON-NLS-1$
    }

    /**
     * Sustituye los nombres de par&aacute;metro utilizados por versiones anteriores,
     * por el nombre actual.
     * @param params Par&aacute;metros extra&iacute;dos de la petici&oacute;n.
     */
    private static void updateLegacyKeys(final RequestParameters params) {
    	params.replaceParamKey(OLD_PARAMETER_NAME_APPLICATION_ID, PARAMETER_NAME_APPLICATION_ID);
    	params.replaceParamKey(OLD_PARAMETER_NAME_TRANSACTION_ID, PARAMETER_NAME_TRANSACTION_ID);
    }


	/**
	 * Ejecuta una operaci&oacute;n de recuperaci&oacute;n del certificado generado
	 * en servidor. Este m&eacute;todo s&oacute;lo se utiliza desde el servicio Legacy
	 * de Cl@ve Firma.
	 * @param params Par&aacute;metros extra&iacute;dos de la petici&oacute;n.
	 * @param response Respuesta HTTP de generaci&oacute;n de certificado.
	 */
	private static void recoverCertificate(
			final RequestParameters params,
            final HttpServletResponse response) {

		final String appId = params.getParameter(ServiceParams.HTTP_PARAM_APPLICATION_ID);
        final String transactionId = params.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID);
        final String configB64  = params.getParameter(ServiceParams.HTTP_PARAM_CONFIG);
        final String configuredProvider  = params.getParameter(ServiceParams.HTTP_PARAM_CERT_ORIGIN);

		final LogTransactionFormatter logF = new LogTransactionFormatter(appId, transactionId);

		// Comprobamos que se hayan proporcionado los parametros indispensables
        if (transactionId == null || transactionId.isEmpty()) {
        	LOGGER.warning(logF.f("No se ha proporcionado el ID de transaccion")); //$NON-NLS-1$
        	Responser.sendError(response, HttpServletResponse.SC_BAD_REQUEST, FIReError.PARAMETER_TRANSACTION_ID_NEEDED);
            return;
        }

		LOGGER.fine(logF.f("Peticion bien formada")); //$NON-NLS-1$

    	Properties config = null;
    	if (configB64 != null && !configB64.isEmpty()) {
    		try {
    			config = PropertiesUtils.base642Properties(configB64);
    		}
    		catch (final Exception e) {
    			LOGGER.log(Level.SEVERE, logF.f("Error al decodificar las configuracion de los proveedores de firma"), e); //$NON-NLS-1$
    			Responser.sendError(response, HttpServletResponse.SC_BAD_REQUEST, FIReError.PARAMETER_CONFIG_TRANSACTION_INVALID);
    			return;
    		}
    	}

    	String providerName = configuredProvider;
    	if (providerName == null) {
    		providerName = ProviderLegacy.PROVIDER_NAME_CLAVEFIRMA;
    	}

    	LOGGER.info(logF.f("Recuperamos el certificado de usuario")); //$NON-NLS-1$

    	byte[] newCertEncoded;
        try {
        	newCertEncoded = RecoverCertificateManager.recoverCertificate(providerName, transactionId, config, logF);
        }
        catch (final FIReConnectorFactoryException e) {
        	LOGGER.log(Level.SEVERE, logF.f("No se ha podido cargar el conector del proveedor de firma: %1s", LogUtils.cleanText(providerName)), e); //$NON-NLS-1$
        	Responser.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        catch (final FIReConnectorNetworkException e) {
        	LOGGER.log(Level.SEVERE, logF.f("No se ha podido conectar con el proveedor de firma en la nube"), e); //$NON-NLS-1$
        	AlarmsManager.notify(Alarm.CONNECTION_SIGNATURE_PROVIDER, providerName);
        	Responser.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, FIReError.PROVIDER_INACCESIBLE_SERVICE);
            return;
        }
        catch (final FIReCertificateException e) {
            LOGGER.log(Level.SEVERE, logF.f("Error en la generacion del certificado"), e); //$NON-NLS-1$
            Responser.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, FIReError.CERTIFICATE_ERROR);
            return;
        }
        catch (final Exception e) {
            LOGGER.log(Level.SEVERE, logF.f("Error desconocido del proveedor de firma en la nube al recuperar un certificado"), e); //$NON-NLS-1$
            Responser.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,  FIReError.PROVIDER_ERROR);
            return;
        }

        LOGGER.info(logF.f("Devolvemos el certificado generado")); //$NON-NLS-1$

        Responser.sendResult(response, newCertEncoded);
	}
}
