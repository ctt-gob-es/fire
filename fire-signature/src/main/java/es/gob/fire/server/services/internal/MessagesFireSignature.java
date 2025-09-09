package es.gob.fire.server.services.internal;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import es.gob.fire.i18n.Language;

/**
 * Clase que carga e instancia los recursos de los distintos idiomas disponibles.
 */
public class MessagesFireSignature {

	public static Map<String, Language> languagesLoaded = new HashMap<>();

	/**
	 * Obtiene los recursos del idioma solicitado.
	 * @param language Idioma a obtener.
	 * @return Recursos del idioma.
	 */
	public static Language getLanguage(final String language) {

		final String localeStr = language.trim();

		if (!languagesLoaded.containsKey(localeStr)) {

			String[ ] localeSplit = localeStr.split("_"); //$NON-NLS-1$
			if (localeSplit == null || localeSplit.length != 2) {
				localeSplit = new String [] {"es", "ES"};  //$NON-NLS-1$//$NON-NLS-2$
			}

			final Locale currentLocale = new Locale(localeSplit[0], localeSplit[1]);
			final Language newLanguage = new Language(currentLocale);

			languagesLoaded.put(localeStr, newLanguage);
		}

		return languagesLoaded.get(localeStr);
	}
}
