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

import javax.servlet.http.HttpServletResponse;

import es.gob.afirma.core.misc.AOUtil;
import es.gob.afirma.core.misc.Base64;
import es.gob.fire.alarms.Alarm;
import es.gob.fire.server.document.FIReDocumentManager;
import es.gob.fire.server.document.FireDocumentManagerBase;
import es.gob.fire.server.services.DocInfo;
import es.gob.fire.server.services.FIReDocumentManagerFactory;
import es.gob.fire.server.services.FIReError;
import es.gob.fire.server.services.RequestParameters;
import es.gob.fire.server.services.Responser;
import es.gob.fire.signature.ConfigManager;
import es.gob.fire.upgrade.UpgraderUtils;

/**
 * Manejador que gestiona las peticiones para agregar nuevos documentos a un lote de firma.
 */
public class AddDocumentBatchManager {

	private static final Logger LOGGER = Logger.getLogger(AddDocumentBatchManager.class.getName());

    /**
     * Agrega un nuevo documento a un lote de firma.
	 * @param params Par&aacute;metros extra&iacute;dos de la petici&oacute;n.
	 * @param trAux Informaci&oacute;n auxiliar de la transacci&oacute;n.
	 * @param response Respuesta con el resultado de agregar el documento al lote.
	 * @throws IOException Cuando se produce un error de lectura o env&iacute;o de datos.
     */
    public static void addDocument(final RequestParameters params, final TransactionAuxParams trAux, final HttpServletResponse response)
    		throws IOException {

    	// Recogemos los parametros proporcionados en la peticion
    	final String appId			= params.getParameter(ServiceParams.HTTP_PARAM_APPLICATION_ID);
    	final String transactionId	= params.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID);
    	final String subjectId		= params.getParameter(ServiceParams.HTTP_PARAM_SUBJECT_ID);
    	final String docId			= params.getParameter(ServiceParams.HTTP_PARAM_DOCUMENT_ID);
    	final String dataB64		= params.getParameter(ServiceParams.HTTP_PARAM_DATA);

		final LogTransactionFormatter logF = trAux.getLogFormatter();

    	// Comprobamos que se hayan proporcionado los parametros indispensables
    	if (transactionId == null || transactionId.isEmpty()) {
    		LOGGER.warning(logF.f("No se ha proporcionado el identificado de la transaccion")); //$NON-NLS-1$
    		Responser.sendError(response, FIReError.PARAMETER_TRANSACTION_ID_NEEDED);
    		return;
    	}

    	if (docId == null || docId.isEmpty()) {
    		LOGGER.warning(logF.f("No se ha proporcionado el ID del documento")); //$NON-NLS-1$
        	Responser.sendError(response, FIReError.PARAMETER_DOCUMENT_ID_NEEDED);
    		return;
    	}

    	// Comprobamos si se ha establecido configuracion particular de fichero o para su postprocesado
    	Properties documentConfig = null;
    	if (params.containsKey(ServiceParams.HTTP_PARAM_CONFIG)) {
    		try {
    			documentConfig = AOUtil.base642Properties(params.getParameter(ServiceParams.HTTP_PARAM_CONFIG));
    		}
    		catch (final IOException e) {
    			LOGGER.warning(logF.f("Se ha proporcionado una configuracion de la operacion mal formada: ") + e); //$NON-NLS-1$
    			Responser.sendError(response, FIReError.PARAMETER_CONFIG_TRANSACTION_INVALID);
    			return;
    		}
    	}

    	// Obtenemos la configuracion particular si existe
    	SignBatchConfig config;
    	try {
    		config = getParticularConfig(params, documentConfig);
    	}
    	catch (final IOException e) {
    		LOGGER.warning(logF.f("Se ha proporcionado un extraParam mal formado: ") + e); //$NON-NLS-1$
    		Responser.sendError(response, FIReError.PARAMETER_SIGNATURE_PARAMS_INVALID);
    		return;
		}

    	// Informacion del documento
    	final DocInfo docInfo = DocInfo.extractDocInfo(documentConfig);

		LOGGER.fine(logF.f("Peticion bien formada")); //$NON-NLS-1$

    	final FireSession session = SessionCollector.getFireSession(transactionId, subjectId, null, false, true, trAux);
    	if (session == null) {
    		LOGGER.warning(logF.f("La transaccion no se ha inicializado o ha caducado")); //$NON-NLS-1$
    		Responser.sendError(response, FIReError.INVALID_TRANSACTION);
    		return;
    	}

        // Si se definio un DocumentManager, lo usaremos
        final FIReDocumentManager documentManager = (FIReDocumentManager) session.getObject(ServiceParams.SESSION_PARAM_DOCUMENT_MANAGER);
    	if (FIReDocumentManagerFactory.isDefaultDocumentManager(documentManager) && (dataB64 == null || dataB64.isEmpty())) {
    		LOGGER.warning(logF.f("No se ha proporcionado el documento a firmar ni un gestor de documentos del que recuperarlo")); //$NON-NLS-1$
    		Responser.sendError(response, FIReError.PARAMETER_DATA_TO_SIGN_NEEDED);
    		return;
    	}

    	final String format = config != null ? config.getFormat() :
    		session.getString(ServiceParams.SESSION_PARAM_FORMAT);
    	final Properties extraParams = config != null ? config.getExtraParams() :
    		(Properties) session.getObject(ServiceParams.SESSION_PARAM_EXTRA_PARAM);

    	byte[] docReferenceId;
        if (dataB64 != null && !dataB64.isEmpty()) {
        	try {
        		docReferenceId = Base64.decode(dataB64, true);
        	}
        	catch (final Exception e) {
        		LOGGER.warning(logF.f("El documento enviado a firmar no esta bien codificado: " + e)); //$NON-NLS-1$
        		Responser.sendError(response, FIReError.PARAMETER_DATA_TO_SIGN_INVALID);
        		return;
        	}
        }
        else {
        	docReferenceId = docId.getBytes(StandardCharsets.UTF_8);
        }

    	byte[] data;
    	try {
    		// Por motivos de compatibilidad, mantenemos el uso de las funciones de la interfaz en
    		// base a la que se construyen los DocumentManager, pero, si es posible, se utilizara
    		// la funcion equivalente de la implementacion de FireDocumentManagerBase, que recibe
    		// mas parametros
    		if (documentManager instanceof FireDocumentManagerBase) {
    			data = ((FireDocumentManagerBase) documentManager).getDocument(docReferenceId, transactionId, appId, format, extraParams);
    		}
    		else {
    			data = documentManager.getDocument(docReferenceId, appId, format, extraParams);
    		}
    	}
    	catch (final Exception e) {
    		LOGGER.log(Level.SEVERE, logF.f("Error en la carga de los datos a agregar al lote"), e); //$NON-NLS-1$
    		AlarmsManager.notify(Alarm.CONNECTION_DOCUMENT_MANAGER, documentManager.getClass().getCanonicalName());
    		Responser.sendError(response, FIReError.PARAMETER_DATA_TO_SIGN_NOT_FOUND);
    		return;
    	}
    	if (data == null) {
    		LOGGER.warning(logF.f("No se han obtenido los datos para agregarlos al lote de firma")); //$NON-NLS-1$
    		Responser.sendError(response, FIReError.PARAMETER_DATA_TO_SIGN_NOT_FOUND);
    		return;
    	}

    	// Registramos el tamano del documento con fines estadisticos
    	session.setAttribute(ServiceParams.SESSION_PARAM_DOCSIZE, Long.valueOf(data.length));
    	
    	// Obtenemos el tamano de la transaccion
		Long transactionSize = new Long(0);
		final Object transactionSizeObject = session.getObject(ServiceParams.SESSION_PARAM_TRANSACTION_SIZE);
		if (transactionSize != null) {
			transactionSize = (Long) transactionSizeObject;
			if (transactionSize == null) {
				transactionSize = new Long(0);
			}
		}
    	
		//Sumamos el tamano del documento actual al tamano de la transaccion
		transactionSize = transactionSize + Long.valueOf(data.length);
		
    	session.setAttribute(ServiceParams.SESSION_PARAM_TRANSACTION_SIZE, transactionSize);

    	// Recuperamos el listado de documentos del lote o lo creamos si este es el primero
        BatchResult batchResult = (BatchResult) session.getObject(ServiceParams.SESSION_PARAM_BATCH_RESULT);
        if (batchResult == null) {
        	batchResult = new BatchResult();
        }

        if (batchResult.hasDocument(docId)) {
        	LOGGER.warning(logF.f("El identificador de documento indicado ya existe en el lote")); //$NON-NLS-1$
    		Responser.sendError(response, FIReError.BATCH_DUPLICATE_DOCUMENT);
        	return;
        }

        // Almacenamos el documento en disco, registramos su ID
        //final int docNumber = ((Integer) configOperation.getOrDefault(ServiceParams.SESSION_PARAM_BATCH_NUM_DOCS, Integer.valueOf(0))).intValue() + 1;
        final int maxDocuments = ConfigManager.getBatchMaxDocuments();
        if (maxDocuments != ConfigManager.UNLIMITED_NUM_DOCUMENTS && batchResult.documentsCount() >= maxDocuments) {
        	LOGGER.warning(logF.f("Se ha excedido el numero maximo de documentos permitido en el lote")); //$NON-NLS-1$
        	Responser.sendError(response, FIReError.BATCH_NUM_DOCUMENTS_EXCEEDED);
        	return;
        }

        final String filename = transactionId + "_" + docId; //$NON-NLS-1$
        try {
        	TempDocumentsManager.storeDocument(filename, data, true);
        	docInfo.setSize(data.length);
        }
        catch (final Exception e) {
        	LOGGER.severe(logF.f("Error en el guardado temporal de los datos a firmar: " + e)); //$NON-NLS-1$
        	Responser.sendError(response, FIReError.INTERNAL_ERROR);
        	return;
        }

        // Si se establecio una configuracion particular para esta firma,
        // introducimos el formato de actualizacion, como parte de los
        // extraParams para que el Cliente @firma (que no soporta la actualizacion
        // en los lotes) lo pueda identificar
        if (config != null) {
        	final String upgrade = config.getUpgrade();
        	if (upgrade == null) {
        		if (config.getExtraParams() != null) {
        			config.getExtraParams().remove(MiniAppletHelper.PARAM_UPGRADE_FORMAT);
        		}
        	}
        	else {
        		if (config.getExtraParams() == null) {
        			config.setExtraParamsB64(""); //$NON-NLS-1$
        		}
        		config.getExtraParams().setProperty(MiniAppletHelper.PARAM_UPGRADE_FORMAT, config.getUpgrade());
        	}
        }

        batchResult.addDocument(docId, filename, config, docInfo);

        session.setAttribute(ServiceParams.SESSION_PARAM_BATCH_RESULT, batchResult);
        SessionCollector.commit(session, trAux);

        LOGGER.info(logF.f("Se devuelve el resultado de la operacion")); //$NON-NLS-1$

        Responser.sendResult(response, Boolean.TRUE.toString().getBytes(StandardCharsets.UTF_8));
    }

	/**
     * Extrae de la peticion la configuraci&oacute;n de firma que se debe
     * aplicar a este documento particular.
     * @param request Solicitud de agregar documento al lote.
     * @param docConfig Configuraci&oacute;n particular establecida para el procesado
     * del fichero. No es configuraci&oacute;n para la generaci&oacute;n de la firma,
     * aunque puede serlo para la validaci&oacute;n y mejora.
     * @return Configuraci&oacute;n de firma particular o {@code null} si no
     * se defini&oacute;.
     * @throws IOException Si se configuran extraParams y no son un Base 64 bien formado.
     */
	private static SignBatchConfig getParticularConfig(final RequestParameters params,
			final Properties docConfig) throws IOException {

		// Comprobamos si se ha establecido configuracion particular
		if (!params.containsKey(ServiceParams.HTTP_PARAM_CRYPTO_OPERATION) &&
				!params.containsKey(ServiceParams.HTTP_PARAM_FORMAT) &&
				!params.containsKey(ServiceParams.HTTP_PARAM_EXTRA_PARAM) &&
				!params.containsKey(ServiceParams.HTTP_PARAM_UPGRADE)) {
			return null;
		}

		final SignBatchConfig config = new SignBatchConfig();
		config.setCryptoOperation(params.getParameter(ServiceParams.HTTP_PARAM_CRYPTO_OPERATION));
		config.setFormat(params.getParameter(ServiceParams.HTTP_PARAM_FORMAT));
		config.setExtraParamsB64(params.getParameter(ServiceParams.HTTP_PARAM_EXTRA_PARAM));
		config.setUpgrade(params.getParameter(ServiceParams.HTTP_PARAM_UPGRADE));
		config.setUpgradeConfig(UpgraderUtils.extractUpdaterProperties(docConfig));

		return config;
	}
}
