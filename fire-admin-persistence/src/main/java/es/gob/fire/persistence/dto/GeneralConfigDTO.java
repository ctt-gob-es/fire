package es.gob.fire.persistence.dto;

import java.util.List;

public class GeneralConfigDTO {
	private Long maxSizeDoc;
	private Long maxSizePetition;
	private Long maxAmountDocs;

	private Long maxEntitiesBeforeGrouping;

	private List<ProviderDTO> providers;

	public Long getMaxSizeDoc() {
		return this.maxSizeDoc;
	}

	public void setMaxSizeDoc(final Long maxSizeDoc) {
		this.maxSizeDoc = maxSizeDoc;
	}

	public Long getMaxSizePetition() {
		return this.maxSizePetition;
	}

	public void setMaxSizePetition(final Long maxSizePetition) {
		this.maxSizePetition = maxSizePetition;
	}

	public Long getMaxAmountDocs() {
		return this.maxAmountDocs;
	}

	public void setMaxAmountDocs(final Long maxAmountDocs) {
		this.maxAmountDocs = maxAmountDocs;
	}

	public List<ProviderDTO> getProviders() {
		return this.providers;
	}

	public void setProviders(final List<ProviderDTO> providers) {
		this.providers = providers;
	}

	public Long getMaxEntitiesBeforeGrouping() {
		return this.maxEntitiesBeforeGrouping;
	}

	public void setMaxEntitiesBeforeGrouping(final Long maxEntitiesBeforeGrouping) {
		this.maxEntitiesBeforeGrouping = maxEntitiesBeforeGrouping;
	}
}
