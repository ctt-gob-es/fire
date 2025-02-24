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
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import es.gob.afirma.core.misc.Base64;
import es.gob.fire.alarms.Alarm;
import es.gob.fire.server.connector.CertificateBlockedException;
import es.gob.fire.server.connector.FIReCertificateException;
import es.gob.fire.server.connector.FIReConnector;
import es.gob.fire.server.connector.FIReConnectorFactoryException;
import es.gob.fire.server.connector.FIReConnectorNetworkException;
import es.gob.fire.server.connector.FIReConnectorUnknownUserException;
import es.gob.fire.server.connector.WeakRegistryException;
import es.gob.fire.server.services.internal.AlarmsManager;
import es.gob.fire.server.services.internal.LogTransactionFormatter;
import es.gob.fire.server.services.internal.ProviderManager;
import es.gob.fire.server.services.internal.ServiceParams;
import es.gob.fire.server.services.internal.TransactionAuxParams;
import es.gob.fire.signature.ConfigFilesException;
import es.gob.fire.signature.ConfigManager;
import es.gob.fire.signature.InvalidConfigurationException;

/** Servlet para la obtenci&oacute;n de certificados de un usuario. */
public final class CertificateService extends HttpServlet {

    private static final long serialVersionUID = 9165731108863824136L;

	private static final String PARAM_APPLICATION_ID = "appId"; //$NON-NLS-1$
    private static final String PARAM_SUBJECT_ID = "subjectId"; //$NON-NLS-1$

    private static final Logger LOGGER = Logger.getLogger(CertificateService.class.getName());

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

	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) {
    	LOGGER.info("Peticion de tipo GET_CERTIFICATES"); //$NON-NLS-1$

		if (!ConfigManager.isInitialized()) {
			try {
				ConfigManager.checkConfiguration();
			}
	    	catch (final ConfigFilesException e) {
	    		LOGGER.log(Level.SEVERE, "No se encontro el fichero de configuracion del componente central: " + e.getFileName(), e); //$NON-NLS-1$
	    		AlarmsManager.notify(Alarm.RESOURCE_NOT_FOUND, e.getMessage());
	    		Responser.sendError(response, ConfigFilesException.getHttpError());
	    		return;
	    	}
	    	catch (final InvalidConfigurationException e) {
	    		LOGGER.log(Level.SEVERE, "Error en la configuracion de la propiedad " + e.getProperty() + " del fichero " + e.getFileName(), e); //$NON-NLS-1$ //$NON-NLS-2$
	    		AlarmsManager.notify(Alarm.RESOURCE_CONFIG, e.getProperty(), e.getFileName());
	    		Responser.sendError(response, InvalidConfigurationException.getHttpError());
	    		return;
	    	}
		}
		
		// Verificar si los servicios antiguos están habilitados
	    if (!ConfigManager.isLegacyServicesEnabled()) {
	        LOGGER.log(Level.WARNING, "Acceso denegado: las peticiones a los servicios antiguos estan deshabilitadas"); //$NON-NLS-1$
	        Responser.sendError(response, HttpServletResponse.SC_FORBIDDEN, "Acceso denegado: los servicios antiguos están deshabilitados"); //$NON-NLS-1$
	        return;
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

    	final String appId = params.getParameter(PARAM_APPLICATION_ID);

    	final TransactionAuxParams trAux = new TransactionAuxParams(appId);
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

        final String subjectId = params.getParameter(PARAM_SUBJECT_ID);
        if (subjectId == null || subjectId.isEmpty()) {
        	LOGGER.warning(logF.f("No se ha proporcionado el identificador del titular"));//$NON-NLS-1$
        	Responser.sendError(response,
        		HttpServletResponse.SC_BAD_REQUEST,
                "No se ha proporcionado el identificador del titular" //$NON-NLS-1$
    		);
            return;
        }

        // Obtenemos el conector con el backend ya configurado
        String providerName = null;
        final FIReConnector connector;
        try {
        	providerName = params.getParameter(ServiceParams.HTTP_PARAM_CERT_ORIGIN);
        	if (providerName == null) {
        		providerName = ProviderLegacy.PROVIDER_NAME_CLAVEFIRMA;
        	}
            connector = ProviderManager.getProviderConnector(providerName, null, logF);
        }
        catch (final FIReConnectorFactoryException e) {
        	LOGGER.log(Level.SEVERE, logF.f("No se ha podido cargar el conector del proveedor de firma: %1s", LogUtils.cleanText(providerName)), e); //$NON-NLS-1$
        	Responser.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
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
            LOGGER.severe(logF.f("El usuario " + subjectId + " no tiene certificados en el sistema: ") + e); //$NON-NLS-1$ //$NON-NLS-2$
            Responser.sendError(response,
                HttpCustomErrors.NO_USER.getErrorCode(),
                "El usuario " + subjectId + " no tiene certificados en el sistema: " + e //$NON-NLS-1$//$NON-NLS-2$
    		);
            return;
        }
        catch (final CertificateBlockedException e) {
            LOGGER.severe(logF.f("El certificado de firma del usuario " + subjectId + " esta bloqueado: ") + e); //$NON-NLS-1$ //$NON-NLS-2$
            Responser.sendError(response,
                HttpCustomErrors.CERTIFICATE_BLOCKED.getErrorCode(),
                "El certificado de firma del usuario " + subjectId + " esta bloqueado: " + e //$NON-NLS-1$//$NON-NLS-2$
    		);
            return;
        }
        catch (final WeakRegistryException e) {
            LOGGER.severe(logF.f("El usuario " + subjectId + " realizo un registro debil y no puede tener certificados de firma: ") + e); //$NON-NLS-1$ //$NON-NLS-2$
            Responser.sendError(response,
                HttpCustomErrors.WEAK_REGISTRY.getErrorCode(),
                "El usuario " + subjectId + " realizo un registro debil y no puede tener certificados de firma: " + e //$NON-NLS-1$//$NON-NLS-2$
    		);
            return;
        }
        catch (final FIReConnectorNetworkException e) {
        	LOGGER.log(Level.SEVERE, logF.f("No se ha podido conectar con el proveedor de firma en la nube: ") + e, e); //$NON-NLS-1$
        	AlarmsManager.notify(Alarm.CONNECTION_SIGNATURE_PROVIDER, providerName);
        	Responser.sendError(response,
        		HttpServletResponse.SC_REQUEST_TIMEOUT,
                "No se ha podido conectar con el sistema: " + e //$NON-NLS-1$
    		);
            return;
        }
        catch (final FIReCertificateException e) {
            LOGGER.log(Level.WARNING, logF.f("El usuario no tiene certificados de firma: ") + e, e); //$NON-NLS-1$
            certs = new X509Certificate[0];
        }
        catch (final Exception e) {
            LOGGER.log(Level.SEVERE, logF.f("Error obteniendo los certificados del usuario: ") + e, e); //$NON-NLS-1$
            Responser.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
            return;
        }

        // JSON de los certificados
        final String certJSON;
        try {
            certJSON = getCertJSON(certs);
        }
        catch (final CertificateEncodingException e) {
            LOGGER.warning(logF.f("Error en la codificacion de los certificados: ") + e); //$NON-NLS-1$
            Responser.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        response.setContentType("application/json"); //$NON-NLS-1$

        // El servicio devuelve el JSON con la lista de certificados
        Responser.sendResult(response, certJSON.getBytes(StandardCharsets.UTF_8));
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
