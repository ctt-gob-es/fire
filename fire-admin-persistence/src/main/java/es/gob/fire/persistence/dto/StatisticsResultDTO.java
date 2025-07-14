package es.gob.fire.persistence.dto;

import java.util.List;
import java.util.Map;

import es.gob.fire.commons.utils.QueryEnum;

public class StatisticsResultDTO {
	private List<TransactionDTO> transactions;
	private List<SignatureDTO> signatures;
	Map<String, List<TransactionDTO>> transactionsByMonth;
	Map<String, List<SignatureDTO>> signaturesByMonth;
	
	private String queryName;
	
	private Boolean isSignatureQuery;
	private Boolean isQueryWithCorrectAndIncorrect;
	private Boolean isQueryBySize;
	private Boolean isQueryByOperation;
	private Boolean isQueryByOrganization;
	
	private Boolean enableDonutChart;
	private Boolean enableBarChart;
	private Boolean enableBarTimeChart;

	public StatisticsResultDTO() {}

	public StatisticsResultDTO(List<TransactionDTO> transactions, List<SignatureDTO> signatures,
			Map<String, List<TransactionDTO>> transactionsByMonth, Map<String, List<SignatureDTO>> signaturesByMonth,
			String queryName, Boolean isSignatureQuery, Boolean isQueryWithCorrectAndIncorrect, Boolean isQueryBySize,
			Boolean isQueryByOperation, Boolean enableDonutChart, Boolean enableBarChart, Boolean enableBarTimeChart) {
		super();
		this.transactions = transactions;
		this.signatures = signatures;
		this.transactionsByMonth = transactionsByMonth;
		this.signaturesByMonth = signaturesByMonth;
		this.queryName = queryName;
		this.isSignatureQuery = isSignatureQuery;
		this.isQueryWithCorrectAndIncorrect = isQueryWithCorrectAndIncorrect;
		this.isQueryBySize = isQueryBySize;
		this.isQueryByOperation = isQueryByOperation;
		this.enableDonutChart = enableDonutChart;
		this.enableBarChart = enableBarChart;
		this.enableBarTimeChart = enableBarTimeChart;
	}

	public List<TransactionDTO> getTransactions() {
		return transactions;
	}

	public void setTransactions(List<TransactionDTO> transactions) {
		this.transactions = transactions;
	}

	public List<SignatureDTO> getSignatures() {
		return signatures;
	}

	public void setSignatures(List<SignatureDTO> signatures) {
		this.signatures = signatures;
	}
	
	public Map<String, List<TransactionDTO>> getTransactionsByMonth() {
		return transactionsByMonth;
	}

	public void setTransactionsByMonth(Map<String, List<TransactionDTO>> transactionsByMonth) {
		this.transactionsByMonth = transactionsByMonth;
	}

	public Map<String, List<SignatureDTO>> getSignaturesByMonth() {
		return signaturesByMonth;
	}

	public void setSignaturesByMonth(Map<String, List<SignatureDTO>> signaturesByMonth) {
		this.signaturesByMonth = signaturesByMonth;
	}

	public Boolean getIsSignatureQuery() {
		return isSignatureQuery;
	}

	public void setIsSignatureQuery(Boolean isSignatureQuery) {
		this.isSignatureQuery = isSignatureQuery;
	}

	public Boolean getIsQueryByOperation() {
		return isQueryByOperation;
	}

	public void setIsQueryByOperation(Boolean isQueryByOperation) {
		this.isQueryByOperation = isQueryByOperation;
	}

	public Boolean getEnableDonutChart() {
		return enableDonutChart;
	}

	public void setEnableDonutChart(Boolean enableDonutChart) {
		this.enableDonutChart = enableDonutChart;
	}

	public Boolean getEnableBarChart() {
		return enableBarChart;
	}

	public void setEnableBarChart(Boolean enableBarChart) {
		this.enableBarChart = enableBarChart;
	}

	public Boolean getEnableBarTimeChart() {
		return enableBarTimeChart;
	}

	public void setEnableBarTimeChart(Boolean enableBarTimeChart) {
		this.enableBarTimeChart = enableBarTimeChart;
	}

	public String getQueryName() {
		return queryName;
	}

	public void setQueryName(String queryName) {
		this.queryName = queryName;
	}

	public Boolean getIsQueryBySize() {
		return isQueryBySize;
	}

	public void setIsQueryBySize(Boolean isQueryBySize) {
		this.isQueryBySize = isQueryBySize;
	}
	
    public Boolean getIsQueryWithCorrectAndIncorrect() {
		return isQueryWithCorrectAndIncorrect;
	}

	public void setIsQueryWithCorrectAndIncorrect(Boolean isQueryWithCorrectAndIncorrect) {
		this.isQueryWithCorrectAndIncorrect = isQueryWithCorrectAndIncorrect;
	}
	
	public Boolean getIsQueryByOrganization() {
		return isQueryByOrganization;
	}

	public void setIsQueryByOrganization(Boolean isQueryByOrganization) {
		this.isQueryByOrganization = isQueryByOrganization;
	}

	public StatisticsResultDTO obtainStatisticsResultByQueryType(List<?> result, String queryName) {
        // Asignamos el query recibido.
        this.setQueryName(queryName);
        
        // Reiniciamos los flags antes de asignarlos de forma explícita
        this.setIsQueryWithCorrectAndIncorrect(Boolean.FALSE);
        this.setIsQueryBySize(Boolean.FALSE);
        this.setIsQueryByOperation(Boolean.FALSE);
        this.setIsSignatureQuery(Boolean.FALSE);
        
        if(queryName.equalsIgnoreCase(QueryEnum.TRANSACTIONS_ENDED_BY_APP.getName())) {
            this.setIsQueryWithCorrectAndIncorrect(Boolean.TRUE);
            this.setTransactions((List<TransactionDTO>) result);
            this.setEnableDonutChart(Boolean.TRUE);
            this.setEnableBarChart(Boolean.TRUE);
            this.setEnableBarTimeChart(Boolean.FALSE);
        } else if(queryName.equalsIgnoreCase(QueryEnum.TRANSACTIONS_ENDED_BY_PROVIDER.getName())) {
            this.setIsQueryWithCorrectAndIncorrect(Boolean.TRUE);
            this.setTransactions((List<TransactionDTO>) result);
            this.setEnableDonutChart(Boolean.TRUE);
            this.setEnableBarChart(Boolean.TRUE);
            this.setEnableBarTimeChart(Boolean.FALSE);
        } else if(queryName.equalsIgnoreCase(QueryEnum.TRANSACTIONS_BY_DATES_SIZE_APP.getName())) {
            this.setIsQueryBySize(Boolean.TRUE);
            this.setTransactions((List<TransactionDTO>) result);
            this.setEnableDonutChart(Boolean.FALSE);
            this.setEnableBarChart(Boolean.TRUE);
            this.setEnableBarTimeChart(Boolean.FALSE);
        } else if(queryName.equalsIgnoreCase(QueryEnum.TRANSACTIONS_BY_TYPE_TRANSACTION.getName())) {
            this.setIsQueryByOperation(Boolean.TRUE);
            this.setTransactions((List<TransactionDTO>) result);
            this.setEnableDonutChart(Boolean.TRUE);
            this.setEnableBarChart(Boolean.TRUE);
            this.setEnableBarTimeChart(Boolean.FALSE);
        } else if(queryName.equalsIgnoreCase(QueryEnum.TRANSACTIONS_ENDED_BY_ORGANISM.getName())) {
            this.setIsQueryByOrganization(Boolean.TRUE);
            this.setTransactions((List<TransactionDTO>) result);
            this.setEnableDonutChart(Boolean.TRUE);
            this.setEnableBarChart(Boolean.TRUE);
            this.setEnableBarTimeChart(Boolean.FALSE);
        } 
        // Consultas de firmas
        else if(queryName.equalsIgnoreCase(QueryEnum.DOCUMENTS_SIGNED_BY_APP.getName())) {
            this.setIsSignatureQuery(Boolean.TRUE);
            this.setSignatures((List<SignatureDTO>) result);
            this.setEnableDonutChart(Boolean.TRUE);
            this.setEnableBarChart(Boolean.TRUE);
            this.setEnableBarTimeChart(Boolean.FALSE);
        } else if(queryName.equalsIgnoreCase(QueryEnum.DOCUMENTS_SIGNED_BY_PROVIDER.getName())) {
            this.setIsSignatureQuery(Boolean.TRUE);
            this.setSignatures((List<SignatureDTO>) result);
            this.setEnableDonutChart(Boolean.TRUE);
            this.setEnableBarChart(Boolean.TRUE);
            this.setEnableBarTimeChart(Boolean.FALSE);
        } else if(queryName.equalsIgnoreCase(QueryEnum.DOCUMENTS_SIGNED_BY_SIGNATURE_FORMAT.getName())) {
            this.setIsSignatureQuery(Boolean.TRUE);
            this.setSignatures((List<SignatureDTO>) result);
            this.setEnableDonutChart(Boolean.TRUE);
            this.setEnableBarChart(Boolean.TRUE);
            this.setEnableBarTimeChart(Boolean.FALSE);
        } else if(queryName.equalsIgnoreCase(QueryEnum.DOCUMENTS_USED_IN_SIGNATURE_FORMAT.getName())) {
            this.setIsSignatureQuery(Boolean.TRUE);
            this.setSignatures((List<SignatureDTO>) result);
            this.setEnableDonutChart(Boolean.TRUE);
            this.setEnableBarChart(Boolean.TRUE);
            this.setEnableBarTimeChart(Boolean.FALSE);
        } else if(queryName.equalsIgnoreCase(QueryEnum.DOCUMENTS_SIGNED_BY_ORGANISM.getName())) {
            this.setIsQueryByOrganization(Boolean.TRUE);
            this.setSignatures((List<SignatureDTO>) result);
            this.setEnableDonutChart(Boolean.TRUE);
            this.setEnableBarChart(Boolean.TRUE);
            this.setEnableBarTimeChart(Boolean.FALSE);
        }
        
        return this;
    }
}
