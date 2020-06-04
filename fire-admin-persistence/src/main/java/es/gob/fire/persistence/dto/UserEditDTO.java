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
import es.gob.fire.persistence.dto.validation.CheckItFirst;
import es.gob.fire.persistence.dto.validation.ThenCheckIt;

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
	@Size(min = 1, max = NumberConstants.NUM15, groups = ThenCheckIt.class)
	private String nameEdit = UtilsStringChar.EMPTY_STRING;

	/**
	 * Attribute that represents the value of the input surnames of the user in
	 * the form.
	 */
	@NotNull(groups = CheckItFirst.class, message = "{form.valid.user.surnames.notempty}")
	@Size(min = 1, max = NumberConstants.NUM30, groups = ThenCheckIt.class)
	private String surnamesEdit = UtilsStringChar.EMPTY_STRING;

	/**
	 * Attribute that represents the value of the input username of the user in
	 * the form.
	 */
	@NotNull(groups = CheckItFirst.class, message = "{form.valid.user.login.notempty}")
	@Size(min = NumberConstants.NUM5, max = NumberConstants.NUM30, groups = ThenCheckIt.class)
	private String usernameEdit = UtilsStringChar.EMPTY_STRING;

	/**
	 * Attribute that represents the value of the input email of the user in the
	 * form.
	 */
	@NotNull(groups = CheckItFirst.class, message = "{form.valid.user.email.notempty}")
	@Size(min = NumberConstants.NUM3, max = NumberConstants.NUM255, groups = ThenCheckIt.class)
	private String emailEdit = UtilsStringChar.EMPTY_STRING;

	
	/**
	 * Attribute that represents the value of the input telf of the user in
	 * the form.
	 */
	@NotNull(groups = CheckItFirst.class, message = "{form.valid.user.telf.notempty}")
	@Size(min = 1, max = NumberConstants.NUM30, groups = ThenCheckIt.class)
	private String telfEdit = UtilsStringChar.EMPTY_STRING;
	
	
	/**
	 * Attribute that represents the value of the input rol of the user in
	 * the form.
	 */
	@NotNull(groups = CheckItFirst.class, message = "{form.valid.user.rol.notempty}")
	@Size(min = 1, max = NumberConstants.NUM30, groups = ThenCheckIt.class)
	private String rolEdit = UtilsStringChar.EMPTY_STRING;
	
	
	/**
	 * Gets the value of the attribute {@link #idUserFireEdit}.
	 * @return the value of the attribute {@link #idUserFireEdit}.
	 */
	public Long getIdUserFireEdit() {
		return idUserFireEdit;
	}

	
	/**
	 * Sets the value of the attribute {@link #idUserFireEdit}.
	 * @param idUserFireEditParam The value for the attribute {@link #idUserFireEdit}.
	 */
	public void setIdUserFireEdit(Long idUserFireEditParam) {
		this.idUserFireEdit = idUserFireEditParam;
	}

	
	/**
	 * Gets the value of the attribute {@link #nameEdit}.
	 * @return the value of the attribute {@link #nameEdit}.
	 */
	public String getNameEdit() {
		return nameEdit;
	}

	
	/**
	 * Sets the value of the attribute {@link #nameEdit}.
	 * @param nameEditParam The value for the attribute {@link #nameEdit}.
	 */
	public void setNameEdit(String nameEditParam) {
		this.nameEdit = nameEditParam;
	}

	
	/**
	 * Gets the value of the attribute {@link #surnamesEdit}.
	 * @return the value of the attribute {@link #surnamesEdit}.
	 */
	public String getSurnamesEdit() {
		return surnamesEdit;
	}

	
	/**
	 * Sets the value of the attribute {@link #surnamesEdit}.
	 * @param surnamesEditParam The value for the attribute {@link #surnamesEdit}.
	 */
	public void setSurnamesEdit(String surnamesEditParam) {
		this.surnamesEdit = surnamesEditParam;
	}

	
	/**
	 * Gets the value of the attribute {@link #usernameEdit}.
	 * @return the value of the attribute {@link #usernameEdit}.
	 */
	public String getUsernameEdit() {
		return usernameEdit;
	}
	

	
	/**
	 * Sets the value of the attribute {@link #usernameEdit}.
	 * @param usernameEditParam The value for the attribute {@link #usernameEdit}.
	 */
	public void setUsernameEdit(String usernameEditParam) {
		this.usernameEdit = usernameEditParam;
	}

	
	/**
	 * Gets the value of the attribute {@link #emailEdit}.
	 * @return the value of the attribute {@link #emailEdit}.
	 */
	public String getEmailEdit() {
		return emailEdit;
	}

	
	/**
	 * Sets the value of the attribute {@link #emailEdit}.
	 * @param emailEditParam The value for the attribute {@link #emailEdit}.
	 */
	public void setEmailEdit(String emailEditParam) {
		this.emailEdit = emailEditParam;
	}
	
	/**
	 * Gets the value of the attribute {@link #telfEdit}.
	 * @return the value of the attribute {@link #telfEdit}.
	 */
	public String getTelfEdit() {
		return telfEdit;
	}

	
	/**
	 * Sets the value of the attribute {@link #telfEdit}.
	 * @param surnamesEditParam The value for the attribute {@link #telfEdit}.
	 */
	public void setTelfEdit(String telfEditParam) {
		this.telfEdit = telfEditParam;
	}
	
	/**
	 * Gets the value of the attribute {@link #rolEdit}.
	 * @return the value of the attribute {@link #rolEdit}.
	 */
	public String getRolEdit() {
		return rolEdit;
	}

	
	/**
	 * Sets the value of the attribute {@link #rolEdit}.
	 * @param surnamesEditParam The value for the attribute {@link #rolfEdit}.
	 */
	public void setRolEdit(String rolEditParam) {
		this.rolEdit = rolEditParam;
	}



}
