
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
 * <b>File:</b><p>es.gob.fire.persistence.configuration.model.repository.datatable.CertificateDataTablesRepository.java.</p>
 * <b>Description:</b><p>Interface that provides CRUD functionality for the Certificate entity and DataTables.</p>
  * <b>Project:</b><p>Platform for detection and validation of certificates recognized in European TSL.</p>
 * <b>Date:</b><p>18 sept. 2018.</p>
 * @author Gobierno de Espa&ntilde;a.
 * @version 1.0, 18 sept. 2018.
 */
package es.gob.fire.persistence.repository.datatable;

import org.springframework.data.jpa.datatables.repository.DataTablesRepository;

import es.gob.fire.persistence.entity.Certificate;


/**
 * <p>Interface that provides CRUD functionality for the Certificate entity and DataTables.</p>
 * <b>Project:</b><p>Platform for detection and validation of certificates recognized in European TSL.</p>
 * @version 1.0, 18 sept. 2018.
 */
public interface CertificateDataTablesRepository extends DataTablesRepository<Certificate, Long> {
	// Clase vacia
}