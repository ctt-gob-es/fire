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
	
	private static final String PARAM_ID="id-cert";
	private static final String PARAM_NAME="nombre-cer";
	private static final String PARAM_CER_PRIN="fichero-firma-prin";
	private static final String PARAM_CER_RESP="fichero-firma-resp";
	
	private static final String PARAM_CERB64PRIM="b64CertPrin";
	private static final String PARAM_CERB64RESP="b64CertBkup";
	
	private static final String PARAM_OP = "op"; //$NON-NLS-1$
	private static final String X509 = "X.509"; //$NON-NLS-1$
	
	
	private String idCert=null;
	private String name=null;
	private String b64Cert_prin=null;
	private String b64Cert_resp=null;
	private X509Certificate cert_prin=null;
	private X509Certificate cert_resp=null;
	private String huella_prin =null;
	private String huella_resp =null;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public NewCertificateService() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub		
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	
		
		/*Obtenemos los parámetros enviados del formulario junto con el Certificado*/
		this.getParameters(request);
		//Obtener el tipo de operación 1-Alta 2-Edición
		final int op = Integer.parseInt(request.getParameter(PARAM_OP));//$NON-NLS-1$
		final String stringOp = op == 1 ? "alta" : "edicion" ; //$NON-NLS-2$
		// tenemos el certificado en base 64 en String.
		// tenemos que sacar la huella
		try {
					
			final MessageDigest md = MessageDigest.getInstance("SHA-1");
			byte[] digest;//$NON-NLS-1$
			
			//Comprobar que se ha cargado el Certificado principal nuevo.
			//Obtenemos la huella de dicho certificado
			if(this.getCert_prin()!=null) {
				final byte[] der =this.getCert_prin().getEncoded();
				md.update(der);
				digest = md.digest();
				this.setHuella_prin(Base64.encode(digest));
			}
			if(this.getCert_resp()!=null) {
				final byte[] der =this.getCert_resp().getEncoded();
				md.update(der);
				digest = md.digest();
				this.setHuella_resp(Base64.encode(digest)); 
			}
			
			
			
			boolean isOk = true;
			if (this.getName() == null && this.getCert_prin()== null && this.getCert_resp()==null ) {
				LOGGER.log(Level.SEVERE,
						"No se han proporcionado todos los datos requeridos para el alta del certificado (nombre y certificado principal)"); //$NON-NLS-1$
				isOk = false;
			} else {
				// nuevo certificado
				if (op == 1){
				LOGGER.info("Alta del certificado con nombre: " + this.getName()); //$NON-NLS-1$
					try {
						CertificatesDAO.createCertificate(this.getName(), this.getB64Cert_prin(), this.getHuella_prin(), this.getB64Cert_resp(), this.getHuella_resp());
						
					} catch (final Exception e) {
						LOGGER.log(Level.SEVERE, "Error en el alta del certificado", e); //$NON-NLS-1$
						isOk = false;
					}
				}
				// editar certificado
				else if (op == 2){
					LOGGER.info("Edicion del certificado con nombre: " + this.getName()); //$NON-NLS-1$

					final String b64CertPrin = this.getB64Cert_prin()!=null? this.getB64Cert_prin() : null;
					final String b64CertResp = this.getB64Cert_resp()!=null? this.getB64Cert_resp() : null;	
				
					CertificatesDAO.updateCertificate(this.getIdCert(), this.getName(), b64CertPrin, this.getHuella_prin(), b64CertResp, this.getHuella_resp() );
									
				}
			
				else{
					throw new IllegalStateException("Estado no permitido");//$NON-NLS-1$
				}

			}

			response.sendRedirect("Certificate/CertificatePage.jsp?op=" + stringOp + "&r=" + (isOk ? "1" : "0")+"&ent=cer"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}
		catch (final IllegalArgumentException e){
			LOGGER.log(Level.SEVERE,"Ha ocurrido un error con el base64 : " + e, e); //$NON-NLS-1$
			response.sendRedirect("Certificate/NewCertificate.jsp?error=true&name=" + this.getName()+"&op="+op); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		}
		catch (final CertificateException e){
			LOGGER.log(Level.SEVERE,"Ha ocurrido un error al decodificar el certificado : " + e, e); //$NON-NLS-1$
			response.sendRedirect("Certificate/NewCertificate.jsp?error=true&name="+this.getName()+"&op="+op); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE,"Ha ocurrido un error crear la aplicacion : " + e, e); //$NON-NLS-1$
			response.sendRedirect("Certificate/NewCertificate.jsp?op="+op+"&r=0&ent=cer"); //$NON-NLS-1$
		}

				
		
	}

	/**
	 * Procedimiento que obtine los datos de los parámetros enviados desde el formulario para añadir o editar certificado .
	 * Parámetros:id-cert, nombre-cer, fichero-firma-prin, fichero-firma-resp
	 * @param req
	 * @throws IOException
	 * @throws ServletException
	 */
	private void getParameters(HttpServletRequest req) throws IOException, ServletException {
		
		this.setB64Cert_prin(null);
		this.setB64Cert_resp(null);
		this.setCert_prin(null);
		this.setCert_resp(null);
		this.setHuella_prin(null);
		this.setHuella_resp(null);
		
		try {
			if(req.getParameter(PARAM_ID)!=null && !"".equals(req.getParameter(PARAM_ID))) {
				this.setIdCert(req.getParameter(PARAM_ID));
			}
			
	        final List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(req);
	        for (final FileItem item : items) {
	        	if (item.isFormField() && PARAM_NAME.equals(item.getFieldName())) { //$NON-NLS-1$
	        		this.setName(item.getString());
	        	}
	        	else if (!item.isFormField() && PARAM_CER_PRIN.equals(item.getFieldName()) && item.getInputStream()!=null && item.getSize() > 0L) { //$NON-NLS-1$
	        		final InputStream isFileContent = item.getInputStream(); 	        			        		       				
	        		this.setCert_prin((X509Certificate) CertificateFactory.getInstance(X509).generateCertificate(isFileContent));
	        		isFileContent.close();
	        		final InputStream isCert = item.getInputStream(); 	
	        		final byte[] bCert=Utils.getDataFromInputStream(isCert);
	        		this.setB64Cert_prin(Base64.encode(bCert));	        		
	        		isCert.close();
	        		}
	        	else if (!item.isFormField() && PARAM_CER_RESP.equals(item.getFieldName()) && item.getInputStream()!=null && item.getSize() > 0L) { //$NON-NLS-1$
	        		final InputStream isFileContent = item.getInputStream(); 	        			        		       				
	        		this.setCert_resp((X509Certificate) CertificateFactory.getInstance(X509).generateCertificate(isFileContent));
	        		isFileContent.close();
	        		final InputStream isCert = item.getInputStream(); 	
	        		final byte[] bCert=Utils.getDataFromInputStream(isCert);
	        		this.setB64Cert_resp(Base64.encode(bCert));	        		
	        		isCert.close();
	        		}
	        	else if (item.isFormField() && PARAM_CERB64PRIM.equals(item.getFieldName()) && item.getSize() > 0L) { //$NON-NLS-1$	
	        		CertificateFactory certFactory = CertificateFactory.getInstance(X509);
	        		X509Certificate cert = (X509Certificate)certFactory.generateCertificate(new ByteArrayInputStream(Base64.decode(item.getString())));
	        		this.setCert_prin(cert);	        			        		        			        		        		       			        		       					        		
	        		this.setB64Cert_prin(item.getString());	        			
	        		}
	        	else if (item.isFormField() && PARAM_CERB64RESP.equals(item.getFieldName()) && item.getSize() > 0L) { //$NON-NLS-1$	  
	        		CertificateFactory certFactory = CertificateFactory.getInstance(X509);
	        		X509Certificate cert = (X509Certificate)certFactory.generateCertificate(new ByteArrayInputStream(Base64.decode(item.getString())));
	        		this.setCert_resp(cert);	
	        		this.setB64Cert_resp(item.getString());	        			
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
	private final String getName() {
		return name;
	}

	private final X509Certificate getCert_prin() {
		return cert_prin;
	}

	private final void setCert_prin(X509Certificate cert_prin) {
		this.cert_prin = cert_prin;
	}

	private final X509Certificate getCert_resp() {
		return cert_resp;
	}

	private final void setCert_resp(X509Certificate cert_resp) {
		this.cert_resp = cert_resp;
	}

	private final void setName(String name) {
		this.name = name;
	}

	private final String getB64Cert_prin() {
		return b64Cert_prin;
	}

	private final void setB64Cert_prin(String b64Cert_prin) {
		this.b64Cert_prin = b64Cert_prin;
	}

	private final String getB64Cert_resp() {
		return b64Cert_resp;
	}

	private final void setB64Cert_resp(String b64Cert_resp) {
		this.b64Cert_resp = b64Cert_resp;
	}

	private final String getHuella_prin() {
		return huella_prin;
	}

	private final void setHuella_prin(String huella_prin) {
		this.huella_prin = huella_prin;
	}

	private final String getHuella_resp() {
		return huella_resp;
	}

	private final void setHuella_resp(String huella_resp) {
		this.huella_resp = huella_resp;
	}

	private final String getIdCert() {
		return idCert;
	}

	private final void setIdCert(String idCert) {
		this.idCert = idCert;
	}

	
}
