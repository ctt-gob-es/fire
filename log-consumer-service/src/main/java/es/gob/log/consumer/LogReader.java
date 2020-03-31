package es.gob.log.consumer;

import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

/**
 *	Interfaz que define las funciones a incorporar por cualquier clase para la carga
 *	de logs.
 */
public interface LogReader {

	/**
	 * Indica si se deben ignorar las l&iacute;neas vac&iacute;as.
	 * @param ignoreEmptyLines {@code true} para indicar que se ignoren las l&iacute;neas vac&iacute;s,
	 * {@code false} (comportamiento por defecto) para que tambi&eacute;n se carguen.
	 */
	void setIgnoreEmptyLines(final boolean ignoreEmptyLines);

	/**
	 * Obtiene el juego de caracteres usado para interpretar el log.
	 * @return Juego de caracteres.
	 */
	Charset getCharset();

	/** Inicia el proceso de carga del log.
	 * @throws IOException Cuando se produce un error durante la carga. */
	void load() throws IOException;

	/**
	 * Inicia el proceso de carga del log indicando una posici&oacute;n.
	 * @param position Posici&oacute;n del fichero a partir de la cual cargar.
	 * @throws IOException Cuando falla la carga.
	 */
	void load(final long position) throws IOException;

	/** Cierra y reinicia el proceso de carga del log a partir de una posici&oacute;n.
	 * @param position Posici&oacute;n del fichero a partir de la cual cargar.
	 * @throws IOException Cuando se produce un error durante la carga. */
	void reload(final long position) throws IOException;


	/**
	 * Devuelve la l&iacute;nea actualmente cargada. Si nunca se ha cargado a&uacute;n una,
	 * se devolver&aacute; {@code null}.
	 * @return L&iacute;nea actual.
	 */
	CharBuffer getCurrentLine();

	/**
	 * Lee una l&iacute;nea del log y la carga como la l&iacute;nea actual.
	 * @return Buffer con una linea de texto o {@code null} si no quedan m&aacute;s.
	 * @throws IOException Cuando se produce un error durante la carga.
	 */
	CharBuffer readLine() throws IOException;

	/**
	 * Reinicia la lectura del log.
	 * @throws IOException Cuando se produce un error durante la apertura del fichero.
	 */
	void rewind() throws IOException;

	/**
	 * Cierra el proceso de lectura del log.
	 * @throws IOException Cuando ocurre alg&uacute;n error durante el cierre.
	 */
	 void close() throws IOException;

	 /**
	  * Obtiene la posici&oacute;n actual dentro del log.
	  * @return Posici&oacute;n en el log.
	  */
	 long getFilePosition() ;


	 /**
	  * Obtiene la posici&oacute;n actual dentro del log.
	  * @return Posici&oacute;n en el log.
	  */
	 long getFileFragmentedPosition() ;

	 /**
	  * Indica si hemos llegado al final de la lectura del fichero.
	  * @return {@code true} si la posicion actual es el fin de fichero, {@code false} en
	  * caso contrario.
	  */
	 boolean isEndFile();

	 /**
	  * Establece el indicador de si hemos llegado al final de la lectura del fichero.
	  * @param endOfFile Se&ntilde;ala si hemos alacanzado el final de fichero.
	  */
	 void setEndFile(final boolean endOfFile);

	 /**
	  * Indica si hemos llegado al final de la lectura del fichero.
	  * @return {@code true} si la posicion actual es el fin de fichero, {@code false} en
	  * caso contrario.
	  */
	 boolean isReloaded();
}
