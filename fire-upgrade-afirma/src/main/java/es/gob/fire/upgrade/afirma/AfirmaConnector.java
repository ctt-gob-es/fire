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
import java.rmi.RemoteException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.xml.rpc.ServiceException;
import javax.xml.rpc.soap.SOAPFaultException;

import org.apache.axis.AxisFault;
import org.apache.axis.Handler;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;

import es.gob.fire.upgrade.ConnectionException;

/**
 * Clase para la conexi&oacute;n con la Plataforma @firma.
 */
public final class AfirmaConnector {

    private static final Logger LOGGER = Logger.getLogger(AfirmaConnector.class.getName());

    private static Handler REQUEST_HANDLER;
    private static String WEB_SERVICES_TIMEOUT;

    static String SERVICE_SIGNUPGRADE;
    static String SERVICE_RECOVERSIGNATURE;

    static String SIGNUPGRADE_OPERATION_UPGRADE = "verify"; //$NON-NLS-1$
    static String SIGNUPGRADE_OPERATION_VERIFY = "verify"; //$NON-NLS-1$
    static String RECOVERSIGN_OPERATION_ASYNC_RECOVER = "getProcessResponse"; //$NON-NLS-1$

    private String AFIRMA_ENDPOINT;

    /**
     * Inicializa el objeto para que pueda conectar con la Plataforma @firma.
     * @param config Configuraci&oacute;n a partir de la cual realizar la
     * conexi&oacute;n con la Plataforma @firma.
     */
    public void init(final Properties config) {

        setSystemParameters(
                config.getProperty("com.trustedstore.path"), //$NON-NLS-1$
                config.getProperty("com.trustedstore.password"), //$NON-NLS-1$
                config.getProperty("com.trustedstore.type") //$NON-NLS-1$
        );

        try {
            REQUEST_HANDLER = new ClientHandler(
                    generateHandlerProperties(
                            config
                                    .getProperty("webservices.authorization.ks.path"), //$NON-NLS-1$
                            config
                                    .getProperty("webservices.authorization.ks.type"), //$NON-NLS-1$
                            config
                                    .getProperty("webservices.authorization.ks.password"), //$NON-NLS-1$
                            config
                                    .getProperty("webservices.authorization.ks.cert.alias"), //$NON-NLS-1$
                            config
                                    .getProperty("webservices.authorization.ks.cert.password") //$NON-NLS-1$
                    ));
        } catch (final AxisFault e) {
            throw new IllegalStateException(
                    "Error estableciendo la configuracion del cliente del Servicio Web", e //$NON-NLS-1$
            );
        }

        WEB_SERVICES_TIMEOUT = config.getProperty("webservices.timeout", "25000"); //$NON-NLS-1$ //$NON-NLS-2$

        SERVICE_SIGNUPGRADE = config.getProperty("webservices.service.signupgrade"); //$NON-NLS-1$

        SERVICE_RECOVERSIGNATURE = config.getProperty("webservices.service.recoversignature"); //$NON-NLS-1$

        this.AFIRMA_ENDPOINT = config.getProperty("webservices.endpoint"); //$NON-NLS-1$
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
     */
    byte[] doPlatformCall(final String inputDss, final String serviceName, final String operation)
            throws IOException, PlatformWsException, ConnectionException {

        final Service service = new Service();
        final Call call;
        try {
            call = (Call) service.createCall();
        }
        catch (final ServiceException e) {
            throw new ConnectionException(
                "Error al crear la llamada al servicio de actualizacion de firma de la Plataforma @firma", e //$NON-NLS-1$
            );
        }

        // Se configura del endponit del servicio
        call.setTargetEndpointAddress(new java.net.URL(this.AFIRMA_ENDPOINT + serviceName));

        //call.setOperationName(new QName("http://soapinterop.org/", operation)); //$NON-NLS-1$
        call.setOperationName(operation);
        call.setTimeout(new Integer(WEB_SERVICES_TIMEOUT));
        call.setClientHandlers(REQUEST_HANDLER, null);

        // Se envia el DSS
        final String ret;
        try {
            ret = (String) call.invoke(new Object[] { inputDss });
        } catch (final RemoteException e) {
            throw new ConnectionException(
                    "No se pudo conectar con la Plataforma @firma", e //$NON-NLS-1$
            );
        } catch (final SOAPFaultException e) {
            throw new PlatformWsException(
                    "Error en la invocacion al servicio de actualizacion de firma de la Plataforma @firma", e //$NON-NLS-1$
            );
        }

        return ret.getBytes(StandardCharsets.UTF_8);
    }

    private static void setSystemParameters(final String tsPath, final String tsPass, final String tsType) {
   		setSystemProperty("javax.net.ssl.trustStore", tsPath); //$NON-NLS-1$
   		setSystemProperty("javax.net.ssl.trustStorePassword", tsPass); //$NON-NLS-1$
   		setSystemProperty("javax.net.ssl.trustStoreType", tsType); //$NON-NLS-1$
    }

    /**
     * Establece una propiedad del sistema.
     * @param property Propiedad a establecer.
     * @param value Valor que asinar a la propiedad.
     */
    private static void setSystemProperty(final String property, final String value) {
    	if (value != null) {
    		System.setProperty(property, value);
    	}
    	else {
    		LOGGER.warning("No se ha encontrado la propiedad \"" + property + "\" en el fichero de configuracion"); //$NON-NLS-1$ //$NON-NLS-2$
    	}
    }

    private static Properties generateHandlerProperties(
            final String authorizationKeyStorePath,
            final String authorizationKeyStoreType,
            final String authorizationKeyStorePassword,
            final String authorizationKeyStoreCertAlias,
            final String authorizationKeyStoreCertPassword) {
        final Properties config = new Properties();
        config.setProperty(
                "security.keystore.location", authorizationKeyStorePath); //$NON-NLS-1$
        config.setProperty(
        		"security.keystore.type", authorizationKeyStoreType); //$NON-NLS-1$
        config.setProperty(
                "security.keystore.password", authorizationKeyStorePassword); //$NON-NLS-1$
        config.setProperty(
                "security.keystore.cert.alias", authorizationKeyStoreCertAlias); //$NON-NLS-1$
        config.setProperty(
                "security.keystore.cert.password", authorizationKeyStoreCertPassword); //$NON-NLS-1$
        return config;
    }
}
