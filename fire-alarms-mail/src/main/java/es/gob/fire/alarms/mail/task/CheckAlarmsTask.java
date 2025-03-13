package es.gob.fire.alarms.mail.task;

import java.util.Date;

import es.gob.fire.alarms.mail.MailAlarmNotifier;

/**
 * Tarea que comprueba si se han cumplido las condiciones para enviar el resumen por e-mail
 */
public class CheckAlarmsTask implements Runnable {

	@Override
	public void run() {
		if (MailAlarmNotifier.initialized && MailAlarmNotifier.notifyAttemps >= MailAlarmNotifier.notifyLimits) {
			MailAlarmNotifier.sendSummary();
		} else {
			MailAlarmNotifier.lastDateChecked = new Date();
		}
	}

}
