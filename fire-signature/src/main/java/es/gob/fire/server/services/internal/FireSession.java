/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.services.internal;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import es.gob.fire.signature.ConfigManager;

/**
 * Sesi&oacute;n de FIRe en la que se almacenan los datos de una transacci&oacute;n.
 */
public class FireSession implements Serializable {

	/** Serial Id. */
	private static final long serialVersionUID = 2379947907716059060L;

	/** Tiempo m&aacute;ximo de inactividad de una sesi&oacute;n. */
	private static final long MAX_INACTIVE_INTERVAL = Math.max(10000, ConfigManager.getTempsTimeout());

	private final String transactionId;
	private final Map<String, Object> ssData;
	private long expirationTime;

	private FireSession(final String trId, final Map<String, Object> fireSessionData) {
    	this.transactionId = trId;
		this.ssData = fireSessionData;
		this.expirationTime = System.currentTimeMillis() + MAX_INACTIVE_INTERVAL;
	}

	/**
	 * Carga una sesi&oacute;n de FIRe de la sesi&oacute;n web proporcionada.
	 * @param trId Identificador de la transacci&oacute;n que se debe cargar.
	 * @param httpSession Sesi&oacute;n web.
	 * @return Datos de la sesi&oacute;n o {@code null} si no se encuentran
	 * los datos de la transacci&oacute;n indicada.
	 */
	static FireSession loadFireSession(final String trId, final HttpSession httpSession) {

		if (httpSession == null) {
    		return null;
    	}

    	return (FireSession) httpSession.getAttribute(trId);
	}

	/**
	 * Crea una nueva sesi&oacute;n de FIRe en la que se va a procesar una transacci&oacute;n.
	 * @param trId Identificador de transacci&oacute;n.
	 * @return Sesi&oacute;n de FIRe.
	 */
	public static FireSession newSession(final String trId) {

		final Map<String, Object> sessionData = new HashMap<>();
		sessionData.put(ServiceParams.SESSION_PARAM_TRANSACTION_ID, trId);

		return new FireSession(trId, sessionData);
	}

	/**
	 * Crea una nueva sesi&oacute;n de FIRe en la que se va a procesar una transacci&oacute;n.
	 * @param id Identificador de transacci&oacute;n.
	 * @param sessionData Datos de la sesi&oacute;n.
	 * @param expirationTime Momento del tiempo en el que expirara la sesi&oacute;n.
	 * @return Sesi&oacute;n de FIRe.
	 */
	public static FireSession newSession(final String trId, final Map<String, Object> sessionData, final long expirationTime) {

		final FireSession session = new FireSession(trId, sessionData);
		session.expirationTime = expirationTime;

		return session;
	}

	/**
	 * Recupera el ID de transacci&oacute;n.
	 * @return Identificador de la transacci&oacute;n gestionada por esta sesi&oacute;n.
	 */
	public String getTransactionId() {
		return this.transactionId;
	}

	/**
	 * Recupera la fecha de expiraci&oacute;n de la sesi&oacute;n en milisegundos.
	 * @return Fecha de expiraci&oacute;n.
	 */
	public long getExpirationTime() {
		return this.expirationTime;
	}

	/**
	 * Recupera un atributo de la sesi&oacute;n en forma de cadena de texto.
	 * @param attr Atributo que se desea recuperar.
	 * @return Valor del atributo o {@code null} si no existe.
	 */
	public String getString(final String attr) {

		if (!containsAttribute(attr)) {
			return null;
		}

		return (String) this.ssData.get(attr);
	}

	/**
	 * Recupera un atributo de la sesi&oacute;n.
	 * @param attr Atributo que se desea recuperar.
	 * @return Valor del atributo o {@code null} si no existe.
	 */
	public Object getObject(final String attr) {

		if (!containsAttribute(attr)) {
			return null;
		}

		return this.ssData.get(attr);
	}

	/**
	 * Indica si en la sesi&oacute;n est&aacute; declarado un atributo.
	 * @param attr Atributo que se desea comprobar.
	 * @return {@code true} si en la sesi&oacute;n se encuentra declarado el atributo,
	 * {@code false} en caso contrario.
	 */
	public boolean containsAttribute(final String attr) {
		return this.ssData.containsKey(attr);
	}

	/**
	 * Actualiza un atributo de la sesi&oacute;n.
	 * @param name Nombre del atributo.
	 * @param value Nuevo valor del atributo.
	 */
	public void setAttribute(final String name, final Object value) {
		this.ssData.put(name, value);
	}

	/**
	 * Guarda la sesion de FIRe en la sesi&oacute;n HTTP indicada.
	 * @param targetSession Sesi&oacute;n HTTP en la que guardarla.
	 */
	public void saveIntoHttpSession(final HttpSession targetSession) {
		targetSession.setAttribute(this.transactionId, this);
	}

	/**
	 * Recupera un listado con el nombre de todos los atributos
	 * contenidos en la sesi&oacute;n.
	 * @return Nombres de los atributos de la sesi&oacute;n.
	 */
	public String[] getAttributteNames() {
		return this.ssData.keySet().toArray(new String[this.ssData.size()]);
	}

	/**
	 * Obtiene el conjunto de atributos de la sesi&oacute;n.
	 * @return Conjunto de atributos.
	 */
	public Map<String, Object> getAttributtes() {
		return this.ssData;
	}

	/**
	 * Elimina un atributo de la sesi&oacute;n.
	 * @param attr Nombre del atributo a eliminar.
	 */
	public void removeAttribute(final String attr) {
		this.ssData.remove(attr);
	}

	/**
	 * Extiende la vigencia de la sesi&oacute;n el tiempo en milisegundos
	 * establecida como periodo m&aacute;ximo de inactividad.
	 */
	public void updateExpirationTime() {
		this.expirationTime = System.currentTimeMillis() + MAX_INACTIVE_INTERVAL;
	}

	/**
	 * Identifica si la sesi&oacute;n ha expirado.
	 * @return {@code true} en caso de que la sesi&oacute;n haya expirado, {@code false}
	 * en caso contrario.
	 */
	public boolean isExpired() {
		return System.currentTimeMillis() > this.expirationTime;
	}

	/**
	 * Elimina los datos de la sesi&oacute;n.
	 */
	public void invalidate() {
		if (this.ssData != null) {
			try {
				this.ssData.clear();
			}
			catch (final Exception e) {
				// No hacemos nada
			}
		}
	}
}
