/*
/*******************************************************************************
 * Copyright (C) 2018 MINHAFP, Gobierno de Espa&ntilde;a
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
 * <b>File:</b><p>es.gob.fire.web.controller.LogServerController.java.</p>
 * <b>Description:</b><p>Class that manages the requests related to the log servers administration.</p>
  * <b>Project:</b><p>Application for signing documents of @firma suite systems</p>
 * <b>Date:</b><p>14/04/2020.</p>
 * @author Gobierno de Espa&ntilde;a.
 * @version 1.1, 02/06/2021.
 */
package es.gob.fire.web.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import es.gob.fire.persistence.dto.LogConsumerConnectionDTO;
import es.gob.fire.persistence.dto.LogFileInfoDTO;
import es.gob.fire.persistence.dto.LogServerDTO;
import es.gob.fire.persistence.entity.LogServer;
import es.gob.fire.persistence.service.ILogConsumerService;
import es.gob.fire.persistence.service.ILogServerService;

/**
 * <p>Class that manages the requests related to the log servers administration.</p>
 * <b>Project:</b><p>Application for signing documents of @firma suite systems.</p>
 * @version 1.1, 02/06/2021.
 */
@Controller
public class LogServerController {

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
	 * Attribute that represents the injected interface that store the information
	 * of the current LogConsumer.
	 */
	@Autowired
	private LogConsumerConnectionDTO logConsumerConnectionDTO;

	/**
	 * Method that maps the web requests to the controller and forwards the list of log server
	 * to the view.
	 * @param model Holder object for model attributes.
	 * @return String that represents the name of the view to forward.
	 */
	@RequestMapping(value = "logserver", method = RequestMethod.GET)
	public String logServer(final Model model) {
		return "fragments/logserver.html";
	}

	/**
	 * Method that maps the add log server web request to the controller and sets
	 * the backing form.
	 * @param model Holder object for model attributes.
	 * @return String that represents the name of the view to forward.
	 */
	@RequestMapping(value = "addlogserver", method = RequestMethod.GET)
	public String addLogServer(final Model model) {
		final LogServerDTO logServerForm = new LogServerDTO();
		logServerForm.setVerifySSL(Boolean.TRUE);
		model.addAttribute("logServerForm", logServerForm);
		//model.addAttribute("verifySSLCheckbox", Boolean.TRUE);
		return "modal/logServerForm";
	}

	/**
	 * Method that maps the edit web request to the controller and loads
	 * the log server to the backing form.
	 * @param logServerId Identifier of the log server to be edited.
	 * @param model Holder object for model attributes.
	 * @return String that represents the name of the view to forward.
	 */
	@RequestMapping(value = "editlogserver", method = RequestMethod.POST)
	public String editLogServer(@RequestParam("id") final Long logServerId, final Model model) {
		final LogServer logServer = this.logServerService.getLogServerByLogServerId(logServerId);

		final LogServerDTO logServerForm = new LogServerDTO();
		logServerForm.setLogServerId(logServerId);
		logServerForm.setName(logServer.getName());
		logServerForm.setUrlService(logServer.getUrlService());
		logServerForm.setKey(logServer.getKey());
		logServerForm.setVerifySSL(logServer.getVerifySSL());

		model.addAttribute("logServerForm", logServerForm);
		return "modal/logServerForm";
	}

	/**
	 * Method that maps the connecting request to the log server and list its log files.
	 * @param logServerId Identifier of the log server to connect.
	 * @param name Name of the log server to connect.
	 * @param model Holder object for model attributes.
	 * @return String that represents the name of the view to forward.
	 * @throws IOException Error related with the connection to the log server.
	 */
	@RequestMapping(value = "connectlogserver", method = RequestMethod.POST)
	public String connectLogServer(@RequestParam("id") final Long logServerId, @RequestParam("name") final String name, final Model model) throws IOException {
		final LogServer logServer = this.logServerService.getLogServerByLogServerId(logServerId);
		// Configuramos el servicio para que en las siguientes llamadas se pueda
		// recuperar la informacion de los logs
		this.logConsumerService.connect(logServer.getUrlService(), logServer.getKey(), logServer.getVerifySSL());
		// Guardamos la informacion del servicio de logs
		this.logConsumerConnectionDTO.setServerInfo(logServerId, name);
		model.addAttribute("connectioninfo", this.logConsumerConnectionDTO);
		return "fragments/logserverfiles.html";
	}

	/**
	 * Method that maps the disconnecting request to the current SPL and list the SPLs.
	 * @return String that represents the name of the view to forward.
	 */
	@RequestMapping(value = "disconnectlogserver", method = RequestMethod.POST)
	public String disconnectSpl() {
		this.logConsumerService.closeConnection();
		// Eliminamos la informacion del servicio de logs
		this.logConsumerConnectionDTO.setServerInfo(null, null);
		return "fragments/logserver.html";
	}

	/**
	 * Method that maps the openning file request to the controller, select the
	 * file and show the log search screen.
	 * @param logFileName Name/Id  of the log file.
	 * @param model Holder object for model attributes.
	 * @return String that represents the name of the view to forward.
	 * @throws IOException Error related with the file selection.
	 */
	@RequestMapping(value = "openlogfile", method = RequestMethod.POST)
	public String openLogFile(@RequestParam("id") final String logFileName, final Model model) throws IOException {
		final LogFileInfoDTO logInfo = this.logConsumerService.openLogFile(logFileName);
		this.logConsumerConnectionDTO.setFilename(logFileName);
		model.addAttribute("connectioninfo", this.logConsumerConnectionDTO);
		model.addAttribute("searchinfoform", logInfo);
		return "fragments/logsearch.html";
	}

	/**
	 * Method that maps the closing file request to the controller, close the
	 * file and show the log files list.
	 * @param model Holder object for model attributes.
	 * @return String that represents the name of the view to forward.
	 */
	@RequestMapping(value = "closelogfile", method = RequestMethod.POST)
	public String closeLogFile(final Model model) {
		this.logConsumerService.closeLogFile();
		// Borramos la referencia al fichero que cerramos
		this.logConsumerConnectionDTO.setFilename(null);
		model.addAttribute("connectioninfo", this.logConsumerConnectionDTO);
	    return "fragments/logserverfiles.html";
	}
}
