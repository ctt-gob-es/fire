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
	
	/**
	 * Attribute that defines the number of days of period communication.
	 */
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

	/**
	 * Gets the value of the attribute {@link #periodCommunicationEdit}.
	 * @return the value of the attribute {@link #periodCommunicationEdit}.
	 */
	public Long getPeriodCommunicationEdit() {
		return periodCommunicationEdit;
	}

	/**
	 * Sets the value of the attribute {@link #periodCommunicationEdit}.
	 * @param initDayStringEditParam The value for the attribute {@link #periodCommunicationEdit}.
	 */
	public void setPeriodCommunicationEdit(Long periodCommunicationEdit) {
		this.periodCommunicationEdit = periodCommunicationEdit;
	}
	
}
