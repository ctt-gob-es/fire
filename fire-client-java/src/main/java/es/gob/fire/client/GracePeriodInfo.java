package es.gob.fire.client;

import java.util.Date;

public class GracePeriodInfo {

	private final String responseId;

	private final Date resolutionDate;

	public GracePeriodInfo(final String responseId, final Date resolutionDate) {
		this.responseId = responseId;
		this.resolutionDate = resolutionDate;
	}

	public String getResponseId() {
		return this.responseId;
	}

	public Date getResolutionDate() {
		return this.resolutionDate;
	}
}
