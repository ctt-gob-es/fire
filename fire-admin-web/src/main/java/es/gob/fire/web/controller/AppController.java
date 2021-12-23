package es.gob.fire.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

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
	
	@GetMapping("/mailpasswordrestoration")
	public String mailPasswordRestoration() {
		return "mailpasswordrestoration.html";
	}
	
}
