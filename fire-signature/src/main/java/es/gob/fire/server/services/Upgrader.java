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

import es.gob.fire.upgrade.UpgradeResult;

/** Interfaz para la actualizaci&oacute;n de firmas. */
public interface Upgrader {

	/** Actualiza una firma.
	 * Si no se indica formato de actualizaci&oacute;n, se devuelve la propia firma.
	 * @param signature Firma que se desea actualizar.
	 * @param upgradeFormat Formato avanzado al que actualizar.
	 * @return Firma actualizada o, si no se indica un formato de actualizacion, la propia firma.
	 * @throws UpgradeException Cuando ocurre cualquier problema que impida la
	 *                          actualizaci&oacute;n de la firma. */
	UpgradeResult upgradeSignature(final byte[] signature, final String upgradeFormat) throws UpgradeException;
}
