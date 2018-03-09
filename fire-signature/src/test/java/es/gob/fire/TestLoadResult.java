/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import es.gob.afirma.core.misc.Base64;
import es.gob.fire.server.connector.LoadResult;
import es.gob.fire.server.connector.TriphaseData;
import es.gob.fire.server.connector.TriphaseData.TriSign;

/** Pruebas del resultado de la carga de datos.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
public final class TestLoadResult {

	private static final String JSON = "{\r\n" + //$NON-NLS-1$
		"  \"transacionid\":\"ID001\",\r\n" + //$NON-NLS-1$
		"  \"redirecturl\":\"http://google.com\",\r\n" + //$NON-NLS-1$
		"  \"triphasedata\":\"PHhtbD4KIDxmaXJtYXM+CiAgPGZpcm1hIElkPSJJRFRSSVNJR04tMDAxIj4KICAgPHBhcmFtIG49IlBSRSI+YUc5c1lTQnRkVzVrYnc9PTwvcGFyYW0+CiAgPC9maXJtYT4KIDwvZmlybWFzPgo8L3htbD4=\"\r\n" + //$NON-NLS-1$
		"}\r\n"; //$NON-NLS-1$

	/** prueba de la conversi&oacute;n a JSON de un resultado de carga ficticio. */
	@SuppressWarnings("static-method")
	@Test
	public void testDummyLoadResultCreation() {

		final Map<String, String> prop = new HashMap<>(1);
		prop.put("PRE", Base64.encode("hola mundo".getBytes())); //$NON-NLS-1$ //$NON-NLS-2$

		final TriSign ts = new TriSign(prop, "IDTRISIGN-001"); //$NON-NLS-1$

		final TriphaseData td = new TriphaseData(Collections.singletonList(ts));

		final LoadResult dlr = new LoadResult(
			"ID001", //$NON-NLS-1$
			"http://google.com", //$NON-NLS-1$
			td
		);
		System.out.println(dlr.toString());
	}

	/** prueba de la lectura de un JSON de un resultado de carga ficticio.
	 * @throws Exception En cualquier error. */
	@SuppressWarnings("static-method")
	@Test
	public void testDummyLoadResultparse() throws Exception {
		final LoadResult dlr = new LoadResult(JSON);
		System.out.println("Resultado:\n" + dlr); //$NON-NLS-1$
	}

}
