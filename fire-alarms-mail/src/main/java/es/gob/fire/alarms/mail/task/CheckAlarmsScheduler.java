package es.gob.fire.alarms.mail.task;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Programador de tarea de env&iacute;o de correo electr&oacute;nico
 */
public class CheckAlarmsScheduler {
	
    private final ScheduledExecutorService scheduler;
    
    private final int delayMins;

    public CheckAlarmsScheduler(final int mins) {
    	this.delayMins = mins;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    public void scheduleTask() {
        final CheckAlarmsTask task = new CheckAlarmsTask();

        this.scheduler.scheduleAtFixedRate(task, 0, this.delayMins, TimeUnit.MINUTES);
    }

    public void stop() {
        this.scheduler.shutdown();
    }

}
