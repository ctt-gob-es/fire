package es.gob.fire.alarms;

/**
 * Alarmas que pueden emitirse.
 */
public enum Alarm {
	/** No se ha encontrado o no se ha podido cargar un fichero de configuraci&oacute;n. */
	RESOURCE_CONFIG("000", AlarmLevel.CRITICAL, "No se encuentra un fichero de configuracion: %1s"), //$NON-NLS-1$ //$NON-NLS-2$
	/** No se ha podido cargar o se ha encontrado un defecto de implementaci&oacute;n en una biblioteca externa
	 * agregada al componente central: conector para el acceso a un proveedor de firma en la nube, gestor de documentos,
	 * gestor de compartici&oacute;n de sesiones, conector con una plataforma de validaci&oacute;n de firmas... */
	LIBRARY_NOT_FOUND("001", AlarmLevel.CRITICAL, "Biblioteca o dependencia no encontrada: %1s"), //$NON-NLS-1$ //$NON-NLS-2$
	/** No se ha podido conectar con la base de datos. */
	CONNECTION_DB("002", AlarmLevel.CRITICAL, "No se puede conectar con la base de datos"), //$NON-NLS-1$ //$NON-NLS-2$
	/** No se ha podido conectar con la plataforma de validacion de firmas. */
	CONNECTION_VALIDATION_PLATFORM("003", AlarmLevel.ERROR, "No se puede conectar con la plataforma de validacion de firmas"), //$NON-NLS-1$ //$NON-NLS-2$
	/** No se ha podido conectar con un proveedor de firma en la nube. */
	CONNECTION_SIGNATURE_PROVIDER("004", AlarmLevel.ERROR, "No se puede conectar con un proveedor de firma en la nube: %1s"), //$NON-NLS-1$ //$NON-NLS-2$
	/** No se ha podido conectar con el gestor de documentos. */
	CONNECTION_DOCUMENT_MANAGER("005", AlarmLevel.ERROR, "No se puede conectar con un gestor de documentos"); //$NON-NLS-1$ //$NON-NLS-2$

	private String eventCode;
	private AlarmLevel defaultLevel;
	private String description;

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
