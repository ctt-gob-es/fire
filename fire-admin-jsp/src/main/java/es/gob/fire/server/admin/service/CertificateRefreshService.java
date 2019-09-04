package es.gob.fire.server.admin.service;

import java.io.IOException;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import es.gob.fire.server.admin.dao.CertificatesDAO;
import es.gob.fire.server.admin.entity.CertificateFire;

public class CertificateRefreshService extends HttpServlet{
	/** Serial Id. */
	private static final long serialVersionUID = -6304364862591344482L;

	private static final Logger LOGGER = Logger.getLogger(CertificateRefreshService.class.getName());

	private static final String PARAM_IDCERT = "id"; //$NON-NLS-1$

	@Override
	protected void doPost(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {

		final HttpSession session = req.getSession(false);
		if (session == null) {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		req.setCharacterEncoding("utf-8"); //$NON-NLS-1$

		// Identificador del certificado
		JsonObject result;
		final String id = req.getParameter(PARAM_IDCERT);
		if (id == null) {
			LOGGER.log(Level.WARNING, "No se ha proporcionado identificador de operacion"); //$NON-NLS-1$
			final JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder()
					.add("error"," Id no valido"); //$NON-NLS-1$ //$NON-NLS-2$

			result = jsonObjectBuilder.build();
		}
		else {
			final CertificateFire certificate = CertificatesDAO.selectCertificateByID(id);

			final JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder()
					.add("cert-prin", certificate.getCertPrincipal()); //$NON-NLS-1$

			if (certificate.getCertBackup() != null) {
				jsonObjectBuilder.add("cert-resp", certificate.getCertBackup()); //$NON-NLS-1$
			}

			result = jsonObjectBuilder.build();
		}

		final StringWriter writer = new StringWriter();
		try (JsonWriter jsonWriter = Json.createWriter(writer)) {
			jsonWriter.writeObject(result);
		}

		resp.getWriter().print( writer.toString() );
}

	/**
	 * Procedimiento que obtine los datos de los par&aacute;metros reconocidos del
	 * formulario para a&ntilde;adir aplicaci&oacute;n.
	 * Par&aacute;metros: nombre-app, nombre-resp,email-resp, telf-resp y id-certificate.
	 * @param req Petici&oacute;n HTTP.
	 */
	private Parameters getParameters(final HttpServletRequest req) {

		final Parameters params = new Parameters();

		final String idParam = req.getParameter(PARAM_IDCERT);
		if (idParam != null && !idParam.isEmpty()) {
			params.setId(idParam);
		}

		return params;
	}

	/** Conjunto de parametros admitidos por el servicio. */
	class Parameters {

		private String id = null;

		/**
		 * Obtiene el id de la aplicaci&ácute;n
		 * @return
		 */
		String getId() {
			return this.id;
		}
		/**
		 *  Establece el id de la aplicaci&ácute;n
		 * @param name
		 */
		void setId(final String id) {
			this.id = id;
		}
	}

}
//ESTA PARTE ES DE NEW APPLICATION



//$("#id-certificate").change(function (event) {
////
////	var id = event.target.selectedOptions[0].value;
////
//////	var data="&id="+id;
//////////		$.ajax({
////////			async:true,
////////			cache:false,
////////	        type: "POST",
////////	        url: "./certificaterefresh",
////////	        data: data,
////////	        success: function (data) {
////////	        	if (data != null) {
////////
////////					console.log(data);
////////
////////
////////					var jsonData = JSON.parse(data);
////////					if (jsonData.hasOwnProperty('cert-prin')) {
////////						$("#cert-prin").val(jsonData['cert-prin']);
////////
////////					}
////////					if (jsonData.hasOwnProperty('cert-resp')) {
////////						$("#cert-resp").val(jsonData['cert-resp']);
////////
////////					}
////////				}
//////////			}

//});
//});