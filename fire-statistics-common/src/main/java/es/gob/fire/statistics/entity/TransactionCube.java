package es.gob.fire.statistics.entity;

import java.text.ParseException;
import java.util.Objects;

/** Conjunto de datos de las transacciones que se registran con objeto de obtener
 * estad&iacute;sticas. */
public final class TransactionCube {

	private String application;
	private String operation;
	private String  provider;
	private  boolean mandatoryProvider = false;
	private  boolean resultTransaction = false;

	// Propiedades para obtener el tamano de los datos de las operaciones de firma
	private String idTransaction;
	private long dataSize = 0;

	// Valor utilizado para almacenar el numero de ocurrencias encontradas hasta el momento
	private long total = 1;

	/**
	 * Construye un cubo sin datos.
	 */
	public TransactionCube() {
		super();
	}

	/**
	 * Construye un objeto cargando sus propiedades de una cadena de texto con el formato
	 * "Aplicacion;Operacion;Proveedor;ProveedorForzado;ResultadoOperacion;IdTransaccion".<br>
	 * <ul>
	 * <li>ProveedorForzado => 0 si es {@code false}, 1 si es {@code true}.</li>
	 * <li>resultSign => 0 si es {@code false}, 1 si es {@code true}.</li>
	 * </ul>
	 * @param registry Cadena de texto con las propiedades de la transacci&oacute;n.
	 * @return Informaci&oacute;n de la transacci&oacute;n.
	 * @throws ParseException Cuando se encuentra una fecha mal formada.
	 */
	public final static TransactionCube parse(final String registry) throws ParseException {

		if (registry == null || registry.isEmpty()) {
			throw new IllegalArgumentException("Se ha proporcionado una cadena vacia"); //$NON-NLS-1$
		}

		final String [] cube = registry.split(";"); //$NON-NLS-1$
		if (!checkRegistryData(cube)) {
			throw new IllegalArgumentException("Se ha encontrado un registro con formato no valido: " + registry); //$NON-NLS-1$
		}

		final TransactionCube trans =  new TransactionCube();

		// Aplicacion
		trans.setApplication(cube[0]);

		// Operacion
		trans.setOperation(cube[1]);

		// Proveedor
		trans.setProvider(cube[2]);

		// Proveedor forzado
		trans.setMandatoryProvider(cube[3].equals("1")); //$NON-NLS-1$

		// Resultado de la transaccion
		trans.setResultTransaction(cube[4].equals("1")); //$NON-NLS-1$

		// Identificador de la transaccion
		trans.setIdTransaction(cube[5]);

		return trans;
	}


	/**
	 * Comprueba que un registro de datos contenga el numero de campos adecuado y
	 * que estos contengan un valor.
	 * @param registryDatas Listado de campos del registro.
	 * @return {@code true} si el registro contiene los campos requeridos,
	 * {@code false} en caso contrario.
	 */
	private static boolean checkRegistryData(final String[] registryDatas) {

		if (registryDatas == null || registryDatas.length != 6) {
			return false;
		}

		for (final String data : registryDatas) {
			if (data == null || data.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	/* Propiedades  Getter & Setter*/

	public final String getApplication() {
		return this.application;
	}

	public final void setApplication(final String application) {
		this.application = application;
	}

	public final String getOperation() {
		return this.operation;
	}

	public final void setOperation(final String operation) {
		this.operation = operation;
	}

	public final String getProvider() {
		return this.provider;
	}

	public final void setProvider(final String proveedor) {
		this.provider = proveedor;
	}

	public final boolean isMandatoryProvider() {
		return this.mandatoryProvider;
	}

	public final void setMandatoryProvider(final boolean mandatoryProvider) {
		this.mandatoryProvider = mandatoryProvider;
	}

	public final boolean isResultTransaction() {
		return this.resultTransaction;
	}

	public final void setResultTransaction(final boolean resultTransaction) {
		this.resultTransaction = resultTransaction;
	}

	public String getIdTransaction() {
		return this.idTransaction;
	}

	public void setIdTransaction(final String idTransaction) {
		this.idTransaction = idTransaction;
	}

	public final long getDataSize() {
		return this.dataSize;
	}

	public final void setDataSize(final long size) {
		this.dataSize = size;
	}

	public void addToTotal(final long n) {
		this.total += n;
	}

	public long getTotal() {
		return this.total;
	}

	/**
	 * Devuelve una cadena con las propiedades del objeto
	 * con el formato "Aplicacion;Operacion;Proveedor;ProveedorForzado;ResultadoOperacion;IdTransaccion".<br>
	 * <ul>
	 * <li>ProveedorForzado => 0 si es {@code false}, 1 si es {@code true}.</li>
	 * <li>resultSign => 0 si es {@code false}, 1 si es {@code true}.</li>
	 * </ul>
	 */
	@Override
	public final String toString() {

		final StringBuilder result = new StringBuilder();

		if (getApplication() != null) {
			result.append(clean(getApplication(), 45));
		}
		result.append(";");//$NON-NLS-1$

		if (getOperation() != null) {
			result.append(clean(getOperation(), 10));
		}
		result.append(";");//$NON-NLS-1$

		if (getProvider() != null  && !getProvider().isEmpty()) {
			result.append(clean(getProvider(), 45));
		}
		result.append(";") //$NON-NLS-1$
			.append(isMandatoryProvider() ? "1" : "0").append(";")  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			.append(isResultTransaction() ? "1" : "0").append(";")  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			.append(getIdTransaction() != null ? getIdTransaction() : "0"); //$NON-NLS-1$

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

	@Override
	public boolean equals(final Object obj) {

		if (obj == null) {
			return false;
		}

		// Solo implementamos un nuevo modo de comparacion cuando el objeto al que se compara
		// es de tipo TransactionCube
		if (!(obj instanceof TransactionCube)) {
			return super.equals(obj);
		}

		// Comparamos cada valor contenido para ver si son iguales (ambos nulos o iguales entre si)

		final TransactionCube transaction = (TransactionCube) obj;

		// Comparamos cada valor contenido para ver si son iguales (ambos nulos o iguales entre si)

		// Aplicacion
		if (!(getApplication() == null && transaction.getApplication() == null ||
				getApplication() != null && getApplication().equals(transaction.getApplication()))) {
			return false;
		}

		// Operacion
		if (!(getOperation() == null && transaction.getOperation() == null ||
				getOperation() != null && getOperation().equals(transaction.getOperation()))) {
			return false;
		}

		// Proveedor
		if (!(getProvider() == null && transaction.getProvider() == null ||
				getProvider() != null && getProvider().equals(transaction.getProvider()))) {
			return false;
		}

		// Proveedor forzado
		if (isMandatoryProvider() != transaction.isMandatoryProvider()) {
			return  false;
		}

		// Resultado
		if (isResultTransaction() != transaction.isResultTransaction()) {
			return  false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.application, this.operation, this.provider,
				new Boolean(this.mandatoryProvider), new Boolean(this.resultTransaction));
	}
}
