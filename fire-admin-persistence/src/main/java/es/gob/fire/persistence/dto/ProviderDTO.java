package es.gob.fire.persistence.dto;

public class ProviderDTO {
	/**
	 * Attribute that represents the value of the idProvider attribute.
	 */
	private Long idProvider;
	
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

	public Long getIdProvider() {
		return idProvider;
	}

	public void setIdProvider(Long idProvider) {
		this.idProvider = idProvider;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getMandatory() {
		return mandatory;
	}

	public void setMandatory(Boolean mandatory) {
		this.mandatory = mandatory;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public Long getOrderIndex() {
		return orderIndex;
	}

	public void setOrderIndex(Long orderIndex) {
		this.orderIndex = orderIndex;
	}
}
