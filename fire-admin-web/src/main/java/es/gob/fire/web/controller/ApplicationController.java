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

import es.gob.fire.commons.utils.Base64;
import es.gob.fire.commons.utils.NumberConstants;
import es.gob.fire.commons.utils.UtilsStringChar;
import es.gob.fire.persistence.dto.ApplicationDTO;
import es.gob.fire.persistence.dto.CertificateDTO;
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
	private static final String FIELD_ID_APPLICATION = "appId";
	
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
		
		return "fragments/applicationadmin.html";
	}
	
	/**
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "addapp", method = RequestMethod.POST)
	public String addApp(final Model model) {
		
		ApplicationDTO appDto = new ApplicationDTO();
		
		List<Certificate> certificates = certificateService.getAllCertificate();
					
		List<User> selectedUsers = new ArrayList<User>();
		List<User> availableUsers = StreamSupport.stream(userService.getAllUser().spliterator(), false).collect(Collectors.toList());
		
		String userName;
		for (User userApp : availableUsers) {

			userName = userApp.getName().concat(UtilsStringChar.SPECIAL_BLANK_SPACE_STRING).concat(userApp.getSurnames());
			userApp.setUserName(userName.concat(UtilsStringChar.SPECIAL_BLANK_SPACE_STRING)
					.concat(UtilsStringChar.SYMBOL_OPEN_BRACKET_STRING)
					.concat(userApp.getUserName().concat(UtilsStringChar.SYMBOL_CLOSE_BRACKET_STRING)));

		}
		
		model.addAttribute("selectedUsers", selectedUsers);
		model.addAttribute("availableUsers", availableUsers);
		model.addAttribute("certificados", certificates);
		
		String certPrincipal = "";
		String certBackup = "";
		if (certificates.size() > 0) {
			
			Certificate cert = certificates.get(NumberConstants.NUM0);
			certPrincipal = certificateService.getCertificateText(cert.getCertPrincipal());
			certBackup = certificateService.getCertificateText(cert.getCertBackup());
		}
				
		model.addAttribute("certPrincipal", certPrincipal);
		model.addAttribute("certBackup", certBackup);
		model.addAttribute("appAddForm", appDto);
		
		return "modal/applicationAddForm.html";
		
	}
	
	/**
	 *  Method that loads the necessary information to show the confirmation modal to remove a selected application.
	 * @param idApplication Parameter that represetns ID of application.
	 * @param model Holder object for model attributes.
	 * @return String that represents the name of the view to forward.
	 */
	@RequestMapping(value = "/loadconfirmdeleteapplication", method = RequestMethod.GET)
	public String deleteConfirmApplication(@RequestParam(FIELD_ID_APPLICATION) String appId, Model model) {
		//Metemos los datos en el dto
		ApplicationDTO applicationDto = new ApplicationDTO();
		applicationDto.setAppId(appId);
				
		model.addAttribute("applicationForm", applicationDto);
		return "modal/applicationDelete.html";
	}
}
