/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.clavefirma.test.services;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import es.gob.afirma.core.misc.AOUtil;
import es.gob.afirma.core.misc.Base64;
import es.gob.afirma.core.signers.AOPkcs1Signer;
import es.gob.afirma.core.signers.AOSignConstants;
import es.gob.afirma.core.signers.AOSimpleSigner;
import es.gob.afirma.core.signers.TriphaseData;
import es.gob.afirma.core.signers.TriphaseData.TriSign;

/**
 * Servlet implementation class TestSignService
 */
public class TestSignService extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = Logger.getLogger(TestSignService.class.getName());

	/** Clave para la propiedad de datos trif&aacute;sicos. */
	private static final String KEY_TRIPHASEDATA = "triphasedata"; //$NON-NLS-1$

	/** Clave para la propiedad de identificador del titular. */
	private static final String KEY_SUBJECTID = "subjectid"; //$NON-NLS-1$

	/** Clave para la propiedad de algoritmo de firma. */
	private static final String KEY_ALGORITHM = "algorithm"; //$NON-NLS-1$

	/** Clave para la propiedad de certificado de firma. */
	private static final String KEY_CERTIFICATE = "certificate"; //$NON-NLS-1$

	/** Clave para la propiedad de identificador de la transaccion. */
	private static final String KEY_TRANSACTIONID = "transactionid"; //$NON-NLS-1$

    /** Nombre de la propiedad para almac&eacute;n de prefirmas en la
     * sesi&oacute;n trif&aacute;sica. */
    private static final String PROPERTY_NAME_PRESIGN = "PRE"; //$NON-NLS-1$

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) {

		final String transactionId = request.getParameter(KEY_TRANSACTIONID);
		if (transactionId == null || transactionId.isEmpty()) {
			LOGGER.warning("No se ha proporcionado id de transaccion"); //$NON-NLS-1$
			Responser.sendError(response, "No se ha proporcionado ID de transaccion"); //$NON-NLS-1$
			return;
		}

		File transactionFile;
		try {
			transactionFile = TestHelper.getCanonicalFile(TestHelper.getDataFolder(), transactionId);
		} catch (final Exception e) {
			LOGGER.severe("No se pudo componer la ruta del fichero '" + transactionId + "': " + e); //$NON-NLS-1$ //$NON-NLS-2$
			Responser.sendError(response, "La transaccion " + transactionId + " no existe o no es valida"); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}

		if (!transactionFile.isFile() || !transactionFile.canRead()) {
			Responser.sendError(response, "La transaccion " + transactionId + " no existe o no es valida"); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}

		final Properties p = new Properties();
		try (final InputStream fis = new FileInputStream(transactionFile)) {
			p.load(fis);
		}
		catch(final IOException e) {
			LOGGER.warning("Error cargando la transaccion"); //$NON-NLS-1$
			Responser.sendError(response, "Error cargando la transaccion: " + e); //$NON-NLS-1$
			return;
		}

		transactionFile.delete();

		if (!Boolean.parseBoolean(p.getProperty("auth"))) { //$NON-NLS-1$
			LOGGER.warning(" La transaccion " + transactionId + " no esta autorizada"); //$NON-NLS-1$ //$NON-NLS-2$
			Responser.sendError(response, "La transaccion " + transactionId + " no esta autorizada"); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}

		final X509Certificate signingCert;
		try {
			signingCert = (X509Certificate) CertificateFactory.getInstance(
				"X.509" //$NON-NLS-1$
			).generateCertificate(
				new ByteArrayInputStream(Base64.decode(p.getProperty(KEY_CERTIFICATE)))
			);
		}
		catch (final Exception e) {
			LOGGER.warning("La transaccion " + transactionId + " no contiene un certificado valido"); //$NON-NLS-1$ //$NON-NLS-2$
			Responser.sendError(response, "La transaccion " + transactionId + " no contiene un certificado valido: " + e); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}

		final String algorithm = p.getProperty(KEY_ALGORITHM);
		if (algorithm == null || algorithm.isEmpty()) {
			LOGGER.warning("La transaccion " + transactionId + " no contiene un algoritmo valido"); //; //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}

		final String subjectid = p.getProperty(KEY_SUBJECTID);
		if (subjectid == null || subjectid.isEmpty()) {
			LOGGER.warning("La transaccion " + transactionId + " no contiene un  identificador de titular valido"); //$NON-NLS-1$ //$NON-NLS-2$
			Responser.sendError(response, "La transaccion " + transactionId + " no contiene un identificador de titular valido"); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}

		final TriphaseData td;
		try {
			final byte[] triphasedataBytes = Base64.decode(p.getProperty(KEY_TRIPHASEDATA));
			if (triphasedataBytes == null || triphasedataBytes.length < 1) {
				LOGGER.warning("La transaccion " + transactionId + " no contiene datos cargados para su firma"); //$NON-NLS-1$ //$NON-NLS-2$
				Responser.sendError(response, "La transaccion " + transactionId + " no contiene datos cargados para su firma"); //$NON-NLS-1$ //$NON-NLS-2$
				return;
			}
			td = TriphaseData.parser(triphasedataBytes);
		}
		catch(final IOException e) {
			LOGGER.log(Level.SEVERE, "Error cargando los datos trifasicos de la transaccion: " + e, e); //$NON-NLS-1$
			Responser.sendError(response, "Error cargando los datos trifasicos de la transaccion: " + e); //$NON-NLS-1$
			return;
		}

		final KeyStore ks;
		final char[] pass;
		final Enumeration<String> aliases;

		try {
			ks = TestHelper.getKeyStore(subjectid, false);
			pass = TestHelper.getSubjectPassword(subjectid).toCharArray();
			aliases = ks.aliases();
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Error accediendo al nuevo almacen de usuario: " + e, e); //$NON-NLS-1$
			Responser.sendError(response, "Error accediendo al nuevoalmacen de usuario '"  + subjectid + "': " + e); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}

		PrivateKeyEntry pke = null;
		while(aliases.hasMoreElements()) {
			final String alias = aliases.nextElement();
			try {
				final X509Certificate c = (X509Certificate) ks.getCertificate(alias);
				if (c.getSerialNumber().equals(signingCert.getSerialNumber())) {
					pke = (PrivateKeyEntry) ks.getEntry(
						alias,
						new KeyStore.PasswordProtection(pass)
					);
				}
			}
			catch(final Exception e) {
				LOGGER.log(Level.SEVERE, "Error accediendo al almacen del usuario '"  + subjectid + "': "  + e, e); //$NON-NLS-1$ //$NON-NLS-2$
				Responser.sendError(response, "Error accediendo al almacen del usuario '"  + subjectid + "': " + e); //$NON-NLS-1$ //$NON-NLS-2$
				return;
			}
		}

		if (pke == null) {
			LOGGER.severe("El almacen del usuario '"  + subjectid + "' no contiene el certificado indicado: " + AOUtil.getCN(signingCert)); //$NON-NLS-1$ //$NON-NLS-2$
			Responser.sendError(response, "El almacen del usuario '"  + subjectid + "' no contiene el certificado indicado: " + AOUtil.getCN(signingCert)); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}

		final String digestAlgorithm = AOSignConstants.getDigestAlgorithmName(algorithm);
		final String keyType = pke.getCertificate().getPublicKey().getAlgorithm();
		final String signatureAlgorithm = AOSignConstants.composeSignatureAlgorithmName(digestAlgorithm, keyType);


		final Map<String, byte[]> ret = new ConcurrentHashMap<>(td.getSignsCount());
		final AOSimpleSigner signer = new AOPkcs1Signer();
		for (final TriSign ts : td.getTriSigns()) {
			try {
				ret.put(
					ts.getId(),
					signer.sign(
						Base64.decode(ts.getProperty(PROPERTY_NAME_PRESIGN)),
						signatureAlgorithm,
						pke.getPrivateKey(),
						pke.getCertificateChain(),
						null // extraParams, no usado en PKCS#1
					)
				);
			}
			catch (final Exception e) {
				LOGGER.log(Level.SEVERE, "Error realizando la firma PKCS#1 de  '" + ts.getId() + "': " + e, e); //$NON-NLS-1$ //$NON-NLS-2$
				Responser.sendError(response, "Error realizando la firma PKCS#1 de '" + ts.getId() + "': " + e); //$NON-NLS-1$ //$NON-NLS-2$
				return;
			}
		}

		try (PrintWriter writer = response.getWriter()) {
			writer.print(buildJsonResult(ret).toString());
		} catch (final IOException e) {
			LOGGER.log(Level.SEVERE, "No se pudo enviar la respuesta al servidor: " + e); //$NON-NLS-1$
		}
	}

	private static StringBuilder buildJsonResult(final Map<String, byte[]> result) {

		final StringBuilder mapResult = new StringBuilder("["); //$NON-NLS-1$

		final Iterator<String> it = result.keySet().iterator();
		while (it.hasNext()) {
			final String id = it.next();
			mapResult.append("{\"id\":\"").append(id). //$NON-NLS-1$
			append("\", \"pk1\":\"").append(Base64.encode(result.get(id))).append("\"}"); //$NON-NLS-1$ //$NON-NLS-2$
			if (it.hasNext()) {
				mapResult.append(","); //$NON-NLS-1$
			}
		}
		mapResult.append("]"); //$NON-NLS-1$

		return mapResult;
	}
}
