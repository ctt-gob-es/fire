package es.gob.fire.server.services.internal;

/**
 * Estado de una aplicaci&oacute;n.
 */
public class ApplicationAccessInfo {

	private final String id;
	private final String name;
	private final boolean enabled;
	private final DigestInfo[] certDigests;

	/**
	 * Identifica el estado de una aplicaci&oacute;n.
	 * @param id Identificador de la aplicaci&oacute;n.
	 * @param name Nombre de la aplicaci&oacute;n.
	 * @param enabled Indica si est&aacute; habilitada ({@code true}) o no ({@code false}).
	 * @param certDigests Listado de huellas de certificado permitidas para el certificado.
	 */
	public ApplicationAccessInfo(final String id, final String name, final boolean enabled, final DigestInfo[] certDigests) {
		this.id = id;
		this.name = name;
		this.enabled = enabled;
		this.certDigests = certDigests;
	}

	/**
	 * Identificador de la aplicaci&oacute;n.
	 * @return Identificador de la aplicaci&oacute;n.
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * Nombre de la aplicaci&oacute;n.
	 * @return Nombre de la aplicaci&oacute;n.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Indica si la aplicaci&oacute;n est&aacute;a habilitada o no.
	 * @return {@code true} si la aplicaci&oacute;n est&aacute; habilitada,
	 * {@code false} en caso contrario.
	 */
	public boolean isEnabled() {
		return this.enabled;
	}

	/**
	 * Recupera el listado de huellas de los certificados dados de alta para el uso de la aplicaci&oacute;n.
	 * @return
	 */
	public DigestInfo[] getCertDigests() {
		return this.certDigests;
	}
}
