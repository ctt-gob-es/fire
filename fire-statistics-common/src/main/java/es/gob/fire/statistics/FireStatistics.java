package es.gob.fire.statistics;

import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


public class FireStatistics {

	static final Logger LOGGER = Logger.getLogger(FireStatistics.class.getName());

	/** Segundos en un d&iacute;a. */
	private static final long SECONDS_OF_A_DAY = 24 * 60 * 60;

	/** Ruta de los ficheros con los datos. */
	private static  String dataPath = null;

	/** Hora a la que iniciar diariamente la carga de los datos. */
	private static String startTime = null;

	/**
	 * Lanza la ejecuci&oacute;n de la carga de datos de los fichero de estad&iacute;sticas a la base de
	 * datos, a la hora indicada por par&aacute;metro.
	 * @param path Ruta del directorio con los datos estad&iacute;sticos.
	 * @param time Hora a la que realizar el volcado a base de datos.
	 * @param jdbcDriver Clase controladora JDBC para la conexi&oacute;n con la base de datos.
	 * @param dbConnectionString Cadena de conexi&oacute;on a la base de datos.
	 * @param processCurrentDay Indica si se deben procesar tambi&eacute;n los datos del d&iacute;a actual.
	 * @return Si la operaci&oacute;n se ejecuta de inmediato, el resultado de la operaci&oacute;n. Si no, {@code null}.
	 */
	public static final LoadStatisticsResult init(final String path, final String time, final String jdbcDriver,
			final String dbConnectionString, final boolean processCurrentDay) {

		dataPath = path;
		startTime = time;

		if (dataPath == null) {
			throw new NullPointerException("No se ha indicado la ruta del directorio con los ficheros de datos estadisticos"); //$NON-NLS-1$
		}

		if (jdbcDriver == null || dbConnectionString == null) {
			throw new NullPointerException("No se ha indicado la informacion necesaria para la comunicaci&oacute;n con la base de datos"); //$NON-NLS-1$
		}

		// Se crea una tarea para la carga de los datos de estadistica
		final LoadStatisticsRunnable loadStatisticsDataTask = new LoadStatisticsRunnable(dataPath, jdbcDriver, dbConnectionString, processCurrentDay);

		// Si se ha indicado una hora, se crea un hilo que se ejecutara cada dia a dicha hora.
		// Si no (caso de uso por consola), se ejecuta de inmediato la tarea en el hilo principal y se devuelve el resultado

		if (startTime != null) {
			final ScheduledThreadPoolExecutor sch = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1);
			sch.scheduleAtFixedRate(loadStatisticsDataTask, getSecondsInitialDelay(startTime), SECONDS_OF_A_DAY, TimeUnit.SECONDS);
			return null;
		}

		loadStatisticsDataTask.run();
		return loadStatisticsDataTask.getResult();

	}

	/**
	 * Obtiene los segundos que faltan para iniciar una nueva carga de datos estad&iacute;sticos,
	 * respecto al par&aacute;metro indicado en el fichero de configuraci&oacute;n
	 * @return
	 * @throws IllegalArgumentException Cuando se ha indicado una cadena no v&aacute;lida.
	 */
	private static long getSecondsInitialDelay(final String time) throws IllegalArgumentException {

		// Extraemos la hora, minutos y segundos del tiempo indicado
		final String [] start = time.split(":"); //$NON-NLS-1$
		int hour = 0;
		int minute = 0;
		int second = 0;

		try {
			hour = Integer.parseInt(start[0]);
			if (start.length > 1) {
				minute = Integer.parseInt(start[1]);
			}
			if (start.length > 2) {
				second = Integer.parseInt(start[2]);
			}

			final Calendar c = Calendar.getInstance();

			final long now = c.getTimeInMillis();

			c.set(Calendar.HOUR_OF_DAY, hour);
			c.set(Calendar.MINUTE, minute);
			c.set(Calendar.SECOND, second);
			c.set(Calendar.MILLISECOND, 0);

			final long millisPassed = now - c.getTimeInMillis();
			final long secondsPassed = millisPassed / 1000;

			// Si ya hoy ha pasado la hora, el valor sera negativo
			if (secondsPassed < 0) {
				return - secondsPassed;
			}
			return SECONDS_OF_A_DAY - secondsPassed;
		}
		catch (final Exception e) {
			throw new IllegalArgumentException("No se ha proporcionado una hora valida", e); //$NON-NLS-1$
		}

	}
}
