package es.gob.fire.control.tasks;

import java.util.ArrayList;
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
import es.gob.fire.persistence.repository.SchedulerRepository;
import es.gob.fire.persistence.repository.UserRepository;
import es.gob.fire.quartz.job.FireTaskException;
import es.gob.fire.quartz.task.FireTask;
import es.gob.fire.service.impl.SchedulerService;
import es.gob.fire.spring.config.ApplicationContextProvider;

public class TaskVerifyCertExpired extends FireTask {

	/**
	 * Attribute that represents the object that manages the log of the class.
	 */
	private static final Logger LOGGER = Logger.getLogger(TaskVerifyCertExpired.class);
	
	@Override
	protected void initialMessage() {
		Language.getResWebFire(IWebLogMessages.LOG_CTV001);
	}

	@Transactional
	@Override
	protected void doActionOfTheTask() throws Exception {
		List<Certificate> listCertificateNotYedValid = new ArrayList<Certificate>();
		List<Certificate> listCertificateExpired = new ArrayList<Certificate>();
		List<Certificate> listCertificateExpDaysAdvanceNotice = new ArrayList<Certificate>();
		boolean calculateAdavanceNotice = false;
		boolean periodCommunication = false;
		boolean sendEmailByPeriodComm = false;
		Date futureDateCertExpired = null;
		Date dateNow = Calendar.getInstance().getTime();
		
		// Obtenemos el scheduler para ver los dias de preaviso configurados 
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
			// Si han transcurrido mas de x dias desde la ultima fecha de comunicacion hasta ahora, deberemos enviar correo a los responsables para aquellos
			// certificados que esten proximos a caducar
			if(null == scheduler.getDateLastCommunication() || TimeUnit.DAYS.convert((dateNow.getTime() - scheduler.getDateLastCommunication().getTime()), TimeUnit.MILLISECONDS) 
					> scheduler.getPeriodCommunication()){
				sendEmailByPeriodComm = true;
			}
		} else {
			LOGGER.warn(Language.getResWebFire(IWebLogMessages.LOG_CTV004));
		}
		
		// Obtenemos una lista de todos los responsables que tienen asociados una app y un certificado
		List<MailInfoDTO> listMailInfoDTOResponsible =  ApplicationContextProvider.getApplicationContext().getBean(UserRepository.class).obtainAllCertWithAppAndResposible();
		
		// Para cada certificado evaluaremos si esta caducado, aun no valido o va a caducar en funcion de los dias de preaviso configurados
		List<Certificate> listCertificate = ApplicationContextProvider.getApplicationContext().getBean(CertificateRepository.class).findAll();
		for (Certificate certificate : listCertificate) {
			Date expDate = certificate.getFechaCaducidad();
			Date startDate = certificate.getFechaInicio();
			if (dateNow.before(startDate)) {
			    // El certificado aún no es válido
				listCertificateNotYedValid.add(certificate);
				sendEmailWithCertNotValidToResponsible(listMailInfoDTOResponsible, certificate);
			} else if (dateNow.after(expDate)) {
			    // El certificado está caducado
				listCertificateExpired.add(certificate);
				sendEmailWithCertExpiredToResponsible(listMailInfoDTOResponsible, certificate);
			} else {
			    // Si hay dias de preaviso configurados evaluaremos la validez del certificado
			    if(calculateAdavanceNotice) {
			    	// Verificamos si el certificado caduca en los días de preaviso configurados
			        if (!expDate.after(futureDateCertExpired)) {
			        	listCertificateExpDaysAdvanceNotice.add(certificate);
			        	// Si el periodo de comunicacion esta establecido y hace mas de x dias que se envio el ultimo correo de comunicacion enviamos el email
			        	if(periodCommunication && sendEmailByPeriodComm) {
			        		sendEmailWithCertCloseToExpiryForResponsibles(listMailInfoDTOResponsible, certificate);
			        		//Actualizamos la fecha de ultima comunicacion del scheduler
			        		scheduler.setDateLastCommunication(dateNow);
			        		ApplicationContextProvider.getApplicationContext().getBean(SchedulerRepository.class).save(scheduler);
			        	}
			        }
			    }
			}
		}
		
		// Ahora enviaremos los mails a los administradores
		sendEmailWithDiffCertStatus(listCertificateNotYedValid, listCertificateExpired, listCertificateExpDaysAdvanceNotice);
	}

	private void sendEmailWithDiffCertStatus(List<Certificate> listCertificateNotYedValid,
			List<Certificate> listCertificateExpired, List<Certificate> listCertificateExpDaysAdvanceNotice) {
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
		
		ApplicationContextProvider.getApplicationContext().getBean(MailSenderService.class).sendEmail(addresses,subject,bodySubject);
	}

	private void sendEmailWithCertCloseToExpiryForResponsibles(List<MailInfoDTO> listMailInfoDTOResponsible,
			Certificate certificate) {
		List<MailInfoDTO> listMailInfoDTORespWithCert = listMailInfoDTOResponsible.stream().filter(p -> p.getIdCertificado().equals(certificate.getIdCertificado())).collect(Collectors.toList());
		
		if(listMailInfoDTORespWithCert != null) {
			
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
			
			ApplicationContextProvider.getApplicationContext().getBean(MailSenderService.class).sendEmail(addresses,subject,bodySubject);
		}
	}

	private void sendEmailWithCertExpiredToResponsible(List<MailInfoDTO> listMailInfoDTOResponsible,
			Certificate certificate) {
		List<MailInfoDTO> listMailInfoDTORespWithCert = listMailInfoDTOResponsible.stream().filter(p -> p.getIdCertificado().equals(certificate.getIdCertificado())).collect(Collectors.toList());
		
		if(listMailInfoDTORespWithCert != null) {
			
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
			
			ApplicationContextProvider.getApplicationContext().getBean(MailSenderService.class).sendEmail(addresses,subject,bodySubject);
		}
	}

	private void sendEmailWithCertNotValidToResponsible(List<MailInfoDTO> listMailInfoDTOResponsible,
			Certificate certificate) {
		List<MailInfoDTO> listMailInfoDTORespWithCert = listMailInfoDTOResponsible.stream().filter(p -> p.getIdCertificado().equals(certificate.getIdCertificado())).collect(Collectors.toList());
		
		if(listMailInfoDTORespWithCert != null) {
			
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
			
			ApplicationContextProvider.getApplicationContext().getBean(MailSenderService.class).sendEmail(addresses,subject,bodySubject);
		}
	}

	@Override
	protected void endMessage() {
		Language.getResWebFire(IWebLogMessages.LOG_CTV002);
	}

	@Override
	protected void prepareParametersForTheTask(Map<String, Object> dataMap) throws FireTaskException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected Map<String, Object> getDataResult() throws FireTaskException {
		// TODO Auto-generated method stub
		return null;
	}

}
