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
import java.net.URLDecoder;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import es.gob.fire.server.connector.CertificateBlockedException;
import es.gob.fire.server.connector.FIReCertificateException;
import es.gob.fire.server.connector.FIReConnector;
import es.gob.fire.server.connector.FIReConnectorFactoryException;
import es.gob.fire.server.connector.FIReConnectorNetworkException;
import es.gob.fire.server.connector.FIReConnectorUnknownUserException;
import es.gob.fire.server.connector.WeakRegistryException;
import es.gob.fire.statistics.entity.Browser;


/**
 * Servlet implementation class ChooseCertificateOriginService
 */
public class ChooseCertificateOriginService extends HttpServlet {

	/** Serial Id. */
	private static final long serialVersionUID = -1367908808211903695L;

	private static final Logger LOGGER = Logger.getLogger(ChooseCertificateOriginService.class.getName());
	private static final String URL_ENCODING = "utf-8"; //$NON-NLS-1$

	@Override
	protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

		final String transactionId = request.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID);
		final String subjectId = request.getParameter(ServiceParams.HTTP_PARAM_SUBJECT_ID);
		final String origin = request.getParameter(ServiceParams.HTTP_PARAM_CERT_ORIGIN);
		final boolean originForced = Boolean.parseBoolean(request.getParameter(ServiceParams.HTTP_PARAM_CERT_ORIGIN_FORCED));
		String redirectErrorUrl = request.getParameter(ServiceParams.HTTP_PARAM_ERROR_URL);

		final LogTransactionFormatter logF = new LogTransactionFormatter(null, transactionId);

		if (transactionId == null || transactionId.isEmpty()) {
			LOGGER.warning("No se ha proporcionado el identificador de transaccion"); //$NON-NLS-1$
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		if (subjectId == null || subjectId.isEmpty()) {
			LOGGER.warning(logF.format("No se ha proporcionado el identificador de usuario")); //$NON-NLS-1$
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		if (origin == null || origin.isEmpty()) {
			LOGGER.warning(logF.format("No se ha proporcionado el origen del certificado")); //$NON-NLS-1$
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		if (redirectErrorUrl == null || redirectErrorUrl.isEmpty()) {
			LOGGER.warning(logF.format("No se ha proporcionado la URL de error")); //$NON-NLS-1$
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		redirectErrorUrl = URLDecoder.decode(redirectErrorUrl, URL_ENCODING);

		FireSession session = SessionCollector.getFireSession(transactionId, subjectId, request.getSession(), false, false);
		if (session == null) {
			LOGGER.severe(logF.format("No existe sesion vigente asociada a la transaccion")); //$NON-NLS-1$
			response.sendRedirect(redirectErrorUrl);
			return;
		}

		// Si la operacion anterior no fue de solicitud de firma, forzamos a que se recargue por si faltan datos
		if (SessionFlags.OP_SIGN != session.getObject(ServiceParams.SESSION_PARAM_PREVIOUS_OPERATION)) {
			session = SessionCollector.getFireSession(transactionId, subjectId, request.getSession(false), false, true);
		}


		final String userAgent = request.getHeader("user-agent"); //$NON-NLS-1$
	    final Browser browser =  Browser.identify(userAgent);
	    session.setAttribute(ServiceParams.SESSION_PARAM_BROWSER, browser.getName());

		// Agregamos a la sesion el origen del certificado
		session.setAttribute(ServiceParams.SESSION_PARAM_CERT_ORIGIN, origin);

		// Si se forzo a ese origen desde la aplicacion, se registra
		if (originForced) {
			session.setAttribute(ServiceParams.SESSION_PARAM_CERT_ORIGIN_FORCED, Boolean.TRUE.toString());
		}

		// Se selecciono firmar con un certificado local
		if (ServiceParams.CERTIFICATE_ORIGIN_LOCAL.equalsIgnoreCase(origin)) {
			signWithClienteAfirma(session, request, response, logF);
		}
		// Si no se selecciono firma local, se firmara con un proveedor de firma en la nube
		else {
			signWithProvider(origin, session, request, response, redirectErrorUrl, originForced, logF);
		}
	}

	/**
	 * Ejecuta y gestiona el flujo de firma con el Cliente @firma.
	 * @param session Datos de la transacci&oacute;n.
	 * @param request Petici&oacute;n HTTP realizada al servicio.
	 * @param response Objeto HTTP de respuesta del servicio.
	 * @param logF Objeto para el formateo de los logs.
	 * @throws IOException Cuando ocurre un error al redirigir al usuario.
	 */
	private static void signWithClienteAfirma(final FireSession session, final HttpServletRequest request,
			final HttpServletResponse response, final LogTransactionFormatter logF) throws IOException {

		LOGGER.info(logF.format("Se ha seleccionado el proveedor local")); //$NON-NLS-1$

		SessionCollector.commit(session);

		try {
			request.getRequestDispatcher(FirePages.PG_MINI_APPLET).forward(request, response);
			return;
		} catch (final ServletException e) {
			LOGGER.warning(logF.format("No se pudo continuar hasta la pagina del MiniApplet. Se redirigira al usuario a la misma pagina: " + e)); //$NON-NLS-1$
			response.sendRedirect(FirePages.PG_MINI_APPLET+ "?" + ServiceParams.HTTP_PARAM_TRANSACTION_ID + //$NON-NLS-1$
					"=" + request.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID)); //$NON-NLS-1$
		}

	}

	/**
	 * Redirige el flujo de ejecuci&oacute;n para la firma con los certificados
	 * en la nube del proveedor indicado.
	 * @param providerName Nombre del proveedor de firma en la nube que se debe utilizar.
	 * @param session Datos de la transacci&oacute;n.
	 * @param request Petici&oacute;n HTTP realizada al servicio.
	 * @param response Objeto HTTP de respuesta del servicio.
	 * @param errorUrl URL a la que redirigir en caso de error hasta que se obtenga la de sesi&oacute;n.
	 * @param originForced Indica si se forz&oacute; el uso de un proveedor concreto.
	 * @param logF Objeto para el formateo de los logs.
	 * @throws IOException Cuando ocurre un error al redirigir al usuario.
	 * @throws ServletException Cuando ocurre un error al redirigir al usuario.
	 */
	private static void signWithProvider(final String providerName, final FireSession session,
			final HttpServletRequest request, final HttpServletResponse response,
			final String errorUrl, final boolean originForced, final LogTransactionFormatter logF)
					throws IOException, ServletException {

		final String trId = session.getString(ServiceParams.SESSION_PARAM_TRANSACTION_ID);
		final String subjectId = session.getString(ServiceParams.SESSION_PARAM_SUBJECT_ID);
		final TransactionConfig connConfig =
				(TransactionConfig) session.getObject(ServiceParams.SESSION_PARAM_CONNECTION_CONFIG);

		if (connConfig == null || !connConfig.isDefinedRedirectErrorUrl()) {
			LOGGER.warning(logF.format("No se encontro en la sesion la URL de redireccion de error para la operacion")); //$NON-NLS-1$
			ErrorManager.setErrorToSession(session, OperationError.INVALID_STATE);
        	response.sendRedirect(errorUrl);
			return;
		}

		final String redirectErrorUrl = connConfig.getRedirectErrorUrl();
		// Listamos los certificados del usuario
		X509Certificate[] certificates = null;
		try {
			LOGGER.info(logF.format("Se ha seleccionado el proveedor " + providerName.replaceAll("[\r\n]",""))); //$NON-NLS-1$
			final FIReConnector connector = ProviderManager.initTransacction(
					providerName,
					connConfig.getProperties()
			);
	        LOGGER.info(logF.format("Se ha cargado el conector " + connector.getClass().getName())); //$NON-NLS-1$
			certificates = connector.getCertificates(subjectId);
			if (certificates == null || certificates.length == 0) {
				if (connector.allowRequestNewCerts()) {
					LOGGER.info(logF.format("El usuario no dispone de certificados, pero el conector le permite generarlos")); //$NON-NLS-1$
					request.getRequestDispatcher(FirePages.PG_CHOOSE_CERTIFICATE_NOCERT).forward(request, response);
				}
				else {
					LOGGER.log(Level.WARNING, logF.format("El usuario no dispone de certificados y el conector no le permite generarlos")); //$NON-NLS-1$
					ErrorManager.setErrorToSession(session, OperationError.CERTIFICATES_NO_CERTS, originForced);
					if (originForced) {
						response.sendRedirect(redirectErrorUrl);
					}
					else {
						request.getRequestDispatcher(FirePages.PG_SIGNATURE_ERROR).forward(request, response);
					}
				}
				return;
			}
		}
		catch (final FIReConnectorFactoryException e) {
			LOGGER.log(Level.SEVERE, logF.format("Error en la configuracion del conector del proveedor de firma"), e); //$NON-NLS-1$
			ErrorManager.setErrorToSession(session, OperationError.INTERNAL_ERROR);
			response.sendRedirect(redirectErrorUrl);
			return;
		}
		catch (final FIReCertificateException e) {
			LOGGER.log(Level.SEVERE, logF.format("No se ha podido recuperar los certificados del usuario " + subjectId), e); //$NON-NLS-1$
			ErrorManager.setErrorToSession(session, OperationError.CERTIFICATES_SERVICE, originForced);
			if (originForced) {
				response.sendRedirect(redirectErrorUrl);
			}
			else {
				 request.getRequestDispatcher(FirePages.PG_SIGNATURE_ERROR).forward(request, response);
			}
			return;
		}
		catch (final FIReConnectorNetworkException e) {
			LOGGER.log(Level.SEVERE, logF.format("No se ha podido conectar con el sistema"), e); //$NON-NLS-1$
			ErrorManager.setErrorToSession(session, OperationError.CERTIFICATES_SERVICE_NETWORK, originForced);
			if (originForced) {
				response.sendRedirect(redirectErrorUrl);
			}
			else {
				 request.getRequestDispatcher(FirePages.PG_SIGNATURE_ERROR).forward(request, response);
			}
			return;
		}
		catch (final CertificateBlockedException e) {
			LOGGER.log(Level.WARNING, logF.format("Los certificados del usuario " + subjectId + " estan bloqueados: " + e)); //$NON-NLS-1$ //$NON-NLS-2$
			ErrorManager.setErrorToSession(session, OperationError.CERTIFICATES_BLOCKED, originForced);
			if (originForced) {
				response.sendRedirect(redirectErrorUrl);
			}
			else {
				 request.getRequestDispatcher(FirePages.PG_SIGNATURE_ERROR).forward(request, response);
			}
			return;
		}
		catch (final WeakRegistryException e) {
			LOGGER.log(Level.WARNING, logF.format("El usuario " + subjectId + " realizo un registro debil: " + e)); //$NON-NLS-1$ //$NON-NLS-2$
			ErrorManager.setErrorToSession(session, OperationError.CERTIFICATES_WEAK_REGISTRY, originForced);
			if (originForced) {
				response.sendRedirect(redirectErrorUrl);
			}
			else {
				 request.getRequestDispatcher(FirePages.PG_SIGNATURE_ERROR).forward(request, response);
			}
			return;
		}
		catch (final FIReConnectorUnknownUserException e) {
			LOGGER.log(Level.WARNING, logF.format("El usuario " + subjectId + " no esta registrado en el sistema: " + e)); //$NON-NLS-1$ //$NON-NLS-2$
			ErrorManager.setErrorToSession(session, OperationError.UNKNOWN_USER, originForced);
			if (originForced) {
				response.sendRedirect(redirectErrorUrl);
			}
			else {
				 request.getRequestDispatcher(FirePages.PG_SIGNATURE_ERROR).forward(request, response);
			}
			return;
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, logF.format("Error indeterminado al recuperar los certificados del usuario " + subjectId), e); //$NON-NLS-1$
			ErrorManager.setErrorToSession(session, OperationError.CERTIFICATES_SERVICE, originForced);
			if (originForced) {
				response.sendRedirect(redirectErrorUrl);
			}
			else {
				 request.getRequestDispatcher(FirePages.PG_SIGNATURE_ERROR).forward(request, response);
			}
			return;
		}

		// Adjuntamos los certificados a la sesion para que los reciba el JSP
		session.setAttribute(trId + "-certs", certificates); //$NON-NLS-1$
		SessionCollector.commit(session);

		try {
			request.getRequestDispatcher(FirePages.PG_CHOOSE_CERTIFICATE).forward(request, response);
			return;
		}
		catch (final ServletException e) {
			LOGGER.warning(logF.format("No se pudo continuar hasta la pagina de seleccion de certificado. Se redirigira al usuario a la misma pagina: " + e)); //$NON-NLS-1$
			response.sendRedirect( FirePages.PG_CHOOSE_CERTIFICATE+"?" + ServiceParams.HTTP_PARAM_TRANSACTION_ID + //$NON-NLS-1$
					"=" + request.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID)); //$NON-NLS-1$
		}
	}


}
