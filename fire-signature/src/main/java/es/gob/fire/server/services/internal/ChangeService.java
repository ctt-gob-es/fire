package es.gob.fire.server.services.internal;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import es.gob.fire.i18n.Language;
import es.gob.fire.server.services.FIReError;
import es.gob.fire.server.services.LogUtils;
import es.gob.fire.server.services.RequestParameters;
import es.gob.fire.server.services.Responser;

public class ChangeService extends HttpServlet {

	/** Serial Id. */
	private static final long serialVersionUID = 1550757574817907597L;

	private static final Logger LOGGER = Logger.getLogger(ChangeService.class.getName());

	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

		response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); //$NON-NLS-1$ //$NON-NLS-2$

		RequestParameters params;
		try {
			params = RequestParameters.extractParameters(request);
		}
		catch (final Exception e) {
			LOGGER.log(Level.WARNING, "Error en la lectura de los parametros de entrada", e); //$NON-NLS-1$
			Responser.sendError(response, FIReError.READING_PARAMETERS);
			return;
		}
		
		final String subjectRef = params.getParameter(ServiceParams.HTTP_PARAM_SUBJECT_REF);
		final String trId = params.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID);
		final String returnPage = params.getParameter(ServiceParams.HTTP_PARAM_PAGE);
		String redirectErrorUrl = params.getParameter(ServiceParams.HTTP_PARAM_ERROR_URL);
		final String language = params.getParameter(ServiceParams.HTTP_PARAM_LANGUAGE);
		final String errorType = params.getParameter(ServiceParams.HTTP_PARAM_ERROR_TYPE);
		final String errorMsg = params.getParameter(ServiceParams.HTTP_PARAM_ERROR_MESSAGE);

		final TransactionAuxParams trAux = new TransactionAuxParams(null, LogUtils.limitText(trId));
		final LogTransactionFormatter logF = trAux.getLogFormatter();

		// Comprobamos que se haya indicado el identificador de transaccion
		if (trId == null || trId.isEmpty()) {
			LOGGER.warning(logF.f("No se ha proporcionado el identificador de transaccion")); //$NON-NLS-1$
			Responser.sendError(response, FIReError.FORBIDDEN);
			return;
		}

		// Comprobamos que se haya indicado el identificador de usuario
		if (subjectRef == null || subjectRef.isEmpty()) {
			LOGGER.warning(logF.f("No se ha proporcionado la referencia del usuario")); //$NON-NLS-1$
			Responser.sendError(response, FIReError.FORBIDDEN);
			return;
		}

		// Comprobamos que se haya indicado la pagina de retorno
		if (returnPage == null || returnPage.isEmpty()) {
			LOGGER.warning(logF.f("No se ha proporcionado la pagina de retorno")); //$NON-NLS-1$
			Responser.sendError(response, FIReError.FORBIDDEN);
			return;
		}

		// Comprobamos que se haya indicado la URL a la que redirigir en caso de error
		if (redirectErrorUrl == null || redirectErrorUrl.isEmpty()) {
			LOGGER.warning(logF.f("No se ha proporcionado la URL de error")); //$NON-NLS-1$
			Responser.sendError(response, FIReError.FORBIDDEN);
			return;
		}
		
		try {
        	redirectErrorUrl = URLDecoder.decode(redirectErrorUrl, StandardCharsets.UTF_8.name());
        }
        catch (final Exception e) {
        	LOGGER.warning(logF.f("No se pudo deshacer el URL Encoding de la URL de redireccion: ") + e); //$NON-NLS-1$
		}

		final FireSession session = SessionCollector.getFireSessionOfuscated(trId, subjectRef, request.getSession(), true, false, trAux);
		if (session == null) {
			LOGGER.warning(logF.f("La transaccion no se ha inicializado o ha caducado. Se redirige a la pagina proporcionada en la llamada")); //$NON-NLS-1$
			Responser.redirectToExternalUrl(redirectErrorUrl, request, response, trAux);
			return;
		}
		
		// Actualizamos la sesion con el idioma en caso de que se haya indicado por parametro
		if (language != null && !language.isEmpty()) {
			Language.changeFireSignatureMessagesConfiguration(new Locale(language));
			session.setAttribute(ServiceParams.SESSION_PARAM_LANGUAGE, language);
		}
		
		// Si se indicara algun mensaje de error lo guardamos en la sesion
		if (errorType != null && !errorType.isEmpty() && errorMsg != null && !errorMsg.isEmpty()) {
			final FIReError error = FIReError.getByCode(Integer.valueOf(errorType));
			session.setAttribute(ServiceParams.SESSION_PARAM_ERROR_TYPE, errorType);
			session.setAttribute(ServiceParams.SESSION_PARAM_ERROR_MESSAGE, error.getMessage());
		}

		SessionCollector.commit(session, trAux);
		session.saveIntoHttpSession(request.getSession());

		// Redirigimos a la URL
		Responser.redirectToUrl(returnPage, request, response, trAux);
	}
}
