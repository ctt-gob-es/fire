/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.client;


/**
 * El lote no est&aacute; firmado.
 */
public final class BatchNoSignedException extends HttpOperationException {


    /** Serial Id. */
	private static final long serialVersionUID = 459647017984206993L;

	/**
     * El lote no esta firmado.
     * @param code C&oacute;digo de error.
     * @param msg Mensaje de error.
     */
	public BatchNoSignedException(final int code, final String msg) {
		super(code, msg);
	}

    /**
     * El lote no est&aacute; firmado.
     * @param e Motivo del error.
     */
    public BatchNoSignedException(final Throwable e) {
        super("El lote no esta firmado", e); //$NON-NLS-1$
    }

    /**
     * El lote no est&aacute; firmado.
     * @param msg Mensaje de error.
     * @param e Motivo del error.
     */
    public BatchNoSignedException(final String msg, final Throwable e) {
        super(msg, e);
    }
}
