package es.gob.fire.persistence.dto;

import java.util.List;

public class GeneralConfigDTO {
	private Long maxSizeDoc;
	private Long maxSizePetition;
	private Long maxAmountDocs;
	
	private List<ProviderDTO> providers;

	public Long getMaxSizeDoc() {
		return maxSizeDoc;
	}

	public void setMaxSizeDoc(Long maxSizeDoc) {
		this.maxSizeDoc = maxSizeDoc;
	}

	public Long getMaxSizePetition() {
		return maxSizePetition;
	}

	public void setMaxSizePetition(Long maxSizePetition) {
		this.maxSizePetition = maxSizePetition;
	}

	public Long getMaxAmountDocs() {
		return maxAmountDocs;
	}

	public void setMaxAmountDocs(Long maxAmountDocs) {
		this.maxAmountDocs = maxAmountDocs;
	}

	public List<ProviderDTO> getProviders() {
		return providers;
	}

	public void setProviders(List<ProviderDTO> providers) {
		this.providers = providers;
	}
}
