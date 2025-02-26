package es.gob.fire.web.exception;

public class WebAdminException extends Exception {

    /**
     * Class serial version.
     */
    private static final long serialVersionUID = -3450587375035220833L;

    /**
     * Constructor method for the class WebAdminException.java.
     */
    public WebAdminException() {
	super();
    }

    /**
     * Constructor method for the class WebAdminException.java.
     * @param message Error message.
     */
    public WebAdminException(String message) {
	super(message);
    }

    /**
     * Constructor method for the class WebAdminException.java.
     * @param cause Error cause.
     */
    public WebAdminException(Throwable cause) {
	super(cause);

    }

    /**
     * Constructor method for the class WebAdminException.java.
     * @param message Error message.
     * @param cause Error cause.
     */
    public WebAdminException(String message, Throwable cause) {
	super(message, cause);
    }

}
