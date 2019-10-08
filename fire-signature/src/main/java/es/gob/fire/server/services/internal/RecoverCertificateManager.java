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
import java.io.OutputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import es.gob.fire.server.connector.FIReCertificateException;
import es.gob.fire.server.connector.FIReConnector;
import es.gob.fire.server.connector.FIReConnectorFactoryException;
import es.gob.fire.server.connector.FIReConnectorNetworkException;
import es.gob.fire.server.services.ProviderLegacy;
import es.gob.fire.server.services.RequestParameters;
import es.gob.fire.server.services.ServiceUtil;

/**
 * Manejador de la operaci&oacute;n de recuperaci&oacute;n de los certificados
 * reci&eacute;n generados. Esta clase atiende una peticion de recuperaci&oacute;n
 * de certificados recibida en servidor.
 * Las comprobaciones de acceso deber&aacute;n haberse realizado previamente.
 */
public class RecoverCertificateManager {

	private static final Logger LOGGER = Logger.getLogger(RecoverCertificateManager.class.getName());

	/**
	 * Ejecuta una operacion de recuperaci&oacute;n del certificado generado
	 * en servidor. Este metodo s&oacute;lo se utiliza desde el servicio Legacy
	 * de Cl@ve Firma.
	 * @param params Par&aacute;metros extra&iacute;dos de la petici&oacute;n.
	 * @param response Respuesta HTTP de generaci&oacute;n de certificado.
	 * @throws IOException Cuando ocurre alg&uacute;n error en la comunicaci&oacute;n
	 * con el cliente HTTP.
	 */
	public static void recoverCertificate(
			final RequestParameters params,
            final HttpServletResponse response) throws IOException {

		final String appId = params.getParameter(ServiceParams.HTTP_PARAM_APPLICATION_ID);
        final String transactionId = params.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID);
        final String configB64  = params.getParameter(ServiceParams.HTTP_PARAM_CONFIG);

		final LogTransactionFormatter logF = new LogTransactionFormatter(appId, transactionId);

		// Comprobamos que se hayan proporcionado los parametros indispensables
        if (transactionId == null || transactionId.isEmpty()) {
        	LOGGER.warning(logF.f("No se ha proporcionado el ID de transaccion")); //$NON-NLS-1$
        	response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

		LOGGER.fine(logF.f("Peticion bien formada")); //$NON-NLS-1$

    	Properties config = null;
    	if (configB64 != null && configB64.length() > 0) {
    		config = ServiceUtil.base642Properties(configB64);
    	}

    	LOGGER.info(logF.f("Recuperamos el certificado de usuario")); //$NON-NLS-1$

    	byte[] newCertEncoded;
        try {
        	newCertEncoded = recoverCertificate(ProviderLegacy.PROVIDER_NAME_CLAVEFIRMA, transactionId, config);
        }
        catch (final FIReConnectorFactoryException e) {
        	LOGGER.log(Level.SEVERE, logF.f("Error en la configuracion del conector del proveedor de firma"), e); //$NON-NLS-1$
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        catch (final FIReConnectorNetworkException e) {
        	LOGGER.log(Level.SEVERE, logF.f("No se ha podido conectar con el sistema"), e); //$NON-NLS-1$
            response.sendError(HttpServletResponse.SC_REQUEST_TIMEOUT,
                    "No se ha podido conectar con el sistema: " + e); //$NON-NLS-1$
            return;
        }
        catch (final Exception e) {
            LOGGER.log(Level.WARNING, logF.f("Error en la generacion del certificado"), e); //$NON-NLS-1$
            response.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "No se ha podido obtener el certificado generado" //$NON-NLS-1$
            );
            return;
        }

        LOGGER.info(logF.f("Devolvemos el certificado generado")); //$NON-NLS-1$

        // El servicio devuelve el resultado de la operacion de firma.
        final OutputStream output = ((ServletResponse) response).getOutputStream();
		output.write(newCertEncoded);
        output.flush();
        output.close();
	}

	/**
	 * Ejecuta una operacion de recuperaci&oacute;n del certificado generado
	 * en servidor.
	 * @param providerName Nombre del proveedor en la nube.
	 * @param transactionId Id de la transaccion de generacion de certificado.
	 * @param config Configuraci&oacute;n del conector para la recuperacion del certificador y
	 * redirecci&oacute;n del usuario.
	 * @return Bytes que componen el certificado.
	 * @throws FIReCertificateException Si ocurre un error durante la generaci&oacute;n del certificado.
	 * @throws FIReConnectorNetworkException Cuando falla la comunicaci&oacute;n con el servicio.
	 * @throws FIReConnectorFactoryException Cuando la configuraci&oacute;n del conector del
	 * servicio de custodia no es v&aacute;lida.
	 */
	public static byte[] recoverCertificate(final String providerName, final String transactionId, final Properties config)
			throws	FIReCertificateException, FIReConnectorNetworkException,
					FIReConnectorFactoryException {

		// Obtenemos el conector con el backend ya configurado
		final FIReConnector connector = ProviderManager.initTransacction(providerName, config);

		// Recuperamos el certificado
		return connector.recoverCertificate(transactionId);
	}
}
