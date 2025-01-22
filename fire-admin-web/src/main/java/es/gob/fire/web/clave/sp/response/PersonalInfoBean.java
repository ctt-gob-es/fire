package es.gob.fire.web.clave.sp.response;

/**
 * PersonalInfoBean is a JavaBean that implements the SimplePersonalInfoBean interface.
 * It holds personal information such as DNI, name, surname, info token, error type, error message, and service URL.
 */
public class PersonalInfoBean implements SimplePersonalInfoBean {

    /**
     * The user's DNI.
     */
    private String dni;

    /**
     * The user's first name.
     */
    private String nombre;

    /**
     * The user's surname.
     */
    private String apellidos;

    /**
     * The information token associated with the user.
     */
    private String infoToken; 

    /**
     * The type of error encountered.
     */
    private String errorType;

    /**
     * The error message associated with the error type.
     */
    private String errorMessage;

    /**
     * The service URL.
     */
    private String serviceUrl;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDni() {
        return this.dni;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDni(String dni) {
        this.dni = dni;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNombre() {
        return this.nombre;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getApellidos() {
        return this.apellidos;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getInfoToken() {
        return this.infoToken;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setInfoToken(String infoToken) {
        this.infoToken = infoToken;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getErrorType() {
        return this.errorType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getErrorMessage() {
        return this.errorMessage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getServiceUrl() {
        return this.serviceUrl;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }
}
