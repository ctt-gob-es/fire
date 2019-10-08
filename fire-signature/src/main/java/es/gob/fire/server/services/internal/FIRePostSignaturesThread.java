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
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import es.gob.afirma.core.signers.TriphaseData;
import es.gob.afirma.core.signers.TriphaseData.TriSign;
import es.gob.fire.server.connector.FIReSignatureException;
import es.gob.fire.server.document.FIReDocumentManager;
import es.gob.fire.server.services.DocInfo;
import es.gob.fire.server.services.FIReTriHelper;
import es.gob.fire.server.services.FIReTriSignIdProcessor;
import es.gob.fire.server.services.statistics.SignatureRecorder;
import es.gob.fire.upgrade.SignatureValidator;
import es.gob.fire.upgrade.UpgradeResult;
import es.gob.fire.upgrade.VerifyException;
import es.gob.fire.upgrade.VerifyResult;

/**
 * Hilo que finaliza que realiza una postfirma dentro de una operaci&oacute;n de firma
 * de lote.
 *
 */
class FIRePostSignaturesThread extends ConcurrentProcessThread {

	private static final SignatureRecorder SIGNLOGGER = SignatureRecorder.getInstance();
	private static final Logger LOGGER = Logger.getLogger(FIRePostSignaturesThread.class.getName());

	private final String appId;

	private final String trId;

	private final String docId;

	private final String algorithm;

	private final SignBatchConfig signConfig;

	private final boolean needValidation;

	private final X509Certificate signingCert;

	private final BatchResult batchResult;

	private final Map<String, byte[]> pkcs1s;

	private final TriphaseData partialTd;

	private final FIReDocumentManager docManager;

	private final FireSession session;

	/**
	 * Construye un hilo que se encargar&aacute; de componer la firma electr&oacute;nica
	 * realizada con el certificado en la nube y la actualizar&aacute; al formato avanzado
	 * que corresponda (si se configura).
	 * @param appId Identificador de la aplicaci&oacute;n.
	 * @param trId Identificador de transacci&oacute;n.
	 * @param docId Identificador del documento.
	 * @param batchResult Objeto con todos los resultados de cada firma indivdual.
	 * @param algorithm Algoritmo de firma.
	 * @param signConfig Configuraci&oacute;n de firma.
	 * @param needValidation {@code true} si se debe validarse la firma cuando se pida, {@code false}
	 * si no debe validarse nunca.
	 * @param signingCert Certificado de firma.
	 * @param pkcs1s Conjunto de PKCS#1 de la firma.
	 * @param partialTd Datos parciales de la firma.
	 * @param docManager Gestor de documentos que realizar&aacute; el tratamiento de la firma.
	 * del hilo en caso de detectar un error en alguna de las firmas del lote.
	 */
	public FIRePostSignaturesThread(final String appId, final String trId, final String docId, final BatchResult batchResult,
			final String algorithm, final SignBatchConfig signConfig, final boolean needValidation, final X509Certificate signingCert,
			final Map<String, byte[]> pkcs1s, final TriphaseData partialTd, final FIReDocumentManager docManager, final FireSession session) {

		this.appId = appId;
		this.trId = trId;
		this.docId = docId;
		this.batchResult = batchResult;
		this.algorithm = algorithm;
		this.signConfig = signConfig;
		this.needValidation = needValidation;
		this.signingCert = signingCert;
		this.pkcs1s = pkcs1s;
		this.partialTd = partialTd;
		this.docManager = docManager;
		this.session = session;
	}

	@Override
	public void run() {

		final LogTransactionFormatter logF = new LogTransactionFormatter(this.appId, this.trId);

    	if (this.batchResult.isSignFailed(this.docId)) {
    		setFailed(true);
    		final DocInfo docInf = this.batchResult.getDocInfo(this.docId);
        	if(docInf != null) {
        		this.session.setAttribute(ServiceParams.SESSION_PARAM_DOCSIZE, docInf.getSize());
        	}
    		SIGNLOGGER.register(this.session, false,this.docId);
    		interrupt();
    		return;
		}

    	// Ahora y antes de las operaciones pesadas, comprobaremos si debemos detenernos
    	// al detectar que nos han interrumpido desde afuera
    	if (isInterrupted()) {
    		this.batchResult.setErrorResult(this.docId, BatchResult.NO_PROCESSED);
    		return;
    	}

    	final String docFilename = this.batchResult.getDocumentReference(this.docId);

    	final byte[] data;
        try {
        	data = TempFilesHelper.retrieveAndDeleteTempData(docFilename);
        }
        catch (final Exception e) {
        	LOGGER.warning(logF.f("No se encuentran los datos a firmar: ") + e); //$NON-NLS-1$
        	this.batchResult.setErrorResult(this.docId, BatchResult.DATA_NOT_FOUND);

        	final DocInfo docInf = this.batchResult.getDocInfo(this.docId);
        	if(docInf != null) {
        		this.session.setAttribute(ServiceParams.SESSION_PARAM_DOCSIZE, docInf.getSize());
        	}

        	SIGNLOGGER.register(this.session, false, this.docId);
        	setFailed(true);
        	interrupt();
    		return;
		}

    	if (isInterrupted()) {
    		this.batchResult.setErrorResult(this.docId, BatchResult.NO_PROCESSED);
    		SIGNLOGGER.register(this.session, false, this.docId);
    		return;
    	}

        // De la informacion trifasica que tenemos, extraemos lo correspondiente
    	// a la firma de este documento. Tenemos cuidado de deshacer los cambios en los
    	// ID que pudieran haberse hecho para evitar problemas con los ID repetidos
    	final TriphaseData currentTd = new TriphaseData();
        for (final TriSign triSign : this.partialTd.getTriSigns()) {
        	if (this.docId.equals(FIReTriSignIdProcessor.unmake(triSign.getId()))) {
        		currentTd.addSignOperation(triSign);
        	}
        }

        // Insertamos los PKCS#1 en la sesion trifasica
        for (final String key : this.pkcs1s.keySet()) {
           	FIReTriHelper.addPkcs1ToTriSign(this.pkcs1s.get(key), key, currentTd);
        }

    	if (isInterrupted()) {
    		this.batchResult.setErrorResult(this.docId, BatchResult.NO_PROCESSED);
    		SIGNLOGGER.register(this.session, false,this.docId);
    		return;
    	}

        // Realizamos la postfirma para calcular la firma
    	byte[] signature;
    	try {
    		signature = FIReTriHelper.getPostSign(
    				this.signConfig.getCryptoOperation(),
    				this.signConfig.getFormat(),
    				this.algorithm,
    				this.signConfig.getExtraParams(),
    				this.signingCert,
    				data,
    				currentTd
    				);
    	}
    	catch (final FIReSignatureException e) {
    		LOGGER.log(
    				Level.WARNING, logF.f("Error durante la postfirma. Verifique el codigo de operacion (") + //$NON-NLS-1$
    						this.signConfig.getCryptoOperation() +
    						") y el formato (" + this.signConfig.getFormat() + ")", e //$NON-NLS-1$ //$NON-NLS-2$
    				);
    		this.batchResult.setErrorResult(this.docId, BatchResult.POSTSIGN_ERROR);
    		SIGNLOGGER.register(this.session, false, this.docId);
    		setFailed(true);
    		interrupt();
    		return;
    	}

    	if (isInterrupted()) {
    		this.batchResult.setErrorResult(this.docId, BatchResult.NO_PROCESSED);
    		SIGNLOGGER.register(this.session, false,this.docId);
    		return;
    	}

    	// Actualizamos/validamos la firma si se definio un formato de actualizacion
    	byte[] processedSignature = signature;
    	final String upgradeLevel = this.signConfig.getUpgrade();
    	if (upgradeLevel != null && !upgradeLevel.isEmpty()) {
    		try {
    			final SignatureValidator validator = SignatureValidatorBuilder.getSignatureValidator();
    			if (ServiceParams.UPGRADE_VERIFY.equalsIgnoreCase(upgradeLevel)) {
    				if (this.needValidation) {
    					LOGGER.info(logF.f("Validamos la firma: " + this.docId)); //$NON-NLS-1$
    					final VerifyResult verifyResult = validator.validateSignature(processedSignature, this.signConfig.getUpgradeConfig());
    					if (!verifyResult.isOk()) {
    		    			LOGGER.log(Level.SEVERE, logF.f("La firma generada no es valida: " + this.docId)); //$NON-NLS-1$
    		    			this.batchResult.setErrorResult(this.docId, BatchResult.INVALID_SIGNATURE);
    		    			SIGNLOGGER.register(this.session, false, this.docId);
    		    			setFailed(true);
    		    			interrupt();
    		    			return;
    					}
    				}
    				else {
    					LOGGER.info(logF.f("El proveedor es seguro y no es necesario validar la firma: " + this.docId)); //$NON-NLS-1$
    				}
    			}
    			else {
    				LOGGER.info(logF.f("Actualizamos la firma '" + this.docId + "' a: " + upgradeLevel)); //$NON-NLS-1$
    				UpgradeResult upgradeResult;
    				try {
    					upgradeResult = validator.upgradeSignature(processedSignature,
    							upgradeLevel, this.signConfig.getUpgradeConfig());
    				}
    				catch (final VerifyException e) {
    	    			LOGGER.log(Level.WARNING, "Se ha intentado actualizar una firma invalida con el docId: " + this.docId, e); //$NON-NLS-1$
    	    			this.batchResult.setErrorResult(this.docId, BatchResult.INVALID_SIGNATURE);
    	    			SIGNLOGGER.register(this.session, false, this.docId);
    	    			setFailed(true);
    	    			interrupt();
    	    			return;
    	    		}
    				processedSignature = upgradeResult.getResult();
    				if (this.batchResult.getSignConfig(this.docId) != null) {
    					this.batchResult.getSignConfig(this.docId).setUpgrade(upgradeResult.getFormat());
    				}
    			}
    		}
    		catch (final Exception e) {
    			LOGGER.log(Level.SEVERE, "Error al validar/actualizar la firma con docId: " + this.docId, e); //$NON-NLS-1$
    			this.batchResult.setErrorResult(this.docId, BatchResult.UPGRADE_ERROR);
    			SIGNLOGGER.register(this.session, false, this.docId);
    			setFailed(true);
    			interrupt();
    			return;
    		}
    	}

    	// Si se definio un gestor de documentos, procesamos la firma tal como este indique
    	byte[] result;
    	if (this.docManager != null) {
    		try {
    			result = this.docManager.storeDocument(this.docId.getBytes(StandardCharsets.UTF_8),
    					this.appId, processedSignature, this.signingCert, this.signConfig.getFormat(),
    					this.signConfig.getExtraParams());
    		}
    		catch (final Exception e) {
    			LOGGER.log(Level.SEVERE, "Error al postprocesar con el FIReDocumentManager la firma del documento: " + this.docId, e); //$NON-NLS-1$
    			this.batchResult.setErrorResult(this.docId, BatchResult.ERROR_SAVING_DATA);
    			SIGNLOGGER.register(this.session, false, this.docId);
    			setFailed(true);
    			interrupt();
    			return;
    		}
    	}
    	else {
    		result = processedSignature;
    	}

    	// Guardamos el resultado pisando el documento de datos
    	try {
    		TempFilesHelper.storeTempData(docFilename, result);
    	}
    	catch (final Exception e) {
    		LOGGER.severe("Error al almacenar la firma en el directorio temporal: " + e); //$NON-NLS-1$
    		this.batchResult.setErrorResult(this.docId, BatchResult.ERROR_SAVING_DATA);
    		SIGNLOGGER.register(this.session, false, this.docId);
    		setFailed(true);
    		interrupt();
    		return;
		}

    	if (isInterrupted()) {
    		this.batchResult.setErrorResult(this.docId, BatchResult.NO_PROCESSED);
    		SIGNLOGGER.register(this.session, false, this.docId);
    		return;
    	}

    	// Registrar en el listado de resultados el de la operacion
    	this.batchResult.setSuccessResult(this.docId);
	}
}