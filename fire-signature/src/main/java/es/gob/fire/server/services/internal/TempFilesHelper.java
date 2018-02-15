/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.services.internal;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.logging.Logger;

import es.gob.afirma.core.misc.AOUtil;
import es.gob.fire.signature.ConfigManager;

/**
 * @author carlos.gamuci
 *
 */
public final class TempFilesHelper {

    private static final Logger LOGGER = Logger.getLogger(TempFilesHelper.class.getName());

    private static final String DEFAULT_PREFIX = "fire-"; //$NON-NLS-1$

    private static File TMPDIR;

    static {

        final String defaultDir = System.getProperty("java.io.tmpdir"); //$NON-NLS-1$

        try {
            final String tmpDir = ConfigManager.getTempDir();
            final File f = new File(tmpDir != null && tmpDir.trim().length() > 0 ? tmpDir.trim() : defaultDir);
            if (!f.isDirectory() || !f.canRead() || !f.canWrite()) {
                LOGGER.severe(
                		"El directorio temporal configurado (" + f.getAbsolutePath() + //$NON-NLS-1$
                		") no es valido, se usaran el por defecto: " + defaultDir); //$NON-NLS-1$
                TMPDIR = new File(defaultDir);
            } else {
                TMPDIR = f;
            }
        }
        catch (final Exception e) {
        	LOGGER.severe("No se ha podido cargar la configuracion del modulo: " + e); //$NON-NLS-1$
        	TMPDIR = new File(defaultDir);
        }
    }

    /**
     * Constructor privado para no permir la instanciaci&oacute;n
     */
    private TempFilesHelper() {
        // no instanciable
    }

    /**
     * Lee el contenido de un documento guardado en un fichero situado en un directorio
     * concreto y despu&eacute;s lo elimina.
     * @param filename Nombre del fichero.
     * @return Contenido del fichero.
     * @throws IOException Cuando no se encuentra el fichero o no puede leerse.
     */
    public static byte[] retrieveAndDeleteTempData(final String filename) throws IOException {
    	final byte[] ret = retrieveTempData(filename);
        Files.deleteIfExists(new File(TMPDIR, filename).toPath());
        return ret;
    }

    /**
     * Lee el contenido de un documento guardado en un fichero situado en un directorio
     * concreto.
     * @param filename Nombre del fichero.
     * @return Contenido del fichero.
     * @throws IOException Cuando no se encuentra el fichero o no puede leerse.
     */
    public static byte[] retrieveTempData(final String filename) throws IOException {
        if (filename == null || filename.isEmpty()) {
            throw new IllegalArgumentException(
                    "El nombre del fichero a recuperar no puede ser nulo" //$NON-NLS-1$
            );
        }
        if (filename.contains("..") || filename.contains(File.separator) || filename.contains(File.pathSeparator)) { //$NON-NLS-1$
            throw new IllegalArgumentException(
                    "El nombre del fichero a recuperar no es valido: " + filename //$NON-NLS-1$
            );
        }
        final File f = new File(TMPDIR, filename);
        final InputStream fis = new FileInputStream(f);
        final InputStream bis = new BufferedInputStream(fis);
        final byte[] ret = AOUtil.getDataFromInputStream(bis);
        bis.close();
        fis.close();

        return ret;
    }

    /**
     * Elimina un fichero situado en un directorio
     * concreto.
     * @param filename Nombre del fichero.
     */
    public static void deleteTempData(final String filename) {
        new File(TMPDIR, filename).delete();
    }

    /**
     * Almacena datos en el directorio temporal.
     * @param tempFileName Nombre del fichero en el que se almacenar&aacute;n los datos.
     * 			Si se indica null, se generar&aacute; un nombre aleatorio
     * @param data Datos a almacenar.
     * @return Nombre del fichero.
     * @throws IOException Cuando ocurre un error durante el guardado.
     */
    public static String storeTempData(final String tempFileName, final byte[] data) throws IOException {
        if (data == null || data.length < 1) {
            throw new IllegalArgumentException(
                    "Los datos a guardar no pueden ser nulos ni vacios" //$NON-NLS-1$
            );
        }
        File f;
        if (tempFileName != null) {
        	f = new File(TMPDIR, tempFileName);
        }
        else {
        	f = File.createTempFile(DEFAULT_PREFIX, null, TMPDIR);
        }

        final OutputStream fos = new FileOutputStream(f);
        final OutputStream bos = new BufferedOutputStream(fos);
        bos.write(data);
        bos.close();
        fos.close();
        LOGGER.info("Almacenado temporal de datos en: " + f.getAbsolutePath()); //$NON-NLS-1$
        return f.getName();
    }

}
