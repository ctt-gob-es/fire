/*
/*******************************************************************************
 * Copyright (C) 2018 MINHAFP, Gobierno de España
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
 * <b>File:</b><p>es.gob.monitoriza.controller.UserRestController.java.</p>
 * <b>Description:</b><p> .</p>
 * <b>Project:</b><p>Application for signing documents of @firma suite systems</p>
 * <b>Date:</b><p>21/03/2018.</p>
 * @author Gobierno de España.
 * @version 1.7, 14/03/2019.
 */
package es.gob.fire.web.rest.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.servlet.ServletContext;
import javax.validation.constraints.NotEmpty;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
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

import es.gob.fire.persistence.dto.UserDTO;
import es.gob.fire.persistence.dto.UserEditDTO;
import es.gob.fire.persistence.dto.UserPasswordDTO;
import es.gob.fire.commons.utils.UtilsStringChar;
import es.gob.fire.i18n.IWebLogMessages;
import es.gob.fire.i18n.Language;
import es.gob.fire.persistence.dto.validation.OrderedValidation;
import es.gob.fire.persistence.entity.User;
import es.gob.fire.persistence.service.IUserService;

/**
 * <p>Class that manages the REST requests related to the Users administration and JSON communication.</p>
 * <b>Project:</b><p>Application for signing documents of @firma suite systems.</p>
 * @version 1.7, 14/03/2019.
 */
@RestController
public class UserRestController {
	
	/**
	 * Attribute that represents the object that manages the log of the class.
	 */
	private static final Logger LOGGER = Logger.getLogger(UserRestController.class);

	/**
	 * Attribute that represents the identifier of the html input file field for the keystore file.
	 */
	private static final String FIELD_FILE = "file";

	/**
	 * Attribute that represents the identifier of the html input id field for the user.
	 */
	private static final String FIELD_ID_USER = "idUser";

	/**
	 * Attribute that represents the span text.
	 */
	private static final String SPAN = "_span";

	/**
	 * Attribute that represents the user column someCertNotValid. 
	 */
	private static final String COLUMN_CERT_NOT_VALID = "someCertNotValid";
	
	/**
	 * Constant that represents the key Json 'errorSaveUser'.
	 */
	private static final String KEY_JS_ERROR_SAVE_USER = "errorSaveUser";

	/**
	 * Attribute that represents the service object for accessing the
	 * repository.
	 */
	@Autowired
	private IUserService userService;
	
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
		//input.getColumn(COLUMN_CERT_NOT_VALID).setSearchable(Boolean.FALSE);
		return (DataTablesOutput<User>) userService.getAllUser(input);
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
	@JsonView(DataTablesOutput.View.class)
	@RequestMapping(path = "/deleteuser", method = RequestMethod.POST)
	@Transactional
	public String deleteUser(@RequestParam("id") final Long userId, @RequestParam("index") final String index) {
		userService.deleteUser(userId);
		return index;
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
		DataTablesOutput<User> dtOutput = new DataTablesOutput<User>();
		List<User> listNewUser = new ArrayList<User>();
		JSONObject json = new JSONObject();
		
		if (bindingResult.hasErrors()) {
			listNewUser = StreamSupport.stream(userService.getAllUser().spliterator(), false).collect(Collectors.toList());
			for (FieldError o: bindingResult.getFieldErrors()) {
				json.put(o.getField() + SPAN, o.getDefaultMessage());
			}
			dtOutput.setError(json.toString());
		} else {
			try {
				
				User user = userService.saveUser(userForm);

				listNewUser.add(user);
			} catch (Exception e) {
				LOGGER.error(Language.getResWebMonitoriza(IWebLogMessages.ERRORWEB022), e);
				listNewUser = StreamSupport.stream(userService.getAllUser().spliterator(), false).collect(Collectors.toList());
				json.put(KEY_JS_ERROR_SAVE_USER, Language.getResWebMonitoriza(IWebLogMessages.ERRORWEB022));
				dtOutput.setError(json.toString());
			}
		}

		dtOutput.setData(listNewUser);

		return dtOutput;

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
		DataTablesOutput<User> dtOutput = new DataTablesOutput<>();
		List<User> listNewUser = new ArrayList<User>();

		if (bindingResult.hasErrors()) {
			listNewUser = StreamSupport.stream(userService.getAllUser().spliterator(), false).collect(Collectors.toList());
			JSONObject json = new JSONObject();
			for (FieldError o: bindingResult.getFieldErrors()) {
				json.put(o.getField() + SPAN, o.getDefaultMessage());
			}
			dtOutput.setError(json.toString());
		} else {
			try {
				
				User user = userService.updateUser(userForm);

				listNewUser.add(user);
			} catch (Exception e) {
				listNewUser = StreamSupport.stream(userService.getAllUser().spliterator(), false).collect(Collectors.toList());
				throw e;
			}
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
			JSONObject json = new JSONObject();
			for (FieldError o: bindingResult.getFieldErrors()) {
				json.put(o.getField() + SPAN, o.getDefaultMessage());
			}
			result = json.toString();
		} else {
			
			result = userService.changeUserPassword(userFormPassword);
		}

		return result;
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
			JSONObject json = new JSONObject();
			for (FieldError o: bindingResult.getFieldErrors()) {
				json.put(o.getField() + SPAN, o.getDefaultMessage());
			}
			result = json.toString();
		} else {
			try {
				
				userService.updateUser(userForm);

				result = "0";
			} catch (Exception e) {
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
		return userService;
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
		return context;
	}

	/**
	 * Set context.
	 * @param contextP set context
	 */
	public void setContext(ServletContext contextP) {
		this.context = contextP;
	}

}