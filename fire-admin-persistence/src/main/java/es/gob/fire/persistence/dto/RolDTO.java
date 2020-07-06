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
 * <b>File:</b><p>es.gob.fire.core.dto.RolDTO.java.</p>
 * <b>Description:</b><p> .</p>
  * <b>Project:</b><p>Signature Integral System FIRe.</p>
 * <b>Date:</b><p>28/05/2020.</p>
 * @author Gobierno de España.
 * @version 1.0, 28/05/2020.
 */
package es.gob.fire.persistence.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import es.gob.fire.commons.utils.NumberConstants;
import es.gob.fire.persistence.dto.validation.CheckItFirst;
import es.gob.fire.persistence.dto.validation.ThenCheckIt;

/** 
 * <p>Class that represents the backing form for adding/editing an user Rol.</p>
 * <b>Project:</b><p>Signature Integral System FIRe.</p>
 * @version 1.0, 02/11/2018.
 */
public class RolDTO {
	
	/**
	 * Attribute that represents the rol id.
	 */
	private Long rolId = null;
	
	
	/**
	 * Constructor using arguments
	 * @param rolId Long that represetns the rol identifier
	 * @param rolName String that represents role name
	 * @param permissions String that represents permissions
	 */
	public RolDTO(Long rolId,
			@NotNull(groups = CheckItFirst.class, message = "{form.valid.user.rol.notempty}") @Size(min = 1, max = 45, groups = ThenCheckIt.class) String rolName,
			@Size(min = 1, max = 45, groups = ThenCheckIt.class) String permissions) {
		super();
		this.rolId = rolId;
		this.rolName = rolName;
		this.permissions = permissions;
		
	}

	/**
	 * Attribute that represents the rol name.
	 */
	@NotNull(groups=CheckItFirst.class, message="{form.valid.user.rol.notempty}")
    @Size(min=1, max=NumberConstants.NUM45, groups=ThenCheckIt.class)
	private String rolName;
	
	/**
	 * Attribute that represents the permissions.
	 */
	 @Size(min=1, max=NumberConstants.NUM45, groups=ThenCheckIt.class)
	private String permissions;

	/**
	 * Gets the value of the attribute {@link #rolId}.
	 * @return the value of the attribute {@link #rolId}.
	 */
	public Long getRolId() {
		return rolId;
	}

	/**
	 * Sets the value of the attribute {@link #rolId}.
	 * @param rolIdParam The value for the attribute {@link #rolId}.
	 */
	public void setRolId(Long rolIdParam) {
		this.rolId = rolIdParam;
	}

	/**
	 * Gets the value of the attribute {@link #rolName}.
	 * @return the value of the attribute {@link #rolName}.
	 */
	public String getRolName() {
		return rolName;
	}

	/**
	 * Sets the value of the attribute {@link #rolId}.
	 * @param rolNameParam The value for the attribute {@link #rolId}.
	 */
	public void setRolName(String rolNameParam) {
		this.rolName = rolNameParam;
	}

	/**
	 * Gets the value of the attribute {@link #permissions}.
	 * @return the value of the attribute {@link #permissions}.
	 */
	public String getPermissions() {
		return permissions;
	}

	/**
	 * Sets the value of the attribute {@link #permissions}.
	 * @param permissionsParam The value for the attribute {@link #permissions}.
	 */
	public void setPermissions(String permissionsParam) {
		this.permissions = permissionsParam;
	}
	
}
