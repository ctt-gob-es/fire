package es.gob.fire.persistence.service;

import java.security.GeneralSecurityException;
import java.util.List;

import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import es.gob.fire.persistence.dto.ApplicationCertDTO;
import es.gob.fire.persistence.dto.ApplicationDTO;
import es.gob.fire.persistence.entity.Application;
import es.gob.fire.persistence.entity.ApplicationResponsible;

/**
 * <p>Interface that provides communication with the operations of the persistence layer.</p>
 * <b>Project:</b><p>Platform for detection and validation of certificates recognized in European TSL.</p>
 * @version 1.0, 15/10/2020.
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
	 * @return The application.
	 */
	Application saveApplication(ApplicationDTO app, List<Long> idsUsers) throws GeneralSecurityException;

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
}
