package es.gob.fire.persistence.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.stereotype.Service;

import es.gob.fire.persistence.entity.Application;
import es.gob.fire.persistence.repository.ApplicationRepository;
import es.gob.fire.persistence.repository.CertificateRepository;
import es.gob.fire.persistence.repository.datatable.ApplicationDataTablesRepository;
import es.gob.fire.persistence.service.IApplicationService;

/**
 * <p>Class that implements the communication with the operations of the persistence layer.</p>
 * <b>Project:</b><p>Platform for detection and validation of certificates recognized in European TSL.</p>
 * @version 1.0, 15/06/2018.
 */
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ApplicationService implements IApplicationService{
	
	/**
	 * Attribute that represents the injected interface that proves CRUD operations for the persistence.
	 */
	@Autowired
	private ApplicationRepository repository;
	
	
	
	
	/**
	 * Attribute that represents the injected interface that proves CRUD operations for the persistence.
	 */
	@Autowired
	private ApplicationRepository apprepository;
	
	/**
	 * Attribute that represents the injected interface that proves CRUD operations for the persistence.
	 */
	@Autowired
	private CertificateRepository certRepository;

	/**
	 * Attribute that represents the injected interface that provides CRUD operations for the persistence.
	 */
	@Autowired
	private ApplicationDataTablesRepository appdtRepository;

	@Override
	public Application getAppByAppId(String appId) {
		return repository.findByAppId(appId);
	}

	@Override
	public Application getAppByAppName(String appName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Application saveApplication(Application app) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.services.IApplicationService#deleteAplication(java.lang.String)
	 */
	@Override
	public void deleteApplication(String appId) {
		repository.deleteById(appId);
	}
	
	@Override
	public List<Application> getAllApplication() {
		return repository.findAll();
	}
	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.services.IApplicationService#findAll(org.springframework.data.jpa.datatables.mapping.DataTablesInput)
	 */
	@Override
	public DataTablesOutput<Application> getAllApplication(DataTablesInput input) {
		return appdtRepository.findAll(input);
	}      

}
