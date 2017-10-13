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

import es.gob.afirma.core.signers.TriphaseData;
import es.gob.afirma.core.signers.TriphaseData.TriSign;

/**
 * Procesador para hacer que los ID de una operaci&oacute;n de firma sean todos distintos
 * y despu&eacute;s deshacer el cambio.
 */
public class FIReTriSignIdProcessor {

	private static final char SEP = '*';

	/**
	 * Obtiene un objeto con la mismas firmas que el indicado, pero con los ID modificados
	 * para que no sean iguales.
	 * @param triData Objeto de firmas.
	 * @return Objeto de firmas con los ID &uacute;nicos.
	 */
	public static TriphaseData make(final TriphaseData triData) {

		final TriphaseData data = new TriphaseData();
		data.setFormat(triData.getFormat());

		int n = 0;
		for (final TriSign sign : triData.getTriSigns()) {
			if (sign.getId() != null) {
				data.addSignOperation(new TriSign(sign.getDict(), sign.getId() + SEP + n));
			}
			else {
				data.addSignOperation(new TriSign(sign));
			}
			n++;
		}

		return data;
	}

	/**
	 * Deshace el cambio que garantizaba que los ID de las firmas eran distintos.
	 * @param triData Objeto de firmas modificado.
	 * @return Objeto de firmas con los ID originales.
	 */
	public static TriphaseData unmake(final TriphaseData triData) {
		final TriphaseData data = new TriphaseData();
		data.setFormat(triData.getFormat());

		for (final TriSign sign : triData.getTriSigns()) {
			if (sign.getId() != null && sign.getId().lastIndexOf(SEP) != -1) {
				data.addSignOperation(new TriSign(sign.getDict(), sign.getId().substring(0, sign.getId().lastIndexOf(SEP))));
			}
			else {
				data.addSignOperation(new TriSign(sign));
			}
		}

		return data;
	}

	/**
	 * Deshace el cambio de formato de los ID para un ID concreto.
	 * @param id Identificador del que se desea deshacer el cambio.
	 * @return Identificador sin modificar.
	 */
	public static String unmake(final String id) {
		return id.lastIndexOf(SEP) != -1 ? id.substring(0, id.lastIndexOf(SEP)) : id;
	}
}
