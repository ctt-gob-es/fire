/*
/*******************************************************************************
 * Copyright (C) 2018 MINHAFP, Gobierno de Espana
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
 * <b>File:</b><p>es.gob.fire.control.tasks.TaskVerifyCertExpired.java.</p>
 * <b>Description:</b><p>Class that performs a task for updated status certificate X509 and send emails to users with differents role.</p>
 * for all the scheduler task classes in FIRe.</p>
 * <b>Project:</b><p>Horizontal platform of validation services of multiPKI certificates and electronic signature.</p>
 * <b>Date:</b><p>12/02/2025.</p>
 * @author Gobierno de Espa&ntilde;a.
 * @version 1.3, 06/03/2025.
 */
package es.gob.fire.control.tasks;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import es.gob.fire.commons.utils.Base64;
import es.gob.fire.commons.utils.NumberConstants;
import es.gob.fire.commons.utils.Utils;
import es.gob.fire.commons.utils.UtilsCertificate;
import es.gob.fire.commons.utils.UtilsKeystore;
import es.gob.fire.crypto.aes.AESCipher;
import es.gob.fire.crypto.exceptions.CipherException;
import es.gob.fire.i18n.IWebLogMessages;
import es.gob.fire.i18n.Language;
import es.gob.fire.mail.MailSenderService;
import es.gob.fire.persistence.dto.MailInfoDTO;
import es.gob.fire.persistence.entity.Certificate;
import es.gob.fire.persistence.entity.CertificatesApplication;
import es.gob.fire.persistence.entity.Scheduler;
import es.gob.fire.persistence.entity.ServerAfirma;
import es.gob.fire.persistence.repository.CertificateRepository;
import es.gob.fire.persistence.repository.CertificatesApplicationRepository;
import es.gob.fire.persistence.repository.UserRepository;
import es.gob.fire.quartz.job.FireTaskException;
import es.gob.fire.quartz.task.FireTask;
import es.gob.fire.service.ICertificateService;
import es.gob.fire.service.IServerAfirmaService;
import es.gob.fire.service.impl.CertificateService;
import es.gob.fire.service.impl.SchedulerService;
import es.gob.fire.spring.config.ApplicationContextProvider;

/**
 * <p>Class that performs a task for updated status certificate X509 and send emails to users with differents role.</p>
 * <b>Project:</b><p>Horizontal platform of validation services of multiPKI
 * certificates and electronic signature.</p>
 * @version 1.3, 06/03/2025.
 */
public class TaskVerifyCertExpired extends FireTask {

	/**
	 * Attribute that represents the object that manages the log of the class.
	 */
	private static final Logger LOGGER = LogManager.getLogger(TaskVerifyCertExpired.class);

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.quartz.task.FireTask#initialMessage()
	 */
	@Override
	protected void initialMessage() {
		LOGGER.info(Language.getResWebFire(IWebLogMessages.LOG_CTV001));
	}

	/**
	 * Servicio de env&iacute;o de correo.
	 */
	private MailSenderService senderService = null;

	/**
	 * The future date when the certificate is set to expire.
	 */
	private Date futureDateCertExpired = null;

	/**
	 * Flag indicating whether the system should calculate the days remaining
	 * until the certificate expires and trigger related actions.
	 */
	private boolean calculateDaysCloseToExpiry = false;

	/**
	 * Flag indicating whether a periodic communication process is active.
	 */
	private boolean periodCommunication = false;

	/**
	 * The current system date at the moment of processing.
	 */
	private Date dateNow;

	/**
	 * Scheduler instance responsible for handling scheduled tasks.
	 */
	private Scheduler scheduler;

	public TaskVerifyCertExpired() {
		super();

		// Configuramos las propiedades de Java Mail y enviamos el correo
		final ConfigurationMail config = ApplicationContextProvider.getApplicationContext().getBean(ConfigurationMail.class);
		this.senderService = new MailSenderService();
		this.senderService.init(config.getProperties());
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.quartz.task.FireTask#doActionOfTheTask()
	 */
	@Transactional
	@Override
	protected void doActionOfTheTask() throws Exception {
		LOGGER.info(Language.getResWebFire(IWebLogMessages.LOG_CTV022));

		final List<Certificate> listCertificateNotYedValid = new ArrayList<>();
		final List<Certificate> listCertificateExpired = new ArrayList<>();
		final List<Certificate> listCertificateExpDaysAdvanceNotice = new ArrayList<>();
		this.dateNow = Calendar.getInstance().getTime();

		this.scheduler = obtainDaysCloseToExpiry();
		this.periodCommunication = isExistPeriodCommunication(this.periodCommunication, this.scheduler);

		// Obtenemos una lista de todos los responsables que tienen asociados una app y un certificado
		final List<MailInfoDTO> listMailInfoDTOResponsible =  ApplicationContextProvider.getApplicationContext().getBean(UserRepository.class).obtainAllCertWithAppAndResposible();

		// Para cada certificado evaluaremos si esta caducado, aun no valido o va a caducar en funcion de los dias de preaviso configurados
		final List<Certificate> listCertificate = ApplicationContextProvider.getApplicationContext().getBean(CertificateRepository.class).findAll();
		for (final Certificate certificate : listCertificate) {

			// Obtenemos a partir de la factoria de java un certificado X509
			final X509Certificate x509Certificate = (X509Certificate) CertificateFactory.getInstance(CertificateService.X509).generateCertificate(new ByteArrayInputStream(Base64.decode(certificate.getCertificate())));

			// Chequeamos la validez del certificado
			try {
				x509Certificate.checkValidity();
				// Cuando el certificado es valido evaluamos la caducidad en base a dias de preaviso y periodo de comunicacion
				if(this.calculateDaysCloseToExpiry) {
					// Verificamos si el certificado caduca en los dias de preaviso configurados
			        if (!x509Certificate.getNotAfter().after(this.futureDateCertExpired)) {
			        	listCertificateExpDaysAdvanceNotice.add(certificate);
			        	if(this.periodCommunication) {
			        		// Enviaremos email al responsable cuando:
				        	//		- No haya fecha de ultima comunicacion
				        	//		- La diferencia de dias entre la fecha actual y la fecha de la ultima comunicacion sea mayor o igual que el numero de dias establecidos para el periodo de comunicacion
				        	//		- La diferencia de dias entre la fecha de caducidad y la fecha actual sea menor o igual que el numero de dias establecidos para el periodo de comunicacion
			        		if(certificate.getDateLastCommunication() == null ) {
			        			sendEmailToResponsiblesForCertCloseToExpiry(listMailInfoDTOResponsible, certificate);
				        		certificate.setDateLastCommunication(this.dateNow);
			        		} else {
			        			// Obtenemos la diferencia de dias entre la fecha actual y la fecha de la ultima comunicacion
					        	final Long diffDaysBetweenDNandDLC = TimeUnit.DAYS.convert(this.dateNow.getTime() - certificate.getDateLastCommunication().getTime(), TimeUnit.MILLISECONDS);
								// Obtenemos la diferencia de dias entre la fecha actual y la fecha de caducidad
					        	final Long diffDaysBetweenDEandDLC = TimeUnit.DAYS.convert(x509Certificate.getNotAfter().getTime() - this.dateNow.getTime(), TimeUnit.MILLISECONDS);
					        	if(diffDaysBetweenDNandDLC >= this.scheduler.getPeriodCommunication() || diffDaysBetweenDEandDLC <= this.scheduler.getPeriodCommunication()) {
					        		sendEmailToResponsiblesForCertCloseToExpiry(listMailInfoDTOResponsible, certificate);
					        		certificate.setDateLastCommunication(this.dateNow);
					        	}
			        		}
			        	}
			        }
				}
			} catch (final CertificateExpiredException e) {
				 // El certificado esta caducado
				listCertificateExpired.add(certificate);
				certificate.setDateLastCommunication(this.dateNow);
				sendEmailToResponsiblesForCertExpired(listMailInfoDTOResponsible, certificate);
			} catch (final CertificateNotYetValidException e) {
				// El certificado aun no es valido
				listCertificateNotYedValid.add(certificate);
				certificate.setDateLastCommunication(this.dateNow);
				sendEmailToResponsiblesForCertNotValid(listMailInfoDTOResponsible, certificate);
			}

			// Actualizamos los campos del certificados
			ApplicationContextProvider.getApplicationContext().getBean(ICertificateService.class).updateCertificateFromTaskValidation(certificate, x509Certificate);
		}

		// Enviaremos los mails a los administradores con el estado de los certificados de sistema
		sendEmailToAdminForCertWithDiffStatus(listCertificateNotYedValid, listCertificateExpired, listCertificateExpDaysAdvanceNotice);

		// Comprobaremos si el servidor de afirma esta configurado con autenticacion de certficado y enviaremos un mail dependiendo de si esta caducado o proximo a caducar
		validateCertAfirmaServerAndSendEmail();
	}

	/**
	 * Validates the certificate of the Afirma server and sends an email notification
	 * if the certificate is close to expiration or has expired.
	 *
	 * @throws KeyStoreException       If there is an issue with the keystore.
	 * @throws NoSuchAlgorithmException If the cryptographic algorithm is not available.
	 * @throws CertificateException     If the certificate is invalid.
	 * @throws CipherException          If there is an issue during decryption.
	 * @throws IOException              If an I/O error occurs.
	 */
	private void validateCertAfirmaServerAndSendEmail() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, CipherException, IOException {
		LOGGER.info(Language.getResWebFire(IWebLogMessages.LOG_CTV023));
		final ServerAfirma serverAfirma = ApplicationContextProvider.getApplicationContext().getBean(IServerAfirmaService.class).obtainServerAfirmaService(NumberConstants.NUM_1_LONG);
		if(null != serverAfirma && serverAfirma.getcAuthenticationType().getIdAuthenticationType().equals(NumberConstants.NUM_2_LONG)) {
			final byte[] byteKeyStore = Base64.decode(serverAfirma.getKeystore());
			final String passwordKeystore = AESCipher.getInstance().decryptMessageBC(serverAfirma.getPasswordKeystore());
			final KeyStore keyStore = UtilsKeystore.loadKsPKCS12(byteKeyStore, passwordKeystore);
			final X509Certificate x509Certificate = UtilsKeystore.listAllX509Certificate(keyStore).get(NumberConstants.NUM0);
			try{
				x509Certificate.checkValidity();
				// Cuando el certificado es valido evaluamos la caducidad en base a dias de preaviso y periodo de comunicacion
				if(this.calculateDaysCloseToExpiry) {
					// Verificamos si el certificado caduca en los dias de preaviso configurados
			        if (!x509Certificate.getNotAfter().after(this.futureDateCertExpired)) {
			        	if(this.periodCommunication) {
			        		// Enviaremos email a los administradores cuando:
				        	//		- No haya fecha de ultima comunicacion
				        	//		- La diferencia de dias entre la fecha actual y la fecha de la ultima comunicacion sea mayor o igual que el numero de dias establecidos para el periodo de comunicacion
				        	//		- La diferencia de dias entre la fecha de caducidad y la fecha actual sea menor o igual que el numero de dias establecidos para el periodo de comunicacion
			        		if(serverAfirma.getDateLastCommunication() == null) {
			        			final String status = Language.getResWebFire(IWebLogMessages.LOG_CTV026);
			        			sendEmailToAdminForCertWithStatusExpiredOrCloseToExpired(x509Certificate, status);
			        			serverAfirma.setDateLastCommunication(this.dateNow);
			        		} else {
			        			// Obtenemos la diferencia de dias entre la fecha actual y la fecha de la ultima comunicacion
					        	final Long diffDaysBetweenDNandDLC = TimeUnit.DAYS.convert(this.dateNow.getTime() - serverAfirma.getDateLastCommunication().getTime(), TimeUnit.MILLISECONDS);
								// Obtenemos la diferencia de dias entre la fecha actual y la fecha de caducidad
					        	final Long diffDaysBetweenDEandDLC = TimeUnit.DAYS.convert(x509Certificate.getNotAfter().getTime() - this.dateNow.getTime(), TimeUnit.MILLISECONDS);
					        	if(diffDaysBetweenDNandDLC >= this.scheduler.getPeriodCommunication() || diffDaysBetweenDEandDLC <= this.scheduler.getPeriodCommunication()) {
					        		final String status = Language.getResWebFire(IWebLogMessages.LOG_CTV026);
					        		sendEmailToAdminForCertWithStatusExpiredOrCloseToExpired(x509Certificate, status);
				        			serverAfirma.setDateLastCommunication(this.dateNow);
					        	}
			        		}

			        		// Actualizamos los campos del servidor de afirma con la fecha de la ultima comuinicacion
			    			ApplicationContextProvider.getApplicationContext().getBean(IServerAfirmaService.class).saveServerAfirma(serverAfirma);
			        	}
			        }
				}
			} catch (final CertificateExpiredException e) {
				final String status = Language.getResWebFire(IWebLogMessages.LOG_CTV025);
				sendEmailToAdminForCertWithStatusExpiredOrCloseToExpired(x509Certificate, status);
			}
		}
	}

	/**
	 * Sends an email notification to administrators when the Afirma server's certificate
	 * is either close to expiration or has already expired.
	 *
	 * @param x509Certificate The X.509 certificate of the Afirma server.
	 * @param status          The status message indicating whether the certificate is close to expiration or expired.
	 */
	private void sendEmailToAdminForCertWithStatusExpiredOrCloseToExpired(final X509Certificate x509Certificate, final String status) {
		// Obtenemos los destinatarios
		final Address[] addresses = obtainUsersAdmin();

		final String subject = Language.getResWebFire(IWebLogMessages.LOG_CTV024);

		final StringBuilder bodySubject = new StringBuilder();

		final String subjectCert = UtilsCertificate.getReadableSubject(x509Certificate);
		final String dateExp = Utils.getStringDateFormat(x509Certificate.getNotAfter());
		bodySubject.append(Language.getFormatResWebFire(IWebLogMessages.LOG_CTV027, new Object[ ] { subjectCert, dateExp,  status }));

		final String msgEmailSucces = Language.getFormatResWebFire(IWebLogMessages.LOG_CTV021, new Object[ ] { Arrays.stream(addresses).map(Address::toString).collect(Collectors.joining(", ")) }); //$NON-NLS-1$

		// Enviamos el correo
		this.senderService.sendEmail(addresses, subject, bodySubject, msgEmailSucces, MailSenderService.MAIL_TEXT_PLAIN_CHARSET);
	}

	/**
	 * Sends an email notification regarding certificates with different statuses:
	 * not yet valid, expired, or close to expiration.
	 *
	 * @param listCertificateNotYedValid         	List of certificates that are not yet valid.
	 * @param listCertificateExpired             	List of expired certificates.
	 * @param listCertificateExpDaysAdvanceNotice 	List of certificates nearing expiration.
	 */
	private void sendEmailToAdminForCertWithDiffStatus(final List<Certificate> listCertificateNotYedValid,
			final List<Certificate> listCertificateExpired, final List<Certificate> listCertificateExpDaysAdvanceNotice) {

		// Solo enviaremos correo de notificacion a los administradores si hay algun certificado en estado: aun no valido, caducado o proximo a caducar
		if(!listCertificateNotYedValid.isEmpty() || !listCertificateExpired.isEmpty() || !listCertificateExpDaysAdvanceNotice.isEmpty()) {
			// Obtenemos los destinatarios
			final Address[] addresses = obtainUsersAdmin();

			final String subject = Language.getResWebFire(IWebLogMessages.LOG_CTV011);

			final StringBuilder bodySubject = new StringBuilder();

			bodySubject.append(Language.getResWebFire(IWebLogMessages.LOG_CTV012));
			bodySubject.append("\n"); //$NON-NLS-1$
			bodySubject.append("\n"); //$NON-NLS-1$

			final List<CertificatesApplication> listCertificatesApplication = ApplicationContextProvider.getApplicationContext().getBean(CertificatesApplicationRepository.class).findAllWithCertificateAndApplication();

			if(!listCertificateNotYedValid.isEmpty()) {

				bodySubject.append(Language.getFormatResWebFire(IWebLogMessages.LOG_CTV013, new Object[ ] { listCertificateNotYedValid.size()}));
				bodySubject.append("\n"); //$NON-NLS-1$

				for (final Certificate certificateNotYedValid : listCertificateNotYedValid) {
					final String appNames = listCertificatesApplication.stream()
						    .filter(p -> p.getCertificate().getIdCertificado().equals(certificateNotYedValid.getIdCertificado()))
						    .map(p -> p.getApplication().getAppName())
						    .distinct()
						    .collect(Collectors.collectingAndThen(Collectors.joining(", "), result -> result == null || result.isEmpty() ? "N/A" : result)); //$NON-NLS-1$ //$NON-NLS-2$
					bodySubject.append(Language.getFormatResWebFire(IWebLogMessages.LOG_CTV014, new Object[ ] { certificateNotYedValid.getCertificateName(), certificateNotYedValid.getSubject(), Utils.getStringDateFormat(certificateNotYedValid.getFechaInicio()), appNames}));
					bodySubject.append("\n"); //$NON-NLS-1$
				}
			}

			if(!listCertificateExpired.isEmpty()) {

				bodySubject.append("\n"); //$NON-NLS-1$
				bodySubject.append("\n"); //$NON-NLS-1$
				bodySubject.append(Language.getFormatResWebFire(IWebLogMessages.LOG_CTV015, new Object[ ] { listCertificateExpired.size()}));
				bodySubject.append("\n"); //$NON-NLS-1$

				for (final Certificate certificateExpired : listCertificateExpired) {
					final String appNames = listCertificatesApplication.stream()
						    .filter(p -> p.getCertificate().getIdCertificado().equals(certificateExpired.getIdCertificado()))
						    .map(p -> p.getApplication().getAppName())
						    .distinct()
						    .collect(Collectors.collectingAndThen(Collectors.joining(", "), result -> result == null || result.isEmpty() ? "N/A" : result)); //$NON-NLS-1$ //$NON-NLS-2$
					bodySubject.append(Language.getFormatResWebFire(IWebLogMessages.LOG_CTV016, new Object[ ] { certificateExpired.getCertificateName(), certificateExpired.getSubject(), Utils.getStringDateFormat(certificateExpired.getFechaCaducidad()), appNames}));
					bodySubject.append("\n"); //$NON-NLS-1$
				}
			}

			if(!listCertificateExpDaysAdvanceNotice.isEmpty()) {

				bodySubject.append("\n"); //$NON-NLS-1$
				bodySubject.append("\n"); //$NON-NLS-1$
				bodySubject.append(Language.getFormatResWebFire(IWebLogMessages.LOG_CTV017, new Object[ ] { listCertificateExpDaysAdvanceNotice.size()}));
				bodySubject.append("\n"); //$NON-NLS-1$

				for (final Certificate certificateCloseToExpired : listCertificateExpDaysAdvanceNotice) {
					final String appNames = listCertificatesApplication.stream()
							.filter(p -> p.getCertificate().getIdCertificado().equals(certificateCloseToExpired.getIdCertificado()))
							.map(p -> p.getApplication().getAppName())
							.distinct()
							.collect(Collectors.collectingAndThen(Collectors.joining(", "), result -> result == null || result.isEmpty() ? "N/A" : result)); //$NON-NLS-1$ //$NON-NLS-2$
					bodySubject.append(Language.getFormatResWebFire(IWebLogMessages.LOG_CTV016, new Object[ ] { certificateCloseToExpired.getCertificateName(), certificateCloseToExpired.getSubject(), Utils.getStringDateFormat(certificateCloseToExpired.getFechaCaducidad()), appNames}));
					bodySubject.append("\n"); //$NON-NLS-1$
				}
			}

			final String msgEmailSucces = Language.getFormatResWebFire(IWebLogMessages.LOG_CTV021, new Object[ ] { Arrays.stream(addresses).map(Address::toString).collect(Collectors.joining(", ")) }); //$NON-NLS-1$

			// Enviamos el correo
			this.senderService.sendEmail(addresses, subject, bodySubject, msgEmailSucces, MailSenderService.MAIL_TEXT_PLAIN_CHARSET);
		}

	}

	/**
	 * Sends an email notification to responsible users for a certificate
	 * that is nearing its expiration date.
	 *
	 * @param listMailInfoDTOResponsible List of responsible users with certificate details.
	 * @param certificate                The certificate that is close to expiration.
	 */
	private void sendEmailToResponsiblesForCertCloseToExpiry(final List<MailInfoDTO> listMailInfoDTOResponsible,
			final Certificate certificate) {
		final List<MailInfoDTO> listMailInfoDTORespWithCert = listMailInfoDTOResponsible.stream().filter(p -> p.getIdCertificado().equals(certificate.getIdCertificado())).collect(Collectors.toList());

		if(listMailInfoDTORespWithCert != null && !listMailInfoDTORespWithCert.isEmpty()) {

			final Address[] addresses = listMailInfoDTORespWithCert.stream()
				    .map(MailInfoDTO::getEmailResponsible)
				    .distinct()
				    .map(email -> {
				        try {
				            return new InternetAddress(email);
				        } catch (final Exception e) {
				            throw new RuntimeException(e);
				        }
				    })
				    .toArray(InternetAddress[]::new);

			final String subject = Language.getResWebFire(IWebLogMessages.LOG_CTV005);

			final StringBuilder bodySubject = new StringBuilder();

			for (final MailInfoDTO mailInfoDTO : listMailInfoDTORespWithCert) {
				bodySubject.append(Language.getFormatResWebFire(IWebLogMessages.LOG_CTV006, new Object[ ] { mailInfoDTO.getSubjectCertificate(), mailInfoDTO.getDateCertExpired()}));
				bodySubject.append("\n"); //$NON-NLS-1$
			}

			final String msgEmailSucces = Language.getFormatResWebFire(IWebLogMessages.LOG_CTV020, new Object[ ] { certificate.getCertificateName(), Arrays.stream(addresses).map(Address::toString).collect(Collectors.joining(", ")) }); //$NON-NLS-1$

			// Enviamos el correo
			this.senderService.sendEmail(addresses, subject, bodySubject, msgEmailSucces, MailSenderService.MAIL_TEXT_PLAIN_CHARSET);
		}
	}

	/**
	 * Sends an email to the responsible parties for an expired certificate.
	 *
	 * This method filters the list of responsible parties to get those associated with the provided certificate
	 * and sends an email with details about the expired certificate.
	 *
	 * @param listMailInfoDTOResponsible List of {@link MailInfoDTO} objects containing information about the responsible parties and their certificates.
	 * @param certificate {@link Certificate} object containing information about the expired certificate.
	 */
	private void sendEmailToResponsiblesForCertExpired(final List<MailInfoDTO> listMailInfoDTOResponsible,
			final Certificate certificate) {
		final List<MailInfoDTO> listMailInfoDTORespWithCert = listMailInfoDTOResponsible.stream().filter(p -> p.getIdCertificado().equals(certificate.getIdCertificado())).collect(Collectors.toList());

		if(listMailInfoDTORespWithCert != null && !listMailInfoDTORespWithCert.isEmpty()) {

			final Address[] addresses = listMailInfoDTORespWithCert.stream()
				    .map(MailInfoDTO::getEmailResponsible)
				    .distinct()
				    .map(email -> {
				        try {
				            return new InternetAddress(email);
				        } catch (final Exception e) {
				            throw new RuntimeException(e);
				        }
				    })
				    .toArray(InternetAddress[]::new);

			final String subject = Language.getResWebFire(IWebLogMessages.LOG_CTV007);

			final StringBuilder bodySubject = new StringBuilder();

			for (final MailInfoDTO mailInfoDTO : listMailInfoDTORespWithCert) {
				bodySubject.append(Language.getFormatResWebFire(IWebLogMessages.LOG_CTV008, new Object[ ] { mailInfoDTO.getSubjectCertificate(), mailInfoDTO.getDateCertExpired()}));
				bodySubject.append("\n"); //$NON-NLS-1$
			}

			final String msgEmailSucces = Language.getFormatResWebFire(IWebLogMessages.LOG_CTV019, new Object[ ] { certificate.getCertificateName(), Arrays.stream(addresses).map(Address::toString).collect(Collectors.joining(", ")) }); //$NON-NLS-1$

			// Enviamos el correo
			this.senderService.sendEmail(addresses, subject, bodySubject, msgEmailSucces, MailSenderService.MAIL_TEXT_PLAIN_CHARSET);
		}
	}

	/**
	 * Sends an email to the responsible parties for an invalid certificate.
	 *
	 * This method filters the list of responsible parties to get those associated with the provided certificate
	 * and sends an email with details about the certificate that is no longer valid.
	 *
	 * @param listMailInfoDTOResponsible List of {@link MailInfoDTO} objects containing information about the responsible parties and their certificates.
	 * @param certificate {@link Certificate} object containing information about the invalid certificate.
	 */
	private void sendEmailToResponsiblesForCertNotValid(final List<MailInfoDTO> listMailInfoDTOResponsible,
			final Certificate certificate) {
		final List<MailInfoDTO> listMailInfoDTORespWithCert = listMailInfoDTOResponsible.stream().filter(p -> p.getIdCertificado().equals(certificate.getIdCertificado())).collect(Collectors.toList());

		if(listMailInfoDTORespWithCert != null && !listMailInfoDTORespWithCert.isEmpty()) {

			final Address[] addresses = listMailInfoDTORespWithCert.stream()
				    .map(MailInfoDTO::getEmailResponsible)
				    .distinct()
				    .map(email -> {
				        try {
				            return new InternetAddress(email);
				        } catch (final Exception e) {
				            throw new RuntimeException(e);
				        }
				    })
				    .toArray(InternetAddress[]::new);

			final String subject = Language.getResWebFire(IWebLogMessages.LOG_CTV009);

			final StringBuilder bodySubject = new StringBuilder();

			for (final MailInfoDTO mailInfoDTO : listMailInfoDTORespWithCert) {
				bodySubject.append(Language.getFormatResWebFire(IWebLogMessages.LOG_CTV010, new Object[ ] { mailInfoDTO.getSubjectCertificate(), mailInfoDTO.getDateCertExpired()}));
				bodySubject.append("\n"); //$NON-NLS-1$
			}

			final String msgEmailSucces = Language.getFormatResWebFire(IWebLogMessages.LOG_CTV018, new Object[ ] { certificate.getCertificateName(), Arrays.stream(addresses).map(Address::toString).collect(Collectors.joining(", ")) }); //$NON-NLS-1$

			// Enviamos el correo
			this.senderService.sendEmail(addresses, subject, bodySubject, msgEmailSucces, MailSenderService.MAIL_TEXT_PLAIN_CHARSET);
		}
	}

	/**
	 * Retrieves the email addresses of all users who have an administrator role.
	 *
	 * @return An array of {@link Address} objects representing the email addresses of administrators.
	 */
	private static Address[] obtainUsersAdmin() {
		final Address[] addresses = ApplicationContextProvider.getApplicationContext()
			    .getBean(UserRepository.class)
			    .findAll().stream()
			    .filter(p -> p.getRol().getRolId().equals(NumberConstants.NUM_1_LONG))
			    .map(user -> {
			        try {
			            return new InternetAddress(user.getEmail());
			        } catch (final Exception e) {
			            throw new RuntimeException(e);
			        }
			    })
			    .toArray(InternetAddress[]::new);
		return addresses;
	}

	/**
	 * Checks if a period of communication is defined based on the scheduler configuration.
	 *
	 * @param periodCommunication The current status of period communication (true/false).
	 * @param scheduler           The scheduler containing configuration details.
	 * @return {@code true} if a period of communication exists, otherwise {@code false}.
	 */
	private static boolean isExistPeriodCommunication(final boolean periodCommunication, final Scheduler scheduler) {

		boolean defined = periodCommunication;
		// Si existe un periodo de comunicacion expresado en dias lo obtenemos
		if(scheduler.getPeriodCommunication() != null && !scheduler.getPeriodCommunication().equals(NumberConstants.NUM_0_LONG)) {
			defined = true;
		} else {
			LOGGER.warn(Language.getResWebFire(IWebLogMessages.LOG_CTV004));
		}
		return defined;
	}

	/**
	 * Retrieves the scheduler configuration and calculates the future expiration date
	 * based on the advance notice period.
	 *
	 * @return A {@link Scheduler} object containing the scheduling configuration.
	 */
	private Scheduler obtainDaysCloseToExpiry() {
		// Obtenemos el scheduler para la programacion de la tarea de valiacion
		final Scheduler scheduler = ApplicationContextProvider.getApplicationContext().getBean(SchedulerService.class).getSchedulerById(NumberConstants.NUM_1_LONG);

		// Si hay dias de preaviso configurado, obtenemos la fecha actual + dias de preaviso
		if(scheduler.getAdvanceNotice() != null && !scheduler.getAdvanceNotice().equals(NumberConstants.NUM_0_LONG)) {
			this.calculateDaysCloseToExpiry  = true;
			final Calendar futureCal = Calendar.getInstance();
			futureCal.add(Calendar.DAY_OF_YEAR, scheduler.getAdvanceNotice().intValue());
			this.futureDateCertExpired = futureCal.getTime();
		} else {
			LOGGER.warn(Language.getResWebFire(IWebLogMessages.LOG_CTV003));
		}
		return scheduler;
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.quartz.task.FireTask#endMessage()
	 */
	@Override
	protected void endMessage() {
		LOGGER.info(Language.getResWebFire(IWebLogMessages.LOG_CTV002));
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.quartz.task.FireTask#prepareParametersForTheTask()
	 */
	@Override
	protected void prepareParametersForTheTask(final Map<String, Object> dataMap) throws FireTaskException {
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.quartz.task.FireTask#getDataResult()
	 */
	@Override
	protected Map<String, Object> getDataResult() throws FireTaskException {
		// TODO Auto-generated method stub
		return null;
	}

}
