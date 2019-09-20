/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.services.batch;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.logging.Logger;

import es.gob.afirma.core.misc.AOUtil;
import es.gob.fire.server.services.internal.TempFilesHelper;

final class TempStoreFileSystem {

	private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

	private static final Logger LOGGER = Logger.getLogger(TempStoreFileSystem.class.getName());
	private static final MessageDigest MD;
	static {
		try {
			MD = MessageDigest.getInstance("SHA-1"); //$NON-NLS-1$
		}
		catch (final Exception e) {
			throw new IllegalStateException(
				"No se ha podido cargar el motor de huellas para SHA-1: " + e, e //$NON-NLS-1$
			);
		}
	}

	public static void store(final byte[] dataToSave, final SingleSign ss, final String batchId) throws IOException {
		final String filename = getFilename(ss, batchId);
		TempFilesHelper.storeTempData(filename, dataToSave);
		LOGGER.fine("Firma '" + ss.getId() + "' almacenada temporalmente en " + filename); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static byte[] retrieve(final SingleSign ss, final String batchId) throws IOException {
		return TempFilesHelper.retrieveTempData(getFilename(ss, batchId));
	}

	public static void delete(final SingleSign ss, final String batchId) {
		TempFilesHelper.deleteTempData(getFilename(ss, batchId));
	}

	private static String getFilename(final SingleSign ss, final String batchId) {
		return AOUtil.hexify(MD.digest(ss.getId().getBytes(DEFAULT_CHARSET)), false) + "." + batchId; //$NON-NLS-1$
	}
}
