package es.gob.fire.server.services.internal;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Properties;
import java.util.logging.Logger;

import es.gob.fire.alarms.Alarm;
import es.gob.fire.signature.ConfigException;
import es.gob.fire.signature.DbManager;
import es.gob.fire.signature.ProviderElements;
import es.gob.fire.signature.TempConfigLoader;

public class DBOperationConfigLoader {

	private static final String SQL_SELECT_DEFAULT_PROVIDERS = "SELECT nombre, obligatorio FROM tb_proveedores WHERE habilitado = true ORDER BY orden"; //$NON-NLS-1$

	private static final String STATEMENT_SELECT_OPERATION_CONFIG = "SELECT tamano_peticion, tamano_documento, tamano_lote, proveedores FROM tb_aplicaciones  WHERE  tb_aplicaciones.id =  ?"; //$NON-NLS-1$

	private static final Logger LOGGER = Logger.getLogger(DBOperationConfigLoader.class.getName());

	private final DBApplicationConfigLoader configLoader;

	public DBOperationConfigLoader() {
		this.configLoader = new DBApplicationConfigLoader();
	}

	public ApplicationOperationConfig getOperationConfig(final String app) {

		final ApplicationOperationConfig config = (ApplicationOperationConfig) this.configLoader.getObject("app" + app);

		return config != null ? config : (ApplicationOperationConfig) this.configLoader.getObject("default");
	}

	/**
	 * Clase para la carga de l
	 */
	private static class DBApplicationConfigLoader extends TempConfigLoader {

		@Override
		public Hashtable<Object, Object> loadConfiguration() throws IOException, ConfigException {

			Hashtable<Object, Object> result = new Hashtable<>();
			
			try (Connection conn = DbManager.getConnection()) {

				final String defaultProviders = loadDefaultProviders(conn);

				

				final ApplicationOperationConfig defaultConfig = new ApplicationOperationConfig();
				defaultConfig.set
				
				
				
				result.put("default", defaultConfig);
				
				

				

//					final boolean configured = rs.getBoolean(1);
//					if (configured) {
//						result = new AplicationOperationConfig();
//						result.setRequestMaxSize(rs.getInt(2));
//						result.setParamsMaxSize(rs.getInt(3));
//						result.setBatchMaxDocuments(rs.getInt(4));
//						result.setProviders(ProviderElements.parse(rs.getString(5)));
//					}
//				}


				// ---------
				// Cargamos la configuracion por defecto
				// ---------

				// Listado de proveedores

				// Limites de configuracion

				// ---------
				// Cargamos la configuracion de las aplicaciones
				// ---------

				// Listado de proveedores

				// Limites de configuracion


				// Comprobamos en BD

			}
			catch (final SQLException e) {
				AlarmsManager.notify(Alarm.CONNECTION_DB);
				throw new IOException("Error al consultar en BD la configuracion de la apicacion", e); //$NON-NLS-1$
			}

			return result;
		}

		/**
		 * Obtiene el listado de proveedores por defecto.
		 * @param conn Conexi&oacute;n con base de datos.
		 * @return Cadena con la configuraci&oacute;n de proveedores que debe usarse por defecto.
		 * @throws ConfigException
		 * @throws SQLException
		 */
		private String loadDefaultProviders(final Connection conn) throws ConfigException, SQLException {

			final StringBuilder providers = new StringBuilder();

			final PreparedStatement st = conn.prepareStatement(SQL_SELECT_DEFAULT_PROVIDERS);
			try (ResultSet rs = st.executeQuery()) {
				if (!rs.next()) {
					throw new ConfigException("No se han encontrado proveedores configurados"); //$NON-NLS-1$
				}


				do {
					if (!rs.isFirst()) {
						providers.append(",");
					}

					final String provider = rs.getString(1);
					if (rs.getBoolean(2)) {
						providers.append("@");
					}
					providers.append(provider);
				} while (rs.next());
			}

			return providers.toString();
		}

		private String loadDefaultConfigOperation(final Connection conn) {
			final PreparedStatement st = conn.prepareStatement(SQL_SELECT_DEFAULT_PROVIDERS);

			try (ResultSet rs = st.executeQuery()) {


				final boolean configured = rs.getBoolean(1);
				if (configured) {
					result = new AplicationOperationConfig();
					result.setRequestMaxSize(rs.getInt(2));
					result.setParamsMaxSize(rs.getInt(3));
					result.setBatchMaxDocuments(rs.getInt(4));
					result.setProviders(ProviderElements.parse(rs.getString(5)));
				}
			}

			return null;
		}
	}
}
