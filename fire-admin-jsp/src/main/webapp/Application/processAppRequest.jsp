<%@page import="es.gob.fire.server.admin.entity.CertificateFire"%>
<%@page import="es.gob.fire.server.admin.dao.CertificatesDAO"%>
<%@page import="es.gob.fire.server.admin.dao.AplicationsDAO" %>
<%@page import="es.gob.fire.server.admin.tool.Utils" %>
<%@page import="java.util.Date"%>
<%

if (session == null) {
	response.sendRedirect("../Login.jsp?login=fail"); //$NON-NLS-1$
	return;
}

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
	if (cert != null) {
		if (cert.getX509Principal() != null) {
			final String[] datCertificate = cert.getX509Principal().getSubjectX500Principal().getName().split(",");//$NON-NLS-1$
			for (int i = 0; i < datCertificate.length; i++){
				data += datCertificate[i] + "</br>";//$NON-NLS-1$
			}
			// Fecha caducidad
			Date fecha = cert.getX509Principal().getNotAfter();		
			data += "Fecha de Caducidad = " + Utils.getStringDateFormat(fecha) + "§"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		else {
			data += "--§"; //$NON-NLS-1$
		}
		if (cert.getX509Backup() != null) {
			final String[] datCertificate = cert.getX509Backup().getSubjectX500Principal().getName().split(",");//$NON-NLS-1$
			for (int i = 0; i < datCertificate.length; i++){
				data += datCertificate[i] + "</br>"; //$NON-NLS-1$
			}
			// Fecha caducidad
			Date fecha = new Date();
			fecha = cert.getX509Backup().getNotAfter();		
			data += "Fecha de Caducidad = " + Utils.getStringDateFormat(fecha); //$NON-NLS-1$	
		}
		else {
			data += "--"; //$NON-NLS-1$
		}
	}
}

out.print(data);
%>