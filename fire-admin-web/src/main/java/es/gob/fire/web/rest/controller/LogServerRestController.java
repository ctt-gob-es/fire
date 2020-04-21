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
 * <b>File:</b><p>es.gob.fire.web.controller.LogServerRestController.java.</p>
 * <b>Description:</b><p>Class that manages the REST requests related to the log server administration and JSON communication.</p>
  * <b>Project:</b><p>Application for signing documents of @firma suite systems</p>
 * <b>Date:</b><p>14/04/2020.</p>
 * @author Gobierno de España.
 * @version 1.0, 14/04/2020.
 */
package es.gob.fire.web.rest.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotEmpty;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.http.MediaType;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

import es.gob.fire.core.constant.Constants;
import es.gob.fire.core.dto.DownloadedLogFileDTO;
import es.gob.fire.core.dto.LogDataDTO;
import es.gob.fire.core.dto.LogFilesDTO;
import es.gob.fire.core.dto.LogFilterFormDTO;
import es.gob.fire.core.dto.LogLastLinesFormDTO;
import es.gob.fire.core.dto.LogSearchTextFormDTO;
import es.gob.fire.core.dto.LogServerDTO;
import es.gob.fire.core.dto.RowLogFileDTO;
import es.gob.fire.persistence.entity.LogServer;
import es.gob.fire.persistence.service.ILogConsumerService;
import es.gob.fire.persistence.service.ILogServerService;

/** 
 * <p>Class that manages the REST requests related to the log server administration and JSON communication.</p>
 * <b>Project:</b><p>Application for signing documents of @firma suite systems.</p>
 * @version 1.0, 14/04/2020.
 */
@RestController
public class LogServerRestController {

	/**
	 * Attribute that represents the object that manages the log of the class.
	 */
	private static final Logger LOGGER = Logger.getLogger(LogServerRestController.class);
	
	/**
	 * Attribute that represents the date field format.
	 */
	private static final String DATE_FIELD_FORMAT = "dd/MM/yyyy HH:mm";
	
	/**
	 * Attribute that represents the service object for managing the log server.
	 */
	@Autowired
	private ILogServerService logServerService;

	/**
	 * Attribute that represents the service object for accessing to the logs.
	 */
	@Autowired
	private ILogConsumerService logConsumerService;

	/**
	 * Method that maps the list log server requests to the controller and
	 * forwards the list of users to the view.
	 * @param input Holder object for data table attributes.
	 * @return String that represents the name of the view to forward.
	 */
	@JsonView(DataTablesOutput.View.class)
	@RequestMapping(path = "/logserverdatatable", method = RequestMethod.GET)
	public DataTablesOutput<LogServer> dtLogServer(@NotEmpty final DataTablesInput input) {
		return this.logServerService.getAllLogServer(input);
	}

	/**
	 * Method that maps the SPL's log files to the controller and
	 * forwards the logs files to the view.
	 *
	 * @return String that represents the name of the view to forward.
	 */
	@RequestMapping(path = "/logfilesdatatable", method = RequestMethod.GET)
	public DataTablesOutput<RowLogFileDTO> dtLogFiles() {
		final DataTablesOutput<RowLogFileDTO> dtOutput = new DataTablesOutput<>();
		final List<RowLogFileDTO> filesList = new ArrayList<>();
		try {
			final LogFilesDTO logFiles = this.logConsumerService.getLogFiles();
			for (final RowLogFileDTO logFile: logFiles.getFileList()) {
				filesList.add(logFile);
			}
			dtOutput.setRecordsTotal(logFiles.getFileList().size());
		} catch (final Exception e) {
			LOGGER.warn("Error al llamar al servicio de listado de ficheros de log", e);
			dtOutput.setError("No se pudo obtener el listado de ficheros del SPL");
		}
		dtOutput.setData(filesList);
		return dtOutput;
	}

	/**
	 * Method that maps the save user web request to the controller and saves it
	 * in the persistence. It also updates the scheduled timers.
	 * @param logServerForm Object that represents the backing SPL form.
	 * @param bindingResult Object that represents the form validation result.
	 * @return {@link DataTablesOutput<LogServer>}
	 */
	@RequestMapping(value = "/savelogserver", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	@JsonView(DataTablesOutput.View.class)
	public @ResponseBody DataTablesOutput<LogServer> saveLogServer(@RequestBody final LogServerDTO logServerForm, final BindingResult bindingResult) {
		final DataTablesOutput<LogServer> dtOutput = new DataTablesOutput<LogServer>();
		List<LogServer> listNewLogServer = new ArrayList<LogServer>();
		if (bindingResult.hasErrors()) {
			listNewLogServer = StreamSupport.stream(this.logServerService.getAllLogServer().spliterator(), false).collect(Collectors.toList());
			final JSONObject json = new JSONObject();
			for (final FieldError o: bindingResult.getFieldErrors()) {
				json.put("invalid-" + o.getField(), o.getDefaultMessage());
			}
			dtOutput.setError(json.toString());
		} else {
			try {
				final LogServer logServer = this.logServerService.saveLogServer(logServerForm);
				listNewLogServer.add(logServer);
			} catch (final Exception e) {
				listNewLogServer = StreamSupport.stream(this.logServerService.getAllLogServer().spliterator(), false).collect(Collectors.toList());
				throw e;
			}
		}
		dtOutput.setData(listNewLogServer);
		return dtOutput;
	}
	
	/**
	 * Method that maps the delete user request from data table to the controller
	 * and performs the delete of the log server identified by its id.
	 * @param logServerId Identifier of the log server to be deleted.
	 * @param index Row index of the data table.
	 * @return String that represents the name of the view to redirect.
	 */
	@JsonView(DataTablesOutput.View.class)
	@RequestMapping(path = "/deletelogserver", method = RequestMethod.POST)
	public String deleteSpl(@RequestParam("id") final Long logServerId, @RequestParam("index") final String index) {
		String rowIndex = index;
		try {
			this.logServerService.deleteLogServerById(logServerId);
		} catch (final Exception e) {
			rowIndex = Constants.ROW_INDEX_ERROR;
		}
		return rowIndex;
	}

	/**
	 * Method that maps the connection URL to the controller and loads.
	 * * The log server to the backup form.
	 * @param logServerUrlTex Identifier of the SPL to be edited.
	 * @return Boolean with the result of checking.
	 */
	@RequestMapping(value = "checklogserver", method = RequestMethod.POST)
	public Boolean checkConnectionLogServer(@RequestParam("urlTex") final String logServerUrlTex) {
		final boolean checked = this.logConsumerService.echo(logServerUrlTex);
		return new Boolean(checked);
	}
	
	/**
	 * Method that maps the last lines of a log file request and
	 * forwards them to the view.
	 *
	 * @return String that represents the name of the view to forward.
	 */
	@RequestMapping(path = "/loglastlines", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public void logLastLines(@RequestBody final LogLastLinesFormDTO requestForm,
			final HttpServletRequest request, final HttpServletResponse response) throws IOException {
		final HttpSession session = request.getSession(false);
		if (session == null) {
			response.sendError(401, "Sesi&oacute;n caducada");
			response.flushBuffer();
			return;
		}
		LogDataDTO logData;
		if (!requestForm.isMore()) {
			logData = this.logConsumerService.lastLines(requestForm.getLogFilename(),
			                                            requestForm.getNumLines());
		}
		else {
			logData = this.logConsumerService.getMore(requestForm.getNumLines());
		}
		response.setContentType("text/plain");
		response.setCharacterEncoding(requestForm.getCharsetName());
		if (logData.getErrorMessage() != null) {
			response.sendError(logData.getErrorCode(), logData.getErrorMessage());
		}
		else if (logData.getLog() != null) {
			response.getOutputStream().write(logData.getLog());
		}
		response.flushBuffer();
	}
	
	/**
     * Method that maps the openning file request to the controller, select the
     * file and show the log search screen.
     * @param logFileName Name/Id  of the log file.
     * @param model Holder object for model attributes.
     * @throws IOException Error related with the file selection.
     */
    @RequestMapping(value = "downloadlogfile", produces = "application/zip")
    public void downloadLog(@RequestParam("filename") final String logFilename,
    		final HttpServletRequest request, final HttpServletResponse response, final Model model) throws IOException {

		final HttpSession session = request.getSession(false);
		if (session == null) {
			response.sendError(403, "Sesi&oacute;n caducada");
			response.flushBuffer();
			return;
		}

    	final DownloadedLogFileDTO downloadResult = this.logConsumerService.downloadLogFile(logFilename);

    	if (downloadResult.getError() != null) {
    		LOGGER.warn("Error al descargar el fichero de log: " + downloadResult.getError());
    		response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, downloadResult.getError());
    		return;
    	}

    	final String fileName = "attachment; filename=" + downloadResult.getFilename();

		response.setHeader("Content-Disposition", fileName);
		response.setContentType(downloadResult.getContentType());
		response.setCharacterEncoding(StandardCharsets.ISO_8859_1.name());
		FileCopyUtils.copy(downloadResult.getData(), response.getOutputStream());
		response.flushBuffer();
    }
    
    /**
	 * Method that maps the last lines of a log file request and
	 * forwards them to the view.
	 *
	 * @return String that represents the name of the view to forward.
	 */
	@RequestMapping(path = "/logfilter", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public void logFilterLines(@RequestBody final LogFilterFormDTO requestForm,
			final HttpServletRequest request, final HttpServletResponse response) throws IOException {
		final HttpSession session = request.getSession(false);
		if (session == null) {
			response.sendError(401, "Sesi&oacute;n caducada");
			response.flushBuffer();
			return;
		}
		final SimpleDateFormat formatter = new SimpleDateFormat(DATE_FIELD_FORMAT, Locale.ROOT);
		long startDate = 0;
		if (requestForm.getStartDate() != null && !requestForm.getStartDate().isEmpty()) {
			try {
				startDate = formatter.parse(requestForm.getStartDate()).getTime();
			} catch (final ParseException e) {
				LOGGER.warn("Se ha enviado una fecha de fin con formato invalido: " + e);
				startDate = 0;
			}
		}
		long endDate = 0;
		if (requestForm.getEndDate() != null && !requestForm.getEndDate().isEmpty()) {
			try {
				endDate = formatter.parse(requestForm.getEndDate()).getTime();
			} catch (final ParseException e) {
				LOGGER.warn("Se ha enviado una fecha de fin con formato invalido: " + e);
				endDate = 0;
			}
		}
		final LogDataDTO logData = this.logConsumerService.filterLines(requestForm.getNumLines(),
		                                                               startDate,
		                                                               endDate,
		                                                               requestForm.getLevel(),
		                                                               requestForm.isMore());
		response.setContentType("text/plain");
		response.setCharacterEncoding(requestForm.getCharsetName());
		if (logData.getErrorMessage() != null) {
			response.sendError(logData.getErrorCode(), logData.getErrorMessage());
		}
		else {
			response.getOutputStream().write(logData.getLog());
		}
		response.flushBuffer();
	}

	/**
	 * Method that maps the text searches of a log file request and
	 * forwards them to the view.
	 *
	 * @return String that represents the name of the view to forward.
	 */
	@RequestMapping(path = "/searchtext", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public void logSearchText(@RequestBody final LogSearchTextFormDTO requestForm,
			final HttpServletRequest request, final HttpServletResponse response) throws IOException {
		final HttpSession session = request.getSession(false);
		if (session == null) {
			response.sendError(401, "Sesi&oacute;n caducada");
			response.flushBuffer();
			return;
		}
		final SimpleDateFormat formatter = new SimpleDateFormat(DATE_FIELD_FORMAT, Locale.ROOT);
		long startDate = 0;
		if (requestForm.getStartDate() != null && !requestForm.getStartDate().isEmpty()) {
			try {
				startDate = formatter.parse(requestForm.getStartDate()).getTime();
			} catch (final ParseException e) {
				LOGGER.warn("Se ha enviado una fecha de fin con formato invalido: " + e);
				startDate = 0;
			}
		}
		final LogDataDTO logData = this.logConsumerService.searchText(requestForm.getNumLines(),
			                                             requestForm.getText(),
			                                             startDate, requestForm.isMore());
		response.setContentType("text/plain");
		response.setCharacterEncoding(requestForm.getCharsetName());
		if (logData.getErrorMessage() != null) {
			response.sendError(logData.getErrorCode(), logData.getErrorMessage());
		}
		else {
			response.getOutputStream().write(logData.getLog());
		}
		response.flushBuffer();
	}

	/**
	 * Method that maps the text searches of a log file request and
	 * forwards them to the view.
	 *
	 * @return String that represents the name of the view to forward.
	 */
	@RequestMapping(path = "/searchTextMore", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public void searchTextMore(@RequestBody final LogSearchTextFormDTO requestForm,
			final HttpServletRequest request, final HttpServletResponse response) throws IOException {
		final HttpSession session = request.getSession(false);
		if (session == null) {
			response.sendError(401, "Sesi&oacute;n caducada");
			response.flushBuffer();
			return;
		}
		final LogDataDTO logData = this.logConsumerService.getMore(requestForm.getNumLines());
		response.setContentType("text/plain");
		response.setCharacterEncoding(requestForm.getCharsetName());
		if (logData.getErrorMessage() != null) {
			response.sendError(logData.getErrorCode(), logData.getErrorMessage());
		}
		else if (logData.getLog() != null) {
			response.getOutputStream().write(logData.getLog());
		}
		response.flushBuffer();
	}

}
