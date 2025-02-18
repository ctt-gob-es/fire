package es.gob.fire.persistence.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.gob.fire.persistence.dto.GeneralConfigDTO;
import es.gob.fire.persistence.dto.Property;
import es.gob.fire.persistence.repository.PropertyRepository;
import es.gob.fire.persistence.service.IPropertyService;

@Service
public class PropertyService implements IPropertyService {
	
	public static final String PROPERTY_NAME_MAX_SIZE_DOC = "TAMANO_MAXIMO_DOC";
	
	public static final String PROPERTY_NAME_MAX_SIZE_PETITION = "TAMANO_MAXIMO_PETICION";
	
	public static final String PROPERTY_NAME_MAX_AMOUNT_DOCS = "CANTIDAD_MAXIMA_DOCUMENTOS";
	
	public static final String PROPERTY_DATA_TYPE_NUMERIC = "NUMBER";
	
	public static final String PROPERTY_DATA_TYPE_DATE = "DATE";
	
	public static final String PROPERTY_DATA_TYPE_TEXT = "TEXT";

	@Autowired
	private PropertyRepository repository;

    @Override
    public List<Property> getAllProperties() {
        return repository.findAll();
    }

    @Override
    public Optional<Property> getPropertyByKey(String key) {
        return repository.findByKey(key);
    }

    @Override
    public Property saveProperty(Property property) {
        return repository.save(property);
    }

    @Override
    public void deleteProperty(String key) {
        repository.deleteById(key);
    }

	@Override
	public void saveGeneralConfig(GeneralConfigDTO request) {
		//Creamos la primera propiedad MAX_SIZE_DOC
		Property maxSizeDoc = new Property();
		maxSizeDoc.setKey(PROPERTY_NAME_MAX_SIZE_DOC);
		maxSizeDoc.setNumericValue(request.getMaxSizeDoc());
		maxSizeDoc.setType(PROPERTY_DATA_TYPE_NUMERIC);
		
		repository.save(maxSizeDoc);
		
		//Creamos la primera propiedad MAX_SIZE_PETITION
		Property maxSizePetition = new Property();
		maxSizePetition.setKey(PROPERTY_NAME_MAX_SIZE_PETITION);
		maxSizePetition.setNumericValue(request.getMaxSizePetition());
		maxSizePetition.setType(PROPERTY_DATA_TYPE_NUMERIC);
		
		repository.save(maxSizePetition);
		
		//Creamos la primera propiedad MAX_AMOUNT_DOCS
		Property maxAmountDocs = new Property();
		maxAmountDocs.setKey(PROPERTY_NAME_MAX_AMOUNT_DOCS);
		maxAmountDocs.setNumericValue(request.getMaxAmountDocs());
		maxAmountDocs.setType(PROPERTY_DATA_TYPE_NUMERIC);
		
		repository.save(maxAmountDocs);
	}
	
	@Override
	public List<Property> getGeneralConfigProperties() {
	    List<String> propertyKeys = Arrays.asList(
	        PROPERTY_NAME_MAX_SIZE_DOC,
	        PROPERTY_NAME_MAX_SIZE_PETITION,
	        PROPERTY_NAME_MAX_AMOUNT_DOCS
	    );

	    List<Property> response = new ArrayList<>();

	    for (String key : propertyKeys) {
	        repository.findById(key).ifPresent(response::add);
	    }

	    return response;
	}
}
