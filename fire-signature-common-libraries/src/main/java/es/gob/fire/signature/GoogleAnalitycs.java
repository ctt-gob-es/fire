/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.signature;


import java.util.logging.Logger;

import com.dmurph.tracking.AnalyticsConfigData;
import com.dmurph.tracking.JGoogleAnalyticsTracker;
import com.dmurph.tracking.JGoogleAnalyticsTracker.GoogleAnalyticsVersion;

/**
 * Clase para la recopilaci&oacute;n de estad&iacute;sticas con Google Analytics.
 */
public class GoogleAnalitycs {

	private final Logger LOGGER = Logger.getLogger(GoogleAnalitycs.class.getName());

	private final String eventName;

	JGoogleAnalyticsTracker tracker;

	/**
	 * Crea el objeto para la recopilaci&oacute;n de estad&iacute;sticas asoci&aacute;ndolo a un
	 * c&oacute;digo de rastreo.
	 * @param trackingCode C&oacute;digo de rastreo.
	 * @param eventName Nombre del evento que debe registrarse .Equivalente al
	 * t&iacute;tulo de la p&aacute;gina de cara al registro.
	 */
	public GoogleAnalitycs(final String trackingCode, final String eventName) {

		this.eventName = eventName;

		if (trackingCode != null && !trackingCode.isEmpty()) {
			try {
				final AnalyticsConfigData config = new AnalyticsConfigData(trackingCode);
				this.tracker = new JGoogleAnalyticsTracker(config, GoogleAnalyticsVersion.V_4_7_2);
			}
			catch(final Exception e) {
				this.LOGGER.warning("Error registrando datos en Google Analytics: " + e); //$NON-NLS-1$
				this.tracker = null;
			}
		}
		else {
			this.tracker = null;
		}
	}

	/**
	 * Registra un nuevo acceso a la aplicaci&oacute;n.
	 * @param origin Aplicaci&oacute;n. origen desde el que se realiza la petici&oacute;n.
	 */
	public void trackRequest(final String origin) {
		if (this.tracker != null) {
			new Thread(new Track(this.tracker, this.eventName, origin)).start();
		}
	}

	/**
	 * Registra un nuevo acceso a la aplicaci&oacute;n identificando de forma concreta
	 * el evento que se ejecuta.
	 * @param origin Aplicaci&oacute;n. origen desde el que se realiza la petici&oacute;n.
	 * @param event Evento que se debe notificar.
	 */
	public void trackRequest(final String origin, final String event) {
		if (this.tracker != null) {
			new Thread(new Track(this.tracker, event, origin)).start();
		}
	}

	/**
	 * Acceso que se manda a registrar a GoogleAnalytics.
	 */
	class Track implements Runnable {

		private static final String APP_NAME = "ClaveFirma"; //$NON-NLS-1$

		final JGoogleAnalyticsTracker tckr;
		final String service;
		final String origin;

		public Track(final JGoogleAnalyticsTracker tracker, final String service, final String origin) {
			this.tckr = tracker;
			this.service = service;
			this.origin = origin;
		}

		@Override
		public void run() {
			this.tckr.trackPageView(
					APP_NAME,
					this.service,
					this.origin);
		}
	}
}
