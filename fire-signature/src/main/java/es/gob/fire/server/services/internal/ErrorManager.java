package es.gob.fire.server.services.internal;



import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Logger;

import es.gob.fire.server.services.statistics.SignatureRecorder;
import es.gob.fire.server.services.statistics.TransactionRecorder;
import es.gob.fire.server.services.statistics.TransactionType;
import es.gob.fire.signature.ConfigFileLoader;

/**
 * Clase que gestiona los mensajes de erroes. Carga el fichero de configuraci&oacute;n del fichero errors_es_ES.messages.
 * @author Adolfo.Navarro
 *
 */
public class ErrorManager {

	private static final Logger LOGGER = Logger.getLogger(ErrorManager.class.getName());
	private static final TransactionRecorder TRANSLOGGER = TransactionRecorder.getInstance();
	private static final SignatureRecorder SIGNLOGGER = SignatureRecorder.getInstance();

	private static final String ERR_FILE = "errors_es_ES.messages"; //$NON-NLS-1$

	private static final String PARAM_USER = "PARAM_USER"; //$NON-NLS-1$
	private static final String PARAM_PROVIDER = "PARAM_PROVIDER"; //$NON-NLS-1$

	private static Properties error = null;

	static {

		// Cargamos el fichero con los mensajes de error
		if (error == null) {
			try {
				error = ConfigFileLoader.loadConfigFile(ERR_FILE);
			}
			catch (final Exception e) {
				LOGGER.severe("No se pudo cargar el fichero de configuracion " + ERR_FILE); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Establece un error en sesi&oacute;n interpretando que se va a redirigir
	 * al usuario a la aplicaci&oacute;n que llamo al servicio. Se limpiar&aacute;
	 * la sesi&oacute;n para s&oacute;lo conservar los mensajes de error y
	 * cualquier otro dato imprescindible.
	 * @param session Sesi&oacute;n en la que se produce y se debe almacenar el error.
	 * @param error Error producido.
	 */
	public static void setErrorToSession(final FireSession session, final OperationError error) {
		setErrorToSession(session, error, true);
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
	 */
	public static void setErrorToSession(final FireSession session, final OperationError error,
			final boolean returnToApp) {
		setErrorToSession(session, error, returnToApp, null);
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
	 */
	public static void setErrorToSession(final FireSession session, final OperationError error,
			final boolean returnToApp, final String messageError) {

		// Si se va a volver a la aplicacion, eliminamos los datos de sesion innecesarios
		if (returnToApp) {
			// Se registra en log de estadisticas que la transaccion ha terminado erroneamente.
			TRANSLOGGER.register(session, false);
			// Comprobar el tipo de operacion si es simple o lote  (SIGN o BATCH)
			final TransactionType op = TransactionType.getOperation(session.getString(ServiceParams.SESSION_PARAM_TRANSACTION_TYPE));
			if (op == TransactionType.BATCH) { // Operacion por Lote
				final BatchResult batchResult = (BatchResult) session.getObject(ServiceParams.SESSION_PARAM_BATCH_RESULT);
				if (batchResult != null) {
					final Iterator<String> it = batchResult.iterator();
					while (it.hasNext()) {
						final String docId = it.next();
						SIGNLOGGER.register(session, false, docId);
					}
				}
			}
			else if (op == TransactionType.SIGN) { // Operacion Simple
				SIGNLOGGER.register(session, false, null);
			}
			SessionCollector.cleanSession(session);
		}

		session.setAttribute(ServiceParams.SESSION_PARAM_ERROR_TYPE, Integer.toString(error.getCode()));
		if (messageError != null) {
			session.setAttribute(ServiceParams.SESSION_PARAM_ERROR_MESSAGE, messageError);
		}
		else if (returnToApp) {
			session.setAttribute(ServiceParams.SESSION_PARAM_ERROR_MESSAGE, error.getMessage());
		}
		else {
			session.setAttribute(ServiceParams.SESSION_PARAM_ERROR_MESSAGE, getMessage(String.valueOf(error.getCode()), session));
		}

		SessionCollector.commit(session);
	}

	/**
	 * Obtiene el mensaje de texto asociado a un c&oacute;digo de error y sustituyendo
	 * las cadenas PARAM_USER y PARAM_PROVIDER que encuentra por el identificador de
	 * usuario y el nombre de proveedor seleccionado.
	 * @param code C&oacute;digo de error.
	 * @param session Sesi&oacute;n de la operaci&oacute;n
	 * @return Mensaje asociado al c&oacute;digo de error.
	 */
	private static final String getMessage(final String code, final FireSession session) {

		String msg = error.getProperty(code);

		// Aqui comenzaremos a tratar la cadena para ver si tiene $USER, $PROVIDER, etc.
		if (msg.contains(PARAM_USER)) {
			msg = msg.replaceAll(PARAM_USER, session.getString(ServiceParams.SESSION_PARAM_SUBJECT_ID));
		}
		if (msg.contains(PARAM_PROVIDER)) {
			final ProviderInfo info = ProviderManager.getProviderInfo(
					session.getString(ServiceParams.SESSION_PARAM_CERT_ORIGIN));
			if (info != null && info.getTitle() != null) {
				msg = msg.replaceAll(PARAM_PROVIDER, info.getTitle());
			} else {
				msg = msg.replaceAll(PARAM_PROVIDER, ""); //$NON-NLS-1$
			}
		}

		return msg;
	}
}
