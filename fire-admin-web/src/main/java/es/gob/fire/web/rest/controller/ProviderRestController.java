package es.gob.fire.web.rest.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.gob.fire.persistence.entity.Provider;
import es.gob.fire.persistence.service.IProviderService;

@RestController
public class ProviderRestController {

	/**
	 * Attribute that represents the service object for accessing the repository.
	 */
	@Autowired
	private IProviderService providerService;

	@GetMapping("/getProvidersGeneral")
    public List<Provider> getProviders() {
        return this.providerService.findProviders();
    }

	// Subir el orden de un proveedor
    @PostMapping("/upOrderProvider")
    public ResponseEntity<?> upOrder(@RequestParam final int providerOrder, @RequestParam final String idProvider) {
        try {
            this.providerService.moveProviderUp(providerOrder, idProvider);
            return ResponseEntity.ok().body("Proveedor movido hacia arriba con \u00E9xito.");
        } catch (final Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al subir el proveedor.");
        }
    }

    // Bajar el orden de un proveedor
    @PostMapping("/downOrderProvider")
    public ResponseEntity<?> downOrder(@RequestParam final int providerOrder, @RequestParam final String idProvider) {
        try {
            this.providerService.moveProviderDown(providerOrder, idProvider);
            return ResponseEntity.ok().body("Proveedor movido hacia abajo con \u00E9xito.");
        } catch (final Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al bajar el proveedor.");
        }
    }
}
