package es.gob.fire.server.services.internal;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mysql.jdbc.log.LogUtils;

import es.gob.fire.server.connector.FIReConnector;
import es.gob.fire.server.connector.FIReConnectorFactory;
import es.gob.fire.server.connector.FIReConnectorFactoryException;
import es.gob.fire.server.services.LogUtils;
import es.gob.fire.signature.ConfigFileLoader;
import es.gob.fire.signature.ConfigManager;
import es.gob.fire.signature.ProviderElement;

/**
 * Gestor para la obtenci&oacute;n de conectores ya configurados para iniciar
 * transacciones con un proveedor.
 */
public class ProviderManager {

	private static final Logger LOGGER = Logger.getLogger(ProviderManager.class.getName());

	private static final String PROVIDER_INFO_FILE = "provider_info.properties"; //$NON-NLS-1$

	private static final String PROVIDER_CONFIG_FILE_TEMPLATE = "provider_%s.properties"; //$NON-NLS-1$

	private static final String LOCAL_PROVIDER_INFO_PATH = "/es/gob/fire/server/resources/local_provider.properties"; //$NON-NLS-1$

	/** Nombre del proveedor local. */
	public static final String PROVIDER_NAME_LOCAL = "local"; //$NON-NLS-1$

	/**
	 * Inicializamos una transacci&oacute;n a trav&eacute;s de un proveedor.
	 * @param providerName Nombre del proveedor.
	 * @param transactionConfig Configuraci&oacute;n de la transacci&oacute;n.
	 * @return Conector con el proveedor ya configurado para realizar cualquier transacci&oacute;n.
	 * @throws FIReConnectorFactoryException Cuando falle la inicializaci&oacute;n del conector.
	 */
	public static FIReConnector initTransacction(final String providerName, final Properties transactionConfig)
			throws FIReConnectorFactoryException{

		// Obtenemos la clase del connector
		final String providerClass = ConfigManager.getProviderClass(providerName);
		if (providerClass == null) {
			throw new FIReConnectorFactoryException(
					"No se ha encontrado el nombre de la clase conectora del proveedor " + providerName); //$NON-NLS-1$
		}

		// Obtenemos el fichero de configuracion del proveedor
		final Properties providerConfig = loadProviderConfig(providerName);

		// Si se ha definido una clase para el descifrado de constrasenas, actualizamos
		// el objeto con los valores descifrados de cada una de ellas
		if (ConfigManager.hasDecipher()) {
			for (final String key : providerConfig.keySet().toArray(new String[providerConfig.size()])) {
				providerConfig.setProperty(key, ConfigManager.getDecipheredProperty(providerConfig, key, null));
			}
		}

		// Inicializamos el proveedor
		final FIReConnector connector = FIReConnectorFactory.getConnector(providerClass);
		connector.init(providerConfig);

		// Inicializamos la transaccion
		connector.initOperation(transactionConfig);

		return connector;
	}

	/**
	 * Obtiene el listado de proveedores configurados.
	 * @return Listado con los proveedores.
	 */
	public static ProviderElement[] getProviders() {
		return ConfigManager.getProviders();
	}

	/**
	 * Obtiene el listado con el nombre de los proveedores configurados.
	 * @return Listado con los nombres de los proveedores.
	 */
	public static String[] getProviderNames() {
		final ProviderElement[] provs = ConfigManager.getProviders();
		final String[] provNames = new String[provs.length];
		for (int i = 0; i < provs.length; i++) {
			provNames[i] = provs[i].getName();
		}
		return provNames;
	}

	/**
	 * Obtiene la informaci&oacute;n necesaria de un proveedor para pod&eacute;rsela
	 * mostrar a un usuario y que as&iacute; identifique su uso.
	 * @param providerName Nombre del proveedor.
	 * @return Informaci&oacute;n del proveedor.
	 */
	public static ProviderInfo getProviderInfo(final String providerName) {

		Properties infoProperties;
		if (PROVIDER_NAME_LOCAL.equalsIgnoreCase(providerName)) {
			infoProperties = loadLocalProviderInfoProperties();
		}
		else {
			final String classname = ConfigManager.getProviderClass(providerName);
			infoProperties = loadProviderInfoProperties(classname);
		}
		return new ProviderInfo(providerName, infoProperties);
	}

	/**
	 * Carga el fichero de configuraci&oacute;n de un proveedor.
	 * @param providerName Nombre el proveedor.
	 * @return Configuraci&oacute;n cargada.
	 */
	private static Properties loadProviderConfig(final String providerName) {

		final String providerConfigFilename = String.format(PROVIDER_CONFIG_FILE_TEMPLATE, providerName);
		Properties providerConfig;
		try {
			providerConfig = ConfigFileLoader.loadConfigFile(providerConfigFilename);
		} catch (final FileNotFoundException e) {
			LOGGER.warning(String.format(
					"No se ha encontrado el fichero '%s' para la configuracion del proveedor '%s': " + e, //$NON-NLS-1$
					LogUtils.cleanText(providerConfigFilename), LogUtils.cleanText(providerName)
			));
			providerConfig = new Properties();
		} catch (final IOException e) {
			LOGGER.log(
					Level.SEVERE,
					String.format(
							"No se ha podido cargar el fichero de configuracion del proveedor %s", //$NON-NLS-1$
							LogUtils.cleanText(providerName)),
					e);
			providerConfig = new Properties();
		}

		return providerConfig;
	}

	/**
	 * Carga el fichero interno de propiedades del proveedor en el que se encuentra
	 * la informaci&oacute;n generica que debe proporcionar. El fichero debe tener
	 * el nombre determinado por {@link #PROVIDER_INFO_FILE} y encontrarse en el
	 * mismo paquete que la clase conectora.
	 * @param classname Clase conectora del proveedor.
	 * @return Properties cargado.
	 */
	private static Properties loadProviderInfoProperties(final String classname) {

		String classPath;
		if (classname.lastIndexOf('.') == -1) {
			classPath = classname;
		} else {
			classPath = classname.substring(0, classname.lastIndexOf('.')).replace('.', '/');
		}
		if (!classPath.startsWith("/")) { //$NON-NLS-1$
			classPath = "/" + classPath; //$NON-NLS-1$
		}
		if (!classPath.endsWith("/")) { //$NON-NLS-1$
			classPath += "/"; //$NON-NLS-1$
		}
		final String providerInfoPath = classPath + PROVIDER_INFO_FILE;
		return loadInternalProperties(providerInfoPath);
	}

	/**
	 * Carga el fichero interno de propiedades del proveedor de firma con certificados
	 * locales.
	 * @return Properties cargado.
	 */
	private static Properties loadLocalProviderInfoProperties() {
		return loadInternalProperties(LOCAL_PROVIDER_INFO_PATH);
	}

	/**
	 * Carga un fichero interno de propiedades.
	 * @param path Ruta interna del fichero.
	 * @return Properties cargado.
	 */
	private static Properties loadInternalProperties(final String path) {

		final Properties providerInfoProperties = new Properties();
		try (InputStream is = ProviderManager.class.getResourceAsStream(path)) {
			providerInfoProperties.load(is);
		}
		catch (final Exception e) {
			LOGGER.warning(
				String.format(
					"No se ha encontrado o no ha podido cargarse el fichero interno '%s' con la informacion del proveedor", //$NON-NLS-1$
					path)
			);
		}
		return providerInfoProperties;
	}

	/**
	 * Filtra los proveedores configurados para s&oacute;lo mostrar aquellos solicitados
	 * por la aplicaci&oacute;n y aquellos configurados como imprescindibles. Los
	 * proveedores indicados por la aplicaci&oacute;n y no configurados en el componente
	 * central se ignoran.
	 * @param requestedProviders Proveedores solicitados.
	 * @return Listado de proveedores ya filtrados.
	 */
	public static String[] getFilteredProviders(final String[] requestedProviders) {

		final List<String> filteredProviders = new ArrayList<>();
		final ProviderElement[] allProviders = getProviders();

		// Agregamos al listado final los proveedores solicitados en el orden
		// en el que se indican
		boolean added;
		for (final String rProv : requestedProviders) {
			added = false;
			// Si el proveedor ya se agrego, se ignora
			if (filteredProviders.contains(rProv)) {
				added = true;
			}
			// Recorremos los proveedores disponibles para comprobar que
			// el solicitado esta disponible
			int i = 0;
			while (i < allProviders.length && !added) {
				final ProviderElement cProv = allProviders[i];
				if (cProv.equals(rProv)) {
					filteredProviders.add(rProv);
					added = true;
				}
				i++;
			}
		}

		// Al final de la lista, agregamos los proveedores imprescindibles que se
		// configurase en el componente central y no esten ya en la lista
		for (final ProviderElement prov : allProviders) {
			if (prov.isIndispensable() && !filteredProviders.contains(prov.getName())) {
				filteredProviders.add(prov.getName());
			}
		}
		return filteredProviders.toArray(new String[filteredProviders.size()]);
	}
}
