package es.gob.fire.server.admin.message;

public enum UserMessages {

	ERR_LOGIN_ACCESS(1, "Error de acceso, el login o la contrase&ntilde;a no son correctos"), //$NON-NLS-1$
	ERR_SEND_MAIL(2, "No se ha podido realizar el env&iacute;o de correo electr&oacute;nico, vuelva a intentarlo mas tarde"), //$NON-NLS-1$
	ERR_LINKED_EXPIRED(3, "link caducado"), //$NON-NLS-1$
	SUC_SEND_MAIL(101, "Su correo se ha enviado correctamente"), //$NON-NLS-1$
	USER_CREATED_CORRECT(102, "El usuario ha sido creado correctamente"), //$NON-NLS-1$
	PASS_RESTORE_CORRECT(103, "La nueva contrase&ntilde;a se ha establecido"), //$NON-NLS-1$
	EXCEP_TIME(104, "Se ha excedido el tiempo maximo de espera para la renovacion de la contrase&ntilde;a"), //$NON-NLS-1$
	ERR_INCORRET_USER(105,"No existe usuario registrado en el sistema"), //$NON-NLS-1$
	ERR_INCORRET_MAIL(106,"No existe usuario registrado en el sistema"), //$NON-NLS-1$
	CERT_CREATED_CORRECT(107, "El certificado ha sido creado correctamente"),
	CERT_REMOVE_CORRECT(108, "El certificado ha sido eliminado correctamente")
			;


	private int code;

	private String text;


	private UserMessages(final int code, final String text) {
		this.code = code;
		this.text = text;
	}

	public int getCode() {
		return this.code;
	}

	public String getText() {
		return this.text;
	}

	public static UserMessages parse(final String code) throws IllegalArgumentException {

		int numCode;
		try {
			numCode = Integer.parseInt(code);
		}
		catch (final Exception e) {
			throw new IllegalArgumentException("Mensaje no soportado"); //$NON-NLS-1$
		}

		for (final UserMessages msg : values()) {
			if (msg.getCode() == numCode) {
				return msg;
			}
		}
		throw new IllegalArgumentException("Mensaje no soportado"); //$NON-NLS-1$
	}

}
