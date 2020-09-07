package es.gob.fire.web.rest.controller;

import javax.validation.constraints.NotEmpty;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

import es.gob.fire.persistence.entity.Certificate;
import es.gob.fire.persistence.service.ICertificateService;

@RestController
public class CertificateRestController {
	
	/**
	 * Attribute that represents the service object for accessing the
	 * repository.
	 */
	@Autowired
	private ICertificateService certificateService;
	
	@JsonView(DataTablesOutput.View.class)
	@RequestMapping(path = "/certificatedatatable", method = RequestMethod.GET)
	public DataTablesOutput<Certificate> users(@NotEmpty final DataTablesInput input) {
		//input.getColumn(COLUMN_CERT_NOT_VALID).setSearchable(Boolean.FALSE);
		return (DataTablesOutput<Certificate>) certificateService.getAllCertificate();
	}


}
