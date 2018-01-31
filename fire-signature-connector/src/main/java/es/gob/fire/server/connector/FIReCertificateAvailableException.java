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
 * Error producido al solicitar un certificado cuando ya hay otro disponible.
 */
public final class FIReCertificateAvailableException extends FIReCertificateException {

    /** Serial Id. */
	private static final long serialVersionUID = -3384174742441075646L;

	/**
     * Construye una excepci&oacute;n de error en la obtenci&oacute;n o uso de
     * los certificados del usuario debido a que ya existia otro certificado de
     * ese tipo.
     *
     * @param msg
     *            Mensaje de la excepci&oacute;n.
     * @param e
     *            Causa inicial de la excepci&oacute;n.
     */
    public FIReCertificateAvailableException(final String msg, final Throwable e) {
        super(msg, e);
    }

    /**
     * Construye una excepci&oacute;n de error en la obtenci&oacute;n o uso de
     * los certificados del usuario debido a que ya existia otro certificado de
     * ese tipo.
     *
     * @param msg
     *            Mensaje de la excepci&oacute;n.
     */
    public FIReCertificateAvailableException(final String msg) {
        super(msg);
    }

    /**
     * Construye una excepci&oacute;n de error en la obtenci&oacute;n o uso de
     * los certificados del usuario debido a que ya existia otro certificado de
     * ese tipo.
     * @param e
     *            Causa inicial de la excepci&oacute;n.
     */
    public FIReCertificateAvailableException(final Throwable e) {
        super(e);
    }

}
