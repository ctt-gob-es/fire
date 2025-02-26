package es.gob.fire.persistence.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import es.gob.fire.persistence.entity.Application;
import es.gob.fire.persistence.entity.ProviderApplication;

@Repository
public interface ProviderApplicationRepository extends JpaRepository<ProviderApplication, String> {
    
    List<ProviderApplication> findByApplicationOrderByOrderIndexAsc(Application application);
}

