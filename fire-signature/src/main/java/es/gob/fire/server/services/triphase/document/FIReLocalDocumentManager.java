/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.services.triphase.document;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.Properties;

import es.gob.afirma.core.misc.Base64;
import es.gob.fire.server.services.internal.TempFilesHelper;

/**
 *  Manejador que determina de donde recoge los documentos FIRe para realizar una firma
 *  trifasica mediante el Cliente @firma y donde almacenar la firma resultante. Concretamente,
 *  obtendr&aacute; los datos del directorio temporal en el que se almacenan al enviarse al
 *  servidor y almacena las firmas en el mismo directorio temporal.
 */
public class FIReLocalDocumentManager implements DocumentManager {

	private static final String RESULT_OK = "OK"; //$NON-NLS-1$

	private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

	@Override
	public byte[] getDocument(final String id, final X509Certificate[] certChain, final Properties config) throws IOException {
		return TempFilesHelper.retrieveTempData(
				new String(
						Base64.decode(id.replace('-', '+').replace('_', '/')),
						DEFAULT_CHARSET));
	}

	@Override
	public String storeDocument(final String id, final X509Certificate[] certChain, final byte[] signature, final Properties config)
			throws IOException {

		// Guardamos la firma en un temporal
		TempFilesHelper.storeTempData(
				new String(
						Base64.decode(id.replace('-', '+').replace('_', '/')),
						DEFAULT_CHARSET),
				signature);

		return Base64.encode(RESULT_OK.getBytes());
	}

}
