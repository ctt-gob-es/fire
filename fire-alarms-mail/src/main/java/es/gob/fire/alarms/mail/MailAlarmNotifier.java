package es.gob.fire.alarms.mail;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
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
import es.gob.fire.signature.DbManager;
import es.gob.fire.utils.ConstantsMail;

/**
 * Notificador para el env&iacute;o de alarmas por e-mail.
 */
public class MailAlarmNotifier implements AlarmNotifier {

	static final Logger LOGGER = Logger.getLogger(MailAlarmNotifier.class.getName());

	/**
	 * Consulta SQL para recuperar las direcciones de correo de los usuarios registrados en BD
	 */
	private static final String SQL_SELECT_MAIL_RECIPIENTS = "SELECT u.CORREO_ELEC FROM TB_USUARIOS u JOIN TB_ROLES r ON u.FK_ROL = r.ID WHERE r.NOMBRE_ROL = 'admin'"; //$NON-NLS-1$

	/**
	 * Intervalo de minutos por defecto para la ejecuci&oacute;n de la tarea.
	 */
	public static final int DEFAULT_DELAY_TIME = 30;
	
	/**
	 * Campo declarado con el nombre del modulo de FIRe. Este campo no se toma del
	 * fichero de propiedades, ya que este fichero pueden usarlo varios m&oacute;dulos.
	 * El valor se indica en la solicitud de notificaci&oacute;n.
	 */
	public static final String PROP_EXTRA_FIELD_MODULE = "module";
	
	/**
	 * Nombre del nodo desde el que se notifica.
	 */
	public static final String ENVIRONMENT_VAR_NODE_NAME = "fire.node.name";
	
	/**
	 * Servicio que se encarga del envio de correos.
	 */
	private static MailSenderService mailService;
	
	/**
	 * Mapa con alarmas registradas donde la clave es el mensaje, ya que es el que diferencia una alarma de otra.
	 */
	private static Map<String, AlarmNotification> alarmsRegistered = new HashMap<>();
	
	/**
	 * Configuraci&oacute;n adicional com&uacute;n a todas las alarmas.
	 */
	private static Properties config;
	
	/**
	 * Fecha desde la que se comprueba que existan alarmas registradas.
	 */
	public static Date lastDateChecked;
	
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
	public static int criticalNotifyAttempts = 0;

	/**
	 * Intentos de notificacion de alertas ERROR.
	 */
	public static int errorNotifyAttempts = 0;

	/**
	 * Intentos de notificacion de alertas WARNING.
	 */
	public static int warningNotifyAttempts = 0;

	/**
	 * Intentos de notificacion de alertas INFO.
	 */
	public static int infoNotifyAttempts = 0;
	
	@Override
	public void init(final Properties clientConfig) throws InitializationException {
		if (initialized) {
			return;
		}

		if (clientConfig == null) {
			throw new InitializationException("No se ha proporcionado la configuracion del notificador");
		}

		config = initializeConfig(clientConfig);

		mailService = new MailSenderService();
		mailService.init(clientConfig);

		criticalNotifyLimits = getIntProperty(ConstantsMail.MAIL_CRITICAL_NOTIFY_LIMIT);
		errorNotifyLimits = getIntProperty(ConstantsMail.MAIL_ERROR_NOTIFY_LIMIT);
		warningNotifyLimits = getIntProperty(ConstantsMail.MAIL_WARNING_NOTIFY_LIMIT);
		infoNotifyLimits = getIntProperty(ConstantsMail.MAIL_INFO_NOTIFY_LIMIT);

		initialized = true;

		final int delay = Optional.ofNullable(config.getProperty(ConstantsMail.MAIL_NOTIFY_DELAY))
				.map(Integer::parseInt)
				.orElse(DEFAULT_DELAY_TIME);

		new CheckAlarmsScheduler(delay).scheduleTask();
	}

	@Override
	public void setModule(String module) {
		config.setProperty(PROP_EXTRA_FIELD_MODULE, module);
	}

	@Override
	public void notify(AlarmLevel level, Alarm alarm, String... source) throws IOException {
		String message = (source == null) ? alarm.getDescription() : alarm.formatDescription((Object[]) source);

		switch (level) {
			case CRITICAL: criticalNotifyAttempts++; break;
			case ERROR: errorNotifyAttempts++; break;
			case WARNING: warningNotifyAttempts++; break;
			case INFO: infoNotifyAttempts++; break;
			default: break;
		}

		alarmsRegistered.compute(message, (k, existingNotification) -> {
			if (existingNotification == null) {
				AlarmNotification newNotification = new AlarmNotification(alarm, level);
				newNotification.addNotification();
				return newNotification;
			}
			existingNotification.addNotification();
			return existingNotification;
		});
	}
	
	/**
	 * Carga de las propiedades de entrada aquellas que deben trasmitirse con cada alerta.
	 * @param clientConfig Configuraci&oacute;n de entrada.
	 * @return Configuraci&oacute;n de las alertas.
	 */
	private Properties initializeConfig(Properties clientConfig) {
		Properties configuration = new Properties();

		if (clientConfig != null && !clientConfig.isEmpty()) {
			clientConfig.forEach((key, value) -> configuration.setProperty((String) key, (String) value));
		}

		return configuration;
	}
	
	/**
	 * Envia el resumen de las alarmas almacenadas en la cola.
	 */
	public static void sendSummary() {
		// Construimos el cuerpo del mensaje
		final StringBuilder bodySubject = buildBodySubject();
		
		final String [] addresses;
		// En caso de que se haya notificado una alarma de problema de conexion con la BD, se obtiene directamente las direcciones desde la propiedad
		if (alarmsRegistered.containsKey(Alarm.CONNECTION_DB.getDescription())) {
			addresses = getMailRecipientsFromProperty();
		} else {
			addresses = getMailRecipients();
		}
		
		final InternetAddress [] emails = new InternetAddress [addresses.length];
		
		for (int i = 0; i < addresses.length ; i++) {
			try {
				emails[i] = new InternetAddress(addresses[i]);
			} catch (final AddressException e) {
				LOGGER.log(Level.WARNING, "Error al instanciar direccion de correo electronico", e); //$NON-NLS-1$
			}
		}
		
		// Se envia la alarma
		LOGGER.log(Level.INFO, "Se procedera a enviar una notificacion de alarma por correo electronico"); //$NON-NLS-1$
		mailService.sendEmail(emails, config.getProperty(ConstantsMail.MAIL_SUBJECT, AlarmInternalMessages.getString("Alarm.8")), bodySubject, "Notificacion de alarma enviada", MailSenderService.MAIL_TEXT_HTML_CHARSET); //$NON-NLS-1$ //$NON-NLS-2$
		
		// Se limpia las alarmas registradas y los intentos
		alarmsRegistered.clear();
		
		criticalNotifyAttempts = 0;
		errorNotifyAttempts = 0;
		warningNotifyAttempts = 0;
		infoNotifyAttempts = 0;
	}
	
	private static StringBuilder buildBodySubject() {
		StringBuilder bodySubject = new StringBuilder();

		String nodeName = System.getProperty(ENVIRONMENT_VAR_NODE_NAME);
		String envMsg = config != null ? config.getProperty(ConstantsMail.MAIL_ENVIRONMENT) : null;

		Date now = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm");

		String startHour = "??:??";
		String endHour = hourFormat.format(now);
		String date = dateFormat.format(now);

		if (lastDateChecked != null) {
			startHour = hourFormat.format(lastDateChecked);
		}

		String introMsg = "";

		if (nodeName != null && !nodeName.isEmpty() && envMsg != null && !envMsg.isEmpty()) {
			introMsg = AlarmInternalMessages.getString("AlarmBodySubject.1",
					startHour,
					endHour,
					date,
					nodeName,
					envMsg);
		} else if (nodeName != null && !nodeName.isEmpty()) {
			introMsg = AlarmInternalMessages.getString("AlarmBodySubject.2",
					startHour,
					endHour,
					date,
					nodeName);
		} else if (envMsg != null && !envMsg.isEmpty()) {
			introMsg = AlarmInternalMessages.getString("AlarmBodySubject.3",
					startHour,
					endHour,
					date,
					envMsg);
		} else {
			introMsg = AlarmInternalMessages.getString("AlarmBodySubject.0",
					startHour,
					endHour,
					date);
		}

		bodySubject.append(introMsg).append("\n\n<ul>\n");

		if (alarmsRegistered != null) {
			for (Map.Entry<String, AlarmNotification> entry : alarmsRegistered.entrySet()) {
				String message = entry.getKey();
				AlarmNotification notif = entry.getValue();

				bodySubject.append("<li>");
				if (notif != null && notif.getNotifications() > 1) {
					bodySubject.append(message).append(" ")
							.append(AlarmInternalMessages.getString("AlarmBodySubject.5", String.valueOf(notif.getNotifications())));
				} else {
					bodySubject.append(message).append(" ")
							.append(AlarmInternalMessages.getString("AlarmBodySubject.4"));
				}
				bodySubject.append("</li>\n");
			}
		}

		bodySubject.append("</ul>");
		return bodySubject;
	}
	
	private static String[] getMailRecipients() {
		List<String> emailList = new ArrayList<>();

		try (Connection conn = DbManager.getConnection();
		     PreparedStatement st = conn.prepareStatement(SQL_SELECT_MAIL_RECIPIENTS);
		     ResultSet rs = st.executeQuery()) {
			
			while (rs.next()) {
				String mail = rs.getString(1);
				if (mail != null && !mail.isEmpty()) {
					emailList.add(mail.trim());
				}
			}
		}
		catch (Exception e) {
			// En caso de error en la BD, volvemos a los correos por propiedad
			LOGGER.log(Level.WARNING, "Error cargando direcciones de correo desde BD. Se usara la configuracion del properties.", e); //$NON-NLS-1$
			return getMailRecipientsFromProperty();
		}

		return emailList.toArray(new String[0]);
	}
	
	private static String[] getMailRecipientsFromProperty() {
		final String fallbackRecipients = config.getProperty(ConstantsMail.MAIL_RECIPIENTS);
		
		if (fallbackRecipients != null) {
			return fallbackRecipients.split(","); //$NON-NLS-1$
		}
		
		return new String[0];
	}
	
	private int getIntProperty(String key) {
		String value = config.getProperty(key);
		if (value != null && !value.isEmpty()) {
			try {
				return Integer.parseInt(value);
			} catch (NumberFormatException e) {
				// Se ignora y se retorna el valor por defecto
			}
		}
		return 0;
	}
}
