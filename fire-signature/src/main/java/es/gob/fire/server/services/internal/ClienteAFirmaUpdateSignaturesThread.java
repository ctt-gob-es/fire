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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import es.gob.fire.server.document.FIReDocumentManager;
import es.gob.fire.server.services.AfirmaUpgrader;
import es.gob.fire.server.services.ServiceUtil;
import es.gob.fire.services.statistics.FireSignLogger;

/**
 * Hilo que ejecuta la carga, actualizaci&oacute;n y guardado de una firma de lote
 * generada con el Cliente @firma.
 */
public class ClienteAFirmaUpdateSignaturesThread extends ConcurrentProcessThread {

	private static Logger LOGGER =  FireSignLogger.getFireSignLogger().getFireLogger().getLogger();
//	private static final Logger LOGGER = Logger.getLogger(ClienteAFirmaUpdateSignaturesThread.class.getName());

	private final String appId;

	private final String docId;

	private final BatchResult batchResult;

	private final FireSession defaultConfig;

	private final FIReDocumentManager docManager;

	/**
	 * Crea un hilo para la actualizaci&oacute;n de una firma de un lote.
	 * @param appId Identificador de la aplicaci&oacute;n.
	 * @param docId Identificador a partir del cual obtener el resultado parcial y la firma.
	 * @param batchResult Objeto con todos los resultados parciales del lote.
	 * @param defaultConfig Configuraci&oacute;n por defecto en caso de no tener una espec&iacute;fica.
	 * espec&iacute;fico para el documento {@code docId} (Puede ser {@code null}).
	 * @param docManager Gestor de documentos con el que postprocesar la firma (Puede ser {@code null}).
	 */
	public ClienteAFirmaUpdateSignaturesThread(final String appId, final String docId, final BatchResult batchResult,
			final FireSession defaultConfig, final FIReDocumentManager docManager) {
		this.appId = appId;
		this.docId = docId;
		this.batchResult = batchResult;
		this.defaultConfig = defaultConfig;
		this.docManager = docManager;
	}

	@Override
	public void run() {

		// Cargamos la firma
    	final String docFilename = this.batchResult.getDocumentReference(this.docId);

    	if (isInterrupted()) {
    		this.batchResult.setErrorResult(this.docId, BatchResult.NO_PROCESSED);
    		return;
    	}

    	byte[] signature;
        try {
        	signature = TempFilesHelper.retrieveAndDeleteTempData(docFilename);
        }
        catch (final Exception e) {
        	LOGGER.warning("No se encuentra la firma: " + e); //$NON-NLS-1$
        	this.batchResult.setErrorResult(this.docId, BatchResult.UPGRADE_ERROR);
        	setFailed(true);
        	interrupt();
        	return;
		}

    	if (isInterrupted()) {
    		this.batchResult.setErrorResult(this.docId, BatchResult.NO_PROCESSED);
    		return;
    	}

        // Actualizamos la firma
        final SignBatchConfig config = this.batchResult.getSignConfig(this.docId);
        final String upgradeFormat = config != null ?
        		config.getUpgrade() :
        			(String) this.defaultConfig.getString(ServiceParams.SESSION_PARAM_UPGRADE);
        try {
        	signature = AfirmaUpgrader.upgradeSignature(signature, upgradeFormat);
        }
        catch (final Exception e) {
        	this.batchResult.setErrorResult(this.docId, BatchResult.UPGRADE_ERROR);
        	setFailed(true);
        	interrupt();
        	return;
        }

    	if (isInterrupted()) {
    		this.batchResult.setErrorResult(this.docId, BatchResult.NO_PROCESSED);
    		return;
    	}

    	// Si se definio un gestor de documentos, postprocesamos los datos con el
    	if (this.docManager != null) {
    		try {
        		final String format = config != null ? config.getFormat() :
        			(String) this.defaultConfig.getString(ServiceParams.SESSION_PARAM_FORMAT);
        		final Properties extraParams = config != null ? config.getExtraParams() :
        			ServiceUtil.base642Properties(this.defaultConfig.getString(ServiceParams.SESSION_PARAM_EXTRA_PARAM));
				signature = this.docManager.storeDocument(this.docId.getBytes(StandardCharsets.UTF_8),
						this.appId, signature, this.batchResult.getSigningCertificate(),
						format, extraParams);
			} catch (final IOException e) {
	        	LOGGER.log(Level.WARNING, "Error al postprocesar con el FIReDocumentManager la firma del documento: " + this.docId, e); //$NON-NLS-1$
	        	this.batchResult.setErrorResult(this.docId, BatchResult.ERROR_SAVING_DATA);
	        	setFailed(true);
	        	interrupt();
	        	return;
			}
    	}

    	if (isInterrupted()) {
    		this.batchResult.setErrorResult(this.docId, BatchResult.NO_PROCESSED);
    		return;
    	}

        // Pisamos la firma por el resultado de la actualizacion y el postproceso
        try {
        	TempFilesHelper.storeTempData(docFilename, signature);
        }
        catch (final Exception e) {
        	LOGGER.severe("Error al almacenar la firma en el directorio temporal: " + e); //$NON-NLS-1$
        	this.batchResult.setErrorResult(this.docId, BatchResult.ERROR_SAVING_DATA);
        	setFailed(true);
        	interrupt();
        	return;
        }

    	if (isInterrupted()) {
    		TempFilesHelper.deleteTempData(docFilename);
    		this.batchResult.setErrorResult(this.docId, BatchResult.NO_PROCESSED);
    		return;
    	}
	}

}
