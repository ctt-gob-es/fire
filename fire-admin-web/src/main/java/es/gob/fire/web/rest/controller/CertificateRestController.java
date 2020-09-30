package es.gob.fire.web.rest.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.validation.constraints.NotEmpty;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

import es.gob.fire.i18n.IWebLogMessages;
import es.gob.fire.i18n.Language;
import es.gob.fire.persistence.dto.CertificateDTO;
import es.gob.fire.persistence.dto.UserDTO;
import es.gob.fire.persistence.dto.validation.OrderedValidation;
import es.gob.fire.persistence.entity.Certificate;
import es.gob.fire.persistence.entity.User;
import es.gob.fire.persistence.service.ICertificateService;

@RestController
public class CertificateRestController {
	
	/**
	 * Attribute that represents the service object for accessing the
	 * repository.
	 */
	@Autowired
	private ICertificateService certificateService;
	
	/**
	 * Constant that represents the key Json 'errorSaveCertificate'.
	 */
	private static final String KEY_JS_ERROR_SAVE_CERT = "errorSaveCertificate";
	
	/**
	 * Attribute that represents the span text.
	 */
	private static final String SPAN = "_span";
	
	/**
	 * Attribute that represents the object that manages the log of the class.
	 */
	private static final Logger LOGGER = Logger.getLogger(CertificateRestController.class);
	
	/**
	 * Method that maps the list certificates web requests to the controller and
	 * forwards the list of users to the view.
	 *
	 * @param input
	 *            Holder object for datatable attributes.
	 * @return String that represents the name of the view to forward.
	 */

	
	@JsonView(DataTablesOutput.View.class)
	@RequestMapping(path = "/certificatedatatable", method = RequestMethod.GET)
	public DataTablesOutput<Certificate> users(@NotEmpty final DataTablesInput input) {
		//input.getColumn(COLUMN_CERT_NOT_VALID).setSearchable(Boolean.FALSE);
		return (DataTablesOutput<Certificate>) certificateService.getAllCertificate();
	}

	/**
	 * Method that maps the delete user request from datatable to the controller
	 * and performs the delete of the certificate identified by its id.
	 *
	 * @param userId
	 *            Identifier of the user to be deleted.
	 * @param index
	 *            Row index of the datatable.
	 * @return String that represents the name of the view to redirect.
	 */
	@JsonView(DataTablesOutput.View.class)
	@RequestMapping(path = "/deletecertificate", method = RequestMethod.POST)
	@Transactional
	public String deleteCertificate(@RequestParam("id") final Long idCertificate, @RequestParam("index") final String index) {
		certificateService.deleteCertificate(idCertificate);
		return index;
	}
	
	/**
	 * Method that maps the save user web request to the controller and saves it
	 * in the persistence.
	 *
	 * @param userForm
	 *            Object that represents the backing user form.
	 * @param bindingResult
	 *            Object that represents the form validation result.
	 * @return {@link DataTablesOutput<User>}
	 */
	@RequestMapping(value = "/savecertificate", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	@JsonView(DataTablesOutput.View.class)
	public @ResponseBody DataTablesOutput<Certificate> save(@Validated(OrderedValidation.class) @RequestBody final CertificateDTO certificateForm, final BindingResult bindingResult) {
		DataTablesOutput<Certificate> dtOutput = new DataTablesOutput<Certificate>();
		List<Certificate> listNewCertificate = new ArrayList<Certificate>();
		JSONObject json = new JSONObject();
		
		if (bindingResult.hasErrors()) {
			listNewCertificate = StreamSupport.stream(certificateService.getAllCertificate().spliterator(), false).collect(Collectors.toList());
			for (FieldError o: bindingResult.getFieldErrors()) {
				json.put(o.getField() + SPAN, o.getDefaultMessage());
			}
			dtOutput.setError(json.toString());
		} else {
			try {
				
				Certificate certificate = certificateService.saveCertificate(certificateForm);

				listNewCertificate.add(certificate);
			} catch (Exception e) {
				LOGGER.error(Language.getResWebFire(IWebLogMessages.ERRORWEB022), e);
				listNewCertificate = StreamSupport.stream(certificateService.getAllCertificate().spliterator(), false).collect(Collectors.toList());
				json.put(KEY_JS_ERROR_SAVE_CERT, Language.getResWebFire(IWebLogMessages.ERRORWEB022));
				dtOutput.setError(json.toString());
			}
		}

		dtOutput.setData(listNewCertificate);

		return dtOutput;

	}

}
