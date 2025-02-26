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
 * <b>File:</b><p>es.gob.fire.service.IStatisticsService.java.</p>
 * <b>Description:</b><p>Interface for generating statistics charts and exporting statistical data as PDF reports.</p>
 * <b>Project:</b><p>Horizontal platform of validation services of multiPKI certificates and electronic signature.</p>
 * <b>Date:</b><p> 22/01/2025.</p>
 * @author Gobierno de España.
 * @version 1.0, 22/01/2025.
 */
package es.gob.fire.service;

import java.util.List;

import org.jfree.chart.JFreeChart;

import com.aowagie.text.DocumentException;

import es.gob.fire.persistence.dto.SignatureDTO;
import es.gob.fire.persistence.dto.TransactionDTO;

/** 
 * <p>Interface for generating statistics charts and exporting statistical data as PDF reports.</p>
 * <b>Project:</b><p>Horizontal platform of validation services of multiPKI certificates and electronic signature.</p>
 * @version 1.0, 22/01/2025.
 */
public interface IStatisticsService {

	/**
	 * @param transactions
	 * @param field
	 * @return
	 */
	JFreeChart getChartTransactions(List<TransactionDTO> transactions, String field);


	/**
	 * @param signatures
	 * @param field
	 * @return
	 */
	JFreeChart getChartSignatures(List<SignatureDTO> signatures, String field);

	/**
	 * @param width
	 * @param height
	 * @param chartList
	 * @param stats
	 * @param tableType: 1 Tipo b&aacute;sico de correctas e incorrectas
	 * 					 2 Tipo compuesto (simple o lote) de correctas e incorrectas
	 * 				     3 Tipo tama&ntilde;os
	 * @param title
	 * @return
	 * @throws DocumentException
	 */
	byte[] writeTransStatAsPDF(int width, int height, List<JFreeChart> chartList, List<TransactionDTO> stats, int tableType, String title) throws DocumentException;

	/**
	 * @param width
	 * @param height
	 * @param chartList
	 * @param stats
	 * @param tableType: 1 Tipo b&aacute;sico de correctas e incorrectas
	 * 					 2 Tipo compuesto (simple o lote) de correctas e incorrectas
	 * 				     3 Tipo tama&ntilde;os
	 * @param title
	 * @return
	 * @throws DocumentException
	 */
	byte[] writeTransStatCompositeAsPDF(int width, int height, List<JFreeChart> chartList, List<TransactionDTO> stats, int tableType, List<String> titles) throws DocumentException;

	/**
	 * @param width
	 * @param height
	 * @param chartList
	 * @param stats
	 * @param title
	 * @return
	 * @throws DocumentException
	 */
	byte[] writeSigStatAsPDF(int width, int height, List<JFreeChart> chartList, List<SignatureDTO> stats, String title) throws DocumentException;

}
