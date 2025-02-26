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
 * <b>File:</b><p>es.gob.afirma.persistence.configuration.model.pojo.XApplicationResponsiblePOJO.java.</p>
 * <b>Description:</b><p>Class that represents the representation of the <i>X_APPLICATION_RESPONSIBLE</i> database table as a Plain Old Java Object.</p>
 * <b>Project:</b><p>Horizontal platform of validation services of multiPKI certificates and electronic signature.</p>
 * <b>Date:</b><p>27/01/2025.</p>
 * @author Gobierno de Espa&ntilde;a.
 * @version 1.0, 27/01/2025.
 */
package es.gob.fire.persistence.entity;

import java.io.Serializable;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

/**
 * <p>Class that maps the <i>TB_CERTIFICADOS_DE_APLICACION</i> database table as a Plain Old Java Object.</p>
 * <b>Project:</b><p>Application for monitoring services of @firma suite systems.</p>
 * @version 1.0, 12/01/2021.
 */
@Entity
@Table(name = "TB_CERTIFICADOS_DE_APLICACION")
public class CertificatesApplication implements Serializable {

	/**
	 * Constant attribute that represents the serial version UID.
	 */
	private static final long serialVersionUID = -844772441561734046L;

	/**
	 * Attribute that represents the object ID.
	 */
	private CertificatesApplicationPK idCertificatesApplication;

	/**
	 * Attribute that represents the associated application.
	 */
	private Application application;

	/**
	 * Attribute that represents the associated responsible.
	 */
	private Certificate certificate;

	/**
	 * Gets the value of the attribute {@link #idCertificatesApplication}.
	 * @return the value of the attribute {@link #idCertificatesApplication}.
	 */
	// CHECKSTYLE:OFF -- Checkstyle rule "Design for Extension" is not applied
	// because Hibernate JPA needs not final access methods.
	@EmbeddedId
	public CertificatesApplicationPK getIdCertificatesApplication() {
		// CHECKSTYLE:ON
		return this.idCertificatesApplication;
	}

	/**
	 * Sets the value of the attribute {@link #idCertificatesApplication}.
	 * @param idApplicationResponsibleParam The value for the attribute {@link #idCertificatesApplication}.
	 */
	// CHECKSTYLE:OFF -- Checkstyle rule "Design for Extension" is not applied
	// because Hibernate JPA needs not final access methods.
	public void setIdCertificatesApplication(final CertificatesApplicationPK idCertificatesApplicationParam) {
		// CHECKSTYLE:ON
		this.idCertificatesApplication = idCertificatesApplicationParam;
	}

	/**
	 * Gets the value of the attribute {@link #application}.
	 * @return the value of the attribute {@link #application}.
	 */
	// CHECKSTYLE:OFF -- Checkstyle rule "Design for Extension" is not applied
	// because Hibernate JPA needs not final access methods.
	@MapsId("idApplication")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_aplicaciones", nullable = false, insertable = false, updatable = false)
	public Application getApplication() {
		// CHECKSTYLE:ON
		return this.application;
	}

	/**
	 * Sets the value of the attribute {@link #application}.
	 * @param applicationParam The value for the attribute {@link #application}.
	 */
	// CHECKSTYLE:OFF -- Checkstyle rule "Design for Extension" is not applied
	// because Hibernate JPA needs not final access methods.
	public void setApplication(final Application applicationParam) {
		// CHECKSTYLE:ON
		this.application = applicationParam;
	}

	/**
	 * Gets the value of the attribute {@link #responsible}.
	 * @return the value of the attribute {@link #responsible}.
	 */
	// CHECKSTYLE:OFF -- Checkstyle rule "Design for Extension" is not applied
	// because Hibernate JPA needs not final access methods.
	@MapsId("idCertificate")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_certificados", nullable = false, insertable = false, updatable = false)
	public Certificate getCertificate() {
		// CHECKSTYLE:ON
		return this.certificate;
	}

	/**
	 * Sets the value of the attribute {@link #responsible}.
	 * @param responsibleParam The value for the attribute {@link #responsible}.
	 */
	// CHECKSTYLE:OFF -- Checkstyle rule "Design for Extension" is not applied
	// because Hibernate JPA needs not final access methods.
	public void setCertificate(final Certificate certificateParam) {
		// CHECKSTYLE:ON
		this.certificate = certificateParam;
	}

}
