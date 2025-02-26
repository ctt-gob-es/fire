package es.gob.fire.persistence.entity;

import java.io.Serializable;
import javax.persistence.*;

import org.hibernate.annotations.Type;

@Entity
@Table(name = "TB_PROVEEDORES_APLICACION")
@IdClass(ProviderApplicationPK.class)
public class ProviderApplication implements Serializable {

    @Id
    @ManyToOne
    @JoinColumn(name = "ID_PROVEEDOR", nullable = false)
    private Provider provider;

    @Id
    @ManyToOne
    @JoinColumn(name = "ID_APLICACION", nullable = false)
    private Application application;

    @Column(name = "OBLIGATORIO", nullable = false)
    @Type(type = "yes_no")
    private boolean mandatory;

    @Column(name = "HABILITADO", nullable = false)
    @Type(type = "yes_no")
    private boolean enabled;

    @Column(name = "ORDEN", nullable = false)
    private Long orderIndex;

    // Getters y Setters
    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public boolean getMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Long getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Long orderIndex) {
        this.orderIndex = orderIndex;
    }
}