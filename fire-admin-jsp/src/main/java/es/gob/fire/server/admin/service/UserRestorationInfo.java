package es.gob.fire.server.admin.service;

import java.util.Date;

public class UserRestorationInfo {

	private String  id;
	private String name;
	private String codeInfo;
	private Date renovationDate;
	private boolean restoreExpired;


	public UserRestorationInfo(){

		this.id=null;
		this.name=null;
		this.codeInfo=null;
		this.renovationDate=null;
		this.restoreExpired= false;
	}

	 /**
	    * Establece el c&oacute;digo de renovaci&oacute;n de contrase&ntilde;a
	    * @return c&oacute;digo de renovaci&oacute;n de contrase&ntilde;a.
	    */

	public String getCodeInfo() {
		return this.codeInfo;
	}
	/**
	  * Obtiene el c&oacute;digo al que se le va a enviar el c&oacute;digo de renovaci&oacute;n de contrase&ntilde;a
	  * @param El c&oacute;digo del usuario al que se le va a enviar el c&oacute;digo de renovaci&oacute;n de contrase&ntilde;a
	  */

	public void setCodeInfo(final String codeInfo) {
		this.codeInfo = codeInfo;
	}

	/**
	 * Establece la fecha de creacion del c&oacute;digo de renovaci&oacute;n de contrase&ntilde;a
	 * @return La fecha de creaci&oacute;n c&oacute;digo de renovaci&oacute;n de contrase&ntilde;a.
	 */
	public Date getRenovationDate() {
		return this.renovationDate;
	}
	/**
	  * Obtiene la fecha de creacion de la url al que se le va a enviar el c&oacute;digo de renovaci&oacute;n de contrase&ntilde;a
	  * @param La fecha de creacion de la url al que se le va a enviar el c&oacute;digo de renovaci&oacute;n de contrase&ntilde;a
	  */

	public void setRenovationDate(final Date renovationDate) {
		this.renovationDate = renovationDate;
	}

	/**
	 * Establece el tiempo del c&oacute;digo de renovaci&oacute;n de contrase&ntilde;a
	 * @return El tiempo del c&oacute;digo de renovaci&oacute;n de contrase&ntilde;a
	 */
	 public boolean hasRestoreExpire() {
	    	return this.restoreExpired;
	    	    }
	 /**
	  * Obtiene el tiempo del c&oacute;digo al que se le va a enviar el c&oacute;digo de renovaci&oacute;n de contrase&ntilde;a
	  * @param el tiempo del c&oacute;digo del usuario al que se le va a enviar el c&oacute;digo de renovaci&oacute;n de contrase&ntilde;a
	  */
	 public void setRestoreExpired(final boolean restoreExpired) {
			this.restoreExpired = restoreExpired;
	    }

	 /**
	  * Establece el id del usuario al que se le va a enviar el c&oacute;digo de renovaci&oacute;n de contrase&ntilde;a
	  * @return El id del usuario al que se le va a enviar el c&oacute;digo de renovaci&oacute;n de contrase&ntilde;a
	  */
		public String getId() {
			return this.id;
		}
		/**
		  * Obtiene el id del usuario al que se le va a enviar el c&oacute;digo de renovaci&oacute;n de contrase&ntilde;a
		  * @param El id del usuario al que se le va a enviar el c&oacute;digo de renovaci&oacute;n de contrase&ntilde;a
		  */
		public void setId(final String id) {
			this.id = id;
		}


		/**
		  * Establece el nombre del usuario al que se le va a enviar el c&oacute;digo de renovaci&oacute;n de contrase&ntilde;a
		  * @return El nombre del usuario al que se le va a enviar el c&oacute;digo de renovaci&oacute;n de contrase&ntilde;a
		  */
		public String getName() {
			return this.name;
		}
		/**
		  * Obtiene el nombre del usuario al que se le va a enviar el c&oacute;digo de renovaci&oacute;n de contrase&ntilde;a
		  * @param el nombre del usuario al que se le va a enviar el c&oacute;digo de renovaci&oacute;n de contrase&ntilde;a
		  */
		public void setName(final String name) {
			this.name = name;
		}


}
