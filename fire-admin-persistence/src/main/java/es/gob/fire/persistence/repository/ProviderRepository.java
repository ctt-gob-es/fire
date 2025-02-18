package es.gob.fire.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.gob.fire.persistence.entity.Provider;

@Repository
public interface ProviderRepository extends JpaRepository<Provider, Long> {
	List<Provider> findAllByOrderByOrderIndexAsc();

	Optional<Provider> findByOrderIndex(int orderIndex);
}
