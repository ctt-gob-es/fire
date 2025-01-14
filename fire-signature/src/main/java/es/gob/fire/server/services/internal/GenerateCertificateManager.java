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
import java.util.Properties;

import es.gob.fire.server.connector.FIReCertificateAvailableException;
import es.gob.fire.server.connector.FIReCertificateException;
import es.gob.fire.server.connector.FIReConnector;
import es.gob.fire.server.connector.FIReConnectorFactoryException;
import es.gob.fire.server.connector.FIReConnectorNetworkException;
import es.gob.fire.server.connector.FIReConnectorUnknownUserException;
import es.gob.fire.server.connector.GenerateCertificateResult;
import es.gob.fire.server.connector.WeakRegistryException;

/**
 * Manejador de la operaci&oacute;n de generaci&oacute;n de certificados. Esta clase
 * atiende una peticion de generaci&oacute;n de certificados recibida en servidor.
 * Las comprobaciones de acceso deber&aacute;n haberse realizado previamente.
 */
public class GenerateCertificateManager {

	/**
	 * Ejecuta una operacion de generaci&oacute;n de certificado en servidor.
	 * @param providerName Nombre del proveedor de firma en la nube.
	 * @param subjectId Identificador del usuario.
	 * @param config Properties de configuraci&oacute;n del conector con el servicio de custodia.
     * @param logF Formateador de trazas de log.
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
	public static GenerateCertificateResult generateCertificate(final String providerName,
			final String subjectId, final Properties config, final LogTransactionFormatter logF)
					throws IOException, FIReConnectorFactoryException, FIReCertificateAvailableException,
					FIReCertificateException, FIReConnectorUnknownUserException, FIReConnectorNetworkException,
					WeakRegistryException {

    	// Obtenemos el conector con el backend ya configurado
    	final FIReConnector connector = ProviderManager.getProviderConnector(providerName, config, logF);

        return connector.generateCertificate(subjectId);
	}
}
