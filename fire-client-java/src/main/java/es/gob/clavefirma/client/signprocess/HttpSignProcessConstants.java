/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.clavefirma.client.signprocess;

/** Constantes para el proceso de firma en la nube.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
public final class HttpSignProcessConstants {

    private HttpSignProcessConstants() {
        // No instanciable
    }

    /** Algoritmno de firma. */
    public enum SignatureAlgorithm {

        /** SHA1withRSA. */
        SHA1WITHRSA("SHA1withRSA"), //$NON-NLS-1$

        /** SHA512withRSA. */
        SHA512WITHRSA("SHA512withRSA"), //$NON-NLS-1$

        /** SHA384withRSA. */
        SHA384WITHRSA("SHA384withRSA"), //$NON-NLS-1$

        /** SHA256withRSA. */
        SHA256WITHRSA("SHA256withRSA"), //$NON-NLS-1$

    	/** SHA512withECDSA. */
        SHA512withECDSA("SHA512withECDSA"), //$NON-NLS-1$

    	/** SHA384withECDSA. */
        SHA384withECDSA("SHA384withECDSA"), //$NON-NLS-1$

    	/** SHA256withECDSA. */
        SHA256withECDSA("SHA256withECDSA"), //$NON-NLS-1$

    	/** NONEwithECDSA. */
    	NONEwithECDSA("NONEwithECDSA"); //$NON-NLS-1$

        private final String algName;

        private SignatureAlgorithm(final String name) {
            this.algName = name;
        }

        @Override
        public String toString() {
            return this.algName;
        }

        /** Obtiene una algoritmo de firma a partir de su nombre.
         * @param name Nombre del algoritmo de firma.
         * @return Algoritmo de firma. */
        public static SignatureAlgorithm getSignatureAlgorithm(final String name) {
        	if (name == null) {
        		throw new IllegalArgumentException(
    				"El nombre del algoritmo de firma no puede ser nulo" //$NON-NLS-1$
				);
        	}
        	switch(name.toLowerCase().trim()) {
	        	case "sha1withrsa": //$NON-NLS-1$
	        		return SHA1WITHRSA;
	        	case "sha512withrsa": //$NON-NLS-1$
	        		return SHA512WITHRSA;
	        	case "sha384withrsa": //$NON-NLS-1$
	        		return SHA384WITHRSA;
	        	case "sha256withrsa": //$NON-NLS-1$
	        		return SHA256WITHRSA;
	        	case "sha512withecdsa": //$NON-NLS-1$
	        		return SHA384withECDSA;
	        	case "sha384withecdsa": //$NON-NLS-1$
	        		return SHA512withECDSA;
	        	case "sha256withecdsa": //$NON-NLS-1$
	        		return SHA256withECDSA;
	        	case "nonewithecdsa": //$NON-NLS-1$
	        		return NONEwithECDSA;
	        	default:
	        		throw new IllegalArgumentException(
        				"El nombre del algoritmo de firma no es adecuado: " + name //$NON-NLS-1$
    				);
        	}
        }
    }

    /** Operaci&oacute;n de firma. */
    public enum SignatureOperation {

        /** Firma. */
        SIGN("SIGN"), //$NON-NLS-1$

        /** Cofirma. */
        COSIGN("COSIGN"), //$NON-NLS-1$

        /** Contrafirma. */
        COUNTERSIGN("COUNTERSIGN"); //$NON-NLS-1$

        private final String opName;

        private SignatureOperation(final String op) {
            this.opName = op;
        }

        @Override
        public String toString() {
            return this.opName;
        }

        /** Obtiene una operaci&oacute;n de firma a partir de su nombre.
         * @param name Nombre del operaci&oacute;n de firma.
         * @return Operaci&oacute;n de firma. */
        public static SignatureOperation getSignatureOperation(final String name) {
        	if (name == null) {
        		throw new IllegalArgumentException(
    				"El nombre de la operacion de firma no puede ser nulo" //$NON-NLS-1$
				);
        	}
        	switch(name.toLowerCase().trim()) {
	        	case "sign": //$NON-NLS-1$
	        		return SIGN;
	        	case "cosign": //$NON-NLS-1$
	        		return COSIGN;
	        	case "countersign": //$NON-NLS-1$
	        		return COUNTERSIGN;
	        	default:
	        		throw new IllegalArgumentException(
        				"El nombre de la operacion de firma no es adecuado: " + name //$NON-NLS-1$
    				);
        	}
        }
    }

    /** Formato de firma. */
    public enum SignatureFormat {

        /** CAdES. */
        CADES("CAdES"), //$NON-NLS-1$

        /** CAdES-ASiC-S. */
        CADES_ASIC_S("CAdES-ASiC-S-tri"), //$NON-NLS-1$

        /** XAdES. */
        XADES("XAdES"), //$NON-NLS-1$

        /** XAdES-ASiC-S. */
        XADES_ASIC_S("CAdES-ASiC-S-tri"), //$NON-NLS-1$

        /** FacturaE. */
        FACTURAE("FacturaE"), //$NON-NLS-1$

        /** PAdES. */
        PADES("PAdES"), //$NON-NLS-1$

    	/** PKCS1. */
    	NONE("NONE"); //$NON-NLS-1$

        private final String fmtName;

        private SignatureFormat(final String fmt) {
            this.fmtName = fmt;
        }

        @Override
        public String toString() {
            return this.fmtName;
        }

        /** Obtiene un formato de firma a partir de su nombre.
         * @param name Nombre del formato de firma.
         * @return Formato de firma. */
        public static SignatureFormat getSignatureFormat(final String name) {
        	if (name == null) {
        		throw new IllegalArgumentException(
    				"El nombre del formato de firma no puede ser nulo" //$NON-NLS-1$
				);
        	}
        	switch(name.toLowerCase().trim()) {
	        	case "cades": //$NON-NLS-1$
	        		return CADES;
	        	case "cades-asic-s-tri": //$NON-NLS-1$
	        		return CADES_ASIC_S;
	        	case "xades": //$NON-NLS-1$
	        		return XADES;
	        	case "xades-asic-s-tri": //$NON-NLS-1$
	        		return XADES_ASIC_S;
	        	case "facturae": //$NON-NLS-1$
	        		return FACTURAE;
	        	case "pades": //$NON-NLS-1$
	        		return PADES;
	        	case "none": //$NON-NLS-1$
	        		return NONE;
	        	default:
	        		throw new IllegalArgumentException(
        				"El nombre del formato de firma no es adecuado: " + name //$NON-NLS-1$
    				);
        	}
        }
    }

    /** Tipo de mejora de firma. */
    public enum SignatureUpgrade {

        /** AdES-T. */
        T_FORMAT("ES-T"), //$NON-NLS-1$

        /** AdES-C. */
        C_FORMAT("ES-C"), //$NON-NLS-1$

        /** AdES-X. */
        X_FORMAT("ES-X"), //$NON-NLS-1$

        /** AdES-X1. */
        X_1_FORMAT("ES-X-1"), //$NON-NLS-1$

        /** AdES-X2. */
        X_2_FORMAT("ES-X-2"), //$NON-NLS-1$

        /** AdES-XL. */
        X_L_FORMAT("ES-X-L"), //$NON-NLS-1$

        /** AdES-L1. */
        X_L_1_FORMAT("ES-X-L-1"), //$NON-NLS-1$

        /** AdES-L2. */
        X_L_2_FORMAT("ES-X-L-2"), //$NON-NLS-1$

        /** AdES-A. */
        A_FORMAT("ES-A"), //$NON-NLS-1$

        /** PAdES-LTV. */
        PADES_LTV_FORMAT("ES-LTV"); //$NON-NLS-1$

        private final String str;

        @Override
        public String toString() {
            return this.str;
        }

        private SignatureUpgrade(final String s) {
            this.str = s;
        }
    }

}
