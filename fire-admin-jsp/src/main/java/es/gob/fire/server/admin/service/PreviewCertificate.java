package es.gob.fire.server.admin.service;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import es.gob.fire.server.admin.tool.Utils;

/**
 * Servlet implementation class PreviewCertificate
 */
public class PreviewCertificate extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final String PARAM_CER_PRINCIPAL = "fichero-firma-prin"; //$NON-NLS-1$
	private static final String PARAM_CER_BKUP="fichero-firma-resp";//$NON-NLS-1$
	private static final String PARAM_OP = "op"; //$NON-NLS-1$
	private static final String X509 = "X.509"; //$NON-NLS-1$


	private X509Certificate cert=null;


    /**
     * @see HttpServlet#HttpServlet()
     */
    public PreviewCertificate() {
        super();
    }

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

		/*Obtenemos el par�metro enviado del formulario junto con el Certificado*/
		this.getParameters(request);
		//Obtener el tipo de operaci�n 1-Alta 2-Edici�n
		final int op = Integer.parseInt(request.getParameter(PARAM_OP));

		String txtCert=null;
		if(this.getCert()!=null) {
			Date expDate= new Date();
			expDate=this.getCert().getNotAfter();
			txtCert = this.getCert().getSubjectX500Principal().getName().concat(", Fecha de Caducidad=").concat(Utils.getStringDateFormat(expDate));		 //$NON-NLS-1$
		}

		if(txtCert!=null) {
			response.setContentType("text/html"); //$NON-NLS-1$
			final String[] datCertificate=txtCert.split(","); //$NON-NLS-1$
			String certData=""; //$NON-NLS-1$
			for (int i=0;i<= datCertificate.length-1;i++){
				certData=certData.concat(datCertificate[i]).concat("</br>");//$NON-NLS-1$
			}
			response.getWriter().write(certData);
		}
		else {
			response.sendRedirect("Certificate/NewCertificate.jsp?error=true&op="+op); //$NON-NLS-1$
		}

	}

	/**
	 * Obtiene los parámetros enviados al servicio
	 * @param req
	 * @throws IOException
	 * @throws ServletException
	 */
	private void getParameters(final HttpServletRequest req) throws IOException, ServletException {
		try {
	        final List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(req);
	        for (final FileItem item : items) {

	        	if (!item.isFormField() && (PARAM_CER_PRINCIPAL.equals(item.getFieldName()) || PARAM_CER_BKUP.equals(item.getFieldName()) )&& item.getInputStream()!=null && item.getSize() > 0L) {
	        		final InputStream isFileContent = item.getInputStream();
	        		this.setCert((X509Certificate) CertificateFactory.getInstance(X509).generateCertificate(isFileContent));
	        		isFileContent.close();
	        		}

	        	}

		}
	    catch (final FileUploadException e) {
	    	throw new ServletException("Error al procesar el fichero", e); //$NON-NLS-1$
	    }
		catch (final CertificateException e) {
			throw new ServletException("Error al procesar el certificado", e); //$NON-NLS-1$
	    }
	}

	public final X509Certificate getCert() {
		return this.cert;
	}

	public final void setCert(final X509Certificate cert) {
		this.cert = cert;
	}


}
