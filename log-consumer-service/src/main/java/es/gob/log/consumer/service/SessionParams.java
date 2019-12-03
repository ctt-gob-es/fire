package es.gob.log.consumer.service;

public class SessionParams {

	/** Almacena si el token de inicio de sesi&oacute;n del usuario. */
	static final String TOKEN = "tkn";  //$NON-NLS-1$

	/** Almacena si el salto para el cifrado del token de inicio de sesi&oacute;n. */
	static final String IV = "iv";  //$NON-NLS-1$

	/** Almacena si el usuario ha establecido y validado su sesi&oacute;n. */
	static final String LOGGED = "lgd";  //$NON-NLS-1$

	/** Almacena el objeto para la descarga fragmentada de un fichero. Objeto de tipo (LogDownload). */
	static final String DOWNLOAD_FILE = "Download";  //$NON-NLS-1$

	/** Almacena la posici&oacute;n del fichero que se desea descargar a partir del cual debe
	 * continuar la descarga. Objeto de tipo Long. */
	static final String DOWNLOAD_FILE_POS = "FileDownloadPos";  //$NON-NLS-1$

	/** Almacena el canal utilizado para la lectura del fichero que se est&aacute; descargando.
	 * Objeto de tipo SeekableByteChannel. */
	static final String DOWNLOAD_CHANNEL = "ChannelDownload";  //$NON-NLS-1$

	/** Almacena el objeto con la informaci&oacute;n de log del fichero abierto.
	 * Objeto de tipo LogInfo. */
	static final String LOG_INFO = "LogInfo";  //$NON-NLS-1$

	/** Almacena el objeto con el canal para la carga del fichero de log.
	 * Objeto de tipo AsynchronousFileChannel. */
	static final String FILE_CHANNEL = "Channel";  //$NON-NLS-1$

	/** Almacena el objeto con el lector para la lectura del canal del fichero de log.
	 * Objeto de tipo LogReader. */
	static final String FILE_READER = "Reader";  //$NON-NLS-1$

	/** Almacena la posici&oacute;n por la que va la lectura del fichero de log.
	 * Objeto de tipo Long. */
	static final String FILE_POSITION = "FilePosition";  //$NON-NLS-1$

	/** Almacena el tama&ntilde;o del fichero de log.
	 * Objeto de tipo Long. */
	static final String FILE_SIZE = "FileSize";  //$NON-NLS-1$
}
