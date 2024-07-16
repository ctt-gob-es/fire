package es.gob.fire.test.webapp;

class SignatureInfo {

	private byte[] signature;
	private String mimetype;
	private String filename;

	public void setSignature(final byte[] signature) {
		this.signature = signature;
	}

	public byte[] getSignature() {
		return this.signature;
	}

	public void setMimetype(final String mimetype) {
		this.mimetype = mimetype;
	}

	public String getMimetype() {
		return this.mimetype;
	}

	public void setFilename(final String filename) {
		this.filename = filename;
	}

	public String getFilename() {
		return this.filename;
	}


	public void setFileInfo(final String name, final String format) {

		// Definimos la extension para la descarga de la firma segun el formato
		String ext = null;
		String type = null;
		if (format != null) {
			switch (format) {
			case "CAdES": //$NON-NLS-1$
				ext = ".csig"; //$NON-NLS-1$
				type = "application/octet-stream"; //$NON-NLS-1$
				break;
			case "XAdES": //$NON-NLS-1$
				ext = ".xsig"; //$NON-NLS-1$
				type = "text/xml"; //$NON-NLS-1$
				break;
			case "PAdES": //$NON-NLS-1$
				ext = ".pdf"; //$NON-NLS-1$
				type = "application/pdf"; //$NON-NLS-1$
				break;
			case "FacturaE": //$NON-NLS-1$
				ext = ".xml"; //$NON-NLS-1$
				type = "text/xml"; //$NON-NLS-1$
				break;
			case "CAdES-ASiC-S": //$NON-NLS-1$
			case "XAdES-ASiC-S": //$NON-NLS-1$
				ext = ".asics"; //$NON-NLS-1$
				type = "application/x-zip-compressed"; //$NON-NLS-1$
				break;
			default:
				ext = ""; //$NON-NLS-1$
			}
		}

		setFilename(name  + ext);
		setMimetype(type);
	}
}
