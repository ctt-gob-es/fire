package es.gob.fire.services.statistics.entity;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import es.gob.fire.services.statistics.Browser;
import es.gob.fire.services.statistics.config.DBConnectionException;


public class SignatureCube {
	//static Logger LOGGER =  FireSignLogger.getFireSignLogger().getFireLogger().getLogger();
	private Date fecha;
	private String aplicacion;
	private String format;
	private String algorithm;
	private String proveedor;
	private Browser navegador;
	private boolean resultSign = false ;
	private String id_transaccion;
	private Long size = new Long(0L);
	private String improvedFormat;
	private Long total = 1L;
	private static String OTRO = "OTRO"; //$NON-NLS-1$
	private static SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //$NON-NLS-1$

	public SignatureCube() {
		super();
	}



	public SignatureCube(final Date fecha ,final String format, final String algorithm, final String proveedor,
			final Browser navegador, final boolean resultsign, final String id_tr, final String improvedFormat, final String aplicacion) {
		super();
		this.fecha = fecha;
		this.format = format;
		this.algorithm = algorithm;
		this.proveedor = proveedor;
		this.navegador = navegador;
		this.resultSign = resultsign;
		this.id_transaccion = id_tr;
		this.improvedFormat = improvedFormat;
		this.aplicacion = aplicacion;
	}





	/**
	 * Obtiene un objeto SignatureCube de la lectura de un registro con formato, del log FIReSIGNATURE
	 * @param registry
	 * @return
	 * @throws ParseException
	 * @throws NumberFormatException
	 * @throws SQLException
	 * @throws DBConnectionException
	 */
	public final static SignatureCube parse(final String registry) throws ParseException, NumberFormatException, SQLException, DBConnectionException {
		SignatureCube sign = null;
		if(registry != null && !"".equals(registry) && registry.contains(";")) { //$NON-NLS-1$ //$NON-NLS-2$
			final String [] cube = registry.split(";"); //$NON-NLS-1$
			if(cube != null && cube.length > 0) {
				sign =  new SignatureCube();
				//Fecha
				if(!cube[0].isEmpty()) {
					sign.setFecha(formater.parse(cube[0]));
				}
				else {
					return null;
				}
				//Formato
				if(!cube[1].isEmpty()) {
					sign.setFormat(cube[1]);
				}
				else {
					 sign.setFormat(OTRO);
				}
				//Formato Mejorado
				if(!cube[2].isEmpty()) {
					sign.setImprovedFormat(cube[2]);
				}

				//Algoritmo
				if(!cube[3].isEmpty()) {
					sign.setAlgorithm(cube[3]);
				}
				else {
					sign.setAlgorithm(OTRO);
				}
				//Proveedor
				if(!cube[4].isEmpty()) {
					sign.setProveedor(cube[4]);
				}
				else {
					sign.setProveedor(OTRO);
				}
				//Navegador
				if(!cube[5].isEmpty()) {
					final Browser b = new Browser();
					b.setName(cube[5]);
					sign.setNavegador(b);
				}
				else {
					return null;
				}
				//Resultado de la firma
				if(!cube[6].isEmpty()) {
					if(cube[6].equals("1")) { //$NON-NLS-1$
						sign.setResultSign(true);
					}
					else {
						sign.setResultSign(false);
					}
				}
				else {
					return null;
				}
				//Id Tr
				if(!cube[7].isEmpty()) {
					sign.setId_transaccion(cube[7]);
				}
				else {
					return null;
				}
				//Tama&ntilde;o de la firma
				if(!cube[8].isEmpty()) {
					sign.setSize(new Long(Long.parseLong(cube[8])));
				}
				else {
					sign.setSize(new Long(0L));
				}
			}
		}
		return sign;
	}

	/**
	 * Devuelve un string con las propiedades del objeto
	 * con el formato "Formato;Algoritmo;Proveedor;Navegador;resultSign"
	 * siendo resultSign 0 si no se realizo la firma o hubo un error y 1 si termino la firma correctamente.
	 */
	@Override
	public String toString() {

		String result = new String();

		result  = result.concat(this.getFormat() != null && !this.getFormat().isEmpty() ? this.getFormat() : OTRO ).concat(";");//$NON-NLS-1$
		result  = result.concat(this.getImprovedFormat() != null  && !this.getImprovedFormat().isEmpty()  ? this.getImprovedFormat() : "" ).concat(";");//$NON-NLS-1$ //$NON-NLS-2$
		result  = result.concat(this.getAlgorithm() != null && !this.getAlgorithm().isEmpty() ? this.getAlgorithm() : OTRO ).concat(";");//$NON-NLS-1$
		result  = result.concat(this.getProveedor() != null && !this.getProveedor().isEmpty() ?  this.getProveedor() :  OTRO ).concat(";");//$NON-NLS-1$
		if(getNavegador() != null) {
			result  = result.concat(! getNavegador().getName().isEmpty() ? getNavegador().getName() : OTRO).concat(";");//$NON-NLS-1$
		}
		else {
			result  = result.concat(OTRO).concat(";"); //$NON-NLS-1$
		}
		result  = result.concat(this.isResultSign() ? "1":"0" ).concat(";");  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		result  = result.concat(this.getId_transaccion() != null ? this.getId_transaccion() :"0" ).concat(";");  //$NON-NLS-1$ //$NON-NLS-2$
		result  = result.concat(this.getSize().longValue() != 0L ? String.valueOf(this.getSize()) :"0" );  //$NON-NLS-1$
		return result;
	}



	/**Getter & Setter*/

	/**
	 * Obtiene la fecha
	 * @return
	 */
	public final Date getFecha() {
		return this.fecha;
	}
	/**
	 * Establece la fecha
	 * @param fecha
	 */
	public final void setFecha(final Date fecha) {
		this.fecha = fecha;
	}
	/**
	 * Obtiene el formtato del cubo de la firma
	 * @return
	 */
	public final String getFormat() {
		return this.format;
	}
	/**
	 * Establece el formtato del cubo de la firma
	 * @param format
	 */
	public final void setFormat(final String format) {
		this.format = format;
	}
	/**
	 * Obtiene el algoritmo del cubo de la firma
	 * @return
	 */
	public final String getAlgorithm() {
		return this.algorithm;
	}
	/**
	 *Establece el algoritmo del cubo de la firma
	 * @param algorithm
	 */
	public final void setAlgorithm(final String algorithm) {
		this.algorithm = algorithm;
	}
	/**
	 * Obtiene el proveedor del cubo de la firma
	 * @return
	 */
	public final String getProveedor() {
		return this.proveedor;
	}
	/**
	 * Establece el proveedor del cubo de la firma
	 * @param proveedor
	 */
	public final void setProveedor(final String proveedor) {
		this.proveedor = proveedor;
	}
	/**
	 * Obtiene el Navegador  del cubo de la firma
	 * @return
	 */
	public final Browser getNavegador() {
		return this.navegador;
	}
	/**
	 *  Establece el Navegador  del cubo de la firma
	 * @param navegador
	 */
	public final void setNavegador(final Browser navegador) {
		this.navegador = navegador;
	}
	/**
	 *
	 * @return
	 */
	public final boolean isResultSign() {
		return this.resultSign;
	}
	/**
	 *
	 * @param resultSign
	 */
	public final void setResultSign(final boolean resultSign) {
		this.resultSign = resultSign;
	}

	public String getId_transaccion() {
		return this.id_transaccion;
	}

	public void setId_transaccion(final String id_transaccion) {
		this.id_transaccion = id_transaccion;
	}


	public final Long getSize() {
		return this.size;
	}

	public final void setSize(final Long size) {
		this.size = size;
	}


	public final String getImprovedFormat() {
		return this.improvedFormat;
	}

	public final void setImprovedFormat(final String improvedFormat) {
		this.improvedFormat = improvedFormat;
	}

	public final Long getTotal() {
		return this.total;
	}

	public final void setTotal(final Long total) {
		this.total = total;
	}

	public final String getAplicacion() {
		return this.aplicacion;
	}
	public final void setAplicacion(final String aplicacion) {
		this.aplicacion = aplicacion;
	}


}
