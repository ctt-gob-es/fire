package es.gob.fire.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import es.gob.fire.persistence.dto.UserLoggedDTO;

@Controller
public class AppController {

	/** The Constant LOG. */
	private static final Logger LOGGER = LoggerFactory.getLogger(AppController.class);
	
	@Autowired
	private UserLoggedDTO userLoggedDTO;

	@GetMapping({"/"})
	public String index() {
		return "login.html";
	}

	@GetMapping("/inicio")
	public String inicio() {
		// Informamos en la traza que el usuario X se ha logueado en la administracion
		LOGGER.info("El usuario "+ userLoggedDTO.getName() + " ha accedido a la administraci\u00f3n");
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
	
	@GetMapping("/mailpasswordrestoration")
	public String mailPasswordRestoration() {
		return "mailpasswordrestoration.html";
	}
}
