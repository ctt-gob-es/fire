package es.gob.fire.statistics.cmd;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import es.gob.fire.statistics.LoadStatisticsResult;
import es.gob.fire.statistics.LoadStatisticsRunnable;
import es.gob.fire.statistics.cmd.config.ConfigManager;

/**
 * Clase para la carga de estad&iacute;sticas de FIRe en base de datos.
 */
public class StatisticsDBLoader {

	private static final String PARAM_CONFIG_FILE_PATH = "-configFilePath"; //$NON-NLS-1$

	private static final String PARAM_PROCESS_CURRENT_DAY = "-processCurrentDay"; //$NON-NLS-1$

	private static final String PARAM_DATA_DIR = "-dir"; //$NON-NLS-1$

	private static final String PARAM_DB_DRIVER = "-driver"; //$NON-NLS-1$

	private static final String PARAM_DB_CONNECTION = "-conn"; //$NON-NLS-1$

	private static final String PARAM_DB_USERNAME = "-user"; //$NON-NLS-1$

	private static final String PARAM_DB_PASSWORD = "-pwd"; //$NON-NLS-1$

	private static final String DEFAULT_JAR_NAME = "FireStatisticsDbLoader.jar"; //$NON-NLS-1$

	private static String JAR_NAME;

	static {
		try {
			final Class<?> clazz = StatisticsDBLoader.class;
			final URL location = clazz.getProtectionDomain().getCodeSource().getLocation();
			JAR_NAME = new File(location.toURI()).getName();
		}
		catch (final Exception e) {
			JAR_NAME = DEFAULT_JAR_NAME;
		}
	}

	public static void main(final String[] args) {

		// Comprobamos si se pide mostrar la ayuda
		if (args.length == 1 && (args[0].equals("help") || args[0].equals("-help") ||  //$NON-NLS-1$ //$NON-NLS-2$
				args[0].equals("--help") ||  args[0].equals("-h") || args[0].equals("?"))) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			showHelp();
			return;
		}

		boolean processCurrentDay = false;
		String dataDir = null;
		String dbDriver = null;
		String dbConnection = null;
		String dbUsername = null;
		String dbPassword = null;
		String configFilePath = null;

		for (int i = 0; i < args.length; i++) {

			switch (args[i]) {
			case PARAM_CONFIG_FILE_PATH:
				if (i < args.length - 1) {
					configFilePath = args[++i];
				} else {
					System.out.println("No se ha indicado una ruta para el fichero de configuracion"); //$NON-NLS-1$
					showHelp();
				}
				break;

			case PARAM_PROCESS_CURRENT_DAY:
				processCurrentDay = true;
				break;

			case PARAM_DATA_DIR:
				if (i < args.length - 1) {
					dataDir = args[++i];
				} else {
					System.out.println("No se ha indicado el directorio con los ficheros de estadisticas"); //$NON-NLS-1$
					showHelp();
				}
				break;

			case PARAM_DB_DRIVER:
				if (i < args.length - 1) {
					dbDriver = args[++i];
				} else {
					System.out.println("No se ha indicado el driver para la conexion con la base de datos"); //$NON-NLS-1$
					showHelp();
				}
				break;

			case PARAM_DB_CONNECTION:
				if (i < args.length - 1) {
					dbConnection = args[++i];
				} else {
					System.out.println("No se ha indicado la cadena de conexion con la base de datos"); //$NON-NLS-1$
					showHelp();
				}
				break;

			case PARAM_DB_USERNAME:
				if (i < args.length - 1) {
					dbUsername = args[++i];
				} else {
					System.out.println("No se ha indicado el usuario de base de datos"); //$NON-NLS-1$
					showHelp();
				}
				break;

			case PARAM_DB_PASSWORD:
				if (i < args.length - 1) {
					dbPassword = args[++i];
				} else {
					System.out.println("No se ha indicado la contrasena de base de datos"); //$NON-NLS-1$
					showHelp();
				}
				break;

			default:
				System.out.println("Se ha indicado un parametro no soportado: " + args[i].substring(0, Math.min(args[i].length(), 30))); //$NON-NLS-1$
				showHelp();
				return;
			}
		}

		// Si no se ha indicado configuracion ni se ha petido usar el fichero del componente central,
		// entonces preguntamos por consola
		if (configFilePath == null && dataDir == null && dbDriver == null && dbConnection == null) {
			final Console console = System.console();
			if (console != null) {
				System.out.println("Desea utilizar un fichero de configuracion de FIRe para cargar los datos? [s/N]"); //$NON-NLS-1$
				final String r = console.readLine();
				if (!"s".equalsIgnoreCase(r)) { //$NON-NLS-1$
					System.out.println("Se cancela la carga de datos.\n"); //$NON-NLS-1$
					showHelp();
					return;
				}
				System.out.println("Introduzca la ruta del fichero de configuracion de FIRe: "); //$NON-NLS-1$
				final String r2 = console.readLine();
				if (r2 == null || r2.isEmpty() || !new File(r2).exists()){
					System.out.println("La ruta introducida no es valida. Se cancela la carga de datos.\n"); //$NON-NLS-1$
					showHelp();
					return;
				}
				configFilePath = r2;
			}
		}



		// Comprobamos si se indica usar expresamente el fichero de configuracion
		LoadStatisticsResult result = null;
		try {
			if (configFilePath != null) {
				result = dumpData(processCurrentDay, configFilePath);
			}
			// Comprobamos si se nos pasa la configuracion especifica para ejecutar el proceso
			else if (dataDir != null && dbDriver != null && dbConnection != null ) {
				result = dumpData(dataDir, dbDriver, dbConnection, dbUsername, dbPassword, processCurrentDay);
			}
		}
		catch (final Exception e) {
			System.err.println("No se pudo realizar el volcado de datos: " + e.getMessage()); //$NON-NLS-1$
		}

		if (result != null) {
			if (result.isCorrect()) {
				System.out.println("Se han cargado los datos estadisticos en base de datos"); //$NON-NLS-1$
			}
			else {
				System.err.println(result.getErrorMsg());
				System.err.println("Fecha de los ultimos datos cargados: " + result.getLastDateProcessedText()); //$NON-NLS-1$
			}
			return;
		}

		// Si no se ha mostrado ninguna combinacion valida de parametros para ejecutar
		// la operacion, mostramos el mensaje de ayuda
		showHelp();
	}

	/**
	 * Iniciamos la carga de datos utilizando la informaci&oacute;n del fichero de configuraci&oacute;n.
	 * @param processCurrentDay Indica si se deben procesar los datos del d&iacute;a actual.
	 * @param configFilePath Indica la ruta del fichero de configuraci&oacute;n.
	 * @throws IOException Cuando falla el volcado.
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	private static LoadStatisticsResult dumpData(final boolean processCurrentDay, final String configFilePath) throws IOException, ClassNotFoundException, SQLException {

		try {
			ConfigManager.checkConfiguration(configFilePath);
		}
		catch (final Exception e) {
			System.err.println("Error al cargar la configuracion"); //$NON-NLS-1$
			return null;
		}

		final String dataDir = ConfigManager.getStatisticsDir();
		final String dbDriver = ConfigManager.getJdbcDriverString();
		final String dbConn = ConfigManager.getDataBaseConnectionString();
		final String username = ConfigManager.getDataBaseUsername();
		final String password = ConfigManager.getDataBasePassword();

		return dumpData(dataDir, dbDriver, dbConn, username, password, processCurrentDay);
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

		final Map<String, String> connectionAttributes = new HashMap<>();
		connectionAttributes.put("jdbcDriver", jdbcDriver); //$NON-NLS-1$
		connectionAttributes.put("dbConnectionString", dbConnectionString); //$NON-NLS-1$
		connectionAttributes.put("username", username); //$NON-NLS-1$
		connectionAttributes.put("password", password); //$NON-NLS-1$

		// Se crea una tarea para la carga de los datos de estadistica
		final LoadStatisticsRunnable loadStatisticsDataTask = new LoadStatisticsRunnable(path, processCurrentDay, connectionAttributes);

		// Se ejecuta la tarea de forma sincrona (sin usar hilos)
		try {
			loadStatisticsDataTask.run();
		} catch (final Exception e) {
			throw new IOException("Fallo la ejecucion inmediata de la carga de los datos estadisticos", e); //$NON-NLS-1$
		}

		// Devolvemos el resultado que debe haber quedado registrado
		return loadStatisticsDataTask.getResult();
	}

	/**
	 * Muestra por consola la ayuda de la aplicaci&oacute;n.
	 */
	private static void showHelp() {
		System.out.println("Aplicacion para la carga inmediata de estadisticas de FIRe en base de datos.\n"); //$NON-NLS-1$
		System.out.println("Dependencias:"); //$NON-NLS-1$
		System.out.println("\tEsta aplicacion requiere de un controlador JDBC para conectar con su base de datos. Configure su \n" //$NON-NLS-1$
				+ "\tClassPath para tener acceso al controlador desde esta aplicacion o copielo al mismo directorio que \n" //$NON-NLS-1$
				+ "\teste JAR con el nombre 'jdbc.jar'.\n"); //$NON-NLS-1$

		System.out.println("Uso:"); //$NON-NLS-1$
		System.out.println(String.format("\tjava -jar %1s [Opciones]\n", JAR_NAME)); //$NON-NLS-1$
		System.out.println("\t\tCarga los datos usando las opciones proporcionadas.\n"); //$NON-NLS-1$
		System.out.println(String.format("\tjava -jar %1s -configFilePath \"[Ruta fichero]\"\n", JAR_NAME)); //$NON-NLS-1$
		System.out.println("\t\tCarga los datos usando las propiedades definidas en el fichero con la\n" //$NON-NLS-1$
				+ "\t\truta absoluta \"[Ruta fichero]\".\n"); //$NON-NLS-1$
		System.out.println(String.format("\tjava -cp \"Ruta_JDBC%1s%2s\" es.gob.fire.statistics.cmd.StatisticsDBLoader [Opciones]\n", File.pathSeparator, JAR_NAME)); //$NON-NLS-1$
		System.out.println("\t\tCarga los datos usando las opciones proporcionadas y agregando al classpath ficheros JAR\n" //$NON-NLS-1$
				+ "\t\tadicionales, entre los que se puede encontrar el del controlador de base de datos.\n"); //$NON-NLS-1$

		System.out.println("Ruta_JDBC:"); //$NON-NLS-1$

		System.out.println("\tRuta al JAR controlador JDBC de nuestra base de datos.\n"); //$NON-NLS-1$

		System.out.println("Opciones:"); //$NON-NLS-1$

		System.out.println("\t" + PARAM_CONFIG_FILE_PATH); //$NON-NLS-1$
		System.out.println("\t\tCarga los datos en BD usando el fichero de configuracion de FIRe indicado en la propiedad. Si no se usa, se deben\n" //$NON-NLS-1$
				+ String.format("\t\tusar las opciones %1s, %2s y %3s.\n", PARAM_DATA_DIR, PARAM_DB_DRIVER, PARAM_DB_CONNECTION)); //$NON-NLS-1$

		System.out.println("\t" + PARAM_DATA_DIR); //$NON-NLS-1$
		System.out.println("\t\tRuta absoluta del directorio con los ficheros de datos estadisticos.\n"); //$NON-NLS-1$

		System.out.println("\t" + PARAM_DB_DRIVER); //$NON-NLS-1$
		System.out.println("\t\tClase controladora JDBC para el acceso a la base de datos.\n"); //$NON-NLS-1$

		System.out.println("\t" + PARAM_DB_CONNECTION); //$NON-NLS-1$
		System.out.println("\t\tCadena de conexion con la base de datos.\n"); //$NON-NLS-1$

		System.out.println("\t" + PARAM_DB_USERNAME); //$NON-NLS-1$
		System.out.println("\t\tUsuario de base de datos (en caso de que no se indique en la cadena de conexion).\n"); //$NON-NLS-1$

		System.out.println("\t" + PARAM_DB_PASSWORD); //$NON-NLS-1$
		System.out.println("\t\tContrasena de base de datos (en caso de que no se indique en la cadena de conexion).\n"); //$NON-NLS-1$

		System.out.println("\t" + PARAM_PROCESS_CURRENT_DAY); //$NON-NLS-1$
		System.out.println("\t\tEl proceso de carga incluira los datos de hoy (por defecto, no se hace). Esto provoca que despues \n" //$NON-NLS-1$
				+ "\t\tno se puedan registrar los datos generados el resto del dia.\n"); //$NON-NLS-1$

		System.out.println("Propiedades del fichero de configuracion:"); //$NON-NLS-1$

		System.out.println("\tEn el fichero deben aparecer una serie de lineas compuestas por el nombre de\n" //$NON-NLS-1$
				+ "\tuna propiedad el signo igual ('=') y el valor asignado. A continuacion se\n" //$NON-NLS-1$
				+ "\tlistan las propiedades que pueden configurarse.\n"); //$NON-NLS-1$

		System.out.println("\tbbdd.driver"); //$NON-NLS-1$
		System.out.println("\t\tClase controladora JDBC para el acceso a la base de datos.\n"); //$NON-NLS-1$

		System.out.println("\tbbdd.conn"); //$NON-NLS-1$
		System.out.println("\t\tCadena de conexion con la base de datos.\n"); //$NON-NLS-1$

		System.out.println("\tbbdd.username"); //$NON-NLS-1$
		System.out.println("\t\tUsuario de base de datos (en caso de que no se indique en la cadena de\n" //$NON-NLS-1$
				+ "\t\tde conexion).\n"); //$NON-NLS-1$

		System.out.println("\tbbdd.password"); //$NON-NLS-1$
		System.out.println("\t\tContrasena de base de datos (en caso de que no se indique en la cadena\n" //$NON-NLS-1$
				+ "\t\tde conexion).\n"); //$NON-NLS-1$

		System.out.println("\tstatistics.dir"); //$NON-NLS-1$
		System.out.println("\t\tRuta absoluta del directorio con los ficheros de datos estadisticos.\n"); //$NON-NLS-1$

		System.out.println("Ejemplos de comandos:"); //$NON-NLS-1$
		System.out.println(String.format("\tjava -jar %1s %2s \"/usr/fire/statistics\" " //$NON-NLS-1$
				+ "%3s oracle.jdbc.driver.OracleDriver %4s " //$NON-NLS-1$
				+ "jdbc:oracle:thin:Fire/1111@XXX.XXX.XXX.XXX:1521:FIRE_DB\n", //$NON-NLS-1$
				JAR_NAME, PARAM_DATA_DIR, PARAM_DB_DRIVER, PARAM_DB_CONNECTION));

		System.out.println(String.format("\tjava -Dfire.config.path=\"/usr/fire/config\" -jar %1s", //$NON-NLS-1$
				JAR_NAME));

		System.out.println(String.format("\tjava -jar %1s -configFilePath \"/usr/fire/config/stats_cmd_config.properties\" ", //$NON-NLS-1$
				JAR_NAME));

		System.out.println("Ejemplo de fichero de configuracion:"); //$NON-NLS-1$
		System.out.println("\tstatistics.dir = /usr/fire/statistics"); //$NON-NLS-1$
		System.out.println("\tbbdd.driver = oracle.jdbc.driver.OracleDriver"); //$NON-NLS-1$
		System.out.println("\tbbdd.conn = jdbc:oracle:thin:Fire/1111@12.34.56.78:1521:FIRE_DB"); //$NON-NLS-1$
	}
}
