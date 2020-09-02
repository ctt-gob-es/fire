/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.clavefirma.client.generatecert;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import es.gob.clavefirma.client.ClientConfigFilesNotFoundException;
import es.gob.clavefirma.client.ConnectionManager;
import es.gob.clavefirma.client.ConnectionManager.Method;
import es.gob.clavefirma.client.HttpForbiddenException;
import es.gob.clavefirma.client.HttpNetworkException;
import es.gob.clavefirma.client.HttpNoUserException;
import es.gob.clavefirma.client.HttpOperationException;
import es.gob.clavefirma.client.HttpWeakRegistryException;
import es.gob.clavefirma.client.InvalidTransactionException;
import es.gob.fire.client.ConfigManager;
import es.gob.fire.client.HttpCustomErrors;
import es.gob.fire.client.HttpError;
import es.gob.fire.client.Utils;

/**
 * Clase para solicitar la generaci&oacute;n de un nuevo certificado.
 */
public class HttpGenerateCertificate {

    private static final Logger LOGGER = Logger.getLogger(HttpGenerateCertificate.class.getName());

    private static boolean initialized = false;

    private static final String PARAMETER_NAME_APPID = "appId"; //$NON-NLS-1$
	private static final String PARAMETER_NAME_CONFIG = "config"; //$NON-NLS-1$
    private static final String PARAMETER_NAME_SUBJECT_ID = "subjectid"; //$NON-NLS-1$
    private static final String PARAMETER_NAME_TRANSACTION_ID = "transactionid"; //$NON-NLS-1$
    private static final String PARAMETER_NAME_CERT_ORIGIN = "certorigin"; //$NON-NLS-1$

    private static final String TAG_NAME_CONFIG = "$$CONFIG$$"; //$NON-NLS-1$
    private static final String TAG_NAME_APP_ID = "$$APPID$$"; //$NON-NLS-1$
    private static final String TAG_NAME_SUBJECT_ID = "$$SUBJECTID$$"; //$NON-NLS-1$
    private static final String TAG_NAME_TRANSACTION_ID = "$$TRANSACTIONID$$"; //$NON-NLS-1$

    private static final String EQ = "="; //$NON-NLS-1$
    private static final String AM = "&"; //$NON-NLS-1$

    private static final String URL_REQUEST_SUFIX =
            PARAMETER_NAME_CONFIG + EQ + TAG_NAME_CONFIG + AM
            + PARAMETER_NAME_APPID + EQ + TAG_NAME_APP_ID + AM
            + PARAMETER_NAME_SUBJECT_ID + EQ + TAG_NAME_SUBJECT_ID;

    private static String URL_REQUEST_SERVICE;

    private static final String URL_RECOVER_SUFIX = "?" + //$NON-NLS-1$
            PARAMETER_NAME_APPID + EQ + TAG_NAME_APP_ID + AM
            + PARAMETER_NAME_TRANSACTION_ID + EQ + TAG_NAME_TRANSACTION_ID;

    private static String URL_RECOVER_SERVICE;

    /**
     * Constructor vac&iacute;o no instanciable
     */
    private HttpGenerateCertificate() {
		// Impedimos la instanciacion de la clase
	}

    /**
     * Inicializa las propiedades de sistema a trav&eacute;s del fichero de propiedades.
     * @throws ClientConfigFilesNotFoundException Si no encuentra el fichero de configuraci&oacute;n.
     */
    public static void initialize() throws ClientConfigFilesNotFoundException{
    	if (!initialized) {
    		initializeProperties();
            initialized = true;
    	}
    }

    private static void initializeProperties() throws ClientConfigFilesNotFoundException {

    	Properties p;
		try {
			p = ConfigManager.loadConfig();
		} catch (final es.gob.fire.client.ClientConfigFilesNotFoundException e) {
			throw new ClientConfigFilesNotFoundException(e.getMessage(), e.getCause());
		}

        URL_REQUEST_SERVICE = p.getProperty("newCertUrl"); //$NON-NLS-1$
        if (URL_REQUEST_SERVICE == null) {
            throw new IllegalStateException(
                    "No esta declarada la configuracion de URL de generacion de certificado en la configuracion" //$NON-NLS-1$
            );
        }

        URL_RECOVER_SERVICE = p.getProperty("recoverNewCertUrl"); //$NON-NLS-1$
        if (URL_RECOVER_SERVICE == null) {
            throw new IllegalStateException(
                    "No esta declarada la configuracion de URL de recuperacion de certifciado en la configuracion" //$NON-NLS-1$
            );
        }

        try {
			ConnectionManager.configureConnection(p);
		} catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Error en la configuracion de la comunicacion con el componente centralizado: " + e, e); //$NON-NLS-1$
			throw new SecurityException("Error en la configuracion de la comunicacion con el componente centralizado", e); //$NON-NLS-1$
		}

        LOGGER.info(
        		"Se usara el siguiente servicio de listado de certificados: " + URL_REQUEST_SERVICE //$NON-NLS-1$
        );
    }

    /**
     * Realiza una solicitud para la generaci&oacute;n de un nuevo certificado de firma para el usuario.
     * @param appId
     *            Identificador de la aplicaci&oacute;n que realiza la
     *            petici&oacute;n.
     * @param subjectId
     *            Identificador del usuario que solicita el certificado.
     * @param configB64
     * 			  Configuraci&oacute;n a indicar al servicio remoto para ejecutar la operaci&oacute;n.
     * 			  Este ser&aacute; el resultado de codificar en base 64 una cadena compuesta por tuplas
     * 			  "clave=valor" separadas por "\n".
     * @return Informaci&oacute;n resultante de la operaci&oacute;n de generaci&oacute;n del certificado.
     * @throws HttpForbiddenException Cuando no se tiene acceso al servicio remoto.
     * @throws HttpNetworkException Cuando ocurre un error de red.
     * @throws HttpOperationException Cuando ocurre un error durante la ejecuci&oacute;n de la operaci&oacute;n.
     * @throws ClientConfigFilesNotFoundException Cuando no se encuentra el fichero de configuraci&oacute;n.
     * @throws HttpCertificateAvailableException Cuando se solicita crear una certificado para un usuario que ya tiene.
     * @throws HttpNoUserException Cuando el usuario indicado no existe.
     * @throws HttpWeakRegistryException Cuando el usuario realiz&oacute; un registro d&eacute;bil y no puede tener certificados de firma.
     */
    public static GenerateCertificateResult generateCertificate(
    		final String appId,
    		final String subjectId,
            final String configB64)
            		throws HttpForbiddenException, HttpNetworkException, HttpOperationException,
            		ClientConfigFilesNotFoundException, HttpCertificateAvailableException, HttpNoUserException,
            		HttpWeakRegistryException {
    	return generateCertificate(appId, subjectId, null, configB64);
    }

    /**
     * Realiza una solicitud para la generaci&oacute;n de un nuevo certificado de firma para el usuario.
     * @param appId
     *            Identificador de la aplicaci&oacute;n que realiza la
     *            petici&oacute;n.
     * @param subjectId
     *            Identificador del usuario que solicita el certificado.
     * @param providerName Nombre del proveedor de firma en la nube que se desee utilizar.
     * @param configB64
     * 			  Configuraci&oacute;n a indicar al servicio remoto para ejecutar la operaci&oacute;n.
     * 			  Este ser&aacute; el resultado de codificar en base 64 una cadena compuesta por tuplas
     * 			  "clave=valor" separadas por "\n".
     * @return Informaci&oacute;n resultante de la operaci&oacute;n de generaci&oacute;n del certificado.
     * @throws HttpForbiddenException Cuando no se tiene acceso al servicio remoto.
     * @throws HttpNetworkException Cuando ocurre un error de red.
     * @throws HttpOperationException Cuando ocurre un error durante la ejecuci&oacute;n de la operaci&oacute;n.
     * @throws ClientConfigFilesNotFoundException Cuando no se encuentra el fichero de configuraci&oacute;n.
     * @throws HttpCertificateAvailableException Cuando se solicita crear una certificado para un usuario que ya tiene.
     * @throws HttpNoUserException Cuando el usuario indicado no existe.
     * @throws HttpWeakRegistryException Cuando el usuario realiz&oacute; un registro d&eacute;bil y no puede tener certificados de firma.
     */
    public static GenerateCertificateResult generateCertificate(
    		final String appId,
    		final String subjectId,
    		final String providerName,
            final String configB64)
            		throws HttpForbiddenException, HttpNetworkException, HttpOperationException,
            		ClientConfigFilesNotFoundException, HttpCertificateAvailableException, HttpNoUserException,
            		HttpWeakRegistryException {

    	initialize();

    	if (appId == null) {
    		throw new IllegalArgumentException(
    				"El identificador de aplicacion no puede ser nulo" //$NON-NLS-1$
    				);
    	}

    	if (subjectId == null) {
    		throw new IllegalArgumentException(
    				"El identificador de usuario no puede ser nulo" //$NON-NLS-1$
    				);
    	}

    	// Realizamos la peticion y cargamos el JSON de respuesta
   		String urlParameters = URL_REQUEST_SUFIX
				.replace(TAG_NAME_APP_ID, appId)
				.replace(TAG_NAME_SUBJECT_ID, subjectId)
				.replace(TAG_NAME_CONFIG, configB64 != null ? Utils.doBase64UrlSafe(configB64) : ""); //$NON-NLS-1$

   		if (providerName != null) {
   			urlParameters += AM + PARAMETER_NAME_CERT_ORIGIN + EQ + providerName;
   		}

    	final byte[] responseJSON;
        try {
        	responseJSON = ConnectionManager.readUrl(URL_REQUEST_SERVICE, urlParameters, Method.POST);
        } catch (final IOException e) {
        	if (e instanceof HttpError) {
        		final HttpError he = (HttpError) e;
        		LOGGER.severe(
        				"Error en la llamada al servicio de generacion de nuevo certificado: " + he.getResponseDescription() //$NON-NLS-1$
        				);
        		if (HttpURLConnection.HTTP_FORBIDDEN == he.getResponseCode()) {
        			throw new HttpForbiddenException(he.getResponseDescription(), e);
        		}
        		else if (HttpURLConnection.HTTP_CLIENT_TIMEOUT == he.getResponseCode()) {
        			throw new HttpNetworkException(he.getResponseDescription(), e);
        		}
        		else if (HttpCustomErrors.NO_USER.getErrorCode()  == he.getResponseCode()) {
        			throw new HttpNoUserException(he.getResponseDescription(), e);
        		}
        		else if (HttpCustomErrors.CERTIFICATE_AVAILABLE.getErrorCode()  == he.getResponseCode()) {
        			throw new HttpCertificateAvailableException(he.getResponseDescription(), e);
        		}
        		else if (HttpCustomErrors.WEAK_REGISTRY.getErrorCode()  == he.getResponseCode()) {
        			throw new HttpWeakRegistryException(he.getResponseDescription(), e);
        		}
        		else {
        			throw new HttpOperationException(he.getResponseDescription(), e);
        		}
        	}
        	LOGGER.severe("Error en la llamada al servicio remoto: " + e); //$NON-NLS-1$
        	throw new HttpNetworkException("Error en la llamada al servicio remoto", e); //$NON-NLS-1$
        }

        try {
			return new GenerateCertificateResult(new String(responseJSON));
		} catch (final IOException e) {
			LOGGER.severe("El formato de la respuesta es incorrecto:\n" + responseJSON); //$NON-NLS-1$
			throw new HttpOperationException("El resultado obtenido no tiene el formato JSON esperado", e); //$NON-NLS-1$
		}
    }

    /**
     * Realiza una solicitud para la generaci&oacute;n de un nuevo certificado para el usuario.
     * @param appId
     *            Identificador de la aplicaci&oacute;n que realiza la
     *            petici&oacute;n.
     * @param transactionId
     *            Identificador del usuario que solicita el certificado.
     * @return Certificado reci&eacute;n generado.
     * @throws HttpForbiddenException Cuando no se tiene acceso al servicio remoto.
     * @throws HttpNetworkException Cuando ocurre un error de red.
     * @throws HttpOperationException Cuando ocurre un error durante la ejecuci&oacute;n de la operaci&oacute;n.
     * @throws ClientConfigFilesNotFoundException Cuando no se encuentra el fichero de configuraci&oacute;n.
     * @throws InvalidTransactionException Cuando la transacci&oacute;n no existe o
     * est&aacute; caducada.
     */
    public static X509Certificate recoverCertificate(
    		final String appId,
    		final String transactionId) throws HttpForbiddenException, HttpNetworkException, HttpOperationException, ClientConfigFilesNotFoundException, InvalidTransactionException {
    	return recoverCertificate(appId, transactionId, null);
    }

    /**
     * Realiza una solicitud para la generaci&oacute;n de un nuevo certificado para el usuario.
     * @param appId Identificador de la aplicaci&oacute;n que realiza la petici&oacute;n.
     * @param transactionId Identificador del usuario que solicita el certificado.
     * @param providerName Nombre del proveedor de firma en la nube deseado. Si se indica
     * {@code null}, se usar&aacute; el por defecto.
     * @return Certificado reci&eacute;n generado.
     * @throws HttpForbiddenException Cuando no se tiene acceso al servicio remoto.
     * @throws HttpNetworkException Cuando ocurre un error de red.
     * @throws HttpOperationException Cuando ocurre un error durante la ejecuci&oacute;n de la operaci&oacute;n.
     * @throws ClientConfigFilesNotFoundException Cuando no se encuentra el fichero de configuraci&oacute;n.
     * @throws InvalidTransactionException Cuando la transacci&oacute;n no existe o
     * est&aacute; caducada.
     */
    public static X509Certificate recoverCertificate(
    		final String appId,
    		final String transactionId,
    		final String providerName) throws HttpForbiddenException, HttpNetworkException, HttpOperationException, ClientConfigFilesNotFoundException, InvalidTransactionException {

    	initialize();

    	 if (appId == null) {
             throw new IllegalArgumentException(
                     "El identificador de aplicacion no puede ser nulo" //$NON-NLS-1$
             );
         }

    	 if (transactionId == null) {
             throw new InvalidTransactionException(
                     "El identificador de transaccion no puede ser nulo" //$NON-NLS-1$
             );
         }

    	// Realizamos la peticion y cargamos el JSON de respuesta
    	String url = URL_RECOVER_SERVICE + URL_RECOVER_SUFIX
				.replace(TAG_NAME_APP_ID, appId)
				.replace(TAG_NAME_TRANSACTION_ID, transactionId);

    	if (providerName != null) {
    		url += AM + PARAMETER_NAME_CERT_ORIGIN + EQ + providerName;
    	}

    	final byte[] certEncoded;
        try {
        	certEncoded = ConnectionManager.readUrlByGet(url);
        } catch (final IOException e) {
        	if (e instanceof HttpError) {
        		final HttpError he = (HttpError) e;
        		LOGGER.severe(
        				"Error en la llamada al servicio de recuperacion del nuevo certificado: " + he.getResponseDescription() //$NON-NLS-1$
        				);
        		if (he.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN) {
            		throw new HttpForbiddenException(e);
            	} else if (he.getResponseCode() == HttpURLConnection.HTTP_CLIENT_TIMEOUT) {
            		throw new HttpNetworkException(he);
            	} else if (he.getResponseCode() == HttpCustomErrors.INVALID_TRANSACTION.getErrorCode()) {
            		throw new InvalidTransactionException(HttpCustomErrors.INVALID_TRANSACTION.getErrorDescription(), e);
            	} else {
            		throw new HttpOperationException(he.getResponseDescription(), e);
            	}
        	}
        	LOGGER.severe("Error en la llamada al servicio remoto de recuperacion de certificado: " + e); //$NON-NLS-1$
        	throw new HttpNetworkException("Error en la llamada al servicio remoto de recuperacion de certificado", e); //$NON-NLS-1$
        }

        try {
        	return (X509Certificate) CertificateFactory.getInstance("X.509") //$NON-NLS-1$
        			.generateCertificate(new ByteArrayInputStream(certEncoded));
        }
        catch (final Exception e) {
        	LOGGER.severe("El servicio remoto no ha devuelto un certificado valido: " + e); //$NON-NLS-1$
        	throw new HttpOperationException("El servicio remoto no ha devuelto un certificado valido", e); //$NON-NLS-1$
        }
    }
}
