package es.gob.fire.persistence.dto;

import java.util.Date;

import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonView;

import es.gob.fire.commons.utils.UtilsStringChar;

public class ApplicationCertDTO {

	/**
	 * Attribute that represents the value of the primary key as a hidden input in the form.
	 */
	private String appId;

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
	@JsonFormat(pattern="dd/MM/yyyy HH:mm")
	private Date fechaAltaApp;

	/**
	 * Attribute that represents the list of responsible users for the application.
	 */
	private String responsables;

	/**
	 * Attribute that represents the data of the principal certificate in base64.
	 */
	private String certificatesB64;
	
	/**
	 * The name of the organization associated with the application.
	 */
	private String organization;

	/**
	 * The DIR3 code identifying the organization in administrative systems.
	 */
	private String dir3Code;

	/**
	 * Indicates whether the application uses a custom configuration, 
	 * based on the combination of custom size and custom providers.
	 */
	private Boolean customConfiguration;

	/**
	 * Indicates whether the application uses a custom size setting.
	 */
	private Boolean customSize;

	/**
	 * Indicates whether the application uses custom certificate providers.
	 */
	private Boolean customProviders;
	
	/**
	 * Attribute that represents the maximum size of the documents for this application.
	 */
    private Long maxSizeDoc;
    
    /**
	 * Attribute that represents the maximum size of the petitions for this application.
	 */
    private Long maxSizePetition;
    
    /**
	 * Attribute that represents the maximum amount of documents for this application.
	 */
    private Long maxAmountDocs;

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
	public ApplicationCertDTO(final String appiIdParam, final String appNameParam, final String alias) {
		super();
		this.appId = appiIdParam;
		this.appName = appNameParam;
		this.alias = alias;
	}
	
	/**
	 * Constructs a new {@link ApplicationCertDTO} with the provided parameters, including size and provider customization details.
	 * <p>
	 * Initializes the DTO with application metadata such as ID, name, registration date, organization info,
	 * and custom configuration flags. Also sets the {@code customConfiguration} flag based on both {@code customSize} and {@code customProviders}.
	 * </p>
	 *
	 * @param appiIdParam the application ID
	 * @param appNameParam the name of the application
	 * @param fechaAltaAppParam the registration date of the application
	 * @param organizationParam the name of the organization associated with the application
	 * @param dir3CodeParam the DIR3 code of the organization
	 * @param customSizeParam whether the application uses a custom size configuration
	 * @param customProvidersParam whether the application uses custom providers
	 * @param maxSizeDocParamParam the maximum allowed document size (currently unused in constructor logic)
	 * @param maxSizePetitionParam the maximum allowed size of a petition (currently unused in constructor logic)
	 * @param maxAmountDocsParam the maximum number of documents allowed (currently unused in constructor logic)
	 */
	public ApplicationCertDTO(final String appiIdParam, final String appNameParam, final Date fechaAltaAppParam, final String organizationParam, final String dir3CodeParam, final Boolean customSizeParam, final Boolean customProvidersParam, final Long maxSizeDocParam, final Long maxSizePetitionParam, final Long maxAmountDocsParam) {
		super();
		this.appId = appiIdParam;
		this.appName = appNameParam;
		this.fechaAltaApp = fechaAltaAppParam;
		this.organization = organizationParam;
		this.dir3Code = dir3CodeParam;
		this.customSize = customSizeParam;
		this.customProviders = customProvidersParam;
		this.customConfiguration = customSizeParam || customProvidersParam;
		this.maxSizeDoc = convertBytesToMegabytes(maxSizeDocParam);
		this.maxSizePetition = convertBytesToMegabytes(maxSizePetitionParam);
		this.maxAmountDocs = maxAmountDocsParam;
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
	 * Gets the value of the attribute {@link #certificatesB64}.
	 * @return the value of the attribute {@link #certificatesB64}.
	 */
	public String getCertificatesB64() {
		return this.certificatesB64;
	}

	/**
	 * Sets the value of the attribute {@link #certificatesB64}.
	 * @param certBackup The value for the attribute {@link #certificatesB64}.
	 */
	public void setCertificatesB64(final String certPrincipalB64) {
		this.certificatesB64 = certPrincipalB64;
	}

	/**
	 * Returns the name of the organization associated with the application.
	 *
	 * @return the organization name
	 */
	public String getOrganization() {
		return organization;
	}

	/**
	 * Sets the name of the organization associated with the application.
	 *
	 * @param organization the organization name
	 */
	public void setOrganization(String organization) {
		this.organization = organization;
	}

	/**
	 * Returns the DIR3 code of the organization.
	 *
	 * @return the DIR3 code
	 */
	public String getDir3Code() {
		return dir3Code;
	}

	/**
	 * Sets the DIR3 code of the organization.
	 *
	 * @param dir3Code the DIR3 code
	 */
	public void setDir3Code(String dir3Code) {
		this.dir3Code = dir3Code;
	}

	/**
	 * Returns whether the application uses a custom configuration.
	 *
	 * @return {@code true} if both custom size and custom providers are enabled; otherwise {@code false}
	 */
	public Boolean getCustomConfiguration() {
		return customConfiguration;
	}

	/**
	 * Sets whether the application uses a custom configuration.
	 *
	 * @param customConfiguration {@code true} to indicate custom configuration; otherwise {@code false}
	 */
	public void setCustomConfiguration(Boolean customConfiguration) {
		this.customConfiguration = customConfiguration;
	}

	/**
	 * Returns whether the application uses a custom size configuration.
	 *
	 * @return {@code true} if custom size is enabled; otherwise {@code false}
	 */
	public Boolean getCustomSize() {
		return customSize;
	}

	/**
	 * Sets whether the application uses a custom size configuration.
	 *
	 * @param customSize {@code true} to enable custom size; otherwise {@code false}
	 */
	public void setCustomSize(Boolean customSize) {
		this.customSize = customSize;
	}

	/**
	 * Returns whether the application uses custom providers.
	 *
	 * @return {@code true} if custom providers are enabled; otherwise {@code false}
	 */
	public Boolean getCustomProviders() {
		return customProviders;
	}

	/**
	 * Sets whether the application uses custom providers.
	 *
	 * @param customProviders {@code true} to enable custom providers; otherwise {@code false}
	 */
	public void setCustomProviders(Boolean customProviders) {
		this.customProviders = customProviders;
	}

	/**
	 * Returns the maximum allowed document size in bytes.
	 *
	 * @return the max document size
	 */
	public Long getMaxSizeDoc() {
		return maxSizeDoc;
	}

	/**
	 * Sets the maximum allowed document size in bytes.
	 *
	 * @param maxSizeDoc the max document size
	 */
	public void setMaxSizeDoc(Long maxSizeDoc) {
		this.maxSizeDoc = maxSizeDoc;
	}

	/**
	 * Returns the maximum allowed petition size in bytes.
	 *
	 * @return the max petition size
	 */
	public Long getMaxSizePetition() {
		return maxSizePetition;
	}

	/**
	 * Sets the maximum allowed petition size in bytes.
	 *
	 * @param maxSizePetition the max petition size
	 */
	public void setMaxSizePetition(Long maxSizePetition) {
		this.maxSizePetition = maxSizePetition;
	}

	/**
	 * Returns the maximum number of documents allowed in a petition.
	 *
	 * @return the max number of documents
	 */
	public Long getMaxAmountDocs() {
		return maxAmountDocs;
	}

	/**
	 * Sets the maximum number of documents allowed in a petition.
	 *
	 * @param maxAmountDocs the max number of documents
	 */
	public void setMaxAmountDocs(Long maxAmountDocs) {
		this.maxAmountDocs = maxAmountDocs;
	}
	
	/**
	 * Converts a value in bytes to megabytes.
	 *
	 * @param bytes the size in bytes
	 * @return the size in megabytes
	 */
	private static Long convertBytesToMegabytes(Long bytes) {
		return Long.valueOf(bytes == null ? 0 : bytes.longValue() / (1024 * 1024));
	}
}
