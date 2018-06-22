package es.gob.fire.server.services.statistics;

/**
 * Informaci&oacute;n de un Navegador web
 */
public class Browser {

	private final int id;
	private final String version;
	private static Browser browser;

	private static String BRNAME_FIREFOX = "Firefox/"; //$NON-NLS-1$
	private static String BRNAME_IE = "MSIE"; //$NON-NLS-1$

	private static String BRNAME_EDGE = "Edge/"; //$NON-NLS-1$
	private static String BRNAME_CHROME = "Chrome/"; //$NON-NLS-1$
	private static String BRNAME_SAFARI = "Safari/"; //$NON-NLS-1$
	private static String BRNAME_OPERA = "Opera/"; //$NON-NLS-1$
	private static String BRNAME_OPERA2 = "OPR/"; //$NON-NLS-1$
	private static String BRNAME_SEAMONKEY = "Seamonkey/"; //$NON-NLS-1$
	private static String BRNAME_CHROMIUN =  "Chromium/"; //$NON-NLS-1$

	private Browser(final int id, final String version) {
		this.id = id;
		this.version = version;
	}

	/** Identifica un navegador web a trav&eacute;s de las cabeceras de las
	 * peticiones que realiza.
	 * @param userAgent UserAgent del navegador.
	 * @return Navegador web.
	 */
	public static Browser identify( final String userAgent) {


		//FIREFOX
		if (userAgent.contains(BRNAME_FIREFOX) && !userAgent.contains(BRNAME_SEAMONKEY)){
			final String version = userAgent.substring(userAgent.indexOf(BRNAME_FIREFOX) + BRNAME_FIREFOX.length(), userAgent.length());
			browser =  new Browser(Browsers.FIREFOX.getId(), version);
		}
		//IE
		else if(userAgent.contains(BRNAME_IE)) {
			String version = ""; //$NON-NLS-1$
			final String[] ua_part= userAgent.split(";"); //$NON-NLS-1$
			for(int i = 0; i < ua_part.length; i++) {
				if(ua_part[i].contains(BRNAME_IE)) {
					version = ua_part[i].substring(BRNAME_IE.length() + 1).trim();
				}
			}
			browser =  new Browser(Browsers.INTERNET_EXPLORER.getId(), version);
		}
		//EDGE
		else if(userAgent.contains(BRNAME_EDGE)) {
			final String version = userAgent.substring(userAgent.indexOf(BRNAME_EDGE), userAgent.lastIndexOf("", userAgent.indexOf(BRNAME_EDGE))); //$NON-NLS-1$
			browser =  new Browser(Browsers.EDGE.getId(), version);
		}
		//CHROME
		else if(userAgent.contains(BRNAME_CHROME) && !userAgent.contains(BRNAME_CHROMIUN) && !userAgent.contains(BRNAME_OPERA) && !userAgent.contains(BRNAME_OPERA2)) {
			final String version = userAgent.substring(userAgent.indexOf(BRNAME_CHROME) + BRNAME_CHROME.length(), userAgent.indexOf(BRNAME_SAFARI));
			browser =  new Browser(Browsers.CHROME.getId(), version);
		}
		//SAFARI
		else if(userAgent.contains(BRNAME_SAFARI) && !userAgent.contains(BRNAME_CHROMIUN) && !userAgent.contains(BRNAME_CHROME)) {
			final String version = userAgent.substring(userAgent.indexOf(BRNAME_SAFARI)+BRNAME_SAFARI.length());
			browser =  new Browser(Browsers.SAFARI.getId(), version);
		}
		//OPERA
		else if(userAgent.contains(BRNAME_OPERA)) {
			final String version = userAgent.substring(userAgent.indexOf(BRNAME_OPERA) + BRNAME_OPERA.length());
			browser =  new Browser(Browsers.OPERA.getId(), version);
		}
		//OPERA
		else if(userAgent.contains(BRNAME_OPERA2)) {
			final String version = userAgent.substring(userAgent.indexOf(BRNAME_OPERA2) + BRNAME_OPERA2.length());
			browser =  new Browser(Browsers.OPERA.getId(), version);
		}
		//OTRO
		else {
			final String version = "XYZ"; //$NON-NLS-1$
			browser =  new Browser(Browsers.OTHER.getId(), version);
		}
		return browser;

	}

	/**
	 * Recupera el identificador del navegador web.
	 * @return Identificador del navegador web.
	 */
	public String getId() {
		return Integer.toString(this.id);
	}

	/**
	 * Recupera la versi&oacute;n del navegador.
	 * @return Versi&oacute;n del navegador.
	 */
	public String getVersion() {
		return this.version;
	}
}
