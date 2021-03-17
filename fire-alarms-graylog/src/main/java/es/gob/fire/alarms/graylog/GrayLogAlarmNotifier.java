package es.gob.fire.alarms.graylog;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import biz.paluch.logging.gelf.intern.GelfMessage;
import biz.paluch.logging.gelf.intern.GelfSender;
import biz.paluch.logging.gelf.intern.sender.GelfUDPSender;
import es.gob.fire.alarms.Alarm;
import es.gob.fire.alarms.AlarmLevel;
import es.gob.fire.alarms.AlarmNotifier;
import es.gob.fire.alarms.InitializationException;

/**
 * Notificador para el env&iacute;o de alarmas a GrayLog.
 */
public class GrayLogAlarmNotifier implements AlarmNotifier {

	static final Logger LOGGER = Logger.getLogger(GrayLogAlarmNotifier.class.getName());

	/**
	 * Constant attribute that represents the token key 'cod_err' for a Gray Log Message Field.
	 */
	private static final String TOKEN_KEY_ERROR_CODE = "cod_err"; //$NON-NLS-1$

	/**
	 * Constant attribute that represents the token key 'MESSAGE' for a Gray Log Message Field.
	 */
	private static final String TOKEN_KEY_MESSAGE = "message"; //$NON-NLS-1$

	/**
	 * Constant attribute that represents the token key 'source' for a Gray Log Message Field.
	 */
	private static final String TOKEN_KEY_SOURCE = "source"; //$NON-NLS-1$

	/**
	 * Nombre de la propiedad de configuraci&oacute;n que determina si deben
	 * enviarse las alertas a GrayLog.
	 */
	public static final String PROP_ENABLED = "enabled"; //$NON-NLS-1$

	/**
	 * Nombre de la propiedad de configuraci&oacute;n que determina si deben
	 * enviarse las alertas a GrayLog.
	 */
	public static final String PROP_DESTINATION_HOST = "destination.host"; //$NON-NLS-1$

	/**
	 * Nombre de la propiedad de configuraci&oacute;n que determina el puerto
	 * de conexi&oacute;n con GrayLog.
	 */
	public static final String PROP_DESTINATION_PORT = "destination.port"; //$NON-NLS-1$

	/**
	 * Prefijo de las propiedades que establecen campos adicionales para la
	 * configuraci&oacute;n de las alertas a GrayLog.
	 */
	public static final String PROP_EXTRA_FIELDS_PREFIX = "field."; //$NON-NLS-1$

	/**
	 * Campo declarado con el nombre del modulo de FIRe. Este campo no se toma del
	 * fichero de propiedades, ya que este fichero pueden usarlo varios modulos. El
	 * valor se indica en la solicitud de notificaci&oacute;n.
	 */
	public static final String PROP_EXTRA_FIELD_MODULE = "module"; //$NON-NLS-1$

	/** Nombre asignado cuando no se conoce el nombre del host. */
	private static final String HOSTNAME_UNDEFINED = "UNDEFINED"; //$NON-NLS-1$

	/**
	 * Flag that indicates if the configuration of Gray Log has been initialized (with or without errors).
	 */
	private boolean initialized = false;

	/**
	 * Flag that indicates if there is some error in the initialization of Gray Log properties.
	 */
	private boolean initializationError = false;

	/**
	 * Flag that indicates if Gray Log is enabled.
	 */
	private boolean grayLogEnabled = false;

	/**
	 * Attribute that represents the destination host of the Gray Log Server.
	 */
	private String grayLogHost = null;

	/**
	 * Attribute that represents the destination port of the Gray Log Server.
	 */
	private int grayLogPort = -1;

	/**
	 * Attribute that represents the set of static declared fields to use in the
	 * messages to Gray Log.
	 */
	private Map<String, String> grayLogDeclaredFields = null;

	/**
	 * Nombre del host.
	 */
	private String hostname;

	/**
	 * Attribute that represents the Gray Log Messages Sender.
	 */
	private GelfSender grayLogMessageSender = null;

	@Override
	public void init(final Properties config) throws InitializationException {

		if (!this.initialized) {

			if (config == null) {
				throw new InitializationException("No se ha proporcionado la configuracion del notificador"); //$NON-NLS-1$
			}

			this.initializationError = false;
			loadIfGrayLogIsEnabled(config);
			if (this.grayLogEnabled) {

				loadGrayLogServerHost(config);
				loadGrayLogServerPort(config);
				loadGrayLogDeclaredFields(config);
				loadGrayLogMessagesSender();
				identifyEnvironmentHostname();

				if (this.initializationError) {
					this.grayLogEnabled = false;
					LOGGER.severe("Error al inicializar el notificador de alarmas a GrayLog"); //$NON-NLS-1$
				}
			}

			this.initialized = true;
		}
	}

	/**
	 * Load if the GrayLog is enabled in the 'StaticMonitorizaConfig.properties' configuration file.
	 */
	private void loadIfGrayLogIsEnabled(final Properties config) {
		final String isGrayLogEnabled = config.getProperty(PROP_ENABLED, Boolean.FALSE.toString());
		this.grayLogEnabled = Boolean.parseBoolean(isGrayLogEnabled);
	}

	/**
	 * Load the host of the Gray Log destination server.
	 */
	private void loadGrayLogServerHost(final Properties config) {
		final String result = config.getProperty(PROP_DESTINATION_HOST);
		this.grayLogHost = result == null || result.isEmpty() ? null : result;
	}

	/**
	 * Load the port of the Gray Log destination port.
	 */
	private void loadGrayLogServerPort(final Properties config) {
		int result = -1;
		final String portString = config.getProperty(PROP_DESTINATION_PORT);
		if (portString != null && !portString.isEmpty()) {
			result = Integer.parseInt(portString);
		}
		this.grayLogPort = result;
	}

	/**
	 * Load the declared fields to add in the messages to Gray Log.
	 */
	private void loadGrayLogDeclaredFields(final Properties config) {

		this.grayLogDeclaredFields = new ConcurrentHashMap<>();
		if (config != null && !config.isEmpty()) {

			final Set<Object> keySet = config.keySet();
			for (final Object key: keySet) {
				final String keyString = (String) key;
				if (keyString.startsWith(PROP_EXTRA_FIELDS_PREFIX)) {
					final String value = config.getProperty(keyString);
					this.grayLogDeclaredFields.put(keyString.substring(PROP_EXTRA_FIELDS_PREFIX.length()), value);
				}
			}
		}
	}


	/**
	 * Load the Gray Log message sender manager.
	 */
	private void loadGrayLogMessagesSender() {

		if (this.grayLogMessageSender != null) {
			this.grayLogMessageSender.close();
			this.grayLogMessageSender = null;
		}

		if (this.grayLogHost != null && this.grayLogPort > 0) {
			try {
				this.grayLogMessageSender = new GelfUDPSender(this.grayLogHost, this.grayLogPort, null);
			} catch (final IOException e) {
				this.grayLogMessageSender = null;
				this.initializationError = true;
				LOGGER.log(Level.SEVERE, "No se ha podido cargar el objeto para el envio de mensajes a GrayLog", e); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Identifica el nombre asignado al host.
	 */
	private void identifyEnvironmentHostname() {
		InetAddress ip;
		try {

			ip = InetAddress.getLocalHost();
			this.hostname = ip.getHostName();

		} catch (final UnknownHostException e) {
			this.initializationError = true;
			LOGGER.log(Level.SEVERE, "No se ha podido cargar el objeto para el envio de mensajes a GrayLog", e); //$NON-NLS-1$
			this.hostname = HOSTNAME_UNDEFINED;
		}
	}

	@Override
	public void setModule(final String module) {
		this.grayLogDeclaredFields.put(PROP_EXTRA_FIELD_MODULE, module);
	}

	@Override
	public void notify(final AlarmLevel level, final Alarm alarm, final String source)
			throws IOException {

		// Si GrayLog esta habilitado en el sistema...
		if (this.grayLogEnabled) {

			// Si ni el codigo de evento ni el mensaje son cadenas nulas o
			// vacias...
			boolean sended = false;
			if (alarm != null) {

				final GelfMessage gm = new GelfMessage();
				final String message = alarm.formatDescription(source);
				gm.setShortMessage(message);
				gm.setJavaTimestamp(Calendar.getInstance().getTimeInMillis());
				gm.setLevel(translateLevel(level));
				gm.addField(TOKEN_KEY_ERROR_CODE, alarm.getEventCode());
				gm.addField(TOKEN_KEY_SOURCE, this.hostname);
				gm.addField(TOKEN_KEY_MESSAGE, message);
				gm.addFields(this.grayLogDeclaredFields);

				sended = this.grayLogMessageSender.sendMessage(gm);

				if (!sended) {
					throw new IOException("No se ha podido enviar el mensaje a GrayLog"); //$NON-NLS-1$
				}
			}
		}
	}

	/**
	 * Obtiene el nivel de alarma a notificar en base al nivel de alarma identificado
	 * por el sistema.
	 * @param level Nivel de alarma notificado.
	 * @return Nivel de alarma a declarar.
	 */
	private static final String translateLevel(final AlarmLevel level) {
		int grayLogLevel;
		switch (level) {
		case CRITICAL:
			grayLogLevel = 2;
			break;
		case ERROR:
			grayLogLevel = 3;
			break;
		case WARNING:
			grayLogLevel = 4;
			break;
		case INFO:
			grayLogLevel = 6;
			break;
		case DEBUG:
			grayLogLevel = 7;
			break;
		default:
			grayLogLevel = 0;
		}
		return Integer.toString(grayLogLevel);
	}

}
