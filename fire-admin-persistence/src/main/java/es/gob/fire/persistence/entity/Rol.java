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
 * <b>File:</b><p>es.gob.fire.persistence.entity.Rol.java.</p>
 * <b>Description:</b><p>Class that maps the <i>TB_ROLES</i> database table as a Plain Old Java Object.</p>
  * <b>Project:</b><p>Application for signing documents of @firma suite systems</p>
 * <b>Date:</b><p>07/07/2020.</p>
 * @author Gobierno de Espa&ntilde;a.
 * @version 1.0, 07/07/2020.
 */
package es.gob.fire.persistence.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonView;

import es.gob.fire.commons.utils.NumberConstants;

/**
 * <p>Class that maps the <i>TB_ROLES</i> database table as a Plain Old Java Object.</p>
 * <b>Project:</b><p>Application for monitoring services of @firma suite systems.</p>
 * @version 1.0, 07/07/2020.
 */
@Entity
@Table(name = "TB_ROLES")
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class Rol implements Serializable {

	/**
	 * Class serial version.
	 */
	private static final long serialVersionUID = -60419018366799736L;

	/**
	 * Attribute that represents the rol id.
	 */
	private Long rolId;

	/**
	 * Attribute that represents the rol name.
	 */
	private String rolName;
	
	/**
	 * Attribute that represents the permissions.
	 */
	private String permissions;
	
	/**
	 * Attribute that represents the permissions.
	 */
	

	/**
	 * Gets the value of the attribute {@link #rolId}.
	 * @return the value of the attribute {@link #rolId}.
	 */
	@Id
	@Column(name = "ID", unique = true, nullable = false, precision = NumberConstants.NUM11)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@NotNull
	@JsonView(DataTablesOutput.View.class)
	public Long getRolId() {
		return this.rolId;
	}

	/**
	 * Sets the value of the attribute {@link #rolId}.
	 * @param userIdP The value for the attribute {@link #rolId}.
	 */
	public void setRolId(final Long rolIdP) {
		this.rolId = rolIdP;
	}

	/**
	 * Gets the value of the attribute {@link #rolName}.
	 * @return the value of the attribute {@link #rolName}.
	 */
	@Column(name = "NOMBRE_ROL", nullable = false, unique = true, length = NumberConstants.NUM45)
	@Size(max = NumberConstants.NUM45)
	@NotNull
	@JsonView(DataTablesOutput.View.class)
	public String getRolName() {
		return this.rolName;
	}

	/**
	 * Sets the value of the attribute {@link #rolName}.
	 * @param rolNameP The value for the attribute {@link #rolName}.
	 */
	public void setRolName(final String rolNameP) {
		this.rolName = rolNameP;
	}

	/**
	 * Gets the value of the attribute {@link #permissions}.
	 * @return the value of the attribute {@link #permissions}.
	 */
	@Column(name = "PERMISOS", nullable = true, length = NumberConstants.NUM45)
	@Size(max = NumberConstants.NUM45)
	@JsonView(DataTablesOutput.View.class)
	public String getPermissions() {
		return this.permissions;
	}

	/**
	 * Sets the value of the attribute {@link #permissions}.
	 * @param permissionsP The value for the attribute {@link #permissions}.
	 */
	public void setPermissions(final String permissionsP) {
		this.permissions = permissionsP;
	}
	
}