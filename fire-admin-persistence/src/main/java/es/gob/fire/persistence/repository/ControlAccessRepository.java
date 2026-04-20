package es.gob.fire.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.gob.fire.persistence.entity.ControlAccess;

@Repository
public interface ControlAccessRepository extends JpaRepository<ControlAccess, Long>{

}
