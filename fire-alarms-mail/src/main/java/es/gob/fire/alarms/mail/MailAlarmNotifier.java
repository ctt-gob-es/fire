package es.gob.fire.alarms.mail;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import es.gob.fire.alarms.Alarm;
import es.gob.fire.alarms.AlarmLevel;
import es.gob.fire.alarms.AlarmNotifier;
import es.gob.fire.alarms.InitializationException;
import es.gob.fire.alarms.mail.task.AlarmSummarySendingScheduler;
import es.gob.fire.mail.ConstantsMail;
import es.gob.fire.mail.MailSenderService;
import es.gob.fire.signature.DbManager;
import es.gob.fire.signature.LogUtils;

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
	 * Periodo de tiempo en milisegundos por defecto dentro del cual deben producirse el n&uacute;mero
	 * de alarmas indicado de un tipo para enviar el correo.
	 */
	private static final long DEFAULT_NOTIFICATION_PERIOD = 1800000;

	/**
	 * Tiempo de milisegundos que por defecto se esperar&aacute; despu&eacute;s de enviar un correo
	 * antes de enviar un correo resumen.
	 */
	private static final long DEFAULT_NOTIFICATION_DELAY = 1800000;

	/**
	 * Entorno desde el que se notifica.
	 */
	public static final String MAIL_ENVIRONMENT = "mail.environment"; //$NON-NLS-1$

	/**
	 * Nombre de la propiedad de configuraci&oacute;n que determina el limite de notificaciones CRITICAL
	 * para enviar un correo.
	 */
	public static final String NOTIFICATION_CRITICAL_LIMIT = "notification.critical.limit"; //$NON-NLS-1$

	/**
	 * Nombre de la propiedad de configuraci&oacute;n que determina el limite de notificaciones ERROR
	 * para enviar un correo.
	 */
	public static final String NOTIFICATION_ERROR_LIMIT = "notification.error.limit"; //$NON-NLS-1$

	/**
	 * Nombre de la propiedad de configuraci&oacute;n que determina el limite de notificaciones WARNING
	 * para enviar un correo.
	 */
	public static final String NOTIFICATION_WARNING_LIMIT = "notification.warning.limit"; //$NON-NLS-1$

	/**
	 * Nombre de la propiedad de configuraci&oacute;n que determina el limite de notificaciones INFO
	 * para enviar un correo.
	 */
	public static final String NOTIFICATION_INFO_LIMIT = "notification.info.limit"; //$NON-NLS-1$

	/**
	 * Intervalo de tiempo (en minutos) en el que se comprobar&aacute; cuantas alarmas hay registradas.
	 */
	public static final String NOTIFICATION_PERIOD = "notification.period"; //$NON-NLS-1$

	/**
	 * Intervalo de tiempo de espera (en minutos) en el que se acumular&aacute;n notificaciones para generar
	 * un resumen.
	 */
	public static final String NOTIFICATION_DELAY = "notification.delay"; //$NON-NLS-1$

	/**
	 * Campo declarado con el nombre del modulo de FIRe. Este campo no se toma del
	 * fichero de propiedades, ya que este fichero pueden usarlo varios m&oacute;dulos.
	 * El valor se indica en la solicitud de notificaci&oacute;n.
	 */
	public static final String PROP_EXTRA_FIELD_MODULE = "module"; //$NON-NLS-1$

	/**
	 * Nombre del nodo desde el que se notifica.
	 */
	public static final String ENVIRONMENT_VAR_NODE_NAME = "fire.node.name"; //$NON-NLS-1$

	/**
	 * Formato de fecha que se usara en los mensajes de correo.
	 */
	private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd/MM/yyyy"); //$NON-NLS-1$

	/**
	 * Formato de hora que se usara en los mensajes de correo.
	 */
	private static final SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat("HH:mm"); //$NON-NLS-1$

	/**
	 * Servicio que se encarga del envio de correos.
	 */
	private MailSenderService mailService;

	/**
	 * Mapa con alarmas registradas donde la clave es el mensaje, ya que es el que diferencia una alarma de otra,
	 * y el valor un listado .
	 */
	private Map<String, List<AlarmNotification>> alarmsRegistered;

	/**
	 * Configuraci&oacute;n adicional com&uacute;n a todas las alarmas.
	 */
	private Properties config;

	/**
	 * Fecha desde la que se comprueba que existan alarmas registradas.
	 */
	private Date lastDateChecked;

	/**
	 * Indica si se encuentra activo el periodo de retardo en el que se van acumulando alarmas.
	 */
	private boolean summaryProcessEnabled = false;

	/**
	 * Indica si alguno de los tipos de alarma ha alcanzado el l&iacute;mite de tolerancia antes de enviar correo.
	 */
	private boolean limitReached = false;

	/**
	 * Nombre de la propiedad de configuraci&oacute;n se ha inicializado.
	 */
	private boolean initialized = false;

	/**
	 * L&iacute;mite de notificaciones CRITICAL para enviar un correo.
	 */
	private int criticalNotifyLimits = 0;

	/**
	 * L&iacute;mite de notificaciones ERROR para enviar un correo.
	 */
	private int errorNotifyLimits = 0;

	/**
	 * L&iacute;mite de notificaciones WARNING para enviar un correo.
	 */
	private int warningNotifyLimits = 0;

	/**
	 * L&iacute;mite de notificaciones INFO para enviar un correo.
	 */
	private int infoNotifyLimits = 0;

	/**
	 * Periodo de tiempo dentro del cual deben producirse el n&uacute;mero de alarmas indicado de
	 * un tipo para enviar el correo.
	 */
	private long notificationPeriod = DEFAULT_NOTIFICATION_PERIOD;

	/**
	 * Tiempo que se espera despu&eacute;s del env&iacute;o de una alarma antes de enviar un correo resumen.
	 */
	private long notificationDelay = DEFAULT_NOTIFICATION_DELAY;

	@Override
	public void init(final Properties clientConfig) throws InitializationException {
		if (this.initialized) {
			return;
		}

		if (clientConfig == null) {
			throw new InitializationException("No se ha proporcionado la configuracion del notificador"); //$NON-NLS-1$
		}

		// Copiamos la configuracion para evitar ingerencias externas
		this.config = copyConfig(clientConfig);

		// Extraemos de la configuracion los valores que no usaremos como cadenas para optimizar la operativa
		initConfigurationConstants();

		// Inicializamos el servicio de envio de correos
		this.mailService = new MailSenderService();
		this.mailService.init(this.config);

		this.alarmsRegistered = new HashMap<>();

		// Registramos la fecha de inicio de la captura de eventos
		this.lastDateChecked = new Date();

		this.initialized = true;
	}

	private void initConfigurationConstants() {
		try {
			this.criticalNotifyLimits = Integer.parseInt(this.config.getProperty(NOTIFICATION_CRITICAL_LIMIT));
		}
		catch (final NumberFormatException e) {
			this.criticalNotifyLimits = 0;
		}
		try {
			this.errorNotifyLimits = Integer.parseInt(this.config.getProperty(NOTIFICATION_ERROR_LIMIT));
		}
		catch (final NumberFormatException e) {
			this.errorNotifyLimits = 0;
		}
		try {
			this.warningNotifyLimits = Integer.parseInt(this.config.getProperty(NOTIFICATION_WARNING_LIMIT));
		}
		catch (final NumberFormatException e) {
			this.warningNotifyLimits = 0;
		}
		try {
			this.infoNotifyLimits = Integer.parseInt(this.config.getProperty(NOTIFICATION_INFO_LIMIT));
		}
		catch (final NumberFormatException e) {
			this.infoNotifyLimits = 0;
		}
		try {
			this.notificationPeriod = TimeUnit.MINUTES.toMillis(Integer.parseInt(this.config.getProperty(NOTIFICATION_PERIOD)));
		}
		catch (final NumberFormatException e) {
			this.notificationPeriod = DEFAULT_NOTIFICATION_PERIOD;
		}
		try {
			this.notificationDelay = TimeUnit.MINUTES.toMillis(Integer.parseInt(this.config.getProperty(NOTIFICATION_DELAY)));
		}
		catch (final NumberFormatException e) {
			this.notificationDelay = DEFAULT_NOTIFICATION_DELAY;
		}
	}

	/**
	 * Crea una copia de la configuraci&oacute;n.
	 * @return Configuraci&oacute;n de las alertas.
	 */
	private static Properties copyConfig(final Properties clientConfig) {
		final Properties configuration = new Properties();
		if (clientConfig != null && !clientConfig.isEmpty()) {
			clientConfig.forEach((key, value) -> configuration.setProperty((String) key, (String) value));
		}
		return configuration;
	}

	@Override
	public void setModule(final String module) {
		// El envio de alarmas por correo no hace referencia a componentes concretos de FIRe
	}

	@Override
	public void notify(final AlarmLevel level, final Alarm alarm, final String... source) throws IOException {

		// Comprobamos cuantas notificaciones de este tipo se soportan y, salvo que estemos en tiempo de
		// retardo generando un resumen, no anotaremos aquellas para las que no se ha establecido limite
		final int levelLimit = getLevelLimit(level);
		if (levelLimit <= 0 && !this.summaryProcessEnabled) {
			return;
		}

		final long limitTime = new Date().getTime() - this.notificationPeriod;

		final String message = source == null ? alarm.getDescription() : alarm.formatDescription((Object[]) source);

		List<AlarmNotification> alarmsList;

		synchronized (LOGGER) {
			alarmsList = this.alarmsRegistered.compute(message, (k, existingNotificationList) -> {
				// Si no habia registradas entradas de esa notificacion, creamos una lista para almacenarlas
				final List<AlarmNotification> list;
				if (existingNotificationList == null) {
					list = new ArrayList<>();
				}
				// Si ya existia, eliminamos las entradas antiguas siempre y cuando no estemos en el modo resumen
				else {
					if (!this.summaryProcessEnabled) {
						existingNotificationList.removeIf((notification) -> {
							return notification.getTime() < limitTime;
						});
					}
					list = existingNotificationList;
				}
				// Introducimos la nueva entrada y devolvemos la lista
				final AlarmNotification newNotification = new AlarmNotification(alarm, level);
				list.add(newNotification);
				return list;
			});


			// Si aun no se habia alcanzado el limite para el envio de correo, comprobamos si se ha alcanzado ahora
			if (!this.limitReached) {
				this.limitReached = isLimitReached(message, levelLimit);
			}
			// Si estamos en periodo de retardo (acumulando alarmas para enviar un correo resumen), no es
			// necesario que hagamos nada mas
			if (this.summaryProcessEnabled) {
				return;
			}

			// Si se alcanzo el limite, iniciamos el periodo de resumen
			if (this.limitReached) {
				LOGGER.info("Iniciamos el modo resumen para el envio de alarmas por correo"); //$NON-NLS-1$
				initSummaryProcess();
			}
		}

		// Ademas, enviamos el correo de notificacion (esto ya no requiere que sea sincronizado)
		if (this.limitReached) {
			// Enviamos el correo con la informacion de la alarma
			sendNotice(message, alarmsList);
		}
	}

	private int getLevelLimit(final AlarmLevel level) {

		switch (level) {
		case CRITICAL:
			return this.criticalNotifyLimits;
		case ERROR:
			return this.errorNotifyLimits;
		case WARNING:
			return this.warningNotifyLimits;
		case INFO:
			return this.infoNotifyLimits;
		default:
			break;
		}
		return 0;
	}

	/**
	 * Comprueba si la alarma se ha producido el numero de veces necesarias para enviar correo.
	 * @param message Mensaje que identifica a la alarma concreta.
	 * @param levelLimit Limite de veces que puede producirse una alarma antes de tener que notificarla por correo.
	 * @return {@code true} si la alarma debe notificarse por correo, {@code false} en caso contrario.
	 */
	private boolean isLimitReached(final String message, final int levelLimit) {
		final List<AlarmNotification> notifications = this.alarmsRegistered.get(message);
		return notifications != null && notifications.size() >= levelLimit;
	}

	private void sendNotice(final String alarmMessage, final List<AlarmNotification> alarmsList) {

		final InternetAddress[] recipients = getRecipients();
		final String subject = getNoticeSubject();
		final StringBuilder body = buildNoticeMessage(subject, alarmMessage, alarmsList);

		// Se envia la alarma
		LOGGER.log(Level.INFO, "Se procedera a enviar una notificacion de alarma por correo electronico a los administradores"); //$NON-NLS-1$
		sendMail(recipients, subject, body);
	}


	private InternetAddress[] getRecipients() {

		// Obtenemos el listado de destinatarios de base de datos. Si no se puede, se obtendran
		// del fichero de propiedades
		String [] recipientsAdderesses;
		try {
			recipientsAdderesses = getMailRecipientsFromDB();
		}
		catch (final Exception e) {
			LOGGER.log(Level.WARNING, "Error cargando direcciones de correo desde BD. Se usara la configuracion del properties.", e); //$NON-NLS-1$
			recipientsAdderesses = getMailRecipientsFromProperty();
		}

		// Componemos las direcciones con el listado proporcionado
		final List<InternetAddress> recipients = new ArrayList<>();
		for (int i = 0; i < recipientsAdderesses.length ; i++) {
			try {
				recipients.add(new InternetAddress(recipientsAdderesses[i]));
			} catch (final AddressException e) {
				LOGGER.log(Level.WARNING, "Error al instanciar direccion de correo electronico " + LogUtils.limitText(recipientsAdderesses[i]), e); //$NON-NLS-1$
			}
		}

		return recipients.toArray(new InternetAddress[0]);
	}

	private String getNoticeSubject() {
		final String nodeName = System.getProperty(ENVIRONMENT_VAR_NODE_NAME, ""); //$NON-NLS-1$
		final String envMsg = this.config != null ? this.config.getProperty(MAIL_ENVIRONMENT, "") : ""; //$NON-NLS-1$ //$NON-NLS-2$

		String titleMsg;
		if (!nodeName.isEmpty() && !envMsg.isEmpty()) {
			titleMsg = MailAlarmMessages.getString("noticeHead.0", nodeName, envMsg); //$NON-NLS-1$
		} else if (!nodeName.isEmpty()) {
			titleMsg = MailAlarmMessages.getString("noticeHead.1", nodeName); //$NON-NLS-1$
		} else if (envMsg != null && !envMsg.isEmpty()) {
			titleMsg = MailAlarmMessages.getString("noticeHead.2", envMsg); //$NON-NLS-1$
		} else {
			titleMsg = MailAlarmMessages.getString("noticeHead.3"); //$NON-NLS-1$
		}

		return titleMsg;
	}

	private StringBuilder buildNoticeMessage(final String titleMessage, final String alarmMessage, final List<AlarmNotification> alarms) {

		final String bodyMessage = getNoticeBodyMessage(alarmMessage, alarms);
		final String resultMessage = getNoticeResultMessage();

		final StringBuilder bodySubject = new StringBuilder();
		bodySubject.append("<p>").append(titleMessage).append("</p>") //$NON-NLS-1$ //$NON-NLS-2$
			.append("<p>").append(bodyMessage).append("</p>") //$NON-NLS-1$ //$NON-NLS-2$
			.append("<p>").append(resultMessage).append("</p>"); //$NON-NLS-1$ //$NON-NLS-2$

		return bodySubject;
	}

	private static String getNoticeBodyMessage(final String alarmMsg, final List<AlarmNotification> alarms) {


		final int numAlarms = alarms.size();
		final String level = MailAlarmMessages.getString(alarms.get(0).getLevel().name());
		final Date firstAlarmDate = new Date(alarms.get(0).getTime());
		final String firstAlarmDay = DATE_FORMATTER.format(firstAlarmDate);
		final String firstAlarmHour = TIME_FORMATTER.format(firstAlarmDate);

		String bodyMsg;
		if (numAlarms == 1) {
			// Se ha detectado la alarma con el mensaje ? el dia ? a las ?. Esta alarma esta categorizada como ?.
			bodyMsg = MailAlarmMessages.getString("noticeBody.0", alarmMsg, firstAlarmDay, firstAlarmHour, level); //$NON-NLS-1$
		}
		else {
			final String numAlarmsText = Integer.toString(numAlarms);
			final Date lastAlarmDate = new Date(alarms.get(alarms.size() - 1).getTime());
			final String lastAlarmDay = DATE_FORMATTER.format(lastAlarmDate);
			final String lastAlarmHour = TIME_FORMATTER.format(lastAlarmDate);
			if (firstAlarmDay.equals(lastAlarmDay)) {
				// Se han detectado ? alarmas con el mensaje ? entre las ? y las ? del dia ?. Esta alarma esta categorizada como ?.
				bodyMsg = MailAlarmMessages.getString("noticeBody.1", numAlarmsText, alarmMsg, firstAlarmHour, lastAlarmHour, firstAlarmDay, level); //$NON-NLS-1$
			} else {
				// Se han detectado ? alarmas con el mensaje ? entre las ? del dia y las ? del dia ?. Esta alarma esta caterorizada como ?.
				bodyMsg = MailAlarmMessages.getString("noticeBody.2", numAlarmsText, alarmMsg, firstAlarmHour, firstAlarmDay, lastAlarmHour, lastAlarmDay, level); //$NON-NLS-1$
			}
		}

		return bodyMsg;
	}

	private String getNoticeResultMessage() {

		final Date notificationDate = new Date(new Date().getTime() + this.notificationDelay);
		final String notificationDay = DATE_FORMATTER.format(notificationDate);
		final String notificationHour = TIME_FORMATTER.format(notificationDate);

		// Se inicia la recopilacion de alarmas. En dia ? a las ? recibira el correo resumen con las alarmas detectadas en este periodo.
		return  MailAlarmMessages.getString("noticeResult.0", notificationDay, notificationHour); //$NON-NLS-1$
	}

	private void sendMail(final InternetAddress[] recipients, final String subject, final StringBuilder message) {
		Executors.newSingleThreadExecutor().submit(
				new SendingMailRunnable(this.mailService, recipients, subject, message));
	}

	private static String[] getMailRecipientsFromDB() throws SQLException {
		final List<String> emailList = new ArrayList<>();

		try (Connection conn = DbManager.getConnection();
		     PreparedStatement st = conn.prepareStatement(SQL_SELECT_MAIL_RECIPIENTS);
		     ResultSet rs = st.executeQuery()) {

			while (rs.next()) {
				final String mail = rs.getString(1);
				if (mail != null && !mail.isEmpty()) {
					emailList.add(mail.trim());
				}
			}
		}

		return emailList.toArray(new String[0]);
	}

	private String[] getMailRecipientsFromProperty() {
		final String fallbackRecipients = this.config.getProperty(ConstantsMail.MAIL_RECIPIENTS);

		if (fallbackRecipients != null) {
			return fallbackRecipients.split(","); //$NON-NLS-1$
		}

		return new String[0];
	}


	private void initSummaryProcess() {

		// Eliminamos las notificaciones acumuladas hasta ahora para que en el resumen aparezcan
		// las nuevas
		clearNotifications();
		// Establecemos la fecha de inicio del retardo
		this.lastDateChecked = new Date();
		// Identificamos la fecha actual
		// Habilitamos el periodo de retardo
		this.summaryProcessEnabled = true;
		// Arrancamos la tarea para el envio del resumen
		AlarmSummarySendingScheduler.getInstance().scheduleTask((int) this.notificationDelay, this);
	}


	private void clearNotifications() {
		this.alarmsRegistered.clear();
	}

	public boolean isInitialized() {
		return this.initialized;
	}

	/**
	 * Envia el resumen de las alarmas almacenadas en la cola.
	 */
	public void sendSummary() {

		LOGGER.log(Level.INFO, "Se envia el resumen de alarmas por correo electronico"); //$NON-NLS-1$

		final InternetAddress[] recipients = getRecipients();

		final String subject = getSummarySubject();

		StringBuilder message;

		// Bloqueamos el acceso para evitar que se modifiquen las alarmas durante la generacion del resumen.
		// Si se volvio a alcanzar el limite, volvemos a lanzar la yarea de resumen
		synchronized (LOGGER) {
			message = buildSummaryBodyMessage(subject);

			// Si se ha alcanzado el limite durante el tiempo de acumulado, se vuelve a iniciar el proceso de resumen.
			// Si no, se da por finalizado el proceso de resumen
			if (this.limitReached) {
				this.limitReached = false;
				initSummaryProcess();
			} else {
				this.summaryProcessEnabled = false;
			}
		}

		// Se envia la alarma
		sendMail(recipients, subject, message);
	}

	private String getSummarySubject() {
		final String nodeName = System.getProperty(ENVIRONMENT_VAR_NODE_NAME, ""); //$NON-NLS-1$
		final String envMsg = this.config != null ? this.config.getProperty(MAIL_ENVIRONMENT, "") : ""; //$NON-NLS-1$ //$NON-NLS-2$

		String titleMsg;
		if (!nodeName.isEmpty() && !envMsg.isEmpty()) {
			// Resumen de alarmas de FIRe registradas en el nodo ? del entorno ?
			titleMsg = MailAlarmMessages.getString("summaryHead.0", nodeName, envMsg); //$NON-NLS-1$
		} else if (!nodeName.isEmpty()) {
			// Resumen de alarmas de FIRe registradas en el nodo ?
			titleMsg = MailAlarmMessages.getString("summaryHead.1", nodeName); //$NON-NLS-1$
		} else if (envMsg != null && !envMsg.isEmpty()) {
			// Resumen de alarmas de FIRe registradas en el entorno ?
			titleMsg = MailAlarmMessages.getString("summaryHead.2", envMsg); //$NON-NLS-1$
		} else {
			// Resumen de alarmas de FIRe
			titleMsg = MailAlarmMessages.getString("summaryHead.3"); //$NON-NLS-1$
		}

		return titleMsg;
	}

	private StringBuilder buildSummaryBodyMessage(final String titleMessage) {

		final String bodyMessage = getSummaryBodyMessage();
		final String listMessage = getSummaryListMessage();
		final String resultMessage = getSummaryResultMessage();

		final StringBuilder bodySubject = new StringBuilder();
		bodySubject.append("<p>").append(titleMessage).append("</p>") //$NON-NLS-1$ //$NON-NLS-2$
			.append("<p>").append(bodyMessage).append("</p>") //$NON-NLS-1$ //$NON-NLS-2$
			.append("<ul>").append(listMessage).append("</ul>") //$NON-NLS-1$ //$NON-NLS-2$
			.append("<p>").append(resultMessage).append("</p>"); //$NON-NLS-1$ //$NON-NLS-2$

		return bodySubject;
	}

	private String getSummaryBodyMessage() {

		final String initialDay = DATE_FORMATTER.format(this.lastDateChecked);
		final String initialHour = TIME_FORMATTER.format(this.lastDateChecked);
		final Date now = new Date();
		final String nowDay = DATE_FORMATTER.format(now);
		final String nowHour = TIME_FORMATTER.format(now);

		String bodyMsg;
		if (nowDay.equals(initialDay)) {
			// Se han detectado las siguientes alarmas entre las ? y las ? del dia ?:
			bodyMsg = MailAlarmMessages.getString("summaryBody.0", initialHour, nowHour, nowDay); //$NON-NLS-1$
		} else {
			// Se han detectado las siguientes alarmas entre las ? del dia ? y las ? del dia ?:
			bodyMsg = MailAlarmMessages.getString("summaryBody.1", initialHour, initialDay, nowHour, nowDay); //$NON-NLS-1$
		}

		return bodyMsg;
	}

	private String getSummaryListMessage() {

		final StringBuilder bodyMsg = new StringBuilder();
		for (final Map.Entry<String, List<AlarmNotification>> entry : this.alarmsRegistered.entrySet()) {
			final String message = entry.getKey();
			final List<AlarmNotification> notifs = entry.getValue();
			if (notifs != null) {
				bodyMsg.append("<li>").append(message).append(" "); //$NON-NLS-1$ //$NON-NLS-2$
				if (notifs.size() == 1) {
					bodyMsg.append(MailAlarmMessages.getString("summaryList.0")); //$NON-NLS-1$
				} else {
					bodyMsg.append(MailAlarmMessages.getString("summaryList.1", String.valueOf(notifs.size()))); //$NON-NLS-1$
				}
				bodyMsg.append("</li>"); //$NON-NLS-1$
			}
		}

		return bodyMsg.toString();
	}

	private String getSummaryResultMessage() {
		String result;
		if (this.limitReached) {
			final Date notificationDate = new Date(new Date().getTime() + this.notificationDelay);
			final String notificationDay = DATE_FORMATTER.format(notificationDate);
			final String notificationHour = TIME_FORMATTER.format(notificationDate);
			// Se ha vuelto a alcanzar el limite de tolerancia establecido. En el dia ? a las ? recibira  el correo resumen con las alarmas detectadas en ese periodo.
			result = MailAlarmMessages.getString("summaryResult.0", notificationDay, notificationHour); //$NON-NLS-1$
		}
		else {
			// En este periodo no se ha alcanzado el limite de tolerancia establecido. Finaliza el envio de resumenes de alarmas.
			result = MailAlarmMessages.getString("summaryResult.1"); //$NON-NLS-1$
		}

		return  result;
	}

	@Override
	public void destroy() {
		this.initialized = false;
		AlarmSummarySendingScheduler.getInstance().stop();
	}

	private class SendingMailRunnable implements Runnable {

		private final MailSenderService service;
		private final InternetAddress[] recipients;
		private final String subject;
		private final StringBuilder message;


		SendingMailRunnable(final MailSenderService service, final InternetAddress[] recipients,
				final String subject, final StringBuilder message) {
			this.service = service;
			this.recipients = recipients;
			this.subject = subject;
			this.message = message;
		}

		@Override
		public void run() {
			this.service.sendEmail(this.recipients, this.subject, this.message,
					"Correo de notificacion de alarmas enviado", MailSenderService.MAIL_TEXT_HTML_CHARSET); //$NON-NLS-1$
		}
	}
}
