package es.gob.fire.web.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import es.gob.fire.commons.log.Logger;
import es.gob.fire.commons.utils.Base64;
import es.gob.fire.commons.utils.NumberConstants;
import es.gob.fire.commons.utils.UtilsStringChar;
import es.gob.fire.persistence.dto.ApplicationCertDTO;
import es.gob.fire.persistence.dto.ApplicationDTO;
import es.gob.fire.persistence.entity.Application;
import es.gob.fire.persistence.entity.ApplicationResponsible;
import es.gob.fire.persistence.entity.Certificate;
import es.gob.fire.persistence.entity.User;
import es.gob.fire.persistence.service.IApplicationService;
import es.gob.fire.persistence.service.ICertificateService;
import es.gob.fire.persistence.service.IUserService;


/**
 * <p>
 * Class that manages the requests related to the Users administration.
 * </p>
 * <b>Project:</b>
 * <p>
 * Application for monitoring services of @firma suite systems.
 * </p>
 *
 * @version 1.2, 28/10/2020.
 */


@Controller
public class ApplicationController {

	/**
	 * Constant that represents the parameter 'appId'.
	 */
	private static final String FIELD_ID_APPLICATION = "appId"; //$NON-NLS-1$


	/**
	 * Constant that represents the parameter log.
	 */
	private static final Logger LOGGER = Logger.getLogger(ApplicationController.class);


	/**
	 * Attribute that represents the service object for accessing the
	 * repository.
	 */
	@Autowired
	private IApplicationService applicationService;

	/**
	 * Attribute that represents the service object for accessing the
	 * repository.
	 */
	@Autowired
	private ICertificateService certificateService;

	/**
	 * Attribute that represents the service object for accessing the
	 * repository.
	 */
	@Autowired
	private IUserService userService;


	/**
	 * Method that maps the list applications web requests to the controller and
	 * forwards the list of users to the view.
	 *
	 * @param model
	 *            Holder object for model attributes.
	 * @return String that represents the name of the view to forward.
	 */
	@RequestMapping(value = "applicationadmin")
	public String index(final Model model) {

		return "fragments/applicationadmin.html"; //$NON-NLS-1$
	}

	/**
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "addapp", method = RequestMethod.POST)
	public String addApp(final Model model) {

		final ApplicationDTO appDto = new ApplicationDTO();

		final List<Certificate> certificates = this.certificateService.getAllCertificate();

		final List<User> selectedUsers = new ArrayList<>();
		final List<User> availableUsers = StreamSupport.stream(this.userService.getAllUser().spliterator(), false).collect(Collectors.toList());


		String userName;
		for (final User userApp : availableUsers) {

			userName = userApp.getName().concat(UtilsStringChar.SPECIAL_BLANK_SPACE_STRING).concat(userApp.getSurnames());
			userApp.setUserName(userName.concat(UtilsStringChar.SPECIAL_BLANK_SPACE_STRING)
					.concat(UtilsStringChar.SYMBOL_OPEN_BRACKET_STRING)
					.concat(userApp.getUserName().concat(UtilsStringChar.SYMBOL_CLOSE_BRACKET_STRING)));

		}

		model.addAttribute("selectedUsers", selectedUsers); //$NON-NLS-1$
		model.addAttribute("availableUsers", availableUsers); //$NON-NLS-1$
		model.addAttribute("certificados", certificates); //$NON-NLS-1$

		String certPrincipal = ""; //$NON-NLS-1$
		String certBackup = ""; //$NON-NLS-1$
		if (certificates.size() > 0) {

			final Certificate cert = certificates.get(NumberConstants.NUM0);
			certPrincipal = this.certificateService.getCertificateText(cert.getCertPrincipal());
			certBackup = this.certificateService.getCertificateText(cert.getCertBackup());
		}

		model.addAttribute("certPrincipal", certPrincipal); //$NON-NLS-1$
		model.addAttribute("certBackup", certBackup); //$NON-NLS-1$
		model.addAttribute("appAddForm", appDto); //$NON-NLS-1$

		return "modal/applicationAddForm.html"; //$NON-NLS-1$

	}

	/**
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "appedit", method = RequestMethod.POST)
	public String editApp(@RequestParam(FIELD_ID_APPLICATION) final String appId, final Model model) {

		final Application app = this.applicationService.getAppByAppId(appId);

		final ApplicationDTO appDto = this.applicationService.applicationEntityToDto(app);

		final List<Certificate> certificates = this.certificateService.getAllCertificate();

		final List<User> selectedUsers = new ArrayList<>();

		final List<ApplicationResponsible> appRespList = this.applicationService.getApplicationResponsibleByApprId(appId);

		for (final ApplicationResponsible appResp : appRespList) {

			selectedUsers.add(appResp.getResponsible());
		}

		final List<User> availableUsers = StreamSupport.stream(this.userService.getAllUser().spliterator(), false).collect(Collectors.toList());

		String userName;
		for (final User userApp : availableUsers) {

			userName = userApp.getName().concat(UtilsStringChar.SPECIAL_BLANK_SPACE_STRING).concat(userApp.getSurnames());
			userApp.setUserName(userName.concat(UtilsStringChar.SPECIAL_BLANK_SPACE_STRING)
					.concat(UtilsStringChar.SYMBOL_OPEN_BRACKET_STRING)
					.concat(userApp.getUserName().concat(UtilsStringChar.SYMBOL_CLOSE_BRACKET_STRING)));
		}

		model.addAttribute("selectedUsers", selectedUsers); //$NON-NLS-1$
		model.addAttribute("availableUsers", availableUsers); //$NON-NLS-1$
		model.addAttribute("certificados", certificates); //$NON-NLS-1$

		String certPrincipal = ""; //$NON-NLS-1$
		String certBackup = ""; //$NON-NLS-1$

		if (certificates.size() > 0) {

			final Certificate cert = app.getCertificate();
			certPrincipal = this.certificateService.getCertificateText(cert.getCertPrincipal());
			certBackup = this.certificateService.getCertificateText(cert.getCertBackup());
		}

		model.addAttribute("certPrincipal", certPrincipal); //$NON-NLS-1$
		model.addAttribute("certBackup", certBackup); //$NON-NLS-1$
		model.addAttribute("appEditForm", appDto); //$NON-NLS-1$

		return "modal/applicationEditForm.html"; //$NON-NLS-1$

	}

	/**
	 *  Method that loads the necessary information to show the confirmation modal to remove a selected application.
	 * @param idApplication Parameter that represetns ID of application.
	 * @param model Holder object for model attributes.
	 * @return String that represents the name of the view to forward.
	 */
	@RequestMapping(value = "/loadconfirmdeleteapplication", method = RequestMethod.GET)
	public String deleteConfirmApplication(@RequestParam(FIELD_ID_APPLICATION) final String appId, final Model model) {
		//Metemos los datos en el dto
		final ApplicationDTO applicationDto = new ApplicationDTO();
		applicationDto.setAppId(appId);

		model.addAttribute("applicationForm", applicationDto); //$NON-NLS-1$
		return "modal/applicationDelete.html"; //$NON-NLS-1$
	}

	/**
	 * Method that maps the request for opening the view application modal
	 * @param idCertificado Long that represents the certificate identifier
	 * @param model view Model object
	 * @return String that represents the navigation HTML modal
	 */
	@RequestMapping(value = "/viewapplication", method = RequestMethod.POST)
	public String appView(@RequestParam("appId") final String appId, final Model model) {
		final ApplicationCertDTO appViewForm = this.applicationService.getViewApplication(appId);

		final Certificate cert = this.certificateService.getCertificateByCertificateId(appViewForm.getIdCertificate());
		appViewForm.setCertPrincipalB64(cert.getCertPrincipal());
		appViewForm.setCertBackupB64(cert.getCertBackup());
		String certData = ""; //$NON-NLS-1$

		if (cert.getCertPrincipal() != null && !cert.getCertPrincipal().isEmpty()) {
			try (final InputStream certIs = new ByteArrayInputStream(Base64.decode(cert.getCertPrincipal()));) {

				certData = this.certificateService.getFormatCertText(certIs);
				appViewForm.setCertPrincipal(certData);

			} catch (final IOException e) {
				LOGGER.error("No se ha podido cargar el certificado principal de la aplicacion", e); //$NON-NLS-1$
			} catch (final CertificateException e) {
				LOGGER.error("No se ha podido componer el certificado principal de la aplicacion", e); //$NON-NLS-1$
			}
		}

		if (cert.getCertBackup() != null && !cert.getCertBackup().isEmpty()) {
			try (final InputStream certIs = new ByteArrayInputStream(Base64.decode(cert.getCertBackup()));) {

				certData = this.certificateService.getFormatCertText(certIs);
				appViewForm.setCertBackup(certData);

			} catch (final IOException e) {
				LOGGER.error("No se ha podido cargar el certificado secundario de la aplicacion", e); //$NON-NLS-1$
			} catch (final CertificateException e) {
				LOGGER.error("No se ha podido componer el certificado secundario de la aplicacion", e); //$NON-NLS-1$
			}
		}

		model.addAttribute("certBackup", appViewForm.getCertBackup()); //$NON-NLS-1$
		model.addAttribute("certPrincipal", appViewForm.getCertPrincipal()); //$NON-NLS-1$
		model.addAttribute("appViewForm", appViewForm); //$NON-NLS-1$
		return "modal/applicationViewForm.html"; //$NON-NLS-1$
	}
}
