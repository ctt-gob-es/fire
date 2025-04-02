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
 * <b>File:</b><p>es.gob.fire.persistence.service.ITransactionService.java.</p>
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

import es.gob.fire.commons.utils.QueryEnum;
import es.gob.fire.persistence.dto.TransactionDTO;
import es.gob.fire.persistence.entity.Transaction;

/** 
 * <p>Interface that provides communication with the operations of the persistence layer.</p>
 * <b>Project:</b><p>Application for signing documents of @firma suite systems.</p>
 * @version 1.0, 14/04/2020.
 */
public interface ITransactionService {

	/**
	 * Method that obtains the information for a transaction by its identifier.
	 * @param transactionId The transaction identifier.
	 * @return {@link Transaction}
	 */
	Transaction getTransactionByTransactionId(Long transactionId);
	
	/**
     * Method that obtains from the persistence a transactions list grouped by application and filtered by its year and month.
     * @param month month
     * @param year year
     * @return Object list that represents the transactions from the persistence
     */
	List<TransactionDTO> getTransactionsByApplication(Integer month, Integer year);
	
	/**
     * Method that obtains from the persistence a transactions list grouped by provider and filtered by its year and month.
     * @param month month
     * @param year year
     * @return Object list that represents the transactions from the persistence
     */
	List<TransactionDTO> getTransactionsByProvider(Integer month, Integer year);
	
	/**
     * Method that obtains from the persistence a transactions list grouped by application and size and filtered by its year and month.
     * @param month month
     * @param year year
     * @return Object list that represents the transactions from the persistence
     */
	List<TransactionDTO> getTransactionsByDatesSizeApp(final Integer month, final Integer year);
	
	/**
     * Method that obtains from the persistence a transactions list grouped by application and filtered by its operation, year and month.
     * @param month month
     * @param year year
     * @return Object list that represents the transactions from the persistence
     */
	List<TransactionDTO> getTransactionsByOperation(final Integer month, final Integer year);
	
	/**
	 * Method that stores a transaction object.
	 * @param transaction transaction object
	 * @return {@link Transaction}
	 */
	Transaction saveTransaction(Transaction transaction);

	/**
	 * Method that deletes a transaction in the persistence.
	 * @param transactionId {@link Long} that represents the transaction to delete.
	 */
	void deleteTransactionById(Long transactionId);

	/**
	 * Method that gets all the transactions from the persistence.
	 * @return a {@link Iterable<Transaction>} with all transactions.
	 */
	Iterable<Transaction> getAllTransaction();

	/**
	 * Method that returns a list of transactions to be showed in DataTable.
	 * @param input DataTableInput with filtering, paging and sorting configuration.
	 * @return A set of DataTable rows that matches the query.
	 */
	DataTablesOutput<Transaction> getAllTransaction(DataTablesInput input);
	
	/**
	 * Retrieves a list of transactions grouped or filtered by application, 
	 * within a date range defined by the provided start and end month/year.
	 *
	 * @param startMonth the starting month (1–12) of the date range
	 * @param startYear the starting year of the date range
	 * @param endMonth the ending month (1–12) of the date range
	 * @param endYear the ending year of the date range
	 * @return a list of {@link TransactionDTO} objects matching the specified date range
	 */
	List<TransactionDTO> getTransactionsByApplication(final Integer startMonth, final Integer startYear, final Integer endMonth, final Integer endYear);
	
	/**
	 * Retrieves a list of transactions grouped or filtered by provider,
	 * within a date range defined by the provided start and end month/year.
	 *
	 * @param startMonth the starting month (1–12) of the date range
	 * @param startYear the starting year of the date range
	 * @param endMonth the ending month (1–12) of the date range
	 * @param endYear the ending year of the date range
	 * @return a list of {@link TransactionDTO} objects matching the specified date range
	 */
	List<TransactionDTO> getTransactionsByProvider(final Integer startMonth, final Integer startYear, final Integer endMonth, final Integer endYear);
	
	/**
	 * Retrieves a list of transactions grouped by application and filtered by size and date range.
	 * The range is defined by the provided start and end month/year values.
	 *
	 * @param startMonth the starting month (1–12) of the date range
	 * @param startYear the starting year of the date range
	 * @param endMonth the ending month (1–12) of the date range
	 * @param endYear the ending year of the date range
	 * @return a list of {@link TransactionDTO} objects filtered by date and size per application
	 */
	List<TransactionDTO> getTransactionsByDatesSizeApp(final Integer startMonth, final Integer startYear, final Integer endMonth, final Integer endYear);
	
	/**
	 * Retrieves a list of transactions grouped or filtered by operation type,
	 * within a date range defined by the provided start and end month/year.
	 *
	 * @param startMonth the starting month (1–12) of the date range
	 * @param startYear the starting year of the date range
	 * @param endMonth the ending month (1–12) of the date range
	 * @param endYear the ending year of the date range
	 * @return a list of {@link TransactionDTO} objects matching the specified operation and date range
	 */
	List<TransactionDTO> getTransactionsByOperation(final Integer startMonth, final Integer startYear, final Integer endMonth, final Integer endYear);

	List<TransactionDTO> getTransactionsByOrganism(Integer month, Integer year);
	
	List<TransactionDTO> getTransactionsByOrganism(final Integer startMonth, final Integer startYear, final Integer endMonth, final Integer endYear);
}
