/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.services.internal.sessions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpSession;

import es.gob.fire.server.services.internal.FireSession;
import es.gob.fire.signature.ConfigManager;

/**
 * Gestor para el guardado de sesiones que utiliza un directorio en disco como
 * lugar para el guardado y lectura de datos de sesi&oacute;n. Este mecanismo
 * puede conllevar un uso intensivo de la unidad de disco y problemas de
 * inconsistencia si un retardo de escritura en disco permitiese que se buscase
 * una sesi&oacute;n antes de que terminase de grabarse.<br>
 * El borrado de sesiones de disco caducadas debe llevarse a cabo a trav&eacute;s
 * del proceso de gesti&oacute;n de sesiones pero esta clase incluye su propia
 * l&oacute;gica para el borrado de sesiones hu&eacute;rfanas que no se borraron en
 * su momento. Al instanciar el DAO se inicia el proceso de borrado de sesiones caducadas.
 */
public class FileSystemSessionsDAO implements SessionsDAO {

	private static final Logger LOGGER = Logger.getLogger(FileSystemSessionsDAO.class.getName());

	private static final String SESSIONS_TEMP_DIR = "sessions"; //$NON-NLS-1$

	/** Directorio de sesiones. */
	private final File dir;

	/** Gestor asociado para el guardado de ficheros temporales. */
	private final TempDocumentsDAO documentsDAO;

	/**
	 * Construye el gestor y crea el directorio para el guardado.
	 */
	public FileSystemSessionsDAO() {

		this.dir = new File(ConfigManager.getTempDir(), SESSIONS_TEMP_DIR);

		if (!this.dir.exists()) {
			// Creamos el directorio con permisos para despues poder crear subdirectorios desde la propia aplicacion
			try {
				final Set<PosixFilePermission> ownerWritable = PosixFilePermissions.fromString("rwxrwxrwx"); //$NON-NLS-1$
				final FileAttribute<?> permissions = PosixFilePermissions.asFileAttribute(ownerWritable);
				Files.createDirectories(this.dir.toPath(), permissions);
			}
			catch (final Exception e) {
				this.dir.mkdirs();
			}
		}
		this.documentsDAO = new FileSystemTempDocumentsDAO();
	}

	@Override
	public boolean existsSession(final String id) {
		return Files.exists(new File(this.dir, id).toPath());
	}

	@Override
	public FireSession recoverSession(final String id, final HttpSession session) {

		final File sessionFile = new File(this.dir, id);

		final Map<String, Object> sessionData;
		try {
			sessionData = loadSessionData(sessionFile);
		}
		catch (final FileNotFoundException e) {
			LOGGER.warning("No se encontro en disco la sesion: " + id); //$NON-NLS-1$
			return null;
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Error al cargar de disco la sesion: " + id, e); //$NON-NLS-1$
			return null;
		}

		// Ya que la fecha del fichero es la fecha en la que se guardo y este tambien sera el momento
		// en el que se actualizo la fecha de expiracion, podemos recoger esa fecha y sumarle el tiempo
		// de vigencia para obtener la misma fecha de caducidad
		return FireSession.newSession(id, sessionData,
				sessionFile.lastModified() + ConfigManager.getTempsTimeout());
	}

	/**
	 * Carga de un fichero de sesi&oacute;n los datos que contiene.
	 * @param sessionFile Fichero de sesi&oacute;n.
	 * @return Datos contenidos en la sesi&oacute;n del fichero.
	 * @throws FileNotFoundException Cuando no se encuentra el fichero.
	 * @throws Exception Cuando ocurre un error durante la carga de la sesi&oacute;n.
	 */
	private static Map<String, Object> loadSessionData(final File sessionFile) throws FileNotFoundException, Exception {

		final Map<String, Object> sessionData;
		try (final FileInputStream fis = new FileInputStream(sessionFile);
				final ObjectInputStream ois = new ObjectInputStream(fis)) {
			sessionData = (Map<String, Object>) ois.readObject();
		}
		return sessionData;
	}

	@Override
	public void saveSession(final FireSession session, final boolean created) {
		try (final FileOutputStream fos = new FileOutputStream(new File(this.dir, session.getTransactionId()));) {
			try (ObjectOutputStream oos = new ObjectOutputStream(fos)) {
				oos.writeObject(session.getAttributtes());
			}
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Error al guardar en disco la sesion: " + session.getTransactionId(), e); //$NON-NLS-1$
		}
	}

	@Override
	public void deleteSession(final String id) {
		try {
			Files.delete(new File(this.dir, id).toPath());
		} catch (final NoSuchFileException e) {
			// No hacemos nada
		} catch (final IOException e) {
			LOGGER.warning("No se pudo eliminar de disco la sesion " + id); //$NON-NLS-1$
		}
	}

	@Override
	public void deleteExpiredSessions(final long expirationTime) throws IOException {

    	for (final File tempFile : this.dir.listFiles(new ExpiredFileFilter(expirationTime))) {
    		try {
    			Files.delete(tempFile.toPath());
    		}
    		catch (final Exception e) {
    			LOGGER.warning("No se pudo eliminar la sesion caducada " + tempFile.getName() + //$NON-NLS-1$
    					": " + e); //$NON-NLS-1$
    		}
    	}
	}

	@Override
	public TempDocumentsDAO getAssociatedDocumentsDAO() {
		return this.documentsDAO;
	}

}
