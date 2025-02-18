package es.gob.fire.persistence.entity;

import java.io.Serializable;
import java.util.Objects;

public class ProviderApplicationPK implements Serializable {
    
    private Long provider;
    private String application;

    public ProviderApplicationPK() {}

    public ProviderApplicationPK(Long provider, String application) {
        this.provider = provider;
        this.application = application;
    }

    public Long getProvider() {
        return provider;
    }

    public void setProvider(Long provider) {
        this.provider = provider;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProviderApplicationPK that = (ProviderApplicationPK) o;
        return Objects.equals(provider, that.provider) && Objects.equals(application, that.application);
    }

    @Override
    public int hashCode() {
        return Objects.hash(provider, application);
    }
}