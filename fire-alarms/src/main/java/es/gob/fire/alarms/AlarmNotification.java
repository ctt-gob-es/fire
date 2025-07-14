package es.gob.fire.alarms;

/**
 * Clase que relaciona la informaci&oacute;n de una alarma 
 * con las notificaciones realizadas sobre la misma.
 */
public class AlarmNotification {

	private Alarm alarm;
	private AlarmLevel level;
	private int notifications;

	public AlarmNotification(final Alarm alarm, final AlarmLevel level) {
		this.alarm = alarm;
		this.level = level;
		this.notifications = 0;
	}
	
	/**
	 * Agrega una nueva notificaci&oacute;n a la alarma
	 */
	public void addNotification() {
		this.notifications++;
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

	public int getNotifications() {
		return this.notifications;
	}

	public void setNotifications(final int notifications) {
		this.notifications = notifications;
	}

	public void setAlarm(final Alarm alarm) {
		this.alarm = alarm;
	}

	public void setLevel(final AlarmLevel level) {
		this.level = level;
	}

}
