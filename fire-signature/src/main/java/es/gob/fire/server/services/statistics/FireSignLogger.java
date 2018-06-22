package es.gob.fire.server.services.statistics;

import es.gob.fire.services.FireLogger;

public class FireSignLogger {

	private FireLogger fireLogger ;

	private static FireSignLogger fireSignLogger;

	private static String LOGGER_NAME = "LOG"; //$NON-NLS-1$

	private FireSignLogger() {
		this.setFireLogger(new FireLogger(LOGGER_NAME));
	}

	public final static FireSignLogger getFireSignLogger() {
		if (fireSignLogger == null) {
			fireSignLogger =  new FireSignLogger();
		}
		return fireSignLogger;
	}

	public final FireLogger getFireLogger() {
		return this.fireLogger;
	}

	private final void setFireLogger(final FireLogger fireLogger) {
		this.fireLogger = fireLogger;
	}


}
