/*
/*******************************************************************************
 * Copyright (C) 2018 MINHAFP, Gobierno de España
 * This program is licensed and may be used, modified and redistributed under the  terms
 * of the European Public License (EUPL), either version 1.1 or (at your option)
 * any later version as soon as they are approved by the European Commission.
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and
 * more details.
 * You should have received a copy of the EUPL1.1 license
 * along with this program; if not, you may find it at
 * http:joinup.ec.europa.eu/software/page/eupl/licence-eupl
 ******************************************************************************/

/**
 * <p>Class that implements the communication with the operations of the persistence layer.</p>
 * <b>Project:</b><p>Platform for detection and validation of certificates recognized in European TSL.</p>
 * @version 1.0, 15/06/2018.
 */
package es.gob.fire.persistence.service.impl;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.security.KeyStore;
import java.util.Collections;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import es.gob.fire.core.dto.DownloadedLogFileDTO;
import es.gob.fire.core.dto.LogDataDTO;
import es.gob.fire.core.dto.LogFileInfoDTO;
import es.gob.fire.core.dto.LogFilesDTO;
import es.gob.fire.core.dto.RowLogFileErrorDTO;
import es.gob.fire.core.log.LogErrors;
import es.gob.fire.i18n.IWebLogMessages;
import es.gob.fire.i18n.Language;
import es.gob.fire.persistence.service.ILogConsumerService;
import es.gob.log.consumer.client.DownloadedLogFile;
import es.gob.log.consumer.client.LogConsumerClient;
import es.gob.log.consumer.client.LogData;
import es.gob.log.consumer.client.LogError;
import es.gob.log.consumer.client.LogInfo;
import es.gob.log.consumer.client.LogResult;

/**
 * <p>
 * Class that implements the communication with the operations of the persistence layer
 * for LogConsumetClient.</p>
 * <b>Project:</b><p>Application for monitoring services of @firma suite systems.</p>
  * @version 1.0, 20/03/2019.
 */
@Service
public class LogConsumerService implements ILogConsumerService {

	/** Logger. */
	private static final Logger LOGGER = Logger.getLogger(LogConsumerService.class);

	/**
	 * Attribute that represents the injected interface that provides operation
	 * to check logs.
	 */
	@Autowired
	private LogConsumerClient logConsumerBean;

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.service.ILogConsumerService#setSslTrustStore(java.security.KeyStore)
	 */
	@Override
	public void setSslTrustStore(final KeyStore trustStore) {
		this.logConsumerBean.setTrustStore(trustStore);
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.service.ILogConsumerService#connect(java.lang.String, java.lang.String, java.lang.Boolean)
	 */
	@Override
	public void connect(final String url, final String key, final Boolean isVerificarSsl) throws IOException {
		this.logConsumerBean.setDisableSslChecks(!isVerificarSsl);
		this.logConsumerBean.init(url, key);
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.service.ILogConsumerService#closeConnection()
	 */
	@Override
	public void closeConnection() {
		this.logConsumerBean.closeConnection();
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.service.ILogConsumerService#echo(java.lang.String)
	 */
	@Override
	public boolean echo(final String urlTex) {
		final String result = this.logConsumerBean.echo(urlTex);
		final String ok = "\"Code\":200";
		if (result.indexOf(ok) == -1) {
			LOGGER.warn("No se pudo conectar con el servicio: " + result);
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.service.ILogConsumerService#getLogFiles()
	 */
	@Override
	public LogFilesDTO getLogFiles() {
		LogFilesDTO logFiles = new LogFilesDTO();
		final Type listType = new TypeToken<LogFilesDTO>() {

			/**
			 * Attribute that represents the serial version.
			 */
			private static final long serialVersionUID = 1L;
		}.getType();
		try {
			final byte[ ] logFilesJson = this.logConsumerBean.getLogFiles();
			logFiles = new Gson().fromJson(new String(logFilesJson), listType);
		} catch (final JsonSyntaxException e) {
			final String errorJson = Language.getResWebMonitoriza(IWebLogMessages.ERRORWEB025);
			final RowLogFileErrorDTO errorDTO = new RowLogFileErrorDTO();
			errorDTO.setCode(400);
			errorDTO.setMessage(errorJson);
			logFiles.setError(Collections.singletonList(errorDTO));
		}
		return logFiles;
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.service.ILogConsumerService#openLogFile(java.lang.String)
	 */
	@Override
	public LogFileInfoDTO openLogFile(final String logFilename) {
		final LogFileInfoDTO logFileInfo = new LogFileInfoDTO();
		final LogInfo logInfo = this.logConsumerBean.openFile(logFilename);
		if (logInfo.getError() != null) {
			final String errorMsg = Language.getResWebMonitoriza(IWebLogMessages.ERRORWEB026);
			logFileInfo.setError(errorMsg);
		} else {
			logFileInfo.setFilename(logFilename);
			logFileInfo.setCharset(logInfo.getCharset());
			logFileInfo.setDate(logInfo.isDate());
			logFileInfo.setTime(logInfo.isTime());
			logFileInfo.setDateTimeFormat(logInfo.getDateTimeFormat());
			logFileInfo.setLevels(logInfo.getLevels());
		}
		return logFileInfo;
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.service.ILogConsumerService#closeLogFile()
	 */
	@Override
	public void closeLogFile() {
		// No hacemos nada con el resultado. Si recogemos el resultado, ya que
		final LogResult result = this.logConsumerBean.closeFile();
		if (result.getError() != null) {
			LOGGER.warn("No se ha podido cerrar el fichero de log abierto: " + result.getError());
		}
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.service.ILogConsumerService#downloadLogFile(java.lang.String)
	 */
	@Override
	public DownloadedLogFileDTO downloadLogFile(final String logFilename) {
		final DownloadedLogFileDTO logFileInfo = new DownloadedLogFileDTO();
		final String tempDir = System.getProperty("java.io.tmpdir");
		final DownloadedLogFile downloadedFile = this.logConsumerBean.download(logFilename, tempDir);
		if (downloadedFile.getError() != null) {
			final String errorMsg = Language.getResWebMonitoriza(IWebLogMessages.ERRORWEB027);
			logFileInfo.setError(errorMsg);
		} else {
			final File logFile = new File(downloadedFile.getPath());
			try {
				logFileInfo.setData(Files.readAllBytes(logFile.toPath()));
				logFileInfo.setFilename(logFile.getName());
				logFileInfo.setContentType("application/zip");
			} catch (final IOException e) {
				LOGGER.error("No se pudo leer el fichero de log almacenado en el directorio temporal", e);
				logFileInfo.setError(Language.getResWebMonitoriza(IWebLogMessages.ERRORWEB028));
			}
		}
		return logFileInfo;
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.service.ILogConsumerService#lastLines(java.lang.String, int)
	 */
	@Override
	public LogDataDTO lastLines(final String logName, final int numLines) {
		final LogData logData = this.logConsumerBean.getLogTail(numLines, logName);
		final LogDataDTO log = new LogDataDTO();
		if (logData.getError() != null) {
			if (LogError.EC_NO_MORE_LINES.equals(logData.getError().getCode())) {
				log.setErrorCode(LogErrors.NO_MORE_LINES.getCode());
				log.setErrorMessage(LogErrors.NO_MORE_LINES.getMessage());
			} else {
				LOGGER.warn("Error desconocido al solicitar las ultimas lineas: " + logData.getError().getMessage());
				log.setErrorMessage(LogErrors.UNKNOWN_ERROR.getMessage());
			}
		} else {
			log.setLog(logData.getLog());
			log.setCharset(logData.getCharset());
		}
		return log;
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.service.ILogConsumerService#filterLines(int, long, long, java.lang.String, boolean)
	 */
	@Override
	public LogDataDTO filterLines(final int numLines, final long startDate, final long endDate, final String level, final boolean more) {
		final LogData logData = this.logConsumerBean.getLogFiltered(numLines, startDate, endDate, level, !more);
		final LogDataDTO log = new LogDataDTO();
		if (logData.getError() != null) {
			if (LogError.EC_NO_MORE_LINES.equals(logData.getError().getCode())) {
				log.setErrorCode(LogErrors.NO_MORE_LINES.getCode());
				log.setErrorMessage(LogErrors.NO_MORE_LINES.getMessage());
			} else {
				LOGGER.warn("Error desconocido al filtar lineas: " + logData.getError().getMessage());
				log.setErrorCode(LogErrors.UNKNOWN_ERROR.getCode());
				log.setErrorMessage(LogErrors.UNKNOWN_ERROR.getMessage());
			}
		} else {
			log.setLog(logData.getLog());
			log.setCharset(logData.getCharset());
		}
		return log;
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.service.ILogConsumerService#searchText(int, java.lang.String, long, boolean)
	 */
	@Override
	public LogDataDTO searchText(final int numLines, final String text, final long startDate, final boolean more) {
		final LogData logData = this.logConsumerBean.searchText(numLines, text, startDate, !more);
		final LogDataDTO log = new LogDataDTO();
		if (logData.getError() != null) {
			if (LogError.EC_NO_MORE_LINES.equals(logData.getError().getCode())) {
				log.setErrorCode(LogErrors.NO_MORE_LINES.getCode());
				log.setErrorMessage(LogErrors.NO_MORE_LINES.getMessage());
			} else {
				LOGGER.warn("Error desconocido al buscar un texto: " + logData.getError().getMessage());
				log.setErrorCode(LogErrors.UNKNOWN_ERROR.getCode());
				log.setErrorMessage(LogErrors.UNKNOWN_ERROR.getMessage());
			}
		} else {
			log.setLog(logData.getLog());
			log.setCharset(logData.getCharset());
		}
		return log;
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.service.ILogConsumerService#getMore(int)
	 */
	@Override
	public LogDataDTO getMore(final int numLines) {
		final LogData logData = this.logConsumerBean.getMoreLog(numLines);
		final LogDataDTO log = new LogDataDTO();
		if (logData.getError() != null) {
			if (LogError.EC_NO_MORE_LINES.equals(logData.getError().getCode())) {
				log.setErrorCode(LogErrors.NO_MORE_LINES.getCode());
				log.setErrorMessage(LogErrors.NO_MORE_LINES.getMessage());
			} else {
				LOGGER.warn("Error desconocido al solicitar mas lineas: " + logData.getError().getMessage());
				log.setErrorMessage(LogErrors.UNKNOWN_ERROR.getMessage());
			}
		} else {
			log.setLog(logData.getLog());
			log.setCharset(logData.getCharset());
		}
		return log;
	}

}
