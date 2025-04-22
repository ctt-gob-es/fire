package es.gob.fire.server.services.internal;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import es.gob.fire.server.services.FIReError;
import es.gob.fire.server.services.RequestParameters;
import es.gob.fire.server.services.Responser;

public class BackService extends HttpServlet {

	/** Serial Id. */
	private static final long serialVersionUID = 1550757574817907597L;

	private static final Logger LOGGER = Logger.getLogger(BackService.class.getName());

	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) {

		response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); //$NON-NLS-1$ //$NON-NLS-2$

		// Leemos los parametros de la peticion
		final RequestParameters params;
		try {
			params = RequestParameters.parseParameters(request, false);
		}
		catch (final Exception e) {
			LOGGER.log(Level.WARNING, "Error en la lectura de los parametros de entrada", e); //$NON-NLS-1$
			Responser.sendError(response, FIReError.READING_PARAMETERS);
			return;
		}

		// Recuperamos el identificador de transaccion
		final String trId = params.getTransactionId();
		if (trId == null || trId.isEmpty()) {
			LOGGER.warning("No se ha proporcionado el identificador de transaccion"); //$NON-NLS-1$
			Responser.sendError(response, FIReError.READING_PARAMETERS);
			return;
		}

		final TransactionAuxParams trAux = new TransactionAuxParams(null, trId);
		final LogTransactionFormatter logF = trAux.getLogFormatter();

		try {
			params.checkParameters(logF);
		}
		catch (final Exception e) {
			LOGGER.log(Level.WARNING, logF.f("Error en la comprobacion de los parametros de entrada"), e); //$NON-NLS-1$
			Responser.sendError(response, FIReError.READING_PARAMETERS);
			return;
		}

		final String subjectRef = params.getParameter(ServiceParams.HTTP_PARAM_SUBJECT_REF);
		final String returnPage = params.getParameter(ServiceParams.HTTP_PARAM_PAGE);
		String redirectErrorUrl = params.getParameter(ServiceParams.HTTP_PARAM_ERROR_URL);

		// Comprobamos que se haya indicado el identificador de usuario
		if (subjectRef == null || subjectRef.isEmpty()) {
			LOGGER.warning(logF.f("No se ha proporcionado la referencia del usuario")); //$NON-NLS-1$
			Responser.sendError(response, FIReError.READING_PARAMETERS);
			return;
		}

		// Comprobamos que se haya indicado la pagina de retorno
		if (returnPage == null || returnPage.isEmpty()) {
			LOGGER.warning(logF.f("No se ha proporcionado la pagina de retorno")); //$NON-NLS-1$
			Responser.sendError(response, FIReError.READING_PARAMETERS);
			return;
		}

		// Comprobamos que se haya indicado la URL a la que redirigir en caso de error
		if (redirectErrorUrl == null || redirectErrorUrl.isEmpty()) {
			LOGGER.warning(logF.f("No se ha proporcionado la URL de error")); //$NON-NLS-1$
			Responser.sendError(response, FIReError.READING_PARAMETERS);
			return;
		}

		try {
        	redirectErrorUrl = URLDecoder.decode(redirectErrorUrl, StandardCharsets.UTF_8.name());
        }
        catch (final Exception e) {
        	LOGGER.warning(logF.f("No se pudo deshacer el URL Encoding de la URL de redireccion: ") + e); //$NON-NLS-1$
		}

		final FireSession session = SessionCollector.getFireSessionOfuscated(trId, subjectRef, request.getSession(false), false, true, trAux);
		if (session == null) {
			LOGGER.warning(logF.f("La transaccion no se ha inicializado o ha caducado. Se redirige a la pagina proporcionada en la llamada")); //$NON-NLS-1$
			Responser.redirectToExternalUrl(redirectErrorUrl, request, response, trAux);
			return;
		}

		session.saveIntoHttpSession(request.getSession());

		// Redirigimos a la URL
		Responser.redirectToUrl(returnPage, request, response, trAux);
	}
}
