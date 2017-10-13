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

/**
 * Excepci&oacute;n indicando que el usuario indicado no
 * est&aacute; dado de alta en el sistema.
 *
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s.
 */
public final class FIReConnectorUnknownUserException extends FIReConnectorException {

    private static final long serialVersionUID = -1075653592617420439L;

    /**
     * Crea una excepci&oacute;n indicando que el usuario
     * indicado no est&aacute;a dado de alta en el sistema.
     *
     * @param msg
     * 			  Descripci&oacute;n del error.
     * @param e
     *            Excepci&oacute;n de origen.
     */
    public FIReConnectorUnknownUserException(final String msg, final Throwable e) {
        super(msg, e);
    }

    /** Crea una excepci&oacute;n indicando que el usuario
     * indicado no est&aacute;a dado de alta en el sistema.
     * @param msg
     * 			  Descripci&oacute;n del error. */
	public FIReConnectorUnknownUserException(final String msg) {
		super(msg);
	}

    /**
     * Crea una excepci&oacute;n indicando que el usuario
     * indicado no est&aacute;a dado de alta en el sistema.
     *
     * @param e
     *          Excepci&oacute;n de origen.
     */
    public FIReConnectorUnknownUserException(final Throwable e) {
        super(e);
    }
}
