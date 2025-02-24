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
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import es.gob.fire.server.services.FIReError;
import es.gob.fire.server.services.LogUtils;
import es.gob.fire.server.services.RequestParameters;
import es.gob.fire.server.services.Responser;

/**
 * Servlet que redirige a la autenticacion de usuarios para la obtenci&oacute;n
 * de certificados en la nube.
 */
public class AuthenticationService extends HttpServlet {

	/** Serial ID. */
	private static final long serialVersionUID = -1459483346687635386L;

	private static final Logger LOGGER = Logger.getLogger(AuthenticationService.class.getName());


	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException {

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
		
		// Obtenemos los datos proporcionados por parametro
		final String trId = params.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID);
		final String subjectRef = params.getParameter(ServiceParams.HTTP_PARAM_SUBJECT_REF);
		String redirectErrorUrl = params.getParameter(ServiceParams.HTTP_PARAM_ERROR_URL);

		final TransactionAuxParams trAux = new TransactionAuxParams(null, LogUtils.limitText(trId));
		final LogTransactionFormatter logF = trAux.getLogFormatter();

		// Comprobamos que se haya indicado el identificador de transaccion
		if (trId == null || trId.isEmpty()) {
			LOGGER.warning(logF.f("No se ha proporcionado el identificador de transaccion")); //$NON-NLS-1$
			Responser.sendError(response, FIReError.FORBIDDEN);
			return;
		}

		// Comprobamos que se haya indicado el identificador de usuario
		if (subjectRef == null || subjectRef.isEmpty()) {
			LOGGER.warning(logF.f("No se ha proporcionado la referencia del usuario")); //$NON-NLS-1$
			Responser.sendError(response, FIReError.FORBIDDEN);
			return;
		}

		// Comprobamos que se haya indicado la URL a la que redirigir en caso de error
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

		// Cargamos los datos de sesion
		final FireSession session = SessionCollector.getFireSessionOfuscated(trId, subjectRef, request.getSession(false), false, true, trAux);
		if (session == null) {
			LOGGER.warning(logF.f("La transaccion %1s no se ha inicializado o ha caducado. Se redirige a la pagina proporcionada en la llamada", LogUtils.cleanText(trId))); //$NON-NLS-1$
			Responser.redirectToExternalUrl(redirectErrorUrl, request, response, trAux);
			return;
		}

		// Terminamos de configurar el formateador para los logs
		final String appId = session.getString(ServiceParams.SESSION_PARAM_APPLICATION_ID);
		trAux.setAppId(appId);

		// Si llegamos hasta aqui, es que no se produjo ningun error al autenticar al usuario,
		// asi que podemos eliminar el valor bandera que nos indicaba que habiamos sido
		// redirigidos para evitar confundir posibles errores futuros con esta misma
		// transaccion
		session.removeAttribute(ServiceParams.SESSION_PARAM_REDIRECTED_LOGIN);

		final String provName = session.getString(ServiceParams.SESSION_PARAM_CERT_ORIGIN);

		ProviderBusiness.signWithCloudProvider(provName, trId, session, request, response, trAux);
	}
}
