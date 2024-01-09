package es.gob.fire.server.services;

/** Formato de firma. */
public enum SignatureFormat {

    /** CAdES. */
    CADES("CAdES"), //$NON-NLS-1$

    /** CAdES-ASiC-S. */
    CADES_ASIC_S("CAdES-ASiC-S"), //$NON-NLS-1$

    /** XAdES. */
    XADES("XAdES"), //$NON-NLS-1$

    /** XAdES-ASiC-S. */
    XADES_ASIC_S("XAdES-ASiC-S"), //$NON-NLS-1$

    /** FacturaE. */
    FACTURAE("FacturaE"), //$NON-NLS-1$

    /** PAdES. */
    PADES("PAdES"), //$NON-NLS-1$

	/** Sin formato (PKCS#1). */
    NONE("NONE"); //$NON-NLS-1$

    private final String fmtName;

    SignatureFormat(final String fmt) {
        this.fmtName = fmt;
    }

    @Override
    public String toString() {
        return this.fmtName;
    }
}