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
 * <b>File:</b><p>es.gob.fire.core.dto.UserDTO.java.</p>
 * <b>Description:</b><p> .</p>
  * <b>Project:</b><p>Platform for detection and validation of certificates recognized in European TSL.</p>
 * <b>Date:</b><p>19/06/2018.</p>
 * @author Gobierno de España.
 * @version 1.1, 02/11/2018.
 */
package es.gob.fire.persistence.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
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
 * <p>Class that represents the backing form for adding/editing a user.</p>
 * <b>Project:</b><p>Platform for detection and validation of certificates recognized in European TSL.</p>
 * @version 1.1, 02/11/2018.
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
    @Size(min=1, max=NumberConstants.NUM15, groups=ThenCheckIt.class)
    private String name = UtilsStringChar.EMPTY_STRING;

	/**
	 * Attribute that represents the value of the input surnames of the user in the form. 
	 */
	@NotNull(groups=CheckItFirst.class, message="{form.valid.user.surnames.notempty}")
    @Size(min=1, max=NumberConstants.NUM30, groups=ThenCheckIt.class)
    private String surnames = UtilsStringChar.EMPTY_STRING;
	
	/**
	 * Attribute that represents the value of the input username of the user in the form. 
	 */
	@NotNull(groups=CheckItFirst.class, message="{form.valid.user.login.notempty}")
    //@Size(min=NumberConstants.NUM5, max=NumberConstants.NUM30, groups=ThenCheckIt.class)
    private String login = UtilsStringChar.EMPTY_STRING;

	/**
	 * Attribute that represents the value of the input password of the user in the form. 
	 */
	@NotNull(groups=CheckItFirst.class, message="{form.valid.user.password.notempty}")
    //@Size(min=NumberConstants.NUM7, max=NumberConstants.NUM30, groups=ThenCheckIt.class)
	//@Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$", message="{form.valid.user.password.noPattern}", groups=ThenCheckIt.class)
    private String password = UtilsStringChar.EMPTY_STRING;
	
	/**
	 * Attribute that represents the value of the input password of the user in the form. 
	 */
	@NotNull(groups=CheckItFirst.class, message="{form.valid.user.confirmPassword.notempty}")
    //@Size(min=NumberConstants.NUM7, max=NumberConstants.NUM30, groups=ThenCheckIt.class)
    private String confirmPassword = UtilsStringChar.EMPTY_STRING;
		
	/**
	 * Attribute that represents the value of the input email of the user in the form. 
	 */
	@NotNull(groups=CheckItFirst.class, message="{form.valid.user.email.notempty}")
    @Size(min=NumberConstants.NUM3, max=NumberConstants.NUM255, groups=ThenCheckIt.class)
    private String email = UtilsStringChar.EMPTY_STRING;
	
	/**
	 * Attribute that represents the value of the input telf of the user in the form. 
	 */
	@NotNull(groups=CheckItFirst.class, message="{form.valid.user.telf.notempty}")
    @Size(min=NumberConstants.NUM3, max=NumberConstants.NUM255, groups=ThenCheckIt.class)
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
		return userId;
	}

	
	/**
	 * Sets the value of the attribute {@link #userId}.
	 * @param userIdParam The value for the attribute {@link #userId}.
	 */
	public void setUserId(Long userIdParam) {
		this.userId = userIdParam;
	}

	
	/**
	 * Gets the value of the attribute {@link #name}.
	 * @return the value of the attribute {@link #name}.
	 */
	public String getName() {
		return name;
	}

	
	/**
	 * Sets the value of the attribute {@link #name}.
	 * @param nameParam The value for the attribute {@link #name}.
	 */
	public void setName(String nameParam) {
		this.name = nameParam;
	}

	
	/**
	 * Gets the value of the attribute {@link #surnames}.
	 * @return the value of the attribute {@link #surnames}.
	 */
	public String getSurnames() {
		return surnames;
	}

	
	/**
	 * Sets the value of the attribute {@link #surnames}.
	 * @param surnamesParam The value for the attribute {@link #surnames}.
	 */
	public void setSurnames(String surnamesParam) {
		this.surnames = surnamesParam;
	}

	
	/**
	 * Gets the value of the attribute {@link #login}.
	 * @return the value of the attribute {@link #login}.
	 */
	public String getLogin() {
		return login;
	}

	
	/**
	 * Sets the value of the attribute {@link #login}.
	 * @param loginParam The value for the attribute {@link #login}.
	 */
	public void setLogin(String loginParam) {
		this.login = loginParam;
	}

	
	/**
	 * Gets the value of the attribute {@link #password}.
	 * @return the value of the attribute {@link #password}.
	 */
	public String getPassword() {
		return password;
	}

	
	/**
	 * Sets the value of the attribute {@link #password}.
	 * @param passwordParam The value for the attribute {@link #password}.
	 */
	public void setPassword(String passwordParam) {
		this.password = passwordParam;
	}

	
	/**
	 * Gets the value of the attribute {@link #confirmPassword}.
	 * @return the value of the attribute {@link #confirmPassword}.
	 */
	public String getConfirmPassword() {
		return confirmPassword;
	}

	
	/**
	 * Sets the value of the attribute {@link #confirmPassword}.
	 * @param confirmPasswordParam The value for the attribute {@link #confirmPassword}.
	 */
	public void setConfirmPassword(String confirmPasswordParam) {
		this.confirmPassword = confirmPasswordParam;
	}

	
	/**
	 * Gets the value of the attribute {@link #email}.
	 * @return the value of the attribute {@link #email}.
	 */
	public String getEmail() {
		return email;
	}

	
	/**
	 * Sets the value of the attribute {@link #email}.
	 * @param emailParam The value for the attribute {@link #email}.
	 */
	public void setEmail(String emailParam) {
		this.email = emailParam;
	}
	
	/**
	 * Gets the value of the attribute {@link #rol}.
	 * @return the value of the attribute {@link #rol}.
	 */
	public String getTelf() {
		return this.telfAdd;
	}

	/**
	 * Sets the value of the attribute {@link #phone}.
	 * @param phoneP The value for the attribute {@link #phone}.
	 */
	public void setTelf(final String telfP) {
		this.telfAdd = telfP;
	}


	public Long getRolId() {
		return rolId;
	}


	public void setRolId(Long rolId) {
		this.rolId = rolId;
	}
		
}
