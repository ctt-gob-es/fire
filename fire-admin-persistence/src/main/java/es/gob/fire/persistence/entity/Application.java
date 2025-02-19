package es.gob.fire.persistence.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonView;

import es.gob.fire.commons.utils.NumberConstants;

/**
 * <p>Class that maps the <i>APPLICATION</i> database table as a Plain Old Java Object.</p>
 * <b>Project:</b><p>Application for monitoring services of @firma suite systems.</p>
 * @version 1.1, 27/01/2025.
 */
@Entity
@Table(name = "TB_APLICACIONES")
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class Application implements Serializable{

	/**
	 * Class serial version.
	 */
	private static final long serialVersionUID = -60419018366799736L;

	/**
	 * Attribute that represents the app id.
	 */
	private String appId;

	/**
	 * Attribute that represents the app name.
	 */
	private String appName;

	/**
	 * Attribute that represents the data.
	 */
	private Date fechaAltaApp;

	 /**
	  * Attribute that represents the habilitado.
	 */
	private boolean habilitado;
	
	/**
	 * Attribute that represents the organism.
	 */
	private String organization;
	
	/**
	 * Attribute that represents the DIR3 code.
	 */
	private String dir3Code;

	 /**
	  * Attribute that represents the header list for the validation method.
	  */
	private List<ApplicationResponsible> listApplicationResponsible;
	
	/**
	 * Attribute that represents the header list for the validation method.
	 */
	private List<CertificatesApplication> listCertificatesApplication;

	/**
	 * Attribute that represents if the app has a custom provider configuration.
	 */
	private boolean customProvider;
	
	/**
	 * Attribute that represents if the app has a custom size configuration.
	 */
	private boolean customSize;
	
    private List<ProviderApplication> providers;
    
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
	 * Gets the value of the attribute {@link #appId}.
	 * @return the value of the attribute {@link #appId}.
	 */
	@Id
	@Column(name = "ID", unique = true, nullable = false, precision = NumberConstants.NUM11)
	//@GeneratedValue(generator = "tb_aplicaciones_seq")
	//@GenericGenerator(name = "tb_aplicaciones_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = { @Parameter(name = "sequence_name", value = "TB_APLICACIONES_SEQ"), @Parameter(name = "initial_value", value = "2"), @Parameter(name = "increment_size", value = "1") })
	@GeneratedValue(generator = "app-generator")
    @GenericGenerator(name = "app-generator", strategy = "es.gob.fire.persistence.generator.ApplicationIdGenerator")
	@JsonView(DataTablesOutput.View.class)
	public String getAppId() {
		return this.appId;
	}

	/**
	 * Sets the value of the attribute {@link #appId}.
	 * @param appIdP The value for the attribute {@link #appId}.
	 */
	public void setAppId(final String appIdP) {
		this.appId = appIdP;
	}


	/**
	 * Gets the value of the attribute {@link #userName}.
	 * @return the value of the attribute {@link #userName}.
	 */

	@Column(name = "NOMBRE", nullable = false, length = NumberConstants.NUM30, unique = true)
	@Size(max = NumberConstants.NUM30)
	@JsonView(DataTablesOutput.View.class)
	public String getAppName() {
		return this.appName;
	}

	/**
	 * Sets the value of the attribute {@link #userName}.
	 * @param userNameP The value for the attribute {@link #userName}.
	 */
	public void setAppName(final String appNameP) {
		this.appName = appNameP;
	}

	/**
	 * Gets the value of the attribute {@link #fechaAltaApp}.
	 * @return the value of the attribute {@link #fechaAltaApp}.
	 */
	@Column(name = "FECHA_ALTA", nullable = false, length = NumberConstants.NUM19)
	@DateTimeFormat (pattern = "yyyy-MM-dd HH:mm:ss.SSS")
	@JsonFormat (pattern = "yyyy-MM-dd HH:mm:ss.SSS")
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
	 * Gets the value of the attribute {@link #habilitado}.
	 * @return the value of the attribute {@link #habilitado}.
	 */
	@JoinColumn(name = "HABILITADO", nullable = false)
	@JsonView(DataTablesOutput.View.class)
	public boolean isHabilitado() {
		return this.habilitado;
	}


	/**
	 * Sets the value of the attribute {@link #habilitado}.
	 * @param rolP The value for the attribute {@link #habilitado}.
	 */
	public void setHabilitado(final boolean habilitado) {
		this.habilitado = habilitado;
	}

	/**
	 * Gets the value of the attribute {@link #listApplicationResponsible}.
	 * @return the value of the attribute {@link #listApplicationResponsible}.
	 */
	// CHECKSTYLE:OFF -- Checkstyle rule "Design for Extension" is not applied
	// because Hibernate JPA needs not final access methods.
	@OneToMany(mappedBy = "application", cascade = {CascadeType.ALL }, orphanRemoval = true, fetch = FetchType.LAZY)
	public List<ApplicationResponsible> getListApplicationResponsible() {
		// CHECKSTYLE:ON
		return this.listApplicationResponsible;
	}

	/**
	 * Sets the value of the attribute {@link #listApplicationResponsible}.
	 * @param listXValidationMethodHeaderParam The value for the attribute {@link #listApplicationResponsible}.
	 */
	// CHECKSTYLE:OFF -- Checkstyle rule "Design for Extension" is not applied
	// because Hibernate JPA needs not final access methods.
	public void setListApplicationResponsible(final List<ApplicationResponsible> listApplicationResponsibleParam) {
		// CHECKSTYLE:ON
		this.listApplicationResponsible = listApplicationResponsibleParam;
	}
	
	/**
	 * Gets the value of the attribute {@link #listCertificatesApplication}.
	 * @return the value of the attribute {@link #listCertificatesApplication}.
	 */
	// CHECKSTYLE:OFF -- Checkstyle rule "Design for Extension" is not applied
	// because Hibernate JPA needs not final access methods.
	@OneToMany(mappedBy = "application", cascade = {CascadeType.ALL }, orphanRemoval = true, fetch = FetchType.LAZY)
	public List<CertificatesApplication> getListCertificatesApplication() {
		// CHECKSTYLE:ON
		return this.listCertificatesApplication;
	}
	
	/**
	 * Sets the value of the attribute {@link #listCertificatesApplication}.
	 * @param listCertificatesApplication The value for the attribute {@link #listCertificatesApplication}.
	 */
	// CHECKSTYLE:OFF -- Checkstyle rule "Design for Extension" is not applied
	// because Hibernate JPA needs not final access methods.
	public void setListCertificatesApplication(final List<CertificatesApplication> listCertificatesApplicationParam) {
		// CHECKSTYLE:ON
		this.listCertificatesApplication = listCertificatesApplicationParam;
	}
	
	/**
	 * Gets the value of the attribute {@link #organization}.
	 * @return the value of the attribute {@link #organization}.
	 */

	@Column(name = "ORGANIZATION", nullable = true, length = NumberConstants.NUM255, unique = false)
	@Size(max = NumberConstants.NUM255)
	public String getOrganization() {
		return this.organization;
	}

	/**
	 * Sets the value of the attribute {@link #organization}.
	 * @param userNameP The value for the attribute {@link #organization}.
	 */
	public void setOrganization(final String organization) {
		this.organization = organization;
	}
	
	/**
	 * Gets the value of the attribute {@link #dir3Code}.
	 * @return the value of the attribute {@link #dir3Code}.
	 */

	@Column(name = "DIR3_CODE", nullable = true, length = NumberConstants.NUM50, unique = false)
	@Size(max = NumberConstants.NUM50)
	public String getDir3Code() {
		return this.dir3Code;
	}

	/**
	 * Sets the value of the attribute {@link #dir3Code}.
	 * @param userNameP The value for the attribute {@link #dir3Code}.
	 */
	public void setDir3Code(final String dir3Code) {
		this.dir3Code = dir3Code;
	}
	
	/**
     * Gets the value of the attribute {@link #customProvider}.
     * @return the value of the attribute {@link #customProvider}.
     */
    @Column(name = "PROVEEDOR_PERSONALIZADO", nullable = false, precision = 1)
    @Type(type = "yes_no")
    public Boolean getCustomProvider() {
        return customProvider;
    }
    
    /**
	 * Sets the value of the attribute {@link #customProvider}.
	 * @param userNameP The value for the attribute {@link #customProvider}.
	 */
	public void setCustomProvider(final Boolean customProvider) {
		this.customProvider = customProvider;
	}
	
	/**
     * Gets the value of the attribute {@link #customSize}.
     * @return the value of the attribute {@link #customSize}.
     */
    @Column(name = "TAMANO_PERSONALIZADO", nullable = false, precision = 1)
    @Type(type = "yes_no")
    public Boolean getCustomSize() {
        return customSize;
    }
    
    /**
	 * Sets the value of the attribute {@link #customSize}.
	 * @param userNameP The value for the attribute {@link #customSize}.
	 */
	public void setCustomSize(final Boolean customSize) {
		this.customSize = customSize;
	}

	@OneToMany(mappedBy = "application", cascade = {CascadeType.ALL }, orphanRemoval = true, fetch = FetchType.LAZY)
	public List<ProviderApplication> getProviders() {
		return providers;
	}

	public void setProviders(List<ProviderApplication> providers) {
		this.providers = providers;
	}

	@Column(name = "TAMANO_MAXIMO_DOCUMENTO", precision = NumberConstants.NUM19)
	public Long getMaxSizeDoc() {
		return maxSizeDoc;
	}

	public void setMaxSizeDoc(Long maxSizeDoc) {
		this.maxSizeDoc = maxSizeDoc;
	}
	
	@Column(name = "TAMANO_MAXIMO_PETICION", precision = NumberConstants.NUM19)
	public Long getMaxSizePetition() {
		return maxSizePetition;
	}

	public void setMaxSizePetition(Long maxSizePetition) {
		this.maxSizePetition = maxSizePetition;
	}
	
	@Column(name = "CANTIDAD_MAXIMA_DOCUMENTOS", precision = NumberConstants.NUM19)
	public Long getMaxAmountDocs() {
		return maxAmountDocs;
	}

	public void setMaxAmountDocs(Long maxAmountDocs) {
		this.maxAmountDocs = maxAmountDocs;
	}
}
