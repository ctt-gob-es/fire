package es.gob.fire.server.services.internal;

import javax.servlet.http.HttpServletRequest;

import es.gob.fire.signature.ConfigManager;

/**
 * Clase para la gesti&oacute;n del contexto p&uacute;blico de la aplicaci&oacute;n.
 */
public class PublicContext {

	private PublicContext() {
		// No permitimos la instanciacion de la clase
	}

	/**
	 * Obtiene la URL base del contexto p&uacute;blico de la aplicaci&oacute;n. Si est&aacute;
	 * definido en la configuraci&oacute;n lo obtiene de la misma. Si no, la obtiene a partir de la
	 * URL actual.
	 * @param request Petici&oacute;n con la cual se alcanz&oacute; el contexto actual.
	 * @return URL base del contexto p&uacute;blico.
	 */
	public static String getPublicContext(final HttpServletRequest request) {
		String redirectUrlBase = ConfigManager.getPublicContextUrl();
		if (redirectUrlBase == null || redirectUrlBase.isEmpty()) {
			final String requestUrl = request.getRequestURL().toString();
			if (requestUrl != null) {
				redirectUrlBase = requestUrl.substring(0, requestUrl.lastIndexOf('/'));
			}
		}

		if (redirectUrlBase != null && !redirectUrlBase.endsWith("/public/")) { //$NON-NLS-1$
			if (redirectUrlBase.endsWith("/public")) { //$NON-NLS-1$
				redirectUrlBase += "/"; //$NON-NLS-1$
			}
			else {
				redirectUrlBase += "/public/"; //$NON-NLS-1$
			}
		}
		return redirectUrlBase;
	}
}
