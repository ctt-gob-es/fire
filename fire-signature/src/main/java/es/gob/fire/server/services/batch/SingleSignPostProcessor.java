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
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import es.gob.afirma.core.AOException;
import es.gob.afirma.core.signers.CounterSignTarget;
import es.gob.afirma.core.signers.ExtraParamsProcessor;
import es.gob.afirma.core.signers.ExtraParamsProcessor.IncompatiblePolicyException;
import es.gob.afirma.core.signers.TriphaseData;
import es.gob.afirma.core.signers.TriphaseData.TriSign;
import es.gob.afirma.triphase.signer.processors.TriPhasePreProcessor;
import es.gob.fire.server.services.FIReTriHelper;
import es.gob.fire.server.services.internal.TempDocumentsManager;

final class SingleSignPostProcessor {

	private static final Logger LOGGER = Logger.getLogger(SingleSignPostProcessor.class.getName());

	private SingleSignPostProcessor() {
		// No instanciable
	}

	/** Realiza el proceso de postfirma, incluyendo la subida o guardado de datos.
	 * @param sSign Firma sobre la que hay que hacer el postproceso.
	 * @param certChain Cadena de certificados del firmante.
	 * @param tdata Datos trif&aacute;sicos relativos <b>&uacute;nicamente</b> a esta firma.
	 *           Debe serializarse como un XML con esta forma (ejemplo):
	 *           <pre>
	 *            &lt;xml&gt;
	 *             &lt;firmas&gt;
	 *              &lt;firma Id="53820fb4-336a-47ee-b7ba-f32f58e5cfd6"&gt;
	 *               &lt;param n="PRE"&gt;MYICXDAYBgk[...]GvykA=&lt;/param&gt;
	 *               &lt;param n="PK1"&gt;dC2dIILB9HV[...]xT1bY=&lt;/param&gt;
	 *               &lt;param n="NEED_PRE"&gt;true&lt;/param&gt;
	 *              &lt;/firma&gt;
	 *             &lt;/firmas&gt;
	 *            &lt;/xml&gt;
	 *           </pre>
	 * @param algorithm Algoritmo de firma.
	 * @param batchId Identificador del lote de firma.
	 * @throws AOException Si hay problemas en la propia firma electr&oacute;nica.
	 * @throws IOException Si hay problemas en la obtenci&oacute;n, tratamiento o gradado de datos.
	 * @throws NoSuchAlgorithmException Si no se soporta alg&uacute;n algoritmo necesario. */
	static void doPostProcess(final SingleSign sSign,
			                  final X509Certificate[] certChain,
			                  final TriphaseData tdata,
			                  final SignatureAlgorithm algorithm,
			                  final String batchId) throws IOException,
			                                                                            AOException,
			                                                                            NoSuchAlgorithmException {
		if (certChain == null || certChain.length < 1) {
			throw new IllegalArgumentException(
				"La cadena de certificados del firmante no puede ser nula ni vacia" //$NON-NLS-1$
			);
		}

		final TriphaseData td = cleanTriphaseData(tdata, sSign.getId());

		try {
			FIReTriHelper.checkSignaturesIntegrity(td, certChain[0], null);
		}
		catch (final SecurityException e) {
			throw new AOException("Error de integridad al validar las firmas PKCS#1 recibidas", e); //$NON-NLS-1$
		}
		catch (final Exception e) {
			throw new AOException("Error en la verificacion de los PKCS#1 de las firmas recibidas", e); //$NON-NLS-1$
		}

		// Instanciamos el preprocesador adecuado
		final TriPhasePreProcessor prep = SingleSignConstants.getTriPhasePreProcessor(sSign);

		Properties extraParams;
		try {
			extraParams = ExtraParamsProcessor.expandProperties(sSign.getExtraParams(), null, sSign.getFormat().name());
		}
		catch (final IncompatiblePolicyException e) {
			LOGGER.log(
					Level.WARNING, "Se han indicado una politica de firma y un formato incompatibles: " + e); //$NON-NLS-1$
			extraParams = sSign.getExtraParams();
		}

		final byte[] docBytes = sSign.getData();

		final byte[] signedDoc;
		switch(sSign.getSubOperation()) {
			case SIGN:
				signedDoc = prep.preProcessPostSign(
					docBytes,
					algorithm.toString(),
					certChain,
					extraParams,
					td
				);
				break;
			case COSIGN:
				signedDoc = prep.preProcessPostCoSign(
					docBytes,
					algorithm.toString(),
					certChain,
					extraParams,
					td
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
				signedDoc = prep.preProcessPostCounterSign(
					docBytes,
					algorithm.toString(),
					certChain,
					extraParams,
					td,
					target
				);
				break;
			default:
				throw new UnsupportedOperationException(
					"Operacion no soportada: " + sSign.getSubOperation() //$NON-NLS-1$
				);
		}

		// Guardamos el resultado en almacenamiento temporal
		TempDocumentsManager.storeDocument(sSign.getName(batchId), signedDoc, true, null);  //TODO: Pasar parametros para logs
	}

	/** Elimina los datos de sesi&oacute;n que no est&eacute;n relacionados con la firma actual.
	 * @param td Datos de sesi&oacute;n a limpiar.
	 * @param signId Identificador de la firma actual.
	 * @return Datos de sesi&oacute;n que contienen &uacute;nicamente informaci&oacute;n relacionada
	 *         con la firma actual. */
	private static TriphaseData cleanTriphaseData(final TriphaseData td, final String signId) {
		if (td == null) {
			throw new IllegalArgumentException("Los datos trifasicos no pueden ser nulos"); //$NON-NLS-1$
		}
		final List<TriSign> tmpTs = td.getTriSigns(signId);
		if (tmpTs == null) {
			throw new IllegalArgumentException(
				"Los datos trifasicos proporcionados no contienen una firma con ID=" + signId //$NON-NLS-1$
			);
		}

		return new TriphaseData(tmpTs);
	}

}
