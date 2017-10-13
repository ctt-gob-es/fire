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

import java.util.logging.Level;
import java.util.logging.Logger;

import es.gob.fire.signature.connector.FIReConnector;
import es.gob.fire.signature.connector.FIReConnectorFactory;
import es.gob.fire.signature.connector.FIReConnectorFactoryException;
import es.gob.fire.signature.connector.FIReConnectorUnknownUserException;

/**
 * Clase con m&eacute;todos de utilidad para la gesti&oacute;n de las peticiones al
 * servicio de custodia de certificados.
 */
class FIReHelper {

	private static final Logger LOGGER = Logger.getLogger(FIReHelper.class.getName());

	/**
	 * Conecta con el servicio de custodia de certificados en la nube para comprobar si
	 * el usuario est&aacute; dado de alta en &eacute;l.
	 * @param subjectId Identificador del usuario del que se desee comprobar el alta.
	 * @return {@code true} si el usuario est&aacute; dado de alta en el servicio
	 * de custodia de certificados en la nube, {@code false} en caso contrario.
	 */
	public static boolean isUserRegistered(final String subjectId) {

        final FIReConnector connector;
        try {
            connector = FIReConnectorFactory.getClaveFirmaConnector(null);
        }
		catch(final FIReConnectorFactoryException e) {
			LOGGER.log(Level.SEVERE, "Error en la configuracion del conector del servicio de custodia", e); //$NON-NLS-1$
            return false;
		}

        try {
           connector.getCertificates(subjectId);
        }
        catch (final FIReConnectorUnknownUserException e) {
        	return false;
        }
        catch (final Exception e) {
        	// No tratamos errores
        }

		return true;
	}
}
