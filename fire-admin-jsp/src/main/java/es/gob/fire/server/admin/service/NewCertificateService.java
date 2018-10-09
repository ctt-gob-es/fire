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
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import es.gob.fire.server.admin.dao.CertificatesDAO;
import es.gob.fire.server.admin.tool.Base64;
import es.gob.fire.server.admin.tool.Utils;

/**
 * Servlet implementation class NewCertificateService
 */
@WebServlet("/newCert")
public class NewCertificateService extends HttpServlet {


	/**
	 *
	 */
	private static final long serialVersionUID = -2026882282382737471L;

	private static final Logger LOGGER = Logger.getLogger(NewCertificateService.class.getName());

	private static final String PARAM_ID = "id-cert"; //$NON-NLS-1$
	private static final String PARAM_NAME = "nombre-cer";//$NON-NLS-1$
	private static final String PARAM_CER_PRIN = "fichero-firma-prin";//$NON-NLS-1$
	private static final String PARAM_CER_RESP = "fichero-firma-resp";//$NON-NLS-1$

	private static final String PARAM_CERB64PRIM = "b64CertPrin";//$NON-NLS-1$
	private static final String PARAM_CERB64RESP = "b64CertBkup";//$NON-NLS-1$

	private static final String PARAM_OP = "op"; //$NON-NLS-1$
	private static final String X509 = "X.509"; //$NON-NLS-1$


	private String idCert = null;
	private String name = null;
	private String b64Cert_prin = null;
	private String b64Cert_resp = null;
	private X509Certificate cert_prin = null;
	private X509Certificate cert_resp = null;
	private String huella_prin = null;
	private String huella_resp = null;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public NewCertificateService() {
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

		/*Obtenemos los parametros enviados del formulario junto con el Certificado*/
		getParameters(request);
		//Obtener el tipo de operacion 1-Alta 2-Edicion
		final int op = Integer.parseInt(request.getParameter(PARAM_OP));
		final String stringOp = op == 1 ? "alta" : "edicion" ;  //$NON-NLS-1$//$NON-NLS-2$
		// tenemos el certificado en base 64 en String.
		// tenemos que sacar la huella
		try {

			final MessageDigest md = MessageDigest.getInstance("SHA-1"); //$NON-NLS-1$
			byte[] digest;

			//Comprobar que se ha cargado el Certificado principal nuevo.
			//Obtenemos la huella de dicho certificado
			if(getCert_prin() != null) {
				final byte[] der = getCert_prin().getEncoded();
				md.update(der);
				digest = md.digest();
				setHuella_prin(Base64.encode(digest));
			}
			if(getCert_resp() != null) {
				final byte[] der = getCert_resp().getEncoded();
				md.update(der);
				digest = md.digest();
				setHuella_resp(Base64.encode(digest));
			}



			boolean isOk = true;
			if (getName() == null && getCert_prin() == null && getCert_resp() == null ) {
				LOGGER.log(Level.SEVERE,
						"No se han proporcionado todos los datos requeridos para el alta del certificado (nombre y certificado principal)"); //$NON-NLS-1$
				isOk = false;
			} else {
				// nuevo certificado
				if (op == 1){
				LOGGER.info("Alta del certificado con nombre: " + getName()); //$NON-NLS-1$
					try {
						CertificatesDAO.createCertificate(getName(), getB64Cert_prin(), getHuella_prin(), getB64Cert_resp(), getHuella_resp());

					} catch (final Exception e) {
						LOGGER.log(Level.SEVERE, "Error en el alta del certificado", e); //$NON-NLS-1$
						isOk = false;
					}
				}
				// editar certificado
				else if (op == 2){
					LOGGER.info("Edicion del certificado con nombre: " + getName()); //$NON-NLS-1$

					final String b64CertPrin = getB64Cert_prin() != null ? getB64Cert_prin() : null;
					final String b64CertResp = getB64Cert_resp() != null ? getB64Cert_resp() : null;

					CertificatesDAO.updateCertificate(getIdCert(), getName(), b64CertPrin, getHuella_prin(), b64CertResp, getHuella_resp() );

				}

				else{
					throw new IllegalStateException("Estado no permitido");//$NON-NLS-1$
				}

			}

			response.sendRedirect("Certificate/CertificatePage.jsp?op=" + stringOp + "&r=" + (isOk ? "1" : "0") + "&ent=cer"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		}
		catch (final IllegalArgumentException e){
			LOGGER.log(Level.SEVERE,"Ha ocurrido un error con el base64 : " + e, e); //$NON-NLS-1$
			response.sendRedirect("Certificate/NewCertificate.jsp?error=true&name=" + getName() + "&op=" + op); //$NON-NLS-1$ //$NON-NLS-2$
		}
		catch (final CertificateException e){
			LOGGER.log(Level.SEVERE,"Ha ocurrido un error al decodificar el certificado : " + e, e); //$NON-NLS-1$
			response.sendRedirect("Certificate/NewCertificate.jsp?error=true&name=" + getName() + "&op=" + op); //$NON-NLS-1$ //$NON-NLS-2$
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE,"Ha ocurrido un error crear la aplicacion : " + e, e); //$NON-NLS-1$
			response.sendRedirect("Certificate/NewCertificate.jsp?op=" + op + "&r=0&ent=cer"); //$NON-NLS-1$ //$NON-NLS-2$
		}



	}

	/**
	 * Procedimiento que obtine los datos de los par&aacute;metros enviados desde el formulario para  a&ntilde;adir o editar certificado .
	 * Par&aacute;metros:id-cert, nombre-cer, fichero-firma-prin, fichero-firma-resp
	 * @param req
	 * @throws IOException
	 * @throws ServletException
	 */
	private void getParameters(final HttpServletRequest req) throws IOException, ServletException {

		setB64Cert_prin(null);
		setB64Cert_resp(null);
		setCert_prin(null);
		setCert_resp(null);
		setHuella_prin(null);
		setHuella_resp(null);

		try {
			if(req.getParameter(PARAM_ID) != null && !"".equals(req.getParameter(PARAM_ID))) {//$NON-NLS-1$
				setIdCert(req.getParameter(PARAM_ID));
			}

	        final List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(req);
	        for (final FileItem item : items) {
	        	if (item.isFormField() && PARAM_NAME.equals(item.getFieldName())) {
	        		setName(item.getString());
	        	}
	        	else if (!item.isFormField() && PARAM_CER_PRIN.equals(item.getFieldName()) && item.getInputStream() != null && item.getSize() > 0L) {
	        		final InputStream isFileContent = item.getInputStream();
	        		setCert_prin((X509Certificate) CertificateFactory.getInstance(X509).generateCertificate(isFileContent));
	        		isFileContent.close();
	        		final InputStream isCert = item.getInputStream();
	        		final byte[] bCert = Utils.getDataFromInputStream(isCert);
	        		setB64Cert_prin(Base64.encode(bCert));
	        		isCert.close();
	        		}
	        	else if (!item.isFormField() && PARAM_CER_RESP.equals(item.getFieldName()) && item.getInputStream() != null && item.getSize() > 0L) {
	        		final InputStream isFileContent = item.getInputStream();
	        		setCert_resp((X509Certificate) CertificateFactory.getInstance(X509).generateCertificate(isFileContent));
	        		isFileContent.close();
	        		final InputStream isCert = item.getInputStream();
	        		final byte[] bCert = Utils.getDataFromInputStream(isCert);
	        		setB64Cert_resp(Base64.encode(bCert));
	        		isCert.close();
	        		}
	        	else if (item.isFormField() && PARAM_CERB64PRIM.equals(item.getFieldName()) && item.getSize() > 0L) {
	        		final CertificateFactory certFactory = CertificateFactory.getInstance(X509);
	        		final X509Certificate cert = (X509Certificate)certFactory.generateCertificate(new ByteArrayInputStream(Base64.decode(item.getString())));
	        		setCert_prin(cert);
	        		setB64Cert_prin(item.getString());
	        		}
	        	else if (item.isFormField() && PARAM_CERB64RESP.equals(item.getFieldName()) && item.getSize() > 0L) {
	        		final CertificateFactory certFactory = CertificateFactory.getInstance(X509);
	        		final X509Certificate cert = (X509Certificate)certFactory.generateCertificate(new ByteArrayInputStream(Base64.decode(item.getString())));
	        		setCert_resp(cert);
	        		setB64Cert_resp(item.getString());
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

	// Getters and Setters
	/**
	 * Obtiene el nombre del certificado
	 * @return
	 */
	private final String getName() {
		return this.name;
	}
	/**
	 *  Establece el nombre del certificado
	 * @return
	 */
	private final X509Certificate getCert_prin() {
		return this.cert_prin;
	}
	/**
	 * Establece los datos del certificado principal
	 * @param cert_prin
	 */
	private final void setCert_prin(final X509Certificate cert_prin) {
		this.cert_prin = cert_prin;
	}
	/**
	 * Obtiene los datos del certificado respaldo o backup
	 * @return
	 */
	private final X509Certificate getCert_resp() {
		return this.cert_resp;
	}
	/**
	 * Establece los datos del certificado respaldo o backup
	 * @param cert_resp
	 */
	private final void setCert_resp(final X509Certificate cert_resp) {
		this.cert_resp = cert_resp;
	}
	/**
	 * Establece el nombre del certificado
	 * @param name
	 */
	private final void setName(final String name) {
		this.name = name;
	}
	/**
	 * Obtiene los datos del certificado principal en Base64
	 * @return
	 */
	private final String getB64Cert_prin() {
		return this.b64Cert_prin;
	}
	/**
	 * Establece los datos del certificado principal en Base64
	 */
	private final void setB64Cert_prin(final String b64Cert_prin) {
		this.b64Cert_prin = b64Cert_prin;
	}
	/**
	 * Obtiene los datos del certificado respaldo en Base64
	 * @return
	 */
	private final String getB64Cert_resp() {
		return this.b64Cert_resp;
	}
	/**
	 * Establece los datos del certificado respaldo en Base64
	 * @param b64Cert_resp
	 */
	private final void setB64Cert_resp(final String b64Cert_resp) {
		this.b64Cert_resp = b64Cert_resp;
	}
	/**
	 * Obtiene la huella del certificado principal
	 * @return
	 */
	private final String getHuella_prin() {
		return this.huella_prin;
	}
	/**
	 * Establece la huella del certificado principal
	 */
	private final void setHuella_prin(final String huella_prin) {
		this.huella_prin = huella_prin;
	}
	/**
	 * Obtiene la huella del certificado respaldo
	 * @return
	 */
	private final String getHuella_resp() {
		return this.huella_resp;
	}
	/**
	 * Establece la huella del certificado respaldo
	 */
	private final void setHuella_resp(final String huella_resp) {
		this.huella_resp = huella_resp;
	}
	/**
	 * Obtiene el id del certificado
	 * @return
	 */
	private final String getIdCert() {
		return this.idCert;
	}
	/**
	 * Establece el id del certificado
	 */
	private final void setIdCert(final String idCert) {
		this.idCert = idCert;
	}


}
