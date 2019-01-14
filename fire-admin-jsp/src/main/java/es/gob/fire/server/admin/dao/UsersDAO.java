package es.gob.fire.server.admin.dao;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;

import es.gob.fire.server.admin.conf.DbManager;
import es.gob.fire.server.admin.entity.User;
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

	private static final String ST_SELECT_USER_BY_ID = "SELECT id_usuario, nombre_usuario, clave,nombre, apellidos, correo_elec, telf_contacto, rol, fec_alta, usu_defecto FROM tb_usuarios  WHERE id_usuario = ?";//$NON-NLS-1$

	private static final String ST_SELECT_USER_BY_NAME = "SELECT id_usuario, nombre_usuario, clave,nombre, apellidos, correo_elec, telf_contacto, rol, fec_alta, usu_defecto  FROM tb_usuarios  WHERE nombre_usuario = ?";//$NON-NLS-1$

	private static final String ST_SELECT_ALL_USERS = "SELECT id_usuario, nombre_usuario, clave,nombre, apellidos, correo_elec, telf_contacto, rol, fec_alta, usu_defecto  FROM tb_usuarios ORDER BY id_usuario";//$NON-NLS-1$

	private static final String ST_SELECT_ALL_USERS_COUNT = "SELECT count(*) FROM tb_usuarios"; //$NON-NLS-1$

	private static final String ST_SELECT_ALL_USERS_PAG = "SELECT id_usuario, nombre_usuario, clave,nombre, apellidos, correo_elec, telf_contacto, rol, fec_alta, usu_defecto FROM tb_usuarios ORDER BY id_usuario limit ?,?";//$NON-NLS-1$

	private static final String ST_UDATE_USER_BY_ID = "UPDATE tb_usuarios SET  nombre=?, apellidos=?, correo_elec=?, telf_contacto=? WHERE id_usuario = ?";//$NON-NLS-1$

	private static final String ST_UDATE_PASSWD_BY_ID = "UPDATE tb_usuarios SET nombre_usuario=?, clave=? WHERE id_usuario = ?";//$NON-NLS-1$

	private static final String ST_REMOVE_USER = "DELETE FROM tb_usuarios WHERE id_usuario = ?"; //$NON-NLS-1$

	private static final String ST_INSERT_USER = "INSERT INTO tb_usuarios(nombre_usuario, clave, nombre, apellidos, correo_elec, fec_alta, telf_contacto) VALUES (?,?,?,?,?,?,?)"; //$NON-NLS-1$
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
	 * @return Datos del usuario.
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
				usr.setNombreUsuario(rs.getString(2));
				usr.setClave(rs.getString(3));
				usr.setNombre(rs.getString(4));
				usr.setApellidos(rs.getString(5));
				if(rs.getString(6) != null && !"".equals(rs.getString(6))) { //$NON-NLS-1$
					usr.setCorreo_elec(rs.getString(6));
				}

				if(rs.getString(7) != null && !"".equals(rs.getString(7))) { //$NON-NLS-1$
					usr.setTelfefono(rs.getString(7));
				}
				usr.setRol(rs.getString(8));
				usr.setFechaAlta(rs.getDate(9));
			}

			st.close();
			rs.close();
		} catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Error al recuperar la informacion de un usuario", e); //$NON-NLS-1$
			throw new SQLException(e);
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
				usr.setNombreUsuario(rs.getString(2));
				usr.setClave(rs.getString(3));
				usr.setNombre(rs.getString(4));
				usr.setApellidos(rs.getString(5));
				if(rs.getString(6) != null && !"".equals(rs.getString(6))) { //$NON-NLS-1$
					usr.setCorreo_elec(rs.getString(6));
				}
				if(rs.getString(7) != null && !"".equals(rs.getString(7))) { //$NON-NLS-1$
					usr.setTelfefono(rs.getString(7));
				}
				usr.setRol(rs.getString(8));
				usr.setFechaAlta(rs.getDate(9));
				usr.setPorDefecto(String.valueOf(rs.getInt(10)));
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
	public static String getUsersJSON() throws SQLException {

		final JsonObjectBuilder jsonObj= Json.createObjectBuilder();
		final JsonArrayBuilder data = Json.createArrayBuilder();

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
					.add("clave", rs.getString(3)) //$NON-NLS-1$
					.add("nombre", rs.getString(4)) //$NON-NLS-1$
					.add("apellidos", rs.getString(5)) //$NON-NLS-1$
					.add("correo_elec", rs.getString(6) != null && !"".equals(rs.getString(6)) ? rs.getString(6) : "") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					.add("telf_contacto", rs.getString(7) != null && !"".equals(rs.getString(7)) ? rs.getString(7) : "") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					.add("rol", rs.getString(8)) //$NON-NLS-1$
					.add("fec_alta", date != null ? Utils.getStringDateFormat(date) : "") //$NON-NLS-1$ //$NON-NLS-2$
					.add("usu_defecto", rs.getString(10) !=null ? rs.getString(10) : "0") //$NON-NLS-1$ //$NON-NLS-2$
					);

		}
		rs.close();
		st.close();
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
						.add("rol", rs.getString(8)) //$NON-NLS-1$
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

		st.setString(1, userName);
		st.setString(2, passwd);
		st.setString(3, idUser);

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
	 * @throws SQLException
	 */
	public static void updateUser (final String idUser, final String name, final String surname, final String email, final String telf) throws SQLException {
		final PreparedStatement st = DbManager.prepareStatement(ST_UDATE_USER_BY_ID);

		st.setString(1, name);
		st.setString(2, surname);
		st.setString(3, email);
		st.setString(4, telf);
		st.setString(5, idUser);

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
	 * @throws SQLException Cuando no se puede agregar el usuario.
	 */
	public static void createUser(final String userName, final String passwd, final String name, final String surname, final String email, final String telf) throws SQLException {

		final PreparedStatement st = DbManager.prepareStatement(ST_INSERT_USER);

		st.setString(1, userName);
		st.setString(2, passwd);
		st.setString(3, name);
		st.setString(4, surname);
		st.setString(5, email);
		st.setDate(6, new Date(new java.util.Date().getTime()));
		st.setString(7, telf);
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

		LOGGER.info("Damos de baja al usuario con el ID: " + idUser + " Nombre: " + username); //$NON-NLS-1$ //$NON-NLS-2$

		st.execute();
		st.close();
	}
}
