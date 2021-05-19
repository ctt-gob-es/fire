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
 * <b>File:</b><p>es.gob.fire.web.rest.controller.CertificateRestController.java.</p>
 * <b>Description:</b><p>Class that manages the REST requests related to the Certificate administration and JSON communication.</p>
  * <b>Project:</b><p>Application for signing documents of @firma suite systems</p>
 * <b>Date:</b><p>22/01/2021.</p>
 * @author Gobierno de España.
 * @version 1.1, 19/05/2021.
 */
package es.gob.fire.web.rest.controller;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonView;

import es.gob.fire.commons.utils.Base64;
import es.gob.fire.commons.utils.NumberConstants;
import es.gob.fire.i18n.IWebLogMessages;
import es.gob.fire.i18n.IWebViewMessages;
import es.gob.fire.i18n.Language;
import es.gob.fire.persistence.dto.ApplicationCertDTO;
import es.gob.fire.persistence.dto.CertificateDTO;
import es.gob.fire.persistence.entity.Application;
import es.gob.fire.persistence.entity.Certificate;
import es.gob.fire.persistence.service.IApplicationService;
import es.gob.fire.persistence.service.ICertificateService;

/**
 * <p>Class that manages the REST requests related to the Certificate administration and JSON communication.</p>
 * <b>Project:</b><p>Application for signing documents of @firma suite systems.</p>
 * @version 1.1, 19/05/2021.
 */
@RestController
public class CertificateRestController {
	
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
	private IApplicationService appService;
	
	/**
	 * Constant that represents the key Json 'errorSaveCertificate'.
	 */
	private static final String KEY_JS_ERROR_SAVE_CERT = "errorSaveCertificate";
	
	/**
	 * Constant that represents the parameter 'idSystemCertificate'.
	 */
	private static final String FIELD_ID_CERTIFICATE = "idCertificate";
	
	/**
	 * Constant that represents the field 'alias'.
	 */
	private static final String FIELD_ALIAS = "alias";
	
	/**
	 * Constant that represents the field 'certFile1'.
	 */
	private static final String FIELD_FILE_CERTIFICATE1 = "certFile1";
	
	/**
	 * Constant that represents the field 'certFile2'.
	 */
	private static final String FIELD_FILE_CERTIFICATE2 = "certFile2";
	
	/**
	 * Constant that represents the parameter 'certificateFile'.
	 */
	private static final String FIELD_ROW_INDEX_CERTIFICATE = "rowIndexCert";
		
	/**
	 * Attribute that represents the span text.
	 */
	private static final String SPAN = "_span";
	
	/**
	 * Constant that represents the parameter 'certFile1'.
	 */
	private static final String PARAM_CER_PRINCIPAL = "certFile1";
	/**
	 * Constant that represents the parameter 'certFile2'.
	 */
	private static final String PARAM_CER_BKUP = "certFile2";
	
	/**
	 * Attribute that represents the object that manages the log of the class.
	 */
	private static final Logger LOGGER = Logger.getLogger(CertificateRestController.class);
	
	/**
	 * Attribute that represents the view message wource. 
	 */
	@Autowired
	private MessageSource messageSource;
	
	/**
	 * Method that maps the list certificates web requests to the controller and
	 * forwards the list of users to the view.
	 *
	 * @param input
	 *            Holder object for datatable attributes.
	 * @return String that represents the name of the view to forward.
	 */

	
	@JsonView(DataTablesOutput.View.class)
	@RequestMapping(path = "/certificatedatatable", method = RequestMethod.GET)
	public DataTablesOutput<Certificate> certificates(@NotEmpty final DataTablesInput input) {
		//input.getColumn(COLUMN_CERT_NOT_VALID).setSearchable(Boolean.FALSE);
		
		DataTablesOutput<Certificate> certificates = (DataTablesOutput<Certificate>) certificateService.certificatesDataTable(input);
		List<Certificate> listCertificates = certificates.getData();
		
		certificateService.getSubjectValuesForView(listCertificates);		
		
		certificates.setData(listCertificates);
		
		return certificates;
	}

	/**
	 * Method that maps the delete user request from datatable to the controller
	 * and performs the delete of the certificate identified by its id.
	 *
	 * @param userId
	 *            Identifier of the user to be deleted.
	 * @param index
	 *            Row index of the datatable.
	 * @return String that represents the name of the view to redirect.
	 */
	@JsonView(DataTablesOutput.View.class)
	@RequestMapping(path = "/deletecertificate", method = RequestMethod.POST)
	public String deleteCertificate(@RequestParam(FIELD_ID_CERTIFICATE) final Long idCertificate,
			@RequestParam(FIELD_ROW_INDEX_CERTIFICATE) final String index) {
		String result = index;

		List<Application> aplicacionesCert = appService.getByIdCertificado(idCertificate);

		if (aplicacionesCert != null && aplicacionesCert.size() > 0) {
			result = "error.No se ha podido borrar el certificado, tiene aplicaciones asociadas.";
		} else {

			try {
				certificateService.deleteCertificate(idCertificate);
			} catch (Exception e) {
				result = "-1";
			}
		}
		return result;
	}
	
	/**
	 * Method that maps the save Certificate web request to the controller and saves it
	 * in the persistence.
	 *
	 * @param userForm
	 *            Object that represents the backing user form.
	 * @param bindingResult
	 *            Object that represents the form validation result.
	 * @return {@link DataTablesOutput<Certificate>}
	 */
	@RequestMapping(value = "/savecertificate", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@JsonView(DataTablesOutput.View.class)
	public @ResponseBody DataTablesOutput<Certificate> saveNew(@RequestPart("certAddForm") final CertificateDTO certAddForm, @RequestPart("certFile1") final MultipartFile certFile1, @RequestPart("certFile2") final MultipartFile certFile2, HttpServletRequest request) {
		DataTablesOutput<Certificate> dtOutput = new DataTablesOutput<Certificate>();
		List<Certificate> listNewCertificate = new ArrayList<Certificate>();
		JSONObject json = new JSONObject();
		
		if (isAliasBlank(certAddForm.getAlias()) || isAliasSizeNotValid(certAddForm.getAlias()) || hasNoCertData(certAddForm, certFile1, certFile2)) {
			listNewCertificate = StreamSupport.stream(certificateService.getAllCertificate().spliterator(), false).collect(Collectors.toList());
						
			if (isAliasBlank(certAddForm.getAlias())) {
				
				String errorValEmptyAlias = messageSource.getMessage(IWebViewMessages.ERROR_VAL_ALIAS_REQUIRED, null, request.getLocale());
				
				json.put(FIELD_ALIAS + SPAN, errorValEmptyAlias);
			}
			
			if (isAliasSizeNotValid(certAddForm.getAlias())) {
				
				String errorValSizeAlias = messageSource.getMessage(IWebViewMessages.ERROR_VAL_ALIAS_SIZE, null, request.getLocale());
				
				json.put(FIELD_ALIAS + SPAN, errorValSizeAlias);
			}
			
			
			if (hasNoCertData(certAddForm, certFile1, certFile2)) {
				
				//"Al menos debe indicarse un archivo de certificado"
				
				String errorValCert = messageSource.getMessage(IWebViewMessages.ERROR_VAL_CERT_REQUIRED, null, request.getLocale());
				
				json.put(FIELD_FILE_CERTIFICATE1 + SPAN, errorValCert);
				json.put(FIELD_FILE_CERTIFICATE2 + SPAN, errorValCert);
			}
			
			dtOutput.setError(json.toString());
			
		} else {
				
			String msgerror = null;
			try {
				
				msgerror = "Error al instanciar el proveedor X.509"; 
				final CertificateFactory certFactory = CertificateFactory.getInstance("X.509"); //$NON-NLS-1$
				
				X509Certificate cert1;
				X509Certificate cert2;				
				
				if (!certFile1.isEmpty()) {
	        		try (final InputStream certIs = certFile1.getInputStream();) {
	        			cert1 = (X509Certificate) certFactory.generateCertificate(certIs);
	        			certAddForm.setCertBytes1(cert1.getEncoded());
	        		} catch (CertificateException e) {
	        			msgerror = certFile1.getOriginalFilename() + " no representa un certificado válido";
	        			throw e;
	        		}
				}
        		
				if (!certFile2.isEmpty()) {
				
					try (final InputStream certIs = certFile2.getInputStream();) {
	        			cert2 = (X509Certificate) certFactory.generateCertificate(certIs);
	        			certAddForm.setCertBytes2(cert2.getEncoded());
	        		} catch (CertificateException e) {
	        			msgerror = certFile2.getOriginalFilename() + " no representa un certificado válido";
	        			throw e;
	        		}
				}
				
				certAddForm.setCertFile1(certFile1);
				certAddForm.setCertFile2(certFile2);				
				
				Certificate certificate = certificateService.saveCertificate(certAddForm);
								
				listNewCertificate.add(certificate);
				certificateService.getSubjectValuesForView(listNewCertificate);
				
			} catch (IOException | CertificateException e) {
				LOGGER.error(Language.getFormatResWebFire(IWebLogMessages.ERRORWEB030, new Object[]{e.getMessage()}), e);
				listNewCertificate = StreamSupport.stream(certificateService.getAllCertificate().spliterator(), false).collect(Collectors.toList());
				json.put(KEY_JS_ERROR_SAVE_CERT, Language.getFormatResWebFire(IWebLogMessages.ERRORWEB030, new Object[]{msgerror}));
				dtOutput.setError(json.toString());
			}
		}

		dtOutput.setData(listNewCertificate);

		return dtOutput;

	}
	
	/**
	 * Method that maps the save Certificate web request to the controller and saves it
	 * in the persistence.
	 *
	 * @param userForm
	 *            Object that represents the backing user form.
	 * @param bindingResult
	 *            Object that represents the form validation result.
	 * @return {@link DataTablesOutput<Certificate>}
	 */
	@RequestMapping(value = "/saveeditcert", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@JsonView(DataTablesOutput.View.class)
	public @ResponseBody DataTablesOutput<Certificate> saveEdit(@RequestPart("certEditForm") final CertificateDTO certEditForm, @RequestPart("certFile1") final MultipartFile certFile1, @RequestPart("certFile2") final MultipartFile certFile2, HttpServletRequest request) {
		DataTablesOutput<Certificate> dtOutput = new DataTablesOutput<Certificate>();
		List<Certificate> listNewCertificate = new ArrayList<Certificate>();
		JSONObject json = new JSONObject();		
		
		if (isAliasBlank(certEditForm.getAlias()) || isAliasSizeNotValid(certEditForm.getAlias()) || hasNoCertData(certEditForm, certFile1, certFile2)) {
			listNewCertificate = StreamSupport.stream(certificateService.getAllCertificate().spliterator(), false).collect(Collectors.toList());
					
			if (isAliasBlank(certEditForm.getAlias())) {
				
				String errorValEmptyAlias = messageSource.getMessage(IWebViewMessages.ERROR_VAL_ALIAS_REQUIRED, null, request.getLocale());
				
				json.put(FIELD_ALIAS + SPAN, errorValEmptyAlias);
			}
			
			if (isAliasSizeNotValid(certEditForm.getAlias())) {
				
				String errorValSizeAlias = messageSource.getMessage(IWebViewMessages.ERROR_VAL_ALIAS_SIZE, null, request.getLocale());
				
				json.put(FIELD_ALIAS + SPAN, errorValSizeAlias);
			}
			
			
			if (hasNoCertData(certEditForm, certFile1, certFile2)) {
				
				//"Al menos debe indicarse un archivo de certificado"
				
				String errorValCert = messageSource.getMessage(IWebViewMessages.ERROR_VAL_CERT_REQUIRED, null, request.getLocale());
				
				json.put(FIELD_FILE_CERTIFICATE1 + SPAN, errorValCert);
				json.put(FIELD_FILE_CERTIFICATE2 + SPAN, errorValCert);
			}
			
			dtOutput.setError(json.toString());
			
		} else {
			
			String msgerror = null;
			try {
				
				msgerror = "Error al instanciar el proveedor X.509"; 
				final CertificateFactory certFactory = CertificateFactory.getInstance("X.509"); //$NON-NLS-1$
				
				X509Certificate cert1;
				X509Certificate cert2;				
					
				// Si no se actualiza el certificado 1, dejamos el que estaba
				if (certFile1.isEmpty() && certEditForm.getCertPrincipalB64() != null) {
					
					certEditForm.setCertBytes1(Base64.decode(certEditForm.getCertPrincipalB64()));
				// Si se actualiza el certificado 1, tenemos que comprobar que el archivo representa un certificado valido	
				} else if (!certFile1.isEmpty()) {
					
					try (final InputStream certIs = certFile1.getInputStream();) {
	        			cert1 = (X509Certificate) certFactory.generateCertificate(certIs);
	        			certEditForm.setCertBytes1(cert1.getEncoded());
	        		} catch (CertificateException e) {
	        			msgerror = certFile1.getOriginalFilename() + " no representa un certificado válido";
	        			throw e;
	        		}
				}
				
				// Si no se actualiza el certificado 2, dejamos el que estaba
				if (certFile2.isEmpty() && certEditForm.getCertBackupB64() != null) {
					
					certEditForm.setCertBytes2(Base64.decode(certEditForm.getCertBackupB64()));
				// Si se actualiza el certificado 2, tenemos que comprobar que el archivo representa un certificado valido		
				} else if (!certFile2.isEmpty()) {
					
					try (final InputStream certIs = certFile2.getInputStream();) {
	        			cert2 = (X509Certificate) certFactory.generateCertificate(certIs);
	        			certEditForm.setCertBytes2(cert2.getEncoded());
	        		} catch (CertificateException e) {
	        			msgerror = certFile2.getOriginalFilename() + " no representa un certificado válido";
	        			throw e;
	        		}
				}
				
				Certificate certificate = certificateService.saveCertificate(certEditForm);
								
				listNewCertificate.add(certificate);
				certificateService.getSubjectValuesForView(listNewCertificate);
				
			} catch (IOException | CertificateException e) {
				LOGGER.error(Language.getFormatResWebFire(IWebLogMessages.ERRORWEB030, new Object[]{e.getMessage()}), e);
				listNewCertificate = StreamSupport.stream(certificateService.getAllCertificate().spliterator(), false).collect(Collectors.toList());
				json.put(KEY_JS_ERROR_SAVE_CERT, Language.getFormatResWebFire(IWebLogMessages.ERRORWEB030, new Object[]{msgerror}));
				dtOutput.setError(json.toString());
			}
		}

		dtOutput.setData(listNewCertificate);

		return dtOutput;

	}
	

	/**
	 * Method that checks if no certificate data is sent during edit
	 * @param certEditForm
	 * @param certFile1
	 * @param certFile2
	 * @return
	 */
	private boolean hasNoCertData(CertificateDTO certEditForm, MultipartFile certFile1, MultipartFile certFile2) {
		
		boolean hasNoFileData = false;
		
		if ((certFile1 == null || certFile1.isEmpty()) && (certFile2 == null || certFile2.isEmpty()) && (certEditForm.getCertPrincipalB64() == null || certEditForm.getCertPrincipalB64().isEmpty()) && (certEditForm.getCertBackupB64() == null || certEditForm.getCertBackupB64().isEmpty())) {
			hasNoFileData = true;
		}
		
		return hasNoFileData;
	}
	
	/**
	 * Method that checks if the field 'Name' is empty
	 * @param alias String that represents the value of the field 'Name' to check.
	 * @return true if the value of the field 'Name' is null or empty.
	 */
	private boolean isAliasBlank(String alias) {
		
		boolean result = false;
		
		if (alias == null) {
			result = true;
		} else {
			
			if (alias.isEmpty()) {
				result = true;
			}
			
		}
		
		return result;
	}
	
	/**
	 * Method that checks if the field 'Name' has a invalid size
	 * @param alias String that represents the value of the field 'Name' to check.
	 * @return true if the length of the value of the field 'Name' is not valid.
	 */
	private boolean isAliasSizeNotValid(String alias) {
		
		boolean result = false;
		
		if (alias != null && !alias.isEmpty()) {
			
			result = (alias.length() < NumberConstants.NUM1) || (alias.length() > NumberConstants.NUM45);
			
		} 
		
		return result;
	}

	/**
	 * Method that gets the certificate data from a File and returns it as a String.
	 * @param certFile1 Object that represents the File of the Certificate 1.
	 * @param certFile2 Object that represents the File of the Certificate 2.
	 * @param idField Identifier of the HTML field that triggers the event.
	 * @return String that represents the certificate data.
	 */
	@RequestMapping(value = "/previewCert", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public String previewCert(@RequestPart("certFile1") final MultipartFile certFile1, @RequestPart("certFile2") final MultipartFile certFile2, @RequestPart("idField") final String idField) {
		
		X509Certificate cert = null;
		String certData = "";
		
		if (certFile1 != null && !certFile1.isEmpty() && PARAM_CER_PRINCIPAL.equals(idField)) {
						
			try (final InputStream certIs = certFile1.getInputStream();) {
				
				certData = certificateService.getFormatCertText(certIs);
				
			} catch (IOException e) {
				LOGGER.error(Language.getResWebFire(IWebLogMessages.ERRORWEB030), e.getCause());
			} catch (CertificateException e) {
				LOGGER.error(Language.getFormatResWebFire(IWebLogMessages.ERRORWEB030, new Object[]{certFile1.getOriginalFilename() + " no representa un certificado válido"}));
				e.printStackTrace();
			}
			
		} else if (certFile2 != null && !certFile2.isEmpty() && PARAM_CER_BKUP.equals(idField)) {
			
			try (final InputStream certIs = certFile2.getInputStream();) {
				
				certData = certificateService.getFormatCertText(certIs);
				
			} catch (IOException e) {
				LOGGER.error(Language.getResWebFire(IWebLogMessages.ERRORWEB030), e.getCause());
			} catch (CertificateException e) {
				Language.getFormatResWebFire(IWebLogMessages.ERRORWEB030, new Object[]{certFile2.getOriginalFilename() + " no representa un certificado válido"});
				e.printStackTrace();
			}
			
		}
				
		return certData;		
	}
	
	/**
	 * @param input
	 * @param idCertificate
	 * @return
	 */
	@JsonView(DataTablesOutput.View.class)
	@RequestMapping(path = "/certappdatatable", method = RequestMethod.POST)
	public DataTablesOutput<ApplicationCertDTO> certApplications(@NotEmpty final DataTablesInput input, @RequestParam(FIELD_ID_CERTIFICATE) final Long idCertificate) {
		
		DataTablesOutput<ApplicationCertDTO> certApplications = (DataTablesOutput<ApplicationCertDTO>) appService.getApplicationsCert(input, idCertificate);
				
		return certApplications;
	} 
	
	/**
	 * Method that download the PSC certificate.
	 *
	 * @param response Parameter that represents the response with information about file to download.
	 * @param idCertPsc Parameter that represents the identifier of the PSC certificate.
	 * @throws IOException If the method fails.
	 */
	@RequestMapping(value = "/downloadcert", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public void downloadCert(HttpServletResponse response, @RequestParam(FIELD_ID_CERTIFICATE) Long idCertificate) throws IOException {
		
	}
	
}
