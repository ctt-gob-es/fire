package es.gob.fire.server.services.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

import es.gob.afirma.core.misc.Base64;
import es.gob.fire.services.statistics.FireSignLogger;

/**
 * Informaci&oacute;n necesaria de un proveedor para permitir su correcto uso
 * por parte del componente central de firma.
 */
public class ProviderInfo {

	private static final String PROP_LOGO_PATH = "logo"; //$NON-NLS-1$

	private static final String DEFAULT_LOGO_URI = ""; //$NON-NLS-1$

	private static final String PROP_TITLE = "title"; //$NON-NLS-1$

	private static final String DEFAULT_TITLE = "{{TITULO}}"; //$NON-NLS-1$

	private static final String PROP_HEADER = "header"; //$NON-NLS-1$

	private static final String DEFAULT_HEADER = "{{CABECERA}}"; //$NON-NLS-1$

	private static final String PROP_DESCRIPTION = "description"; //$NON-NLS-1$

	private static final String DEFAULT_DESCRIPTION = "{{DESCRIPCION DEL PROVEEDOR}}"; //$NON-NLS-1$

	private static final String PROP_NO_REGISTERED = "noregistered"; //$NON-NLS-1$

	private static final String DEFAULT_NO_REGISTERED = "{{USUARIO NO REGISTRADO}}"; //$NON-NLS-1$

	private static final String PROP_NEED_JAVASCRIPT = "needjavascript"; //$NON-NLS-1$

	private static final String DEFAULT_NEED_JAVASCRIPT = Boolean.FALSE.toString();

	private static final String DATA_URI_SCHEME = "data:"; //$NON-NLS-1$
	private static final String HTTP_URI_SCHEME = "http:"; //$NON-NLS-1$
	private static final String HTTPS_URI_SCHEME = "https:"; //$NON-NLS-1$

	private static final String DATA_URI_IMAGE_HEADER = "image/png;base64,"; //$NON-NLS-1$

	private static Logger LOGGER =  FireSignLogger.getFireSignLogger().getFireLogger().getLogger();
//	private static final Logger LOGGER = Logger.getLogger(ProviderInfo.class.getName());

	private final String name;
	private final Properties config;

	/**
	 * Obtiene la informaci&oacute;n de un proveedor a partir de la configuraci&oacute;n
	 * de dicho proveedor.
	 * @param name Nombre del proveedor.
	 * @param config Configuraci&oacute;n del proveedor.
	 */
	public ProviderInfo(final String name, final Properties config) {
		if (config == null) {
			throw new NullPointerException("La configuracion del proveedor no puede ser nula"); //$NON-NLS-1$
		}
		this.name = name;
		this.config = (Properties) config.clone();
	}

	/**
	 * Obtiene el nombre asignado al proveedor.
	 * @return Nombre del proveedor.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Obtiene el t&iacute;tulo del proveedor.
	 * @return T&iacute;tulo del proveedor.
	 */
	public String getTitle() {
		return this.config.getProperty(PROP_TITLE, DEFAULT_TITLE);
	}

	/**
	 * Obtiene la cabecera que debe mostrar el proveedor.
	 * @return Cabecera del proveedor.
	 */
	public String getHeader() {
		return this.config.getProperty(PROP_HEADER, DEFAULT_HEADER);
	}

	/**
	 * Obtiene la descripci&oacute;n del proveedor.
	 * @return Descripci&oacute;n del proveedor.
	 */
	public String getDescription() {
		return this.config.getProperty(PROP_DESCRIPTION, DEFAULT_DESCRIPTION);
	}

	/**
	 * Obtiene el mensaje que indica que el usuario no est&aacute; registrado.
	 * @return Mensaje de error.
	 */
	public String getNoRegisteredMessage() {
		return this.config.getProperty(PROP_NO_REGISTERED, DEFAULT_NO_REGISTERED);
	}

	/**
	 * Obtiene la URI para la visualizaci&oacute;n del logo del proveedor.
	 * @return URI del logo del proveedor.
	 */
	public String getLogoUri() {

		// Si es una URI compatible, la usamos directamente
		String uri = this.config.getProperty(PROP_LOGO_PATH, DEFAULT_LOGO_URI);
		if (uri.isEmpty() ||
				uri.startsWith(DATA_URI_SCHEME) ||
				uri.startsWith(HTTP_URI_SCHEME) ||
				uri.startsWith(HTTPS_URI_SCHEME)) {
			return uri;
		}

		// Ya que no es una URI compatible, interpretamos que es la ruta de un fichero PNG

		// Preparamos la ruta
		uri = uri.replace('\\', '/');
		if (!uri.startsWith("/")) { //$NON-NLS-1$
			uri = "/" + uri; //$NON-NLS-1$
		}
		// Cargamos el logo
		byte[] logo;
		try (final InputStream logoIs = ProviderInfo.class.getResourceAsStream(uri);) {
			logo = readInputStream(logoIs);
		}
		catch (final Exception e) {
			LOGGER.warning(String.format("No se ha podido cargar la imagen %s: " + e, uri)); //$NON-NLS-1$
			return DEFAULT_LOGO_URI;
		}
		// Componentemos la URI
		return DATA_URI_SCHEME + DATA_URI_IMAGE_HEADER + Base64.encode(logo);
	}

	/**
	 * Indica si el proveedor requiere JavaScript para funcionar correctamente.
	 * @return {@code true} si el proveedor requiere JavaScript, {@code false}
	 * en caso contrario.
	 */
	public boolean isNeedJavaScript() {
		return Boolean.parseBoolean(
				this.config.getProperty(PROP_NEED_JAVASCRIPT,
						DEFAULT_NEED_JAVASCRIPT));
	}

	/**
	 * Lee un flujo de datos de entrada.
	 * @param is Flujo de datos.
	 * @return Datos le&iacute;dos.
	 * @throws IOException Cuando ocurre un error en la lectura.
	 */
	private static byte[] readInputStream(final InputStream is) throws IOException {

		int n;
		final byte[] buffer = new byte[2048];
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		while ((n = is.read(buffer)) > 0) {
			baos.write(buffer, 0, n);
		}
		return baos.toByteArray();
	}
}
