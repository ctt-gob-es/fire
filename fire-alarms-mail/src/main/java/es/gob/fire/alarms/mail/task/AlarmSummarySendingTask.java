package es.gob.fire.alarms.mail.task;

import es.gob.fire.alarms.mail.MailAlarmNotifier;

/**
 * Tarea que comprueba si se han cumplido las condiciones para enviar el resumen por e-mail
 */
public class AlarmSummarySendingTask implements Runnable {

	private final MailAlarmNotifier notifier;

	public AlarmSummarySendingTask(final MailAlarmNotifier notifier) {
		this.notifier = notifier;
	}

	@Override
	public void run() {
		if (this.notifier.isInitialized()) {
			this.notifier.sendSummary();
		}
	}

}
