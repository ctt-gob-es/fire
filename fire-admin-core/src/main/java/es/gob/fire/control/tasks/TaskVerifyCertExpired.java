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
 * <b>File:</b><p>es.gob.fire.control.tasks.TaskVerifyCertExpired.java.</p>
 * <b>Description:</b><p>Class that performs a task for updated status certificate X509 and send emails to users with differents role.</p>
 * for all the scheduler task classes in FIRe.</p>
 * <b>Project:</b><p>Horizontal platform of validation services of multiPKI certificates and electronic signature.</p>
 * <b>Date:</b><p>12/02/2025.</p>
 * @author Gobierno de España.
 * @version 1.2, 25/02/2025.
 */
package es.gob.fire.control.tasks;

import java.io.ByteArrayInputStream;
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

import org.springframework.transaction.annotation.Transactional;

import es.gob.fire.commons.log.Logger;
import es.gob.fire.commons.utils.Base64;
import es.gob.fire.commons.utils.NumberConstants;
import es.gob.fire.commons.utils.Utils;
import es.gob.fire.i18n.IWebLogMessages;
import es.gob.fire.i18n.Language;
import es.gob.fire.mail.MailSenderService;
import es.gob.fire.persistence.dto.MailInfoDTO;
import es.gob.fire.persistence.entity.Certificate;
import es.gob.fire.persistence.entity.CertificatesApplication;
import es.gob.fire.persistence.entity.Scheduler;
import es.gob.fire.persistence.repository.CertificateRepository;
import es.gob.fire.persistence.repository.CertificatesApplicationRepository;
import es.gob.fire.persistence.repository.UserRepository;
import es.gob.fire.persistence.service.ICertificateService;
import es.gob.fire.persistence.service.impl.CertificateService;
import es.gob.fire.quartz.job.FireTaskException;
import es.gob.fire.quartz.task.FireTask;
import es.gob.fire.service.impl.SchedulerService;
import es.gob.fire.spring.config.ApplicationContextProvider;

/**
 * <p>Class that performs a task for updated status certificate X509 and send emails to users with differents role.</p>
 * <b>Project:</b><p>Horizontal platform of validation services of multiPKI
 * certificates and electronic signature.</p>
 * @version 1.2, 25/02/2025.
 */
public class TaskVerifyCertExpired extends FireTask {

	/**
	 * Attribute that represents the object that manages the log of the class.
	 */
	private static final Logger LOGGER = Logger.getLogger(TaskVerifyCertExpired.class);
	
	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.quartz.task.FireTask#initialMessage()
	 */
	@Override
	protected void initialMessage() {
		LOGGER.info(Language.getResWebFire(IWebLogMessages.LOG_CTV001));
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.quartz.task.FireTask#doActionOfTheTask()
	 */
	@Transactional
	@Override
	protected void doActionOfTheTask() throws Exception {
		List<Certificate> listCertificateNotYedValid = new ArrayList<Certificate>();
		List<Certificate> listCertificateExpired = new ArrayList<Certificate>();
		List<Certificate> listCertificateExpDaysAdvanceNotice = new ArrayList<Certificate>();
		boolean calculateAdavanceNotice = false;
		boolean periodCommunication = false;
		Date futureDateCertExpired = null;
		Date dateNow = Calendar.getInstance().getTime();
		
		// Obtenemos el scheduler para la programacion de la tarea de valiacion
		Scheduler scheduler = ApplicationContextProvider.getApplicationContext().getBean(SchedulerService.class).getSchedulerById(NumberConstants.NUM_1_LONG);
		
		// Si hay dias de preaviso configurado, obtenemos la fecha actual + días de preaviso
		if(scheduler.getAdvanceNotice() != null && !scheduler.getAdvanceNotice().equals(NumberConstants.NUM_0_LONG)) {
			calculateAdavanceNotice  = true;
			Calendar futureCal = Calendar.getInstance();
			futureCal.add(Calendar.DAY_OF_YEAR, scheduler.getAdvanceNotice().intValue());
			futureDateCertExpired = futureCal.getTime();
		} else {
			LOGGER.warn(Language.getResWebFire(IWebLogMessages.LOG_CTV003));
		}
		
		// Si existe un periodo de comunicacion expresado en dias lo obtenemos
		if(scheduler.getPeriodCommunication() != null && !scheduler.getPeriodCommunication().equals(NumberConstants.NUM_0_LONG)) {
			periodCommunication = true;
		} else {
			LOGGER.warn(Language.getResWebFire(IWebLogMessages.LOG_CTV004));
		}
		
		// Obtenemos una lista de todos los responsables que tienen asociados una app y un certificado
		List<MailInfoDTO> listMailInfoDTOResponsible =  ApplicationContextProvider.getApplicationContext().getBean(UserRepository.class).obtainAllCertWithAppAndResposible();
		
		// Para cada certificado evaluaremos si esta caducado, aun no valido o va a caducar en funcion de los dias de preaviso configurados
		List<Certificate> listCertificate = ApplicationContextProvider.getApplicationContext().getBean(CertificateRepository.class).findAll();
		for (Certificate certificate : listCertificate) {
			
			// Obtenemos a partir de la factoria de java un certificado X509
			X509Certificate x509Certificate = (X509Certificate) CertificateFactory.getInstance(CertificateService.X509).generateCertificate(new ByteArrayInputStream(Base64.decode(certificate.getCertificate()))); //$NON-NLS-1$
			
			// Chequeamos la validez del certificado
			try {
				x509Certificate.checkValidity();
				// Cuando el certificado es valido evaluamos la caducidad en base a dias de preaviso y periodo de comunicacion
				if(calculateAdavanceNotice) {
					// Verificamos si el certificado caduca en los días de preaviso configurados
			        if (!x509Certificate.getNotAfter().after(futureDateCertExpired)) {
			        	listCertificateExpDaysAdvanceNotice.add(certificate);
			        	if(periodCommunication) {
			        		// Enviaremos email al responsable cuando:
				        	//		- No haya fecha de ultima comunicacion
				        	//		- La diferencia de dias entre la fecha actual y la fecha de la ultima comunicacion sea mayor o igual que el numero de dias establecidos para el periodo de comunicacion
				        	//		- La diferencia de dias entre la fecha de caducidad y la fecha actual sea menor o igual que el numero de dias establecidos para el periodo de comunicacion
			        		if(certificate.getDateLastCommunication() == null ) {
			        			sendEmailToResponsiblesForCertCloseToExpiry(listMailInfoDTOResponsible, certificate);
				        		certificate.setDateLastCommunication(dateNow);
			        		} else {
			        			// Obtenemos la diferencia de dias entre la fecha actual y la fecha de la ultima comunicacion
					        	Long diffDaysBetweenDNandDLC = TimeUnit.DAYS.convert((dateNow.getTime() - certificate.getDateLastCommunication().getTime()), TimeUnit.MILLISECONDS); 
								// Obtenemos la diferencia de dias entre la fecha actual y la fecha de caducidad
					        	Long diffDaysBetweenDEandDLC = TimeUnit.DAYS.convert((x509Certificate.getNotAfter().getTime() - dateNow.getTime()), TimeUnit.MILLISECONDS); 
					        	if(diffDaysBetweenDNandDLC >= scheduler.getPeriodCommunication() || diffDaysBetweenDEandDLC <= scheduler.getPeriodCommunication()) {
					        		sendEmailToResponsiblesForCertCloseToExpiry(listMailInfoDTOResponsible, certificate);
					        		certificate.setDateLastCommunication(dateNow);
					        	}
			        		}
			        		
			        	}
			        }
				}
			} catch (final CertificateExpiredException e) {
				 // El certificado está caducado
				listCertificateExpired.add(certificate);
				certificate.setDateLastCommunication(dateNow);
				sendEmailToResponsiblesForCertExpired(listMailInfoDTOResponsible, certificate);
			} catch (final CertificateNotYetValidException e) {
				// El certificado aún no es válido
				listCertificateNotYedValid.add(certificate);
				certificate.setDateLastCommunication(dateNow);
				sendEmailToResponsiblesForCertNotValid(listMailInfoDTOResponsible, certificate);
			}
			
			// Actualizamos los campos del certificados
			ApplicationContextProvider.getApplicationContext().getBean(ICertificateService.class).updateCertificateFromTaskValidation(certificate, x509Certificate);
		}
		
		// Ahora enviaremos los mails a los administradores
		sendEmailWithDiffCertStatus(listCertificateNotYedValid, listCertificateExpired, listCertificateExpDaysAdvanceNotice);
	}

	/**
	 * Sends an email notification regarding certificates with different statuses:
	 * not yet valid, expired, or close to expiration.
	 *
	 * @param listCertificateNotYedValid         	List of certificates that are not yet valid.
	 * @param listCertificateExpired             	List of expired certificates.
	 * @param listCertificateExpDaysAdvanceNotice 	List of certificates nearing expiration.
	 */
	private void sendEmailWithDiffCertStatus(List<Certificate> listCertificateNotYedValid,
			List<Certificate> listCertificateExpired, List<Certificate> listCertificateExpDaysAdvanceNotice) {
		
		// Solo enviaremos correo de notificacion a los administradores si hay algun certificado en estado: aun no valido, caducado o proximo a caducar
		if(!listCertificateNotYedValid.isEmpty() || !listCertificateExpired.isEmpty() || !listCertificateExpDaysAdvanceNotice.isEmpty()) {
			// Obtenemos los destinatarios
			Address[] addresses = ApplicationContextProvider.getApplicationContext()
				    .getBean(UserRepository.class)
				    .findAll().stream()
				    .filter(p -> p.getRol().getRolId().equals(NumberConstants.NUM_1_LONG))
				    .map(user -> {
				        try {
				            return new InternetAddress(user.getEmail());
				        } catch (Exception e) {
				            throw new RuntimeException(e);
				        }
				    })
				    .toArray(InternetAddress[]::new);
			
			String subject = Language.getResWebFire(IWebLogMessages.LOG_CTV011);
			
			StringBuilder bodySubject = new StringBuilder();
			
			bodySubject.append(Language.getResWebFire(IWebLogMessages.LOG_CTV012));
			bodySubject.append("\n");
			bodySubject.append("\n");
			
			List<CertificatesApplication> listCertificatesApplication = ApplicationContextProvider.getApplicationContext().getBean(CertificatesApplicationRepository.class).findAllWithCertificateAndApplication();

			if(!listCertificateNotYedValid.isEmpty()) {
				
				bodySubject.append(Language.getFormatResWebFire(IWebLogMessages.LOG_CTV013, new Object[ ] { listCertificateNotYedValid.size()}));
				bodySubject.append("\n");
				
				for (Certificate certificateNotYedValid : listCertificateNotYedValid) {
					String appNames = listCertificatesApplication.stream()
						    .filter(p -> p.getCertificate().getIdCertificado().equals(certificateNotYedValid.getIdCertificado()))
						    .map(p -> p.getApplication().getAppName())
						    .distinct()
						    .collect(Collectors.collectingAndThen(Collectors.joining(", "), result -> (result == null || result.isEmpty()) ? "N/A" : result));
					bodySubject.append(Language.getFormatResWebFire(IWebLogMessages.LOG_CTV014, new Object[ ] { certificateNotYedValid.getCertificateName(), certificateNotYedValid.getSubject(), Utils.getStringDateFormat(certificateNotYedValid.getFechaInicio()), appNames}));
					bodySubject.append("\n");
				}
			}
			
			if(!listCertificateExpired.isEmpty()) {
				
				bodySubject.append("\n");
				bodySubject.append("\n");
				bodySubject.append(Language.getFormatResWebFire(IWebLogMessages.LOG_CTV015, new Object[ ] { listCertificateExpired.size()}));
				bodySubject.append("\n");
				
				for (Certificate certificateExpired : listCertificateExpired) {
					String appNames = listCertificatesApplication.stream()
						    .filter(p -> p.getCertificate().getIdCertificado().equals(certificateExpired.getIdCertificado()))
						    .map(p -> p.getApplication().getAppName())
						    .distinct()
						    .collect(Collectors.collectingAndThen(Collectors.joining(", "), result -> (result == null || result.isEmpty()) ? "N/A" : result));
					bodySubject.append(Language.getFormatResWebFire(IWebLogMessages.LOG_CTV016, new Object[ ] { certificateExpired.getCertificateName(), certificateExpired.getSubject(), Utils.getStringDateFormat(certificateExpired.getFechaCaducidad()), appNames}));
					bodySubject.append("\n");
				}
			}
			
			if(!listCertificateExpDaysAdvanceNotice.isEmpty()) {
				
				bodySubject.append("\n");
				bodySubject.append("\n");
				bodySubject.append(Language.getFormatResWebFire(IWebLogMessages.LOG_CTV017, new Object[ ] { listCertificateExpDaysAdvanceNotice.size()}));
				bodySubject.append("\n");
				
				for (Certificate certificateCloseToExpired : listCertificateExpDaysAdvanceNotice) {
					String appNames = listCertificatesApplication.stream()
							.filter(p -> p.getCertificate().getIdCertificado().equals(certificateCloseToExpired.getIdCertificado()))
							.map(p -> p.getApplication().getAppName())
							.distinct()
							.collect(Collectors.collectingAndThen(Collectors.joining(", "), result -> (result == null || result.isEmpty()) ? "N/A" : result));
					bodySubject.append(Language.getFormatResWebFire(IWebLogMessages.LOG_CTV016, new Object[ ] { certificateCloseToExpired.getCertificateName(), certificateCloseToExpired.getSubject(), Utils.getStringDateFormat(certificateCloseToExpired.getFechaCaducidad()), appNames}));
					bodySubject.append("\n");
				}
			}
			
			String msgEmailSucces = Language.getFormatResWebFire(IWebLogMessages.LOG_CTV021, new Object[ ] { Arrays.stream(addresses).map(Address::toString).collect(Collectors.joining(", ")) });
			
			// Configuramos las propiedades de Java Mail y enviamos el correo
			ApplicationContextProvider.getApplicationContext().getBean(MailSenderService.class).confJavaMailFromFromEnv();
			ApplicationContextProvider.getApplicationContext().getBean(MailSenderService.class).sendEmail(addresses,subject,bodySubject, msgEmailSucces);
		}
		
	}

	/**
	 * Sends an email notification to responsible users for a certificate 
	 * that is nearing its expiration date.
	 *
	 * @param listMailInfoDTOResponsible List of responsible users with certificate details.
	 * @param certificate                The certificate that is close to expiration.
	 */
	private void sendEmailToResponsiblesForCertCloseToExpiry(List<MailInfoDTO> listMailInfoDTOResponsible,
			Certificate certificate) {
		List<MailInfoDTO> listMailInfoDTORespWithCert = listMailInfoDTOResponsible.stream().filter(p -> p.getIdCertificado().equals(certificate.getIdCertificado())).collect(Collectors.toList());
		
		if(listMailInfoDTORespWithCert != null && !listMailInfoDTORespWithCert.isEmpty()) {
			
			Address[] addresses = listMailInfoDTORespWithCert.stream()
				    .map(MailInfoDTO::getEmailResponsible)
				    .distinct()
				    .map(email -> {
				        try {
				            return new InternetAddress(email);
				        } catch (Exception e) {
				            throw new RuntimeException(e);
				        }
				    })
				    .toArray(InternetAddress[]::new);
			
			String subject = Language.getResWebFire(IWebLogMessages.LOG_CTV005);
			
			StringBuilder bodySubject = new StringBuilder();
			
			for (MailInfoDTO mailInfoDTO : listMailInfoDTORespWithCert) {
				bodySubject.append(Language.getFormatResWebFire(IWebLogMessages.LOG_CTV006, new Object[ ] { mailInfoDTO.getSubjectCertificate(), mailInfoDTO.getDateCertExpired()}));
				bodySubject.append("\n");
			}
			
			String msgEmailSucces = Language.getFormatResWebFire(IWebLogMessages.LOG_CTV020, new Object[ ] { certificate.getCertificateName(), Arrays.stream(addresses).map(Address::toString).collect(Collectors.joining(", ")) });
			
			// Configuramos las propiedades de Java Mail y enviamos el correo
			ApplicationContextProvider.getApplicationContext().getBean(MailSenderService.class).confJavaMailFromFromEnv();
			ApplicationContextProvider.getApplicationContext().getBean(MailSenderService.class).sendEmail(addresses,subject,bodySubject, msgEmailSucces);
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
	private void sendEmailToResponsiblesForCertExpired(List<MailInfoDTO> listMailInfoDTOResponsible,
			Certificate certificate) {
		List<MailInfoDTO> listMailInfoDTORespWithCert = listMailInfoDTOResponsible.stream().filter(p -> p.getIdCertificado().equals(certificate.getIdCertificado())).collect(Collectors.toList());
		
		if(listMailInfoDTORespWithCert != null && !listMailInfoDTORespWithCert.isEmpty()) {
			
			Address[] addresses = listMailInfoDTORespWithCert.stream()
				    .map(MailInfoDTO::getEmailResponsible)
				    .distinct()
				    .map(email -> {
				        try {
				            return new InternetAddress(email);
				        } catch (Exception e) {
				            throw new RuntimeException(e);
				        }
				    })
				    .toArray(InternetAddress[]::new);
			
			String subject = Language.getResWebFire(IWebLogMessages.LOG_CTV007);
			
			StringBuilder bodySubject = new StringBuilder();
			
			for (MailInfoDTO mailInfoDTO : listMailInfoDTORespWithCert) {
				bodySubject.append(Language.getFormatResWebFire(IWebLogMessages.LOG_CTV008, new Object[ ] { mailInfoDTO.getSubjectCertificate(), mailInfoDTO.getDateCertExpired()}));
				bodySubject.append("\n");
			}
			
			String msgEmailSucces = Language.getFormatResWebFire(IWebLogMessages.LOG_CTV019, new Object[ ] { certificate.getCertificateName(), Arrays.stream(addresses).map(Address::toString).collect(Collectors.joining(", ")) });
			
			// Configuramos las propiedades de Java Mail y enviamos el correo
			ApplicationContextProvider.getApplicationContext().getBean(MailSenderService.class).confJavaMailFromFromEnv();
			ApplicationContextProvider.getApplicationContext().getBean(MailSenderService.class).sendEmail(addresses,subject,bodySubject, msgEmailSucces);
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
	private void sendEmailToResponsiblesForCertNotValid(List<MailInfoDTO> listMailInfoDTOResponsible,
			Certificate certificate) {
		List<MailInfoDTO> listMailInfoDTORespWithCert = listMailInfoDTOResponsible.stream().filter(p -> p.getIdCertificado().equals(certificate.getIdCertificado())).collect(Collectors.toList());
		
		if(listMailInfoDTORespWithCert != null && !listMailInfoDTORespWithCert.isEmpty()) {
			
			Address[] addresses = listMailInfoDTORespWithCert.stream()
				    .map(MailInfoDTO::getEmailResponsible)
				    .distinct()
				    .map(email -> {
				        try {
				            return new InternetAddress(email);
				        } catch (Exception e) {
				            throw new RuntimeException(e);
				        }
				    })
				    .toArray(InternetAddress[]::new);
			
			String subject = Language.getResWebFire(IWebLogMessages.LOG_CTV009);
			
			StringBuilder bodySubject = new StringBuilder();
			
			for (MailInfoDTO mailInfoDTO : listMailInfoDTORespWithCert) {
				bodySubject.append(Language.getFormatResWebFire(IWebLogMessages.LOG_CTV010, new Object[ ] { mailInfoDTO.getSubjectCertificate(), mailInfoDTO.getDateCertExpired()}));
				bodySubject.append("\n");
			}
			
			String msgEmailSucces = Language.getFormatResWebFire(IWebLogMessages.LOG_CTV018, new Object[ ] { certificate.getCertificateName(), Arrays.stream(addresses).map(Address::toString).collect(Collectors.joining(", ")) });
			
			// Configuramos las propiedades de Java Mail y enviamos el correo
			ApplicationContextProvider.getApplicationContext().getBean(MailSenderService.class).confJavaMailFromFromEnv();
			ApplicationContextProvider.getApplicationContext().getBean(MailSenderService.class).sendEmail(addresses,subject,bodySubject, msgEmailSucces);
		}
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
	protected void prepareParametersForTheTask(Map<String, Object> dataMap) throws FireTaskException {
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
