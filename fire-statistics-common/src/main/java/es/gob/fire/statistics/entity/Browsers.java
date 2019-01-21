package es.gob.fire.statistics.entity;

/**
 * Navegadores web.
 */
enum Browsers {
	/** Navegador Microsoft Internet Explorer. */
	INTERNET_EXPLORER(1),
	/** Navegador Microsoft Edge. */
	EDGE(2),
	/** Navegador Mozilla Firefox. */
	FIREFOX(3),
	/** Navegador Google Chrome. */
	CHROME(4),
	/** Navegador Apple Safari. */
	SAFARI(5),
	/** Navegador Opera. */
	OPERA(6),
	/** Cualquier otro navegador. */
	OTHER(99);

	private int id;

	private Browsers(final int id) {
		this.id = id;
	}

	public final int getId() {
		return this.id;
	}

	@Override
	public String toString() {
		return Integer.toString(this.id);
	}
}
