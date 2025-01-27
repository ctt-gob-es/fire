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
 * <b>File:</b><p>es.gob.fire.persistence.repository.CertificatesApplicationRepository.java.</p>
 * <b>Description:</b><p>Interface that provides CRUD functionality for the CertificatesApplication entity.</p>
  * <b>Project:</b><p>Horizontal platform of validation services of multiPKI
 * certificates and electronic signature.</p>
 * <b>Date:</b><p>27/01/2025.</p>
 * @author Gobierno de España.
 * @version 1.0, 27/01/2025.
 */
package es.gob.fire.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.gob.fire.persistence.entity.CertificatesApplication;
import es.gob.fire.persistence.entity.CertificatesApplicationPK;

/** 
 * <p>Interface that provides CRUD functionality for the CertificatesApplication entity.</p>
 * <b>Project:</b><p>Horizontal platform of validation services of multiPKI
 * certificates and electronic signature.</p>
 * @version 1.0, 27/01/2025.
 */
@Repository
public interface CertificatesApplicationRepository extends JpaRepository<CertificatesApplication, CertificatesApplicationPK>{
	
	/**
	 * Finds a list of CertificatesApplication entities associated with the specified application ID.
	 *
	 * @param appId the unique identifier of the application for which the certificates are to be retrieved.
	 * @return a list of {@link CertificatesApplication} entities corresponding to the given application ID.
	 *         Returns an empty list if no entities are found.
	 */
	List<CertificatesApplication> findByApplicationAppId(String appId);
	
}
