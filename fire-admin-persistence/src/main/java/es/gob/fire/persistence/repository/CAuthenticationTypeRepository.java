package es.gob.fire.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import es.gob.fire.persistence.entity.CAuthenticationType;

public interface CAuthenticationTypeRepository extends JpaRepository<CAuthenticationType, Long>{

}
