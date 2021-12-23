/*
 * Este fichero forma parte de la plataforma de @firma.
 * La plataforma de @firma es de libre distribucion cuyo codigo fuente puede ser consultado
 * y descargado desde http://administracionelectronica.gob.es
 *
 * Copyright 2005-2019 Gobierno de Espana
 * Este fichero se distribuye bajo las licencias EUPL version 1.1, segun las
 * condiciones que figuran en el fichero 'LICENSE.txt' que se acompana.  Si se   distribuyera este
 * fichero individualmente, deben incluirse aqui las condiciones expresadas alli.
 */

/**
 * <b>File:</b><p>es.gob.afirma.persistence.configuration.model.pojo.XApplicationResponsiblePKPOJO.java.</p>
 * <b>Description:</b><p>Class that represents the Primary Key for the <i>X_APPLICATION_RESOPNSIBLE</i> database table.</p>
 * <b>Project:</b><p>Horizontal platform of validation services of multiPKI certificates and electronic signature.</p>
 * <b>Date:</b><p>20/10/2020.</p>
 * @author Gobierno de Espa&ntilde;a.
 * @version 1.0, 20/10/2020.
 */
package es.gob.fire.persistence.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import es.gob.fire.commons.utils.NumberConstants;

/**
 * <p>Class that represents the Primary Key for the <i>TB_RESPONSABLE_DE_APLICACIONES</i> database table.</p>
 * <b>Project:</b><p>Application for monitoring services of @firma suite systems.</p>
 * @version 1.0, 12/01/2021.
 */
@Embeddable
public class ApplicationResponsiblePK implements Serializable {

	/**
	 * Constant attribute that represents the serial version UID.
	 */
	private static final long serialVersionUID = 8005230868092969909L;

	/**
	 * Attribute that represents the object ID for <i>APPLICATION</i> database table.
	 */
	private String idApplication;

	/**
	 * Attribute that represents the object ID for <i>RESPONSIBLE</i> database table.
	 */
	private Long idResponsible;

	/**
	 * Gets the value of the attribute {@link #idApplication}.
	 * @return the value of the attribute {@link #idApplication}.
	 */
	// CHECKSTYLE:OFF -- Checkstyle rule "Design for Extension" is not applied
	// because Hibernate JPA needs not final access methods.
	@Column(name = "id_aplicaciones", nullable = false, precision = NumberConstants.NUM19)
	public String getIdApplication() {
		// CHECKSTYLE:ON
		return this.idApplication;
	}

	/**
	 * Sets the value of the attribute {@link #idApplication}.
	 * @param idApplicationParam The value for the attribute {@link #idApplication}.
	 */
	// CHECKSTYLE:OFF -- Checkstyle rule "Design for Extension" is not applied
	// because Hibernate JPA needs not final access methods.
	public void setIdApplication(final String idApplicationParam) {
		// CHECKSTYLE:ON
		this.idApplication = idApplicationParam;
	}

	/**
	 * Gets the value of the attribute {@link #idTslData}.
	 * @return the value of the attribute {@link #idTslData}.
	 */
	// CHECKSTYLE:OFF -- Checkstyle rule "Design for Extension" is not applied
	// because Hibernate JPA needs not final access methods.
	@Column(name = "id_responsables", nullable = false, precision = NumberConstants.NUM19)
	public Long getIdResponsible() {
		// CHECKSTYLE:ON
		return this.idResponsible;
	}

	/**
	 * Sets the value of the attribute {@link #idResponsible}.
	 * @param idResponsibleParam The value for the attribute {@link #idResponsible}.
	 */
	// CHECKSTYLE:OFF -- Checkstyle rule "Design for Extension" is not applied
	// because Hibernate JPA needs not final access methods.
	public void setIdResponsible(final Long idResponsibleParam) {
		// CHECKSTYLE:ON
		this.idResponsible = idResponsibleParam;
	}

	/**
	 * {@inheritDoc}
	 * @see java.lang.Object#hashCode()
	 */
	// CHECKSTYLE:OFF -- Checkstyle rule "Design for Extension" is not applied
	// because Hibernate JPA needs not final access methods.
	@Override
	public int hashCode() {
		// CHECKSTYLE:ON
		final int prime = 31;
		int result = 1;
		result = prime * result + (this.idApplication == null ? 0 : this.idApplication.hashCode());
		result = prime * result + (this.idResponsible == null ? 0 : this.idResponsible.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	// CHECKSTYLE:OFF -- Checkstyle rule "Design for Extension" is not applied
	// because Hibernate JPA needs not final access methods.
	@Override
	public boolean equals(final Object obj) {
		// CHECKSTYLE:ON
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ApplicationResponsiblePK other = (ApplicationResponsiblePK) obj;
		if (this.idApplication == null) {
			if (other.idApplication != null) {
				return false;
			}
		} else if (!this.idApplication.equals(other.idApplication)) {
			return false;
		}
		if (this.idResponsible == null) {
			if (other.idResponsible != null) {
				return false;
			}
		} else if (!this.idResponsible.equals(other.idResponsible)) {
			return false;
		}
		return true;
	}

}