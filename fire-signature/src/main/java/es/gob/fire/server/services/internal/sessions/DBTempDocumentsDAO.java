package es.gob.fire.server.services.internal.sessions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import es.gob.fire.server.services.internal.LogTransactionFormatter;
import es.gob.fire.signature.DbManager;

/**
 * DAO para la gesti&oacute;n de datos temporales a traves de base de datos.
 */
public class DBTempDocumentsDAO implements TempDocumentsDAO {

	private static final String DB_STATEMENT_CHECK_DOCUMENT = "SELECT id FROM tb_comp_documentos WHERE id = ?"; //$NON-NLS-1$

	private static final String DB_STATEMENT_UPDATE_DOCUMENT = "UPDATE tb_comp_documentos SET datos=?, f_modificacion=? WHERE id = ?"; //$NON-NLS-1$

	private static final String DB_STATEMENT_INSERT_DOCUMENT = "INSERT INTO tb_comp_documentos(id, datos, f_modificacion) VALUES (?, ?, ?)"; //$NON-NLS-1$

	private static final String DB_STATEMENT_RECOVER_DOCUMENT = "SELECT datos, f_modificacion FROM tb_comp_documentos WHERE id = ?"; //$NON-NLS-1$

	private static final String DB_STATEMENT_REMOVE_DOCUMENT = "DELETE FROM tb_comp_documentos WHERE id = ?"; //$NON-NLS-1$

	private static final String DB_STATEMENT_REMOVE_EXPIRED_DOCUMENTS = "DELETE FROM tb_comp_documentos WHERE f_modificacion < ?"; //$NON-NLS-1$

	private static final Logger LOGGER = Logger.getLogger(DBTempDocumentsDAO.class.getName());

	private static final int MAX_ID_SIZE = 80;


	@Override
	public boolean existDocument(final String id) throws IOException {

		final String docId = cleanDataId(id);

		boolean exists;
		try (Connection conn = DbManager.getConnection();
				PreparedStatement st = conn.prepareStatement(DB_STATEMENT_CHECK_DOCUMENT)) {
			st.setString(1, docId);
			try (ResultSet rs = st.executeQuery()) {
				exists = rs.next();
			}
		}
		catch (final Exception e) {
			throw new IOException("Error al buscar el documento con ID: " + docId, e); //$NON-NLS-1$
		}
		return exists;
	}

	@Override
	public String storeDocument(final String id, final byte[] data, final boolean newDocument, final LogTransactionFormatter formt) throws IOException {

		// Insertamos o actualizamos los datos segun corresponda
		String docId = cleanDataId(id);
		if (newDocument || docId == null) {
			// Si no se nos ha pasado un identificador de documento, generamos uno que no exista
			if (docId == null) {
				do {
					docId = UUID.randomUUID().toString();
				} while (existDocument(docId));
			}

			try (Connection conn = DbManager.getConnection(true);
					PreparedStatement st = conn.prepareStatement(DB_STATEMENT_INSERT_DOCUMENT)) {
				st.setString(1, docId);
				st.setBlob(2, new ByteArrayInputStream(data));
				st.setLong(3, new Date().getTime());
				if (st.executeUpdate() < 1) {
					LOGGER.log(Level.WARNING, formt.f("No se pudo insertar en base de datos el documento temporal con ID: " + docId)); //$NON-NLS-1$
				}
			}
			catch (final Exception e) {
				throw new IOException("Error al insertar en base de datos el documento con ID: " + docId, e); //$NON-NLS-1$
			}
		}
		else {
			try (Connection conn = DbManager.getConnection(true);
					PreparedStatement st = conn.prepareStatement(DB_STATEMENT_UPDATE_DOCUMENT)) {
				st.setBlob(1, new ByteArrayInputStream(data));
				st.setLong(2, new Date().getTime());
				st.setString(3, docId);
				if (st.executeUpdate() < 1) {
					LOGGER.log(Level.WARNING, formt.f("No se pudo actualizar en base de datos el documento temporal con ID: " + docId)); //$NON-NLS-1$
				}
			}
			catch (final Exception e) {
				throw new IOException("Error al actualizar en base de datos el documento con ID: " + docId, e); //$NON-NLS-1$
			}
		}
		return docId;
	}

	@Override
	public byte[] retrieveDocument(final String id) throws IOException {

		final String docId = cleanDataId(id);

		byte[] data = null;
		try (Connection conn = DbManager.getConnection();
				PreparedStatement st = conn.prepareStatement(DB_STATEMENT_RECOVER_DOCUMENT)) {
			st.setString(1, docId);
			Blob dataBlob = null;
			try (final ResultSet dbResult = st.executeQuery()) {
				if (dbResult.next()) {
					dataBlob = dbResult.getBlob(1);
				}
				// Cargamos los datos en memoria
				if (dataBlob != null) {
					try (InputStream dataIs = dataBlob.getBinaryStream()) {
						data = readInputStream(dataIs);
					}
					catch (final Exception e) {
						throw new IOException("Error al cargar el documento con ID: " + docId, e); //$NON-NLS-1$
					}
				}
				else {
					throw new IOException("No se encontro el documento con ID: " + docId); //$NON-NLS-1$
				}
			}
		}
		catch (final Exception e) {
			throw new IOException("Error al recuperar el documento con ID: " + docId, e); //$NON-NLS-1$
		}

		return data;
	}

	@Override
	public void deleteDocument(final String id) throws IOException {

		final String docId = cleanDataId(id);

		try (Connection conn = DbManager.getConnection(true);
				PreparedStatement st = conn.prepareStatement(DB_STATEMENT_REMOVE_DOCUMENT)) {
			st.setString(1, docId);
			st.execute();
		}
		catch (final Exception e) {
			LOGGER.warning("Error al eliminar el documento con ID: " + docId); //$NON-NLS-1$
		}
	}

	@Override
	public byte[] retrieveAndDeleteDocument(final String id) throws IOException {

		final byte[] data = retrieveDocument(id);
		deleteDocument(id);
		return data;
	}

	@Override
	public void deleteExpiredDocuments(final long expirationTime) throws IOException {

		final long maxTime = new Date().getTime() - expirationTime;

		try (Connection conn = DbManager.getConnection(true);
				PreparedStatement st = conn.prepareStatement(DB_STATEMENT_REMOVE_EXPIRED_DOCUMENTS)) {
			st.setLong(1, maxTime);
			st.execute();
		}
		catch (final Exception e) {
			LOGGER.warning("Error durante la limpieza de documentos caducados: " + e); //$NON-NLS-1$
		}
	}

	/**
	 * Lee los datos del flujo de datos de entrada.
	 * @param is Flujo de datos de entrada.
	 * @return Datos le&iacute;dos.
	 * @throws IOException Cuando ocurre un error de lectura.
	 */
	private static byte[] readInputStream(final InputStream is) throws IOException {
		int n = 0;
		final byte[] buffer = new byte[4096];
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		while ((n = is.read(buffer)) > 0) {
			baos.write(buffer,  0,  n);
		}
		return baos.toByteArray();
	}


    /**
     * Limpia un nombre de fichero para asegurar que no haya caracteres con los que no puedan
     * guardarse los ficheros en disco y lo recorta a un tama&ntilde;o m&aacute;ximo.
     * @param filename Nombre de fichero.
     * @return Nombre de fichero limpio.
     */
    private static String cleanDataId(final String id) {

    	// Componemos un nombre de hasta 64 caracters con caracteres validos para nombres de fichero
    	final StringBuilder builder = new StringBuilder();
    	for (final char c : id.toCharArray()) {
    		if (Character.isLetterOrDigit(c) || c == '.' || c == '-') {
    			builder.append(c);
    		} else {
    			builder.append('_');
    		}
    		if (builder.length() >= MAX_ID_SIZE) {
    			break;
    		}
    	}
    	return builder.toString();
    }
}
