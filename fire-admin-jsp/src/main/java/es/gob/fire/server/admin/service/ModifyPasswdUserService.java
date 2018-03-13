package es.gob.fire.server.admin.service;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import es.gob.fire.server.admin.dao.UsersDAO;
import es.gob.fire.server.admin.entity.User;
import es.gob.fire.server.admin.tool.Base64;

/**
 * Servlet implementation class ModifyUserService
 */
@WebServlet("/modifyPasswdUser")
public class ModifyPasswdUserService extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = Logger.getLogger(NewUserService.class.getName());

	private static final String PARAM_NAME = "nombre-usr"; //$NON-NLS-1$
	private static final String PARAM_OLD_PASSWD = "old_passwd-usr"; //$NON-NLS-1$
	private static final String PARAM_NEW_PASSWD = "passwd-usr_1"; //$NON-NLS-1$
	private static final String PARAM_NEW_PASSWD_COPY = "passwd-usr_2"; //$NON-NLS-1$
	private static final String PARAM_OP = "op"; //$NON-NLS-1$
	private static final String PARAM_IDUSER = "id-usr"; //$NON-NLS-1$
	private static final String SHA_2="SHA-256"; //$NON-NLS-1$

	private String idUser=null;
	private String user_name=null;
	private String old_password=null;
	private String new_password=null;
	private String new_passwdCopy=null;


    /**
     * @see HttpServlet#HttpServlet()
     */
    public ModifyPasswdUserService() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		//Obtener el tipo de operaci�n 2-Edici�n, 3 Cambio de contrase�a
		final int op = Integer.parseInt(req.getParameter(PARAM_OP));
		final String stringOp = op == 3 ? "clave" : "" ;  //$NON-NLS-1$ //$NON-NLS-2$
		/*Obtenemos los par�metros enviados del formulario */
		this.getParameters(req);
		boolean isOk = true;
		User usr;
		try
		{
			usr= this.getIdUser()!=null?UsersDAO.getUser(this.getIdUser()):new User();


			if(this.getUser_name()==null || this.getOld_password()==null ||
					this.getNew_password()==null && this.getNew_passwdCopy()==null) {
				LOGGER.log(Level.SEVERE, "No se han proporcionado todos los datos requeridos para cambiar la clave (Nombre de usuario , clave antig�a, Clave nueva y repetir clave nueva)"); //$NON-NLS-1$
				isOk = false;
			}
			else if(this.getNew_password()!=null && this.getNew_passwdCopy()!=null
						&& !this.getNew_password().equals(this.getNew_passwdCopy())) {
				LOGGER.log(Level.SEVERE, "Clave nueva y repetir clave nueva, deben ser iguales"); //$NON-NLS-1$
				isOk = false;
			}
			//Comprobamos que la clave introducida como antig�a es la que tiene guardada en la base de datos
			else if(usr.getClave()!=null && this.getOld_password()!=null && !"".equals(this.getOld_password())) { //$NON-NLS-1$
				final MessageDigest md_oldPass = MessageDigest.getInstance(SHA_2);
				byte[] digest_oldPass;
				String oldPasswd =null;
				md_oldPass.update(this.getOld_password().getBytes());
				digest_oldPass = md_oldPass.digest();
				oldPasswd = Base64.encode(digest_oldPass);
				if(!oldPasswd.equals(usr.getClave())) {
					LOGGER.log(Level.SEVERE, "La clave antig�a introducida es erronea."); //$NON-NLS-1$
					isOk = false;
				}
				else {

					try {

						if(op==3) {
							final MessageDigest md = MessageDigest.getInstance(SHA_2);
							byte[] digest;
							String clave =null;

							//Obtenemos la clave codificada
							if(this.getNew_password()!=null && !"".equals(this.getNew_password())) { //$NON-NLS-1$
								md.update(this.getNew_password().getBytes());
								digest = md.digest();
								clave = Base64.encode(digest);
							}

							LOGGER.info("Cambio de clave del usuario con nombre: " + this.getUser_name()); //$NON-NLS-1$
							try {
								UsersDAO.updateUserPasswd(this.getIdUser() , this.getUser_name(),clave);
							}
							catch (final SQLException e) {
								LOGGER.log(Level.SEVERE, "Error en la modificaci�n de la contrase�a del usuario con nombre:" + usr.getNombre_usuario(), e); //$NON-NLS-1$
								isOk = false;
							}

						}
					}
					catch (final NoSuchAlgorithmException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			else {
				throw new IllegalStateException("Estado no permitido");//$NON-NLS-1$
			}
			resp.sendRedirect("User/UserPage.jsp?op=" + stringOp + "&r=" + (isOk ? "1" : "0")+"&ent=user"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		}
		catch (final IllegalArgumentException e){
			LOGGER.log(Level.SEVERE,"Ha ocurrido un error con el base64 : " + e, e); //$NON-NLS-1$
			resp.sendRedirect("./User/UserPage.jsp?error=true&name=" + this.getUser_name() ); //$NON-NLS-1$
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE,"Ha ocurrido un error cambiar la clave : " + e, e); //$NON-NLS-1$
			resp.sendRedirect("./User/UserPage.jsp?op=clave&r=0&ent=user"); //$NON-NLS-1$
		}

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

	/**
	 * Obtiene los par�metros pasados
	 * @param req
	 * @throws IOException
	 * @throws ServletException
	 */
	private void getParameters(final HttpServletRequest req) throws IOException, ServletException {

		this.setIdUser(null);
		this.setUser_name(null);
		this.setNew_password(null);
		this.setOld_password(null);
		this.setNew_passwdCopy(null);

		if(req.getParameter(PARAM_IDUSER)!=null && !"".equals(req.getParameter(PARAM_IDUSER))) { //$NON-NLS-1$
			this.setIdUser(req.getParameter(PARAM_IDUSER));
		}
		if(req.getParameter(PARAM_NAME)!=null && !"".equals(req.getParameter(PARAM_NAME))) { //$NON-NLS-1$
			this.setUser_name(req.getParameter(PARAM_NAME));
		}
		if(req.getParameter(PARAM_NEW_PASSWD)!=null && !"".equals(req.getParameter(PARAM_NEW_PASSWD))) { //$NON-NLS-1$
			this.setNew_password(req.getParameter(PARAM_NEW_PASSWD));
		}
		if(req.getParameter(PARAM_OLD_PASSWD)!=null && !"".equals(req.getParameter(PARAM_OLD_PASSWD))) { //$NON-NLS-1$
			this.setOld_password(req.getParameter(PARAM_OLD_PASSWD));
		}
		if(req.getParameter(PARAM_NEW_PASSWD_COPY)!=null && !"".equals(req.getParameter(PARAM_NEW_PASSWD_COPY))) { //$NON-NLS-1$
			this.setNew_passwdCopy(req.getParameter(PARAM_NEW_PASSWD_COPY));
		}

	}


	/**
	 * Obtiene el nombre del usuario
	 * @return
	 */
	private final String getUser_name() {
		return this.user_name;
	}
	/**
	 * Establece el nombre del usuario
	 * @param user_name
	 */
	private final void setUser_name(final String user_name) {
		this.user_name = user_name;
	}
	/**
	 * Obtiene la clave antig�a
	 * @return
	 */
	private final String getOld_password() {
		return this.old_password;
	}
	/**
	 *
	 * @param old_password
	 */
	private final void setOld_password(final String old_password) {
		this.old_password = old_password;
	}

	private final String getNew_password() {
		return this.new_password;
	}

	private final void setNew_password(final String new_password) {
		this.new_password = new_password;
	}

	private final String getNew_passwdCopy() {
		return this.new_passwdCopy;
	}

	private final void setNew_passwdCopy(final String new_passwdCopy) {
		this.new_passwdCopy = new_passwdCopy;
	}

	private final String getIdUser() {
		return this.idUser;
	}

	private final void setIdUser(final String idUser) {
		this.idUser = idUser;
	}



}
