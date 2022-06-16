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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import es.gob.afirma.core.misc.Base64;

/**
 * Servicio para procesar los errores encontrados por el MiniApplet y los clientes nativos.
 */
public class MiniAppletSuccessService extends HttpServlet {

	/** Serial Id. */
	private static final long serialVersionUID = 2487217258327717181L;

	private static final Logger LOGGER = Logger.getLogger(MiniAppletSuccessService.class.getName());

	@Override
	protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

		// Comprobamos que se hayan prorcionado los parametros indispensables
		final String transactionId = request.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID);
        if (transactionId == null || transactionId.isEmpty()) {
        	LOGGER.warning("No se ha proporcionado el ID de transaccion"); //$NON-NLS-1$
       		response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

		// No se guardaran los resultados en cache
		response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); //$NON-NLS-1$ //$NON-NLS-2$

        final LogTransactionFormatter logF = new LogTransactionFormatter(null, transactionId);

		LOGGER.fine(logF.f("Inicio de la llamada al servicio publico de exito de firma con certificado local")); //$NON-NLS-1$

		final String subjectRef = request.getParameter(ServiceParams.HTTP_PARAM_SUBJECT_REF);

        String redirectErrorUrl = request.getParameter(ServiceParams.HTTP_PARAM_ERROR_URL);
        if (redirectErrorUrl == null || redirectErrorUrl.isEmpty()) {
        	LOGGER.warning(logF.f("No se ha proporcionado la URL de redireccion de error")); //$NON-NLS-1$
            SessionCollector.removeSession(transactionId);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        FireSession session = SessionCollector.getFireSessionOfuscated(transactionId, subjectRef, request.getSession(false), true, false);
        if (session == null) {
        	LOGGER.warning(logF.f("La transaccion %1s no se ha inicializado o ha caducado", transactionId)); //$NON-NLS-1$
        	SessionCollector.removeSession(transactionId);
        	redirectToExternalUrl(redirectErrorUrl, request, response);
    		return;
        }

		// Si la operacion anterior no fue de solicitud de firma, forzamos a que se recargue por si faltan datos
		if (SessionFlags.OP_SIGN != session.getObject(ServiceParams.SESSION_PARAM_PREVIOUS_OPERATION)) {
			session = SessionCollector.getFireSessionOfuscated(transactionId, subjectRef, request.getSession(false), false, true);
		}

    	// Comprobamos que haya URL de redireccion
    	final TransactionConfig connConfig	= (TransactionConfig) session
    			.getObject(ServiceParams.SESSION_PARAM_CONNECTION_CONFIG);
    	if (connConfig == null || connConfig.getRedirectSuccessUrl() == null || !connConfig.isDefinedRedirectErrorUrl()) {
    		LOGGER.warning(logF.f("No se encontraron en la sesion las URL de redireccion para la operacion")); //$NON-NLS-1$
    		ErrorManager.setErrorToSession(session, OperationError.INVALID_STATE);
    		redirectToExternalUrl(redirectErrorUrl, request, response);
    		return;
    	}
		final String redirectUrl = connConfig.getRedirectSuccessUrl();
		redirectErrorUrl = connConfig.getRedirectSuccessUrl();

		// Agregamos el certificado en caso de haberlo recibido. Para el proceso de firma simple,
		// sera obligatorio ya que se requerira para completar la firma
		final String certB64 = request.getParameter(ServiceParams.HTTP_PARAM_CERT);
        if (certB64 != null && !certB64.isEmpty()) {
        	session.setAttribute(ServiceParams.SESSION_PARAM_CERT, certB64);
        }

        // Se comprueba si se ha realizado una firma de lote para actualizar el estado del lote
    	// en la sesion
        if (Boolean.parseBoolean(request.getParameter(ServiceParams.HTTP_PARAM_IS_BATCH_OPERATION))) {
        	final String afirmaBatchResultB64 = request.getParameter(ServiceParams.HTTP_PARAM_AFIRMA_BATCH_RESULT);
        	final BatchResult batchResult = (BatchResult) session.getObject(ServiceParams.SESSION_PARAM_BATCH_RESULT);

        	// Actualizamos el resultado del lote con el resultado reportado por el Cliente @firma
        	try {
        		updateBatchResult(batchResult, afirmaBatchResultB64, session);
        	} catch (final Exception e) {
        		LOGGER.log(Level.SEVERE, logF.f("Error al procesar el resultado de la firma de lote del Cliente @firma"), e); //$NON-NLS-1$
        		ErrorManager.setErrorToSession(session, OperationError.SIGN_LOCAL_BATCH, true, null);
        		redirectToExternalUrl(redirectErrorUrl, request, response);
        		return;
        	}
        }

        // Actualizamos la bandera de operacion anterior para avisar de que se ha iniciado la
        // ejecucion de una firma
        session.setAttribute(ServiceParams.SESSION_PARAM_PREVIOUS_OPERATION, SessionFlags.OP_PRE);

        SessionCollector.commit(session);

        redirectToExternalUrl(redirectUrl, request, response);

		LOGGER.fine(logF.f("Fin de la llamada al servicio publico de exito de firma con certificado local")); //$NON-NLS-1$
	}

    /**
     * Redirige al usuario a una URL externa y elimina su sesion HTTP, si la
     * tuviese, para borrar cualquier dato que hubiese en ella.
     * @param url URL a la que redirigir al usuario.
     * @param request Objeto de petici&oacute;n realizada al servlet.
     * @param response Objeto de respuesta con el que realizar la redirecci&oacute;n.
     * @throws IOException Cuando no se puede redirigir al usuario.
     */
    private static void redirectToExternalUrl(final String url, final HttpServletRequest request, final HttpServletResponse response) throws IOException {

        // Invalidamos la sesion entre el navegador y el componente central porque no se usara mas
    	final HttpSession httpSession = request.getSession(false);
        if (httpSession != null) {
        	httpSession.invalidate();
        }

    	response.sendRedirect(url);
    }

	/**
	 * Actualiza el estado del lote con el resultado obtenido al firmarlo con el Cliente @firma.
	 * @param batchResult Resultado parcial de la firma del lote.
	 * @param afirmaBatchResultB64 Resultado obtenido del Cliente @firma al finalizar la firma
	 * 		  del lote.
	 * @param session Sesi&oacute;n de la transacci&oacute;n de firma para el registro de
	 * 		  estad&iacute;sticas.
	 * @throws Exception Cuando ocurre alg&uacute;n error al procesar el resultado devuelto por
	 * 		   el Cliente @firma.
	 */
	private static void updateBatchResult(final BatchResult batchResult, final String afirmaBatchResultB64, final  FireSession session) throws Exception {

		final byte[] afirmaResultXml = Base64.decode(afirmaBatchResultB64);
		final Document afirmaResultDoc = SecurityUtils.getDocumentBuilder().parse(
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
