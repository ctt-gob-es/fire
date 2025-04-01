package es.gob.fire.server.services.internal;

public class ApplicationInfo {

	private final String id;

	private final String name;
	
	private final String dir3Code;

	public ApplicationInfo(final String id, final String name, final String dir3Code) {
		this.id = id;
		this.name = name;
		this.dir3Code = dir3Code;
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
}
