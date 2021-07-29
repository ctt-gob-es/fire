package es.gob.clavefirma.test.services;

/**
 * Clase con los identificadores de los distintos parametros que se utilizan en la
 * ejecuci&oacute;n de las operaciones de Clave Firma.
 */
public class TestServiceParams {

	/** Par&aacute;metro usado en el env&iacute;o de datos HTTP con el ID del usuario. */
	public static final String HTTP_PARAM_SUBJECT_REF = "subjectref"; //$NON-NLS-1$

    /** Par&aacute;metro usado en el env&iacute;o de datos HTTP con el origen del certificado. */
    public static final String HTTP_PARAM_CERT_ORIGIN = "certorigin"; //$NON-NLS-1$

    /** Par&aacute;metro usado en el env&iacute;o de datos HTTP que indica si se configur&oacute; el origen del certificado desde la aplicaci&oacute;n cliente. */
    public static final String HTTP_PARAM_CERT_ORIGIN_FORCED = "originforced"; //$NON-NLS-1$

}
