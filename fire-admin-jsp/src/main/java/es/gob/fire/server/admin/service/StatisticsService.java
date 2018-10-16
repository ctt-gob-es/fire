package es.gob.fire.server.admin.service;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import es.gob.fire.services.statistics.config.ConfigFilesException;
import es.gob.fire.services.statistics.config.ConfigManager;
import es.gob.fire.services.statistics.config.DBConnectionException;
import es.gob.fire.services.statistics.dao.SignaturesDAO;
import es.gob.fire.services.statistics.dao.TransactionsDAO;
/**
 * Servlet implementation class StatisticsService
 */
public class StatisticsService extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(StatisticsService.class.getName());
	/** Nombre del fichero de configuraci&oacute;n. */
	private static final String CONFIG_FILE = "admin_config.properties"; //$NON-NLS-1$
	private  Integer month = null;
	private  Integer year = null;
	private  Integer consulta = null;
	private final String opString = "";//$NON-NLS-1$


	/**
     * @see HttpServlet#HttpServlet()
     */
    public StatisticsService() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		final String stringOp = "seleccion"; //$NON-NLS-1$
		final boolean isOk = false;
		final HttpSession session = request.getSession(false);
		String result = null;

		try {
			this.getParameters(request);

		}
		catch (final Exception e) {
			LOGGER.warning("No se han podido recuperar correctamente los parametros."); //$NON-NLS-1$
			final String jsonError = getJsonError("No se han podido recuperar correctamente los parametros.", HttpServletResponse.SC_BAD_REQUEST); //$NON-NLS-1$
			response.getWriter().write(jsonError);
			return;
		}

		try {
			ConfigManager.checkConfiguration(CONFIG_FILE);
		}
    	catch (final Exception e) {
    		LOGGER.severe("Error al cargar la configuracion: " + e); //$NON-NLS-1$
			final String jsonError = getJsonError("Error al cargar la configuracion.", HttpServletResponse.SC_BAD_REQUEST); //$NON-NLS-1$
			response.getWriter().write(jsonError);
    		return;
    	}


		switch(this.getConsulta().intValue()) {

			case 1://Transacciones finalizadas por cada aplicación
				try {
					result = TransactionsDAO.getTransactionsByAppJSON(this.year.intValue(), this.month.intValue());
				} catch ( DBConnectionException | ConfigFilesException e) {
					// TODO Respuesta de error
					LOGGER.warning("No se han podido recuperar correctamente los parametros."); //$NON-NLS-1$
					final String jsonError = getJsonError("No se han podido recuperar correctamente los parametros.", HttpServletResponse.SC_BAD_REQUEST); //$NON-NLS-1$
					e.printStackTrace();
				}
				catch(final SQLException e) {

				}
				break;
			case 2://Transacciones finalizadas  por cada origen de certificados/proveedor.
				try {
					result = TransactionsDAO.getTransactionsByProviderJSON(this.year.intValue(), this.month.intValue());
				} catch (SQLException  | DBConnectionException | ConfigFilesException e) {
					// TODO REspuesta de error
					e.printStackTrace();
				}

				break;
			case 3://Transacciones segun el tamaño de los datos de cada aplicacion
				try {
					result = TransactionsDAO.getTransactionsByDocSizeJSON(this.year.intValue(), this.month.intValue());
				} catch (SQLException | DBConnectionException | ConfigFilesException e) {
					// TODO REspuesta de error
					e.printStackTrace();
				}
				break;
			case 4://Transacciones realizadas segun el tipo de transaccion (simple o lote)
				try {
					result = TransactionsDAO.getTransactionsByOperationJSON(this.year.intValue(), this.month.intValue());
				} catch (SQLException | DBConnectionException | ConfigFilesException e) {
					// TODO REspuesta de error
					e.printStackTrace();
				}
				break;
			case 5://Documentos firmados por cada aplicacion.
				try {
					result = SignaturesDAO.getSignaturesByAppJSON(this.year.intValue(), this.month.intValue());
				} catch (SQLException | DBConnectionException e) {
					// TODO REspuesta de error
					e.printStackTrace();
				}
				break;
			case 6://Documentos firmados por cada origen de certificados/proveedor.
				try {
					result = SignaturesDAO.getSignaturesByProviderJSON(this.year.intValue(), this.month.intValue());
				} catch (SQLException | DBConnectionException e) {
					// TODO REspuesta de error
					e.printStackTrace();
				}
				break;
			case 7://Documentos firmados en cada formato de firma.
				try {
					result = SignaturesDAO.getSignaturesByFormatJSON(this.year.intValue(), this.month.intValue());
				} catch (SQLException | DBConnectionException e) {
					// TODO REspuesta de error
					e.printStackTrace();
				}
				break;
			case 8://Documentos que utilizan cada formato de firma longevo.
				try {
					result = SignaturesDAO.getSignaturesByLongLiveFormatJSON(this.year.intValue(), this.month.intValue());
				} catch (SQLException | DBConnectionException e) {
					// TODO REspuesta de error
					e.printStackTrace();
				}
				break;
			default:
				break;
		}



		if (result != null) {
			response.getWriter().write(result);
		}

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}


	/**
	 *
	 * @param request
	 * @throws Exception
	 */
	private final void getParameters(final HttpServletRequest request) throws Exception {

		if(request.getParameter(ServiceParams.START_DATE) != null && !"".equals(request.getParameter(ServiceParams.START_DATE))) { //$NON-NLS-1$
			final String fecha[] = request.getParameter(ServiceParams.START_DATE).split("/"); //$NON-NLS-1$
			this.setMonth(new Integer(Integer.parseInt(fecha[0])));
			this.setYear(new Integer(Integer.parseInt(fecha[1])));
		}
		else {
			throw new Exception();
		}
		if(request.getParameter(ServiceParams.PARAM_SELECT_QUERY) != null && !"".equals(request.getParameter(ServiceParams.PARAM_SELECT_QUERY))) { //$NON-NLS-1$
			this.setConsulta(new Integer(Integer.parseInt(request.getParameter(ServiceParams.PARAM_SELECT_QUERY))));
		}
		else {
			throw new Exception();
		}
		if(this.getConsulta().intValue() == 0 || this.getMonth().intValue() == 0 || this.getYear().intValue() == 0) {
			throw new Exception();
		}

	}

	/**
	 * Devuelve un String con formato JSON
	 * @param msgError
	 * @return
	 */
	private final static String getJsonError(final String msgError, final int errorCode) {
		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
		final JsonArrayBuilder data = Json.createArrayBuilder();
		final StringWriter error = new StringWriter();
		data.add(Json.createObjectBuilder()
				.add("Code",errorCode) //$NON-NLS-1$
				.add("Message", msgError)); //$NON-NLS-1$
		jsonObj.add("Error", data); //$NON-NLS-1$
		final JsonWriter jw = Json.createWriter(error);
	    jw.writeObject(jsonObj.build());
	    jw.close();
        return error.toString();
	}

	private final Integer getMonth() {
			return this.month;
	}

	private final void setMonth(final Integer month) {
			this.month = month;
	}

	private final Integer getYear() {
			return this.year;
	}

	private final void setYear(final Integer year) {
			this.year = year;
	}

	private final Integer getConsulta() {
			return this.consulta;
	}

	private final void setConsulta(final Integer consulta) {
			this.consulta = consulta;
	}


}
