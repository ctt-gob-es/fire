package es.gob.fire.upgrade;

import java.util.Properties;

/**
 * M&eacute;todos de utilidad para trabajar con las funciones de actualizacion y validaci&oacute;n de firmas.
 */
public class UpgraderUtils {

	private static final String PREFIX_UPDATER_PROPERTY = "updater.";  //$NON-NLS-1$

	/**
	 * Extrae de un {@code Properties} las propiedades de configuraci&oacute;n para la
	 * validaci&oacute;n o actualizaci&oacute;n de firma. Se tomar&aacute;n las propiedades
	 * que empiecen por el prefijo definido por {@code PREFIX_UPDATER_PROPERTY} y se les
	 * retirar&aacute; dicho pref&iacute;jo.
	 * propiedades pasan a estar en el {@code Properties} resultado y deja de estar en el
	 * {@code Properties} de entrada.
	 * @param properties Entrada de las que se extraer&aacute;n las propiedades.
	 * @return Propiedades extra&iacute;das o {@code null} si no se han definido propiedades.
	 */
	public static Properties extractUpdaterProperties(final Properties config) {

		Properties updateConfig = null;
		if (config != null) {
			for (final String key : config.keySet().toArray(new String[0])) {
				if (key.startsWith(PREFIX_UPDATER_PROPERTY)) {
					if (updateConfig == null) {
						updateConfig = new Properties();
					}
					updateConfig.setProperty(
							key.substring(PREFIX_UPDATER_PROPERTY.length()),
							config.getProperty(key));
					config.remove(key);
				}
			}
		}
		return updateConfig;
	}
}
