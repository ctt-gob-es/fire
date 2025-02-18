package es.gob.fire.web.clave.sp.exception;

import es.gob.fire.commons.log.Logger;
import es.gob.fire.exceptions.FireException;
import es.gob.fire.i18n.ICommonsUtilLogMessages;
import es.gob.fire.i18n.Language;

public class ClaveException extends Exception {

	/**
	 * Attribute that represents the object that manages the log of the class.
	 */
	private static final Logger LOGGER = Logger.getLogger(ClaveException.class);
	
	/**
	 * Attribute that represents the error code.
	 */
	private String errorCode;

	/**
	 * Attribute that represents a description associated to the error.
	 */
	private String errorDesc;
	/**
	 * Attribute that represents a java exception associated to the error. It is optional.
	 */
	private Exception exception;
	
	/**
	 * Constructor method for the class FireException.java.
	 */
	public ClaveException() {
		super();
	}
	
	public ClaveException(final Exception exceptionParam) {
		super();
		exception = exceptionParam;
		LOGGER.debug(Language.getResCommonsUtilsFire(ICommonsUtilLogMessages.EXCEPTION_001), this);
	}
	
	/**
	 * Constructor method for the class AfirmaException.java.
	 * @param errorCodeParam Error code.
	 * @param errorDescParam Description for the error.
	 */
	public ClaveException(final String errorDescParam) {
		super(Language.getFormatResCommonsUtilsFire(ICommonsUtilLogMessages.EXCEPTION_005, new Object[ ] { errorDescParam }));
		errorDesc = errorDescParam;
		// Solamente en trace escribimos el mensaje y la excepción, por si es
		// controlada
		// posteriormente para ocultarla pero es necesario tenerla en cuenta
		// para
		// desarrollo.
		LOGGER.debug(Language.getResCommonsUtilsFire(ICommonsUtilLogMessages.EXCEPTION_001), this);
	}
	
	/**
	 * Constructor method for the class AfirmaException.java.
	 * @param errorCodeParam Error code.
	 * @param errorDescParam Description for the error.
	 */
	public ClaveException(final String errorCodeParam, final String errorDescParam) {
		super(Language.getFormatResCommonsUtilsFire(ICommonsUtilLogMessages.EXCEPTION_000, new Object[ ] { errorCodeParam, errorDescParam }));
		errorCode = errorCodeParam;
		errorDesc = errorDescParam;
		// Solamente en trace escribimos el mensaje y la excepción, por si es
		// controlada
		// posteriormente para ocultarla pero es necesario tenerla en cuenta
		// para
		// desarrollo.
		LOGGER.debug(Language.getResCommonsUtilsFire(ICommonsUtilLogMessages.EXCEPTION_001), this);
	}


	/**
	 * Constructor method for the class AfirmaException.java.
	 * @param errorCodeParam Error code.
	 * @param errorDescParam Description for the error.
	 * @param exceptionParam Exception that causes the error.
	 */
	public ClaveException(final String errorCodeParam, final String errorDescParam, final Exception exceptionParam) {
		super(Language.getFormatResCommonsUtilsFire(ICommonsUtilLogMessages.EXCEPTION_000, new Object[ ] { errorCodeParam, errorDescParam }));
		errorCode = errorCodeParam;
		errorDesc = errorDescParam;
		exception = exceptionParam;
		// Solamente en trace escribimos el mensaje y la excepción, por si es
		// controlada
		// posteriormente para ocultarla pero es necesario tenerla en cuenta
		// para
		// desarrollo.
		LOGGER.debug(Language.getResCommonsUtilsFire(ICommonsUtilLogMessages.EXCEPTION_001), this);
	}


	/**
	 * Gets the value of the attribute {@link #errorCode}.
	 * @return the value of the attribute {@link #errorCode}.
	 */
	public String getErrorCode() {
		return errorCode;
	}


	/**
	 * Sets the value of the attribute {@link #errorCode}.
	 * @param errorCodeParam The value for the attribute {@link #errorCode}.
	 */
	public void setErrorCode(String errorCodeParam) {
		this.errorCode = errorCodeParam;
	}


	/**
	 * Gets the value of the attribute {@link #errorDesc}.
	 * @return the value of the attribute {@link #errorDesc}.
	 */
	public String getErrorDesc() {
		return errorDesc;
	}


	/**
	 * Sets the value of the attribute {@link #errorDesc}.
	 * @param errorDescParam The value for the attribute {@link #errorDesc}.
	 */
	public void setErrorDesc(String errorDescParam) {
		this.errorDesc = errorDescParam;
	}


	/**
	 * Gets the value of the attribute {@link #exception}.
	 * @return the value of the attribute {@link #exception}.
	 */
	public Exception getException() {
		return exception;
	}


	/**
	 * Sets the value of the attribute {@link #exception}.
	 * @param exceptionParam The value for the attribute {@link #exception}.
	 */
	public void setException(Exception exceptionParam) {
		this.exception = exceptionParam;
	}

	/**
	 * 
	 * {@inheritDoc}
	 * @see java.lang.Throwable#toString()
	 */
	public final String toString() {

		if (exception == null) {
			return Language.getFormatResCommonsUtilsFire(ICommonsUtilLogMessages.EXCEPTION_002, new Object[ ] { errorCode, errorDesc });
		} else {
			return Language.getFormatResCommonsUtilsFire(ICommonsUtilLogMessages.EXCEPTION_003, new Object[ ] { errorCode, errorDesc, exception.toString() });
		}

	}
}
