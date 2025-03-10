package es.gob.fire.alarms.mail;

import java.io.IOException;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import es.gob.fire.alarms.Alarm;
import es.gob.fire.alarms.AlarmLevel;
import es.gob.fire.alarms.AlarmNotifier;
import es.gob.fire.alarms.InitializationException;
import es.gob.fire.mail.MailSenderService;

/**
 * Notificador para el env&iacute;o de alarmas por e-mail.
 */
public class MailAlarmNotifier implements AlarmNotifier {

	static final Logger LOGGER = Logger.getLogger(MailAlarmNotifier.class.getName());
	
	/**
	 * Campo declarado con el nombre del modulo de FIRe. Este campo no se toma del
	 * fichero de propiedades, ya que este fichero pueden usarlo varios modulos. El
	 * valor se indica en la solicitud de notificaci&oacute;n.
	 */
	public static final String PROP_EXTRA_FIELD_MODULE = "module"; //$NON-NLS-1$
	
	/**
	 * Prefijo de las propiedades que establecen campos adicionales para la
	 * configuraci&oacute;n de las alertas a GrayLog.
	 */
	public static final String PROP_EXTRA_FIELDS_PREFIX = "field."; //$NON-NLS-1$
	
	/**
	 * Nombre de la propiedad de configuraci&oacute;n que determina los destinatarios
	 * de la alarma por correo.
	 */
	public static final String PROP_MAIL_RECIPIENTS = "mail.recipients"; //$NON-NLS-1$
	
	/**
	 * Nombre de la propiedad de configuraci&oacute;n que determina el limite de notificaciones
	 * para enviar un correo
	 */
	public static final String PROP_MAIL_NOTIFY_LIMIT = "mail.notify.limit"; //$NON-NLS-1$
	
	private MailSenderService mailService;

	/**
	 * Nombre de la propiedad de configuraci&oacute;n se ha inicializado.
	 */
	private boolean initialized = false;
	
	/**
	 * Nombre de la propiedad de configuraci&oacute;n que indica el limite de notificaciones para enviar un correo.
	 */
	private static int notifyLimits = 5;
	
	/**
	 * Intentos de notificacion.
	 */
	private static int notifyAttemps = 0;

	/**
	 * Configuraci&oacute;n adicional com&uacute;n a todas las alarmas.
	 */
	private Properties config;

	@Override
	public void init(final Properties clientConfig) throws InitializationException {

		if (!this.initialized) {

			if (clientConfig == null) {
				throw new InitializationException("No se ha proporcionado la configuracion del notificador"); //$NON-NLS-1$
			}

			this.config = initConfig(clientConfig);
			
			this.mailService = new MailSenderService();
			this.mailService.init(clientConfig);
			
			if (this.config.containsKey(PROP_MAIL_NOTIFY_LIMIT)) {
				final String val = this.config.getProperty(PROP_MAIL_NOTIFY_LIMIT, ""); //$NON-NLS-1$
				if (!val.isEmpty()) {
					notifyLimits = Integer.valueOf(val);
				}
			}

			this.initialized = true;
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
		this.config.setProperty(PROP_EXTRA_FIELD_MODULE, module);
	}

	@Override
	public void notify(final AlarmLevel level, final Alarm alarm, final String... source)
			throws IOException {
		
		notifyAttemps++;

		String message;
		if (source == null) {
			message = alarm.getDescription();
		} else {
			message = alarm.formatDescription((Object[]) source);
		}

		if (this.initialized && notifyAttemps == notifyLimits) {
			final String recipients = this.config.getProperty(PROP_MAIL_RECIPIENTS);
			final String [] addresses = recipients.split(","); //$NON-NLS-1$
			final InternetAddress [] emails = new InternetAddress [addresses.length];
			for (int i = 0; i < addresses.length ; i++) {
				try {
					emails[i] = new InternetAddress(addresses[i]);
				} catch (final AddressException e) {
					LOGGER.log(Level.WARNING, "Error al instanciar direccion de correo electronico", e); //$NON-NLS-1$
				}
			}
			final StringBuilder bodySubject = new StringBuilder();
			bodySubject.append(message);
			this.mailService.sendEmail(emails, "Notificacion de alarma", bodySubject, message); //$NON-NLS-1$
			notifyAttemps = 0;
		}
		
	}
}
