/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.connector.clavefirma;

import com.openlandsw.rss.gateway.exception.SafeCertGateWayException;

/**
 * Clase de ayuda para la identificacion de los errores emitidos por
 * el servicio de custodia de Cl@ve Firma.
 */
public class ClaveFirmaErrorManager {

	/** Error de usuario desconocido devuelto por la pasarela. */
	static final String ERROR_CODE_UNKNOWN_USER = "OPQUEFIL00003"; //$NON-NLS-1$

	/** Error de registro d&eacute;bil devuelto por la pasarela. */
	static final String ERROR_CODE_WEAK_REGISTRY = "OPSTR00023"; //$NON-NLS-1$

	/** Error de registro d&eacute;bil devuelto por el GCC. */
	static final String ERROR_CODE_GCC_WEAK_REGISTRY = "ADMINWS_0039"; //$NON-NLS-1$

	private static final String ERROR_TEXT_CERT_AVAILABLE = "El titular ya dispone de un certificado para el uso indicado"; //$NON-NLS-1$

	private ClaveFirmaErrorManager() {
		// No se puede instanciar
	}

	/**
	 * Indica si el error se debe a que ya existe un certificado para el uso indicado.
	 * @param e Excepcion emitida.
	 * @return {@code true} si el error se debe a que el usuario ya tiene un certificado
	 * de este tipo, {@code false} en caso contrario.
	 */
	public static boolean isCertAvailable(final SafeCertGateWayException e) {
		return e.getMessage() != null &&
				e.getMessage().startsWith(ERROR_TEXT_CERT_AVAILABLE);
	}
}
