package es.gob.fire.web.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import es.gob.fire.persistence.dto.RolDTO;
import es.gob.fire.persistence.dto.UserDTO;
import es.gob.fire.persistence.dto.UserEditDTO;
import es.gob.fire.persistence.dto.UserPasswordDTO;
import es.gob.fire.persistence.entity.Rol;
import es.gob.fire.persistence.entity.User;
import es.gob.fire.persistence.service.IUserService;
import es.gob.fire.persistence.service.ManagerPersistenceServices;

/**
 * <p>
 * Class that manages the requests related to the Users administration.
 * </p>
 * <b>Project:</b>
 * <p>
 * Application for monitoring services of @firma suite systems.
 * </p>
 *
 * @version 1.2, 28/10/2018.
 */
@Controller
public class UserController {

	/**
	 * Attribute that represents the service object for accessing the
	 * repository.
	 */
	@Autowired
	private IUserService userService;

	/**
	 * Method that maps the list users web requests to the controller and
	 * forwards the list of users to the view.
	 *
	 * @param model
	 *            Holder object for model attributes.
	 * @return String that represents the name of the view to forward.
	 */
	@RequestMapping(value = "useradmin")
	public String index(final Model model) {
		model.addAttribute("userFormPassword", new UserPasswordDTO());
		model.addAttribute("userformEdit", new UserEditDTO());
		return "fragments/useradmin.html";
	}

	/**
	 * Method that maps the add user web request to the controller and sets the
	 * backing form.
	 *
	 * @param model
	 *            Holder object for model attributes.
	 * @return String that represents the name of the view to forward.
	 */
	@RequestMapping(value = "adduser", method = RequestMethod.POST)
	public String addUser(final Model model) {
		
		model.addAttribute("listRoles", loadRoles());
		model.addAttribute("userform", new UserDTO());
		model.addAttribute("accion", "add");
		return "modal/userForm";
	}

	/**
	 * Method that opens the modal form password.
	 * @param username String that represents the user's name
	 * @param model view Model object
	 * @return String that represents the navigation HTML fragment
	 */
	@RequestMapping(value = "menupass")
	public String menuPass(@RequestParam("username") final String username, final Model model) {
		final User user = this.userService.getUserByUserName(username);
		final UserPasswordDTO userFormPassword = new UserPasswordDTO();

		userFormPassword.setIdUserFirePass(user.getUserId());

		model.addAttribute("userFormPassword", userFormPassword);
		return "modal/userFormPass.html";
	}

	/**
	 * Method that opens the modal form user edit.
	 * @param username String that represents the user's name
	 * @param model view Model object
	 * @return String that represents the navigation HTML fragment
	 */
	@RequestMapping(value = "menuedit")
	public String menuEdit(@RequestParam("username") final String username, final Model model) {
		 User user = this.userService.getUserByUserName(username);
		 UserEditDTO userformedit = new UserEditDTO();

		userformedit.setIdUserFireEdit(user.getUserId());
		userformedit.setNameEdit(user.getName());
		userformedit.setSurnamesEdit(user.getSurnames());
		userformedit.setEmailEdit(user.getEmail());
		userformedit.setUsernameEdit(user.getUserName());
		userformedit.setRolId(user.getRol().getRolId());
		//userformedit.setTelfEdit(user.getTelfEdit());

		model.addAttribute("listRoles", loadRoles());
		model.addAttribute("userformedit", userformedit);
		return "modal/userFormEdit.html";
	}

	/**
	 * Method that maps the add user certificate web requests to the controller and forwards to the form
	 * to the view.
	 * @param model Holder object for model attributes.
	 * @param idUser Identifier for the idUser
	 * @return String that represents the name of the view to forward.
	 */
	@RequestMapping(value = "/managecertuser/{idUser}")
	public String manageCertUser(final Model model, @PathVariable("idUser") final Long idUser) {
		model.addAttribute("idUser", idUser);
		return "modal/certUser.html";
	}

	/**
	 * Method that loads association types.
	 * @return List of constants that represents the different association types.
	 */
	private List<RolDTO> loadRoles() {
		List<RolDTO> listRoles = new ArrayList<RolDTO>();
		// obtenemos los tipos de planificadores.
		IUserService userService = ManagerPersistenceServices.getInstance().getManagerPersistenceConfigurationServices().getUserFireService();
		List<Rol> listRol = userService.getAllRol();
		for (Rol rol: listRol) {
			RolDTO item = new RolDTO(rol.getRolId(), rol.getRolName(), rol.getPermissions());
			listRoles.add(item);
			
		}

		return listRoles;
	}

	/**
	 * Get userService.
	 * @return userService
	 */
	public IUserService getUserService() {
		return this.userService;
	}

	/**
	 * SetuserService.
	 * @param userServiceP set userService
	 */
	public void setUserService(final IUserService userServiceP) {
		this.userService = userServiceP;
	}

}