package es.gob.fire.server.services.statistics;

import java.io.File;
import java.sql.SQLException;
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
import es.gob.fire.signature.DBConnectionException;
import es.gob.fire.statistics.dao.AuditSignaturesDAO;
import es.gob.fire.statistics.entity.AuditSignatureCube;

public class AuditSignatureRecorder {

	private static final Logger LOGGER = Logger.getLogger(AuditSignatureRecorder.class.getName());

	private static String LOGGER_NAME = "AUDIT_SIGNATURE"; //$NON-NLS-1$

	private static String LOG_FILENAME = "FIRE_" + LOGGER_NAME + ".log"; //$NON-NLS-1$ //$NON-NLS-2$

	private static String LOG_CHARSET = "utf-8"; //$NON-NLS-1$

	private Logger dataLogger = null;

	private boolean enable;

	private boolean enableDB;

	private static AuditSignatureRecorder instance;

	/**
	 * Obtenemos el logger para el guardado de los datos de auditor&iacute;a de las peticiones realizadas.
	 * @return Objeto para el registro de los datos de las peticiones.
	 */
	public final static AuditSignatureRecorder getInstance() {
		if (instance == null){
			instance = new AuditSignatureRecorder();
		}
		return instance;
	}

	private AuditSignatureRecorder(){
		final AuditConfig config;

		try {
			config = AuditConfig.load();
		} catch (final Exception e) {
			LOGGER.warning("No se configuro una politica valida para el guardado de auditoria. No almacenaran"); //$NON-NLS-1$
			return;
		}

		this.enable = config.isEnabled();
		this.enableDB = config.isSavingToDB();

		final String logsPath = config.getDataDirPath();
		if (logsPath == null || logsPath.isEmpty()) {
			this.enable = false;
		}

		LOGGER.fine("Se registraran los datos de auditor&iacute;a de peticion"); //$NON-NLS-1$

		// Comprobamos que el directorio exista y se pueda escribir en el
		final File logsDir = new File(logsPath);
		if (!logsDir.isDirectory() || !logsDir.canWrite()) {
			LOGGER.log(Level.WARNING, "El directorio para el guardado de auditoria no existe o no se tienen permisos"); //$NON-NLS-1$
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
		Handler logHandler = null;
		try {
			logHandler = new DailyFileHandler(new File(logsPath, LOG_FILENAME).getAbsolutePath());
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
			LOGGER.log(Level.WARNING, "No se ha podido crear el fichero de datos para la auditoria de transaccion", e); //$NON-NLS-1$
			this.enable = false;
			return;
		}

		this.dataLogger = fileLogger;
	}

	/**
	 * Registra los datos de la petici&oacute;n.
	 * @param fireSession Sesi&oacute;n con la informaci&oacute;n de la firma a realizar.
	 * @param result Resultado de la operaci&oacute;n ({@code true}, la firma termino
	 * correctamente o lo hizo alguna de las firmas del lote; {@code false}, no se pudo
	 * generar la firma o fallaron todas las firmas del lote).
	 * @param docId Identificador del documento firmado en caso de encontrarse dentro de un lote.
	 */
	public final void register(final FireSession fireSession, final boolean result, final String docId) {

		register(fireSession, result, docId, fireSession.getString(ServiceParams.SESSION_PARAM_ERROR_MESSAGE));
	}

	/**
	 * Registra los datos de la petici&oacute;n.
	 * @param fireSession Sesi&oacute;n con la informaci&oacute;n de la firma a realizar.
	 * @param result Resultado de la operaci&oacute;n ({@code true}, la firma termino
	 * correctamente o lo hizo alguna de las firmas del lote; {@code false}, no se pudo
	 * generar la firma o fallaron todas las firmas del lote).
	 * @param docId Identificador del documento firmado en caso de encontrarse dentro de un lote.
	 * @param messageError Mensaje de error a almacenar. Si no se indica, se
	 * usar&aacute; el por defecto del tipo de error.
	 */
	public final void register(final FireSession fireSession, final boolean result, final String docId, final String messageError) {

		// Si no hay que registrar estadisticas, no se hace
		if (!this.enable) {
			return;
		}

		// Inicializamos el cubo de datos si no lo estaba
		final AuditSignatureCube signatureCube = new AuditSignatureCube();

		//Id int lote
		if (docId != null){
			signatureCube.setIdIntLote(docId);
		}

		// Id transaccion
		final String trId = fireSession.getString(ServiceParams.SESSION_PARAM_TRANSACTION_ID);
		signatureCube.setIdTransaction(trId != null && !trId.isEmpty() ? trId : "0"); //$NON-NLS-1$

		// Resultado
		signatureCube.setResult(result);

		// Operacion criptografica
		signatureCube.setCryptoOperation(fireSession.getString(ServiceParams.SESSION_PARAM_CRYPTO_OPERATION));

		// Obtenemos el tamano del documento

		final Object docSizeObject = fireSession.getObject(ServiceParams.SESSION_PARAM_DOCSIZE);
		Long docSize = docSizeObject != null && docSizeObject instanceof Long
				? (Long) docSizeObject
				: Long.valueOf(0);

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

		// Resultado
		signatureCube.setResult(result);

		//Error detalle
		final String errorDetail = messageError;
		if (errorDetail != null && !result) {
			signatureCube.setErrorDetail(errorDetail);
		} else {
			signatureCube.setErrorDetail(null);
		}

		this.dataLogger.finest(signatureCube.toString());

		if (this.enableDB) {
			AuditSignaturesDAO.insertAuditSignature(signatureCube);
		}
	}

	public Logger getDataLogger() {
		return this.dataLogger;
	}

	public boolean isEnable() {
		return this.enable;
	}
}
