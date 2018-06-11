/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.upgrade;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;

import org.apache.axis.AxisFault;
import org.apache.axis.Handler;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;

/**
 * Clase para la conexi&oacute;n con la Plataforma @firma.
 */
public final class PlatformWsHelper {

    static final String CONFIG_FILE = "platform.properties" ;//$NON-NLS-1$

	/** Variable de entorno que determina el directorio en el que buscar el fichero de configuraci&oacute;n. */
	private static final String ENVIRONMENT_VAR_CONFIG_DIR = "fire.config.path"; //$NON-NLS-1$

	/** Variable de entorno antigua que determinaba el directorio en el que buscar el fichero
	 * de configuraci&oacute;n. Se utiliza si no se ha establecido la nueva variable. */
	private static final String ENVIRONMENT_VAR_CONFIG_DIR_OLD = "clavefirma.config.path"; //$NON-NLS-1$

    private static final Logger LOGGER = Logger.getLogger(PlatformWsHelper.class.getName());

    private static Handler REQUEST_HANDLER;
    private static String WEB_SERVICES_TIMEOUT;

    static String SERVICE_SIGNUPGRADE;
    static String SERVICE_CERTVERIFY;

    private String AFIRMA_ENDPOINT;

    /**
     * Inicializa el objeto para que pueda conectar con la Plataforma @firma.
     * @throws ConfigFileNotFoundException Cuando no encuentra el fichero de configuraci&oacute;n.
     */
    void init() throws ConfigFileNotFoundException {
    	init(loadConfig());
    }

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
                    "Error estableciendo la configuracion del cliente del Servicio Web: " + e, e //$NON-NLS-1$
            );
        }

        WEB_SERVICES_TIMEOUT = config.getProperty(
                "webservices.timeout", "25000"); //$NON-NLS-1$ //$NON-NLS-2$

        SERVICE_SIGNUPGRADE = config
                .getProperty("webservices.service.signupgrade"); //$NON-NLS-1$
        SERVICE_CERTVERIFY = config
                .getProperty("webservices.service.certverify"); //$NON-NLS-1$

        this.AFIRMA_ENDPOINT = config.getProperty("webservices.endpoint"); //$NON-NLS-1$
    }

    byte[] doPlatformCall(final String inputDss, final String serviceName)
            throws IOException, PlatformWsException {

        final Service service = new Service();
        final Call call;
        try {
            call = (Call) service.createCall();
        }
        catch (final ServiceException e) {
            throw new PlatformWsException(
                "Error al crear la llamada al servicio de actualizacion de firma de la Plataforma @firma: " + e, e //$NON-NLS-1$
            );
        }

        // Se configura del endponit del servicio
        call.setTargetEndpointAddress(new java.net.URL(this.AFIRMA_ENDPOINT
                + serviceName));

        final String servicioDSS = "verify"; //$NON-NLS-1$
        call.setOperationName(new QName("http://soapinterop.org/", servicioDSS)); //$NON-NLS-1$
        call.setTimeout(new Integer(WEB_SERVICES_TIMEOUT));
        call.setClientHandlers(REQUEST_HANDLER, null);

        // Se envia el DSS
        final String ret;
        try {
            ret = (String) call.invoke(new Object[] { inputDss });
        } catch (final RemoteException e) {
            throw new PlatformWsException(
                    "Error en la invocacion al servicio de actualizacion de firma de la Plataforma @firma: " + e, e //$NON-NLS-1$
            );
        }

        return ret.getBytes("UTF-8"); //$NON-NLS-1$
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

    /**
	 * Carga el fichero de configuraci&oacute;n del m&oacute;dulo o lo devuelve directamente si ya
	 * tuviese cargado.
	 * @return Propiedades de fichero de configuraci&oacute:n.
	 * @throws ConfigFileNotFoundException Cuando no se encuentra o no se puede cargar el fichero de configuraci&oacute;n.
	 */
	public static Properties loadConfig() throws  ConfigFileNotFoundException{

		InputStream is = null;
		final Properties config = new Properties();
		try {
			String configDir = System.getProperty(ENVIRONMENT_VAR_CONFIG_DIR);
			if (configDir == null) {
				configDir = System.getProperty(ENVIRONMENT_VAR_CONFIG_DIR_OLD);
			}
			if (configDir != null) {
				final File configFile = new File(configDir, CONFIG_FILE).getCanonicalFile();
				if (!configFile.isFile() || !configFile.canRead()) {
					LOGGER.warning(
							"No se encontro el fichero " + CONFIG_FILE + " en el directorio configurado en la variable " + //$NON-NLS-1$ //$NON-NLS-2$
									ENVIRONMENT_VAR_CONFIG_DIR + ": " + configFile.getAbsolutePath() + //$NON-NLS-1$
									"\nSe buscara en el CLASSPATH."); //$NON-NLS-1$
				}
				else {
					is = new FileInputStream(configFile);
				}
			}

			if (is == null) {
				is = PlatformWsHelper.class.getResourceAsStream('/' + CONFIG_FILE);
			}

			config.load(is);
			is.close();
		}
		catch(final NullPointerException e){
			LOGGER.severe("No se ha encontrado el fichero de configuracion: " + e); //$NON-NLS-1$
			if (is != null) {
				try { is.close(); } catch (final Exception ex) { /* No hacemos nada */ }
			}
			throw new ConfigFileNotFoundException("No se ha encontrado el fichero de propiedades " + CONFIG_FILE, CONFIG_FILE, e); //$NON-NLS-1$
		}
		catch (final Exception e) {
			LOGGER.severe("No se pudo cargar el fichero de configuracion " + CONFIG_FILE); //$NON-NLS-1$
			if (is != null) {
				try { is.close(); } catch (final Exception ex) { /* No hacemos nada */ }
			}
			throw new ConfigFileNotFoundException("No se pudo cargar el fichero de configuracion " + CONFIG_FILE, CONFIG_FILE, e); //$NON-NLS-1$
		}
		finally {
			if (is != null) {
				try { is.close(); } catch (final Exception ex) { /* No hacemos nada */ }
			}
		}
		return config;
	}
}
