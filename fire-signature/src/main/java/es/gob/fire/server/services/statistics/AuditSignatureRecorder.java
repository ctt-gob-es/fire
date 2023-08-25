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
import es.gob.fire.statistics.entity.AuditSignatureCube;

public class AuditSignatureRecorder {
	
	private static final Logger LOGGER = Logger.getLogger(AuditSignatureRecorder.class.getName());

	private static String LOGGER_NAME = "AUDIT_SIGNATURE"; //$NON-NLS-1$

	private static String LOG_FILENAME = "FIRE_" + LOGGER_NAME + ".log"; //$NON-NLS-1$ //$NON-NLS-2$

	private static String LOG_CHARSET = "utf-8"; //$NON-NLS-1$
	
	private AuditSignatureCube auditSignatureCube;
	
	private Logger dataLogger = null;
	
	private boolean enable;
	
	private static AuditSignatureRecorder instance;
	
	/**
	 * Obtenemos el logger para el guardado de los datos estad&iacute;sticos de las peticiones realizadas.
	 * @return Objeto para el registro de los datos de las peticiones.
	 */
	public final static AuditSignatureRecorder getInstance() {
		if (instance == null){
			instance = new AuditSignatureRecorder();
		}
		return instance;
	}
	
	private AuditSignatureRecorder(){
		final StatisticsConfig config;
		
		try {
			config = StatisticsConfig.load();
		} catch (final Exception e) {
			LOGGER.warning("No se configuro una politica valida para el guardado de estadisticas. No se almacenaran"); //$NON-NLS-1$
			return;
		}
		
		this.enable = config.isEnabled();
		
		final String logsPath = config.getDataDirPath();
		if (logsPath == null || logsPath.isEmpty()) {
			this.enable = false;
		}
		
		LOGGER.info("Se registraran los datos de las estadisticas de peticion"); //$NON-NLS-1$
		
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
			LOGGER.log(Level.WARNING, "No se ha podido crear el fichero de datos para las estadisticas de transaccion", e); //$NON-NLS-1$
			this.enable = false;
			return;
		}

		this.setDataLogger(fileLogger);
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
		// Si no hay que registrar estadisticas, no se hace
		if (!this.enable) {
			return;
		}
		
		// Inicializamos el cubo de datos si no lo estaba
		if(getAuditSignatureCube() == null) {
			this.setAuditSignatureCube(new AuditSignatureCube());
		}
		
		//Id int lote
		if (docId != null){
			this.getAuditSignatureCube().setIdIntLote(docId);
		}
		
		// Id transaccion
		final String trId = fireSession.getString(ServiceParams.SESSION_PARAM_TRANSACTION_ID);
		this.getAuditSignatureCube().setIdTransaction(trId != null && !trId.isEmpty() ? trId : "0"); //$NON-NLS-1$
		
		// Resultado
		this.getAuditSignatureCube().setResult(result);
		
		// Operacion criptografica
		this.getAuditSignatureCube().setCryptoOperation(fireSession.getString(ServiceParams.SESSION_PARAM_CRYPTO_OPERATION));
		
		// Obtenemos el tamano del documento
		Long docSize = new Long(0);
		final Object docSizeObject = fireSession.getObject(ServiceParams.SESSION_PARAM_DOCSIZE);
		if (docSize != null) {
			docSize = (Long) docSizeObject;
			if (docSize == null) {
				docSize = new Long(0);
			}
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
			    	docSize = new Long(docinf.getSize());
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
		this.getAuditSignatureCube().setDataSize(docSize.longValue());
		this.getAuditSignatureCube().setFormat(format);
		this.getAuditSignatureCube().setImprovedFormat(upgrade);
		
		// Resultado
		this.getAuditSignatureCube().setResult(result);
		
		//Error detalle
		final String errorDetail = fireSession.getString(ServiceParams.SESSION_PARAM_ERROR_MESSAGE);
		if (errorDetail != null && !result) {
			this.getAuditSignatureCube().setErrorDetail(errorDetail);
		} else {
			this.getAuditSignatureCube().setErrorDetail(null);
		}
		
		this.dataLogger.finest(this.getAuditSignatureCube().toString());
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
	public final void register(final FireSession fireSession, final boolean result, final String docId, String messageError) {
		// Si no hay que registrar estadisticas, no se hace
		if (!this.enable) {
			return;
		}
		
		// Inicializamos el cubo de datos si no lo estaba
		if(getAuditSignatureCube() == null) {
			this.setAuditSignatureCube(new AuditSignatureCube());
		}
		
		//Id int lote
		if (docId != null){
			this.getAuditSignatureCube().setIdIntLote(docId);
		}
		
		// Id transaccion
		final String trId = fireSession.getString(ServiceParams.SESSION_PARAM_TRANSACTION_ID);
		this.getAuditSignatureCube().setIdTransaction(trId != null && !trId.isEmpty() ? trId : "0"); //$NON-NLS-1$
		
		// Resultado
		this.getAuditSignatureCube().setResult(result);
		
		// Operacion criptografica
		this.getAuditSignatureCube().setCryptoOperation(fireSession.getString(ServiceParams.SESSION_PARAM_CRYPTO_OPERATION));
		
		// Obtenemos el tamano del documento
		Long docSize = new Long(0);
		final Object docSizeObject = fireSession.getObject(ServiceParams.SESSION_PARAM_DOCSIZE);
		if (docSize != null) {
			docSize = (Long) docSizeObject;
			if (docSize == null) {
				docSize = new Long(0);
			}
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
			    	docSize = new Long(docinf.getSize());
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
		this.getAuditSignatureCube().setDataSize(docSize.longValue());
		this.getAuditSignatureCube().setFormat(format);
		this.getAuditSignatureCube().setImprovedFormat(upgrade);
		
		// Resultado
		this.getAuditSignatureCube().setResult(result);
		
		//Error detalle
		final String errorDetail = messageError;
		if (errorDetail != null && !result) {
			this.getAuditSignatureCube().setErrorDetail(errorDetail);
		} else {
			this.getAuditSignatureCube().setErrorDetail(null);
		}
		
		this.dataLogger.finest(this.getAuditSignatureCube().toString());
	}

	public AuditSignatureCube getAuditSignatureCube() {
		return auditSignatureCube;
	}

	public void setAuditSignatureCube(AuditSignatureCube auditSignatureCube) {
		this.auditSignatureCube = auditSignatureCube;
	}

	public Logger getDataLogger() {
		return dataLogger;
	}

	public void setDataLogger(Logger dataLogger) {
		this.dataLogger = dataLogger;
	}

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

}
