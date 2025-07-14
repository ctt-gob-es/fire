package es.gob.fire.server.services.internal;

public class ApplicationInfo {

	private final String id;

	private final String name;
	
	private final String dir3Code;
	
	private final String organization;

	public ApplicationInfo(final String id, final String name, final String dir3Code, final String organization) {
		this.id = id;
		this.name = name;
		this.dir3Code = dir3Code;
		this.organization = organization;
	}

	public String getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}
	
	public String getDir3Code() {
		return this.dir3Code;
	}
	
	public String getOrganization() {
		return this.organization;
	}
}
