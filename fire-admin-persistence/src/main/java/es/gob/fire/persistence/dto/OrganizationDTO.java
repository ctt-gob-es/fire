package es.gob.fire.persistence.dto;

import java.util.Objects;

public class OrganizationDTO {
	private String organization;
	
	private String dir3Code;

	public OrganizationDTO(String organization, String dir3Code) {
		super();
		this.organization = organization;
		this.dir3Code = dir3Code;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public String getDir3Code() {
		return dir3Code;
	}

	public void setDir3Code(String dir3Code) {
		this.dir3Code = dir3Code;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		OrganizationDTO that = (OrganizationDTO) o;
		return Objects.equals(organization, that.organization) &&
		       Objects.equals(dir3Code, that.dir3Code);
	}

	@Override
	public int hashCode() {
		return Objects.hash(organization, dir3Code);
	}
}
