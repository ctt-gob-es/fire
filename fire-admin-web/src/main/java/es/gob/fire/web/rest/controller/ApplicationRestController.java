package es.gob.fire.web.rest.controller;

import javax.validation.constraints.NotEmpty;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.jackson.annotation.JsonView;
import org.apache.log4j.Logger;


import es.gob.fire.persistence.entity.Application;
import es.gob.fire.persistence.service.IApplicationService;

public class ApplicationRestController {
	
	/**
	 * Attribute that represents the object that manages the log of the class.
	 */
	private static final Logger LOGGER = Logger.getLogger(ApplicationRestController.class);

	
	/**
	 * Attribute that represents the service object for accessing the
	 * repository.
	 */
	@Autowired
	private IApplicationService appService;
	
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
	
	

}
