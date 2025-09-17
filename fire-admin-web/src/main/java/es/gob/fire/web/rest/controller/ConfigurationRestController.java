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
 * @version 1.3, 10/03/2025.
 */
package es.gob.fire.web.rest.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import es.gob.fire.commons.utils.NumberConstants;
import es.gob.fire.commons.utils.UtilsCertificate;
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
 * @version 1.3, 10/03/2025.
 */
@RestController
public class ConfigurationRestController {

	/**
	 * Attribute that represents the object that manages the log of the class.
	 */
	private static final Logger LOGGER = LogManager.getLogger(ConfigurationRestController.class);

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
	private static final String FIELD_PASSWORD_KEYSTORE = "ksPassword";

	/**
	 * Constant that represents the parameter 'userId'.
	 */
	private static final String FIELD_USER_ID = "userId";

	/**
	 * Constant that represents the parameter 'password'.
	 */
	private static final String FIELD_PASSWORD_USER = "password";
	
	private static final String FIELD_KEYSTORE_TYPE         = "ksType";
	private static final String FIELD_KEYSTORE_CERT_ALIAS   = "ksCertAlias";
	private static final String FIELD_KEYSTORE_CERT_PASS    = "ksCertPassword";

	private static final String FIELD_TRUSTSTORE_FILE       = "truststoreFile";
	private static final String FIELD_TRUSTSTORE_PASSWORD   = "truststorePassword";
	private static final String FIELD_TRUSTSTORE_TYPE       = "truststoreType";

	private static final String FIELD_AUTH_TS_FILE          = "authTruststoreFile";
	private static final String FIELD_AUTH_TS_PASSWORD      = "authTruststorePassword";
	private static final String FIELD_AUTH_TS_TYPE          = "authTruststoreType";
	private static final String FIELD_AUTH_CERT_ALIAS    	= "authCertAlias";

	// ===== Auth type constants (ajusta si vuestra tabla catálogo usa otros IDs) =====
	private static final Long AUTH_UT  = NumberConstants.NUM_1_LONG; // UsernameToken
	private static final Long AUTH_BST = NumberConstants.NUM_2_LONG; // BinarySecurityToken
	
	private static final String SECRET_SENTINEL = "******";

	/**
	 * Method to update the task.
	 * @param taskForm Parameter that represents the backing form for editing a Task
	 * @return Modified task.
	 */
	@RequestMapping(value = "/updatescheduler", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody SchedulerEditDTO updateScheduler(@RequestBody final SchedulerVerifyCertExpiredDTO taskForm) {

		try {
			if (!validateInitDate(taskForm.getInitDayStringEdit())) {
				taskForm.setErrorEdit(Language.getResWebFire(IWebLogMessages.ERROR_VALIDATE_DATE));
			} else {
				// se obtiene el planificador
				final Planner planner = this.iPlannerService.getPlannerById(taskForm.getIdPlannerEdit());

				// se obtiene el tipo de planificador seleccionado
				final Long idCPlannerType = taskForm.getIdPlannerTypeEdit();

				// se actualizan los campos horas, minutos y segundos si el
				// planificador es tipo periodico.
				if (idCPlannerType.equals(PLANNING_TYPE_PERIODIC)) {
					// se actualizan los campos horas, minutos y segundos
					planner.setHourPeriod(taskForm.getHourPeriodEdit());
					planner.setMinutePeriod(taskForm.getMinutePeriodEdit());
					planner.setSecondPeriod(taskForm.getSecondPeriodEdit());
				}

				// se actualiza la fecha inicial por si se ha modificado.
				final Date initDay = UtilsDate.transformDate(taskForm.getInitDayStringEdit(), UtilsDate.FORMAT_DATE_TIME);
				planner.setInitDay(initDay);

				final CPlannerType plannerType = this.iCPlannerTypeService.getCPlannerTypeById(idCPlannerType);
				planner.setPlannerType(plannerType);

				final Scheduler scheduler = this.iSchedulerService.getSchedulerById(taskForm.getIdSchedulerEdit());

				// se actualiza la tarea indicando si esta habilitada o no.
				scheduler.setActive(taskForm.getIsEnabledEdit());

				// actualizamos los dias de preaviso y de periodo de comunicacion
				scheduler.setAdvanceNotice(taskForm.getDayAdviceNoticeEdit());
				scheduler.setPeriodCommunication(taskForm.getPeriodCommunicationEdit());

				// persistimos los cambios del planificador y de la tarea.
				final Scheduler updatedTask = this.iSchedulerService.saveScheduler(scheduler);
				this.iPlannerService.savePlanner(planner);

				if (updatedTask.getIdScheduler() == ISchedulerIdConstants.ID_VALIDATION_CERTIFICATES_EXPIRED) {
					TasksManager.addOrUpdateTaskScheduler(updatedTask);
				}

				final String taskName = scheduler.getSchedulerName();
				final String infoMsg = Language.getFormatResWebFire(IWebLogMessages.INFO_UPDATE_TASK_OK, new Object[ ] { taskName });
				LOGGER.info(infoMsg);
				taskForm.setMsgOkEdit(infoMsg);
			}
		} catch (final ParseException e) {
			LOGGER.error(Language.getFormatResWebFire(IWebLogMessages.ERROR_PARSE_DATE, new Object[ ] { e.getMessage() }));
			taskForm.setErrorEdit(Language.getResWebFire(IWebLogMessages.ERROR_UPDATE_TASK_WEB));
		} catch (final Exception e) {
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
	private Boolean validateInitDate(final String date) {
		Boolean result = true;
		// se comprueba que la fecha indicada no sea anterior a la actual
		final LocalDate now = LocalDate.now();
		final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(UtilsDate.FORMAT_DATE_TIME);
		if(date!=null && !date.isEmpty()){
			final LocalDate initDay = LocalDate.parse(date, formatter);
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
    public ResponseEntity<String> saveConfig(@RequestBody final GeneralConfigDTO request) {
    	this.providerService.saveProviders(request.getProviders());

    	this.propertyService.saveGeneralConfig(request);

        return ResponseEntity.ok("Configuracion guardada exitosamente.");
    }

    /**
     * Updates Afirma WS configuration via multipart POST and returns the effective DTO.
     * <p>
     * Validates input parameters, resolves and persists keystore/truststore/auth-truststore blobs,
     * bumps version counters only when binary data changes, encrypts provided secrets, and (best-effort)
     * extracts certificate metadata for UI convenience. When validation fails, the DTO is returned with
     * its {@code error} field populated (no persistence is performed).
     * <p>
     * Behavior highlights:
     * <ul>
     *   <li>Loads (or creates) the singleton {@code ServerAfirma} entity (id = 1).</li>
     *   <li>Determines {@code CAuthenticationType} from the provided {@code idAuthenticationType}.</li>
     *   <li>Keystore/Truststore/Auth-Truststore:
     *     <ul>
     *       <li>Prefers uploaded file over base64 payload when both are provided.</li>
     *       <li>Increments the corresponding version only if the stored bytes actually change.</li>
     *       <li>Allows clearing the Auth Truststore and resets its related metadata when requested.</li>
     *       <li>Encrypts passwords with {@code AESCipher} when new values are supplied (sentinel values are ignored).</li>
     *       <li>Attempts to read the keystore to expose certificate subject and its base64 for UI (best-effort, non-fatal).</li>
     *     </ul>
     *   </li>
     *   <li>For UsernameToken auth, updates username and (optionally) password (encrypted).</li>
     *   <li>Sanitizes DTO secret fields in the response and sets boolean flags indicating presence of stored secrets.</li>
     * </ul>
     *
     * @param serverAfirmaDTO     JSON part with the desired Afirma configuration and metadata.
     * @param keystoreFile        optional keystore file upload (wins over {@code keystoreB64} when present).
     * @param truststoreFile      optional TLS truststore file upload (wins over {@code truststoreB64} when present).
     * @param authTruststoreFile  optional Auth truststore file upload (wins over {@code authTruststoreB64} when present).
     * @return the updated {@link ServerAfirmaDTO}: on success, it reflects persisted versions/metadata and
     *         clears sensitive input fields; on validation failure, it contains an {@code error} JSON string.
     *
     * @throws CipherException               if encryption of secrets fails.
     * @throws KeyStoreException             if keystore operations (UI extraction path) fail unexpectedly.
     * @throws NoSuchAlgorithmException      if a required crypto algorithm is unavailable.
     * @throws CertificateException          if certificate parsing fails during UI extraction.
     * @throws IOException                   if reading uploaded files/base64 content fails.
     */
    @RequestMapping(
            value = "/updateWsAfirma",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public @ResponseBody ServerAfirmaDTO updateWsAfirma(
            @RequestPart("serverAfirmaDTO") final ServerAfirmaDTO serverAfirmaDTO,
            @RequestPart(name = "keystoreFile",       required = false) final MultipartFile keystoreFile,
            @RequestPart(name = "truststoreFile",     required = false) final MultipartFile truststoreFile,
            @RequestPart(name = "authTruststoreFile", required = false) final MultipartFile authTruststoreFile
    ) throws CipherException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {

        final JSONObject json = new JSONObject();
        this.validateServerAfirmaParams(serverAfirmaDTO, json, keystoreFile, truststoreFile, authTruststoreFile);
        if (json.length() > 0) {
            serverAfirmaDTO.setError(json.toString());
            return serverAfirmaDTO;
        }

        ServerAfirma serverAfirma = this.iServerAfirmaService.obtainServerAfirmaService(NumberConstants.NUM_1_LONG);
        final CAuthenticationType cAuthenticationType = this.iCAuthenticationTypeService.obtainAllCAuthenticationType()
                .stream()
                .filter(p -> p.getIdAuthenticationType().equals(serverAfirmaDTO.getIdAuthenticationType()))
                .findAny()
                .orElse(null);

        if (serverAfirma == null) {
            serverAfirma = new ServerAfirma();
            serverAfirma.setIdServerAfirma(NumberConstants.NUM_1_LONG);
        }

        serverAfirma.setUrlServer(serverAfirmaDTO.getUrlServer());
        serverAfirma.setTimeout(serverAfirmaDTO.getTimeout());
        serverAfirma.setNameApp(serverAfirmaDTO.getNameApp());
        serverAfirma.setcAuthenticationType(cAuthenticationType);

        final byte[] prevKs     = serverAfirma.getKsBlob();
        final byte[] prevTs     = serverAfirma.getTruststoreBlob();
        final byte[] prevAuthTs = serverAfirma.getAuthTsBlob();

        final Long prevKsVer     = serverAfirma.getKeystoreVersion();
        final Long prevTsVer     = serverAfirma.getTruststoreVersion();
        final Long prevAuthTsVer = serverAfirma.getAuthenticationVersion();

        boolean ksChanged = false, tsChanged = false, authTsChanged = false;

        // =====================================================================
        // 1) Keystore
        // =====================================================================
        byte[] ksCandidate = resolveBytesPreferFile(keystoreFile, serverAfirmaDTO.getKeystoreB64());
        if (ksCandidate != null && !bytesEqual(prevKs, ksCandidate)) {
            serverAfirma.setKsBlob(ksCandidate);
            serverAfirma.setKeystoreVersion(incrementVersion(prevKsVer));
            ksChanged = true;
        }
        
        if (!isBlank(serverAfirmaDTO.getKsType())) { 
        	serverAfirma.setKsType(serverAfirmaDTO.getKsType().trim()); 
        }
        
        if (!isBlank(serverAfirmaDTO.getKsCertAlias())) { 
        	serverAfirma.setKsCertAlias(serverAfirmaDTO.getKsCertAlias().trim()); 
        }
        
        if (!isBlank(serverAfirmaDTO.getKsPassword()) && !isSentinel(serverAfirmaDTO.getKsPassword())) {
            serverAfirma.setKsPassword(AESCipher.getInstance().encryptMessageWithBC(serverAfirmaDTO.getKsPassword()));
        }
        
        if (!isBlank(serverAfirmaDTO.getKsCertPassword()) && !isSentinel(serverAfirmaDTO.getKsCertPassword())) {
            serverAfirma.setKsCertPassword(AESCipher.getInstance().encryptMessageWithBC(serverAfirmaDTO.getKsCertPassword()));
        }

        try {
            final byte[] ksToOpen = ksChanged ? serverAfirma.getKsBlob() : prevKs;
            final String ksType   = defaultString(serverAfirma.getKsType(), "PKCS12");
            final String ksPass   = firstNonBlank(serverAfirmaDTO.getKsPassword(), decryptNullable(serverAfirma.getKsPassword()));
            if (ksToOpen != null && ksPass != null) {
                final KeyStore ks = KeyStore.getInstance(ksType);
                try (InputStream is = new ByteArrayInputStream(ksToOpen)) {
                    ks.load(is, ksPass.toCharArray());
                }
                String alias = serverAfirma.getKsCertAlias();
                if (isBlank(alias)) { alias = firstCertOrKeyAlias(ks); }
                final java.security.cert.Certificate cert = (alias != null) ? ks.getCertificate(alias) : null;
                if (cert instanceof X509Certificate) {
                    final X509Certificate x509 = (X509Certificate) cert;
                    serverAfirmaDTO.setSubject(UtilsCertificate.getReadableSubjectSafe(x509));
                    serverAfirmaDTO.setCertificateB64(java.util.Base64.getEncoder().encodeToString(x509.getEncoded()));
                    if (ksChanged) {
                        serverAfirmaDTO.setKeystoreB64(java.util.Base64.getEncoder().encodeToString(serverAfirma.getKsBlob()));
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Keystore UI extraction failed (ignored).", e);
        }

        // =====================================================================
        // 2) Truststore (TLS)
        // =====================================================================
        byte[] tsCandidate = resolveBytesPreferFile(truststoreFile, serverAfirmaDTO.getTruststoreB64());
        if (tsCandidate != null && !bytesEqual(prevTs, tsCandidate)) {
            serverAfirma.setTruststoreBlob(tsCandidate);
            serverAfirma.setTruststoreVersion(incrementVersion(prevTsVer));
            tsChanged = true;
        }
        if (!isBlank(serverAfirmaDTO.getTruststoreType())) { 
        	serverAfirma.setTruststoreType(serverAfirmaDTO.getTruststoreType().trim()); 
        }
        
        if (!isBlank(serverAfirmaDTO.getTruststorePassword()) && !isSentinel(serverAfirmaDTO.getTruststorePassword())) {
            serverAfirma.setTruststorePassword(AESCipher.getInstance().encryptMessageWithBC(serverAfirmaDTO.getTruststorePassword()));
        }
        // =====================================================================
		// 3) Auth Truststore
		// =====================================================================
		byte[] authTsCandidate = null;
		
		// Priority 1: uploaded file wins
		if (!serverAfirmaDTO.isClearAuthTruststore() && authTruststoreFile != null && !authTruststoreFile.isEmpty()) {
		    authTsCandidate = resolveBytesPreferFile(authTruststoreFile, null);
		    if (authTsCandidate != null && !bytesEqual(prevAuthTs, authTsCandidate)) {
		        serverAfirma.setAuthTsBlob(authTsCandidate);
		        serverAfirma.setAuthenticationVersion(incrementVersion(prevAuthTsVer));
		        authTsChanged = true;
		    }
		} else if (serverAfirmaDTO.isClearAuthTruststore()) {
		    if (prevAuthTs != null) {
		        serverAfirma.setAuthTsBlob(null);
		        serverAfirma.setAuthenticationVersion(incrementVersion(prevAuthTsVer));
		        authTsChanged = true;
		    } else {
		        serverAfirma.setAuthTsBlob(null);
		    }
		    serverAfirma.setAuthTsType(null);
		    serverAfirma.setAuthTsPassword(null);
		    serverAfirma.setAuthCertAlias(null);
		
		    serverAfirmaDTO.setAuthTruststoreB64(null);
		    serverAfirmaDTO.setAuthTruststorePassword(null);
		    serverAfirmaDTO.setAuthTruststoreType(null);
		    serverAfirmaDTO.setAuthCertAlias(null);
		} else {
		    authTsCandidate = resolveBytesPreferFile(null, serverAfirmaDTO.getAuthTruststoreB64());
		    if (authTsCandidate != null && !bytesEqual(prevAuthTs, authTsCandidate)) {
		        serverAfirma.setAuthTsBlob(authTsCandidate);
		        serverAfirma.setAuthenticationVersion(incrementVersion(prevAuthTsVer));
		        authTsChanged = true;
		    }
		}
		
		// Metadata update if provided (only when not cleared)
		if (!serverAfirmaDTO.isClearAuthTruststore()) {
		    if (!isBlank(serverAfirmaDTO.getAuthTruststoreType())) {
		        serverAfirma.setAuthTsType(serverAfirmaDTO.getAuthTruststoreType().trim());
		    }
		    if (!isBlank(serverAfirmaDTO.getAuthTruststorePassword()) && !isSentinel(serverAfirmaDTO.getAuthTruststorePassword())) {
		        serverAfirma.setAuthTsPassword(AESCipher.getInstance().encryptMessageWithBC(serverAfirmaDTO.getAuthTruststorePassword()));
		    }
		    if (!isBlank(serverAfirmaDTO.getAuthCertAlias())) {
		        serverAfirma.setAuthCertAlias(serverAfirmaDTO.getAuthCertAlias().trim());
		    }
		}

        // =====================================================================
        // 4) UsernameToken
        // =====================================================================
        final Long authType = (cAuthenticationType != null) ? cAuthenticationType.getIdAuthenticationType() : null;
        final Long AUTH_UT  = NumberConstants.NUM_1_LONG;
        if (safeEquals(authType, AUTH_UT)) {
            if (!isBlank(serverAfirmaDTO.getUser())) {
                serverAfirma.setUser(serverAfirmaDTO.getUser().trim());
            }
            if (safeEquals(authType, AUTH_UT) && !isBlank(serverAfirmaDTO.getPassword()) && !isSentinel(serverAfirmaDTO.getPassword())) {
                serverAfirma.setPassword(AESCipher.getInstance().encryptMessageWithBC(serverAfirmaDTO.getPassword()));
            }
        }
        
        serverAfirmaDTO.setKeystoreVersion(serverAfirma.getKeystoreVersion());
        serverAfirmaDTO.setTruststoreVersion(serverAfirma.getTruststoreVersion());
        serverAfirmaDTO.setAuthenticationVersion(serverAfirma.getAuthenticationVersion());

        serverAfirmaDTO.setHasKsPassword(serverAfirma.getKsPassword() != null);
        serverAfirmaDTO.setHasKsCertPassword(serverAfirma.getKsCertPassword() != null);
        serverAfirmaDTO.setHasTruststorePassword(serverAfirma.getTruststorePassword() != null);
        serverAfirmaDTO.setHasAuthTruststorePassword(serverAfirma.getAuthTsPassword() != null);
        serverAfirmaDTO.setHasUserPassword(serverAfirma.getPassword() != null);

        serverAfirmaDTO.setKsPassword(null);
        serverAfirmaDTO.setKsCertPassword(null);
        serverAfirmaDTO.setTruststorePassword(null);
        serverAfirmaDTO.setAuthTruststorePassword(null);
        serverAfirmaDTO.setPassword(null);
        
        this.iServerAfirmaService.saveServerAfirma(serverAfirma);

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
    private void validateServerAfirmaParams(
            final ServerAfirmaDTO serverAfirmaDTO,
            final JSONObject json,
            final MultipartFile keystoreFile,
            final MultipartFile truststoreFile,
            final MultipartFile authTruststoreFile) {

        // ===== Load existing entity to know what is already stored (passwords/blobs) =====
        final ServerAfirma existing = this.iServerAfirmaService.obtainServerAfirmaService(NumberConstants.NUM_1_LONG);

        final boolean storedKsBlob   = existing != null && existing.getKsBlob() != null && existing.getKsBlob().length > 0;
        final boolean storedKsPass   = existing != null && existing.getKsPassword() != null;

        final boolean storedTsBlob   = existing != null && existing.getTruststoreBlob() != null && existing.getTruststoreBlob().length > 0;
        final boolean storedTsPass   = existing != null && existing.getTruststorePassword() != null;

        final boolean storedAuthTsBlob = existing != null && existing.getAuthTsBlob() != null && existing.getAuthTsBlob().length > 0;
        final boolean storedAuthTsPass = existing != null && existing.getAuthTsPassword() != null;

        final boolean storedUtPass   = existing != null && existing.getPassword() != null;

        // ===== Basic fields =====
        if (isEmpty(serverAfirmaDTO.getUrlServer())) {
            final String msgError = Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_CSA001);
            LOGGER.error(msgError);
            json.put(FIELD_URL_SERVER_ID + "_span", msgError);
        } else if (!serverAfirmaDTO.getUrlServer().endsWith("/")) {
            final String msgError = Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_CSA015);
            LOGGER.error(msgError);
            json.put(FIELD_URL_SERVER_ID + "_span", msgError);
        }

        if (serverAfirmaDTO.getTimeout() == null || serverAfirmaDTO.getTimeout().intValue() <= 0) {
            final String msgError = Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_CSA002);
            LOGGER.error(msgError);
            json.put(FIELD_TIMEOUT_ID + "_span", msgError);
        }

        if (isEmpty(serverAfirmaDTO.getNameApp())) {
            final String msgError = Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_CSA003);
            LOGGER.error(msgError);
            json.put(FIELD_NAME_APP_ID + "_span", msgError);
        }

        final Long authType = serverAfirmaDTO.getIdAuthenticationType();

        // =================================================================================
        // 1) BST (BinarySecurityToken)
        // =================================================================================
        if (safeEquals(authType, AUTH_BST)) {
            final boolean hasKsFile = keystoreFile != null && !keystoreFile.isEmpty();
            final boolean hasKsB64  = !isEmpty(serverAfirmaDTO.getKeystoreB64());
            if (!hasKsFile && !hasKsB64 && !storedKsBlob) {
                final String msgError = Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_CSA004);
                LOGGER.error(msgError);
                json.put(FIELD_KEYSTORE_FILE + "_span", msgError);
            }

            final String dtoKsPass = serverAfirmaDTO.getKsPassword();
            final boolean dtoKsPassProvided = !isEmpty(dtoKsPass) && !isSentinel(dtoKsPass);

            if (hasKsFile && !dtoKsPassProvided) {
                final String msgError = Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_CSA005);
                LOGGER.error(msgError);
                json.put(FIELD_PASSWORD_KEYSTORE + "_span", msgError);
            }

            try {
                final byte[] ksBytes = resolveBytes(keystoreFile, serverAfirmaDTO.getKeystoreB64());
                if (ksBytes != null && ksBytes.length > 0) {
                    String ksPassForLoad = null;
                    if (dtoKsPassProvided) {
                        ksPassForLoad = dtoKsPass;
                    } else if (!hasKsFile && storedKsPass) {
                        ksPassForLoad = decryptNullable(existing.getKsPassword());
                    }

                    final String ksTypeInput = serverAfirmaDTO.getKsType();
                    final KeyStore keyStore = loadKeyStore(ksBytes, ksTypeInput, ksPassForLoad, "PKCS12");

                    String alias = serverAfirmaDTO.getKsCertAlias();
                    java.security.cert.Certificate cert = null;
                    if (!isEmpty(alias)) {
                        cert = keyStore.getCertificate(alias);
                    }
                    if (cert == null) {
                        final java.util.List<X509Certificate> listX509Certificate = UtilsKeystore.listAllX509Certificate(keyStore);
                        if (listX509Certificate == null || listX509Certificate.isEmpty()) {
                            final String msgError = Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_CSA016);
                            LOGGER.error(msgError);
                            json.put(FIELD_KEYSTORE_FILE + "_span", msgError);
                        } else if (listX509Certificate.size() > NumberConstants.NUM_1_LONG) {
                            final String msgError = Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_CSA012);
                            LOGGER.error(msgError);
                            json.put(FIELD_KEYSTORE_FILE + "_span", msgError);
                        } else {
                            listX509Certificate.get(NumberConstants.NUM0).checkValidity();
                        }
                    }

                    if (isEmpty(serverAfirmaDTO.getKeystoreB64())) {
                        serverAfirmaDTO.setKeystoreB64(java.util.Base64.getEncoder().encodeToString(ksBytes));
                    }
                }
            } catch (final KeyStoreException e) {
                LOGGER.error(e);
                final String msgError = Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_CSA006);
                json.put(FIELD_KEYSTORE_FILE + "_span", msgError);
            } catch (final NoSuchAlgorithmException e) {
                LOGGER.error(e);
                final String msgError = Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_CSA007);
                json.put(FIELD_KEYSTORE_FILE + "_span", msgError);
            } catch (final CertificateException e) {
                LOGGER.error(e);
                final String msgError = Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_CSA008);
                json.put(FIELD_KEYSTORE_FILE + "_span", msgError);
            } catch (final IOException e) {
                LOGGER.error(e);
                final String msgError = Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_CSA009);
                json.put(FIELD_KEYSTORE_FILE + "_span", msgError);
            } catch (final GeneralSecurityException e) {
                LOGGER.error(e);
                final String msgError = Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_CSA006);
                json.put(FIELD_KEYSTORE_FILE + "_span", msgError);
            }
        }

        // =================================================================================
        // 2) UT (UsernameToken)
        // =================================================================================
        if (safeEquals(authType, AUTH_UT)) {
            if (isEmpty(serverAfirmaDTO.getUser())) {
                final String msgError = Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_CSA010);
                LOGGER.error(msgError);
                json.put(FIELD_USER_ID + "_span", msgError);
            }
            final String dtoUtPass = serverAfirmaDTO.getPassword();
            final boolean dtoUtPassProvided = !isEmpty(dtoUtPass) && !isSentinel(dtoUtPass);
            if (!storedUtPass && !dtoUtPassProvided) {
                final String msgError = Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_CSA011);
                LOGGER.error(msgError);
                json.put(FIELD_PASSWORD_USER + "_span", msgError);
            }
        }

        // =================================================================================
        // 3) TLS Truststore (optional)
        // =================================================================================
        try {
            final boolean hasTsFile = truststoreFile != null && !truststoreFile.isEmpty();
            final byte[] tsBytes = resolveBytes(truststoreFile, serverAfirmaDTO.getTruststoreB64());
            if (tsBytes != null && tsBytes.length > 0) {
                final String tsType = isEmpty(serverAfirmaDTO.getTruststoreType()) ? "JKS" : serverAfirmaDTO.getTruststoreType();

                final String dtoTsPass = serverAfirmaDTO.getTruststorePassword();
                final boolean dtoTsPassProvided = !isEmpty(dtoTsPass) && !isSentinel(dtoTsPass);
                String tsPassForLoad = null;
                if (dtoTsPassProvided) {
                    tsPassForLoad = dtoTsPass;
                } else if (!hasTsFile && storedTsPass) {
                    tsPassForLoad = decryptNullable(existing.getTruststorePassword());
                }
                loadKeyStore(tsBytes, tsType, tsPassForLoad, "JKS");
            }
        } catch (final KeyStoreException e) {
            LOGGER.error(e);
            final String msgError = Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_CSA006);
            json.put(FIELD_TRUSTSTORE_FILE + "_span", msgError);
        } catch (final NoSuchAlgorithmException e) {
            LOGGER.error(e);
            final String msgError = Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_CSA007);
            json.put(FIELD_TRUSTSTORE_FILE + "_span", msgError);
        } catch (final CertificateException e) {
            LOGGER.error(e);
            final String msgError = Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_CSA008);
            json.put(FIELD_TRUSTSTORE_FILE + "_span", msgError);
        } catch (final IOException | GeneralSecurityException e) {
            LOGGER.error(e);
            final String msgError = Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_CSA009);
            json.put(FIELD_TRUSTSTORE_FILE + "_span", msgError);
        }

        // =================================================================================
        // 4) Auth Truststore (optional)  -- skip completely if clearAuthTruststore==true
        // =================================================================================
        if (!serverAfirmaDTO.isClearAuthTruststore()) {
            try {
                final boolean hasAuthTsFile = authTruststoreFile != null && !authTruststoreFile.isEmpty();
                final byte[] atsBytes = resolveBytes(authTruststoreFile, serverAfirmaDTO.getAuthTruststoreB64());
                if (atsBytes != null && atsBytes.length > 0) {
                    final String atsType = isEmpty(serverAfirmaDTO.getAuthTruststoreType()) ? "JKS" : serverAfirmaDTO.getAuthTruststoreType();

                    final String dtoAuthTsPass = serverAfirmaDTO.getAuthTruststorePassword();
                    final boolean dtoAuthTsPassProvided = !isEmpty(dtoAuthTsPass) && !isSentinel(dtoAuthTsPass);
                    String authTsPassForLoad = null;
                    if (dtoAuthTsPassProvided) {
                        authTsPassForLoad = dtoAuthTsPass;
                    } else if (!hasAuthTsFile && storedAuthTsPass) {
                        authTsPassForLoad = decryptNullable(existing.getAuthTsPassword());
                    }
                    loadKeyStore(atsBytes, atsType, authTsPassForLoad, "JKS");
                }
            } catch (final KeyStoreException e) {
                LOGGER.error(e);
                final String msgError = Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_CSA006);
                json.put(FIELD_AUTH_TS_FILE + "_span", msgError);
            } catch (final NoSuchAlgorithmException e) {
                LOGGER.error(e);
                final String msgError = Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_CSA007);
                json.put(FIELD_AUTH_TS_FILE + "_span", msgError);
            } catch (final CertificateException e) {
                LOGGER.error(e);
                final String msgError = Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_CSA008);
                json.put(FIELD_AUTH_TS_FILE + "_span", msgError);
            } catch (final IOException | GeneralSecurityException e) {
                LOGGER.error(e);
                final String msgError = Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_CSA009);
                json.put(FIELD_AUTH_TS_FILE + "_span", msgError);
            }
        }
    }
	
    /**
     * Resolves binary content from either a multipart file or a Base64 string.
     * Prefers the uploaded file when available; otherwise decodes the Base64 input.
     *
     * @param file   optional multipart file; if non-empty, its bytes are returned.
     * @param base64 optional Base64-encoded string used when {@code file} is absent or empty.
     * @return byte array with the resolved content, or {@code null} if neither source is provided.
     * @throws IOException if reading the multipart file fails.
     */
    private static byte[] resolveBytes(final MultipartFile file, final String base64) throws IOException {
	    if (file != null && !file.isEmpty()) {
	        return file.getBytes();
	    }
	    if (base64 != null && !base64.isEmpty()) {
	        return Base64.getDecoder().decode(base64);
	    }
	    return null;
	}

    /**
     * Determines whether a string is {@code null} or blank after trimming.
     *
     * @param s input string.
     * @return {@code true} if {@code s} is {@code null} or contains only whitespace; otherwise {@code false}.
     */
    private static boolean isEmpty(final String s) { 
		return s == null || s.trim().isEmpty(); 
	}

    /**
     * Loads a {@link KeyStore} from in-memory bytes with the given type and password.
     * Falls back to {@code defaultType} when {@code type} is blank. Accepts {@code null} password.
     *
     * @param bytes       keystore bytes.
     * @param type        requested keystore type (e.g., "JKS", "PKCS12"); may be blank.
     * @param password    keystore password (nullable).
     * @param defaultType default keystore type to use when {@code type} is blank.
     * @return initialized {@link KeyStore}.
     * @throws GeneralSecurityException if keystore type/algorithm is unsupported or loading fails.
     * @throws IOException              if the keystore bytes cannot be read.
     */
    private static KeyStore loadKeyStore(final byte[] bytes, final String type, final String password, final String defaultType)
	        throws GeneralSecurityException, IOException {
	    final String ksType = isEmpty(type) ? defaultType : type.trim();
	    final KeyStore ks = KeyStore.getInstance(ksType);
	    try (InputStream in = new java.io.ByteArrayInputStream(bytes)) {
	        ks.load(in, isEmpty(password) ? null : password.toCharArray());
	    }
	    return ks;
	}
	
    /**
     * Null-safe equality check for {@link Long} values.
     *
     * @param a first value (nullable).
     * @param b second value (nullable).
     * @return {@code true} if both values are equal (including both {@code null}); otherwise {@code false}.
     */
    private static boolean safeEquals(final Long a, final Long b) {
	    return java.util.Objects.equals(a, b);
	}

    /**
     * Checks if a string is {@code null} or blank after trimming.
     *
     * @param s input string.
     * @return {@code true} if blank or {@code null}; otherwise {@code false}.
     */
    private static boolean isBlank(final String s) { return s == null || s.trim().isEmpty(); }

    /**
     * Returns the trimmed input string unless it is blank; otherwise returns the provided default.
     *
     * @param v   candidate string (nullable).
     * @param def default value to return when {@code v} is blank.
     * @return trimmed input or {@code def} if blank.
     */
    private static String defaultString(final String v, final String def) {
	    return isBlank(v) ? def : v.trim();
	}

    /**
     * Returns the first non-blank string between two candidates.
     *
     * @param a primary candidate.
     * @param b secondary candidate.
     * @return {@code a} if non-blank; otherwise {@code b} if non-blank; otherwise {@code null}.
     */
    private static String firstNonBlank(final String a, final String b) {
	    return !isBlank(a) ? a : (!isBlank(b) ? b : null);
	}

    /**
     * Resolves bytes preferring an uploaded file over a Base64 string.
     * If the file is present and non-empty, returns its bytes; otherwise decodes Base64.
     *
     * @param file optional multipart file.
     * @param b64  optional Base64 string.
     * @return resolved bytes, or {@code null} if neither source is provided.
     * @throws IOException if reading the multipart file fails.
     */
    private static byte[] resolveBytesPreferFile(final MultipartFile file, final String b64) throws IOException {
	    if (file != null && !file.isEmpty()) { return file.getBytes(); }
	    if (!isBlank(b64)) { return java.util.Base64.getDecoder().decode(b64); }
	    return null;
	}

    /**
     * Constant-time-ish byte array equality comparison.
     * Avoids early exit to reduce timing differences; safe for non-cryptographic use.
     *
     * @param a left byte array (nullable).
     * @param b right byte array (nullable).
     * @return {@code true} if arrays are both {@code null} or have identical length and contents; otherwise {@code false}.
     */
    private static boolean bytesEqual(final byte[] a, final byte[] b) {
	    if (a == b) return true;
	    if (a == null || b == null) return false;
	    if (a.length != b.length) return false;
	    int diff = 0;
	    for (int i = 0; i < a.length; i++) { diff |= (a[i] ^ b[i]); }
	    return diff == 0;
	}
	
    /**
     * Increments a nullable version number.
     *
     * @param current current version (nullable).
     * @return {@code 1L} when {@code current} is {@code null}; otherwise {@code current + 1}.
     */
    private static Long incrementVersion(final Long current) {
	    return (current == null) ? 1L : (current + 1L);
	}

    /**
     * Decrypts a Base64-encoded secret using {@code AESCipher}, returning {@code null} on failure.
     *
     * @param enc encrypted string (nullable).
     * @return decrypted plaintext, or {@code null} if input is null or decryption fails.
     */
    private static String decryptNullable(final String enc) {
	    try { return (enc == null) ? null : AESCipher.getInstance().decryptMessageBC(enc); }
	    catch (Exception e) { return null; }
	}

    /**
     * Returns the first alias in the given {@link KeyStore} that is either a key entry or a certificate entry.
     *
     * @param ks initialized keystore.
     * @return first matching alias, or {@code null} if none found.
     * @throws KeyStoreException if alias enumeration fails.
     */
    private static String firstCertOrKeyAlias(final KeyStore ks) throws KeyStoreException {
	    final java.util.Enumeration<String> aliases = ks.aliases();
	    while (aliases.hasMoreElements()) {
	        final String a = aliases.nextElement();
	        if (ks.isKeyEntry(a) || ks.isCertificateEntry(a)) { return a; }
	    }
	    return null;
	}

    /**
     * Indicates whether a given string equals the sentinel placeholder used to signal "no change".
     *
     * @param s input string (nullable).
     * @return {@code true} if {@code s} equals the sentinel value; otherwise {@code false}.
     */
    private static boolean isSentinel(final String s) { 
		return SECRET_SENTINEL.equals(s); 
	}
}
