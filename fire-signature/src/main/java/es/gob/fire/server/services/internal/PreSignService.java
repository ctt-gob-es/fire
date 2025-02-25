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

import java.io.ByteArrayInputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import es.gob.afirma.core.misc.Base64;
import es.gob.afirma.core.signers.TriphaseData;
import es.gob.afirma.signers.xml.XmlDSigProviderHelper;
import es.gob.fire.alarms.Alarm;
import es.gob.fire.server.connector.DocInfo;
import es.gob.fire.server.connector.FIReConnector;
import es.gob.fire.server.connector.FIReConnectorFactoryException;
import es.gob.fire.server.connector.FIReConnectorNetworkException;
import es.gob.fire.server.connector.FIReConnectorUnknownUserException;
import es.gob.fire.server.connector.LoadResult;
import es.gob.fire.server.services.FIReError;
import es.gob.fire.server.services.FIReTriHelper;
import es.gob.fire.server.services.LogUtils;
import es.gob.fire.server.services.RequestParameters;
import es.gob.fire.server.services.Responser;
import es.gob.fire.server.services.ServiceUtil;
import es.gob.fire.server.services.SignOperation;
import es.gob.fire.server.services.statistics.TransactionType;
import es.gob.fire.signature.ConfigManager;

/**
 * Servicio de carga y prefirma de datos para su posterior firma a trav&eacute;s
 * de
 * un proveedor de firma en la nube. Este servicio se utiliza internamente, por
 * lo que
 * se devolver&aacute;n errores gen&eacute;ricos cuando ocurr&aacute;n errores
 * que no
 * deber&iacute;an ocurrir nunca.
 *
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s.
 */
public final class PreSignService extends HttpServlet {

    /** Serial Id. */
    private static final long serialVersionUID = 7165850857019380976L;

    private static final Logger LOGGER = Logger.getLogger(PreSignService.class.getName());

    /**
     * Par&aacute;metro de configuraci&oacute;n para obligar a la multifirma de
     * firmas longevas.
     */
    private static final String EXTRA_PARAM_ALLOW_SIGN_LTS_SIGNATURES = "allowSignLTSignature"; //$NON-NLS-1$

    static {
        // Configuramos el proveedor de firma XML
        XmlDSigProviderHelper.configureXmlDSigProvider();
    }

    /** Carga los datos para su posterior firma en servidor.
     * @see HttpServlet#service(HttpServletRequest request, HttpServletResponse response) */
    @Override
	protected void doPost(final HttpServletRequest request,
    		               final HttpServletResponse response) {
		// No se guardaran los resultados en cache
		response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); //$NON-NLS-1$ //$NON-NLS-2$

		RequestParameters params;
		try {
			params = RequestParameters.extractParameters(request);
		}
		catch (final Exception e) {
			LOGGER.log(Level.WARNING, "Error en la lectura de los parametros de entrada", e); //$NON-NLS-1$
			Responser.sendError(response, FIReError.READING_PARAMETERS);
			return;
		}
		
    	final String trId = params.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID);
    	final String userRef = params.getParameter(ServiceParams.HTTP_PARAM_SUBJECT_REF);
    	String certB64 = params.getParameter(ServiceParams.HTTP_PARAM_CERT);
		String redirectErrorUrl = params.getParameter(ServiceParams.HTTP_PARAM_ERROR_URL);

    	// Con la seleccion automatica de certificado, se recibe el certificado y la URL
		// de error en un atributo en lugar de por parametro
    	if (certB64 == null || certB64.isEmpty()) {
    		certB64 = (String) request.getAttribute(ServiceParams.HTTP_ATTR_CERT);
        	redirectErrorUrl = (String) request.getAttribute(ServiceParams.HTTP_ATTR_ERROR_URL);
    	}

    	final TransactionAuxParams trAux = new TransactionAuxParams(null, LogUtils.limitText(trId));
    	final LogTransactionFormatter logF = trAux.getLogFormatter();

		LOGGER.fine(logF.f("Inicio de la llamada al servicio publico de prefirma")); //$NON-NLS-1$

        // Comprobamos que se hayan proporcionado los parametros indispensables
        if (trId == null || trId.isEmpty()) {
        	LOGGER.warning(logF.f("No se ha proporcionado el ID de transaccion")); //$NON-NLS-1$
			Responser.sendError(response, FIReError.FORBIDDEN);
            return;
        }

        if (userRef == null || userRef.isEmpty()) {
            LOGGER.warning(logF.f("No se ha proporcionado la referencia del firmante")); //$NON-NLS-1$
			Responser.sendError(response, FIReError.FORBIDDEN);
            return;
        }

        if (certB64 == null || certB64.isEmpty()) {
        	LOGGER.warning(logF.f("No se ha proporcionado el certificado del firmante")); //$NON-NLS-1$
			Responser.sendError(response, FIReError.FORBIDDEN);
        	return;
        }

		if (redirectErrorUrl == null || redirectErrorUrl.isEmpty()) {
			LOGGER.warning(logF.f("No se ha proporcionado la URL de error")); //$NON-NLS-1$
			Responser.sendError(response, FIReError.FORBIDDEN);
			return;
		}
		try {
        	redirectErrorUrl = URLDecoder.decode(redirectErrorUrl, StandardCharsets.UTF_8.name());
        }
        catch (final Exception e) {
        	LOGGER.warning(logF.f("No se pudo deshacer el URL Encoding de la URL de redireccion: ") + e); //$NON-NLS-1$
		}

		final FireSession session = loadSession(trId, userRef, request, trAux);
		if (session == null) {
       		LOGGER.warning(logF.f("Se redirige a la pagina proporcionada en la llamada")); //$NON-NLS-1$
       		Responser.redirectToExternalUrl(redirectErrorUrl, request, response, trAux);
        	return;
		}

    	// Leemos los valores necesarios de la configuracion
		final String appId         	= session.getString(ServiceParams.SESSION_PARAM_APPLICATION_ID);
		final String userId         = session.getString(ServiceParams.SESSION_PARAM_SUBJECT_ID);
        final String algorithm      = session.getString(ServiceParams.SESSION_PARAM_ALGORITHM);
        Properties extraParams 		= (Properties) session.getObject(ServiceParams.SESSION_PARAM_EXTRA_PARAM);
        final String cryptoOperation   = session.getString(ServiceParams.SESSION_PARAM_CRYPTO_OPERATION);
        final String format         = session.getString(ServiceParams.SESSION_PARAM_FORMAT);
        final String providerName	= session.getString(ServiceParams.SESSION_PARAM_CERT_ORIGIN);
    	final boolean originForced  = Boolean.parseBoolean(session.getString(ServiceParams.SESSION_PARAM_CERT_ORIGIN_FORCED));
        final boolean stopOnError   = Boolean.parseBoolean(session.getString(ServiceParams.SESSION_PARAM_BATCH_STOP_ON_ERROR));
        final TransactionConfig connConfig = (TransactionConfig) session.getObject(ServiceParams.SESSION_PARAM_CONNECTION_CONFIG);
        final TransactionType transactionType = (TransactionType) session.getObject(ServiceParams.SESSION_PARAM_TRANSACTION_TYPE);

        trAux.setAppId(appId);

        // Comprobamos primero cual es la URL de redireccion en casos de error
		if (connConfig == null || !connConfig.isDefinedRedirectErrorUrl()) {
			LOGGER.warning(logF.f("No se encontro en la sesion la URL redireccion de error para la operacion")); //$NON-NLS-1$
			ErrorManager.setErrorToSession(session, FIReError.INTERNAL_ERROR, trAux);
			Responser.redirectToExternalUrl(redirectErrorUrl, request, response, trAux);
			return;
		}

		// Usaremos preferiblemente la URL de error establecida en la sesion
		redirectErrorUrl = connConfig.getRedirectErrorUrl();

    	// Comprobacion del resto de parametros
        if (algorithm == null || algorithm.isEmpty()) {
            LOGGER.warning(logF.f("No se encontro en la sesion el algoritmo de firma")); //$NON-NLS-1$
            ErrorManager.setErrorToSession(session, FIReError.INTERNAL_ERROR, trAux);
            Responser.redirectToExternalUrl(redirectErrorUrl, request, response, trAux);
            return;
        }

        if (cryptoOperation == null || cryptoOperation.isEmpty()) {
            LOGGER.warning(logF.f("No se encontro en la sesion la operacion de firma a realizar")); //$NON-NLS-1$
            ErrorManager.setErrorToSession(session, FIReError.INTERNAL_ERROR, trAux);
            Responser.redirectToExternalUrl(redirectErrorUrl, request, response, trAux);
            return;
        }

        if (format == null || format.isEmpty()) {
            LOGGER.warning(logF.f("No se encontro en la sesion el formato de firma")); //$NON-NLS-1$
            ErrorManager.setErrorToSession(session, FIReError.INTERNAL_ERROR, trAux);
            Responser.redirectToExternalUrl(redirectErrorUrl, request, response, trAux);
            return;
        }

        // Evitamos que se interrumpa la operacion en caso de estar cofirmandose o
        // contrafirmandose una firma longeva
        if (!SignOperation.SIGN.toString().equalsIgnoreCase(cryptoOperation)) {
        	if (extraParams == null) {
        		extraParams = new Properties();
        	}
        	extraParams.setProperty(EXTRA_PARAM_ALLOW_SIGN_LTS_SIGNATURES, Boolean.TRUE.toString());
        }


        // Decodificamos el certificado de firma
        final X509Certificate signerCert;
        try {
            signerCert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate( //$NON-NLS-1$
                new ByteArrayInputStream(Base64.decode(certB64, true))
            );
        }
        catch (final Exception e) {
        	LOGGER.warning(logF.f("No se ha podido decodificar el certificado del firmante: " + e)); //$NON-NLS-1$
        	ErrorManager.setErrorToSession(session, FIReError.SIGNING, trAux);
        	Responser.redirectToExternalUrl(redirectErrorUrl, request, response, trAux);
        	return;
        }

        // Calculamos la prefirma de una forma u otra segun se trate de una operacion de
        // firma individual o la de un lote.
        TriphaseData td;

        // Transaccion de firma
        if (TransactionType.SIGN == transactionType) {
        	final byte[] data;
        	try {
        		data = TempDocumentsManager.retrieveDocument(trId);
        	}
        	catch (final Exception e) {
        		LOGGER.warning(logF.f("No se han podido recuperar los datos de la operacion: " + e)); //$NON-NLS-1$
            	ErrorManager.setErrorToSession(session, FIReError.INVALID_TRANSACTION, trAux);
            	Responser.redirectToExternalUrl(redirectErrorUrl, request, response, trAux);
        		return;
        	}

            // En caso de haberse indicado un nombre de fichero a traves del parametro
            // de configuracion, los pasamos a los extraParams para que se puedan procesar
            // en el conector como parte de la configuracion de la firma
            final DocInfo docInfo = DocInfo.extractDocInfo(connConfig.getProperties());
            DocInfo.addDocInfoToSign(extraParams, docInfo);

        	try {
                td = FIReTriHelper.getPreSign(
                    cryptoOperation,
                    format,
                    algorithm,
                    extraParams,
        			signerCert,
                    data,
                    logF
        		);
            }
        	catch (final UnsupportedOperationException uoe) {
                LOGGER.log(Level.SEVERE, logF.f("Operacion no soportada por el sistema"), uoe); //$NON-NLS-1$
                ErrorManager.setErrorToSession(session, FIReError.SIGNING, true,
                		uoe.getMessage(), trAux);
                Responser.redirectToExternalUrl(redirectErrorUrl, request, response, trAux);
                return;
            }
            catch (final Exception e) {
                LOGGER.log(Level.SEVERE, logF.f("Error en la prefirma de los datos"), e); //$NON-NLS-1$
                ErrorManager.setErrorToSession(session, FIReError.SIGNING, true, e.getMessage(), trAux);
                Responser.redirectToExternalUrl(redirectErrorUrl, request, response, trAux);
                return;
            }
        }
     // Transaccion de lote
        else if (TransactionType.BATCH == transactionType) {

        	final BatchResult batchResult = (BatchResult) session.getObject(ServiceParams.SESSION_PARAM_BATCH_RESULT);
        	if (batchResult == null || batchResult.documentsCount() == 0) {
        		LOGGER.log(Level.WARNING, logF.f("No se han encontrado documentos en el lote")); //$NON-NLS-1$
            	ErrorManager.setErrorToSession(session, FIReError.INTERNAL_ERROR, trAux);
            	Responser.redirectToExternalUrl(redirectErrorUrl, request, response, trAux);
        		return;
			}

        	// Cargamos los daos del
            final List<BatchDocument> documents = new ArrayList<>();
        	final Iterator<String> it = batchResult.iterator();
        	while (it.hasNext()) {

        		final String docId = it.next();

        		if (batchResult.isSignFailed(docId)) {
        			LOGGER.log(Level.WARNING, logF.f("La firma estaba marcada como erronea desde el inicio")); //$NON-NLS-1$
            		if (stopOnError) {
						ErrorManager.setErrorToSession(session, FIReError.BATCH_SIGNING, trAux);
						Responser.redirectToExternalUrl(redirectErrorUrl, request, response, trAux);
	            		return;
					}
        			continue;
        		}

        		byte[] data;
        		try {
        			data = TempDocumentsManager.retrieveDocument(batchResult.getDocumentReference(docId));
            	}
            	catch (final Exception e) {
            		LOGGER.log(Level.WARNING, logF.f("No se pudo recuperar uno de los datos agregados al lote: " + e)); //$NON-NLS-1$
            		if (stopOnError) {
            			ErrorManager.setErrorToSession(session, FIReError.INVALID_TRANSACTION, trAux);
            			Responser.redirectToExternalUrl(redirectErrorUrl, request, response, trAux);
	            		return;
					}
            		data = null;
            	}
        		documents.add(new BatchDocument(docId, data, batchResult.getSignConfig(docId), batchResult.getDocInfo(docId)));
        	}

            if (documents.size() == 0) {
            	LOGGER.log(Level.WARNING, logF.f("No se han podido recuperar los datos a firmar")); //$NON-NLS-1$
            	ErrorManager.setErrorToSession(session, FIReError.INTERNAL_ERROR, trAux);
            	Responser.redirectToExternalUrl(redirectErrorUrl, request, response, trAux);
                return;
            }

            //TODO: Hacerlo de esta manera obliga a tener cargados todos los documentos del lote en
            // memoria simultaneamente para poder prefirmarlos. Lo ideal seria que se cargara cada
            // documento, se procesara y se descargara antes de pasar al siguiente

            try {
                td = FIReTriHelper.getPreSign(
                    cryptoOperation,
                    format,
                    algorithm,
                    extraParams,
        			signerCert,
                    documents,
                    stopOnError,
                    logF
        		);
            }
            catch (final Throwable e) {
                LOGGER.log(Level.SEVERE, logF.f("No se ha podido obtener la prefirma"), e); //$NON-NLS-1$
                ErrorManager.setErrorToSession(session, FIReError.BATCH_SIGNING, trAux);
                Responser.redirectToExternalUrl(redirectErrorUrl, request, response, trAux);
                return;
            }

            // Actualizamos el resultado de las firmas en caso de haber detectado algun error
            // al procesar su documento asociado
            boolean failed = false;
            for (final BatchDocument doc : documents) {
        		if (doc.getResult() != null) {
        			batchResult.setErrorResult(doc.getId(), doc.getResult());
        			batchResult.setErrorMessage(doc.getId(), doc.getErrorMessage());
        			failed = true;
        		}
        	}

            // En caso de haber detectado algun error y que no se permitan errores, se aborta la operacion
            if (failed && stopOnError) {
            	final String errorMessage = "Se encontraron errores en las prefirmas del lote y se aborta la operacion"; //$NON-NLS-1$
                LOGGER.log(Level.SEVERE, logF.f(errorMessage));
                ErrorManager.setErrorToSession(session, FIReError.BATCH_SIGNING, false, errorMessage, trAux);
                Responser.redirectToExternalUrl(redirectErrorUrl, request, response, trAux);
                return;
            }

            // Si todas las firmas fallaron, saltamos a la pantalla de resultado en lugar de redirigir al
            // proveedor de firma
            if (batchResult.hasErrors() == BatchResult.ALL_FAILED) {
            	LOGGER.info(logF.f("Han fallado todas las firmas del lote. Se redirige a la pantalla de resultado")); //$NON-NLS-1$
            	session.setAttribute(ServiceParams.SESSION_PARAM_TRIPHASE_DATA, Base64.encode(td.toString().getBytes(StandardCharsets.UTF_8), true));
            	session.setAttribute(ServiceParams.SESSION_PARAM_PREVIOUS_OPERATION, SessionFlags.OP_PRE);
                SessionCollector.commit(session, trAux);
                Responser.redirectToExternalUrl(connConfig.getRedirectSuccessUrl(), request, response, trAux);
                return;
            }
        }
        // Transaccion no reconocida
        else {
        	LOGGER.log(Level.WARNING, logF.f("Tipo de transaccion no soportada: " + transactionType)); //$NON-NLS-1$
        	ErrorManager.setErrorToSession(session, FIReError.INTERNAL_ERROR, trAux);
        	Responser.redirectToExternalUrl(redirectErrorUrl, request, response, trAux);
    		return;
        }

        // Obtenemos el conector con el backend ya configurado
        final FIReConnector connector;
        try {
            connector = ProviderManager.getProviderConnector(providerName, connConfig.getProperties(), logF);
        }
        catch (final FIReConnectorFactoryException e) {
        	LOGGER.log(Level.SEVERE, logF.f("No se ha podido cargar el conector del proveedor de firma: %1s", LogUtils.cleanText(providerName)), e); //$NON-NLS-1$
        	ErrorManager.setErrorToSession(session, FIReError.INTERNAL_ERROR, trAux);
        	redirectToErrorPage(originForced, redirectErrorUrl, request, response, trAux);
            return;
        }

        final LoadResult lr;
        try {
            lr = connector.loadDataToSign(
            	userId,
                algorithm,
                FIReTriHelper.fromTriPhaseDataAfirmaToFire(td),
                CertificateFactory.getInstance("X.509").generateCertificate( //$NON-NLS-1$
                    new ByteArrayInputStream(Base64.decode(ServiceUtil.undoUrlSafe(certB64)))
                )
            );
        }
        catch (final FIReConnectorUnknownUserException e) {
        	LOGGER.log(Level.SEVERE, logF.f("El usuario %1s no tiene certificados en el sistema: %2s", LogUtils.cleanText(userId), e)); //$NON-NLS-1$
        	ErrorManager.setErrorToSession(session, FIReError.UNKNOWN_USER, originForced, trAux);
        	redirectToErrorPage(originForced, redirectErrorUrl, request, response, trAux);
        	return;
        }
        catch (final FIReConnectorNetworkException e) {
        	LOGGER.log(Level.SEVERE, logF.f("No se ha podido conectar con el proveedor de firma en la nube"), e); //$NON-NLS-1$
			AlarmsManager.notify(Alarm.CONNECTION_SIGNATURE_PROVIDER, providerName);
            ErrorManager.setErrorToSession(session, FIReError.PROVIDER_INACCESIBLE_SERVICE, originForced, trAux);
            redirectToErrorPage(originForced, redirectErrorUrl, request, response, trAux);
            return;
        }
        catch (final Exception e) {
            LOGGER.log(Level.SEVERE, logF.f("Error en la carga de datos: ") + e, e); //$NON-NLS-1$
            ErrorManager.setErrorToSession(session, FIReError.PROVIDER_ERROR, originForced, trAux);
            redirectToErrorPage(originForced, redirectErrorUrl, request, response, trAux);
        	return;
        }

        // Guardamos en la sesion de FIRe:
        // - El resultado de la prefirma de los datos.
        // - El ID de la transaccion contra el servicio remoto, necesario para solicitar completar la firma
        // - El certificado utilizado para la firma.
        // - Un valor indicativo de que se ha redirigido a la pasarela de autorizacion
        session.setAttribute(ServiceParams.SESSION_PARAM_TRIPHASE_DATA, Base64.encode(lr.getTriphaseData().toString().getBytes(StandardCharsets.UTF_8), true));
        session.setAttribute(ServiceParams.SESSION_PARAM_REMOTE_TRANSACTION_ID, lr.getTransactionId());
        session.setAttribute(ServiceParams.SESSION_PARAM_CERT, certB64);
        session.setAttribute(ServiceParams.SESSION_PARAM_PREVIOUS_OPERATION, SessionFlags.OP_PRE);
        session.setAttribute(ServiceParams.SESSION_PARAM_REDIRECTED_SIGN, Boolean.TRUE);

        SessionCollector.commit(session, trAux);
        session.saveIntoHttpSession(request.getSession());

        // Redirigimos al usuario a la pantalla de autorizacion indicada por el conector
        Responser.redirectToExternalUrl(lr.getRedirectUrl(), request, response, trAux);
    }

    private static FireSession loadSession(final String transactionId, final String userRef, final HttpServletRequest request,
			final TransactionAuxParams trAux) {

    	FireSession session = SessionCollector.getFireSessionOfuscated(transactionId, userRef, request.getSession(false), false, ConfigManager.isSessionSharingForced(), trAux);
        if (session == null && ConfigManager.isSessionSharingForced()) {
        	LOGGER.warning(trAux.getLogFormatter().f("La transaccion %1s no se ha inicializado o ha caducado", LogUtils.cleanText(transactionId))); //$NON-NLS-1$
        	return null;
		}

		// Si la operacion anterior no fue de seleccion de proveedor, forzamos a que se recargue por si faltan datos
		if (session == null || SessionFlags.OP_CHOOSE != session.getObject(ServiceParams.SESSION_PARAM_PREVIOUS_OPERATION)) {
			LOGGER.info(trAux.getLogFormatter().f("No se encontro la sesion o no estaba actualizada. Forzamos la carga")); //$NON-NLS-1$
			session = SessionCollector.getFireSessionOfuscated(transactionId, userRef, request.getSession(false), false, true, trAux);
		}

		return session;
	}

	/**
	 * Redirige a una p&aacute;gina de error. La p&aacute;gina sera de de error de firma, si existe la posibilidad de
	 * que se pueda reintentar la operaci&oacute;n, o la p&aacute;gina de error proporcionada por el usuario.
	 * @param originForced Indica si era obligatorio el uso de un proveedor de firma concreto.
	 * @param connConfig Configuraci&oacute;n de la transacci&oacute;n.
	 * @param request Objeto de petici&oacute;n al servlet.
	 * @param response Objeto de respuesta del servlet.
	 * @param trAux Informaci&oacute;n auxiliar de la transacci&oacute;n.
	 */
	private static void redirectToErrorPage(final boolean originForced, final String redirectErrorUrl,
			final HttpServletRequest request, final HttpServletResponse response,
			final TransactionAuxParams trAux) {
		if (originForced) {
			Responser.redirectToExternalUrl(redirectErrorUrl, request, response, trAux);
		}
		else {
			Responser.redirectToUrl(FirePages.PG_SIGNATURE_ERROR, request, response, trAux);
		}
	}
}