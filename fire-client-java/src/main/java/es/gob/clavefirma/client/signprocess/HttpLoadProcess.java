/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.clavefirma.client.signprocess;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import es.gob.clavefirma.client.ClientConfigFilesNotFoundException;
import es.gob.clavefirma.client.HttpForbiddenException;
import es.gob.clavefirma.client.HttpNetworkException;
import es.gob.clavefirma.client.HttpNoUserException;
import es.gob.clavefirma.client.HttpOperationException;
import es.gob.fire.client.Base64;
import es.gob.fire.client.ConfigManager;
import es.gob.fire.client.ConnectionManager;
import es.gob.fire.client.ConnectionManager.Method;
import es.gob.fire.client.HttpCustomErrors;
import es.gob.fire.client.HttpError;
import es.gob.fire.client.Utils;

/**
 * Cliente del servicio de carga de datos para firma de Clave Firma. Debe
 * recibir los siguientes datos:
 * <ol>
 * <li>subjectId</li>
 * <li>operation</li>
 * <li>format</li>
 * <li>algoritmo</li>
 * <li>extraparams</li>
 * <li>certificate</li>
 * <li>datos a firmar</li>
 * <li>firma a firmar</li>
 * <li>configuracion</li>
 * <li>upgrade</li>
 * </ol>
 *
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s.
 */
public final class HttpLoadProcess {

	private static final String PARAMETER_NAME_APPID = "appId"; //$NON-NLS-1$
	private static final String PARAMETER_NAME_CONFIG = "config"; //$NON-NLS-1$
    private static final String PARAMETER_NAME_ALGORITHM = "algorithm"; //$NON-NLS-1$
    private static final String PARAMETER_NAME_SUBJECT_ID = "subjectId"; //$NON-NLS-1$
    private static final String PARAMETER_NAME_CERT = "cert"; //$NON-NLS-1$
    private static final String PARAMETER_NAME_EXTRA_PARAM = "properties"; //$NON-NLS-1$
    private static final String PARAMETER_NAME_OPERATION = "operation"; //$NON-NLS-1$
    private static final String PARAMETER_NAME_FORMAT = "format"; //$NON-NLS-1$
    private static final String PARAMETER_NAME_DATA = "dat"; //$NON-NLS-1$
    private static final String LOAD_URL = "loadUrl"; //$NON-NLS-1$


    private static final String TAG_NAME_CONFIG = "$$CONFIG$$"; //$NON-NLS-1$
    private static final String TAG_NAME_APP_ID = "$$APPID$$"; //$NON-NLS-1$
    private static final String TAG_NAME_ALGORITHM = "$$ALGORITHM$$"; //$NON-NLS-1$
    private static final String TAG_NAME_SUBJECT_ID = "$$SUBJECTID$$"; //$NON-NLS-1$
    private static final String TAG_NAME_CERT = "$$CERTIFICATE$$"; //$NON-NLS-1$
    private static final String TAG_NAME_EXTRA_PARAM = "$$EXTRAPARAMS$$"; //$NON-NLS-1$
    private static final String TAG_NAME_OPERATION = "$$SUBOPERATION$$"; //$NON-NLS-1$
    private static final String TAG_NAME_FORMAT = "$$FORMAT$$"; //$NON-NLS-1$
    private static final String TAG_NAME_DATA = "$$DATA$$"; //$NON-NLS-1$

    private static final String EQ = "="; //$NON-NLS-1$
    private static final String AM = "&"; //$NON-NLS-1$

    private static final String URL_SUFIX =
            PARAMETER_NAME_CONFIG + EQ + TAG_NAME_CONFIG + AM
            + PARAMETER_NAME_APPID + EQ + TAG_NAME_APP_ID + AM
            + PARAMETER_NAME_ALGORITHM + EQ + TAG_NAME_ALGORITHM + AM
            + PARAMETER_NAME_SUBJECT_ID + EQ + TAG_NAME_SUBJECT_ID + AM
            + PARAMETER_NAME_CERT + EQ + TAG_NAME_CERT + AM
            + PARAMETER_NAME_EXTRA_PARAM + EQ + TAG_NAME_EXTRA_PARAM + AM
            + PARAMETER_NAME_OPERATION + EQ + TAG_NAME_OPERATION + AM
            + PARAMETER_NAME_FORMAT + EQ + TAG_NAME_FORMAT + AM
            + PARAMETER_NAME_DATA + EQ + TAG_NAME_DATA;

    private static String URL;

    private static final Logger LOGGER = Logger.getLogger(HttpLoadProcess.class.getName());

    private static boolean initialized = false;

    /**
     * Constructor privado para no permir la instanciaci&oacute;n
     */
    private HttpLoadProcess() {
        // no instanciable
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

        URL = p.getProperty(LOAD_URL);
        if (URL == null) {
            throw new IllegalStateException(
                    "No esta declarada la configuracion de URL en la configuracion" //$NON-NLS-1$
            );
        }

        try {
			ConnectionManager.configureConnection(p);
		} catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Error en la configuracion de la comunicacion con el componente centralizado: " + e, e); //$NON-NLS-1$
			throw new SecurityException("Error en la configuracion de la comunicacion con el componente centralizado", e); //$NON-NLS-1$
		}

        LOGGER.info(
        		"Se usara el siguiente servicio de carga de datos: " + URL //$NON-NLS-1$
        );
    }

    /**
     * Carga datos para ser posteriormente firmados.
     * @param appId
     *            Identificador de la aplicaci&oacute;n que realiza la
     *            petici&oacute;n.
     * @param subjectId
     *            Identificador del titular de la clave de firma.
     * @param op
     *            Tipo de operaci&oacute;n a realizar.
     * @param ft
     *            Formato de la operaci&oacute;n.
     * @param algth
     *            Algoritmo de firma.
     * @param prop
     *            Propiedades extra a a&ntilde;adir a la firma (puede ser
     *            <code>null</code>).
     * @param cert
     *            Certificado de usuario para realizar la firma.
     * @param d
     *            Datos a firmar.
     * @param conf
     *            Configuraci&oacute;n a indicar al servicio remoto (dependiente
     *            de la implementaci&oacute;n).
     * @return Resultado de la carga.
     * @throws IOException
     *             Cuando no se pueden codificar en base 64 los objetos de propiedades.
     * @throws CertificateEncodingException
     * 				Si el certificado proporcionado no es v&aacute;lido.
     * @throws HttpForbiddenException
     * 				Cuando no se tiene acceso al servicio remoto.
     * @throws HttpNetworkException
     * 				Si hay problemas en la llamada al servicio de red.
     * @throws HttpOperationException
     * 				Cuando ocurre un error durante la ejecuci&oacute;n de la operaci&oacute;n.
     * @throws ClientConfigFilesNotFoundException
     * 				Cuando no se encuentra el fichero de configuraci&oacute;n.
     * @throws HttpNoUserException
     * 				Cuando el usuario no est&eacute; dado de alta en el sistema.
     */
    public static LoadResult loadData(final String appId,
    		final String subjectId,
            final HttpSignProcessConstants.SignatureOperation op,
            final HttpSignProcessConstants.SignatureFormat ft,
            final HttpSignProcessConstants.SignatureAlgorithm algth,
            final Properties prop, final X509Certificate cert, final byte[] d,
            final Properties conf)
            throws IOException, CertificateEncodingException,
            HttpForbiddenException, HttpNetworkException,
            HttpOperationException, ClientConfigFilesNotFoundException,
            HttpNoUserException {

        if (op == null) {
            throw new IllegalArgumentException(
                    "El tipo de operacion de firma a realizar no puede ser nulo" //$NON-NLS-1$
            );
        }
        if (ft == null) {
            throw new IllegalArgumentException(
                    "El formato de firma no puede ser nulo" //$NON-NLS-1$
            );
        }
        if (algth == null) {
            throw new IllegalArgumentException(
                    "El algoritmo de firma no puede ser nulo" //$NON-NLS-1$
            );
        }
        if (d == null) {
            throw new IllegalArgumentException(
                    "Los datos a firmar no pueden ser nulos" //$NON-NLS-1$
            );
        }
        if (subjectId == null || "".equals(subjectId)) { //$NON-NLS-1$
            throw new IllegalArgumentException(
                    "El identificador del titular no puede ser nulo" //$NON-NLS-1$
            );
        }
        if (cert == null) {
            throw new IllegalArgumentException(
                    "El certificado del firmante no puede ser nulo" //$NON-NLS-1$
            );
        }

        final String certB64 = Base64.encode(cert.getEncoded(), true);
        final String dataB64 = Base64.encode(d, true);
        final String extraParamsB64 = Utils.properties2Base64(prop, true);
        final String configB64 = Utils.properties2Base64(conf, true);

        return loadData(appId, subjectId, op.toString(), ft.toString(),
                algth.toString(), extraParamsB64, certB64, dataB64, configB64);
    }

    /**
     * Carga datos para ser posteriormente firmados.
     * @param appId
     *            Identificador de la aplicaci&oacute;n que realiza la
     *            petici&oacute;n.
     * @param subjectId
     *            Identificador del titular de la clave de firma.
     * @param op
     *            Tipo de operaci&oacute;n a realizar: sign, cosign o
     *            countersign.
     * @param ft
     *            Formato de la operaci&oacute;n.
     * @param algth
     *            Algoritmo de firma.
     * @param propB64
     *            Propiedades extra a a&ntilde;adir a la firma  en Base64 (puede ser
     *            <code>null</code>).
     * @param certB64
     *            Certificado de usuario para realizar la firma en Base64.
     * @param dataB64
     *            Datos a firmar en Base64.
     * @param confB64
     *            Configuraci&oacute;n a indicar al servicio remoto (dependiente
     *            de la implementaci&oacute;n).
     * @return Resultado de la carga.
     * @throws HttpForbiddenException
     * 				Cuando no se tiene acceso al servicio remoto.
     * @throws HttpNetworkException
     * 				Si hay problemas en la llamada al servicio de red.
     * @throws HttpOperationException
     * 				Cuando ocurre un error durante la ejecuci&oacute;n de la operaci&oacute;n.
     * @throws ClientConfigFilesNotFoundException
     * 				Cuando no se encuentra el fichero de configuraci&oacute;n.
     * @throws HttpNoUserException
     * 				Cuando el usuario no est&eacute; dado de alta en el sistema.
     */
    public static LoadResult loadData(final String appId,
    		final String subjectId, final String op, final String ft,
    		final String algth, final String propB64, final String certB64,
    		final String dataB64, final String confB64)
    				throws HttpForbiddenException, HttpNetworkException,
    				HttpOperationException, ClientConfigFilesNotFoundException,
    				HttpNoUserException {

    	initialize();

    	if (op == null) {
            throw new IllegalArgumentException(
                    "El tipo de operacion de firma a realizar no puede ser nulo" //$NON-NLS-1$
            );
        }
        if (ft == null) {
            throw new IllegalArgumentException(
                    "El formato de firma no puede ser nulo" //$NON-NLS-1$
            );
        }
        if (algth == null) {
            throw new IllegalArgumentException(
                    "El algoritmo de firma no puede ser nulo" //$NON-NLS-1$
            );
        }
        if (dataB64 == null) {
            throw new IllegalArgumentException(
                    "Los datos a firmar no pueden ser nulos" //$NON-NLS-1$
            );
        }
        if (subjectId == null || "".equals(subjectId)) { //$NON-NLS-1$
            throw new IllegalArgumentException(
                    "El identificador del titular no puede ser nulo" //$NON-NLS-1$
            );
        }
        if (certB64 == null) {
            throw new IllegalArgumentException(
                    "El certificado del firmante no puede ser nulo" //$NON-NLS-1$
            );
        }

        final String urlParameters =
        		URL_SUFIX
        		.replace(TAG_NAME_APP_ID, appId)
                .replace(TAG_NAME_SUBJECT_ID, subjectId)
                .replace(TAG_NAME_OPERATION, op)
                .replace(TAG_NAME_FORMAT, ft)
                .replace(TAG_NAME_ALGORITHM, algth)
                .replace(
                        TAG_NAME_EXTRA_PARAM,
                        propB64 != null ? Utils.doBase64UrlSafe(propB64) : "") //$NON-NLS-1$
                .replace(TAG_NAME_CERT, Utils.doBase64UrlSafe(certB64))
                .replace(TAG_NAME_DATA, Utils.doBase64UrlSafe(dataB64))
                .replace(
                        TAG_NAME_CONFIG,
                        confB64 != null ? Utils.doBase64UrlSafe(confB64) : ""); //$NON-NLS-1$

        final byte[] responseJSON;
        try {
        	responseJSON = ConnectionManager.readUrl(
                            URL, urlParameters, Method.POST);
        }
        catch (final IOException e) {
        	if (e instanceof HttpError) {
        		final HttpError he = (HttpError) e;
        		LOGGER.severe(
        				"Error en la llamada al servicio de carga de datos: " + he.getResponseDescription() //$NON-NLS-1$
        				);
        		if (he.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN) {
        			throw new HttpForbiddenException(he.getResponseDescription(), he);
        		} else if (he.getResponseCode() == HttpURLConnection.HTTP_CLIENT_TIMEOUT) {
        			throw new HttpNetworkException(he.getResponseDescription(), he);
        		} else if (he.getResponseCode() == HttpCustomErrors.NO_USER.getErrorCode()) {
        			throw new HttpNoUserException(he.getResponseDescription(), he);
        		} else {
        			throw new HttpOperationException(he.getResponseDescription(), he);
        		}
        	}
        	LOGGER.severe("Error en la llamada al servicio remoto de carga de datos: " + e); //$NON-NLS-1$
        	throw new HttpNetworkException("Error en la llamada al servicio remoto de carga de datos", e); //$NON-NLS-1$
        }
        catch (final Exception e) {
        	LOGGER.log(
        			Level.SEVERE,
        			"Error en la invocacion al servicio de recuperacion de carga de datos", e //$NON-NLS-1$
        			);
        	throw new HttpOperationException("Error en la invocacion al servicio de recuperacion de carga de datos", e); //$NON-NLS-1$
        }

        try {
        	return new LoadResult(new String(responseJSON));
        }
        catch (final Exception e) {
        	throw new HttpOperationException("Error al componer el resultado de la operacion en JSON", e); //$NON-NLS-1$
        }
    }
}
