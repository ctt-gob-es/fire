package es.gob.fire.signature;

/**
 * Elemento del listado de proveedores que puede configurarse para su uso.
 */
public class ProviderElement {

	/** Sufijo usado en los nombres de proveedor para indicar que estos son indispensables. */
	private static final String SUFIX_IMPORTANT = "@"; //$NON-NLS-1$

	/** Nombre del proveedor. */
	private final String name;

	/** Indica si el proveedor es indispensable y debe darse a elegir siempre al usuario. */
	private final boolean indispensable;

	/**
	 * Construye el elemento proveedor, extrayendo de su nombre si es indispensable o no.
	 * @param name Nombre del proveedor. Si el nombre del pro
	 */
	public ProviderElement(final String name) {

		if (name == null || name.trim().isEmpty()) {
			throw new NullPointerException("No se puede establecer un proveedor sin nombre"); //$NON-NLS-1$
		}

		String provName = name.trim();
		if (provName.startsWith(SUFIX_IMPORTANT)) {
			while (provName.startsWith(SUFIX_IMPORTANT)) {
				provName = provName.substring(1).trim();
			}
			if (provName.isEmpty()) {
				throw new NullPointerException("No se puede establecer un proveedor sin nombre"); //$NON-NLS-1$
			}
			this.name = provName;
			this.indispensable = true;
		}
		else {
			this.name = provName;
			this.indispensable = false;
		}
	}

	/**
	 * Construye el elemento proveedor, indic&acute;ndo directamente si es indispensable o no.
	 * @param name Nombre del proveedor.
	 * @param mandatory Indica si es indispensable que el proveedor se muestre al usuario.
	 */
	public ProviderElement(final String name, final boolean mandatory) {

		if (name == null || name.trim().isEmpty()) {
			throw new NullPointerException("No se puede establecer un proveedor sin nombre"); //$NON-NLS-1$
		}

		this.name = name.trim();
		this.indispensable = mandatory;
	}

	/**
	 * Obtiene el nombre del proveedor.
	 * @return Nombre del proveedor.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Obtiene si el proveedor debe mostrarse siempre o no.
	 * @return {@code true} si el proveedor debe mostrarse siempre,
	 * {@code false} en caso contrario.
	 */
	public boolean isIndispensable() {
		return this.indispensable;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj != null) {
			if (obj instanceof ProviderElement) {
				return this.name.equalsIgnoreCase(((ProviderElement) obj).getName());
			}
			return this.name.equalsIgnoreCase(obj.toString());
		}
		return false;
	}

	@Override
	public String toString() {
		return this.name;
	}

	@Override
	public int hashCode() {
		return this.name.hashCode();
	}
}
