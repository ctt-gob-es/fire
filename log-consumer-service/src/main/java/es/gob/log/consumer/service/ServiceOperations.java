package es.gob.log.consumer.service;

/**
 * Operaciones soportadas por el servicio.
 */
public enum ServiceOperations {

	/** Echo. */
	ECHO,				// Operacion: 0
	/** Solicitud de acceso. */
	REQUEST_LOGIN,		// Operacion: 1
	/** Validacion de la solicitud de acceso. */
	VALIDATE_LOGIN,		// Operacion: 2
	/** Obtener ficheros de log disponibles. */
	GET_LOG_FILES,		// Operacion: 3
	/** Abrir fichero de log. */
	OPEN_FILE,			// Operacion: 4
	/** Cerrar fichero de log. */
	CLOSE_FILE,			// Operacion: 5
	/** Obtener final del fichero de log. */
	TAIL,				// Operacion: 6
	/** Obtener lineas del fichero de log a partir de la posicion actual. */
	GET_MORE,			// Operacion: 7
	/** Buscar texto en el log. */
	SEARCH_TEXT,		// Operacion: 8
	/** Obtener fragmento del log. */
	FILTER,				// Operacion: 9
	/** Descargar fichero de log. */
	DOWNLOAD; 			// Operacion: 10

	/**
	 * Obtiene la operaci&oacute;n a partir de su identificador.
	 * @param opId Identificador de operaci&oacute;n.
	 * @return Operaci&oacute;n.
	 * @throws UnsupportedOperationException Cuando el c&oacute;digo de operaci&oacute;n no sea v&aacute;lido.
	 */
	public static ServiceOperations parseOperation(final int opId) {
		for (final ServiceOperations op : values()) {
			if (op.ordinal() == opId) {
				return op;
			}
		}
		throw new UnsupportedOperationException();
	}
}
