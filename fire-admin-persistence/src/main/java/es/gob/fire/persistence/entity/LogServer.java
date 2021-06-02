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
 * <b>File:</b><p>es.gob.fire.persistence.entity.LogServer.java.</p>
 * <b>Description:</b><p>Class that maps the <i>TB_SERVIDORES_LOG</i> database table as a Plain Old Java Object.</p>
  * <b>Project:</b><p>Application for signing documents of @firma suite systems</p>
 * <b>Date:</b><p>14/04/2020.</p>
 * @author Gobierno de España.
 * @version 1.0, 14/04/2020.
 */
package es.gob.fire.persistence.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonView;

import es.gob.fire.commons.utils.NumberConstants;

/** 
 * <p>Class that maps the <i>TB_SERVIDORES_LOG</i> database table as a Plain Old Java Object.</p>
 * <b>Project:</b><p>Application for signing documents of @firma suite systems.</p>
 * @version 1.0, 14/04/2020.
 */
@Entity
@Table(name = "TB_SERVIDORES_LOG")
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class LogServer implements Serializable {

	/**
	 * Attribute that represents the serial version.
	 */
	private static final long serialVersionUID = 3103770202624207187L;
	
	/**
	 * Constant attribute that represents the string <i>"yes_no"</i>.
	 */
	private static final String CONS_YES_NO = "yes_no";
	
	/**
	 * Attribute that represents the log servers id.
	 */
	private Long logServerId;
	
	/**
	 * Attribute that represents the log servers name.
	 */
	private String name;
	
	/**
	 * Attribute that represents the URL service.
	 */
	private String urlService;
	
	/**
	 * Attribute that represents the key for log servers.
	 */
	private String key;
	
	/**
	 * Attribute that represents the verify SSL.
	 */
	private Boolean verifySSL;
	
	/**
	 * Gets the value of the attribute {@link #logServerId}.
	 * @return the value of the attribute {@link #logServerId}.
	 */
	@Id
	@Column(name = "ID_SERVIDOR", unique = true, nullable = false, precision = NumberConstants.NUM11)
	@GeneratedValue(generator = "tb_servidores_log_seq")
	@GenericGenerator(name = "tb_servidores_log_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = { @Parameter(name = "sequence_name", value = "TB_SERVIDORES_LOG_SEQ"), @Parameter(name = "initial_value", value = "2"), @Parameter(name = "increment_size", value = "1") })
	@JsonView(DataTablesOutput.View.class)
	public Long getLogServerId() {
		return this.logServerId;
	}

	/**
	 * Sets the value of the attribute {@link #logServerId}.
	 * @param logServerIdP The value for the attribute {@link #logServerId}.
	 */
	public void setLogServerId(final Long logServerIdP) {
		this.logServerId = logServerIdP;
	}
	
	/**
	 * Gets the value of the attribute {@link #name}.
	 * @return the value of the attribute {@link #name}.
	 */
	@Column(name = "NOMBRE", nullable = false, length = NumberConstants.NUM45, unique = true)
	@Size(max = NumberConstants.NUM45)
	@NotNull
	@JsonView(DataTablesOutput.View.class)
	public String getName() {
		return this.name;
	}

	/**
	 * Sets the value of the attribute {@link #name}.
	 * @param nameP The value for the attribute {@link #name}.
	 */
	public void setName(final String nameP) {
		this.name = nameP;
	}
	
	/**
	 * Gets the value of the attribute {@link #urlService}.
	 * @return the value of the attribute {@link #urlService}.
	 */
	@Column(name = "URL_SERVICIO_LOG", nullable = false, length = NumberConstants.NUM500, unique = true)
	@Size(max = NumberConstants.NUM500)
	@NotNull
	@JsonView(DataTablesOutput.View.class)
	public String getUrlService() {
		return this.urlService;
	}

	/**
	 * Sets the value of the attribute {@link #urlService}.
	 * @param urlServiceP The value for the attribute {@link #urlService}.
	 */
	public void setUrlService(final String urlServiceP) {
		this.urlService = urlServiceP;
	}
	
	/**
	 * Gets the value of the attribute {@link #key}.
	 * @return the value of the attribute {@link #key}.
	 */
	@Column(name = "CLAVE", nullable = false, length = NumberConstants.NUM45, unique = true)
	@Size(max = NumberConstants.NUM45)
	@NotNull
	@JsonView(DataTablesOutput.View.class)
	public String getKey() {
		return this.key;
	}

	/**
	 * Sets the value of the attribute {@link #key}.
	 * @param keyP The value for the attribute {@link #key}.
	 */
	public void setKey(final String keyP) {
		this.key = keyP;
	}
	
	/**
	 * Gets the value of the attribute {@link #verifySSL}.
	 * @return the value of the attribute {@link #verifySSL}.
	 */
   @Column(name = "VERIFICAR_SSL", nullable = false, precision = 1)
   @NotNull
   @JsonView(DataTablesOutput.View.class)
   public Boolean getVerifySSL() {
	   return verifySSL;
   }

   /**
	 * Sets the value of the attribute {@link #verifySSL}.
	 * @param verifySSLP The value for the attribute {@link #verifySSL}.
	 */
   public void setVerifySSL(Boolean verifySSLP) {
	this.verifySSL = verifySSLP;
   }

}
