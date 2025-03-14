package es.gob.fire.persistence.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

@Entity
@Table(name = "TB_PROVEEDORES")
public class Provider implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PROVEEDOR", length = 20)
    private String id;

    @Column(name = "NOMBRE", nullable = false, length = 50)
    private String name;

    @Column(name = "OBLIGATORIO", nullable = false)
    @Type(type = "yes_no")
    private boolean mandatory;

    @Column(name = "HABILITADO", nullable = false)
    @Type(type = "yes_no")
    private boolean enabled;

    @Column(name = "ORDEN", nullable = false)
    private Long orderIndex;

    // Getters y Setters
	public String getId() {
		return this.id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public boolean getMandatory() {
		return this.mandatory;
	}

	public void setMandatory(final boolean mandatory) {
		this.mandatory = mandatory;
	}

	public boolean getEnabled() {
		return this.enabled;
	}

	public void setEnabled(final boolean enabled) {
		this.enabled = enabled;
	}

	public Long getOrderIndex() {
		return this.orderIndex;
	}

	public void setOrderIndex(final Long orderIndex) {
		this.orderIndex = orderIndex;
	}
}
