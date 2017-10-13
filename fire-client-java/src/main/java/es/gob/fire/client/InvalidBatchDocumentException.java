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
 * La firma del documento no se gener&oacute; correctamente o se indic&oacute;
 * un documento no v&aacute;lido.
 */
public final class InvalidBatchDocumentException extends HttpOperationException {

	/** Serial Id. */
	private static final long serialVersionUID = -2000803668147485604L;

	InvalidBatchDocumentException() {
        super();
    }

    /**
     * La firma del documento no se gener&oacute; correctamente o se indic&oacute;
     * un documento no v&aacute;lido.
     * @param e Motivo del error.
     */
    public InvalidBatchDocumentException(final Throwable e) {
        super("No hay firma del documento indicado", e); //$NON-NLS-1$
    }

    /**
     * La firma del documento no se gener&oacute; correctamente o se indic&oacute;
     * un documento no v&aacute;lido.
     * @param msg Mensaje de error.
     * @param e Motivo del error.
     */
    public InvalidBatchDocumentException(final String msg, final Throwable e) {
        super(msg, e);
    }
}
