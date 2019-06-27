package es.gob.log.consumer;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Informaci&oacute;n sobre el log.
 */
public class LogInfo implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

	private static final String DEFAULT_DATE_FORMAT = "MMM dd, yyyy hh:mm:ss a"; //$NON-NLS-1$

	private static final String DEFAULT_LEVELS;

	private static final String DEFAULT_LOG_PATTERN = "[DATE]*\n[LEVEL]: *"; //$NON-NLS-1$

	private static final String PROPERTY_CHARSET = "charset"; //$NON-NLS-1$

	private static final String PROPERTY_DATE_FORMAT = "dateTimeFormat"; //$NON-NLS-1$

	private static final String PROPERTY_LEVELS = "levels"; //$NON-NLS-1$

	private static final String PROPERTY_LOG_PATTERN = "logPattern"; //$NON-NLS-1$

	private static final String LEVELS_SEPARATOR = ","; //$NON-NLS-1$

	private static final String DATE_FORMAT_CHARSET = "GyYMwWDdFEu"; //$NON-NLS-1$

	private static final String TIME_FORMAT_CHARSET = "aHkKhmsSzZX"; //$NON-NLS-1$

	private static final Logger LOGGER = Logger.getLogger(LogInfo.class.getName());

	static {
		final StringBuilder builder = new StringBuilder(70)
				.append(Level.FINEST.getLocalizedName()).append(LEVELS_SEPARATOR)
				.append(Level.FINER.getLocalizedName()).append(LEVELS_SEPARATOR)
				.append(Level.FINE.getLocalizedName()).append(LEVELS_SEPARATOR)
				.append(Level.CONFIG.getLocalizedName()).append(LEVELS_SEPARATOR)
				.append(Level.INFO.getLocalizedName()).append(LEVELS_SEPARATOR)
				.append(Level.WARNING.getLocalizedName()).append(LEVELS_SEPARATOR)
				.append(Level.SEVERE.getLocalizedName());

		DEFAULT_LEVELS = builder.toString();
	}

	private Charset charset;

	private String logPattern;

	private String dateFormat;

	private String[] levels;

	private boolean hasDate = false;

	private boolean hasTime = false;

	/**
	 * Construye un objeto de informaci&oacute;n de un log estableciendo todas las opciones
	 * a las utilizadas por defecto por la Java Logging Api.
	 */
	public LogInfo() {
		this.charset = DEFAULT_CHARSET;
		this.logPattern = DEFAULT_LOG_PATTERN;
		this.dateFormat = DEFAULT_DATE_FORMAT;
		this.levels = DEFAULT_LEVELS.split(LEVELS_SEPARATOR);

		this.hasDate = identifyDateTimeComponent(this.dateFormat, DATE_FORMAT_CHARSET);
		this.hasTime = identifyDateTimeComponent(this.dateFormat, TIME_FORMAT_CHARSET);
	}

	private static boolean identifyDateTimeComponent(final String format, final String charset) {
		boolean found = false;
		for (int i = 0; !found && i < charset.length(); i++) {
			if (format.contains(Character.toString(charset.charAt(i)))) {
				found = true;
			}
		}
		return found;
	}

	/**
	 * Carga la informaci&oacute;n del log.
	 * @param is Flujo de entrada con la informaci&oacute;n del log.
	 * @throws IOException Cuando ocurre alg&uacute;n error durante la carga.
	 */
	public void load(final InputStream is) throws IOException {

		final Properties config = new Properties();
		config.load(is);

		try {
			this.charset = Charset.forName(config.getProperty(PROPERTY_CHARSET, DEFAULT_CHARSET.name()));
		}
		catch (final Exception e) {
			LOGGER.warning(String.format(
					"Se configuro un juego de caracteres no valido (%s), se usara el por defecto: %s", //$NON-NLS-1$
					config.getProperty(PROPERTY_CHARSET),
					DEFAULT_CHARSET.name()));
			this.charset = DEFAULT_CHARSET;
		}

		this.logPattern = config.getProperty(PROPERTY_LOG_PATTERN);
		this.dateFormat = config.getProperty(PROPERTY_DATE_FORMAT);
		final String levelsText = config.getProperty(PROPERTY_LEVELS);

		this.levels = levelsText!= null ? levelsText.split(LEVELS_SEPARATOR) : null;
		this.hasDate = this.dateFormat != null ? identifyDateTimeComponent(this.dateFormat, DATE_FORMAT_CHARSET) : false;
		this.hasTime = this.dateFormat != null ? identifyDateTimeComponent(this.dateFormat, TIME_FORMAT_CHARSET) : false;
	}

	/**
	 * Obtiene el juego de caracteres del log o el juego de caracteres por defecto
	 * si no se especific&oacute;.
	 * @return Juego de caracteres.
	 */
	public Charset getCharset() {
		return this.charset;
	}

	/**
	 * Establece el juego de caracteres del log.
	 * @param charset Juego de caracteres.
	 */
	public void setCharset(final Charset charset) {
		this.charset = charset;
	}

	/**
	 * Obtiene el patr&oacute;n del log.
	 * @return Patr&oacute;n.
	 */
	public String getLogPattern() {
		return this.logPattern;
	}

	/**
	 * Obtiene el formato de la fecha de entrada de los logs.
	 * @return Cadena con formato parseable por SimpleDateFormat.
	 */
	public String getDateFormat() {
		return this.dateFormat;
	}

	/**
	 * Obtiene el listado de niveles de log.
	 * @return Listado de niveles de log.
	 */
	public String[] getLevels() {
		return this.levels;
	}

	/**
	 * Indica si el formato de fecha/hora indicado tiene componente fecha. Esto es
	 * util para saber si generar criterios de filtrado de logs que incluyan la fecha.
	 * @return {@code true} si la fecha/hora del log incluye fecha, {@code false} en
	 * caso contrario.
	 */
	public boolean hasDateComponent() {
		return this.hasDate;
	}

	/**
	 * Indica si el formato de fecha/hora indicado tiene componente hora. Esto es
	 * util para saber si generar criterios de filtrado de logs que incluyan la hora.
	 * @return {@code true} si la fecha/hora del log incluye hora, {@code false} en
	 * caso contrario.
	 */
	public boolean hasTimeComponent() {
		return this.hasTime;
	}
}
