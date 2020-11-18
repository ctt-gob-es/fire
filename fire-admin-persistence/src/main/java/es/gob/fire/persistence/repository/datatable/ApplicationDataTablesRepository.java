package es.gob.fire.persistence.repository.datatable;

import org.springframework.data.jpa.datatables.repository.DataTablesRepository;

import es.gob.fire.persistence.entity.Application;


/** 
 * <p>Interface that provides CRUD functionality for the Application entity and DataTables.</p>
 * <b>Project:</b><p>Platform for detection and validation of certificates recognized in European TSL.</p>
 * @version 1.0, 15 oct. 2020.
 */

public interface ApplicationDataTablesRepository extends DataTablesRepository<Application, Long>{

}
