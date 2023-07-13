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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Properties;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import es.gob.afirma.core.misc.Base64;
import es.gob.fire.server.services.internal.ApplicationAccessChecker;
import es.gob.fire.server.services.internal.ApplicationAccessInfo;
import es.gob.fire.server.services.internal.ApplicationInfo;
import es.gob.fire.server.services.internal.ApplicationsDAOFactory;
import es.gob.fire.server.services.internal.TransactionAuxParams;
import es.gob.fire.signature.ConfigManager;

/**
 *  Conjunto de funciones est&aacute;ticas de car&aacute;cter general.
 */
public final class ServiceUtil {

	private static final Logger LOGGER = Logger.getLogger(ServiceUtil.class.getName());

    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private static final String[] CERT_REQUEST_ATTRS = {
    		// Atributo usado por los clientes Apache Tomcat
    		"javax.servlet.request.X509Certificate", //$NON-NLS-1$
    		// Atributo usado por los clientes Oracle WebLogic
    		"java.security.cert.X509Certificate", //$NON-NLS-1$
    		// Atributo usado por los clientes antiguos
    		"javax.net.ssl.peer_certificates" //$NON-NLS-1$
    };

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
    			DEFAULT_CHARSET));

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
    	return Base64.encode(buffer.toString().getBytes(DEFAULT_CHARSET));
    }

    /**
     * Obtiene la cadena de certificados cliente de la petici&oacute;n HTTP.
     * @param request Petici&oacute; HTTP recibida.
     * @return Cadena de certificados X509.
     * @throws IOException Cuando no se encuentra el certificado cliente.
     * @throws CertificateValidationException Cuando no se puede decodificar el certificado cliente.
     */
    static X509Certificate[] getCertificatesFromRequest(final HttpServletRequest request) throws IOException, CertificateValidationException {

    	X509Certificate[] certificates = getCertificatesFromAttribute(request);
    	if (certificates == null) {
    		final String propName = ConfigManager.getHttpsCertAttributeHeader();
    		if (propName != null && !propName.isEmpty()) {
    			certificates = getCertificatesFromHeader(request, propName);
    			if (certificates == null) {
    				throw new IOException(String.format("No se encontro el certificado cliente como atributo de la peticion ni en el atributo %1s de su cabecera", propName)); //$NON-NLS-1$
    			}
    		}
    		else {
    			throw new IOException("No se encontro el certificado cliente como atributo de la peticion"); //$NON-NLS-1$
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

    	// Revisamos los posibles atributos en los que se pueda encontrar el
    	// certificado
    	int i = 0;
    	Object[] cer;
    	do {
    		cer = (Object[]) request.getAttribute(CERT_REQUEST_ATTRS[i++]);
    	} while (cer == null && i < CERT_REQUEST_ATTRS.length);

    	// Si no lo hemos encontrado, devolvemos null
    	if (cer == null) {
			return null;
		}

    	// Cargamos los certificados
    	final X509Certificate[] certificates = new X509Certificate[cer.length];
    	for (int j = 0; j < cer.length; j++) {
    		certificates[j] = (X509Certificate) cer[j];
  	   	}
    	return certificates;
    }

    /**
     * Recupera los certificados de autenticacion SSL como una propiedad de la cabecera de las
     * peticiones. Se deber&aacute; indicar el nombre de la propiedad de la cabecera en la que
     * se transmiten los certificados.
     * @param request Petici&oacute;n con los certificados.
     * @param propName Nombre de la propiedad de la que extraer los certificados.
     * @return Lista de los certificados X509 enviados o {@code null} se enviaron en la propiedad
     * de la cabecera.
     * @throws CertificateValidationException Cuando ocurre un error al extraer el certificado de la cabecera de la
     * petici&oacute;n o cuando no se puede componer el certificado extra&iacute;do.
     */
    private static X509Certificate[] getCertificatesFromHeader(final HttpServletRequest request,
    		final String propName) throws CertificateValidationException {

        X509Certificate certificates[] = null;
        final String headerName = propName;
        String headerCert = request.getHeader(headerName);
        try {
            if (headerCert != null && !headerCert.isEmpty()) {
                headerCert = headerCert.replace("-----BEGIN CERTIFICATE----- ", ""); //$NON-NLS-1$ //$NON-NLS-2$
                headerCert = headerCert.replace(" -----END CERTIFICATE-----", ""); //$NON-NLS-1$ //$NON-NLS-2$
                final byte certBytes[] = Base64.decode(headerCert);
                final ByteArrayInputStream bis = new ByteArrayInputStream(certBytes);
                final CertificateFactory fact = CertificateFactory.getInstance("X.509"); //$NON-NLS-1$
                final X509Certificate cer = (X509Certificate)fact.generateCertificate(bis);
                certificates = new X509Certificate[1];
                certificates[0] = cer;
            }
        }
        catch(final Exception ex) {
            throw new CertificateValidationException(FIReError.PARAMETER_AUTHENTICATION_CERTIFICATE_INVALID,
            		"Error al extraer el certificado de la cabecera de la peticion", ex); //$NON-NLS-1$
        }
        return certificates;
    }

    /**
     * Comprueba que una petici&oacute;n tenga acceso.
     * @param appId Identificador declarado por la aplicaci&oacute;n.
     * @param request Petici&oacute;n realizada.
	 * @param trAux Informaci&oacute;n auxiliar de la transacci&oacute;n.
     * @return La informaci&oacute;n de la aplicaci&oacute;n asociada a la petici&oacute;n o {@code null}
     * si no se llega a indicar el identificador de aplicaci&oacute;n.
     * @throws IOException Cuando no se pude validar la petici&oacute;n.
     * @throws IllegalArgumentException Cuando no se indica el identificador de aplicacion y es obligatorio.
     * @throws UnauthorizedApplicacionException Cuando la aplicaci&oacute;n no est&aacute; autorizada.
     * @throws CertificateValidationException Cuando no se puede comprobar la validez del certificado cliente.
     */
	public static ApplicationInfo checkAccess(final String appId, final HttpServletRequest request, final TransactionAuxParams trAux)
			throws IOException, IllegalArgumentException, UnauthorizedApplicacionException, CertificateValidationException {

		ApplicationInfo appInfo = null;

    	if (ConfigManager.isCheckApplicationNeeded() || ConfigManager.isCheckCertificateNeeded()) {
    		LOGGER.fine(trAux.getLogFormatter().f("Se comprueba que la aplicacion este dada de alta en el sistema")); //$NON-NLS-1$

    		if (appId == null) {
    			throw new IllegalArgumentException("No se ha proporcionado el identificador de aplicacion"); //$NON-NLS-1$
    		}

    		ApplicationAccessInfo registeredAppInfo = null;
			try {
				registeredAppInfo = ApplicationsDAOFactory.getApplicationsDAO()
						.getApplicationAccessInfo(appId, trAux);
			}
			catch (final IOException e) {
				throw new IOException("No se pudo obtener la informacion del sistema para la validacion de la peticion", e); //$NON-NLS-1$
			}

			appInfo = new ApplicationInfo(appId, registeredAppInfo.getName());

    		// Comprobamos que la aplicacion este registrada en el sistema y habilitada
    		if (ConfigManager.isCheckApplicationNeeded()) {
    			LOGGER.fine(trAux.getLogFormatter().f("Se realizara la validacion del Id de aplicacion")); //$NON-NLS-1$
    			try {
    				ApplicationAccessChecker.checkAppEnabled(registeredAppInfo);
    			}
    			catch (final IllegalAccessException e) {
    				throw new UnauthorizedApplicacionException("La aplicacion no se encuentra habilitada", e); //$NON-NLS-1$
    			}
    		}
    		else {
    			LOGGER.fine(trAux.getLogFormatter().f("No se realiza la validacion del identificador de aplicacion")); //$NON-NLS-1$
    		}

    		if (ConfigManager.isCheckCertificateNeeded()) {
    			LOGGER.fine(trAux.getLogFormatter().f("Se realizara la validacion del certificado")); //$NON-NLS-1$

    			X509Certificate[] certificates;
    			try {
    				certificates = ServiceUtil.getCertificatesFromRequest(request);
    			}
    			catch (final IOException e) {
    				throw new CertificateValidationException(FIReError.PARAMETER_AUTHENTICATION_CERTIFICATE_NEEDED, "No se encontro el certificado cliente en la peticion entrante", e); //$NON-NLS-1$
    			}

    			try {
    				ApplicationAccessChecker.checkValidCertificate(certificates, registeredAppInfo);
    			}
    			catch (final IllegalAccessException e) {
    				throw new UnauthorizedApplicacionException("El certificado no esta autorizado", e); //$NON-NLS-1$
    			}
    		}
    		else {
    			LOGGER.fine(trAux.getLogFormatter().f("No se valida el certificado"));//$NON-NLS-1$
    		}
    	}

    	if (appInfo == null && appId != null) {
    		appInfo = new ApplicationInfo(appId, appId);
    	}

		return appInfo;
	}

//    /**
//	 * Valida si la aplicaci&oacute;n indicada existe y si se encuentra activada.
//	 * @param appId Identificador de la aplicaci&oacute;n.
//	 * @param trAux Informaci&oacute;n auxiliar de la transacci&oacute;n.
//	 * @return Nombre de la aplicaci&oacute;n o {@code null} si no est&aacute; definido.
//	 * @throws DBConnectionException Cuando ocurre un error al conectar con la base de datos.
//     * @throws InvalidApplicationException Cuando la aplicaci&oacute;n no existe.
//	 * @throws InactiveApplicationException Cuando la aplicaci&oacute;n est&aacute; desactivada
//	 */
//	public static String checkValidApplication(final String appId, final TransactionAuxParams trAux)
//			throws DBConnectionException, InvalidApplicationException, InactiveApplicationException {
//
//		ApplicationChecking appCheck;
//		try {
//			appCheck = ApplicationsDAO.checkApplicationId(appId, trAux);
//		}
//		catch (final Exception e) {
//			throw new DBConnectionException("Ha ocurrido un problema en el acceso a la base de datos", e); //$NON-NLS-1$
//		}
//    	if (!appCheck.isValid()) {
//    		throw new InvalidApplicationException("Se proporciono un identificador de aplicacion no valido"); //$NON-NLS-1$
//    	}
//    	if (!appCheck.isEnabled()) {
//    		throw new InactiveApplicationException("Se proporciono un identificador de aplicacion desactivada"); //$NON-NLS-1$
//    	}
//    	return appCheck.getName();
//	}
//
//	/**
//	 * Valida si el certificado que se le pasa a la petici&oacute;n tiene permisos.
//	 * Lee el certificado de la cabecera HTTP.
//	 * @param appId Identificador de la aplicaci&oacute;n.
//	 * @param certificates Listado de certificados.
//	 * @param trAux Informaci&oacute;n auxiliar de la transacci&oacute;n.
//	 * @throws CertificateValidationException En caso de ocurrir alg&uacute;n error o si el certificado
//	 *                                        no tiene acceso.
//	 * @throws DBConnectionException Cuando ocurre un error al conectar con la base de datos.
//	 */
//	public static void checkValidCertificate(final String appId, final X509Certificate[] certificates,
//			final TransactionAuxParams trAux)
//			throws CertificateValidationException, DBConnectionException {
//
//		if (certificates == null || certificates.length == 0 || certificates[0] == null) {
//			throw new CertificateValidationException(FIReError.UNAUTHORIZED, "No se ha recibido ningun certificado para la autenticacion del cliente"); //$NON-NLS-1$
//		}
//
//		try {
//			final String thumbPrint = ServiceUtil.getThumbPrint(certificates[0]);
//			ServiceUtil.checkValideThumbPrint(appId, thumbPrint, trAux);
//		}
//		catch (final NoSuchAlgorithmException e) {
//			throw new CertificateValidationException(FIReError.INTERNAL_ERROR, "El algoritmo de huella no se ha encontrado en el sistema", e);//$NON-NLS-1$
//		}
//		catch (final IllegalArgumentException e){
//			throw new CertificateValidationException(FIReError.PARAMETER_AUTHENTICATION_CERTIFICATE_NEEDED, "Ha ocurrido un error con los parametros de la llamada, no se ha recibido ningun certificado", e); //$NON-NLS-1$
//		}
//		catch (final IllegalAccessException e) {
//			throw new CertificateValidationException(FIReError.UNAUTHORIZED, "Acceso no permitido. El certificado utilizado no tiene permiso para acceder", e); //$NON-NLS-1$
//		}
//		catch (final SQLException e) {
//			throw new DBConnectionException ("Ha ocurrido un problema en el acceso a la base de datos", e); //$NON-NLS-1$
//		}
//		catch (final Exception e) {
//			throw new CertificateValidationException(FIReError.PARAMETER_AUTHENTICATION_CERTIFICATE_INVALID, "Ha ocurrido un error al decodificar el certificado", e); //$NON-NLS-1$
//		}
//	}
//
//    /**
//     * Devuelve la huella del certificado en Base64.
//     * @param cert Certificado del que extraer su huella.
//     * @return Huella obtenida codificada en Base64.
//     * @throws NoSuchAlgorithmException Si no se encuentra el algoritmo SHA-1.
//     * @throws CertificateEncodingException Si hay alg&uacute;n problema codificando el certificado.
//     */
//	private static String getThumbPrint(final X509Certificate cert) throws NoSuchAlgorithmException,
//	                                                                       CertificateEncodingException {
//		final MessageDigest md = MessageDigest.getInstance("SHA-1"); //$NON-NLS-1$
//		return Base64.encode(md.digest(cert.getEncoded()));
//	}
//
//	/**
//	 * Comprueba si la huella del certificado pasada est&aacute;n registrada en la base de datos.
//	 * @param appId Identificador de la aplicaci&oacute;n.
//	 * @param thumbPrint Huella a encontrar en la base de datos.
//	 * @param trAux Informaci&oacute;n auxiliar de la transacci&oacute;n.
//	 * @throws SQLException Si ocurre una excepci&oacute;n en el acceso a la base de datos.
//	 * @throws IllegalAccessException Si la huella no se encuentra registrada en el sistema.
//	 * @throws IOException Si hay un error de entrada o salida.
//	 * @throws CertificateException Si hay un problema al decodificar el certificado.
//	 * @throws NoSuchAlgorithmException No se encuentra el algoritmo en el sistema.
//	 */
//	private static void checkValideThumbPrint(final String appId, final String thumbPrint,
//			final TransactionAuxParams trAux) throws SQLException, IllegalAccessException,
//				CertificateException, NoSuchAlgorithmException, IOException {
//		if (!ApplicationsDAO.checkThumbPrint(appId, thumbPrint, trAux)) {
//    		throw new IllegalAccessException("El certificado utilizado no tiene permiso para acceder"); //$NON-NLS-1$
//    	}
//	}
}
