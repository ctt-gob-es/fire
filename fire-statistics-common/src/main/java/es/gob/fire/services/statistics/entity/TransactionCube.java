package es.gob.fire.services.statistics.entity;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import es.gob.fire.services.statistics.config.DBConnectionException;
import es.gob.fire.services.statistics.dao.ProvidersDAO;

public class TransactionCube {

	private Date fecha;
	private String idAplicacion;
	private Integer idOperacion;
	private int  idProveedor;
	private static int OTRO = 99;
	private  boolean ProveedorForzado = false;
	private  boolean resultTransaction = false;
	private String id_transaccion;
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
	public TransactionCube(final Date fecha ,final String idAplicacion, final Integer idOperacion, final int idProveedor,
			final boolean proveedorForzado, final boolean resultTransact, final String id_tr) {
		super();
		this.fecha = fecha;
		this.idAplicacion = idAplicacion;
		this.idOperacion = idOperacion;
		this.idProveedor = idProveedor;
		this.ProveedorForzado = proveedorForzado;
		this.resultTransaction = resultTransact;
		this.id_transaccion = id_tr;
	}



	/* Propiedades */

	public final String getIdAplicacion() {
		return this.idAplicacion;
	}
	public final void setIdAplicacion(final String idAplicacion) {
		this.idAplicacion = idAplicacion;
	}
	public final Integer getIdOperacion() {
		return this.idOperacion;
	}
	public final void setIdOperacion(final Integer idOperacion) {
		this.idOperacion = idOperacion;
	}
	public final int getIdProveedor() {
		return this.idProveedor;
	}
	public final void setIdProveedor(final int idProveedor) {
		this.idProveedor = idProveedor;
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
					trans.setIdAplicacion(cube[1]);
				}
				else {
					return null;
				}
				//Operacion
				if(!cube[2].isEmpty()) {
					trans.setIdOperacion(Integer.valueOf(cube[2]));
				}
				else {
					return null;
				}
				//Proveedor
				if(!cube[3].isEmpty()) {
					final Provider provider  = ProvidersDAO.getProviderById(Integer.parseInt(cube[3]));
					if(provider != null && provider.getIdProveedor() != 0) {
						trans.setIdProveedor(provider.getIdProveedor());
					}
					else {
						trans.setIdProveedor(OTRO);
					}
				}
				else {
					return null;
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
	 * con el formato "IdAplicacion;IdOperacion;IdProveedor;proveedorForzado;resultSign"
	 * proveedorForzado => 0 si es false, 1 si es true.
	 * resultSign => 0 si es false, 1 si es true.
	 */
	@Override
	public final String toString(){
		String result = new String();

		if(this.getIdAplicacion() != null) {
			result  = result.concat(this.getIdAplicacion()).concat(";");//$NON-NLS-1$
		}
		if(this.getIdOperacion() != null) {
			result  = result.concat(String.valueOf(this.getIdOperacion())).concat(";");//$NON-NLS-1$
		}
		else {
			result  = result.concat(String.valueOf(OTRO)).concat(";");//$NON-NLS-1$
		}
		if(this.getIdProveedor() != 0) {
			result  = result.concat(String.valueOf(this.getIdProveedor())).concat(";");//$NON-NLS-1$
		}
		else {
			result  = result.concat(String.valueOf(OTRO)).concat(";");//$NON-NLS-1$
		}
		result  = result.concat(this.isProveedorForzado() ? "1":"0" ).concat(";");  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		result  = result.concat(this.isResultTransaction() ? "1":"0" ).concat(";");  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		result  = result.concat(this.getId_transaccion() != null ? this.getId_transaccion() : "0" );  //$NON-NLS-1$
		return result;
	}




}
