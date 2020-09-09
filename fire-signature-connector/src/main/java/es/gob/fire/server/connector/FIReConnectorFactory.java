/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.connector;

/** Factor&iacute;a para la obtenci&oacute;n de las clases conectoras de los distintos
 * proveedores de firma en la nube. */
public final class FIReConnectorFactory {

    /** Constructor privado para no permir la instanciaci&oacute;n. */
    private FIReConnectorFactory() {
        // no instanciable
    }

    /** Devuelve la clase conectora.
     * @param connectorClass Nombre de la clase conectora.
     * @return Implementaci&oacute;n de FIReConnector.
     * @throws FIReConnectorFactoryException Si se ha producido alg&uacute;n problema en la creaci&oacute;
     *                                     del conector. */
    public static FIReConnector getConnector(final String connectorClass) throws FIReConnectorFactoryException {
        final FIReConnector ret;
        try {
        	ret = (FIReConnector) Class.forName(connectorClass).newInstance();
        }
        catch (final Exception e) {
            throw new FIReConnectorFactoryException("No se ha podido cargar el conector definido", e); //$NON-NLS-1$
        }
        return ret;
    }

}
