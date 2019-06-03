package es.gob.log.consumer.client;

/**
 * Operaciones soportadas por el servicio.
 */
public enum ServiceOperations {

	/** Echo. */
	ECHO(0),
	/** Solicitud de acceso. */
	REQUEST_LOGIN(1),
	/** Validacion de la solicitud de acceso. */
	VALIDATE_LOGIN(2),
	/** Obtener ficheros de log disponibles. */
	GET_LOG_FILES(3),
	/** Abrir fichero de log. */
	OPEN_FILE(4),
	/** Cerrar fichero de log. */
	CLOSE_FILE(5),
	/** Obtener final del fichero de log. */
	TAIL(6),
	/** Obtener lineas del fichero de log a partir de la posicion actual. */
	GET_MORE(7),
	/** Buscar texto en el log. */
	SEARCH_TEXT(8),
	/** Obtener fragmento del log. */
	FILTER(9),
	/** Descargar fichero de log. */
	DOWNLOAD(10),
	/** Cierra la conexi&oacute;n con el servidor de logs. */
	CLOSE_CONNECTION(11);

	private int id;

	private ServiceOperations(final int id) {
		this.id = id;
	}

	/**
	 * Recupera el identificador de la operaci&oacute;n.
	 * @return Identificador.
	 */
	public int getId() {
		return this.id;
	}

	/**
	 * Obtiene la operaci&oacute;n a partir de su identificador.
	 * @param opId Identificador de operaci&oacute;n.
	 * @return Operaci&oacute;n.
	 * @throws UnsupportedOperationException Cuando el c&oacute;digo de operaci&oacute;n no sea v&aacute;lido.
	 */
	public static ServiceOperations parseOperation(final int opId) {
		for (final ServiceOperations op : values()) {
			if (op.getId() == opId) {
				return op;
			}
		}
		throw new UnsupportedOperationException();
	}
}
