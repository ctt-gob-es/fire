/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.admin;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
	private static final String PARAM_CER = "cert-resp"; //$NON-NLS-1$
	private static final String PARAM_OP = "op"; //$NON-NLS-1$

	@Override
	protected void doPost(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {

		req.setCharacterEncoding("utf-8"); //$NON-NLS-1$

		final String name = req.getParameter(PARAM_NAME);
		final String res = req.getParameter(PARAM_RESP);
		final String email = req.getParameter(PARAM_EMAIL);
		final String tel = req.getParameter(PARAM_TEL);
		final String cer = req.getParameter(PARAM_CER);
		final int op = Integer.parseInt(req.getParameter(PARAM_OP));

		final String stringOp = op == 1 ? "alta" : "edicion" ; //$NON-NLS-1$ //$NON-NLS-2$
		// tenemos el certificado en base 64 en String.
		// tenemos que sacar la huella
		try {
			final MessageDigest md = MessageDigest.getInstance("SHA-1"); //$NON-NLS-1$
			final byte[] der = CertificateFactory.getInstance("X.509").generateCertificate( //$NON-NLS-1$
					new ByteArrayInputStream(Base64.decode(cer))).getEncoded();
			md.update(der);
			final byte[] digest = md.digest();
			final String huella = Base64.encode(digest);

			boolean isOk = true;
			if (name == null || res == null) {
				LOGGER.log(Level.SEVERE,
						"No se han proporcionado todos los datos requeridos para el alta de la aplicacion (nombre y responsable)"); //$NON-NLS-1$
				isOk = false;
			} else {
				// nueva aplicacion
				if (op == 1){
				LOGGER.info("Alta de la aplicacion con nombre: " + name); //$NON-NLS-1$
					try {
						AplicationsDAO.createApplication(name, res, email, tel, cer, huella);
					} catch (final Exception e) {
						LOGGER.log(Level.SEVERE, "Error en el alta de la aplicacion", e); //$NON-NLS-1$
						isOk = false;
					}
				}
				// editar aplicacion
				else if (op == 2){
					LOGGER.info("Edicion de la aplicacion con nombre: " + name); //$NON-NLS-1$
					AplicationsDAO.updateApplication(req.getParameter("iddApp"), name, res, email, tel, cer, huella);//$NON-NLS-1$
				}
				else{
					throw new IllegalStateException("Estado no permitido");//$NON-NLS-1$
				}

			}

			resp.sendRedirect("AdminMainPage.jsp?op=" + stringOp + "&r=" + (isOk ? "1" : "0")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}
		catch (final IllegalArgumentException e){
			LOGGER.log(Level.SEVERE,"Ha ocurrido un error con el base64 : " + e, e); //$NON-NLS-1$
			resp.sendRedirect("NewApplication.jsp?error=true&name=" + name + "&res=" + res + "&email=" + email + "&tel=" + tel + "&cer" + cer); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		}
		catch (final CertificateException e){
			LOGGER.log(Level.SEVERE,"Ha ocurrido un error al decodificar el certificado : " + e, e); //$NON-NLS-1$
			resp.sendRedirect("NewApplication.jsp?error=true&name="+name+"&res="+res+"&email="+email+"&tel="+tel+"&cer="+cer+"&op="+op); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE,"Ha ocurrido un error crear la aplicacion : " + e, e); //$NON-NLS-1$
			resp.sendRedirect("AdminMainPage.jsp?op=alta&r=0"); //$NON-NLS-1$
		}

	}
}
