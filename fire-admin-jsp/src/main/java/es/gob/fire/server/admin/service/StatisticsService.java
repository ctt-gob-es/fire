package es.gob.fire.server.admin.service;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import es.gob.fire.server.admin.conf.ConfigManager;
import es.gob.fire.statistics.config.DBConnectionException;
import es.gob.fire.statistics.config.DbManager;
import es.gob.fire.statistics.dao.SignaturesDAO;
import es.gob.fire.statistics.dao.TransactionsDAO;
/**
 * Servlet implementation class StatisticsService
 */
public class StatisticsService extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = Logger.getLogger(StatisticsService.class.getName());

	static {

		// Inicializamos el gestor de base de datos de las estadisticas
		DbManager.initialize(ConfigManager.getDbDriverString(), ConfigManager.getDbConnectionString());
	}

	private  int month = 0;
	private  int year = 0;
	private  int consulta = 0;

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

		String result = null;

		try {
			this.getParameters(request);
		}
		catch (final Exception e) {
			LOGGER.log(Level.WARNING, "No se han podido recuperar correctamente los parametros.", e); //$NON-NLS-1$
			final String jsonError = getJsonError("No se han podido recuperar correctamente los par&aacute;metros.", HttpServletResponse.SC_BAD_REQUEST); //$NON-NLS-1$
			response.getWriter().write(jsonError);
			return;
		}

		String statisticsName = null;
		try {
			switch (this.consulta) {

			case 1: //Transacciones finalizadas por cada aplicacion
				statisticsName = "Transacciones finalizadas por cada aplicacion"; //$NON-NLS-1$
				result = TransactionsDAO.getTransactionsByAppJSON(this.year, this.month);
				break;
			case 2: //Transacciones finalizadas  por cada origen de certificados/proveedor.
				statisticsName = "Transacciones finalizadas  por cada origen de certificados/proveedor"; //$NON-NLS-1$
				result = TransactionsDAO.getTransactionsByProviderJSON(this.year, this.month);
				break;
			case 3: //Transacciones segun el tamano de los datos de cada aplicacion
				statisticsName = "Transacciones segun el tamano de los datos de cada aplicacion"; //$NON-NLS-1$
				result = TransactionsDAO.getTransactionsByDocSizeJSON(this.year, this.month);
				break;
			case 4: //Transacciones realizadas segun el tipo de transaccion (simple o lote)
				statisticsName = "Transacciones realizadas segun el tipo de transaccion (simple o lote)"; //$NON-NLS-1$
				result = TransactionsDAO.getTransactionsByOperationJSON(this.year, this.month);
				break;
			case 5: //Documentos firmados por cada aplicacion.
				statisticsName = "Documentos firmados por cada aplicacion"; //$NON-NLS-1$
				result = SignaturesDAO.getSignaturesByAppJSON(this.year, this.month);
				break;
			case 6: //Documentos firmados por cada origen de certificados/proveedor.
				statisticsName = "Documentos firmados por cada origen de certificados/proveedor"; //$NON-NLS-1$
				result = SignaturesDAO.getSignaturesByProviderJSON(this.year, this.month);
				break;
			case 7: //Documentos firmados en cada formato de firma.
				statisticsName = "Documentos firmados en cada formato de firma"; //$NON-NLS-1$
				result = SignaturesDAO.getSignaturesByFormatJSON(this.year, this.month);
				break;
			case 8: //Documentos que utilizan cada formato de firma longevo.
				statisticsName = "Documentos que utilizan cada formato de firma longevo"; //$NON-NLS-1$
				result = SignaturesDAO.getSignaturesByLongLiveFormatJSON(this.year, this.month);
				break;
			default:
				LOGGER.log(Level.WARNING, "No se ha pasado el parametro de consulta valido."); //$NON-NLS-1$
				final String jsonError = getJsonError("No se ha pasado el par&aacute;metro de consulta v&aacute;lido.", //$NON-NLS-1$
						HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().write(jsonError);
				return;
			}
		} catch (final DBConnectionException e) {
			LOGGER.log(Level.WARNING, "No se han podido conectar con la BBDD.", e); //$NON-NLS-1$
			final String jsonError = getJsonError("No se ha podido recuperar correctamente la conexi&oacute;n con la BBDD.", //$NON-NLS-1$
					HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().write(jsonError);
			return;
		}
		catch(final SQLException e) {
			LOGGER.log(Level.WARNING, "Error al realizar en base de datos la consulta: " + statisticsName, e);  //$NON-NLS-1$
			final String jsonError = getJsonError("Error al realizar en base de datos la consulta: " + statisticsName, //$NON-NLS-1$
					HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().write(jsonError);
			return;
		}

		response.getOutputStream().write(result.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Carga los par&aacute;metros recibidos en la petici&oacute;n.
	 * @param request Petici&oacute;n recibida por el servicio.
	 * @throws Exception Si no se reciben los par&aacute;metros solicitados o si sus valores no son v&aacute;lidos.
	 */
	private final void getParameters(final HttpServletRequest request) throws Exception {

		final String dateParam = request.getParameter(ServiceParams.PARAM_START_DATETIME);
		if(dateParam != null && !dateParam.isEmpty()) {
			final String fecha[] = dateParam.split("/"); //$NON-NLS-1$
			this.month = Integer.parseInt(fecha[0]);
			this.year = Integer.parseInt(fecha[1]);
		}

		if (this.month == 0 || this.year == 0) {
			throw new Exception("No se proporciono una fecha valida"); //$NON-NLS-1$
		}

		final String consultaParam = request.getParameter(ServiceParams.PARAM_SELECT_QUERY);
		if (consultaParam != null && !consultaParam.isEmpty()) {
			this.consulta = Integer.parseInt(consultaParam);
		}

		if (this.consulta == 0) {
			throw new Exception("No se proporciono un valor valido de consulta"); //$NON-NLS-1$
		}
	}

	/**
	 * Devuelve un JSON con un mensaje de error.
	 * @param msgError Mensaje de error.
	 * @param errorCode C&oacute;digo de error.
	 * @return JSON con el mensaje y c&oacute;digo de error.
	 */
	private final static String getJsonError(final String msgError, final int errorCode) {

		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
		final JsonArrayBuilder data = Json.createArrayBuilder();
		data.add(Json.createObjectBuilder()
				.add("Code",errorCode) //$NON-NLS-1$
				.add("Message", msgError)); //$NON-NLS-1$
		jsonObj.add("Error", data); //$NON-NLS-1$

		final StringWriter error = new StringWriter();
		try (final JsonWriter jw = Json.createWriter(error);) {
			jw.writeObject(jsonObj.build());
		}
        return error.toString();
	}
}
