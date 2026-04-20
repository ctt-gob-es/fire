package es.gob.fire.web.rest.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import es.gob.fire.commons.utils.NumberConstants;
import es.gob.fire.commons.utils.QueryEnum;
import es.gob.fire.persistence.dto.SignatureDTO;
import es.gob.fire.persistence.dto.StatisticsFilterDTO;
import es.gob.fire.persistence.dto.StatisticsResultDTO;
import es.gob.fire.persistence.dto.TransactionDTO;
import es.gob.fire.persistence.service.ISignatureService;
import es.gob.fire.persistence.service.ITransactionService;

@RestController
public class StatisticsRestController {

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
	 * Method that maps the queries for service status between a time range.
	 * @param model Holder object for model attributes.
	 * @param query query
	 * @param month month
	 * @return String that represents the name of the view to forward.
	 */
	@PostMapping(value = "filterStatistics")
	public StatisticsResultDTO statisticsResult(final StatisticsFilterDTO filter) {
	    final String query = filter.getQuery();
	    final String monthDate = filter.getMonthDate();
	    final String endMonthDate = filter.getEndMonthDate();

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
	        final Integer month = Integer.valueOf(monthDate.substring(0, NumberConstants.NUM2));
	        final Integer year = Integer.valueOf(monthDate.substring(NumberConstants.NUM3, NumberConstants.NUM7));

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
	        final int month = Integer.parseInt(monthDate.substring(0, NumberConstants.NUM2));
	        final int year = Integer.parseInt(monthDate.substring(NumberConstants.NUM3, NumberConstants.NUM7));
	        final int endMonth = Integer.parseInt(endMonthDate.substring(0, NumberConstants.NUM2));
	        final int endYear = Integer.parseInt(endMonthDate.substring(NumberConstants.NUM3, NumberConstants.NUM7));

	        int currentMonth = month;
	        int currentYear = year;

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

	            final int totalMonthsDiff = endYear * 12 + endMonth - (year * 12 + month);

	            if (totalMonthsDiff > 24) {
	            	for (int y = year; y <= endYear; y++) {
	            		final List<TransactionDTO> listYear = this.transactionService
	            			.getTransactionsByApplication(1, y, 12, y, applicationsList, organizationsList);
	            		final String key = String.format("%04d", y);
	            		transactionsByMonth.put(key, listYear);
	            	}
	            } else {
	            	while (currentYear < endYear || currentYear == endYear && currentMonth <= endMonth) {
		                final List<TransactionDTO> listMonth = this.transactionService
		                        .getTransactionsByApplication(currentMonth, currentYear, applicationsList, organizationsList);
		                final String key = String.format("%02d/%04d", currentMonth, currentYear);
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

            	final int totalMonthsDiff = endYear * 12 + endMonth - (year * 12 + month);

	            if (totalMonthsDiff > 24) {
	            	for (int y = year; y <= endYear; y++) {
	            		final List<TransactionDTO> listYear = this.transactionService
	            			.getTransactionsByProvider(1, y, 12, y, applicationsList, organizationsList);
	            		final String key = String.format("%04d", y);
	            		transactionsByMonth.put(key, listYear);
	            	}
	            } else {
	            	while (currentYear < endYear || currentYear == endYear && currentMonth <= endMonth) {
		                final List<TransactionDTO> listMonth = this.transactionService
		                        .getTransactionsByProvider(currentMonth, currentYear, applicationsList, organizationsList);
		                final String key = String.format("%02d/%04d", currentMonth, currentYear);
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

            	final int totalMonthsDiff = endYear * 12 + endMonth - (year * 12 + month);

	            if (totalMonthsDiff > 24) {
	            	for (int y = year; y <= endYear; y++) {
	            		final List<TransactionDTO> listYear = this.transactionService
	            			.getTransactionsByDatesSizeApp(1, y, 12, y, applicationsList, organizationsList);
	            		final String key = String.format("%04d", y);
	            		transactionsByMonth.put(key, listYear);
	            	}
	            } else {
	            	while (currentYear < endYear || currentYear == endYear && currentMonth <= endMonth) {
		                final List<TransactionDTO> listMonth = this.transactionService
		                        .getTransactionsByDatesSizeApp(currentMonth, currentYear, applicationsList, organizationsList);
		                final String key = String.format("%02d/%04d", currentMonth, currentYear);
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

            	final int totalMonthsDiff = endYear * 12 + endMonth - (year * 12 + month);

	            if (totalMonthsDiff > 24) {
	            	for (int y = year; y <= endYear; y++) {
	            		final List<TransactionDTO> listYear = this.transactionService
	            			.getTransactionsByOperation(1, y, 12, y, applicationsList, organizationsList);
	            		final String key = String.format("%04d", y);
	            		transactionsByMonth.put(key, listYear);
	            	}
	            } else {
	            	while (currentYear < endYear || currentYear == endYear && currentMonth <= endMonth) {
		                final List<TransactionDTO> listMonth = this.transactionService
		                        .getTransactionsByOperation(currentMonth, currentYear, applicationsList, organizationsList);
		                final String key = String.format("%02d/%04d", currentMonth, currentYear);
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

	            final int totalMonthsDiff = endYear * 12 + endMonth - (year * 12 + month);

	            if (totalMonthsDiff > 24) {
	            	for (int y = year; y <= endYear; y++) {
	            		final List<TransactionDTO> listYear = this.transactionService
	            			.getTransactionsByOrganism(1, y, 12, y, applicationsList, organizationsList);
	            		final String key = String.format("%04d", y);
	            		transactionsByMonth.put(key, listYear);
	            	}
	            } else {
	            	while (currentYear < endYear || currentYear == endYear && currentMonth <= endMonth) {
		                final List<TransactionDTO> listMonth = this.transactionService
		                        .getTransactionsByOrganism(currentMonth, currentYear, applicationsList, organizationsList);
		                final String key = String.format("%02d/%04d", currentMonth, currentYear);
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

	            final int totalMonthsDiff = endYear * 12 + endMonth - (year * 12 + month);

	            if (totalMonthsDiff > 24) {
	            	for (int y = year; y <= endYear; y++) {
	            		final List<SignatureDTO> listYear = this.signatureService
	            			.getSignaturesByApplication(1, y, 12, y, applicationsList, organizationsList);
	            		final String key = String.format("%04d", y);
	            		signaturesByMonth.put(key, listYear);
	            	}
	            } else {
	            	while (currentYear < endYear || currentYear == endYear && currentMonth <= endMonth) {
		                final List<SignatureDTO> listMonth = this.signatureService
		                        .getSignaturesByApplication(currentMonth, currentYear, applicationsList, organizationsList);
		                final String key = String.format("%02d/%04d", currentMonth, currentYear);
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

	            final int totalMonthsDiff = endYear * 12 + endMonth - (year * 12 + month);

	            if (totalMonthsDiff > 24) {
	            	for (int y = year; y <= endYear; y++) {
	            		final List<SignatureDTO> listYear = this.signatureService
	            			.getSignaturesByProvider(1, y, 12, y, applicationsList, organizationsList);
	            		final String key = String.format("%04d", y);
	            		signaturesByMonth.put(key, listYear);
	            	}
	            } else {
	            	while (currentYear < endYear || currentYear == endYear && currentMonth <= endMonth) {
		                final List<SignatureDTO> listMonth = this.signatureService
		                        .getSignaturesByProvider(currentMonth, currentYear, applicationsList, organizationsList);
		                final String key = String.format("%02d/%04d", currentMonth, currentYear);
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

	            final int totalMonthsDiff = endYear * 12 + endMonth - (year * 12 + month);

	            if (totalMonthsDiff > 24) {
	            	for (int y = year; y <= endYear; y++) {
	            		final List<SignatureDTO> listYear = this.signatureService
	            			.getSignaturesByFormat(1, y, 12, y, applicationsList, organizationsList);
	            		final String key = String.format("%04d", y);
	            		signaturesByMonth.put(key, listYear);
	            	}
	            } else {
	            	while (currentYear < endYear || currentYear == endYear && currentMonth <= endMonth) {
		                final List<SignatureDTO> listMonth = this.signatureService
		                        .getSignaturesByFormat(currentMonth, currentYear, applicationsList, organizationsList);
		                final String key = String.format("%02d/%04d", currentMonth, currentYear);
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

	            final int totalMonthsDiff = endYear * 12 + endMonth - (year * 12 + month);

	            if (totalMonthsDiff > 24) {
	            	for (int y = year; y <= endYear; y++) {
	            		final List<SignatureDTO> listYear = this.signatureService
	            			.getSignaturesByImprovedFormat(1, y, 12, y, applicationsList, organizationsList);
	            		final String key = String.format("%04d", y);
	            		signaturesByMonth.put(key, listYear);
	            	}
	            } else {
	            	while (currentYear < endYear || currentYear == endYear && currentMonth <= endMonth) {
		                final List<SignatureDTO> listMonth = this.signatureService
		                        .getSignaturesByImprovedFormat(currentMonth, currentYear, applicationsList, organizationsList);
		                final String key = String.format("%02d/%04d", currentMonth, currentYear);
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

	            final int totalMonthsDiff = endYear * 12 + endMonth - (year * 12 + month);

	            if (totalMonthsDiff > 24) {
	            	for (int y = year; y <= endYear; y++) {
	            		final List<SignatureDTO> listYear = this.signatureService
	            			.getSignaturesByOrganism(1, y, 12, y, applicationsList, organizationsList);
	            		final String key = String.format("%04d", y);
	            		signaturesByMonth.put(key, listYear);
	            	}
	            } else {
	            	while (currentYear < endYear || currentYear == endYear && currentMonth <= endMonth) {
		                final List<SignatureDTO> listMonth = this.signatureService
		                        .getSignaturesByOrganism(currentMonth, currentYear, applicationsList, organizationsList);
		                final String key = String.format("%02d/%04d", currentMonth, currentYear);
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
