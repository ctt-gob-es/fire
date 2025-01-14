package es.gob.fire.server.services.internal.sessions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpSession;

import es.gob.fire.server.services.internal.FireSession;
import es.gob.fire.signature.ConfigManager;
import es.gob.fire.signature.DbManager;

/**
 * DAO para la gesti&oacute;n de sesiones en base de datos.
 */
public class DBSessionsDAO implements SessionsDAO, Serializable {

	/** Serial Id. */
	private static final long serialVersionUID = 2795967166266934274L;

	private static final Logger LOGGER = Logger.getLogger(DBSessionsDAO.class.getName());

	private static final String DB_STATEMENT_CHECK_SESSION = "SELECT id FROM tb_comp_sesiones WHERE id = ?"; //$NON-NLS-1$

	private static final String DB_STATEMENT_UPDATE_SESSION = "UPDATE tb_comp_sesiones SET sesion=?, f_modificacion=? WHERE id = ?"; //$NON-NLS-1$

	private static final String DB_STATEMENT_INSERT_SESSION = "INSERT INTO tb_comp_sesiones(id, sesion, f_modificacion) VALUES (?, ?, ?)"; //$NON-NLS-1$

	private static final String DB_STATEMENT_RECOVER_SESSION = "SELECT sesion, f_modificacion FROM tb_comp_sesiones WHERE id = ?"; //$NON-NLS-1$

	private static final String DB_STATEMENT_REMOVE_SESSION = "DELETE FROM tb_comp_sesiones WHERE id = ?"; //$NON-NLS-1$

	private static final String DB_STATEMENT_REMOVE_EXPIRED_SESSIONS = "DELETE FROM tb_comp_sesiones WHERE f_modificacion < ?"; //$NON-NLS-1$

	private final TempDocumentsDAO documentsDAO;

	public DBSessionsDAO() {
		this.documentsDAO = new DBTempDocumentsDAO();
	}

	@Override
	public boolean existsSession(final String id) throws SessionException {

		boolean exists;
		try (Connection conn = DbManager.getConnection();
				PreparedStatement st = conn.prepareStatement(DB_STATEMENT_CHECK_SESSION)) {
			st.setString(1, id);
			try (ResultSet result = st.executeQuery()) {
				exists = result.next();
			}
		}
		catch (final Exception e) {
			throw new SessionException("Error al buscar la session con ID: " + id, e); //$NON-NLS-1$
		}

		return exists;
	}

	@Override
	public void saveSession(final FireSession session, final boolean firstSave) {

		byte[] serializedSession;
		try (final ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(baos);) {
			oos.writeObject(session.getAttributtes());
			serializedSession = baos.toByteArray();
		}
		catch (final Exception e) {
			LOGGER.log(Level.WARNING, "Error al preparar para el guardado la sesion con ID: " + session.getTransactionId(), e); //$NON-NLS-1$
			return;
		}

		final String sessionId = session.getTransactionId();

		// Creamos o actualizamos la sesion segun se indique
		if (firstSave) {
			try (Connection conn = DbManager.getConnection(true);
					PreparedStatement st = conn.prepareStatement(DB_STATEMENT_INSERT_SESSION)) {
				st.setString(1, sessionId);
				st.setBlob(2, new ByteArrayInputStream(serializedSession));
				st.setLong(3, new Date().getTime());
				if (st.executeUpdate() < 1) {
					LOGGER.log(Level.WARNING, "No se pudo insertar en base de datos la sesion con ID: " + sessionId); //$NON-NLS-1$
				}
			}
			catch (final Exception e) {
				LOGGER.log(Level.WARNING, "Error al crear la sesion con ID: " + sessionId, e); //$NON-NLS-1$
			}
		}
		else {
			try (Connection conn = DbManager.getConnection(true);
					PreparedStatement st = conn.prepareStatement(DB_STATEMENT_UPDATE_SESSION)) {
				st.setBlob(1, new ByteArrayInputStream(serializedSession));
				st.setLong(2, new Date().getTime());
				st.setString(3, sessionId);
				if (st.executeUpdate() < 1) {
					LOGGER.log(Level.WARNING, "No se pudo actualizar en base de datos la sesion con ID: " + sessionId); //$NON-NLS-1$
				}
			}
			catch (final Exception e) {
				LOGGER.log(Level.WARNING, "Error al actualizar la sesion con ID: " + sessionId, e); //$NON-NLS-1$
			}
		}
	}

	@Override
	public FireSession recoverSession(final String id, final HttpSession session) {

		// Cargamos los datos de sesion
		FireSession fireSession = null;
		long lastModification = 0;
		try (Connection conn = DbManager.getConnection();
				PreparedStatement st = conn.prepareStatement(DB_STATEMENT_RECOVER_SESSION)) {
			st.setString(1, id);
			try (final ResultSet dbResult = st.executeQuery()) {
				InputStream sessionIs = null;
				if (dbResult.next()) {
					sessionIs = dbResult.getBlob(1).getBinaryStream();
					lastModification = dbResult.getLong(2);
				}
				if (sessionIs != null) {
					try (ObjectInputStream ois = new ObjectInputStream(sessionIs)) {
						final Map<String, Object> sessionData = (Map<String, Object>) ois.readObject();
						fireSession = FireSession.newSession(
								id, sessionData, lastModification + ConfigManager.getTempsTimeout());
					}
					catch (final Exception e) {
						LOGGER.log(Level.WARNING, "Error al reconstruir los datos de la session con ID: " + id, e); //$NON-NLS-1$
						fireSession = null;
					}
				}
			}

			if (fireSession == null) {
				LOGGER.warning("No se encontro la sesion con ID: " + id); //$NON-NLS-1$
			}

		}
		catch (final Exception e) {
			LOGGER.log(Level.WARNING, "Error al recuperar la session con ID: " + id, e); //$NON-NLS-1$
			fireSession = null;
		}

		return fireSession;
	}

	@Override
	public boolean deleteSession(final String id) {

		boolean deleted = false;
		try (Connection conn = DbManager.getConnection(true);
				PreparedStatement st = conn.prepareStatement(DB_STATEMENT_REMOVE_SESSION)) {
			st.setString(1, id);
			if (st.executeUpdate() > 0) {
				deleted = true;
			}
		}
		catch (final Exception e) {
			LOGGER.warning("Error al eliminar la sesion con ID: " + id); //$NON-NLS-1$
		}
		return deleted;
	}

	@Override
	public boolean deleteExpiredSessions(final long expirationTime) throws IOException {

		final long maxTime = new Date().getTime() - expirationTime;

		boolean deleted = false;
		try (Connection conn = DbManager.getConnection(true);
				PreparedStatement st = conn.prepareStatement(DB_STATEMENT_REMOVE_EXPIRED_SESSIONS)) {
			st.setLong(1, maxTime);
			if (st.executeUpdate() > 0) {
				deleted = true;
			}
		}
		catch (final Exception e) {
			LOGGER.warning("Error durante la limpieza de sesiones caducadas: " + e); //$NON-NLS-1$
		}
		return deleted;
	}

	@Override
	public TempDocumentsDAO getAssociatedDocumentsDAO() {
		return this.documentsDAO;
	}
}
