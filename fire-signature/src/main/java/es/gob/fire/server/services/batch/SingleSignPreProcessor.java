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
import java.security.cert.X509Certificate;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import es.gob.afirma.core.AOException;
import es.gob.afirma.core.misc.LoggerUtil;
import es.gob.afirma.core.signers.CounterSignTarget;
import es.gob.afirma.core.signers.ExtraParamsProcessor;
import es.gob.afirma.core.signers.ExtraParamsProcessor.IncompatiblePolicyException;
import es.gob.afirma.core.signers.TriphaseData;
import es.gob.afirma.triphase.signer.processors.TriPhasePreProcessor;
import es.gob.fire.server.services.FIReTriHelper;

final class SingleSignPreProcessor {

	private static final String EXTRA_PARAM_CHECK_SIGNATURES = "checkSignatures"; //$NON-NLS-1$

	private static final Logger LOGGER = Logger.getLogger(SingleSignPreProcessor.class.getName());

	private SingleSignPreProcessor() {
		// No instanciable
	}

	/** Realiza el proceso de prefirma, incluyendo la descarga u obtenci&oacute;n de datos.
	 * @param sSign Firma sobre la que hay que hacer el preproceso.
	 * @param certChain Cadena de certificados del firmante.
	 * @param algorithm Algoritmo de firma.
	 * @param docManager Gestor de documentos con el que procesar el lote.
	 * @param docCacheManager Gestor para el guardado de datos en cach&eacute;.
	 * @return Nodo <code>firma</code> del JSON de datos trif&aacute;sicos (sin ninguna etiqueta
	 *         antes ni despu&eacute;s).
	 * @throws AOException Si hay problemas en la propia firma electr&oacute;nica.
	 * @throws IOException Si hay problemas en la obtenci&oacute;n, tratamiento o gradado de datos. */
	static TriphaseData doPreProcess(final SingleSign sSign,
			                   final X509Certificate[] certChain,
			                   final SignatureAlgorithm algorithm) throws IOException,
			                                                                             AOException {

		if (certChain == null || certChain.length < 1) {
			throw new IllegalArgumentException(
				"La cadena de certificados del firmante no puede ser nula ni vacia" //$NON-NLS-1$
			);
		}

		// Instanciamos el preprocesador adecuado
		final TriPhasePreProcessor prep = SingleSignConstants.getTriPhasePreProcessor(sSign);
		byte[] docBytes;
		try {
			docBytes = sSign.getData();
		}
		catch (final IOException e) {
			LOGGER.log(Level.WARNING,
					"No se ha podido recuperar el documento con la referencia " + LoggerUtil.getTrimStr(sSign.getDataRef()), e); //$NON-NLS-1$
			throw new IOException("No se ha podido recuperar el documento a firmar", e); //$NON-NLS-1$
		}
		catch (final SecurityException e) {
			LOGGER.log(Level.WARNING,
					"El documento excedio el limite de tamano establecido: " + sSign.getDataRef().length(), e); //$NON-NLS-1$
			throw new IOException("Se excedio el limite establecido de tamano de documento", e); //$NON-NLS-1$
		}

		Properties extraParams;
		try {
			extraParams = ExtraParamsProcessor.expandProperties(sSign.getExtraParams(), null, sSign.getFormat().name());
		}
		catch (final IncompatiblePolicyException e) {
			LOGGER.log(
					Level.WARNING, "Se han indicado una politica de firma y un formato incompatibles: " + e); //$NON-NLS-1$
			extraParams = sSign.getExtraParams();
		}

		// Eliminamos configuraciones que no deseemos que se utilicen externamente
		extraParams.remove("profile"); //$NON-NLS-1$

		// Comprobamos si se ha pedido validar las firmas antes de agregarles una nueva
        final boolean checkSignatures = Boolean.parseBoolean(extraParams.getProperty(EXTRA_PARAM_CHECK_SIGNATURES));

        TriphaseData td;

		switch(sSign.getSubOperation()) {
			case SIGN:
				td = prep.preProcessPreSign(
						docBytes,
						algorithm.toString(),
						certChain,
						extraParams,
						checkSignatures
					);
				break;
			case COSIGN:
				td = prep.preProcessPreCoSign(
						docBytes,
						algorithm.toString(),
						certChain,
						extraParams,
						checkSignatures
					);
				break;
			case COUNTERSIGN:
				final CounterSignTarget target = CounterSignTarget.getTarget(
					extraParams.getProperty("target", CounterSignTarget.LEAFS.name()) //$NON-NLS-1$
				);
				if (!target.equals(CounterSignTarget.LEAFS) && !target.equals(CounterSignTarget.TREE)) {
					throw new IllegalArgumentException(
						"Objetivo de contrafirma no soportado en proceso por lotes: " + target //$NON-NLS-1$
					);
				}
				td = prep.preProcessPreCounterSign(
						docBytes,
						algorithm.toString(),
						certChain,
						extraParams,
						target,
						checkSignatures
					);
				break;
			default:
				throw new UnsupportedOperationException(
					"Operacion no soportada: " + sSign.getSubOperation() //$NON-NLS-1$
				);
		}

		// Agregamos los codigos de verificacion para posteriormente poder comprobar
		// que el PKCS#1 recibido se genero con el certificado de firma
		try {
			FIReTriHelper.addVerificationCodes(td, certChain[0]);
		} catch (final Exception e) {
			throw new AOException("No se pudo agregar el codigo de verificacion de firmas", e); //$NON-NLS-1$
		}

		return td;
	}

}
