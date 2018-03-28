package es.gob.log.consumer;

/**
 * Criterio de b&uacute;squeda que aplicar.
 */
public class Criteria {

	static final int DEFAULT_LEVEL = -1;
	static final int DEFAULT_START_DATE = -1;
	static final int DEFAULT_END_DATE = -1;

	/** Nivel m&iacute;nimo de log a mostrar. */
	private int level;

	/** Fecha de inicio. */
	private long startDate;

	/** Fecha de fin. */
	private long endDate;

	/**
	 * Crea un conjunto de criterios de b&uacute;squeda vac&iacute;o.
	 */
	public Criteria() {
		this(-1, -1, -1);
	}

	/**
	 * Crea un conjunto de critorios de b&uacute;squeda.
	 * @param level Nivel de log m&iacute;nimo que se debe mostrar.
	 * @param startDate Fecha m&iacute;nima de los logs a mostrar.
	 * @param endDate Fecha m&aacute;xima de los logs a mostrar.
	 */
	public Criteria(final int level, final long startDate, final long endDate) {
		this.level = level;
		this.startDate = startDate;
		this.endDate = endDate;
	}

	/**
	 * Recupera el nivel de log m&iacute;nimo que se debe mostrar. Si no se ha
	 * especificado, se devolvera el nivel 0, que deber&iacute;a devolverlo todo.
	 * @return Nivel de log m&iacute;nimo que se debe mostrar.
	 */
	public int getLevel() {
		return this.level;
	}

	/**
	 * Recupera la fecha m&iacute;nima de los logs a mostrar.
	 * @return Fecha m&iacute;nima de los logs a mostrar.
	 */
	public long getStartDate() {
		return this.startDate;
	}

	/**
	 * Recupera la fecha m&aacute;xima de los logs a mostrar.
	 * @return Fecha m&aacute;xima de los logs a mostrar.
	 */
	public long getEndDate() {
		return this.endDate;
	}

	/**
	 * Establece el nivel de log m&iacute;nimo que se debe mostrar.
	 * @param level Nivel de log m&iacute;nimo que se debe mostrar.
	 */
	public void setLevel(final int level) {
		this.level = level;
	}

	/**
	 * Establece la fecha m&iacute;nima de los logs a mostrar.
	 * @param startDate Fecha m&iacute;nima de los logs a mostrar.
	 */
	public void setStartDate(final long startDate) {
		this.startDate = startDate;
	}

	/**
	 * Establece la fecha m&aacute;xima de los logs a mostrar.
	 * @param endDate Fecha m&aacute;xima de los logs a mostrar.
	 */
	public void setEndDate(final long endDate) {
		this.endDate = endDate;
	}
}
