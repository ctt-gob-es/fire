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
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.gob.fire.client.SignOperationResult;

/**
 * Servicio para la firma de datos.
 */
public class SignatureService extends HttpServlet {

	/** Serial Id. */
	private static final long serialVersionUID = 1991462934952495784L;

	private static final Logger LOGGER = LoggerFactory.getLogger(SignatureService.class);

	private static final String REDIRECT_SUCCESS_PAGE = "RecoverSignatureService"; //$NON-NLS-1$
	private static final String REDIRECT_ERROR_PAGE = "ErrorTransactionPage.jsp"; //$NON-NLS-1$

	@Override
	protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

		final HttpSession session = request.getSession(false);
		if ( session == null || session.getAttribute("user") == null) { //$NON-NLS-1$
			response.sendRedirect("Login.jsp"); //$NON-NLS-1$
			return;
		}

		final ConfigManager configManager = ConfigManager.getInstance();

		// El identificador de aplicacion es propio de cada aplicacion. En esta de ejemplo,
		// se lee del fichero de configuracion
		final String appId = configManager.getAppId();

		// Obtenemos el ID del usuario activo (lo teniamos guardado en la sesion
		final String userId = (String) session.getAttribute("user"); //$NON-NLS-1$

		// Recobemos los parametros para la operacion. A excepcion de los datos,
		// normalmente estos parametros estaran prefijados para cada aplicacion
		OperationConfig config;
		try {
			config = getOperationConfig(request);
		}
		catch (final Exception e) {
			request.getRequestDispatcher("Sign.jsp?attributes=fail").forward(request, response); //$NON-NLS-1$
			return;
		}

		final String op = config.getCryptoOperation();
		final String format = config.getFormat();
		final String algorithm = config.getAlgorithm();
		final String extraparams = config.getExtraParamsB64();
		final String upgrade = config.getUpgrade();
		final byte[] data = config.getData();

		if (op == null || op.isEmpty() || format == null || format.isEmpty() ||
				algorithm == null || algorithm.isEmpty() || data == null || data.length == 0) {
			request.getRequestDispatcher("Sign.jsp?attributes=fail").forward(request, response); //$NON-NLS-1$
			return;
		}

		// Guardamos en sesion el formato de firma para despues permitir descargar la firma con la
		// extension adecuada
		session.setAttribute("format", format); //$NON-NLS-1$

		// Guardamos en la sesion el formato de actualizacion porque no se
		// usara hasta despues de completar la firma
		session.setAttribute("upgrade", upgrade); //$NON-NLS-1$

		// Configuramos el comportamiento de la aplicacion:
		// - redirectOkUrl: URL a la que debe redirigirse al usuario si la firma se completa correctamente.
		// - redirectErrorUrl: URL a la que debe redirigirse al usuario si ocurre un error durante la firma.
		// - procedureName: Nombre del procedimiento dentro del cual se realiza la firma. Debera haberse dado
		//					de alta al solicitar los permisos de la aplicacion para el acceso a los certificados
		//					en la nube.
        final Properties confProperties = new Properties();
        // Configuramos la URL de nuestra aplicacion a la que redirigir en caso de exito en la firma (Obligatorio)
        confProperties.setProperty("redirectOkUrl", configManager.addUrlBase(REDIRECT_SUCCESS_PAGE)); //$NON-NLS-1$
        // Configuramos la URL de nuestra aplicacion a la que redirigir en caso de error en la firma (Obligatorio)
        confProperties.setProperty("redirectErrorUrl", configManager.addUrlBase(REDIRECT_ERROR_PAGE)); //$NON-NLS-1$

        // Configuramos el nombre del procedimiento de cara a la GISS (Obligatorio si se desea usar el proveedor de Cl@ve Firma)
        if (configManager.getProcedureName() != null) {
        	confProperties.setProperty("procedureName", configManager.getProcedureName()); //$NON-NLS-1$
        }

        // Configuramos si el certificado es local o de Cl@ve Firma (Opcional)
        if (configManager.getCertOrigin() != null) {
        	confProperties.setProperty("certOrigin", configManager.getCertOrigin()); //$NON-NLS-1$
        }

        // Configuramos el nombre de la aplicacion (Opcional)
        if (configManager.getAppName() != null) {
        	confProperties.setProperty("appName", configManager.getAppName()); //$NON-NLS-1$
        }

        // Configuramos la omision del certificado (Opcional)
        if (configManager.isSkipCertSelection()) {
        	confProperties.setProperty("skipCertSelection", Boolean.TRUE.toString()); //$NON-NLS-1$
        }

        // Desactivacion del periodo de gracia (puede ser necesario si se configura politica de firma)
       	confProperties.setProperty("updater.ignoreGracePeriod", Boolean.TRUE.toString()); //$NON-NLS-1$

        // Configuracion del nombre y titulo del documento
//        confProperties.setProperty("docTitle", "Mi titulo"); //$NON-NLS-1$ //$NON-NLS-2$
//        confProperties.setProperty("docName", "Mi nombre"); //$NON-NLS-1$ //$NON-NLS-2$

        // Podemos configurar un DocumentManager configurado en el componente central.
        // Con esto, en lugar de tomar el campo datos que le pasamos a la aplicacion,
        // se le pasara este campo al DocumentManager configurado para que lo use de
        // identificador
//        confProperties.setProperty("docManager", "filesystem"); //$NON-NLS-1$ //$NON-NLS-2$


		// Utilizamos el cliente distribuido para solicitar la operacion de firma
		SignOperationResult signResult;
		try {
			signResult = configManager.getFireClient(appId).sign(
					userId,
					op,
					format,
					algorithm,
					extraparams,
					Base64.encode(data, true),
					confProperties);
		}
		catch (final Exception e) {
			LOGGER.error("Error durante la operacion de firma", e); //$NON-NLS-1$
	    	response.sendRedirect("ErrorPage.jsp?msg=" + URLEncoder.encode("Error en la llamada a la operacion de firma:<br>" + e.toString(), "utf-8")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$));
	    	return;
		}

		// Del resultado obtenemos:
		// - El identificador de transaccion necesario para recuperar la firma generada.
		// - La URL a la que deberemos redirigir al usuario para que se autentique.
		final String transactionId = signResult.getTransactionId();
		final String redirectUrl = signResult.getRedirectUrl();

		// Guardamos el ID de transaccion en la sesion para despues poder recuperar la firma
		if (transactionId != null) {
			session.setAttribute("transactionId", transactionId); //$NON-NLS-1$
		}

		// Redirigimos la usuario
		response.sendRedirect(redirectUrl);
	}

	/**
	 * Recoge la configuraci&oacute;n de firma transmitida.
	 * @param request Petici&oacute;n.
	 * @return Configuraci&oacute;n de la operaci&oacute;n de firma.
	 * @throws ServletException Cuando ocurre un error en el parseo de la petici&oacute;n.
	 * @throws IOException Cuando ocurre un error en la lectura del fichero.
	 * @throws IllegalArgumentException Cuando no se recibe el fichero.
	 */
	private static OperationConfig getOperationConfig(final HttpServletRequest request) throws ServletException, IOException {
		final OperationConfig config = new OperationConfig();
		try {
	        final List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
	        for (final FileItem item : items) {
	        	if (item.isFormField() && "operation".equals(item.getFieldName())) { //$NON-NLS-1$
	        		config.setCryptoOperation(item.getString());
	        	} else if (item.isFormField() && "algorithm".equals(item.getFieldName())) { //$NON-NLS-1$
	        		config.setAlgorithm(item.getString());
	        	} else if (item.isFormField() && "format".equals(item.getFieldName())) { //$NON-NLS-1$
	        		config.setFormat(item.getString());
	        	} else if (item.isFormField() && "extraParams".equals(item.getFieldName())) { //$NON-NLS-1$
	        		config.setExtraParamsB64(item.getString());
	        	} else if (item.isFormField() && "upgrade".equals(item.getFieldName())) { //$NON-NLS-1$
	        		config.setUpgrade(item.getString());
	        	} else if (!item.isFormField() && "sign-file".equals(item.getFieldName())) { //$NON-NLS-1$
	        		try (final InputStream fileContent = item.getInputStream()) {
	        			config.setData(Utils.getDataFromInputStream(fileContent));
	        		}
	        	}
	        }
	    } catch (final Exception e) {
	        throw new ServletException("Error al procesar los parametros de la peticion", e); //$NON-NLS-1$
	    }

		return config;
	}

	private static class OperationConfig {

		private byte[] data;

		private String cryptoOperation;
		private String algorithm;
		private String format;
		private String extraParamsB64;
		private String upgrade;

		OperationConfig() {
			this.data = null;
		}

		byte[] getData() {
			return this.data;
		}

		void setData(final byte[] data) {
			this.data = data;
		}

		String getCryptoOperation() {
			return this.cryptoOperation;
		}

		void setCryptoOperation(final String cryptoOperation) {
			this.cryptoOperation = cryptoOperation;
		}

		public String getAlgorithm() {
			return this.algorithm;
		}

		public void setAlgorithm(final String algorithm) {
			this.algorithm = algorithm;
		}

		String getFormat() {
			return this.format;
		}

		void setFormat(final String format) {
			this.format = format;
		}

		String getExtraParamsB64() {
			return this.extraParamsB64;
		}

		void setExtraParamsB64(final String extraParamsB64) {
			this.extraParamsB64 = extraParamsB64;
		}

		public String getUpgrade() {
			return this.upgrade;
		}

		public void setUpgrade(final String upgrade) {
			this.upgrade = upgrade;
		}
	}
}
