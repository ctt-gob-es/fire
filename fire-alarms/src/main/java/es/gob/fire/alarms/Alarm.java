package es.gob.fire.alarms;

/**
 * Alarmas que pueden emitirse.
 */
public enum Alarm {
	/** No se ha encontrado o no se ha podido cargar un fichero de configuraci&oacute;n. */
	RESOURCE_NOT_FOUND("000", AlarmLevel.CRITICAL, AlarmInternalMessages.getString("Alarm.1")), //$NON-NLS-1$ //$NON-NLS-2$
	/** Se ha detectado un error en uno de los ficheros de configuraci&oacute;n. */
	RESOURCE_CONFIG("001", AlarmLevel.ERROR, AlarmInternalMessages.getString("Alarm.2")), //$NON-NLS-1$ //$NON-NLS-2$
	/** No se ha podido cargar o se ha encontrado un defecto de implementaci&oacute;n en una biblioteca externa
	 * agregada al componente central: conector para el acceso a un proveedor de firma en la nube, gestor de documentos,
	 * gestor de compartici&oacute;n de sesiones, conector con una plataforma de validaci&oacute;n de firmas... */
	LIBRARY_NOT_FOUND("002", AlarmLevel.CRITICAL, AlarmInternalMessages.getString("Alarm.3")), //$NON-NLS-1$ //$NON-NLS-2$
	/** No se ha podido conectar con la base de datos. */
	CONNECTION_DB("003", AlarmLevel.CRITICAL, AlarmInternalMessages.getString("Alarm.4")), //$NON-NLS-1$ //$NON-NLS-2$
	/** No se ha podido conectar con la plataforma de validacion de firmas. */
	CONNECTION_VALIDATION_PLATFORM("004", AlarmLevel.ERROR, AlarmInternalMessages.getString("Alarm.5")), //$NON-NLS-1$ //$NON-NLS-2$
	/** No se ha podido conectar con un proveedor de firma en la nube. */
	CONNECTION_SIGNATURE_PROVIDER("005", AlarmLevel.ERROR, AlarmInternalMessages.getString("Alarm.6")), //$NON-NLS-1$ //$NON-NLS-2$
	/** No se ha podido conectar con el gestor de documentos. */
	CONNECTION_DOCUMENT_MANAGER("006", AlarmLevel.ERROR, AlarmInternalMessages.getString("Alarm.7")); //$NON-NLS-1$ //$NON-NLS-2$

	private final String eventCode;
	private final AlarmLevel defaultLevel;
	private final String description;

	private Alarm(final String eventCode, final AlarmLevel defaultLevel, final String description) {
		this.eventCode = eventCode;
		this.defaultLevel = defaultLevel;
		this.description = description;
	}

	/**
	 * Obtiene el c&oacute;digo de un evento de alarma.
	 * @return C&oacute;digo de evento.
	 */
	public String getEventCode() {
		return this.eventCode;
	}

	/**
	 * Obtiene el nivel de error asociado por defecto al tipo de error.
	 * @return Nivel de error asociado por defecto.
	 */
	public AlarmLevel getDefaultLevel() {
		return this.defaultLevel;
	}

	/**
	 * Obtiene el texto descriptivo de la alarma.
	 * @return Texto descriptivo de la alarma.
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * Obtiene el texto descriptivo de la alarma con los par&aacute;metros
	 * insertados en la cadena de texto.
	 * @return Texto descriptivo de la alarma con los par&aacute;metros
	 * insertados.
	 */
	public String formatDescription(final Object...params) {
		try {
			return String.format(this.description, params);
		}
		catch (final Exception e) {
			return this.description;
		}
	}
}
