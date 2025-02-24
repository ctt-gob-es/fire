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
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;
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

	private static final String UNIDENTIFIED = "NO_IDENTIFICADA"; //$NON-NLS-1$

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

			appInfo = new ApplicationInfo(appId, registeredAppInfo != null ? registeredAppInfo.getName() : appId);

    		// Comprobamos que la aplicacion este registrada en el sistema y habilitada
    		if (ConfigManager.isCheckApplicationNeeded()) {
    			LOGGER.fine(trAux.getLogFormatter().f("Se realizara la validacion del Id de aplicacion")); //$NON-NLS-1$
    			try {
    				ApplicationAccessChecker.checkAppEnabled(registeredAppInfo);
    			}
    			catch (final IllegalAccessException e) {
    				throw new UnauthorizedApplicacionException("Se deniega el acceso a la aplicacion " + appId, e); //$NON-NLS-1$
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
    			
    			if (isExpired(certificates[0])) {
    				throw new UnauthorizedApplicacionException("El certificado esta caducado"); //$NON-NLS-1$
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

    	if (appInfo == null) {
    		if (appId != null) {
        		appInfo = new ApplicationInfo(appId, appId);
        	} else {
        		appInfo = new ApplicationInfo(UNIDENTIFIED, UNIDENTIFIED);
        	}
    	}

		return appInfo;
	}
	
	/**
	 * Indica si un certificado est&aacute; fuera de su periodo de vigencia.
	 * @param cert Certificado a comprobar.
	 * @return {@code true} si el certificado est&aacute;caducado o a&uacute;n no es v&aacute;lido,
	 * {@code false} en caso contrario.
	 */
	private static boolean isExpired(final X509Certificate cert) {
		final long currentDate = new Date().getTime();
		return currentDate >= cert.getNotAfter().getTime() || currentDate <= cert.getNotBefore().getTime();
	}
}
