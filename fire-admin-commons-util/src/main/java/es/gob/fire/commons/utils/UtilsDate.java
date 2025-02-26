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
 * <b>File:</b><p>es.gob.fire.commons.utils.UtilsDate.java.</p>
 * <b>Description:</b><p>Class that provides methods for managing dates.</p>
 * <b>Project:</b><p>Horizontal platform of validation services of multiPKI
 * certificates and electronic signature.</p>
 * <b>Date:</b><p>15/05/2020.</p>
 * @author Gobierno de España.
 * @version 1.1, 24/09/2021.
 */
package es.gob.fire.commons.utils;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * <p>Class that provides methods for managing dates.</p>
 * <b>Project:</b><p>Horizontal platform of validation services of multiPKI
 * certificates and electronic signature.</p>
 * @version 1.1, 24/09/2021.
 */
public class UtilsDate {

	/**
	 * Constant attribute that represents the date format <code>yyyy-MM-dd EEE HH:mm:ss ZZZZ</code>.
	 */
	public static final String FORMAT_FULL = "yyyy-MM-dd EEE HH:mm:ss ZZZZ";

	/**
	 * Constant attribute that represents the date format <code>yyyy-MM-dd</code>.
	 */
	public static final String FORMAT_DATE = "yyyy-MM-dd";

	/**
	 * Constant attribute that represents the date format <code>HH:mm:ss</code>.
	 */
	public static final String FORMAT_TIME = "HH:mm:ss";

	/**
	 * Constant attribute that represents the date format <code>yyyy-MM-dd HH:mm:ss</code>.
	 */
	public static final String FORMAT_DATE_TIME = "yyyy-MM-dd HH:mm:ss";

	/**
	 * Constant attribute that represents the date format <code>dd/MM/yyyy</code>.
	 */
	public static final String FORMAT_DATE_SMALL = "dd/MM/yyyy";

	/**
	 * Constant attribute that represents the date format <code>dd_MM_yyyy</code>.
	 */
	public static final String FORMAT_DATE_DOWN = "dd_MM_yyyy";

	/**
	 * Constant attribute that represents the date format <code>"dd/MM/yyyy HH:mm:ss"</code>.
	 */
	public static final String FORMAT_DATE_TIME_STANDARD = "dd/MM/yyyy HH:mm:ss";

	/**
	 * Constant attribute that represents the date format <code>"dd/MM/yyyy/HH/mm/ss"</code>.
	 */
	public static final String FORMAT_DATE_SLASH = "dd/MM/yyyy/HH/mm/ss";

	/**
	 * Constant attribute that represents the date format <code>yyyyMMddHHmmss</code>.
	 */
	public static final String FORMAT_DATE_TIME_JUNTA = "yyyyMMddHHmmss";

	/**
	 * Constant attribute that represents the date format <code>"yyyyMMddHHmmss.SZ"</code>.
	 */
	public static final String FORMAT_DATE_TIME_JUNTA_ADDITIONAL = "yyyyMMddHHmmss.SZ";
	
		/**
	 * Constant attribute that represents the date format <code>"yyyy-MM-dd'T'HHmmssZ"</code>.
	 */
	public static final String FORMAT_DATE_TIME_ADDITIONAL = "yyyy-MM-dd'T'HHmmssZ";

	/**
	 * Constant attribute that represents the date format <code>"yyyy-MM-dd HH:mm:ss,SSS"</code>.
	 */
	public static final String FORMAT_DATE_TIME_FULL = "yyyy-MM-dd HH:mm:ss,SSS";

	/**
	 * Constant attribute that represents the date format <code>"dd-MM-yyyy"</code>.
	 */
	public static final String FORMAT_DATE_INVERTED = "dd-MM-yyyy";

	/**
	 * Constant attribute that represents the date format <code>"dd/MM/yyyy HH:mm:ss.SSS"</code>.
	 */
	public static final String FORMAT_DATE_TIME_STANDARD_ADDITIONAL = "dd/MM/yyyy HH:mm:ss.SSS";

	/**
	 * Constant attribute that represents the date format <code>"dd-MMM-yyyy HH:mm"</code>.
	 */
	public static final String FORMAT_DATE_TIME_MINUTES = "dd-MMM-yyyy HH:mm";

	/**
	 * Constant attribute that represents the date format <code>"yyyy-MM-dd HH:mm:ss ZZZZ"</code>.
	 */
	public static final String FORMAT_DATE_TIME_NOTFULL = "yyyy-MM-dd HH:mm:ss ZZZZ";

	/**
	 * Constant attribute that represents the date format <code>"yyyy-MM-dd'T'HH:mm:ss.SSS"</code>.
	 */
	public static final String FORMAT_DATE_TIME_UTC = "yyyy-MM-dd'T'HH:mm:ss.SSS";

	/**
	 * Constant attribute that represents the date format <code>"yyyy/MM/dd EEE hh:mm:ss zzzz"</code>.
	 */
	public static final String FORMAT_DATE_ADDITIONAL = "yyyy/MM/dd EEE hh:mm:ss zzzz";

	/**
	 * Constant attribute that represents the date format <code>"dd-MM-yy_HH-mm-ss"</code>.
	 */
	public static final String FORMAT_DATE_TIME_SECONDS = "dd-MM-yy_HH-mm-ss";

	/**
	 * Constant attribute that represents the date format <code>"MM/DD/YYYY HH24:MI:SS"</code>.
	 */
	public static final String FORMAT_DATE_TIME_ORACLE = "MM/dd/yyyy HH:mm:ss";

	/**
	 * Constant attribute that represents the date format <code>"YYYY/MM/DD HH24:MI:SS"</code>.
	 */
	public static final String FORMAT_DATE_TIME_MYSQL_HSQLDB = "yyyy-MM-dd HH:mm:ss";

	/**
	 * Constant attribute that represents the date format <code>"yyyy-MM-dd HH:mm:ss.SSS zzz"</code>.
	 */
	public static final String FORMAT_DATE_TIME_XML_CONFIGURATION = "yyyy-MM-dd HH:mm:ss.SSS zzz";

	/**
	 * Constant attribute that represents the date format <code>"yyyy-MM-dd'T'HH:mm:ss"</code>.
	 */
	public static final String FORMAT_DATE_TIME_CRL_ISSUE_TIME = "yyyy-MM-dd'T'HH:mm:ss";

	/**
	 * Constant attribute that represents the date format <code>"yyyy-MM-dd'T'HH:mm:ss'.'SSS'Z'"</code>.
	 */
	public static final String FORMAT_DATE_TIME_TIMESTAMP = "yyyy-MM-dd'T'HH:mm:ss'.'SSS'Z'";

	/**
	 * Constant attribute that represents the date format <code>"yyyy-MM-dd'T'HH:mm:ss.SSSXXX"</code>.
	 */
	public static final String FORMAT_DATE_TIME_JSON = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

	/**
	 * Attribute that represents the value of the date.
	 */
	private Date date;

	/**
	 * Attribute that represents the time zone offset.
	 */
	private TimeZone timeZone = null;

	/**
	 * Constructor method for the class UtilsDate.java.
	 */
	public UtilsDate() {
		Calendar cal = Calendar.getInstance();
		timeZone = cal.getTimeZone();
		date = cal.getTime();
	}

	/**
	 * Constructor method for the class UtilsDate.java.
	 * @param pFecha Parameter that represents the value of the date.
	 */
	public UtilsDate(Date pFecha) {
		timeZone = null;
		date = pFecha;
	}

	/**
	 * Constructor method for the class UtilsDate.java.
	 * @param c Parameter that represents the object to set the value of the date and the time zone offset.
	 */
	public UtilsDate(Calendar c) {
		timeZone = c.getTimeZone();
		date = c.getTime();
	}

	/**
	 * Constructor method for the class UtilsDate.java.
	 * @param t Parameter that represents the time zone offset.
	 */
	public UtilsDate(TimeZone t) {
		Calendar cal = Calendar.getInstance(t);
		timeZone = cal.getTimeZone();
		date = cal.getTime();
	}

	/**
	 * Constructor method for the class UtilsDate.java.
	 * @param timezone Parameter that represents the identifier for the time zone offset.
	 */
	public UtilsDate(String timezone) {
		timeZone = TimeZone.getTimeZone(timezone);
		date = Calendar.getInstance(timeZone).getTime();
	}

	/**
	 * Constructor method for the class UtilsDate.java.
	 * @param dateString Parameter that represents the value of the date.
	 * @param dateFormat Parameter that represents the format used for the date.
	 * @throws ParseException If the method fails.
	 */
	public UtilsDate(String dateString, String dateFormat) throws ParseException {

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, UtilsCountryLanguage.getFirstLocaleOfCountryCode(UtilsCountryLanguage.ES_COUNTRY_CODE));
		timeZone = Calendar.getInstance().getTimeZone();
		simpleDateFormat.setTimeZone(timeZone);
		date = simpleDateFormat.parse(dateString);

	}

	/**
	 * Method that obtains a string with the value of the date for certain format.
	 * @param dateFormat Parameter that represents the format to apply for the date.
	 * @return a string with the value of the date for certain format.
	 */
	public final String toString(String dateFormat) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, UtilsCountryLanguage.getFirstLocaleOfCountryCode(UtilsCountryLanguage.ES_COUNTRY_CODE));
		if (timeZone != null) {
			simpleDateFormat.setTimeZone(timeZone);
		}
		return simpleDateFormat.format(date);
	}

	/**
	 * Method that obtains a string with the value of the date for certain format.
	 * @param dateFormat Parameter that represents the format to apply for the date.
	 * @param dateParam Date to be transform to String
	 * @return a string with the value of the date for certain format.
	 */
	public static final String toString(String dateFormat, Date dateParam) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, UtilsCountryLanguage.getFirstLocaleOfCountryCode(UtilsCountryLanguage.ES_COUNTRY_CODE));
		return simpleDateFormat.format(dateParam);
	}

	/**
	 * Method that obtains a string with the value of the date for certain format.
	 * @param simpleDateFormat Parameter that represents the concrete object for formatting and parsing the date in a locale-sensitive manner.
	 * @return a string with the value of the date for certain format.
	 */
	public final String toString(SimpleDateFormat simpleDateFormat) {
		return simpleDateFormat.format(date);
	}

	/**
	 * Method that obtains a string which represents the date on <code>UTC</code> format.
	 * @return the string on <code>UTC</code> format.
	 */
	public final String toUTCString() {
		SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_DATE_TIME_UTC);
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		return sdf.format(date) + "Z";
	}

	/**
	 * Method that obtains a date from a string with <code>UTC</code> format.
	 * @param utcDate Parameter that represents the string with <code>UTC</code> format.
	 * @return the date.
	 * @throws ParseException If the method fails.
	 */
	public static Date getUTCDate(String utcDate) throws ParseException {
		String[ ] t = utcDate.split("T");
		String pattern = "yyyy";
		String dateStr = null;
		dateStr = t[0].substring(0, NumberConstants.NUM4);
		if (t[0].length() > NumberConstants.NUM6) {
			dateStr = dateStr + t[0].substring(NumberConstants.NUM5, NumberConstants.NUM7);
			pattern = pattern + "MM";
			if (t[0].length() > NumberConstants.NUM9) {
				dateStr = dateStr + t[0].substring(NumberConstants.NUM8, NumberConstants.NUM10);
				pattern = pattern + "dd";
			}
		}
		if (t.length == 2) {
			String offSet = null;
			if (t[1].indexOf('Z') > -1) {
				t[1] = t[1].substring(0, t[1].indexOf('Z'));
				offSet = "+0000";
			} else if (t[1].indexOf('-') > -1) {
				offSet = t[1].substring(t[1].indexOf('-')).replaceAll(":", "");
				t[1] = t[1].substring(0, t[1].indexOf('-'));
			} else if (t[1].indexOf('+') > -1) {
				offSet = t[1].substring(t[1].indexOf('+')).replaceAll(":", "");
				t[1] = t[1].substring(0, t[1].indexOf('+'));
			}
			if (t[1].length() > 1) {
				dateStr = dateStr + t[1].substring(0, 2);
				pattern = pattern + "HH";
				if (t[1].length() > NumberConstants.NUM4) {
					dateStr = dateStr + t[1].substring(NumberConstants.NUM3, NumberConstants.NUM5);
					pattern = pattern + "mm";
					if (t[1].length() > NumberConstants.NUM7) {
						dateStr = dateStr + t[1].substring(NumberConstants.NUM6, NumberConstants.NUM8);
						pattern = pattern + "ss";
						if (t[1].length() > NumberConstants.NUM9) {
							pattern = pattern + ".SSS";
							t[1] = t[1].substring(NumberConstants.NUM8);
							for (int i = t[1].length(); i < NumberConstants.NUM4; i++) {
								t[1] = t[1] + "0";
							}
							dateStr = dateStr + t[1].substring(0, NumberConstants.NUM4);
						}
					}
				}
				if (offSet != null) {
					pattern = pattern + "Z";
					dateStr = dateStr + offSet;
				}
			}
		}
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		return sdf.parse(dateStr);
	}

	/**
	 * Method that obtains the current date and time of the system.
	 * @param formato Parameter that represents the format used to obtain the date.
	 * @return the current date and hour of the system in the input format.
	 */
	public static String getSystemDate(String formato) {
		String systemDate = null;
		try {
			UtilsDate serClsFecha = new UtilsDate();
			systemDate = serClsFecha.toString(formato);
		} catch (Exception e) {
			systemDate = "FECHA KO";
		}
		return systemDate;
	}

	/**
	 * {@inheritDoc}
	 * @see java.lang.Object#toString()
	 */
	public final String toString() {
		return toString(FORMAT_DATE_TIME);
	}

	/**
	 * Gets the value of the attribute {@link #date}.
	 * @return the value of the attribute {@link #date}.
	 */
	public final Date getDate() {
		return date;
	}

	/**
	 * Sets the value of the attribute {@link #date}.
	 * @param pFecha The value for the attribute {@link #date}.
	 */
	public final void setDate(Date pFecha) {
		date = pFecha;
	}

	/**
	 * Method that adds a number of days to the date.
	 * @param numDays Parameter that represents the number of days to add.
	 */
	public final void addDays(int numDays) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DATE, numDays);
		date = cal.getTime();
	}

	/**
	 * Method that modifies the format of a date.
	 * @param initialDate Parameter that represents the date.
	 * @param originalFormat Parameter that represents the original format.
	 * @param newFormat Parameter that represents the new format.
	 * @return a string that represents the date with the new format.
	 * @throws ParseException If the method fails.
	 */
	public static String modifyFormat(String initialDate, String originalFormat, String newFormat) throws ParseException {
		UtilsDate auxDate = new UtilsDate(initialDate, originalFormat);
		return auxDate.toString(newFormat);
	}

	/**
	 * Method that adds a number of days to the system date.
	 * @param numDays Parameter that represents the number of days to add.
	 * @return a string that represents the date returned on format <code>yyyy-mm-dd</code>.
	 * @throws ParseException If the method fails.
	 */
	public final String addDaysToSystemDate(int numDays) throws ParseException {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, numDays);
		date = cal.getTime();
		return modifyFormat(toString(), FORMAT_DATE_ADDITIONAL, FORMAT_DATE);
	}

	/**
	 * Method that adds a number of days to certain date.
	 * @param initialDate Parameter that represents the date to add the days. It's a string with <code>YYYYMMDD</code> format.
	 * @param numDays Parameter that represents the number of days to add.
	 * @return a string that represents the date returned on format <code>yyyyMMDD</code>.
	 * @throws ParseException If the method fails.
	 */
	public static String getSystemDateAddingDays(String initialDate, int numDays) throws ParseException {
		UtilsDate utilsDate = new UtilsDate(initialDate, FORMAT_DATE);
		return utilsDate.addDaysToSystemDate(numDays);
	}

	/**
	 * Method that adds a number of days to certain date.
	 * @param initialDate Parameter that represents the date to add the days.
	 * @param numDays Parameter that represents the number of days to add.
	 * @return a Date with diasAdd more.
	 */
	public static Date getDateAddingDays(Date initialDate, int numDays) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(initialDate);
		cal.add(Calendar.DATE, numDays);
		return cal.getTime();
	}

	/**
	 * Method that obtains a date from a string using a determined pattern.
	 * @param initialDate Parameter that represents the string to transform in date.
	 * @param pattern Parameter that represents the pattern used to generate the date.
	 * @return a date from a string using a determined pattern.
	 * @throws ParseException If the method fails.
	 */
	public static Date transformDate(String initialDate, String pattern) throws ParseException {
		SimpleDateFormat format = new SimpleDateFormat(pattern);
		Date dateParsed = null;
		dateParsed = format.parse(initialDate);
		return dateParsed;
	}

	/**
	 * Method that returns a Date from a string representing a PDF Date conform to the ASN.1 date format. This consists of
	 *  D:YYYYMMDDHHmmSSOHH'mm' where everything before and after YYYY is optional.
	 * @param value Parameter that represents the PDF Date.
	 * @return a wellformed Date if the input param is valid, or null in another case.
	 */
	public static Date parseToPDFDate(String value) {
		// Inicializamos variables
		int year = 0;
		int month = 0;
		int day = 0;
		int hour = 0;
		int minute = 0;
		int second = 0;

		String yearStr = null;
		String monthStr = null;
		String dayStr = null;
		String hourStr = null;
		String minuteStr = null;
		String secondStr = null;
		String timezoneminuteStr = null;

		char timezonechar = '?'; // +, -, o Z
		int timezonehour = 0;
		int timezoneminute = 0;

		Calendar cal = null;

		// Verificación 1: el valor de entrada no puede ser nulo
		String str = value;
		if (str == null) {
			return null;
		}
		// Verificación 2: el valor de entrada debe tener, como mínimo 6
		// caracteres, esto es, formato D:YYYY
		str = str.trim();
		if (str.length() < NumberConstants.NUM6) {
			return null;
		}
		int datestate = 0;
		int charidx = 0;
		try {
			wloop : while (charidx < str.length()) {
				// Para parsear la date se utilizará una variable por cada
				// componente de la date
				switch (datestate) {
					// Verificación 3: el valor de entrada debe comenzar por
					// "D:"
					case 0:
						if ("D:".equals(str.substring(charidx, charidx + 2))) {
							charidx += 2;
						} else {
							return null;
						}
						datestate = 1;
						break;
					// Verificación 4: El año debe tener 4 cifras (0000-9999)
					case 1:
						yearStr = str.substring(charidx, charidx + NumberConstants.NUM4);
						year = Integer.parseInt(yearStr);
						charidx += NumberConstants.NUM4;
						if (year < 0 || year > NumberConstants.NUM9999) {
							return null;
						}
						datestate = 2;
						break;
					// Verificación 5: El mes debe tener 2 cifras (01-12)
					case 2:
						monthStr = str.substring(charidx, charidx + 2);
						if (!monthStr.startsWith("Z") && !monthStr.startsWith("-") && !monthStr.startsWith("+")) {
							month = Integer.parseInt(monthStr);
							charidx += 2;
							if (month < 1 || month > NumberConstants.NUM12) {
								return null;
							}
						} else {
							monthStr = null;
						}
						datestate = NumberConstants.NUM3;
						break;
					// Verificación 6: El día debe tener 2 cifras (01-31)
					case NumberConstants.NUM3:
						dayStr = str.substring(charidx, charidx + 2);
						if (!dayStr.startsWith("Z") && !dayStr.startsWith("-") && !dayStr.startsWith("+")) {
							day = Integer.parseInt(dayStr);
							if (day < 1 || day > NumberConstants.NUM31) {
								return null;
							}
							charidx += 2;
						} else {
							dayStr = null;
						}
						datestate = NumberConstants.NUM4;
						break;
					// Verificación 7: La hora debe tener 2 cifras (00-23)
					case NumberConstants.NUM4:
						hourStr = str.substring(charidx, charidx + 2);
						if (!hourStr.startsWith("Z") && !hourStr.startsWith("-") && !hourStr.startsWith("+")) {
							hour = Integer.parseInt(hourStr);
							charidx += 2;
							if (hour < 0 || hour > NumberConstants.NUM23) {
								return null;
							}
						} else {
							hourStr = null;
						}
						datestate = NumberConstants.NUM5;
						break;
					// Verificación 8: El minuto debe tener 2 cifras (00-59)
					case NumberConstants.NUM5:
						minuteStr = str.substring(charidx, charidx + 2);
						if (!minuteStr.startsWith("Z") && !minuteStr.startsWith("-") && !minuteStr.startsWith("+")) {
							minute = Integer.parseInt(minuteStr);
							charidx += 2;
							if (minute < 0 || minute > NumberConstants.NUM59) {
								return null;
							}
						} else {
							minuteStr = null;
						}
						datestate = NumberConstants.NUM6;
						break;
					// Verificación 9: El segundo debe tener 2 cifras (00-59)
					case NumberConstants.NUM6:
						secondStr = str.substring(charidx, charidx + 2);
						if (!secondStr.startsWith("Z") && !secondStr.startsWith("-") && !secondStr.startsWith("+")) {
							second = Integer.parseInt(secondStr);
							charidx += 2;
							if (second < 0 || second > NumberConstants.NUM59) {
								return null;
							}
						} else {
							secondStr = null;
						}
						datestate = NumberConstants.NUM7;
						break;
					// Verificación 10: La timeZone horaria debe tener 1
					// carácter
					// válido
					// ('+', '-', o 'Z')
					case NumberConstants.NUM7:
						timezonechar = str.charAt(charidx);
						if (timezonechar != 'Z' && timezonechar != '+' && timezonechar != '-') {
							return null;
						}
						charidx++;
						datestate = NumberConstants.NUM8;
						break;
					// Verificación 11: La hora que va tras la timeZone horaria
					// debe
					// tener 2 cifras (00-23) si y sólo si
					// la timeZone horaria no tiene el carácter 'Z'
					case NumberConstants.NUM8:
						if (timezonechar == '+' || timezonechar == '-') {
							timezonehour = Integer.parseInt(str.substring(charidx, charidx + 2));
							if (timezonehour < 0 || timezonehour > NumberConstants.NUM23) {
								return null;
							}
							if (timezonechar == '-') {
								timezonehour = -timezonehour;
							}
							// Verificación 12: La hora que va tras la timeZone
							// horaria debe acabar en comilla simple
							if (!str.substring(charidx + 2, charidx + NumberConstants.NUM3).equals("'")) {
								return null;
							}
							charidx += 2;
						}
						datestate = NumberConstants.NUM9;
						break;
					// Verificación 13: El minuto que va tras la timeZone
					// horaria
					// debe tener 2 cifras (00-59) si y sólo si
					// la timeZone horaria no tiene el carácter 'Z'
					case NumberConstants.NUM9:
						if (timezonechar == '+' || timezonechar == '-') {
							if (str.charAt(charidx) == '\'') {
								timezoneminuteStr = str.substring(charidx + 1, charidx + NumberConstants.NUM3);
								if (timezoneminuteStr.length() != 2) {
									return null;
								}
								timezoneminute = Integer.parseInt(timezoneminuteStr);
							}
							if (timezoneminute < 0 || timezoneminute > NumberConstants.NUM59) {
								return null;
							}
							if (timezonechar == '-') {
								timezoneminute = -timezoneminute;
							}
						}
						break wloop;
				}
			}
			// Verificación 14: El día debe ser válido para el mes obtenido
			if (yearStr != null && monthStr != null && dayStr != null) {

				// Mes con 28 o 29 días
				if (month == 2) {
					GregorianCalendar gc = new GregorianCalendar();
					// Año bisiesto
					if (gc.isLeapYear(year)) {
						if (day > NumberConstants.NUM29) {
							return null;
						}
					}
					// Año no bisiesto
					else {
						if (day > NumberConstants.NUM28) {
							return null;
						}
					}
				}
				// Meses con 30 días
				else if ((month == NumberConstants.NUM4 || month == NumberConstants.NUM6 || month == NumberConstants.NUM9 || month == NumberConstants.NUM11) && day > NumberConstants.NUM30) {
					return null;
				}
			}
			// Verificación 15: El número de campos rescatados debe ser al menos
			// 2
			if (datestate < 2) {
				return null;
			}
		}
		// Si se produce alguna excepción durante el proceso de asignación de
		// fechas entendemos que la date no está bien formada
		// y por tanto no es correcta.
		catch (Exception e) {
			return null;
		}
		// Construimos el objeto TimeZone que representará la timeZone horaria
		// si se
		// especifica timeZone horaria.
		if (timezonechar != '?') {
			String tzStr = "GMT";
			if (timezonechar == 'Z') {
				tzStr += "+0000";
			} else {
				tzStr += timezonechar;
				NumberFormat nfmt = NumberFormat.getInstance();
				nfmt.setMinimumIntegerDigits(2);
				nfmt.setMaximumIntegerDigits(2);
				tzStr += nfmt.format(timezonehour);
				tzStr += nfmt.format(timezoneminute);
			}
			TimeZone tz = TimeZone.getTimeZone(tzStr);

			// Usamos el objeto TimeZone para crear un objeto Calendar con la
			// date teniendo en cuenta que los meses en Java comienzan en 0.
			cal = Calendar.getInstance(tz);
		}
		// Si no se especifica timeZone horaria
		else {
			cal = Calendar.getInstance();
		}
		if (month == 0) {
			month = 1;
		}
		cal.set(year, month - 1, day, hour, minute, second);
		return cal.getTime();
	}

	/**
	 * Method that obtains a period in milliseconds.
	 * @param hourPeriod Parameter that represents the hour.
	 * @param minutePeriod Parameter that represents the minutes.
	 * @param secondPeriod Parameter that represents the seconds.
	 * @return the period in milliseconds or <code>null</code> if some of the values.
	 */
	public static Long getPeriod(Long hourPeriod, Long minutePeriod, Long secondPeriod) {
		if (hourPeriod != null && minutePeriod != null && secondPeriod != null) {
			return (hourPeriod * NumberConstants.NUM3600 + minutePeriod * NumberConstants.NUM60 + secondPeriod) * NumberConstants.NUM1000;
		}
		return null;
	}

	/**
	 * Method that obtains the value in hours, minutes and seconds of a period in milliseconds.
	 * @param milliseconds Parameter that represents the period in milliseconds.
	 * @return an array where the first position is the value of the hour (24-hour), the second position is the value of the minutes
	 * and the third position is the value of the minutes.
	 */
	public static Integer[ ] getHoursSecondsMinutes(Long milliseconds) {

		int seconds = (int) (milliseconds / NumberConstants.NUM1000) % NumberConstants.NUM60;
		int minutes = (int) (milliseconds / (NumberConstants.NUM1000 * NumberConstants.NUM60) % NumberConstants.NUM60);
		int hours = (int) (milliseconds / (NumberConstants.NUM1000 * NumberConstants.NUM60 * NumberConstants.NUM60));

		Integer[ ] result = new Integer[NumberConstants.NUM3];
		result[0] = hours;
		result[1] = minutes;
		result[2] = seconds;

		return result;
	}
}