package es.gob.fire.alarms.eventmanager;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import es.gob.eventmanager.client.EventClient;
import es.gob.eventmanager.message.Alert;
import es.gob.fire.alarms.Alarm;
import es.gob.fire.alarms.AlarmLevel;
import es.gob.fire.alarms.AlarmNotifier;
import es.gob.fire.alarms.InitializationException;

/**
 * Notificador para el env&iacute;o de alarmas a GrayLog.
 */
public class EventManagerAlarmNotifier implements AlarmNotifier {

	static final Logger LOGGER = Logger.getLogger(EventManagerAlarmNotifier.class.getName());

	/**
	 * Propiedad de configuraci&oacute;n que determina el nombre de nodo
	 * en el que se ejecuta el sistema.
	 */
	public static final String PROP_HOSTNAME = "hostname"; //$NON-NLS-1$

	/**
	 * Propiedad de configuraci&oacute;n que indica la propiedad del sistema
	 * de la que obtener el nombre del nodo en el que se ejecuta el sistema.
	 */
	public static final String PROP_HOSTNAME_VARIABLE_NAME = "hostname.variable"; //$NON-NLS-1$

	/**
	 * Propiedad de configuraci&oacute;n que indica el identificador del sistema
	 * que notifica la alarma.
	 */
	public static final String PROP_SYSTEM_ID = "system.id"; //$NON-NLS-1$

	/**
	 * Propiedad de configuraci&oacute;n que indica la cadena en base a la cual se
	 * genera la clave de comunicaci&oacute; con el sistema.
	 */
	public static final String PROP_SYSTEM_KEY = "system.key"; //$NON-NLS-1$

	/**
	 * Propiedad de configuraci&oacute;n con la URL del servicio al que notificar las alarmas.
	 */
	public static final String PROP_SERVICE_URL = "service.url"; //$NON-NLS-1$

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

	/** Nombre asignado cuando no se conoce el nombre del host. */
	public static final String HOSTNAME_UNDEFINED = "UNDEFINED"; //$NON-NLS-1$

	/**
	 * Flag that indicates if the configuration of GrayLog has been initialized (with or without errors).
	 */
	private boolean initialized = false;

	/**
	 * Cliente para el env&iacute;o de notificaciones.
	 */
	private EventClient client = null;

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

			this.client = createClient(clientConfig);

			this.config = initConfig(clientConfig);

			this.initialized = true;
		}
	}

	/**
	 * Crea e inicializa el cliente para el env&iacute;o de eventos.
	 * @param config Configuraci&oacute;n para la inicializaci&oacute;n del Cliente.
	 * @return Cliente para el env&iacute;o de eventos.
	 */
	private static EventClient createClient(final Properties clientConfig) throws InitializationException {

		// Identificador del sistema que emite las notificaciones
		final String systemId = clientConfig.getProperty(PROP_SYSTEM_ID);
		if (systemId == null) {
			throw new InitializationException("No se ha configurado la propiedad " + PROP_SYSTEM_ID + " con el identificador del sistema para la notificacion de los eventos"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		// Nombre de nodo este nodo
		final String node = identifyNodeName(clientConfig);

		// URL del servicio al que se envian las notificaciones
		final String urlSpec = clientConfig.getProperty(PROP_SERVICE_URL);
		if (urlSpec == null) {
			throw new InitializationException("No se ha configurado la propiedad " + PROP_SERVICE_URL + " con la URL del servicio del sistema para la notificacion de los eventos"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		URL url;
		try {
			url = new URL(urlSpec);
		}
		catch (final Exception e) {
			throw new InitializationException("El valor de la propiedad " + PROP_SERVICE_URL + " no es una URL valida"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		// Clave con la que se genera la informacion
		final String systemKey = clientConfig.getProperty(PROP_SYSTEM_KEY);
		if (systemKey == null) {
			throw new InitializationException("No se ha configurado la propiedad " + PROP_SYSTEM_KEY + " con la clave para la comunicacion segura con el sistema para la notificacion de los eventos"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		return new EventClient(systemId, node, url, systemKey);
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
				if (keyString.startsWith(PROP_EXTRA_FIELDS_PREFIX)) {
					final String value = clientConfig.getProperty(keyString);
					configuration.setProperty(keyString.substring(PROP_EXTRA_FIELDS_PREFIX.length()), value);
				}
			}
		}
		return configuration;
	}


	/**
	 * Identifica el nombre asignado al host.
	 */
	private static String identifyNodeName(final Properties clientConfig) {

		// Si en la configuracion se establece un nombre de nodo, se devuelve ese
		String hostname = clientConfig.getProperty(PROP_HOSTNAME);
		if (hostname != null) {
			return hostname;
		}

		// Si en la configuracion se establece un nombre de propiedad de la que obtener
		// el nombre, lo obtenermos de la misma
		final String systemVariable = clientConfig.getProperty(PROP_HOSTNAME_VARIABLE_NAME);
		if (systemVariable != null) {
			hostname = System.getProperty(systemVariable);
			if (hostname != null) {
				return hostname;
			}
		}

		// Usamos de nombre de nodo el nombre de red del host. En caso de no poder obtenerlo
		// se usara una cadena por defecto
		InetAddress ip;
		try {
			ip = InetAddress.getLocalHost();
			hostname = ip.getHostName();

		} catch (final UnknownHostException e) {
			hostname = HOSTNAME_UNDEFINED;
		}

		return hostname;
	}

	@Override
	public void setModule(final String module) {
		this.config.setProperty(PROP_EXTRA_FIELD_MODULE, module);
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

		final Alert alert = new Alert(alarm.getEventCode(), message);

		this.client.send(alert, this.config);
	}
}
