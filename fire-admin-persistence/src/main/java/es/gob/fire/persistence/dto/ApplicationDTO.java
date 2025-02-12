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
 * <b>File:</b><p>es.gob.fire.persistence.ApplicationDTO.java.</p>
 * <b>Description:</b><p>Class that represents the backing form for adding/editing an Application.</p>
  * <b>Project:</b><p>Application for signing documents of @firma suite systems</p>
 * <b>Date:</b><p>22/01/2021.</p>
 * @author Gobierno de Espa&ntilde;a.
 * @version 1.0, 22/01/2021.
 */
package es.gob.fire.persistence.dto;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import es.gob.fire.commons.utils.UtilsStringChar;

/**
 * <p>Class that represents the backing form for adding/editing an Application.</p>
 * <b>Project:</b><p>Application for signing documents of @firma suite systems.</p>
 * @version 1.0, 22/01/2021.
 */
public class ApplicationDTO {

	/**
	 * Attribute that represents the value of the primary key as a hidden input in the form.
	 */
	private String appId;

	/**
	 * Attribute that represents the value of the input appName of the application in the form.
	 */
	private String appName = UtilsStringChar.EMPTY_STRING;


	/**
	 * Attribute that represents the value of the input fechaAltaApp of the application in the form.
	 */
	@DateTimeFormat (pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    @JsonFormat (pattern = "yyyy-MM-dd HH:mm:ss.SSS")
	private Date fechaAltaApp;


	/**
	 * Attribute that represents the value of the input habilitado of the application in the form.
	 */
	private boolean habilitado;

	/**
	 * Attribute that represents the value of the input idCertificado of the application in the form.
	 */
	private Long idCertificado;

	/**
	 * Attribute that represents index of the row of the selected application.
	 */
	private String rowIndexApp;
	
	/**
	 * Attribute that represents the value of the organism in the form.
	 */
	private String organization;
	
	/**
	 * Attribute that represents the value of the dir3 code in the form.
	 */
	private String dir3Code;

	/**
	 * Gets the value of the attribute {@link #appId}.
	 * @return the value of the attribute {@link #appId}.
	 */
	public String getAppId() {
		return this.appId;
	}


	/**
	 * Sets the value of the attribute {@link #appId}.
	 * @param appIdParam The value for the attribute {@link #appId}.
	 */
	public void setAppId(final String appIdParam) {
		this.appId = appIdParam;
	}

	/**
	 * Gets the value of the attribute {@link #appNameP}.
	 * @return the value of the attribute {@link #appNameP}.
	 */
	public String getAppName() {
		return this.appName;
	}



	/**
	 * Sets the value of the attribute {@link #appNameP}.
	 * @param appNameParam The value for the attribute {@link #appNameP}.
	 */
	public void setAppName(final String appNameP) {
		this.appName = appNameP;
	}



	/**
	 * Gets the value of the attribute {@link #fechaAltaApp}.
	 * @return the value of the attribute {@link #fechaAltaApp}.
	 */

	public Date getFechaAltaApp() {
		return this.fechaAltaApp;
	}

	/**
	 * Sets the value of the attribute {@link #fechaAlta}.
	 * @param fechaAltaParam The value for the attribute {@link #fechaAlta}.
	 */
	public void setFechaAltaApp(final Date fechaAltaAppParam) {
		this.fechaAltaApp = fechaAltaAppParam;
	}

	/**
	 * Gets the value of the attribute {@link #habilitado}.
	 * @return the value of the attribute {@link #habilitado}.
	 */
	public boolean getHabilitado() {
		return this.habilitado;
	}


	/**
	 * Sets the value of the attribute {@link #habilitado}.
	 * @param habilitado The value for the attribute {@link #habilitado}.
	 */
	public void setHabilitado(final boolean habilitado) {
		this.habilitado = habilitado;
	}

	/**
	 * Gets the value of the attribute {@link #idCertificado}.
	 * @return the value of the attribute {@link #idCertificado}.
	 */
	public Long getIdCertificado() {
		return this.idCertificado;
	}

	/**
	 * Sets the value of the attribute {@link #idCertificado}.
	 * @param idCertificado The value for the attribute {@link #idCertificado}.
	 */
	public void setIdCertificado(final Long idCertificado) {
		this.idCertificado = idCertificado;
	}

	/**
	 * Gets the value of the attribute {@link #rowIndexApp}.
	 * @return the value of the attribute {@link #rowIndexApp}.
	 */
	public String getRowIndexApp() {
		return this.rowIndexApp;
	}

	/**
	 * Sets the value of the attribute {@link #rowIndexApp}.
	 * @param idCertificado The value for the attribute {@link #rowIndexApp}.
	 */
	public void setRowIndexApp(final String rowIndexApp) {
		this.rowIndexApp = rowIndexApp;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public String getDir3Code() {
		return dir3Code;
	}

	public void setDir3Code(String dir3Code) {
		this.dir3Code = dir3Code;
	}


}
