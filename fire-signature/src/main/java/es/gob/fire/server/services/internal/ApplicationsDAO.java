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


import java.io.IOException;


/**
 * DAO para la gesti&oacute;n de aplicaciones dadas de alta en el sistema.
 */
public interface ApplicationsDAO {

	/**
	 * Obtiene la informaci&oacute;n necesaria para validar el acceso de una aplicaci&oacute;n. En caso de
	 * estar deshabilitada la aplicaci&oacute;n, puede omitirse la informaci&oacute;n de los certificados
	 * necesarios para la autenticaci&oacute;n.
	 * @param appId Identificador de la aplicaci&oacute;n.
	 * @param trAux Informaci&oacute;n auxiliar de la transacci&oacute;n.
	 * @return Informaci&oacute;n de acceso o {@code null} si la aplicacion no tiene informaci&oacute;n de acceso asociada.
	 * @throws IOException Cuando no se puede realizar la comprobaci&oacute;n.
	 */
	ApplicationAccessInfo getApplicationAccessInfo(String appId, TransactionAuxParams trAux)
			throws IOException;
}
