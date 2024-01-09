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
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import es.gob.fire.alarms.Alarm;
import es.gob.fire.server.document.FIReDocumentManager;
import es.gob.fire.server.document.FireAsyncDocumentManager;
import es.gob.fire.server.services.FIReDocumentManagerFactory;
import es.gob.fire.server.services.FIReError;
import es.gob.fire.server.services.RequestParameters;
import es.gob.fire.server.services.Responser;
import es.gob.fire.upgrade.ConnectionException;
import es.gob.fire.upgrade.SignatureValidator;
import es.gob.fire.upgrade.UpgradeException;
import es.gob.fire.upgrade.UpgradeResult;
import es.gob.fire.upgrade.UpgradeResult.State;
import es.gob.fire.upgrade.UpgraderUtils;
import es.gob.fire.upgrade.ValidatorException;


/**
 * Manejador encargado de recuperar de la plataforma de actualizaci&oacute;n de firmas una firma
 * enviada anteriormente y que requer&iacute;a la espera de un periodo de gracia antes de su
 * obtenci&oacute;n.
 */
public class RecoverUpdatedSignManager {

	private static final Logger LOGGER = Logger.getLogger(RecoverUpdatedSignManager.class.getName());

	private static final String CONFIG_PARAM_ALLOW_PARTIAL_UPGRADE = "allowPartialUpgrade"; //$NON-NLS-1$

	/**
	 * Obtiene de la plataforma de actualizaci&oacute;n la firma actualizada.
	 * @param params Par&aacute;metros extra&iacute;dos de la petici&oacute;n.
	 * @param trAux Informaci&oacute;n auxiliar de la transacci&oacute;n.
	 * @param response Respuesta de la petici&oacute;n.
	 * @throws IOException Cuando se produce un error de lectura o env&iacute;o de datos.
	 */
	public static void recoverSignature(final RequestParameters params, final TransactionAuxParams trAux,
			final HttpServletResponse response) throws IOException {

		// Recogemos los parametros proporcionados en la peticion
		final String appId = params.getParameter(ServiceParams.HTTP_PARAM_APPLICATION_ID);
		final String asyncId = params.getParameter(ServiceParams.HTTP_PARAM_DOCUMENT_ID);
		final String upgrade = params.getParameter(ServiceParams.HTTP_PARAM_UPGRADE);
        final String configB64  = params.getParameter(ServiceParams.HTTP_PARAM_CONFIG);

		final LogTransactionFormatter logF = trAux.getLogFormatter();

        // Comprobamos que se hayan prorcionado los parametros indispensables
        if (asyncId == null || asyncId.isEmpty()) {
        	LOGGER.warning(logF.f("No se ha proporcionado el ID devuelto por la plataforma para la recuperacion de la firma")); //$NON-NLS-1$
        	Responser.sendError(response, FIReError.PARAMETER_ASYNC_ID_NEEDED);
            return;
        }

		LOGGER.fine(logF.f("Peticion bien formada")); //$NON-NLS-1$

		// Cargamos la configuracion de la operacion
        Properties config = null;
    	if (configB64 != null && !configB64.isEmpty()) {
    		try {
    			config = PropertiesUtils.base642Properties(configB64);
    		}
    		catch (final Exception e) {
            	LOGGER.log(Level.WARNING, logF.f("Error al decodificar las configuracion de los proveedores de firma"), e); //$NON-NLS-1$
                Responser.sendError(response, FIReError.PARAMETER_CONFIG_TRANSACTION_INVALID);
                return;
			}
    	}

		final TransactionConfig connConfig = config != null
				? new TransactionConfig((Properties) config.clone())
				: null;

        // Obtenemos la informacion para la configuracion particular de la mejora/validacion de la firma
        final Properties upgraterConfig = UpgraderUtils.extractUpdaterProperties(config);

        UpgradeResult upgradeResult;
        try {
        	final SignatureValidator validator = SignatureValidatorBuilder.getSignatureValidator(logF);
        	upgradeResult = validator.recoverUpgradedSignature(asyncId, upgrade, upgraterConfig);
        } catch (final ValidatorException e) {
        	LOGGER.log(Level.SEVERE, logF.f("Error interno al cargar el conector con el sistema de actualizacion de firmas"), e); //$NON-NLS-1$
        	Responser.sendError(response, FIReError.INTERNAL_ERROR);
        	return;
        } catch (final ConnectionException e) {
			LOGGER.log(Level.SEVERE, logF.f("No se pudo conectar con el servicio de validacion y mejora de firmas"), e); //$NON-NLS-1$
			AlarmsManager.notify(Alarm.CONNECTION_VALIDATION_PLATFORM);
        	Responser.sendError(response, FIReError.UPGRADING_SIGNATURE);
        	return;
		} catch (final UpgradeException e) {
        	LOGGER.log(Level.SEVERE, logF.f("Error devuelto por la plataforma de actualizacion"), e); //$NON-NLS-1$
        	Responser.sendError(response, FIReError.UPGRADING_SIGNATURE);
        	return;
        } catch (final Exception e) {
        	LOGGER.log(Level.SEVERE, logF.f("Error de conexion al recuperar la firma actualizada"), e); //$NON-NLS-1$
        	Responser.sendError(response, FIReError.UPGRADING_SIGNATURE);
        	return;
        }

        boolean allowPartialUpgrade = false;
		if (config != null) {
			allowPartialUpgrade = Boolean.parseBoolean(config.getProperty(CONFIG_PARAM_ALLOW_PARTIAL_UPGRADE));
		}

        // Comprobamos si era necesario recuperar la firma totalmente actualizada y si se ha hecho asi
        if (!allowPartialUpgrade && upgradeResult.getState() == State.PARTIAL) {
        	LOGGER.log(Level.SEVERE, logF.f("La aplicacion requiere una actualizacion completa y se obtuvo una actualizacion parcial")); //$NON-NLS-1$
        	Responser.sendError(response, FIReError.UPGRADING_SIGNATURE);
        	return;
        }

        final String docManagerName = connConfig != null ? connConfig.getDocumentManager() : null;

		FIReDocumentManager docManager;
		try {
			docManager = FIReDocumentManagerFactory.newDocumentManager(appId, null, docManagerName);
		}
        catch (final IllegalAccessException | IllegalArgumentException e) {
        	LOGGER.log(Level.WARNING, logF.f("El gestor de documentos no existe o no se tiene permiso para acceder a el: " + docManagerName), e); //$NON-NLS-1$
        	// En el mensaje de error se indica que no existe para no revelar si simplemente no se tiene permiso
        	Responser.sendError(response, FIReError.PARAMETER_DOCUMENT_MANAGER_INVALID);
        	return;
        }
        catch (final Exception e) {
        	LOGGER.log(Level.SEVERE, logF.f("No se ha podido cargar el gestor de documentos con el nombre: " + docManagerName), e); //$NON-NLS-1$
        	Responser.sendError(response, FIReError.INTERNAL_ERROR);
        	return;
        }

        // Si debemos seguir esperando, actualizamos la situacion en el gestor de documentos
        // y devolvemos la informacion del nuevo tiempo de gracia
        if (upgradeResult.getState() == State.PENDING) {
        	LOGGER.log(Level.INFO, logF.f("Se solicita la espera de un nuevo periodo de gracia para la operacion asincrona " + asyncId)); //$NON-NLS-1$
        	try {
        		updateAsynOperation(upgradeResult, appId, docManager);
        	}
        	catch (final Exception e) {
        		LOGGER.log(Level.WARNING,
        				logF.f("No se pudo actualizar la informacion de la operacion asincrona " + asyncId), e); //$NON-NLS-1$
			}
            final TransactionResult result = new TransactionResult(TransactionResult.RESULT_TYPE_SIGN, trAux);
            result.setGracePeriod(upgradeResult.getGracePeriodInfo());
            Responser.sendResult(response, result);
            return;
        }

        // Almacenamos la firma con el gestor de documentos
        byte[] partialResult;
        try {
        	partialResult = storeWithDocumentManager(upgradeResult, asyncId, appId, docManager, logF);
        }
        catch (final Exception e) {
        	LOGGER.log(Level.SEVERE, logF.f("No se pudo almacenar la firma con el gestor de documentos indicado"), e); //$NON-NLS-1$
        	Responser.sendError(response, FIReError.INTERNAL_ERROR);
        	return;
		}

        // Si hemos recibido la firma, la almacenamos temporalmente para recuperarla en
        // una futura llamada
        if (upgradeResult.getResult() != null) {
        	try {
        		TempDocumentsManager.storeDocument(asyncId, partialResult, true, trAux);
        	}
        	catch (final Exception e) {
        		LOGGER.log(Level.SEVERE, logF.f("No se pudo almacenar la firma actualizada en el almacen temporal"), e); //$NON-NLS-1$
            	Responser.sendError(response, FIReError.INTERNAL_ERROR);
            	return;
			}
        }

        // Devolvemos la informacion de la firma
        final TransactionResult result = new TransactionResult(TransactionResult.RESULT_TYPE_SIGN, trAux);
        result.setUpgradeFormat(upgradeResult.getFormat());
        result.setState(upgradeResult.getState() == State.PARTIAL ? TransactionResult.STATE_PARTIAL : TransactionResult.STATE_OK);

        LOGGER.info(logF.f("Se devuelve el estado de la actualizacion asincrona")); //$NON-NLS-1$

        // Enviamos la firma electronica como resultado
        Responser.sendResult(response, result);
	}

	/**
	 * Actualiza la informaci&oacute;n de la operaci&oacute;n as&iacute;crona.
	 * @param upgradeResult Resultado obtenido al tratar de recuperar el resultado de la operaci&opacute;n.
	 * @param appId Identificador de la aplicaci&oacute;n que solicit&oacute; la firma.
	 * @param docManager Gestor de documentos.
	 * @throws IOException Cuando ocurre un error al solicitar la firma asincrona o cuando el gestor no
	 * soporta esta operaci&oacute;n.
	 */
	private static void updateAsynOperation(final UpgradeResult upgradeResult,
			final String appId, final FIReDocumentManager docManager) throws IOException {

        if (docManager instanceof FireAsyncDocumentManager) {
        	try {
        		((FireAsyncDocumentManager) docManager).registryAsyncOperation(
        				upgradeResult.getGracePeriodInfo().getResponseId(),
        				upgradeResult.getGracePeriodInfo().getResolutionDate(),
        				appId, null, null, null, null, null, null);
        	}
        	catch (final Exception e) {
        		throw new IOException("Error al actualizar la operacion de la firma asincrona", e); //$NON-NLS-1$
        	}
        }
        else {
        	throw new IOException("El gestor de documentos indicado no soporta la recuperacion asincrona de firmas"); //$NON-NLS-1$
        }
	}

	/**
	 * Env&iacute;a el resultado de la operaci&oacute;n al gestor de documentos en cuesti&oacute;n.
	 * @param upgradeResult Resultado de la operaci&oacute;n de actualizaci&oacute;n.
	 * @param asyncId Identificador de la operaci&oacute;n as&iacute;ncrona.
	 * @param appId Identificador de la aplicaci&oacute;n que solicit&oacute; la operaci&oacute;n.
	 * @param docManager Gestor de documentos a utilizar.
	 * @param logF Formateador de logs.
	 * @return Valor devuelto por el gestor de documentos utilizado. En el caso de ser el por defecto,
	 * se devuelve la firma actualizada.
	 * @throws IOException Cuando ocurre un error al procesar la firma con el gestor de documentos
	 * seleccionado, si el gestor no existe o si no se tiene permiso de acceso a &eacute;l.
	 */
	private static byte[] storeWithDocumentManager(final UpgradeResult upgradeResult,
			final String asyncId, final String appId, final FIReDocumentManager docManager,
			final LogTransactionFormatter logF) throws IOException {

        byte[] result;
        if (docManager instanceof FireAsyncDocumentManager) {
        	try {
        		result = ((FireAsyncDocumentManager) docManager).storeAsyncDocument(asyncId, appId,
        				upgradeResult.getResult(), upgradeResult.getFormat());
        	}
        	catch (final Exception e) {
        		throw new IOException("Error en el guardado de la firma del documento " + asyncId, e); //$NON-NLS-1$
        	}
        }
        else {
        	LOGGER.warning(logF.f("Se solicito recuperar una firma asincrona indicando un proveedor que no " //$NON-NLS-1$
        			+ "soporta esta operacion. Se devolvera la firma a la operacion")); //$NON-NLS-1$
        	result = upgradeResult.getResult();
        }

        return result;
	}
}
