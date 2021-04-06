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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import es.gob.afirma.core.misc.Base64;
import es.gob.afirma.core.signers.TriphaseData;
import es.gob.fire.alarms.Alarm;
import es.gob.fire.server.connector.FIReConnector;
import es.gob.fire.server.connector.FIReConnectorFactoryException;
import es.gob.fire.server.connector.FIReConnectorNetworkException;
import es.gob.fire.server.connector.FIReConnectorUnknownUserException;
import es.gob.fire.server.connector.LoadResult;
import es.gob.fire.server.services.internal.AlarmsManager;
import es.gob.fire.server.services.internal.LogTransactionFormatter;
import es.gob.fire.server.services.internal.ProviderManager;
import es.gob.fire.server.services.internal.ServiceParams;
import es.gob.fire.signature.AplicationsDAO;
import es.gob.fire.signature.ApplicationChecking;
import es.gob.fire.signature.ConfigFilesException;
import es.gob.fire.signature.ConfigManager;
import es.gob.fire.signature.InvalidConfigurationException;

/** Servicio de carga de datos para su posterior firma en servidor.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
public final class LoadService extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(LoadService.class.getName());

    private static final String APPLICATION_ID_PARAM = "appId"; //$NON-NLS-1$
    private static final String PARAMETER_NAME_CONFIG = "config"; //$NON-NLS-1$
    private static final String PARAMETER_NAME_ALGORITHM = "algorithm"; //$NON-NLS-1$
    private static final String PARAMETER_NAME_SUBJECT_ID = "subjectId"; //$NON-NLS-1$
    private static final String PARAMETER_NAME_CERT = "cert"; //$NON-NLS-1$
    private static final String PARAMETER_NAME_EXTRA_PARAM = "properties"; //$NON-NLS-1$
    private static final String PARAMETER_NAME_OPERATION = "operation"; //$NON-NLS-1$
    private static final String PARAMETER_NAME_FORMAT = "format"; //$NON-NLS-1$
    private static final String PARAMETER_NAME_DATA = "dat"; //$NON-NLS-1$

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

    /** Carga los datos para su posterior firma en servidor.
     * @see HttpServlet#service(HttpServletRequest request, HttpServletResponse response) */
    @Override
    protected void service(final HttpServletRequest request,
    		               final HttpServletResponse response) throws ServletException, IOException {

		LOGGER.fine("Peticion recibida"); //$NON-NLS-1$

		if (!ConfigManager.isInitialized()) {
			try {
				ConfigManager.checkConfiguration();
			}
	    	catch (final ConfigFilesException e) {
	    		LOGGER.log(Level.SEVERE, "No se encontro el fichero de configuracion del componente central: " + e.getFileName(), e); //$NON-NLS-1$
	    		AlarmsManager.notify(Alarm.RESOURCE_NOT_FOUND, e.getMessage());
	    		response.sendError(ConfigFilesException.getHttpError(), e.getMessage());
	    		return;
	    	}
	    	catch (final InvalidConfigurationException e) {
	    		LOGGER.log(Level.SEVERE, "Error en la configuracion de la/s propiedad/es " + e.getProperty() + " (" + e.getFileName() + ")", e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	    		AlarmsManager.notify(Alarm.RESOURCE_CONFIG, e.getProperty(), e.getFileName());
	    		response.sendError(InvalidConfigurationException.getHttpError(), e.getMessage());
	    		return;
	    	}
		}

    	final RequestParameters params = RequestParameters.extractParameters(request);

    	final String appId          = params.getParameter(APPLICATION_ID_PARAM);
        final String configB64      = params.getParameter(PARAMETER_NAME_CONFIG);
        final String subjectId      = params.getParameter(PARAMETER_NAME_SUBJECT_ID);
        final String algorithm      = params.getParameter(PARAMETER_NAME_ALGORITHM);
        final String certB64        = params.getParameter(PARAMETER_NAME_CERT);
        final String extraParamsB64 = params.getParameter(PARAMETER_NAME_EXTRA_PARAM);
        final String subOperation   = params.getParameter(PARAMETER_NAME_OPERATION);
        final String format         = params.getParameter(PARAMETER_NAME_FORMAT);
        final String dataB64        = params.getParameter(PARAMETER_NAME_DATA);
        String providerName  	= params.getParameter(ServiceParams.HTTP_PARAM_CERT_ORIGIN);

        final LogTransactionFormatter logF = new LogTransactionFormatter(appId, null);

        if (ConfigManager.isCheckApplicationNeeded()) {
        	LOGGER.fine(logF.f("Se realizara la validacion del Id de aplicacion")); //$NON-NLS-1$
        	if (appId == null || appId.isEmpty()) {
        		LOGGER.warning(logF.f("No se ha proporcionado el identificador de la aplicacion")); //$NON-NLS-1$
                response.sendError(
            		HttpServletResponse.SC_BAD_REQUEST,
                    "No se ha proporcionado el identificador de la aplicacion" //$NON-NLS-1$
        		);
                return;
            }

	        try {
	        	final ApplicationChecking appCheck = AplicationsDAO.checkApplicationId(appId);
	        	if (!appCheck.isValid()) {
	        		LOGGER.warning(logF.f("Se proporciono un identificador de aplicacion no valido. Se rechaza la peticion")); //$NON-NLS-1$
	        		response.sendError(HttpServletResponse.SC_FORBIDDEN);
	        		return;
	        	}
	        }
	        catch (final Exception e) {
	        	LOGGER.log(Level.SEVERE, logF.f("Error grave al validar el identificador de la aplicacion: ") + e, e); //$NON-NLS-1$
	        	AlarmsManager.notify(Alarm.CONNECTION_DB);
	        	response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	        	return;
	        }
        }
        else{
        	LOGGER.fine("No se realiza la validacion de aplicacion en la base de datos"); //$NON-NLS-1$
        }

    	if (ConfigManager.isCheckCertificateNeeded()){
    		LOGGER.fine(logF.f("Se realizara la validacion del certificado")); //$NON-NLS-1$
    		final X509Certificate[] certificates = ServiceUtil.getCertificatesFromRequest(request);
	    	try {
				ServiceUtil.checkValidCertificate(appId, certificates);
			}
	    	catch (final DBConnectionException e) {
				LOGGER.log(Level.SEVERE, logF.f("No se pudo conectar con la base de datos"), e); //$NON-NLS-1$
				AlarmsManager.notify(Alarm.CONNECTION_DB);
	        	response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
				return;
			}
	    	catch (final CertificateValidationException e) {
				LOGGER.log(Level.SEVERE, logF.f("Error en la validacion del certificado: ") + e, e); //$NON-NLS-1$
				response.sendError(e.getHttpError(), e.getMessage());
				return;
			}
    	}
    	else {
    		LOGGER.fine(logF.f("No se validara el certificado"));//$NON-NLS-1$
    	}

        if (subjectId == null || subjectId.isEmpty()) {
            LOGGER.warning(logF.f("No se ha proporcionado el identificador del titular de la clave de firma")); //$NON-NLS-1$
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                "No se ha proporcionado el identificador del titular de la clave de firma"); //$NON-NLS-1$
            return;
        }

        if (algorithm == null || algorithm.isEmpty()) {
            LOGGER.warning(logF.f("No se ha proporcionado el algoritmo de firma")); //$NON-NLS-1$
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                "No se ha proporcionado el algoritmo de firma"); //$NON-NLS-1$
            return;
        }

        if (certB64 == null || certB64.isEmpty()) {
            LOGGER.warning(logF.f("No se ha proporcionado el certificado del firmante")); //$NON-NLS-1$
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                "No se ha proporcionado el certificado del firmante"); //$NON-NLS-1$
            return;
        }

        if (subOperation == null || subOperation.isEmpty()) {
            LOGGER.warning(logF.f("No se ha indicado la operacion de firma a realizar")); //$NON-NLS-1$
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                "No se ha indicado la operacion de firma a realizar"); //$NON-NLS-1$
            return;
        }

        if (format == null || format.isEmpty()) {
            LOGGER.warning(logF.f("No se ha indicado el formato de firma")); //$NON-NLS-1$
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                "No se ha indicado el formato de firma"); //$NON-NLS-1$
            return;
        }

        if (dataB64 == null || dataB64.isEmpty()) {
        	LOGGER.warning(logF.f("No se han proporcionado los datos a firmar")); //$NON-NLS-1$
            response.sendError(
        		HttpServletResponse.SC_BAD_REQUEST,
                "No se han proporcionado los datos a firmar"); //$NON-NLS-1$
            return;
        }

        final X509Certificate signerCert;
        try {
            signerCert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate( //$NON-NLS-1$
                new ByteArrayInputStream(Base64.decode(certB64, true))
            );
        }
        catch (final Exception e) {
        	LOGGER.log(Level.SEVERE, logF.f("No se ha podido decodificar el certificado del firmante: ") + e, e); //$NON-NLS-1$
        	response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        	return;
        }

        final TriphaseData td;
        try {
            td = FIReTriHelper.getPreSign(
                subOperation,
                format,
                algorithm,
                extraParamsB64 != null ? ServiceUtil.base642Properties(extraParamsB64) : null,
    			signerCert,
                Base64.decode(dataB64, true)
    		);
        }
        catch (final Exception e) {
            LOGGER.log(Level.SEVERE, logF.f("No se ha podido obtener la prefirma"), e); //$NON-NLS-1$
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "No se ha podido obtener la prefirma: " + e); //$NON-NLS-1$
            return;
        }

        // Obtenemos el conector con el backend ya configurado
        final FIReConnector connector;
        try {
        	Properties config = null;
        	if (configB64 != null && configB64.length() > 0) {
        		config = ServiceUtil.base642Properties(configB64);
        	}
        	if (providerName == null) {
        		providerName = ProviderLegacy.PROVIDER_NAME_CLAVEFIRMA;
        	}
            connector = ProviderManager.getProviderConnector(providerName, config);
        }
        catch (final FIReConnectorFactoryException e) {
        	LOGGER.log(Level.SEVERE, logF.f("No se ha podido cargar el conector del proveedor de firma: %1s", providerName), e); //$NON-NLS-1$
        	response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Error en la configuracion del conector con el servicio de custodia: " + e); //$NON-NLS-1$
            return;
        }

        final LoadResult lr;
        try {
            lr = connector.loadDataToSign(
                subjectId,
                algorithm,
                FIReTriHelper.fromTriPhaseDataAfirmaToFire(td),
                CertificateFactory.getInstance("X.509").generateCertificate( //$NON-NLS-1$
                    new ByteArrayInputStream(Base64.decode(ServiceUtil.undoUrlSafe(certB64)))
                )
            );
        }
        catch (final FIReConnectorUnknownUserException e) {
            LOGGER.severe(logF.f("El usuario " + subjectId + " no tiene certificados en el sistema: ") + e); //$NON-NLS-1$ //$NON-NLS-2$
            response.sendError(HttpCustomErrors.NO_USER.getErrorCode(),
                "El usuario " + subjectId + " no tiene certificados en el sistema: " + e); //$NON-NLS-1$//$NON-NLS-2$
            return;
        }
        catch (final FIReConnectorNetworkException e) {
        	LOGGER.log(Level.SEVERE, logF.f("No se ha podido conectar con el proveedor de firma en la nube: ") + e, e); //$NON-NLS-1$
        	AlarmsManager.notify(Alarm.CONNECTION_SIGNATURE_PROVIDER, providerName);
            response.sendError(HttpServletResponse.SC_REQUEST_TIMEOUT,
                    "No se ha podido conectar con el sistema: " + e); //$NON-NLS-1$
            return;
        }
        catch (final Exception e) {
            LOGGER.log(Level.SEVERE, logF.f("Error en la carga de datos: ") + e, e); //$NON-NLS-1$
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Error en la carga de datos: " + e); //$NON-NLS-1$
            return;
        }

        response.setContentType("application/json"); //$NON-NLS-1$
        final PrintWriter out = response.getWriter();
        out.print(lr.toString());
        out.flush();
        out.close();

    }

}
