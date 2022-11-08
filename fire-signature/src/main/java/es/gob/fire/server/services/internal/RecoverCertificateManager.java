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

import java.util.Properties;

import es.gob.fire.server.connector.FIReCertificateException;
import es.gob.fire.server.connector.FIReConnector;
import es.gob.fire.server.connector.FIReConnectorFactoryException;
import es.gob.fire.server.connector.FIReConnectorNetworkException;

/**
 * Manejador de la operaci&oacute;n de recuperaci&oacute;n de los certificados
 * reci&eacute;n generados. Esta clase atiende una peticion de recuperaci&oacute;n
 * de certificados recibida en servidor.
 * Las comprobaciones de acceso deber&aacute;n haberse realizado previamente.
 */
public class RecoverCertificateManager {

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
		final FIReConnector connector = ProviderManager.getProviderConnector(providerName, config);

		// Recuperamos el certificado
		return connector.recoverCertificate(transactionId);
	}
}
