package es.gob.fire.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import es.gob.fire.persistence.repository.UserRepository;

@Controller
public class AppController {

	@Autowired
	UserRepository userRepository;

	@GetMapping({"/"})
	public String index() {
		return "login.html";
	}

	@GetMapping("/inicio")
	public String inicio() {
		return "inicio.html";
	}
	@GetMapping("/user-form")
	public String user() {
		return "user-form";
	}

	@GetMapping("/application")
	public String application() {
		return "application";
	}

	@GetMapping("/certificates")
	public String certificates() {
		return "certificates";
	}

	@GetMapping("/logs")
	public String logs() {
		return "logs";
	}

	@GetMapping("/statistics")
	public String statistics() {
		return "statistics";
	}

	/**
	 * Method that maps the invalid session request.
	* @param model Holder object for model attributes.
	 * @return String that represents the name of the view to forward.
	 */
	@RequestMapping(value = "invalidSession", method = { RequestMethod.GET, RequestMethod.POST })
	public String invalid(Model model) {
		return "invalidSession.html";
	}
	
	@GetMapping("/mailpasswordrestoration")
	public String mailPasswordRestoration() {
		return "mailPasswordRestoration.html";
	}
	
}
