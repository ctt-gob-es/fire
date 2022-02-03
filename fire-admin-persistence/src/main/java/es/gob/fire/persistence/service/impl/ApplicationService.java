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
 * <b>File:</b><p>es.gob.fire.persistence.service.ApplicationService.java.</p>
 * <b>Description:</b><p>Class that implements the communication with the operations of the persistence layer.</p>
  * <b>Project:</b><p>Application for signing documents of @firma suite systems</p>
 * <b>Date:</b><p>22/01/2021.</p>
 * @author Gobierno de Espa&ntilde;a.
 * @version 1.1, 02/02/2022.
 */
package es.gob.fire.persistence.service.impl;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.transaction.Transactional;

import es.gob.fire.commons.log.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonView;

import es.gob.fire.commons.utils.Hexify;
import es.gob.fire.commons.utils.NumberConstants;
import es.gob.fire.commons.utils.UtilsStringChar;
import es.gob.fire.persistence.dto.ApplicationCertDTO;
import es.gob.fire.persistence.dto.ApplicationDTO;
import es.gob.fire.persistence.entity.Application;
import es.gob.fire.persistence.entity.ApplicationResponsible;
import es.gob.fire.persistence.entity.ApplicationResponsiblePK;
import es.gob.fire.persistence.entity.Certificate;
import es.gob.fire.persistence.entity.User;
import es.gob.fire.persistence.repository.ApplicationRepository;
import es.gob.fire.persistence.repository.ApplicationResponsibleRepository;
import es.gob.fire.persistence.repository.CertificateRepository;
import es.gob.fire.persistence.repository.UserRepository;
import es.gob.fire.persistence.repository.datatable.ApplicationDataTablesRepository;
import es.gob.fire.persistence.service.IApplicationService;

/**
 * <p>Class that implements the communication with the operations of the persistence layer.</p>
 * <b>Project:</b><p>Platform for detection and validation of certificates recognized in European TSL.</p>
 * @version 1.1, 02/02/2022.
 */
@Service
public class ApplicationService implements IApplicationService{

	/**
	 * Attribute that represents the object that manages the log of the class.
	 */
	private static final Logger LOGGER = Logger.getLogger(ApplicationService.class);

	/**
	 *
	 */
	private static final String HMAC_ALGORITHM = "HmacMD5";

	/**
	 * Attribute that represents the injected interface that proves CRUD operations for the persistence.
	 */
	@Autowired
	private ApplicationRepository repository;

	/**
	 * Attribute that represents the injected interface that proves CRUD operations for the persistence.
	 */
	@Autowired
	private ApplicationResponsibleRepository appRespRepository;

	/**
	 * Attribute that represents the injected interface that proves CRUD operations for the persistence.
	 */
	@Autowired
	private CertificateRepository certRepository;

	/**
	 * Attribute that represents the injected interface that proves CRUD operations for the persistence.
	 */
	@Autowired
	private UserRepository userRepository;

	/**
	 * Attribute that represents the injected interface that provides CRUD operations for the persistence.
	 */
	@Autowired
	private ApplicationDataTablesRepository appdtRepository;

	/* (non-Javadoc)
	 * @see es.gob.fire.persistence.service.IApplicationService#getAppByAppId(java.lang.String)
	 */
	@Override
	public Application getAppByAppId(final String appId) {
		return this.repository.findByAppId(appId);
	}

	/* (non-Javadoc)
	 * @see es.gob.fire.persistence.service.IApplicationService#getAppByAppName(java.lang.String)
	 */
	@Override
	public Application getAppByAppName(final String appName) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see es.gob.fire.persistence.service.IApplicationService#saveApplication(es.gob.fire.persistence.dto.ApplicationDTO, java.util.List)
	 */
	@Override
	@Transactional
	public Application saveApplication(final ApplicationDTO appDto, final List<Long> idsUsers) throws GeneralSecurityException{

		Application appToSave = null;

		// Nueva aplicacion
		if (appDto.getAppId().isEmpty() || appDto.getAppId() == null) {
			appToSave = applicationDtoToEntity(appDto);
			appToSave.setAppId(generateId());
			appToSave.setFechaAltaApp(new Date());
		// Aplicacion existente
		} else {
			appToSave = this.repository.findByAppId(appDto.getAppId());
			appToSave.setAppName(appDto.getAppName());
			if (appDto.getIdCertificado() != null) {
				final Certificate cert = this.certRepository.findByIdCertificado(appDto.getIdCertificado());
				appToSave.setCertificate(cert);
			}
			appToSave.setHabilitado(appDto.getHabilitado());

		}

		ApplicationResponsiblePK appRespPk = null;
		ApplicationResponsible appResp = null;
		final List<ApplicationResponsible> updated = new ArrayList<>();
		for (final Long idUser : idsUsers) {

			appResp = new ApplicationResponsible();
			appResp.setApplication(appToSave);
			appResp.setResponsible(this.userRepository.findByUserId(idUser));

			appRespPk = new ApplicationResponsiblePK();
			appRespPk.setIdApplication(appToSave.getAppId());
			appRespPk.setIdResponsible(idUser);
			appResp.setIdApplicationResponsible(appRespPk);

			//(appRespRepository.saveAndFlush(appResp);
			//listApplicationResponsible.add(appResp);
			updated.add(appResp);

			//appToSave.getListApplicationResponsible().add(appResp);
		}

		if (appToSave.getAppId() != null) {
			final List<ApplicationResponsible> current = appToSave.getListApplicationResponsible();

			current.removeIf(x->!updated.contains(x));

			for (final ApplicationResponsible ar : updated) {
				if (!current.contains(ar)) {
					current.add(ar);
				}
			}


		} else {
			appToSave.getListApplicationResponsible().addAll(updated);
		}

		//app.setListApplicationResponsible(listApplicationResponsible);
		final Application savedApp = this.repository.saveAndFlush(appToSave);

		return savedApp;
	}

	/**
	 * Genera un nuevo identificador de aplicaci&oacute;n.
	 * @return Identificador de aplicaci&oacute;n.
	 * @throws GeneralSecurityException Cuando no se puede generar un identificador.
	 */
	private static String generateId() throws GeneralSecurityException {

		Mac mac;
		try {
			final KeyGenerator kGen = KeyGenerator.getInstance(HMAC_ALGORITHM);
			final SecretKey hmacKey = kGen.generateKey();

			mac = Mac.getInstance(hmacKey.getAlgorithm());
			mac.init(hmacKey);
		}
		catch (final GeneralSecurityException e) {
			LOGGER.equals("No ha sido posible generar una clave aleatoria como identificador de aplicacion: " + e);
			throw e;
		}

		return Hexify.hexify(mac.doFinal(), "").substring(0, 12); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.services.IApplicationService#deleteAplication(java.lang.String)
	 */
	@Override
	public void deleteApplication(final String appId) {
		this.repository.deleteById(appId);
	}

	/* (non-Javadoc)
	 * @see es.gob.fire.persistence.service.IApplicationService#getAllApplication()
	 */
	@Override
	public List<Application> getAllApplication() {
		return this.repository.findAll();
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.services.IApplicationService#findAll(org.springframework.data.jpa.datatables.mapping.DataTablesInput)
	 */
	@Override
	public DataTablesOutput<Application> getAllApplication(final DataTablesInput input) {
		return this.appdtRepository.findAll(input);
	}

	/* (non-Javadoc)
	 * @see es.gob.fire.persistence.service.IApplicationService#applicationDtoToEntity(es.gob.fire.persistence.dto.ApplicationDTO)
	 */
	@Override
	public Application applicationDtoToEntity(final ApplicationDTO applicationDto) {

		final Application app = new Application();

		app.setAppId(applicationDto.getAppId());
		app.setAppName(applicationDto.getAppName());

		if (applicationDto.getIdCertificado() != null) {
			final Certificate cert = this.certRepository.findByIdCertificado(applicationDto.getIdCertificado());
			app.setCertificate(cert);
		}

		if (applicationDto.getAppId() != null && !"".equals(applicationDto.getAppId())) {

			final Application existingApp = this.repository.findByAppId(applicationDto.getAppId());
			// Se obtiene la lista de la aplicacion responsable
			app.setListApplicationResponsible(existingApp.getListApplicationResponsible());
		} else {

			app.setListApplicationResponsible(new ArrayList<ApplicationResponsible>());
		}

		app.setFechaAltaApp(applicationDto.getFechaAltaApp());
		app.setHabilitado(Boolean.TRUE);

		return app;
	}

	/* (non-Javadoc)
	 * @see es.gob.fire.persistence.service.IApplicationService#applicationEntityToDto(es.gob.fire.persistence.entity.Application)
	 */
	@Override
	public ApplicationDTO applicationEntityToDto(final Application application) {

		final ApplicationDTO appDto = new ApplicationDTO();

		appDto.setAppId(application.getAppId());
		appDto.setAppName(application.getAppName());
		appDto.setIdCertificado(application.getCertificate().getIdCertificado());
		appDto.setFechaAltaApp(application.getFechaAltaApp());
		appDto.setHabilitado(application.isHabilitado());

		return appDto;
	}

	/* (non-Javadoc)
	 * @see es.gob.fire.persistence.service.IApplicationService#getApplicationResponsibleByUserId(java.lang.Long)
	 */
	@Override
	public List<ApplicationResponsible> getApplicationResponsibleByUserId(final Long userId) {

		return this.appRespRepository.findByResponsibleUserId(userId);
	}

	/* (non-Javadoc)
	 * @see es.gob.fire.persistence.service.IApplicationService#getApplicationsCert(org.springframework.data.jpa.datatables.mapping.DataTablesInput, java.lang.Long)
	 */
	@Override
	@JsonView(DataTablesOutput.View.class)
	public DataTablesOutput<ApplicationCertDTO> getApplicationsCert(final DataTablesInput input, final Long idCertificate) {

		final List<ApplicationCertDTO> appsCert = this.repository.findApplicationCert(idCertificate);
		final DataTablesOutput<ApplicationCertDTO> dtOutput = new DataTablesOutput<>();

		for (final ApplicationCertDTO appCertDto : appsCert) {

			final List<ApplicationResponsible> appRespList = this.appRespRepository.findByApplicationAppId(appCertDto.getAppId());

			String responsiblesString = "";
			String nombreCompleto = "";
			for (final ApplicationResponsible appresp : appRespList) {

				if (!responsiblesString.isEmpty()) {
					responsiblesString += "</br>";
				}

				final User user = this.userRepository.findByUserId(appresp.getResponsible().getUserId());
				nombreCompleto = user.getName().concat(UtilsStringChar.SPECIAL_BLANK_SPACE_STRING).concat(user.getSurnames());
				responsiblesString += nombreCompleto;


			}

			appCertDto.setResponsables(responsiblesString);

		}

		dtOutput.setDraw(NumberConstants.NUM1);
		dtOutput.setRecordsFiltered(new Long(appsCert.size()));
		dtOutput.setRecordsTotal(new Long(appsCert.size()));
		dtOutput.setData(appsCert);

		return dtOutput;
	}

	/* (non-Javadoc)
	 * @see es.gob.fire.persistence.service.IApplicationService#getApplicationResponsibleByApprId(java.lang.String)
	 */
	@Override
	public List<ApplicationResponsible> getApplicationResponsibleByApprId(final String appId) {
		//
		return this.appRespRepository.findByApplicationAppId(appId);
	}

	/* (non-Javadoc)
	 * @see es.gob.fire.persistence.service.IApplicationService#getViewApplication(java.lang.String)
	 */
	@Override
	public ApplicationCertDTO getViewApplication(final String appId) {
		//
		return this.repository.findViewApplication(appId);
	}

	/* (non-Javadoc)
	 * @see es.gob.fire.persistence.service.IApplicationService#getByIdCertificado(java.lang.Long)
	 */
	@Override
	public List<Application> getByIdCertificado(final Long idCertificado) {

		return this.repository.findByCertificateIdCertificado(idCertificado);
	}

}
