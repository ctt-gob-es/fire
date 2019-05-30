package es.gob.fire.server.admin.service;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import es.gob.fire.server.admin.dao.AplicationsDAO;
import es.gob.fire.server.admin.dao.CertificatesDAO;

public class CertificateService extends HttpServlet {

	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

		final HttpSession session = request.getSession(false);
		if (session == null) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		final String type = request.getParameter("requestType");//$NON-NLS-1$
		final String idCert = request.getParameter("id-cert");//$NON-NLS-1$

		if (type == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		String data = "";//$NON-NLS-1$
		if (type.equals("countRecordsCert")) {//$NON-NLS-1$
		    data = CertificatesDAO.getCertificatesCount();
		}
		else if (type.equals("All")) {//$NON-NLS-1$
		    data = CertificatesDAO.getCertificatesJSON();
		}
		//Paginado
		else if (type.equals("getRecordsCert")){//$NON-NLS-1$
			final String start = request.getParameter("currentIndex");//$NON-NLS-1$
			final String total = request.getParameter("recordsToFetch");//$NON-NLS-1$
			data = CertificatesDAO.getCertificatesPag(start, total);
		}

		if (type.equals("countRecordsCertApp")) { //$NON-NLS-1$
			if (idCert == null || "null".equals(idCert) || "".equals(idCert)) { //$NON-NLS-1$ //$NON-NLS-2$
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}
		    data = AplicationsDAO.getApplicationsCountByCertificate(idCert);
		}

		if (type.equals("getRecordsCertApp")) { //$NON-NLS-1$
			if (idCert == null || "null".equals(idCert) || "".equals(idCert)) { //$NON-NLS-1$ //$NON-NLS-2$
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}
			data = AplicationsDAO.getApplicationsByCertificateJSON(idCert);
		}

		try (final PrintWriter out = response.getWriter();) {
			out.print(data);
		}
	}
}
