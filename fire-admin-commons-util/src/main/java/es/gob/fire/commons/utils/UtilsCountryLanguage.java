/*
/*******************************************************************************
 * Copyright (C) 2018 MINHAFP, Gobierno de España
 * This program is licensed and may be used, modified and redistributed under the  terms
 * of the European Public License (EUPL), either version 1.1 or (at your option)
 * any later version as soon as they are approved by the European Commission.
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and
 * more details.
 * You should have received a copy of the EUPL1.1 license
 * along with this program; if not, you may find it at
 * http:joinup.ec.europa.eu/software/page/eupl/licence-eupl
 ******************************************************************************/

/**
 * <b>File:</b><p>es.gob.fire.commons.utils.UtilsCountryLanguage.java.</p>
 * <b>Description:</b><p>Class that provides methods to get or check the country codes and
 * language codes supported by this java version.</p>
 * <b>Project:</b><p>Horizontal platform of validation services of multiPKI
 * certificates and electronic signature.</p>
 * <b>Date:</b><p>15/05/2020.</p>
 * @author Gobierno de España.
 * @version 1.0, 15/05/2020.
 */
package es.gob.fire.commons.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * <p>Class that provides methods to get or check the country codes and
 * language codes supported by this java version.</p>
 * <b>Project:</b><p>Horizontal platform of validation services of multiPKI
 * certificates and electronic signature.</p>
 * @version 1.0, 15/05/2020.
 */
public final class UtilsCountryLanguage {

	/**
	 * Constant attribute that represents the token 'UNKNOWN'.
	 */
	private static final String TOKEN_UNKNOWN = "UNKNOWN";

	/**
	 * Constant attribute that represents the region code 'EU' for 'Europe'.
	 */
	private static final String EU_REGION_CODE = "EU";

	/**
	 * Constant attribute that represents the region name 'Europe' for the code 'EU'.
	 */
	private static final String EU_REGION_NAME = "Europe";

	/**
	 * Constant attribute that represents the region code 'EL' for 'Greek'.
	 */
	private static final String EL_COUNTRY_CODE = "EL";

	/**
	 * Constant attribute that represents the region name 'Greek' for the code 'EL'.
	 */
	private static final String EL_COUNTRY_NAME = "Greek";

	/**
	 * Constant attribute that represents the region code 'UK' for 'United Kingdom'.
	 */
	private static final String UK_COUNTRY_CODE = "UK";

	/**
	 * Constant attribute that represents the region name 'United Kingdom' for the code 'UK'.
	 */
	private static final String UK_COUNTRY_NAME = "United Kingdom";

	/**
	 * Constant attribute that represents the region code 'LI' for 'Liechtenstein'.
	 */
	private static final String LI_COUNTRY_CODE = "LI";

	/**
	 * Constant attribute that represents the region name 'Liechtenstein' for the code 'LI'.
	 */
	private static final String LI_COUNTRY_NAME = "Liechtenstein";

	/**
	 * Constant attribute that represents the region code 'ES' for 'Spain'.
	 */
	public static final String ES_COUNTRY_CODE = "ES";

	/**
	 * Constant attribute that represents the region name 'Spain' for the code 'ES'.
	 */
	public static final String ES_COUNTRY_NAME = "Spain";

	/**
	 * Constructor method for the class UtilsCountryLanguage.java.
	 */
	private UtilsCountryLanguage() {
		super();
	}

	/**
	 * Attribute that represents the set of country codes specified in ISO 3166.
	 */
	private static Set<String> countryCodes = null;

	/**
	 * Attribute that represents the set of language codes specified in ISO 639.
	 */
	private static Set<String> languageCodes = null;

	/**
	 * Attribute that represents a map with the relation with country/region code
	 * and country/region name for the country/region that are not defined in some Locale.
	 */
	private static Map<String, String> additionalCountryRegionNames = null;

	static {

		additionalCountryRegionNames = new HashMap<String, String>();
		additionalCountryRegionNames.put(EU_REGION_CODE, EU_REGION_NAME);
		additionalCountryRegionNames.put(EL_COUNTRY_CODE, EL_COUNTRY_NAME);
		additionalCountryRegionNames.put(UK_COUNTRY_CODE, UK_COUNTRY_NAME);
		additionalCountryRegionNames.put(LI_COUNTRY_CODE, LI_COUNTRY_NAME);

	}

	/**
	 * Checks if the input parameter is a string taht represents a country code specified in ISO 3166.
	 * @param countryCode String with the country code to analyze.
	 * @return <code>true</code> if the input parameter represents a country code in ISO 3166. Otherwise <code>false</code>.
	 */
	public static boolean checkCountryCode(String countryCode) {

		boolean result = false;

		if (!UtilsStringChar.isNullOrEmpty(countryCode)) {
			result = getCountryCodes().contains(countryCode);
		}

		return result;

	}

	/**
	 * Gets a set with all the country codes (ISO 3166) and 'EU'.
	 * @return set of string with all the country codes (ISO 3166) and 'EU'.
	 */
	public static synchronized Set<String> getCountryCodes() {

		if (countryCodes == null) {

			String[ ] countryCodesFromLocale = Locale.getISOCountries();
			countryCodes = new HashSet<String>();
			for (String cc: countryCodesFromLocale) {
				countryCodes.add(cc);
			}
			countryCodes.add(EU_REGION_CODE);
			countryCodes.add(EL_COUNTRY_CODE);
			countryCodes.add(UK_COUNTRY_CODE);
			countryCodes.add(LI_COUNTRY_CODE);

		}

		return countryCodes;

	}

	/**
	 * Checks if the input parameter is a string that represents a language code specified in ISO 639 (not stable standard).
	 * @param languageCode String with the language code to analyze.
	 * @return <code>true</code> if the input parameter represents a language code in ISO 639 (not stable standard).
	 * Otherwise <code>false</code>.
	 */
	public static boolean checkLanguageCode(String languageCode) {

		boolean result = false;

		if (!UtilsStringChar.isNullOrEmpty(languageCode)) {
			result = getLanguageCodes().contains(languageCode);
		}

		return result;

	}

	/**
	 * Gets a set with all the language codes (ISO 639).
	 * @return set of string with all the language codes (ISO 639).
	 */
	public static synchronized Set<String> getLanguageCodes() {

		if (languageCodes == null) {

			String[ ] languageCodesFromLocale = Locale.getISOLanguages();
			languageCodes = new HashSet<String>();
			for (String lc: languageCodesFromLocale) {
				languageCodes.add(lc);
			}

		}

		return languageCodes;

	}

	/**
	 * Gets the first finded locale associated to the input country code (ISO 3166).
	 * @param countryCode Country Code (ISO 3166) from which search the locale.
	 * @return first finded locale associated to the input country code (ISO 3166),
	 * otherwise <code>null</code>.
	 */
	public static Locale getFirstLocaleOfCountryCode(String countryCode) {

		Locale result = null;

		// Si el código del país/región está entre los reconocidos, continuamos.
		if (checkCountryCode(countryCode)) {

			// Obtenemos todos los locales disponibles.
			Locale[ ] availableLocales = Locale.getAvailableLocales();

			// Los vamos recorriendo, y el primero que coincida con el código de
			// país, ese devolvemos.
			for (Locale locale: availableLocales) {

				if (countryCode.equals(locale.getCountry())) {

					result = locale;
					break;

				}

			}

		}

		return result;

	}

	/**
	 * Gets the first finded country/region name assigne to the locale associated to the input
	 * country code (ISO 3166). If it is not defined how locale, then try to find it in
	 * the additionals country declared.
	 * @param countryCode Country Code (ISO 3166) from which search the locale.
	 * @return first finded country/region name assigne to the locale associated to the input
	 * country code (ISO 3166), otherwise <code>null</code>.
	 */
	public static String getFirstLocaleCountryNameOfCountryCode(String countryCode) {

		String result = TOKEN_UNKNOWN;

		// Si el código del país/región está entre los reconocidos, continuamos.
		if (checkCountryCode(countryCode)) {

			// Primero recuperamos el locale asociado.
			Locale locale = getFirstLocaleOfCountryCode(countryCode);

			// Si es nulo, comprobamos si se trata de alguno de los adicionales.
			if (locale == null) {

				if (additionalCountryRegionNames.containsKey(countryCode)) {

					// Si es así, devolvemos el nombre que tenga asignado.
					result = additionalCountryRegionNames.get(countryCode);

				} else {

					result = TOKEN_UNKNOWN;

				}

			} else {

				// Si hemos encontrado el locale, devolvemos el nombre del
				// país/región
				// en inglés.
				result = locale.getDisplayCountry(Locale.UK);

			}

		}

		return result;

	}

}
