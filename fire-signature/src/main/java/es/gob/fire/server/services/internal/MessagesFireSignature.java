package es.gob.fire.server.services.internal;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import es.gob.fire.i18n.Language;

/**
 * Clase que carga e instancia los distintos idiomas disponibles en FIRe
 */
public class MessagesFireSignature {
	
	public static Map<String, Language> languagesLoaded = new HashMap<String, Language>();
	
	/**
	 * Obtiene el idioma con su recursos que se haya pasado por par&aacute;metro
	 * @param language Idioma a obtener.
	 * @return Idioma y sus recursos.
	 */
	public static Language getLanguage(final String language) {
		
		if (languagesLoaded.containsKey(language)) {
			return languagesLoaded.get(language);
		}
		
		final String localeStr = language.trim();
		
		String[ ] localeSplit = localeStr.split("_"); //$NON-NLS-1$
		if (localeSplit == null || localeSplit.length != 2) {
			localeSplit = new String [] {"es", "ES"};  //$NON-NLS-1$//$NON-NLS-2$
		}
		
		final Locale currentLocale = new Locale(localeSplit[0], localeSplit[1]);
		
		final Language newLanguage = new Language(currentLocale);
		
		languagesLoaded.put(localeStr, newLanguage);
		
		return newLanguage;
	}
	

}
