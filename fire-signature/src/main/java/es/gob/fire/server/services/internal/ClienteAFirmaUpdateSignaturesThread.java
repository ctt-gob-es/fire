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
import es.gob.fire.server.services.statistics.SignatureRecorder;
import es.gob.fire.upgrade.SignatureValidator;
import es.gob.fire.upgrade.UpgradeResult;
import es.gob.fire.upgrade.VerifyException;
import es.gob.fire.upgrade.VerifyResult;

/**
 * Hilo que ejecuta la carga, actualizaci&oacute;n y guardado de una firma de lote
 * generada con el Cliente @firma.
 */
public class ClienteAFirmaUpdateSignaturesThread extends ConcurrentProcessThread {

	private static final Logger LOGGER = Logger.getLogger(ClienteAFirmaUpdateSignaturesThread.class.getName());

	private static final SignatureRecorder SIGNLOGGER = SignatureRecorder.getInstance();

	private final String appId;

	private final String docId;

	private final BatchResult batchResult;

	private final SignBatchConfig signConfig;

	private final FireSession session;

	private final FIReDocumentManager docManager;

	private final LogTransactionFormatter logF;

	/**
	 * Crea un hilo para la actualizaci&oacute;n de una firma de un lote.
	 * @param appId Identificador de la aplicaci&oacute;n.
	 * @param trId Identificador de la transacci&oacute;n.
	 * @param docId Identificador a partir del cual obtener el resultado parcial y la firma.
	 * @param batchResult Objeto con todos los resultados parciales del lote.
	 * @param signConfig Configuraci&oacute;n de firma aplicada.
	 * @param session Sesi&oacute;n de la transacci&oacute;n en la que se realiza la operaci&oacute;n.
	 * espec&iacute;fico para el documento {@code docId} (Puede ser {@code null}).
	 * @param docManager Gestor de documentos con el que postprocesar la firma (Puede ser {@code null}).
	 */
	public ClienteAFirmaUpdateSignaturesThread(final String appId, final String trId,
			final String docId, final BatchResult batchResult, final SignBatchConfig signConfig,
			final FireSession session, final FIReDocumentManager docManager) {
		this.appId = appId;
		this.docId = docId;
		this.batchResult = batchResult;
		this.signConfig = signConfig;
		this.session = session;
		this.docManager = docManager;

		this.logF = new LogTransactionFormatter(appId, trId);
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
        	signature = TempDocumentsManager.retrieveAndDeleteDocument(docFilename);
        }
        catch (final Exception e) {
        	LOGGER.warning(this.logF.f("No se encuentra la firma: " + e)); //$NON-NLS-1$
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
        final String upgradeLevel = this.signConfig.getUpgrade();

		if (upgradeLevel != null && !upgradeLevel.isEmpty()) {

			// La configuracion de mejora/validacion de firma
			final Properties upgradeConfig = this.signConfig.getUpgradeConfig();
			try {
				final SignatureValidator validator = SignatureValidatorBuilder.getSignatureValidator();
				if (ServiceParams.UPGRADE_VERIFY.equalsIgnoreCase(upgradeLevel)) {
					LOGGER.info(this.logF.f("Validamos la firma")); //$NON-NLS-1$
					final VerifyResult verifyResult = validator.validateSignature(signature, upgradeConfig);
					if (!verifyResult.isOk()) {
						LOGGER.log(Level.WARNING, this.logF.f("La firma generada no es valida: " + this.docId)); //$NON-NLS-1$
						SIGNLOGGER.register(this.session, false, this.docId);
						this.batchResult.setErrorResult(this.docId, BatchResult.INVALID_SIGNATURE);
						setFailed(true);
						interrupt();
						return;
					}
    			}
				else {
					LOGGER.info(this.logF.f("Actualizamos la firma '" + this.docId + "' a: " + upgradeLevel)); //$NON-NLS-1$ //$NON-NLS-2$
					UpgradeResult upgradeResult;
					try {
						upgradeResult = validator.upgradeSignature(signature, upgradeLevel, upgradeConfig);
    				}
    				catch (final VerifyException e) {
    	    			LOGGER.log(Level.WARNING, this.logF.f("Se ha intentado actualizar una firma invalida con el docId: ") + this.docId, e); //$NON-NLS-1$
    	    			this.batchResult.setErrorResult(this.docId, BatchResult.INVALID_SIGNATURE);
    	    			SIGNLOGGER.register(this.session, false, this.docId);
    	    			setFailed(true);
    	    			interrupt();
    	    			return;
    	    		}
					signature = upgradeResult.getResult();
				}
			}
			catch (final Exception e) {
				LOGGER.log(Level.SEVERE, this.logF.f("Error en la actualizacion de la firma"), e); //$NON-NLS-1$
				SIGNLOGGER.register(this.session, false, this.docId);
				this.batchResult.setErrorResult(this.docId, BatchResult.UPGRADE_ERROR);
				setFailed(true);
				interrupt();
				return;
			}

			if (isInterrupted()) {
				this.batchResult.setErrorResult(this.docId, BatchResult.NO_PROCESSED);
				return;
			}
		}

    	// Si se definio un gestor de documentos, postprocesamos los datos con el
    	if (this.docManager != null) {
    		try {
				signature = this.docManager.storeDocument(this.docId.getBytes(StandardCharsets.UTF_8),
						this.appId, signature, this.batchResult.getSigningCertificate(),
						this.signConfig.getFormat(), this.signConfig.getExtraParams());
			} catch (final IOException e) {
	        	LOGGER.log(Level.WARNING, this.logF.f("Error al postprocesar con el DocumentManager la firma del documento: " + this.docId), e); //$NON-NLS-1$
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
        	TempDocumentsManager.storeDocument(docFilename, signature, true);
        }
        catch (final Exception e) {
        	LOGGER.severe(this.logF.f("Error al almacenar la firma en el directorio temporal: " + e)); //$NON-NLS-1$
        	this.batchResult.setErrorResult(this.docId, BatchResult.ERROR_SAVING_DATA);
        	setFailed(true);
        	interrupt();
        	return;
        }

    	if (isInterrupted()) {
    		try {
				TempDocumentsManager.deleteDocument(docFilename);
			} catch (final IOException e) {
				LOGGER.warning("No se pudo borrar el documento: " + docFilename); //$NON-NLS-1$
			}
    		this.batchResult.setErrorResult(this.docId, BatchResult.NO_PROCESSED);
    		return;
    	}
	}

}
