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
	private static final long SECONDS_OF_A_DAY = (long) 24 * 60 * 60;

	private static ScheduledExecutorService sch = null;

	/**
	 * Lanza la ejecuci&oacute;n de la carga de datos de los fichero de estad&iacute;sticas a la
	 * base de datos, a la hora indicada por par&aacute;metro.  Este metodo y el m&eacute;todo
	 * {@link #dumpData(String, String, String, String, String, boolean)} nunca se deberian
	 * ejecutar en el mismo contexto.
	 * @param dataPath Ruta del directorio con los datos estad&iacute;sticos.
	 * @param startTime Hora a la que realizar el volcado a base de datos. Si no se indica, se usar&aacute; la 00:00:00.
	 * @param datasourceJndiName Nombre del datasource para el acceso a base de datos.
	 * @param processCurrentDay Indica si se deben procesar tambi&eacute;n los datos del d&iacute;a actual.
	 * @return Si la operaci&oacute;n se ejecuta de inmediato, el resultado de la operaci&oacute;n. Si no, {@code null}.
	 * @throws IOException Cuando falla la inicializacion de la tarea.
	 */
	public static final void init(final String dataPath, final String startTime, final String datasourceJndiName, final boolean processCurrentDay)
					throws IOException {

		LOGGER.info("Programamos el volcado recurrente de las estadisticas"); //$NON-NLS-1$

		if (dataPath == null) {
			throw new NullPointerException("No se ha indicado la ruta del directorio con los ficheros de datos estadisticos"); //$NON-NLS-1$
		}

		if (startTime == null) {
			throw new NullPointerException("No se ha indicado la hora de arranque del volcado de los ficheros de datos estadisticos"); //$NON-NLS-1$
		}

		// Paramos cualquier tarea anterior que pudiese estar ejecutandose
		shutdownTasks();

		// Se crea una tarea para la carga de los datos estadisticos
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

		// Si no ha pasado la hora, el valor sera negativo e indicara el tiempo que queda
		// Si ya paso, tendremos que esperar un dia menos el tiempo que ya ha pasado
		return secondsPassed < 0 ? -secondsPassed : SECONDS_OF_A_DAY - secondsPassed;
	}

	/**
	 * Libera los recursos reservados al ejecutar el m&eacute;todo
	 * {@link #init(String, String, String, boolean)}.
	 */
	public static void release() {
		LOGGER.info("Liberamos los recursos del servicio"); //$NON-NLS-1$

		// Paramos las tareas
		shutdownTasks();

		// Liberamos los recursos de base de datos que hayan usado los hilos
		try {
			DbManager.closeResources();
		}
		catch (final Throwable e) {
			LOGGER.log(Level.WARNING, "Error al liberar la conexion de base de datos para el guardado de datos estadisticos", e); //$NON-NLS-1$
		}
	}

	private static void shutdownTasks() {
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
	}
}
