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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import es.gob.fire.client.DuplicateDocumentException;
import es.gob.fire.client.FireClient;
import es.gob.fire.client.NumDocumentsExceededException;

/**
 * Servicio para agregar documentos a un lote de firma.
 */
public class AddDocumentBatchService extends HttpServlet {

	/** Serial Id. */
	private static final long serialVersionUID = 1117256372054541704L;

	private static final Logger LOGGER = Logger.getLogger(AddDocumentBatchService.class.getName());

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
			LOGGER.severe("No se ha encontrado id de transaccion iniciada"); //$NON-NLS-1$
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
			LOGGER.log(Level.SEVERE, "No se han podido obtener los datos del documento", e); //$NON-NLS-1$
			request.getRequestDispatcher("AddDocumentToBatch.jsp?error=errordocument").forward(request, response); //$NON-NLS-1$
			return;
		}

		if (doc.getId() == null || doc.getId().isEmpty()) {
			LOGGER.warning("No se ha establecido un identificado para el documento"); //$NON-NLS-1$
			request.getRequestDispatcher("AddDocumentToBatch.jsp?attributes=fail&error=noid").forward(request, response); //$NON-NLS-1$
			return;
		}

		if (doc.getData() == null || doc.getData().length == 0) {
			LOGGER.warning("No se ha adjuntado el documento al formulario"); //$NON-NLS-1$
			request.getRequestDispatcher("AddDocumentToBatch.jsp?attributes=fail&error=nodocument").forward(request, response); //$NON-NLS-1$
			return;
		}

		final Properties config = new Properties();
		config.setProperty("docTitle", "T\u00EDtulo " + doc.getId()); //$NON-NLS-1$ //$NON-NLS-2$
		config.setProperty("docName", "Nombre " + doc.getId()); //$NON-NLS-1$ //$NON-NLS-2$


		// Si se indico una configuracion de firma particular para el documento, llamamos al metodo
		// del API que permite introducirla. En caso contrario, llamamos al metodo del API que no
		try {
			final FireClient fireClient = ConfigManager.getInstance().getFireClient(appId);
			if (doc.isCustomConfig()) {
				fireClient.addDocumentToBatch(transactionId, userId, doc.getId(), doc.getData(),
						config, doc.getCryptoOperation(), doc.getFormat(),
						doc.getExtraParamsB64(), doc.getUpgrade());
			}
			else {
				fireClient.addDocumentToBatch(transactionId, userId, doc.getId(), doc.getData(), config);
			}
		} catch (final DuplicateDocumentException e) {
			LOGGER.log(Level.SEVERE, "El identificador de documento ya se utilizo para otro documento del lote", e); //$NON-NLS-1$
			request.getRequestDispatcher("AddDocumentToBatch.jsp?error=duplid").forward(request, response); //$NON-NLS-1$
			return;
		} catch (final NumDocumentsExceededException e) {
			LOGGER.log(Level.SEVERE, "Se excedio el numero maximo de documentos configurados para el lote", e); //$NON-NLS-1$
			request.getRequestDispatcher("AddDocumentToBatch.jsp?error=maxdocs").forward(request, response); //$NON-NLS-1$
			return;
		} catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Ocurrio un error al agregar el documento al lote", e); //$NON-NLS-1$
			response.sendRedirect("ErrorPage.jsp?msg=" + e.getMessage()); //$NON-NLS-1$
			return;
		}

		// Al completar la carga del fichero guardamos un indicador en la sesion
		// para en la pagina habilitar la funcion de firma del lote
		session.setAttribute("fileLoaded", Boolean.TRUE.toString()); //$NON-NLS-1$

		// Guardamos los nombres de fichero cargados
		List<String> docNames = (List<String>) session.getAttribute("docNames"); //$NON-NLS-1$
		if (docNames == null) {
			docNames = new ArrayList<String>();
		}
		docNames.add(doc.getName());
		session.setAttribute("docNames", docNames); //$NON-NLS-1$

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
	        		final InputStream fileContent = item.getInputStream();
	        		doc.setName(item.getName());
	        		doc.setData(Utils.getDataFromInputStream(fileContent));
	        		fileContent.close();
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
