
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
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import es.gob.fire.commons.utils.Base64;
import es.gob.fire.commons.utils.Constants;
import es.gob.fire.commons.utils.Utils;
import es.gob.fire.persistence.dto.CertificateDTO;
import es.gob.fire.persistence.dto.CertificateEditDTO;
import es.gob.fire.persistence.dto.UserDTO;
import es.gob.fire.persistence.dto.UserEditDTO;
import es.gob.fire.persistence.entity.Certificate;
import es.gob.fire.persistence.entity.User;
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
	 * Attribute that represents the service object for accessing the
	 * repository.
	 */
	@Autowired
	private ICertificateService certificateService;

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
	 * Method that maps the add TSL web request to the controller and sets the
	 * backing form.
	 * 
	 * @param model
	 *            Holder object for model attributes.
	 * @return String that represents the name of the view to forward.
	 * @throws IOException
	 *             If the method fails.
	 */

	// @RequestMapping(value = "/addcertificate")
	// public String addCertificate(Model model) throws IOException {
	// CertificateForm CertificateForm = new CertificateForm();
	// model.addAttribute("addcertificateform", CertificateForm);
	// return "modal/keystore/CertificateForm.html";
	// }
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
		
		model.addAttribute("userform", new CertificateDTO());
		model.addAttribute("accion", "add");
		return "modal/certificateForm.html";
	}
	
	/**
	 * Method that opens the modal form user edit.
	 * @param username String that represents the user's name
	 * @param model view Model object
	 * @return String that represents the navigation HTML fragment
	 */
	@RequestMapping(value = "menucertedit")
	public String menuEdit(@RequestParam("idCertificado") final Long idCertificado, final Model model) {
		 Certificate cert = certificateService.getCertificateByCertificateId(idCertificado);
		 CertificateEditDTO certformedit = new CertificateEditDTO();

//		 	certificateEditDTO = new CertificateDTO();
//		 	certificateEditDTO.setIdCertificate(cert.getIdCertificado());
//		 	certificateEditDTO.setAlias(cert.getCertificateName());
//		 	certificateEditDTO.setCertPrincipal(cert.getCertPrincipal());
//		 	certificateEditDTO.setCertBackup(cert.getCertBackup());
//		 	certificateEditDTO.setfechaAlta(cert.getfechaAlta());

		
		model.addAttribute("certformedit", certformedit);
		return "modal/certFormEdit.html";
	}
}
