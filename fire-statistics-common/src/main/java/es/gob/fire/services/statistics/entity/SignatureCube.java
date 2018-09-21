package es.gob.fire.services.statistics.entity;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import es.gob.fire.services.statistics.Browser;
import es.gob.fire.services.statistics.config.DBConnectionException;
import es.gob.fire.services.statistics.dao.ProvidersDAO;


public class SignatureCube {
	//static Logger LOGGER =  FireSignLogger.getFireSignLogger().getFireLogger().getLogger();
	private Date fecha;
	private int idFormat;
	private int idAlgorithm;
	private int idProveedor;
	private Browser navegador;
	private boolean resultSign = false ;
	private String id_transaccion;
	private Long size = new Long(0L);
	private int idImprovedFormat;
	private static int OTRO = 99;
	private static SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //$NON-NLS-1$

	public SignatureCube() {
		super();
	}



	public SignatureCube(final Date fecha ,final int idFormat, final int idAlgorithm, final int idProveedor,
			final Browser navegador, final boolean resultsign, final String id_tr, final int idImprovedFormat) {
		super();
		this.fecha = fecha;
		this.idFormat = idFormat;
		this.idAlgorithm = idAlgorithm;
		this.idProveedor = idProveedor;
		this.navegador = navegador;
		this.resultSign = resultsign;
		this.id_transaccion= id_tr;
		this.idImprovedFormat= idImprovedFormat;
	}






	public final Date getFecha() {
		return this.fecha;
	}

	public final void setFecha(final Date fecha) {
		this.fecha = fecha;
	}

	public final int getIdFormat() {
		return this.idFormat;
	}

	public final void setIdFormat(final int idFormat) {
		this.idFormat = idFormat;
	}

	public final int getIdAlgorithm() {
		return this.idAlgorithm;
	}

	public final void setIdAlgorithm(final int idAlgorithm) {
		this.idAlgorithm = idAlgorithm;
	}

	public final int getIdProveedor() {
		return this.idProveedor;
	}

	public final void setIdProveedor(final int idProveedor) {
		this.idProveedor = idProveedor;
	}

	public final Browser getNavegador() {
		return this.navegador;
	}

	public final void setNavegador(final Browser navegador) {
		this.navegador = navegador;
	}

	public final boolean isResultSign() {
		return this.resultSign;
	}

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


	public final int getIdImprovedFormat() {
		return this.idImprovedFormat;
	}

	public final void setIdImprovedFormat(final int idImprovedFormat) {
		this.idImprovedFormat = idImprovedFormat;
	}



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
					sign.setIdFormat(Integer.parseInt(cube[1]));
				}
				else {
					return null;
				}
				//Formato Mejorado
				if(!cube[2].isEmpty()) {
					sign.setIdImprovedFormat(Integer.parseInt(cube[2]));
				}

				//Algoritmo
				if(!cube[3].isEmpty()) {
					sign.setIdAlgorithm(Integer.parseInt(cube[3]));
				}
				else {
					return null;
				}
				//Proveedor
				if(!cube[4].isEmpty()) {
					final Provider provider  = ProvidersDAO.getProviderById(Integer.parseInt(cube[4]));
					if(provider != null && provider.getIdProveedor() != 0) {
						sign.setIdProveedor(provider.getIdProveedor());
					}
					else {
						sign.setIdProveedor(OTRO);
					}
				}
				else {
					return null;
				}
				//Navegador
				if(!cube[5].isEmpty()) {
					final String[] nav = cube[5].split("/"); //$NON-NLS-1$
					final Browser brow = new Browser(Integer.parseInt(nav[0]) , nav[1]);
					sign.setNavegador(brow);
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
				//Tamaño de la firma
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
	 * con el formato "IdFormato;IdAlgoritmo;IdProveedor;idNavegador-Version;resultSign"
	 * siendo resultSign 0 si no se realizo la firma o hubo un error y 1 si termino la firma correctamente.
	 */
	@Override
	public String toString() {

		String result = new String();

		result  = result.concat(this.getIdFormat() != 0 ? String.valueOf(this.getIdFormat()) : String.valueOf(OTRO) ).concat(";");//$NON-NLS-1$
		result  = result.concat(this.getIdImprovedFormat() != 0 ? String.valueOf(this.getIdImprovedFormat()) : "" ).concat(";");//$NON-NLS-1$ //$NON-NLS-2$
		result  = result.concat(this.getIdAlgorithm() != 0 ? String.valueOf(this.getIdAlgorithm()) :  String.valueOf(OTRO) ).concat(";");//$NON-NLS-1$
		result  = result.concat(this.getIdProveedor() != 0 ?  String.valueOf(this.getIdProveedor()) :  String.valueOf(OTRO) ).concat(";");//$NON-NLS-1$
		if(getNavegador() != null) {
			result  = result.concat(getNavegador().getId() != null ? getNavegador().getId() :  String.valueOf(OTRO)).concat("/")//$NON-NLS-1$
					.concat(getNavegador().getVersion() != null ? getNavegador().getVersion() :"-" ).concat(";");//$NON-NLS-1$ //$NON-NLS-2$
		}
		else {
			result  = result.concat(String.valueOf(OTRO)).concat("/-;"); //$NON-NLS-1$
		}
		result  = result.concat(this.isResultSign() ? "1":"0" ).concat(";");  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		result  = result.concat(this.getId_transaccion() != null ? this.getId_transaccion() :"0" ).concat(";");  //$NON-NLS-1$ //$NON-NLS-2$
		result  = result.concat(this.getSize().longValue() != 0L ? String.valueOf(this.getSize()) :"0" );  //$NON-NLS-1$
		return result;
	}


}
