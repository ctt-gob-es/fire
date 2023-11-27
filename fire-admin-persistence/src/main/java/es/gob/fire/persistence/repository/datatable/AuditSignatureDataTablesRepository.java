package es.gob.fire.persistence.repository.datatable;

import org.springframework.data.jpa.datatables.repository.DataTablesRepository;

import es.gob.fire.persistence.entity.AuditSignature;

/**
 * <p>Interface that provides CRUD functionality for the PetitionBatchSignature entity and DataTables.</p>
 * <b>Project:</b><p>Platform for detection and validation of certificates recognized in European TSL.</p>
 * @version 1.0, 06 jul. 2023.
 */
public interface AuditSignatureDataTablesRepository extends DataTablesRepository<AuditSignature, String>{
	//Clase vacia
}
