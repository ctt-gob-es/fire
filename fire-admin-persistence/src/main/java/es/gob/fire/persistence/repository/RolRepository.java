package es.gob.fire.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import es.gob.fire.persistence.entity.Rol;

public interface RolRepository extends JpaRepository<Rol, Long> {
	
	/**
	  * Method that obtains from the persistence an user role identified by its primary key.
	 * @param rolId String that represents the primary key of the user in the persistence.
	 * @return Object that represents an user role from the persistence.
	 */
	Rol findByRolId(Long rolId);
	
	/**
	 * Method that obtains from the persistence an user role identified by its role name.
	 * @param rolName String that represents the userName used to log in.
	 * @return Object that represents an user role from the persistence.
	 */
	Rol findByRolName(String rolName);

}
