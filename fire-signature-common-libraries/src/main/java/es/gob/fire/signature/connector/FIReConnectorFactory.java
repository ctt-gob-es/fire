/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.signature.connector;

import java.util.Properties;
import java.util.logging.Logger;

import es.gob.fire.signature.ConfigManager;

/** Factor&iacute;a que genera diferentes proveedores de certificados y
 * operaciones de firma PKCS#1. */
public final class FIReConnectorFactory {


	private static final String SERVICE_DEFAULT = "es.gob.fire.signature.connector.clavefirma.ClaveFirmaConnector"; //$NON-NLS-1$
	private static final String SERVICE_CLASSNAME = ConfigManager.getBackEndService(SERVICE_DEFAULT);

	private static final Logger LOGGER = Logger.getLogger(FIReConnectorFactory.class.getName());

    /** Constructor privado para no permir la instanciaci&oacute;n. */
    private FIReConnectorFactory() {
        // no instanciable
    }

    /** Devuelve la implementaci&oacute;n por defecto de FIReConnector.
     * @param config Configuraci&oacute;n a aplicar al <code>FIReConnector</code>
     *               devuelto.
     * @return Implementaci&oacute;n de FIReConnector.
     * @throws FIReConnectorFactoryException Si se ha producido alg&uacute;n problema en la creaci&oacute;
     *                                     del FIReConnector. */
    public static FIReConnector getClaveFirmaConnector(final Properties config) throws FIReConnectorFactoryException {
        final FIReConnector ret;
        try {
        	ret = (FIReConnector) Class.forName(SERVICE_CLASSNAME).newInstance();
        	ret.init(config);
        }
        catch (final Exception e) {
            throw new FIReConnectorFactoryException("No se ha podido cargar el conector definido", e); //$NON-NLS-1$
        }
        LOGGER.info("Se usara el servicio " + SERVICE_CLASSNAME); //$NON-NLS-1$
        return ret;
    }

}
