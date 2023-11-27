package es.gob.fire.statistics.entity;

import java.util.Arrays;
import java.util.Objects;

/**
 * Conjunto de datos de las firmas de auditor&iacute;a que se registran con objeto de obtener estad&iacute;sticas.
 */
public class AuditSignatureCube {
	private String idIntLote;
	private String cryptoOperation;
	private String format;
	private String improvedFormat;
	private boolean result = false;
	private String errorDetail;

	// Propiedades para obtener el tamano de los datos de las operaciones de firma
	private String idTransaction;
	private long dataSize = 0;
	
	// Valor utilizado para almacenar el numero de ocurrencias encontradas hasta el momento
	private long total = 1;
	
	/**
	 * Construye un cubo sin datos.
	 */
	public AuditSignatureCube() {
		super();
	}
	
	public final static AuditSignatureCube parse(final String registry){
		
		if (registry == null || registry.isEmpty()) {
			throw new IllegalArgumentException("Se ha proporcionado una cadena vacia"); //$NON-NLS-1$
		}

		final String [] cube = registry.split(";"); //$NON-NLS-1$
		if (!checkRegistryData(cube)) {
			throw new IllegalArgumentException("Se ha encontrado un registro con formato no valido: " + registry); //$NON-NLS-1$
		}
		
		final AuditSignatureCube auditSignature = new AuditSignatureCube();
		
		auditSignature.setIdTransaction(cube[0]);
		auditSignature.setIdIntLote(cube[1]);
		auditSignature.setCryptoOperation(cube[2]);
		auditSignature.setFormat(cube[3]);
		auditSignature.setImprovedFormat(cube[4]);
		auditSignature.setDataSize(Long.parseLong(cube[5]));
		auditSignature.setErrorDetail(cube[6]);
		auditSignature.setResult(cube[7].equals("1"));
		
		return auditSignature;
		
	}
	
	/**
	 * Comprueba que un registro de datos contenga el numero de campos adecuado y
	 * que estos contengan un valor.
	 * @param registryDatas Listado de campos del registro.
	 * @return {@code true} si el registro contiene los campos requeridos,
	 * {@code false} en caso contrario.
	 */
	private static boolean checkRegistryData(final String[] registryDatas) {

		int[] nullableIndex = new int[]{1, 4, 6};
		
		if (registryDatas == null || registryDatas.length != 8) {
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
	
	public String getIdIntLote() {
		return idIntLote;
	}

	public void setIdIntLote(String idIntLote) {
		this.idIntLote = idIntLote;
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

	public boolean isResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}

	public String getErrorDetail() {
		return errorDetail;
	}

	public void setErrorDetail(String errorDetail) {
		this.errorDetail = errorDetail;
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

	@Override
	public int hashCode() {
		return Objects.hash(this.cryptoOperation, this.format, this.improvedFormat, new Boolean(this.result));
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		
		// Solo implementamos un nuevo modo de comparacion cuando el objeto al que se compara es de tipo PetitionCube
		if (!(obj instanceof AuditSignatureCube)) {
			return super.equals(obj);
		}
		
		// Comparamos cada valor contenido para ver si son iguales (ambos nulos o iguales entre si)

		final AuditSignatureCube petition = (AuditSignatureCube) obj;
		
		// Id Int Lote
		if (!(getIdIntLote() == null && petition.getIdIntLote() == null ||
				getIdIntLote() != null && getIdIntLote().equals(petition.getIdIntLote()))) {
			return false;
		}
		
		// Id Transaction
		if (!(getIdTransaction() == null && petition.getIdTransaction() == null ||
				getIdTransaction() != null && getIdTransaction().equals(petition.getIdTransaction()))) {
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

		// Resultado
		if (isResult() != petition.isResult()) {
			return  false;
		}
		
		/*
		// Error detalle
		if (!(getErrorDetail() == null && petition.getErrorDetail() == null ||
				getErrorDetail() != null && getErrorDetail().equals(petition.getErrorDetail()))) {
			return false;
		}
		*/
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		
		//Id transaccion
		result.append(this.getIdTransaction() != null ? this.getIdTransaction() : "0");
		result.append(";");//$NON-NLS-1$
		
		//Id Int Lote
		if (this.getIdIntLote() != null) {
			result.append(clean(this.getIdIntLote(), 48));
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
		
		//Tamano
		if ((Long)this.getDataSize() != null) {
			result.append(this.getDataSize());
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
	
	/**
	 * Comprueba cual de los objetos contiene un detalle de error
	 * @param signature Objeto AuditSignatureCube con el que comparar.
	 * @return int con el resultado.
	 */
	public int checkErrorDetail(AuditSignatureCube signature) {
	    if (getErrorDetail() == null && signature.getErrorDetail() == null) {
	        return 1; // Ambos nulos
	    } else if (getErrorDetail() != null && signature.getErrorDetail() != null) {
	        if (getErrorDetail().equals(signature.getErrorDetail())) {
	            return 1; // Iguales
	        } else {
	            return 2; // Diferentes
	        }
	    } else if (getErrorDetail() == null) {
	        return 3; // El de la firma no es nulo, el actual sí
	    } else {
	        return 4; // El de la firma es nulo, el actual no
	    }
	}






	
}
