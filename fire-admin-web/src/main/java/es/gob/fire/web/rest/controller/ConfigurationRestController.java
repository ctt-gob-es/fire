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
 * <b>File:</b><p>es.gob.fire.web.controller.ConfigurationRestController.java.</p>
 * <b>Description:</b><p>Class that manages the REST requests related to the Configuration administration and JSON communication.</p>
 * <b>Project:</b><p>Application for signing documents of FIRe system.</p>
 * <b>Date:</b><p>07/02/2025.</p>
 * @author Gobierno de Espa&ntilde;a.
 * @version 1.1, 04/03/2025.
 */
package es.gob.fire.web.rest.controller;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import SchedulerEditDTO.SchedulerVerifyCertExpiredDTO;
import es.gob.fire.commons.log.Logger;
import es.gob.fire.commons.utils.NumberConstants;
import es.gob.fire.commons.utils.UtilsDate;
import es.gob.fire.commons.utils.UtilsKeystore;
import es.gob.fire.crypto.aes.AESCipher;
import es.gob.fire.crypto.exceptions.CipherException;
import es.gob.fire.i18n.ISchedulerIdConstants;
import es.gob.fire.i18n.IWebAdminGeneral;
import es.gob.fire.i18n.IWebLogMessages;
import es.gob.fire.i18n.Language;
import es.gob.fire.persistence.dto.GeneralConfigDTO;
import es.gob.fire.persistence.dto.SchedulerEditDTO;
import es.gob.fire.persistence.dto.ServerAfirmaDTO;
import es.gob.fire.persistence.entity.CAuthenticationType;
import es.gob.fire.persistence.entity.CPlannerType;
import es.gob.fire.persistence.entity.Planner;
import es.gob.fire.persistence.entity.Scheduler;
import es.gob.fire.persistence.entity.ServerAfirma;
import es.gob.fire.persistence.service.IPropertyService;
import es.gob.fire.persistence.service.IProviderService;
import es.gob.fire.quartz.task.TasksManager;
import es.gob.fire.service.ICAuthenticationTypeService;
import es.gob.fire.service.ICPlannerTypeService;
import es.gob.fire.service.IPlannerService;
import es.gob.fire.service.ISchedulerService;
import es.gob.fire.service.IServerAfirmaService;

/** 
 * <p>Class that manages the REST requests related to the Configuration administration and JSON communication.</p>
 * <b>Project:</b><p>Application for monitoring services of FIRe system.</p>
 * @version 1.1, 04/03/2025.
 */
@RestController
public class ConfigurationRestController {
	
	/**
	 * Attribute that represents the object that manages the log of the class.
	 */
	private static final Logger LOGGER = Logger.getLogger(ConfigurationRestController.class);
	
	/**
	 * Constant attribute that represents the number to identify the daily planner type.
	 */
	public static final Long PLANNING_TYPE_DAILY = 0L;

	/**
	 * Constant attribute that represents the number to identify the periodic planner type.
	 */
	public static final Long PLANNING_TYPE_PERIODIC = 1L;

	/**
	 * Constant attribute that represents the number to identify the planner type by date.
	 */
	public static final Long PLANNING_TYPE_DATE = 2L;

	/**
	 * Attribute that represents the service object for accessing the repository.
	 */
	@Autowired
	private IPlannerService iPlannerService;
	
	/**
	 * Attribute that represents the service object for accessing the repository.
	 */
	@Autowired
	private ICPlannerTypeService iCPlannerTypeService;
	
	/**
	 * Attribute that represents the service object for accessing the repository.
	 */
	@Autowired
	private ISchedulerService iSchedulerService;
	
	/**
	 * Attribute that represents the service object for accessing the repository.
	 */
	@Autowired
	private IProviderService providerService;
	
	/**
	 * Attribute that represents the service object for accessing the repository.
	 */
	@Autowired
	private IPropertyService propertyService;
	
	/**
	 * Attribute that represents the service object for accessing the repository.
	 */
	@Autowired
	private IServerAfirmaService iServerAfirmaService;
	
	/**
	 * Attribute that represents the service object for accessing the repository.
	 */
	@Autowired
	private ICAuthenticationTypeService iCAuthenticationTypeService;
	
	/**
	 * Constant that represents the parameter 'idValmet'.
	 */
	private static final String FIELD_URL_SERVER_ID = "urlServerId";

	/**
	 * Constant that represents the parameter 'timeoutId'.
	 */
	private static final String FIELD_TIMEOUT_ID = "timeoutId";

	/**
	 * Constant that represents the parameter 'nameAppId'.
	 */
	private static final String FIELD_NAME_APP_ID = "nameAppId";
	
	/**
	 * Constant that represents the parameter 'keystoreFile'.
	 */
	private static final String FIELD_KEYSTORE_FILE = "keystoreFile";

	/**
	 * Constant that represents the parameter 'passwordKeystore'.
	 */
	private static final String FIELD_PASSWORD_KEYSTORE = "passwordKeystore";

	/**
	 * Constant that represents the parameter 'userId'.
	 */
	private static final String FIELD_USER_ID = "userId";
	
	/**
	 * Constant that represents the parameter 'password'.
	 */
	private static final String FIELD_PASSWORD_USER = "password";
	
	/**
	 * Method to update the task.
	 * @param taskForm Parameter that represents the backing form for editing a Task
	 * @return Modified task.
	 */
	@RequestMapping(value = "/updatescheduler", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody SchedulerEditDTO updateScheduler(@RequestBody SchedulerVerifyCertExpiredDTO taskForm) {
		
		try {
			if (!validateInitDate(taskForm.getInitDayStringEdit())) {
				taskForm.setErrorEdit(Language.getResWebFire(IWebLogMessages.ERROR_VALIDATE_DATE));
			} else {
				// se obtiene el planificador
				Planner planner = iPlannerService.getPlannerById(taskForm.getIdPlannerEdit());
				
				// se obtiene el tipo de planificador seleccionado
				Long idCPlannerType = taskForm.getIdPlannerTypeEdit();
				
				// se actualizan los campos horas, minutos y segundos si el
				// planificador es tipo periodico.
				if (idCPlannerType.equals(PLANNING_TYPE_PERIODIC)) {
					// se actualizan los campos horas, minutos y segundos
					planner.setHourPeriod(taskForm.getHourPeriodEdit());
					planner.setMinutePeriod(taskForm.getMinutePeriodEdit());
					planner.setSecondPeriod(taskForm.getSecondPeriodEdit());
				}
				
				// se actualiza la fecha inicial por si se ha modificado.
				Date initDay = UtilsDate.transformDate(taskForm.getInitDayStringEdit(), UtilsDate.FORMAT_DATE_TIME_STANDARD);
				planner.setInitDay(initDay);
				
				CPlannerType plannerType = iCPlannerTypeService.getCPlannerTypeById(idCPlannerType);
				planner.setPlannerType(plannerType);
				
				Scheduler scheduler = iSchedulerService.getSchedulerById(taskForm.getIdSchedulerEdit());
				
				// se actualiza la tarea indicando si esta habilitada o no.
				scheduler.setIsActive(taskForm.getIsEnabledEdit());
				
				// actualizamos los dias de preaviso y de periodo de comunicacion
				scheduler.setAdvanceNotice(taskForm.getDayAdviceNoticeEdit());
				scheduler.setPeriodCommunication(taskForm.getPeriodCommunicationEdit());
				
				// persistimos los cambios del planificador y de la tarea.
				Scheduler updatedTask = iSchedulerService.saveScheduler(scheduler);
				iPlannerService.savePlanner(planner);
				
				if (updatedTask.getIdScheduler() == ISchedulerIdConstants.ID_VALIDATION_CERTIFICATES_EXPIRED) {
					TasksManager.addOrUpdateTaskScheduler(updatedTask);
				}
				
				String taskName = scheduler.getSchedulerName();
				String infoMsg = Language.getFormatResWebFire(IWebLogMessages.INFO_UPDATE_TASK_OK, new Object[ ] { taskName });
				LOGGER.info(infoMsg);
				taskForm.setMsgOkEdit(infoMsg);
			}
		} catch (ParseException e) {
			LOGGER.error(Language.getFormatResWebFire(IWebLogMessages.ERROR_PARSE_DATE, new Object[ ] { e.getMessage() }));
			taskForm.setErrorEdit(Language.getResWebFire(IWebLogMessages.ERROR_UPDATE_TASK_WEB));
		} catch (Exception e) {
			LOGGER.error(Language.getFormatResWebFire(IWebLogMessages.ERROR_UPDATE_TASK_WEB, new Object[ ] { e.getMessage() }));
			taskForm.setErrorEdit(Language.getResWebFire(IWebLogMessages.ERROR_UPDATE_TASK_WEB));
		}
		
		return taskForm;
		
	}
	
	/**
	 * Method to validate the date indicated for the planning of the task.
	 * @param taskForm Parameter that represents the backing form for editing a Task
	 * @return true, if the date is correct.
	 */
	private Boolean validateInitDate(String date) {
		Boolean result = true;
		// se comprueba que la fecha indicada no sea anterior a la actual
		LocalDate now = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(UtilsDate.FORMAT_DATE_TIME_STANDARD);
		if(date!=null && !date.isEmpty()){
			LocalDate initDay = LocalDate.parse(date, formatter);
			if (initDay == null || initDay.isBefore(now)) {
				result = false;
			}
		}
		else{
			result = false;
		}

		return result;
	}

	/**
	 * Handles POST requests to save the general configuration.
	 *
	 * <p>This method receives a {@link GeneralConfigDTO} object in the request body,
	 * saves the provider configurations, and updates the general configuration settings.
	 *
	 * @param request the {@link GeneralConfigDTO} object containing the configuration data to be saved.
	 * @return a {@link ResponseEntity} with a success message if the configuration is saved successfully.
	 */
    @PostMapping("/saveConfigGeneral")
    public ResponseEntity<String> saveConfig(@RequestBody GeneralConfigDTO request) {
    	providerService.saveProviders(request.getProviders());
    	
    	propertyService.saveGeneralConfig(request);

        return ResponseEntity.ok("Configuración guardada exitosamente.");
    }
    
    /**
     * Handles POST requests to update the configuration for WS Afirma.
     *
     * <p>This method receives a {@link ServerAfirmaDTO} object and a keystore file 
     * as multipart data. It validates the provided parameters, updates the server 
     * configuration, and saves the changes.
     *
     * @param serverAfirmaDTO the {@link ServerAfirmaDTO} object containing the server configuration details.
     * @param keystoreFile the keystore file provided as a {@link MultipartFile}.
     * @return the updated {@link ServerAfirmaDTO} object.
     * @throws CipherException if an error occurs during encryption.
     * @throws KeyStoreException if an error occurs while handling the keystore.
     * @throws NoSuchAlgorithmException if the encryption algorithm is not available.
     * @throws CertificateException if an error occurs while processing the certificate.
     * @throws IOException if an error occurs while reading the keystore file.
     */
    @RequestMapping(value = "/updateWsAfirma", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public @ResponseBody ServerAfirmaDTO updateWsAfirma(@RequestPart("serverAfirmaDTO") final ServerAfirmaDTO serverAfirmaDTO, @RequestPart("keystoreFile") final MultipartFile keystoreFile) throws CipherException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
    	JSONObject json = new JSONObject();
    	
    	this.validateServerAfirmaParams(serverAfirmaDTO, json, keystoreFile);
    	
    	if(json.length() > 0) {
    		serverAfirmaDTO.setError(json.toString());
    		return serverAfirmaDTO;
    	}
    	
    	ServerAfirma serverAfirma = iServerAfirmaService.obtainServerAfirmaService(NumberConstants.NUM_1_LONG);
    	CAuthenticationType cAuthenticationType = iCAuthenticationTypeService.obtainAllCAuthenticationType().stream().filter(p -> p.getIdAuthenticationType().equals(serverAfirmaDTO.getIdAuthenticationType())).findAny().orElse(null);
    	
    	if(serverAfirma == null) {
    		serverAfirma = new ServerAfirma();
    		serverAfirma.setIdServerAfirma(NumberConstants.NUM_1_LONG);
    	}
    	
    	serverAfirma.setUrlServer(serverAfirmaDTO.getUrlServer());
    	serverAfirma.setTimeout(serverAfirmaDTO.getTimeout());
    	serverAfirma.setNameApp(serverAfirmaDTO.getNameApp());
    	serverAfirma.setcAuthenticationType(cAuthenticationType);
    	
    	if(cAuthenticationType.getIdAuthenticationType().equals(NumberConstants.NUM_2_LONG)) {
    		serverAfirma.setKeystore(serverAfirmaDTO.getKeystoreB64());
    		serverAfirma.setPasswordKeystore(AESCipher.getInstance().encryptMessageWithBC(serverAfirmaDTO.getPasswordKeystore()));
    		serverAfirma.setUser(null);
    		serverAfirma.setPassword(null);
    		// Obtenemos el subject del certificado para mostrarlo
        	KeyStore keyStore = UtilsKeystore.loadKsPKCS12(Base64.getDecoder().decode(serverAfirmaDTO.getKeystoreB64()), serverAfirmaDTO.getPasswordKeystore());
        	serverAfirmaDTO.setSubject(UtilsKeystore.listAllSubjects(keyStore).get(NumberConstants.NUM0));
    	} else if(cAuthenticationType.getIdAuthenticationType().equals(NumberConstants.NUM_1_LONG)) {
    		serverAfirma.setUser(serverAfirmaDTO.getUser());
    		serverAfirma.setPassword(AESCipher.getInstance().encryptMessageWithBC(serverAfirmaDTO.getPassword()));
    		serverAfirma.setKeystore(null);
    		serverAfirma.setPasswordKeystore(null);
    		serverAfirmaDTO.setKeystoreB64(null);
    	} else {
    		serverAfirma.setUser(null);
    		serverAfirma.setPassword(null);
    		serverAfirma.setPasswordKeystore(null);
    		serverAfirma.setKeystore(null);
    		serverAfirmaDTO.setKeystoreB64(null);
    	}
    	
    	iServerAfirmaService.saveServerAfirma(serverAfirma);
    	
    	return serverAfirmaDTO;
    }

    /**
     * Validates the parameters of a WS Afirma server configuration.
     *
     * <p>This method checks if the required fields in the {@link ServerAfirmaDTO} object 
     * are properly set and adds any validation errors to the provided {@link JSONObject}.
     * If authentication type 2 (keystore-based authentication) is selected, it also validates 
     * the keystore file, checks its validity, and ensures it contains only one certificate.
     *
     * @param serverAfirmaDTO the {@link ServerAfirmaDTO} object containing the server configuration details.
     * @param json the {@link JSONObject} used to store validation errors.
     * @param keystoreFile the keystore file provided as a {@link MultipartFile}, required for keystore-based authentication.
     */
	private void validateServerAfirmaParams(ServerAfirmaDTO serverAfirmaDTO, JSONObject json, MultipartFile keystoreFile) {
		if (StringUtils.isEmpty(serverAfirmaDTO.getUrlServer())) {
			String msgError = Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_CSA001);
			LOGGER.error(msgError);
			json.put(FIELD_URL_SERVER_ID + "_span", msgError);
		} else if(!serverAfirmaDTO.getUrlServer().endsWith("/")) {
			String msgError = Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_CSA015);
			LOGGER.error(msgError);
			json.put(FIELD_URL_SERVER_ID + "_span", msgError);
		}
		
		if (serverAfirmaDTO.getTimeout() == null) {
			String msgError = Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_CSA002);
			LOGGER.error(msgError);
			json.put(FIELD_TIMEOUT_ID + "_span", msgError);
		}
		
		if (StringUtils.isEmpty(serverAfirmaDTO.getNameApp())) {
			String msgError = Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_CSA003);
			LOGGER.error(msgError);
			json.put(FIELD_NAME_APP_ID + "_span", msgError);
		}
		
		if(serverAfirmaDTO.getIdAuthenticationType().equals(NumberConstants.NUM_2_LONG)) {
			if(keystoreFile.isEmpty() && (serverAfirmaDTO.getKeystoreB64() == null || serverAfirmaDTO.getKeystoreB64().isEmpty())) {
				String msgError = Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_CSA004);
				LOGGER.error(msgError);
				json.put(FIELD_KEYSTORE_FILE + "_span", msgError);
			} else if(StringUtils.isEmpty(serverAfirmaDTO.getPasswordKeystore())) {
				String msgError = Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_CSA005);
				LOGGER.error(msgError);
				json.put(FIELD_PASSWORD_KEYSTORE + "_span", msgError);
			} else {
				try {
					byte[] byteCert = !keystoreFile.isEmpty() ? keystoreFile.getBytes() : Base64.getDecoder().decode(serverAfirmaDTO.getKeystoreB64());
					serverAfirmaDTO.setKeystoreB64(Base64.getEncoder().encodeToString(byteCert));
					KeyStore keyStore = UtilsKeystore.loadKsPKCS12(byteCert, serverAfirmaDTO.getPasswordKeystore());
					List<X509Certificate> listX509Certificate = UtilsKeystore.listAllX509Certificate(keyStore);
					if(listX509Certificate.size() > NumberConstants.NUM_1_LONG) {
						String msgError = Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_CSA012);
						LOGGER.error(msgError);
						json.put(FIELD_KEYSTORE_FILE + "_span", msgError);
					} else {
						listX509Certificate.get(NumberConstants.NUM0).checkValidity();
					}
				} catch (CertificateExpiredException e) {
					String msgError = Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_CSA013);
					LOGGER.error(msgError);
					json.put(FIELD_KEYSTORE_FILE + "_span", msgError);
				} catch (CertificateNotYetValidException e) {
					String msgError = Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_CSA014);
					LOGGER.error(msgError);
					json.put(FIELD_KEYSTORE_FILE + "_span", msgError);
				} catch (KeyStoreException e) {
					String msgError = Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_CSA006);
					LOGGER.error(msgError);
					json.put(FIELD_KEYSTORE_FILE + "_span", msgError);
				} catch (NoSuchAlgorithmException e) {
					String msgError = Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_CSA007);
					LOGGER.error(msgError);
					json.put(FIELD_KEYSTORE_FILE + "_span", msgError);
				} catch (CertificateException e) {
					String msgError = Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_CSA008);
					LOGGER.error(msgError);
					json.put(FIELD_KEYSTORE_FILE + "_span", msgError);
				} catch (IOException e) {
					String msgError = Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_CSA009);
					LOGGER.error(msgError);
					json.put(FIELD_KEYSTORE_FILE + "_span", msgError);
				}
			}
		}
		
		if(serverAfirmaDTO.getIdAuthenticationType().equals(NumberConstants.NUM_1_LONG)) {
			if(StringUtils.isEmpty(serverAfirmaDTO.getUser())) {
				String msgError = Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_CSA010);
				LOGGER.error(msgError);
				json.put(FIELD_USER_ID + "_span", msgError);
			}
			
			if(StringUtils.isEmpty(serverAfirmaDTO.getPassword())) {
				String msgError = Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_CSA011);
				LOGGER.error(msgError);
				json.put(FIELD_PASSWORD_USER + "_span", msgError);
			}
		}
	}
}
