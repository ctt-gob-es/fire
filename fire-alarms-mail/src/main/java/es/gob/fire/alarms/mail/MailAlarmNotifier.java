package es.gob.fire.alarms.mail;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import es.gob.fire.alarms.Alarm;
import es.gob.fire.alarms.AlarmInternalMessages;
import es.gob.fire.alarms.AlarmLevel;
import es.gob.fire.alarms.AlarmNotification;
import es.gob.fire.alarms.AlarmNotifier;
import es.gob.fire.alarms.InitializationException;
import es.gob.fire.alarms.mail.task.CheckAlarmsScheduler;
import es.gob.fire.mail.MailSenderService;

/**
 * Notificador para el env&iacute;o de alarmas por e-mail.
 */
public class MailAlarmNotifier implements AlarmNotifier {

	static final Logger LOGGER = Logger.getLogger(MailAlarmNotifier.class.getName());

	/**
	 * Campo declarado con el nombre del modulo de FIRe. Este campo no se toma del
	 * fichero de propiedades, ya que este fichero pueden usarlo varios m&oacute;dulos.
	 * El valor se indica en la solicitud de notificaci&oacute;n.
	 */
	public static final String PROP_EXTRA_FIELD_MODULE = "module"; //$NON-NLS-1$

	/**
	 * Nombre de la propiedad de configuraci&oacute;n que determina los destinatarios
	 * de la alarma por correo.
	 */
	public static final String PROP_MAIL_RECIPIENTS = "mail.recipients"; //$NON-NLS-1$

	/**
	 * Nombre de la propiedad de configuraci&oacute;n que determina el limite de notificaciones CRITICAL
	 * para enviar un correo.
	 */
	public static final String PROP_MAIL_CRITICAL_NOTIFY_LIMIT = "mail.critical.notify.limit"; //$NON-NLS-1

	/**
	 * Nombre de la propiedad de configuraci&oacute;n que determina el limite de notificaciones ERROR
	 * para enviar un correo.
	 */
	public static final String PROP_MAIL_ERROR_NOTIFY_LIMIT = "mail.error.notify.limit"; //$NON-NLS-1$
	
	/**
	 * Nombre de la propiedad de configuraci&oacute;n que determina el limite de notificaciones WARNING
	 * para enviar un correo.
	 */
	public static final String PROP_MAIL_WARNING_NOTIFY_LIMIT = "mail.warning.notify.limit"; //$NON-NLS-1$
	
	/**
	 * Nombre de la propiedad de configuraci&oacute;n que determina el limite de notificaciones INFO
	 * para enviar un correo.
	 */
	public static final String PROP_MAIL_INFO_NOTIFY_LIMIT = "mail.info.notify.limit"; //$NON-NLS-1$

	/**
	 * Intervalo de tiempo en el que se comprobar&aacute; cuantas alarmas hay registradas.
	 */
	public static final String PROP_MAIL_NOTIFY_DELAY = "mail.notify.delay.time"; //$NON-NLS-1$

	/**
	 * Asunto del correo a enviar.
	 */
	public static final String PROP_MAIL_SUBJECT = "mail.subject"; //$NON-NLS-1$

	/**
	 * Entorno desde el que se notifica.
	 */
	public static final String PROP_MAIL_ENVIRONMENT = "mail.environment"; //$NON-NLS-1$

	/**
	 * Nombre del nodo desde el que se notifica.
	 */
	public static final String ENVIRONMENT_VAR_NODE_NAME = "fire.node.name"; //$NON-NLS-1$

	/**
	 * Intervalo de minutos por defecto para la ejecuci&oacute;n de la tarea.
	 */
	public static int DEFAULT_DELAY_TIME = 30;

	/**
	 * Fecha desde la que se comprueba que existan alarmas registradas.
	 */
	public static Date lastDateChecked;

	private static MailSenderService mailService;

	/**
	 * Nombre de la propiedad de configuraci&oacute;n se ha inicializado.
	 */
	public static boolean initialized = false;

	/**
	 * Nombre de la propiedad de configuraci&oacute;n que indica el limite de
	 * notificaciones CRITICAL para enviar un correo.
	 */
	public static int criticalNotifyLimits = 0;

	/**
	 * Nombre de la propiedad de configuraci&oacute;n que indica el limite de
	 * notificaciones ERROR para enviar un correo.
	 */
	public static int errorNotifyLimits = 0;

	/**
	 * Nombre de la propiedad de configuraci&oacute;n que indica el limite de
	 * notificaciones WARNING para enviar un correo.
	 */
	public static int warningNotifyLimits = 0;

	/**
	 * Nombre de la propiedad de configuraci&oacute;n que indica el limite de
	 * notificaciones INFO para enviar un correo.
	 */
	public static int infoNotifyLimits = 0;

	/**
	 * Intentos de notificacion de alertas CRITICAL.
	 */
	public static int criticalNotifyAttemps = 0;

	/**
	 * Intentos de notificacion de alertas ERROR.
	 */
	public static int errorNotifyAttemps = 0;

	/**
	 * Intentos de notificacion de alertas WARNING.
	 */
	public static int warningNotifyAttemps = 0;

	/**
	 * Intentos de notificacion de alertas INFO.
	 */
	public static int infoNotifyAttemps = 0;

	/**
	 * Configuraci&oacute;n adicional com&uacute;n a todas las alarmas.
	 */
	private static Properties config;

	/**
	 * Mapa con alarmas registradas donde la clave es el mensaje, ya que es el que diferencia una alarma de otra.
	 */
	private static Map<String, AlarmNotification> alarmsRegistered = new HashMap<>();

	@Override
	public void init(final Properties clientConfig) throws InitializationException {

		if (!initialized) {

			if (clientConfig == null) {
				throw new InitializationException("No se ha proporcionado la configuracion del notificador"); //$NON-NLS-1$
			}

			config = initConfig(clientConfig);

			mailService = new MailSenderService();
			mailService.init(clientConfig);

			if (config.containsKey(PROP_MAIL_CRITICAL_NOTIFY_LIMIT)) {
				final String val = config.getProperty(PROP_MAIL_CRITICAL_NOTIFY_LIMIT, ""); //$NON-NLS-1$
				if (!val.isEmpty()) {
					criticalNotifyLimits = Integer.valueOf(val);
				}
			}
			if (config.containsKey(PROP_MAIL_ERROR_NOTIFY_LIMIT)) {
				final String val = config.getProperty(PROP_MAIL_ERROR_NOTIFY_LIMIT, ""); //$NON-NLS-1$
				if (!val.isEmpty()) {
					errorNotifyLimits = Integer.valueOf(val);
				}
			}
			if (config.containsKey(PROP_MAIL_WARNING_NOTIFY_LIMIT)) {
				final String val = config.getProperty(PROP_MAIL_WARNING_NOTIFY_LIMIT, ""); //$NON-NLS-1$
				if (!val.isEmpty()) {
					warningNotifyLimits = Integer.valueOf(val);
				}
			}
			if (config.containsKey(PROP_MAIL_INFO_NOTIFY_LIMIT)) {
				final String val = config.getProperty(PROP_MAIL_INFO_NOTIFY_LIMIT, ""); //$NON-NLS-1$
				if (!val.isEmpty()) {
					infoNotifyLimits = Integer.valueOf(val);
				}
			}

			initialized = true;

			// Se programa una tarea cada X tiempo que detectara si se debe de enviar un resumen de las alarmas enviadas por
			// correo o no.
			final String delayString = config.getProperty(PROP_MAIL_NOTIFY_DELAY);
			int delay;
			try {
				delay = delayString != null ? Integer.parseInt(delayString) : DEFAULT_DELAY_TIME;
			}
			catch (final Exception e) {
				delay = DEFAULT_DELAY_TIME;
			}
			new CheckAlarmsScheduler(delay).scheduleTask();
		}
	}

	/**
	 * Carga de las propiedades de entrada aquellas que deben trasmitirse con cada alerta.
	 * @param clientConfig Configuraci&oacute;n de entrada.
	 * @return Configuraci&oacute;n de las alertas.
	 */
	private static Properties initConfig(final Properties clientConfig) {
		final Properties configuration = new Properties();
		if (clientConfig != null && !clientConfig.isEmpty()) {
			final Set<Object> keySet = clientConfig.keySet();
			for (final Object key: keySet) {
				final String keyString = (String) key;
				final String value = clientConfig.getProperty(keyString);
				configuration.setProperty(keyString, value);
			}
		}
		return configuration;
	}

	@Override
	public void setModule(final String module) {
		config.setProperty(PROP_EXTRA_FIELD_MODULE, module);
	}

	@Override
	public void notify(final AlarmLevel level, final Alarm alarm, final String... source)
			throws IOException {

		String message;
		if (source == null) {
			message = alarm.getDescription();
		} else {
			message = alarm.formatDescription((Object[]) source);
		}

		switch(level) {
			case CRITICAL:
				criticalNotifyAttemps++;
				break;
			case ERROR:
				errorNotifyAttemps++;
				break;
			case WARNING:
				warningNotifyAttemps++;
				break;
			case INFO:
				infoNotifyAttemps++;
				break;
			default:
				break;			
		}
		
		if (alarmsRegistered.containsKey(message)) {
			alarmsRegistered.get(message).addNotification();
		} else {
			final AlarmNotification notification = new AlarmNotification(alarm, level);
			notification.addNotification();
			alarmsRegistered.put(message, notification);
		}
	}

	public static void sendSummary() {
		final String recipients = config.getProperty(PROP_MAIL_RECIPIENTS);
		final String [] addresses = recipients.split(","); //$NON-NLS-1$
		final InternetAddress [] emails = new InternetAddress [addresses.length];
		for (int i = 0; i < addresses.length ; i++) {
			try {
				emails[i] = new InternetAddress(addresses[i]);
			} catch (final AddressException e) {
				LOGGER.log(Level.WARNING, "Error al instanciar direccion de correo electronico", e); //$NON-NLS-1$
			}
		}
		final StringBuilder bodySubject = buildBodySubject();
		LOGGER.log(Level.INFO, "Se procedera a enviar una notificacion de alarma por correo electronico"); //$NON-NLS-1$
		mailService.sendEmail(emails, config.getProperty(PROP_MAIL_SUBJECT, AlarmInternalMessages.getString("Alarm.8")), bodySubject, "Notificacion de alarma enviada", MailSenderService.MAIL_TEXT_HTML_CHARSET); //$NON-NLS-1$ //$NON-NLS-2$
		alarmsRegistered.clear();
		criticalNotifyAttemps = 0;
		errorNotifyAttemps = 0;
		warningNotifyAttemps = 0;
		infoNotifyAttemps = 0;
	}

	private static StringBuilder buildBodySubject() {
		final StringBuilder bodySubject = new StringBuilder();
		final String nodeName = System.getProperty(ENVIRONMENT_VAR_NODE_NAME);
		final String envMsg = config.getProperty(PROP_MAIL_ENVIRONMENT);
		final Date actualDate = new Date();
		final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy"); //$NON-NLS-1$
		final SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm");		 //$NON-NLS-1$
		// Se imprime un mensaje de introduccion u otro depende de las propiedades indicadas
		if (nodeName != null && !nodeName.isEmpty() && envMsg != null && !envMsg.isEmpty()) {
			bodySubject.append(AlarmInternalMessages.getString("AlarmBodySubject.1",  //$NON-NLS-1$
																hourFormat.format(lastDateChecked),
																hourFormat.format(actualDate),
																dateFormat.format(actualDate),
																nodeName,
																envMsg));
		} else if (nodeName != null && !nodeName.isEmpty()) {
			bodySubject.append(AlarmInternalMessages.getString("AlarmBodySubject.2",  //$NON-NLS-1$
																hourFormat.format(lastDateChecked),
																hourFormat.format(actualDate),
																dateFormat.format(actualDate),
																nodeName));
		} else if (envMsg != null && !envMsg.isEmpty()) {
			bodySubject.append(AlarmInternalMessages.getString("AlarmBodySubject.3",  //$NON-NLS-1$
																hourFormat.format(lastDateChecked),
																hourFormat.format(actualDate),
																dateFormat.format(actualDate),
																envMsg));
		} else {
			bodySubject.append(AlarmInternalMessages.getString("AlarmBodySubject.0",  //$NON-NLS-1$
																hourFormat.format(lastDateChecked),
																hourFormat.format(actualDate),
																dateFormat.format(actualDate)));
		}

		bodySubject.append("\n\n"); //$NON-NLS-1$

		bodySubject.append("<ul>"); //$NON-NLS-1$
		// Se imprimen las alarmas registradas y sus notificaciones
        for (final Map.Entry<String, AlarmNotification> entry : alarmsRegistered.entrySet()) {
            final String key = entry.getKey(); //
            final AlarmNotification value = entry.getValue();
            bodySubject.append("<li>"); //$NON-NLS-1$
            if (value.getNotifications() > 1) {
            	bodySubject.append(key + " " + AlarmInternalMessages.getString("AlarmBodySubject.5",  //$NON-NLS-1$ //$NON-NLS-2$
            									String.valueOf(value.getNotifications())));
            } else {
            	bodySubject.append(key + " " + AlarmInternalMessages.getString("AlarmBodySubject.4")); //$NON-NLS-1$ //$NON-NLS-2$
            }
            bodySubject.append("</li>"); //$NON-NLS-1$
            bodySubject.append("\n"); //$NON-NLS-1$
        }
        bodySubject.append("</ul>"); //$NON-NLS-1$

		return bodySubject;
	}
}
