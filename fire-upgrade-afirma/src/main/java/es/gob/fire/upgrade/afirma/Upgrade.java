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
import java.text.SimpleDateFormat;

import es.gob.fire.upgrade.UpgradeResult;

/**
 * Actualizador de firmas AdES contra la Plataforma Afirma.
 *
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s
 */
public final class Upgrade {

	private static final SimpleDateFormat formatter;

	static {
		formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX"); //$NON-NLS-1$
	}

    /**
     * Constructor privado para no permir la instanciaci&oacute;n
     */
    private Upgrade() {
        // no instanciable
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
     * @param ignoreGracePeriod
     * 			  Indica que debe ignorase el periodo de gracia de la actualizaci&oacute;n de firma.
     * @return Resultado de la actualizaci&oacute;n de la firma.
     * @throws IOException
     *             Si hay problemas en los tratamientos de datos o lectura de
     *             opciones de configuraci&oacute;n.
     * @throws PlatformWsException
     *             Si hay problemas con los servicios Web de mejora de firmas.
     * @throws AfirmaResponseException
     *             Si el servicio Web de mejora de firmas env&iacute;a una
     *             respuesta de error.
     */
    public static UpgradeResult signUpgradeCreate(final AfirmaConnector conn, final byte[] data,
            final UpgradeTarget format, final String afirmaAppName, final boolean ignoreGracePeriod)
            throws IOException, PlatformWsException, AfirmaResponseException {

        final String inputDss = DssServicesUtils.createSignUpgradeDss(
        		data,
        		format,
        		afirmaAppName,
        		ignoreGracePeriod);

        final byte[] response = conn.doPlatformCall(
        		inputDss,
                AfirmaConnector.SERVICE_SIGNUPGRADE);

        // Analisis de la respuesta
        final UpgradeAfirmaResponse vr;
        try {
            vr = new UpgradeAfirmaResponse(response);
        } catch (final Exception e) {
            throw new PlatformWsException(
                    "Error analizando la respuesta de la Plataforma @firma: " + e, e); //$NON-NLS-1$
        }

        // Comprobamos si la operacion termino indicando que requiere periodo de gracia
        if (vr.isPending()) {
        	try {
        		return new UpgradeResult(vr.getResponseId(), formatter.parse(vr.getResponseTime()));
	        }
	        catch (final Exception e) {
	        	throw new AfirmaResponseException(vr.getMajorCode(),
	        			vr.getMinorCode(), "Error al componer el resultado con el periodo de gracia", e); //$NON-NLS-1$
	        }
        }

        // Comprobamos que la operacion haya finalizado correctamente
        if (!vr.isOk()) {
            throw new AfirmaResponseException(vr.getMajorCode(),
                    vr.getMinorCode(), vr.getDescription());
        }

        // Comprobamos que la actualizacion haya finalizado correctamente y que el formato
        // actualizado sea el que se habia pedido
        if (!format.equivalent(vr.getSignatureForm())) {
        	throw new AfirmaResponseException(vr.getMajorCode(),
        			vr.getMinorCode(), "No se ha actualizado al formato solicitado. Formato recibido: " + vr.getSignatureForm()); //$NON-NLS-1$
        }

        try {
        	return new UpgradeResult(
        			vr.getUpgradedSignature(),
        			vr.getSignatureForm().substring(vr.getSignatureForm().lastIndexOf(':') + 1));
        }
        catch (final Exception e) {
        	throw new AfirmaResponseException(vr.getMajorCode(),
        			vr.getMinorCode(), "Error al componer el resultado con la firma", e); //$NON-NLS-1$
        }
    }
}
