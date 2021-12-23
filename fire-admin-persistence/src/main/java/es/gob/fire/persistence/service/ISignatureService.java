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
 * <b>File:</b><p>es.gob.fire.persistence.service.ISignatureService.java.</p>
 * <b>Description:</b><p>Interface that provides communication with the operations of the persistence layer.</p>
  * <b>Project:</b><p>Application for signing documents of @firma suite systems</p>
 * <b>Date:</b><p>14/04/2020.</p>
 * @author Gobierno de Espa&ntilde;a.
 * @version 1.0, 14/04/2020.
 */
package es.gob.fire.persistence.service;

import java.util.List;

import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import es.gob.fire.persistence.dto.SignatureDTO;
import es.gob.fire.persistence.entity.Signature;

/** 
 * <p>Interface that provides communication with the operations of the persistence layer.</p>
 * <b>Project:</b><p>Application for signing documents of @firma suite systems.</p>
 * @version 1.0, 14/04/2020.
 */
public interface ISignatureService {

	/**
	 * Method that obtains the information for a signature by its identifier.
	 * @param signatureId The signature identifier.
	 * @return {@link Signature}
	 */
	Signature getSignatureBySignatureId(Long signatureId);
	
	/**
     * Method that obtains from the persistence a signatures list grouped by application and filtered by its year and month.
     * @param month month
     * @param year year
     * @return Object list that represents the signatures from the persistence
     */
	List<SignatureDTO> getSignaturesByApplication(Integer month, Integer year);
	
	/**
     * Method that obtains from the persistence a signatures list grouped by provider and filtered by its year and month.
     * @param month month
     * @param year year
     * @return Object list that represents the signatures from the persistence
     */
	List<SignatureDTO> getSignaturesByProvider(Integer month, Integer year);
	
	/**
     * Method that obtains from the persistence a signatures list grouped by format and filtered by its year and month.
     * @param month month
     * @param year year
     * @return Object list that represents the signatures from the persistence
     */
	List<SignatureDTO> getSignaturesByFormat(final Integer month, final Integer year);
	
	/**
     * Method that obtains from the persistence a signatures list grouped by application and filtered by its long live format, year and month.
     * @param month month
     * @param year year
     * @return Object list that represents the signatures from the persistence
     */
	List<SignatureDTO> getSignaturesByImprovedFormat(final Integer month, final Integer year);
	
	/**
	 * Method that stores a signature object.
	 * @param signature signature object
	 * @return {@link Signature}
	 */
	Signature saveSignature(Signature signature);

	/**
	 * Method that deletes a signature in the persistence.
	 * @param signatureId {@link Long} that represents the signature to delete.
	 */
	void deleteSignatureById(Long signatureId);

	/**
	 * Method that gets all the signatures from the persistence.
	 * @return a {@link Iterable<Signature>} with all signatures.
	 */
	Iterable<Signature> getAllSignature();

	/**
	 * Method that returns a list of signatures to be showed in DataTable.
	 * @param input DataTableInput with filtering, paging and sorting configuration.
	 * @return A set of DataTable rows that matches the query.
	 */
	DataTablesOutput<Signature> getAllSignature(DataTablesInput input);
}
