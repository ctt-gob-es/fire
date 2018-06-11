/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.services.internal;

/**
 * Error de operacion identificado en servidor. Establece los codigos y mensajes de error
 * que se env&iacute;n a los clientes distribuidos como resultado de una operaci&oacute;n.
 */
enum OperationError {

	// Errores generales
	UNKNOWN_USER(1, "El usuario no esta dado de alta en el sistema"), //$NON-NLS-1$
	INVALID_STATE(2, "Estado invalido"), //$NON-NLS-1$

	UNDEFINED_ERROR(3, "Error desconocido durante la operaci&oacute;n"), //$NON-NLS-1$
	OPERATION_CANCELED(4, "Operacion cancelada por el usuario"), //$NON-NLS-1$
	INVALID_SESSION(6, "La sesi&oacute;n no es v&aacute;lida o ha caducado"), //$NON-NLS-1$

	INTERNAL_ERROR(7, "Error interno del servidor"), //$NON-NLS-1$
	EXTERNAL_SERVICE_ERROR(8, "Error detectado despues de llamar a la pasarela externa"), //$NON-NLS-1$

	// Errores especificos del servicios de listado de certificados
	CERTIFICATES_SERVICE(101, "Error en la obtencion de los certificados"), //$NON-NLS-1$
	CERTIFICATES_SERVICE_NETWORK(102, "Error al conectar con el servicio para la recuperacion de certificados"), //$NON-NLS-1$
	CERTIFICATES_BLOCKED(103, "Los certificados del usuario estan bloqueados"), //$NON-NLS-1$
	CERTIFICATES_WEAK_REGISTRY(104, "El usuario no puede poseer certificados de firma por haber realizado un registro no fehaciente"), //$NON-NLS-1$
	CERTIFICATES_NO_CERTS(105, "El usuario no dispone de certificados de firma ni puede generarlos desde FIRe"), //$NON-NLS-1$

	// Errores propios de la operacion de firma
	SIGN_SERVICE(201, "Error en la obtencion de la firma de los datos"), //$NON-NLS-1$
	SIGN_SERVICE_PRESIGN(202, "Error al ejecutar la prefirma de los datos"), //$NON-NLS-1$
	//SIGN_SERVICE_POSTSIGN(203, "Error al ejecutar la postfirma de los datos"), //$NON-NLS-1$
	SIGN_SERVICE_NETWORK(205, "Error al conectar con el servicio para la generacion de la firma con la clave remota"), //$NON-NLS-1$

	SIGN_MINIAPPLET(250, "Error al generar la firma con el cliente nativo"), //$NON-NLS-1$
	SIGN_MINIAPPLET_BATCH(251, "No se completo correctamente la firma del lote con certificado local"); //$NON-NLS-1$

	private int code;

	private String message;

	private OperationError(final int code, final String message) {
		this.code = code;
		this.message = message;
	}

	public int getCode() {
		return this.code;
	}

	public String getMessage() {
		return this.message;
	}

}
