package es.gob.fire.server.services.internal;

import es.gob.fire.signature.ProviderElement;

/**
 * Configuraci&oacute;n de aplicaci&oacute;n.
 */
public class ApplicationOperationConfig {

	/** Taman&tilde;o m&aacute;ximo en bytes  de una petici&oacute;n al servicio. */
	private long requestMaxSize = 0;

	/**
	 * Taman&tilde;o m&aacute;ximo en bytes  de cada par&aacute;metro enviado al
	 * servicio. Ser&aacute; el tama&ntilde;o m&aacute;ximo de fichero aceptado.
	 */
	private int paramsMaxSize = 0;

	/** N&acute;mero m&aacute;ximo de documentos que pueden ir en un lote de firma. */
	private int batchMaxDocuments = 0;

	/** Configuraci&oacute;n de proveedores habilitados. */
	private ProviderElement[] providers = null;

	/**
	 * Obtiene el taman&tilde;o m&aacute;ximo en bytes de una petici&oacute;n al servicio.
	 * @return Taman&tilde;o m&aacute;ximo en bytes  de una petici&oacute;n al servicio.
	 */
	public long getRequestMaxSize() {
		return this.requestMaxSize;
	}

	/**
	 * Establece el taman&tilde;o m&aacute;ximo en bytes  de una petici&oacute;n al servicio.
	 * @param requestMaxSize Taman&tilde;o m&aacute;ximo en bytes  de una petici&oacute;n al servicio.
	 */
	public void setRequestMaxSize(final long requestMaxSize) {
		this.requestMaxSize = requestMaxSize;
	}

	/**
	 * Obtiene el taman&tilde;o m&aacute;ximo en bytes  de cada par&aacute;metro enviado al servicio.
	 * @return Taman&tilde;o m&aacute;ximo en bytes  de cada par&aacute;metro enviado al servicio.
	 */
	public int getParamsMaxSize() {
		return this.paramsMaxSize;
	}

	/**
	 * Establece el taman&tilde;o m&aacute;ximo en bytes  de cada par&aacute;metro enviado al servicio.
	 * @param paramsMaxSize Taman&tilde;o m&aacute;ximo en bytes  de cada par&aacute;metro enviado al servicio.
	 */
	public void setParamsMaxSize(final int paramsMaxSize) {
		this.paramsMaxSize = paramsMaxSize;
	}

	/**
	 * Obtiene el n&acute;mero m&aacute;ximo de documentos que pueden ir en un lote de firma.
	 * @return N&acute;mero m&aacute;ximo de documentos que pueden ir en un lote de firma.
	 */
	public int getBatchMaxDocuments() {
		return this.batchMaxDocuments;
	}

	/**
	 * Establece n&acute;mero m&aacute;ximo de documentos que pueden ir en un lote de firma.
	 * @param batchMaxDocuments N&acute;mero m&aacute;ximo de documentos que pueden ir en un lote de firma.
	 */
	public void setBatchMaxDocuments(final int batchMaxDocuments) {
		this.batchMaxDocuments = batchMaxDocuments;
	}

	/**
	 * Obtiene el listado de proveedores habilitados.
	 * @return Listado de proveedores habilitados.
	 */
	public ProviderElement[] getProviders() {
		return this.providers;
	}

	/**
	 * Establece el listado de proveedores habilitados.
	 * @param providerElements Listado de proveedores habilitados.
	 */
	public void setProviders(final ProviderElement[] providers) {
		this.providers = providers;
	}

	@Override
	protected ApplicationOperationConfig clone() {

		final ApplicationOperationConfig object = new ApplicationOperationConfig();
		object.setBatchMaxDocuments(getBatchMaxDocuments());
		object.setParamsMaxSize(getParamsMaxSize());
		object.setRequestMaxSize(getRequestMaxSize());
		final ProviderElement[] provs = getProviders();
		if (provs != null) {
			object.setProviders(provs.clone());
		}

		return object;
	}
}
