package es.gob.fire.service;

import java.util.List;

import org.jfree.chart.JFreeChart;

import com.lowagie.text.DocumentException;

import es.gob.fire.persistence.dto.SignatureDTO;
import es.gob.fire.persistence.dto.TransactionDTO;

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
	 * @param tableType: 1 Tipo básico de correctas e incorrectas
	 * 					 2 Tipo compuesto (simple o lote) de correctas e incorrectas
	 * 				     3 Tipo tamaños	 
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
	 * @param tableType: 1 Tipo básico de correctas e incorrectas
	 * 					 2 Tipo compuesto (simple o lote) de correctas e incorrectas
	 * 				     3 Tipo tamaños	 
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
