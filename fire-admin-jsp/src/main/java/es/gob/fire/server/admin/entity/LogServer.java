package es.gob.fire.server.admin.entity;

public class LogServer {

	private int id;
	private String nombre;
	private String url;
	private String clave;


	/**
	 * Obtiene el id &uacute;nico del servidor
	 * @return
	 */
	public final int getId() {
		return this.id;
	}

	/**
	 * Establece el id &uacute;nico del servidor
	 * @param id
	 */
	public final void setId(final int id) {
		this.id = id;
	}

	/**
	 * Obtiene el nombre del servidor
	 * @return
	 */
	public final String getNombre() {
		return this.nombre;
	}

	/**
	 * Establece el nombre del servidor
	 * @param nombre
	 */
	public final void setNombre(final String nombre) {
		this.nombre = nombre;
	}

	/**
	 * Obtiene la direcci&oacute;n del servicio de logs del servidor
	 * @return
	 */
	public final String getUrl() {
		return this.url;
	}

	/**
	 * Establece la direcci&oacute;n del servicio de logs del servidor
	 * @param url
	 */
	public final void setUrl(final String url) {
		this.url = url;
	}

	/**
	 * Obtiene la clave de conexi&oacute;n con el servidor
	 * @return
	 */
	public final String getClave() {
		return this.clave;
	}

	/**
	 * Establece la clave de conexi&oacute;n con el servidor
	 * @param clave
	 */
	public final void setClave(final String clave) {
		this.clave = clave;
	}




}
