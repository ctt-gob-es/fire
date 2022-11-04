package es.gob.fire.server.services.internal;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import es.gob.fire.alarms.Alarm;
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

	/** Informaci&oacute;n para la presentaci&oacute;n y comportamiento del conector de un proveedor */
	private static Map<String, ProviderInfo> providersInfo = new HashMap<>();

	/** Configuraci&oacute;n de un conector para el acceso a su servicio. */
	private static Map<String, Properties> providersConfig = new HashMap<>();


	/**
	 * Obtenemos el conector necesario para operar con un proveedor de firma en la nube.
	 * @param providerName Nombre del proveedor.
	 * @param transactionConfig Configuraci&oacute;n a aplicar al conector.
	 * @return Conector con el proveedor ya configurado para realizar cualquier transacci&oacute;n.
	 * @throws FIReConnectorFactoryException Cuando falla la inicializaci&oacute;n del conector.
	 */
	public static FIReConnector getProviderConnector(final String providerName, final Properties transactionConfig)
			throws FIReConnectorFactoryException {

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

		// Cargamos el conector
		FIReConnector connector;
		try {
			connector = FIReConnectorFactory.getConnector(providerClass);
		}
		catch (final FIReConnectorFactoryException e) {
			AlarmsManager.notify(Alarm.LIBRARY_NOT_FOUND, providerClass);
			throw e;
	    }
		catch (final Throwable e) {
			AlarmsManager.notify(Alarm.LIBRARY_NOT_FOUND, providerClass);
			throw new FIReConnectorFactoryException("Error grave al cargar el conector", e); //$NON-NLS-1$
		}

		// Inicializamos el conector
		try {
			connector.init(providerConfig);
		}
		catch (final Throwable e) {
			AlarmsManager.notify(Alarm.CONNECTION_SIGNATURE_PROVIDER, providerName);
			throw new FIReConnectorFactoryException("No se pudo inicializar el conector", e); //$NON-NLS-1$
		}


		// Inicializamos la transaccion
		try {
			connector.initOperation(transactionConfig);
		}
		catch (final Throwable e) {
			AlarmsManager.notify(Alarm.CONNECTION_SIGNATURE_PROVIDER, providerName);
			throw new FIReConnectorFactoryException("No se pudo inicializar la transaccion con el conector", e); //$NON-NLS-1$
		}

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

		if (providersInfo.containsKey(providerName)) {
			return providersInfo.get(providerName);
		}

		Properties infoProperties;
		if (PROVIDER_NAME_LOCAL.equalsIgnoreCase(providerName)) {
			infoProperties = loadLocalProviderInfoProperties();
		}
		else {
			final String classname = ConfigManager.getProviderClass(providerName);
			final String infoFilename = ConfigManager.getProviderInfoFile(providerName);

			infoProperties = loadProviderInfoProperties(classname, null);

			// Si se detecta un fichero 'provider info' externo, miramos primero si el conector permite usarlo mediante la
			// propiedad 'allowexternalproviderinfo', en caso de que no se permita se cargaran las propiedades del fichero
			// 'provider info' interno.

			final boolean allowExternalProviderInfo = ProviderInfo.isAllowExternalProviderInfo(infoProperties);

			if (infoFilename != null && allowExternalProviderInfo) {
				infoProperties = loadProviderInfoProperties(classname, infoFilename);
			}

		}

		// Contruimos la informacion del proveedor y la almacenamos en la coleccion
		// para evitar su recarga
		final ProviderInfo providerInfo = new ProviderInfo(providerName, infoProperties);
		providersInfo.put(providerName, providerInfo);

		return providerInfo;
	}

	/**
	 * Carga la configuraci&oacute;n de un proveedor, de cache si ya la hab&iacute;a
	 * cargado anteriormente o de fichero si no.
	 * @param providerName Nombre el proveedor.
	 * @return Configuraci&oacute;n cargada.
	 */
	private static Properties loadProviderConfig(final String providerName) {

		Properties providerConfig;
		if (providersConfig.containsKey(providerName)) {
			providerConfig = providersConfig.get(providerName);
		} else {
			providerConfig = loadProviderConfigFromFile(providerName);
			providersConfig.put(providerName, providerConfig);
		}

		return providerConfig;
	}

	/**
	 * Carga el fichero de configuraci&oacute;n de un proveedor.
	 * @param providerName Nombre el proveedor.
	 * @return Configuraci&oacute;n cargada.
	 */
	private static Properties loadProviderConfigFromFile(final String providerName) {

		Properties providerConfig;
		final String providerConfigFilename = String.format(PROVIDER_CONFIG_FILE_TEMPLATE, providerName);
		try {
			providerConfig = ConfigFileLoader.loadConfigFile(providerConfigFilename);

		} catch (final FileNotFoundException e) {
			LOGGER.warning(String.format(
					"No se ha encontrado el fichero '%s' para la configuracion del proveedor '%s': " + e, //$NON-NLS-1$
					LogUtils.cleanText(providerConfigFilename), LogUtils.cleanText(providerName)
					));
			AlarmsManager.notify(Alarm.RESOURCE_NOT_FOUND, providerConfigFilename);
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

		try {
			providerConfig = ConfigManager.mapEnvironmentVariables(providerConfig);
		}
		catch (final Exception e) {
			LOGGER.log(
					Level.SEVERE,
					String.format(
							"No se han podido mapear las variables declaradas en el fichero de configuracion del proveedor %s", //$NON-NLS-1$
							LogUtils.cleanText(providerName)),
					e);
		}
		return providerConfig;
	}

	/**
	 * Carga el fichero de propiedades en el que se encuentra la configuraci&oacute;n interna del
	 * conector. Este fichero puede indicarse externamente o, si no, se cargar&aacute; el fichero
	 * interno. El fichero interno debe tener el nombre determinado por {@link #PROVIDER_INFO_FILE}
	 * y encontrarse en el mismo paquete que la clase conectora.
	 * @param classname Clase conectora del proveedor.
	 * @param infoFilename Nombre del fichero externo con las propiedades visuales
	 * y comprobaciones del proveedor. Debe encontrarse con el resto de ficheros de
	 * configuraci&oacute;n.
	 * @return Propiedades de visualizaci&oacute;n.
	 */
	private static Properties loadProviderInfoProperties(final String classname, final String infoFilename) {

		Properties infoProperties;

		// Si se configuro un fichero externo con la informacion del proveedor, se cargara
		if (infoFilename != null) {
			try {
				infoProperties = ConfigFileLoader.loadConfigFile(infoFilename);
			}
			catch (final Exception e) {
				LOGGER.warning(
						String.format(
								"No se ha encontrado o no ha podido cargarse el fichero externo '%s'", //$NON-NLS-1$
								infoFilename)
						);
				infoProperties = new Properties();
			}
		}
		// En caso contrario, se carga el fichero interno
		else {
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
			infoProperties = loadInternalProperties(providerInfoPath);
		}
		return infoProperties;
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
		try (InputStream is = ProviderManager.class.getResourceAsStream(path);
			InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
			providerInfoProperties.load(reader);
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
