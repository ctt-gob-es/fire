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
 * <b>File:</b><p>es.gob.fire.persistence.service.impl.LogServerService.java.</p> * <b>Description:</b><p>Class that implements the communication with the operations of the persistence layer.</p>
 * <b>Project:</b><p>Platform for signing documents.</p>
 * <b>Date:</b><p>15/06/2018.</p>
 * @author Gobierno de España.
 * @version 1.1, 02/06/2021.
 */
package es.gob.fire.persistence.service.impl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.stereotype.Service;

import es.gob.fire.persistence.dto.LogServerDTO;
import es.gob.fire.persistence.entity.LogServer;
import es.gob.fire.persistence.repository.LogServerRepository;
import es.gob.fire.persistence.repository.datatable.LogServerDataTablesRepository;
import es.gob.fire.persistence.service.ILogServerService;

/**
 * <p>Class that implements the communication with the operations of the persistence layer.</p>
 * <b>Project:</b><p>Platform for detection and validation of certificates recognized in European TSL.</p>
 * @version 1.1, 02/06/2021.
 */
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class LogServerService implements ILogServerService {

	/**
	 * Attribute that represents the injected interface that proves CRUD operations for the persistence.
	 */
	@Autowired
	private LogServerRepository repository;

	/**
	 * Attribute that represents the injected interface that provides CRUD operations for the persistence.
	 */
	@Autowired
	private LogServerDataTablesRepository dtRepository;

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.service.ILogServerService#getLogServerByLogServerId(java.lang.Long)
	 */
	@Override
	public LogServer getLogServerByLogServerId(final Long logServerId) {
		return this.repository.findByLogServerId(logServerId);
	}
	
	/* (non-Javadoc)
	 * @see es.gob.fire.persistence.service.ILogServerService#getLogServerByName(java.lang.String)
	 */
	@Override
	public LogServer getLogServerByName(String name) {		
		return this.repository.findByName(name);
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.service.ILogServerService#getLogServerByUrlService(java.lang.String)
	 */
	@Override
	public LogServer getLogServerByUrlService(final String urlService) {
		return this.repository.findByUrlService(urlService);
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.services.ILogServerService#saveUser(es.gob.fire.persistence.model.entity.LogServer)
	 */
	@Override
	public LogServer saveLogServer(final LogServer logServer) {
		return repository.save(logServer);
	}
	
	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.service.ILogServerService#saveLogServer(es.gob.fire.core.dto.LogServerDTO)
	 */
	@Override
	public LogServer saveLogServer(final LogServerDTO logServerDTO) {
		LogServer logServer;
		
		if (logServerDTO.getLogServerId() != null) {
			logServer = this.repository.findByLogServerId(logServerDTO.getLogServerId());
		} else {
			logServer = new LogServer();
		}
		
		logServer.setName(logServerDTO.getName());
		logServer.setUrlService(logServerDTO.getUrlService());
		logServer.setKey(logServerDTO.getKey());
		logServer.setVerifySSL(logServerDTO.getVerifySSL());
		
		return this.repository.save(logServer);
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.services.ILogServerService#deleteLogServer(java.lang.Long)
	 */
	@Override
	public void deleteLogServerById(final Long logServerId) {
		repository.deleteById(logServerId);
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.services.ILogServerService#getAllLogServer()
	 */
	@Override
	public Iterable<LogServer> getAllLogServer() {
		return repository.findAll();
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.services.ILogServerService#findAll(org.springframework.data.jpa.datatables.mapping.DataTablesInput)
	 */
	@Override
	public DataTablesOutput<LogServer> getAllLogServer(final DataTablesInput input) {
		return dtRepository.findAll(input);
	}

}
