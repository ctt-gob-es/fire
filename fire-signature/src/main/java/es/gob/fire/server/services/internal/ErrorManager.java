package es.gob.fire.server.services.internal;



import java.util.Properties;
import java.util.logging.Logger;
import es.gob.fire.signature.ConfigFileLoader;
/**
 * Clase que gestiona los mensajes de erroes. Carga el fichero de configuraci&oacute;n del fichero errors_es_ES.messages.
 * @author Adolfo.Navarro
 *
 */
public class ErrorManager {

	private static final String ERR_FILE = "errors_es_ES.messages"; //$NON-NLS-1$
	private static final Logger LOGGER = Logger.getLogger(ErrorManager.class.getName());
	private static Properties error=null;
	
	private static final String PARAM_USER="PARAM_USER";
	private static final String PARAM_PROVIDER="PARAM_PROVIDER";
	/**Secuencia "PARAM_USER"*/
	private static final CharSequence USER=PARAM_USER;
	/**Secuencia "PARAM_PROVIDER"*/
	private static final CharSequence PROVIDER=PARAM_PROVIDER;
	
	/**
	 * Carga el fichero de configuraci&oacute;n del fichero errors_es_ES.messages
	 * @throws ConfigFilesException Cuando no se encuentra o no se puede cargar el fichero de configuraci&oacute;n.
	 */
	static {

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
	 * Obtiene el mensaje de texto asociado al valor del parámetro code, y sustituye las variables PARAM_USER y PARAM_PROVIDER por el valor de sesión indicado en dicha variable
	 * devolviendo el mensaje completo.
	 * @param code
	 * @param session
	 * @return
	 */
	private static final String getMessage(final String code,final FireSession session) {
		
		String msg=error.getProperty(code);
		//Aqui comenzaremos a tratar la cadena para ver si tiene $USER $PROVIDER etc..
		/*Por Ejemplo: El usuario PARAM_USER no está dado de alta para el proveedor PARAM_PROVIDER. Pulse botón Volver para seleccionar otro proveedor.*/
	
		
		if(msg.contains(USER)) {
			msg = msg.replaceAll(PARAM_USER, session.getString(ServiceParams.SESSION_PARAM_SUBJECT_ID));
		}
		if(msg.contains(PROVIDER)) {			
			ProviderInfo info = ProviderManager.getProviderInfo(session.getString(ServiceParams.SESSION_PARAM_CERT_ORIGIN));
			if(info!=null && info.getTitle()!=null) {
				msg = msg.replaceAll(PARAM_PROVIDER, info.getTitle());
			}else {
				msg = msg.replaceAll(PARAM_PROVIDER, "");
			}
		}
	
		return msg;
	}
	

	/**
	 * Establece el ,emsaje de error en sesión, Si el origen del certificado (proveedor) está forzado se borra la sesión,
	 no tiene sentido dar oportunidad de volver a cargar el mismo proveedor. A traves del código de error se obtiene el mensaje.
	 En el caso de haber obtenido el mensaje previamente, este se pasa por parámetro para establecerlo en sesión.
	 * @param session
	 * @param error
	 * @param originForced
	 */
	public static void setErrorToSession(final FireSession session, final OperationError error,final boolean originForced, final String messageError ) {	
		
		// Si el origen del certificado (proveedor) está forzado se borra la sesión,
		//no tiene sentido dar oportunidad de volver a cargar el mismo proveedor
		if(originForced) {
			SessionCollector.cleanSession(session);
		}
		
		session.setAttribute(ServiceParams.SESSION_PARAM_ERROR_TYPE, Integer.toString(error.getCode()));
		if(messageError!=null) {
			session.setAttribute(ServiceParams.SESSION_PARAM_ERROR_MESSAGE,messageError);
		}
		else{
			session.setAttribute(ServiceParams.SESSION_PARAM_ERROR_MESSAGE, getMessage(String.valueOf(error.getCode()),session));
		}
		
		SessionCollector.commit(session);
	}
	
	
}
