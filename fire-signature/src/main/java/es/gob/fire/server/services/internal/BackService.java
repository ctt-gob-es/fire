package es.gob.fire.server.services.internal;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import es.gob.fire.server.services.FIReError;
import es.gob.fire.server.services.Responser;

public class BackService extends HttpServlet {

	/** Serial Id. */
	private static final long serialVersionUID = 1550757574817907597L;

	private static final Logger LOGGER = Logger.getLogger(BackService.class.getName());

	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

		response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); //$NON-NLS-1$ //$NON-NLS-2$

		final String subjectRef = request.getParameter(ServiceParams.HTTP_PARAM_SUBJECT_REF);
		final String trId = request.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID);
		String redirectErrorUrl = request.getParameter(ServiceParams.HTTP_PARAM_ERROR_URL);
		final String returnPage = request.getParameter(ServiceParams.HTTP_PARAM_PAGE);

		if (subjectRef == null || trId == null || redirectErrorUrl == null || returnPage == null) {
			Responser.sendError(response, FIReError.FORBIDDEN);
			return;
		}

		final TransactionAuxParams trAux = new TransactionAuxParams(null, trId);
		final LogTransactionFormatter logF = trAux.getLogFormatter();

		final FireSession fireSession = SessionCollector.getFireSessionOfuscated(trId, subjectRef, request.getSession(false), false, false, trAux);
		if (fireSession == null) {
			LOGGER.warning(logF.f("La transaccion %1s no se ha inicializado o ha caducado. Se redirige a la pagina proporcionada en la llamada", trId)); //$NON-NLS-1$
        	SessionCollector.removeSession(trId, trAux);
			Responser.redirectToExternalUrl(redirectErrorUrl, request, response, trAux);
			return;
		}

		final TransactionConfig connConfig = (TransactionConfig) fireSession.getObject(ServiceParams.SESSION_PARAM_CONNECTION_CONFIG);

		// Usamos la URL de error indicada en la transaccion
		redirectErrorUrl = connConfig.getRedirectErrorUrl();

		// Preparamos la URL de redireccion
		final String url = returnPage + "?" + ServiceParams.HTTP_PARAM_TRANSACTION_ID + "=" + trId + "&" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			+ ServiceParams.HTTP_PARAM_SUBJECT_REF + "=" + subjectRef; //$NON-NLS-1$

		response.sendRedirect(url);
	}
}
