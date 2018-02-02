package es.gob.fire.server.admin.entity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;

import es.gob.fire.server.admin.tool.Base64;

public class CertificateFire {

	private String id_certificado;
	private String nombre_cert;
	private Date fec_alta;
	private String cert_principal;
	private String cert_backup;
	private String huella_principal;
	private String huella_backup;
	private X509Certificate certX509_principal;
	private X509Certificate certX509_backup;
	
	/**
	 * Constructor vacío
	 */
	public CertificateFire() {}
	
	
	//GETTER Y SETTER
	/**
	 * Recupera el id del Certificado.
	 * @return
	 */
	public final String getId_certificado() {
		return id_certificado;
	}
	
	
	/**
	 * Establece el id del Certificado.
	 * @param id_certificado
	 */
	public final void setId_certificado(String id_certificado) {
		this.id_certificado = id_certificado;
	}	

	/**
	 * Recupera el nombre del certificado
	 * @return
	 */
	public final String getNombre_cert() {
		return nombre_cert;
	}
	/**
	 * Establece el nombre del certificado
	 * @param nombre_cert
	 */
	public final void setNombre_cert(String nombre_cert) {
		this.nombre_cert = nombre_cert;
	}
	/**
	 * Recupera la fecha de alta en base de datos
	 * @return
	 */
	public final Date getFec_alta() {
		return fec_alta;
	}
	/**
	 * Establece la fecha de alta del certificado en el sistema
	 * @param fec_alta
	 */
	public final void setFec_alta(Date fec_alta) {
		this.fec_alta = fec_alta;
	}
	/**
	 * Recupera los datos en base 64 del certificado principal 
	 * @return
	 */
	public final String getCert_principal() {
		return cert_principal;
	}
	/**
	 * Establece los datos en base 64 del certificado principal
	 * @param cert_principal
	 */
	public final void setCert_principal(String cert_principal) {
		this.cert_principal = cert_principal;
	}
	/**
	 * Recupera los datos en base 64 del certificado backup
	 * @return
	 */
	public final String getCert_backup() {
		return cert_backup;
	}
	/**
	 * Establece los datos en base 64 del certificado backup
	 * @param cert_backup
	 */
	public final void setCert_backup(String cert_backup) {
		this.cert_backup = cert_backup;
	}
	/**
	 * Recupera la huella del certificado principal
	 * @return
	 */
	public final String getHuella_principal() {
		return huella_principal;
	}
	/**
	 *Establece  la huella del certificado principal
	 * @param huella_principal
	 */
	public final void setHuella_principal(String huella_principal) {
		this.huella_principal = huella_principal;
	}
	/**
	 * Recupera  la huella del certificado backup
	 * @return
	 */
	public final String getHuella_backup() {
		return huella_backup;
	}
	/**
	 * Establece  la huella del certificado backup
	 * @param huella_backup
	 */
	public final void setHuella_backup(String huella_backup) {
		this.huella_backup = huella_backup;
	}
	
	/**
	 * Recupera objeto certificado principal
	 * @return
	 */
	public final X509Certificate getCertX509_principal() {
		return certX509_principal;
	}

	/**
	 *  Establece objeto certificado principal
	 * @param certX509
	 */
	public final void setCertX509_principal(X509Certificate certX509_principal) {
		this.certX509_principal = certX509_principal;
	}

	/**
	 * Recupera objeto certificado backup
	 * @return
	 */
	public final X509Certificate getCertX509_backup() {
		return certX509_backup;
	}

	/**
	 * Establece objeto certificado backup
	 * @param certX509_backup
	 */
	public final void setCertX509_backup(X509Certificate certX509_backup) {
		this.certX509_backup = certX509_backup;
	}

	
	/**
	 * Establece el Certificado X509 mediante una cadena en base 64
	 * @param cert
	 * @throws CertificateException
	 * @throws IOException
	 */
	public void setCertPrincipalb64ToX509(String cert) throws CertificateException, IOException {
		this.setCertX509_principal ((X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(Base64.decode(cert))));
	}
	/**
	 * Establece el Certificado X509 mediante una cadena en base 64
	 * @param cert
	 * @throws CertificateException
	 * @throws IOException
	 */
	public void setCertBkupb64ToX509(String cert) throws CertificateException, IOException {
		this.setCertX509_backup((X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(Base64.decode(cert))));
	}


	
	
	
	
	
}
