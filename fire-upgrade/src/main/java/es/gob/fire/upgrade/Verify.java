/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.upgrade;

import java.io.IOException;

/**
 * Verificador de certificados contra la Plataforma Afirma.
 *
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s
 */
public final class Verify {

	private static PlatformWsHelper defaultConn = null;

    /**
     * Constructor privado para no permir la instanciaci&oacute;n
     */
    private Verify() {
        // no instanciable
    }

    /**
     * Verifica el estado de un certificado usando la plataforma Afirma.
     *
     * @param cert
     *            Certificado.
     * @param afirmaAppName
     *            Nombre de aplicaci&oacute;n en la Plataforma Afirma.
     * @return Respuesta del servicio de validaci&oacute;n.
     * @throws IOException
     *             Si hay problemas en los tratamientos de datos o lectura de
     *             opciones de configuraci&oacute;n.
     * @throws PlatformWsException
     *             Si hay problemas con los servicios Web de la plataforma
     *             Afirma.
     * @throws VerifyException
     *             Si hay problemas en la propia validaci&oacute;n del
     *             certificado.
     * @throws ConfigFileNotFoundException
     * 			   Cuando no se puede cargar el fichero de configuraci&oacute;n.
     */
    public static VerifyResponse vertifyCertificate(final byte[] cert,
            final String afirmaAppName) throws PlatformWsException,
            IOException, VerifyException, ConfigFileNotFoundException {

    	if (defaultConn == null) {
    		defaultConn = new PlatformWsHelper();
    		defaultConn.init();
    	}

    	return vertifyCertificate(defaultConn, cert, afirmaAppName);
    }

    /**
     * Verifica el estado de un certificado usando la Plataforma Afirma.
     * @param conn
     * 			  Conexi&oacute;n con la Plataforma @firma.
     * @param cert
     *            Certificado.
     * @param afirmaAppName
     *            Nombre de aplicaci&oacute;n en la Plataforma Afirma.
     * @return Respuesta del servicio de validaci&oacute;n.
     * @throws IOException
     *             Si hay problemas en los tratamientos de datos o lectura de
     *             opciones de configuraci&oacute;n.
     * @throws PlatformWsException
     *             Si hay problemas con los servicios Web de la plataforma
     *             Afirma.
     * @throws VerifyException
     *             Si hay problemas en la propia validaci&oacute;n del
     *             certificado.
     * @throws ConfigFileNotFoundException
     * 			   Cuando no se puede cargar el fichero de configuraci&oacute;n.
     */
    public static VerifyResponse vertifyCertificate(final PlatformWsHelper conn,
    		final byte[] cert, final String afirmaAppName) throws PlatformWsException,
            IOException, VerifyException, ConfigFileNotFoundException {

        final String inputDss = VerifyUtils.createCertVerifyDss(cert,
                afirmaAppName);
        final byte[] response = conn.doPlatformCall(inputDss,
                PlatformWsHelper.SERVICE_CERTVERIFY);

        try {
            return new VerifyResponse(response);
        } catch (final Exception e) {
            throw new VerifyException(
                    "Error analizando al respuesta de la plataforma a una solicitud de validacion de certificado", //$NON-NLS-1$
                    e);
        }

    }

}
