package es.gob.fire.server.admin.entity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;

import es.gob.fire.server.admin.tool.Base64;

/**
 * Certificado para la autenticaci&oacute;n de aplicaciones en FIRe. Este certificado
 * es un objeto que comprende hasta 2 certificados reales que pueden utilizarse para
 * la autenticaci&oacute;n indistintamente.
 */
public class CertificateFire {

	private String id;
	private String nombre;
	private Date fechaAlta;
	private String certPrincipal;
	private String certBackup;
	private String huellaPrincipal;
	private String huellaBackup;
	private X509Certificate x509Principal;
	private X509Certificate x509Backup;

	/**
	 * Recupera el identificador del certificado.
	 * @return Identificador del certificado.
	 */
	public final String getId() {
		return this.id;
	}


	/**
	 * Establece el identificador del certificado.
	 * @param id Identificador del certificado.
	 */
	public final void setId(final String id) {
		this.id = id;
	}

	/**
	 * Recupera el nombre del certificado.
	 * @return Nombre del certificado.
	 */
	public final String getNombre() {
		return this.nombre;
	}
	/**
	 * Establece el nombre del certificado.
	 * @param nombre Nombre del certificado.
	 */
	public final void setNombre(final String nombre) {
		this.nombre = nombre;
	}
	/**
	 * Recupera la fecha de alta del certificado.
	 * @return Fecha de alta del certificado.
	 */
	public final Date getFechaAlta() {
		return this.fechaAlta;
	}
	/**
	 * Establece la fecha de alta del certificado en el sistema
	 * @param fechaAlta Fecha de alta del certificado.
	 */
	public final void setFechaAlta(final Date fechaAlta) {
		this.fechaAlta = fechaAlta;
	}
	/**
	 * Recupera los datos en base 64 del certificado principal
	 * @return Certificado en base 64.
	 */
	public final String getCertPrincipal() {
		return this.certPrincipal;
	}
	/**
	 * Establece los datos en base 64 del certificado principal.
	 * @param certificado Certificado en base 64.
	 */
	public final void setCertPrincipal(final String certificado) {
		this.certPrincipal = certificado;
	}
	/**
	 * Recupera los datos en base 64 del certificado secundario.
	 * @return Certificado en base 64.
	 */
	public final String getCertBackup() {
		return this.certBackup;
	}
	/**
	 * Establece los datos en base 64 del certificado secundario.
	 * @param certificado Certificado en base 64.
	 */
	public final void setCertBackup(final String certificado) {
		this.certBackup = certificado;
	}
	/**
	 * Recupera la huella del certificado principal
	 * @return Huella del certificado.
	 */
	public final String getHuellaPrincipal() {
		return this.huellaPrincipal;
	}
	/**
	 *Establece  la huella del certificado principal
	 * @param hash Huella del certificado.
	 */
	public final void setHuellaPrincipal(final String hash) {
		this.huellaPrincipal = hash;
	}
	/**
	 * Recupera  la huella del certificado backup
	 * @return Huella del certificado.
	 */
	public final String getHuellaBackup() {
		return this.huellaBackup;
	}
	/**
	 * Establece  la huella del certificado backup
	 * @param hash Huella del certificado.
	 */
	public final void setHuellaBackup(final String hash) {
		this.huellaBackup = hash;
	}

	/**
	 * Recupera objeto certificado principal
	 * @return Certificado.
	 */
	public final X509Certificate getX509Principal() {
		return this.x509Principal;
	}

	/**
	 *  Establece objeto certificado principal
	 * @param certificado Certificado.
	 */
	public final void setX509Principal(final X509Certificate certificado) {
		this.x509Principal = certificado;
	}

	/**
	 * Recupera objeto certificado backup
	 * @return Certificado.
	 */
	public final X509Certificate getX509Backup() {
		return this.x509Backup;
	}

	/**
	 * Establece objeto certificado backup
	 * @param certificado Certificado.
	 */
	public final void setX509Backup(final X509Certificate certificado) {
		this.x509Backup = certificado;
	}

	/**
	 * Establece el Certificado X509 mediante una cadena en base 64
	 * @param cert
	 * @throws CertificateException
	 * @throws IOException
	 */
	public void setCertPrincipalb64ToX509(final String cert) throws CertificateException, IOException {
		setX509Principal ((X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(Base64.decode(cert)))); //$NON-NLS-1$
	}
	/**
	 * Establece el Certificado X509 mediante una cadena en base 64
	 * @param cert
	 * @throws CertificateException
	 * @throws IOException
	 */
	public void setCertBkupb64ToX509(final String cert) throws CertificateException, IOException {
		setX509Backup((X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(Base64.decode(cert)))); //$NON-NLS-1$
	}
}
