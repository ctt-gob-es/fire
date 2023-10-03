package es.gob.fire.statistics;

import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import es.gob.fire.signature.DbManager;



public class FireStatistics {

	static final Logger LOGGER = Logger.getLogger(FireStatistics.class.getName());

	/** Segundos en un d&iacute;a. */
	private static final long SECONDS_OF_A_DAY = 24 * 60 * 60;


	/** Ruta de los ficheros con los datos. */
	private static  String dataPath = null;

	/** Hora a la que iniciar diariamente la carga de los datos. */
	private static String startTime = null;

	private static ScheduledExecutorService sch = null;

	/**
	 * Lanza la ejecuci&oacute;n de la carga de datos de los fichero de estad&iacute;sticas a la
	 * base de datos, a la hora indicada por par&aacute;metro.  Este metodo y el m&eacute;todo
	 * {@link #dumpData(String, String, String, String, String, boolean)} nunca se deberian
	 * ejecutar en el mismo contexto.
	 * @param path Ruta del directorio con los datos estad&iacute;sticos.
	 * @param time Hora a la que realizar el volcado a base de datos. Si no se indica, se usar&aacute; la 00:00:00.
	 * @param jdbcDriver Clase controladora JDBC para la conexi&oacute;n con la base de datos.
	 * @param dbConnectionString Cadena de conexi&oacute;on a la base de datos.
	 * @param username Nombre de usuario con el que conectarse a la base de datos.
	 * @param password Contrase&ntilde;a del usuario.
	 * @param processCurrentDay Indica si se deben procesar tambi&eacute;n los datos del d&iacute;a actual.
	 * @return Si la operaci&oacute;n se ejecuta de inmediato, el resultado de la operaci&oacute;n. Si no, {@code null}.
	 * @throws IOException Cuando falla la inicializacion de la tarea.
	 */
	public static final void init(final String path, final String time, final String jdbcDriver,
			final String dbConnectionString, final String username, final String password, final boolean processCurrentDay)
					throws IOException {

		if (path == null) {
			throw new NullPointerException("No se ha indicado la ruta del directorio con los ficheros de datos estadisticos"); //$NON-NLS-1$
		}

		if (time == null) {
			throw new NullPointerException("No se ha indicado la hora de arranque del volcado de los ficheros de datos estadisticos"); //$NON-NLS-1$
		}

		dataPath = path;
		startTime = time;

		// Se crea una tarea para la carga de los datos de estadistica
		final LoadStatisticsRunnable loadStatisticsDataTask = new LoadStatisticsRunnable(dataPath, processCurrentDay);

		sch = Executors.newScheduledThreadPool(1);
		try {
			sch.scheduleAtFixedRate(loadStatisticsDataTask, getSecondsInitialDelay(startTime), SECONDS_OF_A_DAY, TimeUnit.SECONDS);
		}
		catch (final Exception e) {
			throw new IOException("No se pudo programar la tarea de volcado de estadisticas"); //$NON-NLS-1$
		}
	}

	/**
	 * Obtiene los segundos que se deben esperar para iniciar la carga de datos estad&iacute;sticos,
	 * a la hora indicada.
	 * @param time Hora del dia a la que iniciar el volcado en formato "hh:mm:ss".
	 * @return Segundos hasta la hora indicada.
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
		}
		catch (final Exception e) {
			LOGGER.warning("No se ha proporcionado una hora valida. Se iniciara el volcado a las '00:00': " + e); //$NON-NLS-1$
			hour = 0;
			minute = 0;
			second = 0;
		}

		final Calendar c = Calendar.getInstance();

		final long now = c.getTimeInMillis();

		c.set(Calendar.HOUR_OF_DAY, hour);
		c.set(Calendar.MINUTE, minute);
		c.set(Calendar.SECOND, second);
		c.set(Calendar.MILLISECOND, 0);

		final long millisPassed = now - c.getTimeInMillis();
		final long secondsPassed = millisPassed / 1000;

		// Si no ha pasado la hora, el valor sera ngeativo e indicara el tiempo que queda
		// Si ya paso, tendremos que esperar un dia menos el tiempo que ya ha pasado
		return secondsPassed < 0 ? -secondsPassed : SECONDS_OF_A_DAY - secondsPassed;
	}

	/**
	 * Libera los recursos reservados al ejecutar el m&eacute;todo
	 * {@link #init(String, String, String, String, String, String, boolean)}.
	 */
	public static void release() {
		if (sch != null) {
			try {
				sch.shutdown();
				try {
					if (!sch.awaitTermination(2000, TimeUnit.MILLISECONDS)) {
						sch.shutdownNow();
					}
				} catch (final InterruptedException e) {
					sch.shutdownNow();
				}
			}
			catch (final Throwable e) {
				LOGGER.log(Level.WARNING, "Error al cerrar la tarea de volcado periodico de estadisticas", e); //$NON-NLS-1$
			}
		}

		try {
			DbManager.closeResources();
		}
		catch (final Throwable e) {
			LOGGER.log(Level.WARNING, "Error al liberar la conexion de base de datos para el guardado de datos estadisticos", e); //$NON-NLS-1$
		}
	}


	/**
	 * Lanza la ejecuci&oacute;n de la carga de datos de los fichero de estad&iacute;sticas a la base de
	 * datos. Este metodo y el m&eacute;todo {@link #init(String, String, String, String, String, String, boolean)}
	 * nunca se deberian ejecutar en el mismo contexto.
	 * @param path Ruta del directorio con los datos estad&iacute;sticos.
	 * @param jdbcDriver Clase controladora JDBC para la conexi&oacute;n con la base de datos.
	 * @param dbConnectionString Cadena de conexi&oacute;on a la base de datos.
	 * @param username Nombre de usuario con el que conectarse a la base de datos.
	 * @param password Contrase&ntilde;a del usuario.
	 * @param processCurrentDay Indica si se deben procesar tambi&eacute;n los datos del d&iacute;a actual.
	 * @return Resultado de la carga.
	 * @throws IOException Si no es posible realizar la carga.
	 */
	public static final LoadStatisticsResult dumpData(final String path, final String jdbcDriver,
			final String dbConnectionString, final String username, final String password, final boolean processCurrentDay)
					throws IOException {

		if (path == null) {
			throw new NullPointerException("No se ha indicado la ruta del directorio con los ficheros de datos estadisticos"); //$NON-NLS-1$
		}

		if (jdbcDriver == null || dbConnectionString == null) {
			throw new NullPointerException("No se ha indicado la informacion necesaria para la comunicaci&oacute;n con la base de datos"); //$NON-NLS-1$
		}

		// Se crea una tarea para la carga de los datos de estadistica
		final LoadStatisticsRunnable loadStatisticsDataTask = new LoadStatisticsRunnable(dataPath, processCurrentDay);

		// Se ejecuta la tarea de forma sincrona (sin usar hilos)
		try {
			loadStatisticsDataTask.run();
		} catch (final Exception e) {
			throw new IOException("Fallo la ejecucion inmediata de la carga de los datos estadisticos", e); //$NON-NLS-1$
		}

		// Devolvemos el resultado que debe haber quedado registrado
		return loadStatisticsDataTask.getResult();
	}
}
