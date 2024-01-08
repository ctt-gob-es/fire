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
 * <b>File:</b><p>es.gob.fire.persistence.service.impl.TransactionService.java.</p>
 * <b>Description:</b><p>Class that implements the communication with the operations of the persistence layer.</p>
 * <b>Project:</b><p>Platform for signing documents.</p>
 * <b>Date:</b><p>15/06/2018.</p>
 * @author Gobierno de Espa&ntilde;a.
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

import es.gob.fire.persistence.dto.TransactionDTO;
import es.gob.fire.persistence.entity.Transaction;
import es.gob.fire.persistence.repository.TransactionRepository;
import es.gob.fire.persistence.repository.datatable.TransactionDataTablesRepository;
import es.gob.fire.persistence.service.ITransactionService;

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
public class TransactionService implements ITransactionService {

	/**
	 * Attribute that represents the injected interface that proves CRUD
	 * operations for the persistence.
	 */
	@Autowired
	private TransactionRepository repository;

	/**
	 * Attribute that represents the injected interface that provides CRUD
	 * operations for the persistence.
	 */
	@Autowired
	private TransactionDataTablesRepository dtRepository;

	/**
	 * Attribute that represents the entity manager.
	 */
	@Autowired
	private EntityManager entityManager;

	/**
	 * {@inheritDoc}
	 *
	 * @see es.gob.fire.persistence.service.ITransactionService#getTransactionByTransactionId(java.lang.Long)
	 */
	@Override
	public Transaction getTransactionByTransactionId(final Long transactionId) {
		return this.repository.findByTransactionId(transactionId);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see es.gob.fire.persistence.service.ITransactionService#getTransactionsByApplication(java.lang.Integer,
	 *      java.lang.Integer)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<TransactionDTO> getTransactionsByApplication(final Integer month, final Integer year) {
		List<TransactionDTO> transactions = null;
		final Query nativeQuery = this.entityManager.createNativeQuery(
				"SELECT  t.aplicacion, "
						+ " SUM(t.correcta) AS corrects, "
						+ " SUM(CASE WHEN t.correcta = 0 THEN 1 ELSE 0 END) AS incorrects "
						+ " FROM tb_transacciones t"
						+ " WHERE EXTRACT(MONTH FROM t.fecha) = ? AND EXTRACT(YEAR FROM t.fecha) = ? "
						+ " GROUP BY t.aplicacion ");

		nativeQuery.setParameter(1, month);
		nativeQuery.setParameter(2, year);

		final List<Object[]> results = nativeQuery.getResultList();

		transactions = results.stream()
				.map(result -> new TransactionDTO((String) result[0], ((BigDecimal) result[1]).intValue(),
						((BigDecimal) result[2]).intValue(), ((BigDecimal) result[1]).intValue() + ((BigDecimal) result[2]).intValue()))
				.collect(Collectors.toList());

		return transactions;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see es.gob.fire.persistence.service.ITransactionService#getTransactionsByProvider(java.lang.Integer,
	 *      java.lang.Integer)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<TransactionDTO> getTransactionsByProvider(final Integer month, final Integer year) {
		List<TransactionDTO> transactions = null;
		final Query nativeQuery = this.entityManager.createNativeQuery(
				"SELECT  t.proveedor, "
						+ " SUM(t.correcta) AS corrects, "
						+ " SUM(CASE WHEN t.correcta = 0 THEN 1 ELSE 0 END) AS incorrects "
						+ " FROM tb_transacciones t"
						+ " WHERE EXTRACT(MONTH FROM t.fecha) = ? AND EXTRACT(YEAR FROM t.fecha) = ? "
						+ " GROUP BY t.proveedor ");

		nativeQuery.setParameter(1, month);
		nativeQuery.setParameter(2, year);

		final List<Object[]> results = nativeQuery.getResultList();

		transactions = results.stream()
				.map(result -> new TransactionDTO((String) result[0], ((BigDecimal) result[1]).intValue(),
						((BigDecimal) result[2]).intValue(), ((BigDecimal) result[1]).intValue() + ((BigDecimal) result[2]).intValue()))
				.collect(Collectors.toList());

		return transactions;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see es.gob.fire.persistence.service.ITransactionService#getTransactionsByDatesSizeApp(java.lang.Integer,
	 *      java.lang.Integer)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<TransactionDTO> getTransactionsByDatesSizeApp(final Integer month, final Integer year) {
		List<TransactionDTO> transactions = null;
		final Query nativeQuery = this.entityManager.createNativeQuery(
				"select t.aplicacion, "
						+ "sum(t.tamanno) as sizeBytes "
						+ "from tb_transacciones t "
						+ "where extract(month from t.fecha) = ? and extract(year from t.fecha) = ? "
						+ "group by t.aplicacion ");

		nativeQuery.setParameter(1, month);
		nativeQuery.setParameter(2, year);

		final List<Object[]> results = nativeQuery.getResultList();

		transactions = results.stream()
				.map(result -> new TransactionDTO((String) result[0], Math.floor(((BigDecimal) result[1]).intValue() / (1024 * 1024.0) * 100) / 100))
				.collect(Collectors.toList());

		return transactions;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see es.gob.fire.persistence.service.ITransactionService#getTransactionsByOperation(java.lang.Integer,
	 *      java.lang.Integer)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<TransactionDTO> getTransactionsByOperation(final Integer month, final Integer year) {
		List<TransactionDTO> transactions = null;
		final Query nativeQuery = this.entityManager.createNativeQuery(
				"select t.aplicacion, "
						+ "sum(case when t.operacion = 'SIGN' then (case when t.correcta = '1' then t.total else 0 end) else 0 end ) as correctSimpleSignatures, "
						+ "sum(case when t.operacion = 'SIGN' then (case when t.correcta = '0' then t.total else 0 end) else 0 end ) as incorrectSimpleSignatures, "
						+ "sum(case when t.operacion = 'BATCH' then (case when t.correcta = '1' then t.total else 0 end) else 0 end ) as correctBatchSignatures, "
						+ "sum(case when t.operacion = 'BATCH' then (case when t.correcta = '0' then t.total else 0 end) else 0 end ) as incorrectBatchSignatures "
						+ "from tb_transacciones t "
						+ "where extract(month from t.fecha) = ? and extract(year from t.fecha) = ? "
						+ "group by t.aplicacion ");

		nativeQuery.setParameter(1, month);
		nativeQuery.setParameter(2, year);

		final List<Object[]> results = nativeQuery.getResultList();

		transactions = results.stream()
				.map(result -> new TransactionDTO((String) result[0], ((BigDecimal) result[1]).intValue(),
						((BigDecimal) result[2]).intValue(), ((BigDecimal) result[1]).intValue() + ((BigDecimal) result[2]).intValue(),
						((BigDecimal) result[3]).intValue(), ((BigDecimal) result[4]).intValue(), ((BigDecimal) result[3]).intValue() + ((BigDecimal) result[4]).intValue()))
				.collect(Collectors.toList());

		return transactions;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see es.gob.fire.persistence.services.ITransactionService#saveTransaction(es.gob.fire.persistence.model.entity.Transaction)
	 */
	@Override
	public Transaction saveTransaction(final Transaction transaction) {
		return this.repository.save(transaction);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see es.gob.fire.persistence.services.ITransactionService#deleteTransaction(java.lang.Long)
	 */
	@Override
	public void deleteTransactionById(final Long transactionId) {
		this.repository.deleteById(transactionId);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see es.gob.fire.persistence.services.ITransactionService#getAllTransaction()
	 */
	@Override
	public Iterable<Transaction> getAllTransaction() {
		return this.repository.findAll();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see es.gob.fire.persistence.services.ITransactionService#findAll(org.springframework.data.jpa.datatables.mapping.DataTablesInput)
	 */
	@Override
	public DataTablesOutput<Transaction> getAllTransaction(final DataTablesInput input) {
		return this.dtRepository.findAll(input);
	}

}
