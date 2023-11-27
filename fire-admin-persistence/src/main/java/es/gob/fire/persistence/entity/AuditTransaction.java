package es.gob.fire.persistence.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

import es.gob.fire.commons.utils.NumberConstants;

/**
 * <p>Class that maps the <i>TB_AUDIT_TRANSACCIONES</i> database table as a Plain Old Java Object.</p>
 * <b>Project:</b><p>Application for monitoring services of @firma suite systems.</p>
 * @version 1.0, 10/08/2023.
 */
@Entity
@Table(name = "TB_AUDIT_TRANSACCIONES")
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class AuditTransaction {

	/**
	 * Attribute that represents the value of the identificator of the petition.
	 */
	private Integer idAuditTransaction;

	/**
	 * Attribute that represents the value of the date of the petition.
	 */
	private Date date;

	/**
	 * Attribute that represents the value of the identifier of the app.
	 */
	private String idApp;
	
	/**
	 * Attribute that represents the value of the name of the app.
	 */
	private String nameApp;
	
	/**
	 * Attribute that represents the value of the transaction identificator of the petition.
	 */
	private String idTransaction;
	
	/**
	 * Attribute that represents the type of operation made by the petition.
	 */
	private String operation;
	
	/**
	 * Attribute that represents the cryptographic type of operation made by the petition.
	 */
	private String cryptoOperation;
	
	/**
	 * Attribute that represents the algorithm used for the signature.
	 */
	private String algorithm;
	
	/**
	 * Attribute that represents the value of the format of the signature.
	 */
	private String format;
	
	/**
	 * Attribute that represents the value of the format of the signature.
	 */
	private String updateFormat;
	
	/**
	 * Attribute that represents the value of the provider of the signature.
	 */
	private String provider;
	
	/**
	 * Attribute that represents if the value of the provider of the signature was asked to the user.
	 */
	private Boolean forcedProvider;
	
	/**
	 * Attribute that represents the value of the browser used.
	 */
	private String browser;
	
	/**
	 * Attribute that represents the value of the size of the petition.
	 */
	private Integer size;

	/**
	 * Attribute that represents the result of the petition.
	 */
	private Boolean result;
	
	/**
	 * Attribute that represents the value of the detail of the error of the petition.
	 */
	private String errorDetail;
	
	/**
	 * Attribute that represents the value of the last node to process the petition.
	 */
	private String node;

	/**
	 * Gets the value of the attribute {@link #idAuditTransaction}.
	 * @return the value of the attribute {@link #idAuditTransaction}.
	 */
	@Id
	@Column(name = "ID", unique = true, nullable = false, precision = NumberConstants.NUM11)
	@GeneratedValue(generator = "tb_peticiones_seq")
    @GenericGenerator(name = "tb_peticiones_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = { @Parameter(name = "sequence_name", value = "TB_PETICIONES_SEQ"), @Parameter(name = "initial_value", value = "1"), @Parameter(name = "increment_size", value = "1") })
	@JsonView(DataTablesOutput.View.class)
	public Integer getIdAuditTransaction() {
		return idAuditTransaction;
	}

	/**
	 * Sets the value of the attribute {@link #idAuditTransaction}.
	 * @param idAuditTransaction The value for the attribute {@link #idAuditTransaction}.
	 */
	public void setIdAuditTransaction(Integer idAuditTransaction) {
		this.idAuditTransaction = idAuditTransaction;
	}

	/**
	 * Gets the value of the attribute {@link #date}.
	 * @return the value of the attribute {@link #date}.
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "FECHA", nullable = false, length = NumberConstants.NUM19)
	@DateTimeFormat (pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Europe/Paris")
	@JsonView(DataTablesOutput.View.class)
	public Date getDate() {
		return date;
	}

	/**
	 * Sets the value of the attribute {@link #date}.
	 * @param date The value for the attribute {@link #date}.
	 */
	public void setDate(Date date) {
		this.date = date;
	}

	/**
	 * Gets the value of the attribute {@link #idApp}.
	 * @return the value of the attribute {@link #idApp}.
	 */
	@Column(name = "ID_APLICACION", nullable = false, length = NumberConstants.NUM48)
	@JsonView(DataTablesOutput.View.class)
	public String getIdApp() {
		return idApp;
	}

	/**
	 * Sets the value of the attribute {@link #idApp}.
	 * @param idApp The value for the attribute {@link #idApp}.
	 */
	public void setIdApp(String idApp) {
		this.idApp = idApp;
	}
	
	/**
	 * Gets the value of the attribute {@link #nameApp}.
	 * @return the value of the attribute {@link #nameApp}.
	 */
	@Column(name = "NOMBRE_APLICACION", nullable = false, length = NumberConstants.NUM45)
	@JsonView(DataTablesOutput.View.class)
	public String getNameApp() {
		return nameApp;
	}

	/**
	 * Sets the value of the attribute {@link #nameApp}.
	 * @param nameApp The value for the attribute {@link #nameApp}.
	 */
	public void setNameApp(String nameApp) {
		this.nameApp = nameApp;
	}
	
	/**
	 * Gets the value of the attribute {@link #idTransaction}.
	 * @return the value of the attribute {@link #idTransaction}.
	 */
	@Column(name = "ID_TRANSACCION", nullable = false, length = NumberConstants.NUM45)
	@JsonView(DataTablesOutput.View.class)
	public String getIdTransaction() {
		return idTransaction;
	}

	/**
	 * Sets the value of the attribute {@link #idTransaction}.
	 * @param idTransaction The value for the attribute {@link #idTransaction}.
	 */
	public void setIdTransaction(String idTransaction) {
		this.idTransaction = idTransaction;
	}

	/**
	 * Gets the value of the attribute {@link #operation}.
	 * @return the value of the attribute {@link #operation}.
	 */
	@Column(name = "OPERACION", nullable = false, length = NumberConstants.NUM10)
	@JsonView(DataTablesOutput.View.class)
	public String getOperation() {
		return operation;
	}

	/**
	 * Sets the value of the attribute {@link #operation}.
	 * @param operation The value for the attribute {@link #operation}.
	 */
	public void setOperation(String operation) {
		this.operation = operation;
	}

	/**
	 * Gets the value of the attribute {@link #cryptoOperation}.
	 * @return the value of the attribute {@link #cryptoOperation}.
	 */
	@Column(name = "OPERACION_CRIPTOGRAFICA", nullable = false, length = NumberConstants.NUM10)
	@JsonView(DataTablesOutput.View.class)
	public String getCryptoOperation() {
		return cryptoOperation;
	}

	/**
	 * Sets the value of the attribute {@link #cryptoOperation}.
	 * @param cryptoOperation The value for the attribute {@link #cryptoOperation}.
	 */
	public void setCryptoOperation(String cryptoOperation) {
		this.cryptoOperation = cryptoOperation;
	}

	/**
	 * Gets the value of the attribute {@link #algorithm}.
	 * @return the value of the attribute {@link #algorithm}.
	 */
	@Column(name = "ALGORITMO", nullable = false, length = NumberConstants.NUM20)
	@JsonView(DataTablesOutput.View.class)
	public String getAlgorithm() {
		return algorithm;
	}

	/**
	 * Sets the value of the attribute {@link #algorithm}.
	 * @param algorithm The value for the attribute {@link #algorithm}.
	 */
	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	/**
	 * Gets the value of the attribute {@link #format}.
	 * @return the value of the attribute {@link #format}.
	 */
	@Column(name = "FORMATO", nullable = false, length = NumberConstants.NUM20)
	@JsonView(DataTablesOutput.View.class)
	public String getFormat() {
		return format;
	}

	/**
	 * Sets the value of the attribute {@link #format}.
	 * @param format The value for the attribute {@link #format}.
	 */
	public void setFormat(String format) {
		this.format = format;
	}

	/**
	 * Gets the value of the attribute {@link #updateFormat}.
	 * @return the value of the attribute {@link #updateFormat}.
	 */
	@Column(name = "FORMATO_ACTUALIZADO", nullable = false, length = NumberConstants.NUM20)
	@JsonView(DataTablesOutput.View.class)
	public String getUpdateFormat() {
		return updateFormat;
	}

	/**
	 * Sets the value of the attribute {@link #updateFormat}.
	 * @param updateFormat The value for the attribute {@link #updateFormat}.
	 */
	public void setUpdateFormat(String updateFormat) {
		this.updateFormat = updateFormat;
	}

	/**
	 * Gets the value of the attribute {@link #provider}.
	 * @return the value of the attribute {@link #provider}.
	 */
	@Column(name = "PROVEEDOR", nullable = false, length = NumberConstants.NUM45)
	@JsonView(DataTablesOutput.View.class)
	public String getProvider() {
		return provider;
	}

	/**
	 * Sets the value of the attribute {@link #provider}.
	 * @param provider The value for the attribute {@link #provider}.
	 */
	public void setProvider(String provider) {
		this.provider = provider;
	}

	/**
	 * Gets the value of the attribute {@link #forcedProvider}.
	 * @return the value of the attribute {@link #forcedProvider}.
	 */
	@Column(name = "PROVEEDOR_FORZADO", nullable = false)
	@JsonView(DataTablesOutput.View.class)
	public Boolean getForcedProvider() {
		return forcedProvider;
	}

	/**
	 * Sets the value of the attribute {@link #forcedProvider}.
	 * @param forcedProvider The value for the attribute {@link #forcedProvider}.
	 */
	public void setForcedProvider(Boolean forcedProvider) {
		this.forcedProvider = forcedProvider;
	}

	/**
	 * Gets the value of the attribute {@link #browser}.
	 * @return the value of the attribute {@link #browser}.
	 */
	@Column(name = "NAVEGADOR", nullable = false, length = NumberConstants.NUM20)
	@JsonView(DataTablesOutput.View.class)
	public String getBrowser() {
		return browser;
	}

	/**
	 * Sets the value of the attribute {@link #browser}.
	 * @param browser The value for the attribute {@link #browser}.
	 */
	public void setBrowser(String browser) {
		this.browser = browser;
	}
	
	/**
	 * Gets the value of the attribute {@link #size}.
	 * @return the value of the attribute {@link #size}.
	 */
	@Column(name = "TAMANNO", nullable = false, length = NumberConstants.NUM19)
	@JsonView(DataTablesOutput.View.class)
	public Integer getSize() {
		return size;
	}

	/**
	 * Sets the value of the attribute {@link #size}.
	 * @param size The value for the attribute {@link #size}.
	 */
	public void setSize(Integer size) {
		this.size = size;
	}

	/**
	 * Gets the value of the attribute {@link #result}.
	 * @return the value of the attribute {@link #result}.
	 */
	@Column(name = "RESULTADO", nullable = false)
	@JsonView(DataTablesOutput.View.class)
	public Boolean getResult() {
		return result;
	}

	/**
	 * Sets the value of the attribute {@link #result}.
	 * @param result The value for the attribute {@link #result}.
	 */
	public void setResult(Boolean result) {
		this.result = result;
	}
	
	/**
	 * Gets the value of the attribute {@link #errorDetail}.
	 * @return the value of the attribute {@link #errorDetail}.
	 */
	@Column(name = "ERROR_DETALLE", nullable = false, length = NumberConstants.NUM20)
	@JsonView(DataTablesOutput.View.class)
	public String getErrorDetail() {
		return errorDetail;
	}

	/**
	 * Sets the value of the attribute {@link #errorDetail}.
	 * @param errorType The value for the attribute {@link #errorDetail}.
	 */
	public void setErrorDetail(String errorType) {
		this.errorDetail = errorType;
	}
	
	/**
	 * Gets the value of the attribute {@link #node}.
	 * @return the value of the attribute {@link #node}.
	 */
	@Column(name = "NODO", nullable = false, length = NumberConstants.NUM20)
	@JsonView(DataTablesOutput.View.class)
	public String getNode() {
		return node;
	}

	/**
	 * Sets the value of the attribute {@link #node}.
	 * @param node The value for the attribute {@link #node}.
	 */
	public void setNode(String node) {
		this.node = node;
	}
}
