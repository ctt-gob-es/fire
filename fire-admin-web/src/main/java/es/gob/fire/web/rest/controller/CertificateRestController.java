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
 * <b>File:</b><p>es.gob.fire.web.rest.controller.CertificateRestController.java.</p>
 * <b>Description:</b><p>Class that manages the REST requests related to the Certificate administration and JSON communication.</p>
  * <b>Project:</b><p>Application for signing documents of @firma suite systems</p>
 * <b>Date:</b><p>22/01/2021.</p>
 * @author Gobierno de Espa&ntilde;a.
 * @version 1.5, 30/01/2025.
 */
package es.gob.fire.web.rest.controller;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;

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

import es.gob.fire.commons.log.Logger;
import es.gob.fire.commons.utils.Base64;
import es.gob.fire.commons.utils.NumberConstants;
import es.gob.fire.i18n.IWebAdminGeneral;
import es.gob.fire.i18n.IWebLogMessages;
import es.gob.fire.i18n.IWebViewMessages;
import es.gob.fire.i18n.Language;
import es.gob.fire.persistence.dto.ApplicationCertDTO;
import es.gob.fire.persistence.dto.CertificateDTO;
import es.gob.fire.persistence.entity.Application;
import es.gob.fire.persistence.entity.Certificate;
import es.gob.fire.persistence.service.IApplicationService;
import es.gob.fire.persistence.service.ICertificateService;
import es.gob.fire.upgrade.afirma.PlatformWsException;
import es.gob.fire.upgrade.afirma.VerifyAfirmaCertificateResponse;
import es.gob.fire.upgrade.afirma.ws.WSServiceInvokerException;

/**
 * <p>Class that manages the REST requests related to the Certificate administration and JSON communication.</p>
 * <b>Project:</b><p>Application for signing documents of @firma suite systems.</p>
 * @version 1.5, 30/01/2025.
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
	 * Constant that represents the field 'certFile'.
	 */
	private static final String FIELD_FILE_CERTIFICATE1 = "certFile";

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
	 * Constant that represents the parameter 'certFile'.
	 */
	private static final String PARAM_CER_PRINCIPAL = "certFile";
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
	public DataTablesOutput<CertificateDTO> certificates() {
		//input.getColumn(COLUMN_CERT_NOT_VALID).setSearchable(Boolean.FALSE);
		
		List<Certificate> listCertificates = this.certificateService.getAllCertificate();
		
		// Creamos un nuevo objeto DataTablesOutput con los DTO
	    DataTablesOutput<CertificateDTO> dtoOutput = new DataTablesOutput<>();
	    dtoOutput.setData(certificateService.obtainAllCertificateToDTO(listCertificates));
		
		return dtoOutput;
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

		final List<Application> aplicacionesCert = this.appService.getByIdCertificado(idCertificate);

		if (aplicacionesCert != null && aplicacionesCert.size() > 0) {
			result = "error.No se ha podido borrar el certificado, tiene aplicaciones asociadas.";
		} else {

			try {
				this.certificateService.deleteCertificate(idCertificate);
			} catch (final Exception e) {
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
	 * @throws Exception 
	 */
	@RequestMapping(value = "/savecertificate", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@JsonView(DataTablesOutput.View.class)
	public @ResponseBody DataTablesOutput<CertificateDTO> saveNew(@RequestPart("certAddForm") final CertificateDTO certAddForm, @RequestPart("certFile") final MultipartFile certFile, final HttpServletRequest request) {
		final DataTablesOutput<CertificateDTO> dtOutput = new DataTablesOutput<>();
		List<Certificate> listNewCertificate = new ArrayList<>();
		final JSONObject json = new JSONObject();

		if (isAliasBlank(certAddForm.getAlias()) || isAliasSizeNotValid(certAddForm.getAlias()) || hasNoCertData(certAddForm, certFile)) {
			listNewCertificate = StreamSupport.stream(this.certificateService.getAllCertificate().spliterator(), false).collect(Collectors.toList());

			if (isAliasBlank(certAddForm.getAlias())) {

				final String errorValEmptyAlias = this.messageSource.getMessage(IWebViewMessages.ERROR_VAL_ALIAS_REQUIRED, null, request.getLocale());

				json.put(FIELD_ALIAS + SPAN, errorValEmptyAlias);
			}

			if (isAliasSizeNotValid(certAddForm.getAlias())) {

				final String errorValSizeAlias = this.messageSource.getMessage(IWebViewMessages.ERROR_VAL_ALIAS_SIZE, null, request.getLocale());

				json.put(FIELD_ALIAS + SPAN, errorValSizeAlias);
			}


			if (hasNoCertData(certAddForm, certFile)) {

				//"Al menos debe indicarse un archivo de certificado"

				final String errorValCert = this.messageSource.getMessage(IWebViewMessages.ERROR_VAL_CERT_REQUIRED, null, request.getLocale());

				json.put(FIELD_FILE_CERTIFICATE1 + SPAN, errorValCert);
				
			}

			dtOutput.setError(json.toString());

		} else {

			String msgerror = null;
			try {

				msgerror = "Error al instanciar el proveedor X.509";
				final CertificateFactory certFactory = CertificateFactory.getInstance("X.509"); //$NON-NLS-1$

				X509Certificate cert1 = null;
				
				// Validaremos si el certificado introducido por el usuario es válido
				if (!certFile.isEmpty()) {
	        		try (final InputStream certIs = certFile.getInputStream();) {
	        			cert1 = (X509Certificate) certFactory.generateCertificate(certIs);
	        		} catch (final CertificateException e) {
	        			msgerror = certFile.getOriginalFilename() + " no representa un certificado v\u00E1lido";
	        			throw e;
	        		}
				}
				
				try {
					// Validaremos si el certificado esta caducado o bien si su fecha de validez aun no ha entrado en vigor
					cert1.checkValidity();
					
					// Validaremos otros estados del certificado haciendo una petición SOAP
					VerifyAfirmaCertificateResponse verifyAfirmaCertificateResponse = this.certificateService.validateStatusCertificateInAfirmaWS(cert1);
					
					LOGGER.info(verifyAfirmaCertificateResponse.getDescription());
					
					// Si el certificado es valido almacenaremos el certificado en la BD
					if(verifyAfirmaCertificateResponse.isDefinitive()) {
						LOGGER.info(Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_MC004));
						
						certAddForm.setCertBytes(cert1.getEncoded());
						certAddForm.setCertFile(certFile);

						final Certificate certificate = this.certificateService.saveCertificate(certAddForm);

						listNewCertificate.add(certificate);
						dtOutput.setData(this.certificateService.obtainAllCertificateToDTO(listNewCertificate));
					} else if(verifyAfirmaCertificateResponse.isBadCertificateFormat()) {
						msgerror = Language.getFormatResWebAdminGeneral(IWebAdminGeneral.LOG_MC005, new Object[] {certFile.getOriginalFilename()});
						json.put(KEY_JS_ERROR_SAVE_CERT, msgerror);
						dtOutput.setError(json.toString());
					} else if(verifyAfirmaCertificateResponse.isBadCertificateSignature()) {
						msgerror = Language.getFormatResWebAdminGeneral(IWebAdminGeneral.LOG_MC006, new Object[] {certFile.getOriginalFilename()});
						json.put(KEY_JS_ERROR_SAVE_CERT, msgerror);
						dtOutput.setError(json.toString());
					} else if(verifyAfirmaCertificateResponse.isExpired()) {
						msgerror = Language.getFormatResWebAdminGeneral(IWebAdminGeneral.LOG_MC007, new Object[] {certFile.getOriginalFilename()});
						json.put(KEY_JS_ERROR_SAVE_CERT, msgerror);
						dtOutput.setError(json.toString());
					} else if(verifyAfirmaCertificateResponse.isNotYetValid()) {
						msgerror = Language.getFormatResWebAdminGeneral(IWebAdminGeneral.LOG_MC008, new Object[] {certFile.getOriginalFilename()});
						json.put(KEY_JS_ERROR_SAVE_CERT, msgerror);
						dtOutput.setError(json.toString());
					} else if(verifyAfirmaCertificateResponse.isOnHold()) {
						msgerror = Language.getFormatResWebAdminGeneral(IWebAdminGeneral.LOG_MC009, new Object[] {certFile.getOriginalFilename()});
						json.put(KEY_JS_ERROR_SAVE_CERT, msgerror);
						dtOutput.setError(json.toString());
					} else if(verifyAfirmaCertificateResponse.isPathValidationFails()) {
						msgerror = Language.getFormatResWebAdminGeneral(IWebAdminGeneral.LOG_MC010, new Object[] {certFile.getOriginalFilename()});
						json.put(KEY_JS_ERROR_SAVE_CERT, msgerror);
						dtOutput.setError(json.toString());
					} else if(verifyAfirmaCertificateResponse.isRevoked()) {
						msgerror = Language.getFormatResWebAdminGeneral(IWebAdminGeneral.LOG_MC011, new Object[] {certFile.getOriginalFilename()});
						json.put(KEY_JS_ERROR_SAVE_CERT, msgerror);
						dtOutput.setError(json.toString());
					} else if(verifyAfirmaCertificateResponse.isRevokedWithoutTST()) {
						msgerror = Language.getFormatResWebAdminGeneral(IWebAdminGeneral.LOG_MC012, new Object[] {certFile.getOriginalFilename()});
						json.put(KEY_JS_ERROR_SAVE_CERT, msgerror);
						dtOutput.setError(json.toString());
					} else if(verifyAfirmaCertificateResponse.isTemporal()) {
						msgerror = Language.getFormatResWebAdminGeneral(IWebAdminGeneral.LOG_MC013, new Object[] {certFile.getOriginalFilename()});
						json.put(KEY_JS_ERROR_SAVE_CERT, msgerror);
						dtOutput.setError(json.toString());
					}
				} catch (final CertificateExpiredException e) {
					// El certificado está caducado
				    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
				    String expirationDate = dateFormat.format(cert1.getNotAfter());
				    msgerror = Language.getFormatResWebAdminGeneral(IWebAdminGeneral.LOG_MC001, new Object[]{certFile.getOriginalFilename(), expirationDate});
				    json.put(KEY_JS_ERROR_SAVE_CERT, msgerror);
					dtOutput.setError(json.toString());
				} catch (final CertificateNotYetValidException e) {
					 // El certificado aún no es válido
				    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
				    String notBeforeDate = dateFormat.format(cert1.getNotBefore());
				    msgerror = Language.getFormatResWebAdminGeneral(IWebAdminGeneral.LOG_MC002, new Object[]{certFile.getOriginalFilename(), notBeforeDate});
				    json.put(KEY_JS_ERROR_SAVE_CERT, msgerror);
					dtOutput.setError(json.toString());
				} catch (PlatformWsException | WSServiceInvokerException e) {
					// Se ha producido un fallo en la peticion o respuesta del SOAP
					LOGGER.error(e);
					msgerror = Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_MC003);
					json.put(KEY_JS_ERROR_SAVE_CERT, msgerror);
					dtOutput.setError(json.toString());
				}
			
			} catch (IOException | CertificateException e) {
				LOGGER.error(Language.getFormatResWebFire(IWebLogMessages.ERRORWEB030, new Object[]{e.getMessage()}), e);
				listNewCertificate = StreamSupport.stream(this.certificateService.getAllCertificate().spliterator(), false).collect(Collectors.toList());
				json.put(KEY_JS_ERROR_SAVE_CERT, Language.getFormatResWebFire(IWebLogMessages.ERRORWEB030, new Object[]{msgerror}));
				dtOutput.setError(json.toString());
			}
		}

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
	public @ResponseBody DataTablesOutput<CertificateDTO> saveEdit(@RequestPart("certEditForm") final CertificateDTO certEditForm, @RequestPart("certFile") final MultipartFile certFile, final HttpServletRequest request) {
		final DataTablesOutput<CertificateDTO> dtOutput = new DataTablesOutput<>();
		List<Certificate> listNewCertificate = new ArrayList<>();
		final JSONObject json = new JSONObject();

		if (isAliasBlank(certEditForm.getAlias()) || isAliasSizeNotValid(certEditForm.getAlias()) || hasNoCertData(certEditForm, certFile)) {
			listNewCertificate = StreamSupport.stream(this.certificateService.getAllCertificate().spliterator(), false).collect(Collectors.toList());

			if (isAliasBlank(certEditForm.getAlias())) {

				final String errorValEmptyAlias = this.messageSource.getMessage(IWebViewMessages.ERROR_VAL_ALIAS_REQUIRED, null, request.getLocale());

				json.put(FIELD_ALIAS + SPAN, errorValEmptyAlias);
			}

			if (isAliasSizeNotValid(certEditForm.getAlias())) {

				final String errorValSizeAlias = this.messageSource.getMessage(IWebViewMessages.ERROR_VAL_ALIAS_SIZE, null, request.getLocale());

				json.put(FIELD_ALIAS + SPAN, errorValSizeAlias);
			}


			if (hasNoCertData(certEditForm, certFile)) {

				//"Al menos debe indicarse un archivo de certificado"

				final String errorValCert = this.messageSource.getMessage(IWebViewMessages.ERROR_VAL_CERT_REQUIRED, null, request.getLocale());

				json.put(FIELD_FILE_CERTIFICATE1 + SPAN, errorValCert);
				json.put(FIELD_FILE_CERTIFICATE2 + SPAN, errorValCert);
			}

			dtOutput.setError(json.toString());

		} else {

			String msgerror = null;
			try {

				msgerror = "Error al instanciar el proveedor X.509";
				final CertificateFactory certFactory = CertificateFactory.getInstance("X.509"); //$NON-NLS-1$

				X509Certificate cert1 = null;
				
				// Si no se actualiza el certificado 1, dejamos el que estaba
				if (certFile.isEmpty() && certEditForm.getCertificateB64() != null) {

					certEditForm.setCertBytes(Base64.decode(certEditForm.getCertificateB64()));
				// Si se actualiza el certificado 1, tenemos que comprobar que el archivo representa un certificado valido
				} else if (!certFile.isEmpty()) {

					try (final InputStream certIs = certFile.getInputStream();) {
	        			cert1 = (X509Certificate) certFactory.generateCertificate(certIs);
	        			certEditForm.setCertBytes(cert1.getEncoded());
	        		} catch (final CertificateException e) {
	        			msgerror = certFile.getOriginalFilename() + " no representa un certificado v\u00E1lido";
	        			throw e;
	        		}
				}
				
				try {
					// Validaremos si el certificado esta caducado o bien si su fecha de validez aun no ha entrado en vigor
					cert1.checkValidity();
					
					// Validaremos otros estados del certificado haciendo una petición SOAP
					VerifyAfirmaCertificateResponse verifyAfirmaCertificateResponse = this.certificateService.validateStatusCertificateInAfirmaWS(cert1);
					
					LOGGER.info(verifyAfirmaCertificateResponse.getDescription());
					
					// Si el certificado es valido almacenaremos el certificado en la BD
					if(verifyAfirmaCertificateResponse.isDefinitive()) {
						LOGGER.info(Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_MC004));
						
						final Certificate certificate = this.certificateService.saveCertificate(certEditForm);

						listNewCertificate.add(certificate);
						dtOutput.setData(this.certificateService.obtainAllCertificateToDTO(listNewCertificate));
						
					} else if(verifyAfirmaCertificateResponse.isBadCertificateFormat()) {
						msgerror = Language.getFormatResWebAdminGeneral(IWebAdminGeneral.LOG_MC005, new Object[] {certFile.getOriginalFilename()});
						json.put(KEY_JS_ERROR_SAVE_CERT, msgerror);
						dtOutput.setError(json.toString());
					} else if(verifyAfirmaCertificateResponse.isBadCertificateSignature()) {
						msgerror = Language.getFormatResWebAdminGeneral(IWebAdminGeneral.LOG_MC006, new Object[] {certFile.getOriginalFilename()});
						json.put(KEY_JS_ERROR_SAVE_CERT, msgerror);
						dtOutput.setError(json.toString());
					} else if(verifyAfirmaCertificateResponse.isExpired()) {
						msgerror = Language.getFormatResWebAdminGeneral(IWebAdminGeneral.LOG_MC007, new Object[] {certFile.getOriginalFilename()});
						json.put(KEY_JS_ERROR_SAVE_CERT, msgerror);
						dtOutput.setError(json.toString());
					} else if(verifyAfirmaCertificateResponse.isNotYetValid()) {
						msgerror = Language.getFormatResWebAdminGeneral(IWebAdminGeneral.LOG_MC008, new Object[] {certFile.getOriginalFilename()});
						json.put(KEY_JS_ERROR_SAVE_CERT, msgerror);
						dtOutput.setError(json.toString());
					} else if(verifyAfirmaCertificateResponse.isOnHold()) {
						msgerror = Language.getFormatResWebAdminGeneral(IWebAdminGeneral.LOG_MC009, new Object[] {certFile.getOriginalFilename()});
						json.put(KEY_JS_ERROR_SAVE_CERT, msgerror);
						dtOutput.setError(json.toString());
					} else if(verifyAfirmaCertificateResponse.isPathValidationFails()) {
						msgerror = Language.getFormatResWebAdminGeneral(IWebAdminGeneral.LOG_MC010, new Object[] {certFile.getOriginalFilename()});
						json.put(KEY_JS_ERROR_SAVE_CERT, msgerror);
						dtOutput.setError(json.toString());
					} else if(verifyAfirmaCertificateResponse.isRevoked()) {
						msgerror = Language.getFormatResWebAdminGeneral(IWebAdminGeneral.LOG_MC011, new Object[] {certFile.getOriginalFilename()});
						json.put(KEY_JS_ERROR_SAVE_CERT, msgerror);
						dtOutput.setError(json.toString());
					} else if(verifyAfirmaCertificateResponse.isRevokedWithoutTST()) {
						msgerror = Language.getFormatResWebAdminGeneral(IWebAdminGeneral.LOG_MC012, new Object[] {certFile.getOriginalFilename()});
						json.put(KEY_JS_ERROR_SAVE_CERT, msgerror);
						dtOutput.setError(json.toString());
					} else if(verifyAfirmaCertificateResponse.isTemporal()) {
						msgerror = Language.getFormatResWebAdminGeneral(IWebAdminGeneral.LOG_MC013, new Object[] {certFile.getOriginalFilename()});
						json.put(KEY_JS_ERROR_SAVE_CERT, msgerror);
						dtOutput.setError(json.toString());
					}
				} catch (final CertificateExpiredException e) {
					// El certificado está caducado
				    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
				    String expirationDate = dateFormat.format(cert1.getNotAfter());
				    msgerror = Language.getFormatResWebAdminGeneral(IWebAdminGeneral.LOG_MC001, new Object[]{certFile.getOriginalFilename(), expirationDate});
				    json.put(KEY_JS_ERROR_SAVE_CERT, msgerror);
					dtOutput.setError(json.toString());
				} catch (final CertificateNotYetValidException e) {
					 // El certificado aún no es válido
				    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
				    String notBeforeDate = dateFormat.format(cert1.getNotBefore());
				    msgerror = Language.getFormatResWebAdminGeneral(IWebAdminGeneral.LOG_MC002, new Object[]{certFile.getOriginalFilename(), notBeforeDate});
				    json.put(KEY_JS_ERROR_SAVE_CERT, msgerror);
					dtOutput.setError(json.toString());
				} catch (PlatformWsException | WSServiceInvokerException e) {
					// Se ha producido un fallo en la peticion o respuesta del SOAP
					LOGGER.error(e);
					msgerror = Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_MC003);
					json.put(KEY_JS_ERROR_SAVE_CERT, msgerror);
					dtOutput.setError(json.toString());
				}
				
			} catch (IOException | CertificateException e) {
				LOGGER.error(Language.getFormatResWebFire(IWebLogMessages.ERRORWEB030, new Object[]{e.getMessage()}), e);
				listNewCertificate = StreamSupport.stream(this.certificateService.getAllCertificate().spliterator(), false).collect(Collectors.toList());
				json.put(KEY_JS_ERROR_SAVE_CERT, Language.getFormatResWebFire(IWebLogMessages.ERRORWEB030, new Object[]{msgerror}));
				dtOutput.setError(json.toString());
			}
		}

		return dtOutput;

	}


	/**
	 * Method that checks if no certificate data is sent during edit
	 * @param certEditForm
	 * @param certFile
	 * @param certFile2
	 * @return
	 */
	private static boolean hasNoCertData(final CertificateDTO certEditForm, final MultipartFile certFile) {

		boolean hasNoFileData = false;

		if ((certFile == null || certFile.isEmpty()) && (certEditForm.getCertificateB64() == null || certEditForm.getCertificateB64().isEmpty())) {
			hasNoFileData = true;
		}

		return hasNoFileData;
	}

	/**
	 * Method that checks if the field 'Name' is empty
	 * @param alias String that represents the value of the field 'Name' to check.
	 * @return true if the value of the field 'Name' is null or empty.
	 */
	private static boolean isAliasBlank(final String alias) {

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
	private boolean isAliasSizeNotValid(final String alias) {

		boolean result = false;

		if (alias != null && !alias.isEmpty()) {

			result = alias.length() < NumberConstants.NUM1 || alias.length() > NumberConstants.NUM45;

		}

		return result;
	}

	/**
	 * Method that gets the certificate data from a File and returns it as a String.
	 * @param certFile Object that represents the File of the Certificate 1.
	 * @param certFile2 Object that represents the File of the Certificate 2.
	 * @param idField Identifier of the HTML field that triggers the event.
	 * @return String that represents the certificate data.
	 */
	@RequestMapping(value = "/previewCert", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public String previewCert(@RequestPart("certFile") final MultipartFile certFile, @RequestPart("idField") final String idField) {

		String certData = "";

		if (certFile != null && !certFile.isEmpty() && PARAM_CER_PRINCIPAL.equals(idField)) {

			try (final InputStream certIs = certFile.getInputStream();) {

				certData = this.certificateService.getFormatCertText(certIs);

			} catch (final IOException e) {
				LOGGER.error(Language.getResWebFire(IWebLogMessages.ERRORWEB030), e);
			} catch (final CertificateException e) {
				LOGGER.error(Language.getFormatResWebFire(IWebLogMessages.ERRORWEB030, new Object[]{certFile.getOriginalFilename() + " no representa un certificado v\u00E1lido"}), e);
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

		final DataTablesOutput<ApplicationCertDTO> certApplications = this.appService.getApplicationsCert(input, idCertificate);

		return certApplications;
	}
}
