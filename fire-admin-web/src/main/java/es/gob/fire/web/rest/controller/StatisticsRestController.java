package es.gob.fire.web.rest.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.gob.fire.commons.utils.Constants;
import es.gob.fire.commons.utils.NumberConstants;
import es.gob.fire.commons.utils.QueryEnum;
import es.gob.fire.commons.utils.UtilsStringChar;
import es.gob.fire.persistence.dto.SignatureDTO;
import es.gob.fire.persistence.dto.StatisticsFilterDTO;
import es.gob.fire.persistence.dto.StatisticsResultDTO;
import es.gob.fire.persistence.dto.TransactionDTO;
import es.gob.fire.persistence.entity.Property;
import es.gob.fire.persistence.service.IApplicationService;
import es.gob.fire.persistence.service.IPropertyService;
import es.gob.fire.persistence.service.ISignatureService;
import es.gob.fire.persistence.service.ITransactionService;
import es.gob.fire.persistence.service.impl.PropertyService;

@RestController
public class StatisticsRestController {
	/**
	 * Attribute that represents the object that manages the log of the class.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsRestController.class);
	
	/**
	 * Attribute that represents the transaction service.
	 */
	@Autowired
	private ITransactionService transactionService;

	/**
	 * Attribute that represents the signature service.
	 */
	@Autowired
	private ISignatureService signatureService;
	
	/**
	 * Attribute that represents the application service.
	 */
	@Autowired
	private IApplicationService applicationService;
	
	/**
	 * Method that maps the queries for service status between a time range.
	 * @param model Holder object for model attributes.
	 * @param query query
	 * @param month month
	 * @return String that represents the name of the view to forward.
	 */
	@PostMapping(value = "filterStatistics")
	public StatisticsResultDTO statisticsResult(StatisticsFilterDTO filter) {
	    String query = filter.getQuery();
	    String monthDate = filter.getMonthDate();
	    String endMonthDate = filter.getEndMonthDate();
	    
	    // Convertir las cadenas en listas usando split y filtrando
	    List<String> applicationsList = null;
	    if (!StringUtils.isEmpty(filter.getApplications())) {
	        applicationsList = StreamSupport.stream(
	                java.util.Arrays.spliterator(filter.getApplications().split(",")), false)
	                .map(String::trim)
	                .filter(s -> !s.isEmpty())
	                .collect(Collectors.toList());
	    }
	    
	    List<String> organizationsList = null;
	    if (!StringUtils.isEmpty(filter.getOrganizations())) {
	        organizationsList = StreamSupport.stream(
	                java.util.Arrays.spliterator(filter.getOrganizations().split(",")), false)
	                .map(String::trim)
	                .filter(s -> !s.isEmpty())
	                .collect(Collectors.toList());
	    }
	    
	    List<?> result = null;
	    StatisticsResultDTO statResult = new StatisticsResultDTO();
	    
	    // Caso SIN intervalo (endMonthDate vacío)
	    if (!StringUtils.isEmpty(query) && !StringUtils.isEmpty(monthDate) && StringUtils.isEmpty(endMonthDate)) {
	        Integer month = Integer.valueOf(monthDate.substring(0, NumberConstants.NUM2));
	        Integer year = Integer.valueOf(monthDate.substring(NumberConstants.NUM3, NumberConstants.NUM7));
	        
	        if(query.equalsIgnoreCase(QueryEnum.TRANSACTIONS_ENDED_BY_APP.getName())) {
	            result = StreamSupport.stream(
	                    this.transactionService.getTransactionsByApplication(month, year, applicationsList, organizationsList)
	                        .spliterator(), false)
	                    .collect(Collectors.toList());
	        } else if(query.equalsIgnoreCase(QueryEnum.TRANSACTIONS_ENDED_BY_PROVIDER.getName())) {
	            result = StreamSupport.stream(
	                    this.transactionService.getTransactionsByProvider(month, year, applicationsList, organizationsList)
	                        .spliterator(), false)
	                    .collect(Collectors.toList());
	        } else if(query.equalsIgnoreCase(QueryEnum.TRANSACTIONS_BY_DATES_SIZE_APP.getName())) {
	            result = StreamSupport.stream(
	                    this.transactionService.getTransactionsByDatesSizeApp(month, year, applicationsList, organizationsList)
	                        .spliterator(), false)
	                    .collect(Collectors.toList());
	        } else if(query.equalsIgnoreCase(QueryEnum.TRANSACTIONS_BY_TYPE_TRANSACTION.getName())) {
	            result = StreamSupport.stream(
	                    this.transactionService.getTransactionsByOperation(month, year, applicationsList, organizationsList)
	                        .spliterator(), false)
	                    .collect(Collectors.toList());
	        } else if(query.equalsIgnoreCase(QueryEnum.TRANSACTIONS_ENDED_BY_ORGANISM.getName())) {
	            result = StreamSupport.stream(
	                    this.transactionService.getTransactionsByOrganism(month, year, applicationsList, organizationsList)
	                        .spliterator(), false)
	                    .collect(Collectors.toList());
	        }
	        // Consultas de firmas sin intervalo
	        else if(query.equalsIgnoreCase(QueryEnum.DOCUMENTS_SIGNED_BY_APP.getName())) {
	            result = StreamSupport.stream(
	                    this.signatureService.getSignaturesByApplication(month, year, applicationsList, organizationsList)
	                        .spliterator(), false)
	                    .collect(Collectors.toList());
	        } else if(query.equalsIgnoreCase(QueryEnum.DOCUMENTS_SIGNED_BY_PROVIDER.getName())) {
	            result = StreamSupport.stream(
	                    this.signatureService.getSignaturesByProvider(month, year, applicationsList, organizationsList)
	                        .spliterator(), false)
	                    .collect(Collectors.toList());
	        } else if(query.equalsIgnoreCase(QueryEnum.DOCUMENTS_SIGNED_BY_SIGNATURE_FORMAT.getName())) {
	            result = StreamSupport.stream(
	                    this.signatureService.getSignaturesByFormat(month, year, applicationsList, organizationsList)
	                        .spliterator(), false)
	                    .collect(Collectors.toList());
	        } else if(query.equalsIgnoreCase(QueryEnum.DOCUMENTS_USED_IN_SIGNATURE_FORMAT.getName())) {
	            result = StreamSupport.stream(
	                    this.signatureService.getSignaturesByImprovedFormat(month, year, applicationsList, organizationsList)
	                        .spliterator(), false)
	                    .collect(Collectors.toList());
	        } else if(query.equalsIgnoreCase(QueryEnum.DOCUMENTS_SIGNED_BY_ORGANISM.getName())) {
	            result = StreamSupport.stream(
	                    this.signatureService.getSignaturesByOrganism(month, year, applicationsList, organizationsList)
	                        .spliterator(), false)
	                    .collect(Collectors.toList());
	        }
	        // Se asume que los resultados ya están filtrados en el service
	        statResult = statResult.obtainStatisticsResultByQueryType(result, query);
	    }
	    // Caso CON intervalo (endMonthDate no vacío)
	    else if (!StringUtils.isEmpty(query) && !StringUtils.isEmpty(monthDate) && !StringUtils.isEmpty(endMonthDate)) {
	        Integer month = Integer.valueOf(monthDate.substring(0, NumberConstants.NUM2));
	        Integer year = Integer.valueOf(monthDate.substring(NumberConstants.NUM3, NumberConstants.NUM7));
	        Integer endMonth = Integer.valueOf(endMonthDate.substring(0, NumberConstants.NUM2));
	        Integer endYear = Integer.valueOf(endMonthDate.substring(NumberConstants.NUM3, NumberConstants.NUM7));
	        
	        Integer currentMonth = month;
	        Integer currentYear = year;
	        
	        // Variables para agrupar resultados mensuales
	        Map<String, List<TransactionDTO>> transactionsByMonth = null;
	        Map<String, List<SignatureDTO>> signaturesByMonth = null;
	        
	        // Transacciones
	        if(query.equalsIgnoreCase(QueryEnum.TRANSACTIONS_ENDED_BY_APP.getName())) {
	            result = StreamSupport.stream(
	                    this.transactionService.getTransactionsByApplication(month, year, endMonth, endYear, applicationsList, organizationsList)
	                        .spliterator(), false)
	                    .collect(Collectors.toList());
	            
	            transactionsByMonth = new HashMap<>();
	            
	            int totalMonthsDiff = (endYear * 12 + endMonth) - (year * 12 + month);
	            
	            if (totalMonthsDiff > 24) {
	            	for (int y = year; y <= endYear; y++) {
	            		List<TransactionDTO> listYear = this.transactionService
	            			.getTransactionsByApplication(1, y, 12, y, applicationsList, organizationsList);
	            		String key = String.format("%04d", y);
	            		transactionsByMonth.put(key, listYear);
	            	}
	            } else {
	            	while (currentYear < endYear || (currentYear.equals(endYear) && currentMonth <= endMonth)) {
		                List<TransactionDTO> listMonth = this.transactionService
		                        .getTransactionsByApplication(currentMonth, currentYear, applicationsList, organizationsList);
		                String key = String.format("%02d/%04d", currentMonth, currentYear);
		                transactionsByMonth.put(key, listMonth);
		                currentMonth++;
		                if(currentMonth > 12) {
		                    currentMonth = 1;
		                    currentYear++;
		                }
		            }
	            }
	            
	            statResult = statResult.obtainStatisticsResultByQueryType(result, query);
	            statResult.setTransactionsByMonth(transactionsByMonth);
	            statResult.setEnableBarTimeChart(Boolean.TRUE);
	        } else if(query.equalsIgnoreCase(QueryEnum.TRANSACTIONS_ENDED_BY_PROVIDER.getName())) {
	            result = StreamSupport.stream(
	                    this.transactionService.getTransactionsByProvider(month, year, endMonth, endYear, applicationsList, organizationsList)
	                        .spliterator(), false)
	                    .collect(Collectors.toList());
	            transactionsByMonth = new HashMap<>();
	            
            	int totalMonthsDiff = (endYear * 12 + endMonth) - (year * 12 + month);
	            
	            if (totalMonthsDiff > 24) {
	            	for (int y = year; y <= endYear; y++) {
	            		List<TransactionDTO> listYear = this.transactionService
	            			.getTransactionsByProvider(1, y, 12, y, applicationsList, organizationsList);
	            		String key = String.format("%04d", y);
	            		transactionsByMonth.put(key, listYear);
	            	}
	            } else {
	            	while (currentYear < endYear || (currentYear.equals(endYear) && currentMonth <= endMonth)) {
		                List<TransactionDTO> listMonth = this.transactionService
		                        .getTransactionsByProvider(currentMonth, currentYear, applicationsList, organizationsList);
		                String key = String.format("%02d/%04d", currentMonth, currentYear);
		                transactionsByMonth.put(key, listMonth);
		                currentMonth++;
		                if(currentMonth > 12) {
		                    currentMonth = 1;
		                    currentYear++;
		                }
		            }
	            }
	            
	            statResult = statResult.obtainStatisticsResultByQueryType(result, query);
	            statResult.setTransactionsByMonth(transactionsByMonth);
	            statResult.setEnableBarTimeChart(Boolean.TRUE);
	        } else if(query.equalsIgnoreCase(QueryEnum.TRANSACTIONS_BY_DATES_SIZE_APP.getName())) {
	            result = StreamSupport.stream(
	                    this.transactionService.getTransactionsByDatesSizeApp(month, year, endMonth, endYear, applicationsList, organizationsList)
	                        .spliterator(), false)
	                    .collect(Collectors.toList());
	            transactionsByMonth = new HashMap<>();

            	int totalMonthsDiff = (endYear * 12 + endMonth) - (year * 12 + month);
	            
	            if (totalMonthsDiff > 24) {
	            	for (int y = year; y <= endYear; y++) {
	            		List<TransactionDTO> listYear = this.transactionService
	            			.getTransactionsByDatesSizeApp(1, y, 12, y, applicationsList, organizationsList);
	            		String key = String.format("%04d", y);
	            		transactionsByMonth.put(key, listYear);
	            	}
	            } else {
	            	while (currentYear < endYear || (currentYear.equals(endYear) && currentMonth <= endMonth)) {
		                List<TransactionDTO> listMonth = this.transactionService
		                        .getTransactionsByDatesSizeApp(currentMonth, currentYear, applicationsList, organizationsList);
		                String key = String.format("%02d/%04d", currentMonth, currentYear);
		                transactionsByMonth.put(key, listMonth);
		                currentMonth++;
		                if(currentMonth > 12) {
		                    currentMonth = 1;
		                    currentYear++;
		                }
		            }
	            }
	            
	            statResult = statResult.obtainStatisticsResultByQueryType(result, query);
	            statResult.setTransactionsByMonth(transactionsByMonth);
	            statResult.setEnableBarTimeChart(Boolean.TRUE);
	        } else if(query.equalsIgnoreCase(QueryEnum.TRANSACTIONS_BY_TYPE_TRANSACTION.getName())) {
	            result = StreamSupport.stream(
	                    this.transactionService.getTransactionsByOperation(month, year, endMonth, endYear, applicationsList, organizationsList)
	                        .spliterator(), false)
	                    .collect(Collectors.toList());
	            
	            transactionsByMonth = new HashMap<>();
	            
            	int totalMonthsDiff = (endYear * 12 + endMonth) - (year * 12 + month);
	            
	            if (totalMonthsDiff > 24) {
	            	for (int y = year; y <= endYear; y++) {
	            		List<TransactionDTO> listYear = this.transactionService
	            			.getTransactionsByOperation(1, y, 12, y, applicationsList, organizationsList);
	            		String key = String.format("%04d", y);
	            		transactionsByMonth.put(key, listYear);
	            	}
	            } else {
	            	while (currentYear < endYear || (currentYear.equals(endYear) && currentMonth <= endMonth)) {
		                List<TransactionDTO> listMonth = this.transactionService
		                        .getTransactionsByOperation(currentMonth, currentYear, applicationsList, organizationsList);
		                String key = String.format("%02d/%04d", currentMonth, currentYear);
		                transactionsByMonth.put(key, listMonth);
		                currentMonth++;
		                if(currentMonth > 12) {
		                    currentMonth = 1;
		                    currentYear++;
		                }
		            }
	            }
	            
	            statResult = statResult.obtainStatisticsResultByQueryType(result, query);
	            statResult.setTransactionsByMonth(transactionsByMonth);
	            statResult.setEnableBarTimeChart(Boolean.TRUE);
	        } else if(query.equalsIgnoreCase(QueryEnum.TRANSACTIONS_ENDED_BY_ORGANISM.getName())) {
	            result = StreamSupport.stream(
	                    this.transactionService.getTransactionsByOrganism(month, year, endMonth, endYear, applicationsList, organizationsList)
	                        .spliterator(), false)
	                    .collect(Collectors.toList());
	            
	            transactionsByMonth = new HashMap<>();
	            
	            int totalMonthsDiff = (endYear * 12 + endMonth) - (year * 12 + month);
	            
	            if (totalMonthsDiff > 24) {
	            	for (int y = year; y <= endYear; y++) {
	            		List<TransactionDTO> listYear = this.transactionService
	            			.getTransactionsByOrganism(1, y, 12, y, applicationsList, organizationsList);
	            		String key = String.format("%04d", y);
	            		transactionsByMonth.put(key, listYear);
	            	}
	            } else {
	            	while (currentYear < endYear || (currentYear.equals(endYear) && currentMonth <= endMonth)) {
		                List<TransactionDTO> listMonth = this.transactionService
		                        .getTransactionsByOrganism(currentMonth, currentYear, applicationsList, organizationsList);
		                String key = String.format("%02d/%04d", currentMonth, currentYear);
		                transactionsByMonth.put(key, listMonth);
		                currentMonth++;
		                if(currentMonth > 12) {
		                    currentMonth = 1;
		                    currentYear++;
		                }
		            }
	            }
	            
	            statResult = statResult.obtainStatisticsResultByQueryType(result, query);
	            statResult.setTransactionsByMonth(transactionsByMonth);
	            statResult.setEnableBarTimeChart(Boolean.TRUE);
	        }
	        // Consultas de firmas CON intervalo
	        else if(query.equalsIgnoreCase(QueryEnum.DOCUMENTS_SIGNED_BY_APP.getName())) {
	            result = StreamSupport.stream(
	                    this.signatureService.getSignaturesByApplication(month, year, endMonth, endYear, applicationsList, organizationsList)
	                        .spliterator(), false)
	                    .collect(Collectors.toList());
	            signaturesByMonth = new HashMap<>();
	            
	            int totalMonthsDiff = (endYear * 12 + endMonth) - (year * 12 + month);
	            
	            if (totalMonthsDiff > 24) {
	            	for (int y = year; y <= endYear; y++) {
	            		List<SignatureDTO> listYear = this.signatureService
	            			.getSignaturesByApplication(1, y, 12, y, applicationsList, organizationsList);
	            		String key = String.format("%04d", y);
	            		signaturesByMonth.put(key, listYear);
	            	}
	            } else {
	            	while (currentYear < endYear || (currentYear.equals(endYear) && currentMonth <= endMonth)) {
		                List<SignatureDTO> listMonth = this.signatureService
		                        .getSignaturesByApplication(currentMonth, currentYear, applicationsList, organizationsList);
		                String key = String.format("%02d/%04d", currentMonth, currentYear);
		                signaturesByMonth.put(key, listMonth);
		                currentMonth++;
		                if(currentMonth > 12) {
		                    currentMonth = 1;
		                    currentYear++;
		                }
		            }
	            }
	            
	            statResult = statResult.obtainStatisticsResultByQueryType(result, query);
	            statResult.setSignaturesByMonth(signaturesByMonth);
	            statResult.setEnableBarTimeChart(Boolean.TRUE);
	        } else if(query.equalsIgnoreCase(QueryEnum.DOCUMENTS_SIGNED_BY_PROVIDER.getName())) {
	            result = StreamSupport.stream(
	                    this.signatureService.getSignaturesByProvider(month, year, endMonth, endYear, applicationsList, organizationsList)
	                        .spliterator(), false)
	                    .collect(Collectors.toList());
	            signaturesByMonth = new HashMap<>();
	            
	            int totalMonthsDiff = (endYear * 12 + endMonth) - (year * 12 + month);
	            
	            if (totalMonthsDiff > 24) {
	            	for (int y = year; y <= endYear; y++) {
	            		List<SignatureDTO> listYear = this.signatureService
	            			.getSignaturesByProvider(1, y, 12, y, applicationsList, organizationsList);
	            		String key = String.format("%04d", y);
	            		signaturesByMonth.put(key, listYear);
	            	}
	            } else {
	            	while (currentYear < endYear || (currentYear.equals(endYear) && currentMonth <= endMonth)) {
		                List<SignatureDTO> listMonth = this.signatureService
		                        .getSignaturesByProvider(currentMonth, currentYear, applicationsList, organizationsList);
		                String key = String.format("%02d/%04d", currentMonth, currentYear);
		                signaturesByMonth.put(key, listMonth);
		                currentMonth++;
		                if(currentMonth > 12) {
		                    currentMonth = 1;
		                    currentYear++;
		                }
		            }
	            }
	            
	            statResult = statResult.obtainStatisticsResultByQueryType(result, query);
	            statResult.setSignaturesByMonth(signaturesByMonth);
	            statResult.setEnableBarTimeChart(Boolean.TRUE);
	        } else if(query.equalsIgnoreCase(QueryEnum.DOCUMENTS_SIGNED_BY_SIGNATURE_FORMAT.getName())) {
	            result = StreamSupport.stream(
	                    this.signatureService.getSignaturesByFormat(month, year, endMonth, endYear, applicationsList, organizationsList)
	                        .spliterator(), false)
	                    .collect(Collectors.toList());
	            signaturesByMonth = new HashMap<>();
	            
	            int totalMonthsDiff = (endYear * 12 + endMonth) - (year * 12 + month);
	            
	            if (totalMonthsDiff > 24) {
	            	for (int y = year; y <= endYear; y++) {
	            		List<SignatureDTO> listYear = this.signatureService
	            			.getSignaturesByFormat(1, y, 12, y, applicationsList, organizationsList);
	            		String key = String.format("%04d", y);
	            		signaturesByMonth.put(key, listYear);
	            	}
	            } else {
	            	while (currentYear < endYear || (currentYear.equals(endYear) && currentMonth <= endMonth)) {
		                List<SignatureDTO> listMonth = this.signatureService
		                        .getSignaturesByFormat(currentMonth, currentYear, applicationsList, organizationsList);
		                String key = String.format("%02d/%04d", currentMonth, currentYear);
		                signaturesByMonth.put(key, listMonth);
		                currentMonth++;
		                if(currentMonth > 12) {
		                    currentMonth = 1;
		                    currentYear++;
		                }
		            }
	            }
	            
	            statResult = statResult.obtainStatisticsResultByQueryType(result, query);
	            statResult.setSignaturesByMonth(signaturesByMonth);
	            statResult.setEnableBarTimeChart(Boolean.TRUE);
	        } else if(query.equalsIgnoreCase(QueryEnum.DOCUMENTS_USED_IN_SIGNATURE_FORMAT.getName())) {
	            result = StreamSupport.stream(
	                    this.signatureService.getSignaturesByImprovedFormat(month, year, endMonth, endYear, applicationsList, organizationsList)
	                        .spliterator(), false)
	                    .collect(Collectors.toList());
	            signaturesByMonth = new HashMap<>();
	            
	            int totalMonthsDiff = (endYear * 12 + endMonth) - (year * 12 + month);
	            
	            if (totalMonthsDiff > 24) {
	            	for (int y = year; y <= endYear; y++) {
	            		List<SignatureDTO> listYear = this.signatureService
	            			.getSignaturesByImprovedFormat(1, y, 12, y, applicationsList, organizationsList);
	            		String key = String.format("%04d", y);
	            		signaturesByMonth.put(key, listYear);
	            	}
	            } else {
	            	while (currentYear < endYear || (currentYear.equals(endYear) && currentMonth <= endMonth)) {
		                List<SignatureDTO> listMonth = this.signatureService
		                        .getSignaturesByImprovedFormat(currentMonth, currentYear, applicationsList, organizationsList);
		                String key = String.format("%02d/%04d", currentMonth, currentYear);
		                signaturesByMonth.put(key, listMonth);
		                currentMonth++;
		                if(currentMonth > 12) {
		                    currentMonth = 1;
		                    currentYear++;
		                }
		            }
	            }
	            
	            statResult = statResult.obtainStatisticsResultByQueryType(result, query);
	            statResult.setSignaturesByMonth(signaturesByMonth);
	            statResult.setEnableBarTimeChart(Boolean.TRUE);
	        } else if(query.equalsIgnoreCase(QueryEnum.DOCUMENTS_SIGNED_BY_ORGANISM.getName())) {
	            result = StreamSupport.stream(
	                    this.signatureService.getSignaturesByOrganism(month, year, endMonth, endYear, applicationsList, organizationsList)
	                        .spliterator(), false)
	                    .collect(Collectors.toList());
	            signaturesByMonth = new HashMap<>();
	            
	            int totalMonthsDiff = (endYear * 12 + endMonth) - (year * 12 + month);
	            
	            if (totalMonthsDiff > 24) {
	            	for (int y = year; y <= endYear; y++) {
	            		List<SignatureDTO> listYear = this.signatureService
	            			.getSignaturesByOrganism(1, y, 12, y, applicationsList, organizationsList);
	            		String key = String.format("%04d", y);
	            		signaturesByMonth.put(key, listYear);
	            	}
	            } else {
	            	while (currentYear < endYear || (currentYear.equals(endYear) && currentMonth <= endMonth)) {
		                List<SignatureDTO> listMonth = this.signatureService
		                        .getSignaturesByOrganism(currentMonth, currentYear, applicationsList, organizationsList);
		                String key = String.format("%02d/%04d", currentMonth, currentYear);
		                signaturesByMonth.put(key, listMonth);
		                currentMonth++;
		                if(currentMonth > 12) {
		                    currentMonth = 1;
		                    currentYear++;
		                }
		            }
	            }
	            
	            statResult = statResult.obtainStatisticsResultByQueryType(result, query);
	            statResult.setSignaturesByMonth(signaturesByMonth);
	            statResult.setEnableBarTimeChart(Boolean.TRUE);
	        }
	    }
	    
	    return statResult;
	}
}
