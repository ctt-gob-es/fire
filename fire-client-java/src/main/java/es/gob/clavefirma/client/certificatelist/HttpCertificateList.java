/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.clavefirma.client.certificatelist;

import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;
import javax.json.JsonValue;

import es.gob.clavefirma.client.ClientConfigFilesNotFoundException;
import es.gob.clavefirma.client.ConnectionManager;
import es.gob.clavefirma.client.HttpCertificateBlockedException;
import es.gob.clavefirma.client.HttpForbiddenException;
import es.gob.clavefirma.client.HttpNetworkException;
import es.gob.clavefirma.client.HttpNoUserException;
import es.gob.clavefirma.client.HttpOperationException;
import es.gob.clavefirma.client.HttpWeakRegistryException;
import es.gob.fire.client.Base64;
import es.gob.fire.client.ConfigManager;
import es.gob.fire.client.HttpCustomErrors;
import es.gob.fire.client.HttpError;

/** Cliente del servicio de listado de certificados. */
public final class HttpCertificateList {

    private static final String X509 = "X.509"; //$NON-NLS-1$

    private static final String CERT_JSON_PARAM = "certificates"; //$NON-NLS-1$

    private static final String APP_ID_TAG = "$$APPID$$"; //$NON-NLS-1$

    private static final String SUBJECT_ID_TAG = "$$SUBJECTID$$"; //$NON-NLS-1$

    private static final String URL_SUFIX = "?appId=" + APP_ID_TAG + "&subjectId=" + SUBJECT_ID_TAG; //$NON-NLS-1$ //$NON-NLS-2$

    private static String URL;

    private static final Logger LOGGER = Logger.getLogger(HttpCertificateList.class.getName());

    private static boolean initialized = false;

    /** Constructor vac&iacute;o no instanciable. */
    private HttpCertificateList() {
        // No instanciable
    }

    /** Inicializa las propiedades de sistema a trav&eacute;s del fichero de propiedades.
     * @throws ClientConfigFilesNotFoundException Si no encuentra el fichero de configuraci&oacute;n. */
    public static void initialize() throws ClientConfigFilesNotFoundException{
    	if (!initialized) {
    		initializeProperties();
            initialized = true;
    	}
    }

    private static void initializeProperties() throws ClientConfigFilesNotFoundException {

    	final Properties p;
		try {
			p = ConfigManager.loadConfig();
		}
		catch (final es.gob.fire.client.ClientConfigFilesNotFoundException e) {
			throw new ClientConfigFilesNotFoundException(e.getMessage(), e.getCause());
		}

        URL = p.getProperty("certificateUrl"); //$NON-NLS-1$
        if (URL == null) {
            throw new IllegalStateException(
                "No esta declarada la URL en la configuracion (propiedad 'certificateUrl')" //$NON-NLS-1$
            );
        }

        try {
			ConnectionManager.configureConnection(p);
		}
        catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Error en la configuracion de la comunicacion con el componente centralizado: " + e, e); //$NON-NLS-1$
			throw new SecurityException("Error en la configuracion de la comunicacion con el componente centralizado", e); //$NON-NLS-1$
		}

        LOGGER.info(
    		"Se usara el siguiente servicio de listado de certificados: " + URL //$NON-NLS-1$
        );
    }

    /** Lista los certificados del sistema.
     * @param appId Identificador de la aplicaci&oacute;n.
     * @param subjectId Identificador del titular de los certificados.
     * @return Lista de certificados almacenados en el sistema.
     * @throws CertificateException Si ocurren errores en la extracci&oacute;n del certificado
     * 				                recibido o si no se puede leer el certificado.
     * @throws HttpNetworkException Si hay errores en la llamada al servicio o la obtenci&oacute;n del resultado.
     * @throws HttpForbiddenException Si la aplicaci&oacute;n no tiene permisos de acceso al servicio.
     * @throws HttpNoUserException Si el usuario no est&aacute; dado de alta en el sistema.
     * @throws HttpCertificateBlockedException Si el certificado de firma del usuario est&aacute; bloqueado.
     * @throws HttpOperationException Si hay cualquier otro error en el extremo servidor del servicio.
     * @throws ClientConfigFilesNotFoundException Si no se ha encontrado en el sistema el fichero de configuraci&oacute;n.
     * @throws HttpWeakRegistryException Si el usuario no puede tener certificados de firma por haber hecho un
     * 				                     registro no fehaciente. */
    public static List<X509Certificate> getList(final String appId, final String subjectId) throws CertificateException,
    	                                                                                           HttpNetworkException,
    	                                                                                           HttpForbiddenException,
    	                                                                                           HttpNoUserException,
    	                                                                                           HttpCertificateBlockedException,
    	                                                                                           HttpOperationException,
    	                                                                                           ClientConfigFilesNotFoundException,
    	                                                                                           HttpWeakRegistryException {
    	initialize();

        final List<X509Certificate> certificates = new ArrayList<>();

        final byte[] responseJSON;
        try {
        	responseJSON = ConnectionManager.readUrlByGet(
    			URL + URL_SUFIX
        			.replace(APP_ID_TAG, appId)
        			.replace(SUBJECT_ID_TAG, subjectId));
        }
        catch (final HttpError e) {
        	LOGGER.log(Level.SEVERE,
        		"Error en la llamada al servicio de listado de certificados: " + e.getMessage(), //$NON-NLS-1$
        		e
        	);

        	// Aplicacion no permitida
        	if (e.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN) {
			    throw new HttpForbiddenException("Aplicacion no permitida", e); //$NON-NLS-1$
        	}
			// Usuario no valido
        	else if (e.getResponseCode() == HttpCustomErrors.NO_USER.getErrorCode()) {
			    throw new HttpNoUserException("Usuario no valido", e); //$NON-NLS-1$
        	}
			// El certificado de firma esta bloqueado
        	else if (e.getResponseCode() == HttpCustomErrors.CERTIFICATE_BLOCKED.getErrorCode()) {
			    throw new HttpCertificateBlockedException("Usuario con certificados bloqueados", e); //$NON-NLS-1$
        	}
			// El usuario realizo un registro debil y no tiene permisos para generar certificados
        	else if (e.getResponseCode() == HttpCustomErrors.WEAK_REGISTRY.getErrorCode()) {
			    throw new HttpWeakRegistryException("El usuario realizo un registro debil y no puede tener certificados de firma", e); //$NON-NLS-1$
        	}
			// El usuario no tiene certificados
        	else if (e.getResponseCode() == HttpCustomErrors.NO_CERTS.getErrorCode()) {
			    return certificates;
        	}
			// Problema de red
        	else if (e.getResponseCode() == HttpURLConnection.HTTP_CLIENT_TIMEOUT) {
			    throw new HttpNetworkException("Error en la conexion", e); //$NON-NLS-1$
        	}
			// Cualquier otro error
        	else {
			    throw new HttpOperationException(e.getResponseDescription(), e);
			}
        }
        catch (final Exception e) {
            LOGGER.log(
        		Level.SEVERE,
        		"Error en la llamada al servicio de obtencion de certificados", e //$NON-NLS-1$
            );
            throw new HttpNetworkException("Error en la llamada al servicio de obtencion de certificados", e); //$NON-NLS-1$
		}

        try (
    		final JsonReader jsonReader = Json.createReader(new ByteArrayInputStream(responseJSON));
		) {

        	final JsonArray certList = jsonReader.readObject().getJsonArray(CERT_JSON_PARAM);
            for (final JsonValue cert : certList) {
                certificates.add(
            		(X509Certificate) CertificateFactory.getInstance(X509).generateCertificate(
        				new ByteArrayInputStream(
    						Base64.decode(cert.toString())
						)
            		)
        		);
            }
            jsonReader.close();
        }
        catch (final CertificateException e) {
        	LOGGER.severe("Error en la composicion de uno de los certificados del usuario: " + e); //$NON-NLS-1$
        	throw e;
        }
        catch (final Exception e) {
            LOGGER.severe("Error en la lectura del JSON de certificados: " + e); //$NON-NLS-1$
            throw new HttpOperationException("Error en la lectura del JSON de certificados", e); //$NON-NLS-1$
        }

        return certificates;
    }
}
