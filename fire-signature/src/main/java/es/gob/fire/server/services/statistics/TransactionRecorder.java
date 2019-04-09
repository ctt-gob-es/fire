package es.gob.fire.server.services.statistics;

import java.io.File;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import es.gob.fire.logs.handlers.DailyFileHandler;
import es.gob.fire.server.services.FIReServiceOperation;
import es.gob.fire.server.services.internal.FireSession;
import es.gob.fire.server.services.internal.ServiceParams;
import es.gob.fire.statistics.entity.TransactionCube;

/** Registro estad&iacute;stico de las transacciones. */
public final class TransactionRecorder {

	private static final Logger LOGGER = Logger.getLogger(TransactionRecorder.class.getName());

	private static String LOGGER_NAME = "TRANSACTION"; //$NON-NLS-1$

	private static String LOG_FILENAME = "FIRE_" + LOGGER_NAME + ".log"; //$NON-NLS-1$ //$NON-NLS-2$

	private static String LOG_CHARSET = "utf-8"; //$NON-NLS-1$

	private  TransactionCube transactCube;

	private Logger dataLogger = null;

	private boolean enable;

	private static TransactionRecorder instance;

	/** Obtenemos el <code>logger</code> para el guardado de los datos estad&iacute;sticos de las
	 * transacciones realizadas.
	 * @return Objeto para el registro de los datos de las transacciones. */
	public final static TransactionRecorder getInstance() {
		if (instance == null) {
			instance =  new TransactionRecorder();
		}
		return instance;
	}


	private TransactionRecorder() {

		final StatisticsConfig config;
		try {
			config = StatisticsConfig.load();
		}
		catch (final Exception e) {
			LOGGER.warning(
				"No se configuro una politica valida para el guardado de estadisticas, estas no se almacenaran: " + e //$NON-NLS-1$
			);
			return;
		}

		this.enable = config.isEnabled();

		final String logsPath = config.getDataDirPath();
		if (logsPath == null || logsPath.isEmpty()) {
			this.enable = false;
		}

		if (!this.enable) {
			LOGGER.info("No se encuentra habilitado el registro de las estadisticas de transaccion o no se ha proporcionado un directorio de salida"); //$NON-NLS-1$
			return;
		}

		LOGGER.info("Se registraran los datos de las estadisticas de transaccion"); //$NON-NLS-1$

		// Comprobamos que el directorio exista y se pueda escribir en el
		final File logsDir = new File(logsPath);
		if (!logsDir.isDirectory() || !logsDir.canWrite()) {
			LOGGER.log(Level.WARNING, "El directorio para el guardado de estadisticas no existe o no se tienen permisos"); //$NON-NLS-1$
			this.enable = false;
			return;
		}

		// Creamos el logger con el que imprimiremos los resultados a disco
		final Logger fileLogger = Logger.getLogger(LOGGER_NAME);
		fileLogger.setLevel(Level.FINEST);

		// Eliminamos todos los manejadores existentes
		for (final Handler handler : fileLogger.getHandlers()) {
			fileLogger.removeHandler(handler);
		}

		// Instalamos el manejador para la impresion en el fichero de estadisticas
		try {
			final Handler logHandler = new DailyFileHandler(
				new File(logsPath, LOG_FILENAME).getAbsolutePath()
			);
			logHandler.setEncoding(LOG_CHARSET);
			logHandler.setFormatter(
				new Formatter() {
					@Override
					public String format(final LogRecord record) {
						return record.getMessage() + "\r\n"; //$NON-NLS-1$
					}
				}
			);

			fileLogger.addHandler(logHandler);
		}
		catch (final Exception e) {
			LOGGER.log(
				Level.WARNING,
				"No se ha podido crear el fichero de datos para las estadisticas de transaccion: " + e, //$NON-NLS-1$
				e
			);
			this.enable = false;
			return;
		}

		this.dataLogger = fileLogger;
	}


	/** Registra los datos de la transacci&oacute;n.
	 * @param fireSession Sesi&oacute;n con la informaci&oacute;n de la firma a realizar.
	 * @param result Resultado de la operaci&oacute;n ({@code true}, la firma termino
	 * correctamente o lo hizo alguna de las firmas del lote; {@code false}, no se pudo
	 * generar la firma o fallaron todas las firmas del lote). */
	public final void register(final FireSession fireSession, final boolean result) {

		// Si no hay que registrar estadisticas, no se hace
		if (!this.enable) {
			return;
		}

		// Inicializamos el cubo de datos si no lo estaba
		if(getTransactCube() == null) {
			setTransactCube(new TransactionCube());
		}

		// Id transaccion
		final String trId = fireSession.getString(ServiceParams.SESSION_PARAM_TRANSACTION_ID);
		getTransactCube().setIdTransaction(trId != null && !trId.isEmpty() ? trId : "0"); //$NON-NLS-1$

		// Resultado
		getTransactCube().setResultTransaction(result);

		// Nombre de la aplicacion
		final String appName = fireSession.getString(ServiceParams.SESSION_PARAM_APPLICATION_NAME);
		if (appName != null && !appName.isEmpty()) {
			getTransactCube().setApplication(appName);
		}
		else {
			final String appId = fireSession.getString(ServiceParams.SESSION_PARAM_APPLICATION_ID);
			getTransactCube().setApplication(appId);
		}

		// Operacion
		final String op = fireSession.getString(ServiceParams.SESSION_PARAM_OPERATION);
		if (op != null && !op.isEmpty()) {
			final FIReServiceOperation fsop = FIReServiceOperation.parse(op) ;
			getTransactCube().setOperation(TransactionType.valueOf(fsop).name());
		}

		// Almacenamos la informacion del proveedor
		 final String[] provsSession = (String []) fireSession.getObject(ServiceParams.SESSION_PARAM_PROVIDERS);
		 final String prov = fireSession.getString(ServiceParams.SESSION_PARAM_CERT_ORIGIN);
		 final String provForced = fireSession.getString(ServiceParams.SESSION_PARAM_CERT_ORIGIN_FORCED);

		if (provForced != null && !provForced.isEmpty()) {
			getTransactCube().setProvider(provForced);
			getTransactCube().setMandatoryProvider(true);
		}
		else if (prov != null && !prov.isEmpty()) {
			getTransactCube().setProvider(prov);
		}
		else if(provsSession != null && provsSession.length == 1) {
			getTransactCube().setProvider(provsSession[0]);
			getTransactCube().setMandatoryProvider(true);
		}

		this.dataLogger.finest(getTransactCube().toString());
	}

	private final TransactionCube getTransactCube() {
		return this.transactCube;
	}


	private final void setTransactCube(final TransactionCube transactCube) {
		this.transactCube = transactCube;
	}

}
