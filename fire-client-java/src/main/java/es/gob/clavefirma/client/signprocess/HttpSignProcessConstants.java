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

/**
 * Constantes para el proceso de firma en la nube-
 *
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s.
 */
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
        SHA384WITHRSA("SHA284withRSA"), //$NON-NLS-1$

        /** SHA256withRSA. */
        SHA256WITHRSA("SHA256withRSA"), //$NON-NLS-1$

        /** SHA1. */
        SHA1("SHA1"), //$NON-NLS-1$

        /** SHA512. */
        SHA512("SHA512"), //$NON-NLS-1$

        /** SHA384. */
        SHA384("SHA284"), //$NON-NLS-1$

        /** SHA256. */
        SHA256("SHA256"); //$NON-NLS-1$

        private final String algName;

        SignatureAlgorithm(final String name) {
            this.algName = name;
        }

        @Override
        public String toString() {
            return this.algName;
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

        SignatureOperation(final String op) {
            this.opName = op;
        }

        @Override
        public String toString() {
            return this.opName;
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
        PADES("PAdES"); //$NON-NLS-1$

        private final String fmtName;

        SignatureFormat(final String fmt) {
            this.fmtName = fmt;
        }

        @Override
        public String toString() {
            return this.fmtName;
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

        SignatureUpgrade(final String s) {
            this.str = s;
        }
    }

}
