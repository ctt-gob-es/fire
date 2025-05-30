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
 * <b>File:</b><p>es.gob.fire.web.rest.controller.ApplicationRestController.java.</p>
 * <b>Description:</b><p>Class that manages the REST requests related to the Applications administration and JSON communication.</p>
  * <b>Project:</b><p>Application for signing documents of @firma suite syste.</p>
 * <b>Date:</b><p>22/01/2021.</p>
 * @author Gobierno de Espa&ntilde;a.
 * @version 1.1, 02/02/2022.
 */
package es.gob.fire.web.rest.controller;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

import es.gob.fire.commons.utils.NumberConstants;
import es.gob.fire.i18n.IWebLogMessages;
import es.gob.fire.i18n.IWebViewMessages;
import es.gob.fire.i18n.Language;
import es.gob.fire.persistence.dto.ApplicationDTO;
import es.gob.fire.persistence.dto.ProviderApplicationDTO;
import es.gob.fire.persistence.entity.Application;
import es.gob.fire.persistence.entity.ApplicationResponsible;
import es.gob.fire.persistence.entity.Certificate;
import es.gob.fire.persistence.entity.User;
import es.gob.fire.persistence.service.IApplicationService;
import es.gob.fire.persistence.service.IProviderService;
import es.gob.fire.service.ICertificateService;

/**
 * <p>Class that manages the REST requests related to the Applications administration and JSON communication.</p>
 * <b>Project:</b><p>Application for signing documents of @firma suite systems.</p>
 * @version 1.1, 02/02/2022.
 */
@RestController
public class ApplicationRestController {

	/**
	 * Attribute that represents the object that manages the log of the class.
	 */
	private static final Logger LOGGER = LogManager.getLogger(ApplicationRestController.class);

	/**
	 * Attribute that represents the identifier of the selected alarms from the summary.
	 */
	private static final String FIELD_ID_USERS_SELECTED = "idUsersSelected";

	/**
	 * Attribute that represents the identifier of the selected alarms from the summary.
	 */
	private static final String FIELD_ID_CERTIFICATES_SELECTED = "idCertificatesSelected";

	/**
	 * Constant that represents the key Json 'errorSaveApplication'.
	 */
	private static final String KEY_JS_ERROR_SAVE_APP = "errorSaveApplication";

	/**
	 * Constant that represents the parameter 'appId'.
	 */
	private static final String FIELD_ID_APPLICATION = "appId";

	/**
	 * Constant that represents the field 'appName'.
	 */
	private static final String FIELD_APP_NAME = "appName";

	/**
	 * Constant that represents the field 'user'.
	 */
	private static final String FIELD_RESPONSIBLE = "user";

	/**
	 * Constant that represents the field 'user'.
	 */
	private static final String FIELD_CERTIFICATE = "cert";
	
	/**
	 * Constant that represents the field 'organization'.
	 */
	private static final String FIELD_ORGANIZATION = "organization";
	
	/**
	 * Constant that represents the field 'dir3Code'.
	 */
	private static final String FIELD_DIR3CODE = "dir3Code";

	/**
	 * Attribute that represents the span text.
	 */
	private static final String SPAN = "_span";

	/**
	 * Constant that represents the parameter 'rowIndexApp'.
	 */
	private static final String FIELD_ROW_INDEX_APPLICATION = "rowIndexApp";

	/**
	 * Attribute that represents the service object for accessing the
	 * repository.
	 */
	@Autowired
	private IApplicationService appService;

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
	private IProviderService providerService;

	/**
	 * Attribute that represents the view message wource.
	 */
	@Autowired
	private MessageSource messageSource;

	
	/**
	 * Method that maps the list users web requests to the controller and
	 * forwards the list of apps to the view.
	 *
	 * @param input
	 *            Holder object for datatable attributes.
	 * @return String that represents the name of the view to forward.
	 */
	@JsonView(DataTablesOutput.View.class)
	@RequestMapping(path = "/applicationdatatable", method = RequestMethod.GET)
	public DataTablesOutput<Application> apps(@NotEmpty final DataTablesInput input) {
		return this.appService.getAllApplication(input);
	}

	/**
	 * Method that maps the list users web requests to the controller and
	 * forwards the list of apps to the view.
	 *
	 * @param input
	 *            Holder object for datatable attributes.
	 * @return String that represents the name of the view to forward.
	 */
	@RequestMapping(path = "/previewCertApp", method = RequestMethod.GET)
	public String previewCertApp(@RequestParam("idCertificate") final Long idCertificate) {
		String data = "";
		final Certificate cert = this.certificateService.getCertificateByCertificateId(idCertificate);
		if (cert != null) {
			final String certPrincipal = this.certificateService.getCertificateText(cert.getCertificate());
			if(certPrincipal.isEmpty()) {
				data += "--"; //$NON-NLS-1$
			} else {
				data += certPrincipal;
			}
			data += "$*$"; //$NON-NLS-1$

		}

		return data;
	}

	/**
	 * Method that maps the request for saving a new aplication in the system.
	 * @param appAddForm Object that represents the backing form.
	 * @param idUsersSelected String that represents the list of users responsibles for the application saved.
	 * @param request Object that represents the request,
	 * @return DataTablesOutput<Application>
	 */
	@RequestMapping(value = "/saveapp", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@JsonView(Application.class)
	public ResponseEntity<?> saveApplication(@RequestPart("appForm") final ApplicationDTO appForm, final HttpServletRequest request) {
	    // Objeto para acumular los mensajes de error
	    final JSONObject json = new JSONObject();
	    // Lista para almacenar las organizations encontradas según el dir3Code
	    List<String> availableOrganizations = new ArrayList<>();

	    // Procesar lista de usuarios responsables
	    final List<Long> listUsers = new ArrayList<>();
	    if (!"-1".equals(appForm.getIdUsersSelected())) {
	        final String[] arrayUsers = appForm.getIdUsersSelected().split(",");
	        for (final String userId : arrayUsers) {
	            listUsers.add(Long.valueOf(userId));
	        }
	    }

	    // Procesar lista de certificados
	    final List<Long> listCertificates = new ArrayList<>();
	    if (!"-1".equals(appForm.getIdCertificatesSelected())) {
	        final String[] arrayCertificates = appForm.getIdCertificatesSelected().split(",");
	        for (final String certId : arrayCertificates) {
	            listCertificates.add(Long.valueOf(certId));
	        }
	    }

	    // Procesar proveedores personalizados
	    List<ProviderApplicationDTO> customProviders = new ArrayList<>();
	    if (appForm.getCustomProviders() != null && !appForm.getCustomProviders().isEmpty()) {
	        customProviders = appForm.getCustomProviders();
	    }

	    // Bandera para detectar si existe algún error de validación
	    boolean hasError = false;

	    // Validación del nombre de la aplicación
	    if (isAppNameBlank(appForm.getAppName())) {
	        final String errorValEmptyAppName = this.messageSource.getMessage(IWebViewMessages.ERROR_VAL_APPNAME_REQUIRED, null, request.getLocale());
	        json.put(FIELD_APP_NAME + SPAN, errorValEmptyAppName);
	        hasError = true;
	    }
	    if (isAppNameSizeNotValid(appForm.getAppName())) {
	        final String errorValSizeAppName = this.messageSource.getMessage(IWebViewMessages.ERROR_VAL_APPNAME_SIZE, null, request.getLocale());
	        json.put(FIELD_APP_NAME + SPAN, errorValSizeAppName);
	        hasError = true;
	    }
	    // Validación de la selección de responsables
	    if (!isResponsibleSelected(listUsers)) {
	        final String errorValRespSelected = this.messageSource.getMessage(IWebViewMessages.ERROR_VAL_APP_USER_SELECTED, null, request.getLocale());
	        json.put(FIELD_RESPONSIBLE + SPAN, errorValRespSelected);
	        hasError = true;
	    }
	    // Validación de la selección de certificados
	    if (!isCertificatesSelected(listCertificates)) {
	        final String errorValCertSelected = this.messageSource.getMessage(IWebViewMessages.ERROR_VAL_APP_CERT_SELECTED, null, request.getLocale());
	        json.put(FIELD_CERTIFICATE + SPAN, errorValCertSelected);
	        hasError = true;
	    }
	    // Validación de organization y dir3Code
	    if (appForm.getDir3Code() != null && !appForm.getDir3Code().isEmpty() && !appForm.getUpdateOrganization()) {
	        availableOrganizations = this.appService.findOrganizationByDIR3(appForm.getDir3Code());
	        if (availableOrganizations != null && !availableOrganizations.isEmpty()) {
	            if (!availableOrganizations.contains(appForm.getOrganization())) {
	                // Se añade la organization que se intenta guardar
	                availableOrganizations.add(appForm.getOrganization());
	                final String errorMsg = "El código DIR3 " + appForm.getDir3Code() + " ya está asociado a las siguientes organizations: " + availableOrganizations + ". Seleccione una de ellas.";
	                json.put(FIELD_DIR3CODE + SPAN, errorMsg);
	                hasError = true;
	            }
	        }
	    }

	    // Si se encontraron errores de validación, se retorna un ResponseEntity con el error y la lista de organizations
	    if (hasError) {
	        final JSONObject errorResponse = new JSONObject();
	        errorResponse.put("error", json.toString());
	        errorResponse.put("organizations", availableOrganizations);
	        return ResponseEntity.badRequest().body(errorResponse.toString());
	    }

	    try {
	    	if (appForm.getUpdateOrganization()) {
	    		int appsUpdated = this.appService.renameOrganizationByDir3Code(appForm.getDir3Code(), appForm.getOrganization());
	    	}
	        final Application newApp = this.appService.saveApplication(appForm, listUsers, listCertificates, customProviders);
	        return ResponseEntity.ok(newApp);
	    } catch (final Exception e) {
	        LOGGER.error(Language.getResWebFire(IWebLogMessages.ERRORWEB022), e);
	        json.put(KEY_JS_ERROR_SAVE_APP, Language.getResWebFire(IWebLogMessages.ERRORWEB022));
	        final JSONObject errorResponse = new JSONObject();
	        errorResponse.put("error", json.toString());
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse.toString());
	    }
	}

	/**
	 * Checks if at least one certificate is selected from the given list.
	 *
	 * @param listCertificates A list of certificate IDs to check.
	 * @return {@code true} if the list is not null and contains at least one certificate, {@code false} otherwise.
	 */
	private boolean isCertificatesSelected(List<Long> listCertificates) {
		return listCertificates!=null && listCertificates.size()>0;
	}

	/**
	 * Method that checks if the field 'Name' is empty
	 * @param appName String that represents the value of the field 'Name' to check.
	 * @return true if the value of the field 'Name' is null or empty.
	 */
	private boolean isAppNameBlank(final String appName) {

		boolean result = false;

		if (appName == null) {
			result = true;
		} else {

			if (appName.isEmpty()) {
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
	private boolean isAppNameSizeNotValid(final String appName) {

		boolean result = false;

		if (appName != null && !appName.isEmpty()) {

			result = appName.length() < NumberConstants.NUM1 || appName.length() > NumberConstants.NUM45;

		}

		return result;
	}

	/**
	 * @param listaResponsables
	 * @return
	 */
	private boolean isResponsibleSelected(final List<Long> listaResponsables) {

		return listaResponsables!=null && listaResponsables.size()>0;
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
	@RequestMapping(path = "/deleteapplication", method = RequestMethod.POST)
	public String deleteApplication(@RequestParam(FIELD_ID_APPLICATION) final String appId, @RequestParam(FIELD_ROW_INDEX_APPLICATION) final String index) {
		String result = index;

		try {
			this.appService.deleteApplication(appId);
		} catch (final Exception e) {
			result = "-1";
		}

		return result;
	}

	/**
	 * @param dtInput
	 * @param appId
	 * @return
	 */
	@JsonView(DataTablesOutput.View.class)
	@RequestMapping(path ="/respappdatatable", method = RequestMethod.POST)
	public DataTablesOutput<User> responsablesAplicacion(final DataTablesInput dtInput, @RequestParam(FIELD_ID_APPLICATION) final String appId) {

		final DataTablesOutput<User> dtOutput = new DataTablesOutput<>();
		final List<User> listaUser = new ArrayList<>();

		final List<ApplicationResponsible> responsblesAplicacion = this.appService.getApplicationResponsibleByApprId(appId);

		for (final ApplicationResponsible appResp : responsblesAplicacion) {

			listaUser.add(appResp.getResponsible());
		}

		dtOutput.setDraw(NumberConstants.NUM1);
		dtOutput.setRecordsFiltered((long) listaUser.size());
		dtOutput.setRecordsTotal((long) listaUser.size());
		dtOutput.setData(listaUser);


		return dtOutput;
	}

	@RequestMapping(path = "/enableApplication", method = RequestMethod.POST)
	public ResponseEntity<?> enableApplication(@RequestParam(FIELD_ID_APPLICATION) final String appId) {
	    final Application app = this.appService.getAppByAppId(appId);

	    if (app != null) {
	        app.setHabilitado(!app.isHabilitado());

	        Application savedApp = this.appService.saveApplication(app);
	        
	        ApplicationDTO savedAppDTO = this.appService.applicationEntityToDto(savedApp);

	        return ResponseEntity.ok(savedAppDTO);
	    } else {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                             .body(Collections.singletonMap("error", "Aplicacion no encontrada"));
	    }
	}
	
	@GetMapping(path = "/getProvidersOfApplication")
	public ResponseEntity<?> getApplicationProviders(@RequestParam(FIELD_ID_APPLICATION) final String appId) {
		Application app = this.appService.getAppByAppId(appId);
		
		List<ProviderApplicationDTO> listProviders = this.appService.findProvidersByApplication(app);

        return ResponseEntity.ok(listProviders);
	}
}
