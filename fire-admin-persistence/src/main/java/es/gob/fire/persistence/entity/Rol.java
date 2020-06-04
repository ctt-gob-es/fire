package es.gob.fire.persistence.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

import es.gob.fire.commons.utils.NumberConstants;

import com.fasterxml.jackson.annotation.JsonView;

/**
 * <p>Class that maps the <i>USER_MONITORIZA</i> database table as a Plain Old Java Object.</p>
 * <b>Project:</b><p>Application for monitoring services of @firma suite systems.</p>
 * @version 1.2, 25/01/2019.
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
	 * tipos de permisos
	 */
	public static final int ID_ADMIN = 1;
	public static final int ID_RESPONSIBLE = 2;
	public static final int ID_CONTACT = 3;

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

	
	
//	public static String getRoleLegibleText(final Rol rol) {
//
//		switch (rol.getRolId()) {
//		case ID_ADMIN:
//			return "Administrador"; //$NON-NLS-1$
//		case ID_RESPONSIBLE:
//			return "Responsable"; //$NON-NLS-1$
//		case ID_CONTACT:
//			return "Contacto"; //$NON-NLS-1$
//		default:
//			return rol.getRolName();
//		}
//	}
}