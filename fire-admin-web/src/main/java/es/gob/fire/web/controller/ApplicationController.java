package es.gob.fire.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import es.gob.fire.persistence.dto.ApplicationEditDTO;
import es.gob.fire.persistence.service.IApplicationService;


/**
 * <p>
 * Class that manages the requests related to the Users administration.
 * </p>
 * <b>Project:</b>
 * <p>
 * Application for monitoring services of @firma suite systems.
 * </p>
 *
 * @version 1.2, 28/10/2020.
 */


@Controller
public class ApplicationController {
	
	/**
	 * Attribute that represents the service object for accessing the
	 * repository.
	 */
	@Autowired
	private IApplicationService applicationService;
	
	/**
	 * Method that maps the list applications web requests to the controller and
	 * forwards the list of users to the view.
	 *
	 * @param model
	 *            Holder object for model attributes.
	 * @return String that represents the name of the view to forward.
	 */
	@RequestMapping(value = "applicationadmin")
	public String index(final Model model) {
		model.addAttribute("applicationformEdit", new ApplicationEditDTO());
		return "fragments/applicationadmin.html";
	}
	
	

}
