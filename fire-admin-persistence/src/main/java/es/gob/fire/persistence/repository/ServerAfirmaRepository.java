package es.gob.fire.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.gob.fire.persistence.entity.ServerAfirma;

@Repository
public interface ServerAfirmaRepository extends JpaRepository<ServerAfirma, Long>{
	
	/**
	 * Retrieves a {@link ServerAfirma} entity based on its unique identifier.
	 *
	 * @param idServerAfirma The unique identifier of the Afirma server configuration.
	 * @return The {@link ServerAfirma} entity associated with the given ID, or {@code null} if not found.
	 */
	ServerAfirma findByIdServerAfirma(Long idServerAfirma);

}
