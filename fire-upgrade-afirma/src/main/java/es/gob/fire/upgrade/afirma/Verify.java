/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.upgrade.afirma;

import java.io.IOException;

import es.gob.fire.upgrade.VerifyResult;

/**
 * Verificador de certificados contra la Plataforma Afirma.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s
 */
public final class Verify {

	/**
	 * Constructor privado para no permir la instanciaci&oacute;n
	 */
	private Verify() {
		// no instanciable
	}

	/**
	 * Valida una firma electr&oacute;nica usando la Plataforma Afirma.
	 * @param conn
	 * 			  Conexi&oacute;n con la Plataforma @firma.
	 * @param signature
	 *            Firma electr&oacute;nica a validar.
	 * @param afirmaAppName
	 *            Nombre de aplicaci&oacute;n en la Plataforma Afirma.
	 * @return Respuesta del servicio de validaci&oacute;n.
	 * @throws IOException
	 *             Si hay problemas en los tratamientos de datos o lectura de
	 *             opciones de configuraci&oacute;n.
	 * @throws PlatformWsException
	 *             Si hay problemas con los servicios Web de la plataforma
	 *             Afirma.
	 */
	public static VerifyResult verifySignature(final AfirmaConnector conn,
			final byte[] signature, final String afirmaAppName) throws IOException,
				PlatformWsException {

		final String inputDss = DssServicesUtils.createSignVerifyDss(signature, afirmaAppName);
		final byte[] responseBytes = conn.doPlatformCall(
				inputDss,
				AfirmaConnector.SERVICE_SIGNUPGRADE,
				AfirmaConnector.SIGNUPGRADE_OPERATION_VERIFY);

		VerifyAfirmaResponse response;
		try {
			response = new VerifyAfirmaResponse(responseBytes);
		} catch (final Exception e) {
			throw new PlatformWsException(
					"Error analizando al respuesta de la plataforma a una solicitud de validacion de firma", //$NON-NLS-1$
					e);
		}

		return new VerifyResult(response.isOk());
	}

}
