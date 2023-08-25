package es.gob.fire.server.services.statistics;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
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
import es.gob.fire.statistics.entity.AuditTransactionCube;

public class AuditTransactionRecorder {
	
	private static final Logger LOGGER = Logger.getLogger(AuditTransactionRecorder.class.getName());

	private static String LOGGER_NAME = "AUDIT_TRANSACTION"; //$NON-NLS-1$

	private static String LOG_FILENAME = "FIRE_" + LOGGER_NAME + ".log"; //$NON-NLS-1$ //$NON-NLS-2$

	private static String LOG_CHARSET = "utf-8"; //$NON-NLS-1$
	
	private AuditTransactionCube auditTransactionCube;
	
	private Logger dataLogger = null;
	
	private boolean enable;
	
	private static AuditTransactionRecorder instance;
	
	/**
	 * Obtenemos el logger para el guardado de los datos estad&iacute;sticos de las peticiones realizadas.
	 * @return Objeto para el registro de los datos de las peticiones.
	 */
	public final static AuditTransactionRecorder getInstance() {
		if (instance == null){
			instance = new AuditTransactionRecorder();
		}
		return instance;
	}

	private AuditTransactionRecorder() {
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
	public final void register(final FireSession fireSession, final boolean result) {
		// Si no hay que registrar estadisticas, no se hace
		if (!this.enable) {
			return;
		}
		
		// Inicializamos el cubo de datos si no lo estaba
		if(getAuditTransactionCube() == null) {
			this.setAuditTransactionCube(new AuditTransactionCube());
		}
		// Fecha
		this.getAuditTransactionCube().setDate(new Date()); //$NON-NLS-1$
		
		// Id transaccion
		final String trId = fireSession.getString(ServiceParams.SESSION_PARAM_TRANSACTION_ID);
		this.getAuditTransactionCube().setIdTransaction(trId != null && !trId.isEmpty() ? trId : "0"); //$NON-NLS-1$
		
		// Resultado
		this.getAuditTransactionCube().setResult(result);
		
		// Id Aplicacion
		final String appId = fireSession.getString(ServiceParams.SESSION_PARAM_APPLICATION_ID);
		this.getAuditTransactionCube().setIdApplication(appId);
		
		// Nombre Aplicacion
		final String appName = fireSession.getString(ServiceParams.SESSION_PARAM_APPLICATION_NAME);
		this.getAuditTransactionCube().setNameApplication(appName);
		
		// Operacion
		TransactionType type = (TransactionType) fireSession.getObject(ServiceParams.SESSION_PARAM_TRANSACTION_TYPE);
		if (type == null) {
			type = TransactionType.OTHER;
		}
		this.getAuditTransactionCube().setOperation(type.name());
		
		// Operacion criptografica
		this.getAuditTransactionCube().setCryptoOperation(fireSession.getString(ServiceParams.SESSION_PARAM_CRYPTO_OPERATION));
		
		//Formato y formato mejorado
		
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

		// Registramos el tamano del documento, el formato y el formato de actualizacion
		this.getAuditTransactionCube().setDataSize(docSize.longValue());
		this.getAuditTransactionCube().setFormat(format);
		this.getAuditTransactionCube().setImprovedFormat(upgrade);
		
		// Algoritmo
		final String algorithm = fireSession.getString(ServiceParams.SESSION_PARAM_ALGORITHM);
		this.getAuditTransactionCube().setAlgorithm(algorithm);
		
		// Almacenamos la informacion del proveedor
		final String[] provsSession = (String []) fireSession.getObject(ServiceParams.SESSION_PARAM_PROVIDERS);
		final String prov = fireSession.getString(ServiceParams.SESSION_PARAM_CERT_ORIGIN);
		final String provForced = fireSession.getString(ServiceParams.SESSION_PARAM_CERT_ORIGIN_FORCED);

		if (provForced != null && !provForced.isEmpty()) {
			this.getAuditTransactionCube().setProvider(provForced);
			this.getAuditTransactionCube().setMandatoryProvider(true);
		}
		else if (prov != null && !prov.isEmpty()) {
			this.getAuditTransactionCube().setProvider(prov);
		}
		else if(provsSession != null && provsSession.length == 1) {
			this.getAuditTransactionCube().setProvider(provsSession[0]);
			this.getAuditTransactionCube().setMandatoryProvider(true);
		}
		
		// Navegador
		final String browser = fireSession.getString(ServiceParams.SESSION_PARAM_BROWSER);
		this.getAuditTransactionCube().setBrowser(browser);
		
		// Resultado
		this.getAuditTransactionCube().setResult(result);
		
		//Error detalle
		final String errorDetail = fireSession.getString(ServiceParams.SESSION_PARAM_ERROR_MESSAGE);
		if (errorDetail != null && !result) {
			this.getAuditTransactionCube().setErrorDetail(errorDetail);
		} else {
			this.getAuditTransactionCube().setErrorDetail(null);
		}
		
		//Nodo
		try {
			final String node = InetAddress.getLocalHost().getHostName();
			this.getAuditTransactionCube().setNode(node);
		} catch (UnknownHostException e) {
			
		}
		
		this.dataLogger.finest(this.getAuditTransactionCube().toString());
	}
	
	/**
	 * Registra los datos de la petici&oacute;n.
	 * @param fireSession Sesi&oacute;n con la informaci&oacute;n de la firma a realizar.
	 * @param result Resultado de la operaci&oacute;n ({@code true}, la firma termino
	 * correctamente o lo hizo alguna de las firmas del lote; {@code false}, no se pudo
	 * generar la firma o fallaron todas las firmas del lote).
	 * @param docId Identificador del documento firmado en caso de encontrarse dentro de un lote.
	 * @param errorMessage Mensaje de error a almacenar. Si no se indica, se
	 * usar&aacute; el por defecto del tipo de error.
	 */
	public final void register(final FireSession fireSession, final boolean result, String errorMessage) {
		// Si no hay que registrar estadisticas, no se hace
		if (!this.enable) {
			return;
		}
		
		// Inicializamos el cubo de datos si no lo estaba
		if(getAuditTransactionCube() == null) {
			this.setAuditTransactionCube(new AuditTransactionCube());
		}
		
		// Fecha
		this.getAuditTransactionCube().setDate(new Date()); //$NON-NLS-1$
		
		// Id transaccion
		final String trId = fireSession.getString(ServiceParams.SESSION_PARAM_TRANSACTION_ID);
		this.getAuditTransactionCube().setIdTransaction(trId != null && !trId.isEmpty() ? trId : "0"); //$NON-NLS-1$
		
		// Resultado
		this.getAuditTransactionCube().setResult(result);
		
		// Id Aplicacion
		final String appId = fireSession.getString(ServiceParams.SESSION_PARAM_APPLICATION_ID);
		this.getAuditTransactionCube().setIdApplication(appId);
		
		// Nombre Aplicacion
		final String appName = fireSession.getString(ServiceParams.SESSION_PARAM_APPLICATION_NAME);
		this.getAuditTransactionCube().setNameApplication(appName);
		
		// Operacion
		TransactionType type = (TransactionType) fireSession.getObject(ServiceParams.SESSION_PARAM_TRANSACTION_TYPE);
		if (type == null) {
			type = TransactionType.OTHER;
		}
		this.getAuditTransactionCube().setOperation(type.name());
		
		// Operacion criptografica
		this.getAuditTransactionCube().setCryptoOperation(fireSession.getString(ServiceParams.SESSION_PARAM_CRYPTO_OPERATION));
		
		//Formato y formato mejorado
		
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

		// Registramos el tamano del documento, el formato y el formato de actualizacion
		this.getAuditTransactionCube().setDataSize(docSize.longValue());
		this.getAuditTransactionCube().setFormat(format);
		this.getAuditTransactionCube().setImprovedFormat(upgrade);
		
		// Algoritmo
		final String algorithm = fireSession.getString(ServiceParams.SESSION_PARAM_ALGORITHM);
		this.getAuditTransactionCube().setAlgorithm(algorithm);
		
		// Almacenamos la informacion del proveedor
		final String[] provsSession = (String []) fireSession.getObject(ServiceParams.SESSION_PARAM_PROVIDERS);
		final String prov = fireSession.getString(ServiceParams.SESSION_PARAM_CERT_ORIGIN);
		final String provForced = fireSession.getString(ServiceParams.SESSION_PARAM_CERT_ORIGIN_FORCED);

		if (provForced != null && !provForced.isEmpty()) {
			this.getAuditTransactionCube().setProvider(provForced);
			this.getAuditTransactionCube().setMandatoryProvider(true);
		}
		else if (prov != null && !prov.isEmpty()) {
			this.getAuditTransactionCube().setProvider(prov);
		}
		else if(provsSession != null && provsSession.length == 1) {
			this.getAuditTransactionCube().setProvider(provsSession[0]);
			this.getAuditTransactionCube().setMandatoryProvider(true);
		}
		
		// Navegador
		final String browser = fireSession.getString(ServiceParams.SESSION_PARAM_BROWSER);
		this.getAuditTransactionCube().setBrowser(browser);
		
		// Resultado
		this.getAuditTransactionCube().setResult(result);
		
		//Error detalle
		final String errorDetail = errorMessage;
		if (errorDetail != null && !result) {
			this.getAuditTransactionCube().setErrorDetail(errorDetail);
		} else {
			this.getAuditTransactionCube().setErrorDetail(null);
		}
		
		//Nodo
		try {
			final String node = InetAddress.getLocalHost().getHostName();
			this.getAuditTransactionCube().setNode(node);
		} catch (UnknownHostException e) {
			
		}
		
		this.dataLogger.finest(this.getAuditTransactionCube().toString());
	}

	public AuditTransactionCube getAuditTransactionCube() {
		return auditTransactionCube;
	}

	public void setAuditTransactionCube(AuditTransactionCube petitionCube) {
		this.auditTransactionCube = petitionCube;
	}

	public Logger getDataLogger() {
		return dataLogger;
	}

	public void setDataLogger(Logger dataLogger) {
		this.dataLogger = dataLogger;
	}
}
