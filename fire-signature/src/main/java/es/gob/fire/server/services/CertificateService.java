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
import java.io.PrintWriter;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import es.gob.afirma.core.misc.Base64;
import es.gob.fire.server.connector.CertificateBlockedException;
import es.gob.fire.server.connector.FIReCertificateException;
import es.gob.fire.server.connector.FIReConnector;
import es.gob.fire.server.connector.FIReConnectorFactoryException;
import es.gob.fire.server.connector.FIReConnectorNetworkException;
import es.gob.fire.server.connector.FIReConnectorUnknownUserException;
import es.gob.fire.server.connector.WeakRegistryException;
import es.gob.fire.server.services.internal.ProviderManager;
import es.gob.fire.signature.AplicationsDAO;
import es.gob.fire.signature.ConfigFilesException;
import es.gob.fire.signature.ConfigManager;
import es.gob.fire.signature.GoogleAnalitycs;

/** Servlet para la obtenci&oacute;n de certificados de un usuario. */
public final class CertificateService extends HttpServlet {

	private static final String PARAM_APPLICATION_ID = "appId"; //$NON-NLS-1$
    private static final String PARAM_SUBJECT_ID = "subjectId"; //$NON-NLS-1$
    private static final String PARAM_CONFIG = "config"; //$NON-NLS-1$

    private static final long serialVersionUID = 9165731108863824136L;
    private static final Logger LOGGER = Logger.getLogger(CertificateService.class.getName());

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
	    			CertificateService.class.getSimpleName()
				);
    		}
    		catch(final Throwable e) {
    			LOGGER.warning(
					"No ha podido inicializarse Google Analytics: " + e //$NON-NLS-1$
				);
    		}
    	}
    }

    @Override
    protected void service(final HttpServletRequest request,
    		               final HttpServletResponse response) throws IOException {

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

    	final String appId = params.getParameter(PARAM_APPLICATION_ID);
        if (appId == null || "".equals(appId)) { //$NON-NLS-1$
        	LOGGER.severe("No se ha proporcionado el identificador de la aplicacion"); //$NON-NLS-1$
    		response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "No se ha proporcionado el identificador de la aplicacion"); //$NON-NLS-1$
            return;
        }

        if (ConfigManager.isCheckApplicationNeeded()){
        	LOGGER.info("Se realizara la validacion de aplicacion en la base de datos"); //$NON-NLS-1$
        	try {
	        	if (!AplicationsDAO.checkApplicationId(appId)) {
	        		LOGGER.warning(
	    				"Se proporciono un identificador de aplicacion no valido. Se rechaza la peticion" //$NON-NLS-1$
					);
	        		response.sendError(HttpServletResponse.SC_FORBIDDEN);
	        		return;
	        	}
	        }
	        catch (final Exception e) {
	        	LOGGER.log(
	    			Level.SEVERE,
	    			"Ocurrio un error grave al validar el identificador de la aplicacion", e //$NON-NLS-1$
				);
	        	response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	        	return;
	        }
        }
        else{
        	LOGGER.warning("No se realiza la validacion de aplicacion en la base de datos"); //$NON-NLS-1$
        }

    	if (ConfigManager.isCheckCertificateNeeded()){
    		LOGGER.info("Se realizara la validacion del certificado"); //$NON-NLS-1$
    		final X509Certificate[] certificates = ServiceUtil.getCertificatesFromRequest(request);
	    	try {
	    		ServiceUtil.checkValidCertificate(appId, certificates);
			}
			catch (final CertificateValidationException e) {
				LOGGER.log(Level.SEVERE, "Error en la validacion del certificado", e); //$NON-NLS-1$
				response.sendError(e.getHttpError(), e.getMessage());
				return;
			}
    	}
    	else {
    		LOGGER.warning("No se validara el certificado cliente");//$NON-NLS-1$
    	}

    	if (analytics != null) {
    		analytics.trackRequest(request.getRemoteHost());
    	}

        final String subjectId = params.getParameter(PARAM_SUBJECT_ID);
        if (subjectId == null || "".equals(subjectId)) { //$NON-NLS-1$
        	LOGGER.warning("No se ha proporcionado el identificador del titular");//$NON-NLS-1$
            response.sendError(
        		HttpServletResponse.SC_BAD_REQUEST,
                "No se ha proporcionado el identificador del titular" //$NON-NLS-1$
    		);
            return;
        }

        // Obtenemos el conector con el backend ya configurado
        final FIReConnector connector;
        try {
        	Properties config = null;
        	final String configB64 = params.getParameter(PARAM_CONFIG);
        	if (configB64 != null && configB64.length() > 0) {
        		config = ServiceUtil.base642Properties(configB64);
        	}
            connector = ProviderManager.initTransacction(ProviderLegacy.PROVIDER_NAME_CLAVEFIRMA, config);
        }
        catch (final FIReConnectorFactoryException e) {
        	LOGGER.log(Level.SEVERE, "Error en la configuracion del conector con el servicio de custodia", e); //$NON-NLS-1$
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Error en la configuracion del conector con el servicio de custodia: " + e //$NON-NLS-1$
            );
            return;
        }

        // Listamos los certificados de firma
        X509Certificate[] certs;
        try {
            certs = connector.getCertificates(subjectId);
        }
        catch (final FIReConnectorUnknownUserException e) {
            LOGGER.severe("El usuario " + subjectId + " no tiene certificados en el sistema: " + e); //$NON-NLS-1$ //$NON-NLS-2$
            response.sendError(
                HttpCustomErrors.NO_USER.getErrorCode(),
                "El usuario " + subjectId + " no tiene certificados en el sistema: " + e //$NON-NLS-1$//$NON-NLS-2$
    		);
            return;
        }
        catch (final CertificateBlockedException e) {
            LOGGER.severe("El certificado de firma del usuario " + subjectId + " esta bloqueado: " + e); //$NON-NLS-1$ //$NON-NLS-2$
            response.sendError(
                HttpCustomErrors.CERTIFICATE_BLOCKED.getErrorCode(),
                "El certificado de firma del usuario " + subjectId + " esta bloqueado: " + e //$NON-NLS-1$//$NON-NLS-2$
    		);
            return;
        }
        catch (final WeakRegistryException e) {
            LOGGER.severe("El usuario " + subjectId + " realizo un registro debil y no puede tener certificados de firma: " + e); //$NON-NLS-1$ //$NON-NLS-2$
            response.sendError(
                HttpCustomErrors.WEAK_REGISTRY.getErrorCode(),
                "El usuario " + subjectId + " realizo un registro debil y no puede tener certificados de firma: " + e //$NON-NLS-1$//$NON-NLS-2$
    		);
            return;
        }
        catch (final FIReConnectorNetworkException e) {
        	LOGGER.log(Level.SEVERE, "No se ha podido conectar con el sistema: " + e, e); //$NON-NLS-1$
            response.sendError(
        		HttpServletResponse.SC_REQUEST_TIMEOUT,
                "No se ha podido conectar con el sistema: " + e //$NON-NLS-1$
    		);
            return;
        }
        catch (final FIReCertificateException e) {
            LOGGER.log(Level.WARNING, "El usuario no tiene certificados de firma: " + e, e); //$NON-NLS-1$
            certs = new X509Certificate[0];
        }
        catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "Error obteniendo los certificados del usuario: " + e, e); //$NON-NLS-1$
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
            return;
        }

        // JSON de los certificados
        final String certJSON;
        try {
            certJSON = getCertJSON(certs);
        }
        catch (final CertificateEncodingException e) {
            LOGGER.warning("Error en la codificacion de los certificados: " + e); //$NON-NLS-1$
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        response.setContentType("application/json"); //$NON-NLS-1$

        // El servicio devuelve el JSON con la lista de certificados
        final PrintWriter out = response.getWriter();
        out.print(certJSON);
        out.flush();
        out.close();
    }

    /** Crea un JSON para el conjunto de certificados.
     * @param certificates Lista de certificados.
     * @throws CertificateEncodingException Excepci&oacute;n en el codficicaci&oacute;n de un certificado.
     * @return JSON conteniendo los certificados obtenidos. */
    private static String getCertJSON(final X509Certificate[] certificates) throws CertificateEncodingException {
        String certJSON = "{\"certificates\":["; //$NON-NLS-1$
        for (int i = 0; i < certificates.length; i++) {
        	final X509Certificate cert = certificates[i];
            certJSON += "\"" + Base64.encode(cert.getEncoded()) + "\""; //$NON-NLS-1$ //$NON-NLS-2$
            if (i < certificates.length - 1) {
            	certJSON += ","; //$NON-NLS-1$
            }
        }
        return certJSON + "]}"; //$NON-NLS-1$
    }
}
