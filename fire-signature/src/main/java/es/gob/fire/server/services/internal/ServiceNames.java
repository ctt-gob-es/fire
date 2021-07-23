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

/**
 * Clase con los identificadores de los distintos servicios que se utilizan en la
 * ejecuci&oacute;n de las operaciones de Clave Firma.
 */
public class ServiceNames {

	/** Servicio para la petici&oacute;nn de certificado. */
	public static final String PUBLIC_SERVICE_REQ_CERT = "requestCertificateService"; //$NON-NLS-1$

	 /** Servicio para la recuperaci&oacute;nn del nuevo certificado de firma en la nube. */
	public static final String PUBLIC_SERVICE_RECOVER_NEW_CERT = "recoverNewCertificateService"; //$NON-NLS-1$

	 /** Servicio interno para la autenticaci&oacuten del usuario que permite obtener los certificados de la nube. */
	public static final String PUBLIC_SERVICE_AUTH_USER = "authenticationService"; //$NON-NLS-1$

	/** Servicio interno para la selecci&oacuten del origen del certificado de firma (nube o local). */
	public static final String PUBLIC_SERVICE_CHOOSE_CERT_ORIGIN = "chooseCertificateOriginService"; //$NON-NLS-1$

	 /** Servicio para la prefirma*/
	public static final String PUBLIC_SERVICE_PRESIGN = "presignService"; //$NON-NLS-1$

	 /** Servicio para errores del Miniapplet*/
	public static final String PUBLIC_SERVICE_MINIAPPLET_ERROR = "miniappletErrorService"; //$NON-NLS-1$

	/** Servicio para operaciones correctas del Miniapplet. */
	public static final String PUBLIC_SERVICE_MINIAPPLET_SUCCESS = "miniappletSuccessService"; //$NON-NLS-1$

	/** Servicio para la cancelacion de la operaci&oacute;n. */
	public static final String PUBLIC_SERVICE_CANCEL_OPERATION = "cancelOperationService"; //$NON-NLS-1$

	/** Servicio para el guardado de datos temporales del Cliente @firma. */
	public static final String PUBLIC_SERVICE_AFIRMA_STORAGE = "storage"; //$NON-NLS-1$

	/** Servicio para la recuperaci&oacute;n de datos temporales del Cliente @firma. */
	public static final String PUBLIC_SERVICE_AFIRMA_RETRIEVE = "retrieve"; //$NON-NLS-1$

	/** Servicio de firma trif&aacute;sica del Cliente @firma. */
	public static final String PUBLIC_SERVICE_AFIRMA_TRISIGN = "triphaseSignService"; //$NON-NLS-1$

	/** Servicio de prefirma de lotes del Cliente @firma. */
	public static final String PUBLIC_SERVICE_AFIRMA_BATCH_PRESIGN = "preSignBatchService"; //$NON-NLS-1$

	/** Servicio de postfirma de lotes del Cliente @firma. */
	public static final String PUBLIC_SERVICE_AFIRMA_BATCH_POSTSIGN = "postSignBatchService"; //$NON-NLS-1$
}
