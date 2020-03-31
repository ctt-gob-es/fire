package es.gob.log.consumer.service;

/**
 * Datos que pueden estar fragmentados.
 */
public class DataFragment {

	private final byte[] data;
	private final boolean complete;

	/**
	 * Crea un nuevo conjunto de datos.
	 * @param data Datos del conjunto.
	 * @param complete {@code true} si los datos estan completos, {@code false} si s&oacute;lo
	 * son un fragmento de los mismos.
	 */
	public DataFragment(final byte[] data, final boolean complete) {
		this.data = data;
		this.complete = complete;
	}

	/**
	 * Crea un nuevo conjunto de datos completo.
	 * @param data Datos del conjunto.
	 */
	public DataFragment(final byte[] data) {
		this.data = data;
		this.complete = true;
	}

	/**
	 * Obtiene los datos.
	 * @return Datos del objeto.
	 */
	public byte[] getData() {
		return this.data;
	}

	/**
	 * Indica si los datos contenidos est&aacute;n completos o si son un fragmento de otro dato.
	 * @return {@code true} si est&aacute; completo, {@code false} en caso contrario.
	 */
	public boolean isComplete() {
		return this.complete;
	}
}
