package es.gob.fire.services.statistics.entity;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import es.gob.fire.services.statistics.config.DBConnectionException;

public class TransactionCube {

	private Date fecha;
	private String aplicacion;
	private String operacion;
	private String  proveedor;
	private static String OTRO = "Otro"; //$NON-NLS-1$
	private  boolean ProveedorForzado = false;
	private  boolean resultTransaction = false;
	private String id_transaccion;
	private Long total = 0L;
	private static SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //$NON-NLS-1$
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
	public TransactionCube(final Date fecha ,final String aplicacion, final String operacion, final String proveedor,
			final boolean proveedorForzado, final boolean resultTransact, final String id_tr) {
		super();
		this.fecha = fecha;
		this.aplicacion = aplicacion;
		this.operacion = operacion;
		this.proveedor = proveedor;
		this.ProveedorForzado = proveedorForzado;
		this.resultTransaction = resultTransact;
		this.id_transaccion = id_tr;
	}





	/**
	 * Obtiene un objeto TransactionCube de la lectura de un registro con formato, del log FIReTRANSACTION
	 * @param registry
	 * @return
	 * @throws ParseException
	 * @throws SQLException
	 * @throws NumberFormatException
	 * @throws DBConnectionException
	 */
	public final static TransactionCube parse(final String registry) throws ParseException, SQLException, NumberFormatException, DBConnectionException {
		TransactionCube trans = null;
		if(registry != null && !"".equals(registry) && registry.contains(";")) { //$NON-NLS-1$ //$NON-NLS-2$
			final String [] cube = registry.split(";"); //$NON-NLS-1$
			if(cube != null && cube.length > 0) {
				trans =  new TransactionCube();
				//Fecha
				if(!cube[0].isEmpty()) {
					trans.setFecha(formater.parse(cube[0]));
				}
				else {
					return null;
				}
				//Aplicacion
				if(!cube[1].isEmpty()) {
					trans.setAplicacion(cube[1]);
				}
				else {
					return null;
				}
				//Operacion
				if(!cube[2].isEmpty()) {
					trans.setOperacion(cube[2]);
				}
				else {
					return null;
				}
				//Proveedor
				if(!cube[3].isEmpty()) {
					trans.setProveedor(cube[3]);
				}
				else {
					trans.setProveedor(OTRO);
				}
				//Proveedor forzado
				if(!cube[4].isEmpty()) {
					if(cube[4].equals("1")) { //$NON-NLS-1$
						trans.setProveedorForzado(true);
					}
					else {
						trans.setProveedorForzado(false);
					}
				}
				else {
					return null;
				}
				//Resultado de la transaccion
				if(!cube[5].isEmpty()) {
					if(cube[5].equals("1")) { //$NON-NLS-1$
						trans.setResultTransaction (true);
					}
					else {
						trans.setResultTransaction(false);
					}
				}
				else {
					return null;
				}
				//Id Tr
				if(!cube[6].isEmpty()) {
					trans.setId_transaccion(cube[6]);
				}
				else {
					return null;
				}
			}
		}
		return trans;
	}

	/**
	 * Devuelve un string con las propiedades del objeto
	 * con el formato "Aplicacion;Operacion;Proveedor;proveedorForzado;resultSign"
	 * proveedorForzado => 0 si es false, 1 si es true.
	 * resultSign => 0 si es false, 1 si es true.
	 */
	@Override
	public final String toString(){
		String result = new String();

		if(this.getAplicacion() != null) {
			result  = result.concat(this.getAplicacion()).concat(";");//$NON-NLS-1$
		}
		if(this.getOperacion() != null) {
			result  = result.concat(String.valueOf(this.getOperacion())).concat(";");//$NON-NLS-1$
		}
		else {
			result  = result.concat(OTRO).concat(";");//$NON-NLS-1$
		}
		if(this.getProveedor() != null  && !this.getProveedor().isEmpty()) {
			result  = result.concat(this.getProveedor()).concat(";");//$NON-NLS-1$
		}
		else {
			result  = result.concat(OTRO).concat(";");//$NON-NLS-1$
		}
		result  = result.concat(this.isProveedorForzado() ? "1":"0" ).concat(";");  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		result  = result.concat(this.isResultTransaction() ? "1":"0" ).concat(";");  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		result  = result.concat(this.getId_transaccion() != null ? this.getId_transaccion() : "0" );  //$NON-NLS-1$
		return result;
	}

	/* Propiedades  Getter & Setter*/

	public final String getAplicacion() {
		return this.aplicacion;
	}
	public final void setAplicacion(final String aplicacion) {
		this.aplicacion = aplicacion;
	}
	public final String getOperacion() {
		return this.operacion;
	}
	public final void setOperacion(final String operacion) {
		this.operacion = operacion;
	}
	public final String getProveedor() {
		return this.proveedor;
	}
	public final void setProveedor(final String proveedor) {
		this.proveedor = proveedor;
	}
	public final boolean isProveedorForzado() {
		return this.ProveedorForzado;
	}
	public final void setProveedorForzado(final boolean proveedorForzado) {
		this.ProveedorForzado = proveedorForzado;
	}

	public final boolean isResultTransaction() {
		return this.resultTransaction;
	}

	public final void setResultTransaction(final boolean resultTransaction) {
		this.resultTransaction = resultTransaction;
	}

	public final Date getFecha() {
		return this.fecha;
	}

	public final void setFecha(final Date fecha) {
		this.fecha = fecha;
	}

	public String getId_transaccion() {
		return this.id_transaccion;
	}

	public void setId_transaccion(final String id_transaccion) {
		this.id_transaccion = id_transaccion;
	}

	public final Long getTotal() {
		return this.total;
	}

	public final void setTotal(final Long total) {
		this.total = total;
	}

}
