package es.gob.fire.server.services.internal.sessions;

import java.io.IOException;

import es.gob.fire.server.services.internal.LogTransactionFormatter;

/**
 * DAO para la gesti&oacute;n de los documentos temporales procesados como
 * parte de las operaciones del
 */
public interface TempDocumentsDAO {

	/**
	 * Comprueba la disponibilidad de un documento.
	 * @param id Identificador del documento.
	 * @return {@code true} si existe el documento, {@code false} en caso contrario.
	 * @throws IOException Cuando ocurre un error que hace fallar la operaci&oacute;n.
	 */
	boolean existDocument(String id) throws IOException;

	/**
	 * Almacena un documento.
	 * @param id Identificador del documento o {@code null} si el propio DAO debe
	 * asign&aacute;rselo.
	 * @param data Contenido del documento.
	 * @param newDocument {@code true} si el documento no exist&iacute;a previamente,
	 * {@code false} si s&iacute; exist&iacute;a.
	 * @param logF Formateador de logs.
	 * @return Identificador del documento almacenado.
	 * @throws IOException Cuando ocurre un error que hace fallar la operaci&oacute;n.
	 */
	String storeDocument(String id, byte[] data, boolean newDocument, LogTransactionFormatter logF) throws IOException;

	/**
	 * Recupera un documento.
	 * @param id Identificador del documento.
	 * @return Contenido del documento.
	 * @throws IOException Cuando ocurre un error que hace fallar la operaci&oacute;n.
	 */
	byte[] retrieveDocument(String id) throws IOException;

	/**
	 * Elimina un documento si existe.
	 * @param id Identificador del documento.
	 * @throws IOException Cuando ocurre un error que hace fallar la operaci&oacute;n.
	 */
	void deleteDocument(String id) throws IOException;

	/**
	 * Recupera el contenido de un documento y luego lo elimina.
	 * @param id Identificador del documento.
	 * @return Contenido del documento.
	 * @throws IOException Cuando ocurre un error que hace fallar la operaci&oacute;n.
	 */
	byte[] retrieveAndDeleteDocument(String id) throws IOException;

	/**
	 * Elimina todos los documentos caducados.
	 * @param expirationTime &Uacute;ltimo momento del tiempo en el que se debi&oacute;
	 * modificar el documento para no considerarse caducado.
	 * @throws IOException Cuando ocurre un error que hace fallar la operaci&oacute;n.
	 */
	void deleteExpiredDocuments(long expirationTime) throws IOException;
}
