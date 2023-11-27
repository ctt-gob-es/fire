package es.gob.fire.report.common;

import java.util.Map;

import es.gob.fire.report.petition.AuditReport;

/**
 * <p>Class that manages the generation of reports.</p>
 * @version 1.0, 06/07/2023.
 */
public class ReportGenerator {
	
	private static final String REPORT_TYPE_AUDIT = "AUDIT";
	
	/**
	 * Constructor method for the class ReportGenerator.java.
	 */
	private ReportGenerator(){
	}
	
	public static Report getReport(String type, Map<Object, Object> parameters){
		Report report = null;
		
		switch (type){
		case REPORT_TYPE_AUDIT:
			report = new AuditReport(parameters);
			break;
		default:
			break;
		}
		
		
		return report;
	}

}
