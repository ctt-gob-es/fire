/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.services;

/**
 * Operaci&oacute;n que puede procesar el servicio web.
 */
public enum FIReServiceOperation {

	/** Operaci&oacute;n de firma. */
	SIGN (1),
	/** Operaci&oacute;n de recuperaci&oacute;n del resultado de firma. */
	RECOVER_SIGN (2),
	/** Operaci&oacute;n de creaci&oacute;n de un lote de firma. */
	CREATE_BATCH (5),
	/** Operaci&oacute;n para agregar un documento a un lote de firma. */
	ADD_DOCUMENT_TO_BATCH (6),
	/** Operaci&oacute;n para completar la firma de un lote. */
	SIGN_BATCH (7),
	/** Operaci&oacute;n para recuperar el resultado de una firma de lote. */
	RECOVER_BATCH (8),
	/** Operaci&oacute;n para recuperar el progreso actual del proceso de un lote de firma. */
	RECOVER_BATCH_STATE(9),
	/** Operaci&oacute;n para recuperar una firma particular de un lote. */
	RECOVER_SIGN_BATCH (10),
	/** Operaci&oacute;n para recuperar una firma simple generada que se sabe que termin&oacute; correctamente. */
	RECOVER_SIGN_RESULT(11),
	/** Operaci&oacute;n para recuperar el estado de una firma previamente enviada a actualizar y para la que se debi&oacute; esperar un periodo de gracia. */
	RECOVER_UPDATED_SIGN(70),
	/** Operaci&oacute;n para recuperar la firma resultante de una operaci&oacute;n de
	 * actualizaci&oacute;n para la que se solicit&oacute; la espera de un periodo de gracia. */
	RECOVER_UPDATED_SIGN_RESULT(71),
	/** Operaci&oacute;n para recuperar el detalle de un error producido durante una transacci&oacute;n. */
	RECOVER_ERROR(99);

	/** Identificador de la operaci&oacute;n. */
	private final String id;

	private FIReServiceOperation(final int id) {
		this.id = Integer.toString(id);
	}

	/**
	 * Recupera el identificador de la operaci&oacute;n.
	 * @return Identificador.
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * Obtiene la operaci&oacute;n con el identificador indicado.
	 * @param id Identificador de operaci&oacute;n.
	 * @return Operaci&oacute;n.
	 */
	public static FIReServiceOperation parse(final String id) {
		for (final FIReServiceOperation op : values()) {
			if (op.id.equals(id)) {
				return op;
			}
		}
		throw new IllegalArgumentException("Se ha indicado un identificador de operacion no valido: " + id); //$NON-NLS-1$
	}
}
