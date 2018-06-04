package es.gob.log.consumer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Clase para la obtenci&oacute;n de logs de un fichero filtrados en base a una
 * serie de criterios.
 */
public class LogFilter {

	private final LogRegistryReader registryReader;

	private LogReader logReader = null;

	private Criteria criteria;

	private boolean more = true;



	/**
	 * Crea el filtro de logs.
	 * @param logInfo Configuraci&oacute;n que se puede utilizar para determinar
	 * el formato de los logs.
	 * @throws InvalidPatternException Cuando en la informaci&oacute;n proporcionada se
	 * indique un patr&oacute;n de registro no v&aacute;lido.
	 */
	public LogFilter(final LogInfo logInfo) throws InvalidPatternException {
		this.registryReader = new LogRegistryReader(logInfo);
	}

	/**
	 * Carga lector de logs. Si se carg&oacute; alg&uacute;n otro anteriormente, se
	 * cierra antes de abrir el nuevo.
	 * @param reader Lector de logs.
	 * @throws IOException Cuando se produce un error durante la apertura del fichero.
	 * @throws InterruptedException Cuando la carga del fichero se ve interrumpida.
	 * @throws ExecutionException Cuando se produce un error al cargar el fichero.
	 */
	public void load(final LogReader reader)
			throws IOException, InterruptedException, ExecutionException {

		if (this.logReader != null) {
			this.logReader.close();
		}

		this.logReader = reader;
		this.logReader.setIgnoreEmptyLines(true);

		if(this.logReader.getFilePosition() <= 0L) {
			this.logReader.load();
		}


		this.registryReader.loadReader(this.logReader);

		this.more = true;
	}

	/**
	 * Establece los criterios de filtrado de los logs.
	 * @param criteria Criterios de consulta.
	 */
	public void setCriteria(final Criteria criteria) {
		this.criteria = criteria;
	}

	/**
	 * Obtiene una serie de entradas filtradas del log. El n&uacute;mero de l&iacute;neas
	 * puede no ser exacto, ya que, las entradas del log no se cortar&aacute;n a mitad, lo
	 * que puede provocar que, la &uacute;ltima entrada empiece dentro de l&iacute;neas
	 * permitidas pero incluya suficientes l&iacute;neas como para exceder el n&uacute;mero
	 * m&aacute;ximo.
	 * @param maxLines N&uacute;mero m&aacute;ximo de entradas.
	 * @return Fragmento filtrado del log.
	 * @throws IOException Cuando no se ha cargado el fcihero de log o cuando ocurre un
	 * error durante su lectura.
	 */
	public byte[] filter(final int maxLines) throws IOException {

		if (this.logReader == null) {
			throw new IOException("No se ha cargado un fichero de log"); //$NON-NLS-1$
		}

		final ByteArrayOutputStream baos = new ByteArrayOutputStream();

		// Leemos el numero de lineas solicitadas,
		int lines = 0;
		while (this.more && lines < maxLines) {
			final LogRegistry registry = this.registryReader.readRegistry();

			// Si hemos llegado al final del log, el resultado solo contendra los
			// registros obtenidos hasta el momento
			if (registry == null) {
				break;
			}

			// Si la entrada cumple los requisitos, la agregamos a la salida. Como una entrada
			// puede contener varias lineas, las contabilizamos. Nunca se devuelve un registro
			// a mitad, por lo que puede excederse el numero maximo de lineas indicado, si un
			// registro contiene multiples lineas
			if (checkCriteria(registry)) {
				baos.write(registry.getBytes(this.logReader.getCharset()));
				baos.write('\n');
				lines += registry.linesCount();
			}
		}

		return baos.toByteArray();
	}





	/**
	 * Comprueba si una entrada de log cumple con ciertos criterios de b&uacute;squeda.
	 * En caso de que la evaluaci&oacute;n del registro determine que ninguno de los
	 * siguientes va a cumplir los criterios, se activar&aacute; esta situaci&oacute;n
	 * que se podr&aacute; consultar con {@code #canHasMore()}.
	 * @param registry Registro que evaluar.
	 * @return {@code true} si el registro cumple el criterio designado por el filtro,
	 * {@code false} en caso contrario.
	 */
	private boolean checkCriteria(final LogRegistry registry) {

		// Si no se indican criterios de busqueda, se muestran todas las entradas
		if (this.criteria == null) {
			return true;
		}

		// El nivel del registro debe ser mayor o igual al nivel minimo solicitado
		if (this.criteria.getLevel() != Criteria.DEFAULT_LEVEL &&
				registry.getLevel() < this.criteria.getLevel()) {
			return false;
		}

		// La fecha salida del registro debe ser mayor o igual a la fecha minima solicitada
		if (this.criteria.getStartDate() != Criteria.DEFAULT_START_DATE &&
				registry.getCurrentMillis() < this.criteria.getStartDate()) {
			return false;
		}

		// La fecha salida del registro debe ser menor o igual a la fecha tope solicitada.
		// Si se excede esta fecha, ya ningun otro registro cumplira las condiciones
		if (this.criteria.getEndDate() != Criteria.DEFAULT_END_DATE &&
				registry.getCurrentMillis() > this.criteria.getEndDate()) {
			this.more = false;
			return false;
		}

		return true;
	}

	/**
	 * Indica si el fichero de log a&uacute;n puede incluir entradas que se ajusten
	 * al filtro en base al que se cargaron entradas la &uacte;tima vez.
	 * @return {@code true} si a&uacute;n puede incluir entradas, {@code false} en
	 * caso contrario.
	 */
	public boolean canHasMore() {
		return this.more;
	}

	/**
	 * Cierra el fichero de log.
	 * @throws IOException Si ocurre alg&uacute;n error durante el cierre.
	 */
	public void close() throws IOException{
		this.logReader.close();
	}

	/**
	 * Recupera la posici&oacute;n actual dentro del fichero.
	 * @return Posici&oacute;n en el fichero.
	 */
	public long getFilePosition() {
		return this.logReader.getFilePosition();
	}

}
