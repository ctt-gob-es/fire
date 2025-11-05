package es.gob.fire.alarms.mail;

import java.util.Date;

import es.gob.fire.alarms.Alarm;
import es.gob.fire.alarms.AlarmLevel;

/**
 * Clase que relaciona la informaci&oacute;n de una alarma
 * con las notificaciones realizadas sobre la misma.
 */
public class AlarmNotification {

	private Alarm alarm;
	private AlarmLevel level;
	private final long time;

	public AlarmNotification(final Alarm alarm, final AlarmLevel level) {
		this.alarm = alarm;
		this.level = level;
		this.time = new Date().getTime();
	}

	/**
	 * Informaci&oacute;n de alarma.
	 * @return Alarma.
	 */
	public Alarm getAlarm() {
		return this.alarm;
	}

	/**
	 * Obtiene el nivel de error asociado por defecto al tipo de error.
	 * @return Nivel de error asociado por defecto.
	 */
	public AlarmLevel getLevel() {
		return this.level;
	}

	/**
	 * Momento del tiempo en milisegundos en el que se creo la notificaci&oacute;n.
	 * @return Milisegundos correspondientes al momento en le que se creo la notificaci&oacute;n.
	 */
	public long getTime() {
		return this.time;
	}

	public void setAlarm(final Alarm alarm) {
		this.alarm = alarm;
	}

	public void setLevel(final AlarmLevel level) {
		this.level = level;
	}

}
