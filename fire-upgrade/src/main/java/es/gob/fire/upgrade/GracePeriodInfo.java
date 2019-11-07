package es.gob.fire.upgrade;

import java.io.Serializable;
import java.util.Date;

public class GracePeriodInfo implements Serializable {

	/** Serial Id. */
	private static final long serialVersionUID = 1515328840855443778L;

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
