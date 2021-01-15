package es.gob.fire.web.rest.controller;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.servlet.http.HttpServletRequest;
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

import com.fasterxml.jackson.annotation.JsonView;

import es.gob.fire.commons.utils.NumberConstants;
import es.gob.fire.i18n.IWebLogMessages;
import es.gob.fire.i18n.IWebViewMessages;
import es.gob.fire.i18n.Language;
import es.gob.fire.persistence.dto.ApplicationDTO;
import es.gob.fire.persistence.entity.Application;
import es.gob.fire.persistence.entity.Certificate;
import es.gob.fire.persistence.service.IApplicationService;
import es.gob.fire.persistence.service.ICertificateService;


@RestController
public class ApplicationRestController {
	
	/**
	 * Attribute that represents the object that manages the log of the class.
	 */
	private static final Logger LOGGER = Logger.getLogger(ApplicationRestController.class);

	/**
	 * Attribute that represents the identifier of the selected alarms from the summary.
	 */
	private static final String FIELD_ID_USERS_SELECTED = "idUsersSelected";
	
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
		return (DataTablesOutput<Application>) appService.getAllApplication(input);
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
	public String previewCertApp(@RequestParam("idCertificate") Long idCertificate) {
		
		String data = "";
		Certificate cert = certificateService.getCertificateByCertificateId(idCertificate);
				
		if (cert != null) {
			String certPrincipal = certificateService.getCertificateText(cert.getCertPrincipal());
			if(certPrincipal.isEmpty()) {
				data += "--"; //$NON-NLS-1$
			} else {
				data += certPrincipal;
			}
			data += "$*$"; //$NON-NLS-1$
			String certBackup = certificateService.getCertificateText(cert.getCertBackup());
			if(certBackup.isEmpty()) {
				data += "--"; //$NON-NLS-1$
			} else {
				data += certBackup;
			}
		}
		
		return data;
	}
	
	@RequestMapping(value = "/savenewapp", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@JsonView(DataTablesOutput.View.class)
	public @ResponseBody DataTablesOutput<Application> saveNewApplication(@RequestPart("appAddForm") final ApplicationDTO appAddForm, @RequestPart(FIELD_ID_USERS_SELECTED) final String idUsersSelected, HttpServletRequest request) {
		DataTablesOutput<Application> dtOutput = new DataTablesOutput<Application>();
		List<Application> listNewApplication = new ArrayList<Application>();
		JSONObject json = new JSONObject();
		List<Long> listUsers = new ArrayList<>();
		
		if (!"-1".equals(idUsersSelected)) {
			String[] arrayUsers = idUsersSelected.split(",");
			
			for(int i=0; i < arrayUsers.length;i++){
				listUsers.add(new Long(arrayUsers[i]));
			}
		}			
		
		if (isAppNameBlank(appAddForm.getAppName()) || isAppNameSizeNotValid(appAddForm.getAppName()) || !isResponsibleSelected(listUsers)) {
			
			if (isAppNameBlank(appAddForm.getAppName())) {
				
				String errorValEmptyAppName = messageSource.getMessage(IWebViewMessages.ERROR_VAL_APPNAME_REQUIRED, null, request.getLocale());
				
				json.put(FIELD_APP_NAME + SPAN, errorValEmptyAppName);
			}
			
			if (isAppNameSizeNotValid(appAddForm.getAppName())) {
				
				String errorValSizeAppName = messageSource.getMessage(IWebViewMessages.ERROR_VAL_APPNAME_SIZE, null, request.getLocale());	
				
				json.put(FIELD_APP_NAME + SPAN, errorValSizeAppName);
			}
			
			if (!isResponsibleSelected(listUsers)) {
				
				String errorValRespSelected = messageSource.getMessage(IWebViewMessages.ERROR_VAL_APP_USER_SELECTED, null, request.getLocale());	
				
				json.put(FIELD_RESPONSIBLE + SPAN, errorValRespSelected);
			}
			
			dtOutput.setError(json.toString());			
			
		} else {			
			
			try {
				Application newApp = appService.saveApplication(appAddForm, listUsers);
				
				listNewApplication.add(newApp);
				
			} catch (GeneralSecurityException e) {
				LOGGER.error(Language.getResWebFire(IWebLogMessages.ERRORWEB022), e);
				listNewApplication = StreamSupport.stream(appService.getAllApplication().spliterator(), false).collect(Collectors.toList());
				json.put(KEY_JS_ERROR_SAVE_APP, Language.getResWebFire(IWebLogMessages.ERRORWEB022));
				dtOutput.setError(json.toString());
			}
		}
		
		dtOutput.setData(listNewApplication);
		
		return dtOutput;				
	}
	
	/**
	 * Method that checks if the field 'Name' is empty
	 * @param appName String that represents the value of the field 'Name' to check.
	 * @return true if the value of the field 'Name' is null or empty.
	 */
	private boolean isAppNameBlank(String appName) {
		
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
	private boolean isAppNameSizeNotValid(String appName) {
		
		boolean result = false;
		
		if (appName != null && !appName.isEmpty()) {
			
			result = (appName.length() < NumberConstants.NUM1) || (appName.length() > NumberConstants.NUM45);
			
		} 
		
		return result;
	}
	
	/**
	 * @param listaResponsables
	 * @return
	 */
	private boolean isResponsibleSelected(List<Long> listaResponsables) {
		
		return (listaResponsables!=null && listaResponsables.size()>0);
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
			appService.deleteApplication(appId);
		} catch (Exception e) {
			result = "-1";
		}
		
		return result;
	}
	

}
