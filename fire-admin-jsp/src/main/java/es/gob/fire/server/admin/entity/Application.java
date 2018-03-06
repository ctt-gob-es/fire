/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.admin.entity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;

import es.gob.fire.server.admin.tool.Base64;

/**
 * Aplicaciones dadas de alta en la base de datos.
 */
public class Application {

	private String id;

	private String nombre;

	private String responsable;

	private String correo;

	private String telefono;

	private Date alta;

//	private String cer;
//
//	private String huella;
	
	private String fk_certificado;
	
//	private X509Certificate certX509;

	/**
	 * Constructor vac&iacute;o que iniciliza los String a cadena vac&iacute;a.
	 */
	public Application(){
		this.id = ""; //$NON-NLS-1$
		this.nombre = ""; //$NON-NLS-1$
		this.responsable = ""; //$NON-NLS-1$
		this.correo = ""; //$NON-NLS-1$
		this.telefono = "";  //$NON-NLS-1$
//		this.cer = ""; //$NON-NLS-1$
//		this.huella = ""; //$NON-NLS-1$
	}

	/**
	 * Recupera el ID de la aplicaci&oacute;n.
	 * @return ID de la aplicaci&oacute;n.
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * Establece el identificador de la aplicaci&oacute;n.
	 * @param id Identificador de la aplicaci&oacute;n.
	 */
	public void setId(final String id) {
		this.id = id;
	}


	/**
	 * Recupera el nombre de la aplicaci&oacute;n.
	 * @return nombre de la aplicaci&oacute;n.
	 */
	public String getNombre() {
		return this.nombre;
	}


	/**
	 * Establece el nombre de la aplicaci&oacute;n.
	 * @param nombre Nombre de la aplicaci&oacute;n.
	 */
	public void setNombre(final String nombre) {
		this.nombre = nombre;
	}


	/**
	 * Recupera el nombre del responsable de la aplicaci&oacute;n.
	 * @return Nombre del responsable de la aplicaci&oacute;n.
	 */
	public String getResponsable() {
		return this.responsable;
	}


	/**
	 * Establece el nombre del responsable de la aplicaci&oacute;n.
	 * @param responsable Nombre del responsable de la aplicaci&oacute;n.
	 */
	public void setResponsable(final String responsable) {
		this.responsable = responsable;
	}


	/**
	 * Recupera el correo del responsable de la aplicaci&oacute;n.
	 * @return correo del responsable de la aplicaci&oacute;n.
	 */
	public String getCorreo() {
		return this.correo;
	}


	/**
	 * Establece el correo del responsable de la aplicaci&oacute;n.
	 * @param correo Correo del responsable de la aplicaci&oacute;n.
	 */
	public void setCorreo(final String correo) {
		this.correo = correo;
	}


	/**
	 * Recupera el tel&eacute;fono del responsable de la aplicaci&oacute;n.
	 * @return Tel&eacute;fono del responsable de la aplicaci&oacute;n.
	 */
	public String getTelefono() {
		return this.telefono;
	}


	/**
	 * Establece el tel&eacute;fono del responsable de la aplicaci&oacute;n.
	 * @param telefono Tel&eacute;fono del responsable de la aplicaci&oacute;n.
	 */
	public void setTelefono(final String telefono) {
		this.telefono = telefono;
	}

	/**
	 * Recupera la fecha de alta de la aplicaci&oacute;n.
	 * @return Fecha de alta de la aplicaci&oacute;n.
	 */
	public Date getAlta() {
		return this.alta;
	}


	/**
	 * Establece la fecha de alta de la aplicaci&oacute;n.
	 * @param alta Fecha de alta de la aplicaci&oacute;n.
	 */
	public void setAlta(final Date alta) {
		this.alta = alta;
	}

	
	
	
	/**
	 * Obtiene la clave foranea del certificado asociado 
	 * @return
	 */
	public final String getFk_certificado() {
		return fk_certificado;
	}
	/**
	 * Establece la clave foranea del certificado
	 * @param fk_certificado
	 */
	public final void setFk_certificado(String fk_certificado) {
		this.fk_certificado = fk_certificado;
	}

	/**
	 * Recupera el certificado de la aplicaci&oacute;n.
	 * @return el certificado.
	 */
//	public String getCer() {
//		return this.cer;
//	}

	/**
	 * Establece el certificado de la aplicaci&oacute;n.
	 * @param cer certificado de la aplicaci&oacute;n.
	 */
//	public void setCer(final String cer) {
//		this.cer = cer;
//	}

	/**
	 * Recupera la huella del certificado de la aplicaci&oacute;n.
	 * @return huella del certificado.
	 */
//	public String getHuella() {
//		return this.huella;
//	}

	/**
	 * Establece la huella dado un certificado
	 * @param huella
	 */
//	public void setHuella(final String huella) {
//		this.huella = huella;
//	}

	/**
	 * Obtiene el certificado de la aplicaci&oacute;n.
	 * @return
	 */
//	public X509Certificate getCertX509() {
//		return certX509;
//	}
	/**
	 * Establece el certificado de la aplicaci&oacute;n.
	 * @param certX509
	 */
//	public void setCertX509(X509Certificate certX509) {
//		this.certX509 = certX509;
//	}
	/**
	 * Establece el Certificado X509 mediante una cadena en base 64
	 * @param cert
	 * @throws CertificateException
	 * @throws IOException
	 */
//	public void setCertb64ToX509(String cert) throws CertificateException, IOException {
//		this.setCertX509((X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(Base64.decode(cert))));
//	}
}
