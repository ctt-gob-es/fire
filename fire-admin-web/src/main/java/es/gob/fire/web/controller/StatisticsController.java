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
 * @version 1.0, 14/04/2020.
 */
package es.gob.fire.web.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.servlet.http.HttpServletResponse;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.title.TextTitle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lowagie.text.DocumentException;

import es.gob.fire.commons.log.Logger;
import es.gob.fire.commons.utils.Constants;
import es.gob.fire.commons.utils.NumberConstants;
import es.gob.fire.commons.utils.QueryEnum;
import es.gob.fire.commons.utils.UtilsStringChar;
import es.gob.fire.i18n.IWebViewMessages;
import es.gob.fire.i18n.Language;
import es.gob.fire.persistence.dto.SignatureDTO;
import es.gob.fire.persistence.dto.TransactionDTO;
import es.gob.fire.persistence.service.ISignatureService;
import es.gob.fire.persistence.service.ITransactionService;
import es.gob.fire.service.IStatisticsService;

/**
 * <p>Class that manages the requests related to the statistics administration.</p>
 * <b>Project:</b><p>Application for signing documents of @firma suite systems.</p>
 * @version 1.0, 14/04/2020.
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
	 * Attribute that represents the statistics service.
	 */
	@Autowired
	private IStatisticsService statService;
	
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
    public String queryVipTimeRange(final Model model){

		List<QueryEnum> queries = new ArrayList<>();

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
    public String statisticsResult(final Model model, final @RequestParam("query") String query, final @RequestParam("monthDate") String monthDate) {
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
				model.addAttribute("textGood", Constants.TRANS_CORRECTAS.concat(UtilsStringChar.SPECIAL_BLANK_SPACE_STRING).concat(Constants.QUERYBYTYPE_APP));
				model.addAttribute("textBad", Constants.TRANS_INCORRECTAS.concat(UtilsStringChar.SPECIAL_BLANK_SPACE_STRING).concat(Constants.QUERYBYTYPE_APP));
				model.addAttribute("queryenum", QueryEnum.TRANSACTIONS_ENDED_BY_APP.getId());
			}
			if (query.equalsIgnoreCase(QueryEnum.TRANSACTIONS_ENDED_BY_PROVIDER.getName())) {
				transactions = StreamSupport.stream(this.transactionService.getTransactionsByProvider(month, year).spliterator(), false).collect(Collectors.toList());
				model.addAttribute("isQueryByAppOrProvider", Boolean.TRUE);
				model.addAttribute("queryStatisticsResult", transactions);
				model.addAttribute("textGood", Constants.TRANS_CORRECTAS.concat(UtilsStringChar.SPECIAL_BLANK_SPACE_STRING).concat(Constants.QUERYBYTYPE_PROVEEDOR));
				model.addAttribute("textBad", Constants.TRANS_INCORRECTAS.concat(UtilsStringChar.SPECIAL_BLANK_SPACE_STRING).concat(Constants.QUERYBYTYPE_PROVEEDOR));
				model.addAttribute("queryenum", QueryEnum.TRANSACTIONS_ENDED_BY_PROVIDER.getId());
			}
			if (query.equalsIgnoreCase(QueryEnum.TRANSACTIONS_BY_DATES_SIZE_APP.getName())) {
				transactions = StreamSupport.stream(this.transactionService.getTransactionsByDatesSizeApp(month, year).spliterator(), false).collect(Collectors.toList());
				model.addAttribute("isQueryByDatesSize", Boolean.TRUE);
				model.addAttribute("queryStatisticsResult", transactions);
				model.addAttribute("queryenum", QueryEnum.TRANSACTIONS_BY_DATES_SIZE_APP.getId());
			}
			if (query.equalsIgnoreCase(QueryEnum.TRANSACTIONS_BY_TYPE_TRANSACTION.getName())) {
				transactions = StreamSupport.stream(this.transactionService.getTransactionsByOperation(month, year).spliterator(), false).collect(Collectors.toList());
				model.addAttribute("isQueryByOperation", Boolean.TRUE);
				model.addAttribute("queryStatisticsResult", transactions);
				model.addAttribute("queryenum", QueryEnum.TRANSACTIONS_BY_TYPE_TRANSACTION.getId());
			}
			// Consulta de firmas
			if (query.equalsIgnoreCase(QueryEnum.DOCUMENTS_SIGNED_BY_APP.getName())) {
				signatures = StreamSupport.stream(this.signatureService.getSignaturesByApplication(month, year).spliterator(), false).collect(Collectors.toList());
				model.addAttribute("isSignatureQuery", Boolean.TRUE);
				model.addAttribute("queryStatisticsResult", signatures);
				model.addAttribute("queryenum", QueryEnum.DOCUMENTS_SIGNED_BY_APP.getId());
			}
			if (query.equalsIgnoreCase(QueryEnum.DOCUMENTS_SIGNED_BY_PROVIDER.getName())) {
				signatures = StreamSupport.stream(this.signatureService.getSignaturesByProvider(month, year).spliterator(), false).collect(Collectors.toList());
				model.addAttribute("isSignatureQuery", Boolean.TRUE);
				model.addAttribute("queryStatisticsResult", signatures);
				model.addAttribute("queryenum", QueryEnum.DOCUMENTS_SIGNED_BY_PROVIDER.getId());
			}
			if (query.equalsIgnoreCase(QueryEnum.DOCUMENTS_SIGNED_BY_SIGNATURE_FORMAT.getName())) {
				signatures = StreamSupport.stream(this.signatureService.getSignaturesByFormat(month, year).spliterator(), false).collect(Collectors.toList());
				model.addAttribute("isSignatureQuery", Boolean.TRUE);
				model.addAttribute("queryStatisticsResult", signatures);
				model.addAttribute("queryenum", QueryEnum.DOCUMENTS_SIGNED_BY_SIGNATURE_FORMAT.getId());
			}
			if (query.equalsIgnoreCase(QueryEnum.DOCUMENTS_USED_IN_SIGNATURE_FORMAT.getName())) {
				signatures = StreamSupport.stream(this.signatureService.getSignaturesByImprovedFormat(month, year).spliterator(), false).collect(Collectors.toList());
				model.addAttribute("isSignatureQuery", Boolean.TRUE);
				model.addAttribute("queryStatisticsResult", signatures);
				model.addAttribute("queryenum", QueryEnum.DOCUMENTS_USED_IN_SIGNATURE_FORMAT.getId());
			}
			model.addAttribute("tableStatisticsTitle", query + " para el mes " + monthDate);

			/*Map<String, Integer> data = new LinkedHashMap<String, Integer>();
	        data.put("Prueba", 3);
	        data.put("Ruby", 20);
	        data.put("Python", 30);

	        model.addAttribute("data", data);*/
		}
        return "fragments/querystatisticstable.html";
    }


	/**
	 * Method that maps the queries for service status between a time range.
	 * @param model Holder object for model attributes.
	 * @param query query
	 * @param month month
	 * @return String that represents the name of the view to forward.
	 */
	@RequestMapping(value = "exportPDF", method = RequestMethod.POST)
    public @ResponseBody byte[] exportPDF(final Model model, final @RequestParam("query") String query, final @RequestParam("monthDate") String monthDate, final HttpServletResponse response) throws IOException {

		List<TransactionDTO> transactions = null;
		List<SignatureDTO> signatures = null;

		byte[] data = null;

		if (!StringUtils.isEmpty(query) && !StringUtils.isEmpty(monthDate)) {
			final Integer month = Integer.valueOf(monthDate.substring(0, NumberConstants.NUM2));
			final Integer year = Integer.valueOf(monthDate.substring(NumberConstants.NUM3, NumberConstants.NUM7));
			// Consultas de transacciones
			if (query.equalsIgnoreCase(QueryEnum.TRANSACTIONS_ENDED_BY_APP.getName())) {

				transactions = StreamSupport.stream(this.transactionService.getTransactionsByApplication(month, year).spliterator(), false).collect(Collectors.toList());
				final JFreeChart correctas = this.statService.getChartTransactions(transactions, Constants.CORRECTAS);
				final JFreeChart incorrectas = this.statService.getChartTransactions(transactions, Constants.INCORRECTAS);

				correctas.addSubtitle(new TextTitle(Language.getResWebFire(IWebViewMessages.STAT_SUBTITLE_TRANS_ENDED_BY_APP_CORRECT)));
				incorrectas.addSubtitle(new TextTitle(Language.getResWebFire(IWebViewMessages.STAT_SUBTITLE_TRANS_ENDED_BY_APP_INCORRECT)));

				final List<JFreeChart> chartList = new ArrayList<>();
				chartList.add(correctas);
				chartList.add(incorrectas);

				try {
					data = this.statService.writeTransStatAsPDF(250, 195, chartList, transactions, NumberConstants.NUM1, Language.getResWebFire(IWebViewMessages.STAT_TITLE_TRANS_ENDED_BY_APP) + monthDate);
				} catch (final DocumentException e) {
					LOGGER.error("Ha ocurrido un error durante la operacion. Excepcion: " + e);
				}
			} else if (query.equalsIgnoreCase(QueryEnum.TRANSACTIONS_ENDED_BY_PROVIDER.getName())) {

				transactions = StreamSupport.stream(this.transactionService.getTransactionsByProvider(month, year).spliterator(), false).collect(Collectors.toList());
				final JFreeChart correctas = this.statService.getChartTransactions(transactions, Constants.CORRECTAS);
				final JFreeChart incorrectas = this.statService.getChartTransactions(transactions, Constants.INCORRECTAS);

				correctas.addSubtitle(new TextTitle(Language.getResWebFire(IWebViewMessages.STAT_SUBTITLE_TRANS_ENDED_BY_PROV_CORRECT)));
				incorrectas.addSubtitle(new TextTitle(Language.getResWebFire(IWebViewMessages.STAT_SUBTITLE_TRANS_ENDED_BY_PROV_INCORRECT)));

				final List<JFreeChart> chartList = new ArrayList<>();
				chartList.add(correctas);
				chartList.add(incorrectas);

				try {
					data = this.statService.writeTransStatAsPDF(250, 195, chartList, transactions, NumberConstants.NUM1, Language.getResWebFire(IWebViewMessages.STAT_TITLE_TRANS_ENDED_BY_PROV) + monthDate);
				} catch (final DocumentException e) {
					LOGGER.error("Ha ocurrido un error durante la operacion. Excepcion: " + e);
				}

			} else if (query.equalsIgnoreCase(QueryEnum.TRANSACTIONS_BY_DATES_SIZE_APP.getName())) {
				transactions = StreamSupport.stream(this.transactionService.getTransactionsByDatesSizeApp(month, year).spliterator(), false).collect(Collectors.toList());
				final JFreeChart size = this.statService.getChartTransactions(transactions, Constants.SIZE);

				size.addSubtitle(new TextTitle(Language.getResWebFire(IWebViewMessages.STAT_SUBTITLE_TRANS_ENDED_BY_SIZE)));

				final List<JFreeChart> chartList = new ArrayList<>();
				chartList.add(size);

				try {
					data = this.statService.writeTransStatAsPDF(375, 293, chartList, transactions, NumberConstants.NUM3, Language.getResWebFire(IWebViewMessages.STAT_TITLE_TRANS_ENDED_BY_SIZE) + monthDate);
				} catch (final DocumentException e) {
					LOGGER.error("Ha ocurrido un error durante la operacion. Excepcion: " + e);
				}

			} else if (query.equalsIgnoreCase(QueryEnum.TRANSACTIONS_BY_TYPE_TRANSACTION.getName())) {

				transactions = StreamSupport.stream(this.transactionService.getTransactionsByOperation(month, year).spliterator(), false).collect(Collectors.toList());

				final JFreeChart correctsimple = this.statService.getChartTransactions(transactions, Constants.CORRECTAS_SIMPLE);
				final JFreeChart incorrectsimple = this.statService.getChartTransactions(transactions, Constants.INCORRECTAS_SIMPLE);
				final JFreeChart correctlote = this.statService.getChartTransactions(transactions, Constants.CORRECTAS_LOTE);
				final JFreeChart incorrectlote = this.statService.getChartTransactions(transactions, Constants.INCORRECTAS_LOTE);

				correctsimple.addSubtitle(new TextTitle(Language.getResWebFire(IWebViewMessages.STAT_SUBTITLE_TRANS_ENDED_BY_TYPE_CORRECT_SIMPLE)));
				incorrectsimple.addSubtitle(new TextTitle(Language.getResWebFire(IWebViewMessages.STAT_SUBTITLE_TRANS_ENDED_BY_TYPE_INCORRECT_SIMPLE)));
				correctlote.addSubtitle(new TextTitle(Language.getResWebFire(IWebViewMessages.STAT_SUBTITLE_TRANS_ENDED_BY_TYPE_CORRECT_BATCH)));
				incorrectlote.addSubtitle(new TextTitle(Language.getResWebFire(IWebViewMessages.STAT_SUBTITLE_TRANS_ENDED_BY_TYPE_INCORRECT_BATCH)));

				final List<JFreeChart> chartList = new ArrayList<>();

				chartList.add(correctsimple);
				chartList.add(incorrectsimple);
				chartList.add(correctlote);
				chartList.add(incorrectlote);

				final List<String> titles = new ArrayList<>();
				titles.add(Language.getResWebFire(IWebViewMessages.STAT_TITLE_TRANS_ENDED_BY_TYPE_SIMPLE) + monthDate);
				titles.add(Language.getResWebFire(IWebViewMessages.STAT_TITLE_TRANS_ENDED_BY_TYPE_BATCH) + monthDate);

				try {
					data = this.statService.writeTransStatCompositeAsPDF(250, 195, chartList, transactions, NumberConstants.NUM2, titles);
				} catch (final DocumentException e) {
					LOGGER.error("Ha ocurrido un error durante la operacion. Excepcion: " + e);
				}
			}
			// Consulta de firmas
			else if (query.equalsIgnoreCase(QueryEnum.DOCUMENTS_SIGNED_BY_APP.getName())) {
				signatures = StreamSupport.stream(this.signatureService.getSignaturesByApplication(month, year).spliterator(), false).collect(Collectors.toList());

				final JFreeChart correctas = this.statService.getChartSignatures(signatures, Constants.CORRECTAS);
				final JFreeChart incorrectas = this.statService.getChartSignatures(signatures, Constants.INCORRECTAS);

				correctas.addSubtitle(new TextTitle(Language.getResWebFire(IWebViewMessages.STAT_SUBTITLE_SIG_ENDED_BY_APP_CORRECT)));
				incorrectas.addSubtitle(new TextTitle(Language.getResWebFire(IWebViewMessages.STAT_SUBTITLE_SIG_ENDED_BY_APP_INCORRECT)));

				final List<JFreeChart> chartList = new ArrayList<>();
				chartList.add(correctas);
				chartList.add(incorrectas);

				try {
					data = this.statService.writeSigStatAsPDF(250, 195, chartList, signatures, Language.getResWebFire(IWebViewMessages.STAT_TITLE_SIG_ENDED_BY_APP) + monthDate);
				} catch (final DocumentException e) {
					LOGGER.error("Ha ocurrido un error durante la operacion. Excepcion: " + e);
				}

			} else if (query.equalsIgnoreCase(QueryEnum.DOCUMENTS_SIGNED_BY_PROVIDER.getName())) {
				signatures = StreamSupport.stream(this.signatureService.getSignaturesByProvider(month, year).spliterator(), false).collect(Collectors.toList());

				final JFreeChart correctas = this.statService.getChartSignatures(signatures, Constants.CORRECTAS);
				final JFreeChart incorrectas = this.statService.getChartSignatures(signatures, Constants.INCORRECTAS);

				correctas.addSubtitle(new TextTitle(Language.getResWebFire(IWebViewMessages.STAT_SUBTITLE_SIG_ENDED_BY_PROV_CORRECT)));
				incorrectas.addSubtitle(new TextTitle(Language.getResWebFire(IWebViewMessages.STAT_SUBTITLE_SIG_ENDED_BY_PROV_INCORRECT)));

				final List<JFreeChart> chartList = new ArrayList<>();
				chartList.add(correctas);
				chartList.add(incorrectas);

				try {
					data = this.statService.writeSigStatAsPDF(250, 195, chartList, signatures, Language.getResWebFire(IWebViewMessages.STAT_TITLE_SIG_ENDED_BY_PROV) + monthDate);
				} catch (final DocumentException e) {
					LOGGER.error("Ha ocurrido un error durante la operacion. Excepcion: " + e);
				}

			} else if (query.equalsIgnoreCase(QueryEnum.DOCUMENTS_SIGNED_BY_SIGNATURE_FORMAT.getName())) {
				signatures = StreamSupport.stream(this.signatureService.getSignaturesByFormat(month, year).spliterator(), false).collect(Collectors.toList());

				final JFreeChart correctas = this.statService.getChartSignatures(signatures, Constants.CORRECTAS);
				final JFreeChart incorrectas = this.statService.getChartSignatures(signatures, Constants.INCORRECTAS);

				correctas.addSubtitle(new TextTitle(Language.getResWebFire(IWebViewMessages.STAT_SUBTITLE_SIG_ENDED_BY_FORMAT_CORRECT)));
				incorrectas.addSubtitle(new TextTitle(Language.getResWebFire(IWebViewMessages.STAT_SUBTITLE_SIG_ENDED_BY_FORMAT_INCORRECT)));

				final List<JFreeChart> chartList = new ArrayList<>();
				chartList.add(correctas);
				chartList.add(incorrectas);

				try {
					data = this.statService.writeSigStatAsPDF(250, 195, chartList, signatures, Language.getResWebFire(IWebViewMessages.STAT_TITLE_SIG_ENDED_BY_FORMAT) + monthDate);
				} catch (final DocumentException e) {
					LOGGER.error("Ha ocurrido un error durante la operacion. Excepcion: " + e);
				}

			} else if (query.equalsIgnoreCase(QueryEnum.DOCUMENTS_USED_IN_SIGNATURE_FORMAT.getName())) {
				signatures = StreamSupport.stream(this.signatureService.getSignaturesByImprovedFormat(month, year).spliterator(), false).collect(Collectors.toList());

				final JFreeChart correctas = this.statService.getChartSignatures(signatures, Constants.CORRECTAS);
				final JFreeChart incorrectas = this.statService.getChartSignatures(signatures, Constants.INCORRECTAS);

				correctas.addSubtitle(new TextTitle(Language.getResWebFire(IWebViewMessages.STAT_SUBTITLE_SIG_ENDED_BY_FORMAT_LONG_CORRECT)));
				incorrectas.addSubtitle(new TextTitle(Language.getResWebFire(IWebViewMessages.STAT_SUBTITLE_SIG_ENDED_BY_FORMAT_LONG_INCORRECT)));

				final List<JFreeChart> chartList = new ArrayList<>();
				chartList.add(correctas);
				chartList.add(incorrectas);

				try {
					data = this.statService.writeSigStatAsPDF(250, 195, chartList, signatures, Language.getResWebFire(IWebViewMessages.STAT_TITLE_SIG_ENDED_BY_FORMAT_LONG) + monthDate);
				} catch (final DocumentException e) {
					LOGGER.error("Ha ocurrido un error durante la operacion. Excepcion: " + e);
				}

			}
		}

		response.setContentType("application/pdf");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=firereport.pdf");

        if (data != null) {
        	data.toString();
        }

		return data;

	}


}
