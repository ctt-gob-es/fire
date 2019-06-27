package es.gob.fire.server.admin.service;

import java.io.ByteArrayInputStream;
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
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import es.gob.fire.server.admin.dao.CertificatesDAO;
import es.gob.fire.server.admin.tool.Base64;

/**
 * Servlet implementation class NewCertificateService
 */
public class NewCertificateService extends HttpServlet {


	/** Serial Id. */
	private static final long serialVersionUID = -2026882282382737471L;

	private static final Logger LOGGER = Logger.getLogger(NewCertificateService.class.getName());

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

		final HttpSession session = request.getSession(false);
		if (session == null) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		// Obtenemos el tipo de operacion 1-Alta 2-Edicion
		int op;
		try {
			op = Integer.parseInt(request.getParameter(ServiceParams.PARAM_OP_CERT));
			if (op != 1 && op != 2) {
				throw new IllegalArgumentException();
			}
		} catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Operacion no soportada: " + LogUtils.cleanText(request.getParameter(ServiceParams.PARAM_OP_CERT))); //$NON-NLS-1$
			response.sendRedirect("Certificate/CertificatePage.jsp?op=alta&r=0&ent=cer"); //$NON-NLS-1$
			return;
		}

		final String stringOp = op == 1 ? "alta" : "edicion" ;  //$NON-NLS-1$//$NON-NLS-2$;

		// Obtenemos los parametros enviados del formulario junto con el Certificado
		Parameters params;
		try {
			params = getParameters(request);
		}
		catch (final Exception e) {
			LOGGER.log(Level.WARNING, "Se han proporcionado parametros invalidos", e); //$NON-NLS-1$
			response.sendRedirect("Certificate/CertificatePage.jsp?op=" + stringOp + "&r=0&ent=cer"); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}

		if (params.getName() == null || params.getCert_prin() == null && params.getCert_resp() == null) {
			LOGGER.log(Level.WARNING,
					"No se han proporcionado todos los datos obligatorios (nombre y un certificado)"); //$NON-NLS-1$
			response.sendRedirect("Certificate/CertificatePage.jsp?op=" + stringOp + "&r=0&ent=cer"); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}

		// Calculamos la huella de los certificados
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-1"); //$NON-NLS-1$
		} catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Se intenta calcular la huella de los certificados con un algoritmo no soportado: " + e); //$NON-NLS-1$
			response.sendRedirect("Certificate/NewCertificate.jsp?op=" + op + "&r=0&ent=cer"); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
		if (params.getCert_prin() != null) {
			final byte[] digest = md.digest(params.getCert_prin());
			params.setHuella_prin(Base64.encode(digest));
		}
		if (params.getCert_resp() != null) {
			final byte[] digest = md.digest(params.getCert_resp());
			params.setHuella_resp(Base64.encode(digest));
		}

		// Nuevo certificado
		if (op == 1){
			LOGGER.info("Alta del certificado con nombre: " + params.getName()); //$NON-NLS-1$
			try {
				CertificatesDAO.createCertificate(
						params.getName(),
						params.getB64Cert_prin(),
						params.getHuella_prin(),
						params.getB64Cert_resp(),
						params.getHuella_resp());
			} catch (final Exception e) {
				LOGGER.log(Level.SEVERE, "Error en el alta del certificado", e); //$NON-NLS-1$
				response.sendRedirect("Certificate/NewCertificate.jsp?op=" + op + "&r=0&ent=cer"); //$NON-NLS-1$ //$NON-NLS-2$
				return;
			}
		}
		// Editar certificado
		else if (op == 2) {
			LOGGER.info("Edicion del certificado con nombre: " + params.getName()); //$NON-NLS-1$
			try {
				CertificatesDAO.updateCertificate(
						params.getIdCert(),
						params.getName(),
						params.getB64Cert_prin(),
						params.getHuella_prin(),
						params.getB64Cert_resp(),
						params.getHuella_resp());
			} catch (final Exception e) {
				LOGGER.log(Level.SEVERE, "Error en el alta del certificado", e); //$NON-NLS-1$
				response.sendRedirect("Certificate/CertificatePage.jsp?op=" + stringOp + "&r=0&ent=cer"); //$NON-NLS-1$ //$NON-NLS-2$
				return;
			}
		}
		response.sendRedirect("Certificate/CertificatePage.jsp?op=" + stringOp + "&r=1&ent=cer"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Procedimiento que obtine los datos de los par&aacute;metros enviados desde el formulario para  a&ntilde;adir o editar certificado .
	 * Par&aacute;metros:id-cert, nombre-cer, fichero-firma-prin, fichero-firma-resp
	 * @param req
	 * @throws IOException
	 * @throws ServletException
	 */
	private Parameters getParameters(final HttpServletRequest req) throws IOException, ServletException {


		final Parameters params = new Parameters();

		try {
			if(req.getParameter(ServiceParams.PARAM_ID_CERT) != null && !"".equals(req.getParameter(ServiceParams.PARAM_ID_CERT))) {//$NON-NLS-1$
				params.setIdCert(req.getParameter(ServiceParams.PARAM_ID_CERT));
			}

			final CertificateFactory certFactory = CertificateFactory.getInstance("X.509"); //$NON-NLS-1$

	        final List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(req);
	        for (final FileItem item : items) {
	        	if (item.isFormField() && ServiceParams.PARAM_NAME_CERT.equals(item.getFieldName())) {
	        		params.setName(item.getString());
	        	}
	        	else if (!item.isFormField() && ServiceParams.PARAM_CER_PRIN.equals(item.getFieldName()) && item.getInputStream() != null && item.getSize() > 0L) {
	        		X509Certificate cert;
	        		try (final InputStream certIs = item.getInputStream();) {
	        			cert = (X509Certificate) certFactory.generateCertificate(certIs);
	        		}
	        		final byte[] certEncoded = cert.getEncoded();
	        		params.setCert_prin(certEncoded);
	        		params.setB64Cert_prin(Base64.encode(certEncoded));
	        	}
	        	else if (!item.isFormField() && ServiceParams.PARAM_CER_RESP.equals(item.getFieldName()) && item.getInputStream() != null && item.getSize() > 0L) {
	        		X509Certificate cert;
	        		try (final InputStream certIs = item.getInputStream();) {
	        			cert = (X509Certificate) certFactory.generateCertificate(certIs);
	        		}
	        		final byte[] certEncoded = cert.getEncoded();
	        		params.setCert_resp(certEncoded);
	        		params.setB64Cert_resp(Base64.encode(certEncoded));
	        	}

	        	else if (item.isFormField() && ServiceParams.PARAM_CERB64PRIM.equals(item.getFieldName()) && item.getSize() > 0L) {
	        		final X509Certificate cert = (X509Certificate)certFactory.generateCertificate(new ByteArrayInputStream(Base64.decode(item.getString())));
	        		params.setB64Cert_prin(item.getString());
	        		params.setCert_prin(cert.getEncoded());
	        	}
	        	else if (item.isFormField() && ServiceParams.PARAM_CERB64RESP.equals(item.getFieldName()) && item.getSize() > 0L) {
	        		final X509Certificate cert = (X509Certificate)certFactory.generateCertificate(new ByteArrayInputStream(Base64.decode(item.getString())));
	        		params.setB64Cert_resp(item.getString());
	        		params.setCert_resp(cert.getEncoded());

	        	}
	        }
		}
	    catch (final FileUploadException e) {
	    	throw new ServletException("Error al procesar el fichero", e); //$NON-NLS-1$
	    }
		catch (final CertificateException e) {
			throw new ServletException("Error al procesar el certificado", e); //$NON-NLS-1$
	    }

		return params;
	}

	class Parameters {

		private String idCert = null;
		private String name = null;
		private String b64Cert_prin = null;
		private String b64Cert_resp = null;
		private byte[] cert_prin = null;
		private byte[] cert_resp = null;
		private String huella_prin = null;
		private String huella_resp = null;

		/**
		 * Obtiene el nombre del certificado
		 * @return
		 */
		final String getName() {
			return this.name;
		}
		/**
		 *  Establece el nombre del certificado
		 * @return
		 */
		final byte[] getCert_prin() {
			return this.cert_prin;
		}
		/**
		 * Establece los datos del certificado principal
		 * @param cert_prin
		 */
		final void setCert_prin(final byte[] cert_prin) {
			this.cert_prin = cert_prin;
		}
		/**
		 * Obtiene los datos del certificado respaldo o backup
		 * @return
		 */
		final byte[] getCert_resp() {
			return this.cert_resp;
		}
		/**
		 * Establece los datos del certificado respaldo o backup
		 * @param cert_resp
		 */
		final void setCert_resp(final byte[] cert_resp) {
			this.cert_resp = cert_resp;
		}
		/**
		 * Establece el nombre del certificado
		 * @param name
		 */
		final void setName(final String name) {
			this.name = name;
		}
		/**
		 * Obtiene los datos del certificado principal en Base64
		 * @return
		 */
		final String getB64Cert_prin() {
			return this.b64Cert_prin;
		}
		/**
		 * Establece los datos del certificado principal en Base64
		 */
		final void setB64Cert_prin(final String b64Cert_prin) {
			this.b64Cert_prin = b64Cert_prin;
		}
		/**
		 * Obtiene los datos del certificado respaldo en Base64
		 * @return
		 */
		final String getB64Cert_resp() {
			return this.b64Cert_resp;
		}
		/**
		 * Establece los datos del certificado respaldo en Base64
		 * @param b64Cert_resp
		 */
		final void setB64Cert_resp(final String b64Cert_resp) {
			this.b64Cert_resp = b64Cert_resp;
		}
		/**
		 * Obtiene la huella del certificado principal
		 * @return
		 */
		final String getHuella_prin() {
			return this.huella_prin;
		}
		/**
		 * Establece la huella del certificado principal
		 */
		final void setHuella_prin(final String huella_prin) {
			this.huella_prin = huella_prin;
		}
		/**
		 * Obtiene la huella del certificado respaldo
		 * @return
		 */
		final String getHuella_resp() {
			return this.huella_resp;
		}
		/**
		 * Establece la huella del certificado respaldo
		 */
		final void setHuella_resp(final String huella_resp) {
			this.huella_resp = huella_resp;
		}
		/**
		 * Obtiene el id del certificado
		 * @return
		 */
		final String getIdCert() {
			return this.idCert;
		}
		/**
		 * Establece el id del certificado
		 */
		final void setIdCert(final String idCert) {
			this.idCert = idCert;
		}
	}
}
