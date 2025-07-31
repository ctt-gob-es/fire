package es.gob.fire.alarms.mail.task;

import java.util.Date;

import es.gob.fire.alarms.mail.MailAlarmNotifier;

/**
 * Tarea que comprueba si se han cumplido las condiciones para enviar el resumen por e-mail
 */
public class CheckAlarmsTask implements Runnable {

	@Override
	public void run() {
		if (MailAlarmNotifier.initialized && 
			(MailAlarmNotifier.criticalNotifyAttempts >= MailAlarmNotifier.criticalNotifyLimits ||
			MailAlarmNotifier.errorNotifyAttempts >= MailAlarmNotifier.errorNotifyLimits ||
			MailAlarmNotifier.warningNotifyAttempts >= MailAlarmNotifier.warningNotifyLimits ||
			MailAlarmNotifier.infoNotifyAttempts >= MailAlarmNotifier.infoNotifyLimits)) {
			
			MailAlarmNotifier.sendSummary();
		} else {
			MailAlarmNotifier.lastDateChecked = new Date();
		}
	}

}
