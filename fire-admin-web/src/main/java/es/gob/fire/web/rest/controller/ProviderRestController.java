package es.gob.fire.web.rest.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.gob.fire.commons.log.Logger;
import es.gob.fire.persistence.entity.Provider;
import es.gob.fire.persistence.service.IProviderService;

@RestController
public class ProviderRestController {
	/**
	 * Attribute that represents the object that manages the log of the class.
	 */
	private static final Logger LOGGER = Logger.getLogger(ProviderRestController.class);
	
	/**
	 * Attribute that represents the service object for accessing the repository.
	 */
	@Autowired
	private IProviderService providerService;
	
	@GetMapping("/getProvidersGeneral")
    public List<Provider> getProviders() {
        return providerService.findProviders();
    }
	
	// Subir el orden de un proveedor
    @PostMapping("/upOrderProvider")
    public ResponseEntity<?> upOrder(@RequestParam int providerOrder, @RequestParam Long idProvider) {
        try {
            providerService.moveProviderUp(providerOrder, idProvider);
            return ResponseEntity.ok().body("Proveedor movido hacia arriba con éxito.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al subir el proveedor.");
        }
    }

    // Bajar el orden de un proveedor
    @PostMapping("/downOrderProvider")
    public ResponseEntity<?> downOrder(@RequestParam int providerOrder, @RequestParam Long idProvider) {
        try {
            providerService.moveProviderDown(providerOrder, idProvider);
            return ResponseEntity.ok().body("Proveedor movido hacia abajo con éxito.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al bajar el proveedor.");
        }
    }
}
