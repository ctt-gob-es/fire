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

import java.net.URLDecoder;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import es.gob.afirma.core.misc.Base64;
import es.gob.fire.alarms.Alarm;
import es.gob.fire.server.connector.CertificateBlockedException;
import es.gob.fire.server.connector.FIReCertificateException;
import es.gob.fire.server.connector.FIReConnector;
import es.gob.fire.server.connector.FIReConnectorFactoryException;
import es.gob.fire.server.connector.FIReConnectorNetworkException;
import es.gob.fire.server.connector.FIReConnectorUnknownUserException;
import es.gob.fire.server.connector.WeakRegistryException;
import es.gob.fire.server.services.FIReError;
import es.gob.fire.server.services.Responser;
import es.gob.fire.signature.ConfigManager;
import es.gob.fire.statistics.entity.Browser;


/**
 * Servlet para la selecci&oacute;n del proveedor local o en la nube con el
 * que se desea firmar.
 */
public class ChooseCertificateOriginService extends HttpServlet {

	/** Serial Id. */
	private static final long serialVersionUID = -1367908808211903695L;

	private static final Logger LOGGER = Logger.getLogger(ChooseCertificateOriginService.class.getName());

	private static final String URL_ENCODING = "utf-8"; //$NON-NLS-1$

	@Override
	protected void service(final HttpServletRequest request, final HttpServletResponse response) {

		// Ya que este es uno de los puntos de entrada del usuario a la operativa de FIRe, se establece aqui
		// el tiempo maximo de sesion
		setSessionMaxAge(request);

		// Obtenemos los datos proporcionados por parametro
		final String transactionId = request.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID);
		final String subjectRef = request.getParameter(ServiceParams.HTTP_PARAM_SUBJECT_REF);
		final String origin = request.getParameter(ServiceParams.HTTP_PARAM_CERT_ORIGIN);
		final boolean originForced = Boolean.parseBoolean(request.getParameter(ServiceParams.HTTP_PARAM_CERT_ORIGIN_FORCED));
		String redirectErrorUrl = request.getParameter(ServiceParams.HTTP_PARAM_ERROR_URL);

		// No se guardaran los resultados en cache
		response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); //$NON-NLS-1$ //$NON-NLS-2$

		final TransactionAuxParams trAux = new TransactionAuxParams(null, transactionId);
		final LogTransactionFormatter logF = trAux.getLogFormatter();

		LOGGER.fine(logF.f("Inicio de la llamada al servicio publico de seleccion de origen")); //$NON-NLS-1$

		// Comprobamos que se haya indicado la URL a la que redirigir en caso de error
		if (redirectErrorUrl == null || redirectErrorUrl.isEmpty()) {
			LOGGER.warning(logF.f("No se ha proporcionado la URL de error")); //$NON-NLS-1$
			Responser.sendError(response, FIReError.FORBIDDEN);
			return;
		}
		try {
        	redirectErrorUrl = URLDecoder.decode(redirectErrorUrl, URL_ENCODING);
        }
        catch (final Exception e) {
        	LOGGER.warning(logF.f("No se pudo deshacer el URL Encoding de la URL de redireccion: %1s", e)); //$NON-NLS-1$
		}

		// Comprobamos que se haya indicado el identificador de transaccion
		if (transactionId == null || transactionId.isEmpty()) {
			LOGGER.warning("No se ha proporcionado el identificador de transaccion"); //$NON-NLS-1$
			Responser.redirectToExternalUrl(redirectErrorUrl, request, response, trAux);
			return;
		}

		// Comprobamos que se haya indicado el identificador de usuario
		if (subjectRef == null || subjectRef.isEmpty()) {
			LOGGER.warning(logF.f("No se ha proporcionado la referencia del usuario")); //$NON-NLS-1$
			Responser.redirectToExternalUrl(redirectErrorUrl, request, response, trAux);
			return;
		}

		// Cargamos los datos de sesion
		FireSession session = SessionCollector.getFireSessionOfuscated(transactionId, subjectRef, request.getSession(false), false, false, trAux);
		if (session == null) {
			LOGGER.severe(logF.f("No existe sesion vigente asociada a la transaccion")); //$NON-NLS-1$
			Responser.redirectToExternalUrl(redirectErrorUrl, request, response, trAux);
			return;
		}

		// Si la operacion anterior no fue de solicitud de firma, forzamos a que se recargue por si faltan datos
		if (SessionFlags.OP_SIGN != session.getObject(ServiceParams.SESSION_PARAM_PREVIOUS_OPERATION)) {
			session = SessionCollector.getFireSessionOfuscated(transactionId, subjectRef, request.getSession(false), false, true, trAux);
		}

		// Terminamos de configurar el objeto auxiliar de la transaccion
		final String appId = session.getString(ServiceParams.SESSION_PARAM_APPLICATION_ID);
		trAux.setAppId(appId);

		final TransactionConfig connConfig =
				(TransactionConfig) session.getObject(ServiceParams.SESSION_PARAM_CONNECTION_CONFIG);
		// Usamos la URL de error indicada en la transaccion
		if (connConfig == null || !connConfig.isDefinedRedirectErrorUrl()) {
			LOGGER.severe(logF.f("No se encontro en la sesion la URL de redireccion de error para la operacion")); //$NON-NLS-1$
			Responser.redirectToExternalUrl(redirectErrorUrl, request, response, trAux);
			return;
		}
		redirectErrorUrl = connConfig.getRedirectErrorUrl();

    	// Si llegamos hasta aqui usando correctamente el API, es que no se produjo ningun
    	// error al autenticar al usuario (si es que el conector requeria autenticacion),
		// asi que podemos eliminar el valor bandera que nos indicaba que habiamos sido
		// redirigidos para evitar confundir posibles errores futuros con esta misma transaccion
    	session.removeAttribute(ServiceParams.SESSION_PARAM_REDIRECTED_LOGIN);

		// Comprobamos que se haya indicado el origen del certificado
		if (origin == null || origin.isEmpty()) {
			LOGGER.warning(logF.f("No se ha proporcionado el origen del certificado")); //$NON-NLS-1$
			ErrorManager.setErrorToSession(session, FIReError.FORBIDDEN, trAux);
			Responser.redirectToExternalUrl(redirectErrorUrl, request, response, trAux);
			return;
		}

		// Identificamos el navegador para uso de las estadisticas
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
		if (ProviderManager.PROVIDER_NAME_LOCAL.equalsIgnoreCase(origin)) {
			signWithClienteAfirma(session, connConfig, request, response, originForced, trAux);
		}
		// Si no se selecciono firma local, se firmara con un proveedor de firma en la nube
		else {
			final String subjectId = session.getString(ServiceParams.SESSION_PARAM_SUBJECT_ID);
			signWithProvider(origin, subjectId, transactionId, session, connConfig, request, response, originForced, trAux);
		}

		LOGGER.fine(logF.f("Fin de la llamada al servicio publico de seleccion de origen")); //$NON-NLS-1$
	}

	/**
	 * Establece el tiempo maximo de vida de la sesi&oacute;n del usuario.
	 * @param request Petici&oacute;n realizada al servicio.
	 */
	private static void setSessionMaxAge(final HttpServletRequest request) {
		final HttpSession httpSession = request.getSession(false);
		if (httpSession != null) {
			httpSession.setMaxInactiveInterval((int) (FireSession.MAX_INACTIVE_INTERVAL / 1000));
		}
	}

	/**
	 * Ejecuta y gestiona el flujo de firma con el Cliente @firma.
	 * @param session Datos de la transacci&oacute;n.
	 * @param connConfig Configuraci&oacute;n indicada por la llamada desde el cliente.
	 * @param request Petici&oacute;n HTTP realizada al servicio.
	 * @param response Objeto HTTP de respuesta del servicio.
	 * @param originForced Indica si se forz&oacute; el uso de un proveedor concreto.
	 * @param trAux Informaci&oacute;n auxiliar de la transacci&oacute;n.
	 */
	private static void signWithClienteAfirma(final FireSession session, final TransactionConfig connConfig,
			final HttpServletRequest request, final HttpServletResponse response, final boolean originForced,
			final TransactionAuxParams trAux) {

		final boolean skipSelection = connConfig.isAppSkipCertSelection() != null ?
				connConfig.isAppSkipCertSelection().booleanValue() : ConfigManager.isSkipCertSelection();

		if (skipSelection) {
			final Properties props = (Properties) session.getObject(ServiceParams.SESSION_PARAM_EXTRA_PARAM);
			props.put(MiniAppletHelper.AFIRMA_EXTRAPARAM_HEADLESS, "true");  //$NON-NLS-1$
			session.setAttribute(ServiceParams.SESSION_PARAM_EXTRA_PARAM, props);
		}

		SessionCollector.commit(session, trAux);

		try {
			request.getRequestDispatcher(FirePages.PG_CLIENTE_AFIRMA).forward(request, response);
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, trAux.getLogFormatter().f("Error al redirigir al usuario"), e); //$NON-NLS-1$
			ErrorManager.setErrorToSession(session, FIReError.INTERNAL_ERROR, originForced, trAux);
			redirectToErrorPage(originForced, connConfig, request, response, trAux);
			return;
		}
	}

	/**
	 * Redirige el flujo de ejecuci&oacute;n para la firma con los certificados
	 * en la nube del proveedor indicado.
	 * @param providerName Nombre del proveedor de firma en la nube que se debe utilizar.
	 * @param subjectId Identificador del usuario.
	 * @param trId Identificador de la transacci&oacute;n.
	 * @param session Datos de la transacci&oacute;n.
	 * @param request Petici&oacute;n HTTP realizada al servicio.
	 * @param response Objeto HTTP de respuesta del servicio.
	 * @param errorUrl URL a la que redirigir en caso de error hasta que se obtenga la de sesi&oacute;n.
	 * @param originForced Indica si se forz&oacute; el uso de un proveedor concreto.
	 * @param trAux Informaci&oacute;n auxiliar de la transacci&oacute;n.
	 */
	private static void signWithProvider(final String providerName, final String subjectId,
			final String trId, final FireSession session, final TransactionConfig connConfig,
			final HttpServletRequest request, final HttpServletResponse response,
			final boolean originForced, final TransactionAuxParams trAux) {

		final LogTransactionFormatter logF = trAux.getLogFormatter();

		// Listamos los certificados del usuario
		X509Certificate[] certificates = null;
		try {
			LOGGER.info(logF.f("Se ha seleccionado el proveedor " + providerName.replaceAll("[\r\n]",""))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			final FIReConnector connector = ProviderManager.getProviderConnector(
					providerName,
					connConfig.getProperties()
			);
	        LOGGER.info(logF.f("Se ha cargado el conector " + connector.getClass().getName())); //$NON-NLS-1$
			certificates = connector.getCertificates(subjectId);
			if (certificates == null || certificates.length == 0) {
				if (connector.allowRequestNewCerts()) {
					LOGGER.info(logF.f("El usuario no dispone de certificados, pero el conector le permite generarlos")); //$NON-NLS-1$
					// Guardamos en la sesion compartida los datos agregados hasta ahora
					SessionCollector.commit(session, trAux);
					request.getRequestDispatcher(FirePages.PG_CHOOSE_CERTIFICATE_NOCERT).forward(request, response);
				}
				else {
					LOGGER.log(Level.WARNING, logF.f("El usuario no dispone de certificados y el conector no le permite generarlos")); //$NON-NLS-1$
					ErrorManager.setErrorToSession(session, FIReError.CERTIFICATE_NO_CERTS, originForced, trAux);
					redirectToErrorPage(originForced, connConfig, request, response, trAux);
				}
				return;
			}
		}
		catch (final FIReConnectorFactoryException e) {
			LOGGER.log(Level.SEVERE, logF.f("No se ha podido cargar el conector del proveedor de firma: %1s", providerName), e); //$NON-NLS-1$
			ErrorManager.setErrorToSession(session, FIReError.INTERNAL_ERROR, trAux);
			redirectToErrorPage(originForced, connConfig, request, response, trAux);
			return;
		}
		catch (final FIReCertificateException e) {
			LOGGER.log(Level.SEVERE, logF.f("No se han podido recuperar los certificados del usuario " + subjectId), e); //$NON-NLS-1$
			ErrorManager.setErrorToSession(session, FIReError.CERTIFICATE_ERROR, originForced, trAux);
			redirectToErrorPage(originForced, connConfig, request, response, trAux);
			return;
		}
		catch (final FIReConnectorNetworkException e) {
			LOGGER.log(Level.SEVERE, logF.f("No se ha podido conectar con el proveedor de firma en la nube"), e); //$NON-NLS-1$
			AlarmsManager.notify(Alarm.CONNECTION_SIGNATURE_PROVIDER, providerName);
            ErrorManager.setErrorToSession(session, FIReError.PROVIDER_INACCESIBLE_SERVICE, originForced, trAux);
            redirectToErrorPage(originForced, connConfig, request, response, trAux);
			return;
		}
		catch (final CertificateBlockedException e) {
			LOGGER.log(Level.WARNING, logF.f("Los certificados del usuario " + subjectId + " estan bloqueados: " + e)); //$NON-NLS-1$ //$NON-NLS-2$
			ErrorManager.setErrorToSession(session, FIReError.CERTIFICATE_BLOCKED, originForced, trAux);
			redirectToErrorPage(originForced, connConfig, request, response, trAux);
			return;
		}
		catch (final WeakRegistryException e) {
			LOGGER.log(Level.WARNING, logF.f("El usuario " + subjectId + " realizo un registro debil: " + e)); //$NON-NLS-1$ //$NON-NLS-2$
			ErrorManager.setErrorToSession(session, FIReError.CERTIFICATE_WEAK_REGISTRY, originForced, trAux);
			redirectToErrorPage(originForced, connConfig, request, response, trAux);
			return;
		}
		catch (final FIReConnectorUnknownUserException e) {
			LOGGER.log(Level.WARNING, logF.f("El usuario " + subjectId + " no esta registrado en el sistema: " + e)); //$NON-NLS-1$ //$NON-NLS-2$
			ErrorManager.setErrorToSession(session, FIReError.UNKNOWN_USER, originForced, trAux);
			redirectToErrorPage(originForced, connConfig, request, response, trAux);
			return;
		}
		catch (final ServletException e) {
			LOGGER.log(Level.SEVERE, logF.f("Error al redirigir al usuario"), e); //$NON-NLS-1$
			ErrorManager.setErrorToSession(session, FIReError.INTERNAL_ERROR, originForced, trAux);
			redirectToErrorPage(originForced, connConfig, request, response, trAux);
			return;
		}
		catch (final Error e) {
			LOGGER.log(Level.SEVERE, logF.f("Error grave, probablemente relacionado con la inicializacion del conector"), e); //$NON-NLS-1$
			ErrorManager.setErrorToSession(session, FIReError.INTERNAL_ERROR, originForced, trAux);
			redirectToErrorPage(originForced, connConfig, request, response, trAux);
			return;
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, logF.f("Error indeterminado al recuperar los certificados del usuario " + subjectId), e); //$NON-NLS-1$
			ErrorManager.setErrorToSession(session, FIReError.PROVIDER_ERROR, originForced, trAux);
			redirectToErrorPage(originForced, connConfig, request, response, trAux);
			return;
		}

		// Adjuntamos los certificados a la sesion para que los reciba el JSP
		session.setAttribute(trId + "-certs", certificates); //$NON-NLS-1$
		SessionCollector.commit(session, trAux);

		final boolean skipSelection = connConfig.isAppSkipCertSelection() != null ?
				connConfig.isAppSkipCertSelection().booleanValue() : ConfigManager.isSkipCertSelection();

		// Creamos una nueva instancia del proveedor para obtener su informacion
		final ProviderInfo providerInfo = ProviderManager.getProviderInfo(providerName);

		final boolean certSelectionInProvider = providerInfo.isCertSelectionInProvider();

		try {
			if (certificates.length == 1 && (skipSelection || certSelectionInProvider)) {
				request.setAttribute(ServiceParams.HTTP_ATTR_CERT, Base64.encode(certificates[0].getEncoded(), true));
				request.getRequestDispatcher(ServiceNames.PUBLIC_SERVICE_PRESIGN).forward(request, response);
			} else {
				request.getRequestDispatcher(FirePages.PG_CHOOSE_CERTIFICATE).forward(request, response);
			}
		}
		catch (final CertificateEncodingException e) {
			LOGGER.log(Level.SEVERE, logF.f("Error al codificar el certificado en Base64"), e); //$NON-NLS-1$
			ErrorManager.setErrorToSession(session, FIReError.SIGNING, originForced, trAux);
			redirectToErrorPage(originForced, connConfig, request, response, trAux);
			return;
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, logF.f("Error al redirigir al usuario"), e); //$NON-NLS-1$
			ErrorManager.setErrorToSession(session, FIReError.INTERNAL_ERROR, originForced, trAux);
			redirectToErrorPage(originForced, connConfig, request, response, trAux);
			return;
		}
	}

	/**
	 * Redirige a una p&aacute;gina de error. La p&aacute;gina sera de de error de firma, si existe la posibilidad de
	 * que se pueda reintentar la operaci&oacute;n, o la p&aacute;gina de error proporcionada por el usuario.
	 * @param originForced Indica si era obligatorio el uso de un proveedor de firma concreto.
	 * @param connConfig Configuraci&oacute;n de la transacci&oacute;n.
	 * @param request Objeto de petici&oacute;n al servlet.
	 * @param response Objeto de respuesta del servlet.
	 * @param trAux Informaci&oacute;n auxiliar de la transacci&oacute;n.
	 *
	 */
	private static void redirectToErrorPage(final boolean originForced, final TransactionConfig connConfig,
			final HttpServletRequest request, final HttpServletResponse response, final TransactionAuxParams trAux) {
		if (originForced) {
			Responser.redirectToExternalUrl(connConfig.getRedirectErrorUrl(), request, response, trAux);
		}
		else {
			try {
				request.getRequestDispatcher(FirePages.PG_SIGNATURE_ERROR).forward(request, response);
			}
			catch (final Exception e) {
				Responser.redirectToExternalUrl(connConfig.getRedirectErrorUrl(), request, response, trAux);
			}
		}
	}
}
