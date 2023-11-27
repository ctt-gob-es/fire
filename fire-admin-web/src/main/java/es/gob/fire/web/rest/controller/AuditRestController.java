package es.gob.fire.web.rest.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

import es.gob.fire.commons.log.Logger;
import es.gob.fire.persistence.entity.AuditTransaction;
import es.gob.fire.persistence.entity.AuditSignature;
import es.gob.fire.persistence.service.IAuditTransactionService;
import es.gob.fire.report.common.Report;
import es.gob.fire.report.common.ReportGenerator;

@RestController
public class AuditRestController {
	
	/**
	 * Attribute that represents the object that manages the log of the class.
	 */
	private static final Logger LOGGER = Logger.getLogger(AuditRestController.class);
	
	/**
	 * Constant attribute that represents the default file name for the petitions report. 
	 */
	private static final String CONTENT_DISPOSITION_DOWNLOAD_REPORT = "Content-disposition";
	
	/**
	 * Attribute that represents the header to download petitions report. 
	 */
	public static final String HEADER_DOWNLOAD_REPORT = "attachment; filename=";
	
	/**
	 * Attribute that represents the content disposition to download report. 
	 */
	public static final String CONTENT_TYPE_DOWNLOAD_REPORT = "application/vnd.ms-excel;base64";
	
	/**
	 * Constant attribute that represents the date format <code>yyyyMMddHHmmss</code>.
	 */
	public static final String REPORT_FILENAME_DATE_FORMAT = "yyyyMMddHHmmss";
	
	/**
	 * Constant attribute that represents the date format <code>yyyyMMddHHmmss</code>.
	 */
	public static final String REPORT_DEFAULT_FILENAME = "AuditReport";
	
	/**
	 * Constant that defines the report extension.
	 */
	public static final String REPORT_EXTENSION = ".xls";
	
	/**
	 * Constant that defines the time expression for no end date in the filter.
	 */
	public static final String TIME_EXPRESSION_SEPARATOR = " - ";
	
	/**
	 * Constant that defines the time expression for no end date in the filter.
	 */
	public static final String TIME_EXPRESSION_NO_START_DATE = "INICIO";
	
	/**
	 * Constant that defines the time expression for no end date in the filter.
	 */
	public static final String TIME_EXPRESSION_NO_END_DATE = "AHORA";
	
	/**
	 * Attribute that represents the mail hot.
	 */
	@Value("${audit.time.default}")
	private String auditTimeProperty;

	public void setAuditTimeProperty(String auditTimeProperty) {
		this.auditTimeProperty = auditTimeProperty;
	}

	/**
	 * Attribute that represents the service object for accessing the
	 * repository.
	 */
	@Autowired
	private IAuditTransactionService auditTransactionService;
	
	/**
	 * Attribute that represents the view message wource.
	 */
	@Autowired
	private MessageSource messageSource;
	
	/**
	 * Method that maps the list users web requests to the controller and forwards the list of audit transactions to the view.
	 *
	 * @param input Holder object for datatable attributes.
	 * @return String that represents the name of the view to forward.
	 */
	@JsonView(DataTablesOutput.View.class)
	@RequestMapping(path = "/auditTransactionDatatable", method = RequestMethod.GET)
	public DataTablesOutput<AuditTransaction> getPetitionsDatatable(@NotEmpty final DataTablesInput input) {
		return auditTransactionService.getAllAuditTransactions(input);
	}
	
	/**
	 * Method that maps the list users web requests to the controller and forwards the list of audit transactions to the view.
	 *
	 * @param input Holder object for datatable attributes.
	 * @return String that represents the name of the view to forward.
	 */
	@JsonView(DataTablesOutput.View.class)
	@RequestMapping(path = "/auditTransactionDatatableWithFilter", method = RequestMethod.GET)
	public DataTablesOutput<AuditTransaction> getPetitionsDatatableWithFilter(@NotEmpty final DataTablesInput input, @RequestParam("from") String from, @RequestParam("to") String to, @RequestParam("app") String app) {
		Date fromDate = null;
		Date toDate = null;
		String appFilter = null;
		
		try {
			if (from != null && !from.isEmpty()){
				fromDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(from);
			}
			
			if (to != null && !to.isEmpty()){
				toDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(to);
			}
			
			if (app != null && !app.isEmpty()){
				appFilter = app;
			}
		} catch (ParseException e) {
			LOGGER.error("Error parsing date parameters");
		}  
		
		DataTablesOutput dtOutput = auditTransactionService.getAuditTransactionsWithDateFilter(input, fromDate, toDate, appFilter);
		
		return dtOutput;
	}
	
	@JsonView(DataTablesOutput.View.class)
	@RequestMapping(path = "/getAuditSignaturesOfTransaction", method = RequestMethod.GET)
	public DataTablesOutput<AuditSignature> getAuditSignaturesOfTransaction(@NotEmpty final DataTablesInput input, @RequestParam("idAuditTransaction") Integer idAuditTransaction){
		DataTablesOutput<AuditSignature> dtOutput = new DataTablesOutput<AuditSignature>();
		
		AuditTransaction auditTransaction = auditTransactionService.getAuditTransactionByAuditTransactionId(idAuditTransaction);
		
		return auditTransactionService.getAllAuditSignaturesOfTransaction(input, auditTransaction);
	}

	@RequestMapping(value = "/exportAudit", method= RequestMethod.GET)
	public void generatePetitionsReport(@RequestParam("from") String from, @RequestParam("to") String to, HttpServletResponse response){
		
		Date fromDate = null;
		Date toDate = null;
		
		try {
			if (from != null && !from.isEmpty()){
				fromDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(from);
			}
			
			if (to != null && !to.isEmpty()){
				toDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(to);
			}
		} catch (ParseException e) {
			LOGGER.error("Error");
		}
		
		List<AuditTransaction> listTransactions = auditTransactionService.getAuditTransactionsWithDateFilter(fromDate, toDate);
		
		List<AuditSignature> listSignatures = auditTransactionService.getAllAuditSignature();
		
		Map<Object, Object> params = new HashMap<Object, Object>();
		
		if (listTransactions != null){
			params.put("listTransactions", listTransactions);
		}
		if (listSignatures != null){
			params.put("listSignatures", listSignatures);
		}
		
		Report report = ReportGenerator.getReport("AUDIT", params);
		
		try {
			byte[] reportBytes = report.getReport();
			
			String encodedBase64 = new String(Base64.getEncoder().encode(reportBytes));
			
			String fileName = new SimpleDateFormat(REPORT_FILENAME_DATE_FORMAT).format(new Date()) + "_" + REPORT_DEFAULT_FILENAME + REPORT_EXTENSION;
			
			response.setHeader(CONTENT_DISPOSITION_DOWNLOAD_REPORT, HEADER_DOWNLOAD_REPORT.concat(fileName));
			response.setContentType(CONTENT_TYPE_DOWNLOAD_REPORT);
			response.getOutputStream().write(encodedBase64.getBytes());
		} catch (Exception e) {
			LOGGER.error("Error");
		}
	}
	
	@JsonView(DataTablesOutput.View.class)
	@RequestMapping(path = "/getAuditTransactionsFirstLoad", method = RequestMethod.GET)
	public DataTablesOutput<AuditTransaction> getPetitionsFirstLoad(@NotEmpty final DataTablesInput input){
		DataTablesOutput<AuditTransaction> dtOuput = new DataTablesOutput<AuditTransaction>();
		
		Integer auditTimeDefault = Integer.parseInt(auditTimeProperty);
		
		if (auditTimeDefault != null){
			dtOuput = auditTransactionService.getAuditTransactionsFirstQuery(input, auditTimeDefault);
		}
		
		return dtOuput;
	}
	
	@RequestMapping(path = "/getAuditTransactionsFirstLoadFromDate", method = RequestMethod.GET)
	public String getAuditFirstLoadFromDate() {
		String loadDateString = "";
		
		Integer auditTimeDefault = Integer.parseInt(auditTimeProperty); 
		
		Date firstLoadDate = new Date(System.currentTimeMillis() - auditTimeDefault * 60 * 1000); 
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		
		loadDateString += dateFormat.format(firstLoadDate);
		
		return loadDateString;
	}
	
	@RequestMapping(path = "/getAuditTransactionsFirstLoadToDate", method = RequestMethod.GET)
	public String getAuditFirstLoadToDate() {
		String loadDateString = "";
		
		Date firstLoadDate = new Date(); 
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		
		loadDateString += dateFormat.format(firstLoadDate);
		
		return loadDateString;
	}
	
	@RequestMapping(path = "/getAuditTransactionsFilterLoadDate", method = RequestMethod.GET)
	public String getAuditFilterLoadDate(@RequestParam("from") String from, @RequestParam("to") String to, @RequestParam("app") String app) {
		String loadDateString = "";
		
		boolean fromIsNotNull = from != null && !from.isEmpty();
		boolean toIsNotNull = to != null && !to.isEmpty();
		
		int compareMode = (fromIsNotNull && toIsNotNull) ? 1 : (fromIsNotNull ? 2 : (toIsNotNull ? 3 : 4)); 
		
		switch (compareMode) {
		case 1:
			loadDateString += from + TIME_EXPRESSION_SEPARATOR + to;
			break;
		case 2:
			loadDateString += from + TIME_EXPRESSION_SEPARATOR + TIME_EXPRESSION_NO_END_DATE;
			break;
		case 3:
			loadDateString += TIME_EXPRESSION_NO_START_DATE + TIME_EXPRESSION_SEPARATOR + to;
			break;
		default:
			loadDateString += TIME_EXPRESSION_NO_START_DATE + TIME_EXPRESSION_SEPARATOR + TIME_EXPRESSION_NO_END_DATE;
			break;
		}
		
		return loadDateString;
	}
}
