package es.gob.fire.persistence.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

import es.gob.fire.commons.utils.NumberConstants;

/**
 * <p>Class that maps the <i>APPLICATION</i> database table as a Plain Old Java Object.</p>
 * <b>Project:</b><p>Application for monitoring services of @firma suite systems.</p>
 * @version 1.2, 25/01/2019.
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
	private Long appId;
	
	/**
	 * Attribute that represents the app name.
	 */
	private String appName;
	
	/**
	 * Attribute that represents the certificate.
	 */
	private Certificate certificate;
	
	
	/**
	 * Attribute that represents the data.
	 */
	 @JsonFormat(pattern="yyyy-MM-dd HH:mm")
	private Date fechaAltaApp;
	 
	 /**
		 * Attribute that represents the habilitado.
		 */
	 private boolean habilitado;

	/**
	 * Gets the value of the attribute {@link #appId}.
	 * @return the value of the attribute {@link #appId}.
	 */
	@Id
	@Column(name = "ID", unique = true, nullable = false, precision = NumberConstants.NUM11)
	@GeneratedValue(generator = "tb_aplicaciones_seq")
	@GenericGenerator(name = "tb_aplicaciones_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = { @Parameter(name = "sequence_name", value = "TB_APLICACIONES_SEQ"), @Parameter(name = "initial_value", value = "2"), @Parameter(name = "increment_size", value = "1") })
	@JsonView(DataTablesOutput.View.class)
	public Long getAppId() {
		return this.appId;
	}

	/**
	 * Sets the value of the attribute {@link #appId}.
	 * @param appIdP The value for the attribute {@link #appId}.
	 */
	public void setAppId(final Long appIdP) {
		this.appId = appIdP;
	}
	
	
	/**
	 * Gets the value of the attribute {@link #userName}.
	 * @return the value of the attribute {@link #userName}.
	 */
	
	@Column(name = "NOMBRE", nullable = false, length = NumberConstants.NUM30, unique = true)
	@Size(max = NumberConstants.NUM30)
//	@Nif
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
	@Column(name = "FECHA_ALTA", nullable = false, length = NumberConstants.NUM150)
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
	 * Gets the value of the attribute {@link #certificate}.
	 * @return the value of the attribute {@link #certificate}.
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "FK_CERTIFICADO", nullable = false)
	@JsonView(DataTablesOutput.View.class)
	public Certificate getCertificate() {
		return certificate;
	}

	/**
	 * Sets the value of the attribute {@link #certificate}.
	 * @param rolP The value for the attribute {@link #certificate}.
	 */
	public void setCertificate(final Certificate certificateP) {
		this.certificate = certificateP;
	}

	/**
	 * Gets the value of the attribute {@link #habilitado}.
	 * @return the value of the attribute {@link #habilitado}.
	 */
	@JoinColumn(name = "HABILITADO", nullable = false)
	@JsonView(DataTablesOutput.View.class)
	public boolean isHabilitado() {
		return habilitado;
	}

	
	/**
	 * Sets the value of the attribute {@link #habilitado}.
	 * @param rolP The value for the attribute {@link #habilitado}.
	 */
	public void setHabilitado(boolean habilitado) {
		this.habilitado = habilitado;
	}
}
