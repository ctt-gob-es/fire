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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import java.util.logging.Logger;

/** Guarda firmas envi&aacute;ndolas a un servicio HTTP POST.
 * <b>Esta clase es &uacute;nicamente un ejemplo de implementaci&oacute;n del interfaz <code>SignSaver</code>
 * para depuraci&oacute;n, <u>nunca</u> debe usarse en entornos reales</b> (no hay comprobaciones de
 * qu&eacute; ficheros pueden sobrescribirse).
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
public final class SignSaverFile {

	private static final String PROP_FILENAME = "FileName"; //$NON-NLS-1$

	/** El guardado real est&aacute; deshabilitado por defecto, habilitar para usar esta clase
	 * para depuraci&oacute;n. No debe usarse para entornos reales, ya que no hay comprobaciones de
	 * qu&eacute; ficheros pueden sobrescribirse. */
	private static final boolean DISABLED = true;

	private static final Logger LOGGER = Logger.getLogger(SignSaverFile.class.getName());

	private String filename;

	/** Construye un objeto de guardado de firmas mediante un servicio HTTP POST.
	 * El servicio recibir&aacute; los datos en Base64 dentro del par&aacute;metro indicado.
	 * @param targetFileName Nombre del fichero, incluyendo ruta, donde guardar la firma. */
	public SignSaverFile(final String targetFileName) {
		if (targetFileName == null) {
			throw new IllegalArgumentException(
				"El nombre de fichero no puede ser nulo" //$NON-NLS-1$
			);
		}
		this.filename = targetFileName;
	}

	/** Constructor vac&iacute;o. */
	public SignSaverFile() {
		// Vacio
	}

	/**
	 * Recupera la configuraci&acote;n establecida para el guardado de ficheros del lote.
	 * @return Configur&aacute;n de guardado de lote.
	 */
	public Properties getConfig() {
		final Properties p = new Properties();
		p.put(PROP_FILENAME, this.filename);
		return p;
	}

	/**
	 * Guarda una firma.
	 * @param sign Datos de identificacion de la firma.
	 * @param dataToSave Contenido de la firma.
	 * @throws IOException Cuando se produce un error durante el guardado.
	 */
	public void saveSign(final SingleSign sign, final byte[] dataToSave) throws IOException {
		if (!DISABLED) {
			final OutputStream fos = new FileOutputStream(this.filename);
			final BufferedOutputStream bos = new BufferedOutputStream(
				fos,
				dataToSave.length
			);
			bos.write(dataToSave);
			bos.flush();
			fos.close();
			LOGGER.info("Guardada finalmente la firma '" + sign.getId() + "' en: " + this.filename); //$NON-NLS-1$ //$NON-NLS-2$
		}
		else {
			LOGGER.warning(
				"El guardado esta deshabilitado. La ruta de guadado solicitada es: " +  this.filename //$NON-NLS-1$
			);
		}
	}

	/**
	 * Inicializa el objeto para el guardado de firmas.
	 * @param config Configuraci&oacute;n para el guardado de las firmas.
	 */
	public void init(final Properties config) {
		if (config == null) {
			throw new IllegalArgumentException(
				"La configuracion no puede ser nula" //$NON-NLS-1$
			);
		}
		final String file = config.getProperty(PROP_FILENAME);
		if (file == null) {
			throw new IllegalArgumentException(
				"Es obligarorio que la configuracion incluya un valor para la propiedad " + PROP_FILENAME //$NON-NLS-1$
			);
		}
		this.filename = file;
	}

	/**
	 * Deshace el guardado de la firma.
	 */
	public void rollback() {
		final File file = new File(this.filename);
		if (file.exists()) {
			file.delete();
		}
	}

}
