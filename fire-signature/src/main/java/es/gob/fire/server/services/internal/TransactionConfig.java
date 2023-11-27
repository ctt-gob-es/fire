package es.gob.fire.server.services.internal;

import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;

import es.gob.fire.server.services.ServiceUtil;

/**
 * Configuraci&oacute;n asociada a una transacci&oacute;n.
 */
public class TransactionConfig implements Serializable {

	/** Serial Id. */
	private static final long serialVersionUID = -990580085715618601L;

	/** Par&aacute;metro usado para configurar la URL a la que redirigir al usuario en caso de error. */
	private static final String PARAM_ERROR_URL = "redirectErrorUrl"; //$NON-NLS-1$
	/** Par&aacute;metro usado para configurar la URL a la que redirigir al usuario en caso de &eacute;xito. */
	private static final String PARAM_SUCCESS_URL = "redirectOkUrl"; //$NON-NLS-1$
	/** Par&aacute;metro usado para configurar el origen del certificado que debe usarse. */
	private static final String PARAM_CERT_ORIGIN = "certOrigin"; //$NON-NLS-1$
	/** Par&aacute;metro usado para configurar el t&iacute;tulo de la aplicaci&oacute;n que solicita firmar. */
	private static final String PARAM_APPLICATION_TITLE = "appName"; //$NON-NLS-1$

	/**
	 * Par&aacute;metro usado para configurar el gestor de documentos con el que obtener los datos
	 * a firmar y guardar las firmas.
	 */
	private static final String PARAM_DOCUMENT_MANAGER = "docManager"; //$NON-NLS-1$

	/**
	 * Par&aacute;metro usado para indicar si debe omitirse o no la selecci&oacute:n del certificado
	 * en caso de que s&oacute;lo se encuentre uno.
	 */
	private static final String PARAM_SKIP_CERT_SELECTION = "skipCertSelection"; //$NON-NLS-1$

    /** Cadena utilizada para separar valores establecidos dentro de una misma propiedad. */
	private static final String VALUES_SEPARATOR = ","; //$NON-NLS-1$

	private final Properties config;

	/**
	 * Crea un objeto de configuraci&oacute;n de transacci&oacute;n a
	 * partir de unas propiedades.
	 * @param config Propiedades de configuraci&oacute;n.
	 */
	public TransactionConfig(final Properties config) {
		this.config = config != null ? config : new Properties();
	}
	
	/**
	 * Crea un objeto de configuraci&oacute;n de transacci&oacute;n a
	 * partir de otro.
	 * @param clone de configuraci&oacute;n de transacci&oacute;n.
	 */
	public TransactionConfig(TransactionConfig clone) {
		this.config = (Properties) clone.config.clone();
	}

	/**
	 * Crea un objeto de configuraci&oacute;n de transacci&oacute;n a
	 * partir de un properties de configuraci&oacute;n codificado en
	 * Base 64.
	 * @param configB64 Properties de configuraci&oacute;n en Base 64.
	 * @throws IOException Cuando el par&aacute;metro no est&eacute;
	 * bien formado y no se pueda decodificar.
	 */
	public TransactionConfig(final String configB64) throws IOException {
		this.config = ServiceUtil.base642Properties(configB64);
	}

	/**
	 * Recupera la URL a la que redirigir en caso de &eacute;xito.
	 * @return URL a la que redirigir.
	 */
	public String getRedirectSuccessUrl() {
		return this.config.getProperty(PARAM_SUCCESS_URL);
	}

	/**
	 * Actualiza la URL a la que redirigir en caso de &eacute;xito.
	 * @param url URL a la que redirigir.
	 */
	public void setRedirectSuccessUrl(final String url) {
		this.config.setProperty(PARAM_SUCCESS_URL, url);
	}

	/**
	 * Recupera la URL a la que redirigir en caso de error.
	 * @return URL a la que redirigir.
	 */
	public String getRedirectErrorUrl() {
		String url = this.config.getProperty(PARAM_ERROR_URL);
		if (url == null) {
			url = this.config.getProperty(PARAM_SUCCESS_URL);
		}
		return url;
	}

	/**
	 * Recupera la URL a la que redirigir en caso de error.
	 * @return URL a la que redirigir.
	 */
	public boolean isDefinedRedirectErrorUrl() {
		return this.config.containsKey(PARAM_ERROR_URL) ||
				this.config.containsKey(PARAM_SUCCESS_URL);
	}

	/**
	 * Recupera el DocumentManager que se debe utilizar en la transacci&oacute;n.
	 * @return Identificador del DocumentManager o {@code null} si no se especific&oacute;.
	 */
	public String getDocumentManager() {
		return this.config.getProperty(PARAM_DOCUMENT_MANAGER);
	}

	/**
	 * Recupera el t&iacute;tulo de la aplicaci&oacute;n cliente en formato legible.
	 * @return T&iacute;tulo de la aplicaci&oacute;n o {@code null} si no se especific&oacute;.
	 */
	public String getAppTitle() {
		return this.config.getProperty(PARAM_APPLICATION_TITLE);
	}

	/**
	 * Recupera el listado de proveedores que se desean utilizar.
	 * @return Listado de proveedores o {@code null} si no se defini&oacute;.
	 */
	public String[] getProviders() {
		if (this.config.containsKey(PARAM_CERT_ORIGIN) &&
				!this.config.getProperty(PARAM_CERT_ORIGIN).trim().isEmpty()) {
			return this.config.getProperty(PARAM_CERT_ORIGIN).split(VALUES_SEPARATOR);
		}
		return null;
	}

	/**
	 * Obtiene un nuevo objeto de configuraci&oacute;n con las propiedades
	 * innecesarias eliminadas.
	 * @return Configuraci&oacute;n de la transacci&oacute;n.
	 */
	public TransactionConfig cleanConfig() {

		// Copiamos las propiedades y eliminamos aquellas necesarias
		final Properties newConfig = (Properties) this.config.clone();
		newConfig.remove(PARAM_DOCUMENT_MANAGER);
		newConfig.remove(PARAM_APPLICATION_TITLE);

		return new TransactionConfig(newConfig) ;
	}

	/**
	 * Recupera el conjunto de propiedades establecidas de la transacci&oacute;n.
	 * @return Propiedades de la transacci&oacute;n.
	 */
	public Properties getProperties() {
		return this.config;
	}

	/**
	 * Recupera valor del par&aacute;metro que indica si se debe omitir la pantalla de seleccion de certificados
	 * @return El par&aacute;metro skipcertselection.
	 */
	public Boolean isAppSkipCertSelection() {
		final String skipSelection = this.config.getProperty(PARAM_SKIP_CERT_SELECTION);
		return skipSelection == null ? null : Boolean.valueOf(skipSelection);
	}
}
