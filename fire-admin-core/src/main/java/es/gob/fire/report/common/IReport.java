package es.gob.fire.report.common;

/** 
 * <p>Interface that defines the methods related to the generation of reports.</p>
 * <b>Project:</b><p>Horizontal platform of validation services of multiPKI 
 * certificates and electronic signature.</p>
 * @version 1.0 06/07/2023.
 */
public interface IReport {
	
	/**
	 * Method that generates a report.
	 * @throws Exception if the method fails.
	 */
	byte[ ] getReport() throws Exception;
}
