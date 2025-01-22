package es.gob.fire.web.clave.sp.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * thread local session holder
 */
public class SessionHolder {

//	private static final ThreadLocal<HttpSession> sessionHolderMap = new ThreadLocal<HttpSession>();
	
	/** Lista de sesiones abiertas. */
	public static volatile Map<String, String> sessionsSAML = new ConcurrentHashMap<String, String>(10);
	
	private SessionHolder() {
	}
}
