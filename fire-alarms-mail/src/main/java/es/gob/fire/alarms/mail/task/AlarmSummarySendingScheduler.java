package es.gob.fire.alarms.mail.task;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import es.gob.fire.alarms.mail.MailAlarmNotifier;

/**
 * Programador de tarea de env&iacute;o de correo electr&oacute;nico
 */
public class AlarmSummarySendingScheduler {

    private final ScheduledExecutorService scheduler;

    public static AlarmSummarySendingScheduler instance = null;

    /**
     * Devuelve una instancia del programador asegur&aacute;ndose de detener cualquier
     * instancia anterior.
     * @return Programador.
     */
    public static AlarmSummarySendingScheduler getInstance() {
    	if (instance == null) {
    		instance = new AlarmSummarySendingScheduler();
    	}
    	return instance;
	}

    private AlarmSummarySendingScheduler() {
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * Programa la tarea que se ejecutara una &uacute;nica vez pasado el tiempo indicado en el constructor.
     * @param mins N&uacute;mero de minutos que deben transcurrir entre ejecuciones de la tarea.
     */
    public void scheduleTask(final long delay, final MailAlarmNotifier notifier) {
        final AlarmSummarySendingTask task = new AlarmSummarySendingTask(notifier);

        this.scheduler.schedule(task, delay, TimeUnit.MILLISECONDS);
    }

    /**
     * Detiene el programador.
     */
    public void stop() {
    	try {
    		this.scheduler.shutdownNow();
    	}
    	catch (final Exception e) {
    		Logger.getLogger(AlarmSummarySendingScheduler.class.getName()).warning("No se pudo finalizar la tarea de envio de alarmas por correo: " + e); //$NON-NLS-1$
		}
    }

}
