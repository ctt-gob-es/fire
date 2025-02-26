package es.gob.fire.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.gob.fire.persistence.dto.Property;

@Repository
public interface PropertyRepository extends JpaRepository<Property, String> {
    Optional<Property> findByKey(String key);
}