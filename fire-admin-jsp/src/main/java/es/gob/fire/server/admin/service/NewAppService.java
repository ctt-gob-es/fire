/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.admin.service;


import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import es.gob.fire.server.admin.dao.AplicationsDAO;
import es.gob.fire.server.admin.entity.Application;
import es.gob.fire.server.admin.tool.Base64;
import es.gob.fire.server.admin.tool.Utils;





/**
 * Servicio para el alta de una nueva aplicaci&oacute;n en el sistema.
 */
@WebServlet("/newApp")
public class NewAppService extends HttpServlet {

	/** Serial Id. */
	private static final long serialVersionUID = -6304364862591344482L;

	private static final Logger LOGGER = Logger.getLogger(NewAppService.class.getName());

	private static final String PARAM_NAME = "nombre-app"; //$NON-NLS-1$
	private static final String PARAM_RESP = "nombre-resp"; //$NON-NLS-1$
	private static final String PARAM_EMAIL = "email-resp"; //$NON-NLS-1$
	private static final String PARAM_TEL = "telf-resp"; //$NON-NLS-1$
	
	private static final String PARAM_CER_FILE = "cert-file"; //$NON-NLS-1$
	private static final String PARAM_OP = "op"; //$NON-NLS-1$
	private static final String X509 = "X.509"; //$NON-NLS-1$
	
	private String name=null;
	private String res = null;
	private String email = null;
	private String tel = null;	
	
	private String b64Cert=null;
	private X509Certificate cert=null;
	
	@Override
	protected void doPost(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {

		req.setCharacterEncoding("utf-8"); //$NON-NLS-1$

		
		/*Obtenemos los parámetros enviados del formulario junto con el Certificado*/
		this.getParameters(req);
		
		//Obtener el tipo de operación 1-Alta 2-Edición
		final int op = Integer.parseInt(req.getParameter(PARAM_OP));//$NON-NLS-1$
		final String stringOp = op == 1 ? "alta" : "edicion" ; //$NON-NLS-2$
		// tenemos el certificado en base 64 en String.
		// tenemos que sacar la huella
		try {
			
			
			final MessageDigest md = MessageDigest.getInstance("SHA-1");
			byte[] digest;//$NON-NLS-1$
			String huella =null;
			//Comprobar que se ha cargado el Certificado nuevo.
			//Obtenemos la huella de dicho certificado
			if(this.getCert()!=null) {
				final byte[] der =this.getCert().getEncoded();
				md.update(der);
				digest = md.digest();
				huella = Base64.encode(digest);
			}
				
			boolean isOk = true;
			if (this.getName() == null || this.getRes() == null) {
				LOGGER.log(Level.SEVERE,
						"No se han proporcionado todos los datos requeridos para el alta de la aplicacion (nombre y responsable)"); //$NON-NLS-1$
				isOk = false;
			} else {
				// nueva aplicacion
				if (op == 1){
				LOGGER.info("Alta de la aplicacion con nombre: " + this.getName()); //$NON-NLS-1$
					try {
						AplicationsDAO.createApplication(this.getName(), this.getRes(), this.getEmail(), this.getTel(), this.getB64Cert(), huella);
					} catch (final Exception e) {
						LOGGER.log(Level.SEVERE, "Error en el alta de la aplicacion", e); //$NON-NLS-1$
						isOk = false;
					}
				}
				// editar aplicacion
				else if (op == 2){
					LOGGER.info("Edicion de la aplicacion con nombre: " + this.getName()); //$NON-NLS-1$
					/*Se realiza consulta de los datos previos y para los casos en los que no se haya cargado un certificado
					 * nuevo, se indican en la consulta de actualización los datos anteriores para el certificado y la huella */
					final Application appData=AplicationsDAO.selectApplication(req.getParameter("iddApp"));
					final String b64Cert = this.getB64Cert()!=null? this.getB64Cert() : appData.getCer();				
					if(huella==null && appData.getHuella()!=null){
						huella=appData.getHuella();
					}
					
					AplicationsDAO.updateApplication(req.getParameter("iddApp"), this.getName(), this.getRes(), this.getEmail(), this.getTel(), b64Cert, huella);//$NON-NLS-1$
				}
				else{
					throw new IllegalStateException("Estado no permitido");//$NON-NLS-1$
				}

			}

			resp.sendRedirect("AdminMainPage.jsp?op=" + stringOp + "&r=" + (isOk ? "1" : "0")+"&ent=app"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}
		catch (final IllegalArgumentException e){
			LOGGER.log(Level.SEVERE,"Ha ocurrido un error con el base64 : " + e, e); //$NON-NLS-1$
			resp.sendRedirect("NewApplication.jsp?error=true&name=" + this.getName() + "&res=" + this.getRes() + "&email=" + this.getEmail() + "&tel=" + this.getTel() + "&cer" + this.getB64Cert()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		}
		catch (final CertificateException e){
			LOGGER.log(Level.SEVERE,"Ha ocurrido un error al decodificar el certificado : " + e, e); //$NON-NLS-1$
			resp.sendRedirect("NewApplication.jsp?error=true&name="+this.getName()+"&res="+this.getRes()+"&email="+this.getEmail()+"&tel="+this.getTel()+"&cer="+this.getB64Cert()+"&op="+op); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE,"Ha ocurrido un error crear la aplicacion : " + e, e); //$NON-NLS-1$
			resp.sendRedirect("AdminMainPage.jsp?op=alta&r=0&ent=app"); //$NON-NLS-1$
		}

					
		
	}

	/**
	 * Procedimiento que obtine los datos de los parámetros enviados desde el formulario para añadir aplicación.
	 * Parámetros: nombre-app, nombre-resp,email-resp, telf-resp y cert-file
	 * Através del certificado cargado (cert-file) se obtiene el contenido en base64
	 * @param req
	 * @throws IOException
	 * @throws ServletException
	 */
	private void getParameters(HttpServletRequest req) throws IOException, ServletException {
		try {
	        final List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(req);
	        for (final FileItem item : items) {
	        	if (item.isFormField() && PARAM_NAME.equals(item.getFieldName())) { //$NON-NLS-1$
	        		this.setName(item.getString());
	        	} else if (item.isFormField() && PARAM_RESP.equals(item.getFieldName())) { //$NON-NLS-1$
	        		this.setRes(item.getString());	        		
	        	} else if (item.isFormField() && PARAM_EMAIL.equals(item.getFieldName())) { //$NON-NLS-1$        		
	        		this.setEmail(item.getString());
	        	} else if (item.isFormField() && PARAM_TEL.equals(item.getFieldName())) { //$NON-NLS-1$	        	
	        		this.setTel(item.getString());
	        	} else if (!item.isFormField() && PARAM_CER_FILE.equals(item.getFieldName()) && item.getInputStream()!=null && item.getSize() > 0L) { //$NON-NLS-1$
	        		final InputStream isFileContent = item.getInputStream(); 	        			        		       				
	        		this.setCert((X509Certificate) CertificateFactory.getInstance(X509).generateCertificate(isFileContent));
	        		isFileContent.close();
	        		final InputStream isCert = item.getInputStream(); 	
	        		final byte[] bCert=Utils.getDataFromInputStream(isCert);
	        		this.setB64Cert(Base64.encode(bCert));	        		
	        		isCert.close();
	        		}
	        	}
		}
	    catch (final FileUploadException e) {
	    	throw new ServletException("Error al procesar el fichero", e); //$NON-NLS-1$
	    }
		catch (CertificateException e) {
			throw new ServletException("Error al procesar el certificado", e); //$NON-NLS-1$
	    }	
	}

	// Getters and Setters
	private String getName() {
		return name;
	}

	private void setName(String name) {
		this.name = name;
	}

	private String getRes() {
		return res;
	}

	private void setRes(String res) {
		this.res = res;
	}

	private String getEmail() {
		return email;
	}

	private void setEmail(String email) {
		this.email = email;
	}

	private String getTel() {
		return tel;
	}

	private void setTel(String tel) {
		this.tel = tel;
	}

	private String getB64Cert() {
		return b64Cert;
	}

	private void setB64Cert(String b64Cert) {
		this.b64Cert = b64Cert;
	}

	private X509Certificate getCert() {
		return cert;
	}

	private void setCert(X509Certificate cert) {
		this.cert = cert;
	}
	
	
}
