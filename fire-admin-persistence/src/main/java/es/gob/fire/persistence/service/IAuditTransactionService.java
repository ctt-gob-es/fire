package es.gob.fire.persistence.service;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import es.gob.fire.persistence.entity.AuditTransaction;
import es.gob.fire.persistence.entity.AuditSignature;
import es.gob.fire.persistence.entity.Transaction;

/** 
 * <p>Interface that provides communication with the operations of the persistence layer.</p>
 * <b>Project:</b><p>Platform for detection and validation of certificates recognized in European TSL.</p>
 * @version 1.0, 10/08/2023.
 */
public interface IAuditTransactionService {

	/**
	 * Method that obtains the information for a transaction by its identifier.
	 * @param idAuditTransaction The petition identifier.
	 * @return {@link AuditTransaction}
	 */
	AuditTransaction getAuditTransactionByAuditTransactionId(Integer idAuditTransaction);
	
	/**
	 * Method that gets all the petitions from the persistence.
	 * @return a {@link List<Petition>} with all petitions.
	 */
	List<AuditTransaction> getAllAuditTransactions();
	
	/**
	 * Method that returns a list of petitions to be showed in DataTable.
	 * @param input DataTableInput with filtering, paging and sorting configuration.
	 * @return A set of DataTable rows that matches the query.
	 */
	DataTablesOutput<AuditTransaction> getAllAuditTransactions(DataTablesInput input);
	
	/**
	 * Method that returns a list of petitions to be showed in DataTable.
	 * @param fromDate Date the date when the filter starts.
	 * @param toDate Date the date when the filter ends.
	 * @return A list of petitions that matches the query.
	 */
	List<AuditTransaction> getAuditTransactionsWithDateFilter(Date fromDate, Date toDate);
	
	/**
	 * Method that returns a list of petitions to be showed in DataTable.
	 * @param input DataTableInput with filtering, paging and sorting configuration.
	 * @param fromDate Date the date when the filter starts.
	 * @param toDate Date the date when the filter ends.
	 * @return A set of DataTable rows that matches the query.
	 */
	DataTablesOutput<AuditTransaction> getAuditTransactionsWithDateFilter(DataTablesInput input, Date fromDate, Date toDate);
	
	/**
	 * Method that gets all the petitions batch signatures from the persistence.
	 * @return a {@link List<PetitionBatchSignature>} with all petitions.
	 */
	List<AuditSignature> getAllAuditSignature();
	
	/**
	 * Method that returns a list of signatures to be showed in DataTable.
	 * @param input DataTableInput with filtering, paging and sorting configuration.
	 * @param transaction AuditTransaction to filter by.
	 * @return A set of DataTable rows that matches the query.
	 */
	DataTablesOutput<AuditSignature> getAllAuditSignaturesOfTransaction(DataTablesInput input, AuditTransaction transaction);
	
	/**
	 * Method that returns a list of petitions to be showed in DataTable.
	 * @param input DataTableInput with filtering, paging and sorting configuration.
	 * @param fromDate Date the date when the filter starts.
	 * @param toDate Date the date when the filter ends.
	 * @param app String the app to filter by.
	 * @return A set of DataTable rows that matches the query.
	 */
	DataTablesOutput<AuditTransaction> getAuditTransactionsWithDateFilter(DataTablesInput input, Date fromDate, Date toDate, String app);
	
	/**
	 * Method that returns a list of petitions to be showed in DataTable.
	 * @param input DataTableInput with filtering, paging and sorting configuration.
	 * @param minutes Integer number of minutes to filter the table by.
	 * @return A set of DataTable rows that matches the query.
	 */
	DataTablesOutput<AuditTransaction> getAuditTransactionsFirstQuery(DataTablesInput input, Integer minutes);
	
	List<String> getApplicationsDropdown();
	
	/**
	 * Method that gets all the petitions batch signatures related to a certain transaction from the persistence.
	 * @param transaction AuditTransaction to filter by.
	 * @return a {@link List<PetitionBatchSignature>} with all petitions related to the given transaction.
	 */
	List<AuditSignature> getAllAuditSignaturesOfTransaction(AuditTransaction transaction);
}
