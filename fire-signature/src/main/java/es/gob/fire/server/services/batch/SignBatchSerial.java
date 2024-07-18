/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.services.batch;

import java.io.IOException;
import java.security.Key;
import java.security.cert.X509Certificate;
import java.util.logging.Level;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;

import es.gob.afirma.core.misc.LoggerUtil;
import es.gob.afirma.core.signers.TriphaseData;
import es.gob.fire.server.services.batch.ProcessResult.Result;
import es.gob.fire.server.services.batch.SingleSignConstants.AsyncCipherAlgorithm;
import es.gob.fire.server.services.internal.TempDocumentsManager;

/**
 * Lote de firmas electr&oacute;nicas que se ejecuta secuencialmente.
 *
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s.
 */
public final class SignBatchSerial extends SignBatch {

	/**
	 * Crea un lote de firmas que se ejecuta secuencialmente.
	 *
	 * @param json JSON de definici&oacute;n de lote.
	 * @throws IOException       Si hay problemas en la creaci&oacute;n del lote.
	 * @throws SecurityException Si se sobrepasa alguna de las limitaciones
	 *                           establecidas para el lote (n&ueacute;mero de
	 *                           documentos, tama&ntilde;o de las referencias,
	 *                           tama&ntilde;o de documento, etc.)
	 */
	public SignBatchSerial(final byte[] json) throws IOException, SecurityException {
		super(json);
	}

	@Override
	public JsonObject doPreBatch(final X509Certificate[] certChain) throws BatchException {

		if (certChain == null || certChain.length < 1) {
			throw new IllegalArgumentException(
				"La cadena de certificados del firmante no puede ser nula ni vacia" //$NON-NLS-1$
			);
		}

		final JsonArrayBuilder errorsArrayBuilder = Json.createArrayBuilder();
		final JsonArrayBuilder trisignsArrayBuilder = Json.createArrayBuilder();

		final Key key = certChain[0].getPublicKey();
		final SignatureAlgorithm signatureAlgoritm = new SignatureAlgorithm(
				this.digestAlgorithm, AsyncCipherAlgorithm.getInstanceFromKey(key));

		boolean ignoreRemaining = false;
		for (int i = 0; i < this.signs.size(); i++) {
			final SingleSign ss = this.signs.get(i);
			if (ignoreRemaining) {
				errorsArrayBuilder.add(buildSignResult(ss.getId(), Result.SKIPPED, null));
				continue;
			}

			try {
				final TriphaseData td = ss.doPreProcess(certChain, signatureAlgoritm);
				trisignsArrayBuilder.add(TriphaseDataParser.triphaseDataToJson(td));
			} catch (final Exception e) {
				errorsArrayBuilder.add(buildSignResult(ss.getId(), Result.ERROR_PRE, e));
				if (this.stopOnError) {
					ignoreRemaining = true;
					LOGGER.log(Level.WARNING,
							String.format(
									"Error en una de las firmas del lote (%1s), se ignoraran el resto de elementos", //$NON-NLS-1$
									LoggerUtil.getTrimStr(ss.getDataRef())),
							e);
				} else {
					LOGGER.log(Level.WARNING,
							String.format(
									"Error en una de las firmas del lote (%1s), se continua con el siguiente elemento", //$NON-NLS-1$
									LoggerUtil.getTrimStr(ss.getDataRef())),
							e);
				}
			}
		}

		return buildPreBatch(this.format.toString(), trisignsArrayBuilder.build(), errorsArrayBuilder.build());
	}

	@Override
	public String doPostBatch(final X509Certificate[] certChain, final TriphaseData td) {

		if (td == null) {
			throw new IllegalArgumentException("Los datos de sesion trifasica no pueden ser nulos"); //$NON-NLS-1$
		}

		final Key key = certChain[0].getPublicKey();
		final SignatureAlgorithm signatureAlgorithm = new SignatureAlgorithm(
				this.digestAlgorithm, AsyncCipherAlgorithm.getInstanceFromKey(key));



		boolean ignoreRemaining = false;
		boolean error = false;

		for (final SingleSign ss : this.signs) {

			// Si se ha detectado un error y no deben procesarse el resto de firmas, se
			// marcan como tal
			if (ignoreRemaining) {
				ss.setProcessResult(ProcessResult.PROCESS_RESULT_SKIPPED);
				continue;
			}

			// Si no se encuentran firmas con ese identificador, es que fallaron en la
			// prefirma
			if (td.getTriSigns(ss.getId()) == null) {
				error = true;
				if (this.stopOnError) {
					LOGGER.warning(String.format(
							"Se detecto un error previo en la firma %1s, se ignoraran el resto de elementos", //$NON-NLS-1$
							LoggerUtil.getTrimStr(ss.getDataRef())));
					ignoreRemaining = true;
				} else {
					LOGGER.warning(String.format(
							"Se detecto un error previo en la firma %1s, se continua con el resto de elementos", //$NON-NLS-1$
							LoggerUtil.getTrimStr(ss.getDataRef())));
				}
				String errorMessage = ss.getProcessResult().getDescription();
				if (errorMessage == null) {
					errorMessage = "Error en la prefirma";
				}
				ss.setProcessResult(new ProcessResult(Result.ERROR_PRE, errorMessage));
				continue;
			}

			// Postfirmamos la firma
			try {
				ss.doPostProcess(certChain, td, signatureAlgorithm, getId());
			} catch (final Exception e) {

				error = true;

				ss.setProcessResult(new ProcessResult(Result.ERROR_POST, e.toString()));

				if (this.stopOnError) {
					LOGGER.log(Level.SEVERE, String.format(
								"Error al postfirmar una de las firmas del lote (%1s), se parara el proceso", //$NON-NLS-1$
								LoggerUtil.getTrimStr(ss.getDataRef())),
							e);
					ignoreRemaining = true;
				}
				LOGGER.log(Level.SEVERE, String.format(
						"Error al postfirmar una de las firmas del lote (%1s), se continua con el siguiente elemento", //$NON-NLS-1$
						LoggerUtil.getTrimStr(ss.getDataRef())),
					e);
				continue;
			}

			ss.setProcessResult(ProcessResult.PROCESS_RESULT_OK_UNSAVED);
		}

		// En este punto las firmas estan en almacenamiento temporal

		// Si hubo errores y se indico parar en error no hacemos los guardados de datos,
		// borramos los temporales
		// y enviamos el log
		if (error && this.stopOnError) {
			deleteAllTemps();
			return getResultLog();
		}

		// En otro caso procedemos a la subida de datos
		error = false;
		for (final SingleSign ss : this.signs) {
			if (ss.getProcessResult() != null && ss.getProcessResult().getResult() != Result.DONE_BUT_NOT_SAVED_YET) {
				continue;
			}

			try {
				ss.save(TempDocumentsManager.retrieveDocument(ss.getName(getId())));
				ss.setProcessResult(ProcessResult.PROCESS_RESULT_DONE_SAVED);
			} catch (final IOException e) {
				LOGGER.warning(String.format("Error en el guardado de la firma %1s: ", LoggerUtil.getTrimStr(ss.getDataRef())) + e); //$NON-NLS-1$
				error = true;
				ss.setProcessResult(new ProcessResult(ProcessResult.Result.DONE_BUT_ERROR_SAVING, e.toString()));
				if (this.stopOnError) {
					break;
				}
			}
		}

		deleteAllTemps();

		return getResultLog();

	}

	protected void deleteAllTemps() {
		for (final SingleSign ss : this.signs) {
			try {
				TempDocumentsManager.deleteDocument(ss.getName(getId()));
			} catch (final IOException e) {
				LOGGER.warning("No se pudo eliminar el fichero: " + ss.getName(getId())); //$NON-NLS-1$
			}
		}
	}
}
