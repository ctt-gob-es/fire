package es.gob.fire.report.petition;

public interface IAuditReport {
	
	/**
	 * Constant attribute that represents the string to identify the key of the parameter that contains the start date
	 */
	final String FROM_DATE_PARAMETER = "fromDate";
	
	/**
	 * Constant attribute that represents the string to identify the key of the parameter that contains the end date
	 */
	final String TO_DATE_PARAMETER = "toDate";
	
	/**
	 * Constant attribute that represents the string to identify the key of the parameter that contains the list of signatures
	 */
	final String LIST_SIGNATURES_PARAMETER = "listSignatures";

	/**
	 * Constant attribute that represents the string to identify the key of the parameter that contains the list of signatures
	 */
	final String REPORT_FONT = "Frutiger-Light";
	
	/**
	 * Constant attribute that represents the string to identify the key of the parameter that contains the list of signatures
	 */
	final String REPORT_TITLE_SECTION_NAME = "title";
	
	/**
	 * Constant attribute that represents the string to identify the key of the parameter that contains the list of signatures
	 */
	final String REPORT_HEADER_SECTION_NAME = "header";
	
	/**
	 * Constant attribute that represents the string to identify the key of the parameter that contains the list of signatures
	 */
	final String REPORT_DATA_UNEVEN_SECTION_NAME = "dataUneven";
	
	/**
	 * Constant attribute that represents the string to identify the key of the parameter that contains the list of signatures
	 */
	final String REPORT_DATA_EVEN_SECTION_NAME = "dataEven";
	
	/**
	 * Constant attribute that represents the string to identify the key of the parameter that contains the list of signatures
	 */
	final String REPORT_LAST_ROW_SECTION_NAME = "lastRow";
}
