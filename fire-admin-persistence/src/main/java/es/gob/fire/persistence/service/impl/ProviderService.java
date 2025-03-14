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
		return this.repository.findAllByOrderByOrderIndexAsc();
	}

	@Override
	public Provider saveProvider(final Provider provider) {
		return this.repository.save(provider);
	}

	@Override
	public Provider findProviderById(final String idProvider) {
		final Optional<Provider> opt = this.repository.findById(idProvider);

		if (opt.isPresent()) {
			return opt.get();
		}
		return null;
	}

	// Subir el orden del proveedor
    public void moveProviderUp(final int providerOrder, final String idProvider) {
        final Optional<Provider> currentProviderOpt = this.repository.findById(idProvider);
        if (currentProviderOpt.isPresent()) {
            final Provider currentProvider = currentProviderOpt.get();
            final Optional<Provider> previousProviderOpt = this.repository.findByOrderIndex(providerOrder - 1);

            if (previousProviderOpt.isPresent()) {
                final Provider previousProvider = previousProviderOpt.get();

                // Intercambiar los valores de orden
                final Long tempOrder = currentProvider.getOrderIndex();
                currentProvider.setOrderIndex(previousProvider.getOrderIndex());
                previousProvider.setOrderIndex(tempOrder);

                // Guardar cambios
                this.repository.save(previousProvider);
                this.repository.save(currentProvider);
            }
        }
    }

    // Bajar el orden del proveedor
    public void moveProviderDown(final int providerOrder, final String idProvider) {
        final Optional<Provider> currentProviderOpt = this.repository.findById(idProvider);
        if (currentProviderOpt.isPresent()) {
            final Provider currentProvider = currentProviderOpt.get();
            final Optional<Provider> nextProviderOpt = this.repository.findByOrderIndex(providerOrder + 1);

            if (nextProviderOpt.isPresent()) {
                final Provider nextProvider = nextProviderOpt.get();

                // Intercambiar los valores de orden
                final Long tempOrder = currentProvider.getOrderIndex();
                currentProvider.setOrderIndex(nextProvider.getOrderIndex());
                nextProvider.setOrderIndex(tempOrder);

                // Guardar cambios
                this.repository.save(nextProvider);
                this.repository.save(currentProvider);
            }
        }
    }

    @Override
    public void saveProviders(final List<ProviderDTO> providers) {
    	if (providers == null || providers.isEmpty()) {
            return;
        }

        for (final ProviderDTO dto : providers) {
            final Provider provider = convertDTOToEntity(dto);
            this.repository.save(provider);
        }
    }

	@Override
    public Provider convertDTOToEntity(final ProviderDTO dto) {
        if (dto == null) {
			return null;
		}

        final Provider provider = new Provider();
        provider.setId(dto.getIdProvider());
        provider.setName(dto.getName());
        provider.setMandatory(dto.getMandatory());
        provider.setEnabled(dto.getEnabled());
        provider.setOrderIndex(dto.getOrderIndex());

        return provider;
    }

    @Override
    public ProviderDTO convertEntityToDTO(final Provider entity) {
        if (entity == null) {
			return null;
		}

        final ProviderDTO dto = new ProviderDTO();
        dto.setIdProvider(entity.getId());
        dto.setName(entity.getName());
        dto.setMandatory(entity.getMandatory());
        dto.setEnabled(entity.getEnabled());
        dto.setOrderIndex(entity.getOrderIndex());

        return dto;
    }
}
