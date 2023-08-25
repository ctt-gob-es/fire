package es.gob.fire.persistence.repository.datatable;

import org.springframework.data.jpa.datatables.repository.DataTablesRepository;

import es.gob.fire.persistence.entity.AuditTransaction;

/**
 * <p>Interface that provides CRUD functionality for the Petition entity and DataTables.</p>
 * <b>Project:</b><p>Platform for detection and validation of certificates recognized in European TSL.</p>
 * @version 1.0, 03 jul. 2023.
 */

public interface AuditTransactionDataTablesRepository extends DataTablesRepository<AuditTransaction, String>{
	//Clase vacia
}
