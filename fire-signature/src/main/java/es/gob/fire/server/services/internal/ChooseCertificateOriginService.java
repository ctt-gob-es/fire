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
import java.util.Properties;
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


/**
 * Servlet implementation class ChooseCertificateOriginService
 */
public class ChooseCertificateOriginService extends HttpServlet {

	/** Serial Id. */
	private static final long serialVersionUID = -1367908808211903695L;

	private static final Logger LOGGER = Logger.getLogger(ChooseCertificateOriginService.class.getName());

	private static final String URL_ENCODING = "utf-8"; //$NON-NLS-1$

	private static String originForced=null;
	
	
	@Override
	protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

		final String subjectId = request.getParameter(ServiceParams.HTTP_PARAM_SUBJECT_ID);
		final String origin = request.getParameter(ServiceParams.HTTP_PARAM_CERT_ORIGIN);
		originForced = request.getParameter(ServiceParams.HTTP_PARAM_CERT_ORIGIN_FORCED);
		final String transactionId = request.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID);
		String redirectErrorUrl = request.getParameter(ServiceParams.HTTP_PARAM_ERROR_URL);
		 
		 
		if (subjectId == null || subjectId.isEmpty()) {
			LOGGER.warning("No se ha proporcionado el identificador de usuario"); //$NON-NLS-1$
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		if (origin == null || origin.isEmpty()) {
			LOGGER.warning("No se ha proporcionado el origen del certificado"); //$NON-NLS-1$
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		if (transactionId == null || transactionId.isEmpty()) {
			LOGGER.warning("No se ha proporcionado el identificador de transaccion"); //$NON-NLS-1$
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		if (redirectErrorUrl == null || redirectErrorUrl.isEmpty()) {
			LOGGER.warning("No se ha proporcionado la URL de error"); //$NON-NLS-1$
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		redirectErrorUrl = URLDecoder.decode(redirectErrorUrl, URL_ENCODING);

		FireSession session = SessionCollector.getFireSession(transactionId, subjectId, request.getSession(), false, false);
		if (session == null) {
			LOGGER.severe("No existe sesion vigente asociada a la transaccion " + transactionId); //$NON-NLS-1$
			response.sendRedirect(redirectErrorUrl);
			return;
		}

		// Si la operacion anterior no fue de solicitud de firma, forzamos a que se recargue por si faltan datos
		if (SessionFlags.OP_SIGN != session.getObject(ServiceParams.SESSION_PARAM_PREVIOUS_OPERATION)) {
			session = SessionCollector.getFireSession(transactionId, subjectId, request.getSession(false), false, true);
		}

		// Agregamos a la sesion el origen del certificado
		session.setAttribute(ServiceParams.SESSION_PARAM_CERT_ORIGIN, origin);

		// Si se forzo a ese origen desde la aplicacion, se registra
		if (Boolean.parseBoolean(originForced)) {
			session.setAttribute(ServiceParams.SESSION_PARAM_CERT_ORIGIN_FORCED, Boolean.TRUE.toString());
		}

		// Se selecciono firmar con un certificado local
		if (ServiceParams.CERTIFICATE_ORIGIN_LOCAL.equalsIgnoreCase(origin)) {
			signWithClienteAfirma(session, request, response);
		}
		// Si no se selecciono firma local, se firmara con un proveedor de firma en la nube
		else {
			signWithProvider(origin, session, request, response, redirectErrorUrl);
		}
	}

	private static void signWithClienteAfirma(final FireSession session, final HttpServletRequest request, final HttpServletResponse response) throws IOException {

		SessionCollector.commit(session);

		try {
			request.getRequestDispatcher(fireSignatureCS.PG_MINI_APPLET).forward(request, response); //$NON-NLS-1$
			return;
		} catch (final ServletException e) {
			LOGGER.warning("No se pudo continuar hasta la pagina del MiniApplet. Se redirigira al usuario a la misma pagina."); //$NON-NLS-1$
			response.sendRedirect(fireSignatureCS.PG_MINI_APPLET+ "?" + ServiceParams.HTTP_PARAM_TRANSACTION_ID + //$NON-NLS-1$
					"=" + request.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID)); //$NON-NLS-1$
		}
	}

	/**
	 * Redirige el flujo de ejecuci&oacute;n para la firma con los certificados
	 * en la nube del proveedor indicado.
	 * @param providerName Nombre del proveedor de firma en la nube que se debe utilizar.
	 * @param session Datos de la transacci&oacute;n.
	 * @param request Petici&oacute;n realizada al servicio.
	 * @param response Objeto de respuesta del servicio.
	 * @param errorUrl URL a la que redirigir en caso de error hasta que se obtenga la de sesi&oacute;n.
	 * @throws IOException Cuando ocurre un error al redirigir al usuario.
	 * @throws ServletException Cuando ocurre un error al redirigir al usuario.
	 */
	private static void signWithProvider(final String providerName, final FireSession session, final HttpServletRequest request, final HttpServletResponse response, final String errorUrl) throws IOException, ServletException {

		final String trId = session.getString(ServiceParams.SESSION_PARAM_TRANSACTION_ID);
		final String subjectId = session.getString(ServiceParams.SESSION_PARAM_SUBJECT_ID);
		final Properties connConfig = (Properties) session.getObject(ServiceParams.SESSION_PARAM_CONNECTION_CONFIG);

		if (connConfig == null || !connConfig.containsKey(ServiceParams.CONNECTION_PARAM_ERROR_URL)) {
			LOGGER.warning("No se encontro en la sesion la URL redireccion de error para la operacion"); //$NON-NLS-1$
            setErrorToSession(session, OperationError.INVALID_STATE);
        	response.sendRedirect(errorUrl);
        	
			return;
		}

		final String redirectErrorUrl = connConfig.getProperty(ServiceParams.CONNECTION_PARAM_ERROR_URL);
	
		// Listamos los certificados del usuario
		X509Certificate[] certificates = null;
		try {
			final FIReConnector connector = ProviderManager.initTransacction(providerName, connConfig);
			certificates = connector.getCertificates(subjectId);
			if (certificates == null || certificates.length == 0) {
				SessionCollector.commit(session);
				request.getRequestDispatcher(fireSignatureCS.PG_CHOOSE_CERTIFICATE_NOCERT).forward(request, response); //$NON-NLS-1$
				return;
			}
		}
		catch(final FIReConnectorFactoryException e) {
			LOGGER.log(Level.SEVERE, "Error en la configuracion del conector del servicio de custodia", e); //$NON-NLS-1$
			if (Boolean.parseBoolean(originForced)) {				
				setErrorToSessionClient(session, OperationError.INTERNAL_ERROR);
				response.sendRedirect(redirectErrorUrl);
			}
			else {
				setErrorToSession(session, OperationError.INTERNAL_ERROR);
				 request.getRequestDispatcher(fireSignatureCS.PG_SIGNATURE_ERROR).forward(request, response); //$NON-NLS-1$
			}
            			
			return;
		}
		catch(final FIReCertificateException e) {
			LOGGER.log(Level.SEVERE, "No se ha podido recuperar los certificados del usuario " + subjectId + ": " + e, e); //$NON-NLS-1$ //$NON-NLS-2$
			if (Boolean.parseBoolean(originForced)) {				
				setErrorToSessionClient(session, OperationError.CERTIFICATES_SERVICE);
				response.sendRedirect(redirectErrorUrl);
			}
			else {
				setErrorToSession(session, OperationError.INTERNAL_ERROR);
				 request.getRequestDispatcher(fireSignatureCS.PG_SIGNATURE_ERROR).forward(request, response); //$NON-NLS-1$
			}						          
			return;
		}
		catch(final FIReConnectorNetworkException e) {
			LOGGER.log(Level.SEVERE, "No se ha podido conectar con el sistema: " + e, e); //$NON-NLS-1$
			if (Boolean.parseBoolean(originForced)) {				
				setErrorToSessionClient(session, OperationError.CERTIFICATES_SERVICE_NETWORK);
				response.sendRedirect(redirectErrorUrl);
			}
			else {
				setErrorToSession(session, OperationError.INTERNAL_ERROR);
				 request.getRequestDispatcher(fireSignatureCS.PG_SIGNATURE_ERROR).forward(request, response); //$NON-NLS-1$
			}						           
			return;
		}
		catch(final CertificateBlockedException e) {
			LOGGER.log(Level.WARNING, "Los certificados del usuario " + subjectId + " estan bloqueados: " + e); //$NON-NLS-1$ //$NON-NLS-2$
			if (Boolean.parseBoolean(originForced)) {				
				setErrorToSessionClient(session, OperationError.CERTIFICATES_BLOCKED);
				response.sendRedirect(redirectErrorUrl);
			}
			else {
				setErrorToSession(session, OperationError.INTERNAL_ERROR);
				 request.getRequestDispatcher(fireSignatureCS.PG_SIGNATURE_ERROR).forward(request, response); //$NON-NLS-1$
			}		  
			return;
		}
		catch(final WeakRegistryException e) {
			LOGGER.log(Level.WARNING, "El usuario " + subjectId + " realizo un registro debil: " + e); //$NON-NLS-1$ //$NON-NLS-2$
			if (Boolean.parseBoolean(originForced)) {				
				setErrorToSessionClient(session, OperationError.CERTIFICATES_WEAK_REGISTRY);
				response.sendRedirect(redirectErrorUrl);
			}
			else {
				setErrorToSession(session, OperationError.INTERNAL_ERROR);
				 request.getRequestDispatcher(fireSignatureCS.PG_SIGNATURE_ERROR).forward(request, response); //$NON-NLS-1$
			}           
			return;
		}
		catch(final FIReConnectorUnknownUserException e) {
			LOGGER.log(Level.WARNING, "El usuario " + subjectId + " no esta registrado en el sistema: " + e); //$NON-NLS-1$ //$NON-NLS-2$
			if (Boolean.parseBoolean(originForced)) {				
				setErrorToSessionClient(session, OperationError.UNKNOWN_USER);
				response.sendRedirect(redirectErrorUrl);
			}
			else {
				setErrorToSession(session, OperationError.INTERNAL_ERROR);
				 request.getRequestDispatcher(fireSignatureCS.PG_SIGNATURE_ERROR).forward(request, response); //$NON-NLS-1$
			} 
			return;
		}
		catch(final Exception e) {
			LOGGER.log(Level.SEVERE, "Error indeterminado al recuperar los certificados del usuario " + subjectId + ": " + e, e); //$NON-NLS-1$ //$NON-NLS-2$
			if (Boolean.parseBoolean(originForced)) {				
				setErrorToSessionClient(session, OperationError.CERTIFICATES_SERVICE);
				response.sendRedirect(redirectErrorUrl);
			}
			else {
				setErrorToSession(session, OperationError.INTERNAL_ERROR);
				 request.getRequestDispatcher(fireSignatureCS.PG_SIGNATURE_ERROR).forward(request, response); //$NON-NLS-1$
			}          
			return;
		}

		// Adjuntamos los certificados a la sesion para que los reciba el JSP
		session.setAttribute(trId + "-certs", certificates); //$NON-NLS-1$
		SessionCollector.commit(session);

		try {
			request.getRequestDispatcher(fireSignatureCS.PG_CHOOSE_CERTIFICATE).forward(request, response); //$NON-NLS-1$
			return;
		}
		catch (final ServletException e) {
			LOGGER.warning("No se pudo continuar hasta la pagina de seleccion de certificado. Se redirigira al usuario a la misma pagina."); //$NON-NLS-1$
			response.sendRedirect( fireSignatureCS.PG_CHOOSE_CERTIFICATE+"?" + ServiceParams.HTTP_PARAM_TRANSACTION_ID + //$NON-NLS-1$
					"=" + request.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID)); //$NON-NLS-1$
		}
	}

	private static void setErrorToSessionClient(final FireSession session, final OperationError error) {
		SessionCollector.cleanSession(session);
		session.setAttribute(ServiceParams.SESSION_PARAM_ERROR_TYPE, Integer.toString(error.getCode()));
		session.setAttribute(ServiceParams.SESSION_PARAM_ERROR_MESSAGE, error.getMessage());
		SessionCollector.commit(session);
	}

	private static void setErrorToSession(final FireSession session, final OperationError error) {
		
		session.setAttribute(ServiceParams.SESSION_PARAM_ERROR_TYPE, Integer.toString(error.getCode()));
		session.setAttribute(ServiceParams.SESSION_PARAM_ERROR_MESSAGE, error.getMessage());
		SessionCollector.commit(session);
	}
	
}
