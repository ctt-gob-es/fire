package es.gob.fire.persistence.dto;

import java.text.SimpleDateFormat;

import es.gob.fire.persistence.entity.AuditTransaction;

public class AuditTransactionDTO {
	
	/**
	 * Attribute that represents the value of the identificator of the petition.
	 */
	private Integer idAuditTransaction;

	/**
	 * Attribute that represents the value of the date of the petition.
	 */
	private String date;
	
	/**
	 * Attribute that represents the value of the transaction identificator of the petition.
	 */
	private String idTransaction;

	/**
	 * Attribute that represents the value of the name of the app of the petition.
	 */
	private String nameApp;
	
	/**
	 * Attribute that represents the value of the identificator of the app of the petition.
	 */
	private String idApp;
	
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
	private String result;
	
	/**
	 * Attribute that represents the value of the detail of the error of the petition.
	 */
	private String errorDetail;
	
	/**
	 * Attribute that represents the value of the last node to process the petition.
	 */
	private String node;
	
	public AuditTransactionDTO(AuditTransaction petition) {
		setIdAuditTransaction(petition.getIdAuditTransaction());
		setDate(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(petition.getDate()));
		setIdApp(petition.getIdApp());
		setNameApp(petition.getNameApp());
		setOperation(petition.getOperation());
		setCryptoOperation(petition.getCryptoOperation());
		setAlgorithm(petition.getAlgorithm());
		setFormat(petition.getFormat());
		setUpdateFormat(petition.getUpdateFormat());
		setProvider(petition.getProvider());
		setForcedProvider(petition.getForcedProvider());
		setBrowser(petition.getBrowser());
		setResult(petition.getResult() ? "OK" : "ERROR");
		setIdTransaction(petition.getIdTransaction());
		setSize(petition.getSize());
		setErrorDetail(petition.getErrorDetail());
		setNode(petition.getNode());
	}

	public Integer getIdAuditTransaction() {
		return idAuditTransaction;
	}

	public void setIdAuditTransaction(Integer idAuditTransaction) {
		this.idAuditTransaction = idAuditTransaction;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getIdApp() {
		return idApp;
	}

	public void setIdApp(String idApp) {
		this.idApp = idApp;
	}
	
	public String getNameApp() {
		return nameApp;
	}

	public void setNameApp(String nameApp) {
		this.nameApp = nameApp;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getCryptoOperation() {
		return cryptoOperation;
	}

	public void setCryptoOperation(String cryptoOperation) {
		this.cryptoOperation = cryptoOperation;
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getUpdateFormat() {
		return updateFormat;
	}

	public void setUpdateFormat(String updateFormat) {
		this.updateFormat = updateFormat;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public Boolean getForcedProvider() {
		return forcedProvider;
	}

	public void setForcedProvider(Boolean forcedProvider) {
		this.forcedProvider = forcedProvider;
	}

	public String getBrowser() {
		return browser;
	}

	public void setBrowser(String browser) {
		this.browser = browser;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getIdTransaction() {
		return idTransaction;
	}

	public void setIdTransaction(String idTransaction) {
		this.idTransaction = idTransaction;
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	public String getErrorDetail() {
		return errorDetail;
	}

	public void setErrorDetail(String errorDetail) {
		this.errorDetail = errorDetail;
	}

	public String getNode() {
		return node;
	}

	public void setNode(String node) {
		this.node = node;
	}
	
}
