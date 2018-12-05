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
	private Format format;
	private Algorithm algorithm;
	private Provider proveedor;
	private Browser navegador;
	private boolean resultSign = false ;
	private String id_transaccion;
	private Long size = new Long(0L);
	private ImprovedFormat improvedFormat;
	private Long total = 0L;
	private static String OTRO = "OTRO"; //$NON-NLS-1$
	private static SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //$NON-NLS-1$

	public SignatureCube() {
		super();
	}



	public SignatureCube(final Date fecha ,final Format format, final Algorithm algorithm, final Provider proveedor,
			final Browser navegador, final boolean resultsign, final String id_tr, final ImprovedFormat improvedFormat) {
		super();
		this.fecha = fecha;
		this.format = format;
		this.algorithm = algorithm;
		this.proveedor = proveedor;
		this.navegador = navegador;
		this.resultSign = resultsign;
		this.id_transaccion= id_tr;
		this.improvedFormat= improvedFormat;
	}






	public final Date getFecha() {
		return this.fecha;
	}

	public final void setFecha(final Date fecha) {
		this.fecha = fecha;
	}

	public final Format getFormat() {
		return this.format;
	}

	public final void setFormat(final Format format) {
		this.format = format;
	}

	public final Algorithm getAlgorithm() {
		return this.algorithm;
	}

	public final void setAlgorithm(final Algorithm algorithm) {
		this.algorithm = algorithm;
	}

	public final Provider getProveedor() {
		return this.proveedor;
	}

	public final void setProveedor(final Provider proveedor) {
		this.proveedor = proveedor;
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


	public final ImprovedFormat getImprovedFormat() {
		return this.improvedFormat;
	}

	public final void setImprovedFormat(final ImprovedFormat improvedFormat) {
		this.improvedFormat = improvedFormat;
	}

	public final Long getTotal() {
		return this.total;
	}

	public final void setTotal(final Long total) {
		this.total = total;
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
					final Format f = new Format();
					f.setNombre(cube[1]);
					sign.setFormat(f);
				}
				else {
					return null;
				}
				//Formato Mejorado
				if(!cube[2].isEmpty()) {
					final ImprovedFormat impFormat = new ImprovedFormat();
					impFormat.setNombre(cube[2]);
					sign.setImprovedFormat(impFormat);
				}

				//Algoritmo
				if(!cube[3].isEmpty()) {
					final Algorithm alg = new Algorithm();
					alg.setNombre(cube[3]);
					sign.setAlgorithm(alg);
				}
				else {
					return null;
				}
				//Proveedor
				if(!cube[4].isEmpty()) {
					final Provider provider  = ProvidersDAO.getProviderByName(cube[4]);
					if(provider != null && provider.getIdProveedor() != 0) {
						sign.setProveedor(provider);
					}
					else {
						final Provider p =  new Provider();
						p.setNombre(OTRO);
						sign.setProveedor(p);
					}
				}
				else {
					return null;
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
	 * con el formato "IdFormato;IdAlgoritmo;IdProveedor;idNavegador-Version;resultSign"
	 * siendo resultSign 0 si no se realizo la firma o hubo un error y 1 si termino la firma correctamente.
	 */
	@Override
	public String toString() {

		String result = new String();

		result  = result.concat(this.getFormat() != null && !this.getFormat().getNombre().isEmpty() ? this.getFormat().getNombre() : OTRO ).concat(";");//$NON-NLS-1$
		result  = result.concat(this.getImprovedFormat() != null  && !this.getImprovedFormat().getNombre().isEmpty()  ? this.getImprovedFormat().getNombre() : "" ).concat(";");//$NON-NLS-1$ //$NON-NLS-2$
		result  = result.concat(this.getAlgorithm() != null && !this.getAlgorithm().getNombre().isEmpty() ? this.getAlgorithm().getNombre() : OTRO ).concat(";");//$NON-NLS-1$
		result  = result.concat(this.getProveedor() != null && !this.getProveedor().getNombre().isEmpty() ?  this.getProveedor().getNombre() :  OTRO ).concat(";");//$NON-NLS-1$
		if(getNavegador() != null) {
			result  = result.concat(getNavegador().getName().isEmpty() ? getNavegador().getName() : OTRO).concat(";");//$NON-NLS-1$
		}
		else {
			result  = result.concat(OTRO).concat(";"); //$NON-NLS-1$
		}
		result  = result.concat(this.isResultSign() ? "1":"0" ).concat(";");  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		result  = result.concat(this.getId_transaccion() != null ? this.getId_transaccion() :"0" ).concat(";");  //$NON-NLS-1$ //$NON-NLS-2$
		result  = result.concat(this.getSize().longValue() != 0L ? String.valueOf(this.getSize()) :"0" );  //$NON-NLS-1$
		return result;
	}


}
