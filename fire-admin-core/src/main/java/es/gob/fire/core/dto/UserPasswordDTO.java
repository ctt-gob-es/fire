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
 * <b>File:</b><p>es.gob.fire.core.dto.UserDTOPassword.java.</p>
 * <b>Description:</b><p> .</p>
  * <b>Project:</b><p>Platform for detection and validation of certificates recognized in European TSL.</p>
 * <b>Date:</b><p>19/06/2018.</p>
 * @author Gobierno de España.
 * @version 1.2, 26/121/2018
 */
package es.gob.fire.core.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import es.gob.fire.core.constant.NumberConstants;
import es.gob.fire.core.rest.exception.CheckItFirst;
import es.gob.fire.core.rest.exception.ThenCheckIt;
import es.gob.fire.core.util.UtilsStringChar;
/** 
 * <p>
 * Class that represents the backing form for adding/editing a user.
 * </p>
 * <b>Project:</b><p>Platform for detection and validation of certificates recognized in European TSL.</p>
 * @version 1.2, 26/12/2018.
 */
public class UserPasswordDTO {

	/**
	 * Attribute that represents the value of the primary key as a hidden input
	 * in the form.
	 */
	private Long idUserFirePass = null;
	

	/**
	 * Attribute that represents the name of user in the form.
	 */
	private String nameUser = null;

	/**
	 * Attribute that represents the value of the input password of the user in
	 * the form.
	 */
	@NotNull(groups = CheckItFirst.class, message = "{form.valid.user.password.notempty}")
	@Size(min = NumberConstants.NUM7, max = NumberConstants.NUM30, groups = ThenCheckIt.class)
	@Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$", message = "{form.valid.user.password.noPattern}", groups = ThenCheckIt.class)
	private String password = UtilsStringChar.EMPTY_STRING;

	/**
	 * Attribute that represents the value of the input password of the user in
	 * the form.
	 */
	@NotNull(groups = CheckItFirst.class, message = "{form.valid.user.confirmPassword.notempty}")
	@Size(min = NumberConstants.NUM7, max = NumberConstants.NUM30, groups = ThenCheckIt.class)
	private String confirmPassword = UtilsStringChar.EMPTY_STRING;

	
	/**
	 * Attribute that represents the value of the current password.
	 */
	@NotNull(groups = CheckItFirst.class, message = "{form.valid.user.oldPassword.notempty}")
	private String oldPassword = UtilsStringChar.EMPTY_STRING;


	
	/**
	 * Gets the value of the attribute {@link #idUserFirePass}.
	 * @return the value of the attribute {@link #idUserFirePass}.
	 */
	public Long getIdUserFirePass() {
		return idUserFirePass;
	}


	
	/**
	 * Sets the value of the attribute {@link #idUserFirePass}.
	 * @param idUserFirePassParam The value for the attribute {@link #idUserFirePass}.
	 */
	public void setIdUserFirePass(Long idUserFirePassParam) {
		this.idUserFirePass = idUserFirePassParam;
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
	 * Gets the value of the attribute {@link #oldPassword}.
	 * @return the value of the attribute {@link #oldPassword}.
	 */
	public String getOldPassword() {
		return oldPassword;
	}


	
	/**
	 * Sets the value of the attribute {@link #oldPassword}.
	 * @param oldPasswordParam The value for the attribute {@link #oldPassword}.
	 */
	public void setOldPassword(String oldPasswordParam) {
		this.oldPassword = oldPasswordParam;
	}



	
	/**
	 * Gets the value of the attribute {@link #nameUser}.
	 * @return the value of the attribute {@link #nameUser}.
	 */
	public String getNameUser() {
		return nameUser;
	}



	
	/**
	 * Sets the value of the attribute {@link #nameUser}.
	 * @param nameUserParam The value for the attribute {@link #nameUser}.
	 */
	public void setNameUser(String nameUserParam) {
		this.nameUser = nameUserParam;
	}

	

}
