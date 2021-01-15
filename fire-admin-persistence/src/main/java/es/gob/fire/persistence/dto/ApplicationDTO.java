package es.gob.fire.persistence.dto;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import com.fasterxml.jackson.annotation.JsonView;

import es.gob.fire.commons.utils.NumberConstants;
import es.gob.fire.commons.utils.UtilsStringChar;
import es.gob.fire.persistence.dto.validation.CheckItFirst;
import es.gob.fire.persistence.dto.validation.ThenCheckIt;

public class ApplicationDTO {
	
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
	 * Attribute that represents the value of the input habilitado of the application in the form. 
	 */
	private boolean habilitado;
	
	/**
	 * Attribute that represents the value of the input idCertificado of the application in the form. 
	 */
	private Long idCertificado;
	
	/**
	 * Attribute that represents index of the row of the selected application.
	 */
	private String rowIndexApp;
	
	/**
	 * Gets the value of the attribute {@link #appId}.
	 * @return the value of the attribute {@link #appId}.
	 */
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
	 * Gets the value of the attribute {@link #habilitado}.
	 * @return the value of the attribute {@link #habilitado}.
	 */
	public boolean isHabilitado() {
		return habilitado;
	}

	
	/**
	 * Sets the value of the attribute {@link #habilitado}.
	 * @param habilitado The value for the attribute {@link #habilitado}.
	 */
	public void setHabilitado(boolean habilitado) {
		this.habilitado = habilitado;
	}

	/**
	 * Gets the value of the attribute {@link #idCertificado}.
	 * @return the value of the attribute {@link #idCertificado}.
	 */
	public Long getIdCertificado() {
		return idCertificado;
	}

	/**
	 * Sets the value of the attribute {@link #idCertificado}.
	 * @param idCertificado The value for the attribute {@link #idCertificado}.
	 */
	public void setIdCertificado(Long idCertificado) {
		this.idCertificado = idCertificado;
	}

	/**
	 * Gets the value of the attribute {@link #rowIndexApp}.
	 * @return the value of the attribute {@link #rowIndexApp}.
	 */
	public String getRowIndexApp() {
		return rowIndexApp;
	}

	/**
	 * Sets the value of the attribute {@link #rowIndexApp}.
	 * @param idCertificado The value for the attribute {@link #rowIndexApp}.
	 */
	public void setRowIndexApp(String rowIndexApp) {
		this.rowIndexApp = rowIndexApp;
	}
	
	
}
