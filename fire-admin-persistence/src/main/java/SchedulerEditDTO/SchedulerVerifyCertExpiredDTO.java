package SchedulerEditDTO;

import es.gob.fire.persistence.dto.SchedulerEditDTO;

public class SchedulerVerifyCertExpiredDTO extends SchedulerEditDTO {
	
	public SchedulerVerifyCertExpiredDTO() {
		super();
	}
	
	/**
	 * Attribute that defines the number of days of notice.
	 */
	private Long dayAdviceNoticeEdit;
	
	private Long periodCommunicationEdit;
	
	/**
	 * Gets the value of the attribute {@link #dayAdviceNoticeEdit}.
	 * @return the value of the attribute {@link #dayAdviceNoticeEdit}.
	 */
	public Long getDayAdviceNoticeEdit() {
		return dayAdviceNoticeEdit;
	}

	/**
	 * Sets the value of the attribute {@link #dayAdviceNoticeEdit}.
	 * @param initDayStringEditParam The value for the attribute {@link #dayAdviceNoticeEdit}.
	 */
	public void setDayAdviceNoticeEdit(Long dayAdviceNoticeEdit) {
		this.dayAdviceNoticeEdit = dayAdviceNoticeEdit;
	}

	public Long getPeriodCommunicationEdit() {
		return periodCommunicationEdit;
	}

	public void setPeriodCommunicationEdit(Long periodCommunicationEdit) {
		this.periodCommunicationEdit = periodCommunicationEdit;
	}
	
}
