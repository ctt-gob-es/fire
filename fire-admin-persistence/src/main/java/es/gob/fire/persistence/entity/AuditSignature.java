package es.gob.fire.persistence.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

import es.gob.fire.commons.utils.NumberConstants;

/**
 * <p>Class that maps the <i>TB_AUDIT_FIRMAS</i> database table as a Plain Old Java Object.</p>
 * <b>Project:</b><p>Application for monitoring services of @firma suite systems.</p>
 * @version 1.0, 10/08/2023.
 */
@Entity
@Table(name = "TB_AUDIT_FIRMAS")
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class AuditSignature {

	/**
	 * Attribute that represents the value of the identificator.
	 */
	private String idAuditSignature;
	
	/**
	 * Attribute that represents the value of the transaction identificator of the petition.
	 */
	private String idTransaction;
	
	/**
	 * Attribute that represents the value of the identifier of the document inside the batch.
	 */
	private String idIntLote;
	
	/**
	 * Attribute that represents the cryptographic type of operation made by the petition.
	 */
	private String cryptoOperation;
	
	/**
	 * Attribute that represents the value of the format of the signature.
	 */
	private String format;
	
	/**
	 * Attribute that represents the value of the format of the signature.
	 */
	private String updateFormat;
	
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
	 * Gets the value of the attribute {@link #idAuditSignature}.
	 * @return the value of the attribute {@link #idAuditSignature}.
	 */
	@Id
	@Column(name = "ID", unique = true, nullable = false, precision = NumberConstants.NUM48)
	@JsonView(DataTablesOutput.View.class)
	public String getIdAuditSignature() {
		return this.idAuditSignature;
	}
	
	/**
	 * Sets the value of the attribute {@link #idAuditSignature}.
	 * @param idPetitionBatchSignature The value for the attribute {@link #idAuditSignature}.
	 */
	public void setIdAuditSignature(final String idPetitionBatchSignature) {
		this.idAuditSignature = idPetitionBatchSignature;
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
	 * Gets the value of the attribute {@link #format}.
	 * @return the value of the attribute {@link #format}.
	 */
	@Column(name = "ID_INT_LOTE", nullable = false, length = NumberConstants.NUM20)
	@JsonView(DataTablesOutput.View.class)
	public String getIdIntLote() {
		return idIntLote;
	}

	/**
	 * Sets the value of the attribute {@link #idIntLote}.
	 * @param idIntLote The value for the attribute {@link #idIntLote}.
	 */
	public void setIdIntLote(String idIntLote) {
		this.idIntLote = idIntLote;
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
	
}
