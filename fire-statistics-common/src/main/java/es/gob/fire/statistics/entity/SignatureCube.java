package es.gob.fire.statistics.entity;

import java.text.ParseException;
import java.util.Date;
import java.util.Objects;

/**
 * Conjunto de datos de las firmas que se registran con objeto de obtener estad&iacute;sticas.
 */
public class SignatureCube {

	private Date date;
	private String application;
	private String format;
	private String improvedFormat;
	private String algorithm;
	private String provider;
	private String browser;
	private boolean resultSign = false;

	// Propiedad para trasladar el tamano de los datos a las transacciones
	private String idTransaction;
	private long dataSize = 0;

	// Valor utilizado para almacenar el numero de ocurrencias encontradas hasta el momento
	private long total = 1;

	/**
	 * Construye un objeto cargando sus propiedades de una cadena de texto con el formato
	 * "Formato;FormatoMejorado;Algoritmo;Proveedor;Navegador;ResultadoOperacion;IdTransaccion;Tama&ntilde;oFichero".<br>
	 * <ul>
	 * <li>FormatoMejorado: Formato longevo de firma o vacio si no se solicit&oacute; actualizar.</li>
	 * <li>ResultadoOperacion: 0 si la operaci&oacute;n fall&oacute;, 1 en caso contrario.</li>
	 * <li>Tama&ntilde;oFichero: Tama&ntilde;o del fichero firmado en bytes.</li>
	 * </ul>
	 * @param registry Cadena de texto con las propiedades de la transacci&oacute;n.
	 * @return Informaci&oacute;n de la transacci&oacute;n.
	 * @throws ParseException Cuando se encuentra una fecha mal formada.
	 */
	public final static SignatureCube parse(final String registry) throws ParseException, NumberFormatException {

		if (registry == null || registry.isEmpty()) {
			throw new IllegalArgumentException("Se ha proporcionado una cadena vacia"); //$NON-NLS-1$
		}

		final String [] cube = registry.split(";"); //$NON-NLS-1$
		if (!checkRegistryData(cube)) {
			throw new IllegalArgumentException("Se ha encontrado un registro con formato no valido: " + registry); //$NON-NLS-1$
		}

		final SignatureCube sign =  new SignatureCube();

		// Aplicacion
		sign.setApplication(cube[0]);

		// Formato
		sign.setFormat(cube[1]);

		// Formato Mejorado
		sign.setImprovedFormat(cube[2].isEmpty() ? null : cube[2]);

		// Algoritmo
		sign.setAlgorithm(cube[3]);

		// Proveedor
		sign.setProvider(cube[4]);

		// Navegador
		sign.setBrowser(cube[5]);

		// Resultado de la firma
		sign.setResultSign("1".equals(cube[6])); //$NON-NLS-1$

		// Identificador de la transaccion
		sign.setIdTransaction(cube[7]);

		// Tamano de la firma
		sign.setDataSize(Long.parseLong(cube[8]));

		return sign;
	}

	/**
	 * Comprueba que un registro de datos contenga el numero de campos adecuado y
	 * que estos contengan un valor.
	 * @param registryDatas Listado de campos del registro.
	 * @return {@code true} si el registro contiene los campos requeridos,
	 * {@code false} en caso contrario.
	 */
	private static boolean checkRegistryData(final String[] registryDatas) {

		if (registryDatas == null || registryDatas.length != 9) {
			return false;
		}

		for (int i = 0; i < registryDatas.length; i++) {

			// El campo de formato mejorado puede estar vacio
			if (i == 2) {
				continue;
			}

			final String data = registryDatas[i];
			if (data == null || data.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	/* Getter & Setter */

	/**
	 * Obtiene la fecha.
	 * @return Fecha de firma.
	 */
	public final Date getDate() {
		return this.date;
	}
	/**
	 * Establece la fecha.
	 * @param date Fecha de firma.
	 */
	public final void setDate(final Date date) {
		this.date = date;
	}
	/**
	 * Obtiene el formtato del cubo de la firma
	 * @return Formato de firma.
	 */
	public final String getFormat() {
		return this.format;
	}
	/**
	 * Establece el formtato del cubo de la firma
	 * @param format Formato de firma.
	 */
	public final void setFormat(final String format) {
		this.format = format;
	}
	/**
	 * Obtiene el algoritmo del cubo de la firma
	 * @return Algoritmo de firma.
	 */
	public final String getAlgorithm() {
		return this.algorithm;
	}
	/**
	 *Establece el algoritmo del cubo de la firma
	 * @param algorithm Algoritmo de firma.
	 */
	public final void setAlgorithm(final String algorithm) {
		this.algorithm = algorithm;
	}
	/**
	 * Obtiene el proveedor del cubo de la firma
	 * @return Proveedor de certificados utilizado en la firma.
	 */
	public final String getProvider() {
		return this.provider;
	}

	/**
	 * Establece el proveedor del cubo de la firma
	 * @param provider Proveedor de certificados utilizado en la firma.
	 */
	public final void setProvider(final String provider) {
		this.provider = provider;
	}

	/**
	 * Obtiene el navegador  del cubo de la firma.
	 * @return Navegador utilizado en la operaci&oacute;n.
	 */
	public final String getBrowser() {
		return this.browser;
	}

	/**
	 *  Establece el navegador del cubo de la firma.
	 * @param browser Navegador utilizado en la operaci&oacute;n.
	 */
	public final void setBrowser(final String browser) {
		this.browser = browser;
	}

	/**
	 * Indica si la firma termin&oacute; correctamente.
	 * @return {@code true} si la firma se complet&oacute; correctamente, {@code false} en caso contrario.
	 */
	public final boolean isResultSign() {
		return this.resultSign;
	}
	/**
	 * Establece si la firma termin&oacute; correctamente o no.
	 * @param resultSign Resultado de la operaci&oacute;n de firma.
	 */
	public final void setResultSign(final boolean resultSign) {
		this.resultSign = resultSign;
	}

	/**
	 * Recupera el identificador de transaci&oacute;n en la cual se realiz&oacute; la firma.
	 * @return Identificador de transaci&oacute;n en la cual se realiz&oacute; la firma.
	 */
	public String getIdTransaction() {
		return this.idTransaction;
	}

	/**
	 * Establece el identificador de transaci&oacute;n en la cual se realiz&oacute; la firma.
	 * @param idTransaction Identificador de transaci&oacute;n en la cual se realiz&oacute; la firma.
	 */
	public void setIdTransaction(final String idTransaction) {
		this.idTransaction = idTransaction;
	}

	/**
	 * Recupera el tama&ntilde;o de datos firmados.
	 * @return Tama&ntilde;o de los datos firmados.
	 */
	public final long getDataSize() {
		return this.dataSize;
	}

	/**
	 * Establece el tama&ntilde;o de datos firmados.
	 * @param size Tama&ntilde;o de los datos firmados.
	 */
	public final void setDataSize(final long size) {
		this.dataSize = size;
	}

	/**
	 * Establece el formato longevo al que se actualiz&oacute; la firma.
	 * @return Formato longevo al que se actualiz&oacute; la firma.
	 */
	public final String getImprovedFormat() {
		return this.improvedFormat;
	}

	/**
	 * Recupera el formato longevo al que se actualiz&oacute; la firma.
	 * @param improvedFormat Formato longevo al que se actualiz&oacute; la firma.
	 */
	public final void setImprovedFormat(final String improvedFormat) {
		this.improvedFormat = improvedFormat;
	}

	/**
	 * Recupera el c&oacute;digo de la aplicaci&oacute;n que realiz&oacute; la firma.
	 * @return C&oacute;digo de la aplicaci&oacute;n que realiz&oacute; la firma.
	 */
	public final String getApplication() {
		return this.application;
	}

	/**
	 * Establece el c&oacute;digo de la aplicaci&oacute;n que realiz&oacute; la firma.
	 * @param aplication C&oacute;digo de la aplicaci&oacute;n que realiz&oacute; la firma.
	 */
	public final void setApplication(final String aplication) {
		this.application = aplication;
	}

	/**
	 * Agrega un n&uacute;mero de firmas al total de firmas acorde a la configuraci&oacute;n establecida.
	 * @param n N&uacute;mero de firmas.
	 */
	public void addToTotal(final long n) {
		this.total += n;
	}

	/**
	 * Recupera el n&uacute;mero total de firmas que se realizaron acorde a la configuraci&oacute;n establecida.
	 * @return N&uacute;mero total de firmas.
	 */
	public long getTotal() {
		return this.total;
	}

	/**
	 * Devuelve una cadena con las propiedades del objeto con el formato
	 * "Formato;FormatoMejorado;Algoritmo;Proveedor;Navegador;ResultadoOperacion;IdTransaccion;Tama&ntilde;oFichero".<br>
	 * <ul>
	 * <li>FormatoMejorado: Formato longevo de firma o vacio si no se solicit&oacute; actualizar.</li>
	 * <li>ResultadoOperacion: 0 si la operaci&oacute;n fall&oacute;, 1 en caso contrario.</li>
	 * <li>Tama&ntilde;oFichero: Tama&ntilde;o del fichero firmado en bytes.</li>
	 * </ul>
	 */
	@Override
	public String toString() {

		final StringBuilder result = new StringBuilder();

		// Aplicacion
		if (this.getApplication() != null) {
			result.append(clean(this.getApplication(), 45));
		}
		result.append(";");//$NON-NLS-1$

		// Formato
		if (this.getFormat() != null) {
			result.append(clean(this.getFormat(), 20));
		}
		result.append(";");//$NON-NLS-1$

		// Formato forzado
		if (this.getImprovedFormat() != null) {
			result.append(clean(this.getImprovedFormat(), 20));
		}
		result.append(";");//$NON-NLS-1$

		// Algoritm
		if (this.getAlgorithm() != null) {
			result.append(clean(this.getAlgorithm(), 20));
		}
		result.append(";");//$NON-NLS-1$

		// Nombre del proveedor
		if (this.getProvider() != null) {
			result.append(clean(this.getProvider(), 45));
		}
		result.append(";");//$NON-NLS-1$

		// Navegador
		if (this.getBrowser() != null) {
			result.append(this.getBrowser());
		}
		result.append(";") //$NON-NLS-1$
			.append(this.isResultSign() ? "1" : "0").append(";")  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			.append(this.getIdTransaction()).append(";") //$NON-NLS-1$
			.append(this.getDataSize());

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
	 * Compara este objeto con otro.
	 * @param obj Objeto con el que comparar.
	 * @return true si son iguales , false si son distintos
	 */
	@Override
	public boolean equals(final Object obj) {

		if (obj == null) {
			return false;
		}

		// Solo implementamos un nuevo modo de comparacion cuando el objeto al que se compara
		// es de tipo SignatureCube
		if (!(obj instanceof SignatureCube)) {
			return super.equals(obj);
		}

		// Comparamos cada valor contenido para ver si son iguales (ambos nulos o iguales entre si)

		final SignatureCube signature = (SignatureCube) obj;

		// Aplicacion
		if (!(getApplication() == null && signature.getApplication() == null ||
				getApplication() != null && getApplication().equals(signature.getApplication()))) {
			return false;
		}

		// Algoritmo
		if (!(getAlgorithm() == null && signature.getAlgorithm() == null ||
				getAlgorithm() != null && getAlgorithm().equals(signature.getAlgorithm()))) {
			return false;
		}

		// Formato
		if (!(getFormat() == null && signature.getFormat() == null ||
				getFormat() != null && getFormat().equals(signature.getFormat()))) {
			return false;
		}

		// Formato longevo
		if (!(getImprovedFormat() == null && signature.getImprovedFormat() == null ||
				getImprovedFormat() != null && getImprovedFormat().equals(signature.getImprovedFormat()))) {
			return false;
		}

		// Proveedor
		if (!(getProvider() == null && signature.getProvider() == null ||
				getProvider() != null && getProvider().equals(signature.getProvider()))) {
			return false;
		}

		// Navegador
		if (!(getBrowser() == null && signature.getBrowser() == null ||
				getBrowser() != null && getBrowser().equals(signature.getBrowser()))) {
			return false;
		}

		// Resultado
		if (isResultSign() != signature.isResultSign()) {
			return  false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.application, this.algorithm, this.format, this.improvedFormat,
				this.provider, this.browser, new Boolean(this.resultSign));
	}
}
