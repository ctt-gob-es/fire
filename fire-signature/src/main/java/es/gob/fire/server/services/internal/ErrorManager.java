package es.gob.fire.server.services.internal;



import java.util.Iterator;

import es.gob.fire.server.services.FIReError;
import es.gob.fire.server.services.statistics.AuditSignatureRecorder;
import es.gob.fire.server.services.statistics.AuditTransactionRecorder;
import es.gob.fire.server.services.statistics.SignatureRecorder;
import es.gob.fire.server.services.statistics.TransactionRecorder;
import es.gob.fire.server.services.statistics.TransactionType;

/**
 * Clase que gestiona los mensajes de erroes. Carga el fichero de configuraci&oacute;n del fichero errors_es_ES.messages.
 * @author Adolfo.Navarro
 *
 */
public class ErrorManager {

	private static final TransactionRecorder TRANSLOGGER = TransactionRecorder.getInstance();
	private static final SignatureRecorder SIGNLOGGER = SignatureRecorder.getInstance();
	private static final AuditTransactionRecorder AUDITTRANSLOGGER = AuditTransactionRecorder.getInstance();
	private static final AuditSignatureRecorder AUDITSIGNLOGGER = AuditSignatureRecorder.getInstance();

	/**
	 * Establece un error en sesi&oacute;n interpretando que se va a redirigir
	 * al usuario a la aplicaci&oacute;n que llamo al servicio. Se limpiar&aacute;
	 * la sesi&oacute;n para s&oacute;lo conservar los mensajes de error y
	 * cualquier otro dato imprescindible.
	 * @param session Sesi&oacute;n en la que se produce y se debe almacenar el error.
	 * @param error Error producido.
	 * @param trAux Informaci&oacute;n auxiliar de la transacci&oacute;n.
	 */
	public static void setErrorToSession(final FireSession session, final FIReError error,
			final TransactionAuxParams trAux) {
		setErrorToSession(session, error, true, trAux);
	}

	/**
	 * Establece un error en sesi&oacute;n. Si se solicita volver a la
	 * aplicaci&oacute;n, ser&aacute; porque se ha abortado la operaci&oacute;n, en
	 * cuyo caso se limpiar&aacute; la sesi&oacute;n para s&oacute;lo conservar los
	 * mensajes de error y cualquier otro dato imprescindible.
	 * @param session Sesi&oacute;n en la que se produce y se debe almacenar el error.
	 * @param error Error producido.
	 * @param returnToApp Indica si debe prepararse la sesi&oacute;n para volver a la
	 * aplicaci&oacute;n y borrarse cualquier dato de sesion ajeno a este error.
	 * @param trAux Informaci&oacute;n auxiliar de la transacci&oacute;n.
	 */
	public static void setErrorToSession(final FireSession session, final FIReError error,
			final boolean returnToApp, final TransactionAuxParams trAux) {
		setErrorToSession(session, error, returnToApp, null, trAux);
	}

	/**
	 * Establece un error en sesi&oacute;n.  Si se solicita volver a la
	 * aplicaci&oacute;n, ser&aacute; porque se ha abortado la operaci&oacute;n, en
	 * cuyo caso se limpiar&aacute; la sesi&oacute;n para s&oacute;lo conservar los
	 * mensajes de error y cualquier otro dato imprescindible.
	 * @param session Sesi&oacute;n en la que se produce y se debe almacenar el error.
	 * @param error Error producido.
	 * @param returnToApp Indica si debe prepararse la sesi&oacute;n para volver a la
	 * aplicaci&oacute;n y borrarse cualquier dato de sesion ajeno a este error.
	 * @param messageError Mensaje de error a almacenar. Si no se indica, se
	 * usar&aacute; el por defecto del tipo de error.
	 * @param trAux Informaci&oacute;n auxiliar de la transacci&oacute;n.
	 */
	public static void setErrorToSession(final FireSession session, final FIReError error,
			final boolean returnToApp, final String messageError, final TransactionAuxParams trAux) {

		// Si se va a volver a la aplicacion, eliminamos los datos de sesion innecesarios
		if (returnToApp) {
			// Se registra en log de estadisticas que la transaccion ha terminado erroneamente.

			TRANSLOGGER.register(session, false);
			AUDITTRANSLOGGER.register(session, false, messageError != null ? messageError : error.getMessage());
			// Comprobar el tipo de operacion si es simple o lote  (SIGN o BATCH)
			final TransactionType op = (TransactionType) session.getObject(ServiceParams.SESSION_PARAM_TRANSACTION_TYPE);
			if (op == TransactionType.BATCH) { // Operacion por Lote
				final BatchResult batchResult = (BatchResult) session.getObject(ServiceParams.SESSION_PARAM_BATCH_RESULT);
				if (batchResult != null) {
					final Iterator<String> it = batchResult.iterator();
					while (it.hasNext()) {
						final String docId = it.next();
						SIGNLOGGER.register(session, false, docId);
						AUDITSIGNLOGGER.register(session, false, docId, messageError);
					}
				}
			}
			else if (op == TransactionType.SIGN) { // Operacion Simple
				SIGNLOGGER.register(session, false, null);
				AUDITSIGNLOGGER.register(session, false, null, messageError);
			}
			SessionCollector.cleanSession(session, trAux);
		}

		session.setAttribute(ServiceParams.SESSION_PARAM_ERROR_TYPE, Integer.toString(error.getCode()));
		if (messageError != null) {
			session.setAttribute(ServiceParams.SESSION_PARAM_ERROR_MESSAGE, messageError);
		}
		else {
			session.setAttribute(ServiceParams.SESSION_PARAM_ERROR_MESSAGE, error.getMessage());
		}

		SessionCollector.commit(session, trAux);
	}
}
