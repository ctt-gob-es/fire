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
 * <b>File:</b><p>es.gob.fire.persistence.service.impl.SignatureService.java.</p> 
 * <b>Description:</b><p>Class that implements the communication with the operations of the persistence layer.</p>
 * <b>Project:</b><p>Platform for signing documents.</p>
 * <b>Date:</b><p>15/06/2018.</p>
 * @author Gobierno de España.
 * @version 1.0, 15/06/2018.
 */
package es.gob.fire.persistence.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.stereotype.Service;

import es.gob.fire.persistence.dto.SignatureDTO;
import es.gob.fire.persistence.entity.Signature;
import es.gob.fire.persistence.repository.SignatureRepository;
import es.gob.fire.persistence.repository.datatable.SignatureDataTablesRepository;
import es.gob.fire.persistence.service.ISignatureService;

/**
 * <p>
 * Class that implements the communication with the operations of the
 * persistence layer.
 * </p>
 * <b>Project:</b>
 * <p>
 * Platform for detection and validation of certificates recognized in European
 * TSL.
 * </p>
 * 
 * @version 1.0, 15/06/2018.
 */
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class SignatureService implements ISignatureService {

	/**
	 * Attribute that represents the injected interface that proves CRUD
	 * operations for the persistence.
	 */
	@Autowired
	private SignatureRepository repository;

	/**
	 * Attribute that represents the injected interface that provides CRUD
	 * operations for the persistence.
	 */
	@Autowired
	private SignatureDataTablesRepository dtRepository;

	/**
	 * Attribute that represents the entity manager.
	 */
	@Autowired
	private EntityManager entityManager;

	/**
	 * {@inheritDoc}
	 * 
	 * @see es.gob.fire.persistence.service.ISignatureService#getSignatureBySignatureId(java.lang.Long)
	 */
	@Override
	public Signature getSignatureBySignatureId(final Long signatureId) {
		return this.repository.findBySignatureId(signatureId);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see es.gob.fire.persistence.service.ISignatureService#getSignaturesByApplication(java.lang.Integer,
	 *      java.lang.Integer)
	 */
	@SuppressWarnings("unchecked")
	public List<SignatureDTO> getSignaturesByApplication(final Integer month, final Integer year) {
		List<SignatureDTO> signatures = null;
		Query nativeQuery = entityManager.createNativeQuery(
				"select  f.aplicacion, " 
						+ " sum(case when f.correcta = '1' then total else 0 end) as corrects, "
						+ " sum(case when f.correcta = '0' then total else 0 end) as incorrects "
						+ " from tb_firmas f"
						+ " where extract(month from f.fecha) = ? and extract(year from f.fecha) = ? "
						+ " group by f.aplicacion ");

		nativeQuery.setParameter(1, month);
		nativeQuery.setParameter(2, year);

		List<Object[]> results = nativeQuery.getResultList();

		signatures = results.stream()
				.map(result -> new SignatureDTO((String) result[0], ((BigDecimal) result[1]).intValue(),
						((BigDecimal) result[2]).intValue(), ((BigDecimal) result[1]).intValue() + ((BigDecimal) result[2]).intValue()))
				.collect(Collectors.toList());

		return signatures;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see es.gob.fire.persistence.service.ISignatureService#getSignaturesByProvider(java.lang.Integer,
	 *      java.lang.Integer)
	 */
	@SuppressWarnings("unchecked")
	public List<SignatureDTO> getSignaturesByProvider(final Integer month, final Integer year) {
		List<SignatureDTO> signatures = null;
		Query nativeQuery = entityManager.createNativeQuery(
				"select  f.proveedor, " 
						+ " sum(case when f.correcta = '1' then total else 0 end) as corrects, "
						+ " sum(case when f.correcta = '0' then total else 0 end) as incorrects "
						+ " from tb_firmas f"
						+ " where extract(month from f.fecha) = ? and extract(year from f.fecha) = ? "
						+ " group by f.proveedor ");

		nativeQuery.setParameter(1, month);
		nativeQuery.setParameter(2, year);

		List<Object[]> results = nativeQuery.getResultList();

		signatures = results.stream()
				.map(result -> new SignatureDTO((String) result[0], ((BigDecimal) result[1]).intValue(),
						((BigDecimal) result[2]).intValue(), ((BigDecimal) result[1]).intValue() + ((BigDecimal) result[2]).intValue()))
				.collect(Collectors.toList());

		return signatures;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * @see es.gob.fire.persistence.service.ISignatureService#getSignaturesByFormat(java.lang.Integer,
	 *      java.lang.Integer)
	 */
	@SuppressWarnings("unchecked")
	public List<SignatureDTO> getSignaturesByFormat(final Integer month, final Integer year) {
		List<SignatureDTO> signatures = null;
		Query nativeQuery = entityManager.createNativeQuery(
				"select f.formato, " 
						+ " sum(case when f.correcta = '1' then total else 0 end) as corrects, "
						+ " sum(case when f.correcta = '0' then total else 0 end) as incorrects "
						+ " from tb_firmas f"
						+ " where extract(month from f.fecha) = ? and extract(year from f.fecha) = ? "
						+ " group by f.formato ");

		nativeQuery.setParameter(1, month);
		nativeQuery.setParameter(2, year);

		List<Object[]> results = nativeQuery.getResultList();

		signatures = results.stream()
				.map(result -> new SignatureDTO((String) result[0], ((BigDecimal) result[1]).intValue(),
						((BigDecimal) result[2]).intValue(), ((BigDecimal) result[1]).intValue() + ((BigDecimal) result[2]).intValue()))
				.collect(Collectors.toList());
		
		return signatures;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * @see es.gob.fire.persistence.service.ISignatureService#getSignaturesByImprovedFormat(java.lang.Integer,
	 *      java.lang.Integer)
	 */
	@SuppressWarnings("unchecked")
	public List<SignatureDTO> getSignaturesByImprovedFormat(final Integer month, final Integer year) {
		List<SignatureDTO> signatures = null;
		Query nativeQuery = entityManager.createNativeQuery(
				"select f.formato_mejorado, " 
						+ " sum(case when f.correcta = '1' then total else 0 end) as corrects, "
						+ " sum(case when f.correcta = '0' then total else 0 end) as incorrects "
						+ " from tb_firmas f"
						+ " where extract(month from f.fecha) = ? and extract(year from f.fecha) = ? "
						+ " group by f.formato_mejorado ");

		nativeQuery.setParameter(1, month);
		nativeQuery.setParameter(2, year);

		List<Object[]> results = nativeQuery.getResultList();

		signatures = results.stream()
				.map(result -> new SignatureDTO((String) result[0], ((BigDecimal) result[1]).intValue(),
						((BigDecimal) result[2]).intValue(), ((BigDecimal) result[1]).intValue() + ((BigDecimal) result[2]).intValue()))
				.collect(Collectors.toList());
		
		return signatures;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * @see es.gob.fire.persistence.services.ISignatureService#saveSignature(es.gob.fire.persistence.model.entity.Signature)
	 */
	@Override
	public Signature saveSignature(final Signature signature) {
		return repository.save(signature);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see es.gob.fire.persistence.services.ISignatureService#deleteSignature(java.lang.Long)
	 */
	@Override
	public void deleteSignatureById(final Long signatureId) {
		repository.deleteById(signatureId);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see es.gob.fire.persistence.services.ISignatureService#getAllSignature()
	 */
	@Override
	public Iterable<Signature> getAllSignature() {
		return repository.findAll();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see es.gob.fire.persistence.services.ISignatureService#findAll(org.springframework.data.jpa.datatables.mapping.DataTablesInput)
	 */
	@Override
	public DataTablesOutput<Signature> getAllSignature(final DataTablesInput input) {
		return dtRepository.findAll(input);
	}

}
