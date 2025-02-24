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
	
	private static final String GRAYLOG_CONFIG_FILE = "alarms_graylog_config.properties"; //$NON-NLS-1$
	
	private static final String EVENTMANAGER_CONFIG_FILE = "alarms_eventmanager_config.properties"; //$NON-NLS-1$
	
	private static final String ALARM_GRAYLOG_CLASS = "es.gob.fire.alarms.graylog.GrayLogAlarmNotifier"; //$NON-NLS-1$
	
	private static final String ALARM_EVENTMANAGER_CLASS = "es.gob.fire.alarms.eventmanager.EventManagerAlarmNotifier"; //$NON-NLS-1$
	
	private static final String PARAM_ALARM_NOTIFIER_SEPARATOR = ","; //$NON-NLS-1$

	private static AlarmNotifier graylogNotifier;
	
	private static AlarmNotifier eventManagerNotifier;
	
	private static AlarmNotifier notifier;

	private static boolean initialized = false;

	/**
	 * Inicializa el gestor de alarmas.
	 * @param moduleName Nombre del m&oacute;dulo de FIRe.
	 * @param notifierClassname Nombre o nombres de las clases a trav&eacute;s de las que se
	 * notificar&aacute;n las alarmas.
	 */
	public static void init(final String moduleName, final String notifierClassname) {

		if (initialized) {
			return;
		}
		
		// Solo configuramos el administrador de alarmas si se ha configurado una clase para ello
		if (notifierClassname != null && !notifierClassname.isEmpty()) {
			final String [] notifierNames = notifierClassname.split(PARAM_ALARM_NOTIFIER_SEPARATOR);
		
			for (final String className : notifierNames) {
				if (!notifierClassname.isEmpty()) {
					if (ALARM_GRAYLOG_CLASS.equals(className)) {
						initGraylogNotifier(moduleName, className);
					} else if (ALARM_EVENTMANAGER_CLASS.equals(className)) {
						initEventManagerNotifier(moduleName, className);
					}
				}
			}
		}
		
		// Si no se han conseguio inicializar los notificadores con sus propios archivos de configuracion,
		// se intentara inicializar el notificador mediante el archivo de configuracion antiguo
		if (graylogNotifier == null && eventManagerNotifier == null) {
			initOldNotifier(moduleName, notifierClassname);
		}

		initialized = true;
	}
	
	private static void initGraylogNotifier(final String moduleName, final String notifierClassname) {
		
		try {
			final Class<?> notifierClass = Class.forName(notifierClassname);
			graylogNotifier = (AlarmNotifier) notifierClass.getConstructor().newInstance();
			final Properties config = ConfigFileLoader.loadConfigFile(GRAYLOG_CONFIG_FILE);
			graylogNotifier.init(config);
			graylogNotifier.setModule(moduleName);
		}
		catch (final IOException e) {
			LOGGER.log(Level.WARNING, "No se pudo cargar el fichero " + GRAYLOG_CONFIG_FILE //$NON-NLS-1$
					+ " con la configuracion para la notificacion de alarmas. " //$NON-NLS-1$
					+ "Debe agregar al directorio de ficheros de configuracion el fichero " //$NON-NLS-1$
					+ GRAYLOG_CONFIG_FILE, e);
			graylogNotifier = null;
		}
		catch (final InitializationException e) {
			LOGGER.log(Level.WARNING,
					"Ocurrio un error durante la inicializacion del notificador de errores" //$NON-NLS-1$
					+ GRAYLOG_CONFIG_FILE, e);
			graylogNotifier = null;
		}
		catch (final Throwable e) {
			LOGGER.log(Level.WARNING, "No se ha podido cargar el gestor de alarmas configurado", e); //$NON-NLS-1$
			graylogNotifier = null;
		}
	}
	
	private static void initEventManagerNotifier(final String moduleName, final String notifierClassname) {
		
		try {
			final Class<?> notifierClass = Class.forName(notifierClassname);
			eventManagerNotifier = (AlarmNotifier) notifierClass.getConstructor().newInstance();
			final Properties config = ConfigFileLoader.loadConfigFile(EVENTMANAGER_CONFIG_FILE);
			eventManagerNotifier.init(config);
			eventManagerNotifier.setModule(moduleName);
		}		
		catch (final IOException e) {
			LOGGER.log(Level.WARNING, "No se pudo cargar el fichero " + EVENTMANAGER_CONFIG_FILE //$NON-NLS-1$
					+ " con la configuracion para la notificacion de alarmas. " //$NON-NLS-1$
					+ "Debe agregar al directorio de ficheros de configuracion el fichero " //$NON-NLS-1$
					+ EVENTMANAGER_CONFIG_FILE, e);
			eventManagerNotifier = null;
		}
		catch (final InitializationException e) {
			LOGGER.log(Level.WARNING,
					"Ocurrio un error durante la inicializacion del notificador de errores" //$NON-NLS-1$
					+ EVENTMANAGER_CONFIG_FILE, e);
			eventManagerNotifier = null;
		}
		catch (final Throwable e) {
			LOGGER.log(Level.WARNING, "No se ha podido cargar el gestor de alarmas configurado", e); //$NON-NLS-1$
			eventManagerNotifier = null;
		}
	}
	
	private static void initOldNotifier(final String moduleName, final String notifierClassname) {
		
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
		if (graylogNotifier != null) {
			try {
				graylogNotifier.notify(level, alarm, resource);
			} catch (final IOException e) {
				LOGGER.log(Level.WARNING, "No se ha podido enviar el error al gestor de notificaciones de Graylog", e); //$NON-NLS-1$
			}
		}
		if (eventManagerNotifier != null) {
			try {
				eventManagerNotifier.notify(level, alarm, resource);
			} catch (final IOException e) {
				LOGGER.log(Level.WARNING, "No se ha podido enviar el error al gestor de notificaciones de EventManager", e); //$NON-NLS-1$
			}
		}
		if (notifier != null) {
			try {
				notifier.notify(level, alarm, resource);
			} catch (final IOException e) {
				LOGGER.log(Level.WARNING, "No se ha podido enviar el error al gestor de notificaciones", e); //$NON-NLS-1$
			}
		}
	}
}
