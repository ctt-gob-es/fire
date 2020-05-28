package es.gob.fire.persistence.dto;

import org.springframework.context.annotation.Configuration;

/**
 * <p>Class that store the information of the connected log consumer service.</p>
 * <b>Project:</b><p>Application for signing documents of @firma suite systems.</p>
 * @version 1.0, 17/05/2019.
 */
@Configuration
public class LogConsumerConnectionDTO {

	/**
	 * Attribute that represents the current log server consumer service.
	 */
	private Long serviceId;

	/**
	 * Attribute that represents the current log server consumer service.
	 */
	private String serviceName;

	/**
	 * Attribute that represents the current openned log server file.
	 */
	private String filename;

	/**
	 * Gets serviceId.
	 * @return serviceId
	 */
	public Long getServiceId() {
		return this.serviceId;
	}
	
	/**
	 * Sets the serviceId.
	 * @param serviceIdP to set serviceId
	 */
	public void setServiceId(final Long serviceIdP) {
		this.serviceId = serviceIdP;
	}

	/**
	 * gets serviceName.
	 * @return serviceName
	 */
	public String getServiceName() {
		return this.serviceName;
	}
	
	/**
	 * Sets the serviceName.
	 * @param serviceNameP to set serviceName
	 */
	public void setServiceName(final String serviceNameP) {
		this.serviceName = serviceNameP;
	}

	/**
	 * Gets the filename.
	 * @return filename
	 */
	public String getFilename() {
		return this.filename;
	}

	/**
	 * Sets the filename.
	 * @param filenameP to set filename
	 */
	public void setFilename(final String filenameP) {
		this.filename = filenameP;
	}
	
	/**
	 * Method that sets the server log info.
	 * @param id id server log
	 * @param name name server log
	 */
	public void setServerInfo(final Long id, final String name) {
		this.serviceId = id;
		this.serviceName = name;
	}
}
