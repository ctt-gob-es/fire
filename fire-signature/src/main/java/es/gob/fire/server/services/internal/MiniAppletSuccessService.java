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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import es.gob.afirma.core.AOException;
import es.gob.afirma.core.misc.Base64;
import es.gob.fire.server.services.FIReError;
import es.gob.fire.server.services.LogUtils;
import es.gob.fire.server.services.RequestParameters;
import es.gob.fire.server.services.Responser;
import es.gob.fire.signature.ConfigManager;

/**
 * Servicio para procesar los errores encontrados por el MiniApplet y los clientes nativos.
 */
public class MiniAppletSuccessService extends HttpServlet {

	/** Serial Id. */
	private static final long serialVersionUID = 2487217258327717181L;

	private static final String JSON_PARAM_ID = "id"; //$NON-NLS-1$
	private static final String JSON_PARAM_RESULT = "result"; //$NON-NLS-1$
	private static final String JSON_PARAM_DESCRIPTION = "description"; //$NON-NLS-1$
	private static final String JSON_PARAM_SIGNS = "signs"; //$NON-NLS-1$

	private static final Logger LOGGER = Logger.getLogger(MiniAppletSuccessService.class.getName());

	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) {

		// No se guardaran los resultados en cache
		response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); //$NON-NLS-1$ //$NON-NLS-2$

		RequestParameters params;
		try {
			params = RequestParameters.extractParameters(request);
		}
		catch (final Exception e) {
			LOGGER.log(Level.WARNING, "Error en la lectura de los parametros de entrada", e); //$NON-NLS-1$
			Responser.sendError(response, FIReError.READING_PARAMETERS);
			return;
		}
		
		// Comprobamos que se hayan prorcionado los parametros indispensables
		final String trId = params.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID);
        if (trId == null || trId.isEmpty()) {
        	LOGGER.warning("No se ha proporcionado el ID de transaccion"); //$NON-NLS-1$
        	Responser.sendError(response, FIReError.FORBIDDEN);
            return;
        }

		final TransactionAuxParams trAux = new TransactionAuxParams(null, LogUtils.limitText(trId));
        final LogTransactionFormatter logF = trAux.getLogFormatter();

		LOGGER.fine(logF.f("Inicio de la llamada al servicio publico de exito de firma con certificado local")); //$NON-NLS-1$

		final String subjectRef = params.getParameter(ServiceParams.HTTP_PARAM_SUBJECT_REF);

        String redirectErrorUrl = params.getParameter(ServiceParams.HTTP_PARAM_ERROR_URL);
		if (redirectErrorUrl == null || redirectErrorUrl.isEmpty()) {
			LOGGER.warning(logF.f("No se ha proporcionado la URL de error")); //$NON-NLS-1$
			Responser.sendError(response, FIReError.FORBIDDEN);
			return;
		}
		try {
        	redirectErrorUrl = URLDecoder.decode(redirectErrorUrl, StandardCharsets.UTF_8.name());
        }
        catch (final Exception e) {
        	LOGGER.warning(logF.f("No se pudo deshacer el URL Encoding de la URL de redireccion: ") + e); //$NON-NLS-1$
		}

        final FireSession session = loadSession(trId, subjectRef, request, trAux);
        if (session == null) {
        	Responser.redirectToExternalUrl(redirectErrorUrl, request, response, trAux);
    		return;
        }

		trAux.setAppId(session.getString(ServiceParams.SESSION_PARAM_APPLICATION_ID));

    	// Comprobamos que haya URL de redireccion
    	final TransactionConfig connConfig	= (TransactionConfig) session
    			.getObject(ServiceParams.SESSION_PARAM_CONNECTION_CONFIG);
    	if (connConfig == null || connConfig.getRedirectSuccessUrl() == null || !connConfig.isDefinedRedirectErrorUrl()) {
    		LOGGER.warning(logF.f("No se encontraron en la sesion las URL de redireccion para la operacion")); //$NON-NLS-1$
    		ErrorManager.setErrorToSession(session, FIReError.INTERNAL_ERROR, trAux);
    		Responser.redirectToExternalUrl(redirectErrorUrl, request, response, trAux);
    		return;
    	}
		final String redirectUrl = connConfig.getRedirectSuccessUrl();
		redirectErrorUrl = connConfig.getRedirectErrorUrl();

		// Agregamos el certificado en caso de haberlo recibido. Para el proceso de firma simple,
		// sera obligatorio ya que se requerira para completar la firma
		final String certB64 = params.getParameter(ServiceParams.HTTP_PARAM_CERT);
        if (certB64 != null && !certB64.isEmpty()) {
        	session.setAttribute(ServiceParams.SESSION_PARAM_CERT, certB64);
        }

        // Se comprueba si se ha realizado una firma de lote para actualizar el estado del lote
    	// en la sesion
        if (Boolean.parseBoolean(params.getParameter(ServiceParams.HTTP_PARAM_IS_BATCH_OPERATION))) {
        	final String afirmaBatchResultB64 = params.getParameter(ServiceParams.HTTP_PARAM_AFIRMA_BATCH_RESULT);
        	final boolean stopOnError = Boolean.parseBoolean(session.getString(ServiceParams.SESSION_PARAM_BATCH_STOP_ON_ERROR));
        	final BatchResult batchResult = (BatchResult) session.getObject(ServiceParams.SESSION_PARAM_BATCH_RESULT);

        	// Actualizamos el resultado del lote con el resultado reportado por el Cliente @firma
        	try {
        		updateBatchResult(batchResult, afirmaBatchResultB64, stopOnError, session);
        	} catch (final AOException e) {
        		LOGGER.log(Level.WARNING, logF.f("Fallo alguna de las firmas del lote y se aborta la operacion como se habia solicitado"), e); //$NON-NLS-1$
        		ErrorManager.setErrorToSession(session, FIReError.BATCH_SIGNING, true, trAux);
        		Responser.redirectToExternalUrl(redirectErrorUrl, request, response, trAux);
        		return;
        	} catch (final Exception e) {
        		LOGGER.log(Level.SEVERE, logF.f("Error al procesar el resultado de la firma de lote del Cliente @firma"), e); //$NON-NLS-1$
        		ErrorManager.setErrorToSession(session, FIReError.BATCH_SIGNING, true, trAux);
        		Responser.redirectToExternalUrl(redirectErrorUrl, request, response, trAux);
        		return;
        	}
        }

        // Actualizamos la bandera de operacion anterior para avisar de que se ha iniciado la
        // ejecucion de una firma
        session.setAttribute(ServiceParams.SESSION_PARAM_PREVIOUS_OPERATION, SessionFlags.OP_PRE);

        SessionCollector.commit(session, trAux);
        session.saveIntoHttpSession(request.getSession());

        Responser.redirectToExternalUrl(redirectUrl, request, response, trAux);
	}

	private static FireSession loadSession(final String transactionId, final String subjectRef, final HttpServletRequest request,
			final TransactionAuxParams trAux) {

		FireSession session = SessionCollector.getFireSessionOfuscated(transactionId, subjectRef, request.getSession(false), false, ConfigManager.isSessionSharingForced(), trAux);
        if (session == null && ConfigManager.isSessionSharingForced()) {
        	LOGGER.warning(trAux.getLogFormatter().f("La transaccion %1s no se ha inicializado o ha caducado. Se redirige a la pagina proporcionada en la llamada", LogUtils.cleanText(transactionId))); //$NON-NLS-1$
    		return null;
        }

		// Si la operacion anterior no fue la eleccion de proveedor, forzamos a que se recargue
        // por si faltan datos
		if (session == null || SessionFlags.OP_CHOOSE != session.getObject(ServiceParams.SESSION_PARAM_PREVIOUS_OPERATION)) {
			LOGGER.info(trAux.getLogFormatter().f("No se encontro la sesion o no estaba actualizada. Forzamos la carga")); //$NON-NLS-1$
			session = SessionCollector.getFireSessionOfuscated(transactionId, subjectRef,
					request.getSession(false), false, true, trAux);
		}

		return session;
	}

	/**
	 * Actualiza el estado del lote con el resultado obtenido al firmarlo con el Cliente @firma.
	 * @param batchResult Resultado parcial de la firma del lote.
	 * @param afirmaBatchResultB64 Resultado obtenido del Cliente @firma al finalizar la firma
	 * 		  del lote.
	 * @param stopOnError {@code true} si se debe abortar la ejecuci&oacute;n en caso de error,
	 * 		  {@code false} en caso contrario.
	 * @param session Sesi&oacute;n de la transacci&oacute;n de firma para el registro de
	 * 		  estad&iacute;sticas.
	 * @throws AOException Cuando se aborta la operaci&oacute;n al encontrar errores cuando estos
	 * 		  no se permiten.
	 * @throws IOException Cuando ocurre alg&uacute;n error al procesar el resultado devuelto por
	 * 		   el Cliente @firma.
	 */
	private static void updateBatchResult(final BatchResult batchResult, final String afirmaBatchResultB64,
			final boolean stopOnError, final FireSession session) throws AOException, IOException {

		final byte[] afirmaResultJSON = Base64.decode(afirmaBatchResultB64);

		JsonObject jsonObject = null;
		try (ByteArrayInputStream bais = new ByteArrayInputStream(afirmaResultJSON);
				JsonReader reader = Json.createReader(bais)) {
				jsonObject = reader.readObject();
		}

		final Map<String, AfirmaSingleResult> afirmaResults = new HashMap<>();
		parseAfirmaResult(jsonObject, afirmaResults);

		final Iterator<String> it = batchResult.iterator();
		while (it.hasNext()) {
			final String docId = it.next();
			final AfirmaSingleResult asr = afirmaResults.get(docId);
			if (asr != null) {
				if (asr.isOk()) {
					batchResult.setSuccessResult(docId);
				}
				else if (stopOnError) {
					throw new AOException("Error en una de las operaciones del lote: " + asr.getError()); //$NON-NLS-1$
				}
				else {
					batchResult.setErrorResult(docId, translateAfirmaError(asr.getError()));
					batchResult.setErrorMessage(docId, asr.getErrorMessage());
				}
			}
		}
	}

	/**
	 * Extrae de un JSON resultado de la firma de un lote con el Cliente @firma el estado
	 * de cada una de las firmas del lote.
	 * @param jsonObjectResult JSON con el resultado de las firmas.
	 * @param afirmaResults Mapa con los resultados de las firmas registrados por su ID de documento.
	 */
	private static void parseAfirmaResult(final JsonObject jsonObjectResult, final Map<String, AfirmaSingleResult> afirmaResults) {
		final JsonArray singleSignsArray = jsonObjectResult.getJsonArray(JSON_PARAM_SIGNS);
		for (int i = 0; i < singleSignsArray.size(); i++) {
			if (singleSignsArray.getJsonObject(i).containsKey(JSON_PARAM_ID)) {
				if (AfirmaResult.DONE_AND_SAVED.name().equals(singleSignsArray.getJsonObject(i).getString(JSON_PARAM_RESULT))) {
					afirmaResults.put(singleSignsArray.getJsonObject(i).getString(JSON_PARAM_ID), AfirmaSingleResult.getAfirmaOkResult());
				} else {
					final String error = singleSignsArray.getJsonObject(i).getString(JSON_PARAM_RESULT);
					final String description = singleSignsArray.getJsonObject(i).getString(JSON_PARAM_DESCRIPTION, "Error desconocido"); //$NON-NLS-1$
					afirmaResults.put(singleSignsArray.getJsonObject(i).getString(JSON_PARAM_ID),
							AfirmaSingleResult.getAfirmaErrorResult(error, description));
				}
			}
		}
	}

	/**
	 * Posibles resultados que puede asignar el Cliente Afirma a las firmas
	 * de un lote. Estos resultados se asignan a medida que avanza el proceso
	 * de firma, por lo que el resultado va cambiando seg&uacute;n avanza.
	 */
	private enum AfirmaResult {
		/** A&uacute;n no se ha iniciado el proceso de firma. */
		NOT_STARTED,
		/** Proceso finalizado correctamente (se ha generado y guardado la firma). */
		DONE_AND_SAVED,
		/** Se ha generado la firma pero todav&iacute;a est&aacute; pendiente de guardarse. */
		DONE_BUT_NOT_SAVED_YET,
		/** Se ha generado la firma y no se guardar&aacute;, posiblemente porque se abort&oacute; el proceso. */
		DONE_BUT_SAVED_SKIPPED,
		/** Se ha generado la firma, pero no se pudo guardar. */
		DONE_BUT_ERROR_SAVING,
		/** Fall&oacute; el proceso de firma durante la prefirma. */
		ERROR_PRE,
		/** Fall&oacute; el proceso de firma durante la postfirma. */
		ERROR_POST,
		/**
		 * No se firmar&aacute;n los datos, posiblemente porque fall&oacute; una operaci&oacute;n de
		 * firma anterior cuando no se admit&iacute;an errores o porque se abord&oacute; el proceso.
		 */
		SKIPPED,
		/**
		 * Despu&eacute;s de finalizar la firma y guardarla, se ha deshecho el guardado, posiblemente
		 * porque fall&oacute; una firma posterior y se estableci&oacute; que se anulasen las firmas
		 * anteriores.
		 */
		SAVE_ROLLBACKED;
	}

	/**
	 * Traduce entre los estados de error que puede asignar el cliente @firma a
	 * las firmas de un lote y los errores manejados por Clave Firma.
	 * @param error Estado indicado por el Cliente @firma para la firma de un lote.
	 * @return Error de clave firma para la firma de un lote o el mismo error de
	 * entrada si no se pudo traducir.
	 */
	private static String translateAfirmaError(final String error) {

		if (AfirmaResult.NOT_STARTED.name().equals(error) ||
				AfirmaResult.SKIPPED.name().equals(error)) {
			return BatchResult.NO_PROCESSED;
		}
		else if (AfirmaResult.DONE_BUT_ERROR_SAVING.name().equals(error)) {
			return BatchResult.ERROR_SAVING_DATA;
		}
		else if (AfirmaResult.DONE_BUT_NOT_SAVED_YET.name().equals(error) ||
				AfirmaResult.SAVE_ROLLBACKED.name().equals(error) ||
				AfirmaResult.DONE_BUT_SAVED_SKIPPED.name().equals(error)) {
			return BatchResult.ABORTED;
		}
		else if (AfirmaResult.ERROR_PRE.name().equals(error)) {
			return BatchResult.PRESIGN_ERROR;
		}
		else if (AfirmaResult.ERROR_POST.name().equals(error)) {
			return BatchResult.POSTSIGN_ERROR;
		}

		return error;
	}

	/**
	 * Clase para el almacenamiento del resultado que Afirma concedio a una firma
	 * particular de un lote.
	 */
	private static class AfirmaSingleResult {

		private final String error;
		private final String errorMessage;

		private AfirmaSingleResult(final String error) {
			this.error = error;
			this.errorMessage = null;
		}

		private AfirmaSingleResult(final String error, final String errorMessage) {
			this.error = error;
			this.errorMessage = errorMessage;
		}

		public boolean isOk() {
			return this.error == null;
		}

		public String getError() {
			return this.error;
		}

		public String getErrorMessage() {
			return this.errorMessage;
		}

		public static AfirmaSingleResult getAfirmaOkResult() {
			return new AfirmaSingleResult(null);
		}

		public static AfirmaSingleResult getAfirmaErrorResult(final String error, final String errorMessage) {
			return new AfirmaSingleResult(error, errorMessage);
		}
	}
}
