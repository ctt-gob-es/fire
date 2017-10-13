/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.clavefirma.test.services;

/**
 * C&oacute;digos de error que se han creado para trasmitirlos como errores HTTP personalizados.
 */
public class CustomHttpErrors {

	/** C&oacute;digo de error que identifica que el usuario no tiene certificados de firma disponibles. */
	public static final int HTTP_ERROR_NO_CERT = 522;
	/** C&oacute;digo de error que identifica que no existe el usuario indicado. */
	public static final int HTTP_ERROR_UNKNOWN_USER = 523;
	/** C&oacute;digo de error que identifica que los certificados de firma del usuario est&aacute;n bloqueados. */
	public static final int HTTP_ERROR_BLOCKED_CERT = 524;
	/** C&oacute;digo de error que identifica que el usuario ya tiene certificados v&aacute;lidos y no es posible expedirle otros. */
	public static final int HTTP_ERROR_EXISTING_CERTS = 525;
	/** C&oacute;digo de error que identifica que el usuario realiz&oacute; un registro d&eacute;bil y no puede tener certificados de firma. */
	public static final int HTTP_ERROR_WEAK_REGISTRY = 530;
}
