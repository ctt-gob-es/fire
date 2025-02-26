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
 * <b>File:</b><p>es.gob.fire.persistence.entity.ControlAccess.java.</p>
 * <b>Description:</b><p>Class that maps the <i>CERTIFICATE</i> database table as a Plain Old Java Object.</p>
 * <b>Project:</b><p>Platform for detection and validation of certificates recognized in European TSL.</p>
 * <b>Date:</b><p>01/08/2020.</p>
 * @author Gobierno de Espa&ntilde;a.
 * @version 1.3, 13/02/2025.
 */
package es.gob.fire.persistence.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import es.gob.fire.commons.utils.NumberConstants;

/**
 * <p>Class that maps the <i>TB_CONTROL_ACCESO</i> database table as a Plain Old Java Object.</p>
 * <b>Project:</b><p>Platform for detection and validation of certificates recognized in European TSL.</p>
 * @version 1.3, 13/02/2025.
 */
@Entity
@Table(name = "TB_CONTROL_ACCESO")
public class ControlAccess implements Serializable {

	/**
	 * Constant attribute that represents the serial version UID.
	 */
	private static final long serialVersionUID = -8379669225979483658L;

	/**
	 * Attribute that represents the object ID.
	 */
	@Id
	@Column(name = "ID_CONTROL_ACCESO", unique = true, nullable = false, precision = NumberConstants.NUM19)
	@GeneratedValue(generator = "tb_control_acceso_seq")
	@GenericGenerator(name = "tb_control_acceso_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = { @Parameter(name = "sequence_name", value = "TB_CONTROL_ACCESO_SEQ"), @Parameter(name = "initial_value", value = "1"), @Parameter(name = "increment_size", value = "1") })
	private Long idControlAccess;
	
	/**
	 * Attribute that represents the ip of the user.
	 */
	@Column(name = "IP", nullable = false, length = NumberConstants.NUM45)
	private String ip;
	
	/**
	 * Attribute that represents the start date to control access.
	 */
	@Column(name = "FECHA_INICIO_ACCESO", nullable = false)
	private Date startDateAccess;

	/**
	 * Gets the value of the attribute {@link #idControlAccess}.
	 * @return the value of the attribute {@link #idControlAccess}.
	 */
	public Long getIdControlAccess() {
		return idControlAccess;
	}

	/**
	 * Sets the value of the attribute {@link #idControlAccess}.
	 * @param idControlAccess The value for the attribute {@link #idControlAccess}.
	 */
	public void setIdControlAccess(Long idControlAccess) {
		this.idControlAccess = idControlAccess;
	}

	/**
	 * Gets the value of the attribute {@link #ip}.
	 * @return the value of the attribute {@link #ip}.
	 */
	public String getIp() {
		return ip;
	}

	/**
	 * Sets the value of the attribute {@link #ip}.
	 * @param ip The value for the attribute {@link #ip}.
	 */
	public void setIp(String ip) {
		this.ip = ip;
	}

	/**
	 * Gets the value of the attribute {@link #startDateAccess}.
	 * @return the value of the attribute {@link #startDateAccess}.
	 */
	public Date getStartDateAccess() {
		return startDateAccess;
	}

	/**
	 * Sets the value of the attribute {@link #startDateAccess}.
	 * @param startDateAccess The value for the attribute {@link #startDateAccess}.
	 */
	public void setStartDateAccess(Date startDateAccess) {
		this.startDateAccess = startDateAccess;
	}
	
}
