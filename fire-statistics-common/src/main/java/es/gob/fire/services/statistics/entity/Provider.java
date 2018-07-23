package es.gob.fire.services.statistics.entity;

public class Provider {

	private int idProveedor;
	private String nombre;
	private String conector;




	public final int getIdProveedor() {
		return this.idProveedor;
	}
	public final void setIdProveedor(final int idProveedor) {
		this.idProveedor = idProveedor;
	}
	public final String getNombre() {
		return this.nombre;
	}
	public final void setNombre(final String nombre) {
		this.nombre = nombre;
	}
	public final String getConector() {
		return this.conector;
	}
	public final void setConector(final String conector) {
		this.conector = conector;
	}




}
