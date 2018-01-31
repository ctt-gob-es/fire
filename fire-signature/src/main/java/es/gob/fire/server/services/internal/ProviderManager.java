package es.gob.fire.server.services.internal;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import es.gob.fire.server.connector.FIReConnector;
import es.gob.fire.server.connector.FIReConnectorFactory;
import es.gob.fire.server.connector.FIReConnectorFactoryException;
import es.gob.fire.signature.ConfigFileLoader;
import es.gob.fire.signature.ConfigManager;

/**
 * Gestor para la obtenci&oacute;n de conectores ya configurados para iniciar
 * transacciones con un proveedor.
 */
public class ProviderManager {

	private static final String SUFIX_PROVIDER_CONFIG = "_config.properties"; //$NON-NLS-1$

	private static final Logger LOGGER = Logger.getLogger(ProviderManager.class.getName());

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
		Properties providerConfig;
		try {
			providerConfig = ConfigFileLoader.loadConfigFile(providerName + SUFIX_PROVIDER_CONFIG);
		} catch (final FileNotFoundException e) {
			LOGGER.warning(String.format(
					"No se ha encontrado el fichero de configuracion del proveedor %s: " + e, //$NON-NLS-1$
					providerName
			));
			providerConfig = new Properties();
		} catch (final IOException e) {
			LOGGER.log(
					Level.SEVERE,
					String.format(
							"No se ha podido cargar el fichero de configuracion del proveedor %s", //$NON-NLS-1$
							providerName),
					e);
			providerConfig = new Properties();
		}

		// Inicializamos el proveedor
		final FIReConnector connector = FIReConnectorFactory.getConnector(providerClass);
		connector.init((Properties) providerConfig.clone());

		// Inicializamos la transaccion
		connector.initOperation(transactionConfig);

		return connector;
	}
}
