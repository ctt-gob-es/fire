package es.gob.fire.persistence.service.impl;

import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import es.gob.fire.persistence.dto.CertificateDTO;
import es.gob.fire.persistence.dto.CertificateEditDTO;
import es.gob.fire.persistence.entity.Certificate;
import es.gob.fire.persistence.repository.CertificateRepository;
import es.gob.fire.persistence.repository.datatable.CertificateDataTablesRepository;
import es.gob.fire.persistence.service.ICertificateService;


@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class CertificateService implements ICertificateService{
	
	/**
	 * Attribute that represents the injected interface that proves CRUD operations for the persistence.
	 */
	@Autowired
	private CertificateRepository repository;
	
	
	/**
	 * Attribute that represents the injected interface that provides CRUD operations for the persistence.
	 */
	@Autowired
	private CertificateDataTablesRepository dtRepository;

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.services.ICertificateService#getCertificatetByCertificateId(java.lang.Long)
	 */
	@Override
	public Certificate getCertificateByCertificateId(Long idCertificado) {
		return repository.findByIdCertificado(idCertificado);
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.services.ICertificateService#saveCertificate(es.gob.fire.persistence.entity.Certificate)
	 */
	@Override
	public Certificate saveCertificate(Certificate certificate) {
		return repository.save(certificate);
	}
	
	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.service.ICertificateService#saveCertificate(es.gob.fire.persistence.configuration.dto.CertificateDTO)
	 */
//	@Override
//	@Transactional
//	public User saveUser(UserDTO userDto) {
//		User user = null;
//		if (userDto.getUserId() != null) {
//			user = repository.findByUserId(userDto.getUserId());
//		} else {
//			user = new User();
//		}
//		if (!StringUtils.isEmpty(userDto.getPassword())) {
//			String pwd = userDto.getPassword();
//			BCryptPasswordEncoder bcpe = new BCryptPasswordEncoder();
//			String hashPwd = bcpe.encode(pwd);
//
//			user.setPassword(hashPwd);
//		}
//		
//		user.setUserName(userDto.getLogin());
//		user.setName(userDto.getName());
//		user.setSurnames(userDto.getSurnames());
//		user.setEmail(userDto.getEmail());
//		user.setStartDate(new Date());
//		user.setRol(rolRepository.findByRolId(userDto.getRolId()));
//		user.setRenovationDate(new Date());
//		user.setRoot(Boolean.FALSE);
//		user.setPhone(userDto.getTelf());
//		//TODO Rellenar los campos que faltan
//		return repository.save(user);
//	}
	
	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.service.ICertificateService#updateCertificate(es.gob.fire.persistence.configuration.dto.CertificateDTO)
	 */
//	@Override
//	@Transactional
//	public User updateUser(UserEditDTO userDto) {
//		
//		User user = null;
//		
//		if (userDto.getIdUserFireEdit() != null) {
//			user = repository.findByUserId(userDto.getIdUserFireEdit());
//		} else {
//			user = new User();
//		}
//		user.setName(userDto.getNameEdit());
//		user.setSurnames(userDto.getSurnamesEdit());
//		user.setUserName(userDto.getUsernameEdit());
//		user.setEmail(userDto.getEmailEdit());
//		user.setRol(rolRepository.findByRolId(userDto.getRolId()));
//		user.setPhone(userDto.getTelfEdit());
//		//TODO Rellenar los campos que faltan
//
//		return repository.save(user);
//	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.services.ICertificateService#deleteCertificate(java.lang.Long)
	 */
	@Override
	public void deleteCertificate(Long IdCertificate) {
		repository.deleteById(IdCertificate);
	}
	
	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.services.ICertificateService#getAllCertificate()
	 */
	@Override
	public List<Certificate> getAllCertificate() {
		return repository.findAll();
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.services.ICertificateService#getCertificateByCertificateName(java.lang.String)
	 */
	@Override
	public Certificate getCertificateByCertificateName(final String nombre_cert) {
		return repository.findByCertificateName(nombre_cert);
	}
	
	
	

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.services.ICertificateService#findAll(org.springframework.data.jpa.datatables.mapping.DataTablesInput)
	 */
//	@Override
//	public DataTablesOutput<Certificate> getAllCertificate() {
//		return dtRepository.findAll();
//	}

	@Override
	public Certificate saveCertificate(CertificateDTO certificateDto) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Certificate updateCertificate(CertificateEditDTO certificateEditDto) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Certificate> getAllCertificate(List input) {
		// TODO Auto-generated method stub
		return null;
	}
	
	

	

}
