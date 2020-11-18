package es.gob.fire.persistence.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import es.gob.fire.commons.utils.NumberConstants;
import es.gob.fire.commons.utils.UtilsStringChar;
import es.gob.fire.persistence.dto.validation.CheckItFirst;
import es.gob.fire.persistence.dto.validation.ThenCheckIt;

public class ApplicationDTO {
	
	/**
	 * Attribute that represents the value of the primary key as a hidden input in the form. 
	 */
	private Long appId;
	
	/**
	 * Attribute that represents the value of the input name of the application in the form. 
	 */
	@NotNull(groups=CheckItFirst.class, message="{form.valid.user.name.notempty}")
    @Size(min=1, max=NumberConstants.NUM15, groups=ThenCheckIt.class)
    private String name = UtilsStringChar.EMPTY_STRING;

	/**
	 * Gets the value of the attribute {@link #appId}.
	 * @return the value of the attribute {@link #appId}.
	 */
	public Long getAppId() {
		return appId;
	}

	
	/**
	 * Sets the value of the attribute {@link #appId}.
	 * @param appIdParam The value for the attribute {@link #appId}.
	 */
	public void setAppId(Long appIdParam) {
		this.appId = appIdParam;
	}

}
