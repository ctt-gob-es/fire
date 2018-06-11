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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import es.gob.afirma.core.misc.Base64;

/**
 * Servicio para procesar los errores encontrados por el MiniApplet y los clientes nativos.
 */
public class MiniAppletSuccessService extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = Logger.getLogger(MiniAppletSuccessService.class.getName());

	private static final String URL_ENCODING = "utf-8"; //$NON-NLS-1$

	@Override
	protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

		// Comprobamos que se hayan prorcionado los parametros indispensables
		final String transactionId = request.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID);
        if (transactionId == null || transactionId.isEmpty()) {
        	LOGGER.warning("No se ha proporcionado el ID de transaccion"); //$NON-NLS-1$
       		response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

		final String userId = request.getParameter(ServiceParams.HTTP_PARAM_SUBJECT_ID);

        String errorUrl = request.getParameter(ServiceParams.HTTP_PARAM_ERROR_URL);
        if (errorUrl == null || errorUrl.isEmpty()) {
        	LOGGER.warning("No se ha proporcionado la URL de redireccion de error"); //$NON-NLS-1$
            SessionCollector.removeSession(transactionId);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        errorUrl = URLDecoder.decode(errorUrl, URL_ENCODING);

        FireSession session = SessionCollector.getFireSession(transactionId, userId, request.getSession(false), true, false);
        if (session == null) {
        	LOGGER.warning("La sesion no existe"); //$NON-NLS-1$
        	SessionCollector.removeSession(transactionId);
   			response.sendRedirect(errorUrl);
    		return;
        }

		// Si la operacion anterior no fue de solicitud de firma, forzamos a que se recargue por si faltan datos
		if (SessionFlags.OP_SIGN != session.getObject(ServiceParams.SESSION_PARAM_PREVIOUS_OPERATION)) {
			session = SessionCollector.getFireSession(transactionId, userId, request.getSession(false), false, true);
		}

    	// Comprobamos que haya URL de redireccion
    	final TransactionConfig connConfig	=
    			(TransactionConfig) session.getObject(ServiceParams.SESSION_PARAM_CONNECTION_CONFIG);
    	if (connConfig == null || connConfig.getRedirectSuccessUrl() == null) {
    		LOGGER.warning("No se encontraron en la sesion las URL de redireccion para la operacion"); //$NON-NLS-1$
    		ErrorManager.setErrorToSession(session, OperationError.INVALID_STATE);
    		response.sendError(HttpServletResponse.SC_BAD_REQUEST);
    		return;
    	}
		final String redirectUrl = connConfig.getRedirectSuccessUrl();
		errorUrl = connConfig.getRedirectErrorUrl();

		// Agregamos el certificado en caso de haberlo recibido (que se deberia, pero no sera imprescindible)
		final String certB64 = request.getParameter(ServiceParams.HTTP_PARAM_CERT);
        if (certB64 != null && !certB64.isEmpty()) {
        	session.setAttribute(ServiceParams.SESSION_PARAM_CERT, certB64);
        }

        // Se comprueba si se ha realizado una firma de lote para actualizar el estado del lote
    	// en la sesion
        if (Boolean.parseBoolean(request.getParameter(ServiceParams.HTTP_PARAM_IS_BATCH_OPERATION))) {
        	final String afirmaBatchResultB64 = request.getParameter(ServiceParams.HTTP_PARAM_AFIRMA_BATCH_RESULT);
        	final BatchResult batchResult = (BatchResult) session.getObject(ServiceParams.SESSION_PARAM_BATCH_RESULT);

        	// Actualizamos el resultado del lote con el resultado reportado el Cliente @firma
        	try {
        		updateSingleResult(batchResult, afirmaBatchResultB64);
        	} catch (final Exception e) {
        		LOGGER.log(Level.SEVERE, "Error al procesar el resultado de la firma de lote del Cliente @firma: " + e, e); //$NON-NLS-1$
        		ErrorManager.setErrorToSession(session, OperationError.SIGN_MINIAPPLET_BATCH, true, null);
        		response.sendRedirect(errorUrl);
        		return;
        	}
        }

        // Actualizamos la bandera de operacion anterior para avisar de que se ha iniciado la
        // ejecucion de una firma
        session.setAttribute(ServiceParams.SESSION_PARAM_PREVIOUS_OPERATION, SessionFlags.OP_PRE);

        SessionCollector.commit(session);

        response.sendRedirect(redirectUrl);
	}

	/**
	 * Actualiza el estado de las firmas del lote con el resultado obtenido al firmarlas
	 * con el Cliente @firma.
	 * @param batchResult Resultado parcial de la firma del lote.
	 * @param afirmaBatchResultB64 Resultado obtenido del Cliente @firma al finalizar la firma
	 * 		  del lote.
	 * @throws Exception Cuando ocurre alg&uacute;n error al procesar el resultado devuelto por
	 * 		   el Cliente @firma.
	 */
	private static void updateSingleResult(final BatchResult batchResult, final String afirmaBatchResultB64) throws Exception {

		final byte[] afirmaResultXml = Base64.decode(afirmaBatchResultB64);
		final Document afirmaResultDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
				new ByteArrayInputStream(afirmaResultXml));

		final Map<String, AfirmaSingleResult> afirmaResults = new HashMap<>();
		parseAfirmaResult(afirmaResultDoc, afirmaResults);

		final Iterator<String> it = batchResult.iterator();
		while (it.hasNext()) {
			final String docId = it.next();
			final AfirmaSingleResult asr = afirmaResults.get(docId);
			if (asr != null) {
				if (asr.isOk()) {
					batchResult.setSuccessResult(docId);
				}
				else {
					batchResult.setErrorResult(docId, translateAfirmaError(asr.getError()));
				}
			}
		}
	}

	/**
	 * Extrae de un XML resultado de la firma de un lote con el Cliente @firma el estado
	 * de cada una de las firmas del lote.
	 * @param afirmaResultDoc &Aacute;rbol DOM con el resultado de las firmas.
	 * @param afirmaResults Mapa con los resultados de las firmas registrados por su ID de documento.
	 */
	private static void parseAfirmaResult(final Document afirmaResultDoc, final Map<String, AfirmaSingleResult> afirmaResults) {
		final Element rootElement = afirmaResultDoc.getDocumentElement();
		final NodeList signNodes = rootElement.getChildNodes();
		for (int i = 0; i < signNodes.getLength(); i++) {
			final Node signNode = signNodes.item(i);
			if (signNode.getNodeType() == Node.ELEMENT_NODE && signNode.getNodeName().equals("signresult") && signNode.getAttributes().getNamedItem("id") != null) { //$NON-NLS-1$ //$NON-NLS-2$
				final Node docIdNode = signNode.getAttributes().getNamedItem("id"); //$NON-NLS-1$
				final Node resultNode = signNode.getAttributes().getNamedItem("result"); //$NON-NLS-1$
				if (AfirmaResult.DONE_AND_SAVED.name().equals(resultNode.getNodeValue())) {
					afirmaResults.put(docIdNode.getNodeValue(), AfirmaSingleResult.getAfirmaOkResult());
				} else {
					afirmaResults.put(docIdNode.getNodeValue(), AfirmaSingleResult.getAfirmaErrorResult(resultNode.getNodeValue()));
				}
			}
		}
	}



	/**
	 * Posibles resultados que puede asignar el Cliente Afirma a las firmas
	 * de un lote.
	 */
	private enum AfirmaResult {
		NOT_STARTED,
		DONE_AND_SAVED,
		DONE_BUT_NOT_SAVED_YET,
		DONE_BUT_SAVED_SKIPPED,
		DONE_BUT_ERROR_SAVING,
		ERROR_PRE,
		ERROR_POST,
		SKIPPED,
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

		private AfirmaSingleResult(final String error) {
			this.error = error;
		}

		public boolean isOk() {
			return this.error == null;
		}

		public String getError() {
			return this.error;
		}

		public static AfirmaSingleResult getAfirmaOkResult() {
			return new AfirmaSingleResult(null);
		}

		public static AfirmaSingleResult getAfirmaErrorResult(final String error) {
			return new AfirmaSingleResult(error);
		}
	}
}
