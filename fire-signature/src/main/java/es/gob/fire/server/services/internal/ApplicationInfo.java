package es.gob.fire.server.services.internal;

public class ApplicationInfo {

	private final String id;

	private final String name;

	public ApplicationInfo(final String id, final String name) {
		this.id = id;
		this.name = name;
	}

	public String getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}
}
