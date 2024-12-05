package es.gob.fire.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import es.gob.fire.commons.log.Logger;
import es.gob.fire.persistence.dto.AuditTransactionDTO;
import es.gob.fire.persistence.entity.AuditTransaction;
import es.gob.fire.persistence.service.IAuditTransactionService;

@Controller
public class AuditController {
	
	/**
	 * Constant that represents the parameter 'idPetition'.
	 */
	private static final String FIELD_ID_AUDIT_TRANSACTION = "idAuditTransaction";
	
	/**
	 * Constant that represents the parameter 'Lote'.
	 */
	private static final String BATCH_SIGN_VALUE = "BATCH";

	/**
	 * Constant that represents the parameter log.
	 */
	private static final Logger LOGGER = Logger.getLogger(AuditController.class);
	
	/**
	 * Attribute that represents the service object for accessing the
	 * repository.
	 */
	@Autowired
	private IAuditTransactionService auditTransactionService;
	
	/**
	 * Method that maps the list of petitions to the controller and
	 * forwards the list of users to the view.
	 *
	 * @param model Holder object for model attributes.
	 * @return String that represents the name of the view to forward.
	 */
	@RequestMapping(value = "auditadmin")
	public String getAuditAdmin(final Model model) {

		model.addAttribute("applicationsDropdown", auditTransactionService.getApplicationsDropdown());

		return "fragments/auditadmin.html";
	}
	
	/**
	 * Method that maps the details of the petition to the controller and
	 * forwards the list of users to the view.
	 *
	 * @param model Holder object for model attributes.
	 * @return String that represents the name of the view to forward.
	 */
	@RequestMapping(value = "viewAuditTransactionDetails")
	public String viewAuditTransactionDetails(@RequestParam(FIELD_ID_AUDIT_TRANSACTION) final Integer idAuditTransaction, final Model model) {
		
		AuditTransaction auditTransaction = auditTransactionService.getAuditTransactionByAuditTransactionId(idAuditTransaction);
		
		AuditTransactionDTO aduitTransactionForm = new AuditTransactionDTO(auditTransaction);
		
		model.addAttribute("auditTransactionViewForm", aduitTransactionForm);
		model.addAttribute("isBatch", auditTransaction.getOperation().equals(BATCH_SIGN_VALUE));
		model.addAttribute("disableInputs", true);
		model.addAttribute("listAuditSignatures", auditTransactionService.getAllAuditSignaturesOfTransaction(auditTransaction));

		return "modal/auditTransactionView.html";
	}
	
}
