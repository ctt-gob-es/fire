package es.gob.fire.server.services.internal;

/**
 * Estado de una aplicaci&oacute;n.
 */
public class ApplicationAccessInfo {

	private final String id;
	private final String name;
	private final boolean enabled;
	private final DigestInfo[] certDigests;
	private final String dir3Code;
	private final String organization;

	/**
	 * Identifica el estado de una aplicaci&oacute;n.
	 * @param id Identificador de la aplicaci&oacute;n.
	 * @param name Nombre de la aplicaci&oacute;n.
	 * @param enabled Indica si est&aacute; habilitada ({@code true}) o no ({@code false}).
	 * @param certDigests Listado de huellas de certificado permitidas para el certificado.
	 * @param dir3Code C&oacute;digo DIR 3.
	 * @param organization Organizaci&oacute;n.
	 */
	public ApplicationAccessInfo(final String id, final String name, final boolean enabled, final DigestInfo[] certDigests, final String dir3Code, final String organization) {
		this.id = id;
		this.name = name;
		this.enabled = enabled;
		this.certDigests = certDigests;
		this.dir3Code = dir3Code;
		this.organization = organization;
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
	 * @return Listado de huellas de certificado permitidas.
	 */
	public DigestInfo[] getCertDigests() {
		return this.certDigests.clone();
	}
	
	/**
	 * C&oacute;digo DIR 3.
	 * @return C&oacute;digo DIR 3.
	 */
	public String getDir3Code() {
		return this.dir3Code;
	}
	
	/**
	 * Organizaci&oacute;n.
	 * @return Nombre de Organizaci&oacute;n.
	 */
	public String getOrganization() {
		return this.organization;
	}
}
