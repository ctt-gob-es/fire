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

	public static final String PROPERTY_NAME_MAX_ENTITIES_BEFORE_GROUPING = "CANTIDAD_MAXIMA_ENTIDADES";

	public static final String PROPERTY_DATA_TYPE_NUMERIC = "NUMBER";

	public static final String PROPERTY_DATA_TYPE_DATE = "DATE";

	public static final String PROPERTY_DATA_TYPE_TEXT = "TEXT";

	@Autowired
	private PropertyRepository repository;

    @Override
    public List<Property> getAllProperties() {

    	final List<Property> properties = this.repository.findAll();
    	for (final Property p : properties) {
    		if (p.getKey().equals(PROPERTY_NAME_MAX_SIZE_DOC) || p.getKey().equals(PROPERTY_NAME_MAX_SIZE_PETITION)) {
    			p.setNumericValue(convertBytesToMegabytes(p.getNumericValue()));
    		}
    	}
    	return properties;
    }

    @Override
    public Optional<Property> getPropertyByKey(final String key) {

    	final Optional<Property> property = this.repository.findByKey(key);
    	if (property.isPresent()) {
    		final Property prop = property.get();
    		if (prop.getKey().equals(PROPERTY_NAME_MAX_SIZE_DOC) || prop.getKey().equals(PROPERTY_NAME_MAX_SIZE_PETITION)) {
    			prop.setNumericValue(convertBytesToMegabytes(property.get().getNumericValue()));
    		}
    	}
        return property;
    }

    @Override
    public Property saveProperty(final Property property) {

    	if (property.getKey().equals(PROPERTY_NAME_MAX_SIZE_DOC) || property.getKey().equals(PROPERTY_NAME_MAX_SIZE_PETITION)) {
    		property.setNumericValue(convertMegabytesToBytes(property.getNumericValue()));
		}
        return this.repository.save(property);
    }

    @Override
    public void deleteProperty(final String key) {
        this.repository.deleteById(key);
    }

	@Override
	public void saveGeneralConfig(final GeneralConfigDTO request) {
		//Creamos la primera propiedad MAX_SIZE_DOC
		final Property maxSizeDoc = new Property();
		maxSizeDoc.setKey(PROPERTY_NAME_MAX_SIZE_DOC);
		maxSizeDoc.setNumericValue(convertMegabytesToBytes(request.getMaxSizeDoc()));
		maxSizeDoc.setType(PROPERTY_DATA_TYPE_NUMERIC);

		this.repository.save(maxSizeDoc);

		//Creamos la primera propiedad MAX_SIZE_PETITION
		final Property maxSizePetition = new Property();
		maxSizePetition.setKey(PROPERTY_NAME_MAX_SIZE_PETITION);
		maxSizePetition.setNumericValue(convertMegabytesToBytes(request.getMaxSizePetition()));
		maxSizePetition.setType(PROPERTY_DATA_TYPE_NUMERIC);

		this.repository.save(maxSizePetition);

		//Creamos la primera propiedad MAX_AMOUNT_DOCS
		final Property maxAmountDocs = new Property();
		maxAmountDocs.setKey(PROPERTY_NAME_MAX_AMOUNT_DOCS);
		maxAmountDocs.setNumericValue(request.getMaxAmountDocs());
		maxAmountDocs.setType(PROPERTY_DATA_TYPE_NUMERIC);

		this.repository.save(maxAmountDocs);

		//Creamos la primera propiedad MAX_ENTITIES_BEFORE_GROUPING
		final Property maxEntitiesBeforeGrouping = new Property();
		maxEntitiesBeforeGrouping.setKey(PROPERTY_NAME_MAX_ENTITIES_BEFORE_GROUPING);
		maxEntitiesBeforeGrouping.setNumericValue(request.getMaxEntitiesBeforeGrouping());
		maxEntitiesBeforeGrouping.setType(PROPERTY_DATA_TYPE_NUMERIC);

		this.repository.save(maxEntitiesBeforeGrouping);
	}

	@Override
	public List<Property> getGeneralConfigProperties() {
	    final List<String> propertyKeys = Arrays.asList(
	        PROPERTY_NAME_MAX_SIZE_DOC,
	        PROPERTY_NAME_MAX_SIZE_PETITION,
	        PROPERTY_NAME_MAX_AMOUNT_DOCS,
	        PROPERTY_NAME_MAX_ENTITIES_BEFORE_GROUPING
	    );

	    final List<Property> response = new ArrayList<>();

	    for (final String key : propertyKeys) {
	    	final Optional<Property> property = this.repository.findById(key);
	        if (property.isPresent()) {
	        	final Property prop = property.get();
	        	if (PROPERTY_NAME_MAX_SIZE_DOC.equals(prop.getKey()) || PROPERTY_NAME_MAX_SIZE_PETITION.equals(prop.getKey())) {
					prop.setNumericValue(convertBytesToMegabytes(prop.getNumericValue()));
				}
	        	response.add(prop);
	        }
	    }

	    return response;
	}


	private static Long convertBytesToMegabytes(final Long bytes) {
		return Long.valueOf(bytes == null ? 0 : bytes.longValue() / (1024 * 1024));
	}

	private static Long convertMegabytesToBytes(final Long mb) {
		return Long.valueOf(mb == null ? 0 : mb.longValue() * 1024 * 1024);
	}
}
