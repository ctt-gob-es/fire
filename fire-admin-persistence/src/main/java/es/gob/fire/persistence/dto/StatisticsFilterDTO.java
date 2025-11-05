package es.gob.fire.persistence.dto;

import java.util.List;

public class StatisticsFilterDTO {
	private String query;
	
	private String monthDate;
	
	private String endMonthDate;
	
	private Boolean filterByApplications;
	
	private Boolean filterByOrganizations;
	
	private String applications;
	
    private String organizations;
	
	public StatisticsFilterDTO() {
    }

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getMonthDate() {
		return monthDate;
	}

	public void setMonthDate(String monthDate) {
		this.monthDate = monthDate;
	}

	public String getEndMonthDate() {
		return endMonthDate;
	}

	public void setEndMonthDate(String endMonthDate) {
		this.endMonthDate = endMonthDate;
	}

	public String getApplications() {
		return applications;
	}

	public void setApplications(String applications) {
		this.applications = applications;
	}

	public String getOrganizations() {
		return organizations;
	}

	public void setOrganizations(String organizations) {
		this.organizations = organizations;
	}

	public Boolean getFilterByApplications() {
		return filterByApplications;
	}

	public void setFilterByApplications(Boolean filterByApplications) {
		this.filterByApplications = filterByApplications;
	}

	public Boolean getFilterByOrganizations() {
		return filterByOrganizations;
	}

	public void setFilterByOrganizations(Boolean filterByOrganizations) {
		this.filterByOrganizations = filterByOrganizations;
	}
}
