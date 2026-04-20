package es.gob.fire.persistence.dto;

import java.util.Objects;

public class OrganizationDTO {
	private String organization;

	private String dir3Code;

	public OrganizationDTO(final String organization, final String dir3Code) {
		super();
		this.organization = organization;
		this.dir3Code = dir3Code;
	}

	public String getOrganization() {
		return this.organization;
	}

	public void setOrganization(final String organization) {
		this.organization = organization;
	}

	public String getDir3Code() {
		return this.dir3Code;
	}

	public void setDir3Code(final String dir3Code) {
		this.dir3Code = dir3Code;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final OrganizationDTO that = (OrganizationDTO) o;
		return Objects.equals(this.organization, that.organization) &&
		       Objects.equals(this.dir3Code, that.dir3Code);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.organization, this.dir3Code);
	}
}
