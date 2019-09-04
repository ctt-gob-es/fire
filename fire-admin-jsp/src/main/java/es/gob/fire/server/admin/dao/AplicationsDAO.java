/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.admin.dao;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;

import es.gob.fire.server.admin.conf.DbManager;
import es.gob.fire.server.admin.entity.Application;
import es.gob.fire.server.admin.entity.CertificateFire;
import es.gob.fire.server.admin.entity.User;
import es.gob.fire.server.admin.service.LogUtils;
import es.gob.fire.server.admin.tool.Base64;
import es.gob.fire.server.admin.tool.Hexify;
import es.gob.fire.server.admin.tool.Utils;



/**
 * DAO para la gesti&oacute;n de aplicaciones dadas de alta en el sistema.
 */
public class AplicationsDAO {

	private static final Logger LOGGER = Logger.getLogger(AplicationsDAO.class.getName());

	private static final String DEFAULT_CHARSET = "utf-8"; //$NON-NLS-1$

	private static final String MD_ALGORITHM = "SHA-1"; //$NON-NLS-1$

	private static final String HMAC_ALGORITHM = "HmacMD5"; //$NON-NLS-1$

	private static final String STATEMENT_SELECT_CONFIG_VALUE = "SELECT valor FROM tb_configuracion WHERE parametro = ?"; //$NON-NLS-1$

	private static final String STATEMENT_SELECT_APPLICATIONS = "SELECT tb_app.id, tb_app.nombre, tb_usu.nombre, tb_usu.apellidos, tb_app.fecha_alta, tb_app.fk_certificado, tb_app.habilitado  \r\n" +
			"FROM tb_aplicaciones AS tb_app, tb_usuarios AS tb_usu \r\n" +
			"WHERE tb_app.fk_responsable = tb_usu.id_usuario ORDER BY tb_app.nombre"; //$NON-NLS-1$

	private static final String STATEMENT_SELECT_APPLICATIONS_PAG = "SELECT id, nombre, fk_responsable, fecha_alta, fk_certificado, habilitado  FROM tb_aplicaciones ORDER BY nombre limit ?,?"; //$NON-NLS-1$

	private static final String ST_SELECT_APPLICATIONS_BYCERT = "SELECT tb_app.id, tb_app.nombre, tb_app.fecha_alta,tb_usu.nombre, tb_usu.apellidos, tb_app.fk_certificado " //$NON-NLS-1$
	       + "FROM tb_certificados AS tb_cert, tb_aplicaciones AS tb_app, tb_usuarios AS tb_usu " //$NON-NLS-1$
	       + "WHERE tb_app.fk_responsable = tb_usu.id_usuario AND tb_app.fk_certificado = tb_cert.id_certificado and tb_cert.id_certificado=? ORDER BY tb_app.nombre "; //$NON-NLS-1$

	private static final String STATEMENT_SELECT_APPLICATIONS_COUNT = "SELECT count(*) FROM tb_aplicaciones"; //$NON-NLS-1$

	private static final String ST_SELECT_APPLICATIONS_COUNT_BYCERT = "SELECT count(*) FROM tb_aplicaciones a, tb_certificados c where a.fk_certificado=c.id_certificado and c.id_certificado=?"; //$NON-NLS-1$

	private static final String STATEMENT_INSERT_APPLICATION = "INSERT INTO tb_aplicaciones(id, nombre, fk_responsable,  fecha_alta,fk_certificado,habilitado ) VALUES (?, ?, ?, ?, ?,?)"; //$NON-NLS-1$

	private static final String STATEMENT_REMOVE_APPLICATION = "DELETE FROM tb_aplicaciones WHERE id = ?"; //$NON-NLS-1$

	private static final String STATEMENT_UPDATE_APPLICATION = "UPDATE tb_aplicaciones SET nombre=?, fk_responsable = ?,  fk_certificado=?, habilitado=?  WHERE id = ?";//$NON-NLS-1$

	private static final String KEY_ADMIN_PASS = "admin_pass"; //$NON-NLS-1$

	private static final String STATEMENT_SELECT_APPLICATION_INFO = "SELECT tb_app.id, tb_app.nombre, tb_app.fecha_alta,tb_app.habilitado, " //$NON-NLS-1$
			+ "tb_usu.id_usuario, tb_usu.nombre_usuario, tb_usu.nombre, tb_usu.apellidos, tb_usu.correo_elec, tb_usu.telf_contacto, tb_cert.id_certificado, tb_cert.nombre_cert, tb_cert.cert_principal, tb_cert.cert_backup " //$NON-NLS-1$
			+ "FROM tb_certificados AS tb_cert, tb_aplicaciones AS tb_app, tb_usuarios AS tb_usu " //$NON-NLS-1$
			+ "WHERE tb_app.fk_responsable = tb_usu.id_usuario AND tb_app.fk_certificado = tb_cert.id_certificado AND tb_app.id = ?"; //$NON-NLS-1$

	private static final String ST_SELECT_APPLICATIONS_BYUSERS = "SELECT tb_app.id, tb_app.nombre,tb_app.fk_responsable, tb_app.fecha_alta " //$NON-NLS-1$
		       + "FROM tb_aplicaciones AS tb_app, tb_usuarios AS tb_usu " //$NON-NLS-1$
		       + "WHERE tb_app.fk_responsable = tb_usu.id_usuario AND tb_usu.id_usuario=? ORDER BY tb_app.nombre "; //$NON-NLS-1$

	private static final String ST_SELECT_APPLICATIONS_COUNT_BYUSERS = "SELECT count(*) FROM tb_aplicaciones AS tb_app, tb_usuarios AS tb_usu WHERE tb_app.fk_responsable = tb_usu.id_usuario and tb_usu.id_usuario=?"; //$NON-NLS-1$



	/**
	 * Comprueba contra base de datos que la contrase&ntilde;a indicada se corresponda
	 * con la del administrador del sistema.
	 * @param psswd Contrase&ntilde;a.
	 * @return {@code true} si la contrase&ntilde;a es del administrador, {@code false} en caso contrario.
	 * @throws SQLException Cuando ocurre un error al comprobar los datos contra la base de datos.
	 */
	public static boolean checkAdminPassword(final String psswd) throws SQLException {

		final byte[] md;
		try {
			md = MessageDigest.getInstance(MD_ALGORITHM).digest(psswd.getBytes(DEFAULT_CHARSET));
		} catch (final NoSuchAlgorithmException e) {
			LOGGER.log(Level.SEVERE, "Error de configuracion en el servicio de administracion. Algoritmo de huella incorrecto", e); //$NON-NLS-1$
			return false;
		} catch (final UnsupportedEncodingException e) {
			LOGGER.log(Level.SEVERE, "Error de configuracion en el servicio de administracion. Codificacion incorrecta", e); //$NON-NLS-1$
			return false;
		}

		boolean result = true;
		final String keyAdminB64 = getConfigValue(KEY_ADMIN_PASS);
		if (keyAdminB64 ==  null || !keyAdminB64.equals(Base64.encode(md))) {
			LOGGER.severe("Se ha insertado una contrasena de administrador no valida"); //$NON-NLS-1$
			result = false;
		}
		return result;
	}

	/**
	 * Obtiene el valor de una clave de la tabla de par&aacute;metros de configuraci&oacute;n.
	 * @param conn Conexi&oacute;n con base de datos.
	 * @param param Clave del par&aacute;metro a obtener.
	 * @return Valor del par&aacute;metro de configuraci&oacute;n.
	 * @throws SQLException Cuando ocurre un error en el acceso al par&aacute;metro.
	 */
	private static String getConfigValue(final String param) throws SQLException {
		String value = null;
		try {
			final PreparedStatement st = DbManager.prepareStatement(STATEMENT_SELECT_CONFIG_VALUE);
			st.setString(1, param);
			final ResultSet rs = st.executeQuery();
			if (rs.next()) {
				value = rs.getString(1);
			}

			st.close();
			rs.close();
		} catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Error al acceder a la base datos", e); //$NON-NLS-1$
			throw new SQLException(e);
		}
		return value;
	}


	/**
	 * Consulta que obtiene todos los registros de la tabla tb_aplicaciones.
	 * @return Devuelve los datos como un string en formato JSON
	 * @throws SQLException
	 */
	public static String getApplicationsJSON() {

		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
		final JsonArrayBuilder data = Json.createArrayBuilder();

		try {
			final PreparedStatement st = DbManager.prepareStatement(STATEMENT_SELECT_APPLICATIONS);
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
						.add("habilitado", rs.getBoolean(7)) //$NON-NLS-1$

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


	/**
	 * Obtiene un JSON con el n&uacute;mero de aplicaciones.
	 * @return Estructura JSON.
	 */
	public static String getApplicationsCount() {
		int count = 0;
		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
		try {
			final PreparedStatement st = DbManager.prepareStatement(STATEMENT_SELECT_APPLICATIONS_COUNT);
			final ResultSet rs = st.executeQuery();
			if(rs.next()) {
				count = rs.getInt(1);
			}
			jsonObj.add("count", count);  //$NON-NLS-1$
			rs.close();
			st.close();

		}
		catch(final Exception e){
			LOGGER.log(Level.SEVERE, "Error al leer los registros en la tabla de aplicaciones", e); //$NON-NLS-1$
		}

		final StringWriter writer = new StringWriter();
		try  {
			final JsonWriter jw = Json.createWriter(writer);
			jw.writeObject(jsonObj.build());
			jw.close();
		}
		catch(final Exception e){
			LOGGER.log(Level.SEVERE, "Error al componer la estructura JSON con el numero de aplicaciones", e); //$NON-NLS-1$
		}

	    return writer.toString();


	}
	/**
	 * Obtiene una estructura JSON con el numero de aplicaciones que utilizan un certificado.
	 * @param id Identificador del certificado.
	 * @return Estructura JSON.
	 */
	public static String getApplicationsCountByCertificate(final String id) {

		int count = 0;
		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
		try {
			final PreparedStatement st = DbManager.prepareStatement(ST_SELECT_APPLICATIONS_COUNT_BYCERT);
			st.setInt(1, Integer.parseInt(id));
			final ResultSet rs = st.executeQuery();
			if (rs.next()) {
				count = rs.getInt(1);
			}
			jsonObj.add("count", count);  //$NON-NLS-1$
			rs.close();
			st.close();
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Error al leer los registros en la tabla de aplicaciones y/o certificados", e); //$NON-NLS-1$
		}

		final StringWriter writer = new StringWriter();
		try  {
			final JsonWriter jw = Json.createWriter(writer);
	        jw.writeObject(jsonObj.build());
	        jw.close();
	    }
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Error al componer la estructura JSON con el listado de aplicaciones que usan un certificado", e); //$NON-NLS-1$
		}

	    return writer.toString();
	}
	/**
	 * Obtiene una estructura JSON con una p&aacute;gina del listado de aplicaciones.
	 * @param start Elemento por el cual empezar a la p&aacute;gina.
	 * @param total N&uacute;mero de elementos de la p&aacute;gina.
	 * @return Estructura JSON.
	 */
	public static String getApplicationsPag(final String start, final String total) {


		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
		final JsonArrayBuilder data = Json.createArrayBuilder();

		try {
			final PreparedStatement st = DbManager.prepareStatement(STATEMENT_SELECT_APPLICATIONS_PAG);
			st.setInt(1,Integer.parseInt(start));
			st.setInt(2, Integer.parseInt(total));
			final ResultSet rs = st.executeQuery();
			while (rs.next()) {
				Date date= null;
				final Timestamp timestamp = rs.getTimestamp(4);
				if (timestamp != null) {
					date = new Date(timestamp.getTime());
				}

				data.add(Json.createObjectBuilder()
						.add("id", rs.getString(1)) //$NON-NLS-1$
						.add("nombre", rs.getString(2)) //$NON-NLS-1$
						.add("fk_responsable", rs.getString(3)) //$NON-NLS-1$
						//.add("correo", rs.getString(4)!= null ? rs.getString(4) : "") //$NON-NLS-1$ //$NON-NLS-2$
						//.add("telefono", rs.getString(5) != null ? rs.getString(5) : "") //$NON-NLS-1$ //$NON-NLS-2$
						.add("alta", es.gob.fire.server.admin.tool.Utils.getStringDateFormat(date !=null ? date : rs.getDate(4))) //$NON-NLS-1$
						.add("fk_certificado", rs.getString(5)) //$NON-NLS-1$
						.add("habilitado", rs.getString(6) !=null ? rs.getString(6) : "0")
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
			LOGGER.log(Level.SEVERE, "Error al componer la estructura JSON con el listado de aplicaciones que usan un certificado", e); //$NON-NLS-1$
		}

	    return writer.toString();

	}

	/**
	 * Obtiene la estructura JSON con todas las aplicaciones que utilizan el certificado indicado.
	 * @param id Identificador del certificado.
	 * @return Estructura JSON.
	 */
	public static String getApplicationsByCertificateJSON(final String id) {

		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
		final JsonArrayBuilder data = Json.createArrayBuilder();

		try {
			final PreparedStatement st = DbManager.prepareStatement(ST_SELECT_APPLICATIONS_BYCERT);
			st.setInt(1, Integer.parseInt(id));

			final ResultSet rs = st.executeQuery();
			while (rs.next()) {

				Date date = null;
				final Timestamp timestamp = rs.getTimestamp(3);
				if (timestamp != null) {
					date = new Date(timestamp.getTime());
				}
				data.add(Json.createObjectBuilder()
						.add("id", rs.getString(1)) //$NON-NLS-1$
						.add("nombre", rs.getString(2)) //$NON-NLS-1$
						.add("nombre_responsable", rs.getString(4) + " " + rs.getString(5)) //$NON-NLS-1$ //$NON-NLS-2$
						.add("alta", es.gob.fire.server.admin.tool.Utils.getStringDateFormat(date !=null ? date : rs.getDate(3))) //$NON-NLS-1$
						.add("fk_certificado", rs.getString(6)) //$NON-NLS-1$
						);
			}
			rs.close();
			st.close();
		}
		catch(final Exception e){
			LOGGER.log(Level.SEVERE, "Error al leer los registros en la tabla de aplicaciones y/o certificados", e); //$NON-NLS-1$
		}
		jsonObj.add("AppList", data); //$NON-NLS-1$

		final StringWriter writer = new StringWriter();
		try  {
			final JsonWriter jw = Json.createWriter(writer);
	        jw.writeObject(jsonObj.build());
	        jw.close();
	    }
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Error al componer la estructura JSON con el listado de aplicaciones que usan un certificado", e); //$NON-NLS-1$
		}
	    return writer.toString();
	}

	/**
	 * Agrega una nueva aplicaci&oacute;n al sistema.
	 * @param nombre Nombre de la aplicacion.
	 * @param fk_responsable Repsonsable de la aplicaci&oacute;n.
	 * @param email Correo electr&oacute;nico de la aplicaci&oacute;n.
	 * @param telefono N&uacute;mero de te&eacute;lefono de la aplicaci&oacute;n.
	 * @param fkCer certificado en base 64 asignado a la la aplicaci&oacute;n.
	 * @throws SQLException Cuando no se puede insertar la nueva aplicacion en base de datos.
	 * @throws GeneralSecurityException  Cuando no se puede generar el identificador aleatorio de la aplicaci&oacute;n.
	 * @SQL INSERT INTO tb_aplicaciones(id, nombre, fk_responsable, resp_correo, resp_telefono, fecha_alta,fk_certificado ) VALUES (?, ?, ?, ?, ?)
	 */
	public static void createApplication(final String nombre, final String fk_responsable, final String fkCer, final boolean habilitado)  throws SQLException, GeneralSecurityException {

		final String id = generateId();
		final PreparedStatement st = DbManager.prepareStatement(STATEMENT_INSERT_APPLICATION);

		st.setString(1, id);
		st.setString(2, nombre);
		st.setString(3, fk_responsable);
	//	st.setString(4, email);
//		st.setString(5, telefono);
		st.setDate(4, new Date(new java.util.Date().getTime()));
	    st.setString(5, fkCer);
	    st.setBoolean(6, habilitado);

		LOGGER.info("Damos de alta la aplicacion '" + nombre + "' con el ID: " + id); //$NON-NLS-1$ //$NON-NLS-2$
		st.execute();
		st.close();
	}

	/**
	 * Genera un nuevo identificador de aplicaci&oacute;n.
	 * @return Identificador de aplicaci&oacute;n.
	 * @throws GeneralSecurityException Cuando no se puede generar un identificador.
	 */
	private static String generateId() throws GeneralSecurityException {

		Mac mac;
		try {
			final KeyGenerator kGen = KeyGenerator.getInstance(HMAC_ALGORITHM);
			final SecretKey hmacKey = kGen.generateKey();

			mac = Mac.getInstance(hmacKey.getAlgorithm());
			mac.init(hmacKey);
		}
		catch (final GeneralSecurityException e) {
			LOGGER.severe("No ha sido posible generar una clave aleatoria como identificador de aplicacion: " + e); //$NON-NLS-1$
			throw e;
		}

		return Hexify.hexify(mac.doFinal(), "").substring(0, 12); //$NON-NLS-1$
	}

	/**
	 * Elimina una aplicaci&oacute;n de la base de datos.
	 * @param id Identificador de la aplicaci&oacute;n.
	 * @throws SQLException Cuando ocurre un error durante la operaci&oacute;n.
	 */
	public static void removeApplication(final String id) throws SQLException {

		final PreparedStatement st = DbManager.prepareStatement(STATEMENT_REMOVE_APPLICATION);
		st.setString(1, id);
		LOGGER.info("Damos de baja la aplicacion con el ID: " + LogUtils.cleanText(id)); //$NON-NLS-1$
		st.execute();
		st.close();
	}

	/**
	 * Devuelve una aplicaci&oacute;n registrada en el sistema dado su responsable.
	 * @param id Identificador de aplicaci&oacute;n.
	 * @return Aplicaci&oacute;n encontrada o {@code null} si no se encontr&oacute;.
	 */
	public static Application getApplicationWithCompleteInfo (final String id) {
		Application result = null;
		try {
			final PreparedStatement st = DbManager.prepareStatement(STATEMENT_SELECT_APPLICATION_INFO);

			st.setString(1, id);
			final ResultSet rs = st.executeQuery();
			if (rs.next()) {

				final User responsable = new User();
				responsable.setId(rs.getString(5));
				responsable.setUserName(rs.getString(6));
				responsable.setName(rs.getString(7));
				responsable.setSurname(rs.getString(8));
				responsable.setMail(rs.getString(9));
				responsable.setTelephone(rs.getString(10));

				final CertificateFire certificate = new CertificateFire();
				certificate.setId(rs.getString(11));
				certificate.setNombre(rs.getString(12));

				if (rs.getString(13) != null && !"".equals(rs.getString(13))) { //$NON-NLS-1$
					certificate.setCertPrincipalb64ToX509(rs.getString(13));
					final String certPrincipal[] = certificate.getX509Principal().getSubjectX500Principal().getName().split(","); //$NON-NLS-1$
					java.util.Date expDatePrincipal = new java.util.Date();
					expDatePrincipal=certificate.getX509Principal().getNotAfter();
					certificate.setCertPrincipal(certPrincipal[0] + "<br>Fecha de Caducidad=" + Utils.getStringDateFormat(expDatePrincipal)); //$NON-NLS-1$
				}
				if (rs.getString(14)!=null && !"".equals(rs.getString(14))) { //$NON-NLS-1$
					certificate.setCertBkupb64ToX509(rs.getString(14));
					final String certBkup[] = certificate.getX509Backup().getSubjectX500Principal().getName().split(",");//$NON-NLS-1$
					java.util.Date expDateBkup= new java.util.Date();
					expDateBkup = certificate.getX509Backup().getNotAfter();
					certificate.setCertBackup(certBkup[0] + "<br>Fecha de Caducidad=" + Utils.getStringDateFormat(expDateBkup)); //$NON-NLS-1$
				}


				result = new Application();
				result.setId(rs.getString(1));
				result.setNombre(rs.getString(2));
				result.setResponsable(responsable);
				result.setAlta(rs.getDate(3));
				result.setHabilitado(rs.getBoolean(4));
				result.setCertificate(certificate) ;
			}
			rs.close();
			st.close();
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "No se pudo leer la tabla con el listado de aplicaciones", e); //$NON-NLS-1$
			result = null;
		}

		return result;
	}


	/**
	 * Actualizamos una aplicaci&oacute;n existente.
	 * @param id Id de la aplicaci&oacute;n.
	 * @param nombre Nombre de la aplicaci&oacute;n.
	 * @param fk_responsable Responsable de la aplicaci&oacute;n.
	 * @param fkCer certificado en base 64 asignado a la la aplicaci&oacute;n.
	 * @throws SQLException si hay un problema en la conexi&oacute;n con la base de datos
	 */
	public static void updateApplication (final String id, final String nombre, final String fk_responsable,
			final String fkCer, final boolean habilitado) throws SQLException{
		final PreparedStatement st = DbManager.prepareStatement(STATEMENT_UPDATE_APPLICATION);

		st.setString(1, nombre);
		st.setString(2, fk_responsable);
	    st.setString(3, fkCer);
	    st.setBoolean(4, habilitado);
	    st.setString(5, id);


		LOGGER.info("Actualizamos la aplicacion '" + nombre + "' con el ID: " + id); //$NON-NLS-1$ //$NON-NLS-2$
		st.execute();
		st.close();
	}

	/**
	 * Obtiene la estructura JSON con todas las aplicaciones que utiliza el usuario indicado.
	 * @param id Identificador del usuario.
	 * @return Estructura JSON.
	 */
	public static String getApplicationsByUserJSON(final String id) {

		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
		final JsonArrayBuilder data = Json.createArrayBuilder();

		try {
			final PreparedStatement st = DbManager.prepareStatement(ST_SELECT_APPLICATIONS_BYUSERS);
			st.setInt(1, Integer.parseInt(id));

			final ResultSet rs = st.executeQuery();
			while (rs.next()) {

				Date date = null;
				final Timestamp timestamp = rs.getTimestamp(4);
				if (timestamp != null) {
					date = new Date(timestamp.getTime());
				}
				data.add(Json.createObjectBuilder()
						.add("id", rs.getString(1)) //$NON-NLS-1$
						.add("nombre", rs.getString(2)) //$NON-NLS-1$
						.add("fk_responsable",rs.getString(3)) //$NON-NLS-1$
						.add("alta", es.gob.fire.server.admin.tool.Utils.getStringDateFormat(date !=null ? date : rs.getDate(4))) //$NON-NLS-1$

						);
			}
			rs.close();
			st.close();
		}
		catch(final Exception e){
			LOGGER.log(Level.SEVERE, "Error al leer los registros en la tabla de aplicaciones y/o usuarios", e); //$NON-NLS-1$
		}
		jsonObj.add("AppList", data); //$NON-NLS-1$

		final StringWriter writer = new StringWriter();
		try  {
			final JsonWriter jw = Json.createWriter(writer);
	        jw.writeObject(jsonObj.build());
	        jw.close();
	    }
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Error al componer la estructura JSON con el listado de aplicaciones que usa un usuario", e); //$NON-NLS-1$
		}
	    return writer.toString();
	}
	/**
	 * Obtiene una estructura JSON con el numero de aplicaciones de las que un usuario. es responsable
	 * @param id Identificador del usuario.
	 * @return Estructura JSON.
	 */
	public static String getApplicationsCountByUsersJSON(final String idUsr) {

		int count = 0;
		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
		try {
			final PreparedStatement st = DbManager.prepareStatement(ST_SELECT_APPLICATIONS_COUNT_BYUSERS);
			st.setInt(1, Integer.parseInt(idUsr));
			final ResultSet rs = st.executeQuery();
			if (rs.next()) {
				count = rs.getInt(1);
			}
			jsonObj.add("count", count);  //$NON-NLS-1$
			rs.close();
			st.close();
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Error al leer los registros en la tabla de aplicaciones y/o usuarios", e); //$NON-NLS-1$
		}

		final StringWriter writer = new StringWriter();
		try  {
			final JsonWriter jw = Json.createWriter(writer);
	        jw.writeObject(jsonObj.build());
	        jw.close();
	    }
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Error al componer la estructura JSON con el listado de aplicaciones que usan un usuario", e); //$NON-NLS-1$
		}

	    return writer.toString();
	}
}
