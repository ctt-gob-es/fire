package es.gob.fire.server.services.internal;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import es.gob.fire.alarms.Alarm;
import es.gob.fire.signature.ConfigException;
import es.gob.fire.signature.DbManager;
import es.gob.fire.signature.TempConfigLoader;

public class FileOperationConfigLoader {

	private static final String SQL_SELECT_DEFAULT_PROVIDERS = "SELECT nombre, obligatorio FROM tb_proveedores WHERE habilitado = TRUE ORDER BY orden"; //$NON-NLS-1$
	private static final String SQL_SELECT_DEFAULT_PROPERTIES = "SELECT clave, valor_numerico FROM tb_propiedades WHERE tipo = 'NUMBER'"; //$NON-NLS-1$

	private static final String SQL_SELECT_APP_PROPERTIES = "SELECT id_aplicacion, tamano_maximo_documento, tamano_maximo_peticion, cantidad_maxima_documentos FROM tb_aplicaciones WHERE tamano_personalizado = TRUE"; //$NON-NLS-1$
	private static final String SQL_SELECT_APP_PROVIDERS = "SELECT app.id_aplicacion, prov.nombre, rel.obligatorio " //$NON-NLS-1$
			+ "FROM tb_aplicaciones app, tb_proveedores_aplicacion rel, ib_proveedores prov " //$NON-NLS-1$
			+ "WHERE app.proveedor_personalizado = TRUE AND app.id_aplicacion = rel.id_aplicacion AND prov.id_proveedor = rel.id_proveedor AND rel.habilitado = TRUE AND prov.habilitado = TRUE " //$NON-NLS-1$
			+ "ORDER BY rel.id_aplicacion, rel.orden"; //$NON-NLS-1$

	private static final String PROVIDER_MANDATORY_SIGN = "@"; //$NON-NLS-1$
	private static final String PROVIDER_SEPARATOR_SIGN = ","; //$NON-NLS-1$

	private static final String CONFIG_APP_PREFIX = "app"; //$NON-NLS-1$
	private static final String CONFIG_DEFAULT = "default"; //$NON-NLS-1$

	private final DBApplicationConfigLoader configLoader;

	public FileOperationConfigLoader() {
		this.configLoader = new DBApplicationConfigLoader();
	}

	public ApplicationOperationConfig getOperationConfig(final String app) {

		final ApplicationOperationConfig config = (ApplicationOperationConfig) this.configLoader.getObject(CONFIG_APP_PREFIX + app);

		return config != null ? config : (ApplicationOperationConfig) this.configLoader.getObject(CONFIG_DEFAULT);
	}

	/**
	 * Clase para la carga de la configuraci&oacute;n de las aplicaciones. Se encarga de que peri&oacute;dicamente
	 * la configuraci&oacute;n se renueve.
	 */
	private static class DBApplicationConfigLoader extends TempConfigLoader {

		@Override
		public Hashtable<Object, Object> loadConfiguration() throws IOException, ConfigException {

			final Hashtable<Object, Object> result = new Hashtable<>();

			try (final Connection conn = DbManager.getConnection()) {

				// Cargamos la configuracion por defecto
				final ApplicationOperationConfig defaultConfig = loadDefaultConfigOperation(conn);
				result.put(CONFIG_DEFAULT, defaultConfig);

				// Cargamos la configuracion particular para cada aplicacion
				final Map<String, ApplicationOperationConfig> particularsConfig = loadAppsParticularConfig(conn, defaultConfig);
				particularsConfig.forEach((appId, config) -> {
					result.put(CONFIG_APP_PREFIX + appId, config);
				});
			}
			catch (final SQLException e) {
				AlarmsManager.notify(Alarm.CONNECTION_DB);
				throw new IOException("Error al consultar en BD la configuracion de la apicacion", e); //$NON-NLS-1$
			}

			return result;
		}

		private static ApplicationOperationConfig loadDefaultConfigOperation(final Connection conn) throws SQLException, ConfigException {


			final ApplicationOperationConfig config = new ApplicationOperationConfig();

			try (final PreparedStatement st = conn.prepareStatement(SQL_SELECT_DEFAULT_PROPERTIES);
					ResultSet rs = st.executeQuery()) {
				while (rs.next()) {
					switch (rs.getString(1)) {
					case "TAMANO_MAXIMO_DOC": //$NON-NLS-1$
						config.setParamsMaxSize(rs.getInt(2));
						break;
					case "TAMANO_MAXIMO_PETICION": //$NON-NLS-1$
						config.setRequestMaxSize(rs.getInt(2));
						break;
					case "CANTIDAD_MAXIMA_DOCUMENTOS": //$NON-NLS-1$
						config.setBatchMaxDocuments(rs.getInt(2));
						break;
					default:
					}
				}
			}

			final String providers = loadDefaultProviders(conn);
			config.setProviders(providers);

			return config;
		}

		/**
		 * Obtiene el listado de proveedores por defecto.
		 * @param conn Conexi&oacute;n con base de datos.
		 * @return Cadena con la configuraci&oacute;n de proveedores que debe usarse por defecto.
		 * @throws ConfigException
		 * @throws SQLException
		 */
		private static String loadDefaultProviders(final Connection conn) throws ConfigException, SQLException {

			final StringBuilder providers = new StringBuilder();

			try (final PreparedStatement st = conn.prepareStatement(SQL_SELECT_DEFAULT_PROVIDERS);
					ResultSet rs = st.executeQuery()) {
				if (!rs.next()) {
					throw new ConfigException("No se han encontrado proveedores configurados"); //$NON-NLS-1$
				}

				do {
					if (!rs.isFirst()) {
						providers.append(PROVIDER_SEPARATOR_SIGN);
					}

					final String provider = rs.getString(1);
					if (rs.getBoolean(2)) {
						providers.append(PROVIDER_MANDATORY_SIGN);
					}
					providers.append(provider);
				} while (rs.next());
			}

			return providers.toString();
		}

		private static Map<String, ApplicationOperationConfig> loadAppsParticularConfig(final Connection conn, final ApplicationOperationConfig defaultConfig) throws SQLException, ConfigException {

			// Obtenemos los parametros y los proveedores que se han establecido personalidzados para rodas las aplicaciones
			final Map<String, ApplicationOperationConfig> sizes = getAppsParticularSizes(conn);
			final Map<String, String> providers = getAppsParticularProviders(conn);

			// Identificamos todas aquellas aplicaciones que usan alguna configuracion personalizada
			final Set<String> appsWithParticularConfig = new HashSet<>();
			appsWithParticularConfig.addAll(sizes.keySet());
			appsWithParticularConfig.addAll(providers.keySet());

			// Construimos la configuracion particular para cada aplicacion que la tiene
			final Map<String, ApplicationOperationConfig> configs = new HashMap<>();
			appsWithParticularConfig.forEach(appId -> {
				final ApplicationOperationConfig size = sizes.get(appId);
				final ApplicationOperationConfig config = size != null ? size.clone() : defaultConfig.clone();
				final String providersConfig = providers.get(appId);
				if (providersConfig != null) {
					config.setProviders(providersConfig);
				}
				configs.put(appId, config);
			});

			return configs;
		}

		private static Map<String, ApplicationOperationConfig> getAppsParticularSizes(final Connection conn) throws SQLException {

			final Map<String, ApplicationOperationConfig> sizes = new HashMap<>();
			try (final PreparedStatement st = conn.prepareStatement(SQL_SELECT_APP_PROPERTIES);
					ResultSet rs = st.executeQuery()) {
				while (rs.next()) {

					final String appId = rs.getString(1);

					final ApplicationOperationConfig config = new ApplicationOperationConfig();
					config.setParamsMaxSize(rs.getInt(2));
					config.setRequestMaxSize(rs.getInt(3));
					config.setBatchMaxDocuments(rs.getInt(4));

					sizes.put(appId, config);
				}
			}

			return sizes;
		}

		private static Map<String, String> getAppsParticularProviders(final Connection conn) throws SQLException {

			final Map<String, String> providers = new HashMap<>();

			try (final PreparedStatement st = conn.prepareStatement(SQL_SELECT_APP_PROVIDERS);
					ResultSet rs = st.executeQuery()) {
				while (rs.next()) {

					final String appId = rs.getString(1);
					String provider = rs.getString(2);
					if (rs.getBoolean(3)) {
						provider = PROVIDER_MANDATORY_SIGN + provider;
					}

					String providerList = providers.get(appId);
					if (providerList == null) {
						providerList = provider;
					} else {
						providerList += PROVIDER_SEPARATOR_SIGN + provider;
					}
					providers.put(appId, providerList);
				}
			}

			return providers;
		}
	}
}
