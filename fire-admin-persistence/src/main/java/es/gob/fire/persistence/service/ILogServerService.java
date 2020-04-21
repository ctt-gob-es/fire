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
 * <b>File:</b><p>es.gob.fire.persistence.service.ILogServerService.java.</p>
 * <b>Description:</b><p>Interface that provides communication with the operations of the persistence layer.</p>
  * <b>Project:</b><p>Application for signing documents of @firma suite systems</p>
 * <b>Date:</b><p>14/04/2020.</p>
 * @author Gobierno de España.
 * @version 1.0, 14/04/2020.
 */
package es.gob.fire.persistence.service;

import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import es.gob.fire.core.dto.LogServerDTO;
import es.gob.fire.persistence.entity.LogServer;

/** 
 * <p>Interface that provides communication with the operations of the persistence layer.</p>
 * <b>Project:</b><p>Application for signing documents of @firma suite systems.</p>
 * @version 1.0, 14/04/2020.
 */
public interface ILogServerService {

	/**
	 * Method that obtains the information for a log server by its identifier.
	 * @param logServerId The log server identifier.
	 * @return {@link LogServer}
	 */
	LogServer getLogServerByLogServerId(Long logServerId);

	/**
	 * Method that obtains the information for a log server by its URL.
	 * @param url The log server URL service.
	 * @return {@link LogServer}
	 */
	LogServer getLogServerByUrlService(String urlService);
	
	/**
	 * Method that stores a log server object.
	 * @param logServer log server object
	 * @return {@link LogServer}
	 */
	LogServer saveLogServer(LogServer logServer);
	
	/**
	 * Method that stores a log server object from log server DTO object.
	 * @param logServer log server DTO object
	 * @return {@link LogServerDTO}
	 */
	LogServer saveLogServer(LogServerDTO logServerDTO);

	/**
	 * Method that deletes a log server in the persistence.
	 * @param logServerId {@link Long} that represents the log server to delete.
	 */
	void deleteLogServerById(Long logServerId);

	/**
	 * Method that gets all the log servers from the persistence.
	 * @return a {@link Iterable<LogServer>} with all log servers.
	 */
	Iterable<LogServer> getAllLogServer();

	/**
	 * Method that returns a list of log serves to be showed in DataTable.
	 * @param input DataTableInput with filtering, paging and sorting configuration.
	 * @return A set of DataTable rows that matches the query.
	 */
	DataTablesOutput<LogServer> getAllLogServer(DataTablesInput input);
}
