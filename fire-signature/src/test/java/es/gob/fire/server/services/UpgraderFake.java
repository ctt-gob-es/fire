package es.gob.fire.server.services;

import java.util.Random;
import java.util.logging.Logger;

import es.gob.fire.upgrade.UpgradeResult;

/** Actualizador de firmas de mentira para pruebas.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
public final class UpgraderFake implements Upgrader {

	@Override
	public UpgradeResult upgradeSignature(final byte[] signature, final String upgradeFormat) throws UpgradeException {
		try {
			Thread.sleep(getRandomNumberInRange(2, 10));
		}
		catch (final InterruptedException e) {
			Logger.getLogger(UpgraderFake.class.getName()).warning(
				"Error en el retraso aleatorio: " + e //$NON-NLS-1$
			);
		}
		return new UpgradeResult(signature, upgradeFormat);
	}

	private static int getRandomNumberInRange(final int min, final int max) {
		if (min >= max) {
			throw new IllegalArgumentException("El maximo debe ser mayor que el minimo"); //$NON-NLS-1$
		}
		final Random r = new Random();
		return r.nextInt(max - min + 1) + min;
	}

}
