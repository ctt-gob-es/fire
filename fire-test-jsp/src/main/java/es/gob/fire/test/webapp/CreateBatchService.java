/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.test.webapp;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.gob.fire.client.CreateBatchResult;
import es.gob.fire.client.HttpOperationException;

/**
 * Servicio para la creacion de un lote de firmas.
 */
public class CreateBatchService extends HttpServlet {

	/** Serial Id. */
	private static final long serialVersionUID = 9216217964645644004L;

	private static final Logger LOGGER = LoggerFactory.getLogger(CreateBatchService.class);

	private static final String REDIRECT_SUCCESS_PAGE = "RecoverBatch.jsp"; //$NON-NLS-1$
	private static final String REDIRECT_ERROR_PAGE = "ErrorTransactionPage.jsp"; //$NON-NLS-1$

	@Override
	protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

		final HttpSession session = request.getSession(false);
		if ( session == null || session.getAttribute("user") == null) { //$NON-NLS-1$
			response.sendRedirect("Login.jsp"); //$NON-NLS-1$
			return;
		}

		// El identificador de aplicacion es propio de cada aplicacion. En esta de ejemplo,
		// se lee del fichero de configuracion
		final String appId = ConfigManager.getInstance().getAppId();

		// Obtenemos el ID del usuario activo (lo teniamos guardado en la sesion
		final String userId = (String) session.getAttribute("user"); //$NON-NLS-1$

		// Configuramos el comportamiento de la aplicacion:
		// - redirectOkUrl: URL a la que debe redirigirse al usuario si la firma se completa correctamente.
		// - redirectErrorUrl: URL a la que debe redirigirse al usuario si ocurre un error durante la firma.
		// - procedureName: Nombre del procedimiento dentro del cual se realiza la firma. Debera haberse dado
		//					de alta al solicitar los permisos de la aplicacion para el acceso a los certificados
		//					en la nube.
		final ConfigManager configManager = ConfigManager.getInstance();
		final Properties confProperties = new Properties();
		// Configuramos la URL de nuestra aplicacion a la que redirigir en caso de exito en la firma (Obligatorio)
        confProperties.setProperty("redirectOkUrl", configManager.addUrlBase(REDIRECT_SUCCESS_PAGE)); //$NON-NLS-1$
        // Configuramos la URL de nuestra aplicacion a la que redirigir en caso de error en la firma (Obligatorio)
        confProperties.setProperty("redirectErrorUrl", configManager.addUrlBase(REDIRECT_ERROR_PAGE)); //$NON-NLS-1$
        // Configuramos el nombre del procedimiento de cara a la GISS (Obligatorio)
        if (configManager.getProcedureName() != null) {
			confProperties.setProperty("procedureName", configManager.getProcedureName()); //$NON-NLS-1$
		}

        // Configuramos si el certificado es local o de Cl@ve Firma (Opcional)
        if (configManager.getCertOrigin() != null) {
        	confProperties.setProperty("certOrigin", configManager.getCertOrigin()); //$NON-NLS-1$
        }

        // Configuramos el nombre de la aplicacion (opcional)
        if (configManager.getAppName() != null) {
        	confProperties.setProperty("appName", configManager.getAppName()); //$NON-NLS-1$
        }

        if (configManager.getAppName() != null) {
        	confProperties.setProperty("updater.ignoreGracePeriod", "true"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        // Podemos configurar un DocumentManager configurado en el componente central.
        // Con esto, en lugar de tomar los datos que le pasamos a la aplicacion, se cargaran
        // en base al DocumentManager y el identificador que le pasemos como datos
        //confProperties.setProperty("docManager", "filesystem"); //$NON-NLS-1$ //$NON-NLS-2$

		// Recobemos los parametros para la operacion.
		final String op = request.getParameter("operation"); //$NON-NLS-1$
		final String format = request.getParameter("format"); //$NON-NLS-1$
		final String algorithm = request.getParameter("algorithm"); //$NON-NLS-1$
		final String extraparams = request.getParameter("extraParams"); //$NON-NLS-1$
		final String upgrade = request.getParameter("upgrade"); //$NON-NLS-1$

		CreateBatchResult createBatchResult;
		try {
			createBatchResult = ConfigManager.getInstance().getFireClient(appId).createBatchProcess(
					userId, op, format, algorithm, extraparams, upgrade, confProperties);
		} catch (final HttpOperationException e) {
			LOGGER.error("Ocurrio un error al crear un lote", e); //$NON-NLS-1$
			response.sendRedirect("ErrorPage.jsp?msg=" + URLEncoder.encode(e.getMessage(), "utf-8")); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		} catch (final Exception e) {
			LOGGER.error("Ocurrio un error grave al crear un lote", e); //$NON-NLS-1$
			response.sendRedirect("ErrorPage.jsp?msg=" + URLEncoder.encode(e.toString(), "utf-8")); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}

		// Del resultado obtenemos:
		// - El identificador de transaccion necesario para operar con el lote.
		final String transactionId = createBatchResult.getTransactionId();

		// Guardamos el ID de transaccion en la sesion para despues poder recuperar la firma
		session.setAttribute("transactionId", transactionId); //$NON-NLS-1$

		// Guardamos el formato de firma por defecto para poder identificar luego la extension de los ficheros de firma
		session.setAttribute("format", format); //$NON-NLS-1$

		// Redirigimos al usuario a la pagina para agregar ficheros al lote
		response.sendRedirect("AddDocumentToBatch.jsp"); //$NON-NLS-1$
	}
}
