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
 * <b>File:</b><p>es.gob.fire.persistence.repository.LogServerRepository.java.</p>
 * <b>Description:</b><p>Class that represents the transaction repository.</p>
  * <b>Project:</b><p>Application for signing documents of @firma suite systems</p>
 * <b>Date:</b><p>01/04/2020.</p>
 * @author Gobierno de España.
 * @version 1.1, 02/06/2021.
 */
package es.gob.fire.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import es.gob.fire.persistence.entity.LogServer;

/** 
 * <p>Class that represents the log server repository.</p>
 * <b>Project:</b><p>Application for signing documents of @firma suite systems.</p>
 * @version 1.1, 02/06/2021.
 */
@Repository
public interface LogServerRepository extends JpaRepository<LogServer, Long>, JpaSpecificationExecutor<LogServer>  {

	/**
	 * Method that obtains from the persistence a log server identified by its primary key.
	 * @param logServerId Long that represents the primary key of the log server in the persistence.
	 * @return Object that represents a log server from the persistence.
	 */
	LogServer findByLogServerId(Long logServerId);
	
	/**
	 * Method that obtains from the persistence a log server identified by name.
	 * @param name String that represents the value of the column 'name'.
	 * @return Object that represents a log server from the persistence.
	 */
	LogServer findByName(String name);
	
	/**
	 * Method that obtains from the persistence a log server identified by its url service.
	 * @param urlService String that represents the URL service of the log server in the persistence.
	 * @return Object that represents a log server from the persistence.
	 */
	LogServer findByUrlService(String urlService);

}