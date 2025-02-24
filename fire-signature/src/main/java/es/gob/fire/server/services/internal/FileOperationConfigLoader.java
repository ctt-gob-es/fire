package es.gob.fire.server.services.internal;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;

import es.gob.fire.alarms.Alarm;
import es.gob.fire.signature.ConfigException;
import es.gob.fire.signature.ConfigManager;
import es.gob.fire.signature.DbManager;
import es.gob.fire.signature.TempConfigLoader;

public class FileOperationConfigLoader {

	private static final String KEY_CONFIG = "config"; //$NON-NLS-1$

	private final FileApplicationConfigLoader configLoader;

	public FileOperationConfigLoader() {
		this.configLoader = new FileApplicationConfigLoader();
	}

	public ApplicationOperationConfig getOperationConfig(final String app) {
		return (ApplicationOperationConfig) this.configLoader.getObject(KEY_CONFIG);
	}

	/**
	 * Clase para la carga de la configuraci&oacute;n de las aplicaciones. Se encarga de que peri&oacute;dicamente
	 * la configuraci&oacute;n se renueve.
	 */
	private static class FileApplicationConfigLoader extends TempConfigLoader {

		@Override
		public Hashtable<Object, Object> loadConfiguration() throws IOException, ConfigException {

			final Hashtable<Object, Object> result = new Hashtable<>();

			try (final Connection conn = DbManager.getConnection()) {

				// Cargamos la configuracion
				final ApplicationOperationConfig config = loadConfigOperation(conn);
				result.put(KEY_CONFIG, config);

			}
			catch (final SQLException e) {
				AlarmsManager.notify(Alarm.CONNECTION_DB);
				throw new IOException("Error al consultar en BD la configuracion de la apicacion", e); //$NON-NLS-1$
			}

			return result;
		}

		private static ApplicationOperationConfig loadConfigOperation(final Connection conn) throws SQLException, ConfigException {


			final ApplicationOperationConfig config = new ApplicationOperationConfig();
			config.setBatchMaxDocuments(ConfigManager.getBatchMaxDocuments());
			config.setParamsMaxSize(ConfigManager.getParamMaxSize());
			config.setRequestMaxSize(ConfigManager.getRequestMaxSize());
			config.setProviders(ConfigManager.getProviders());

			return config;
		}
	}
}
