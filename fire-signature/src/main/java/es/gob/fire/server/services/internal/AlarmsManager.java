package es.gob.fire.server.services.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import es.gob.fire.alarms.Alarm;
import es.gob.fire.alarms.AlarmNotifier;
import es.gob.fire.alarms.InitializationException;
import es.gob.fire.signature.ConfigFileLoader;
import es.gob.fire.signature.ConfigManager;

/**
 * Gestor para la notificaci&oacute;n de alarmas.
 */
public class AlarmsManager {

	private static final Logger LOGGER = Logger.getLogger(AlarmsManager.class.getName());

	private static final String OLD_CONFIG_FILE = "alarms_config.properties"; //$NON-NLS-1$

	private static final String PARAM_ALARM_NOTIFIER_SEPARATOR = ","; //$NON-NLS-1$

	private static ArrayList<AlarmNotifier> notifiersList;

	private static boolean initialized = false;

	/**
	 * Inicializa el gestor de alarmas.
	 * @param moduleName Nombre del m&oacute;dulo de FIRe.
	 * @param notifierName Nombre o nombres de los notificadore a trav&eacute;s de las que se
	 * notificar&aacute;n las alarmas.
	 */
	public static void init(final String moduleName, final String notifierName) {

		if (initialized) {
			return;
		}

		notifiersList = new ArrayList<>();

		// Solo configuramos el administrador de alarmas si se ha configurado una clase para ello
		if (notifierName != null && !notifierName.isEmpty()) {
			final String [] notifierNames = notifierName.split(PARAM_ALARM_NOTIFIER_SEPARATOR);

			for (final String notifName : notifierNames) {
				if (!notifName.isEmpty()) {
					initNotifier(moduleName, notifName);
					}
				}

			// Si no se han conseguido inicializar los notificadores con sus propios archivos de configuracion,
			// se intentara cargar del modo antiguo, en donde el nombre de la configuracion seria en realidad la
			// clase del notificador y la configuracion se carga de un fichero de configuracion concreto
			if (notifiersList.isEmpty()) {
				initOldNotifier(moduleName, notifierName);
			}

		initialized = true;
	}

	/**
	 * Inicializa un notificador cargando la configuraci&oacute;n de su propio fichero.
	 * @param moduleName Nombre del m&oacute;dulo (componente central, administracion, etc).
	 * @param notifierName Nombre del notificador.
	 */
	private static void initNotifier(final String moduleName, final String notifierName) {

		final String configFilename = "alarms_" + notifierName + ".properties"; //$NON-NLS-1$ //$NON-NLS-2$
		final String className = ConfigManager.getNotifierClassName(notifierName);
		if (className == null) {
			LOGGER.warning("No se ha configurado la clase asociada al gestor de alarmas " + notifierName + ". No se cargara"); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
		initNotifier(moduleName, className, configFilename);
		}

	/**
	 * Inicializa un notificador cargando la configuraci&oacute;n del antiguo fichero general de alarmas.
	 * @param moduleName Nombre del m&oacute;dulo (componente central, administracion, etc).
	 * @param notifierClassname Nombre de la clase que implemena el notificador.
	 */
	private static void initOldNotifier(final String moduleName, final String notifierClassname) {
		initNotifier(moduleName, notifierClassname, OLD_CONFIG_FILE);
	}

	/**
	 * Inicializa un notificador.
	 * @param moduleName Nombre del m&oacute;dulo (componente central, administracion, etc).
	 * @param className Nombre de la clase que implementa el notificador.
	 * @param configFilename Nombre del fichero con la configuraci&oacute;n del notificador.
	 */
	private static void initNotifier(final String moduleName, final String className, final String configFilename) {

		try {
			final Class<?> notifierClass = Class.forName(className);
			final AlarmNotifier notifier = (AlarmNotifier) notifierClass.getConstructor().newInstance();
			final Properties config = ConfigFileLoader.loadConfigFile(configFilename);
			notifier.init(config);
			notifier.setModule(moduleName);
			notifiersList.add(notifier);
		}
		catch (final IOException e) {
			LOGGER.log(Level.WARNING, "No se pudo cargar el fichero " + configFilename //$NON-NLS-1$
					+ " con la configuracion para la notificacion de alarmas. " //$NON-NLS-1$
					+ "Debe agregarlo al directorio de ficheros de configuracion", e);  //$NON-NLS-1$
		}
		catch (final InitializationException e) {
			LOGGER.log(Level.WARNING,
					"Ocurrio un error durante la inicializacion del notificador de errores con el fichero: " //$NON-NLS-1$
					+ configFilename, e);
		}
		catch (final Throwable e) {
			LOGGER.log(Level.WARNING, "No se ha podido cargar el gestor de alarmas con la clase configurada: " + className, e); //$NON-NLS-1$
		}
	}

	/**
	 * Notifica una alarma. Como nivel de alarma se utilizar&aacute; el
	 * predefinido seg&uacute;n el tipo de alarma.
	 * @param alarm Tipo de alarma que se desea notificar.
	 */
	public static void notify(final Alarm alarm) {
		notify(alarm, (String[]) null);
	}

	/**
	 * Notifica una alarma. Como nivel de alarma se utilizar&aacute; el
	 * predefinido seg&uacute;n el tipo de alarma.
	 * @param alarm Tipo de alarma que se desea notificar.
	 * @param resource Recurso al que se refiere la alarma. Puede ser nulo.
	 */
	public static void notify(final Alarm alarm, final String... resource) {
		if (notifiersList != null && !notifiersList.isEmpty()) {
			for (final AlarmNotifier notifier : notifiersList) {
				try {

					LOGGER.log(Level.INFO, " ====++++==== Enviamos al notificador " + notifier + " la alarma " + alarm); //$NON-NLS-1$

					notifier.notify(alarm.getDefaultLevel(), alarm, resource);
			} catch (final IOException e) {
					LOGGER.log(Level.WARNING, "No se ha podido enviar el error al gestor de notificaciones", e); //$NON-NLS-1$
			}
		}
			}
		}

	public static void destroy() {

		if (!initialized) {
			return;
			}

		for (final AlarmNotifier notifier : notifiersList) {
			notifier.destroy();
		}

		initialized = false;
	}
}
