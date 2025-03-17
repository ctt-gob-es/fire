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
 * <b>File:</b><p>es.gob.afirma.service.impl.CAuthenticationTypeService.java.</p>
 * <b>Description:</b><p>Service for creating Fire authentication type.</p>
 * <b>Project:</b><p></p>
 * <b>Date:</b><p>15/05/2020.</p>
 * @author Gobierno de España.
 * @version 1.0, 04/03/2025.
 */
package es.gob.fire.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import es.gob.fire.i18n.Language;
import es.gob.fire.persistence.dto.CAuthenticationTypeDTO;
import es.gob.fire.persistence.entity.CAuthenticationType;
import es.gob.fire.persistence.repository.CAuthenticationTypeRepository;
import es.gob.fire.service.ICAuthenticationTypeService;

/**
 * <p>Service for creating Fire authentication type.</p>
 * <b>Project:</b><p></p>
 * @version 1.0, 04/03/2025.
 */
@Service("cAuthenticationTypeService")
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class CAuthenticationTypeService implements ICAuthenticationTypeService {

	/**
	 * Attribute that represents the service object for accessing the repository of control access.
	 */
	@Autowired
	private CAuthenticationTypeRepository cAuthenticationTypeRepository;
	
	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.service#obtainAllCAuthenticationTypeDTO()
	 */
	@Override
	public List<CAuthenticationTypeDTO> obtainAllCAuthenticationTypeDTO() {
		List<CAuthenticationTypeDTO> listCAuthenticationTypeDTO = new ArrayList<CAuthenticationTypeDTO>();
		
		for (CAuthenticationType cAuthenticationType : cAuthenticationTypeRepository.findAll()) {
			String tokenNameValue = Language.getResPersistenceConstants(cAuthenticationType.getTokenName());
			CAuthenticationTypeDTO cAuthenticationTypeDTO = new CAuthenticationTypeDTO(cAuthenticationType.getIdAuthenticationType(), tokenNameValue);
			listCAuthenticationTypeDTO.add(cAuthenticationTypeDTO);
		}
		return listCAuthenticationTypeDTO;
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.service#obtainAllCAuthenticationType()
	 */
	@Override
	public List<CAuthenticationType> obtainAllCAuthenticationType() {
		return cAuthenticationTypeRepository.findAll();
	}

}
