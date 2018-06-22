package es.gob.fire.server.services.statistics;

public class TransactionCube {

	private Integer idAplicacion;
	private Integer idOperacion;
	private String  proveedor;
	private  boolean ProveedorForzado = false;
	private  boolean resultTransaction = false;

	/**
	 * Constructor
	 */
	public TransactionCube() {
		super();
	}



	/**
	 * Constructor
	 * @param fecha
	 * @param idAplicacion
	 * @param idOperacion
	 * @param idProveedor
	 * @param proveedorForzado
	 */
	public TransactionCube(final Integer idAplicacion, final Integer idOperacion, final String proveedor,
			final boolean proveedorForzado, final boolean resultTransact) {
		super();
		this.idAplicacion = idAplicacion;
		this.idOperacion = idOperacion;
		this.proveedor = proveedor;
		this.ProveedorForzado = proveedorForzado;
		this.resultTransaction = resultTransact;
	}



	/* Propiedades */

	protected final Integer getIdAplicacion() {
		return this.idAplicacion;
	}
	protected final void setIdAplicacion(final Integer idAplicacion) {
		this.idAplicacion = idAplicacion;
	}
	protected final Integer getIdOperacion() {
		return this.idOperacion;
	}
	protected final void setIdOperacion(final Integer idOperacion) {
		this.idOperacion = idOperacion;
	}
	protected final String getProveedor() {
		return this.proveedor;
	}
	protected final void setProveedor(final String proveedor) {
		this.proveedor = proveedor;
	}
	protected final boolean isProveedorForzado() {
		return this.ProveedorForzado;
	}
	protected final void setProveedorForzado(final boolean proveedorForzado) {
		this.ProveedorForzado = proveedorForzado;
	}

	protected final boolean isResultTransaction() {
		return this.resultTransaction;
	}

	protected final void setResultTransaction(final boolean resultTransaction) {
		this.resultTransaction = resultTransaction;
	}



	/**
	 * Devuelve un string con las propiedades del objeto
	 * con el formato "IdAplicacion;IdOperacion;IdProveedor;proveedorForzado;resultSign"
	 * proveedorForzado => 0 si es false, 1 si es true.
	 * resultSign => 0 si es false, 1 si es true.
	 */
	@Override
	public final String toString(){
		String result = new String();

		if(this.getIdAplicacion() != null) {
			result  = result.concat(String.valueOf(this.getIdAplicacion())).concat(";");//$NON-NLS-1$
		}
		if(this.getIdOperacion() != null) {
			result  = result.concat(String.valueOf(this.getIdOperacion())).concat(";");//$NON-NLS-1$
		}
		if(this.getProveedor() != null) {
			result  = result.concat(this.getProveedor()).concat(";");//$NON-NLS-1$
		}
		result  = result.concat(this.isProveedorForzado() ? "1":"0" ).concat(";");  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		result  = result.concat(this.isResultTransaction() ? "1":"0" );  //$NON-NLS-1$//$NON-NLS-2$
		return result;
	}
}
