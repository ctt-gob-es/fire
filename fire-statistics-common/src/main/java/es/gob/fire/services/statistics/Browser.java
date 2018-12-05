package es.gob.fire.services.statistics;

/**
 * Informaci&oacute;n de un Navegador web
 */
public class Browser {

	private  int id;
	private String version;
	private String name;
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

	private static String FIREFOX = "Firefox"; //$NON-NLS-1$
	private static String IE = "Internet Explorer"; //$NON-NLS-1$
	private static String EDGE = "Edge"; //$NON-NLS-1$
	private static String CHROME = "Chrome"; //$NON-NLS-1$
	private static String SAFARI = "Safari"; //$NON-NLS-1$
	private static String OPERA = "Opera"; //$NON-NLS-1$
	private static String OTRO = "OTRO"; //$NON-NLS-1$

	public Browser() {
	}


	public Browser(final int id, final String version, final String name) {
		this.id = id;
		this.version = version;
		this.name = name;
	}

	/** Identifica un navegador web a trav&eacute;s de las cabeceras de las
	 * peticiones que realiza.
	 * @param userAgent UserAgent del navegador.
	 * @return Navegador web.
	 */
	public static Browser identify( final String userAgent) {

//Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.79 Safari/537.36 Edge/14.14393
		//FIREFOX
		if (userAgent.contains(BRNAME_FIREFOX) && !userAgent.contains(BRNAME_SEAMONKEY)){
			final String version = userAgent.substring(userAgent.indexOf(BRNAME_FIREFOX) + BRNAME_FIREFOX.length(), userAgent.length());
			browser =  new Browser(Browsers.FIREFOX.getId(), version, FIREFOX);
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
			browser =  new Browser(Browsers.INTERNET_EXPLORER.getId(), version, IE);
		}
		//EDGE
		else if(userAgent.contains(BRNAME_EDGE)) {
			final String version = userAgent.substring(userAgent.indexOf(BRNAME_EDGE) + BRNAME_EDGE.length());
			browser =  new Browser(Browsers.EDGE.getId(), version, EDGE);
		}
		//CHROME
		else if(userAgent.contains(BRNAME_CHROME) && !userAgent.contains(BRNAME_CHROMIUN) && !userAgent.contains(BRNAME_OPERA) && !userAgent.contains(BRNAME_OPERA2)) {
			final String version = userAgent.substring(userAgent.indexOf(BRNAME_CHROME) + BRNAME_CHROME.length(), userAgent.indexOf(BRNAME_SAFARI));
			browser =  new Browser(Browsers.CHROME.getId(), version, CHROME);
		}
		//SAFARI
		else if(userAgent.contains(BRNAME_SAFARI) && !userAgent.contains(BRNAME_CHROMIUN) && !userAgent.contains(BRNAME_CHROME)) {
			final String version = userAgent.substring(userAgent.indexOf(BRNAME_SAFARI) + BRNAME_SAFARI.length());
			browser =  new Browser(Browsers.SAFARI.getId(), version, SAFARI);
		}
		//OPERA
		else if(userAgent.contains(BRNAME_OPERA)) {
			final String version = userAgent.substring(userAgent.indexOf(BRNAME_OPERA) + BRNAME_OPERA.length());
			browser =  new Browser(Browsers.OPERA.getId(), version, OPERA);
		}
		//OPERA
		else if(userAgent.contains(BRNAME_OPERA2)) {
			final String version = userAgent.substring(userAgent.indexOf(BRNAME_OPERA2) + BRNAME_OPERA2.length());
			browser =  new Browser(Browsers.OPERA.getId(), version, OPERA);
		}
		//OTRO
		else {
			final String version = "XYZ"; //$NON-NLS-1$
			browser =  new Browser(Browsers.OTHER.getId(), version, OTRO);
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

	public final void setId(final int id) {
		this.id = id;
	}


	public final void setVersion(final String version) {
		this.version = version;
	}


	/**
	 * Recupera la versi&oacute;n del navegador.
	 * @return Versi&oacute;n del navegador.
	 */
	public String getVersion() {
		return this.version;
	}
	/**
	 * Recupera el nombre del navegador
	 * @return
	 */
	public final String getName() {
		return this.name;
	}
	/**
	 * Establece el nombre del navegador
	 * @param name
	 */
	public final void setName(final String name) {
		this.name = name;
	}


}
