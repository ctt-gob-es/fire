package es.gob.fire.server.services.internal;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import es.gob.fire.alarms.Alarm;
import es.gob.fire.alarms.AlarmLevel;
import es.gob.fire.alarms.AlarmNotifier;
import es.gob.fire.alarms.InitializationException;
import es.gob.fire.signature.ConfigFileLoader;

/**
 * Gestor para la notificaci&oacute;n de alarmas.
 */
public class AlarmsManager {

	private static final Logger LOGGER = Logger.getLogger(AlarmsManager.class.getName());

	private static final String CONFIG_FILE = "alarms_config.properties"; //$NON-NLS-1$

	private static AlarmNotifier notifier;

	private static boolean initialized = false;

	/**
	 * Inicializa el gestor de alarmas.
	 * @param moduleName Nombre del m&oacute;dulo de FIRe.
	 * @param notifierClassname Nombre de la clase a trav&eacute;s de la que se
	 * notificar&aacute;n las alarmas.
	 */
	public static void init(final String moduleName, final String notifierClassname) {

		if (initialized) {
			return;
		}

		// Solo configuramos el administrador de alarmas si se ha configurado una clase para ello
		if (notifierClassname != null && !notifierClassname.isEmpty()) {
			// Inicializamos y configuramos el administrador
			try {
				final Class<?> notifierClass = Class.forName(notifierClassname);
				notifier = (AlarmNotifier) notifierClass.getConstructor().newInstance();

				final Properties config = ConfigFileLoader.loadConfigFile(CONFIG_FILE);
				notifier.init(config);
				notifier.setModule(moduleName);
			}
			catch (final IOException e) {
				LOGGER.log(Level.WARNING, "No se pudo cargar el fichero " + CONFIG_FILE //$NON-NLS-1$
						+ " con la configuracion para la notificacion de alarmas. " //$NON-NLS-1$
						+ "Debe agregar al directorio de ficheros de configuracion el fichero " //$NON-NLS-1$
						+ CONFIG_FILE, e);
				notifier = null;
			}
			catch (final InitializationException e) {
				LOGGER.log(Level.WARNING,
						"Ocurrio un error durante la inicializacion del notificador de errores" //$NON-NLS-1$
						+ CONFIG_FILE, e);
				notifier = null;
			}
			catch (final Throwable e) {
				LOGGER.log(Level.WARNING, "No se ha podido cargar el gestor de alarmas configurado", e); //$NON-NLS-1$
				notifier = null;
			}
		}

		initialized = true;
	}

	/**
	 * Notifica una alarma. Como nivel de alarma se utilizar&aacute; el
	 * predefinido seg&uacute;n el tipo de alarma.
	 * @param alarm Tipo de alarma que se desea notificar.
	 */
	public static void notify(final Alarm alarm) {
		notify(alarm, alarm.getDefaultLevel(), (String[]) null);
	}

	/**
	 * Notifica una alarma. Como nivel de alarma se utilizar&aacute; el
	 * predefinido seg&uacute;n el tipo de alarma.
	 * @param alarm Tipo de alarma que se desea notificar.
	 * @param resource Recurso al que se refiere la alarma. Puede ser nulo.
	 */
	public static void notify(final Alarm alarm, final String... resource) {
		notify(alarm, alarm.getDefaultLevel(), resource);
	}

	/**
	 * Notifica una alarma.
	 * @param alarm Tipo de alarma que se desea notificar.
	 * @param level Nivel de alarma.
	 */
	public static void notify(final Alarm alarm, final AlarmLevel level) {
		notify(alarm, level, (String[]) null);
	}

	/**
	 * Notifica una alarma.
	 * @param alarm Tipo de alarma que se desea notificar.
	 * @param level Nivel de alarma.
	 * @param resource Recursos a los que se refiere la alarma. Puede ser nulo.
	 */
	public static void notify(final Alarm alarm, final AlarmLevel level, final String... resource) {
		if (notifier != null) {
			try {
				notifier.notify(level, alarm, resource);
			} catch (final IOException e) {
				LOGGER.log(Level.WARNING, "No se ha podido enviar el error al gestor de notificaciones", e); //$NON-NLS-1$
			}
		}
	}
}
