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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.Properties;
import java.util.logging.Logger;

import es.gob.afirma.core.misc.AOUtil;
import es.gob.afirma.core.signers.AOSignConstants;
import es.gob.fire.server.document.FIReDocumentManager;

/**
 * Gestor de documentos para la carga de datos desde sistema de ficheros para su
 * uso en FIRe y guardado de las firmas generadas por el propio FIRe.
 */
public class FileSystemFIReDocumentManager implements FIReDocumentManager, Serializable {

	/** Serial Id. */
	private static final long serialVersionUID = -5810048749537708465L;

	private static final String IN_DIR_PARAM = "indir"; //$NON-NLS-1$
	private static final String OUT_DIR_PARAM = "outdir"; //$NON-NLS-1$
	private static final String OVERWRITE_PARAM = "overwrite"; //$NON-NLS-1$

	private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

	private String inDir;
	private String outDir;
	private boolean overwrite;

	final static Logger LOGGER = Logger.getLogger(FileSystemFIReDocumentManager.class.getName());

	@Override
	public boolean needConfiguration() {
		return true;
	}

	@Override
	public void init(final Properties config) throws IOException {

		if (config == null) {
			throw new IOException("No se ha proporcionado la configuracion para el FIReDocumentManager"); //$NON-NLS-1$
		}

		this.inDir = config.getProperty(IN_DIR_PARAM);
		this.outDir = config.getProperty(OUT_DIR_PARAM);
		this.overwrite = Boolean.parseBoolean(config.getProperty(OVERWRITE_PARAM));

		LOGGER.info("Se inicializa FileSystemFIReDocumentManager"); //$NON-NLS-1$
		LOGGER.info("Directorio de entrada de ficheros: " + this.inDir); //$NON-NLS-1$
		LOGGER.info("Directorio de salida de ficheros: " + this.outDir); //$NON-NLS-1$
	}

	@Override
	public byte[] getDocument(final byte[] docId, final String appId, final String format, final Properties extraParams) throws IOException {

		if (docId.length > 255) {
			throw new IOException(
					"El nombre de fichero no puede superar los 255 bytes" //$NON-NLS-1$
				);
		}

		final String id = new String(docId, DEFAULT_CHARSET);
		LOGGER.fine("Recuperamos el documento con identificador: " + (id.length() > 20 ? id.substring(0, 20) + "..." : id)); //$NON-NLS-1$ //$NON-NLS-2$

		final File file = new File(this.inDir, id);

		if (id.contains("\n")) { //$NON-NLS-1$
			throw new IOException(
				"Se han encontrado caracteres invalidos en el nombre de fichero" //$NON-NLS-1$
			);
		}

		if( !isRootParent(new File(this.inDir), file ) ) {
		    throw new IOException(
	    		"Se ha pedido un fichero fuera del directorio configurado: " + printShortPath(file) //$NON-NLS-1$
    		);
		}

		LOGGER.fine("Buscamos el fichero: " + printShortPath(file)); //$NON-NLS-1$

		if (!file.exists()) {
			throw new IOException("No se puede cargar el documento, no existe"); //$NON-NLS-1$
		}

		if (!file.isFile()) {
			throw new IOException(
				"No se puede cargar el documento, el elmento existe, pero no es un fichero" //$NON-NLS-1$
			);
		}

		if (!file.canRead()) {
			throw new IOException(
				"No se puede cargar el documento, no se tienen permisos de lectura sobre el" //$NON-NLS-1$
			);
		}

		final byte[] data;
		try (InputStream fis = new FileInputStream(file)) {
			data = AOUtil.getDataFromInputStream(fis);
		}
		catch (final IOException e) {
			LOGGER.warning("Error en la lectura del fichero '" + printShortPath(file) + "': " + e); //$NON-NLS-1$ //$NON-NLS-2$
			throw e;
		}

		return data;
	}

	/**
	 * Obtiene el AbsolutePath de un fichero cuidando de que, si es demasiado
	 * largo, se recorte por el medio usando puntos suspensivos ('...').
	 * @param file Fichero del que se quiere imprimir la ruta.
	 * @return Ruta completa o recortada si es demasiado larga.
	 */
	private static String printShortPath(final File file) {
		final String absolutePath = file.getAbsolutePath();
		return absolutePath.length() <= 80 ? absolutePath :
			absolutePath.substring(0, 40) + " ... " + absolutePath.substring(absolutePath.length() - 40); //$NON-NLS-1$
	}

	@Override
	public byte[] storeDocument(final byte[] docId, final String appId, final byte[] data, final X509Certificate cert, final String format, final Properties extraParams)
			throws IOException {

		final String initialId = docId != null ? new String(docId, DEFAULT_CHARSET) : "signature"; //$NON-NLS-1$
		String newId = initialId;
		final int lastDotPos = initialId.lastIndexOf('.');
		if (lastDotPos != -1) {
			newId = initialId.substring(0,  lastDotPos);
		}

		if (format != null && AOSignConstants.SIGN_FORMAT_CADES.equalsIgnoreCase(format)) {
			newId += ".csig";  //$NON-NLS-1$
		}
		else if (format != null && format.toLowerCase().startsWith(AOSignConstants.SIGN_FORMAT_XADES.toLowerCase())) {
			newId += ".xsig"; //$NON-NLS-1$
		}
		else if (lastDotPos < initialId.length() - 1) {
			newId += initialId.substring(lastDotPos);
		}

		final File file = new File(this.outDir, newId);
		if (file.exists() && !this.overwrite) {
			throw new IOException("Se ha obtenido un nombre de documento existente en el sistema de ficheros."); //$NON-NLS-1$
		}

		try ( FileOutputStream fos = new FileOutputStream(file) ) {
			fos.write(data);
		}
		catch (final IOException e) {
			LOGGER.severe("Error al almacenar los datos en el fichero '" + file.getAbsolutePath() + "': " + e); //$NON-NLS-1$ //$NON-NLS-2$
			throw e;
		}

		LOGGER.fine("Escribiendo el fichero: " + file.getAbsolutePath()); //$NON-NLS-1$
		return newId.getBytes(StandardCharsets.UTF_8);
	}

	/**
	 * Comprueba si un directorio es el ra&iacute;z de un fichero (es padre suyo o de
	 * cualquiera de los directorios de los que cuelga).
	 * @param p Directorio padre.
	 * @param file Fichero que se va a comprobar.
	 * @return {@code true} si el fichero est&aacute; dentro del directorio o si est&aacute;
	 * cualquiera de sus directorios superiores.
	 */
	private static boolean isRootParent(final File p, final File file) {
	    File f;
	    final File parent;
	    try {
	        parent = p.getCanonicalFile();
	        f = file.getCanonicalFile();
	    }
	    catch( final IOException e ) {
	        return false;
	    }

	    while( f != null ) {
	        if(parent.equals(f)) {
	            return true;
	        }
	        f = f.getParentFile();
	    }
	    return false;
	}
}
