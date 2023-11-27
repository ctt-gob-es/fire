package es.gob.fire.report.common;

import java.util.Map;

/**
 * <p>Class that represents a report.</p>
 * <b>Project:</b><p>Horizontal platform of validation services of multiPKI
 * certificates and electronic signature.</p>
 * @version 1.0 06/07/2023.
 */
public abstract class Report implements IReport{
	
	/**
	 * Attribute that represents the set of parameters of a report.
	 */
	private Map<Object, Object> parameters = null;

	/**
	 * Constructor method for the class Informe.java.
	 * @param parametersParam Parameter that represents the set of parameters of a report.
	 */
	public Report(Map<Object, Object> parametersParam) {
		super();
		this.parameters = parametersParam;
	}

	/**
	 * Gets the value of the attribute {@link #parameters}.
	 * @return the value of the attribute {@link #parameters}.
	 */
	public final Map<Object, Object> getParameters() {
		return parameters;
	}

	/**
	 * Sets the value of the attribute {@link #parameters}.
	 * @param parametersParam The value for the attribute {@link #parameters}.
	 */
	public final void setParameters(Map<Object, Object> parametersParam) {
		this.parameters = parametersParam;
	}

}
