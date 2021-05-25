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
 * <b>File:</b><p>es.gob.fire.persistence.dto.UserDTO.java.</p>
 * <b>Description:</b><p>Class that represents the backing form for adding a user.</p>
  * <b>Project:</b><p>Application for signing documents of @firma suite systems.</p>
 * <b>Date:</b><p>21/06/2020.</p>
 * @author Gobierno de España.
 * @version 1.2, 21/05/2021.
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
 * <p>Class that represents the backing form for adding a user.</p>
 * <b>Project:</b><p>Application for signing documents of @firma suite systems.</p>
 * @version 1.2, 21/05/2021.
 */
public class UserDTO {

	/**
	 * Attribute that represents the value of the primary key as a hidden input in the form.
	 */
	private Long userId;

	/**
	 * Attribute that represents the value of the input name of the user in the form.
	 */
	@NotNull(groups=CheckItFirst.class, message="{form.valid.user.name.notempty}")
    @Size(min=1, max=NumberConstants.NUM45, groups=ThenCheckIt.class)
    private String nameAdd = UtilsStringChar.EMPTY_STRING;

	/**
	 * Attribute that represents the value of the input surnames of the user in the form.
	 */
	@NotNull(groups=CheckItFirst.class, message="{form.valid.user.surnames.notempty}")
    @Size(min=1, max=NumberConstants.NUM100, groups=ThenCheckIt.class)
    private String surnamesAdd = UtilsStringChar.EMPTY_STRING;

	/**
	 * Attribute that represents the value of the input username of the user in the form.
	 */
	@NotNull(groups = CheckItFirst.class, message = "{form.valid.user.login.notempty}")
	@Size(min = NumberConstants.NUM1, max = NumberConstants.NUM30, groups = ThenCheckIt.class)
    private String loginAdd = UtilsStringChar.EMPTY_STRING;

	/**
	 * Attribute that represents the value of the input password of the user in the form.
	 */
	@NotNull(groups=CheckItFirst.class, message="{form.valid.user.password.notempty}")
    //@Size(min=NumberConstants.NUM7, max=NumberConstants.NUM30, groups=ThenCheckIt.class)
	//@Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$", message="{form.valid.user.password.noPattern}", groups=ThenCheckIt.class)
    private String passwordAdd = UtilsStringChar.EMPTY_STRING;

	/**
	 * Attribute that represents the value of the input password of the user in the form.
	 */
	@NotNull(groups=CheckItFirst.class, message="{form.valid.user.confirmPassword.notempty}")
    //@Size(min=NumberConstants.NUM7, max=NumberConstants.NUM30, groups=ThenCheckIt.class)
    private String confirmPasswordAdd = UtilsStringChar.EMPTY_STRING;

	/**
	 * Attribute that represents the value of the input email of the user in the form.
	 */
	@NotNull(groups=CheckItFirst.class, message="{form.valid.user.email.notempty}")
    @Size(min=NumberConstants.NUM3, max=NumberConstants.NUM45, groups=ThenCheckIt.class)
    private String emailAdd = UtilsStringChar.EMPTY_STRING;

	/**
	 * Attribute that represents the value of the input telf of the user in the form.
	 */
	@NotNull(groups=CheckItFirst.class, message="{form.valid.user.telf.notempty}")
   // @Size(min=NumberConstants.NUM3, max=NumberConstants.NUM255, groups=ThenCheckIt.class)
    private String telfAdd = UtilsStringChar.EMPTY_STRING;

	/**
	 * Attribute that represents the identifier for the use role selected in the user form.
	 */
	private Long rolId;

	/**
	 * Gets the value of the attribute {@link #userId}.
	 * @return the value of the attribute {@link #userId}.
	 */
	public Long getUserId() {
		return this.userId;
	}


	/**
	 * Sets the value of the attribute {@link #userId}.
	 * @param userIdParam The value for the attribute {@link #userId}.
	 */
	public void setUserId(final Long userIdParam) {
		this.userId = userIdParam;
	}


	/**
	 * Gets the value of the attribute {@link #name}.
	 * @return the value of the attribute {@link #name}.
	 */
	public String getNameAdd() {
		return this.nameAdd;
	}


	/**
	 * Sets the value of the attribute {@link #name}.
	 * @param nameParam The value for the attribute {@link #name}.
	 */
	public void setNameAdd(final String nameParam) {
		this.nameAdd = nameParam;
	}


	/**
	 * Gets the value of the attribute {@link #surnames}.
	 * @return the value of the attribute {@link #surnames}.
	 */
	public String getSurnamesAdd() {
		return this.surnamesAdd;
	}


	/**
	 * Sets the value of the attribute {@link #surnames}.
	 * @param surnamesParam The value for the attribute {@link #surnames}.
	 */
	public void setSurnamesAdd(final String surnamesParam) {
		this.surnamesAdd = surnamesParam;
	}


	/**
	 * Gets the value of the attribute {@link #login}.
	 * @return the value of the attribute {@link #login}.
	 */
	public String getLoginAdd() {
		return this.loginAdd;
	}


	/**
	 * Sets the value of the attribute {@link #login}.
	 * @param loginParam The value for the attribute {@link #login}.
	 */
	public void setLoginAdd(final String loginParam) {
		this.loginAdd = loginParam;
	}


	/**
	 * Gets the value of the attribute {@link #password}.
	 * @return the value of the attribute {@link #password}.
	 */
	public String getPasswordAdd() {
		return this.passwordAdd;
	}


	/**
	 * Sets the value of the attribute {@link #password}.
	 * @param passwordParam The value for the attribute {@link #password}.
	 */
	public void setPasswordAdd(final String passwordParam) {
		this.passwordAdd = passwordParam;
	}


	/**
	 * Gets the value of the attribute {@link #confirmPassword}.
	 * @return the value of the attribute {@link #confirmPassword}.
	 */
	public String getConfirmPasswordAdd() {
		return this.confirmPasswordAdd;
	}


	/**
	 * Sets the value of the attribute {@link #confirmPassword}.
	 * @param confirmPasswordParam The value for the attribute {@link #confirmPassword}.
	 */
	public void setConfirmPasswordAdd(final String confirmPasswordParam) {
		this.confirmPasswordAdd = confirmPasswordParam;
	}


	/**
	 * Gets the value of the attribute {@link #email}.
	 * @return the value of the attribute {@link #email}.
	 */
	public String getEmailAdd() {
		return this.emailAdd;
	}


	/**
	 * Sets the value of the attribute {@link #email}.
	 * @param emailParam The value for the attribute {@link #email}.
	 */
	public void setEmailAdd(final String emailParam) {
		this.emailAdd = emailParam;
	}

	/**
	 * Gets the value of the attribute {@link #rol}.
	 * @return the value of the attribute {@link #rol}.
	 */
	public String getTelfAdd() {
		return this.telfAdd;
	}

	/**
	 * Sets the value of the attribute {@link #phone}.
	 * @param phoneP The value for the attribute {@link #phone}.
	 */
	public void setTelfAdd(final String telfP) {
		this.telfAdd = telfP;
	}


	public Long getRolId() {
		return this.rolId;
	}


	public void setRolId(final Long rolId) {
		this.rolId = rolId;
	}

}
