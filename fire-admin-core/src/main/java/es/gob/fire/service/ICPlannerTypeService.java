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
 * <b>File:</b><p>es.gob.fire.service.ICPlannerTypeService.java.</p>
 * <b>Description:</b><p>Interface that provides communication with the operations of the persistence layer related to CPlannerType.</p>
 * <b>Project:</b><p>Horizontal platform of validation services of multiPKI
 * certificates and electronic signature.</p>
 * <b>Date:</b><p>15/05/2020.</p>
 * @author Gobierno de España.
 * @version 1.0, 15/05/2020.
 */
package es.gob.fire.service;

import java.util.List;

import es.gob.fire.persistence.entity.CPlannerType;

/**
 * <p>Interface that provides communication with the operations of the persistence layer related to CPlannerType.</p>
 * <b>Project:</b><p>Horizontal platform of validation services of multiPKI
 * certificates and electronic signature.</p>
 * @version 1.0, 15/05/2020.
 */
public interface ICPlannerTypeService {

	/**
	 * Method that gets the list of all type of planners.
	 * @return List of type of planners.
	 */
	List<CPlannerType> getAllPlannerType();

	/**
	 * Method that obtains from the persistence a CPlannerType identified by its id.
	 * @param idCPlannerType Long that represents the ID of the Planner Type.
	 * @return {@link CPlannerType} an object that represents the CPlannerType.
	 */
	CPlannerType getCPlannerTypeById(Long idCPlannerType);

}
