package es.gob.fire.server.admin.service;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import es.gob.fire.server.admin.dao.AplicationsDAO;
import es.gob.fire.server.admin.dao.CertificatesDAO;
import es.gob.fire.server.admin.entity.CertificateFire;
import es.gob.fire.server.admin.tool.Utils;

public class ApplicationsService extends HttpServlet{

	@Override
	protected  void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException {

		final HttpSession session = request.getSession(false);
		if (session == null) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		final String req = request.getParameter("requestType");//$NON-NLS-1$
		String data = "";//$NON-NLS-1$

		if (req == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}


		if(req.equals("countRecords")){//$NON-NLS-1$
		    data = AplicationsDAO.getApplicationsCount();
		}

		if(req.equals("All")){//$NON-NLS-1$
		    data = AplicationsDAO.getApplicationsJSON();


		}



		if(req.equals("getRecords")){//$NON-NLS-1$
			final String start = request.getParameter("currentIndex");//$NON-NLS-1$
			final String total = request.getParameter("recordsToFetch");//$NON-NLS-1$
			data = AplicationsDAO.getApplicationsPag(start, total);
		}

		if(req.equals("getCertificateId")){//$NON-NLS-1$
			final String id = request.getParameter("id-cert");//$NON-NLS-1$
			final String numCert = request.getParameter("num-cert");//$NON-NLS-1$
			final CertificateFire cert = CertificatesDAO.selectCertificateByID(id);
			if (cert != null) {
				if (cert.getX509Principal() != null) {
					final String[] datCertificate = cert.getX509Principal().getSubjectX500Principal().getName().split(",");//$NON-NLS-1$
					for (int i = 0; i < datCertificate.length; i++){
						data += datCertificate[i] + "</br>";//$NON-NLS-1$
					}
					// Fecha caducidad
					final Date fecha = cert.getX509Principal().getNotAfter();
					data += "Fecha de Caducidad = " + Utils.getStringDateFormat(fecha); //$NON-NLS-1$
				}
				else {
					data += "--"; //$NON-NLS-1$
				}
				data += "$*$"; //$NON-NLS-1$
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


		try (final PrintWriter out = response.getWriter();) {
			out.print(data);
		}

	}

}
