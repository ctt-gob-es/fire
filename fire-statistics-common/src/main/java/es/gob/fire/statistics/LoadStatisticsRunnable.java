package es.gob.fire.statistics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import es.gob.fire.statistics.config.DBConnectionException;
import es.gob.fire.statistics.config.DbManager;
import es.gob.fire.statistics.dao.SignaturesDAO;
import es.gob.fire.statistics.dao.TransactionsDAO;
import es.gob.fire.statistics.entity.SignatureCube;
import es.gob.fire.statistics.entity.TransactionCube;
import es.gob.fire.statistics.entity.TransactionTotal;

public class LoadStatisticsRunnable implements Runnable {

	private static final Logger LOGGER = Logger.getLogger(LoadStatisticsRunnable.class.getName());

	private static final String FILE_EXT_LOG = ".log"; //$NON-NLS-1$

	private static final String FILE_STATISTICS_NAME = "FIRe_STATISTICS.log"; //$NON-NLS-1$

	private static final String FILE_SIGN_PREFIX = "FIRE_SIGNATURE_";//$NON-NLS-1$
	private static final String FILE_TRANS_PREFIX = "FIRE_TRANSACTION_";//$NON-NLS-1$

	private final static SimpleDateFormat formatter =  new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$

	private final String dataPath;
	private final boolean processCurrentDay;

	private LoadStatisticsResult result = null;

	/**
	 * Crea la tarea indicando el directorio de los ficheros de datos estadisticos y que no se
	 * quiere procesar la informaci&oacute;n de hoy, ya que puede que a&uacute;n no hayan terminado
	 * de generarse todos los datos.
	 * @param dataPath Ruta de los ficheros de datos.
	 */
	public LoadStatisticsRunnable(final String dataPath) {
		this(dataPath, false);
	}

	/**
	 * Crea la tarea indicando el directorio de los ficheros de datos estadisticos y si se desea
	 * procesar la informaci&oacute;n de hoy, ya que puede que a&uacute;n no hayan terminado
	 * de generarse todos los datos.
	 * @param processCurrentDay Indica si se debe procesar los datos del d&iacute;a actual
	 * ({@code true}) o si no ({@code false}).
	 */
	public LoadStatisticsRunnable(final String dataPath, final boolean processCurrentDay) {
		this.dataPath = dataPath;
		this.processCurrentDay = processCurrentDay;
	}

	@Override
	public void run() {

		LOGGER.log(Level.INFO, "Se inicia la tarea de volcado de estadisticas"); //$NON-NLS-1$

		// Identificamos la fecha de los ultimos ficheros que se cargaron
		Date lastDateLoaded;
		try {
			lastDateLoaded = getLastLoadedStatisticsDate();
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "No se pudo cargar la fecha de los ultimos datos cargados en base de datos", e); //$NON-NLS-1$
			return;
		}

		// Cargamos los nombre de los ficheros de firma y transaccion que aun estan pendientes
		// de cargar en base de datos
		File[] signatureFiles;
		File[] transaccionFiles;
		try {
			signatureFiles = getPendingDataFiles(FILE_SIGN_PREFIX, lastDateLoaded);
			transaccionFiles = getPendingDataFiles(FILE_TRANS_PREFIX, lastDateLoaded);
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "No se pudieron cargar los ficheros de datos a procesar", e); //$NON-NLS-1$
			return;
		}

		// Incluimos en cada listado los elementos del otro de los que este carezca para que las fechas vayan acompasadas
		try {
			signatureFiles = includeMissingElements(signatureFiles, FILE_SIGN_PREFIX, transaccionFiles, FILE_TRANS_PREFIX);
			transaccionFiles = includeMissingElements(transaccionFiles, FILE_TRANS_PREFIX, signatureFiles, FILE_SIGN_PREFIX);
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "No se pudieron acompasar las fechas de los listados de ficheros", e); //$NON-NLS-1$
			return;
		}

		// Si no hay ficheros de datos, terminamos la ejecucion
		if (signatureFiles.length == 0 && transaccionFiles.length == 0) {
			LOGGER.info("No se han encontrado nuevos ficheros de datos estadisticos que cargar"); //$NON-NLS-1$
			return;
		}

		// Cargamos los ficheros en base de datos
		try {
			this.result = exeLoadStatistics(signatureFiles, transaccionFiles);
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "No ha sido posible cargar todos los datos en base de datos", e); //$NON-NLS-1$
			return;
		}

		// Registramos la fecha de los ultimos datos que se han podido cargar en base de datos
		if (this.result.getLastDateProcessedText() != null) {
			try {
				saveLastDateProcessed(this.result.getLastDateProcessedText());
			}
			catch (final Exception e) {
				LOGGER.log(Level.SEVERE, "Se han cargado datos de estadistica en la base de datos " + //$NON-NLS-1$
						"pero no se ha podido registrar en disco la fecha de los ultimos datos insertados. " + //$NON-NLS-1$
						"Futuras operaciones pueden insertar datos repetidos en la base de datos. La fecha " + //$NON-NLS-1$
						"de los ultimos datos insertados es: " + this.result.getLastDateProcessed(), e); //$NON-NLS-1$
			}
		}

		// Si todo ha finalizado correctamente, informamos de ello
		if (this.result.isCorrect()) {
			LOGGER.info("Se han cargado correctamente todos los ficheros de estadisticas en la base de datos"); //$NON-NLS-1$
		}
		else if (this.result.getLastDateProcessedText() != null) {
			LOGGER.warning("No se pudieron cargar todos los ficheros de las estadisticas en la base de datos. " //$NON-NLS-1$
					+ "Los ultimos datos cargados son los del dia " + this.result.getLastDateProcessedText()); //$NON-NLS-1$
		}
		else {
			LOGGER.warning("No se pudo cargar ningun fichero de las estadisticas en la base de datos"); //$NON-NLS-1$
		}
	}

	/**
	 * Obtiene cual es la fecha m&aacute;s reciente de datos cargados.
	 * @return Fecha de los &uacute;ltimos datos cargados o {@code null}
	 * si no se tiene referencia de que se hayan cargado datos.
	 * @throws IOException Si se produce un error en la lectura del fichero de registro de datos.
	 */
	private final Date getLastLoadedStatisticsDate() throws IOException {

		final File infoFile = new File(this.dataPath, FILE_STATISTICS_NAME);

		// Si no se encuentra el fichero, se considera que nunca se han cargado datos
		if (!infoFile.isFile()) {
			return null;
		}

		String dateText;
		try {
			dateText = readOneLine(infoFile);
		}
		catch (final Exception e) {
			throw new IOException("No se pudo leer el fichero con la fecha de los datos insertados por ultima vez", e); //$NON-NLS-1$
		}

		Date date;
		try {
			date = formatter.parse(dateText);
		}
		catch (final Exception e) {
			throw new IOException("La fecha del fichero de registro de inserciones no es valida", e); //$NON-NLS-1$
		}
		return date;
	}

	/**
	 * Lee la &uacute;ltima l&iacute;nea de un fichero.
	 * @param file Fichero de datos.
	 * @return &Uacute;ltima linea del fichero o {@code null} si no tiene contenido.
	 */
	private static String readOneLine(final File file) throws IOException {
		String line;
		try (	FileReader fileReader = new FileReader(file);
				BufferedReader input = new BufferedReader(fileReader)) {
			line = input.readLine();
		}
		return line;
	}

	/**
	 * Obtiene los ficheros del directorio de logs cuyo nombre empiece por un sufijo concreto y
	 * que esten seguidos por una fecha mayor que la indicada.
	 * @param suffix Sufijo de los ficheros.
	 * @param date Fecha m&iacute;nima que deben mostrar los ficheros en su nombre (no incluida).
	 * @return Listado de ficheros.
	 */
	private File[] getPendingDataFiles(final String suffix, final Date date) {

		final File[] dataFiles = new File(this.dataPath).listFiles(new DataStatisticsFileFilter(suffix, date, formatter, this.processCurrentDay));

		Arrays.sort(dataFiles, new Comparator<File>() {
			@Override
			public int compare(final File f1, final File f2) {
				return f1.getName().compareTo(f2.getName());
			}
		});

		return dataFiles;
	}

	/**
	 * Compara los elementos fuente y objetivo, cuyos elementos deben estar ordenados
	 * alfab&eacute;ticamente. El listado resultante tendr&aacute; los elementos del
	 * listado fuente y un nulo en aquellas posiciones del listado que le falten.
	 * La comparaci&oacute;n entre elementos de uno y otro se hace seg&uacute;n el texto
	 * tras el prefijo.
	 * @param sourceFiles Listado de ficheros que se debe completar.
	 * @param sourceFilesPrefix Prefijo de los nombres de fichero del listado fuente.
	 * @param targetFiles Listado de ficheros con los elementos que deberia incluir el listado
	 * fuente. Podr&iacute;a contener nulos.
	 * @param targetFilesPrefix Prefijo de los nombres de fichero del listado objetivo.
	 * @return Listado de ficheros fuentes con los nulos necesarios para ocupar las posiciones
	 * correspondientes a los elementos del listado destino que le faltan.
	 */
	private static File[] includeMissingElements(final File[] sourceFiles, final String sourceFilesPrefix,
			final File[] targetFiles, final String targetFilesPrefix) {

		final List<File> sourceFilesList = new ArrayList<>();

		int sourceIdx = 0;
		int targetIdx = 0;

		// Cargamos las fechas del listado objetivo
		final String[] targetDates = loadDatesList(targetFiles, targetFilesPrefix, FILE_EXT_LOG);

		// Compondremos el listado de ficheros mientras no hayamos incorporado un elemento correspondiente
		// a cada una de las fechas encontradas entre ambos listados
		while (sourceIdx < sourceFiles.length || targetIdx < targetDates.length) {

			// Si se agoto ya el listado de ficheros fuentes, hacemos una entrada nula por cada
			// fecha objetivo que quede
			if (sourceIdx >= sourceFiles.length) {
				for (; targetIdx < targetDates.length; targetIdx++) {
					sourceFilesList.add(null);
				}
			}
			// Si se agoto ya el listado de fechas objetivo, solo debemos volcar los ficheros
			// fuentes que queden
			else if (targetIdx >= targetDates.length) {
				for (; sourceIdx < sourceFiles.length; sourceIdx++) {
					sourceFilesList.add(sourceFiles[sourceIdx]);
				}
			}
			// Si aun quedan de ambos listados, introducimos el elemento que corresponda
			else {
				final String sourceDate = getDateFromName(sourceFiles[sourceIdx].getName(), sourceFilesPrefix, FILE_EXT_LOG);
				final int dateComparation = sourceDate.compareTo(targetDates[targetIdx]);
				// El elemento solo esta en el listado origen. Lo introducimos y avanzamos ese listado
				if (dateComparation < 0) {
					sourceFilesList.add(sourceFiles[sourceIdx]);
					sourceIdx++;
				}
				// El elemento solo esta en el listado destino. Dejamos el hueco y avanzamos en ese listado
				else if (dateComparation > 0) {
					sourceFilesList.add(null);
					targetIdx++;
				}
				// El elemento esta en ambos listados. Lo introducimos y avanzamos en ambos
				else {
					sourceFilesList.add(sourceFiles[sourceIdx]);
					sourceIdx++;
					targetIdx++;
				}
			}
		}

		return sourceFilesList.toArray(new File[0]);
	}

	private static String[] loadDatesList(final File[] files, final String prefix, final String suffix) {
		final List<String> dates = new ArrayList<>();
		for (final File file : files) {
			if (file != null) {
				final String filename = file.getName();
				if (filename.startsWith(prefix)  && filename.endsWith(suffix)) {
					dates.add(getDateFromName(filename, prefix, suffix));
				}
			}
		}
		return dates.toArray(new String[0]);
	}

	private static String getDateFromName(final String filename, final String prefix, final String suffix) {
		return filename.substring(prefix.length(), filename.length() - suffix.length());
	}

	/**
	 * Carga en base de datos los datos estad&iacute;sticos de los ficheros encontrados. Los listados
	 * recibidos ya deberian estar depurados de tal forma que tengan los mismos elementos, rellenando
	 * con nulos de ser necesario, y que en cada posicion se encuentren los elementos de una misma
	 * fecha.
	 * @param signatureFiles Ficheros con los datos de las firmas ejecutadas.
	 * @param transactionFiles Ficheros con los datos de las transacciones ejecutadas.
	 * @return Resultado del proceso de carga.
	 */
	private static LoadStatisticsResult exeLoadStatistics(final File[] signatureFiles, final File[] transactionFiles) {

		Date lastDateProcessed = null;
		String lastDateProcessedText = null;

		for (int i = 0; i < Math.min(signatureFiles.length, transactionFiles.length); i++) {

			// Si alguno es nulo, omitimos el registro del fichero de esta distintas asociados
			if (signatureFiles[i] == null || transactionFiles[i] == null) {
				continue;
			}

			// Por orden, procesamos cada pareja de ficheros, cuidando que tengamos ambos ficheros
			// para cada una de las fechas encontradas (lo que deberia ocurrir siempre al haberse
			// preprocesado los listados)
			final int c = signatureFiles[i].getName().substring(FILE_SIGN_PREFIX.length()).compareTo(
					transactionFiles[i].getName().substring(FILE_TRANS_PREFIX.length()));
			if (c != 0) {
				String errorMsg;
				if (c < 0) {
					errorMsg = "No se ha encontrado el fichero con los datos de las transacciones correspondiente a la fecha " + //$NON-NLS-1$
							parseDateStringFromFilename(signatureFiles[i], FILE_SIGN_PREFIX);
				} else {
					errorMsg = "No se ha encontrado el fichero con los datos de las firmas correspondiente a la fecha " + //$NON-NLS-1$
							parseDateStringFromFilename(transactionFiles[i], FILE_TRANS_PREFIX);
				}
				LOGGER.severe(errorMsg);
				return new LoadStatisticsResult(false, lastDateProcessed, lastDateProcessedText, errorMsg);
			}

			// Identificamos la fecha de los ficheros que estamos procesando
			final String dateText = parseDateStringFromFilename(signatureFiles[i], FILE_SIGN_PREFIX);
			Date date;
			try {
				date = formatter.parse(dateText);
			} catch (final Exception e) {
				final String errorMsg = "Se encontro un fichero con una fecha no valida: " + signatureFiles[i]; //$NON-NLS-1$
				LOGGER.severe(errorMsg);
				return new LoadStatisticsResult(false, lastDateProcessed, lastDateProcessedText, errorMsg);
			}

			// Extraemos la informacion de los ficheros
			CompactedData compactedData;
			try {
				compactedData = extractData(signatureFiles[i], transactionFiles[i]);
			}
			catch (final Exception e) {
				final String errorMsg = "Ocurrio un error al extraer los datos de los ficheros del dia " + dateText; //$NON-NLS-1$
				LOGGER.log(Level.SEVERE, errorMsg, e);
				return new LoadStatisticsResult(false, lastDateProcessed, lastDateProcessedText, errorMsg);
			}

			// Insertamos los datos en base de datos
			try {
				insertDataIntoDb(date, compactedData);
			}
			catch (final DBConnectionException e) {
				final String errorMsg = "No se pudo conectar con la base de datos. Se aborta el proceso de carga de los datos del dia " + dateText; //$NON-NLS-1$
				LOGGER.log(Level.SEVERE, errorMsg, e);
				return new LoadStatisticsResult(false, lastDateProcessed, lastDateProcessedText, errorMsg);
			}
			catch (final Exception e) {
				final String errorMsg = "Ocurrio un error al guardar los datos del dia " + dateText; //$NON-NLS-1$
				LOGGER.log(Level.SEVERE, errorMsg, e);
				return new LoadStatisticsResult(false, lastDateProcessed, lastDateProcessedText, errorMsg);
			}

			// Actualizamos la fecha de los ultimos datos procesados
			lastDateProcessed = date;
			lastDateProcessedText = dateText;
		}

		return new LoadStatisticsResult(true, lastDateProcessed, lastDateProcessedText);
	}

	/**
	 * Funci&oacute;n que prepara los registros de los ficheros log pasados por par&aacute;metro
	 * para ser insertados en la BBDD. Si se encuentra algun registro mal formado, se ignorar&aacute;.
	 * @param signaturesFile Fichero con los datos de firma.
	 * @param transactionsFile Fichero con los datos de transacci&oacute;n.
	 * @return Datos extra&iacute;dos de los ficheros.
	 * @throws IOException Cuando no se pueden leer los datos de los ficheros indicados.
	 */
	private static CompactedData extractData(final File signaturesFile, final File transactionsFile)
			throws IOException {

		final CompactedData compactedData = new CompactedData();

		// Procesamos el fichero de datos de firma
		try (final FileReader fr = new FileReader(signaturesFile);

				final BufferedReader br = new BufferedReader(fr);) {


			String registry;
			while ((registry = br.readLine()) != null) {
				if (registry.trim().isEmpty()) {
					continue;
				}

				// Se obtiene el objeto SignatureCube del registro leido
				SignatureCube signCube;
				try {
					signCube =  SignatureCube.parse(registry);
				}
				catch (final Exception e) {
					LOGGER.warning(String.format("Error al cargar un registro de firma del fichero %1s: %2s", signaturesFile.getAbsolutePath(), e.getMessage())); //$NON-NLS-1$
					continue;
				}

				// Se inserta en el conjunto de datos
				compactedData.addSignatureData(signCube);
			}
		}

		// Procesamos el fichero de transacciones
		try (final FileReader fr = new FileReader(transactionsFile);
				final BufferedReader br = new BufferedReader(fr);) {

			String registry;
			while ((registry = br.readLine()) != null) {
				if (registry.trim().isEmpty()) {
					continue;
				}

				TransactionCube transCube;
				try {
					transCube =  TransactionCube.parse(registry);
				}
				catch (final Exception e) {
					LOGGER.warning(String.format("Error al cargar un registro de transaccion del fichero %1s: %2s", transactionsFile.getAbsolutePath(), e.getMessage())); //$NON-NLS-1$
					continue;
				}

				// Se inserta en el conjunto de datos
				compactedData.addTransactionData(transCube);
			}
		}

		return compactedData;
	}

	/**
	 * Inserta la informacion de las firmas y transacciones de un d&iacute;a en base de datos.
	 * @param date Fecha en la que se realizaron las operaciones.
	 * @param compactedData Conjunto de datos de las operaciones.
	 * @throws SQLException Cuando se produce un error al insertar los datos.
	 * @throws DBConnectionException Cuando se produce un error de conexi&oacute;n con la base de datos.
	 */
	private static void insertDataIntoDb(final Date date, final CompactedData compactedData)
			throws SQLException, DBConnectionException {

		// Insertamos la informacion de las firmas realizadas
		final Map<SignatureCube, Long> signaturesCube = compactedData.getSignatureData();

		final Iterator<SignatureCube> itSigns = signaturesCube.keySet().iterator();
		try (Connection conn = DbManager.getInstance().getConnection(false)) {
			while (itSigns.hasNext()) {

				final SignatureCube signatureConfig = itSigns.next();
				final Long total = signaturesCube.get(signatureConfig);
				try {
					SignaturesDAO.insertSignature(date, signatureConfig, total.longValue(), conn);
				}
				catch (final Exception e) {
					LOGGER.log(Level.SEVERE,
							String.format("No se pudo insertar las firmas del dia %1s. Se desharan las inserciones realizadas de este dia", new SimpleDateFormat("dd/MM/yyyy").format(date)), //$NON-NLS-1$ //$NON-NLS-2$
							e);
					try {
						conn.rollback();
					} catch (final Exception e1) {
						LOGGER.log(Level.WARNING, "No se pudieron deshacer las inserciones ya realizadas", e1); //$NON-NLS-1$
					}
					throw e;
				}
			}

			// Insertamos la informacion de las transacciones realizadas
			final Map<TransactionCube, TransactionTotal> transactionsCube = compactedData.getTransactionData();

			final Iterator<TransactionCube> itTrans = transactionsCube.keySet().iterator();
			while (itTrans.hasNext()) {
				final TransactionCube transactionConfig = itTrans.next();
				final TransactionTotal total = transactionsCube.get(transactionConfig);
				try {
					TransactionsDAO.insertTransaction(date, transactionConfig, total, conn);
				}
				catch (final Exception e) {
					LOGGER.log(Level.SEVERE,
							String.format("No se pudo insertar las transacciones del dia %1s. Se desharan las inserciones realizadas de este dia", new SimpleDateFormat("dd/MM/yyyy").format(date)), //$NON-NLS-1$ //$NON-NLS-2$
							e);
					try {
						conn.rollback();
					} catch (final Exception e1) {
						LOGGER.log(Level.WARNING, "No se pudieron deshacer las inserciones ya realizadas", e1); //$NON-NLS-1$
					}
					throw e;
				}
			}

			try {
				conn.commit();
			} catch (final Exception e1) {
				LOGGER.log(Level.WARNING, "No se pudieron confirmar las inserciones ya realizadas", e1); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Actualiza la fecha del fichero de informaci&oacute;n para indicar la fecha de los
	 * &uacute;ltimos registros cargados en base de datos.
	 * @param lastDateProcessed Fecha de los &uacute;ltimos
	 * @throws IOException Cuando se produce un error al actualizar la fecha.
	 */
	private void saveLastDateProcessed(final String lastDateProcessed) throws IOException {

		final File infoFile = new File(this.dataPath, FILE_STATISTICS_NAME);

		try (FileWriter writer = new FileWriter(infoFile, false);) {
			writer.write(lastDateProcessed);
		}
	}

	/**
	 * Extrae el texto que identifica la fecha a la que pertenecen los datos de un fichero.
	 * @param file Fichero del que obtener la fecha.
	 * @param suffix Sufijo que se antepone a la fecha en el nombre del fichero.
	 * @return Texto con la fecha de los datos.
	 */
	private static String parseDateStringFromFilename(final File file, final String suffix) {
		final String name = file.getName();
		return name.substring(suffix.length(), name.length() - FILE_EXT_LOG.length());
	}

	/**
	 * Permite obtener el resultado de la operaci&oacute;n de carga una vez ha terminado
	 * la actividad.
	 * @return Resultado de la operaci&oacute;n de carga.
	 */
	public LoadStatisticsResult getResult() {
		return this.result;
	}


	/**
	 * Filtro de ficheros que devuelve &uacute;nicamente aquellos ficheros de datos
	 * de firma y transacci&oacute;n.
	 */
	private static class DataStatisticsFileFilter implements java.io.FileFilter {

		private final String fileSuffix;
		private final Date date;
		private final SimpleDateFormat dateFormatter;
		private final boolean processCurrentDay;
		private final long todayMillis;

		public DataStatisticsFileFilter(final String suffix, final Date initialDate,
				final SimpleDateFormat formatter, final boolean processCurrentDay) {
			this.fileSuffix = suffix;
			this.date = initialDate;
			this.dateFormatter = formatter;
			this.processCurrentDay = processCurrentDay;

			this.todayMillis = getTodayMilis();
		}

		@Override
		public boolean accept(final File pathname) {
			if (!pathname.isFile()) {
				return false;
			}

			final String name = pathname.getName();
			if (!name.endsWith(FILE_EXT_LOG) || !name.startsWith(this.fileSuffix)) {
				return false;
			}

			final String dateText = name.substring(this.fileSuffix.length(), name.length() - FILE_EXT_LOG.length());

			Date dataDate;
			try {
				dataDate = this.dateFormatter.parse(dateText);
			} catch (final Exception e) {
				return false;
			}

			// Mostraremos el fichero si no se indico la fecha de los documentos ya procesados o
			// si la fecha de los datos es posterior a esa fecha. Tambien se tiene en cuenta que,
			// si no se debe procesar el dia de hoy, se evitara procesar el fichero de hoy y el de
			// cualquier dia subsiguiente

			return (this.date == null || dataDate.getTime() > this.date.getTime()) &&
					(this.processCurrentDay || dataDate.getTime() < this.todayMillis);
		}

		/** Obtiene el los milisegundos correspondiente al primer instante del d&iacute;a de hoy.
		 * @return Milisegundos del primer instante del d&iacute;a de hoy.
		 */
		private static long getTodayMilis() {

			// Tomamos el primer instante de este dia
			final Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);

			return calendar.getTimeInMillis();
		}
	}
}

