/*
/*******************************************************************************
 * Copyright (C) 2018 MINHAFP, Gobierno de Espa&ntilde;a
 * This program is licensed and may be used, modified and redistributed under the  terms
 * of the European Public License (EUPL), either version 1.1 or (at your option)
 * any later version as soon as they are approved by the European Commission.
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and
 * more details.
 * You should have received a copy of the EUPL1.1 license
 * along with this program; if not, you may find it at
 * http:joinup.ec.europa.eu/software/page/eupl/licence-eupl
 ******************************************************************************/

/**
 * <b>File:</b><p>es.gob.fire.web.controller.UserController.java.</p>
 * <b>Description:</b><p> .</p>
 * <b>Project:</b><p>Application for signing documents of @firma suite systems</p>
 * <b>Date:</b><p>21/06/2020.</p>
 * @author Gobierno de Espa&ntilde;a.
 * @version 1.1, 21/05/2021.
 */
package es.gob.fire.web.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
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

/**
 * <p>Class that manages the requests related to the Users administration.</p>
 * <b>Project:</b><p>Application for signing documents of @firma suite systems.</p>
 * @version 1.1, 21/05/2021.
 */
@Controller
public class UserController {

	/**
	 * Attribute that represents the service object for accessing the
	 * repository.
	 */
	@Autowired
	private IUserService userService;
	
	@Autowired
    private MessageSource messageSource;

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
	@RequestMapping(value = "adduser", method = RequestMethod.GET)
	public String addUser(final Model model, Locale locale) {

		model.addAttribute("listRoles", loadRoles(locale));
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

		userFormPassword.setIdUser(user.getUserId());

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
	public String menuEdit(@RequestParam("username") final String username, final Model model, Locale locale) {
		 final User user = this.userService.getUserByUserName(username);
		 final UserEditDTO userformedit = new UserEditDTO();

		userformedit.setIdUserFireEdit(user.getUserId());
		userformedit.setNameEdit(user.getName());
		userformedit.setSurnamesEdit(user.getSurnames());
		userformedit.setEmailEdit(user.getEmail());
		userformedit.setUsernameEdit(user.getUserName());
		userformedit.setRolId(user.getRol().getRolId());
		userformedit.setTelfEdit(user.getPhone());

		model.addAttribute("listRoles", loadRoles(locale));
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
	 * @return List of es.gob.fire.constants that represents the different association types.
	 */
	private List<RolDTO> loadRoles(Locale locale) {
		final List<RolDTO> listRoles = new ArrayList<>();
		// obtenemos los tipos de planificadores.
		final List<Rol> listRol = this.userService.getAllRol();
		for (final Rol rol: listRol) {
			String rolProperty = messageSource.getMessage("form.user.rol." + rol.getRolName(), null, locale);
			final RolDTO item = new RolDTO(rol.getRolId(), rolProperty, rol.getPermissions());
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
	
	@RequestMapping(value = "menuDeleteUser")
	public String loadConfirmDeleteUser(@RequestParam("username") final String username, Long index, final Model model, Locale locale) {
		User user = this.userService.getUserByUserName(username);
		model.addAttribute("userDeleteForm", user);
		model.addAttribute("tableIndexRow", index);
		return "modal/userDelete.html";
	}

}