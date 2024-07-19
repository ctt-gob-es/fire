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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.gob.fire.client.DuplicateDocumentException;
import es.gob.fire.client.FireClient;
import es.gob.fire.client.HttpOperationException;
import es.gob.fire.client.NumDocumentsExceededException;

/**
 * Servicio para agregar documentos a un lote de firma.
 */
public class AddDocumentBatchService extends HttpServlet {

	/** Serial Id. */
	private static final long serialVersionUID = 1117256372054541704L;

	private static final Logger LOGGER = LoggerFactory.getLogger(AddDocumentBatchService.class);

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

		final String transactionId = (String) session.getAttribute("transactionId"); //$NON-NLS-1$
		if (transactionId == null) {
			LOGGER.error("No se ha encontrado id de transaccion iniciada"); //$NON-NLS-1$
			response.sendRedirect("Login.jsp"); //$NON-NLS-1$
			return;
		}

		final String userId = (String) session.getAttribute("user"); //$NON-NLS-1$

		final FileDocument doc;
		try {
			doc = getFileData(request);
		}
		catch (final IllegalArgumentException e) {
			return;
		}
		catch (final Exception e) {
			LOGGER.error("No se han podido obtener los datos del documento", e); //$NON-NLS-1$
			request.getRequestDispatcher("AddDocumentToBatch.jsp?error=errordocument").forward(request, response); //$NON-NLS-1$
			return;
		}

		if (doc.getId() == null || doc.getId().isEmpty()) {
			LOGGER.warn("No se ha establecido un identificado para el documento"); //$NON-NLS-1$
			request.getRequestDispatcher("AddDocumentToBatch.jsp?attributes=fail&error=noid").forward(request, response); //$NON-NLS-1$
			return;
		}

		if (doc.getData() == null || doc.getData().length == 0) {
			LOGGER.warn("No se ha adjuntado el documento al formulario"); //$NON-NLS-1$
			request.getRequestDispatcher("AddDocumentToBatch.jsp?attributes=fail&error=nodocument").forward(request, response); //$NON-NLS-1$
			return;
		}

		// Mantendremos un mapa de los formatos de firma empleados para permitir despues identificar
		// la extension que debemos asignar a los ficheros de firma
		Map<String, String> signatureFormats = (Map<String, String>) session.getAttribute("formats");
		if (signatureFormats == null) {
			signatureFormats = new HashMap<>();
		}

		final Properties config = new Properties();

		// Opcional. Titulo y nombre que mostrar al usuario al autorizar la firma
		config.setProperty("docTitle", "T\u00EDtulo " + doc.getId()); //$NON-NLS-1$ //$NON-NLS-2$
		config.setProperty("docName", "Nombre " + doc.getId()); //$NON-NLS-1$ //$NON-NLS-2$

		// Si se indico una configuracion de firma particular para el documento, llamamos al metodo
		// del API que permite introducirla. En caso contrario, llamamos al metodo del API que no
		try {
			final FireClient fireClient = ConfigManager.getInstance().getFireClient(appId);
			if (doc.isCustomConfig()) {
				signatureFormats.put(doc.getId(), doc.getFormat());
				fireClient.addDocumentToBatch(transactionId, userId, doc.getId(), doc.getData(),
						config, doc.getCryptoOperation(), doc.getFormat(),
						doc.getExtraParamsB64(), doc.getUpgrade());
			}
			else {
				signatureFormats.put(doc.getId(), (String) session.getAttribute("format")); //$NON-NLS-1$
				fireClient.addDocumentToBatch(transactionId, userId, doc.getId(), doc.getData(), config);
			}
			session.setAttribute("formats", signatureFormats); //$NON-NLS-1$
		} catch (final DuplicateDocumentException e) {
			LOGGER.error("El identificador de documento ya se utilizo para otro documento del lote", e); //$NON-NLS-1$
			request.getRequestDispatcher("AddDocumentToBatch.jsp?error=duplid").forward(request, response); //$NON-NLS-1$
			return;
		} catch (final NumDocumentsExceededException e) {
			LOGGER.error("Se excedio el numero maximo de documentos configurados para el lote", e); //$NON-NLS-1$
			request.getRequestDispatcher("AddDocumentToBatch.jsp?error=maxdocs").forward(request, response); //$NON-NLS-1$
			return;
		}
		catch (final HttpOperationException e) {
			LOGGER.error("Se devolvio un tipo de error no diferenciado", e); //$NON-NLS-1$
			session.setAttribute("errorMessage", e.getMessage()); //$NON-NLS-1$
			request.getRequestDispatcher("AddDocumentToBatch.jsp?error=maxdocs").forward(request, response); //$NON-NLS-1$
			return;
		}
		catch (final Exception e) {
			LOGGER.error("Ocurrio un error al agregar el documento al lote", e); //$NON-NLS-1$
			response.sendRedirect("ErrorPage.jsp?msg=" + URLEncoder.encode(e.getMessage(), "utf-8")); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}

		// Al completar la carga del fichero guardamos un indicador en la sesion
		// para en la pagina habilitar la funcion de firma del lote
		session.setAttribute("fileLoaded", Boolean.TRUE.toString()); //$NON-NLS-1$

		// Guardamos los nombres de fichero cargados
		List<String> docNames = (List<String>) session.getAttribute("docNames"); //$NON-NLS-1$
		if (docNames == null) {
			docNames = new ArrayList<>();
		}
		docNames.add(doc.getName());
		session.setAttribute("docNames", docNames); //$NON-NLS-1$

		// Guardamos el formato de firma de cada fichero para poder identificar luego la extension de los ficheros de firma
		session.setAttribute("formats", signatureFormats); //$NON-NLS-1$

		// Redirigimos la usuario a la misma pagina
		request.getRequestDispatcher("AddDocumentToBatch.jsp?attributes=success").forward(request, response); //$NON-NLS-1$
	}

	/**
	 * Recoge el fichero de la petici&oacute;n recibida.
	 * @param request petici&oacute;n.
	 * @return Contenido del fichero.
	 * @throws ServletException Cuando ocurre un error en el parseo de la petici&oacute;n.
	 * @throws IOException Cuando ocurre un error en la lectura del fichero.
	 * @throws IllegalArgumentException Cuando no se recibe el fichero.
	 */
	private static FileDocument getFileData(final HttpServletRequest request) throws ServletException, IOException {
		final FileDocument doc = new FileDocument();
		try {
	        final List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
	        for (final FileItem item : items) {
	        	if (item.isFormField() && "batch-file-id".equals(item.getFieldName())) { //$NON-NLS-1$
	        		doc.setId(item.getString());
	        	} else if (item.isFormField() && "custom-config".equals(item.getFieldName())) { //$NON-NLS-1$
	        		// Si recibimos el campo es que esta activado
	        		doc.setCustomConfig(true);
	        	} else if (item.isFormField() && "operation".equals(item.getFieldName())) { //$NON-NLS-1$
	        		doc.setCryptoOperation(item.getString());
	        	} else if (item.isFormField() && "format".equals(item.getFieldName())) { //$NON-NLS-1$
	        		doc.setFormat(item.getString());
	        	} else if (item.isFormField() && "extraParams".equals(item.getFieldName())) { //$NON-NLS-1$
	        		doc.setExtraParamsB64(item.getString());
	        	} else if (item.isFormField() && "upgrade".equals(item.getFieldName())) { //$NON-NLS-1$
	        		doc.setUpgrade(item.getString());
	        	} else if (!item.isFormField() && "batch-file".equals(item.getFieldName())) { //$NON-NLS-1$
	        		try (InputStream fileContent = new BufferedInputStream(item.getInputStream());) {
	        			doc.setName(item.getName());
	        			doc.setData(Utils.getDataFromInputStream(fileContent));
	        		}
	        	}
	        }
	    } catch (final FileUploadException e) {
	        throw new ServletException("Error al procesar el fichero", e); //$NON-NLS-1$
	    }

		return doc;
	}

	private static class FileDocument {

		private String id;
		private byte[] data;
		private String name;

		private boolean customConfig;

		private String cryptoOperation;
		private String format;
		private String extraParamsB64;

		private String upgrade;

		FileDocument() {
			this.id = null;
			this.data = null;
		}

		String getId() {
			return this.id;
		}

		void setId(final String id) {
			this.id = id;
		}

		byte[] getData() {
			return this.data;
		}

		void setData(final byte[] data) {
			this.data = data;
		}

		String getName() {
			return this.name;
		}

		void setName(final String name) {
			this.name = name;
		}

		boolean isCustomConfig() {
			return this.customConfig;
		}

		void setCustomConfig(final boolean customConfig) {
			this.customConfig = customConfig;
		}

		String getCryptoOperation() {
			return this.cryptoOperation;
		}

		void setCryptoOperation(final String cryptoOperation) {
			this.cryptoOperation = cryptoOperation;
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
