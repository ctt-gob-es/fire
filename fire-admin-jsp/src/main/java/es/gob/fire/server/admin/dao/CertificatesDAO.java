/* Copyright (C) 2018 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 22/01/2018
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.admin.dao;

import java.io.StringWriter;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;

import es.gob.fire.server.admin.conf.DbManager;
import es.gob.fire.server.admin.entity.CertificateFire;
import es.gob.fire.server.admin.service.LogUtils;
import es.gob.fire.server.admin.tool.Utils;

/**
 * DAO para la gesti&oacute;n de certificados dados de alta en el sistema.
 *
 */
public class CertificatesDAO {

	private static final Logger LOGGER = Logger.getLogger(CertificatesDAO.class.getName());

	private static final String ST_SELECT_CERTIFICATES_BYID = "SELECT id_certificado, nombre_cert, fec_alta, cert_principal,cert_backup,huella_principal,huella_backup FROM tb_certificados WHERE id_certificado=? ORDER BY nombre_cert"; //$NON-NLS-1$

	private static final String ST_SELECT_CERTIFICATES_ALL = "SELECT id_certificado, nombre_cert, fec_alta, cert_principal,cert_backup,huella_principal,huella_backup FROM tb_certificados ORDER BY id_certificado"; //$NON-NLS-1$

	private static final String ST_SELECT_CERTIFICATES_PAG = "SELECT id_certificado, nombre_cert, fec_alta, cert_principal,cert_backup,huella_principal,huella_backup FROM tb_certificados ORDER BY nombre_cert limit ?,?"; //$NON-NLS-1$

	private static final String ST_SELECT_CERTIFICATES_COUNT = "SELECT count(*) FROM tb_certificados"; //$NON-NLS-1$

	private static final String STATEMENT_INSERT_CERTIFICATE = "INSERT INTO tb_certificados(nombre_cert, fec_alta, cert_principal,cert_backup,huella_principal,huella_backup) VALUES (?, ?, ?, ?, ?, ?)"; //$NON-NLS-1$

	private static final String STATEMENT_REMOVE_CERTIFICATE = "DELETE FROM tb_certificados WHERE id_certificado = ?"; //$NON-NLS-1$

	private static final String STATEMENT_UPDATE_CERTIFICATE = "UPDATE tb_certificados SET nombre_cert=?, cert_principal = ?, cert_backup = ?, huella_principal = ?, huella_backup = ? WHERE id_certificado = ?";//$NON-NLS-1$

	private static final String STATEMENT_INFO_APPLICATION_CERTIFICATE  = "SELECT tb_cert.id_certificado, tb_cert.nombre_cert, tb_cert.fec_alta, tb_cert.cert_principal,tb_cert.cert_backup, " //$NON-NLS-1$
			+ "tb_cert.huella_principal,tb_cert.huella_backup, tb_usu.nombre, tb_usu.apellidos " //$NON-NLS-1$
			+ "FROM tb_certificados AS tb_cert, tb_aplicaciones AS tb_app, tb_usuarios AS tb_usu " //$NON-NLS-1$
			+ "WHERE tb_app.fk_responsable = tb_usu.id_usuario AND tb_app.fk_certificado = tb_cert.id_certificado  ORDER BY id_certificado"; //$NON-NLS-1$
	/**
	 * Obtiene un JSON con el numero de certificados dados de alta en el sistema.
	 * @return Estructura JSON.
	 */
	public static String getCertificatesCount() {
		int count = 0;
		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
		try {
			final PreparedStatement st = DbManager.prepareStatement(ST_SELECT_CERTIFICATES_COUNT);
			final ResultSet rs = st.executeQuery();
			if(rs.next()) {
				count = rs.getInt(1);
			}
			rs.close();
			st.close();
		}
		catch(final Exception e){
			LOGGER.log(Level.SEVERE, "No se pudo obtener el numero de certificados de la base de datos", e); //$NON-NLS-1$
		}

		jsonObj.add("count", count);  //$NON-NLS-1$

		final StringWriter writer = new StringWriter();
		try  {
			final JsonWriter jw = Json.createWriter(writer);
	        jw.writeObject(jsonObj.build());
	        jw.close();
	    }
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Error al componer la estructura JSON con el numero de certificados", e); //$NON-NLS-1$
		}

	    return writer.toString();

	}

	/**
	 * Consulta que obtiene todos los registros de la tabla tb_certificados
	 * @return JSON con el listado de certificados.
	 */
	public static String getCertificatesJSON()  {

		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
		final JsonArrayBuilder data = Json.createArrayBuilder();

		try {
			final PreparedStatement st = DbManager.prepareStatement(ST_SELECT_CERTIFICATES_ALL);

			final ResultSet rs = st.executeQuery();
			while (rs.next()) {

				Date date = null;
				final Timestamp timestamp = rs.getTimestamp(3);
				if (timestamp != null) {
					date = new Date(timestamp.getTime());
				}

				final CertificateFire cert = new CertificateFire();
				cert.setId(rs.getString(1));
				cert.setNombre(rs.getString(2));
				cert.setFechaAlta(rs.getDate(3));
				if (rs.getString(4) != null && !"".equals(rs.getString(4))) { //$NON-NLS-1$
					cert.setCertPrincipalb64ToX509(rs.getString(4));
					final String certPrincipal[] = cert.getX509Principal().getSubjectX500Principal().getName().split(","); //$NON-NLS-1$
					java.util.Date expDatePrincipal = new java.util.Date();
					expDatePrincipal=cert.getX509Principal().getNotAfter();
					cert.setCertPrincipal(certPrincipal[0] + "<br>Fecha de Caducidad=" + Utils.getStringDateFormat(expDatePrincipal)); //$NON-NLS-1$
				}
				if (rs.getString(5)!=null && !"".equals(rs.getString(5))) { //$NON-NLS-1$
					cert.setCertBkupb64ToX509(rs.getString(5));
					final String certBkup[] = cert.getX509Backup().getSubjectX500Principal().getName().split(",");//$NON-NLS-1$
					java.util.Date expDateBkup= new java.util.Date();
					expDateBkup = cert.getX509Backup().getNotAfter();
					cert.setCertBackup(certBkup[0] + "<br>Fecha de Caducidad=" + Utils.getStringDateFormat(expDateBkup)); //$NON-NLS-1$
				}
				if (rs.getString(6) != null && !"".equals(rs.getString(6))) { //$NON-NLS-1$
					cert.setHuellaPrincipal(rs.getString(6));
				}

				if (rs.getString(7) != null && !"".equals(rs.getString(7))) { //$NON-NLS-1$
					cert.setHuellaBackup(rs.getString(7));
				}

				data.add(Json.createObjectBuilder()
						.add("id_certificado",cert.getId()) //$NON-NLS-1$
						.add("nombre_cert", cert.getNombre()) //$NON-NLS-1$
						.add("fec_alta", date != null ? Utils.getStringDateFormat(date) : "") //$NON-NLS-1$ //$NON-NLS-2$
						.add("cert_principal", cert.getCertPrincipal() != null ? cert.getCertPrincipal() : "") //$NON-NLS-1$ //$NON-NLS-2$
						.add("cert_backup", cert.getCertBackup() != null ? cert.getCertBackup() : "") //$NON-NLS-1$ //$NON-NLS-2$
						.add("huella_principal", cert.getHuellaPrincipal() != null ? cert.getHuellaPrincipal() : "") //$NON-NLS-1$ //$NON-NLS-2$
						.add("huella_backup", cert.getHuellaBackup() != null ? cert.getHuellaBackup() : "") //$NON-NLS-1$ //$NON-NLS-2$
						);
			}
			rs.close();
			st.close();
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "No se pudo obtener el listado paginado de usuarios de la base de datos", e); //$NON-NLS-1$
		}

		jsonObj.add("CertList", data); //$NON-NLS-1$

		final StringWriter writer = new StringWriter();
		try  {
			final JsonWriter jw = Json.createWriter(writer);
	        jw.writeObject(jsonObj.build());
			jw.close();
	    }
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Error al componer la estructura JSON con el listado de certificados", e); //$NON-NLS-1$
		}

	    return writer.toString();
	}


	/**
	 * Consulta que obtiene todos los registros de la tabla tb_certificados paginados
	 * @param start Elemento por el cual empezar a la p&aacute;gina.
	 * @param total N&uacute;mero de elementos de la p&aacute;gina.
	 * @return Estructura JSON.
	 */
	public static String getCertificatesPag(final String start, final String total) {

		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
		final JsonArrayBuilder data = Json.createArrayBuilder();

		try {
			final PreparedStatement st = DbManager.prepareStatement(ST_SELECT_CERTIFICATES_PAG);
			st.setInt(1,Integer.parseInt(start));
			st.setInt(2, Integer.parseInt(total));
			final ResultSet rs = st.executeQuery();
			while (rs.next()) {
				Date date = null;
				final Timestamp timestamp = rs.getTimestamp(3);
				if (timestamp != null) {
					date = new Date(timestamp.getTime());
				}

				final CertificateFire cert = new CertificateFire();
				cert.setId(rs.getString(1));
				cert.setNombre(rs.getString(2));
				cert.setFechaAlta(rs.getDate(3));
				if (rs.getString(4) != null && !"".equals(rs.getString(4))) { //$NON-NLS-1$
					cert.setCertPrincipalb64ToX509(rs.getString(4));
					final String certPrincipal[] = cert.getX509Principal().getSubjectX500Principal().getName().split(","); //$NON-NLS-1$
					java.util.Date expDatePrincipal = new java.util.Date();
					expDatePrincipal = cert.getX509Principal().getNotAfter();
					cert.setCertPrincipal(certPrincipal[0] + "<br>Fecha de Caducidad=" + Utils.getStringDateFormat(expDatePrincipal)); //$NON-NLS-1$
				}
				if (rs.getString(5) != null && !"".equals(rs.getString(5))) { //$NON-NLS-1$
					cert.setCertBkupb64ToX509(rs.getString(5));
					final String certBkup[] = cert.getX509Backup().getSubjectX500Principal().getName().split(","); //$NON-NLS-1$
					java.util.Date expDateBkup = new java.util.Date();
					expDateBkup = cert.getX509Backup().getNotAfter();
					cert.setCertBackup(certBkup[0] + "<br>Fecha de Caducidad=" + Utils.getStringDateFormat(expDateBkup)); //$NON-NLS-1$
				}
				if (rs.getString(6) != null && !"".equals(rs.getString(6))) { //$NON-NLS-1$
					cert.setHuellaPrincipal(rs.getString(6));
				}

				if (rs.getString(7) != null && !"".equals(rs.getString(7))) { //$NON-NLS-1$
					cert.setHuellaBackup(rs.getString(7));
				}
				data.add(Json.createObjectBuilder()
						.add("id_certificado",cert.getId()) //$NON-NLS-1$
						.add("nombre_cert", cert.getNombre()) //$NON-NLS-1$
						.add("fec_alta", date != null ? Utils.getStringDateFormat(date) : "") //$NON-NLS-1$ //$NON-NLS-2$
						.add("cert_principal", cert.getCertPrincipal() != null ? cert.getCertPrincipal() : "") //$NON-NLS-1$ //$NON-NLS-2$
						.add("cert_backup", cert.getCertBackup() != null ? cert.getCertBackup() : "") //$NON-NLS-1$ //$NON-NLS-2$
						.add("huella_principal", cert.getHuellaPrincipal() !=null ? cert.getHuellaPrincipal() : "") //$NON-NLS-1$ //$NON-NLS-2$
						.add("huella_backup", cert.getHuellaBackup() !=null ? cert.getHuellaBackup() : "") //$NON-NLS-1$ //$NON-NLS-2$
						);
			}
			rs.close();
			st.close();
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "No se pudo obtener el listado paginado de certificados de la base de datos", e); //$NON-NLS-1$
		}

		jsonObj.add("CertList", data); //$NON-NLS-1$

		final StringWriter writer= new StringWriter();
		try  {
			final JsonWriter jw = Json.createWriter(writer);
	        jw.writeObject(jsonObj.build());
			jw.close();
	    }
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Error al componer la estructura JSON con el listado paginado de certificados", e); //$NON-NLS-1$
		}

	    return writer.toString();
	}

	/**
	 * Devuelve una aplicaci&oacute;n registrada en el sistema dado su id.
	 * @param id de la aplicaci&oacute;n a encontrar.
	 * @return aplicaci&oacute;n encontrada.
	 */
	public static CertificateFire selectCertificateByID(final String id) {
		CertificateFire cert = null;
		try {
			final PreparedStatement st = DbManager.prepareStatement(ST_SELECT_CERTIFICATES_BYID);
			st.setString(1, id);
			final ResultSet rs = st.executeQuery();
			if (rs.next()) {
				cert = new CertificateFire();
				cert.setId(rs.getString(1));
				cert.setNombre(rs.getString(2));
				cert.setFechaAlta(rs.getDate(3));
				if (rs.getString(4) != null && !"".equals(rs.getString(4))) { //$NON-NLS-1$
					cert.setCertPrincipal(rs.getString(4));
					cert.setCertPrincipalb64ToX509(rs.getString(4));
				}
				if (rs.getString(5) != null && !"".equals(rs.getString(5))) { //$NON-NLS-1$
					cert.setCertBackup(rs.getString(5));
					cert.setCertBkupb64ToX509(rs.getString(5));
				}
				if (rs.getString(6) != null && !"".equals(rs.getString(6))) { //$NON-NLS-1$
					cert.setHuellaPrincipal(rs.getString(6));
				}

				if (rs.getString(7) != null && !"".equals(rs.getString(7))) { //$NON-NLS-1$
					cert.setHuellaBackup(rs.getString(7));
				}
			}
			rs.close();
			st.close();
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "No se pudo leer la tabla con el listado de certificados", e); //$NON-NLS-1$
			cert = null;
		}
		return cert;

	}

	/**
	 * Selecciona todos los certificados ordenados por id.
	 * @return Listado de certificados.
	 */
	public static List<CertificateFire> selectCertificateAll() {
		final List<CertificateFire> lCert = new ArrayList<>();

		try {
			final PreparedStatement st = DbManager.prepareStatement(ST_SELECT_CERTIFICATES_ALL);

			final ResultSet rs = st.executeQuery();
			while (rs.next()) {
				final CertificateFire cert = new CertificateFire();
				cert.setId(rs.getString(1));
				cert.setNombre(rs.getString(2));
				cert.setFechaAlta(rs.getDate(3));
				if(rs.getString(4) != null && !"".equals(rs.getString(4))) { //$NON-NLS-1$
					cert.setCertPrincipal(rs.getString(4));
					cert.setCertPrincipalb64ToX509(rs.getString(4));
				}
				if(rs.getString(5) != null && !"".equals(rs.getString(5))) { //$NON-NLS-1$
					cert.setCertBackup(rs.getString(5));
					cert.setCertBkupb64ToX509(rs.getString(5));
				}
				if(rs.getString(6) != null && !"".equals(rs.getString(6))) { //$NON-NLS-1$
					cert.setHuellaPrincipal(rs.getString(6));
				}
				if(rs.getString(7) != null && !"".equals(rs.getString(7))) { //$NON-NLS-1$
					cert.setHuellaBackup(rs.getString(7));
				}
				lCert.add(cert);
			}
			rs.close();
			st.close();
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "No se pudo leer toda la tabla con el listado de certificados", e); //$NON-NLS-1$
		}
		return lCert;

	}


	/**
	 * Elimina un certificado de la base de datos.
	 * @param id Identificador del certificado.
	 * @throws SQLException Cuando ocurre un error durante la operaci&oacute;n.
	 */
	public static void removeCertificate(final String id) throws SQLException {

		final PreparedStatement st = DbManager.prepareStatement(STATEMENT_REMOVE_CERTIFICATE);
		st.setString(1, id);

		LOGGER.info("Damos de baja el certificado con el ID: " + LogUtils.cleanText(id)); //$NON-NLS-1$

		st.execute();
		st.close();
	}

	/**
	 * Actualizamos un certificado existente.
	 * @param id Identificador del certificado.
	 * @param nombre Nombre del certificado.
	 * @param certPrincipal Certificado principal.
	 * @param huellaPrincipal Huella del certificado principal.
	 * @param certBackup Certificado de respaldo.
	 * @param huellaBackup Huella del certificado de respaldo.
	 * @throws SQLException si hay un problema en la conexi&oacute;n con la base de datos
	 */
	public static void updateCertificate (final String id,final String nombre, final String certPrincipal,
			final String huellaPrincipal,final String certBackup, final String huellaBackup) throws SQLException{

		final PreparedStatement st = DbManager.prepareStatement(STATEMENT_UPDATE_CERTIFICATE);
		st.setString(1, nombre);
		st.setString(2, certPrincipal);
		st.setString(3, certBackup);
		st.setString(4, huellaPrincipal);
	    st.setString(5, huellaBackup);
	    st.setInt(6, Integer.parseInt(id));

		LOGGER.info("Actualizamos el certificado '" + nombre + "' con el ID: " + id); //$NON-NLS-1$ //$NON-NLS-2$

		st.execute();
		st.close();
	}

	/**
	 * Agrega un nuevo certificado al sistema.
	 * @param nombre Nombre del certificado.
	 * @param certPrincipal Certificado principal.
	 * @param huellaPrincipal Huella del certificado principal.
	 * @param certBackup Certificado de respaldo.
	 * @param huellaBackup Huella del certificado de respaldo.
	 * @throws SQLException Cuando no se puede insertar la nueva aplicacion en base de datos.
	 */
	public static void createCertificate(final String nombre, final String certPrincipal,
										final String huellaPrincipal, final String certBackup,
										final String huellaBackup) throws SQLException {

		final PreparedStatement st = DbManager.prepareStatement(STATEMENT_INSERT_CERTIFICATE);

		st.setString(1, nombre);
		st.setDate(2, new Date(new java.util.Date().getTime()));
		st.setString(3, certPrincipal);
		st.setString(4, certBackup);
		st.setString(5, huellaPrincipal);
	    st.setString(6, huellaBackup);

		LOGGER.info("Damos de alta la aplicacion '" + nombre +"'" ); //$NON-NLS-1$ //$NON-NLS-2$

		st.execute();
		st.close();
	}

	public static String getCertificateApplicationJSON() throws SQLException {

		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
		final JsonArrayBuilder data = Json.createArrayBuilder();

		try {
			final PreparedStatement st = DbManager.prepareStatement(STATEMENT_INFO_APPLICATION_CERTIFICATE);
			final ResultSet rs = st.executeQuery();
			while (rs.next()) {
				Date date= null;
				final Timestamp timestamp = rs.getTimestamp(5);
				if (timestamp != null) {
					date = new Date(timestamp.getTime());

				}

				data.add(Json.createObjectBuilder()

						.add("id", rs.getString(1)) //$NON-NLS-1$
						.add("nombre", rs.getString(2)) //$NON-NLS-1$
						.add("nombre_responsable", rs.getString(3) + " " + rs.getString(4)) //$NON-NLS-1$ //$NON-NLS-2$
						.add("alta", es.gob.fire.server.admin.tool.Utils.getStringDateFormat(date !=null ? date : rs.getDate(5))) //$NON-NLS-1$
						.add("fk_certificado", rs.getString(6)) //$NON-NLS-1$
						);
			}
			rs.close();
			st.close();
		}
		catch(final Exception e){
			LOGGER.log(Level.SEVERE, "Error al leer los registros en la tabla de aplicaciones", e); //$NON-NLS-1$
		}

		jsonObj.add("AppList", data); //$NON-NLS-1$

		final StringWriter writer = new StringWriter();
		try  {
			final JsonWriter jw = Json.createWriter(writer);
	        jw.writeObject(jsonObj.build());
	        jw.close();
	    }
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Error al componente la estructura JSON con el listado de aplicaciones", e); //$NON-NLS-1$
		}

	    return writer.toString();
	}
}
