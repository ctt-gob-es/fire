/*
/*******************************************************************************
 * Copyright (C) 2018 MINHAFP, Gobierno de España
 * This program is licensed and may be used, modified and redistributed under the  terms
 * of the European Public License (EUPL), either version 1.1 or (at your option)
 * any later version as soon as they are approved by the European Commission.
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and
 * more details.
 * You should have received a copy of the EUPL1.1 license
 * along with this program; if not, you may find it at
 * http:joinup.ec.europa.eu/software/page/eupl/licence-eupl
 ******************************************************************************/
/**
 * <b>File:</b><p>es.gob.fire.exception.FireException.java.</p>
 * <b>Description:</b><p>Wrapper class to encapsulate exceptions thrown
 * by the platform.</p>
 * <b>Project:</b><p>Horizontal platform of validation services of multiPKI
 * certificates and electronic signature.</p>
 * <b>Date:</b><p>15/05/2020.</p>
 * @author Gobierno de España.
 * @version 1.0, 15/05/2020.
 */
package es.gob.fire.exceptions;

import es.gob.fire.commons.log.Logger;
import es.gob.fire.i18n.ICommonsUtilLogMessages;
import es.gob.fire.i18n.Language;

/**
 * <p>Class for encapsulate exceptions thrown by the platform.</p>
 * <b>Project:</b><p>Horizontal platform of validation services of multiPKI
 * certificates and electronic signature.</p>
 * @version 1.1, 14/03/2023.
 */
public class FireException extends Exception {
	
	/**
	 * Attribute that represents the serial version of the class .
	 */
	private static final long serialVersionUID = 2247747911833885326L;

	/**
	 * Attribute that represents the object that manages the log of the class.
	 */
	private static final Logger LOGGER = Logger.getLogger(FireException.class);
	
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
	public FireException() {
		super();
	}

	public FireException(final Exception exceptionParam) {
		super();
		exception = exceptionParam;
		LOGGER.debug(Language.getResCommonsUtilsFire(ICommonsUtilLogMessages.EXCEPTION_001), this);
	}
	
	/**
	 * Constructor method for the class AfirmaException.java.
	 * @param errorCodeParam Error code.
	 * @param errorDescParam Description for the error.
	 */
	public FireException(final String errorDescParam) {
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
	public FireException(final String errorCodeParam, final String errorDescParam) {
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
	public FireException(final String errorCodeParam, final String errorDescParam, final Exception exceptionParam) {
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
