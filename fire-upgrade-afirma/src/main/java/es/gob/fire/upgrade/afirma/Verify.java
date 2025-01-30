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

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Base64;

import es.gob.fire.upgrade.VerifyResult;
import es.gob.fire.upgrade.afirma.ws.WSServiceInvokerException;

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
	 * @throws PlatformWsException
	 *             Si ocurre un error al procesar la petici&oacute;n o la
	 *             respuesta del servicio.
	 * @throws WSServiceInvokerException Si falla la conexi&oacute;n con la plataforma.
	 */
	public static VerifyResult verifySignature(final AfirmaConnector conn,
			final byte[] signature, final String afirmaAppName)
					throws PlatformWsException, WSServiceInvokerException {

		final String inputDss = DssServicesUtils.createSignVerifyDss(signature, afirmaAppName);
		final byte[] responseBytes = conn.verifySignature(inputDss);

		VerifyAfirmaResponse response;
		try {
			response = new VerifyAfirmaResponse(responseBytes);
		} catch (final Exception e) {
			throw new PlatformWsException(
					"Error analizando al respuesta de la plataforma a una solicitud de validacion de firma", //$NON-NLS-1$
					e);
		}

		return response.isOk()
				? new VerifyResult(true)
						: new VerifyResult(false, response.getDescription());
	}

	/**
	 * Verifies the status of an X.509 certificate using the Afirma web service.
	 *
	 * <p>This method converts the given certificate to a Base64-encoded string, 
	 * constructs a DSS (Digital Signature Service) XML request, and sends it 
	 * to the Afirma web service for verification.</p>
	 *
	 * @param afirmaConnector The {@link AfirmaConnector} instance used to communicate with the Afirma web service.
	 * @param x509Certificate The {@link X509Certificate} to be verified.
	 * @param afirmaAppName The name of the Afirma application making the request.
	 * @return A {@link VerifyAfirmaCertificateResponse} object containing the verification result.
	 * @throws CertificateEncodingException If an error occurs while encoding the certificate.
	 * @throws WSServiceInvokerException If there is an issue invoking the web service.
	 * @throws PlatformWsException If an error occurs while processing the response from the Afirma platform.
	 */
	public static VerifyAfirmaCertificateResponse verifyCertificate(AfirmaConnector afirmaConnector, X509Certificate x509Certificate, String afirmaAppName) throws CertificateEncodingException, WSServiceInvokerException, PlatformWsException {
		// Convertimos el certificado a Base 64 para que vaya en la petición correctamente
		String certificateB64 = Base64.getEncoder().encodeToString(x509Certificate.getEncoded());
		
		final String inputDss = DssServicesUtils.createDssAfirmaVerifyCertificate(certificateB64, afirmaAppName);
		
		final byte[] responseBytes = afirmaConnector.verifyCertificate(inputDss);
		
		VerifyAfirmaCertificateResponse verifyAfirmaCertificateResponse;
		try {
			verifyAfirmaCertificateResponse = new VerifyAfirmaCertificateResponse(responseBytes);
		} catch (final Exception e) {
			throw new PlatformWsException(
					"Error analizando al respuesta de la plataforma a una solicitud de validacion de firma", //$NON-NLS-1$
					e);
		}
		
		return verifyAfirmaCertificateResponse;
	}

}
