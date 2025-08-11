/*
/*******************************************************************************
 * Copyright (C) 2018 MINHAFP, Gobierno de Espa&ntilde;a
 * This program is licensed and may be used, modified and redistributed under the  terms
 * of the European Public License (EUPL), either version 1.1 or (at your option)
 * any later version as soon as they are approved by the European Commission.
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and
 * more details.
 * You should have received a copy of the EUPL1.1 license
 * along with this program; if not, you may find it at
 * http:joinup.ec.europa.eu/software/page/eupl/licence-eupl
 ******************************************************************************/

/**
 * <b>File:</b><p>es.gob.fire.web.controller.StatisticsController.java.</p>
 * <b>Description:</b><p>Class that manages the requests related to the statistics administration.</p>
  * <b>Project:</b><p>Application for signing documents of @firma suite systems</p>
 * <b>Date:</b><p>14/04/2020.</p>
 * @author Gobierno de Espa&ntilde;a.
 * @version 1.1, 22/01/2025.
 */
package es.gob.fire.web.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import es.gob.fire.commons.log.Logger;
import es.gob.fire.commons.utils.Constants;
import es.gob.fire.commons.utils.NumberConstants;
import es.gob.fire.commons.utils.QueryEnum;
import es.gob.fire.commons.utils.UtilsStringChar;
import es.gob.fire.persistence.dto.OrganizationDTO;
import es.gob.fire.persistence.dto.Property;
import es.gob.fire.persistence.dto.SignatureDTO;
import es.gob.fire.persistence.dto.TransactionDTO;
import es.gob.fire.persistence.entity.Application;
import es.gob.fire.persistence.service.IApplicationService;
import es.gob.fire.persistence.service.IPropertyService;
import es.gob.fire.persistence.service.ISignatureService;
import es.gob.fire.persistence.service.ITransactionService;
import es.gob.fire.persistence.service.impl.PropertyService;

/**
 * <p>Class that manages the requests related to the statistics administration.</p>
 * <b>Project:</b><p>Application for signing documents of @firma suite systems.</p>
 * @version 1.1, 22/01/2025.
 */
@Controller
public class StatisticsController {

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
	 * Attribute that represents the property service.
	 */
	@Autowired
	private IPropertyService propertyService;
	
	/**
	 * Constant that represents the parameter log.
	 */
	private static final Logger LOGGER = Logger.getLogger(StatisticsController.class);

	/**
	 * Method that maps the list users web requests to the controller and forwards the list of platforms
	 * to the view.
	 * @param model Holder object for model attributes.
	 * @return String that represents the name of the view to forward.
	 */
	@RequestMapping(value = "statistics", method = RequestMethod.GET)
    public String loadStatisticsAdmin(final Model model){
		Long maxEntitiesBeforeGrouping = getMaxEntitiesBeforeGrouping();
		
		model.addAttribute("maxEntitiesBeforeGrouping", maxEntitiesBeforeGrouping);
		
        return "fragments/statistics.html";
    }
	
	/**
	 * Method that maps the list users web requests to the controller and forwards the list of platforms
	 * to the view.
	 * @param model Holder object for model attributes.
	 * @return String that represents the name of the view to forward.
	 */
	@RequestMapping(value = "statisticsFilter", method = RequestMethod.GET)
    public String loadStatisticsFilterModal(final Model model){

		List<QueryEnum> queries = new ArrayList<>();

		queries = StreamSupport.stream(EnumSet.allOf(QueryEnum.class).spliterator(), false).collect(Collectors.toList());
		
		Set<String> allApplications = new HashSet<>();
		allApplications.addAll(transactionService.getDifferentApplications());
		allApplications.addAll(signatureService.getDifferentApplications());
		
		List<String> applications = new ArrayList<>(allApplications);
		Collections.sort(applications);
		
		Set<OrganizationDTO> allOrganizations = new HashSet<>();
		allOrganizations.addAll(transactionService.getDifferentOrganizations());
		allOrganizations.addAll(signatureService.getDifferentOrganizations());

		List<OrganizationDTO> organizations = new ArrayList<>(allOrganizations);
		Collections.sort(organizations, new Comparator<OrganizationDTO>() {
			@Override
			public int compare(OrganizationDTO o1, OrganizationDTO o2) {
				if (o1 == null && o2 == null) return 0;
		        if (o1 == null) return 1;
		        if (o2 == null) return -1;

		        if (o1.getOrganization() == null && o2.getOrganization() == null) return 0;
		        if (o1.getOrganization() == null) return 1;
		        if (o2.getOrganization() == null) return -1;
		        
				return o1.getOrganization().compareToIgnoreCase(o2.getOrganization());
			}
		});
		
		organizations.add(0, new OrganizationDTO("No Definido", "__UNDEFINED__"));
				
		model.addAttribute("queries", queries);
		model.addAttribute("applications", applications);
		model.addAttribute("organizations", organizations);

        return "modal/statistics/statisticsFilter.html";
    }

	/**
	 * Method that maps the queries for service status between a time range.
	 * @param model Holder object for model attributes.
	 * @param query query
	 * @param month month
	 * @return String that represents the name of the view to forward.
	 */
	@RequestMapping(value = "statisticsresult", method = RequestMethod.GET)
    public String statisticsResult(final Model model, final @RequestParam("query") String query, final @RequestParam("monthDate") String monthDate, final @RequestParam("endMonthDate") String endMonthDate) {
		List<TransactionDTO> transactions = null;
		List<SignatureDTO> signatures = null;
		
		if (!StringUtils.isEmpty(query) && !StringUtils.isEmpty(monthDate) && StringUtils.isEmpty(endMonthDate)) {
			final Integer month = Integer.valueOf(monthDate.substring(0, NumberConstants.NUM2));
			final Integer year = Integer.valueOf(monthDate.substring(NumberConstants.NUM3, NumberConstants.NUM7));
			// Consultas de transacciones
			if (query.equalsIgnoreCase(QueryEnum.TRANSACTIONS_ENDED_BY_APP.getName())) {
				transactions = StreamSupport.stream(this.transactionService.getTransactionsByApplication(month, year).spliterator(), false).collect(Collectors.toList());
				model.addAttribute("isQueryByAppOrProvider", Boolean.TRUE);
				model.addAttribute("isQueryByApp", Boolean.TRUE);
				model.addAttribute("queryStatisticsResult", transactions);
				model.addAttribute("textGood", Constants.TRANS_CORRECTAS.concat(UtilsStringChar.SPECIAL_BLANK_SPACE_STRING).concat(Constants.QUERYBYTYPE_APP));
				model.addAttribute("textBad", Constants.TRANS_INCORRECTAS.concat(UtilsStringChar.SPECIAL_BLANK_SPACE_STRING).concat(Constants.QUERYBYTYPE_APP));
				model.addAttribute("queryenum", QueryEnum.TRANSACTIONS_ENDED_BY_APP.getId());
				model.addAttribute("enableDonutChart", Boolean.TRUE);
				model.addAttribute("enableBarChart", Boolean.TRUE);
				model.addAttribute("enableBarTimeChart", Boolean.FALSE);
			}
			if (query.equalsIgnoreCase(QueryEnum.TRANSACTIONS_ENDED_BY_PROVIDER.getName())) {
				transactions = StreamSupport.stream(this.transactionService.getTransactionsByProvider(month, year).spliterator(), false).collect(Collectors.toList());
				model.addAttribute("isQueryByAppOrProvider", Boolean.TRUE);
				model.addAttribute("isQueryByProvider", Boolean.TRUE);
				model.addAttribute("queryStatisticsResult", transactions);
				model.addAttribute("textGood", Constants.TRANS_CORRECTAS.concat(UtilsStringChar.SPECIAL_BLANK_SPACE_STRING).concat(Constants.QUERYBYTYPE_PROVEEDOR));
				model.addAttribute("textBad", Constants.TRANS_INCORRECTAS.concat(UtilsStringChar.SPECIAL_BLANK_SPACE_STRING).concat(Constants.QUERYBYTYPE_PROVEEDOR));
				model.addAttribute("queryenum", QueryEnum.TRANSACTIONS_ENDED_BY_PROVIDER.getId());
				model.addAttribute("enableDonutChart", Boolean.TRUE);
				model.addAttribute("enableBarChart", Boolean.TRUE);
				model.addAttribute("enableBarTimeChart", Boolean.FALSE);
			}
			if (query.equalsIgnoreCase(QueryEnum.TRANSACTIONS_BY_DATES_SIZE_APP.getName())) {
				transactions = StreamSupport.stream(this.transactionService.getTransactionsByDatesSizeApp(month, year).spliterator(), false).collect(Collectors.toList());
				model.addAttribute("isQueryByDatesSize", Boolean.TRUE);
				model.addAttribute("queryStatisticsResult", transactions);
				model.addAttribute("queryenum", QueryEnum.TRANSACTIONS_BY_DATES_SIZE_APP.getId());
				model.addAttribute("enableDonutChart", Boolean.FALSE);
				model.addAttribute("enableBarChart", Boolean.TRUE);
				model.addAttribute("enableBarTimeChart", Boolean.FALSE);
			}
			if (query.equalsIgnoreCase(QueryEnum.TRANSACTIONS_BY_TYPE_TRANSACTION.getName())) {
				transactions = StreamSupport.stream(this.transactionService.getTransactionsByOperation(month, year).spliterator(), false).collect(Collectors.toList());
				model.addAttribute("isQueryByOperation", Boolean.TRUE);
				model.addAttribute("queryStatisticsResult", transactions);
				model.addAttribute("queryenum", QueryEnum.TRANSACTIONS_BY_TYPE_TRANSACTION.getId());
				model.addAttribute("enableDonutChart", Boolean.TRUE);
				model.addAttribute("enableBarChart", Boolean.TRUE);
				model.addAttribute("enableBarTimeChart", Boolean.FALSE);
			}
			if (query.equalsIgnoreCase(QueryEnum.TRANSACTIONS_ENDED_BY_ORGANISM.getName())) {
				transactions = StreamSupport.stream(this.transactionService.getTransactionsByOrganism(month, year).spliterator(), false).collect(Collectors.toList());
				model.addAttribute("isQueryByAppOrProvider", Boolean.TRUE);
				model.addAttribute("isQueryByOrganism", Boolean.TRUE);
				model.addAttribute("queryStatisticsResult", transactions);
				model.addAttribute("queryenum", QueryEnum.TRANSACTIONS_ENDED_BY_ORGANISM.getId());
				model.addAttribute("enableDonutChart", Boolean.TRUE);
				model.addAttribute("enableBarChart", Boolean.TRUE);
				model.addAttribute("enableBarTimeChart", Boolean.FALSE);
			}
			
			// Consulta de firmas
			if (query.equalsIgnoreCase(QueryEnum.DOCUMENTS_SIGNED_BY_APP.getName())) {
				signatures = StreamSupport.stream(this.signatureService.getSignaturesByApplication(month, year).spliterator(), false).collect(Collectors.toList());
				model.addAttribute("isSignatureQuery", Boolean.TRUE);
				model.addAttribute("isSignatureQueryByApp", Boolean.TRUE);
				model.addAttribute("queryStatisticsResult", signatures);
				model.addAttribute("queryenum", QueryEnum.DOCUMENTS_SIGNED_BY_APP.getId());
				model.addAttribute("enableDonutChart", Boolean.TRUE);
				model.addAttribute("enableBarChart", Boolean.TRUE);
				model.addAttribute("enableBarTimeChart", Boolean.FALSE);
			}
			if (query.equalsIgnoreCase(QueryEnum.DOCUMENTS_SIGNED_BY_PROVIDER.getName())) {
				signatures = StreamSupport.stream(this.signatureService.getSignaturesByProvider(month, year).spliterator(), false).collect(Collectors.toList());
				model.addAttribute("isSignatureQuery", Boolean.TRUE);
				model.addAttribute("isSignatureQueryByProvider", Boolean.TRUE);
				model.addAttribute("queryStatisticsResult", signatures);
				model.addAttribute("queryenum", QueryEnum.DOCUMENTS_SIGNED_BY_PROVIDER.getId());
				model.addAttribute("enableDonutChart", Boolean.TRUE);
				model.addAttribute("enableBarChart", Boolean.TRUE);
				model.addAttribute("enableBarTimeChart", Boolean.FALSE);
			}
			if (query.equalsIgnoreCase(QueryEnum.DOCUMENTS_SIGNED_BY_SIGNATURE_FORMAT.getName())) {
				signatures = StreamSupport.stream(this.signatureService.getSignaturesByFormat(month, year).spliterator(), false).collect(Collectors.toList());
				model.addAttribute("isSignatureQuery", Boolean.TRUE);
				model.addAttribute("isSignatureQueryByFormat", Boolean.TRUE);
				model.addAttribute("queryStatisticsResult", signatures);
				model.addAttribute("queryenum", QueryEnum.DOCUMENTS_SIGNED_BY_SIGNATURE_FORMAT.getId());
				model.addAttribute("enableDonutChart", Boolean.TRUE);
				model.addAttribute("enableBarChart", Boolean.TRUE);
				model.addAttribute("enableBarTimeChart", Boolean.FALSE);
			}
			if (query.equalsIgnoreCase(QueryEnum.DOCUMENTS_USED_IN_SIGNATURE_FORMAT.getName())) {
				signatures = StreamSupport.stream(this.signatureService.getSignaturesByImprovedFormat(month, year).spliterator(), false).collect(Collectors.toList());
				model.addAttribute("isSignatureQuery", Boolean.TRUE);
				model.addAttribute("isSignatureQueryByImprovedFormat", Boolean.TRUE);
				model.addAttribute("queryStatisticsResult", signatures);
				model.addAttribute("queryenum", QueryEnum.DOCUMENTS_USED_IN_SIGNATURE_FORMAT.getId());
				model.addAttribute("enableDonutChart", Boolean.TRUE);
				model.addAttribute("enableBarChart", Boolean.TRUE);
				model.addAttribute("enableBarTimeChart", Boolean.FALSE);
			}
			if (query.equalsIgnoreCase(QueryEnum.DOCUMENTS_SIGNED_BY_ORGANISM.getName())) {
				signatures = StreamSupport.stream(this.signatureService.getSignaturesByOrganism(month, year).spliterator(), false).collect(Collectors.toList());
				model.addAttribute("isSignatureQuery", Boolean.TRUE);
				model.addAttribute("isSignatureQueryByOrganism", Boolean.TRUE);
				model.addAttribute("queryStatisticsResult", signatures);
				model.addAttribute("queryenum", QueryEnum.DOCUMENTS_SIGNED_BY_ORGANISM.getId());
				model.addAttribute("enableDonutChart", Boolean.TRUE);
				model.addAttribute("enableBarChart", Boolean.TRUE);
				model.addAttribute("enableBarTimeChart", Boolean.FALSE);
			}
			model.addAttribute("tableStatisticsTitle", query + " para el mes " + monthDate);
		} else if (!StringUtils.isEmpty(query) && !StringUtils.isEmpty(monthDate) && !StringUtils.isEmpty(endMonthDate)) {
			final Integer month = Integer.valueOf(monthDate.substring(0, NumberConstants.NUM2));
			final Integer year = Integer.valueOf(monthDate.substring(NumberConstants.NUM3, NumberConstants.NUM7));
			
			final Integer endMonth = Integer.valueOf(endMonthDate.substring(0, NumberConstants.NUM2));
			final Integer endYear = Integer.valueOf(endMonthDate.substring(NumberConstants.NUM3, NumberConstants.NUM7));
			
			Integer currentMonth = month;
			Integer currentYear = year;
			
			// Consultas de transacciones
			if (query.equalsIgnoreCase(QueryEnum.TRANSACTIONS_ENDED_BY_APP.getName())) {
				transactions = StreamSupport.stream(this.transactionService.getTransactionsByApplication(month, year, endMonth, endYear).spliterator(), false).collect(Collectors.toList());

				Map<String, List<TransactionDTO>> transactionsByMonth = new HashMap<>();
				
				while (currentYear.intValue() < endYear.intValue() || (currentYear.intValue() == endYear.intValue() && currentMonth.intValue() <= endMonth.intValue())) {
					List<TransactionDTO> transactionsOfMonth = this.transactionService.getTransactionsByApplication(currentMonth, currentYear);

					// Formatear la clave en formato MM/YYYY
				    String key = String.format("%02d/%04d", currentMonth, currentYear);
				    
				    // Agregar la lista al Map con la clave correspondiente
				    transactionsByMonth.put(key, transactionsOfMonth);
					
				    // Incrementamos el mes
				    currentMonth++;
				    if (currentMonth > 12) {
				        currentMonth = 1;
				        currentYear++;
				    }
				}

				model.addAttribute("queryStatisticsByMonth", transactionsByMonth);
				model.addAttribute("isQueryByAppOrProvider", Boolean.TRUE);
				model.addAttribute("queryStatisticsResult", transactions);
				model.addAttribute("textGood", Constants.TRANS_CORRECTAS.concat(UtilsStringChar.SPECIAL_BLANK_SPACE_STRING).concat(Constants.QUERYBYTYPE_APP));
				model.addAttribute("textBad", Constants.TRANS_INCORRECTAS.concat(UtilsStringChar.SPECIAL_BLANK_SPACE_STRING).concat(Constants.QUERYBYTYPE_APP));
				model.addAttribute("queryenum", QueryEnum.TRANSACTIONS_ENDED_BY_APP.getId());
				model.addAttribute("enableDonutChart", Boolean.TRUE);
				model.addAttribute("enableBarChart", Boolean.TRUE);
				model.addAttribute("enableBarTimeChart", Boolean.TRUE);
			}
			if (query.equalsIgnoreCase(QueryEnum.TRANSACTIONS_ENDED_BY_PROVIDER.getName())) {
				transactions = StreamSupport.stream(this.transactionService.getTransactionsByProvider(month, year, endMonth, endYear).spliterator(), false).collect(Collectors.toList());
				
				Map<String, List<TransactionDTO>> transactionsByMonth = new HashMap<>();

				while (currentYear.intValue() < endYear.intValue() || (currentYear.intValue() == endYear.intValue() && currentMonth.intValue() <= endMonth.intValue())) {
					List<TransactionDTO> transactionsOfMonth = this.transactionService.getTransactionsByProvider(currentMonth, currentYear);
				    
					// Formatear la clave en formato MM/YYYY
				    String key = String.format("%02d/%04d", currentMonth, currentYear);
				    
				    // Agregar la lista al Map con la clave correspondiente
				    transactionsByMonth.put(key, transactionsOfMonth);
					
				    // Incrementamos el mes
				    currentMonth++;
				    if (currentMonth > 12) {
				        currentMonth = 1;
				        currentYear++;
				    }
				}
				
				model.addAttribute("queryStatisticsByMonth", transactionsByMonth);
				model.addAttribute("isQueryByAppOrProvider", Boolean.TRUE);
				model.addAttribute("queryStatisticsResult", transactions);
				model.addAttribute("textGood", Constants.TRANS_CORRECTAS.concat(UtilsStringChar.SPECIAL_BLANK_SPACE_STRING).concat(Constants.QUERYBYTYPE_PROVEEDOR));
				model.addAttribute("textBad", Constants.TRANS_INCORRECTAS.concat(UtilsStringChar.SPECIAL_BLANK_SPACE_STRING).concat(Constants.QUERYBYTYPE_PROVEEDOR));
				model.addAttribute("queryenum", QueryEnum.TRANSACTIONS_ENDED_BY_PROVIDER.getId());
				model.addAttribute("enableDonutChart", Boolean.TRUE);
				model.addAttribute("enableBarChart", Boolean.TRUE);
				model.addAttribute("enableBarTimeChart", Boolean.TRUE);
			}
			if (query.equalsIgnoreCase(QueryEnum.TRANSACTIONS_BY_DATES_SIZE_APP.getName())) {
				transactions = StreamSupport.stream(this.transactionService.getTransactionsByDatesSizeApp(month, year, endMonth, endYear).spliterator(), false).collect(Collectors.toList());

				Map<String, List<TransactionDTO>> transactionsByMonth = new HashMap<>();

				while (currentYear.intValue() < endYear.intValue() || (currentYear.intValue() == endYear.intValue() && currentMonth.intValue() <= endMonth.intValue())) {
					List<TransactionDTO> transactionsOfMonth = this.transactionService.getTransactionsByDatesSizeApp(currentMonth, currentYear);

					// Formatear la clave en formato MM/YYYY
				    String key = String.format("%02d/%04d", currentMonth, currentYear);
				    
				    // Agregar la lista al Map con la clave correspondiente
				    transactionsByMonth.put(key, transactionsOfMonth);
					
				    // Incrementamos el mes
				    currentMonth++;
				    if (currentMonth > 12) {
				        currentMonth = 1;
				        currentYear++;
				    }
				}
				
				model.addAttribute("queryStatisticsByMonth", transactionsByMonth);
				model.addAttribute("isQueryByDatesSize", Boolean.TRUE);
				model.addAttribute("queryStatisticsResult", transactions);
				model.addAttribute("queryenum", QueryEnum.TRANSACTIONS_BY_DATES_SIZE_APP.getId());
				model.addAttribute("enableDonutChart", Boolean.FALSE);
				model.addAttribute("enableBarChart", Boolean.TRUE);
				model.addAttribute("enableBarTimeChart", Boolean.TRUE);
			}
			if (query.equalsIgnoreCase(QueryEnum.TRANSACTIONS_BY_TYPE_TRANSACTION.getName())) {
				transactions = StreamSupport.stream(this.transactionService.getTransactionsByOperation(month, year, endMonth, endYear).spliterator(), false).collect(Collectors.toList());

				Map<String, List<TransactionDTO>> transactionsByMonth = new HashMap<>();

				while (currentYear.intValue() < endYear.intValue() || (currentYear.intValue() == endYear.intValue() && currentMonth.intValue() <= endMonth.intValue())) {
					List<TransactionDTO> transactionsOfMonth = this.transactionService.getTransactionsByOperation(currentMonth, currentYear);

					// Formatear la clave en formato MM/YYYY
				    String key = String.format("%02d/%04d", currentMonth, currentYear);
				    
				    // Agregar la lista al Map con la clave correspondiente
				    transactionsByMonth.put(key, transactionsOfMonth);
					
				    // Incrementamos el mes
				    currentMonth++;
				    if (currentMonth > 12) {
				        currentMonth = 1;
				        currentYear++;
				    }
				}

				model.addAttribute("queryStatisticsByMonth", transactionsByMonth);
				model.addAttribute("isQueryByOperation", Boolean.TRUE);
				model.addAttribute("queryStatisticsResult", transactions);
				model.addAttribute("queryenum", QueryEnum.TRANSACTIONS_BY_TYPE_TRANSACTION.getId());
				model.addAttribute("enableDonutChart", Boolean.TRUE);
				model.addAttribute("enableBarChart", Boolean.TRUE);
				model.addAttribute("enableBarTimeChart", Boolean.TRUE);
			}
			if (query.equalsIgnoreCase(QueryEnum.TRANSACTIONS_ENDED_BY_ORGANISM.getName())) {
				transactions = StreamSupport.stream(this.transactionService.getTransactionsByOrganism(month, year, endMonth, endYear).spliterator(), false).collect(Collectors.toList());

				Map<String, List<TransactionDTO>> transactionsByMonth = new HashMap<>();
				
				while (currentYear.intValue() < endYear.intValue() || (currentYear.intValue() == endYear.intValue() && currentMonth.intValue() <= endMonth.intValue())) {
					List<TransactionDTO> transactionsOfMonth = this.transactionService.getTransactionsByOrganism(currentMonth, currentYear);

					// Formatear la clave en formato MM/YYYY
				    String key = String.format("%02d/%04d", currentMonth, currentYear);
				    
				    // Agregar la lista al Map con la clave correspondiente
				    transactionsByMonth.put(key, transactionsOfMonth);
					
				    // Incrementamos el mes
				    currentMonth++;
				    if (currentMonth > 12) {
				        currentMonth = 1;
				        currentYear++;
				    }
				}

				model.addAttribute("queryStatisticsByMonth", transactionsByMonth);
				model.addAttribute("isQueryByAppOrProvider", Boolean.TRUE);
				model.addAttribute("queryStatisticsResult", transactions);
				model.addAttribute("textGood", Constants.TRANS_CORRECTAS.concat(UtilsStringChar.SPECIAL_BLANK_SPACE_STRING).concat(Constants.QUERYBYTYPE_APP));
				model.addAttribute("textBad", Constants.TRANS_INCORRECTAS.concat(UtilsStringChar.SPECIAL_BLANK_SPACE_STRING).concat(Constants.QUERYBYTYPE_APP));
				model.addAttribute("queryenum", QueryEnum.TRANSACTIONS_ENDED_BY_ORGANISM.getId());
				model.addAttribute("enableDonutChart", Boolean.TRUE);
				model.addAttribute("enableBarChart", Boolean.TRUE);
				model.addAttribute("enableBarTimeChart", Boolean.TRUE);
			}
			
			// Consulta de firmas
			if (query.equalsIgnoreCase(QueryEnum.DOCUMENTS_SIGNED_BY_APP.getName())) {
				signatures = StreamSupport.stream(this.signatureService.getSignaturesByApplication(month, year, endMonth, endYear).spliterator(), false).collect(Collectors.toList());
				
				Map<String, List<SignatureDTO>> signaturesByMonth = new HashMap<>();

				while (currentYear.intValue() < endYear.intValue() || (currentYear.intValue() == endYear.intValue() && currentMonth.intValue() <= endMonth.intValue())) {
				    // Obtener la lista de firmas para el mes y año actual
				    List<SignatureDTO> signaturesOfMonth = this.signatureService.getSignaturesByApplication(currentMonth, currentYear);
				    
				    // Formatear la clave en formato MM/YYYY
				    String key = String.format("%02d/%04d", currentMonth, currentYear);
				    
				    // Agregar la lista al Map con la clave correspondiente
				    signaturesByMonth.put(key, signaturesOfMonth);
				    
				    // Incrementar el mes y, si es necesario, el año
				    currentMonth++;
				    if (currentMonth > 12) {
				        currentMonth = 1;
				        currentYear++;
				    }
				}

				model.addAttribute("queryStatisticsByMonth", signaturesByMonth);
				model.addAttribute("isSignatureQuery", Boolean.TRUE);
				model.addAttribute("queryStatisticsResult", signatures);
				model.addAttribute("queryenum", QueryEnum.DOCUMENTS_SIGNED_BY_APP.getId());
				model.addAttribute("enableDonutChart", Boolean.TRUE);
				model.addAttribute("enableBarChart", Boolean.TRUE);
				model.addAttribute("enableBarTimeChart", Boolean.TRUE);
			}
			if (query.equalsIgnoreCase(QueryEnum.DOCUMENTS_SIGNED_BY_PROVIDER.getName())) {
				signatures = StreamSupport.stream(this.signatureService.getSignaturesByProvider(month, year, endMonth, endYear).spliterator(), false).collect(Collectors.toList());
				
				Map<String, List<SignatureDTO>> signaturesByMonth = new HashMap<>();
				
				while (currentYear.intValue() < endYear.intValue() || (currentYear.intValue() == endYear.intValue() && currentMonth.intValue() <= endMonth.intValue())) {
					List<SignatureDTO> signaturesOfMonth = this.signatureService.getSignaturesByProvider(currentMonth, currentYear);
					
					// Formatear la clave en formato MM/YYYY
				    String key = String.format("%02d/%04d", currentMonth, currentYear);
				    
				    // Agregar la lista al Map con la clave correspondiente
				    signaturesByMonth.put(key, signaturesOfMonth);
				    
				    // Incrementamos el mes
				    currentMonth++;
				    if (currentMonth > 12) {
				        currentMonth = 1;
				        currentYear++;
				    }
				}
				
				model.addAttribute("queryStatisticsByMonth", signaturesByMonth);
				model.addAttribute("isSignatureQuery", Boolean.TRUE);
				model.addAttribute("queryStatisticsResult", signatures);
				model.addAttribute("queryenum", QueryEnum.DOCUMENTS_SIGNED_BY_PROVIDER.getId());
				model.addAttribute("enableDonutChart", Boolean.TRUE);
				model.addAttribute("enableBarChart", Boolean.TRUE);
				model.addAttribute("enableBarTimeChart", Boolean.TRUE);
			}
			if (query.equalsIgnoreCase(QueryEnum.DOCUMENTS_SIGNED_BY_SIGNATURE_FORMAT.getName())) {
				signatures = StreamSupport.stream(this.signatureService.getSignaturesByFormat(month, year, endMonth, endYear).spliterator(), false).collect(Collectors.toList());
				
				Map<String, List<SignatureDTO>> signaturesByMonth = new HashMap<>();

				while (currentYear.intValue() < endYear.intValue() || (currentYear.intValue() == endYear.intValue() && currentMonth.intValue() <= endMonth.intValue())) {
					List<SignatureDTO> signaturesOfMonth = this.signatureService.getSignaturesByFormat(currentMonth, currentYear);

					// Formatear la clave en formato MM/YYYY
				    String key = String.format("%02d/%04d", currentMonth, currentYear);
				    
				    // Agregar la lista al Map con la clave correspondiente
				    signaturesByMonth.put(key, signaturesOfMonth);
				    
				    // Incrementamos el mes
				    currentMonth++;
				    if (currentMonth > 12) {
				        currentMonth = 1;
				        currentYear++;
				    }
				}

				model.addAttribute("queryStatisticsByMonth", signaturesByMonth);
				model.addAttribute("isSignatureQuery", Boolean.TRUE);
				model.addAttribute("queryStatisticsResult", signatures);
				model.addAttribute("queryenum", QueryEnum.DOCUMENTS_SIGNED_BY_SIGNATURE_FORMAT.getId());
				model.addAttribute("enableDonutChart", Boolean.TRUE);
				model.addAttribute("enableBarChart", Boolean.TRUE);
				model.addAttribute("enableBarTimeChart", Boolean.TRUE);
			}
			if (query.equalsIgnoreCase(QueryEnum.DOCUMENTS_USED_IN_SIGNATURE_FORMAT.getName())) {
				signatures = StreamSupport.stream(this.signatureService.getSignaturesByImprovedFormat(month, year, endMonth, endYear).spliterator(), false).collect(Collectors.toList());
				
				Map<String, List<SignatureDTO>> signaturesByMonth = new HashMap<>();

				while (currentYear.intValue() < endYear.intValue() || (currentYear.intValue() == endYear.intValue() && currentMonth.intValue() <= endMonth.intValue())) {
					List<SignatureDTO> signaturesOfMonth = this.signatureService.getSignaturesByImprovedFormat(currentMonth, currentYear);

					// Formatear la clave en formato MM/YYYY
				    String key = String.format("%02d/%04d", currentMonth, currentYear);
				    
				    // Agregar la lista al Map con la clave correspondiente
				    signaturesByMonth.put(key, signaturesOfMonth);
				    
				    // Incrementamos el mes
				    currentMonth++;
				    if (currentMonth > 12) {
				        currentMonth = 1;
				        currentYear++;
				    }
				}

				model.addAttribute("queryStatisticsByMonth", signaturesByMonth);
				model.addAttribute("isSignatureQuery", Boolean.TRUE);
				model.addAttribute("queryStatisticsResult", signatures);
				model.addAttribute("queryenum", QueryEnum.DOCUMENTS_USED_IN_SIGNATURE_FORMAT.getId());
				model.addAttribute("enableDonutChart", Boolean.TRUE);
				model.addAttribute("enableBarChart", Boolean.TRUE);
				model.addAttribute("enableBarTimeChart", Boolean.TRUE);
			}
			if (query.equalsIgnoreCase(QueryEnum.DOCUMENTS_SIGNED_BY_ORGANISM.getName())) {
				signatures = StreamSupport.stream(this.signatureService.getSignaturesByOrganism(month, year, endMonth, endYear).spliterator(), false).collect(Collectors.toList());
				
				Map<String, List<SignatureDTO>> signaturesByMonth = new HashMap<>();

				while (currentYear.intValue() < endYear.intValue() || (currentYear.intValue() == endYear.intValue() && currentMonth.intValue() <= endMonth.intValue())) {
				    // Obtener la lista de firmas para el mes y año actual
				    List<SignatureDTO> signaturesOfMonth = this.signatureService.getSignaturesByOrganism(currentMonth, currentYear);
				    
				    // Formatear la clave en formato MM/YYYY
				    String key = String.format("%02d/%04d", currentMonth, currentYear);
				    
				    // Agregar la lista al Map con la clave correspondiente
				    signaturesByMonth.put(key, signaturesOfMonth);
				    
				    // Incrementar el mes y, si es necesario, el año
				    currentMonth++;
				    if (currentMonth > 12) {
				        currentMonth = 1;
				        currentYear++;
				    }
				}

				model.addAttribute("queryStatisticsByMonth", signaturesByMonth);
				model.addAttribute("isSignatureQuery", Boolean.TRUE);
				model.addAttribute("queryStatisticsResult", signatures);
				model.addAttribute("queryenum", QueryEnum.DOCUMENTS_SIGNED_BY_ORGANISM.getId());
				model.addAttribute("enableDonutChart", Boolean.TRUE);
				model.addAttribute("enableBarChart", Boolean.TRUE);
				model.addAttribute("enableBarTimeChart", Boolean.TRUE);
			}
			model.addAttribute("isDateRange", Boolean.TRUE);
			model.addAttribute("tableStatisticsTitle", query + " para el intervalo de meses " + monthDate + " a " + endMonthDate);
		}
        return "fragments/querystatisticstable.html";
    }
	
	private Long getMaxEntitiesBeforeGrouping() {
		//Devolvemos como valor por defecto 10
		Long maxEntitiesBeforeGrouping = 10L;

		Optional<Property> opt = propertyService.getPropertyByKey(PropertyService.PROPERTY_NAME_MAX_ENTITIES_BEFORE_GROUPING);

		if (opt.isPresent()) {
			Property res = opt.get();
			
			maxEntitiesBeforeGrouping = res.getNumericValue() != null ? res.getNumericValue() : 10L;
		}
		
		return maxEntitiesBeforeGrouping;
	}
}
