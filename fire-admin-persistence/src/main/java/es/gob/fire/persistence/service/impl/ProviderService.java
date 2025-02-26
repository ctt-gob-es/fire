package es.gob.fire.persistence.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.gob.fire.persistence.dto.ProviderDTO;
import es.gob.fire.persistence.entity.Provider;
import es.gob.fire.persistence.repository.ProviderApplicationRepository;
import es.gob.fire.persistence.repository.ProviderRepository;
import es.gob.fire.persistence.service.IProviderService;

@Service
public class ProviderService implements IProviderService{
	
	/**
	 * Attribute that represents the injected interface that provides CRUD operations for the persistence.
	 */
	@Autowired
	private ProviderRepository repository;
	
	/**
	 * Attribute that represents the injected interface that provides CRUD operations for the persistence.
	 */
	@Autowired
	private ProviderApplicationRepository providerApplicationRepository;

	@Override
	public List<Provider> findProviders() {
		return repository.findAllByOrderByOrderIndexAsc();
	}

	@Override
	public Provider saveProvider(Provider provider) {
		return repository.save(provider);
	}

	@Override
	public Provider findProviderById(Long idProvider) {
		Optional<Provider> opt = repository.findById(idProvider);
		
		if (opt.isPresent()) {
			return opt.get();
		} else {
			return null;
		}
	}

	// Subir el orden del proveedor
    public void moveProviderUp(int providerOrder, Long idProvider) {
        Optional<Provider> currentProviderOpt = repository.findById(idProvider);
        if (currentProviderOpt.isPresent()) {
            Provider currentProvider = currentProviderOpt.get();
            Optional<Provider> previousProviderOpt = repository.findByOrderIndex(providerOrder - 1);
            
            if (previousProviderOpt.isPresent()) {
                Provider previousProvider = previousProviderOpt.get();

                // Intercambiar los valores de orden
                Long tempOrder = currentProvider.getOrderIndex();
                currentProvider.setOrderIndex(previousProvider.getOrderIndex());
                previousProvider.setOrderIndex(tempOrder);

                // Guardar cambios
                repository.save(previousProvider);
                repository.save(currentProvider);
            }
        }
    }

    // Bajar el orden del proveedor
    public void moveProviderDown(int providerOrder, Long idProvider) {
        Optional<Provider> currentProviderOpt = repository.findById(idProvider);
        if (currentProviderOpt.isPresent()) {
            Provider currentProvider = currentProviderOpt.get();
            Optional<Provider> nextProviderOpt = repository.findByOrderIndex(providerOrder + 1);
            
            if (nextProviderOpt.isPresent()) {
                Provider nextProvider = nextProviderOpt.get();

                // Intercambiar los valores de orden
                Long tempOrder = currentProvider.getOrderIndex();
                currentProvider.setOrderIndex(nextProvider.getOrderIndex());
                nextProvider.setOrderIndex(tempOrder);

                // Guardar cambios
                repository.save(nextProvider);
                repository.save(currentProvider);
            }
        }
    }

    @Override
    public void saveProviders(List<ProviderDTO> providers) {
    	if (providers == null || providers.isEmpty()) {
            return;
        }

        for (ProviderDTO dto : providers) {
            Provider provider = convertDTOToEntity(dto);
            repository.save(provider);
        }
    }

	@Override
    public Provider convertDTOToEntity(ProviderDTO dto) {
        if (dto == null) return null;

        Provider provider = new Provider();
        provider.setId(dto.getIdProvider());
        provider.setName(dto.getName());
        provider.setMandatory(dto.getMandatory());
        provider.setEnabled(dto.getEnabled());
        provider.setOrderIndex(dto.getOrderIndex());

        return provider;
    }

    @Override
    public ProviderDTO convertEntityToDTO(Provider entity) {
        if (entity == null) return null;

        ProviderDTO dto = new ProviderDTO();
        dto.setIdProvider(entity.getId());
        dto.setName(entity.getName());
        dto.setMandatory(entity.getMandatory());
        dto.setEnabled(entity.getEnabled());
        dto.setOrderIndex(entity.getOrderIndex());

        return dto;
    }
}
