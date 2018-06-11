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
import es.gob.clavefirma.client.ConnectionManager;
import es.gob.clavefirma.client.ConnectionManager.Method;
import es.gob.clavefirma.client.HttpForbiddenException;
import es.gob.clavefirma.client.HttpNetworkException;
import es.gob.clavefirma.client.HttpOperationException;
import es.gob.fire.client.Base64;
import es.gob.fire.client.ConfigManager;
import es.gob.fire.client.HttpError;
import es.gob.fire.client.Utils;

/**
 * Cliente del servicio de firma electr&oacute;nica. La operaci&oacute;n a
 * realizar podr&aacute; ser firma, cofirma o contrafirma.
 */
public final class HttpSignProcess {

    private static String URL_BASE;

    private static final Logger LOGGER = Logger.getLogger(HttpSignProcess.class.getName());

    private static boolean initialized = false;

    // Parametros que necesitamos de la URL.
    private static final String APPID = "%APPID%"; //$NON-NLS-1$
    private static final String TRANSACTION = "%TRANSACTION%"; //$NON-NLS-1$
    private static final String OP = "%OP%"; //$NON-NLS-1$
    private static final String ALG = "%ALG%"; //$NON-NLS-1$
    private static final String FORMAT = "%FT%"; //$NON-NLS-1$
    private static final String CERT = "%CERT%"; //$NON-NLS-1$
    private static final String DATA = "%DATA%"; //$NON-NLS-1$
    private static final String TRIPHASE_DATA = "%TDATA%"; //$NON-NLS-1$

    private static final String AMP = "&"; //$NON-NLS-1$

    private static final String URL_PARAMETERS_SIGN_PROCESS =
    		"appId=" + APPID + AMP + //$NON-NLS-1$
    		"transactionid=" + TRANSACTION + AMP + //$NON-NLS-1$
            "operation=" + OP + AMP + //$NON-NLS-1$
            "algorithm=" + ALG + AMP + //$NON-NLS-1$
            "format=" + FORMAT + AMP + //$NON-NLS-1$
            "cert=" + CERT + AMP + //$NON-NLS-1$
            "data=" + DATA + AMP + //$NON-NLS-1$
            "tri=" + TRIPHASE_DATA; //$NON-NLS-1$

    /**
     * Constructor privado para no permir la instanciaci&oacute;n
     */
    private HttpSignProcess() {
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

    	URL_BASE = p.getProperty("signUrl"); //$NON-NLS-1$
    	if (URL_BASE == null) {
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

    	LOGGER.info("Se usara el siguiente servicio de firma: " + URL_BASE); //$NON-NLS-1$
    }

    /**
     * Firma unos datos haciendo uso del servicio de red de firma en la nube.
     *
     * @param appId
     *            Identificador de la aplicaci&oacute;n que realiza la
     *            petici&oacute;n.
     * @param transactionId
     *            Identificador de la transacci&oacute;n.
     * @param op
     *            Tipo de operaci&acute;n a realizar.
     * @param ft
     *            Formato de la operaci&oacute;n.
     * @param algth
     *            Algoritmo de firma.
     * @param prop
     *            Propiedades extra a a&ntilde;adir a la firma (puede ser
     *            <code>null</code>).
     * @param cert
     *            Certificado de usuario para realizar la firma.
     * @param data
     *            Datos a firmar.
     * @param td
     *            Informaci&oacute;n de la operaci&oacute;n trif&aacute;sica.
     * @param upgrade
     *            Formato al que queremos mejorar la firma (puede ser
     *            <code>null</code>).
     * @return Firma realizada en servidor.
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
     */
    public static byte[] sign(final String appId,
    		final String transactionId,
    		final HttpSignProcessConstants.SignatureOperation op,
    		final HttpSignProcessConstants.SignatureFormat ft,
    		final HttpSignProcessConstants.SignatureAlgorithm algth,
    		final Properties prop, final X509Certificate cert,
    		final byte[] data, final TriphaseData td,
    		final HttpSignProcessConstants.SignatureUpgrade upgrade)
    				throws IOException, CertificateEncodingException, HttpForbiddenException,
    				HttpNetworkException, HttpOperationException, ClientConfigFilesNotFoundException {

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
        if (data == null) {
            throw new IllegalArgumentException(
                    "Los datos a firmar no pueden ser nulos" //$NON-NLS-1$
            );
        }
        if (td == null) {
            throw new IllegalArgumentException(
                    "Los datos de la operacion trifasica no pueden ser nulos" //$NON-NLS-1$
            );
        }
        if (cert == null) {
            throw new IllegalArgumentException(
                    "El certificado del firmante no puede ser nulo" //$NON-NLS-1$
            );
        }
        if (transactionId == null) {
            throw new IllegalArgumentException(
                    "El id de la transaccion no puede ser nulo" //$NON-NLS-1$
            );
        }

        return sign(
        		appId,
        		transactionId,
        		op.toString(),
        		ft.toString(),
                algth.toString(),
                prop != null ? Utils.properties2Base64(prop, true) : "", //$NON-NLS-1$
                Base64.encode(cert.getEncoded(), true),
                Base64.encode(data, true),
                Base64.encode(td.toString().getBytes(), true),
                upgrade != null ? upgrade.toString() : null
        );

    }

    /**
     * Firma unos datos haciendo uso del servicio de red de firma en la nube.
     *
     * @param appId
     *            Identificador de la aplicaci&oacute;n que realiza la
     *            petici&oacute;n.
     * @param transactionId
     *            Identificador de la transacci&oacute;n.
     * @param op
     *            Tipo de operaci&oacute;n a realizar: "sign", "cosign" o
     *            "countersign".
     * @param ft
     *            Formato de la operaci&oacute;n.
     * @param algth
     *            Algoritmo de firma.
     * @param prop
     *            Propiedades extra de configuraci&oacute;n de la firma en Base64 URL SAFE (puede ser
     *            <code>null</code>).
     * @param cert
     *            Certificado de usuario en Base64 para realizar la firma.
     * @param dataB64
     *            Datos a firmar en Base64.
     * @param tdB64
     *            Datos de la operaci&oacute;n trif&aacute;sica en Base64.
     * @param upgrade
     *            Formato al que queremos mejorar la firma (puede ser
     *            <code>null</code>).
     * @return Firma realizada en servidor.
     * @throws HttpForbiddenException
     * 				Cuando no se tiene acceso al servicio remoto.
     * @throws HttpNetworkException
     * 				Si hay problemas en la llamada al servicio de red.
     * @throws HttpOperationException
     * 				Cuando ocurre un error durante la ejecuci&oacute;n de la operaci&oacute;n.
     * @throws ClientConfigFilesNotFoundException
     * 				Cuando no se encuentra el fichero de configuraci&oacute;n.
     */
    public static byte[] sign(final String appId, final String transactionId,
    		final String op, final String ft, final String algth,
    		final String prop, final String cert, final String dataB64,
    		final String tdB64, final String upgrade)
    				throws HttpForbiddenException, HttpNetworkException,
    				HttpOperationException, ClientConfigFilesNotFoundException {

    	initialize();

        if (transactionId == null) {
            throw new IllegalArgumentException(
                    "El id de la transaccion no puede ser nulo" //$NON-NLS-1$
            );
        }
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
        if (tdB64 == null) {
            throw new IllegalArgumentException(
                    "Los datos de la operacion trifasica no pueden ser nulos" //$NON-NLS-1$
            );
        }
        if (cert == null) {
            throw new IllegalArgumentException(
                    "El certificado del firmante no puede ser nulo" //$NON-NLS-1$
            );
        }

        String urlParameters = URL_PARAMETERS_SIGN_PROCESS
        		.replace(APPID, appId)
                .replace(TRANSACTION, transactionId)
                .replace(OP, op)
                .replace(ALG, algth)
                .replace(FORMAT, ft)
                .replace(CERT, Utils.doBase64UrlSafe(cert))
                .replace(DATA, Utils.doBase64UrlSafe(dataB64))
                .replace(TRIPHASE_DATA, Utils.doBase64UrlSafe(tdB64));

        if (upgrade != null && !upgrade.isEmpty()) {
        	urlParameters += "&upgrade=" + upgrade; //$NON-NLS-1$
        }
        if (prop != null && !prop.isEmpty()) {
        	urlParameters += "&properties=" + prop.replace('+', '-').replace('/', '_'); //$NON-NLS-1$
        }

        try {
        	return ConnectionManager.readUrl(URL_BASE, urlParameters, Method.POST);
        }
        catch (final IOException e) {
        	if (e instanceof HttpError) {
        		final HttpError he = (HttpError) e;
        		LOGGER.severe(
        				"Error en la consulta al servicio de recuperacion de firma: " + he.getResponseDescription() //$NON-NLS-1$
        				);
        		switch (he.getResponseCode()) {
        		case HttpURLConnection.HTTP_FORBIDDEN:
        			throw new HttpForbiddenException(he.getResponseDescription(), he);
        		case HttpURLConnection.HTTP_CLIENT_TIMEOUT:
        			throw new HttpNetworkException(he.getResponseDescription(), he);
        		default:
        			throw new HttpOperationException(
        					he.getResponseDescription(), he);
        		}
        	}
        	LOGGER.severe("Error en la llamada al servicio remoto de recuperacion de firma: " + e); //$NON-NLS-1$
        	throw new HttpNetworkException("Error en la llamada al servicio remoto de recuperacion de firma", e); //$NON-NLS-1$
        }
        catch (final Exception e) {
        	LOGGER.log(
        			Level.SEVERE,
        			"Error en la invocacion al servicio de recuperacion de firma", e //$NON-NLS-1$
        			);
        	throw new HttpOperationException("Error en la invocacion al servicio de recuperacion de firma", e); //$NON-NLS-1$
        }
    }
}
