/* Copyright (C) 2024 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 10/12/2024
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.client;


/**
 * El contenido de la petici&oacute;n era demasiado grande.
 */
public final class HttpTooLargeContentException extends HttpOperationException {

    /** Serial Id. */
	private static final long serialVersionUID = -93161736495744712L;

	/** Mensaje de error por defecto. */
	private static final String DEFAUL_ERROR_MSG = "El contenido de la peticion era demasiado grande"; //$NON-NLS-1$

	/**
     * Se rechaza la petici&oacute;n porque era demasiado grande.
     * @param code C&oacute;digo de error.
     * @param msg Mensaje de error.
     */
    public HttpTooLargeContentException(final int code, final String msg) {
        super(code, msg);
    }

    /**
     * Se rechaza la petici&oacute;n porque era demasiado grande.
     * @param code C&oacute;digo de error.
     */
    public HttpTooLargeContentException(final int code) {
        super(code, DEFAUL_ERROR_MSG);
    }

	/**
     * Se rechaza la petici&oacute;n porque era demasiado grande.
     * @param msg Mensaje de error.
     */
    public HttpTooLargeContentException(final String msg) {
        super(msg);
    }

    /**
     * Se rechaza la petici&oacute;n porque era demasiado grande.
     * @param e Motivo del error.
     */
    public HttpTooLargeContentException(final Throwable e) {
        super(DEFAUL_ERROR_MSG, e);
    }

    /**
     * Se rechaza la petici&oacute;n porque era demasiado grande.
     * @param msg Mensaje de error.
     * @param e Motivo del error.
     */
    public HttpTooLargeContentException(final String msg, final Throwable e) {
        super(msg, e);
    }
}
