<%@page import="es.gob.fire.server.admin.entity.CertificateFire"%>
<%@page import="es.gob.fire.server.admin.dao.CertificatesDAO"%>
<%@page import="es.gob.fire.server.admin.dao.AplicationsDAO" %>
<%@page import="es.gob.fire.server.admin.tool.Utils" %>
<%@page import="java.util.Date"%>
<%
String req = request.getParameter("requestType");//$NON-NLS-1$
String data = "";//$NON-NLS-1$

if(req.equals("countRecords")){//$NON-NLS-1$
    data = AplicationsDAO.getApplicationsCount();
}
if(req.equals("All")){//$NON-NLS-1$
    data = AplicationsDAO.getApplicationsJSON();
  
}
if(req.equals("getRecords")){//$NON-NLS-1$
	String start = request.getParameter("currentIndex");//$NON-NLS-1$
	String total = request.getParameter("recordsToFetch");//$NON-NLS-1$
	data = AplicationsDAO.getApplicationsPag(start, total);
}

if(req.equals("getCertificateId")){//$NON-NLS-1$
	String id = request.getParameter("id-cert");//$NON-NLS-1$
	String numCert = request.getParameter("num-cert");//$NON-NLS-1$
	final CertificateFire cert = CertificatesDAO.selectCertificateByID(id);
	if(cert != null){
		if(cert.getCertX509_principal() != null){
			final String[] datCertificate = cert.getCertX509_principal().getSubjectX500Principal().getName().split(",");//$NON-NLS-1$
			for (int i = 0; i < datCertificate.length; i++){
				data = data.concat(datCertificate[i]).concat("</br>");//$NON-NLS-1$
			}
			//fecha caducidad
			Date fecha = new Date();
			fecha = cert.getCertX509_principal().getNotAfter();		
			data = data.concat("Fecha de Caducidad = ").concat(Utils.getStringDateFormat(fecha));	//$NON-NLS-1$
			data = data.concat("§");//$NON-NLS-1$
		}
		else{
			data = data.concat("--").concat("§");//$NON-NLS-1$//$NON-NLS-2$
		}
		if(cert.getCertX509_backup() != null){
			final String[] datCertificate = cert.getCertX509_backup().getSubjectX500Principal().getName().split(",");//$NON-NLS-1$
			for (int i = 0; i < datCertificate.length; i++){
				data = data.concat(datCertificate[i]).concat("</br>");//$NON-NLS-1$
			}
			//fecha caducidad
			Date fecha = new Date();
			fecha = cert.getCertX509_backup().getNotAfter();		
			data = data.concat("Fecha de Caducidad = ").concat(Utils.getStringDateFormat(fecha));//$NON-NLS-1$	
		}
		else{
			data = data.concat("--");//$NON-NLS-1$
		}
	}
}

out.print(data);
%>