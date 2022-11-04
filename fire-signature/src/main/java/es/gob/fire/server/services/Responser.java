package es.gob.fire.server.services;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import es.gob.fire.server.connector.OperationResult;
import es.gob.fire.server.services.internal.ErrorResult;

/**
 * Construye una respuesta estructurada para una petici&oacute;n del servicio.
 * @author carlos.gamuci
 */
public class Responser {

	private static final Charset CHARSET = StandardCharsets.UTF_8;
	private static final String JSON_MIMETYPE = "application/json"; //$NON-NLS-1$

	private static final Logger LOGGER = Logger.getLogger(Responser.class.getName());

	/**
	 * Env&iacute;a una respuesta de operaci&oacute;n procesada correctamente al cliente de un servicio.
	 * @param response Respuesta a la que inscribir el mensaje.
	 * @param content Resultado de la operaci&oacute;n.
	 */
	public static void sendResult(final HttpServletResponse response, final byte[] content) {
		sendResult(response, HttpServletResponse.SC_OK, content);
	}

	/**
	 * Env&iacute;a una respuesta al cliente de un servicio.
	 * @param response Respuesta a la que inscribir el mensaje.
	 * @param status C&oacute;digo de respuesta.
	 * @param content Mensaje.
	 */
	public static void sendResult(final HttpServletResponse response, final int status, final byte[] content) {
		response.setStatus(status);
		try (OutputStream os = response.getOutputStream()) {
			os.write(content);
			os.flush();
		}
		catch (final Exception e) {
			LOGGER.log(Level.WARNING, "No se pudo devolver una respuesta por un error en el flujo de salida", e); //$NON-NLS-1$
		}
	}

	/**
	 * Env&iacute;a al cliente la respuesta con el resultado de la operaci&oacute;n.
	 * @param response Respuesta a la que inscribir el mensaje.
	 * @param operationResult Resultado de la operaci&oacute;n.
	 */
	public static void sendResult(final HttpServletResponse response, final OperationResult operationResult) {
		sendResult(response, HttpServletResponse.SC_OK, operationResult);
	}

	/**
	 * Env&iacute;a al cliente la respuesta con el resultado de la operaci&oacute;n.
	 * @param response Respuesta a la que inscribir el mensaje.
	 * @param status C&oacute;digo de estado de la respuesta.
	 * @param operationResult Resultado de la operaci&oacute;n.
	 */
	public static void sendResult(final HttpServletResponse response, final int status, final OperationResult operationResult) {
		response.setContentType(JSON_MIMETYPE);
		response.setCharacterEncoding(CHARSET.displayName());
		sendResult(response, status, operationResult.encodeResult(CHARSET));
	}

	/**
	 * Env&iacute;a una respuesta al cliente de un servicio utilizando el c&oacute;dio de estado por defecto del error.
	 * @param response Respuesta a la que inscribir el mensaje.
	 * @param content Mensaje.
	 */
	public static void sendError(final HttpServletResponse response, final FIReError error) {
		sendResult(response, error.getHttpStatus(), buildError(error));
	}

	/**
	 * Env&iacute;a una respuesta al cliente de un servicio utilizando el c&oacute;dio de estado por defecto del error.
	 * @param response Respuesta a la que inscribir el mensaje.
	 * @param error Error que se ha producido.
	 * @param operationResult Resultado de la operaci&oacute;n.
	 */
	public static void sendError(final HttpServletResponse response, final FIReError error, final OperationResult operationResult) {
		sendResult(response, error.getHttpStatus(), operationResult.encodeResult(CHARSET));
	}

	/**
	 * Env&iacute;a una respuesta al cliente de un servicio utilizando el c&oacute;dio de estado por defecto del error.
	 * @param response Respuesta a la que inscribir el mensaje.
	 * @param error Error que se ha producido.
	 * @param errorMessage Mensaje que se devuelve.
	 */
	public static void sendError(final HttpServletResponse response, final FIReError error, final String errorMessage) {
		sendResult(response, error.getHttpStatus(), errorMessage.getBytes(CHARSET));
	}

	/**
	 * Env&iacute;a una respuesta al cliente de un servicio utilizando el c&oacute;dio de estado indicado.
	 * @param response Respuesta a la que inscribir el mensaje.
	 * @param status C&oacute;digo de error HTTP.
	 * @param errorMessage Mensaje que se devuelve.
	 */
	public static void sendError(final HttpServletResponse response, final int status, final String errorMessage) {
		sendResult(response, status, errorMessage.getBytes(CHARSET));
	}

	/**
	 * Env&iacute;a una respuesta al cliente de un servicio utilizando el c&oacute;dio de estado indicado.
	 * @param response Respuesta a la que inscribir el mensaje.
	 * @param status C&oacute;digo de error HTTP.
	 */
	public static void sendError(final HttpServletResponse response, final int status) {
		sendResult(response, status, ("Status: " + status).getBytes(CHARSET)); //$NON-NLS-1$
	}

	/**
	 * Env&iacute;a una respuesta al cliente de un servicio.
	 * @param response Respuesta a la que inscribir el mensaje.
	 * @param status C&oacute;digo de respuesta.
	 * @param content Mensaje.
	 */
	public static void sendError(final HttpServletResponse response, final int status, final FIReError error) {
		response.setContentType(JSON_MIMETYPE);
		response.setCharacterEncoding(CHARSET.displayName());
		sendResult(response, status, buildError(error));
	}

	/**
	 * Construye una respuesta de error con el c&oacute;digo y el mensaje del error indicado.
	 * @param error Error que devolver.
	 * @return Resultado de error.
	 */
	private static ErrorResult buildError(final FIReError error) {
		return new ErrorResult(error.getCode(), error.getMessage());
	}

	/**
	 * Env&iacute;a una respuesta al cliente de un servicio.
	 * @param response Respuesta a la que inscribir el mensaje.
	 * @param status C&oacute;digo de respuesta.
	 * @param content Mensaje.
	 */
	public static void sendError(final HttpServletResponse response, final int status, final int errorCode, final String message) {
		sendResult(response, status, buildError(errorCode, message));
	}

	/**
	 * Construye una respuesta de error con el c&oacute;digo y el mensaje indicado.
	 * @param code C&oacute;digo de error.
	 * @param message Mensaje de error.
	 * @return Resultado de error.
	 */
	private static ErrorResult buildError(final int code, final String message) {
		return new ErrorResult(code, message);
	}
}
