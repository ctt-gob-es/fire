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

import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import es.gob.fire.alarms.Alarm;
import es.gob.fire.server.document.FIReDocumentManager;
import es.gob.fire.server.document.FireDocumentManagerBase;
import es.gob.fire.server.services.LogUtils;
import es.gob.fire.upgrade.ConnectionException;
import es.gob.fire.upgrade.SignatureValidator;
import es.gob.fire.upgrade.UpgradeException;
import es.gob.fire.upgrade.UpgradeParams;
import es.gob.fire.upgrade.UpgradeResult;
import es.gob.fire.upgrade.VerifyException;
import es.gob.fire.upgrade.VerifyResult;

/**
 * Hilo que finaliza que realiza una postfirma dentro de una operaci&oacute;n de firma
 * de lote.
 *
 */
class PostSignBatchTask implements Callable<String> {

	private static final Logger LOGGER = Logger.getLogger(PostSignBatchTask.class.getName());

	private final String appId;

	private final String trId;

	private final String docId;

	private final SignBatchConfig signConfig;

	private final boolean needValidation;

	private final BatchResult batchResult;

	private final FIReDocumentManager docManager;

	private final PostSignBatchRecover signRecover;

	private final TransactionAuxParams trAux;

	/**
	 * Construye un hilo que se encargar&aacute; de componer la firma electr&oacute;nica
	 * realizada con el certificado en la nube y, si se configura, de validarla o actualizarla
	 * al formato avanzado que corresponda.
	 * @param appId Identificador de la aplicaci&oacute;n.
	 * @param trId Identificador de transacci&oacute;n.
	 * @param docId Identificador del documento.
	 * @param batchResult Objeto con todos los resultados de cada firma indivdual.
	 * @param signConfig Configuraci&oacute;n de firma.
	 * @param needValidation {@code true} si se debe validarse la firma cuando se pida, {@code false}
	 * si no debe validarse nunca.
	 * @param docManager Gestor de documentos que realizar&aacute; el tratamiento de la firma.
	 * del hilo en caso de detectar un error en alguna de las firmas del lote.
	 * @param signRecover Objeto para la composici&oacute;n y recuperaci&oacute;n de la firma.
	 * @param trAux Informaci&oacute;n auxiliar de la transacci&oacute;n.
	 */
	public PostSignBatchTask(final String appId, final String trId,
			final String docId, final BatchResult batchResult, final SignBatchConfig signConfig,
			final boolean needValidation, final FIReDocumentManager docManager,
			final PostSignBatchRecover signRecover, final TransactionAuxParams trAux) {

		this.appId = appId;
		this.trId = trId;
		this.docId = docId;
		this.batchResult = batchResult;
		this.signConfig = signConfig;
		this.needValidation = needValidation;
		this.docManager = docManager;
		this.signRecover = signRecover;
		this.trAux = trAux;
	}

	@Override
	public String call() throws Exception {

		final LogTransactionFormatter logF = this.trAux.getLogFormatter();

		final Thread currentThread = Thread.currentThread();

    	if (this.batchResult.isSignFailed(this.docId)) {
    		currentThread.interrupt();
    		return null;
		}

    	// Ahora y antes de las operaciones pesadas, comprobaremos si debemos detenernos
    	// al detectar que nos han interrumpido desde afuera
    	if (currentThread.isInterrupted()) {
    		this.batchResult.setErrorResult(this.docId, BatchResult.NO_PROCESSED);
    		this.batchResult.setErrorMessage(this.docId, "Se ha interrumpido la operacion."); //$NON-NLS-1$
    		return null;
    	}

    	byte[] signature;
    	try {
    		signature = this.signRecover.recoverSign();
    	}
    	catch (final BatchRecoverException e) {
			LOGGER.log(Level.SEVERE, logF.f("Error al componer y recuperar la firma con docId: " + this.docId), e); //$NON-NLS-1$
			this.batchResult.setErrorResult(this.docId, e.getResultState());
			this.batchResult.setErrorMessage(this.docId, "Error al componer y recuperar la firma"); //$NON-NLS-1$
			throw e;
		}

    	if (currentThread.isInterrupted()) {
    		this.batchResult.setErrorResult(this.docId, BatchResult.NO_PROCESSED);
    		this.batchResult.setErrorMessage(this.docId, "Se ha interrumpido la operacion."); //$NON-NLS-1$
    		return null;
    	}

    	// Actualizamos/validamos la firma si se definio un formato de actualizacion
    	PostProcessResult processResult = null;
    	final String upgradeLevel = this.signConfig.getUpgrade();
    	if (upgradeLevel != null && !upgradeLevel.isEmpty()) {

			// La configuracion de mejora/validacion de firma
			final Properties upgradeConfig = this.signConfig.getUpgradeConfig();
    		try {
    			final SignatureValidator validator = SignatureValidatorBuilder.getSignatureValidator(logF);
    			if (ServiceParams.UPGRADE_VERIFY.equalsIgnoreCase(upgradeLevel)) {
    				if (this.needValidation) {
    					LOGGER.info(logF.f("Validamos la firma: " + this.docId)); //$NON-NLS-1$
    					final long beforeTimeMillis = System.currentTimeMillis();
    					final VerifyResult verifyResult = validator.validateSignature(signature, upgradeConfig);
    					LOGGER.info(logF.f("Tiempo de validacion: %sms", Long.toString(System.currentTimeMillis() - beforeTimeMillis))); //$NON-NLS-1$

    					if (!verifyResult.isOk()) {
    		    			LOGGER.log(Level.WARNING, logF.f("La firma del document %1s no es valida: %2s", //$NON-NLS-1$
    		    					LogUtils.cleanText(this.docId), LogUtils.cleanText(verifyResult.getDescription())));
    		    			this.batchResult.setErrorResult(this.docId, BatchResult.INVALID_SIGNATURE);
    		    			this.batchResult.setErrorMessage(this.docId, "La firma del documento no es valida"); //$NON-NLS-1$
    		    			throw new VerifyException("La firma del documento no es valida"); //$NON-NLS-1$
    					}
    				}
    				else {
    					LOGGER.info(logF.f("El proveedor es seguro y no es necesario validar la firma: " + this.docId)); //$NON-NLS-1$
    				}
    				processResult = new PostProcessResult(signature);
    			}
    			else {
    				LOGGER.info(logF.f("Actualizamos la firma '%1s' a: %2s", LogUtils.cleanText(this.docId), LogUtils.cleanText(upgradeLevel))); //$NON-NLS-1$
    				UpgradeResult upgradeResult;
    				try {
    					final long beforeTimeMillis = System.currentTimeMillis();
    					upgradeResult = validator.upgradeSignature(signature, upgradeLevel, upgradeConfig);
    					LOGGER.info(logF.f("Tiempo de actualizacion: %sms", Long.toString(System.currentTimeMillis() - beforeTimeMillis))); //$NON-NLS-1$
    				}
    				catch (final VerifyException e) {
    	    			LOGGER.log(Level.WARNING, logF.f("Se ha intentado actualizar una firma invalida con el docId: " + this.docId), e); //$NON-NLS-1$
    	    			this.batchResult.setErrorResult(this.docId, BatchResult.INVALID_SIGNATURE);
    	    			this.batchResult.setErrorMessage(this.docId, "La firma del documento no es valida"); //$NON-NLS-1$
    	    			throw e;
    	    		}

    				boolean allowPartialUpgrade = false;
    				if (upgradeConfig != null) {
    					allowPartialUpgrade = Boolean.parseBoolean(upgradeConfig.getProperty(UpgradeParams.ALLOW_PARTIAL_UPGRADE));
    				}

    		        // Comprobamos si era necesario recuperar la firma totalmente actualizada y si se ha hecho asi
    		        if (!allowPartialUpgrade && upgradeResult.getState() == UpgradeResult.State.PARTIAL) {
    	    			LOGGER.log(Level.WARNING, logF.f("No se pudo actualizar hasta el formato solicitado la firma del documento: " + this.docId)); //$NON-NLS-1$
    	    			this.batchResult.setErrorResult(this.docId, BatchResult.UPGRADE_ERROR);
    	    			this.batchResult.setErrorMessage(this.docId, "No se pudo actualizar hasta el formato solicitado la firma del documento"); //$NON-NLS-1$
    	    			throw new UpgradeException("No se pudo actualizar hasta el formato solicitado la firma del documento"); //$NON-NLS-1$
    		        }

    				if (upgradeResult.getState() == UpgradeResult.State.PENDING) {
    					processResult = new PostProcessResult(upgradeResult.getGracePeriodInfo());
    				}
    				else {
    					processResult = new PostProcessResult(upgradeResult.getResult());
    				}
    				if (this.batchResult.getSignConfig(this.docId) != null) {
    					this.batchResult.getSignConfig(this.docId).setUpgrade(upgradeResult.getFormat());
    				}
    			}
    		}
    		catch (final ConnectionException e) {
    			LOGGER.log(Level.SEVERE, logF.f("No se pudo conectar con el servicio de validacion y mejora de firmas"), e); //$NON-NLS-1$
    			AlarmsManager.notify(Alarm.CONNECTION_VALIDATION_PLATFORM);
    			this.batchResult.setErrorResult(this.docId, BatchResult.UPGRADE_ERROR);
    			this.batchResult.setErrorMessage(this.docId, "No se pudo conectar con el servicio de validacion y mejora de firmas."); //$NON-NLS-1$
    			throw e;
    		}
    		catch (final Exception e) {
    			LOGGER.log(Level.SEVERE, logF.f("Error al validar/actualizar la firma con docId: " + this.docId), e); //$NON-NLS-1$
    			this.batchResult.setErrorResult(this.docId, BatchResult.UPGRADE_ERROR);
    			this.batchResult.setErrorMessage(this.docId, "Error al validar/actualizar la firma."); //$NON-NLS-1$
    			throw e;
    		}

        	if (currentThread.isInterrupted()) {
        		this.batchResult.setErrorResult(this.docId, BatchResult.NO_PROCESSED);
        		this.batchResult.setErrorMessage(this.docId, "No se ha procesado la firma."); //$NON-NLS-1$
    			return null;
        	}
    	}
    	else {
    		processResult = new PostProcessResult(signature);
    	}

    	// Si la firma generada requiere un periodo de gracia para la actualizacion,
    	// se registra que la firma ha terminado bien
    	if (processResult.getGracePeriodInfo() != null) {
    		// Registrar en el listado de resultados el de la operacion
        	this.batchResult.setSuccessResult(this.docId, processResult.getGracePeriodInfo());
        	return null;
    	}

    	// Si se definio un gestor de documentos, procesamos la firma tal como este indique
    	byte[] result;
    	if (this.docManager != null) {
    		try {
    			if (this.docManager instanceof FireDocumentManagerBase) {
    				result = ((FireDocumentManagerBase) this.docManager).storeDocument(
    						this.docId.getBytes(StandardCharsets.UTF_8), this.trId,
    						this.appId, processResult.getResult(), this.batchResult.getSigningCertificate(),
    						this.signConfig.getFormat(), this.signConfig.getUpgrade(),
    						this.signConfig.getExtraParams());
    			}
    			else {
    				result = this.docManager.storeDocument(this.docId.getBytes(StandardCharsets.UTF_8),
    						this.appId, processResult.getResult(), this.batchResult.getSigningCertificate(),
    						this.signConfig.getFormat(), this.signConfig.getExtraParams());
    			}
    		}
    		catch (final Exception e) {
    			LOGGER.log(Level.WARNING, logF.f("Error al postprocesar con el DocumentManager la firma del documento: " + this.docId), e); //$NON-NLS-1$
    			AlarmsManager.notify(Alarm.CONNECTION_DOCUMENT_MANAGER, this.docManager.getClass().getCanonicalName());
        		this.batchResult.setErrorResult(this.docId, BatchResult.ERROR_SAVING_DATA);
        		this.batchResult.setErrorMessage(this.docId, "Error al postprocesar con el DocumentManager la firma del documento"); //$NON-NLS-1$
    			throw e;
    		}
    	}
    	else {
    		result = processResult.getResult();
    	}

    	if (currentThread.isInterrupted()) {
    		this.batchResult.setErrorResult(this.docId, BatchResult.NO_PROCESSED);
    		this.batchResult.setErrorMessage(this.docId, "Se ha interrumpido la operacion."); //$NON-NLS-1$
    		return null;
    	}

    	// Guardamos el resultado sobre el documento de datos
    	final String docFilename = this.batchResult.getDocumentReference(this.docId);
    	try {
    		TempDocumentsManager.storeDocument(docFilename, result, false, this.trAux);
    	}
    	catch (final Exception e) {
    		LOGGER.log(Level.SEVERE, logF.f("Error al almacenar la firma en el temporal"), e); //$NON-NLS-1$
    		this.batchResult.setErrorResult(this.docId, BatchResult.ERROR_SAVING_DATA);
    		this.batchResult.setErrorMessage(this.docId, "Error al almacenar la firma en el temporal"); //$NON-NLS-1$
			throw e;
		}

    	if (currentThread.isInterrupted()) {
    		this.batchResult.setErrorResult(this.docId, BatchResult.NO_PROCESSED);
    		this.batchResult.setErrorMessage(this.docId, "Se ha interrumpido la operacion."); //$NON-NLS-1$
    		return null;
    	}

    	// Registrar en el listado de resultados el de la operacion
    	this.batchResult.setSuccessResult(this.docId);
    	return this.docId;
	}
}