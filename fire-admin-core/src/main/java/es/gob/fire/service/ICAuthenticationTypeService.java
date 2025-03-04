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
 * <b>File:</b><p>es.gob.fire.service.ICAuthenticationTypeService.java.</p>
 * <b>Description:</b><p> .</p>
  * <b>Project:</b><p></p>
 * <b>Date:</b><p>18/02/2025.</p>
 * @author Gobierno de Espa&ntilde;a.
 * @version 1.2, 20/02/2025.
 */
package es.gob.fire.service;

import java.util.List;

import es.gob.fire.persistence.dto.CAuthenticationTypeDTO;
import es.gob.fire.persistence.entity.CAuthenticationType;

/**
 * <p>Interface that provides communication with the operations of the persistence layer.</p>
 * <b>Project:</b><p></p>
 * @version 1.2, 20/02/2025.
 */
public interface ICAuthenticationTypeService {
	
	/**
	 * Retrieves all authentication types and maps them to {@link CAuthenticationTypeDTO} objects.
	 *
	 * <p>This method fetches all available authentication types from the repository, 
	 * translates the token name using language resources, and converts the data 
	 * into a list of {@link CAuthenticationTypeDTO} objects.
	 *
	 * @return a {@link List} of {@link CAuthenticationTypeDTO} containing all authentication types.
	 */
	List<CAuthenticationTypeDTO> obtainAllCAuthenticationTypeDTO();
	
	/**
	 * Retrieves all authentication types from the database.
	 *
	 * <p>This method fetches all records from the authentication type repository 
	 * and returns them as a list of {@link CAuthenticationType} objects.
	 *
	 * @return a {@link List} of {@link CAuthenticationType} containing all authentication types.
	 */
	List<CAuthenticationType> obtainAllCAuthenticationType();
}
