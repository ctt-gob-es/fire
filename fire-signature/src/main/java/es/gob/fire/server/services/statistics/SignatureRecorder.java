package es.gob.fire.server.services.statistics;

import java.io.File;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import es.gob.fire.logs.handlers.DailyFileHandler;
import es.gob.fire.server.services.DocInfo;
import es.gob.fire.server.services.internal.BatchResult;
import es.gob.fire.server.services.internal.FireSession;
import es.gob.fire.server.services.internal.ServiceParams;
import es.gob.fire.server.services.internal.SignBatchConfig;
import es.gob.fire.statistics.entity.SignatureCube;

/**
 * Permite registrar la informaci&oacute;n relevante de las firmas realizadas para el
 * c&aacute;lculo de estad&iacute;sticas.
 */
public class SignatureRecorder {

	private static final Logger LOGGER = Logger.getLogger(SignatureRecorder.class.getName());

	private static String LOGGER_NAME = "SIGNATURE"; //$NON-NLS-1$

	private static String LOG_FILENAME = "FIRE_" + LOGGER_NAME + ".log"; //$NON-NLS-1$ //$NON-NLS-2$

	private static String LOG_CHARSET = "utf-8"; //$NON-NLS-1$

	private Logger dataLogger = null;

	private boolean enable = false;

	private static SignatureRecorder instance;

	/**
	 * Obtenemos el logger para el guardado de los datos estad&iacute;sticos de las
	 * firmas realizadas.
	 * @return Objeto para el registro de los datos de las firmas.
	 */
	public final static SignatureRecorder getInstance() {
		if (instance == null) {
			instance =  new SignatureRecorder();
		}
		return instance;
	}

	/**
	 * Construye un objeto para el registro de los datos de firma en base a los cuales generar
	 * estad&iacute;sticas sobre las firmas generadas por FIRe.
	 */
	private SignatureRecorder() {

		final StatisticsConfig config;
		try {
			config = StatisticsConfig.load();
		}
		catch (final Exception e) {
			LOGGER.warning("No se configuro una politica valida para el guardado de estadisticas. No se almacenaran"); //$NON-NLS-1$
			return;
		}

		this.enable = config.isEnabled();

		final String logsPath = config.getDataDirPath();
		if (logsPath == null || logsPath.isEmpty()) {
			this.enable = false;
		}

		if (!this.enable) {
			LOGGER.info("No se encuentra habilitado el registro de las estadisticas de firma o no se ha proporcionado un directorio de salida"); //$NON-NLS-1$
			return;
		}

		LOGGER.info("Se registraran los datos de las estadisticas de firma"); //$NON-NLS-1$

		// Comprobamos que el directorio exista y se pueda escribir en el
		final File logsDir = new File(logsPath);
		if (!logsDir.isDirectory() || !logsDir.canWrite()) {
			LOGGER.warning("El directorio para el guardado de estadisticas no existe o no se tienen permisos"); //$NON-NLS-1$
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
			final Handler logHandler = new DailyFileHandler(new File(logsPath, LOG_FILENAME).getAbsolutePath());
			logHandler.setEncoding(LOG_CHARSET);
			logHandler.setFormatter(new Formatter() {
				@Override
				public String format(final LogRecord record) {
					return record.getMessage() + "\r\n"; //$NON-NLS-1$
				}
			});

			fileLogger.addHandler(logHandler);
		}
		catch (final Exception e) {
			LOGGER.log(Level.WARNING, "No se ha podido crear el fichero de datos para las estadisticas de firma", e); //$NON-NLS-1$
			this.enable = false;
			return;
		}

		this.dataLogger = fileLogger;
	}

	/**
	 * Registra los datos de firma.
	 * @param fireSesion Sesi&oacute;n con la informaci&oacute;n de la firma a realizar.
	 * @param result Resultado de la operaci&oacute;n ({@code true}, firma correcta;
	 * {@code false}, no se pudo generar la firma).
	 */
	public final void register(final FireSession fireSesion, final boolean result) {
		register(fireSesion, result, null);
	}

	/**
	 * Registra los datos de firma.
	 * @param fireSession Sesi&oacute;n con la informaci&oacute;n de la firma a realizar.
	 * @param result Resultado de la operaci&oacute;n ({@code true}, firma correcta;
	 * {@code false}, no se pudo generar la firma).
	 * @param docId Identificador del documento firmado en caso de encontrarse dentro de un lote.
	 */
	public final void register(final FireSession fireSession, final boolean result, final String docId) {

		// Si no hay que registrar estadisticas, no se hace
		if (!this.enable) {
			return;
		}

		 // Inicializamos el cubo de datos si no lo estaba
		final SignatureCube signatureCube = new SignatureCube();

		// Id transaccion
		final String trId = fireSession.getString(ServiceParams.SESSION_PARAM_TRANSACTION_ID);
		signatureCube.setIdTransaction(trId != null && !trId.isEmpty() ? trId : "0"); //$NON-NLS-1$

		// Aplicacion
		final String appName = fireSession.getString(ServiceParams.SESSION_PARAM_APPLICATION_NAME);
		signatureCube.setApplication(appName);

		// Resultado
		signatureCube.setResultSign(result);

		// Navegador
		final String browser = fireSession.getString(ServiceParams.SESSION_PARAM_BROWSER);
		signatureCube.setBrowser(browser);

		// Proveedor que gestiona el certificado de firma
		final String provider = fireSession.getString(ServiceParams.SESSION_PARAM_CERT_ORIGIN);
		signatureCube.setProvider(provider);

		// Algoritmo
		final String algorithm = fireSession.getString(ServiceParams.SESSION_PARAM_ALGORITHM);
		signatureCube.setAlgorithm(algorithm);

		// Obtenemos el tamano del documento
		Long docSize = Long.valueOf(0);
		final Object docSizeObject = fireSession.getObject(ServiceParams.SESSION_PARAM_DOCSIZE);
		if (docSizeObject != null && docSizeObject instanceof Long) {
			docSize = (Long) docSizeObject;
		}

		// Obtenemos el formato de firma configurado
		String format = fireSession.getString(ServiceParams.SESSION_PARAM_FORMAT);

		// Obtenemos el formato de actualizacion configurado
		String upgrade = fireSession.getString(ServiceParams.SESSION_PARAM_UPGRADE);

		// En caso de firma de lotes, actualizamos la informacion de los documentos y la configuracion empleada
		if (docId != null) {
			final BatchResult batchResult = (BatchResult) fireSession.getObject(ServiceParams.SESSION_PARAM_BATCH_RESULT);
			if (batchResult != null && batchResult.documentsCount() > 0) {
				// Actualizamos el tamano del documento
				final DocInfo docinf = batchResult.getDocInfo(docId);
			    if (docinf != null) {
			    	docSize = Long.valueOf(docinf.getSize());
			    }
			    // Si se establecio una configuracion especifica para el documento, registramos esta
			    final SignBatchConfig signConfig = batchResult.getSignConfig(docId);
			    if (signConfig != null) {
			    	format = signConfig.getFormat();
					upgrade = signConfig.getUpgrade();
				}
			}
		}

		// Registramos el tamano del documento, el formato y el formato de actualizacion
		signatureCube.setDataSize(docSize.longValue());
		signatureCube.setFormat(format);
		signatureCube.setImprovedFormat(upgrade);

		// Imprimimos los datos del cubo en la salida de datos de firma
		this.dataLogger.finest(signatureCube.toString());
	}
}
