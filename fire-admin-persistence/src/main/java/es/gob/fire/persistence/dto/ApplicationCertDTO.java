package es.gob.fire.persistence.dto;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import com.fasterxml.jackson.annotation.JsonView;

import es.gob.fire.commons.utils.UtilsStringChar;

public class ApplicationCertDTO {
	
	/**
	 * Attribute that represents the value of the primary key as a hidden input in the form. 
	 */
	private String appId;
	
	/**
	 * Attribute that represents the value of the input appName of the application in the form. 
	 */
	private String appName = UtilsStringChar.EMPTY_STRING;	
	
	/**
	 * Attribute that represents the value of the input fechaAltaApp of the application in the form. 
	 */
	private Date fechaAltaApp;
	
	/**
	 * Attribute that represents the list of responsible users for the application. 
	 */
	private String responsables;
	
	public ApplicationCertDTO(String appiIdParam, String appNameParam, Date fechaAltaAppParam) {
		super();
		this.appId = appiIdParam;
		this.appName = appNameParam;
		this.fechaAltaApp = fechaAltaAppParam;		
	}
	
	/**
	 * Gets the value of the attribute {@link #appId}.
	 * @return the value of the attribute {@link #appId}.
	 */
	@JsonView(DataTablesOutput.View.class)
	public String getAppId() {
		return appId;
	}

	
	/**
	 * Sets the value of the attribute {@link #appId}.
	 * @param appIdParam The value for the attribute {@link #appId}.
	 */
	public void setAppId(String appIdParam) {
		this.appId = appIdParam;
	}

	/**
	 * Gets the value of the attribute {@link #appNameP}.
	 * @return the value of the attribute {@link #appNameP}.
	 */
	@JsonView(DataTablesOutput.View.class)
	public String getAppName() {
		return this.appName;
	}
	
	/**
	 * Sets the value of the attribute {@link #appNameP}.
	 * @param appNameParam The value for the attribute {@link #appNameP}.
	 */
	public void setAppName(String appNameP) {
		this.appName = appNameP;
	}
	
	/**
	 * Gets the value of the attribute {@link #fechaAltaApp}.
	 * @return the value of the attribute {@link #fechaAltaApp}.
	 */
	@JsonView(DataTablesOutput.View.class)
	public Date getFechaAltaApp() {
		return fechaAltaApp;
	}

	/**
	 * Sets the value of the attribute {@link #fechaAlta}.
	 * @param fechaAltaParam The value for the attribute {@link #fechaAlta}.
	 */
	public void setFechaAltaApp(Date fechaAltaAppParam) {
		this.fechaAltaApp = fechaAltaAppParam;
	}

	/**
	 * Gets the value of the attribute {@link #responsables}.
	 * @return the value of the attribute {@link #responsables}.
	 */
	@JsonView(DataTablesOutput.View.class)
	public String getResponsables() {
		return responsables;
	}

	/**
	 * Sets the value of the attribute {@link #responsables}.
	 * @param fechaAltaParam The value for the attribute {@link #responsables}.
	 */
	public void setResponsables(String responsables) {
		this.responsables = responsables;
	}
		
}
