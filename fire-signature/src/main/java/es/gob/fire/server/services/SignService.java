/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.services;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import es.gob.afirma.core.misc.AOUtil;
import es.gob.afirma.core.misc.Base64;
import es.gob.afirma.core.signers.TriphaseData;
import es.gob.fire.alarms.Alarm;
import es.gob.fire.server.connector.FIReConnector;
import es.gob.fire.server.connector.FIReConnectorFactoryException;
import es.gob.fire.server.connector.FIReConnectorNetworkException;
import es.gob.fire.server.connector.FIReSignatureException;
import es.gob.fire.server.services.internal.AlarmsManager;
import es.gob.fire.server.services.internal.ProviderManager;
import es.gob.fire.server.services.internal.ServiceParams;
import es.gob.fire.server.services.internal.SignatureValidatorBuilder;
import es.gob.fire.signature.AplicationsDAO;
import es.gob.fire.signature.ApplicationChecking;
import es.gob.fire.signature.ConfigFilesException;
import es.gob.fire.signature.ConfigManager;
import es.gob.fire.signature.DBConnectionException;
import es.gob.fire.upgrade.ConnectionException;
import es.gob.fire.upgrade.SignatureValidator;
import es.gob.fire.upgrade.UpgradeException;
import es.gob.fire.upgrade.UpgradeResult;
import es.gob.fire.upgrade.UpgraderUtils;
import es.gob.fire.upgrade.ValidatorException;
import es.gob.fire.upgrade.VerifyException;

/** Servlet que realiza el proceso de firma. */
public final class SignService extends HttpServlet {

    private static final long serialVersionUID = -4539914450930226729L;

    private static final Logger LOGGER = Logger.getLogger(SignService.class.getName());

    // Parametros que necesitamos de la URL.
    private static final String PARAMETER_NAME_APPLICATION_ID = "appId"; //$NON-NLS-1$
    private static final String PARAMETER_NAME_OPERATION = "operation"; //$NON-NLS-1$
    private static final String PARAMETER_NAME_ALGORITHM = "algorithm"; //$NON-NLS-1$
    private static final String PARAMETER_NAME_FORMAT = "format"; //$NON-NLS-1$
    private static final String PARAMETER_NAME_CERT = "cert"; //$NON-NLS-1$
    private static final String PARAMETER_NAME_EXTRA_PARAM = "properties"; //$NON-NLS-1$
    private static final String PARAMETER_NAME_UPGRADE = "upgrade"; //$NON-NLS-1$
    private static final String PARAMETER_NAME_TRANSACTION_ID = "transactionid"; //$NON-NLS-1$
    private static final String PARAMETER_NAME_DATA = "data"; //$NON-NLS-1$
    private static final String PARAMETER_NAME_TRIPHASE_DATA = "tri"; //$NON-NLS-1$

    @Override
    public void init() throws ServletException {
    	super.init();

    	try {
	    	ConfigManager.checkConfiguration();
		}
    	catch (final Exception e) {
    		LOGGER.log(Level.SEVERE, "Error al cargar la configuracion", e); //$NON-NLS-1$
    		final String configFile = e instanceof ConfigFilesException ?
    				((ConfigFilesException) e).getFileName() : "Fichero de configuracion principal del componente central"; //$NON-NLS-1$
    		AlarmsManager.notify(Alarm.RESOURCE_CONFIG, configFile);
    		return;
    	}

    	// Configuramos el modulo de alarmas
    	AlarmsManager.init(ModuleConstants.MODULE_NAME, ConfigManager.getAlarmsNotifierClassName());
    }

    /** Recepci&oacute;n de la petici&oacute;n POST y realizaci&oacute;n de la
     * firma. */
    @Override
    protected void service(final HttpServletRequest request, final HttpServletResponse response) throws IOException {

		LOGGER.fine("Peticion recibida"); //$NON-NLS-1$

		if (!ConfigManager.isInitialized()) {
			try {
				ConfigManager.checkConfiguration();
			}
			catch (final ConfigFilesException e) {
				LOGGER.severe("Error en la configuracion del servidor: " + e); //$NON-NLS-1$
				AlarmsManager.notify(Alarm.RESOURCE_CONFIG, e.getFileName());
				response.sendError(ConfigFilesException.getHttpError(), e.getMessage());
				return;
			}
		}

        // Recepcion de los parametros.
    	final RequestParameters params = RequestParameters.extractParameters(request);

    	final String appId      = params.getParameter(PARAMETER_NAME_APPLICATION_ID);
        final String op         = params.getParameter(PARAMETER_NAME_OPERATION).toLowerCase();
        final String format     = params.getParameter(PARAMETER_NAME_FORMAT);
        final String algorithm  = params.getParameter(PARAMETER_NAME_ALGORITHM);
        final String extraParamsB64 = params.getParameter(PARAMETER_NAME_EXTRA_PARAM);
        final String certB64    = params.getParameter(PARAMETER_NAME_CERT);
        final String upgrade    = params.getParameter(PARAMETER_NAME_UPGRADE);
        final String transactId = params.getParameter(PARAMETER_NAME_TRANSACTION_ID);
        final String dataB64    = params.getParameter(PARAMETER_NAME_DATA);
        final String tdB64      = params.getParameter(PARAMETER_NAME_TRIPHASE_DATA);
        String providerName  	= params.getParameter(ServiceParams.HTTP_PARAM_CERT_ORIGIN);

        if (ConfigManager.isCheckApplicationNeeded()) {
        	LOGGER.fine("Se realizara la validacion del Id de aplicacion"); //$NON-NLS-1$
        	if (appId == null || appId.isEmpty()) {
        		LOGGER.warning("No se ha proporcionado el identificador de la aplicacion"); //$NON-NLS-1$
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
        				"No se ha proporcionado el identificador de la aplicacion"); //$NON-NLS-1$
        		return;
        	}

        	try {
        		final ApplicationChecking appCheck = AplicationsDAO.checkApplicationId(appId);
	        	if (!appCheck.isValid()) {
        			LOGGER.warning("Se proporciono un identificador de aplicacion no valido. Se rechaza la peticion"); //$NON-NLS-1$
        			response.sendError(HttpServletResponse.SC_FORBIDDEN);
        			return;
        		}
        	}
        	catch (final Exception e) {
        		LOGGER.log(Level.SEVERE, "Error grave al validar el identificador de la aplicacion", e); //$NON-NLS-1$
        		AlarmsManager.notify(Alarm.CONNECTION_DB);
	        	response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        		return;
        	}
        }
        else {
        	LOGGER.fine("No se realiza la validacion de aplicacion"); //$NON-NLS-1$
        }

    	if (ConfigManager.isCheckCertificateNeeded()){
    		LOGGER.fine("Se realizara la validacion del certificado"); //$NON-NLS-1$
    		final X509Certificate[] certificates = ServiceUtil.getCertificatesFromRequest(request);
	    	try {
				ServiceUtil.checkValidCertificate(appId, certificates);
			}
	    	catch (final DBConnectionException e) {
				LOGGER.log(Level.SEVERE, "No se pudo conectar con la base de datos", e); //$NON-NLS-1$
				AlarmsManager.notify(Alarm.CONNECTION_DB);
	        	response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
				return;
			}
	    	catch (final CertificateValidationException e) {
				LOGGER.severe("Error en la validacion del certificado: " + e); //$NON-NLS-1$
				response.sendError(e.getHttpError(), e.getMessage());
				return;
			}
    	}
    	else {
    		LOGGER.fine("No se validara el certificado");//$NON-NLS-1$
    	}


        if (dataB64 == null || dataB64.isEmpty()) {
        	LOGGER.warning("No se han proporcionado los datos a firmar"); //$NON-NLS-1$
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
    				"No se han proporcionado los datos a firmar"); //$NON-NLS-1$
    		return;
    	}

    	// Obtenemos el conector con el backend ya configurado
        final Properties config = extraParamsB64 != null ? ServiceUtil.base642Properties(extraParamsB64) : null;
        final FIReConnector connector;
        try {
        	if (providerName == null) {
        		providerName = ProviderLegacy.PROVIDER_NAME_CLAVEFIRMA;
        	}
    		connector = ProviderManager.getProviderConnector(providerName, null);
        }
        catch (final FIReConnectorFactoryException e) {
        	LOGGER.log(Level.SEVERE, String.format("No se ha podido cargar el conector del proveedor de firma: %1s", providerName), e); //$NON-NLS-1$
        	response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Error en la configuracion del conector con el servicio de custodia: " + e); //$NON-NLS-1$
            return;
        }

        final Map<String, byte[]> ret;
        try {
            ret = connector.sign(transactId);
        }
        catch (final FIReConnectorNetworkException e) {
            LOGGER.log(Level.SEVERE, "No se ha podido conectar con el proveedor de firma en la nube", e); //$NON-NLS-1$
            AlarmsManager.notify(Alarm.CONNECTION_SIGNATURE_PROVIDER, providerName);
            response.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "No se ha podido conectar con el proveedor de firma en la nube: " + e //$NON-NLS-1$
            );
            return;
        }
        catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "No se ha podido obtener el resultado de la transaccion de firma", e); //$NON-NLS-1$
            response.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "No se ha podido obtener el resultado de la transaccion de firma: " + e //$NON-NLS-1$
            );
            return;
        }

        final TriphaseData td = TriphaseData.parser(Base64.decode(tdB64, true));

        // Insertamos los PKCS#1 en la sesion trifasica
        final Set<String> keys = ret.keySet();
        for (final String key : keys) {
            LOGGER.fine("Firma " + key + " =\n" + AOUtil.hexify(ret.get(key), true)); //$NON-NLS-1$ //$NON-NLS-2$
            FIReTriHelper.addPkcs1ToTriSign(ret.get(key), key, td);
        }

        final X509Certificate signerCert;
        try {
            signerCert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate( //$NON-NLS-1$
                new ByteArrayInputStream(Base64.decode(certB64, true))
            );
        }
        catch (final Exception e) {
        	LOGGER.severe("No se ha podido decodificar el certificado del firmante: " + e); //$NON-NLS-1$
        	response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "No se ha podido decodificar el certificado proporcionado: " + e); //$NON-NLS-1$
        	return;
        }

        byte[] signResult;
        try {
            signResult = FIReTriHelper.getPostSign(
                    op,
                    format,
                    algorithm,
                    config,
                    signerCert,
                    Base64.decode(dataB64, true),
                    td
            );
        }
        catch (final FIReSignatureException e) {
            LOGGER.log(Level.WARNING,
            		"Error durante la operacion. Verifique el codigo de operacion (" + op + //$NON-NLS-1$
                    ") y el formato (" + format + ")", e); //$NON-NLS-1$ //$NON-NLS-2$
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Error durante la operacion. Verifique el codigo de operacion y el formato: " + e); //$NON-NLS-1$
            return;
        }

        // Si se ha definido un formato de actualizacion de la firma, se actualizara
        if (upgrade != null && !upgrade.isEmpty()) {
        	// Obtenemos la informacion para la configuracion particular de la mejora/validacion de la firma
        	final Properties upgraterConfig = UpgraderUtils.extractUpdaterProperties(config);

        	// Procedemos a la validacion
        	try {
        		final SignatureValidator validator = SignatureValidatorBuilder.getSignatureValidator();
        		final UpgradeResult upgradeResult = validator.upgradeSignature(signResult, upgrade, upgraterConfig);
        		signResult = upgradeResult.getResult();
        	} catch (final ConnectionException e) {
        		LOGGER.log(Level.SEVERE, "No se pudo conectar con el servicio de validacion y mejora de firmas", e); //$NON-NLS-1$
        		AlarmsManager.notify(Alarm.CONNECTION_VALIDATION_PLATFORM);
        		response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
        				"No se pudo conectar con la plataforma de validacion"); //$NON-NLS-1$
        		return;
        	} catch (final ValidatorException e) {
        		LOGGER.log(Level.SEVERE, "Error al cargar el conector con el sistema de validacion de firmas en la transaccion: " + transactId, e); //$NON-NLS-1$
        		response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
        				"Error al actualizar la firma"); //$NON-NLS-1$
        		return;
        	} catch (final UpgradeException e) {
        		LOGGER.log(Level.SEVERE, "Error al actualizar la firma de la transaccion: " + transactId, e); //$NON-NLS-1$
        		response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
        				"Error al actualizar la firma"); //$NON-NLS-1$
        		return;
        	} catch (final VerifyException e) {
        		LOGGER.log(Level.SEVERE, "La firma que se desea actualizar no es valida: " + transactId, e); //$NON-NLS-1$
        		response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
        				"La firma que se intenta actualizar no es valida"); //$NON-NLS-1$
        		return;
        	}
        }

        connector.endSign(transactId);

        // El servicio devuelve el resultado de la operacion de firma.
        final OutputStream output = ((ServletResponse) response).getOutputStream();
        output.write(signResult);
        output.flush();
        output.close();
    }
}
