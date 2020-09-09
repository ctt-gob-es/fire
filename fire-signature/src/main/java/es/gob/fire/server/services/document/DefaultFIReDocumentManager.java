/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.services.document;

import java.io.IOException;
import java.io.Serializable;
import java.security.cert.X509Certificate;
import java.util.Properties;

import es.gob.fire.server.document.FireDocumentManagerBase;

/**
 * Implementaci&oacute;n que por defecto recibe los datos en lugar de un identificador de documento
 * y devuelve los datos firmados como resultado del guardado.
 */
public class DefaultFIReDocumentManager extends FireDocumentManagerBase
	implements Serializable {

	/** Serial Id. */
	private static final long serialVersionUID = 4608510893704491372L;

	@Override
	public boolean needConfiguration() {
		return false;
	}

	@Override
	public byte[] getDocument(final byte[] docId, final String appId, final String format,
			final Properties extraParams) throws IOException {
		return docId;
	}

	@Override
	public byte[] storeDocument(final byte[] docId, final String appId, final byte[] data,
			final X509Certificate cert, final String format, final String upgradeFormat,
			final Properties extraParams) throws IOException {
		return data;
	}
}
