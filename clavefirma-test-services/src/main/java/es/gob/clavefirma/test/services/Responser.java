package es.gob.clavefirma.test.services;

import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

/**
 * Clase para el env&iacute;o de una respuesta al cliente.
 */
class Responser {

	private static final Logger LOGGER = Logger.getLogger(Responser.class.getName());

	/**
	 * Envia al cliente una respuesta con un texto y el c&oacute;digo de error interno (500).
	 * @param response Respuesta a trav&eacute;s de la que enviar el mensaje.
	 * @param msg Mensaje que enviar.
	 */
	static void sendError(final HttpServletResponse response, final String msg) {

		response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		try (PrintWriter writer = response.getWriter()) {
			writer.print(msg);
			writer.close();
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "No se pudo enviar la respuesta de error al cliente", e); //$NON-NLS-1$
		}
	}
}
