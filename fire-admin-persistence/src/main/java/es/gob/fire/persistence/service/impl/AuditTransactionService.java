package es.gob.fire.persistence.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.stereotype.Service;

import es.gob.fire.commons.log.Logger;
import es.gob.fire.persistence.entity.AuditTransaction;
import es.gob.fire.persistence.entity.AuditSignature;
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
	public AuditTransaction getAuditTransactionByAuditTransactionId(Integer idAuditTransaction) {
		return repository.findByIdAuditTransaction(idAuditTransaction);
	}

	@Override
	public List<AuditTransaction> getAllAuditTransactions() {
		return repository.findAll();
	}

	@Override
	public DataTablesOutput<AuditTransaction> getAllAuditTransactions(DataTablesInput input) {
		return dtRepository.findAll(input);
	}

	@Override
	public DataTablesOutput<AuditTransaction> getAuditTransactionsWithDateFilter(DataTablesInput input, Date fromDate, Date toDate) {
		
		DataTablesOutput<AuditTransaction> dtOutput = new DataTablesOutput<AuditTransaction>();
		
		int compareMode = (fromDate != null && toDate != null) ? 1 : (fromDate != null ? 2 : (toDate != null ? 3 : 4)); 
		
		List<AuditTransaction> listPetitions = new ArrayList<AuditTransaction>();
		
		switch (compareMode) {
		case 1:
			listPetitions = repository.findByDateBetween(fromDate, toDate);
			break;
		case 2:
			listPetitions = repository.findByDateAfter(fromDate);
			break;
		case 3:
			listPetitions = repository.findByDateBefore(toDate);
			break;
		default:
			listPetitions = repository.findAll();
			break;
		}
		
		dtOutput.setData(listPetitions);
		
		return dtOutput;
	}

	@Override
	public DataTablesOutput<AuditSignature> getAllAuditSignaturesOfTransaction(DataTablesInput input,
			AuditTransaction auditTransaction) {
		DataTablesOutput<AuditSignature> dtOutput = new DataTablesOutput<AuditSignature>();
		
		String idTransaction = auditTransaction.getIdTransaction();
		
		List<AuditSignature> listBatchSignatures = signatureRepository.findByIdTransaction(idTransaction);
		
		dtOutput.setData(listBatchSignatures);
		
		return dtOutput;
	}

	@Override
	public List<AuditTransaction> getAuditTransactionsWithDateFilter(Date fromDate, Date toDate) {
		int compareMode = (fromDate != null && toDate != null) ? 1 : (fromDate != null ? 2 : (toDate != null ? 3 : 4)); 
		
		List<AuditTransaction> listPetitions = new ArrayList<AuditTransaction>();
		
		switch (compareMode) {
		case 1:
			listPetitions = repository.findByDateBetween(fromDate, toDate);
			break;
		case 2:
			listPetitions = repository.findByDateAfter(fromDate);
			break;
		case 3:
			listPetitions = repository.findByDateBefore(toDate);
			break;
		default:
			listPetitions = repository.findAll();
			break;
		}
		return listPetitions;
	}

	@Override
	public List<AuditSignature> getAllAuditSignature() {
		return signatureRepository.findAll();
	}

	@Override
	public DataTablesOutput<AuditTransaction> getAuditTransactionsWithDateFilter(DataTablesInput input, Date fromDate, Date toDate,
			String app) {
		DataTablesOutput<AuditTransaction> dtOutput = new DataTablesOutput<AuditTransaction>();
		
		List<AuditTransaction> listPetitions = repository.findByDateRangeAndApplication(fromDate, toDate, app);
		
		dtOutput.setData(listPetitions);
		
		return dtOutput;
	}

	@Override
	public DataTablesOutput<AuditTransaction> getAuditTransactionsFirstQuery(DataTablesInput input, Integer minutes) {
		DataTablesOutput<AuditTransaction> dtOutput = new DataTablesOutput<AuditTransaction>();
		
		List<AuditTransaction> listPetitions = repository.findByDateAfter(new Date(System.currentTimeMillis() - minutes * 60 * 1000));
				
		dtOutput.setData(listPetitions);
		
		return dtOutput;
	}

	@Override
	public List<String> getApplicationsDropdown() {
		return repository.findDistinctApp();
	}

}
