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
 * <b>File:</b><p>es.gob.fire.service.IPlannerService.java.</p>
 * <b>Description:</b><p>Interface that provides communication with the operations of the persistence layer related to Planner.</p>
 * <b>Project:</b><p>Horizontal platform of validation services of multiPKI
 * certificates and electronic signature.</p>
 * <b>Date:</b><p>15/05/2020.</p>
 * @author Gobierno de España.
 * @version 1.2, 13/06/2023.
 */
package es.gob.fire.service;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import es.gob.fire.persistence.entity.Planner;

/**
 * <p>Interface that provides communication with the operations of the persistence layer related to Planner.</p>
 * <b>Project:</b><p>Horizontal platform of validation services of multiPKI
 * certificates and electronic signature.</p>
 * @version 1.2, 13/06/2023.
 */
public interface IPlannerService {

	/**
	 * Method that obtains a planner by its identifier.
	 * @param idPlanner The planner identifier.
	 * @return {@link Planner} an object that represents the planner.
	 */
	Planner getPlannerById(Long idPlanner);

	/**
	 * Method that saves planner.
	 * @param planner Planner to update.
	 * @return {@link Planner} an object that represents the Planner.
	 */
	Planner savePlanner(Planner planner);
	
	/**
	 * Method that deletes a planner in the persistence.
	 * @param plannerId {@link Integer} that represents the planner identifier to delete.
	 */
	void deletePlanner(Long plannerId);
	
	/**
	 * Method that use repository for save Planner entity and realize operation DML.
	 * 
	 * @param planner Planner that persist in BD.
	 * @param httpServletRequest HttpServletRequest that contain user that realize operation DML.
	 * @param auditTransNumber String that contain id transacction.
	 * @param auditDate Date that contain date audit.
	 * @param subOperation String with the subOperation.
	 * @param module String with the module.
	 * @return Object that persist.
	 */
	Planner savePlanner(Planner planner, HttpServletRequest httpServletRequest, String auditTransNumber, Date auditDate, String operation, String subOperation, String module);

}

