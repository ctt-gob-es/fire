package es.gob.fire.server.services.statistics;

/** Operaci&oacute;n solicitada al componente central. */
public enum Operations {

	/** Operaci&oacute;n de firma. */
	SIGN(1),
	/** Operaci&oacute;n de firma de lote. */
	BATCH(2),
	/** Cualquier otra operaci&oacute;n. */
	OTHER(99);

	private int id;

	private Operations(final int id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return Integer.toString(this.id);
	}
}
