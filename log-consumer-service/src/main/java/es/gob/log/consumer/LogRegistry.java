package es.gob.log.consumer;

import java.nio.charset.Charset;

/**
 * Entrada del registro de log.
 */
class LogRegistry {

	private final StringBuilder log;

	private long currentMillis = 0;

	private int level = 0;

	private int lines;

	/**
	 * Crea una entrada del log vac&iacute;a.
	 */
	public LogRegistry() {
		this.log = new StringBuilder();
		this.lines = 0;
	}

	/**
	 * Crea una entrada del log.
	 * @param logLine Primera linea de la entrada.
	 */
	public LogRegistry(final String logLine) {
		this.log = new StringBuilder(logLine);
		this.lines = 1;
	}

	/**
	 * Establece la fecha del log.
	 * @param currentMillis Milisegundos que definen la fecha/hora de la
	 * entrada de log.
	 */
	public void setCurrentMillis(final long currentMillis) {
		this.currentMillis = currentMillis;
	}

	/**
	 * Establece el nivel de log.
	 * @param level Nivel de log.
	 */
	public void setLevel(final int level) {
		this.level = level;
	}

	/**
	 * Agrega una nueva l&iacute;nea a la entrada del log.
	 * @param logLine L&iacute;nea de log.
	 */
	public void appendLogLine(final String logLine) {
		this.log.append('\n').append(logLine);
		this.lines++;
	}

	/**
	 * Recupera la fecha del log.
	 * @return Milisegundos que definen la fecha/hora de la entrada de log.
	 */
	public long getCurrentMillis() {
		return this.currentMillis;
	}

	/**
	 * Recupera el nivel de log.
	 * @return Nivel de log.
	 */
	public int getLevel() {
		return this.level;
	}

	/**
	 * Recupera la entrada de log.
	 * @return Entrada de log completa.
	 */
	public StringBuilder getLog() {
		return this.log;
	}

	/**
	 * Recupera la entrada de log en bytes.
	 * @param charset Juego de caracteres.
	 * @return Entrada de log.
	 */
	public byte[] getBytes(final Charset charset) {
		return this.log.toString().getBytes(charset);
	}

	/**
	 * Indica el n&uacute;mero de l&iacute;neas que forman la entrada de log.
	 * @return N&uacute;mero de l&iacute;neas.
	 */
	public int linesCount() {
		return this.lines;
	}
}
