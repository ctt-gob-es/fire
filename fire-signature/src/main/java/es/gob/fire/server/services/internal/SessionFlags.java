package es.gob.fire.server.services.internal;

/** Estado de un tr&aacute;mite de la sesi&oacute;n. */
public enum SessionFlags {

	/** Indicador de que la &uacute;ltima operaci&oacute;n realizada iniciaba una firma. */
	OP_SIGN,

	/** Indicador de que la &uacute;ltima operaci&oacute;n realizada gener&oacute; un certificado. */
	OP_PRE,

	/** Indicador de que la &uacute;ltima operaci&oacute;n realizada gener&oacute; un certificado. */
	OP_GEN,

	/** Indicador de que la &uacute;ltima operaci&oacute;n realizada recuperaba el resultado de firma. */
	OP_RECOVER;
}
