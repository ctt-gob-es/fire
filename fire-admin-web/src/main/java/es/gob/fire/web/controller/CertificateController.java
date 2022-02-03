
/*
/*******************************************************************************
 * Copyright (C) 2018 MINHAFP, Gobierno de Espa&ntilde;a
 * This program is licensed and may be used, modified and redistributed under the  terms
 * of the European Public License (EUPL), either version 1.1 or (at your option)
 * any later version as soon as they are approved by the European Commission.
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and
 * more details.
 * You should have received a copy of the EUPL1.1 license
 * along with this program; if not, you may find it at
 * http:joinup.ec.europa.eu/software/page/eupl/licence-eupl
 ******************************************************************************/

/**
 * <b>File:</b><p>es.gob.valet.controller.KeystoreController.java.</p>
 * <b>Description:</b><p>Class that manages the requests related to the Keystore administration.</p>
 * <b>Project:</b><p>Platform for detection and validation of certificates recognized in European TSL.</p>
 * <b>Date:</b><p>18/09/2018.</p>
 * @author Gobierno de Espa&ntilde;a.
 * @version 1.1, 02/02/2022.
 */
package es.gob.fire.web.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;

import es.gob.fire.commons.log.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import es.gob.fire.commons.utils.Base64;
import es.gob.fire.persistence.dto.CertificateDTO;
import es.gob.fire.persistence.entity.Certificate;
import es.gob.fire.persistence.service.ICertificateService;

/**
 * <p>
 * Class that manages the requests related to the Keystore administration.
 * </p>
 * <b>Project:</b>
 * <p>
 * Platform for detection and validation of certificates recognized in European
 * TSL.
 * </p>
 * 
 * @version 1.1, 02/02/2022.
 */
@Controller
public class CertificateController {

	/**
	 * Constant that represents the parameter 'idKeystore'.
	 */
	// private static final String FIELD_ID_KEYSTORE = "idKeystore";

	/**
	 * Constant that represents the parameter 'idCertificate'.
	 */
	private static final String FIELD_ID_CERTIFICATE = "idCertificate";

	/**
	 * Constant that represents the parameter log.
	 */
	private static final Logger LOGGER = Logger.getLogger(CertificateController.class);
	
	/**
	 * Attribute that represents the identifier of the html input text field for the SSL alias certificate. 
	 */
	private static final String FIELD_NAME_CERT = "NombreCert";
	
	/**
	 * Attribute that represents the service object for accessing the repository. 
	 */
	@Autowired
	private ICertificateService CertService;
	
	/**
	 * Attribute that represents the identifier of the html input file field for the certificate file. 
	 */
	private static final String FIELD_FILE = "file";


	/**
	 * Attribute that represents the service object for accessing the
	 * repository.
	 */
	@Autowired
	private ICertificateService certificateService;
	
	/**
	 * Attribute that represents the view message wource. 
	 */
	@Autowired
	private MessageSource messageSource;

	/**
	 * Method that load the list of certificates .
	 *
	 * @param idCertificate
	 *            Parameter that represents ID .
	 * @param model
	 *            Holder object form model attributes.
	 * @return String that represents the name of the view to forward.
	 */
	@RequestMapping(value = "certificateadmin")
	public String index(final Model model, final String nombre_cert) {

		return "fragments/certificateadmin.html";
	}
	
	/**
	 * Method that maps the add user web request to the controller and sets the
	 * backing form.
	 *
	 * @param model
	 *            Holder object for model attributes.
	 * @return String that represents the name of the view to forward.
	 */
	@RequestMapping(value = "addCertificate", method = RequestMethod.POST)
	public String addUser(final Model model) {
		
		model.addAttribute("certAddForm", new CertificateDTO());
		model.addAttribute("accion", "add");
		return "modal/certificateAddForm.html";
	}
	
	/**
	 * Method that opens the modal form user edit.
	 * @param username String that represents the user's name
	 * @param model view Model object
	 * @return String that represents the navigation HTML fragment
	 */
	@RequestMapping(value = "certEdit", method = RequestMethod.POST)
	public String certEdit(@RequestParam("idCertificado") final Long idCertificado, final Model model) {
		Certificate cert = certificateService.getCertificateByCertificateId(idCertificado);
		
		CertificateDTO certEditForm = certificateService.certificateEntityToDto(cert);	
		String certData = "";
		
		if (cert.getCertPrincipal() != null && !cert.getCertPrincipal().isEmpty()) {
			try (final InputStream certIs = new ByteArrayInputStream(Base64.decode(cert.getCertPrincipal()));) {
							
				certData = certificateService.getFormatCertText(certIs);
				certEditForm.setCertPrincipal(certData);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CertificateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if (cert.getCertBackup() != null && !cert.getCertBackup().isEmpty()) {
			try (final InputStream certIs = new ByteArrayInputStream(Base64.decode(cert.getCertBackup()));) {
							
				certData = certificateService.getFormatCertText(certIs);
				certEditForm.setCertBackup(certData);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CertificateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}			

		model.addAttribute("certEditForm", certEditForm);
		return "modal/certificateEditForm.html";
	}
	
	/**
	 * Method that maps the request for opening the view certificate modal
	 * @param idCertificado Long that represents the certificate identifier
	 * @param model view Model object
	 * @return String that represents the navigation HTML modal
	 */
	@RequestMapping(value = "/viewcertificate", method = RequestMethod.POST)
	public String certView(@RequestParam("idCertificado") final Long idCertificado, final Model model) {
		Certificate cert = certificateService.getCertificateByCertificateId(idCertificado);
		
		CertificateDTO certViewForm = certificateService.certificateEntityToDto(cert);	
		String certData = "";
		
		if (cert.getCertPrincipal() != null && !cert.getCertPrincipal().isEmpty()) {
			try (final InputStream certIs = new ByteArrayInputStream(Base64.decode(cert.getCertPrincipal()));) {
							
				certData = certificateService.getFormatCertText(certIs);
				certViewForm.setCertPrincipal(certData);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CertificateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if (cert.getCertBackup() != null && !cert.getCertBackup().isEmpty()) {
			try (final InputStream certIs = new ByteArrayInputStream(Base64.decode(cert.getCertBackup()));) {
							
				certData = certificateService.getFormatCertText(certIs);
				certViewForm.setCertBackup(certData);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CertificateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}			

		model.addAttribute("certBackup", certViewForm.getCertBackup());
		model.addAttribute("certPrincipal", certViewForm.getCertPrincipal());
		model.addAttribute("certViewForm", certViewForm);
		return "modal/certificateViewForm.html";
	}
	
	/**
	 *  Method that loads the necessary information to show the confirmation modal to remove a selected responsible.
	 * @param idResponsible Parameter that represetns ID of responsible.
	 * @param model Holder object for model attributes.
	 * @return String that represents the name of the view to forward.
	 */
	@RequestMapping(value = "/loadconfirmdeletecertificate", method = RequestMethod.GET)
	public String deleteConfirmCertificate(@RequestParam(FIELD_ID_CERTIFICATE) Long idCertificate, Model model) {
		//Metemos los datos en el dto
		CertificateDTO certificateDto = new CertificateDTO();
		certificateDto.setIdCertificate(idCertificate);
				
		model.addAttribute("certificateform", certificateDto);
		return "modal/certificateDelete.html";
	}

}
