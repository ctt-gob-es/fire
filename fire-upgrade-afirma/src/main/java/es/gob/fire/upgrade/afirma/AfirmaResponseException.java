/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.upgrade.afirma;

/**
 * Error provocado por una respuesta no positiva a una petici&oacute;n de mejora
 * de firma.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s.
 */
public final class AfirmaResponseException extends Exception {

    private static final long serialVersionUID = -5987647422142289573L;

    private final String majorCode;
    private final String minorCode;

    AfirmaResponseException(final String major, final String minor,
            final String desc) {
        super(desc);
        this.majorCode = major;
        this.minorCode = minor;
    }

    /**
     * Obtiene el c&oacute;digo principal de resultado.
     *
     * @return C&oacute;digo principal de resultado.
     */
    public String getMajorCode() {
        return this.majorCode;
    }

    /**
     * Obtiene el c&oacute;digo secundario de resultado.
     *
     * @return C&oacute;digo secundario de resultado.
     */
    public String getMinorCode() {
        return this.minorCode;
    }

    @Override
    public String toString() {
    	return super.toString() + ". MajorCode: " + this.majorCode + ". MinorCode: " + this.minorCode; //$NON-NLS-1$ //$NON-NLS-2$
    }
}
