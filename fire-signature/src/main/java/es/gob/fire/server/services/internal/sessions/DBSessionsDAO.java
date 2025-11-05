package es.gob.fire.server.services.internal.sessions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

	private static final String DB_STATEMENT_SELECT_EXPIRED_SESSIONS = "SELECT id, sesion, f_modificacion FROM tb_comp_sesiones WHERE f_modificacion < ?"; //$NON-NLS-1$

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
				LOGGER.log(Level.WARNING, "Error al crear la sesion: " + sessionId //$NON-NLS-1$
						+ ". No se pueda establecer la conexion con la base de datos", e); //$NON-NLS-1$
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
		try (Connection conn = DbManager.getConnection();
				PreparedStatement st = conn.prepareStatement(DB_STATEMENT_RECOVER_SESSION)) {
			st.setString(1, id);
			try (final ResultSet dbResult = st.executeQuery()) {
				if (dbResult.next()) {
					final Blob sessionBlob = dbResult.getBlob(1);
					final long lastModification = dbResult.getLong(2);
					fireSession = loadSession(id, sessionBlob, lastModification);
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

	/**
	 * Carga la sesi&oacute;n a partir del binario de base de datos. Se crea con la fecha de
	 * caducidad estimada de su &uacute;ltima modificaci&oacute;n m&aacute;s el tiempo permitido
	 * de inactividad.
	 * @param id Identificador de la sesi&oacute;n.
	 * @param sessionBlob Binario con el contenido de la sesi&oacute;n.
	 * @param lastModification Fecha registrada de &uacute;ltima modificacion para mantenerla en la nueva
	 * @return Sesi&oacute;n de FIRe cargada o {@code null} si no se pudo cargar.
	 */
	private static FireSession loadSession(final String id, final Blob sessionBlob, final long lastModification) {

		FireSession fireSession = null;
		if (sessionBlob != null) {
			try (InputStream sessionIs = sessionBlob.getBinaryStream();
					ObjectInputStream ois = new ObjectInputStream(sessionIs)) {
				final Map<String, Object> sessionData = (Map<String, Object>) ois.readObject();
				fireSession = FireSession.newSession(
						id, sessionData, lastModification + ConfigManager.getTempsTimeout());
			}
			catch (final Exception e) {
				LOGGER.log(Level.WARNING, "Error al reconstruir los datos de la session con ID: " + id, e); //$NON-NLS-1$
				fireSession = null;
			}
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
	public FireSession[] deleteExpiredSessions(final long expirationTime, final boolean loadSessions) throws IOException {

		final long maxTime = new Date().getTime() - expirationTime;

		List<SessionInfo> sessionInfos = null;
		try (Connection conn = DbManager.getConnection(true)) {

			// Si se ha solicitado cargar las sesiones caducadas, se leen previamente
			if (loadSessions) {
				sessionInfos = new ArrayList<>();
				try (PreparedStatement st = conn.prepareStatement(DB_STATEMENT_SELECT_EXPIRED_SESSIONS)) {
					st.setLong(1, maxTime);
					// Obtenemos la informacion de todas las sesiones caducadas
					final ResultSet rs = st.executeQuery();
					while (rs.next()) {
						sessionInfos.add(new SessionInfo(rs.getString(1), rs.getBlob(2), rs.getLong(3)));
					}
				}
			}

			// Eliminamos las sesiones caducadas
			try (PreparedStatement st = conn.prepareStatement(DB_STATEMENT_REMOVE_EXPIRED_SESSIONS)) {
				st.setLong(1, maxTime);
				st.executeUpdate();
			}
		}
		catch (final Exception e) {
			LOGGER.warning("Error durante la limpieza de sesiones caducadas: " + e); //$NON-NLS-1$
		}

		// Cargamos las selecciones que hayamos leido en caso de haberlo hecho
		FireSession[] fireSessions = null;
		if (sessionInfos != null) {
			final List<FireSession> sessionsList = new ArrayList<>();
			for (final SessionInfo info : sessionInfos) {
				sessionsList.add(loadSession(info.getId(), info.getBlob(), info.getTime()));
			}
			fireSessions = sessionsList.toArray(new FireSession[0]);
		}

		return fireSessions;
	}

	@Override
	public TempDocumentsDAO getAssociatedDocumentsDAO() {
		return this.documentsDAO;
	}

	private static class SessionInfo {
		private final String id;
		private final Blob blob;
		private final long time;

		public SessionInfo(final String id, final Blob blob, final long time) {
			this.id = id;
			this.blob = blob;
			this.time = time;
		}
		public String getId() {
			return this.id;
		}
		public Blob getBlob() {
			return this.blob;
		}
		public long getTime() {
			return this.time;
		}
	}
}
