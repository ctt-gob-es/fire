
/*
/*******************************************************************************
 * Copyright (C) 2018 MINHAFP, Gobierno de España
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
 * @author Gobierno de España.
 * @version 1.3, 06/11/2018.
 */
package es.gob.fire.web.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonView;

import es.gob.fire.commons.utils.Base64;
import es.gob.fire.commons.utils.StaticFireConfig;
import es.gob.fire.commons.utils.Utils;
import es.gob.fire.commons.utils.UtilsStringChar;
import es.gob.fire.constants.StaticConstants;
import es.gob.fire.i18n.IWebLogMessages;
import es.gob.fire.i18n.IWebViewMessages;
import es.gob.fire.i18n.Language;
import es.gob.fire.persistence.dto.CertificateDTO;
import es.gob.fire.persistence.dto.CertificateEditDTO;
import es.gob.fire.persistence.entity.Certificate;
import es.gob.fire.persistence.service.ICertificateService;
import es.gob.fire.persistence.service.impl.CertificateService;

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
 * @version 1.3, 06/11/2018.
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
	// @RequestMapping(value = "certificateadmin")
	// public String index(final Model model, final HttpServletRequest request)
	// {
	// model.addAttribute("certificateformEdit", new CertificateEditDTO());
	// return "fragments/certificateadmin.html";
	// }

	@RequestMapping(value = "certificateadmin")
	public String index(final Model model, final String nombre_cert) {

		List<Certificate> certificates = certificateService.getAllCertificate();
		List<CertificateDTO> listaCertificados = new ArrayList<CertificateDTO>();

		// recorrer lista certificados
		CertificateDTO certificateDTO = null;
		Certificate cert = null;

		X509Certificate x509CertPrincipal = null;
		X509Certificate x509CertBackup = null;

		for (int i = 0; i < certificates.size(); i++) {
			cert = certificates.get(i);
			try {
				
				if (cert.getCertPrincipal() != null) {
				
					x509CertPrincipal = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(Base64.decode(cert.getCertPrincipal())));
				} 
				
			} catch (CertificateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				
				if (cert.getCertBackup() != null) {
					x509CertBackup = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(Base64.decode(cert.getCertBackup())));
				}
				
				
			} catch (CertificateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// resto de valores
			certificateDTO = new CertificateDTO();
			certificateDTO.setIdCertificate(cert.getIdCertificado());
			certificateDTO.setAlias(cert.getCertificateName());
			certificateDTO.setCertPrincipal(cert.getCertPrincipal());
			certificateDTO.setCertBackup(cert.getCertBackup());
			certificateDTO.setfechaAlta(cert.getfechaAlta());
			// subject

			java.util.Date expDatePrincipal = new java.util.Date();
			
			if (x509CertPrincipal != null) {
				expDatePrincipal = x509CertPrincipal.getNotAfter();
				certificateDTO.setCertPrincipal(x509CertPrincipal.getSubjectX500Principal().getName() + " Fecha de Caducidad=" + Utils.getStringDateFormat(expDatePrincipal));
			} else {
				certificateDTO.setCertPrincipal("");
			}
			
			if (x509CertBackup != null) {
				
				expDatePrincipal = x509CertBackup.getNotAfter();
				certificateDTO.setCertBackup(x509CertBackup.getSubjectX500Principal().getName() + " Fecha de Caducidad=" + Utils.getStringDateFormat(expDatePrincipal));				
			} else {
				certificateDTO.setCertBackup("");
			}
			
			listaCertificados.add(certificateDTO);

			LOGGER.info(certificateDTO.getCertPrincipal());

			LOGGER.info(certificateDTO.getCertBackup());

		}
		model.addAttribute("listaCertificados", listaCertificados);
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
		
		model.addAttribute("certform", new CertificateDTO());
		model.addAttribute("accion", "add");
		return "modal/certificateForm.html";
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
		CertificateDTO certformedit = new CertificateDTO();

		certformedit.setIdCertificate(cert.getIdCertificado());
		certformedit.setAlias(cert.getCertificateName());
		certformedit.setCertPrincipal(cert.getCertPrincipal());
		certformedit.setCertBackup(cert.getCertBackup());
		certformedit.setfechaAlta(cert.getfechaAlta());

		model.addAttribute("certformedit", certformedit);
		return "modal/certificateEditForm.html";
	}
	
	
	/**
	 * Method that maps the delete certificate request from datatable to the controller
	 * and performs the delete of the ceertificate identified by its id.
	 *
	 * @param userId
	 *            Identifier of the user to be deleted.
	 * @param index
	 *            Row index of the datatable.
	 * @return String that represents the name of the view to redirect.
	 */
//	@JsonView(DataTablesOutput.View.class)
//	@RequestMapping(path = "/deletecertificate", method = RequestMethod.POST)
//	@Transactional
//	public String deleteCertificate(@RequestParam("idCertificado") final Long idCertificado, @RequestParam("index") final String index) {
//		certificateService.deleteCertificate(idCertificado);
//		return index;
//	}

	
	/**
	 * Method that maps the save ssl certificate web request to the controller and saves it in the persistence.
	 * @param file Object that contains the uploaded file information.
	 * @param alias String that represents the SSL certificate alias to be stored
	 * @throws Exception If the method fails
	 * @return DataTablesOutput<SystemCertificate>
	 */
	@JsonView(DataTablesOutput.View.class)
	@RequestMapping(value = "/savecert", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public String saveindex(final Model model, @RequestParam(FIELD_NAME_CERT) String nombre_cert, @RequestParam(FIELD_FILE) MultipartFile file) {
		
		boolean error = false;
		byte[ ] certBytes = null;
		JSONObject json = new JSONObject();
		
		List<Certificate> certificates = certificateService.getAllCertificate();
		List<CertificateDTO> listaCertificados = new ArrayList<CertificateDTO>();

		// recorrer lista certificados
		CertificateDTO certificateDTO = null;
		Certificate cert = null;

		

		// Comprobamos que se ha indicado el alias sin espacios ni caracteres
		// especiales.
		if (nombre_cert != null && nombre_cert.length() != nombre_cert.trim().length()) {

			LOGGER.error(Language.getFormatResWebFire(IWebLogMessages.ERRORWEB003, new Object[ ] { nombre_cert }));
			json.put(FIELD_NAME_CERT + "_span", "El campo nombre no puede tener espacios blancos");
			error = true;
		}

		if (nombre_cert == null) {

			json.put(FIELD_NAME_CERT + "_span", "El campo nombre es obligatorio");
			error = true;
		}
		
		if (CertService.getCertificateByCertificateName(nombre_cert) != null) {
			json.put(FIELD_NAME_CERT + "_span", "Ya existe en el sistema un certificado con nombre: " + nombre_cert);
			error = true;
		}

		if (file == null || file.getSize() == 0) {
			LOGGER.error(Language.getFormatResWebFire(IWebLogMessages.ERRORWEB009, new Object[ ] { nombre_cert }));
			json.put(FIELD_FILE + "_span", "Es obligatorio seleccionar un archivo de certifiado");
			error = true;
		} else {
			try {
				certBytes = file.getBytes();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		String listChar = StaticFireConfig.getProperty(StaticConstants.LIST_CHARACTER_SPECIAL);
		String[ ] characters = listChar.split(",");
		String res = UtilsStringChar.EMPTY_STRING;
		for (int i = 0; i < characters.length; i++) {
			int esta = nombre_cert.indexOf(characters[i]);
			if (esta >= 0) {
				char special = nombre_cert.charAt(esta);
				res += special + UtilsStringChar.SPECIAL_BLANK_SPACE_STRING;
			}
		}

		if (res.length() > 0) {
			LOGGER.error(Language.getFormatResWebFire(IWebLogMessages.ERRORWEB004, new Object[ ] { nombre_cert }));
			json.put(FIELD_FILE + "_span", "El formato del campo alias es incorrecto");
			error = true;
		
		} else {
			certificates = StreamSupport.stream(CertService.getAllCertificate().spliterator(), false).collect(Collectors.toList());
			
		}

		
		return "modal/certificateForm.html";

	}

}
