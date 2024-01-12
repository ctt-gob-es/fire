package es.gob.fire.statistics.entity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

/**
 * Conjunto de datos de las transacciones de auditor&iacute;a que se registran con objeto de obtener estad&iacute;sticas.
 */
public class AuditTransactionCube {
	
	private Date date;
	private String idApplication;
	private String nameApplication;
	private String operation;
	private String cryptoOperation;
	private String format;
	private String improvedFormat;
	private String algorithm;
	private String  provider;
	private boolean mandatoryProvider = false;
	private boolean result = false;
	private String browser;
	private String errorDetail;
	private String node;

	// Propiedades para obtener el tamano de los datos de las operaciones de firma
	private String idTransaction;
	private long dataSize = 0;
	
	// Valor utilizado para almacenar el numero de ocurrencias encontradas hasta el momento
	private long total = 1;
	
	/**
	 * Construye un cubo sin datos.
	 */
	public AuditTransactionCube() {
		super();
	}
	
	public final static AuditTransactionCube parse(final String registry){
		
		if (registry == null || registry.isEmpty()) {
			throw new IllegalArgumentException("Se ha proporcionado una cadena vacia"); //$NON-NLS-1$
		}

		final String [] cube = registry.split(";"); //$NON-NLS-1$
		if (!checkRegistryData(cube)) {
			throw new IllegalArgumentException("Se ha encontrado un registro con formato no valido: " + registry); //$NON-NLS-1$
		}
		
		final AuditTransactionCube auditTransaction = new AuditTransactionCube();
		

		auditTransaction.setIdTransaction(cube[0]);
		
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		try {
			auditTransaction.setDate(format.parse(cube[1]));
		} catch (ParseException e) {
			throw new IllegalArgumentException("Ha ocurrido un error al establecer la fecha. Excepcion: " + e);
		}
		auditTransaction.setIdApplication(cube[2]);
		auditTransaction.setNameApplication(cube[3]);
		auditTransaction.setOperation(cube[4]);
		auditTransaction.setCryptoOperation(cube[5]);
		auditTransaction.setFormat(cube[6]);
		auditTransaction.setImprovedFormat(cube[7]);
		auditTransaction.setAlgorithm(cube[8]);
		auditTransaction.setProvider(cube[9]);
		auditTransaction.setMandatoryProvider(cube[10].equals("1"));
		auditTransaction.setBrowser(cube[11]);
		auditTransaction.setNode(cube[12]);
		auditTransaction.setErrorDetail(cube[13]);
		auditTransaction.setResult(cube[14].equals("1"));
		
		return auditTransaction;
		
	}
	
	/**
	 * Comprueba que un registro de datos contenga el numero de campos adecuado y
	 * que estos contengan un valor.
	 * @param registryDatas Listado de campos del registro.
	 * @return {@code true} si el registro contiene los campos requeridos,
	 * {@code false} en caso contrario.
	 */
	private static boolean checkRegistryData(final String[] registryDatas) {

		int[] nullableIndex = new int[]{7, 13};
		
		if (registryDatas == null || registryDatas.length != 15) {
			return false;
		}

		for (int i = 0; i < registryDatas.length; i++) {
			String data = registryDatas[i];
			
			if ((data == null) || (data.isEmpty() && Arrays.binarySearch(nullableIndex, i) < 0)) {
				return false;
			}
		}
		return true;
	}
	
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getIdApplication() {
		return idApplication;
	}

	public void setIdApplication(String idApplication) {
		this.idApplication = idApplication;
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

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getImprovedFormat() {
		return improvedFormat;
	}

	public void setImprovedFormat(String improvedFormat) {
		this.improvedFormat = improvedFormat;
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public boolean isMandatoryProvider() {
		return mandatoryProvider;
	}

	public void setMandatoryProvider(boolean mandatoryProvider) {
		this.mandatoryProvider = mandatoryProvider;
	}

	public boolean isResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}

	public String getBrowser() {
		return browser;
	}

	public void setBrowser(String browser) {
		this.browser = browser;
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

	public String getIdTransaction() {
		return idTransaction;
	}

	public void setIdTransaction(String idTransaction) {
		this.idTransaction = idTransaction;
	}

	public long getDataSize() {
		return dataSize;
	}

	public void setDataSize(long dataSize) {
		this.dataSize = dataSize;
	}

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}
	
	public String getNameApplication() {
		return nameApplication;
	}

	public void setNameApplication(String nameApplication) {
		this.nameApplication = nameApplication;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.idApplication, this.operation, this.provider,
				this.cryptoOperation, this.algorithm, this.browser,
				this.format, this.improvedFormat, this.node,
				new Boolean(this.mandatoryProvider), new Boolean(this.result));
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		
		// Solo implementamos un nuevo modo de comparacion cuando el objeto al que se compara es de tipo PetitionCube
		if (!(obj instanceof AuditTransactionCube)) {
			return super.equals(obj);
		}
		
		// Comparamos cada valor contenido para ver si son iguales (ambos nulos o iguales entre si)

		final AuditTransactionCube petition = (AuditTransactionCube) obj;
		
		// Id Transaccion
		if (!(getIdTransaction() == null && petition.getIdTransaction() == null ||
				getIdTransaction() != null && getIdTransaction().equals(petition.getIdTransaction()))) {
			return false;
		}
		
		// Date
		if (!(getDate() == null && petition.getDate() == null ||
				getDate() != null && getDate().equals(petition.getDate()))) {
			return false;
		}
		
		// Id Aplicacion
		if (!(getIdApplication() == null && petition.getIdApplication() == null ||
				getIdApplication() != null && getIdApplication().equals(petition.getIdApplication()))) {
			return false;
		}
		
		// Nombre Aplicacion
		if (!(getNameApplication() == null && petition.getNameApplication() == null ||
				getNameApplication() != null && getNameApplication().equals(petition.getNameApplication()))) {
			return false;
		}
		
		// Operacion
		if (!(getOperation() == null && petition.getOperation() == null ||
				getOperation() != null && getOperation().equals(petition.getOperation()))) {
			return false;
		}
		
		// Operacion criptografica
		if (!(getCryptoOperation() == null && petition.getCryptoOperation() == null ||
				getOperation() != null && getOperation().equals(petition.getOperation()))) {
			return false;
		}
		
		// Algoritmo
		if (!(getAlgorithm() == null && petition.getAlgorithm() == null ||
				getAlgorithm() != null && getAlgorithm().equals(petition.getAlgorithm()))) {
			return false;
		}

		// Formato
		if (!(getFormat() == null && petition.getFormat() == null ||
				getFormat() != null && getFormat().equals(petition.getFormat()))) {
			return false;
		}

		// Formato mejorado
		if (!(getImprovedFormat() == null && petition.getImprovedFormat() == null ||
				getImprovedFormat() != null && getImprovedFormat().equals(petition.getImprovedFormat()))) {
			return false;
		}

		// Proveedor
		if (!(getProvider() == null && petition.getProvider() == null ||
				getProvider() != null && getProvider().equals(petition.getProvider()))) {
			return false;
		}
		
		// Proveedor forzado
		if (isMandatoryProvider() != petition.isMandatoryProvider()) {
			return  false;
		}

		// Navegador
		if (!(getBrowser() == null && petition.getBrowser() == null ||
				getBrowser() != null && getBrowser().equals(petition.getBrowser()))) {
			return false;
		}
		
		// Nodo
		if (!(getNode() == null && petition.getNode() == null ||
				getNode() != null && getNode().equals(petition.getNode()))) {
			return false;
		}

		// Resultado
		if (isResult() != petition.isResult()) {
			return  false;
		}
		
		// Error detalle
		if (!(getErrorDetail() == null && petition.getErrorDetail() == null ||
				getErrorDetail() != null && getErrorDetail().equals(petition.getErrorDetail()))) {
			return false;
		}
		
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		
		//Id transaccion
		result.append(this.getIdTransaction() != null ? this.getIdTransaction() : "0");
		result.append(";");//$NON-NLS-1$
		
		//Fecha
		if (this.getDate() != null) {
			result.append(clean(format.format(this.getDate()), 19));
		}
		result.append(";");
		
		//Id Aplicacion
		if (this.getIdApplication() != null) {
			result.append(clean(this.getIdApplication(), 48));
		}
		result.append(";");//$NON-NLS-1$
		
		//Nombre Aplicacion
		if (this.getNameApplication() != null) {
			result.append(clean(this.getNameApplication(), 45));
		}
		result.append(";");//$NON-NLS-1$
		
		//Operacion
		if (this.getOperation() != null) {
			result.append(clean(this.getOperation(), 10));
		}
		result.append(";");//$NON-NLS-1$
		
		//Operacion criptografica
		if (this.getCryptoOperation() != null) {
			result.append(clean(this.getCryptoOperation(), 10));
		}
		result.append(";");//$NON-NLS-1$
		
		//Formato
		if (this.getFormat() != null) {
			result.append(clean(this.getFormat(), 20));
		}
		result.append(";");//$NON-NLS-1$
		
		//Formato mejorado
		if (this.getImprovedFormat() != null) {
			result.append(clean(this.getImprovedFormat(), 20));
		}
		result.append(";");//$NON-NLS-1$
		
		//Algoritmo
		if (this.getAlgorithm() != null) {
			result.append(clean(this.getAlgorithm(), 20));
		}
		result.append(";");//$NON-NLS-1$
		
		//Proveedor
		if (this.getProvider() != null) {
			result.append(clean(this.getProvider(), 45));
		}
		result.append(";");//$NON-NLS-1$
		
		//Proveedor forzado
		result.append(this.isMandatoryProvider() ? "1" : "0");
		result.append(";");
		
		//Navegador
		if (this.getBrowser() != null) {
			result.append(this.getBrowser());
		}
		result.append(";");
		
		//Nodo
		if (this.getNode() != null) {
			result.append(this.getNode());
		}
		result.append(";");
		
		//Error detalle
		if (this.getErrorDetail() != null) {
			result.append(this.getErrorDetail());
		}
		result.append(";");
		
		//Resultado
		result.append(this.isResult() ? "1" : "0");
		
		return result.toString();
	}
	
	/**
	 * Elimina caracteres problem&aacute;ticos de un texto y lo ajusta a un tama&ntilde;o m&aacute;ximo.
	 * @param text Texto que hay que limpiar.
	 * @param maxLength Longitud m&aacute;xima del texto.
	 * @return Cadena de texto limpia.
	 */
	private static String clean(final String text, final int maxLength) {
		String cleanedText = text.replace(';', ' ').replace('\n', ' ');
		if (maxLength > 0 && cleanedText.length() > maxLength) {
			cleanedText = cleanedText.substring(0,  maxLength);
		}
		return cleanedText.trim();
	}
	
}
