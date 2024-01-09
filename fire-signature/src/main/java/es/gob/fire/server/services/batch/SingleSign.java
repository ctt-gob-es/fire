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

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import es.gob.afirma.core.AOException;
import es.gob.afirma.core.misc.AOUtil;
import es.gob.afirma.core.misc.LoggerUtil;
import es.gob.afirma.core.signers.TriphaseData;
import es.gob.fire.server.services.batch.ProcessResult.Result;
import es.gob.fire.server.services.batch.SingleSignConstants.SignFormat;
import es.gob.fire.server.services.batch.SingleSignConstants.SignSubOperation;
import es.gob.fire.server.services.internal.PropertiesUtils;
import es.gob.fire.server.services.internal.TempDocumentsManager;

/** Firma electr&oacute;nica &uacute;nica dentro de un lote.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
public final class SingleSign {

	protected Properties extraParams;

	protected String dataRef;

	protected SignFormat format;

	protected String id;

	protected SignSubOperation subOperation;

	private ProcessResult processResult = new ProcessResult(ProcessResult.Result.NOT_STARTED, null, false);

	private static final String PROP_ID = "SignatureId"; //$NON-NLS-1$

	private static final String JSON_ATTRIBUTE_ID = "Id"; //$NON-NLS-1$

	private static final String JSON_ELEMENT_DATAREFERENCE = "datareference"; //$NON-NLS-1$
	private static final String JSON_ELEMENT_FORMAT = "format"; //$NON-NLS-1$
	private static final String JSON_ELEMENT_SUBOPERATION = "suboperation"; //$NON-NLS-1$
	private static final String JSON_ELEMENT_EXTRAPARAMS = "extraparams"; //$NON-NLS-1$

	static final Logger LOGGER = Logger.getLogger(SingleSign.class.getName());

	private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

	private static final MessageDigest MD;
	static {
		try {
			MD = MessageDigest.getInstance("SHA-1"); //$NON-NLS-1$
		}
		catch (final Exception e) {
			throw new IllegalStateException(
				"No se ha podido cargar el motor de huellas para SHA-1: " + e, e //$NON-NLS-1$
			);
		}
	}

	/** Crea una definici&oacute;n de tarea de firma electr&oacute;nica &uacute;nica.
	 * @param id Identificador de la firma. */
	SingleSign(final String id) {
		this.id = id;
	}

	/** Crea una definici&oacute;n de tarea de firma electr&oacute;nica &uacute;nica.
	 * @param id Identificador de la firma.
	 * @param dataSrc Datos a firmar.
	 * @param fmt Formato de firma.
	 * @param subOp Tipo de firma a realizar.
	 * @param xParams Opciones adicionales de la firma.
	 * @param ss Objeto para guardar la firma una vez completada. */
	public SingleSign(final String id,
			          final String dataSrc,
			          final SignFormat fmt,
			          final SignSubOperation subOp,
			          final Properties xParams) {

		if (dataSrc == null) {
			throw new IllegalArgumentException(
				"El origen de los datos a firmar no puede ser nulo" //$NON-NLS-1$
			);
		}

		if (fmt == null) {
			throw new IllegalArgumentException(
				"El formato de firma no puede ser nulo" //$NON-NLS-1$
			);
		}

		this.dataRef = dataSrc;
		this.format = fmt;

		this.id = id;

		this.subOperation = subOp;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("{\n"); //$NON-NLS-1$
		sb.append(":\""); //$NON-NLS-1$
		sb.append(JSON_ATTRIBUTE_ID);
		sb.append(":\""); //$NON-NLS-1$
		sb.append(this.id);
		sb.append("\" , \n\""); //$NON-NLS-1$
		sb.append(JSON_ELEMENT_DATAREFERENCE);
		sb.append("\":\""); //$NON-NLS-1$
		sb.append(this.dataRef);
		sb.append("\",\n"); //$NON-NLS-1$
		sb.append("\""); //$NON-NLS-1$
		sb.append(JSON_ELEMENT_FORMAT);
		sb.append("\":\""); //$NON-NLS-1$
		sb.append(this.format);
		sb.append("\",\n"); //$NON-NLS-1$
		sb.append("\""); //$NON-NLS-1$
		sb.append(JSON_ELEMENT_SUBOPERATION);
		sb.append("\":\""); //$NON-NLS-1$
		sb.append(this.subOperation);
		sb.append("\",\n"); //$NON-NLS-1$
		sb.append("\""); //$NON-NLS-1$
		sb.append(JSON_ELEMENT_EXTRAPARAMS);
		sb.append("\":\""); //$NON-NLS-1$
		sb.append(PropertiesUtils.properties2Base64(this.extraParams));
		sb.append("\",\n"); //$NON-NLS-1$
		sb.append("\""); //$NON-NLS-1$
		sb.append("\n}\n"); //$NON-NLS-1$

		return sb.toString();
	}

	/** Realiza el proceso de prefirma, incluyendo la descarga u obtenci&oacute;n de datos.
	 * @param certChain Cadena de certificados del firmante.
	 * @param algorithm Algoritmo de firma.
	 * @param docManager Gestor de documentos con el que procesar el lote.
	 * @param docCacheManager Gestor para el guardado de datos en cach&eacute;.
	 * @return Objeto JSON con los datos trif&aacute;sicos.
	 * @throws AOException Si hay problemas en la propia firma electr&oacute;nica.
	 * @throws IOException Si hay problemas en la obtenci&oacute;n, tratamiento o gradado de datos. */
	TriphaseData doPreProcess(final X509Certificate[] certChain,
			            final SingleSignConstants.SignAlgorithm algorithm) throws IOException,
			                                                                      AOException {
		return SingleSignPreProcessor.doPreProcess(this, certChain, algorithm);
	}

	/** Obtiene la tarea de preproceso de firma para ser ejecutada en paralelo.
	 * @param certChain Cadena de certificados del firmante.
	 * @param algorithm Algoritmo de firma.
	 * @param docManager Gestor de documentos con el que procesar el lote.
	 * @param docCacheManager Gestor para el guardado de datos en cach&eacute;.
	 * @return Tarea de preproceso de firma para ser ejecutada en paralelo. */
	Callable<PreProcessResult> getPreProcessCallable(final X509Certificate[] certChain,
                                                  final SingleSignConstants.SignAlgorithm algorithm) {
		return new PreProcessCallable(this, certChain, algorithm);
	}

	/** Realiza el proceso de postfirma, incluyendo la subida o guardado de datos.
	 * @param certChain Cadena de certificados del firmante.
	 * @param td Datos trif&aacute;sicos relativos <b>&uacute;nicamente</b> a esta firma.
	 *           Debe serializarse como un JSON con esta forma (ejemplo):
	 *<pre>
	 * {
	 * "format":"PAdES",
	 * "signs":
	 * [{
	 *	"signinfo":[{
 	 *		"Id":"7725374e-728d-4a33-9db9-3a4efea4cead",
	 *		"params":
	 *			[{
	 *			"PRE":"PGGYzMC1iOTub3JnL1RSLzIwMDEvUkVDLXh",
	 *			"ENCODING":"UTF-8",
	 *			"NEED_PRE":"true",
	 *			"BASE":"PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGl"}]}]},
	 *{
	 *	"signinfo":[{
 	 *		"Id":"93d1531c-cd32-4c8e-8cc8-1f1cfe66f64a",
	 *		"params":
	 *			[{
	 *			"PRE":"MYIBAzAYBgkqhkiG9w0BCQMxCwYJKoZIhv",
	 *			"NEED_PRE":"true",
	 *			"TIME":"1621423575727",
	 *			"PID":"Wzw5MjBjODdmYmE4ZTEyZTM0YjU2OWUzOW"}]}]}]
	 * }
	 * </pre>
	 * @param algorithm Algoritmo de firma.
	 * @param batchId Identificador del lote de firma.
	 * @param docManager Gestor de documentos con el que procesar el lote.
	 * @param docCacheManager Gestor para la carga de datos desde cach&eacute;.
	 * @throws AOException Si hay problemas en la propia firma electr&oacute;nica.
	 * @throws IOException Si hay problemas en la obtenci&oacute;n, tratamiento o gradado de datos.
	 * @throws NoSuchAlgorithmException Si no se soporta alg&uacute;n algoritmo necesario. */
	void doPostProcess(final X509Certificate[] certChain,
			                  final TriphaseData td,
			                  final SingleSignConstants.SignAlgorithm algorithm,
			                  final String batchId) throws IOException,
			                                               AOException,
			                                               NoSuchAlgorithmException {
		SingleSignPostProcessor.doPostProcess(
			this, certChain, td, algorithm, batchId
			);
	}

	/** Obtiene la tarea de postproceso de firma para ser ejecutada en paralelo.
	 * @param certChain Cadena de certificados del firmante.
	 * @param td Datos trif&aacute;sicos relativos <b>&uacute;nicamente</b> a esta firma.
	 *           Debe serializarse como un JSON con esta forma (ejemplo):
	 *           <pre>
	 *	{
 	 *	"signs":[
	 *		{	"id":"CADES-001",
 	 *			"result":"DONE_AND_SAVED",
 	 *			"description":""
	 *		},
	 *		{	"id":"XADES-002",
 	 *			"result":"DONE_AND_SAVED",
 	 *			"description":""
	 *		},
	 *		{	"id":"PADES-003",
 	 *			"result":"DONE_AND_SAVED",
 	 *			"description":""
	 *		}
	 *	]
	 * }
	 *           </pre>
	 * @param algorithm Algoritmo de firma.
	 * @param batchId Identificador del lote de firma.
	 * @param docManager Gestor de documentos con el que procesar el lote.
	 * @param docCacheManager Gestor para la carga de datos desde cach&eacute;.
	 * @return Tarea de postproceso de firma para ser ejecutada en paralelo. */
	Callable<ResultSingleSign> getPostProcessCallable(final X509Certificate[] certChain,
			                                                          final TriphaseData td,
			                                                          final SingleSignConstants.SignAlgorithm algorithm,
			                                                          final String batchId) {
		return new PostProcessCallable(this, certChain, td, algorithm, batchId);
	}

	Callable<ResultSingleSign> getSaveCallableJSON(final String batchId) {

		return new JSONSaveCallable(this, batchId);
	}

	public Properties getExtraParams() {
		return this.extraParams;
	}


	public void setExtraParams(final Properties extraParams) {
		// El identificador de la firma debe transmitirse al firmador trifasico a traves
		// de los extraParams para que este lo utilice y asi podamos luego asociar la
		// firma con los datos a los que corresponden
		this.extraParams = extraParams != null ? extraParams : new Properties();
		this.extraParams.put(PROP_ID, getId());
	}

	public String getDataRef() {
		return this.dataRef;
	}

	public void setDataRef(final String dataRef) {
		this.dataRef = dataRef;
	}

	public String getId() {
		return this.id;
	}

	public SignSubOperation getSubOperation() {
		return this.subOperation;
	}

	public void setSubOperation(final SignSubOperation subOperation) {
		this.subOperation = subOperation;
	}

	public ProcessResult getProcessResult() {
		this.processResult.setId(getId());
		return this.processResult;
	}

	public void setProcessResult(final ProcessResult processResult) {
		this.processResult = processResult;
	}

	public SignFormat getFormat() {
		return this.format;
	}

	public void setFormat(final SignFormat format) {
		this.format = format;
	}

	public String getName(final String batchId) {
		return AOUtil.hexify(MD.digest(this.id.getBytes(DEFAULT_CHARSET)), false) + "." + batchId; //$NON-NLS-1$
	}

	/**
	 * Recupera los datos que se deben procesar.
	 * @return Datos.
	 * @throws IOException Cuando no se encuentran los datos o no pueden leerse.
	 */
	public byte[] getData() throws IOException {
		return TempDocumentsManager.retrieveDocument(this.dataRef);
	}

	/**
	 * Guarda los datos en disco.
	 * @param dataToSave Datos a guardar.
	 * @throws IOException Cuando no pueden guardarse los datos.
	 */
	void save(final byte[] dataToSave) throws IOException {
		TempDocumentsManager.storeDocument(this.dataRef, dataToSave, false, null);  //TODO: Pasar parametros para logs
	}

	/**
	 * Devuelve el nombre con el que referenciar a la firma en base al identificador del
	 * lote indicado.
	 * @param batchId Identificador de lote.
	 * @return Nombre que asignar a la firma.
	 */


	static class PreProcessCallable implements Callable<PreProcessResult> {
		private final SingleSign ss;
		private final X509Certificate[] certChain;
		private final SingleSignConstants.SignAlgorithm algorithm;

		public PreProcessCallable(final SingleSign ss, final X509Certificate[] certChain,
                final SingleSignConstants.SignAlgorithm algorithm) {
			this.ss = ss;
			this.certChain = certChain;
			this.algorithm = algorithm;
		}

		@Override
		public PreProcessResult call() throws Exception {

			PreProcessResult result;
			try {
				final TriphaseData presignature = SingleSignPreProcessor.doPreProcess(
						this.ss, this.certChain, this.algorithm);
				result = new PreProcessResult(presignature);
			}
			catch (final Exception e) {
				LOGGER.log(Level.WARNING, "Error en la prefirma del documento con referencia " + LoggerUtil.getTrimStr(this.ss.getDataRef()), e); //$NON-NLS-1$
				final ProcessResult errorResult = new ProcessResult(Result.ERROR_PRE, e.getMessage());
				errorResult.setId(this.ss.getId());
				final ResultSingleSign singleResult = new ResultSingleSign(this.ss.getId(), false, errorResult);
				result = new PreProcessResult(singleResult);

			}

			return result;
		}
	}

	static class PostProcessCallable implements Callable<ResultSingleSign> {

		private final SingleSign ss;
		private final X509Certificate[] certChain;
		private final TriphaseData td;
		private final SingleSignConstants.SignAlgorithm algorithm;
		private final String batchId;

		public PostProcessCallable(final SingleSign ss, final X509Certificate[] certChain,
                final TriphaseData td, final SingleSignConstants.SignAlgorithm algorithm,
                final String batchId) {
			this.ss = ss;
			this.certChain = certChain;
			this.td = td;
			this.algorithm = algorithm;
			this.batchId = batchId;
		}

		@Override
		public ResultSingleSign call() {
			try {
				SingleSignPostProcessor.doPostProcess(this.ss, this.certChain, this.td,
														this.algorithm, this.batchId);
			}
			catch(final Exception e) {
				LOGGER.log(Level.WARNING, "Error en la postfirma del documento con referencia " + LoggerUtil.getTrimStr(this.ss.getDataRef()), e); //$NON-NLS-1$
				final ProcessResult result = new ProcessResult(Result.ERROR_POST, e.getMessage());
				return new ResultSingleSign(this.ss.getId(), false, result);
			}
			return new ResultSingleSign(this.ss.getId(), true, ProcessResult.PROCESS_RESULT_OK_UNSAVED);
		}
	}

	static class JSONSaveCallable implements Callable<ResultSingleSign> {

		private final SingleSign ss;
		private final String batchId;

		public JSONSaveCallable(final SingleSign ss, final String batchId) {
			this.ss = ss;
			this.batchId = batchId;
		}

		@Override
		public ResultSingleSign call() {
			try {
				final byte[] dataToSave = TempDocumentsManager.retrieveDocument(this.ss.getName(this.batchId));
				TempDocumentsManager.storeDocument(this.ss.dataRef, dataToSave, false, null);  //TODO: Pasar parametros para logs
			}
			catch(final Exception e) {
				LOGGER.log(Level.WARNING, "No se puede almacenar la firma del documento: " + this.ss.getId(), e); //$NON-NLS-1$
				final ProcessResult result = new ProcessResult(Result.DONE_BUT_ERROR_SAVING, "Error al almacenar la firma del documento"); //$NON-NLS-1$
				return new ResultSingleSign(this.ss.getId(), false, result);
			}
			return new ResultSingleSign(this.ss.getId(), true, ProcessResult.PROCESS_RESULT_DONE_SAVED);
		}

	}
}
