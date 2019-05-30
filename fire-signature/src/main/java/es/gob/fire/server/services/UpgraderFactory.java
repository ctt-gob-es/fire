package es.gob.fire.server.services;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

import es.gob.fire.signature.ConfigManager;

/** Factor&iacute;a de mejoradores de firma.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
public final class UpgraderFactory {

	private static Upgrader upgrader = null;

	private UpgraderFactory() {
		// No instanciable
	}

	/** Obtiene el mejorador de firmas configurado.
	 * @return Mejorador de firmas configurado. */
	public static Upgrader getUpgrader() {
		if (upgrader == null) {
			try {
				upgrader = (Upgrader) Class.forName(ConfigManager.getUpgraderClassName()).getConstructor().newInstance();
			}
			catch (final InstantiationException    |
				         IllegalAccessException    |
				         IllegalArgumentException  |
				         InvocationTargetException |
				         NoSuchMethodException     |
				         SecurityException         |
				         ClassNotFoundException e) {
				throw new IllegalArgumentException(
					"No se ha podido instanciar el mejorador de firmas configurado (" +  ConfigManager.getUpgraderClassName() + "): " + e //$NON-NLS-1$ //$NON-NLS-2$
				);
			}
			Logger.getLogger(UpgraderFactory.class.getName()).info(
				"Se usara '" + upgrader.getClass().getName() + "' como mejorador de firmas" //$NON-NLS-1$ //$NON-NLS-2$
			);
		}
		return upgrader;
	}

}
