package es.gob.fire.statistics.cmd;

import es.gob.fire.statistics.FireStatistics;
import es.gob.fire.statistics.cmd.config.ConfigManager;

/**
 * Clase para la carga de estad&iacute;sticas de FIRe en base de datos.
 */
public class StatisticsDBLoader {

	private static final String JAR_NAME = "FireStatictDbLoader.jar"; //$NON-NLS-1$

	private static final String PARAM_USE_CONFIG = "-useConfigFile"; //$NON-NLS-1$

	private static final String PARAM_PROCESS_CURRENT_DAY = "-processCurrentDay"; //$NON-NLS-1$

	private static final String PARAM_DATA_DIR = "-dir"; //$NON-NLS-1$

	private static final String PARAM_DB_DRIVER = "-driver"; //$NON-NLS-1$

	private static final String PARAM_DB_CONNECTION = "-conn"; //$NON-NLS-1$

	public static void main(final String[] args) {

		// Comprobamos si se pide mostrar la ayuda
		if (args.length == 1 && (args[0].equals("help") || args[0].equals("-help") ||  //$NON-NLS-1$ //$NON-NLS-2$
				args[0].equals("--help") ||  args[0].equals("-h") || args[0].equals("?"))) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			showHelp();
			return;
		}

		boolean useFileConfig = false;
		boolean processCurrentDay = false;
		String dataDir = null;
		String dbDriver = null;
		String dbConnection = null;

		for (int i = 0; i < args.length; i++) {

			switch (args[i]) {
			case PARAM_USE_CONFIG:
				useFileConfig = true;
				break;

			case PARAM_PROCESS_CURRENT_DAY:
				processCurrentDay = true;
				break;

			case PARAM_DATA_DIR:
				if (i < args.length - 1) {
					dataDir = args[++i];
					break;
				}

			case PARAM_DB_DRIVER:
				if (i < args.length - 1) {
					dbDriver = args[++i];
					break;
				}

			case PARAM_DB_CONNECTION:
				if (i < args.length - 1) {
					dbConnection = args[++i];
					break;
				}

			default:
				showHelp();
				return;
			}
		}

		// Si no se ha indicado configuracion ni se ha petido usar el fichero del componente central,
		// entonces preguntamos por consola
		if (!useFileConfig && dataDir == null && dbDriver == null && dbConnection == null) {
			System.out.println("Desea utilizar el fichero de configuracion de FIRe para cargar los datos? [s/N]"); //$NON-NLS-1$
			final String r = System.console().readLine();
			if (!r.equalsIgnoreCase("s")) { //$NON-NLS-1$
				System.out.println("Se cancela la carga de datos.\n"); //$NON-NLS-1$
				showHelp();
				return;
			}
			useFileConfig = true;
		}

		//

		// Comprobamos si se indica usar expresamente el fichero de configuracion
		if (useFileConfig) {
			init(processCurrentDay);
			return;
		}

		// Comprobamos si se nos pasa la configuracion especifica para ejecutar el proceso
		if (dataDir != null && dbDriver != null && dbConnection != null ) {
			init(dataDir, dbDriver, dbConnection, processCurrentDay);
			return;
		}

		// En caso contrario, mostramos el mensaje de error
		showHelp();
	}

	/**
	 * Iniciamos la carga de datos utilizando la informaci&oacute;n del fichero de configuraci&oacute;n.
	 * @param processCurrentDay Indica si se deben procesar los datos del d&iacute;a actual.
	 */
	private static void init(final boolean processCurrentDay) {

		try {
			ConfigManager.checkConfiguration("config.properties"); //$NON-NLS-1$
		}
		catch (final Exception e) {
			System.err.println("Error al cargar la configuracion"); //$NON-NLS-1$
			return;
		}

		final String dataDir = ConfigManager.getStatisticsDir();
		final String dbDriver = ConfigManager.getJdbcDriverString();
		final String dbConn = ConfigManager.getDataBaseConnectionString();

		init(dataDir, dbDriver, dbConn, processCurrentDay);
	}

	/**
	 * Iniciamos la carga de datos utilizando la configuraci&oacute;n proporcionada.
	 * @param dataDir Ruta absoluta del directorio con los ficheros de datos.
	 * @param dbDriver Clase del controlador JDBC.
	 * @param dbConn Cadena de conexi&oacute;n con la base de datos.
	 * @param processCurrentDay Indica si se deben procesar los datos del d&iacute;a actual.
	 */
	private static void init(final String dataDir, final String dbDriver, final String dbConn,
			final boolean processCurrentDay) {
		FireStatistics.init(dataDir, null, dbDriver, dbConn, processCurrentDay);
	}

	/**
	 * Muestra por consola la ayuda de la aplicaci&oacute;n.
	 */
	private static void showHelp() {
		System.out.println("Aplicacion para la carga inmediata de estadisticas de FIRe en base de datos.\n"); //$NON-NLS-1$

		System.out.println("Dependencias:"); //$NON-NLS-1$
		System.out.println("\tEsta aplicacion requiere de un controlador JDBC para conectar con su " //$NON-NLS-1$
				+ "base de datos. Configure su ClassPath para tener acceso al controlador desde esta " //$NON-NLS-1$
				+ "aplicacion o copielo al mismo directorio que este JAR con el nombre 'jdbc.jar'.\n"); //$NON-NLS-1$

		System.out.println("Uso:"); //$NON-NLS-1$
		System.out.println(String.format("\tjava -cp [Ruta_JDBC] -jar %1s [Opciones]\n", JAR_NAME)); //$NON-NLS-1$

		System.out.println("Ruta_JDBC:"); //$NON-NLS-1$

		System.out.println("\tRuta al JAR controlador JDBC de nuestra base de datos.\n"); //$NON-NLS-1$

		System.out.println("Opciones:"); //$NON-NLS-1$

		System.out.println("\t" + PARAM_USE_CONFIG); //$NON-NLS-1$
		System.out.println("\t\tCarga los datos en BD usando el fichero de configuracion de FIRe. " + //$NON-NLS-1$
				String.format("Si no se usa se deben usar las opciones %1s, %2s y %3s.\n", //$NON-NLS-1$
						PARAM_DATA_DIR, PARAM_DB_DRIVER, PARAM_DB_CONNECTION));

		System.out.println("\t" + PARAM_DATA_DIR); //$NON-NLS-1$
		System.out.println("\t\tRuta absoluta del directorio con los ficheros de datos estadisticos.\n"); //$NON-NLS-1$

		System.out.println("\t" + PARAM_DB_DRIVER); //$NON-NLS-1$
		System.out.println("\t\tClase controladora JDBC para el acceso a la base de datos.\n"); //$NON-NLS-1$

		System.out.println("\t" + PARAM_DB_CONNECTION); //$NON-NLS-1$
		System.out.println("\t\tCadena de conexion con la base de datos.\n"); //$NON-NLS-1$

		System.out.println("\t" + PARAM_PROCESS_CURRENT_DAY); //$NON-NLS-1$
		System.out.println("\t\tEl proceso de carga incluye los datos de hoy (por defecto, no se hace). Esto provoca " //$NON-NLS-1$
				+ "que despues no se puedan registrar los datos generados el resto del dia.\n"); //$NON-NLS-1$

		System.out.println("Ejemplo:"); //$NON-NLS-1$
		System.out.println(String.format("\tjava -cp \"../lib/ojdbc6.jar\" -jar %1s %2s \"/usr/fire/statistics\" " //$NON-NLS-1$
				+ "%3s oracle.jdbc.driver.OracleDriver %4s " //$NON-NLS-1$
				+ "jdbc:oracle:thin:Fire/1111@XXX.XXX.XXX.XXX:1521:FIRE_DB", //$NON-NLS-1$
				JAR_NAME, PARAM_DATA_DIR, PARAM_DB_DRIVER, PARAM_DB_CONNECTION));
	}
}
