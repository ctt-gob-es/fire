package es.gob.fire.persistence.dto;

import java.util.Date;

import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import com.fasterxml.jackson.annotation.JsonView;

import es.gob.fire.commons.utils.UtilsStringChar;

public class ApplicationCertDTO {

	/**
	 * Attribute that represents the value of the primary key as a hidden input in the form.
	 */
	private String appId;

	/**
	 * Attribute that represents the value of the primary key as a hidden input in the form.
	 */
	private Long idCertificate;

	/**
	 * Attribute that represents the value of the input alias in the form.
	 */
	private String alias;

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

	/**
	 * Attribute that represents the subject of the certificate principal.
	 */
	private String certPrincipal;

	/**
	 * Attribute that represents the subject of the certificate backup.
	 */
	private String	certBackup;

	/**
	 * Attribute that represents the data of the principal certificate in base64.
	 */
	private String certPrincipalB64;

	/**
	 * Attribute that represents the data of the backup certificate in base64.
	 */
	private String	certBackupB64;

	/**
	 * @param appiIdParam
	 * @param appNameParam
	 * @param fechaAltaAppParam
	 */
	public ApplicationCertDTO(final String appiIdParam, final String appNameParam, final Date fechaAltaAppParam) {
		super();
		this.appId = appiIdParam;
		this.appName = appNameParam;
		this.fechaAltaApp = fechaAltaAppParam;
	}

	/**
	 * @param appiIdParam
	 * @param appNameParam
	 * @param fechaAltaAppParam
	 */
	public ApplicationCertDTO(final String appiIdParam, final String appNameParam, final Long idCertificate, final String alias) {
		super();
		this.appId = appiIdParam;
		this.appName = appNameParam;
		this.idCertificate = idCertificate;
		this.alias = alias;
	}

	/**
	 * Gets the value of the attribute {@link #appId}.
	 * @return the value of the attribute {@link #appId}.
	 */
	@JsonView(DataTablesOutput.View.class)
	public String getAppId() {
		return this.appId;
	}


	/**
	 * Sets the value of the attribute {@link #appId}.
	 * @param appIdParam The value for the attribute {@link #appId}.
	 */
	public void setAppId(final String appIdParam) {
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
	public void setAppName(final String appNameP) {
		this.appName = appNameP;
	}

	/**
	 * Gets the value of the attribute {@link #fechaAltaApp}.
	 * @return the value of the attribute {@link #fechaAltaApp}.
	 */
	@JsonView(DataTablesOutput.View.class)
	public Date getFechaAltaApp() {
		return this.fechaAltaApp;
	}

	/**
	 * Sets the value of the attribute {@link #fechaAlta}.
	 * @param fechaAltaParam The value for the attribute {@link #fechaAlta}.
	 */
	public void setFechaAltaApp(final Date fechaAltaAppParam) {
		this.fechaAltaApp = fechaAltaAppParam;
	}

	/**
	 * Gets the value of the attribute {@link #responsables}.
	 * @return the value of the attribute {@link #responsables}.
	 */
	@JsonView(DataTablesOutput.View.class)
	public String getResponsables() {
		return this.responsables;
	}

	/**
	 * Sets the value of the attribute {@link #responsables}.
	 * @param fechaAltaParam The value for the attribute {@link #responsables}.
	 */
	public void setResponsables(final String responsables) {
		this.responsables = responsables;
	}

	/**
	 * Gets the value of the attribute {@link #alias}.
	 * @return the value of the attribute {@link #alias}.
	 */
	public String getAlias() {
		return this.alias;
	}

	/**
	 * Sets the value of the attribute {@link #alias}.
	 * @param fechaAltaParam The value for the attribute {@link #alias}.
	 */
	public void setAlias(final String alias) {
		this.alias = alias;
	}

	/**
	 * Gets the value of the attribute {@link #certPrincipal}.
	 * @return the value of the attribute {@link #certPrincipal}.
	 */
	public String getCertPrincipal() {
		return this.certPrincipal;
	}

	/**
	 * Sets the value of the attribute {@link #certPrincipal}.
	 * @param certPrincipal The value for the attribute {@link #certPrincipal}.
	 */
	public void setCertPrincipal(final String certPrincipal) {
		this.certPrincipal = certPrincipal;
	}

	/**
	 * Gets the value of the attribute {@link #certBackup}.
	 * @return the value of the attribute {@link #certBackup}.
	 */
	public String getCertBackup() {
		return this.certBackup;
	}

	/**
	 * Sets the value of the attribute {@link #certBackup}.
	 * @param certBackup The value for the attribute {@link #certBackup}.
	 */
	public void setCertBackup(final String certBackup) {
		this.certBackup = certBackup;
	}

	/**
	 * Gets the value of the attribute {@link #certPrincipalB64}.
	 * @return the value of the attribute {@link #certPrincipalB64}.
	 */
	public String getCertPrincipalB64() {
		return this.certPrincipalB64;
	}

	/**
	 * Sets the value of the attribute {@link #certPrincipalB64}.
	 * @param certBackup The value for the attribute {@link #certPrincipalB64}.
	 */
	public void setCertPrincipalB64(final String certPrincipalB64) {
		this.certPrincipalB64 = certPrincipalB64;
	}

	/**
	 * Gets the value of the attribute {@link #certBackupB64}.
	 * @return the value of the attribute {@link #certBackupB64}.
	 */
	public String getCertBackupB64() {
		return this.certBackupB64;
	}

	/**
	 * Sets the value of the attribute {@link #certBackupB64}.
	 * @param certBackup The value for the attribute {@link #certBackupB64}.
	 */
	public void setCertBackupB64(final String certBackupB64) {
		this.certBackupB64 = certBackupB64;
	}

	/**
	 * Gets the value of the attribute {@link #idCertificate}.
	 * @return the value of the attribute {@link #idCertificate}.
	 */
	public Long getIdCertificate() {
		return this.idCertificate;
	}

	/**
	 * Sets the value of the attribute {@link #idCertificate}.
	 * @param idCertificate The value for the attribute {@link #idCertificate}.
	 */
	public void setIdCertificate(final Long idCertificate) {
		this.idCertificate = idCertificate;
	}


}
