package es.gob.log.consumer.service;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import es.gob.log.register.LogServiceRegister;

/**
 * Servlet to implementation class SystemInitializer
 */
public class SystemInitializer extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public SystemInitializer() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    @Override
    public void init() throws ServletException {
    	super.init();

    	// Registramos el nodo de gestion de logs en la plataforma de monitorizacion
    	registerNode();
    }

    /**
     * Register the log consultant service in a monitorization platform if it's configured.
     */
	private static void registerNode() {
		String registerClass = null;
		try {
			final ConfigManager config = ConfigManager.getInstance();

			LogServiceRegister register = null;
			registerClass = config.getLogServiceRegisterClass();
			if (registerClass != null) {
				register = (LogServiceRegister) Class.forName(registerClass).newInstance();
			}

			if (register != null) {
				register.setServiceUrl(config.getLogServiceRegisterUrl());
				register.setConfig(config.getProperties());
				register.registry();
			}
		}
		catch (final Exception e) {
			Logger.getLogger(SystemInitializer.class.getName()).log(
			                                      Level.WARNING,
			                                      "No se pudo registrar el nodo de consulta de logs mediante el conector: " + registerClass,
			                                      e);
		}
	}
}
