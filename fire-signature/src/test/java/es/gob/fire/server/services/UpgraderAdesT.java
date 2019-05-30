package es.gob.fire.server.services;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.GregorianCalendar;
import java.util.Properties;

import es.gob.afirma.core.AOException;
import es.gob.afirma.signers.cades.AOCAdESSigner;
import es.gob.afirma.signers.tsp.pkcs7.CMSTimestamper;
import es.gob.afirma.signers.tsp.pkcs7.TsaParams;
import es.gob.afirma.signers.xades.AOXAdESSigner;
import es.gob.afirma.signers.xades.XAdESTspUtil;
import es.gob.fire.upgrade.UpgradeResult;
import es.gob.fire.upgrade.UpgradeTarget;

/** Actualizador de firmas que solo soporta CAdES-T y XAdES-T.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
public final class UpgraderAdesT implements Upgrader {

	private static final Properties TSA_CFG = new Properties();
	static {
		try {
			TSA_CFG.load(
				UpgraderAdesT.class.getResourceAsStream("/adestupgrader.properties") //$NON-NLS-1$
			);
		}
		catch (final IOException e) {
			throw new IllegalStateException(
				"Error cargando la configuracion del mejorador AdES-T: " + e, e //$NON-NLS-1$
			);
		}
	}

	@Override
	public UpgradeResult upgradeSignature(final byte[] signature,
			                              final String upgradeFormat) throws UpgradeException {
		final UpgradeTarget target;
		try {
			target = UpgradeTarget.getUpgradeTarget(upgradeFormat);
		}
		catch(final IllegalArgumentException e) {
			throw new UpgradeException(
				"Formato de mejora desconocido ('" + upgradeFormat + "'): " + e, e //$NON-NLS-1$ //$NON-NLS-2$
			);
		}
		switch(target) {
			case T_FORMAT:
				if (new AOXAdESSigner().isSign(signature)) {
					try {
						return new UpgradeResult(
							XAdESTspUtil.timestampXAdES(signature, TSA_CFG),
							upgradeFormat
						);
					}
					catch (final AOException e) {
						throw new UpgradeException(
							"Error mejorando la firma a XAdES-T: " + e, e //$NON-NLS-1$
						);
					}
				}
				if (new AOCAdESSigner().isSign(signature)) {
					try {
						try {
							return new UpgradeResult(
								new CMSTimestamper(
									new TsaParams(TSA_CFG)
								).addTimestamp(
									signature,
									"SHA-256", //$NON-NLS-1$
									new GregorianCalendar()
								),
								upgradeFormat
							);
						}
						catch (NoSuchAlgorithmException | IOException e) {
							throw new UpgradeException(
								"Error mejorando la firma a XAdES-T: " + e, e //$NON-NLS-1$
							);
						}
					}
					catch (final AOException e) {
						throw new UpgradeException(
							"Error mejorando la firma a XAdES-T: " + e, e //$NON-NLS-1$
						);
					}
				}
				throw new UpgradeException(
					"Solo se soprotan firmas XAdES y CAdES" //$NON-NLS-1$
				);
			default:
				throw new UpgradeException(
					"Formato de mejora no soportado ('" + upgradeFormat + "')" //$NON-NLS-1$ //$NON-NLS-2$
				);
		}
	}

}
