/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.upgrade.afirma;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import es.gob.fire.upgrade.ConnectionException;
import es.gob.fire.upgrade.afirma.ws.WSServiceInvokerException;
import es.gob.fire.upgrade.afirma.ws.WebServiceInvoker;
import es.gob.fire.upgrade.afirma.ws.WebServiceInvokerConfig;

/**
 * Clase para la conexi&oacute;n con la Plataforma @firma.
 */
public final class AfirmaConnector {

    public static final String SIGNUPGRADE_OPERATION_UPGRADE = "verify"; //$NON-NLS-1$
    public static final String SIGNUPGRADE_OPERATION_VERIFY = "verify"; //$NON-NLS-1$
    public static final String RECOVERSIGN_OPERATION_ASYNC_RECOVER = "getProcessResponse"; //$NON-NLS-1$

    private String signUpgradeService;
    private String recoverSignatureService;

    private WebServiceInvoker wsInvoker;

    /**
     * Inicializa el objeto para que pueda conectar con la Plataforma @firma.
     * @param config Configuraci&oacute;n a partir de la cual realizar la
     * conexi&oacute;n con la Plataforma @firma.
     */
    public void init(final Properties config) {

    	final WebServiceInvokerConfig wsConfig = new WebServiceInvokerConfig(config);

    	this.wsInvoker = new WebServiceInvoker(wsConfig);

        this.signUpgradeService = wsConfig.getServiceVerify();
        this.recoverSignatureService = wsConfig.getServiceRecovery();
    }

    /**
     * Realiza una llamada a la Plataforma @firma.
     * @param inputDss Mensaje a enviar.
     * @param serviceName Nombre del servicio.
     * @param operation M&eacute;todo del servicio.
     * @return Respuesta del servicio.
     * @throws ConnectionException Cuando ocurre un error en la llamada al servicio.
     * @throws PlatformWsException Cuando ocurre un error la procesar la petici&oacute;n.
     * @throws IOException Cuando el endpoint o el nombre del servicio no tienen un formato v&aacute;lido.
     * @throws WSServiceInvokerException
     */
    byte[] doPlatformCall(final String inputDss, final String serviceName, final String operation)
            throws WSServiceInvokerException {

    	final Object response = this.wsInvoker.performCall(inputDss, serviceName, operation);

    	byte[] res;
    	if (response instanceof String) {
    		res = ((String) response).getBytes(StandardCharsets.UTF_8);
    	}
    	else if (response instanceof byte[]) {
    		res = (byte[]) response;
    	}
    	else {
    		res = null;
    	}

        return res;
    }

    /**
     * Realiza una petici&oacute;n de actualizaci&oacute;n de firma a la Plataforma @firma.
     * @param inputDss Mensaje a enviar.
     * @return Respuesta del servicio.
     * @throws WSServiceInvokerException Cuando ocurre un error en la llamada.
     */
    byte[] upgradeSignature(final String inputDss) throws WSServiceInvokerException {
    	return doPlatformCall(inputDss, this.signUpgradeService, SIGNUPGRADE_OPERATION_UPGRADE);
    }

    /**
     * Realiza una petici&oacute;n de validaci&oacute;n de firma a la Plataforma @firma.
     * @param inputDss Mensaje a enviar.
     * @return Respuesta del servicio.
     * @throws WSServiceInvokerException Cuando ocurre un error en la llamada.
     */
    byte[] verifySignature(final String inputDss) throws WSServiceInvokerException {
    	return doPlatformCall(inputDss, this.signUpgradeService, SIGNUPGRADE_OPERATION_VERIFY);
    }

    /**
     * Realiza una petici&oacute;n de recuperaci&oacute;n de una firma enviada a actualizar
     * anteriormente.
     * @param inputDss Mensaje a enviar.
     * @return Respuesta del servicio.
     * @throws WSServiceInvokerException Cuando ocurre un error en la llamada.
     */
    byte[] recoverSignatureAsync(final String inputDss) throws WSServiceInvokerException {
    	return doPlatformCall(inputDss, this.recoverSignatureService, RECOVERSIGN_OPERATION_ASYNC_RECOVER);
    }
}
