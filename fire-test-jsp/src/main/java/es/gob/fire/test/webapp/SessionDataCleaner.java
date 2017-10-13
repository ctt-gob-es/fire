/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.test.webapp;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * Borra los datos en disco de la sesi&oacute;n de firma.
 *
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s.
 */
@WebListener
public final class SessionDataCleaner implements HttpSessionListener {

    /**
     * Nombre del objeto de sesi&oacute;n que contiene el nombre del fichero
     * temporal donde est&aacute;n los datos.
     */
    public static final String DATA_FILENAME_TAG = "datafile"; //$NON-NLS-1$

    /**
     * Nombre del objeto de sesi&oacute;n que contiene el nombre del fichero
     * temporal donde est&aacute;n los datos temporales de la firma
     * trif&aacute;sica.
     */
    public static final String TRIPHASE_DATA_FILENAME_TAG = "triphasedatafile"; //$NON-NLS-1$

    private static final Logger LOGGER = Logger.getLogger(SessionDataCleaner.class.getName());

    SimpleDateFormat sd = new SimpleDateFormat("HH:mm:ss");

    @Override
    public void sessionCreated(final HttpSessionEvent hse) {
    	final HttpSession session = hse.getSession();
        LOGGER.info("Iniciada sesion " + session.getId()); //$NON-NLS-1$
    }

    @Override
    public void sessionDestroyed(final HttpSessionEvent hse) {
        if (hse != null) {
            final HttpSession ses = hse.getSession();
        	if (ses != null) {
                final String dataFilename = (String) ses
                        .getAttribute(DATA_FILENAME_TAG);
                if (dataFilename != null) {
                    final File dataFile = new File(dataFilename);
                    if (dataFile.exists()) {
                        if (!dataFile.delete()) {
                            SessionDataCleaner.LOGGER
                                    .severe("No se ha podido eliminar el objeto de datos " + dataFilename //$NON-NLS-1$
                                    );
                        }
                    }
                }
                final String triphaseDataFilename = (String) ses
                        .getAttribute(TRIPHASE_DATA_FILENAME_TAG);
                if (triphaseDataFilename != null) {
                    final File dataFile = new File(triphaseDataFilename);
                    if (dataFile.exists()) {
                        if (!dataFile.delete()) {
                            SessionDataCleaner.LOGGER
                                    .severe("No se ha podido eliminar el objeto de datos " + triphaseDataFilename //$NON-NLS-1$
                                    );
                        }
                    }
                }
            }
        }
    }
}
