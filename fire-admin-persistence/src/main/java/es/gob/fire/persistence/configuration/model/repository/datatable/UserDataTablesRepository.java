package es.gob.fire.persistence.configuration.model.repository.datatable;

 
	import org.springframework.data.jpa.datatables.repository.DataTablesRepository;

	import es.gob.fire.persistence.configuration.model.entity.User;

	/** 
	 * <p>Interface that provides CRUD functionality for the UserValet entity and DataTables.</p>
	 * <b>Project:</b><p>Platform for detection and validation of certificates recognized in European TSL.</p>
	 * @version 1.0, 19 jun. 2018.
	 */
	public interface UserDataTablesRepository extends DataTablesRepository<User, Long> {
	

}
