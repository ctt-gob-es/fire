package es.gob.fire.persistence.service;

import java.util.List;
import java.util.Optional;

import es.gob.fire.persistence.dto.GeneralConfigDTO;
import es.gob.fire.persistence.dto.Property;

public interface IPropertyService {
    List<Property> getAllProperties();
    
    Optional<Property> getPropertyByKey(String key);
    
    Property saveProperty(Property property);
    
    void deleteProperty(String key);

	void saveGeneralConfig(GeneralConfigDTO request);

	List<Property> getGeneralConfigProperties();
}