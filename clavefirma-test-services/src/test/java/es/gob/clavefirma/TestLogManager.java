/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.clavefirma;

import java.util.logging.Logger;

import org.junit.Test;

import es.gob.fire.signature.LoggingHandler;

/** Prueba del gestor de registro.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s */
public final class TestLogManager {

	/** Prueba del registro firmado.
	 * @throws Exception Si hay cualquier problema durante la prueba */
	@SuppressWarnings("static-method")
	@Test
	public void testLoghandler() throws Exception {
		LoggingHandler.install();
		final Logger logger = Logger.getLogger(TestLogManager.class.getName());
		logger.info("Info"); //$NON-NLS-1$
		logger.warning("Warning"); //$NON-NLS-1$
		logger.severe("Severe"); //$NON-NLS-1$
	}

}
