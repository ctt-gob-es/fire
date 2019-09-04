package es.gob.fire.server.admin.service;

import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import es.gob.fire.server.admin.dao.AplicationsDAO;
import es.gob.fire.server.admin.dao.CertificatesDAO;

/**
 * Servlet implementation class DeleteCertificateService
 */
public class DeleteCertificateService extends HttpServlet {


    /**
	 *
	 */
	private static final long serialVersionUID = -4929894052421807407L;

	private static final Logger LOGGER = Logger.getLogger(DeleteCertificateService.class.getName());

	private static final String PARAM_ID = "id-cert"; //$NON-NLS-1$

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {


		final HttpSession session = request.getSession(false);
		if (session == null) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}
		final String id = request.getParameter(PARAM_ID);
		final String msg = ""; //$NON-NLS-1$
		LOGGER.info("Baja del certificado con ID: " + LogUtils.cleanText(id)); //$NON-NLS-1$

		boolean isOk = true;
		if (id == null) {
			isOk = false;
		}
		else {
			try {
				/*Comprobamos si tiene asociadas aplicaciones al certificado que se quiere eliminar*/
				final String totalApp=AplicationsDAO.getApplicationsCountByCertificate(id);

				JsonObject jsonObj;
				try (final JsonReader jsonReader = Json.createReader(new StringReader(totalApp));) {
					jsonObj = jsonReader.readObject();
				}

				final int total= jsonObj.getInt("count"); //$NON-NLS-1$
				if(total <= 0) {
					CertificatesDAO.removeCertificate(id);


				}

			}
			catch (final Exception e) {
				LOGGER.log(Level.SEVERE, "Error al dar de baja el certificado", e); //$NON-NLS-1$
				isOk = false;
			}
		}

		response.sendRedirect("Certificate/CertificatePage.jsp?op=baja&r=" + (isOk ? "1" : "0")+"&ent=cer"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
