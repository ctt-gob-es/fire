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
 * Actualizador de firmas AdES contra la Plataforma Afirma.
 *
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s
 */
public final class Upgrade {

	private static PlatformWsHelper defaultConn = null;

    /**
     * Constructor privado para no permir la instanciaci&oacute;n
     */
    private Upgrade() {
        // no instanciable
    }

    /**
     * Actualiza una firma AdES.
     *
     * @param data
     *            Firma a actualizar.
     * @param format
     *            Formato de actualizaci&oacute;n.
     * @param afirmaAppName
     *            Nombre de aplicaci&oacute;n en la Plataforma Afirma.
     * @return Firma actualizada.
     * @throws IOException
     *             Si hay problemas en los tratamientos de datos o lectura de
     *             opciones de configuraci&oacute;n.
     * @throws PlatformWsException
     *             Si hay problemas con los servicios Web de mejora de firmas.
     * @throws UpgradeResponseException
     *             Si el servicio Web de mejora de firmas env&iacute;a una
     *             respuesta de error.
     * @throws ConfigFileNotFoundException
     * 			   Cuando no se puede cargar el fichero de configuraci&oacute;n.
     */
    public static byte[] signUpgradeCreate(final byte[] data,
            final UpgradeTarget format, final String afirmaAppName)
            throws IOException, PlatformWsException, UpgradeResponseException, ConfigFileNotFoundException {

    	if (defaultConn == null) {
    		defaultConn = new PlatformWsHelper();
    		defaultConn.init();
    	}

        return signUpgradeCreate(defaultConn, data, format, afirmaAppName);
    }

    /**
     * Actualiza una firma AdES.
     * @param conn
     * 			  Conexi&oacute;n a usar para el acceso a la Plataforma @firma.
     * @param data
     *            Firma a actualizar.
     * @param format
     *            Formato de actualizaci&oacute;n.
     * @param afirmaAppName
     *            Nombre de aplicaci&oacute;n en la Plataforma Afirma.
     * @return Firma actualizada.
     * @throws IOException
     *             Si hay problemas en los tratamientos de datos o lectura de
     *             opciones de configuraci&oacute;n.
     * @throws PlatformWsException
     *             Si hay problemas con los servicios Web de mejora de firmas.
     * @throws UpgradeResponseException
     *             Si el servicio Web de mejora de firmas env&iacute;a una
     *             respuesta de error.
     * @throws ConfigFileNotFoundException
     * 			   Cuando no se puede cargar el fichero de configuraci&oacute;n.
     */
    public static byte[] signUpgradeCreate(final PlatformWsHelper conn, final byte[] data,
            final UpgradeTarget format, final String afirmaAppName)
            throws IOException, PlatformWsException, UpgradeResponseException, ConfigFileNotFoundException {

    	// Es incompatible la configuracion de dos conexiones contra la Plataforma @firma porque
    	// la conexion establece propiedades del sistema en el momento de crearse y se pisarian
    	// al haber mas de una. Si se creo la por defecto, no se puede usar una externa y viceversa.
    	if (defaultConn != null && defaultConn != conn) {
    		throw new IOException("No se puede configurar simultaneamente una conexion propia y la conexion por defecto contra la Plataforma @firma"); //$NON-NLS-1$
    	}

        final String inputDss = DssServicesUtils.createSignUpgradeDss(
        		data,
        		format,
        		afirmaAppName);

        final byte[] response = conn.doPlatformCall(
        		inputDss,
                PlatformWsHelper.SERVICE_SIGNUPGRADE);

        // Analisis de la respuesta
        final UpgradeResponse vr;
        try {
            vr = new UpgradeResponse(response);
        } catch (final Exception e) {
            throw new PlatformWsException(
                    "Error analizando la respuesta de la Plataforma @firma: " + e, e); //$NON-NLS-1$
        }

        // Comprobamos que la operacion haya finalizado correctamente
        if (!vr.isOk() || vr.getUpgradedSignature() == null) {
            throw new UpgradeResponseException(vr.getMajorCode(),
                    vr.getMinorCode(), vr.getDescription());
        }

        // Comprobamos que la actualizacion haya finalizado correctamente y que el formato
        // actualizado sea el que se habia pedido
        if (!format.equivalent(vr.getSignatureForm())){
        	throw new UpgradeResponseException(vr.getMajorCode(),
        			vr.getMinorCode(), "No se ha actualizado al formato solicitado. Formato recibido: " + vr.getSignatureForm()); //$NON-NLS-1$
        }

        return vr.getUpgradedSignature();
    }
}
