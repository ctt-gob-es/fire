/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.services.triphase;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import es.gob.afirma.core.AOException;
import es.gob.afirma.core.misc.AOUtil;
import es.gob.afirma.core.misc.Base64;
import es.gob.afirma.core.signers.AOSignConstants;
import es.gob.afirma.core.signers.CounterSignTarget;
import es.gob.afirma.core.signers.ExtraParamsProcessor;
import es.gob.afirma.core.signers.TriphaseData;
import es.gob.afirma.triphase.signer.processors.AutoTriPhasePreProcessor;
import es.gob.afirma.triphase.signer.processors.CAdESASiCSTriPhasePreProcessor;
import es.gob.afirma.triphase.signer.processors.CAdESTriPhasePreProcessor;
import es.gob.afirma.triphase.signer.processors.FacturaETriPhasePreProcessor;
import es.gob.afirma.triphase.signer.processors.PAdESTriPhasePreProcessor;
import es.gob.afirma.triphase.signer.processors.TriPhasePreProcessor;
import es.gob.afirma.triphase.signer.processors.XAdESASiCSTriPhasePreProcessor;
import es.gob.afirma.triphase.signer.processors.XAdESTriPhasePreProcessor;
import es.gob.fire.server.services.FIReTriHelper;
import es.gob.fire.server.services.SignOperation;
import es.gob.fire.server.services.internal.Pkcs1TriPhasePreProcessor;
import es.gob.fire.server.services.triphase.document.DocumentManager;
import es.gob.fire.server.services.triphase.document.FIReLocalDocumentManager;
import es.gob.fire.signature.ConfigManager;

/** Servicio de firma electr&oacute;nica en 3 fases. */
public final class ClienteAfirmaSignatureService extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static Logger LOGGER = Logger.getLogger(ClienteAfirmaSignatureService.class.getName());

	private static DocumentManager DOC_MANAGER;

	private static final String PARAM_NAME_OPERATION = "op"; //$NON-NLS-1$

	private static final String PARAM_VALUE_OPERATION_PRESIGN = "pre"; //$NON-NLS-1$
	private static final String PARAM_VALUE_OPERATION_POSTSIGN = "post"; //$NON-NLS-1$

	private static final String PARAM_NAME_SUB_OPERATION = "cop"; //$NON-NLS-1$

	// Parametros que necesitamos para la prefirma
	private static final String PARAM_NAME_DOCID = "doc"; //$NON-NLS-1$
	private static final String PARAM_NAME_ALGORITHM = "algo"; //$NON-NLS-1$
	private static final String PARAM_NAME_FORMAT = "format"; //$NON-NLS-1$
	private static final String PARAM_NAME_EXTRA_PARAM = "params"; //$NON-NLS-1$
	private static final String PARAM_NAME_SESSION_DATA = "session"; //$NON-NLS-1$
	private static final String PARAM_NAME_CERT = "cert"; //$NON-NLS-1$

	/** Separador que debe usarse para incluir varios certificados dentro del mismo par&aacute;metro. */
	private static final String PARAM_NAME_CERT_SEPARATOR = ","; //$NON-NLS-1$

	/** Nombre del par&aacute;metro que identifica los nodos que deben contrafirmarse. */
	private static final String PARAM_NAME_TARGET_TYPE = "target"; //$NON-NLS-1$

	/** Indicador de finalizaci&oacute;n correcta de proceso. */
	private static final String SUCCESS = "OK NEWID="; //$NON-NLS-1$

	private static final String EXTRA_PARAM_HEADLESS = "headless"; //$NON-NLS-1$

	private static final String EXTRA_PARAM_VALIDATE_PKCS1 = "validatePkcs1"; //$NON-NLS-1$

	/** Or&iacute;genes permitidos por defecto desde los que se pueden realizar peticiones al servicio. */
	private static final String ALL_ORIGINS_ALLOWED = "*"; //$NON-NLS-1$

	/** Juego de caracteres usado internamente para la codificaci&oacute;n de textos. */
	private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

	static {
		DOC_MANAGER = new FIReLocalDocumentManager();
	}

	@Override
	protected void service(final HttpServletRequest request, final HttpServletResponse response) {

		LOGGER.fine("== INICIO FIRMA TRIFASICA =="); //$NON-NLS-1$

		final Map<String, String> parameters = new HashMap<>();
		final String[] params;
		try {
			params = new String(AOUtil.getDataFromInputStream(request.getInputStream()), DEFAULT_CHARSET).split("&"); //$NON-NLS-1$
		}
		catch (final Throwable e) {
			LOGGER.severe("No se pudieron leer los parametros de la peticion: " + e); //$NON-NLS-1$
			try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			} catch (final IOException e1) {
				LOGGER.log(Level.SEVERE, "No se pudo enviar un error al cliente", e); //$NON-NLS-1$
			}
			return;
		}

		for (final String param : params) {
			if (param.indexOf('=') != -1) {
				try {
					parameters.put(param.substring(0, param.indexOf('=')), URLDecoder.decode(param.substring(param.indexOf('=') + 1), DEFAULT_CHARSET.name()));
				}
				catch (final Exception e) {
					LOGGER.warning("Error al decodificar un parametro de la peticion: " + e); //$NON-NLS-1$
				}
			}
		}

		response.setHeader("Access-Control-Allow-Origin", ALL_ORIGINS_ALLOWED); //$NON-NLS-1$
		response.setContentType("text/plain"); //$NON-NLS-1$
		response.setCharacterEncoding("utf-8"); //$NON-NLS-1$

		// Obtenemos el codigo de operacion
		final String operation = parameters.get(PARAM_NAME_OPERATION);
		if (operation == null) {
			LOGGER.severe("No se ha indicado la operacion trifasica a realizar"); //$NON-NLS-1$
			sendResponse(response, ErrorManager.getErrorMessage(1));
			return;
		}

		// Obtenemos el codigo de operacion
		final SignOperation subOperation = SignOperation.parse(parameters.get(PARAM_NAME_SUB_OPERATION));
		if (subOperation == null) {
			sendResponse(response, ErrorManager.getErrorMessage(13));
			return;
		}

		// Obtenemos el formato de firma
		final String format = parameters.get(PARAM_NAME_FORMAT);
		LOGGER.fine("Formato de firma seleccionado: " + format); //$NON-NLS-1$
		if (format == null) {
			LOGGER.warning("No se ha indicado formato de firma"); //$NON-NLS-1$
			sendResponse(response, ErrorManager.getErrorMessage(4));
			return;
		}

		// Obtenemos los parametros adicionales para la firma
		Properties extraParams = new Properties();
		try {
			if (parameters.containsKey(PARAM_NAME_EXTRA_PARAM)) {
				extraParams = AOUtil.base642Properties(parameters.get(PARAM_NAME_EXTRA_PARAM));
			}
		}
		catch (final Exception e) {
			LOGGER.severe("El formato de los parametros adicionales suministrado es erroneo: " +  e); //$NON-NLS-1$
			sendResponse(response, ErrorManager.getErrorMessage(6) + ": " + e); //$NON-NLS-1$);
			return;
		}

		// Eliminamos configuraciones que no deseemos que se utilicen extenamente
		extraParams.remove(EXTRA_PARAM_VALIDATE_PKCS1);

		// Introducimos los parametros necesarios para que no se traten
		// de mostrar dialogos en servidor
		extraParams.setProperty(EXTRA_PARAM_HEADLESS, Boolean.TRUE.toString());

		// Expandimos los atributos de los extraParams
		try {
			extraParams = ExtraParamsProcessor.expandProperties(
					extraParams,
					null,
					format
					);
		}
		catch (final Exception e) {
			LOGGER.severe("Se han indicado una politica de firma y un formato incompatibles: "  + e); //$NON-NLS-1$

		}

		// Obtenemos los parametros adicionales para la firma
		byte[] sessionData = null;
		try {
			if (parameters.containsKey(PARAM_NAME_SESSION_DATA)) {
				sessionData = Base64.decode(parameters.get(PARAM_NAME_SESSION_DATA).trim(), true);
			}
		}
		catch (final Exception e) {
			LOGGER.severe("El formato de los datos de sesion suministrados es erroneo: "  + e); //$NON-NLS-1$
			sendResponse(response, ErrorManager.getErrorMessage(6) + ": " + e); //$NON-NLS-1$
			return;
		}

		if (sessionData != null) {
			LOGGER.fine(String.format("Recibidos los siguientes datos de sesion para '%s':\n%s", //$NON-NLS-1$
					operation, new String(sessionData, DEFAULT_CHARSET)));
		}

		// Obtenemos el certificado
		final String cert = parameters.get(PARAM_NAME_CERT);
		if (cert == null) {
			LOGGER.warning("No se ha indicado certificado de firma"); //$NON-NLS-1$
			sendResponse(response, ErrorManager.getErrorMessage(5));
			return;
		}

		final String[] receivedCerts = cert.split(PARAM_NAME_CERT_SEPARATOR);
		final X509Certificate[] signerCertChain = new X509Certificate[receivedCerts.length];
		for (int i = 0; i<receivedCerts.length; i++) {
			try {
				signerCertChain[i] =
					(X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate( //$NON-NLS-1$
						new ByteArrayInputStream(
							Base64.decode(receivedCerts[i], true)
						)
					)
				;
			}
			catch(final Exception e) {

				LOGGER.log(Level.SEVERE, "Error al decodificar el certificado: " + receivedCerts[i], e);  //$NON-NLS-1$
				sendResponse(response, ErrorManager.getErrorMessage(7));
				return;
			}
		}

		byte[] docBytes = null;
		final String docId = parameters.get(PARAM_NAME_DOCID);
		if (docId != null) {
			try {
				LOGGER.fine("Recuperamos el documento mediante el DocumentManager"); //$NON-NLS-1$
				docBytes = DOC_MANAGER.getDocument(docId, signerCertChain, extraParams);
				LOGGER.fine(
					"Recuperado documento de " + docBytes.length + " octetos" //$NON-NLS-1$ //$NON-NLS-2$
				);
			}
			catch (final Throwable e) {
				LOGGER.warning("Error al recuperar el documento: " + e); //$NON-NLS-1$
				sendResponse(response, ErrorManager.getErrorMessage(14));
				return;
			}
		}

		// Obtenemos el algoritmo de firma
		final String algorithm = parameters.get(PARAM_NAME_ALGORITHM);
		if (algorithm == null) {
			LOGGER.warning("No se ha indicado algoritmo de firma"); //$NON-NLS-1$
			sendResponse(response, ErrorManager.getErrorMessage(3));
			return;
		}

		// Instanciamos el preprocesador adecuado
		final TriPhasePreProcessor prep;
		if (AOSignConstants.SIGN_FORMAT_PADES.equalsIgnoreCase(format) ||
			AOSignConstants.SIGN_FORMAT_PADES_TRI.equalsIgnoreCase(format)) {
					prep = new PAdESTriPhasePreProcessor();
		}
		else if (AOSignConstants.SIGN_FORMAT_CADES.equalsIgnoreCase(format) ||
				 AOSignConstants.SIGN_FORMAT_CADES_TRI.equalsIgnoreCase(format)) {
					prep = new CAdESTriPhasePreProcessor();
		}
		else if (AOSignConstants.SIGN_FORMAT_XADES.equalsIgnoreCase(format) ||
				 AOSignConstants.SIGN_FORMAT_XADES_TRI.equalsIgnoreCase(format)) {
					prep = new XAdESTriPhasePreProcessor(ConfigManager.isAlternativeXmlDSigActive());
		}
		else if (AOSignConstants.SIGN_FORMAT_CADES_ASIC_S.equalsIgnoreCase(format) ||
				 AOSignConstants.SIGN_FORMAT_CADES_ASIC_S_TRI.equalsIgnoreCase(format)) {
					prep = new CAdESASiCSTriPhasePreProcessor();
		}
		else if (AOSignConstants.SIGN_FORMAT_XADES_ASIC_S.equalsIgnoreCase(format) ||
				 AOSignConstants.SIGN_FORMAT_XADES_ASIC_S_TRI.equalsIgnoreCase(format)) {
					prep = new XAdESASiCSTriPhasePreProcessor(ConfigManager.isAlternativeXmlDSigActive());
		}
		else if (AOSignConstants.SIGN_FORMAT_FACTURAE.equalsIgnoreCase(format) ||
				 AOSignConstants.SIGN_FORMAT_FACTURAE_TRI.equalsIgnoreCase(format) ||
				 AOSignConstants.SIGN_FORMAT_FACTURAE_ALT1.equalsIgnoreCase(format)) {
					prep = new FacturaETriPhasePreProcessor(ConfigManager.isAlternativeXmlDSigActive());
		}
		else if (AOSignConstants.SIGN_FORMAT_PKCS1.equalsIgnoreCase(format) ||
				 AOSignConstants.SIGN_FORMAT_PKCS1_TRI.equalsIgnoreCase(format)) {
					prep = new Pkcs1TriPhasePreProcessor();
		}
		else if (AOSignConstants.SIGN_FORMAT_AUTO.equalsIgnoreCase(format)) {
			prep = new AutoTriPhasePreProcessor(ConfigManager.isAlternativeXmlDSigActive());
		}
		else {
			LOGGER.severe("Formato de firma no soportado: " + format); //$NON-NLS-1$
			sendResponse(response, ErrorManager.getErrorMessage(8));
			return;
		}

		if (PARAM_VALUE_OPERATION_PRESIGN.equalsIgnoreCase(operation)) {

			LOGGER.fine(" == PREFIRMA en servidor"); //$NON-NLS-1$

			// En FIRe, nunca se solicitara comprobar las firmas previas en este punto del proceso
	        final boolean checkSignatures = false;

			final TriphaseData preRes;
			try {
				switch (subOperation) {
				case SIGN:
					preRes = prep.preProcessPreSign(
							docBytes,
							algorithm,
							signerCertChain,
							extraParams,
							checkSignatures
						);
					break;
				case COSIGN:
					preRes = prep.preProcessPreCoSign(
							docBytes,
							algorithm,
							signerCertChain,
							extraParams,
							checkSignatures
						);
					break;
				case COUNTERSIGN:
					CounterSignTarget target = CounterSignTarget.LEAFS;
					if (extraParams.containsKey(PARAM_NAME_TARGET_TYPE)) {
						final String targetValue = extraParams.getProperty(PARAM_NAME_TARGET_TYPE).trim();
						if (CounterSignTarget.TREE.toString().equalsIgnoreCase(targetValue)) {
							target = CounterSignTarget.TREE;
						}
					}

					preRes = prep.preProcessPreCounterSign(
						docBytes,
						algorithm,
						signerCertChain,
						extraParams,
						target,
						checkSignatures
					);
					break;
				default:
					throw new AOException("No se reconoce el codigo de sub-operacion: " + subOperation); //$NON-NLS-1$
				}

				LOGGER.fine("Se ha calculado el resultado de la prefirma y se devuelve"); //$NON-NLS-1$
			}
			catch (final Exception e) {
				LOGGER.log(Level.SEVERE, "Error en la prefirma: " + e, e); //$NON-NLS-1$
				sendResponse(response, ErrorManager.getErrorMessage(9) + ": " + e); //$NON-NLS-1$
				return;
			}

			// Si se ha definido una clave HMAC para la comprobacion de integridad de
			// las firmas, agregamos a la respuesta la informacion de integridad que
			// asocie las prefirmas con el certificado de firma
			if (ConfigManager.getHMacKey() != null) {
				try {
					FIReTriHelper.addVerificationCodes(preRes, signerCertChain[0]);
				}
				catch (final Exception e) {
					LOGGER.log(Level.SEVERE, "Error al generar los codigos de verificacion de las firmas: " + e, e); //$NON-NLS-1$
					sendResponse(response, ErrorManager.getErrorMessage(16) + ": " + e); //$NON-NLS-1$
					return;
				}
			}

			sendResponse(response, Base64.encode(preRes.toString().getBytes(StandardCharsets.UTF_8), true));

			LOGGER.fine("== FIN PREFIRMA"); //$NON-NLS-1$
		}
		else if (PARAM_VALUE_OPERATION_POSTSIGN.equalsIgnoreCase(operation)) {

			LOGGER.fine(" == POSTFIRMA en servidor"); //$NON-NLS-1$

			TriphaseData triphaseData;
			try {
				triphaseData = TriphaseData.parser(sessionData);
			}
			catch (final Exception e) {
				LOGGER.log(Level.SEVERE, "El formato de los parametros de operacion requeridos incorrecto", e); //$NON-NLS-1$
				sendResponse(response, ErrorManager.getErrorMessage(19) + ": " + e); //$NON-NLS-1$
				return;
			}

			// Si se ha definido una clave HMAC para la comprobacion de
			// integridad de las firmas, comprobamos que las prefirmas y el
			// certificado de firma no se hayan modificado durante en
			// ningun punto de la operacion y que los PKCS#1 proporcionados
			// esten realizados con ese certificado
			if (ConfigManager.getHMacKey() != null) {
				try {
					FIReTriHelper.checkSignaturesIntegrity(triphaseData, signerCertChain[0]);
				}
				catch (final SecurityException e) {
					LOGGER.log(Level.SEVERE, "Las prefirmas y/o el certificado obtenido no se corresponden con los generados en la prefirma", e); //$NON-NLS-1$
					sendResponse(response, ErrorManager.getErrorMessage(17) + ": " + e); //$NON-NLS-1$
					return;
				}
				catch (final Exception e) {
					LOGGER.log(Level.SEVERE, "Error al comprobar los codigos de verificacion de las firmas", e); //$NON-NLS-1$
					sendResponse(response, ErrorManager.getErrorMessage(17) + ": " + e); //$NON-NLS-1$
					return;
				}
			}

			final byte[] signedDoc;
			try {
				switch (subOperation) {
				case SIGN:
					signedDoc = prep.preProcessPostSign(
							docBytes,
							algorithm,
							signerCertChain,
							extraParams,
							triphaseData
							);
					break;
				case COSIGN:
					signedDoc = prep.preProcessPostCoSign(
							docBytes,
							algorithm,
							signerCertChain,
							extraParams,
							triphaseData
							);
					break;
				case COUNTERSIGN:
					CounterSignTarget target = CounterSignTarget.LEAFS;
					if (extraParams.containsKey(PARAM_NAME_TARGET_TYPE)) {
						final String targetValue = extraParams.getProperty(PARAM_NAME_TARGET_TYPE).trim();
						if (CounterSignTarget.TREE.toString().equalsIgnoreCase(targetValue)) {
							target = CounterSignTarget.TREE;
						}
					}
					signedDoc = prep.preProcessPostCounterSign(
							docBytes,
							algorithm,
							signerCertChain,
							extraParams,
							triphaseData,
							target
							);
					break;
				default:
					throw new AOException("No se reconoce el codigo de sub-operacion: " + subOperation); //$NON-NLS-1$
				}
			}
			catch (final Exception e) {
				LOGGER.log(Level.SEVERE, "Error en la postfirma: " + e, e); //$NON-NLS-1$
				sendResponse(response, ErrorManager.getErrorMessage(12) + ": " + e); //$NON-NLS-1$
				return;
			}

			// Establecemos parametros adicionales que se pueden utilizar para guardar el documento
			if (!extraParams.containsKey(PARAM_NAME_FORMAT)) {
				extraParams.setProperty(PARAM_NAME_FORMAT, format);
			}

			LOGGER.fine(" Se ha calculado el resultado de la postfirma y se devuelve. Numero de bytes: " + signedDoc.length); //$NON-NLS-1$

			// Devolvemos al servidor documental el documento firmado
			LOGGER.fine("Almacenamos la firma mediante el DocumentManager"); //$NON-NLS-1$
			final String newDocId;
			try {
				newDocId = DOC_MANAGER.storeDocument(docId, signerCertChain, signedDoc, extraParams);
			}
			catch(final Throwable e) {
				LOGGER.severe("Error al almacenar el documento: " + e); //$NON-NLS-1$
				sendResponse(response, ErrorManager.getErrorMessage(10) + ": " + e); //$NON-NLS-1$
				return;
			}
			LOGGER.fine("Documento almacenado"); //$NON-NLS-1$

			sendResponse(response, SUCCESS + newDocId);

			LOGGER.fine("== FIN POSTFIRMA ****"); //$NON-NLS-1$
		}
		else {
			sendResponse(response, ErrorManager.getErrorMessage(11));
		}
	}

	/**
	 * Env&iacute;a una cadena como respuesta del servicio.
	 * @param response Respuesta.
	 * @param result Cadena a devolver.
	 */
	private static void sendResponse(final HttpServletResponse response, final String result) {

		PrintWriter out = null;
		try {
			out = response.getWriter();
		}
        catch (final Exception e) {
        	LOGGER.severe("No se pudo contestar a la peticion: " + e); //$NON-NLS-1$
        	try {
				response.sendError(HttpURLConnection.HTTP_INTERNAL_ERROR, "No se pude contestar a la peticion: " + e); //$NON-NLS-1$
			}
        	catch (final IOException e1) {
        		LOGGER.severe("No se pudo enviar un error HTTP 500: " + e1); //$NON-NLS-1$
			}
        	return;
        }

		out.print(result);
		out.flush();
	}
}
