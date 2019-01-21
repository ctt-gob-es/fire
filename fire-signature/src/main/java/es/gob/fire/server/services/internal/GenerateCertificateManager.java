/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.services.internal;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import es.gob.fire.server.connector.FIReCertificateAvailableException;
import es.gob.fire.server.connector.FIReCertificateException;
import es.gob.fire.server.connector.FIReConnector;
import es.gob.fire.server.connector.FIReConnectorFactoryException;
import es.gob.fire.server.connector.FIReConnectorNetworkException;
import es.gob.fire.server.connector.FIReConnectorUnknownUserException;
import es.gob.fire.server.connector.GenerateCertificateResult;
import es.gob.fire.server.connector.WeakRegistryException;
import es.gob.fire.server.services.HttpCustomErrors;
import es.gob.fire.server.services.RequestParameters;
import es.gob.fire.server.services.ServiceUtil;

/**
 * Manejador de la operaci&oacute;n de generaci&oacute;n de certificados. Esta clase
 * atiende una peticion de generaci&oacute;n de certificados recibida en servidor.
 * Las comprobaciones de acceso deber&aacute;n haberse realizado previamente.
 */
public class GenerateCertificateManager {

	private static final Logger LOGGER = Logger.getLogger(GenerateCertificateManager.class.getName());

	/**
	 * Ejecuta una operacion de generaci&oacute;n de certificado en servidor.
	 * @param params Par&aacute;metros extra&iacute;dos de la petici&oacute;n.
	 * @param response Respuesta HTTP de generaci&oacute;n de certificado.
	 * @throws IOException Cuando ocurre alg&uacute;n error en la comunicaci&oacute;n
	 * con el cliente HTTP.
	 */
	public static void generateCertificate(
			final RequestParameters params,
            final HttpServletResponse response) throws IOException {

		final String appId 			= params.getParameter(ServiceParams.HTTP_PARAM_APPLICATION_ID);
        final String transactionId	= params.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID);
        final String subjectId      = params.getParameter(ServiceParams.HTTP_PARAM_SUBJECT_ID);
		final String providerName	= params.getParameter(ServiceParams.HTTP_PARAM_CERT_ORIGIN);
		final String configB64      = params.getParameter(ServiceParams.HTTP_PARAM_CONFIG);

		final LogTransactionFormatter logF = new LogTransactionFormatter(appId, transactionId);

		// Comprobamos del usuario
    	if (subjectId == null || subjectId.isEmpty()) {
        	LOGGER.warning(logF.format("No se ha proporcionado el identificador del usuario que solicita el certificado")); //$NON-NLS-1$
        	response.sendError(HttpServletResponse.SC_BAD_REQUEST,
        			"No se ha proporcionado el identificador del usuario que solicita el certificado" //$NON-NLS-1$
        			);
        	return;
        }

		Properties config = null;
    	if (configB64 != null && configB64.length() > 0) {
    		config = ServiceUtil.base642Properties(configB64);
    	}

        final GenerateCertificateResult gcr;
        try {
        	gcr = generateCertificate(providerName, subjectId, config);
        }
        catch (final FIReConnectorFactoryException e) {
        	LOGGER.log(Level.SEVERE, logF.format("Error en la configuracion del conector del proveedor de firma"), e); //$NON-NLS-1$
        	response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
        			"Error en la configuracion del conector con el servicio de custodia: " + e //$NON-NLS-1$
        			);
        	return;
        }
        catch (final FIReConnectorNetworkException e) {
        	LOGGER.log(Level.SEVERE, logF.format("No se ha podido conectar con el sistema"), e); //$NON-NLS-1$
        	response.sendError(HttpServletResponse.SC_REQUEST_TIMEOUT,
        			"No se ha podido conectar con el sistema: " + e); //$NON-NLS-1$
        	return;
        }
        catch (final WeakRegistryException e) {
        	LOGGER.log(Level.WARNING, logF.format("El usuario realizo un registro debil y no puede tener certificados de firma"), e); //$NON-NLS-1$
        	response.sendError(HttpCustomErrors.WEAK_REGISTRY.getErrorCode(),
        			"El usuario realizo un registro debil y no puede tener certificados de firma: " + e //$NON-NLS-1$
    				);
    		return;
        }
        catch (final FIReCertificateException e) {
        	LOGGER.log(Level.SEVERE, logF.format("Error en la generacion del certificado"), e); //$NON-NLS-1$
        	if (e instanceof FIReCertificateAvailableException) {
        		response.sendError(HttpCustomErrors.CERTIFICATE_AVAILABLE.getErrorCode(),
        				"El usuario ya tiene un certificado del tipo indicado: " + e //$NON-NLS-1$
        				);
        		return;
        	}
        	response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
        			"Error en la generacion del certificado: " + e //$NON-NLS-1$
        			);
        	return;
        }
        catch (final Exception e) {
        	LOGGER.log(Level.SEVERE, logF.format("Error desconocido en la generacion del certificado"), e);//$NON-NLS-1$
        	response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
        			"Error desconocido en la generacion del certificado: " + e //$NON-NLS-1$
        			);
        	return;
        }

        response.setContentType("application/json"); //$NON-NLS-1$
        final PrintWriter out = response.getWriter();
        out.print(gcr.toString());
        out.flush();
        out.close();
	}

	/**
	 * Ejecuta una operacion de generaci&oacute;n de certificado en servidor.
	 * @param providerName Nombre del proveedor de firma en la nube.
	 * @param subjectId Identificador del usuario.
	 * @param config Properties de configuraci&oacute;n del conector con el servicio de custodia.
	 * @return Resultado de la operaci&oacute;n, compuesto por el identificador de la
	 * transacci&oacute;n y la URL a la que redirigir al usuario.
	 * @throws IOException Cuando no se puede leer el par&aacute;metro de configuraci&oacute;n.
	 * con el cliente HTTP.
	 * @throws FIReConnectorFactoryException Cuando no se puede crear el conector con el
	 * sistema de firma en la nube.
	 * @throws FIReConnectorUnknownUserException Cuando el ID del usuario no sea v&aacute;lido.
	 * @throws FIReConnectorNetworkException Cuando ocurre un problema de red.
	 * @throws FIReCertificateException Cuando el certificado no sea v&aacute;lido.
	 * @throws FIReCertificateAvailableException Cuando ya exista un certificado v&aacute;lido
	 * @throws WeakRegistryException Cuando el usuario realiz&oacute; un registro d&eacute;bil.
	 * y no se pueda crear otro.
	 */
	public static GenerateCertificateResult generateCertificate(final String providerName, final String subjectId, final Properties config) throws IOException, FIReConnectorFactoryException, FIReCertificateAvailableException, FIReCertificateException, FIReConnectorNetworkException, FIReConnectorUnknownUserException, WeakRegistryException {

    	// Obtenemos el conector con el backend ya configurado
    	final FIReConnector connector = ProviderManager.initTransacction(providerName, config);

        return connector.generateCertificate(subjectId);
	}
}
