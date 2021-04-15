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
 * <b>File:</b><p>es.gob.monitoriza.service.IPlatformController.java.</p>
 * <b>Description:</b><p> .</p>
  * <b>Project:</b><p>Application for monitoring the services of @firma suite systems</p>
 * <b>Date:</b><p>10 abr. 2018.</p>
 * @author Gobierno de España.
 * @version 1.6, 04/01/2019.
 */
package es.gob.fire.persistence.service;

import java.io.IOException;
import java.security.KeyStore;

import es.gob.fire.persistence.dto.DownloadedLogFileDTO;
import es.gob.fire.persistence.dto.LogDataDTO;
import es.gob.fire.persistence.dto.LogFileInfoDTO;
import es.gob.fire.persistence.dto.LogFilesDTO;

/**
 * <p>Interface that provides communication with the operations of the persistence layer.</p>
 * <b>Project:</b><p>Application for monitoring SPLs.</p>
 * @version 1.0, 20/03/2019.
 */
public interface ILogConsumerService {

	/**
	 * Method that connect to the log consumer service.
	 * @param url The URL to the service.
	 * @param key Authentication key.
	 * @param isVerificarSsl to verify SSL connection.
	 * @throws IOException When it is no possible connect to service.
	 */
	void connect(String url, String key, Boolean isVerificarSsl) throws IOException;

	/**
	 * Method that close the connection to service.
	 */
	void closeConnection();

	/**
	 * Method to list the log files.
	 * @return Log files.
	 */
	LogFilesDTO getLogFiles();


	/**
	 * Method that select a log file from the selected SPL.
	 * @param logFilename Name/Id from the file.
	 * @return The log file's information needed to search.
	 */
	LogFileInfoDTO openLogFile(String logFilename);

	/**
	 * Method to close the current opened log file.
	 */
	void closeLogFile();

	/**
	 * Method that download the opened log file.
	 * @param logFilename Name of the log file.
	 * @return The log file's data.
	 */
	DownloadedLogFileDTO downloadLogFile(String logFilename);

	/**
	 * Method to get the last lines from the opened log file.
	 * @param logFilename Name of the log file.
	 * @param numLines Number of lines requested.
	 * @return Last lines.
	 */
	LogDataDTO lastLines(String logFilename, int numLines);

	/**
	 * Method to get a set of lines filter by criterias.
	 * @param numLines Number of lines requested.
	 * @param startDate Minimun date limit to get.
	 * @param endDate Maximun date limit to get.
	 * @param level Minimun level of log to get.
	 * @param more {@code true} if it has to continue at the current line,
	 * {@code false} if it has to start at the beginning of the file.
	 * @return Filtered lines.
	 */
	LogDataDTO filterLines(int numLines, long startDate, long endDate, String level, boolean more);

	/**
	 * Method to search text in a log file.
	 * @param numLines Number of lines requested.
	 * @param text Text to search.
	 * @param startDate Minimun date limit to get.
	 * @param more Request more results when this isn't the first request.
	 * @return Lines with the found text.
	 */
	LogDataDTO searchText(int numLines, String text, long startDate, final boolean more);

	/**
	 * Method to recover more lines from the last request.
	 * @param numLines Number of lines requested.
	 * @return Additionales lines from the log.
	 */
	LogDataDTO getMore(int numLines);


	/**
	 * Method to recover the connection.
	 * @param connection requested.
	 * @return splUrl from the log.
	 */
	boolean echo(String splUrl);

	/**
	 * Establece el almac&eacute;n de confianza para la verificaci&oacute;n de los certificados SSL.
	 * @param trustStore Almac&eacute;n de confianza ya inicializado.
	 */
	void setSslTrustStore(KeyStore trustStore);


}
