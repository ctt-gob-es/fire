package es.gob.fire.web.clave3.sp;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Boot 3 error endpoint. Spring Boot 3 no longer exposes getErrorPath().
 */
@Controller
public class CustomErrorController implements ErrorController {

	@PostMapping("/error")
	public String handleError(final Model model) {
		model.addAttribute("errorMessageErrorPage",
				"Ha habido un error en la aplicación. Consulte los logs para obtener más información.");
		return "error";
	}
}
