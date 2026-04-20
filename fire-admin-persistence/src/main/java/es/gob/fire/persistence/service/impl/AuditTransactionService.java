package es.gob.fire.persistence.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.stereotype.Service;

import es.gob.fire.persistence.entity.AuditSignature;
import es.gob.fire.persistence.entity.AuditTransaction;
import es.gob.fire.persistence.repository.AuditSignatureRepository;
import es.gob.fire.persistence.repository.AuditTransactionRepository;
import es.gob.fire.persistence.repository.datatable.AuditSignatureDataTablesRepository;
import es.gob.fire.persistence.repository.datatable.AuditTransactionDataTablesRepository;
import es.gob.fire.persistence.service.IAuditTransactionService;

@Service
public class AuditTransactionService implements IAuditTransactionService{

	/**
	 * Attribute that represents the injected interface that provides CRUD
	 * operations for the persistence.
	 */
	@Autowired
	private AuditTransactionRepository repository;

	/**
	 * Attribute that represents the injected interface that provides CRUD
	 * operations for the persistence.
	 */
	@Autowired
	private AuditSignatureRepository signatureRepository;

	/**
	 * Attribute that represents the injected interface that provides CRUD
	 * operations for the persistence.
	 */
	@Autowired
	private AuditTransactionDataTablesRepository dtRepository;

	/**
	 * Attribute that represents the injected interface that provides CRUD
	 * operations for the persistence.
	 */
	@Autowired
	private AuditSignatureDataTablesRepository dtBatchSignatureRepository;

	/**
	 * Attribute that represents the injected interface that provides CRUD
	 * operations for the persistence.
	 */

	@Override
	public AuditTransaction getAuditTransactionByAuditTransactionId(final Integer idAuditTransaction) {
		return this.repository.findByIdAuditTransaction(idAuditTransaction);
	}

	@Override
	public List<AuditTransaction> getAllAuditTransactions() {
		return this.repository.findAll();
	}

	@Override
	public DataTablesOutput<AuditTransaction> getAllAuditTransactions(final DataTablesInput input) {
		return this.dtRepository.findAll(input);
	}

	@Override
	public DataTablesOutput<AuditTransaction> getAuditTransactionsWithDateFilter(final DataTablesInput input, final Date fromDate, final Date toDate) {

		final DataTablesOutput<AuditTransaction> dtOutput = new DataTablesOutput<>();

		final int compareMode = fromDate != null && toDate != null ? 1 : fromDate != null ? 2 : toDate != null ? 3 : 4;

		List<AuditTransaction> listPetitions = new ArrayList<>();

		switch (compareMode) {
		case 1:
			listPetitions = this.repository.findByDateBetween(fromDate, toDate);
			break;
		case 2:
			listPetitions = this.repository.findByDateAfter(fromDate);
			break;
		case 3:
			listPetitions = this.repository.findByDateBefore(toDate);
			break;
		default:
			listPetitions = this.repository.findAll();
			break;
		}

		dtOutput.setData(listPetitions);

		return dtOutput;
	}

	@Override
	public DataTablesOutput<AuditSignature> getAllAuditSignaturesOfTransaction(final DataTablesInput input,
			final AuditTransaction auditTransaction) {
		final DataTablesOutput<AuditSignature> dtOutput = new DataTablesOutput<>();

		final String idTransaction = auditTransaction.getIdTransaction();

		final List<AuditSignature> listBatchSignatures = this.signatureRepository.findByIdTransaction(idTransaction);

		dtOutput.setData(listBatchSignatures);

		return dtOutput;
	}

	@Override
	public List<AuditTransaction> getAuditTransactionsWithDateFilter(final Date fromDate, final Date toDate) {
		final int compareMode = fromDate != null && toDate != null ? 1 : fromDate != null ? 2 : toDate != null ? 3 : 4;

		List<AuditTransaction> listPetitions = new ArrayList<>();

		switch (compareMode) {
		case 1:
			listPetitions = this.repository.findByDateBetween(fromDate, toDate);
			break;
		case 2:
			listPetitions = this.repository.findByDateAfter(fromDate);
			break;
		case 3:
			listPetitions = this.repository.findByDateBefore(toDate);
			break;
		default:
			listPetitions = this.repository.findAll();
			break;
		}
		return listPetitions;
	}

	@Override
	public List<AuditSignature> getAllAuditSignature() {
		return this.signatureRepository.findAll();
	}

	@Override
	public DataTablesOutput<AuditTransaction> getAuditTransactionsWithDateFilter(final DataTablesInput input, final Date fromDate, final Date toDate,
			final String app) {
		final DataTablesOutput<AuditTransaction> dtOutput = new DataTablesOutput<>();

		final List<AuditTransaction> listPetitions = this.repository.findByDateRangeAndApplication(fromDate, toDate, app);

		dtOutput.setData(listPetitions);

		return dtOutput;
	}

	@Override
	public DataTablesOutput<AuditTransaction> getAuditTransactionsFirstQuery(final DataTablesInput input, final Integer minutes) {
		final DataTablesOutput<AuditTransaction> dtOutput = new DataTablesOutput<>();

		final List<AuditTransaction> listPetitions = this.repository.findByDateAfter(new Date(System.currentTimeMillis() - minutes * 60 * 1000));

		dtOutput.setData(listPetitions);

		return dtOutput;
	}

	@Override
	public List<String> getApplicationsDropdown() {
		return this.repository.findDistinctApp();
	}

	@Override
	public List<AuditSignature> getAllAuditSignaturesOfTransaction(final AuditTransaction transaction) {

		final String idTransaction = transaction.getIdTransaction();

		if (idTransaction != null && !idTransaction.isEmpty()) {
			return this.signatureRepository.findByIdTransaction(idTransaction);
		}
		return new ArrayList<>();

	}

}
