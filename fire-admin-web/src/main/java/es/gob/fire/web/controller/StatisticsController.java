/* 
/*******************************************************************************
 * Copyright (C) 2018 MINHAFP, Gobierno de España
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
 * @author Gobierno de España.
 * @version 1.0, 14/04/2020.
 */
package es.gob.fire.web.controller;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import es.gob.fire.core.constant.NumberConstants;
import es.gob.fire.core.dto.SignatureDTO;
import es.gob.fire.core.dto.TransactionDTO;
import es.gob.fire.core.util.QueryEnum;
import es.gob.fire.persistence.service.ISignatureService;
import es.gob.fire.persistence.service.ITransactionService;

/** 
 * <p>Class that manages the requests related to the statistics administration.</p>
 * <b>Project:</b><p>Application for signing documents of @firma suite systems.</p>
 * @version 1.0, 14/04/2020.
 */
@Controller
public class StatisticsController {

	/**
	 * Attribute that represents the object that manages the log of the class.
	 */
	private static final Logger LOGGER = Logger.getLogger(StatisticsController.class);
	
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
	 * Method that maps the list users web requests to the controller and forwards the list of platforms
	 * to the view.  
	 * @param model Holder object for model attributes.
	 * @return String that represents the name of the view to forward.
	 */
	@RequestMapping(value = "statistics", method = RequestMethod.GET)
    public String queryVipTimeRange(final Model model){
		
		List<QueryEnum> queries = new ArrayList<QueryEnum>();
		
		queries = StreamSupport.stream(EnumSet.allOf(QueryEnum.class).spliterator(), false)
				.collect(Collectors.toList());
		
		model.addAttribute("queries", queries);
		
        return "fragments/statistics.html";
    }
	
	/**
	 * Method that maps the queries for service status between a time range.
	 * @param model Holder object for model attributes.
	 * @param query query
	 * @param month month
	 * @return String that represents the name of the view to forward.
	 */
	@RequestMapping(value = "statisticsresult", method = RequestMethod.GET)
    public String queryVipTime(final Model model, final @RequestParam("query") String query, final @RequestParam("monthDate") String monthDate) {
		List<TransactionDTO> transactions = null;
		List<SignatureDTO> signatures = null;
		
		if (!StringUtils.isEmpty(query) && !StringUtils.isEmpty(monthDate)) {
			final Integer month = Integer.valueOf(monthDate.substring(0, NumberConstants.NUM2));
			final Integer year = Integer.valueOf(monthDate.substring(NumberConstants.NUM3, NumberConstants.NUM7));
			// Consultas de transacciones
			if (query.equalsIgnoreCase(QueryEnum.TRANSACTIONS_ENDED_BY_APP.getName())) {
				transactions = StreamSupport.stream(this.transactionService.getTransactionsByApplication(month, year).spliterator(), false).collect(Collectors.toList());
				model.addAttribute("isQueryByAppOrProvider", Boolean.TRUE);
				model.addAttribute("queryStatisticsResult", transactions);
			}
			if (query.equalsIgnoreCase(QueryEnum.TRANSACTIONS_ENDED_BY_PROVIDER.getName())) {
				transactions = StreamSupport.stream(this.transactionService.getTransactionsByProvider(month, year).spliterator(), false).collect(Collectors.toList());
				model.addAttribute("isQueryByAppOrProvider", Boolean.TRUE);
				model.addAttribute("queryStatisticsResult", transactions);
			}
			if (query.equalsIgnoreCase(QueryEnum.TRANSACTIONS_BY_DATES_SIZE_APP.getName())) {
				transactions = StreamSupport.stream(this.transactionService.getTransactionsByDatesSizeApp(month, year).spliterator(), false).collect(Collectors.toList());
				model.addAttribute("isQueryByDatesSize", Boolean.TRUE);
				model.addAttribute("queryStatisticsResult", transactions);
			}
			if (query.equalsIgnoreCase(QueryEnum.TRANSACTIONS_BY_TYPE_TRANSACTION.getName())) {
				transactions = StreamSupport.stream(this.transactionService.getTransactionsByOperation(month, year).spliterator(), false).collect(Collectors.toList());
				model.addAttribute("isQueryByOperation", Boolean.TRUE);
				model.addAttribute("queryStatisticsResult", transactions);
			}
			// Consulta de firmas
			if (query.equalsIgnoreCase(QueryEnum.DOCUMENTS_SIGNED_BY_APP.getName())) {
				signatures = StreamSupport.stream(this.signatureService.getSignaturesByApplication(month, year).spliterator(), false).collect(Collectors.toList());
				model.addAttribute("isSignatureQuery", Boolean.TRUE);
				model.addAttribute("queryStatisticsResult", signatures);
			}
			if (query.equalsIgnoreCase(QueryEnum.DOCUMENTS_SIGNED_BY_PROVIDER.getName())) {
				signatures = StreamSupport.stream(this.signatureService.getSignaturesByProvider(month, year).spliterator(), false).collect(Collectors.toList());
				model.addAttribute("isSignatureQuery", Boolean.TRUE);
				model.addAttribute("queryStatisticsResult", signatures);
			}
			if (query.equalsIgnoreCase(QueryEnum.DOCUMENTS_SIGNED_BY_SIGNATURE_FORMAT.getName())) {
				signatures = StreamSupport.stream(this.signatureService.getSignaturesByFormat(month, year).spliterator(), false).collect(Collectors.toList());
				model.addAttribute("isSignatureQuery", Boolean.TRUE);
				model.addAttribute("queryStatisticsResult", signatures);
			}
			if (query.equalsIgnoreCase(QueryEnum.DOCUMENTS_USED_IN_SIGNATURE_FORMAT.getName())) {
				signatures = StreamSupport.stream(this.signatureService.getSignaturesByImprovedFormat(month, year).spliterator(), false).collect(Collectors.toList());
				model.addAttribute("isSignatureQuery", Boolean.TRUE);
				model.addAttribute("queryStatisticsResult", signatures);
			}
			model.addAttribute("tableStatisticsTitle", query + " para el Mes " + monthDate);
			
			/*Map<String, Integer> data = new LinkedHashMap<String, Integer>();
	        data.put("Prueba", 3);
	        data.put("Ruby", 20);
	        data.put("Python", 30);

	        model.addAttribute("data", data);*/
		}
        return "fragments/querystatisticstable.html";
    }
}
