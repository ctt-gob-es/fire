package es.gob.fire.web.clave.sp.response;

/**
 * SimplePersonalInfoBean is an interface that defines the methods for accessing and modifying personal information.
 */
public interface SimplePersonalInfoBean {

    /**
     * Gets the user's DNI.
     * 
     * @return the DNI of the user.
     */
    public abstract String getDni();

    /**
     * Sets the user's DNI.
     * 
     * @param dni the DNI to set.
     */
    public abstract void setDni(String dni);

    /**
     * Gets the user's first name.
     * 
     * @return the first name of the user.
     */
    public abstract String getNombre();

    /**
     * Sets the user's first name.
     * 
     * @param nombre the first name to set.
     */
    public abstract void setNombre(String nombre);

    /**
     * Gets the user's surname.
     * 
     * @return the surname of the user.
     */
    public abstract String getApellidos();

    /**
     * Sets the user's surname.
     * 
     * @param apellidos the surname to set.
     */
    public abstract void setApellidos(String apellidos);

    /**
     * Gets the information token associated with the user.
     * 
     * @return the information token.
     */
    public abstract String getInfoToken();

    /**
     * Sets the information token associated with the user.
     * 
     * @param infoToken the information token to set.
     */
    public abstract void setInfoToken(String infoToken);
    
    /**
     * Gets the type of error encountered.
     * 
     * @return the error type.
     */
    public abstract String getErrorType();

    /**
     * Sets the type of error encountered.
     * 
     * @param errorType the error type to set.
     */
    public abstract void setErrorType(String errorType);

    /**
     * Gets the error message associated with the error type.
     * 
     * @return the error message.
     */
    public abstract String getErrorMessage();

    /**
     * Sets the error message associated with the error type.
     * 
     * @param errorMessage the error message to set.
     */
    public abstract void setErrorMessage(String errorMessage);

    /**
     * Gets the service URL.
     * 
     * @return the service URL.
     */
    public abstract String getServiceUrl();

    /**
     * Sets the service URL.
     * 
     * @param serviceUrl the service URL to set.
     */
    public abstract void setServiceUrl(String serviceUrl);
}
