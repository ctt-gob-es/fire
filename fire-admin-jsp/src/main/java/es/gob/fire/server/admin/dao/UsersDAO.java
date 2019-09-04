package es.gob.fire.server.admin.dao;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
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
import es.gob.fire.server.admin.entity.Application;
import es.gob.fire.server.admin.entity.CertificateFire;
import es.gob.fire.server.admin.entity.User;
import es.gob.fire.server.admin.service.LogUtils;
import es.gob.fire.server.admin.service.RolePermissions;
import es.gob.fire.server.admin.service.UserRestorationInfo;
import es.gob.fire.server.admin.tool.Base64;
import es.gob.fire.server.admin.tool.Utils;

/**
 * DAO para la gesti&oacute;n de usuarios dados de alta en el sistema.
 */
public class UsersDAO {
	private static final Logger LOGGER = Logger.getLogger(UsersDAO.class.getName());

	private static final String DEFAULT_CHARSET = "utf-8"; //$NON-NLS-1$

	private static final String MD_ALGORITHM = "SHA-256"; //$NON-NLS-1$

	private static final String ST_SELECT_PASSWD_BY_NAME = "SELECT clave FROM tb_usuarios WHERE nombre_usuario = ?"; //$NON-NLS-1$

	private static final String ST_SELECT_USER_BY_ID = "SELECT id_usuario, nombre_usuario, clave,nombre, apellidos, correo_elec, telf_contacto, fk_rol, fec_alta, usu_defecto FROM tb_usuarios  WHERE id_usuario = ?";//$NON-NLS-1$

	private static final String ST_SELECT_USER_BY_NAME = "SELECT id_usuario, nombre_usuario, clave,nombre, apellidos, correo_elec, telf_contacto, fk_rol, fec_alta, usu_defecto  FROM tb_usuarios  WHERE nombre_usuario = ?";//$NON-NLS-1$

	private static final String ST_SELECT_USER_BY_MAIL = "SELECT id_usuario, nombre_usuario, clave,nombre, apellidos, correo_elec, telf_contacto, fk_rol, fec_alta, usu_defecto  FROM tb_usuarios  WHERE correo_elec = ?";//$NON-NLS-1$

	private static final String ST_SELECT_ALL_USERS = "SELECT tb_usu.id_usuario, tb_usu.nombre_usuario, tb_usu.clave,tb_usu.nombre, tb_usu.apellidos, tb_usu.correo_elec, tb_usu.telf_contacto,tb_rol.nombre_rol, tb_usu.fec_alta, tb_usu.usu_defecto  FROM tb_usuarios AS tb_usu, tb_roles AS tb_rol "
			+ "WHERE tb_usu.fk_rol = tb_rol.id  ORDER BY tb_usu.nombre_usuario"; //$NON-NLS-1$


	private static final String ST_SELECT_ALL_USERS_PERMISSIONS = "SELECT  id_usuario, nombre, apellidos, permisos  FROM tb_usuarios, tb_roles WHERE tb_roles.id = tb_usuarios.fk_rol";//$NON-NLS-1$

	private static final String ST_SELECT_ALL_USERS_COUNT = "SELECT count(*) FROM tb_usuarios"; //$NON-NLS-1$

	private static final String ST_SELECT_ALL_USERS_PAG = "SELECT id_usuario, nombre_usuario, clave,nombre, apellidos, correo_elec, telf_contacto, fk_rol, fec_alta, usu_defecto FROM tb_usuarios ORDER BY id_usuario limit ?,?";//$NON-NLS-1$

	private static final String ST_UDATE_USER_BY_ID = "UPDATE tb_usuarios SET  nombre=?, apellidos=?, correo_elec=?, fk_rol=?, telf_contacto=? WHERE id_usuario = ?";//$NON-NLS-1$

	private static final String ST_UDATE_PASSWD_BY_ID = "UPDATE tb_usuarios SET clave=? WHERE id_usuario = ?";//$NON-NLS-1$

	private static final String ST_REMOVE_USER = "DELETE FROM tb_usuarios WHERE id_usuario = ?"; //$NON-NLS-1$

	private static final String ST_INSERT_USER = "INSERT INTO tb_usuarios(nombre_usuario, clave, nombre, apellidos, correo_elec, fec_alta, telf_contacto, fk_rol) VALUES (?,?,?,?,?,?,?,?)"; //$NON-NLS-1$

	private static final String ST_SELECT_RESPONSIBLE_APPLICATION_INFO = "SELECT tb_app.id, tb_app.nombre, tb_app.fecha_alta, " //$NON-NLS-1$
			+ "tb_usu.id_usuario, tb_usu.nombre_usuario, tb_usu.nombre, tb_usu.apellidos, tb_usu.correo_elec, tb_usu.telf_contacto, tb_cert.id_certificado, tb_cert.nombre_cert, tb_cert.cert_principal, tb_cert.cert_backup, tb_usu.fk_rol, tb_usu.fec_alta, tb_usu.usu_defecto " //$NON-NLS-1$
			+ "FROM tb_certificados AS tb_cert, tb_aplicaciones AS tb_app, tb_usuarios AS tb_usu " //$NON-NLS-1$
			+ "WHERE tb_app.fk_responsable = tb_usu.id_usuario AND tb_app.fk_certificado = tb_cert.id_certificado AND tb_app.id = ?"; //$NON-NLS-1$


	private static final String ST_SELECT_EMAIL_BY_USERS = "SELECT tb_usu.id_usuario, tb_usu.nombre,tb_usu.nombre_usuario, tb_usu.correo_elec, tb_rol.permisos FROM tb_usuarios AS tb_usu, tb_roles AS tb_rol WHERE tb_usu.fk_rol = tb_rol.id AND (tb_usu.nombre_usuario = ? OR tb_usu.correo_elec = ?)"; //$NON-NLS-1$

	private static final String ST_UDATE_USER_BY_ID_MAIL = "UPDATE tb_usuarios SET  codigo_renovacion=?, fec_renovacion=?, rest_clave=FALSE WHERE id_usuario = ?";//$NON-NLS-1$

	private static final String ST_SELECT_USER_BY_ID_MAIL = "SELECT  codigo_renovacion FROM tb_usuarios WHERE id_usuario = ?";//$NON-NLS-1$

	private static final String ST_SELECT_RENOVATION_INFO_BY_CODE = "SELECT id_usuario,nombre_usuario, codigo_renovacion,fec_renovacion,rest_clave FROM tb_usuarios  WHERE codigo_renovacion = ?";//$NON-NLS-1$

	private static final String ST_UDATE_REST_CLAVE = "UPDATE tb_usuarios SET   rest_clave=? WHERE id_usuario = ?";//$NON-NLS-1$

	private static final String ST_UDATE_REST_VALUES = "UPDATE tb_usuarios SET clave=?, codigo_renovacion=NULL, fec_renovacion=NULL, rest_clave=FALSE WHERE id_usuario = ?";//$NON-NLS-1$


	/**
	 * Comprueba que una contrase&ntilde;a se corresponda pertenezca a un usuario.
	 * @param psswd Contrase&ntilde;a.
	 * @param user Usuario del que comprobar la contrase&ntilde;a
	 * @return {@code true} si la contrase&ntilde;a es del usuario, {@code false} en caso contrario.
	 * @throws SQLException Cuando ocurre un error al comprobar la contrase&tilde;a.
	 */
	public static boolean checkAdminPassword(final String psswd, final String user) throws SQLException {

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
		final String keyAdminB64 = getConfigValue(user);
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
	private static String getConfigValue(final String user) throws SQLException {
		String value = null;
		try {
			final PreparedStatement st = DbManager.prepareStatement(ST_SELECT_PASSWD_BY_NAME);
			st.setString(1, user);
			final ResultSet rs = st.executeQuery();
			if (rs.next()) {
				value = rs.getString(1);
			}

			st.close();
			rs.close();
		} catch (final Exception e) {
			LOGGER.info("Error al acceder a la base datos: " + e //$NON-NLS-1$
			);
			throw new SQLException(e);
		}
		return value;
	}

	/**
	 * Obtiene la informaci&oacute;n de un usuario a partir de su nombre.
	 * @param usrName Nombre del usuario.
	 * @return Datos del usuario o {@code null} si no se encontr&oacute; el usuario.
	 * @throws SQLException Cuando no se pueden recuperar los datos del usuario.
	 * SELECT id_usuario, nombre_usuario, clave,nombre, apellidos, correo_elec, telf_contacto, rol, fec_alta FROM tb_usuarios  WHERE nombre_usuario = ?"
	 */
	public static User getUserByName(final String usrName) throws SQLException {
		User usr = null;
		try {
			final PreparedStatement st = DbManager.prepareStatement(ST_SELECT_USER_BY_NAME);
			st.setString(1, usrName);
			final ResultSet rs = st.executeQuery();
			if (rs.next()) {
				usr = new User();
				usr.setId(rs.getString(1));
				usr.setUserName(rs.getString(2));
				usr.setpassword(rs.getString(3));
				usr.setName(rs.getString(4));
				usr.setSurname(rs.getString(5));
				if(rs.getString(6) != null && !"".equals(rs.getString(6))) { //$NON-NLS-1$
					usr.setMail(rs.getString(6));
				}

				if(rs.getString(7) != null && !"".equals(rs.getString(7))) { //$NON-NLS-1$
					usr.setTelephone(rs.getString(7));
				}
				usr.setRole(rs.getInt(8));
				usr.setStartDate(rs.getDate(9));
			}

			st.close();
			rs.close();
		} catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Error al recuperar la informacion de un usuario", e); //$NON-NLS-1$
			throw new SQLException(e);
		}

		// Si se recupero el usuario, le configuramos los permisos
		if (usr != null) {
			usr.setPermissions(RolesDAO.getPermissions(usr.getRole()));
		}

		return usr;
	}
	/**
	 * Obtiene un usuario a partir de su identificador.
	 * @param idUser Identificador del usuario.
	 * @return Datos del usuario.
	 */
	public static User getUser(final String idUser) {
		User usr = null;
		try {
			final PreparedStatement st = DbManager.prepareStatement(ST_SELECT_USER_BY_ID);
			st.setString(1, idUser);
			final ResultSet rs = st.executeQuery();
			if (rs.next()) {
				usr = new User();
				usr.setId(rs.getString(1));
				usr.setUserName(rs.getString(2));
				usr.setpassword(rs.getString(3));
				usr.setName(rs.getString(4));
				usr.setSurname(rs.getString(5));
				if(rs.getString(6) != null && !"".equals(rs.getString(6))) { //$NON-NLS-1$
					usr.setMail(rs.getString(6));
				}
				if(rs.getString(7) != null && !"".equals(rs.getString(7))) { //$NON-NLS-1$
					usr.setTelephone(rs.getString(7));
				}
				usr.setRole(rs.getInt(8));
				usr.setStartDate(rs.getDate(9));
				usr.setRoot(rs.getInt(10) > 0);
			}

			st.close();
			rs.close();
		} catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "No se pudo obtener la informacion del usuario de la base de datos", e); //$NON-NLS-1$
			usr = null;
		}
		return usr;
	}

	/**
	 * Obtiene una estructura JSON con el numero de usuarios.
	 * @return Estructura JSON.
	 */
	public static String getUsersCount() {
		int count = 0;
		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
		try {
			final PreparedStatement st = DbManager.prepareStatement(ST_SELECT_ALL_USERS_COUNT);
			final ResultSet rs = st.executeQuery();
			if(rs.next()) {
				count = rs.getInt(1);
				jsonObj.add("count", count);  //$NON-NLS-1$
			}
			rs.close();
			st.close();
		}
		catch(final Exception e){
			LOGGER.log(Level.SEVERE, "Error al leer los registros en la tabla de usuarios", e); //$NON-NLS-1$
		}

		final StringWriter writer = new StringWriter();
		try  {
			final JsonWriter jw = Json.createWriter(writer);
	        jw.writeObject(jsonObj.build());
	        jw.close();
	    }
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Error al componer la estructura JSON con el numero de usuarios", e); //$NON-NLS-1$
		}

	    return writer.toString();

	}

	/**
	 * Consulta que obtiene todos los registros de la tabla tb_usuarios
	 * @return Devuelve un String en formato JSON
	 * @throws SQLException
	 */
	public static String getUsersJSON(){

		final JsonObjectBuilder jsonObj= Json.createObjectBuilder();
		final JsonArrayBuilder data = Json.createArrayBuilder();
		try {
		final PreparedStatement st = DbManager.prepareStatement(ST_SELECT_ALL_USERS);
		final ResultSet rs = st.executeQuery();
		while (rs.next()) {

			Date date = null;
			final Timestamp timestamp = rs.getTimestamp(9);
			if (timestamp != null) {
				date = new Date(timestamp.getTime());
			}

			data.add(Json.createObjectBuilder()
					.add("id_usuario", rs.getString(1)) //$NON-NLS-1$
					.add("nombre_usuario", rs.getString(2)) //$NON-NLS-1$
					.add("clave", rs.getString(3)!= null && !"".equals(rs.getString(3)) ? rs.getString(3) : "") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					.add("nombre", rs.getString(4)) //$NON-NLS-1$
					.add("apellidos", rs.getString(5)) //$NON-NLS-1$
					.add("correo_elec", rs.getString(6) != null && !"".equals(rs.getString(6)) ? rs.getString(6) : "") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					.add("telf_contacto", rs.getString(7) != null && !"".equals(rs.getString(7)) ? rs.getString(7) : "") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					.add("nombre_rol", rs.getString(8)) //$NON-NLS-1$
					.add("fec_alta", date != null ? Utils.getStringDateFormat(date) : "") //$NON-NLS-1$ //$NON-NLS-2$
					.add("usu_defecto", rs.getString(10) !=null ? rs.getString(10) : "0") //$NON-NLS-1$ //$NON-NLS-2$
					);

		}
		rs.close();
		st.close();


	}
	catch(final Exception e){
		LOGGER.log(Level.SEVERE, "Error al leer los registros en la tabla de aplicaciones", e); //$NON-NLS-1$
	}
		jsonObj.add("UsrList", data); //$NON-NLS-1$
		final StringWriter writer = new StringWriter();
		try  {
			final JsonWriter jw = Json.createWriter(writer);
	        jw.writeObject(jsonObj.build());
			jw.close();
	    }
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Error al componer la estructura JSON con el listado de usuarios", e); //$NON-NLS-1$
		}
	    return writer.toString();

	}

	/**
	 * Consulta que obtiene todos los usuarios de forma paginada.
	 * @param start Elemento por el cual empezar a la p&aacute;gina.
	 * @param total N&uacute;mero de elementos de la p&aacute;gina.
	 * @return Estructura JSON.
	 */
	public static String getUsersPag(final String start, final String total) {

		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
		final JsonArrayBuilder data = Json.createArrayBuilder();

		try {
			final PreparedStatement st = DbManager.prepareStatement(ST_SELECT_ALL_USERS_PAG);
			st.setInt(1,Integer.parseInt(start));
			st.setInt(2, Integer.parseInt(total));
			final ResultSet rs = st.executeQuery();
			while (rs.next()) {

				Date date = null;
				final Timestamp timestamp = rs.getTimestamp(9);
				if (timestamp != null) {
					date = new Date(timestamp.getTime());
				}
				data.add(Json.createObjectBuilder()
						.add("id_usuario", rs.getString(1)) //$NON-NLS-1$
						.add("nombre_usuario", rs.getString(2)) //$NON-NLS-1$
						.add("clave", rs.getString(3)) //$NON-NLS-1$
						.add("nombre", rs.getString(4)) //$NON-NLS-1$
						.add("apellidos", rs.getString(5)) //$NON-NLS-1$
						.add("correo_elec", rs.getString(6) != null && !"".equals(rs.getString(6)) ? rs.getString(6) : "") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						.add("telf_contacto", rs.getString(7) != null && !"".equals(rs.getString(7)) ? rs.getString(7) : "") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						.add("fk_rol", rs.getString(8)) //$NON-NLS-1$
						.add("fec_alta", date != null ? Utils.getStringDateFormat(date) : "") //$NON-NLS-1$ //$NON-NLS-2$
						.add("usu_defecto", rs.getString(10) !=null ? rs.getString(10) : "0") //$NON-NLS-1$ //$NON-NLS-2$
						);

			}
			rs.close();
			st.close();
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "No se pudo obtener el listado paginado de usuarios de la base de datos", e); //$NON-NLS-1$
		}

		jsonObj.add("UsrList", data); //$NON-NLS-1$

		final StringWriter writer = new StringWriter();
		try  {
			final JsonWriter jw = Json.createWriter(writer);
	        jw.writeObject(jsonObj.build());
			jw.close();
	    }
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Error al componer la estructura JSON con el listado paginado de usuarios", e); //$NON-NLS-1$
		}
	    return writer.toString();
	}



	/**
	 * Cambia la contrase&ntilde;a de un usuario existente.
	 * @param idUser Identificador del usuario.
	 * @param userName Nombre de usuario.
	 * @param passwd Nueva contrase&ntilde;a.
	 * @throws SQLException
	 */
	public static void updateUserPasswd (final String idUser, final String userName, final String passwd) throws SQLException {
		final PreparedStatement st = DbManager.prepareStatement(ST_UDATE_PASSWD_BY_ID);

		st.setString(1, passwd);
		st.setString(2, idUser);

		LOGGER.info("Actualizamos el usuario '" + userName + "' con el ID: " + idUser); //$NON-NLS-1$ //$NON-NLS-2$

		st.execute();
		st.close();
	}
	/**
	 * Actualiza los datos de un usuario existente
	 * @param idUser Identificador del usuario.
	 * @param name Nombre de pila del usuario.
	 * @param surname Apellidos.
	 * @param email Correo electr&oacute;nico.
	 * @param telf Tel&eacute;fono.
	 * @param string
	 * @throws SQLException
	 */
	public static void updateUser (final String idUser, final String name, final String surname, final String email, final String telf, final String role) throws SQLException {
		final PreparedStatement st = DbManager.prepareStatement(ST_UDATE_USER_BY_ID);

		st.setString(1, name);
		st.setString(2, surname);
		st.setString(3, email);
		st.setString(4, role);
		st.setString(5, telf);
		st.setString(6, idUser);


		LOGGER.info("Actualizamos el usuario '" + name + " " + surname + "' con el ID: " + idUser); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		st.execute();
		st.close();
	}



	/**
	 * A&ntilde;ade un nuevo usuario al sistema.
	 * @param userName Nombre de usuario.
	 * @param passwd Contrase&ntilde;a.
	 * @param name Nombre de pila del usuario.
	 * @param surname Apellidos del usuario.
	 * @param email Correo eletr&oacute;nico.
	 * @param telf Tel&eacute;fono de contacto.
	 * @param role Rol del usuario.
	 * @throws SQLException Cuando no se puede agregar el usuario.
	 */
	public static void createUser(final String userName, final String passwd, final String name, final String surname, final String email, final String telf, final String role) throws SQLException {

		final PreparedStatement st = DbManager.prepareStatement(ST_INSERT_USER);

		st.setString(1, userName);
		st.setString(2, passwd);
		st.setString(3, name);
		st.setString(4, surname);
		st.setString(5, email);
		st.setDate(6, new Date(new java.util.Date().getTime()));
		st.setString(7, telf);
		st.setInt(8, Integer.parseInt(role));
		st.execute();
		LOGGER.info("Damos de alta el usuario '" + userName ); //$NON-NLS-1$

		st.close();
	}


	/**
	 * Borra un usuario del sistema
	 * @param idUser Identificador del usuario.
	 * @param username Nombre del usuario.
	 * @throws SQLException
	 */
	public static void removeUser(final String idUser, final String username) throws SQLException {

		final PreparedStatement st = DbManager.prepareStatement(ST_REMOVE_USER);
		st.setString(1, idUser);

		LOGGER.info("Damos de baja al usuario con el ID: " + LogUtils.cleanText(idUser) + " Nombre: " + LogUtils.cleanText(username)); //$NON-NLS-1$ //$NON-NLS-2$

		st.execute();
		st.close();
	}


	/**
	 * Recorremos todos los valores de los usuarios que existen en base de datos
	 * @param id del rol.
	 * @param uname Nombre del rol
	 * @param permisos del rol
	 * @throws SQLException
	 *
	 */
	public static User[] getUserAppResponsables() {

		final List<User> userList = new ArrayList<>();
		try {
			final PreparedStatement st = DbManager.prepareStatement(ST_SELECT_ALL_USERS_PERMISSIONS);
			final ResultSet rs = st.executeQuery();
			while (rs.next()) {

				final String permissions = rs.getString(4);
				final boolean hasPermission = RolePermissions.hasPermission(permissions, RolePermissions.PERMISION_APP_RESPONSABLE);

				if (hasPermission) {
					final User appResponsable = new User();
					appResponsable.setId(rs.getString(1));
					appResponsable.setName(rs.getString(2));
					appResponsable.setSurname(rs.getString(3));

					userList.add(appResponsable);
				}
			}
			st.close();
			rs.close();
		} catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "No se pudo obtener el listado de los usuarios de la base de datos", e); //$NON-NLS-1$
		}
		return userList.toArray(new User[userList.size()]);
	}

/**
 * Recupero toda la informacion que tiene un usuario ya sea la infromacion del usuario o la que esta relacionada con el
 * @param idUser Identificador del usuario.
 * @throws Exception
 */

	public static User getInfoUserAplication(final String idUser) throws Exception {

		User user = new User();
		try {
			final PreparedStatement st = DbManager.prepareStatement(ST_SELECT_RESPONSIBLE_APPLICATION_INFO);

			st.setString(1, idUser);
			final ResultSet rs = st.executeQuery();
			if (rs.next()) {

				final Application responsibleName = new Application();
				responsibleName.setId(rs.getString(1));
				responsibleName.setNombre(rs.getString(2));
				responsibleName.setAlta(rs.getDate(3));

				user.setId(rs.getString(4));
				user.setUserName(rs.getString(5));
				user.setName(rs.getString(6));
				user.setSurname(rs.getString(7));

				if(rs.getString(6) != null && !"".equals(rs.getString(8))) { //$NON-NLS-1$
					user.setMail(rs.getString(8));
				}
				if(rs.getString(7) != null && !"".equals(rs.getString(9))) { //$NON-NLS-1$
					user.setTelephone(rs.getString(9));
				}

				final CertificateFire certificate = new CertificateFire();
				certificate.setId(rs.getString(10));
				certificate.setNombre(rs.getString(11));

				X509Certificate certPrincipal = null;
				final String cerPrincipalString = rs.getString(12);
				if (cerPrincipalString != null) {
					final byte[] certPrincipalDecoded = Base64.decode(cerPrincipalString);
					certPrincipal = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate( //$NON-NLS-1$
							new ByteArrayInputStream(certPrincipalDecoded));
				}

				X509Certificate certBackup = null;
				final String certBackupB64 = rs.getString(13);
				if (certBackupB64 != null) {
					final byte[] certBackupDecoded = Base64.decode(certBackupB64);
					certBackup = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate( //$NON-NLS-1$
							new ByteArrayInputStream(certBackupDecoded));
				}

				certificate.setX509Principal(certPrincipal);
				certificate.setX509Backup(certBackup);

				user.setResponsibleName(responsibleName);
				user.setCertificate(certificate);


				user.setRole(rs.getInt(14));
				user.setStartDate(rs.getDate(15));
				user.setRoot(rs.getInt(16) > 0);

				}


			rs.close();
			st.close();
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "No se pudo leer la tabla con el listado de usuarios responsables", e); //$NON-NLS-1$
			user = null;
			throw e;
		}

		return user;

	}

	/**
	 * Recupero el correo de un usuario que solamente es administrador
	 * @param userOrMail
	 * @return Usuario con el login o correo indicado o {@code null} si no se encuentra.
	 * @throws SQLException
	 */
	public static User getUserInfoByMailOrLogin(final String userOrMail) throws SQLException {

		User usr = null;
		try {
			final PreparedStatement st = DbManager.prepareStatement(ST_SELECT_EMAIL_BY_USERS);
			st.setString(1, userOrMail);
			st.setString(2, userOrMail);
			final ResultSet rs = st.executeQuery();
			if (rs.next()) {
				usr = new User();

				usr.setId(rs.getString(1));
				usr.setName(rs.getString(2));
				usr.setUserName(rs.getString(3));

				if(rs.getString(4) != null && !"".equals(rs.getString(4))) { //$NON-NLS-1$
					usr.setMail(rs.getString(4));
				}
				final RolePermissions permissions = RolePermissions.getInstance(rs.getString(5));
				usr.setPermissions(permissions);
			}

			st.close();
			rs.close();
		} catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Error al recuperar el correo de un usuario", e); //$NON-NLS-1$
			throw new SQLException(e);
		}

		return usr;
	}


	/**
	 * Actualiza el c&oacute;digo y la fecha de un usuario existente
	 * @param idUser Identificador del usuario.
	 * @param code c&oacute;digo de renovaci&oacute;n.
	 * @param date
	 * @param string
	 * @throws SQLException
	 */
	public static void updateRenovationPasswordCode (final String idUser, final String code, final java.util.Date date) throws SQLException {
		final PreparedStatement st = DbManager.prepareStatement(ST_UDATE_USER_BY_ID_MAIL);

		st.setString(1, code);
		st.setTimestamp(2, new Timestamp(date.getTime()));


		st.setString(3, idUser);


		LOGGER.info("Actualizamos el usuario con el ID: " + idUser); //$NON-NLS-1$

		st.execute();
		st.close();
	}

	/**
	 * Obtiene un usuario a partir de su identificador.
	 * @param idUser Identificador del usuario.
	 * @return Datos del usuario.
	 * @throws SQLException
	 */
	public static UserRestorationInfo getRenovationInfo(final String code) throws SQLException {


		UserRestorationInfo usr = null;
			try {
				final PreparedStatement st = DbManager.prepareStatement(ST_SELECT_RENOVATION_INFO_BY_CODE);
				st.setString(1, code);
				final ResultSet rs = st.executeQuery();
				if (rs.next()) {
					usr = new UserRestorationInfo();
					usr.setId(rs.getString(1));
					usr.setName(rs.getString(2));
					usr.setCodeInfo(rs.getString(3));

					final java.sql.Timestamp sqlTimestamp = rs.getTimestamp(4);
					if (sqlTimestamp != null) {
						usr.setRenovationDate(new Date(sqlTimestamp.getTime()));
					}
					usr.setRestoreExpired(rs.getBoolean(5));
				}

				st.close();
				rs.close();
			} catch (final Exception e) {
				LOGGER.log(Level.SEVERE, "No se pudo obtener la informacion del usuario de la base de datos", e); //$NON-NLS-1$
				throw new SQLException(e);
			}
			return usr;

	}

	/**
	 * Actualiza el la clave de expiraci&oacute;n que se ha enviado al mail del usuario
	 * @param idUser Identificador del usuario.
	 * @param clave url enviada para que expire.
	 * @throws SQLException
	 */
	public static void updateRestorePasswordAuthoritation (final String idUser, final boolean clave) throws SQLException {

		LOGGER.info("Actualizamos el usuario con el ID: " + idUser); //$NON-NLS-1$

		final PreparedStatement st = DbManager.prepareStatement(ST_UDATE_REST_CLAVE);
		st.setBoolean(1, clave);
		st.setString(2, idUser);
		st.execute();
		st.close();
	}


	/**
	 * Actualiza el la clave de expiraci&oacute;n que se ha enviado al mail del usuario
	 * @param idUser Identificador del usuario.
	 * @param clave url enviada para que expire.
	 * @throws SQLException
	 */
	public static void updateRestoreValues (final String idUser,final String password) throws SQLException {
		final PreparedStatement st = DbManager.prepareStatement(ST_UDATE_REST_VALUES);

		st.setString(1, password);
		st.setString(2, idUser);

		LOGGER.info("Reseteamos la contrasena del usuario con el ID: " + idUser); //$NON-NLS-1$

		st.execute();
		st.close();
	}
	/**
	 * Obtiene la informaci&oacute;n de un usuario a partir de su nombre.
	 * @param usrName Nombre del usuario.
	 * @return Datos del usuario o {@code null} si no se encontr&oacute; el usuario.
	 * @throws SQLException Cuando no se pueden recuperar los datos del usuario.
	 * SELECT id_usuario, nombre_usuario, clave,nombre, apellidos, correo_elec, telf_contacto, rol, fec_alta FROM tb_usuarios  WHERE nombre_usuario = ?"
	 */
	public static User getUserByMail(final String mail) throws SQLException {
		User usr = null;
		try {
			final PreparedStatement st = DbManager.prepareStatement(ST_SELECT_USER_BY_MAIL);
			st.setString(1, mail);
			final ResultSet rs = st.executeQuery();
			if (rs.next()) {
				usr = new User();
				usr.setId(rs.getString(1));
				usr.setUserName(rs.getString(2));
				usr.setpassword(rs.getString(3));
				usr.setName(rs.getString(4));
				usr.setSurname(rs.getString(5));
				if(rs.getString(6) != null && !"".equals(rs.getString(6))) { //$NON-NLS-1$
					usr.setMail(rs.getString(6));
				}

				if(rs.getString(7) != null && !"".equals(rs.getString(7))) { //$NON-NLS-1$
					usr.setTelephone(rs.getString(7));
				}
				usr.setRole(rs.getInt(8));
				usr.setStartDate(rs.getDate(9));
			}

			st.close();
			rs.close();
		} catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Error al recuperar la informacion de un usuario", e); //$NON-NLS-1$
			throw new SQLException(e);
		}

		// Si se recupero el usuario, le configuramos los permisos
		if (usr != null) {
			usr.setPermissions(RolesDAO.getPermissions(usr.getRole()));
		}

		return usr;
	}

}
