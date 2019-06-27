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
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

import es.gob.afirma.core.misc.AOUtil;
import es.gob.fire.signature.ConfigManager;

/**
 * Clase con m&eacute;todos de ayuda para la gesti&oacute;n de ficheros temporales.
 *
 */
public final class TempFilesHelper {

	private static final Logger LOGGER = Logger.getLogger(TempFilesHelper.class.getName());

    private static final String DEFAULT_PREFIX = "fire-"; //$NON-NLS-1$

    private static File TMPDIR;

    private static long fileSize = 0L;

    static {

        final String defaultDir = System.getProperty("java.io.tmpdir"); //$NON-NLS-1$

        try {
            final String tmpDir = ConfigManager.getTempDir();
            final File f = tmpDir != null && tmpDir.trim().length() > 0 ? new File(tmpDir.trim()) : null;
            if (f == null || !f.isDirectory()) {
                LOGGER.severe(
                		"El directorio temporal configurado (" + //$NON-NLS-1$
                		(f != null ? f.getAbsolutePath() : null) +
                		") no es valido, se usara el por defecto: " + defaultDir); //$NON-NLS-1$
                TMPDIR = new File(defaultDir);
            } else if (!f.canRead() || !f.canWrite()) {
            	LOGGER.severe(
                		"El directorio temporal configurado (" + f.getAbsolutePath() + //$NON-NLS-1$
                		") no tiene permiso de lectura/escritura, se usara el por defecto: " + defaultDir); //$NON-NLS-1$
            	TMPDIR = new File(defaultDir);
            } else {
                LOGGER.info("Se usara el directorio temporal configurado: " + f.getAbsolutePath()); //$NON-NLS-1$
                TMPDIR = f;
            }
        }
        catch (final Exception e) {
        	LOGGER.log(Level.SEVERE, "No se ha podido cargar la configuracion del modulo", e); //$NON-NLS-1$
        	LOGGER.warning("Se usara el directorio temporal por defecto: " + defaultDir); //$NON-NLS-1$
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
        if (!f.getCanonicalPath().startsWith(TMPDIR.getCanonicalPath())){
			throw new SecurityException("Se ha intentado acceder a una ruta fuera del directorio de logs: " + f.getAbsolutePath()); //$NON-NLS-1$
        }
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
        LOGGER.fine("Almacenado temporal de datos en: " + f.getAbsolutePath()); //$NON-NLS-1$
        setFileSize(f.length());
        return f.getName();
    }

	public static long getFileSize() {
		return fileSize;
	}

	private static  void setFileSize(final long fileSize) {
		TempFilesHelper.fileSize = fileSize;
	}
  /**
     * Recorre el directorio temporal eliminando los ficheros que hayan sobrepasado el tiempo
     * indicado sin haber sido modificados.
     * @param timeout Tiempo en milisegundos que debe haber transcurrido desde la &uacute;ltima
     * modificaci&oacute;n de un fichero para considerarse caducado.
     */
    public static void cleanExpiredFiles(final long timeout) {

    	for (final File tempFile : TMPDIR.listFiles(new ExpiredFileFilter(timeout))) {
    		try {
    			Files.delete(tempFile.toPath());
    		}
    		catch (final Exception e) {
    			LOGGER.warning("No se pudo eliminar el fichero caducado " + tempFile.getAbsolutePath() + //$NON-NLS-1$
    					": " + e); //$NON-NLS-1$
    		}
    	}
    }

    /**
     * Filtro de ficheros para la obtenci&oacute;n de ficheros de datos
     * que se hayan modificado hace m&aacute;s del tiempo indicado.
     */
    private static class ExpiredFileFilter implements FileFilter {

    	private final long timeoutMillis;

    	/**
    	 * Tiempo m&aacute;ximo de vigencia de un fichero.
    	 * @param timeout Tiempo de vigencia en milisegundos.
    	 */
    	public ExpiredFileFilter(final long timeout) {
    		this.timeoutMillis = timeout;
		}

		@Override
		public boolean accept(final File pathname) {
			return pathname.isFile() && System.currentTimeMillis() > pathname.lastModified() + this.timeoutMillis;
		}

    }}
