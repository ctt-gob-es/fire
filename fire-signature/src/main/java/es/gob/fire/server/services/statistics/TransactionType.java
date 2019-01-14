package es.gob.fire.server.services.statistics;

import es.gob.fire.server.services.FIReServiceOperation;

/** Tipo de transacci&oacute;n solicitada al componente central. */
public enum TransactionType {

	/** Transacci&oacute;n de firma. */
	SIGN(1),
	/** Transacci&oacute;n de firma de lote. */
	BATCH(2),
	/** Cualquier otra transacci&oacute;n. */
	OTHER(99);

	private int id;

	private TransactionType(final int id) {
		this.id = id;
	}

	public int getId() {
		return this.id;
	}

	public static TransactionType getOperation(final String idString) {
		final int result = Integer.parseInt(idString);
		for (final TransactionType value : values()) {
			if (value.id == result) {
				return value;
			}
		}
		return OTHER;
	}

	public static TransactionType valueOf(final FIReServiceOperation op) {
		switch (op) {
			case SIGN:
			case RECOVER_SIGN:
			case RECOVER_SIGN_RESULT:
				return SIGN;

			case CREATE_BATCH:
			case ADD_DOCUMENT_TO_BATCH:
			case SIGN_BATCH:
			case RECOVER_BATCH:
			case RECOVER_BATCH_STATE:
			case RECOVER_SIGN_BATCH:
				return BATCH;

			case RECOVER_ERROR:
				return OTHER;

			default:
				return OTHER;

		}
	}

	@Override
	public String toString() {
		return Integer.toString(this.id);
	}
}
