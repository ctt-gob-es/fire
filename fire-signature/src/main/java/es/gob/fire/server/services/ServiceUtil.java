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
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import es.gob.afirma.core.misc.Base64;
import es.gob.fire.signature.AplicationsDAO;
import es.gob.fire.signature.ConfigManager;
import es.gob.fire.signature.DBConnectionException;

/**
 *  Conjunto de funciones est&aacute;ticas de car&aacute;cter general.
 */
public final class ServiceUtil {

    private static final Logger LOGGER = Logger.getLogger(ServiceUtil.class.getName());


    private ServiceUtil() {
        // No instanciable
    }


    /**
     * Transforma un Base64 URL Safe en un Base64 corriente.
     * @param b64 Cadena Base64 URl Safe.
     * @return Cadena Base64 corriente.
     */
    public static String undoUrlSafe(final String b64) {
        if (b64 == null) {
            throw new IllegalArgumentException(
                    "Los datos a deshacer del URL SAFE no pueden ser nulos" //$NON-NLS-1$
            );
        }
        return b64.replace("-", "+").replace("_", "/"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }

	/** Convierte una cadena Base64 en un objeto de propiedades.
	 * @param base64 Base64 que descodificado es un fichero de propiedades en texto plano.
	 * @return Objeto de propiedades.
	 * @throws IOException Si hay problemas en el proceso. */
    public static Properties base642Properties(final String base64) throws IOException {
    	final Properties p = new Properties();
    	if (base64 == null || base64.isEmpty()) {
    		return p;
    	}

    	p.load(new InputStreamReader(
    			new ByteArrayInputStream(
    					Base64.decode(base64, base64.indexOf('-') > -1 || base64.indexOf('_') > -1)
    					),
    			StandardCharsets.UTF_8));

    	return p;
    }

    /** Convierte un objeto de propiedades en una cadena Base64 con todos sus elementos.
	 * @param p Objeto de propiedades.
	 * @return Cadena Base64. */
    public static String properties2Base64(final Properties p) {

    	if (p == null) {
    		return ""; //$NON-NLS-1$
    	}
    	final StringBuilder buffer = new StringBuilder();
    	for (final String k : p.keySet().toArray(new String[p.size()])) {
    		buffer.append(k).append("=").append(p.getProperty(k)).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
    	}
    	return Base64.encode(buffer.toString().getBytes());
    }

    /**
     * Devuelve una lista de certificados X509 de la cabecera de una peticion HTTP
     * @param request Cabecera HTTP recibida.
     * @return Lista de los certificados X509 contenidos en la cabecera HTTP o
     * {@code null} si no hay ninguno.
     */
    static X509Certificate[] getCertificatesFromRequest(final HttpServletRequest request){

    	X509Certificate[] certificates = getCertificatesFromAttribute(request);
    	if (certificates == null) {
    		final String propName = ConfigManager.getHttpsCertAttributeHeader();
    		if (propName != null && !propName.isEmpty()) {
    			certificates = getCertificatesFromHeader(request, propName);
    		}
    	}
    	return certificates;
    }

    /**
     * Recupera los certificados de autenticacion SSL como atributo de las peticiones.
     * Esto ser&aacute; cuando se transmitan al servidor de aplicaciones los certificados mediante
     * AJP.
     * @param request Petici&oacute;n con los certificados.
     * @return Lista de los certificados X509 enviados o {@code null} se enviaron como
     * atributos de la petici&oacute;n.
     */
    private static X509Certificate[] getCertificatesFromAttribute(final HttpServletRequest request) {
    	final Object[] cer = (Object[]) request.getAttribute("javax.servlet.request.X509Certificate"); //$NON-NLS-1$
    	if (cer == null){
    		return null;
    	}
    	final X509Certificate[] certificates = new X509Certificate[cer.length];
    	for (int i = 0; i < cer.length; i++) {
    		certificates[i] = (X509Certificate) cer[i];
  	   	}
    	return certificates;
    }

    /**
     * Recupera los certificados de autenticacion SSL como una propiedad de la cabecera de las
     * peticiones. Se deber&aacute; indicar el nombre de la propiedad de la cabecera en la que
     * se transmiten los certificados.
     * @param request Petici&oacute;n con los certificados.
     * @return Lista de los certificados X509 enviados o {@code null} se enviaron en la propiedad
     * de la cabecera.
     */
    private static X509Certificate[] getCertificatesFromHeader(final HttpServletRequest request, final String propName)
    {
        X509Certificate certificates[] = null;
        final String headerName = propName;
        String headerCert = request.getHeader(headerName);
        try {
            if(headerCert != null && !headerCert.isEmpty()) {
                headerCert = headerCert.replace("-----BEGIN CERTIFICATE----- ", ""); //$NON-NLS-1$ //$NON-NLS-2$
                headerCert = headerCert.replace(" -----END CERTIFICATE-----", ""); //$NON-NLS-1$ //$NON-NLS-2$
                final byte certBytes[] = Base64.decode(headerCert);
                final ByteArrayInputStream bis = new ByteArrayInputStream(certBytes);
                final CertificateFactory fact = CertificateFactory.getInstance("X.509"); //$NON-NLS-1$
                final X509Certificate cer = (X509Certificate)fact.generateCertificate(bis);
                certificates = new X509Certificate[1];
                certificates[0] = cer;
            } else {
                LOGGER.warning("No existe certficado en la request en el header " + headerName); //$NON-NLS-1$
            }
        }
        catch(final Exception ex) {
            LOGGER.severe("Ha ocurrido un error al extraer certificado : " + ex); //$NON-NLS-1$
        }
        return certificates;
    }

    /** Devuelve la huella del certificado en Base64.
     * @param cert Certificado del que extraer su huella.
     * @return Huella obtenida codificada en Base64.
     * @throws NoSuchAlgorithmException Si no se encuentra el algoritmo SHA-1.
     * @throws CertificateEncodingException Si hay alg&uacute;n problema codificando el certificado. */
	private static String getThumbPrint(final X509Certificate cert) throws NoSuchAlgorithmException,
	                                                                       CertificateEncodingException {
		final MessageDigest md = MessageDigest.getInstance("SHA-1"); //$NON-NLS-1$
		final byte[] der = cert.getEncoded();
		md.update(der);
		final byte[] digest = md.digest();
		return Base64.encode(digest);
	}

	/** Comprueba si la huella del certificado pasada est&aacute;n registrada en la base de datos.
	 * @param appId Identificador de la aplicaci&oacute;n.
	 * @param thumbPrint Huella a encontrar en la base de datos.
	 * @throws SQLException Si ocurre una excepci&oacute;n en el acceso a la base de datos.
	 * @throws IllegalAccessException Si la huella no se encuentra registrada en el sistema.
	 * @throws IOException Si hay un error de entrada o salida.
	 * @throws CertificateException Si hay un problema al decodificar el certificado.
	 * @throws NoSuchAlgorithmException No se encuentra el algoritmo en el sistema.
	 * @throws DataBaseConnectionException No se ha podido inicializar la conexi&oacute;n con la base de datos.
	 */
	private static void checkValideThumbPrint(final String appId, final String thumbPrint) throws SQLException,
	                                                                          IllegalAccessException, CertificateException,
	                                                                          NoSuchAlgorithmException, IOException, DBConnectionException {
		if (!AplicationsDAO.checkThumbPrint(appId, thumbPrint)) {
    		throw new IllegalAccessException("El certificado utilizado no tiene permiso para acceder"); //$NON-NLS-1$
    	}
	}

	/** Valida si el certificado que se le pasa a la petici&oacute;n tiene permisos.
	 * Lee el certificado de la cabecera HTTP.
	 * @param appId Identificador de la aplicaci&oacute;n.
	 * @param certificates Listado de certificados
	 * @throws CertificateValidationException En caso de ocurrir alg&uacute;n error o si el certificado
	 *                                        no tiene acceso. */
	public static void checkValidCertificate(final String appId, final X509Certificate[] certificates) throws CertificateValidationException {

		if (certificates == null || certificates.length == 0 || certificates[0] == null) {
			LOGGER.severe("No se ha recibido ningun certificado para la autenticacion del cliente"); //$NON-NLS-1$
			throw new CertificateValidationException (HttpServletResponse.SC_UNAUTHORIZED, "No se ha recibido ningun certificado para la autenticacion del cliente"); //$NON-NLS-1$
		}

		try {
			final String thumbPrint = ServiceUtil.getThumbPrint(certificates[0]);
			ServiceUtil.checkValideThumbPrint(appId, thumbPrint);
		}
		catch (final NoSuchAlgorithmException e) {
			LOGGER.severe("El algorimto SHA-1 no se ha encontrado en el sistema : " + e); //$NON-NLS-1$
			throw new CertificateValidationException (HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "El algorimto SHA-1 no se ha encontrado en el sistema.", e);//$NON-NLS-1$
		}
		catch (final IllegalArgumentException e){
			LOGGER.severe("Ha ocurrido un error con los parametros de la llamada : " + e); //$NON-NLS-1$
			throw new CertificateValidationException (HttpServletResponse.SC_BAD_REQUEST, "Ha ocurrido un error con los parametros de la llamada, no se ha recibido ningun certificado.", e); //$NON-NLS-1$
		}
		catch (final IllegalAccessException e) {
			LOGGER.severe("Acceso no permitido : " + e); //$NON-NLS-1$
			throw new CertificateValidationException (HttpServletResponse.SC_UNAUTHORIZED, "Acceso no permitido. El certificado utilizado no tiene permiso para acceder.", e); //$NON-NLS-1$
		}
		catch (final SQLException e) {
			LOGGER.severe("Ha ocurrido un problema en el acceso a la base de datos : " + e); //$NON-NLS-1$
			throw new CertificateValidationException (HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ha ocurrido un problema en el acceso a la base de datos.", e); //$NON-NLS-1$
		}
		catch (final DBConnectionException e) {
			LOGGER.severe("Ha ocurrido un error al conectar con la base de datos : " + e); //$NON-NLS-1$
			throw new CertificateValidationException (HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ha ocurrido un error al conectar con la base de datos.", e); //$NON-NLS-1$
		}
		catch (final Exception e) {
			LOGGER.severe("Ha ocurrido un error al decodificar el certificado: " + e); //$NON-NLS-1$
			throw new CertificateValidationException(HttpServletResponse.SC_BAD_REQUEST, "Ha ocurrido un error al decodificar el certificado.", e); //$NON-NLS-1$
		}
	}
}
