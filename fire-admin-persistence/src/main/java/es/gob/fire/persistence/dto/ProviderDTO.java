package es.gob.fire.persistence.dto;

public class ProviderDTO {
	/**
	 * Attribute that represents the value of the idProvider attribute.
	 */
	private String idProvider;

	/**
	 * Attribute that represents the value of the name attribute.
	 */
	private String name;

	/**
	 * Attribute that represents the value of the mandatory attribute.
	 */
	private Boolean mandatory;

	/**
	 * Attribute that represents the value of the enabled attribute.
	 */
	private Boolean enabled;

	/**
	 * Attribute that represents the value of the order attribute.
	 */
	private Long orderIndex;

	public String getIdProvider() {
		return this.idProvider;
	}

	public void setIdProvider(final String idProvider) {
		this.idProvider = idProvider;
	}

	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public Boolean getMandatory() {
		return this.mandatory;
	}

	public void setMandatory(final Boolean mandatory) {
		this.mandatory = mandatory;
	}

	public Boolean getEnabled() {
		return this.enabled;
	}

	public void setEnabled(final Boolean enabled) {
		this.enabled = enabled;
	}

	public Long getOrderIndex() {
		return this.orderIndex;
	}

	public void setOrderIndex(final Long orderIndex) {
		this.orderIndex = orderIndex;
	}
}
