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
import java.io.PrintWriter;
import java.util.Properties;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import es.gob.fire.server.services.HttpCustomErrors;
import es.gob.fire.server.services.RequestParameters;

/**
 * Manejador que gestiona las peticiones para iniciar el proceso de firma de un lote.
 */
public class SignBatchManager {

	private static final Logger LOGGER = Logger.getLogger(SignBatchManager.class.getName());

    /**
     * Inicia el proceso de firma de un lote.
	 * @param request Petici&oacute;n de firma del lote.
	 * @param params Par&aacute;metros extra&iacute;dos de la petici&oacute;n.
	 * @param response Respuesta con el resultado del inicio de firma del lote.
	 * @throws IOException Cuando se produce un error de lectura o env&iacute;o de datos.
     */
	public static void signBatch(final HttpServletRequest request, final RequestParameters params, final HttpServletResponse response)
    		throws IOException {

		// Recogemos los parametros proporcionados en la peticion
    	final String transactionId = params.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID);
    	final String subjectId = params.getParameter(ServiceParams.HTTP_PARAM_SUBJECT_ID);
		final String stopOnError = params.getParameter(ServiceParams.HTTP_PARAM_BATCH_STOP_ON_ERROR);

		// Comprobamos que se hayan prorcionado los parametros indispensables
    	if (transactionId == null || transactionId.isEmpty()) {
    		LOGGER.warning("No se ha proporcionado el ID de transaccion"); //$NON-NLS-1$
        	response.sendError(HttpServletResponse.SC_BAD_REQUEST,
    				"No se ha proporcionado el identificador de la transaccion"); //$NON-NLS-1$
    		return;
    	}

    	final FireSession session = SessionCollector.getFireSession(transactionId, subjectId, request.getSession(false), false);
    	if (session == null) {
    		LOGGER.warning("La transaccion no se ha inicializado o ha caducado"); //$NON-NLS-1$
    		response.sendError(HttpCustomErrors.INVALID_TRANSACTION.getErrorCode());
    		return;
    	}

    	// TODO: Borrar esto cuando se terimen los cambios en el componente distribuido PHP
    	// en el que, por ahora, no se envia el subjectId por parametro, asi que hemos de usar
    	// el de sesion. Este impide realizar la comprobacion de seguridad adicional de que sea
    	// el mismo usuario el que crease la transaccion y el que ahora dice ser
    	final String currentUserId = session.getString(ServiceParams.SESSION_PARAM_SUBJECT_ID);

        final Properties connConfig = (Properties) session.getObject(ServiceParams.SESSION_PARAM_CONNECTION_CONFIG);

		// Listamos los certificados del usuario
		if (connConfig == null || connConfig.isEmpty()) {
			LOGGER.warning("No se proporcionaron datos para la conexion con el backend"); //$NON-NLS-1$
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,
					"No se proporcionaron datos para la conexion con el backend"); //$NON-NLS-1$
			return;
        }

		String redirectErrorUrl;
		if (!connConfig.containsKey(ServiceParams.CONNECTION_PARAM_ERROR_URL)) {
			LOGGER.warning("No se proporcionaron las URL de redireccion para la operacion"); //$NON-NLS-1$
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,
					"No se proporcionaron las URL de redireccion para la operacion"); //$NON-NLS-1$
			return;
		}
		redirectErrorUrl = connConfig.getProperty(ServiceParams.CONNECTION_PARAM_ERROR_URL);

		// Identificamos si ya esta definido el origen del certificado
		String origin = null;
		if (connConfig.containsKey(ServiceParams.CONNECTION_PARAM_CERT_ORIGIN)) {
			origin = connConfig.getProperty(ServiceParams.CONNECTION_PARAM_CERT_ORIGIN);
		}

		// Devolvemos la pagina a la que se debe redirigir al usuario
        String redirectUrlBase = request.getRequestURL().toString();
        redirectUrlBase = redirectUrlBase.
        		substring(0, redirectUrlBase.toString().lastIndexOf('/') + 1) + "public/"; //$NON-NLS-1$

        // Si ya se definio el origen del certificado, se envia al servicio que se encarga de
        // redirigirlo. Si no, se envia la pagina de seleccion
        String redirectUrl;
        if (origin != null) {
        	redirectUrl = "chooseCertificateOriginService?" + //$NON-NLS-1$
        			ServiceParams.HTTP_PARAM_CERT_ORIGIN + "=" + origin + "&" + //$NON-NLS-1$ //$NON-NLS-2$
        			ServiceParams.HTTP_PARAM_CERT_ORIGIN_FORCED + "=true"; //$NON-NLS-1$
        } else {
        	redirectUrl = "ChooseCertificateOrigin.jsp?" + ServiceParams.HTTP_PARAM_OPERATION + "=" + ServiceParams.OPERATION_BATCH; //$NON-NLS-1$ //$NON-NLS-2$
        	if (!FIReHelper.isUserRegistered(currentUserId)) {
        		redirectUrl += "&"  + ServiceParams.HTTP_PARAM_USER_NOT_REGISTERED + "=true"; //$NON-NLS-1$ //$NON-NLS-2$
        	}
        }

        // Configuramos en la sesion si se debe detener el proceso de error cuando se encuentre uno
        // para tenerlo en cuenta en este paso y los siguientes
        session.setAttribute(ServiceParams.SESSION_PARAM_BATCH_STOP_ON_ERROR, stopOnError);
        SessionCollector.commit(session);

        final SignOperationResult result = new SignOperationResult(
        		transactionId,
        		redirectUrlBase + redirectUrl +
        			"&" + ServiceParams.HTTP_PARAM_TRANSACTION_ID + "=" + transactionId + //$NON-NLS-1$ //$NON-NLS-2$
        			"&" + ServiceParams.HTTP_PARAM_SUBJECT_ID + "=" + currentUserId + //$NON-NLS-1$ //$NON-NLS-2$
        			"&" + ServiceParams.HTTP_PARAM_ERROR_URL + "=" + redirectErrorUrl); //$NON-NLS-1$ //$NON-NLS-2$

        sendResult(response, result);
	}

	private static void sendResult(final HttpServletResponse response, final SignOperationResult result) throws IOException {
        response.setContentType("application/json"); //$NON-NLS-1$
        final PrintWriter out = response.getWriter();
        out.print(result.toString());
        out.flush();
        out.close();
	}
}
