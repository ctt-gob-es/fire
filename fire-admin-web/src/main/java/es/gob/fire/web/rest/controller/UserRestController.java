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
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.servlet.ServletContext;
import javax.validation.constraints.NotEmpty;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.data.jpa.datatables.mapping.Order;
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
import es.gob.fire.persistence.dto.UserTableDTO;
import es.gob.fire.persistence.dto.validation.OrderedValidation;
import es.gob.fire.persistence.entity.ApplicationResponsible;
import es.gob.fire.persistence.entity.Rol;
import es.gob.fire.persistence.entity.User;
import es.gob.fire.persistence.permissions.Permissions;
import es.gob.fire.persistence.permissions.PermissionsChecker;
import es.gob.fire.persistence.service.IApplicationService;
import es.gob.fire.persistence.service.IUserService;

/**
 * <p>
 * Class that manages the REST requests related to the Users administration and
 * JSON communication.
 * </p>
 * <b>Project:</b>
 * <p>
 * Application for signing documents of @firma suite systems.
 * </p>
 * 
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

	@Autowired
	private MessageSource messageSource;

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
	public DataTablesOutput<UserTableDTO> users(@NotEmpty final DataTablesInput input, Locale locale) {
	    // Obtener la lista completa de usuarios desde el servicio
	    Iterable<User> users = this.userService.getAllUser();

	    // Convertir la lista de usuarios en una lista de DTOs
	    List<UserTableDTO> dtoList = StreamSupport.stream(users.spliterator(), false).map(user -> {
	        String rolProperty = messageSource.getMessage("form.user.rol." + user.getRol().getRolName(), null, locale);
	        UserTableDTO userTable = new UserTableDTO(user);
	        userTable.setRolName(rolProperty);
	        return userTable;
	    }).collect(Collectors.toList());

	    // 1. Aplicar la búsqueda global con manejo de valores nulos
	    String searchValue = input.getSearch().getValue(); // Valor de búsqueda global
	    if (searchValue != null && !searchValue.isEmpty()) {
	        dtoList = dtoList.stream()
	            .filter(dto -> 
	                (dto.getUserName() != null && dto.getUserName().toLowerCase().contains(searchValue.toLowerCase())) ||
	                (dto.getEmail() != null && dto.getEmail().toLowerCase().contains(searchValue.toLowerCase())) ||
	                (dto.getName() != null && dto.getName().toLowerCase().contains(searchValue.toLowerCase())) ||
	                (dto.getSurnames() != null && dto.getSurnames().toLowerCase().contains(searchValue.toLowerCase())) ||
	                (dto.getPhone() != null && dto.getPhone().toLowerCase().contains(searchValue.toLowerCase())) ||
	                (dto.getRolName() != null && dto.getRolName().toLowerCase().contains(searchValue.toLowerCase()))
	            )
	            .collect(Collectors.toList());
	    }

	    // 2. Aplicar la ordenación con manejo de valores nulos usando expresiones lambda
	    List<Order> orders = input.getOrder();
	    if (!orders.isEmpty()) {
	        Order order = orders.get(0); // Obtener la primera ordenación (solo manejamos una por ahora)
	        int columnIndex = order.getColumn(); // Índice de la columna a ordenar
	        String sortDirection = order.getDir(); // Dirección ('asc' o 'desc')

	        Comparator<UserTableDTO> comparator = null;

	        // Determinar la columna por la cual se está ordenando y manejar los valores nulos
	        switch (input.getColumns().get(columnIndex).getData()) {
	            case "userName":
	                comparator = (dto1, dto2) -> {
	                    String userName1 = dto1.getUserName();
	                    String userName2 = dto2.getUserName();
	                    if (userName1 == null) return 1;
	                    if (userName2 == null) return -1;
	                    return userName1.compareTo(userName2);
	                };
	                break;
	            case "email":
	                comparator = (dto1, dto2) -> {
	                    String email1 = dto1.getEmail();
	                    String email2 = dto2.getEmail();
	                    if (email1 == null) return 1;
	                    if (email2 == null) return -1;
	                    return email1.compareTo(email2);
	                };
	                break;
	            case "name":
	                comparator = (dto1, dto2) -> {
	                    String name1 = dto1.getName();
	                    String name2 = dto2.getName();
	                    if (name1 == null) return 1;
	                    if (name2 == null) return -1;
	                    return name1.compareTo(name2);
	                };
	                break;
	            case "surnames":
	                comparator = (dto1, dto2) -> {
	                    String surnames1 = dto1.getSurnames();
	                    String surnames2 = dto2.getSurnames();
	                    if (surnames1 == null) return 1;
	                    if (surnames2 == null) return -1;
	                    return surnames1.compareTo(surnames2);
	                };
	                break;
	            case "phone":
	                comparator = (dto1, dto2) -> {
	                    String phone1 = dto1.getPhone();
	                    String phone2 = dto2.getPhone();
	                    if (phone1 == null) return 1;
	                    if (phone2 == null) return -1;
	                    return phone1.compareTo(phone2);
	                };
	                break;
	            case "rolName":
	                comparator = (dto1, dto2) -> {
	                    String rolName1 = dto1.getRolName();
	                    String rolName2 = dto2.getRolName();
	                    if (rolName1 == null) return 1;
	                    if (rolName2 == null) return -1;
	                    return rolName1.compareTo(rolName2);
	                };
	                break;
	            default:
	                comparator = (dto1, dto2) -> {
	                    String userName1 = dto1.getUserName();
	                    String userName2 = dto2.getUserName();
	                    if (userName1 == null) return 1;
	                    if (userName2 == null) return -1;
	                    return userName1.compareTo(userName2);
	                };
	        }

	        // Aplicar la dirección de la ordenación (ascendente o descendente)
	        if ("desc".equalsIgnoreCase(sortDirection)) {
	            comparator = comparator.reversed();
	        }

	        // Ordenar la lista
	        dtoList = dtoList.stream().sorted(comparator).collect(Collectors.toList());
	    }

	    // 3. Paginación
	    int start = input.getStart(); // Índice de inicio de los resultados
	    int length = input.getLength(); // Cantidad de resultados por página
	    List<UserTableDTO> paginatedList = dtoList.stream().skip(start).limit(length).collect(Collectors.toList());

	    // 4. Configurar el resultado para DataTables
	    DataTablesOutput<UserTableDTO> dtoOutput = new DataTablesOutput<>();
	    dtoOutput.setDraw(input.getDraw()); // Configurar el valor de "draw"
	    dtoOutput.setRecordsTotal(dtoList.size()); // Total de registros antes de la paginación
	    dtoOutput.setRecordsFiltered(dtoList.size()); // Registros filtrados después de la búsqueda
	    dtoOutput.setData(paginatedList); // Datos paginados

	    return dtoOutput;
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
	public String deleteUser(@RequestParam("username") final String username,
			@RequestParam("index") final String index) {

		String result = index;

		final User user = this.userService.getUserByUserName(username);

		if (user.getRoot() == Boolean.TRUE) {
			result = "error.No se puede eliminar al administrador principal.";
		} else {
			final List<ApplicationResponsible> responsables = this.appService
					.getApplicationResponsibleByUserId(user.getUserId());

			if (responsables == null || responsables.size() > 0) {
				result = "error.No se ha podido borrar el usuario, tiene aplicaciones asociadas.";
			} else {
				this.userService.deleteUser(user.getUserId());
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
	public @ResponseBody DataTablesOutput<UserTableDTO> save(
			@Validated(OrderedValidation.class) @RequestBody final UserDTO userForm,
			final BindingResult bindingResult, Locale locale) {
		final DataTablesOutput<UserTableDTO> dtOutput = new DataTablesOutput<>();
		List<User> listNewUser = new ArrayList<>();
		final JSONObject json = new JSONObject();

		boolean error = false;

		if (bindingResult.hasErrors()) {
			error = true;

			for (final FieldError o : bindingResult.getFieldErrors()) {
				json.put(o.getField() + SPAN, o.getDefaultMessage());
			}
		}

		final Rol rol = this.userService.getRol(userForm.getRolId());
		final boolean hasAccessPermission = PermissionsChecker.hasPermission(rol, Permissions.ACCESS);

		// Si el usuario debe tener acceso a la administracion, entonces es
		// obligatorio que tenga contrasena
		if (hasAccessPermission && emptyAdminPassword(userForm.getPasswordAdd())) {
			error = true;
			json.put("passwordAdd" + SPAN, "El campo contrase\u00F1a es obligatorio.");

		}

		if (hasAccessPermission
				&& !matchingConfirmPassword(userForm.getPasswordAdd(), userForm.getConfirmPasswordAdd())) {
			error = true;
			json.put("passwordAdd" + SPAN, "Los campos de contrase\u00F1a deben coincidir.");
			json.put("confirmPasswordAdd" + SPAN, "Los campos de contrase\u00F1a deben coincidir.");

		}

		if (this.userService.getUserByUserName(userForm.getLoginAdd()) != null) {
			error = true;
			json.put("loginAdd" + SPAN, "Ya existe un usuario con el login seleccionado.");

		}

		if (userForm.getEmailAdd() != null && !userForm.getEmailAdd().isEmpty()
				&& !Utils.isValidEmail(userForm.getEmailAdd())) {
			error = true;
			json.put("emailAdd" + SPAN, "El campo email no es v\u00E1lido.");
		}

		if (this.userService.getAllUserByEmail(userForm.getEmailAdd())!=null && !this.userService.getAllUserByEmail(userForm.getEmailAdd()).isEmpty()) {
			error = true;
			json.put("emailAdd" + SPAN, "Ya existe un usuario con el correo seleccionado.");
		}

		if (!error) {
			try {

				final User user = this.userService.saveUser(userForm);

				listNewUser.add(user);
			} catch (final Exception e) {
				LOGGER.error(Language.getResWebFire(IWebLogMessages.ERRORWEB022), e);
				listNewUser = StreamSupport.stream(this.userService.getAllUser().spliterator(), false)
						.collect(Collectors.toList());
				json.put(KEY_JS_ERROR_SAVE_USER, Language.getResWebFire(IWebLogMessages.ERRORWEB022));
				dtOutput.setError(json.toString());
			}
		} else {
			listNewUser = StreamSupport.stream(this.userService.getAllUser().spliterator(), false)
					.collect(Collectors.toList());
			dtOutput.setError(json.toString());
		}


		// Convertir la lista de usuarios en una lista de DTOs
		List<UserTableDTO> dtoList = StreamSupport.stream(listNewUser.spliterator(), false).map(user -> {
			String rolProperty = messageSource.getMessage("form.user.rol." + user.getRol().getRolName(), null, locale);
			UserTableDTO userTable = new UserTableDTO(user);
			userTable.setRolName(rolProperty);
			return userTable;
		}).collect(Collectors.toList());

		// Configurar registros totales (sin paginación)
		dtOutput.setRecordsTotal(dtoList.size());
		dtOutput.setRecordsFiltered(dtoList.size());
		dtOutput.setData(dtoList);

		return dtOutput;

	}

	/**
	 * Method that checks if the password field is empty
	 * 
	 * @param userForm
	 * @return
	 */
	private static boolean emptyAdminPassword(final String password) {
		return password == null || password.isEmpty();
	}

	/**
	 * Method that checks if a user had the access permission.
	 * 
	 * @param userId
	 *            User id.
	 * @return
	 */
	private boolean hadAccessBeforeEdit(final Long userId) {

		final User user = this.userService.getUserByUserId(userId);

		return PermissionsChecker.hasPermission(user, Permissions.ACCESS);
	}

	/**
	 * Method that updates a user.
	 * 
	 * @param userForm
	 *            UserForm
	 * @param bindingResult
	 *            BindingResult
	 * @return DataTablesOutput<User> users
	 */
	@RequestMapping(value = "/saveuseredit", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	@JsonView(DataTablesOutput.View.class)
	public @ResponseBody DataTablesOutput<UserTableDTO> saveEdit(
			@Validated(OrderedValidation.class) @RequestBody final UserEditDTO userForm,
			final BindingResult bindingResult, Locale locale) {
		final DataTablesOutput<UserTableDTO> dtOutput = new DataTablesOutput<>();
		List<User> listNewUser = new ArrayList<>();
		final JSONObject json = new JSONObject();

		final User userBeforeUpdate = this.userService.getUserByUserId(userForm.getIdUserFireEdit());

		boolean error = false;
		if (bindingResult.hasErrors()) {
			error = true;
			for (final FieldError o : bindingResult.getFieldErrors()) {
				json.put(o.getField() + SPAN, o.getDefaultMessage());
			}
		}

		// Si se esta estableciendo el rol administrador a un usuario distinto
		// al original,
		// comprobamos que se hayan establecido correctamente las contrasenas
		final Rol rol = this.userService.getRol(userForm.getRolId());
		final boolean hasAccessPermission = PermissionsChecker.hasPermission(rol, Permissions.ACCESS);

		if (hasAccessPermission && !LOGIN_ADMIN_USER.equals(userForm.getUsernameEdit())
				&& !hadAccessBeforeEdit(userForm.getIdUserFireEdit())) {
			if (emptyAdminPassword(userForm.getPasswordEdit())) {
				error = true;
				json.put("passwordEdit" + SPAN, "El campo contrase\u00F1a es obligatorio.");
			} else if (!matchingConfirmPassword(userForm.getPasswordEdit(), userForm.getConfirmPasswordEdit())) {
				error = true;
				json.put("passwordEdit" + SPAN, "Los campos de contrase\u00F1a deben coincidir.");
				json.put("confirmPasswordEdit" + SPAN, "Los campos de contrase\u00F1a deben coincidir.");
			}
		}

		if (userForm.getEmailEdit() != null && !userForm.getEmailEdit().isEmpty()
				&& !Utils.isValidEmail(userForm.getEmailEdit())) {
			error = true;
			json.put("emailEdit" + SPAN, "El campo email no es v\u00E1lido.");
		}

		if (!userBeforeUpdate.getEmail().equals(userForm.getEmailEdit())
				&& (this.userService.getAllUserByEmail(userForm.getEmailEdit())!=null && !this.userService.getAllUserByEmail(userForm.getEmailEdit()).isEmpty())) {
			error = true;
			json.put("emailEdit" + SPAN, "Ya existe un usuario con el correo seleccionado.");
		}

		if (!error) {
			try {

				final User user = this.userService.updateUser(userForm);
				listNewUser.add(user);

			} catch (final Exception e) {
				LOGGER.error(Language.getResWebFire(IWebLogMessages.ERRORWEB022), e);
				listNewUser = StreamSupport.stream(this.userService.getAllUser().spliterator(), false)
						.collect(Collectors.toList());
				json.put(KEY_JS_ERROR_SAVE_USER, Language.getResWebFire(IWebLogMessages.ERRORWEB022));
				dtOutput.setError(json.toString());
			}
		} else {
			listNewUser = StreamSupport.stream(this.userService.getAllUser().spliterator(), false)
					.collect(Collectors.toList());
			dtOutput.setError(json.toString());
		}

		// Convertir la lista de usuarios en una lista de DTOs
		List<UserTableDTO> dtoList = StreamSupport.stream(listNewUser.spliterator(), false).map(user -> {
			String rolProperty = messageSource.getMessage("form.user.rol." + user.getRol().getRolName(), null, locale);
			UserTableDTO userTable = new UserTableDTO(user);
			userTable.setRolName(rolProperty);
			return userTable;
		}).collect(Collectors.toList());

		// Configurar registros totales (sin paginación)
		dtOutput.setRecordsTotal(dtoList.size());
		dtOutput.setRecordsFiltered(dtoList.size());
		dtOutput.setData(dtoList);

		return dtOutput;

	}

	/**
	 * Method that changes the password.
	 * 
	 * @param userFormPassword
	 *            UserFormPassword
	 * @param bindingResult
	 *            BindingResult
	 * @return String result
	 */
	@RequestMapping(value = "/saveuserpassword", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public String savePassword(@Validated(OrderedValidation.class) @RequestBody final UserPasswordDTO userFormPassword,
			final BindingResult bindingResult) {
		String result = UtilsStringChar.EMPTY_STRING;

		if (bindingResult.hasErrors()) {
			final JSONObject json = new JSONObject();
			for (final FieldError o : bindingResult.getFieldErrors()) {
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
	 * 
	 * @param userForm
	 *            UserFormEdit
	 * @param bindingResult
	 *            BindingResult
	 * @return String result
	 */
	@RequestMapping(value = "/menueditsave", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public String saveEditMenu(@Validated(OrderedValidation.class) @RequestBody final UserEditDTO userForm,
			final BindingResult bindingResult) {

		String result = UtilsStringChar.EMPTY_STRING;

		if (bindingResult.hasErrors()) {
			final JSONObject json = new JSONObject();
			for (final FieldError o : bindingResult.getFieldErrors()) {
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
	 * 
	 * @return userService
	 */
	public IUserService getUserService() {
		return this.userService;
	}

	/**
	 * Set userService.
	 * 
	 * @param userServiceP
	 *            set userService
	 */
	public void setUserService(final IUserService userServiceP) {
		this.userService = userServiceP;
	}

	/**
	 * Get context.
	 * 
	 * @return context
	 */
	public ServletContext getContext() {
		return this.context;
	}

	/**
	 * Set context.
	 * 
	 * @param contextP
	 *            set context
	 */
	public void setContext(final ServletContext contextP) {
		this.context = contextP;
	}

}