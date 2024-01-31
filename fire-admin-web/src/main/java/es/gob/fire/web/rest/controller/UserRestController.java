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
 * <b>File:</b><p>es.gob.fire.web.rest.controller.UserRestController.java.</p>
 * <b>Description:</b><p> .</p>
 * <b>Project:</b><p>Application for signing documents of @firma suite systems</p>
 * <b>Date:</b><p>21/06/2020.</p>
 * @author Gobierno de Espa&ntilde;a.
 * @version 1.2, 02/02/2022.
 */
package es.gob.fire.web.rest.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.servlet.ServletContext;
import javax.validation.constraints.NotEmpty;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

import es.gob.fire.commons.log.Logger;
import es.gob.fire.commons.utils.Utils;
import es.gob.fire.commons.utils.UtilsStringChar;
import es.gob.fire.i18n.IWebLogMessages;
import es.gob.fire.i18n.Language;
import es.gob.fire.persistence.dto.UserDTO;
import es.gob.fire.persistence.dto.UserEditDTO;
import es.gob.fire.persistence.dto.UserPasswordDTO;
import es.gob.fire.persistence.dto.validation.OrderedValidation;
import es.gob.fire.persistence.entity.ApplicationResponsible;
import es.gob.fire.persistence.entity.Rol;
import es.gob.fire.persistence.entity.User;
import es.gob.fire.persistence.permissions.Permissions;
import es.gob.fire.persistence.permissions.PermissionsChecker;
import es.gob.fire.persistence.service.IApplicationService;
import es.gob.fire.persistence.service.IUserService;

/**
 * <p>Class that manages the REST requests related to the Users administration and JSON communication.</p>
 * <b>Project:</b><p>Application for signing documents of @firma suite systems.</p>
 * @version 1.2, 02/02/2022.
 */
@RestController
public class UserRestController {

	/**
	 * Attribute that represents the object that manages the log of the class.
	 */
	private static final Logger LOGGER = Logger.getLogger(UserRestController.class);

	/**
	 * Attribute that represents the span text.
	 */
	private static final String SPAN = "_span";

	/**
	 * Constant that represents the key Json 'errorSaveUser'.
	 */
	private static final String KEY_JS_ERROR_SAVE_USER = "errorSaveUser";

	/**
	 * Constant that represents the value of the main admin user.
	 */
	private static final String LOGIN_ADMIN_USER = "admin";

	/**
	 * Attribute that represents the service object for accessing the
	 * repository.
	 */
	@Autowired
	private IUserService userService;

	/**
	 * Attribute that represents the service object for accessing the
	 * repository.
	 */
	@Autowired
	private IApplicationService appService;

	/**
	 * Attribute that represents the context object.
	 */
	@Autowired
	private ServletContext context;

	/**
	 * Method that maps the list users web requests to the controller and
	 * forwards the list of users to the view.
	 *
	 * @param input
	 *            Holder object for datatable attributes.
	 * @return String that represents the name of the view to forward.
	 */
	@JsonView(DataTablesOutput.View.class)
	@RequestMapping(path = "/usersdatatable", method = RequestMethod.GET)
	public DataTablesOutput<User> users(@NotEmpty final DataTablesInput input) {
		return this.userService.getAllUser(input);
	}

	/**
	 * Method that maps the delete user request from datatable to the controller
	 * and performs the delete of the user identified by its id.
	 *
	 * @param userId
	 *            Identifier of the user to be deleted.
	 * @param index
	 *            Row index of the datatable.
	 * @return String that represents the name of the view to redirect.
	 */
	@RequestMapping(path = "/deleteuser", method = RequestMethod.POST)
	public String deleteUser(@RequestParam("id") final Long userId, @RequestParam("index") final String index) {

		String result = index;

		final User user = this.userService.getUserByUserId(userId);

		if (user.getRoot() == Boolean.TRUE) {
			result = "error.No se puede eliminar al administrador principal.";
		}
		else {
			final List<ApplicationResponsible> responsables = this.appService.getApplicationResponsibleByUserId(userId);

			if (responsables == null || responsables.size() > 0) {
				result = "error.No se ha podido borrar el usuario, tiene aplicaciones asociadas.";
			}
			else {
				this.userService.deleteUser(userId);
			}
		}
		return result;
	}

	/**
	 * Method that maps the save user web request to the controller and saves it
	 * in the persistence.
	 *
	 * @param userForm
	 *            Object that represents the backing user form.
	 * @param bindingResult
	 *            Object that represents the form validation result.
	 * @return {@link DataTablesOutput<User>}
	 */
	@RequestMapping(value = "/saveuser", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	@JsonView(DataTablesOutput.View.class)
	public @ResponseBody DataTablesOutput<User> save(@Validated(OrderedValidation.class) @RequestBody final UserDTO userForm, final BindingResult bindingResult) {
		final DataTablesOutput<User> dtOutput = new DataTablesOutput<>();
		List<User> listNewUser = new ArrayList<>();
		final JSONObject json = new JSONObject();


		boolean error = false;

		if (bindingResult.hasErrors()) {
			error = true;

			for (final FieldError o: bindingResult.getFieldErrors()) {
				json.put(o.getField() + SPAN, o.getDefaultMessage());
			}
		}

		final Rol rol = this.userService.getRol(userForm.getRolId());
		final boolean hasAccessPermission = PermissionsChecker.hasPermission(rol, Permissions.ACCESS);

		// Si el usuario debe tener acceso a la administracion, entonces es obligatorio que tenga contrasena
		if (hasAccessPermission && emptyAdminPassword(userForm.getPasswordAdd())) {
			error = true;
			json.put("passwordAdd" + SPAN, "El campo contrase\u00F1a es obligatorio.");

		}

		if (hasAccessPermission && !matchingConfirmPassword(userForm.getPasswordAdd(), userForm.getConfirmPasswordAdd())) {
			error = true;
			json.put("passwordAdd" + SPAN, "Los campos de contrase\u00F1a deben coincidir.");
			json.put("confirmPasswordAdd" + SPAN, "Los campos de contrase\u00F1a deben coincidir.");

		}

		if (this.userService.getUserByUserName(userForm.getLoginAdd()) != null) {
			error = true;
			json.put("loginAdd" + SPAN, "Ya existe un usuario con el login seleccionado.");

		}

		if (userForm.getEmailAdd() != null && !userForm.getEmailAdd().isEmpty() && !Utils.isValidEmail(userForm.getEmailAdd())) {
			error = true;
			json.put("emailAdd" + SPAN, "El campo email no es v\u00E1lido.");
		}

		if (this.userService.getUserByEmail(userForm.getEmailAdd()) != null) {
			error = true;
			json.put("emailAdd" + SPAN, "Ya existe un usuario con el correo seleccionado.");
		}

		if (!error) {
			try {

				final User user = this.userService.saveUser(userForm);

				listNewUser.add(user);
			} catch (final Exception e) {
				LOGGER.error(Language.getResWebFire(IWebLogMessages.ERRORWEB022), e);
				listNewUser = StreamSupport.stream(this.userService.getAllUser().spliterator(), false).collect(Collectors.toList());
				json.put(KEY_JS_ERROR_SAVE_USER, Language.getResWebFire(IWebLogMessages.ERRORWEB022));
				dtOutput.setError(json.toString());
			}
		} else {
			listNewUser = StreamSupport.stream(this.userService.getAllUser().spliterator(), false).collect(Collectors.toList());
			dtOutput.setError(json.toString());
		}

		dtOutput.setData(listNewUser);

		return dtOutput;

	}


	/**
	 * Method that checks if the password field is empty
	 * @param userForm
	 * @return
	 */
	private static boolean emptyAdminPassword(final String password) {
		return password == null || password.isEmpty();
	}

	/**
	 * Method that checks if a user had the access permission.
	 * @param userId User id.
	 * @return
	 */
	private boolean hadAccessBeforeEdit(final Long userId) {

		final User user = this.userService.getUserByUserId(userId);

		return PermissionsChecker.hasPermission(user, Permissions.ACCESS);
	}

	/**
	 * Method that updates a user.
	 * @param userForm UserForm
	 * @param bindingResult  BindingResult
	 * @return DataTablesOutput<User> users
	 */
	@RequestMapping(value = "/saveuseredit", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	@JsonView(DataTablesOutput.View.class)
	public @ResponseBody DataTablesOutput<User> saveEdit(@Validated(OrderedValidation.class) @RequestBody final UserEditDTO userForm, final BindingResult bindingResult) {
		final DataTablesOutput<User> dtOutput = new DataTablesOutput<>();
		List<User> listNewUser = new ArrayList<>();
		final JSONObject json = new JSONObject();
		
		final User userBeforeUpdate = this.userService.getUserByUserId(userForm.getIdUserFireEdit());

		boolean error = false;
		if (bindingResult.hasErrors()) {
			error = true;
			for (final FieldError o: bindingResult.getFieldErrors()) {
				json.put(o.getField() + SPAN, o.getDefaultMessage());
			}
		}

		// Si se esta estableciendo el rol administrador a un usuario distinto al original,
		// comprobamos que se hayan establecido correctamente las contrasenas
		final Rol rol = this.userService.getRol(userForm.getRolId());
		final boolean hasAccessPermission = PermissionsChecker.hasPermission(rol, Permissions.ACCESS);

		if (hasAccessPermission && !LOGIN_ADMIN_USER.equals(userForm.getUsernameEdit()) && !hadAccessBeforeEdit(userForm.getIdUserFireEdit())) {
			if (emptyAdminPassword(userForm.getPasswordEdit())) {
				error = true;
				json.put("passwordEdit" + SPAN, "El campo contrase\u00F1a es obligatorio.");
			}
			else if (!matchingConfirmPassword(userForm.getPasswordEdit(), userForm.getConfirmPasswordEdit())) {
				error = true;
				json.put("passwordEdit" + SPAN, "Los campos de contrase\u00F1a deben coincidir.");
				json.put("confirmPasswordEdit" + SPAN, "Los campos de contrase\u00F1a deben coincidir.");
			}
		}

		if (userForm.getEmailEdit() != null && !userForm.getEmailEdit().isEmpty() && !Utils.isValidEmail(userForm.getEmailEdit())) {
			error = true;
			json.put("emailEdit" + SPAN, "El campo email no es v\u00E1lido.");
		}

		if (!userBeforeUpdate.getEmail().equals(userForm.getEmailEdit()) && this.userService.getUserByEmail(userForm.getEmailEdit()) != null) {
			error = true;
			json.put("emailEdit" + SPAN, "Ya existe un usuario con el correo seleccionado.");
		}

		if (!error) {
			try {

				final User user = this.userService.updateUser(userForm);
				listNewUser.add(user);

			} catch (final Exception e) {
				LOGGER.error(Language.getResWebFire(IWebLogMessages.ERRORWEB022), e);
				listNewUser = StreamSupport.stream(this.userService.getAllUser().spliterator(), false).collect(Collectors.toList());
				json.put(KEY_JS_ERROR_SAVE_USER, Language.getResWebFire(IWebLogMessages.ERRORWEB022));
				dtOutput.setError(json.toString());
			}
		} else {
			listNewUser = StreamSupport.stream(this.userService.getAllUser().spliterator(), false).collect(Collectors.toList());
			dtOutput.setError(json.toString());
		}

		dtOutput.setData(listNewUser);

		return dtOutput;

	}

	/**
	 * Method that changes the password.
	 * @param userFormPassword UserFormPassword
	 * @param bindingResult BindingResult
	 * @return String result
	 */
	@RequestMapping(value = "/saveuserpassword", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public String savePassword(@Validated(OrderedValidation.class) @RequestBody final UserPasswordDTO userFormPassword, final BindingResult bindingResult) {
		String result = UtilsStringChar.EMPTY_STRING;

		if (bindingResult.hasErrors()) {
			final JSONObject json = new JSONObject();
			for (final FieldError o: bindingResult.getFieldErrors()) {
				json.put(o.getField() + SPAN, o.getDefaultMessage());
			}
			result = json.toString();

		} else if (!matchingConfirmPassword(userFormPassword.getPassword(), userFormPassword.getConfirmPassword())) {

			final JSONObject json = new JSONObject();
			json.put("password" + SPAN, "Los campos de contrase\u00F1a deben coincidir.");
			json.put("confirmPassword" + SPAN, "Los campos de contrase\u00F1a deben coincidir.");
			result = json.toString();

		} else {

			result = this.userService.changeUserPassword(userFormPassword);
		}

		return result;
	}

	/**
	 * @param password
	 * @param confirmPassword
	 * @return
	 */
	private static boolean matchingConfirmPassword(final String password, final String confirmPassword) {
		return password.equals(confirmPassword);
	}

	/**
	 * Method that edits the user.
	 * @param userForm UserFormEdit
	 * @param bindingResult BindingResult
	 * @return String result
	 */
	@RequestMapping(value = "/menueditsave", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public String saveEditMenu(@Validated(OrderedValidation.class) @RequestBody final UserEditDTO userForm, final BindingResult bindingResult) {

		String result = UtilsStringChar.EMPTY_STRING;

		if (bindingResult.hasErrors()) {
			final JSONObject json = new JSONObject();
			for (final FieldError o: bindingResult.getFieldErrors()) {
				json.put(o.getField() + SPAN, o.getDefaultMessage());
			}
			result = json.toString();
		} else {
			try {

				this.userService.updateUser(userForm);

				result = "0";
			} catch (final Exception e) {
				result = "-1";
				throw e;
			}
		}

		return result;
	}

	/**
	 * Get userService.
	 * @return userService
	 */
	public IUserService getUserService() {
		return this.userService;
	}

	/**
	 * Set userService.
	 * @param userServiceP set userService
	 */
	public void setUserService(final IUserService userServiceP) {
		this.userService = userServiceP;
	}

	/**
	 * Get context.
	 * @return context
	 */
	public ServletContext getContext() {
		return this.context;
	}

	/**
	 * Set context.
	 * @param contextP set context
	 */
	public void setContext(final ServletContext contextP) {
		this.context = contextP;
	}

}