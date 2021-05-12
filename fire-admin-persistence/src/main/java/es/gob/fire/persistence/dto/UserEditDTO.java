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
 * <b>File:</b><p>es.gob.fire.core.dto.UserDTOEdit.java.</p>
 * <b>Description:</b><p> .</p>
 * <b>Project:</b><p>Platform for detection and validation of certificates recognized in European TSL.</p>
 * <b>Date:</b><p>19/06/2018.</p>
 * @author Gobierno de España.
 * @version 1.1, 02/11/2018.
 */
package es.gob.fire.persistence.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import es.gob.fire.commons.utils.NumberConstants;
import es.gob.fire.commons.utils.UtilsStringChar;
import es.gob.fire.persistence.constraint.FieldMatch;
import es.gob.fire.persistence.dto.validation.CheckItFirst;
import es.gob.fire.persistence.dto.validation.ThenCheckIt;

/**
 * Validation annotation to validate that 2 fields have the same value.
 */
@FieldMatch(first = "password", second = "confirmPassword", message = "{form.valid.user.password.confirm}")

/**
 * <p>
 * Class that represents the backing form for adding/editing a user.
 * </p>
 * <b>Project:</b><p>Platform for detection and validation of certificates recognized in European TSL.</p>
 * @version 1.1, 02/11/2018.
 */
public class UserEditDTO {
	/**
	 * Attribute that represents the value of the primary key as a hidden input
	 * in the form.
	 */
	private Long idUserFireEdit;

	/**
	 * Attribute that represents the value of the input name of the user in the
	 * form.
	 */
	@NotNull(groups = CheckItFirst.class, message = "{form.valid.user.name.notempty}")
	@Size(min = 1, max = NumberConstants.NUM45, groups = ThenCheckIt.class)
	private String nameEdit = UtilsStringChar.EMPTY_STRING;

	/**
	 * Attribute that represents the value of the input surnames of the user in
	 * the form.
	 */
	@NotNull(groups = CheckItFirst.class, message = "{form.valid.user.surnames.notempty}")
	@Size(min = 1, max = NumberConstants.NUM100, groups = ThenCheckIt.class)
	private String surnamesEdit = UtilsStringChar.EMPTY_STRING;

	/**
	 * Attribute that represents the value of the input username of the user in
	 * the form. It can be empty if the user is root.
	 */
	//@NotNull(groups = CheckItFirst.class, message = "{form.valid.user.login.notempty}")
	//@Size(min = NumberConstants.NUM5, max = NumberConstants.NUM30, groups = ThenCheckIt.class)
	private String usernameEdit = UtilsStringChar.EMPTY_STRING;

	/**
	 * Attribute that represents the value of the input email of the user in the
	 * form.
	 */
	@NotNull(groups = CheckItFirst.class, message = "{form.valid.user.email.notempty}")
	@Size(min = NumberConstants.NUM3, max = NumberConstants.NUM255, groups = ThenCheckIt.class)
	private String emailEdit = UtilsStringChar.EMPTY_STRING;

	/**
	 * Attribute that represents the value of the input password of the user in the form.
	 * It can be empty if the user isn't change its role to admin.
	 */
	//@Size(min=NumberConstants.NUM7, max=NumberConstants.NUM30, groups=ThenCheckIt.class)
	//@Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$", message="{form.valid.user.password.noPattern}", groups=ThenCheckIt.class)
    private String password = UtilsStringChar.EMPTY_STRING;

	/**
	 * Attribute that represents the value of the input password of the user in the form.
	 * It can be empty if the user isn't change its role to admin.
	 */
	//@Size(min=NumberConstants.NUM7, max=NumberConstants.NUM30, groups=ThenCheckIt.class)
    private String confirmPassword = UtilsStringChar.EMPTY_STRING;

	/**
	 * Attribute that represents the identifier for the use role selected in the user form.
	 */
	private Long rolId;


	/**
	 * Attribute that represents the value of the input telf of the user in
	 * the form.
	 */
	//@NotNull(groups = CheckItFirst.class, message = "{form.valid.user.telf.notempty}")
	//@Size(min = 1, max = NumberConstants.NUM30, groups = ThenCheckIt.class)
	private String telfEdit = UtilsStringChar.EMPTY_STRING;


	/**
	 * Attribute that represents the value of the input rol of the user in
	 * the form.
	 */
	//@NotNull(groups = CheckItFirst.class, message = "{form.valid.user.rol.notempty}")
	//@Size(min = 1, max = NumberConstants.NUM30, groups = ThenCheckIt.class)
	private String rolEdit = UtilsStringChar.EMPTY_STRING;


	/**
	 * Gets the value of the attribute {@link #idUserFireEdit}.
	 * @return the value of the attribute {@link #idUserFireEdit}.
	 */
	public Long getIdUserFireEdit() {
		return this.idUserFireEdit;
	}


	/**
	 * Sets the value of the attribute {@link #idUserFireEdit}.
	 * @param idUserFireEditParam The value for the attribute {@link #idUserFireEdit}.
	 */
	public void setIdUserFireEdit(final Long idUserFireEditParam) {
		this.idUserFireEdit = idUserFireEditParam;
	}


	/**
	 * Gets the value of the attribute {@link #nameEdit}.
	 * @return the value of the attribute {@link #nameEdit}.
	 */
	public String getNameEdit() {
		return this.nameEdit;
	}


	/**
	 * Sets the value of the attribute {@link #nameEdit}.
	 * @param nameEditParam The value for the attribute {@link #nameEdit}.
	 */
	public void setNameEdit(final String nameEditParam) {
		this.nameEdit = nameEditParam;
	}


	/**
	 * Gets the value of the attribute {@link #surnamesEdit}.
	 * @return the value of the attribute {@link #surnamesEdit}.
	 */
	public String getSurnamesEdit() {
		return this.surnamesEdit;
	}


	/**
	 * Sets the value of the attribute {@link #surnamesEdit}.
	 * @param surnamesEditParam The value for the attribute {@link #surnamesEdit}.
	 */
	public void setSurnamesEdit(final String surnamesEditParam) {
		this.surnamesEdit = surnamesEditParam;
	}


	/**
	 * Gets the value of the attribute {@link #usernameEdit}.
	 * @return the value of the attribute {@link #usernameEdit}.
	 */
	public String getUsernameEdit() {
		return this.usernameEdit;
	}



	/**
	 * Sets the value of the attribute {@link #usernameEdit}.
	 * @param usernameEditParam The value for the attribute {@link #usernameEdit}.
	 */
	public void setUsernameEdit(final String usernameEditParam) {
		this.usernameEdit = usernameEditParam;
	}


	/**
	 * Gets the value of the attribute {@link #emailEdit}.
	 * @return the value of the attribute {@link #emailEdit}.
	 */
	public String getEmailEdit() {
		return this.emailEdit;
	}


	/**
	 * Sets the value of the attribute {@link #emailEdit}.
	 * @param emailEditParam The value for the attribute {@link #emailEdit}.
	 */
	public void setEmailEdit(final String emailEditParam) {
		this.emailEdit = emailEditParam;
	}

	/**
	 * Gets the value of the attribute {@link #telfEdit}.
	 * @return the value of the attribute {@link #telfEdit}.
	 */
	public String getTelfEdit() {
		return this.telfEdit;
	}


	/**
	 * Sets the value of the attribute {@link #telfEdit}.
	 * @param surnamesEditParam The value for the attribute {@link #telfEdit}.
	 */
	public void setTelfEdit(final String telfEditParam) {
		this.telfEdit = telfEditParam;
	}

	/**
	 * Gets the value of the attribute {@link #rolEdit}.
	 * @return the value of the attribute {@link #rolEdit}.
	 */
	public String getRolEdit() {
		return this.rolEdit;
	}


	/**
	 * Sets the value of the attribute {@link #rolEdit}.
	 * @param surnamesEditParam The value for the attribute {@link #rolfEdit}.
	 */
	public void setRolEdit(final String rolEditParam) {
		this.rolEdit = rolEditParam;
	}

	/**
	 * Gets the value of the attribute {@link #rolId}.
	 * @return the value of the attribute {@link #rolId}.
	 */
	public Long getRolId() {
		return this.rolId;
	}


	/**
	 * Sets the value of the attribute {@link #rolId}.
	 * @param rolIdParam The value for the attribute {@link #rolId}.
	 */
	public void setRolId(final Long rolIdParam) {
		this.rolId = rolIdParam;
	}

	/**
	 * Gets the value of the attribute {@link #password}.
	 * @return the value of the attribute {@link #password}.
	 */
	public String getPassword() {
		return this.password;
	}

	/**
	 * Sets the value of the attribute {@link #password}.
	 * @param passwordParam The value for the attribute {@link #password}.
	 */
	public void setPassword(final String passwordParam) {
		this.password = passwordParam;
	}

	/**
	 * Gets the value of the attribute {@link #confirmPassword}.
	 * @return the value of the attribute {@link #confirmPassword}.
	 */
	public String getConfirmPassword() {
		return this.confirmPassword;
	}

	/**
	 * Sets the value of the attribute {@link #confirmPassword}.
	 * @param confirmPasswordParam The value for the attribute {@link #confirmPassword}.
	 */
	public void setConfirmPassword(final String confirmPasswordParam) {
		this.confirmPassword = confirmPasswordParam;
	}
}
