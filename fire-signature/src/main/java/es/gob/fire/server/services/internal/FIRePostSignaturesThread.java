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
import es.gob.fire.server.services.AfirmaUpgrader;
import es.gob.fire.server.services.DocInfo;
import es.gob.fire.server.services.FIReTriHelper;
import es.gob.fire.server.services.FIReTriSignIdProcessor;
import es.gob.fire.server.services.UpgradeException;
import es.gob.fire.server.services.statistics.SignatureRecorder;
import es.gob.fire.upgrade.UpgradeResult;

class FIRePostSignaturesThread extends ConcurrentProcessThread {

	private static final SignatureRecorder SIGNLOGGER = SignatureRecorder.getInstance();
	private static final Logger LOGGER = Logger.getLogger(FIRePostSignaturesThread.class.getName());

	private final String appId;

	private final String docId;

	private final String algorithm;

	private final SignBatchConfig signConfig;

	private final X509Certificate signingCert;

	private final BatchResult batchResult;

	private final Map<String, byte[]> pkcs1s;

	private final TriphaseData partialTd;

	private final FIReDocumentManager docManager;

	private final FireSession sesion;

	/**
	 * Construye un hilo que se encargar&aacute; de componer la firma electr&oacute;nica
	 * realizada con el certificado en la nube y la actualizar&aacute; al formato avanzado
	 * que corresponda (si se configura).
	 * @param appId Identificador de la aplicaci&oacute;n.
	 * @param docId Identificador del documento.
	 * @param batchResult Objeto con todos los resultados de cada firma indivdual.
	 * @param algorithm Algoritmo de firma.
	 * @param signConfig Configuraci&oacute;n de firma.
	 * @param signingCert Certificado de firma.
	 * @param pkcs1s Conjunto de PKCS#1 de la firma.
	 * @param partialTd Datos parciales de la firma.
	 * @param docManager Gestor de documentos que realizar&aacute; el tratamiento de la firma.
	 *                   del hilo en caso de detectar un error en alguna de las firmas del lote.
	 * @param session Sesi&oacute;n con los datos de la transacci&oacute;n. */
	public FIRePostSignaturesThread(final String appId,
			                        final String docId,
			                        final BatchResult batchResult,
			                        final String algorithm,
			                        final SignBatchConfig signConfig,
			                        final X509Certificate signingCert,
			                        final Map<String, byte[]> pkcs1s,
			                        final TriphaseData partialTd,
			                        final FIReDocumentManager docManager,
			                        final FireSession session) {
		this.appId = appId;
		this.docId = docId;
		this.batchResult = batchResult;
		this.algorithm = algorithm;
		this.signConfig = signConfig;
		this.signingCert = signingCert;
		this.pkcs1s = pkcs1s;
		this.partialTd = partialTd;
		this.docManager = docManager;
		this.sesion = session;
	}

	@Override
	public void run() {

    	if (this.batchResult.isSignFailed(this.docId)) {
    		setFailed(true);
    		final DocInfo docInf = this.batchResult.getDocInfo(this.docId);
        	if(docInf != null) {
        		this.sesion.setAttribute(
    				ServiceParams.SESSION_PARAM_DOCSIZE,
    				Long.valueOf(docInf.getSize())
				);
        	}
    		SIGNLOGGER.register(this.sesion, false,this.docId);
    		interrupt();
    		return;
		}

    	// Ahora y antes de las operaciones pesadas, comprobaremos si debemos detenernos
    	// al detectar que nos han interrumpido desde afuera
    	if (isInterrupted()) {
    		this.batchResult.setErrorResult(this.docId, BatchResult.NO_PROCESSED);
    		//SIGNLOGGER.log(this.sesion, false);
    		//TRANSLOGGER.log(this.sesion, false);
    		return;
    	}

    	final String docFilename = this.batchResult.getDocumentReference(this.docId);

    	final byte[] data;
        try {
        	data = TempFilesHelper.retrieveAndDeleteTempData(docFilename);
        }
        catch (final Exception e) {
        	LOGGER.warning("No se encuentran los datos a firmar: " + e); //$NON-NLS-1$
        	this.batchResult.setErrorResult(this.docId, BatchResult.DATA_NOT_FOUND);

        	final DocInfo docInf = this.batchResult.getDocInfo(this.docId);
        	if(docInf != null) {
        		this.sesion.setAttribute(
    				ServiceParams.SESSION_PARAM_DOCSIZE,
    				Long.valueOf(docInf.getSize())
				);
        	}

        	SIGNLOGGER.register(this.sesion, false, this.docId);

        	setFailed(true);
        	interrupt();
    		return;
		}

    	if (isInterrupted()) {
    		this.batchResult.setErrorResult(this.docId, BatchResult.NO_PROCESSED);
    		SIGNLOGGER.register(this.sesion, false, this.docId);

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
    		SIGNLOGGER.register(this.sesion, false,this.docId);
    		//TRANSLOGGER.log(this.sesion, false);
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
    				Level.WARNING, "Error durante la postfirma. Verifique el codigo de operacion (" + //$NON-NLS-1$
    						this.signConfig.getCryptoOperation() +
    						") y el formato (" + this.signConfig.getFormat() + ")", e //$NON-NLS-1$ //$NON-NLS-2$
    				);
    		this.batchResult.setErrorResult(this.docId, BatchResult.POSTSIGN_ERROR);
    		SIGNLOGGER.register(this.sesion, false, this.docId);
    		//TRANSLOGGER.log(this.sesion, false);
    		setFailed(true);
    		interrupt();
    		return;
    	}

    	if (isInterrupted()) {
    		this.batchResult.setErrorResult(this.docId, BatchResult.NO_PROCESSED);
    		SIGNLOGGER.register(this.sesion, false,this.docId);
    		//TRANSLOGGER.log(this.sesion, false);
    		return;
    	}

    	// Actualizamos la firma si se definio un formato de actualizacion
    	try {
			final UpgradeResult upgradeResult = AfirmaUpgrader.upgradeSignature(signature, this.signConfig.getUpgrade());
			signature = upgradeResult.getResult();
			if (this.batchResult.getSignConfig(this.docId) != null) {
				this.batchResult.getSignConfig(this.docId).setUpgrade(upgradeResult.getFormat());
			}
    	}
    	catch (final UpgradeException e) {
    		LOGGER.log(Level.SEVERE, "Error al actualizar la firma con docId: " + this.docId, e); //$NON-NLS-1$
    		this.batchResult.setErrorResult(this.docId, BatchResult.UPGRADE_ERROR);

    		SIGNLOGGER.register(this.sesion, false, this.docId);

    		setFailed(true);
    		interrupt();
    		return;
		}

    	// Si se definio un gestor de documentos, procesamos la firma tal como este indique
    	byte[] result;
    	if (this.docManager != null) {
    		try {
    			result = this.docManager.storeDocument(this.docId.getBytes(StandardCharsets.UTF_8),
    					this.appId, signature, this.signingCert, this.signConfig.getFormat(),
    					this.signConfig.getExtraParams());
    		}
    		catch (final Exception e) {
    			LOGGER.log(Level.SEVERE, "Error al postprocesar con el FIReDocumentManager la firma del documento: " + this.docId, e); //$NON-NLS-1$
    			this.batchResult.setErrorResult(this.docId, BatchResult.ERROR_SAVING_DATA);
    			SIGNLOGGER.register(this.sesion, false, this.docId);
    			//TRANSLOGGER.log(this.sesion, false);
    			setFailed(true);
    			interrupt();
    			return;
    		}
    	}
    	else {
    		result = signature;
    	}

    	// Guardamos el resultado pisando el documento de datos
    	try {
    		TempFilesHelper.storeTempData(docFilename, result);
    	}
    	catch (final Exception e) {
    		LOGGER.severe("Error al almacenar la firma en el directorio temporal: " + e); //$NON-NLS-1$
    		this.batchResult.setErrorResult(this.docId, BatchResult.ERROR_SAVING_DATA);
    		SIGNLOGGER.register(this.sesion, false, this.docId);
    		//TRANSLOGGER.log(this.sesion, false);
    		setFailed(true);
    		interrupt();
    		return;
		}

    	if (isInterrupted()) {
    		this.batchResult.setErrorResult(this.docId, BatchResult.NO_PROCESSED);
    		SIGNLOGGER.register(this.sesion, false, this.docId);
    		//TRANSLOGGER.log(this.sesion, false);
    		return;
    	}


    	// Registrar en el listado de resultados el de la operacion
    	this.batchResult.setSuccessResult(this.docId);
	}
}