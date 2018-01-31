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

/**
 * Excepci&oacute;n indicando que no hay conectividad de red.
 *
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s.
 */
public final class FIReConnectorNetworkException extends FIReConnectorException {

    private static final long serialVersionUID = -1075653592617420439L;

    /**
     * Crea una excepci&oacute;n indicando que hay un
     * problema de red.
     * @param msg
     * 			  Mensaje descriptivo del error.
     * @param e
     *            Excepci&oacute;n de origen.
     */
    public FIReConnectorNetworkException(final String msg, final Throwable e) {
        super(msg, e);
    }

    /** Crea una excepci&oacute;n indicando que hay un
     * problema de red.
     * @param msg
     * 			  Mensaje descriptivo del error.
     */
	public FIReConnectorNetworkException(final String msg) {
		super(msg);
	}


    /**
     * Crea una excepci&oacute;n indicando que hay un
     * problema de red.
     * @param e
     *            Excepci&oacute;n de origen.
     */
    public FIReConnectorNetworkException(final Throwable e) {
        super(e);
    }
}
