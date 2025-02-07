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
 * <b>File:</b><p>es.gob.fire.service.impl.PlannerService.java.</p>
 * <b>Description:</b><p>Class that implements the communication with the operations of the persistence layer for Planner.</p>
 * <b>Project:</b><p>Horizontal platform of validation services of multiPKI certificates and electronic signature.</p>
 * <b>Date:</b><p>03/10/2020.</p>
 * @author Gobierno de España.
 * @version 1.3, 13/06/2023.
 */
package es.gob.fire.service.impl;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.gob.fire.persistence.entity.Planner;
import es.gob.fire.persistence.repository.PlannerRepository;
import es.gob.fire.service.IPlannerService;

/**
 * <p>Class that implements the communication with the operations of the persistence layer for Planner.</p>
 * <b>Project:</b><p>Horizontal platform of validation services of multiPKI
 * certificates and electronic signature.</p>
 * @version 1.3, 13/06/2023.
 */
@Service("plannerService")
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class PlannerService implements IPlannerService {

	/**
	 * Attribute that represents the injected interface that provides CRUD operations for the persistence.
	 */
	@Autowired
	private PlannerRepository repository;
	
	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.service.IPlannerService#getPlannerById(java.lang.Long)
	 */
	@Override
	public Planner getPlannerById(Long idPlanner) {
		return repository.findByIdPlanner(idPlanner);

	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.service.IPlannerService#savePlanner(es.gob.fire.persistence.entity.Planner)
	 */
	@Override
	public Planner savePlanner(Planner planner) {
		return repository.save(planner);
	}

	/**
	 * 
	 * {@inheritDoc}
	 * @see es.gob.fire.service.IPlannerService#savePlanner(es.gob.fire.persistence.entity.Planner, javax.servlet.http.HttpServletRequest, java.lang.String, java.util.Date, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public Planner savePlanner(Planner planner, HttpServletRequest httpServletRequest, String auditTransNumber, Date auditDate, String operation, String subOperation, String module) {
		return planner;
		// return iAuditService.savePlannerAndAuditOp(planner, httpServletRequest, auditTransNumber, auditDate, module, operation, subOperation);
	}
	
	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.service.IPlannerService#deletePlanner(java.lang.Long)
	 */
	@Override
	@Transactional
	public void deletePlanner(final Long plannerId) {
		repository.deleteById(plannerId);

	}

}
