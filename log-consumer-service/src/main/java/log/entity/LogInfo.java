package log.entity;

public class LogInfo {

	private String code;
	private String [] levels;
	private String dateFormat;
	private String logPattern;

	public LogInfo() {super();}


	public LogInfo(final String code, final String[] levels, final String dateFormat, final String logPattern) {
		super();
		this.code = code;
		this.levels = levels;
		this.dateFormat = dateFormat;
		this.logPattern = logPattern;
	}

	//Getter & Setter

	/**
	 * Obtiene el c&oacute;digo (codificac&oacute;n de la p&aacute;gina, UTF-8, ISO-8859-1, etc..)
	 * @return
	 */
	public final String getCode() {
		return this.code;
	}
	/**
	 * Establece el c&oacute;digo (codificac&oacute;n de la p&aacute;gina, UTF-8, ISO-8859-1, etc..)
	 * @return
	 */
	public final void setCode(final String code) {
		this.code = code;
	}
	/**
	 * Obtiene los niveles de log
	 * @return
	 */
	public final String[] getLevels() {
		return this.levels;
	}
	/**
	 * Establece los niveles de log
	 * @param levels
	 */
	public final void setLevels(final String[] levels) {
		this.levels = levels;
	}
	/**
	 * Obtiene el formato de fecha
	 * @return
	 */
	public final String getDateFormat() {
		return this.dateFormat;
	}
	/**
	 *  Establece el formato de fecha
	 * @param dateFormat
	 */
	public final void setDateFormat(final String dateFormat) {
		this.dateFormat = dateFormat;
	}
	/**
	 * Obtiene el patr&oacute;n de las lineas generadas en el log
	 * @return
	 */
	public final String getLogPattern() {
		return this.logPattern;
	}
	/**
	 *  Establece el patr&oacute;n de las lineas generadas en el log
	 * @param logPattern
	 */
	public final void setLogPattern(final String logPattern) {
		this.logPattern = logPattern;
	}



}
