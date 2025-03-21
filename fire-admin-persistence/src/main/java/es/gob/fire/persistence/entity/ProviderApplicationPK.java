package es.gob.fire.persistence.entity;

import java.io.Serializable;
import java.util.Objects;

public class ProviderApplicationPK implements Serializable {
    
    private String provider;
    private String application;

    public ProviderApplicationPK() {}

    public ProviderApplicationPK(String provider, String application) {
        this.provider = provider;
        this.application = application;
    }

    public String getProvider() {
        return this.provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getApplication() {
        return this.application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProviderApplicationPK that = (ProviderApplicationPK) o;
        return Objects.equals(this.provider, that.provider) && Objects.equals(this.application, that.application);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.provider, this.application);
    }
}