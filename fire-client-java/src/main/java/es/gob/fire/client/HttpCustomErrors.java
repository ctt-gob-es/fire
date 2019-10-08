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

/**
 * Identificadores de errores HTTP definidos por Cl@ve Firma.
 */
public enum HttpCustomErrors {

	/** Identifica los casos en los que un usuario no tiene certifciados del tipo indicado. */
	NO_CERTS(522, "El usuario no tiene certificados"), //$NON-NLS-1$

	/** El usuario no esta dado de alta en el sistema. */
	NO_USER(523, "Usuario no dado de alta en el sistema"), //$NON-NLS-1$

	/** El usuario tiene los certificados bloqueados. */
	CERTIFICATE_BLOCKED(524, "El certificado del usuario esta bloqueado"), //$NON-NLS-1$

	/** Error que identifica que el usuario ya dispone de tantos certificados como puede tener del tipo indicado. */
	CERTIFICATE_AVAILABLE(525, "El usuario ya tiene certificados generados"), //$NON-NLS-1$

	/** Se excede el l&iacute;mite de documentos establecido (com&uacute;nmente, el tama&ntilde;o de un lote). */
	NUM_DOCUMENTS_EXCEEDED(526, "Se excedido el numero maximo de documentos permitidos"), //$NON-NLS-1$

	/** Se indica un identificador de documento que ya es&aacute; dado de alta en el lote de firma. */
	DUPLICATE_DOCUMENT(527, "El identificador de documento ya existe en el lote"), //$NON-NLS-1$

	/** La transaccion indicada no es valida o ya ha caducado. */
	INVALID_TRANSACTION(528, "La transaccion no es valida o ha caducado"), //$NON-NLS-1$

	/** Error devuelto por el servicio de custodia al realizar la operaci&oacute;n de firma. */
	SIGN_ERROR(529, "El servicio de custodia devolvio un error durante la firma de los datos"), //$NON-NLS-1$

	/** Error devuelto por el servicio de custodia al realizar la operaci&oacute;n de firma. */
	POSTSIGN_ERROR(530, "Error en la composicion de la firma"), //$NON-NLS-1$

	/** Error devuelto por el servicio de custodia al realizar la operaci&oacute;n de firma. */
	WEAK_REGISTRY(531, "El usuario realizo un registro debil y no puede tener certificados de firma"), //$NON-NLS-1$

	/** Error devuelto por el servicio de custodia al realizar la actualizaci&oacute;n de la firma. */
	UPGRADING_ERROR(532, "Error durante la actualizacion de firma"), //$NON-NLS-1$

	/** Error devuelto al no poder guardar la firma en servidor a trav&eacute;s del gestor de documentos. */
	SAVING_ERROR(533, "Error al guardar la firma en servidor"), //$NON-NLS-1$

	/** Error devuelto cuando se solicita recuperar una firma de un lote sin haberlo firmado antes. */
	BATCH_NO_SIGNED(534, "El lote no se ha firmado"), //$NON-NLS-1$

	/** Error devuelto cuando se solicita recuperar una firma de un lote sin haberlo firmado antes. */
	INVALID_BATCH_DOCUMENT(535, "El documento no existe en el lote"), //$NON-NLS-1$

	/** Error devuelto cuando se solicita recuperar una firma de un lote sin haberlo firmado antes. */
	BATCH_DOCUMENT_FAILED(536, "Fallo la firma del documento que se intenta recuperar"), //$NON-NLS-1$

	/** Error devuelto cuando se solicita firmar un lote sin documentos. */
	BATCH_NO_DOCUMENT(537, "Se intenta firmar un lote sin documentos"), //$NON-NLS-1$

	/** Error devuelto cuando se detecta que la firma generada no es v&aacute;lida. */
	INVALID_SIGNATURE_ERROR(538, "La firma generada no es valida"); //$NON-NLS-1$

	private final int errorCode;
	private final String errorDescription;

	HttpCustomErrors(final int code, final String description) {
		this.errorCode = code;
		this.errorDescription = description;
	}

	/**
	 * Recupera el c&oacute;digo del error.
	 * @return C&oacute;digo de error.
	 */
	public int getErrorCode() {
		return this.errorCode;
	}

	/**
	 * Recupera el mensaje asociado al error.
	 * @return Mensaje de error.
	 */
	public String getErrorDescription() {
		return this.errorDescription;
	}
}
