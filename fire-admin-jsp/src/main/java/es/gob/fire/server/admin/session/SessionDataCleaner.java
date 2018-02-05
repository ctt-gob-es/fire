/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.admin.session;

import java.util.Enumeration;
import java.util.logging.Logger;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/** Borra los datos en disco de la sesi&oacute;n de firma.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
@WebListener
public final class SessionDataCleaner implements HttpSessionListener {

	Logger LOGGER = Logger.getLogger(SessionDataCleaner.class.getName());

	@Override
	public void sessionCreated(final HttpSessionEvent hse) {
		this.LOGGER.info("Iniciada sesion " + hse.getSession().getId()); //$NON-NLS-1$
	}

	@Override
	public void sessionDestroyed(final HttpSessionEvent hse) {
		if (hse != null) {
			this.LOGGER.info("Eliminamos los datos de sesion"); //$NON-NLS-1$

			final HttpSession ses = hse.getSession();
			final Enumeration<String> attrs = ses.getAttributeNames();
			while (attrs.hasMoreElements()) {
				ses.removeAttribute(attrs.nextElement());
			}
		}
	}

}
