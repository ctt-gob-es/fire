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

import java.io.IOException;
import java.io.StringWriter;
import java.security.cert.CertificateException;
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

	public static String getCertificatesCount() {
		int count=0;

		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
		try {
			final PreparedStatement st = DbManager.prepareStatement(ST_SELECT_CERTIFICATES_COUNT);
			final ResultSet rs = st.executeQuery();
			if(rs.next()) {
				count = rs.getInt(1);
			}
			rs.close();
			st.close();
			jsonObj.add("count", count);  //$NON-NLS-1$
		}
		catch(final Exception e){
			e.printStackTrace();
		}

		final StringWriter writer = new StringWriter();
		try  {
			final JsonWriter jw = Json.createWriter(writer);
	        jw.writeObject(jsonObj.build());
	        jw.close();
	    }
		catch (final Exception e) {
			LOGGER.log(Level.WARNING, "Error al leer los registros en la tabla de aplicaciones", e); //$NON-NLS-1$
		}

	    return writer.toString();

	}

	/**
	 * Consulta que obtiene todos los registros de la tabla tb_certificados
	 * @return Devuelve un String con formato JSON
	 * @throws SQLException
	 * @throws CertificateException
	 * @throws IOException
	 */
	public static String getCertificatesJSON() throws SQLException, CertificateException, IOException {

		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
		final JsonArrayBuilder data = Json.createArrayBuilder();

		final PreparedStatement st = DbManager.prepareStatement(ST_SELECT_CERTIFICATES_ALL);

		final ResultSet rs = st.executeQuery();
		while (rs.next()) {

			Date date = null;
			final Timestamp timestamp = rs.getTimestamp(3);
			if (timestamp != null) {
				date = new Date(timestamp.getTime());
			}


			final CertificateFire cert = new CertificateFire();
			cert.setId_certificado(rs.getString(1));
			cert.setNombre_cert(rs.getString(2));
			cert.setFec_alta(rs.getDate(3));
			if(rs.getString(4) != null && !"".equals(rs.getString(4))) { //$NON-NLS-1$
				cert.setCertPrincipalb64ToX509(rs.getString(4));
				final String certPrincipal[] = cert.getCertX509_principal().getSubjectX500Principal().getName().split(","); //$NON-NLS-1$
				java.util.Date expDatePrincipal = new java.util.Date();
				expDatePrincipal=cert.getCertX509_principal().getNotAfter();
				cert.setCert_principal(certPrincipal[0].concat("<br>Fecha de Caducidad=").concat(Utils.getStringDateFormat(expDatePrincipal))); //$NON-NLS-1$
			}
			if(rs.getString(5)!=null && !"".equals(rs.getString(5))) {//$NON-NLS-1$
				cert.setCertBkupb64ToX509(rs.getString(5));
				final String certBkup[] = cert.getCertX509_backup().getSubjectX500Principal().getName().split(",");//$NON-NLS-1$
				java.util.Date expDateBkup= new java.util.Date();
				expDateBkup = cert.getCertX509_backup().getNotAfter();
				cert.setCert_backup(certBkup[0].concat("<br>Fecha de Caducidad=").concat(Utils.getStringDateFormat(expDateBkup))); //$NON-NLS-1$
			}
			if(rs.getString(6) != null && !"".equals(rs.getString(6))) { //$NON-NLS-1$
				cert.setHuella_principal(rs.getString(6));
			}

			if(rs.getString(7) != null && !"".equals(rs.getString(7))) { //$NON-NLS-1$
				cert.setHuella_backup(rs.getString(7));
			}

			data.add(Json.createObjectBuilder()
					.add("id_certificado",cert.getId_certificado()) //$NON-NLS-1$
					.add("nombre_cert", cert.getNombre_cert()) //$NON-NLS-1$
					.add("fec_alta", Utils.getStringDateFormat(date != null ? date : rs.getDate(3))) //$NON-NLS-1$
					.add("cert_principal", cert.getCert_principal() != null ? cert.getCert_principal() : "") //$NON-NLS-1$ //$NON-NLS-2$
					.add("cert_backup", cert.getCert_backup() != null ? cert.getCert_backup() : "") //$NON-NLS-1$ //$NON-NLS-2$
					.add("huella_principal", cert.getHuella_principal() != null ? cert.getHuella_principal() : "") //$NON-NLS-1$ //$NON-NLS-2$
					.add("huella_backup", cert.getHuella_backup() != null ? cert.getHuella_backup() : "") //$NON-NLS-1$ //$NON-NLS-2$
					);

		}
		rs.close();
		st.close();

		jsonObj.add("CertList", data); //$NON-NLS-1$

		final StringWriter writer = new StringWriter();
		try  {
			final JsonWriter jw = Json.createWriter(writer);
	        jw.writeObject(jsonObj.build());
			jw.close();
	    }
		catch (final Exception e) {
			LOGGER.log(Level.WARNING, "Error al leer los registros en la tabla de certificados", e); //$NON-NLS-1$
		}

	    return writer.toString();


	}




	/**
	 * Consulta que obtiene todos los registros de la tabla tb_certificados paginados
	 * @param start
	 * @param total
	 * @return Devuelve un String con formato JSON
	 * @throws SQLException
	 * @throws CertificateException
	 * @throws IOException
	 */
	public static String getCertificatesPag(final String start, final String total) throws SQLException, CertificateException, IOException {

		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
		final JsonArrayBuilder data = Json.createArrayBuilder();

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
			cert.setId_certificado(rs.getString(1));
			cert.setNombre_cert(rs.getString(2));
			cert.setFec_alta(rs.getDate(3));
			if(rs.getString(4) != null && !"".equals(rs.getString(4))) { //$NON-NLS-1$
				cert.setCertPrincipalb64ToX509(rs.getString(4));
				final String certPrincipal[] = cert.getCertX509_principal().getSubjectX500Principal().getName().split(","); //$NON-NLS-1$
				java.util.Date expDatePrincipal = new java.util.Date();
				expDatePrincipal = cert.getCertX509_principal().getNotAfter();
				cert.setCert_principal(certPrincipal[0].concat("<br>Fecha de Caducidad=").concat(Utils.getStringDateFormat(expDatePrincipal))); //$NON-NLS-1$
			}
			if(rs.getString(5) != null && !"".equals(rs.getString(5))) { //$NON-NLS-1$
				cert.setCertBkupb64ToX509(rs.getString(5));
				final String certBkup[] = cert.getCertX509_backup().getSubjectX500Principal().getName().split(","); //$NON-NLS-1$
				java.util.Date expDateBkup = new java.util.Date();
				expDateBkup = cert.getCertX509_backup().getNotAfter();
				cert.setCert_backup(certBkup[0].concat("<br>Fecha de Caducidad=").concat(Utils.getStringDateFormat(expDateBkup))); //$NON-NLS-1$
			}
			if(rs.getString(6) != null && !"".equals(rs.getString(6))) { //$NON-NLS-1$
				cert.setHuella_principal(rs.getString(6));
			}

			if(rs.getString(7) != null && !"".equals(rs.getString(7))) { //$NON-NLS-1$
				cert.setHuella_backup(rs.getString(7));
			}
			data.add(Json.createObjectBuilder()
					.add("id_certificado",cert.getId_certificado()) //$NON-NLS-1$
					.add("nombre_cert", cert.getNombre_cert()) //$NON-NLS-1$
					.add("fec_alta", Utils.getStringDateFormat(date != null ? date : rs.getDate(3))) //$NON-NLS-1$
					.add("cert_principal", cert.getCert_principal() != null ? cert.getCert_principal() : "") //$NON-NLS-1$ //$NON-NLS-2$
					.add("cert_backup", cert.getCert_backup() != null ? cert.getCert_backup() : "") //$NON-NLS-1$ //$NON-NLS-2$
					.add("huella_principal", cert.getHuella_principal() !=null ? cert.getHuella_principal() : "") //$NON-NLS-1$ //$NON-NLS-2$
					.add("huella_backup", cert.getHuella_backup() !=null ? cert.getHuella_backup() : "") //$NON-NLS-1$ //$NON-NLS-2$
					);

		}
		rs.close();
		st.close();

		jsonObj.add("CertList", data); //$NON-NLS-1$

		final StringWriter writer= new StringWriter();
		try  {
			final JsonWriter jw = Json.createWriter(writer);
	        jw.writeObject(jsonObj.build());
			jw.close();
	    }
		catch (final Exception e) {
			LOGGER.log(Level.WARNING, "Error al leer los registros en la tabla de certificados", e); //$NON-NLS-1$
		}

	    return writer.toString();

	}

	/**
	 * Devuelve una aplicaci&oacute;n registrada en el sistema dado su id.
	 * @param id de la aplicaci&oacute;n a encontrar.
	 * @return aplicaci&oacute;n encontrada.
	 * @throws SQLException si hay un problema en la conexi&oacute;n con la base de datos
	 * @throws IOException
	 * @throws CertificateException
	 */
	public static CertificateFire selectCertificateByID(final String id) throws SQLException, CertificateException, IOException {
		final CertificateFire cert = new CertificateFire();
		/*SELECT id_certificado, nombre_cert, fec_alta, cert_principal,cert_backup,huella_principal,huella_backup,fk_id_aplicacion
		 * FROM tb_certificados WHERE id_certificado=? ORDER BY nombre_cert*/
		final PreparedStatement st = DbManager.prepareStatement(ST_SELECT_CERTIFICATES_BYID);
		st.setString(1, id);
		final ResultSet rs = st.executeQuery();
		if (rs.next()){
			cert.setId_certificado(rs.getString(1));
			cert.setNombre_cert(rs.getString(2));
			cert.setFec_alta(rs.getDate(3));
			if(rs.getString(4) != null && !"".equals(rs.getString(4))) { //$NON-NLS-1$
				cert.setCert_principal(rs.getString(4));
				cert.setCertPrincipalb64ToX509(rs.getString(4));
			}
			if(rs.getString(5) != null && !"".equals(rs.getString(5))) { //$NON-NLS-1$
				cert.setCert_backup(rs.getString(5));
				cert.setCertBkupb64ToX509(rs.getString(5));
			}
			if(rs.getString(6) != null && !"".equals(rs.getString(6))) { //$NON-NLS-1$
				cert.setHuella_principal(rs.getString(6));
			}

			if(rs.getString(7) != null && !"".equals(rs.getString(7))) { //$NON-NLS-1$
				cert.setHuella_backup(rs.getString(7));
			}
		}
		rs.close();
		st.close();
		return cert;

	}

	/**
	 * Selecciona todos los certificados ordenados por id_certificado
	 * @return
	 * @throws SQLException
	 * @throws CertificateException
	 * @throws IOException
	 */
	public static List<CertificateFire> selectCertificateALL() throws SQLException, CertificateException, IOException {
		final List<CertificateFire> lCert = new ArrayList<CertificateFire>();
		/*SELECT id_certificado, nombre_cert, fec_alta, cert_principal,cert_backup,huella_principal,huella_backup FROM tb_certificados ORDER BY id_certificado*/
		final PreparedStatement st = DbManager.prepareStatement(ST_SELECT_CERTIFICATES_ALL);

		final ResultSet rs = st.executeQuery();
		while (rs.next()) {
			final CertificateFire cert = new CertificateFire();
			cert.setId_certificado(rs.getString(1));
			cert.setNombre_cert(rs.getString(2));
			cert.setFec_alta(rs.getDate(3));
			if(rs.getString(4) != null && !"".equals(rs.getString(4))) { //$NON-NLS-1$
				cert.setCert_principal(rs.getString(4));
				cert.setCertPrincipalb64ToX509(rs.getString(4));
			}
			if(rs.getString(5) != null && !"".equals(rs.getString(5))) { //$NON-NLS-1$
				cert.setCert_backup(rs.getString(5));
				cert.setCertBkupb64ToX509(rs.getString(5));
			}
			if(rs.getString(6) != null && !"".equals(rs.getString(6))) { //$NON-NLS-1$
				cert.setHuella_principal(rs.getString(6));
			}

			if(rs.getString(7) != null && !"".equals(rs.getString(7))) { //$NON-NLS-1$
				cert.setHuella_backup(rs.getString(7));
			}
			lCert.add(cert);
		}
		rs.close();
		st.close();
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

		LOGGER.info("Damos de baja el certificado con el ID: " + id); //$NON-NLS-1$

		st.execute();

		st.close();
	}

	/**
	 * Actualizamos un certificado existente.
	 * @param nombre - Nombre del certificado.
	 * @param cert_principal - Certificado principal.
	 * @param huella_principal- Huella del certificado principal.
	 * @param cert_backup - Certificado de respaldo.
	 * @param huella_backup - Huella del certificado de respaldo.
	 * @throws SQLException si hay un problema en la conexi&oacute;n con la base de datos
	 */
	public static void updateCertificate (final String id,final String nombre, final String cert_principal,
			final String huella_principal,final String cert_backup, final String huella_backup) throws SQLException{
		final PreparedStatement st = DbManager.prepareStatement(STATEMENT_UPDATE_CERTIFICATE);
		/*UPDATE tb_certificados SET nombre_cert=?, cert_principal = ?, cert_backup = ?, huella_principal = ?, huella_backup = ? WHERE id_certificado = ?*/
		st.setString(1, nombre);
		st.setString(2, cert_principal);
		st.setString(3, cert_backup);
		st.setString(4, huella_principal);
	    st.setString(5, huella_backup);
	    st.setInt(6, Integer.parseInt(id));
		LOGGER.info("Actualizamos el certificado '" + nombre + "' con el ID: "+id ); //$NON-NLS-1$ //$NON-NLS-2$

		st.execute();
		st.close();
	}

	/**
	 * Agrega un nuevo certificado al sistema.
	 * @param nombre Nombre del certificado.
	 * @param nombre - Nombre del certificado.
	 * @param cert_principal - Certificado principal.
	 * @param huella_principal- Huella del certificado principal.
	 * @param cert_backup - Certificado de respaldo.
	 * @param huella_backup - Huella del certificado de respaldo.
	 * @throws SQLException Cuando no se puede insertar la nueva aplicacion en base de datos.
	 */
	public static void createCertificate(final String nombre,final String cert_principal,
										final String huella_principal,final String cert_backup,
										final String huella_backup) throws SQLException {

		final PreparedStatement st = DbManager.prepareStatement(STATEMENT_INSERT_CERTIFICATE);

		st.setString(1, nombre);
		st.setDate(2, new Date(new java.util.Date().getTime()));
		st.setString(3, cert_principal);
		st.setString(4, cert_backup);
		st.setString(5, huella_principal);
	    st.setString(6, huella_backup);


		LOGGER.info("Damos de alta la aplicacion '" + nombre +"'" ); //$NON-NLS-1$ //$NON-NLS-2$

		st.execute();

		st.close();
	}


}
