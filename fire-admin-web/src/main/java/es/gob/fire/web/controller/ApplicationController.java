package es.gob.fire.web.controller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import es.gob.fire.commons.log.Logger;
import es.gob.fire.commons.utils.UtilsStringChar;
import es.gob.fire.persistence.dto.ApplicationCertDTO;
import es.gob.fire.persistence.dto.ApplicationDTO;
import es.gob.fire.persistence.entity.Application;
import es.gob.fire.persistence.entity.ApplicationResponsible;
import es.gob.fire.persistence.entity.Certificate;
import es.gob.fire.persistence.entity.CertificatesApplication;
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
 * @version 1.3, 27/01/2025.
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

		final List<Certificate> availableCertficates = this.certificateService.getAllCertificate();
		final List<Certificate> selectedCertificates = new ArrayList<>();
		
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
		model.addAttribute("availableCertficates", availableCertficates);
		model.addAttribute("selectedCertificates", selectedCertificates);
		
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
		
		final List<Certificate> selectedCertificates = new ArrayList<>();
		
		final List<CertificatesApplication> listCertificatesApplication = this.applicationService.getCertificatesApplicationByAppId(appId);
		
		for (CertificatesApplication certificatesApplication : listCertificatesApplication) {
			selectedCertificates.add(certificatesApplication.getCertificate());
		}
		
		final List<Certificate> availableCertficates = this.certificateService.getAllCertificate();
		
		availableCertficates.removeAll(selectedCertificates);
		
		model.addAttribute("selectedUsers", selectedUsers); //$NON-NLS-1$
		model.addAttribute("availableUsers", availableUsers); //$NON-NLS-1$
		model.addAttribute("availableCertficates", availableCertficates);
		model.addAttribute("selectedCertificates", selectedCertificates);
		
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
		
		final List<CertificatesApplication> listCertificatesApplication = this.applicationService.getCertificatesApplicationByAppId(appId);

		try {
		    // Directorio temporal para guardar los certificados
		    final File tempDir = Files.createTempDirectory("certificates").toFile();
		    final File zipFile = new File(tempDir, "certificados.zip"); // Archivo ZIP temporal

		    // Crear archivo ZIP y procesar los certificados
		    try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile))) {
		        for (CertificatesApplication certificatesApplication : listCertificatesApplication) {
		            // Crear InputStream desde el certificado Base64
		            final InputStream certIs = new ByteArrayInputStream(
		                    Base64.getDecoder().decode(certificatesApplication.getCertificate().getCertificate()));
		            String certFileName = certificatesApplication.getCertificate().getCertificateName() + ".cer"; // Nombre del archivo .cer
		            File certFile = new File(tempDir, certFileName);

		            // Guardar el certificado en un archivo .cer
		            try (FileOutputStream fileOut = new FileOutputStream(certFile)) {
		                byte[] buffer = new byte[4096];
		                int bytesRead;
		                while ((bytesRead = certIs.read(buffer)) != -1) {
		                    fileOut.write(buffer, 0, bytesRead);
		                }
		            }

		            // Añadir el archivo .cer al archivo ZIP
		            try (FileInputStream fis = new FileInputStream(certFile)) {
		                ZipEntry zipEntry = new ZipEntry(certFileName);
		                zipOut.putNextEntry(zipEntry);

		                byte[] buffer = new byte[4096];
		                int bytesRead;
		                while ((bytesRead = fis.read(buffer)) != -1) {
		                    zipOut.write(buffer, 0, bytesRead);
		                }
		                zipOut.closeEntry();
		            }
		        }
		    }

		    // Convertir el archivo ZIP a Base64
		    try (FileInputStream fis = new FileInputStream(zipFile);
		         ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
		        byte[] buffer = new byte[4096];
		        int bytesRead;
		        while ((bytesRead = fis.read(buffer)) != -1) {
		            baos.write(buffer, 0, bytesRead);
		        }
		        appViewForm.setCertificatesB64(Base64.getEncoder().encodeToString(baos.toByteArray()));
		    }

		    // Limpiar archivos temporales
		    File[] tempFiles = tempDir.listFiles();
		    if (tempFiles != null) {
		        for (File tempFile : tempFiles) {
		            tempFile.delete();
		        }
		    }
		    tempDir.delete();

		} catch (IOException | IllegalArgumentException | NullPointerException e) {
		    LOGGER.error("Se produjo un error procesando los certificados", e);
		}
		
		model.addAttribute("appViewForm", appViewForm); //$NON-NLS-1$
		return "modal/applicationViewForm.html"; //$NON-NLS-1$
	}
}
