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
import java.util.Properties;

import es.gob.fire.upgrade.ConnectionException;
import es.gob.fire.upgrade.SignatureValidator;
import es.gob.fire.upgrade.UpgradeException;
import es.gob.fire.upgrade.UpgradeResult;
import es.gob.fire.upgrade.VerifyException;
import es.gob.fire.upgrade.VerifyResult;
import es.gob.fire.upgrade.afirma.ws.WSServiceInvokerException;

/**
 * Clase para la actualizaci&oacute;n de firmas mediante una conexi&oacute;n
 * con la Plataforma @firma.
 */
public class AfirmaValidator implements SignatureValidator {

	private static final String PROP_AFIRMA_APPID = "afirma.appId"; //$NON-NLS-1$

	private static final String PROP_IGNORE_GRACE_PERIOD = "ignoreGracePeriod"; //$NON-NLS-1$

	/**
	 * C&oacute;digo Result Major de respuesta de Afirma usada en los errores de validaci&oacute;n
	 * durante la actualizaci&oacute;n de firma.
	 */
	private static final String RMAJOR_RESPONDER_ERROR = "ResponderError"; //$NON-NLS-1$

	/**
	 * Particula encontrada en el mensaje de Afirma cuando se detecta un error de validaci&oacute;n
	 * durante la actualizaci&oacute;n de firma.
	 */
	private static final String VALIDATION_ERROR_FRAGMENT = "(VALIDATION)"; //$NON-NLS-1$

	private AfirmaConnector conn = null;

	private String appId = null;

	@Override
	public void init(final Properties config) {
		this.conn = new AfirmaConnector();
		this.conn.init(config);

		this.appId = config.getProperty(PROP_AFIRMA_APPID);
	}

	/**
	 * Actualiza una firma utilizando la Plataforma @firma. Si no se indica formato de
	 * actualizaci&oacute;n, se devuelve la propia firma.
	 * @param signature Firma que se desea actualizar.
	 * @param upgradeFormat Formato avanzado al que actualizar.
	 * @return Firma actualizada o, si no se indica un formato de actualizacion, la propia firma.
	 * @throws UpgradeException Cuando ocurre cualquier problema que impida la
	 * actualizaci&oacute;n de la firma.
	 * @throws ConnectionException Cuando falla la conexi&oacute;n con la Plataforma @firma.
	 * @throws VerifyException Cuando no se puede actualizar la firma porque esta es inv&aacute;lida.
	 */
	@Override
	public UpgradeResult upgradeSignature(final byte[] signature, final String upgradeFormat,
			final Properties config) throws UpgradeException, ConnectionException, VerifyException {

		if (upgradeFormat == null || upgradeFormat.isEmpty()) {
			return new UpgradeResult(signature, null);
		}

		if (this.conn == null) {
			throw new UpgradeException("No se ha inicializado el validador. Llame al metodo " //$NON-NLS-1$
					+ "init() proporcionando la configuracion necesaria"); //$NON-NLS-1$
		}
		if (this.appId == null) {
			throw new UpgradeException("No se ha proporcionado el ID de aplicacion de la Plataforma" //$NON-NLS-1$
					+ " Afirma junto con la configuracion de conexion (propiedad " + PROP_AFIRMA_APPID //$NON-NLS-1$
					+ "). Llame al metodo init() proporcionando la configuracion necesaria"); //$NON-NLS-1$
		}

		final boolean ignoreGracePeriod = needIgnoreGracePeriod(config);

		UpgradeResult upgradeResult;
		try {
			upgradeResult = Upgrade.upgradeSignature(
					this.conn,
					signature,
					UpgradeTarget.getUpgradeTarget(upgradeFormat),
					this.appId,
					ignoreGracePeriod);
		} catch (final PlatformWsException e) {
			throw new UpgradeException("Error de conexion con la Plataforma @firma para la actualizacion de la firma", e); //$NON-NLS-1$
		} catch (final AfirmaResponseException e) {
			if (RMAJOR_RESPONDER_ERROR.equals(e.getMajorCode()) && e.getMessage() != null &&
					e.getMessage().contains(VALIDATION_ERROR_FRAGMENT)) {
				throw new VerifyException("La firma que se intenta actualizar no es valida", e); //$NON-NLS-1$
			}
			throw new UpgradeException("Error durante la actualizacion de la firma. MajorCode: " + e.getMajorCode() + //$NON-NLS-1$
					". MinorCode: " + e.getMinorCode(), e); //$NON-NLS-1$
		} catch (final WSServiceInvokerException e) {
			throw new ConnectionException("Error en la comunicacion con la Plataforma @firma", e); //$NON-NLS-1$
		} catch (final Exception e) {
			throw new UpgradeException("Error no identificado durante el proceso de actualizacion de la firma", e); //$NON-NLS-1$
		}

		return upgradeResult;
	}

	@Override
	public UpgradeResult recoverUpgradedSignature(final String docId, final String upgradeFormat,
			final Properties config) throws UpgradeException, ConnectionException, IOException {

		if (this.conn == null) {
			throw new UpgradeException("No se ha inicializado el validador. Llame al metodo " //$NON-NLS-1$
					+ "init() proporcionando la configuracion necesaria"); //$NON-NLS-1$
		}
		if (this.appId == null) {
			throw new UpgradeException("No se ha proporcionado el ID de aplicacion de la Plataforma" //$NON-NLS-1$
					+ " Afirma junto con la configuracion de conexion (propiedad " + PROP_AFIRMA_APPID //$NON-NLS-1$
					+ "). Llame al metodo init() proporcionando la configuracion necesaria"); //$NON-NLS-1$
		}

		UpgradeResult upgradeResult;
		try {
			upgradeResult = Upgrade.recoverUpgradedSignature(
					this.conn,
					docId,
					UpgradeTarget.getUpgradeTarget(upgradeFormat),
					this.appId);
		} catch (final PlatformWsException e) {
			throw new IOException("Error de conexion con la Plataforma @firma para la recuperacion asincrona de una firma", e); //$NON-NLS-1$
		} catch (final AfirmaResponseException e) {
			throw new UpgradeException("Error durante la recuperacion asincrona de la firma. MajorCode: " + e.getMajorCode() + //$NON-NLS-1$
					". MinorCode: " + e.getMinorCode() + ". Description: " + e.getMessage(), e); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (final WSServiceInvokerException e) {
			throw new ConnectionException("Error en la comunicacion con la Plataforma @firma", e); //$NON-NLS-1$
		} catch (final Exception e) {
			throw new IOException("Error no identificado durante el proceso de recuperacion asincrona de la firma", e); //$NON-NLS-1$
		}

		return upgradeResult;
	}

	/**
	 * Valida una firma firma utilizando la Plataforma @firma.
	 * @param signature Firma que se desea validar.
	 * @param config Configuraci&oacute;n adicional para la operaci&oacute;n.
	 * @return Resultado de la validaci&oacute;n.
	 * @throws ConnectionException Cuando no se puede conectar con la Plataforma @firma.
	 * @throws VerifyException Cuando ocurre cualquier problema que impida la
	 * validaci&oacute;n de la firma.
	 */
	@Override
	public VerifyResult validateSignature(final byte[] signature, final Properties config)
			throws ConnectionException, VerifyException {

		if (this.conn == null || this.appId == null) {
			throw new VerifyException("No se ha inicializado el validador. Llame al metodo " //$NON-NLS-1$
					+ "init() proporcionando la configuracion necesaria"); //$NON-NLS-1$
		}
		if (this.appId == null) {
			throw new VerifyException("No se ha proporcionado el ID de aplicacion de la Plataforma" //$NON-NLS-1$
					+ " Afirma junto con la configuracion de conexion (propiedad " + PROP_AFIRMA_APPID //$NON-NLS-1$
					+ "). Llame al metodo init() proporcionando la configuracion necesaria"); //$NON-NLS-1$
		}

		VerifyResult verifyResult;
		try {
			verifyResult = Verify.verifySignature(
					this.conn,
					signature,
					this.appId);
		} catch (final PlatformWsException e) {
			throw new VerifyException("Error en la respuesta de la Plataforma @firma", e); //$NON-NLS-1$
		} catch (final WSServiceInvokerException e) {
			throw new ConnectionException("Error en la comunicacion con la Plataforma @firma", e); //$NON-NLS-1$
		} catch (final Exception e) {
			throw new VerifyException("Error no identificado durante el proceso de validacion de la firma", e); //$NON-NLS-1$
		}

		return verifyResult;
	}

	/**
	 * Indica si se debe ignorar el periodo de gracia en las operaciones de actualizacion de firmas.
	 * @param config Configuraci&oacute;n proporcionada para la operaci&oacute;n.
	 * @return {@code true} si se configur&oacute; el ignorar el periodo de gracia, {@code false}
	 * en caso contrario.
	 */
	private static boolean needIgnoreGracePeriod(final Properties config) {
		return config != null && Boolean.parseBoolean(config.getProperty(PROP_IGNORE_GRACE_PERIOD));
	}
}
