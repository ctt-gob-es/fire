package es.gob.fire.server.services.statistics;

public class SignatureCube {


	private String signFormat;
	private String signAlgorithm;
	private String proveedor;
	private Browser navegador;
	private boolean resultSign = false ;

	public SignatureCube() {
		super();
	}



	public SignatureCube(final String signFormat, final String signAlgorithm, final String proveedor,
			final Browser navegador, final boolean resultsign) {
		super();

		this.signFormat = signFormat;
		this.signAlgorithm = signAlgorithm;
		this.proveedor = proveedor;
		this.navegador = navegador;
		this.resultSign = resultsign;
	}






	protected final String getSignFormat() {
		return this.signFormat;
	}

	protected final void setSignFormat(final String signFormat) {
		this.signFormat = signFormat;
	}

	protected final String getSignAlgorithm() {
		return this.signAlgorithm;
	}

	protected final void setSignAlgorithm(final String signAlgorithm) {
		this.signAlgorithm = signAlgorithm;
	}

	protected final String getProveedor() {
		return this.proveedor;
	}

	protected final void setProveedor(final String proveedor) {
		this.proveedor = proveedor;
	}

	protected final Browser getNavegador() {
		return this.navegador;
	}

	protected final void setNavegador(final Browser navegador) {
		this.navegador = navegador;
	}

	protected final boolean isResultSign() {
		return this.resultSign;
	}

	protected final void setResultSign(final boolean resultSign) {
		this.resultSign = resultSign;
	}

	/**
	 * Devuelve un string con las propiedades del objeto
	 * con el formato "IdFormato;IdAlgoritmo;IdProveedor;idNavegador-Version;resultSign"
	 * siendo resultSign 0 si no se realizo la firma o hubo un error y 1 si termino la firma correctamente.
	 */
	@Override
	public String toString() {

		String result = new String();

		result  = result.concat(this.getSignFormat() != null ? this.getSignFormat() : "-" ).concat(";");//$NON-NLS-1$ //$NON-NLS-2$
		result  = result.concat(this.getSignAlgorithm() != null ? this.getSignAlgorithm() : "-" ).concat(";");//$NON-NLS-1$ //$NON-NLS-2$
		result  = result.concat(this.getProveedor() != null ?  this.getProveedor() : "-" ).concat(";");//$NON-NLS-1$ //$NON-NLS-2$
		if(getNavegador() != null) {
			result  = result.concat(getNavegador().getId() != null ? getNavegador().getId() : "-").concat("/")//$NON-NLS-1$ //$NON-NLS-2$
					.concat(getNavegador().getVersion() != null ? getNavegador().getVersion() :"-" ).concat(";");//$NON-NLS-1$ //$NON-NLS-2$
		}
		else {
			result  = result.concat("-/-;"); //$NON-NLS-1$
		}
		result  = result.concat(this.isResultSign() ? "1":"0" );  //$NON-NLS-1$//$NON-NLS-2$

		return result;
	}

}
