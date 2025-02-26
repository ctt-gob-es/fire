package es.gob.fire.persistence.service;

import java.util.List;

import es.gob.fire.persistence.dto.ProviderDTO;
import es.gob.fire.persistence.entity.Provider;

public interface IProviderService {
	
	/**
	 * Method that obtains a provider by its identifier.
	 * @param idProvider The provider identifier.
	 * @return The provider.
	 */
	Provider findProviderById(Long idProvider);
	
	/**
	 * Method that returns all the providers from the persistence.
     * @return a {@link List<Provider>} with the information of all providers.
	 */
	List<Provider> findProviders();
	
	/**
	 * Method that stores a provider in the persistence.
	 * @param provider a {@link Provider} entity with the information of the provider.
	 * @return The provider.
	 */
	Provider saveProvider(Provider provider);
	
	/**
	 * Method that changes the order up of a provider in the persistence.
	 * @param providerOrder
	 * @param idProvider
	 */
	void moveProviderUp(int providerOrder, Long idProvider);
	
	/**
	 * Method that changes the order down of a provider in the persistence.
	 * @param providerOrder
	 * @param idProvider
	 */
	void moveProviderDown(int providerOrder, Long idProvider);

	/**
	 * Method that stores a list of providers into the persistence.
	 * @param providers a list {@link List} of providers {@link ProviderDTO}
	 */
	void saveProviders(List<ProviderDTO> providers);
	
	/**
	 * Method that transforms a DTO object into a provider entity.
	 * @param dto {@link ProviderDTO} with the info required
	 * @return a {@link Provider} entity equivalent to the info from the DTO
	 */
	Provider convertDTOToEntity(ProviderDTO dto);
	
	/**
	 * Method that transforms an entity object into a DTO object.
	 * @param dto {@link Provider} with the info required
	 * @return a {@link ProviderDTO} DTO equivalent to the info from the entity
	 */
	ProviderDTO convertEntityToDTO(Provider entity);
}
