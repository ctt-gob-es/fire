/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.upgrade;

/**
 * Formato de firma mejorada.
 *
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s.
 */
public enum UpgradeTarget {

    /** ES-T. */
    T_FORMAT("ES-T", "urn:oasis:names:tc:dss:1.0:profiles:AdES:forms:ES-T", "T-Level", "urn:afirma:dss:1.0:profile:XSS:AdES:forms:T-Level"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    /** ES-C. */
    C_FORMAT("ES-C", "urn:oasis:names:tc:dss:1.0:profiles:AdES:forms:ES-C"), //$NON-NLS-1$ //$NON-NLS-2$
    /** ES-X. */
    X_FORMAT("ES-X", "urn:oasis:names:tc:dss:1.0:profiles:AdES:forms:ES-X"), //$NON-NLS-1$ //$NON-NLS-2$
    /** ES-X-1. */
    X_1_FORMAT("ES-X-1", "urn:oasis:names:tc:dss:1.0:profiles:AdES:forms:ES-X-1"), //$NON-NLS-1$ //$NON-NLS-2$
    /** ES-X-2. */
    X_2_FORMAT("ES-X-2", "urn:oasis:names:tc:dss:1.0:profiles:AdES:forms:ES-X-2"), //$NON-NLS-1$ //$NON-NLS-2$
    /** ES-X-L. */
    X_L_FORMAT("ES-X-L", "urn:oasis:names:tc:dss:1.0:profiles:AdES:forms:ES-X-L"), //$NON-NLS-1$ //$NON-NLS-2$
    /** ES-X-L-1. */
    X_L_1_FORMAT("ES-X-L-1", "urn:oasis:names:tc:dss:1.0:profiles:AdES:forms:ES-X-L-1"), //$NON-NLS-1$ //$NON-NLS-2$
    /** ES-X-L-2. */
    X_L_2_FORMAT("ES-X-L-2", "urn:oasis:names:tc:dss:1.0:profiles:AdES:forms:ES-X-L-2"), //$NON-NLS-1$ //$NON-NLS-2$
    /** ES-A. */
    A_FORMAT("ES-A", "urn:oasis:names:tc:dss:1.0:profiles:AdES:forms:ES-A"), //$NON-NLS-1$ //$NON-NLS-2$
    /** ES-LTV. */
    PADES_LTV_FORMAT("ES-LTV", "urn:afirma:dss:1.0:profile:xss:PAdES:1.1.2:forms:LTV"), //$NON-NLS-1$ //$NON-NLS-2$
    /** T-Level. */
    T_LEVEL_FORMAT("T-Level", "urn:afirma:dss:1.0:profile:XSS:AdES:forms:T-Level", "ES-T", "urn:oasis:names:tc:dss:1.0:profiles:AdES:forms:ES-T"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    /** LT-Level. */
    LT_LEVEL_FORMAT("LT-Level", "urn:afirma:dss:1.0:profile:XSS:AdES:forms:LT-Level"), //$NON-NLS-1$ //$NON-NLS-2$
    /** LTA-Level. */
    LTA_LEVEL_FORMAT("LTA-Level", "urn:afirma:dss:1.0:profile:XSS:AdES:forms:LTA-Level"); //$NON-NLS-1$ //$NON-NLS-2$

	private final String str;

	private final String formatUrn;

    private final String[] alternatives;

    /**
     * Construye el objetivo del formato. La primera de las alternativas definidas, sera la URN de definici&oacute;n
     * del formato.
     * @param s Cadena de denominaci&oacute;n del formato de actualizaci&oacute;n.
     * @param formatUrn URN de definici&oacute;n del formato .
     * @param alternatives Cadenas alternativas para denominar al formato.
     */
    private UpgradeTarget(final String s, final String formatUrn, final String... alternatives) {
        this.str = s;
        this.formatUrn = formatUrn;
        this.alternatives = alternatives;
    }

    /**
     * Devuelve la URN de definici&oacute;n del formato.
     * @return URN del formato.
     */
    public String getFormatUrn() {
    	return this.formatUrn;
    }

    @Override
    public String toString() {
        return this.str;
    }

    /**
     * Obtiene el tipo de mejora a partir de su nombre.
     * @param n Nombre del tipo de mejora.
     * @return Tipo de mejora de firma.
     * @throws IllegalArgumentException Si el tipo de mejora no esta soportada.
     */
    public static UpgradeTarget getUpgradeTarget(final String n) {
        if (n == null) {
            throw new IllegalArgumentException(
                "El nombre de la mejora no puede ser nulo" //$NON-NLS-1$
            );
        }
        final String name = n.trim().toUpperCase();
        for (final UpgradeTarget target : values()) {
        	if (compareFormatText(target, name)) {
            	return target;
            }
        }

        throw new IllegalArgumentException("Tipo de mejora no soportada: " + name); //$NON-NLS-1$
    }

    private static boolean compareFormatText(final UpgradeTarget format, final String target) {
    	return format.toString().equalsIgnoreCase(target) ||
    			target.toUpperCase().endsWith(":" + format.toString().toUpperCase()); //$NON-NLS-1$
    }

    /**
     * Comprueba si este formato objetivo para actualizaci&oacute;n es igual o equivale a otro.
     * @param target Formato objetivo.
     * @return {@code true} si los formatos son iguales o equivalentes.
     */
    public boolean equivalent(final Object target) {

    	if (target == null) {
    		return false;
    	}

    	final String upgradeText = target.toString();
    	if (this.str.equalsIgnoreCase(upgradeText)) {
    		return true;
    	}

    	if (this.formatUrn.equalsIgnoreCase(upgradeText)) {
    		return true;
    	}

    	if (this.alternatives != null) {
    		for (final String alt : this.alternatives) {
    			if (alt.equalsIgnoreCase(upgradeText)) {
    				return true;
    			}
    		}
    	}

    	return false;
    }
}
