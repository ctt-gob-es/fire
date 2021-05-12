package es.gob.fire.statistics;

import java.util.Date;

/**
 * Resultado obtenido tras una operaci&oacute;n de carga de datos estad&iacute;sticos en base de datos.
 */
public class LoadStatisticsResult {

	private final boolean correct;

	private final Date lastDateProcessed;

	private final String lastDateProcessedText;

	private final String errorMsg;

	/**
	 * Construye el resultado de la operaci&oacute;n de carga de estad&iacute;sticas en base de datos.
	 * @param correct {@code true} si se cargaron todos los datos estad&iacute;sticos disponibles,
	 * {@code false} si no se pudieron cargar todos ellos.
	 * @param lastDateProcessed Fecha de los &uacute;ltimos datos que se pudieron procesar con &eacute;xito.
	 * @param lastDateProcessedText Fecha en formato texto de los &uacute;ltimos datos que se pudieron
	 * procesar con &eacute;xito.
	 */
	public LoadStatisticsResult(final boolean correct, final Date lastDateProcessed, final String lastDateProcessedText) {
		this.correct = correct;
		this.lastDateProcessed = lastDateProcessed;
		this.lastDateProcessedText = lastDateProcessedText;
		this.errorMsg = null;
	}

	/**
	 * Construye el resultado de la operaci&oacute;n de carga de estad&iacute;sticas en base de datos.
	 * @param correct {@code true} si se cargaron todos los datos estad&iacute;sticos disponibles,
	 * {@code false} si no se pudieron cargar todos ellos.
	 * @param lastDateProcessed Fecha de los &uacute;ltimos datos que se pudieron procesar con &eacute;xito.
	 * @param lastDateProcessedText Fecha en formato texto de los &uacute;ltimos datos que se pudieron
	 * procesar con &eacute;xito.
	 * @param errorMsg Mensaje de error que describe porque no finalizo correctamente la carga.
	 */
	public LoadStatisticsResult(final boolean correct, final Date lastDateProcessed, final String lastDateProcessedText, final String errorMsg) {
		this.correct = correct;
		this.lastDateProcessed = lastDateProcessed;
		this.lastDateProcessedText = lastDateProcessedText;
		this.errorMsg = errorMsg;
	}

	/**
	 * Indica si todos los datos se pudieron cargar correctamente.
	 * @return {@code true} si todos los datos se cargaron correctamente, {@code false}
	 * en caso contrario.
	 */
	public boolean isCorrect() {
		return this.correct;
	}

	/**
	 * Recupera la fecha de los &uacute;ltimos datos que se pudieron cargar en base de datos.
	 * @return Fecha de los datos.
	 */
	public Date getLastDateProcessed() {
		return this.lastDateProcessed;
	}

	/**
	 * Recupera la fecha, en formato texto, de los &uacute;ltimos datos que se pudieron cargar
	 * en base de datos.
	 * @return Fecha de los datos.
	 */
	public String getLastDateProcessedText() {
		return this.lastDateProcessedText;
	}

	/**
	 * Recupera el mensaje de error establecido.
	 * @return Mensaje de error o {@code null} si no se estableci&oacute;.
	 */
	public String getErrorMsg() {
		return this.errorMsg;
	}
}
