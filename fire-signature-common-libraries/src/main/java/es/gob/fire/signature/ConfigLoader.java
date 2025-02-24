package es.gob.fire.signature;

import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

/**
 * Clase para el acceso a las propiedades de configuraci&oacute;n.
 */
public abstract class ConfigLoader {

	private Hashtable<Object, Object> config;

	/**
	 * Metodo para cargar la configur&aacute;cion.
	 * @throws IOException Cuando no es posible cargar la configuraci&oacute;n.
	 * @throws ConfigException Cuando no se ha encontrado algun dato imprescindible
	 * de la configuraci&oacute;n.
	 */
	protected abstract void loadConfig() throws IOException, ConfigException;

	/**
	 * Establece la configuraci&oacute;n actual.
	 * @param newConfig Propiedades de configuraci&oacute;n.
	 */
	protected final void setConfig(final Hashtable<Object, Object> newConfig) {
		// Reseteamos la configuracion
		if (this.config == null) {
			this.config = new Hashtable<>();
		} else {
			this.config.clear();
		}
		// Asignamos una copia de las propiedades indicadas
		if (newConfig != null) {
			this.config.putAll(newConfig);
		}
	}

	/**
	 * Obtiene la cadena de texto asociada a una propiedad.
	 * @param key Clave del valor que desea obtener.
	 * @return Valor textual de la propiedad o {@code null} si no se
	 * encontr&oacute;.
	 */
	public String get(final String key) {
		// Cargamos la configuracion
		try {
			loadConfig();
		} catch (final IOException | ConfigException e) {
			if (this.config == null) {
				return null;
			}
		}

		return (String) this.config.get(key);
	}

	/**
	 * Obtiene la cadena de texto asociada a una propiedad o, si no
	 * est&aacute; definida o era cadena vac&iacute;a, un valor por defecto.
	 * @param key Clave del valor que desea obtener.
	 * @param defaultValue Valor por defecto.
	 * @return Valor textual de la propiedad o el valor por defecto.
	 */
	public String get(final String key, final String defaultValue) {
		// Cargamos la configuracion
		try {
			loadConfig();
		} catch (final IOException | ConfigException e) {
			if (this.config == null) {
				return defaultValue;
			}
		}

		return (String) this.config.getOrDefault(key, defaultValue);
	}

	/**
	 * Obtiene el objeto asociado a una propiedad de la configuraci&pacute;n.
	 * @param key Clave de la propiedad de la que se desea obtener el valor.
	 * @return Valor de la propiedad o {@code null} si no se
	 * encontr&oacute;.
	 */
	public Object getObject(final String key) {
		// Cargamos la configuracion
		try {
			loadConfig();
		} catch (final IOException | ConfigException e) {
			if (this.config == null) {
				return null;
			}
		}

		return this.config.get(key);
	}

	/**
	 * Obtiene el entero asociado a una propiedad o, si no estaba definida o
	 * era un valor no v&aacute;lido, un valor por defecto.
	 * @param key Clave del valor que desea obtener.
	 * @param defaultValue Valor por defecto.
	 * @return Entero asociado a la propiedad o el valor por defecto.
	 */
	public int getInt(final String key, final int defaultValue) {
		// Cargamos la configuracion
		try {
			loadConfig();
		} catch (final IOException | ConfigException e) {
			if (this.config == null) {
				return defaultValue;
			}
		}

		final String value = (String) this.config.get(key);
		if (value == null || value.isEmpty()) {
			return defaultValue;
		}

		int valueInt;
		try {
			valueInt = Integer.parseInt(value);
		} catch (final Exception e) {
			valueInt = defaultValue;
		}
		return valueInt;
	}

	/**
	 * Obtiene el valor asociado a una propiedad, que puede ser {@code true} o
	 * {@code false}. Si no se encuentra la propiedad o tiene asociado otro
	 * valor, se devolvera {@code false}.
	 * @param key Clave del valor que desea obtener.
	 * @return Valor buleano asociado a la propiedad o {@code false} si no
	 * contenia un valor v&aacute;lido.
	 */
	public boolean getBool(final String key) {
		// Cargamos la configuracion
		try {
			loadConfig();
		} catch (final IOException | ConfigException e) {
			if (this.config == null) {
				return false;
			}
		}

		final String value = (String) this.config.get(key);
		return Boolean.parseBoolean(value);
	}

	/**
	 * Obtiene el valor asociado a una propiedad, que puede ser {@code true} o
	 * {@code false}. Si no se encuentra la propiedad o tiene asociado otro
	 * valor, se devolvera el valor por defecto.
	 * @param key Clave del valor que desea obtener.
	 * @param defaultValue Valor por defecto.
	 * @return Valor buleano asociado a la propiedad o el valor por defecto.
	 */
	public boolean getBool(final String key, final boolean defaultValue) {
		// Cargamos la configuracion
		try {
			loadConfig();
		} catch (final IOException | ConfigException e) {
			if (this.config == null) {
				return defaultValue;
			}
		}

		final String value = (String) this.config.get(key);
		if (value == null || value.isEmpty()) {
			return defaultValue;
		}

		if (Boolean.TRUE.toString().equals(value)) {
			return true;
		} else if (Boolean.FALSE.toString().equals(value)) {
			return false;
		}

		return defaultValue;
	}

	/**
	 * Indica si una propiedad est&aacute; definida y contiene un valor.
	 * @param key Clave de la propiedad que se desea comprobar.
	 * @return {@code true} si la propiedad est&aacute; definida y contiene un
	 * valor, {@code false} en caso contrario.
	 */
	public boolean contains(final String key) {
		// Cargamos la configuracion
		try {
			loadConfig();
		} catch (final IOException | ConfigException e) {
			if (this.config == null) {
				return false;
			}
		}

		final String value = (String) this.config.get(key);
		return value != null && !value.isEmpty();
	}

	/**
	 * Obtiene el conjunto de propiedades de la configuraci&oacute;n
	 * @return Configuraci&oacute;n.
	 */
	public Hashtable<Object, Object> getConfig() {
		// Cargamos la configuracion
		try {
			loadConfig();
		} catch (final IOException | ConfigException e) {
			if (this.config == null) {
				return null;
			}
		}

		// Devolvemos una copia
		final Hashtable<Object, Object> copy = new Hashtable<>();
		copy.putAll(this.config);

		return copy;
	}

	/**
	 * Obtiene el conjunto de nombre de las propiedades configuradas.
	 * @return Conjunto de nombres.
	 */
	public Set<String> keys() {
		// Cargamos la configuracion
		try {
			loadConfig();
		} catch (final IOException | ConfigException e) {
			if (this.config == null) {
				return null;
			}
		}

		final Set<String> keys = new HashSet<>();
		this.config.keySet().forEach(t -> keys.add(t.toString()));

		return keys;
	}
}
