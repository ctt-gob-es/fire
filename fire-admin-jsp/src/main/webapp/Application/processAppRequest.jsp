<%@page import="es.gob.fire.server.admin.entity.CertificateFire"%>
<%@page import="es.gob.fire.server.admin.dao.CertificatesDAO"%>
<%@page import="es.gob.fire.server.admin.dao.AplicationsDAO" %>
<%@page import="es.gob.fire.server.admin.tool.Utils" %>
<%@page import="java.util.Date"%>
<%
String req=request.getParameter("requestType");
String data="";

if(req.equals("countRecords")){
    data= AplicationsDAO.getApplicationsCount();
}
if(req.equals("All")){
    data= AplicationsDAO.getApplicationsJSON();
  
}
if(req.equals("getRecords")){
	String start=request.getParameter("currentIndex");
	String total=request.getParameter("recordsToFetch");
	data= AplicationsDAO.getApplicationsPag(start, total);
}

if(req.equals("getCertificateId")){
	String id=request.getParameter("id-cert");
	String numCert=request.getParameter("num-cert");
	final CertificateFire cert= CertificatesDAO.selectCertificateByID(id);
	if(cert!=null){
		if(cert.getCertX509_principal()!=null){
			final String[] datCertificate=cert.getCertX509_principal().getSubjectX500Principal().getName().split(",");
			for (int i=0;i<datCertificate.length;i++){
				data=data.concat(datCertificate[i]).concat("</br>");
			}
			//fecha caducidad
			Date fecha= new Date();
			fecha=cert.getCertX509_principal().getNotAfter();		
			data=data.concat("Fecha de Caducidad = ").concat(Utils.getStringDateFormat(fecha));	
			data=data.concat("§");
		}
		else{
			data=data.concat("--").concat("§");
		}
		if(cert.getCertX509_backup()!=null){
			final String[] datCertificate=cert.getCertX509_backup().getSubjectX500Principal().getName().split(",");
			for (int i=0;i<datCertificate.length;i++){
				data=data.concat(datCertificate[i]).concat("</br>");
			}
			//fecha caducidad
			Date fecha= new Date();
			fecha=cert.getCertX509_backup().getNotAfter();		
			data=data.concat("Fecha de Caducidad = ").concat(Utils.getStringDateFormat(fecha));			
		}
		else{
			data=data.concat("--");
		}
	}
}

out.print(data);
%>