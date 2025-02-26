package es.gob.fire.persistence.service;

import java.security.GeneralSecurityException;
import java.util.List;

import javax.validation.constraints.NotEmpty;

import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import es.gob.fire.persistence.dto.ApplicationCertDTO;
import es.gob.fire.persistence.dto.ApplicationDTO;
import es.gob.fire.persistence.dto.ProviderApplicationDTO;
import es.gob.fire.persistence.dto.ProviderDTO;
import es.gob.fire.persistence.entity.Application;
import es.gob.fire.persistence.entity.ApplicationResponsible;
import es.gob.fire.persistence.entity.CertificatesApplication;
import es.gob.fire.persistence.entity.Provider;
import es.gob.fire.persistence.entity.ProviderApplication;

/**
 * <p>Interface that provides communication with the operations of the persistence layer.</p>
 * <b>Project:</b><p>Platform for detection and validation of certificates recognized in European TSL.</p>
 * @version 1.1, 28/01/2025.
 */
public interface IApplicationService {

	/**
	 * Method that obtains an application by its identifier.
	 * @param appId The user identifier.
	 * @return The application.
	 */
	Application getAppByAppId(String appId);

	/**
	 * Method that obtains an user by its app name.
	 * @param appName The app name.
	 * @return The application.
	 */
	Application getAppByAppName(String appName);


	/**
	 * Method that stores a application in the persistence.
	 * @param app a {@link ApplicationDTO} with the information of the application.
	 * @param listIdCertificates 
	 * @param customProviders 
	 * @return The application.
	 */
	Application saveApplication(ApplicationDTO app, List<Long> idsUsers, List<Long> listIdCertificates, List<ProviderApplicationDTO> customProviders) throws GeneralSecurityException;

	/**
	 * Method that deletes a application in the persistence.
	 * @param appId {@link Integer} that represents the application identifier to delete.
	 */
	void deleteApplication(String appId);

	/**
	 * Method that gets all the app from the persistence.
     * @return a {@link Iterable<Application>} with the information of all application.
	 */
	Iterable<Application> getAllApplication();

	/**
	 * Method that gets the list for the given {@link DataTablesInput}.
	 * @param input the {@link DataTablesInput} mapped from the Ajax request.
	 * @return {@link DataTablesOutput}
	 */
	DataTablesOutput<Application> getAllApplication(DataTablesInput input);

	/**
	 * Method that maos the values of ApplicationDTO to Certificate.
	 * @param applicationDto Object that represents the values of a certificate taken from the view.
	 * @return Object thtat represetns a Application entity with the values of the ApplicationDTO.
	 */
	Application applicationDtoToEntity(ApplicationDTO applicationDto);

	/**
	 * Method that maos the values of ApplicationDTO to Application.
	 * @param certificat Object that represents the mapping entity for Application.
	 * @return Object that represents a ApplicationDTO.
	 */
	ApplicationDTO applicationEntityToDto(Application application);

	/**
	 * Method that gets the list of applications-users relations by userId.
	 * @param userId Long that represents the User identifier
	 * @return List<ApplicationResponsible>
	 */
	List<ApplicationResponsible> getApplicationResponsibleByUserId(Long userId);

	/**
	 * Method that gets the list of applications-users relations by appId.
	 * @param appId String that represents the Application identifier.
	 * @return List<ApplicationResponsible>
	 */
	List<ApplicationResponsible> getApplicationResponsibleByApprId(String appId);

	/**
	 * Method that gets the applications associated to the Certificate identified by idCertificate.
	 * @param input
	 * @param idCertificate
	 * @return
	 */
	DataTablesOutput<ApplicationCertDTO> getApplicationsCert(DataTablesInput input, Long idCertificate);

	/**
	 * Method that gets the application information to be visualized.
	 * @param appId String that represents the Application identifier
	 * @return ApplicationCertDTO
	 */
	ApplicationCertDTO getViewApplication(String appId);

	/**
	 * Method that obtains from the persistence a List of Application
	 * @param idCertificado Long that represents the Certificate identifier.
	 * @return Object that represents a user from the persistence.
	 */
	List<Application> getByIdCertificado(Long idCertificado);

	/**
	 * Retrieves a list of CertificatesApplication entities associated with the specified application ID.
	 *
	 * @param appId the unique identifier of the application whose certificates are to be retrieved.
	 * @return a list of {@link CertificatesApplication} entities linked to the given application ID.
	 *         If no entities are found, an empty list is returned.
	 */
	List<CertificatesApplication> getCertificatesApplicationByAppId(String appId);

	/**
	 * Generates a ZIP file containing certificates and converts it to a Base64 string.
	 * 
	 * <p>This method takes a list of certificates, creates individual `.cer` files for each 
	 * certificate, compresses them into a ZIP file, and encodes the ZIP file into a Base64 string.
	 * The Base64 string is then set in the provided {@code ApplicationCertDTO} object.</p>
	 *
	 * @param appViewForm the {@code ApplicationCertDTO} object where the Base64-encoded ZIP will be set.
	 * @param listCertificatesApplication the list of {@code CertificatesApplication} containing the certificates to be processed.
	 *
	 * @throws IOException if an I/O error occurs during file or ZIP operations.
	 * @throws IllegalArgumentException if there is an error decoding Base64 or processing certificates.
	 * @throws NullPointerException if a required object is null.
	 */
	void obtainZipWithCertificatesApp(ApplicationCertDTO appViewForm, List<CertificatesApplication> listCertificatesApplication);

	/**
	 * Retrieves a paginated and sorted list of applications associated with a specific user, 
	 * formatted for use with DataTables.
	 *
	 * @param input  The {@link DataTablesInput} containing pagination, sorting, and filtering criteria.
	 * @param userId The ID of the user whose applications are to be retrieved.
	 * @return A {@link DataTablesOutput} containing the list of {@link ApplicationCertDTO} objects.
	 * @throws IllegalArgumentException if {@code input} is empty.
	 */
	DataTablesOutput<ApplicationCertDTO> getApplicationsUser(@NotEmpty DataTablesInput input, Long userId);
	
	/**
	 * Method that stores an application in the persistence.
	 * @param app a {@link Application} with the information of the application.
	 * @return The application.
	 */
	Application saveApplication(Application application);
	
	/**
	 * Retrieves a list of ProviderApplication entities associated with the specified application.
	 *
	 * @param application the application whose providers are to be retrieved.
	 * @return a list of {@link ProviderApplicationDTO} entities linked to the given application. If no entities are found, an empty list is returned.
	 */
	List<ProviderApplicationDTO> findProvidersByApplication(Application application);
	
	/**
	 * Method that transforms a DTO object into a provider entity.
	 * @param dto {@link ProviderApplicationDTO} with the info required
	 * @return a {@link ProviderApplication} entity equivalent to the info from the DTO
	 */
	ProviderApplication convertDTOToEntity(ProviderApplicationDTO dto);
	
	/**
	 * Method that transforms an entity object into a DTO object.
	 * @param entity {@link ProviderApplication} with the info required
	 * @return a {@link ProviderApplicationDTO} DTO equivalent to the info from the entity
	 */
	ProviderApplicationDTO convertEntityToDTO(ProviderApplication entity);
	
	/**
	 * Method that transforms an entity object into a DTO object.
	 * @param entity {@link Provider} with the info required
	 * @param application {@link Application} with the info required
	 * @return a {@link ProviderApplicationDTO} DTO equivalent to the info from the entity
	 */
	ProviderApplicationDTO convertProviderEntityToProviderApplicationDTO(Provider entity, Application application);

	ProviderApplication convertProviderApplicationDTOToEntity(ProviderApplicationDTO dto, Application application);
}
