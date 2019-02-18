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


/** El certificado del usuario est&aacute; bloqueado. */
public final class HttpCertificateBlockedException extends HttpOperationException {

    /** Serial Id. */
	private static final long serialVersionUID = 4353657884161773302L;

	HttpCertificateBlockedException() {
        super();
    }

    /** Indica que los certificados de un usuario est&aacute;n bloqueados.
	 * @param e Excepci&oacute;n que caus&oacute; el error. */
    public HttpCertificateBlockedException(final Throwable e) {
        super("Certitificado bloqueado", e); //$NON-NLS-1$
    }

    /** Indica que los certificados de un usuario est&aacute;n bloqueados.
	 * @param msg Mensaje de error.
	 * @param e Excepci&oacute;n que caus&oacute; el error. */
    public HttpCertificateBlockedException(final String msg, final Throwable e) {
        super(msg, e);
    }
}
