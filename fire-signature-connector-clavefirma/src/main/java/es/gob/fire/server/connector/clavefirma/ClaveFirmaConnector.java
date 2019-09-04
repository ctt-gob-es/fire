/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.connector.clavefirma;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.openlandsw.rss.gateway.CertificateInfo;
import com.openlandsw.rss.gateway.DataToSign;
import com.openlandsw.rss.gateway.DataTransactionResult;
import com.openlandsw.rss.gateway.DocumentsToSign;
import com.openlandsw.rss.gateway.EndTransactionResult;
import com.openlandsw.rss.gateway.GateWayAPI;
import com.openlandsw.rss.gateway.ListOwnerCertificateInfo;
import com.openlandsw.rss.gateway.ParameterAux;
import com.openlandsw.rss.gateway.QueryCertificatesResult;
import com.openlandsw.rss.gateway.SignsInfo;
import com.openlandsw.rss.gateway.StartOpTransactionResult;
import com.openlandsw.rss.gateway.StartOperationInfo;
import com.openlandsw.rss.gateway.StartTransactionResult;
import com.openlandsw.rss.gateway.constants.ConstantsGateWay;
import com.openlandsw.rss.gateway.constants.ConstantsGateWay.GateWayOperationCtes.StartOpTransactionCtes;
import com.openlandsw.rss.gateway.exception.SafeCertGateWayException;

import es.gob.afirma.core.misc.Base64;
import es.gob.afirma.core.signers.AOSignConstants;
import es.gob.fire.server.connector.BadConfigurationException;
import es.gob.fire.server.connector.CertificateBlockedException;
import es.gob.fire.server.connector.DocInfo;
import es.gob.fire.server.connector.FIReCertificateAvailableException;
import es.gob.fire.server.connector.FIReCertificateException;
import es.gob.fire.server.connector.FIReConnector;
import es.gob.fire.server.connector.FIReConnectorNetworkException;
import es.gob.fire.server.connector.FIReConnectorUnknownUserException;
import es.gob.fire.server.connector.FIReSignatureException;
import es.gob.fire.server.connector.GenerateCertificateResult;
import es.gob.fire.server.connector.LoadResult;
import es.gob.fire.server.connector.TriphaseData;
import es.gob.fire.server.connector.TriphaseData.TriSign;
import es.gob.fire.server.connector.WeakRegistryException;

/** Implementaci&oacute;n del API interno de firma en la nube mediante los
 * certificados del servicio de custodia de Cl@ve Firma.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
public final class ClaveFirmaConnector extends FIReConnector {

    private static final String EX_MSG_INVALID_PROVIDER_NAME = "El par\u00E1metro no cumple con la longitud requerida. Verifique que no supera el m\u00E1ximo permitido o sea vac\u00EDo."; //$NON-NLS-1$

    /** Resultado de la operacion de generaci&oacute;n de exito en caso de error. */
    private static final String NEW_CERT_SUCCESS_RESULT = "OK"; //$NON-NLS-1$

    /** Identificador del par&aacute;metro con el que indicar el nombre del proveedor del servicio. */
    private static final String PARAM_PROVIDER_NAME = "PROVIDER_NAME"; //$NON-NLS-1$

    /** Identificador del par&aacute;metro con el que indicar el nombre del proceso que invoca al servicio. */
    private static final String PARAM_PROCEDURE_NAME = "procedureName"; //$NON-NLS-1$

    /** Identificador del par&aacute;metro con el que indicar si el proveedor debe permitir
     * generar un nuevo certificado a sus usuarios cuando no tengan uno v&aacute;lido. */
    private static final String PARAM_ALLOW_REQUEST_NEW_CERT = "allowRequestNewCert"; //$NON-NLS-1$

	private static final int CERT_STATE_CODE_BLOCKED = 5;

	private static final String FAKE_URL = "https://localhost"; //$NON-NLS-1$

    /** Manejador de trazas. */
    private static final Logger LOGGER = Logger.getLogger(ClaveFirmaConnector.class.getName());

    private String redirectOkUrl = null;
    private String redirectErrorUrl = null;
    private String procedureName = null;
    private String providerName = null;

    private Properties providerConfig = null;

    @Override
    public void init(final Properties config) {
    	this.providerConfig = config;
        this.providerName = config.getProperty("providerName"); //$NON-NLS-1$
    }

    @Override
    public void initOperation(final Properties config) {
        if (config != null) {
            this.redirectOkUrl = config.getProperty("redirectOkUrl"); //$NON-NLS-1$
            this.redirectErrorUrl = config.getProperty("redirectErrorUrl"); //$NON-NLS-1$
            this.procedureName = config.getProperty("procedureName"); //$NON-NLS-1$
        }
    }

    @Override
    public X509Certificate[] getCertificates(final String subjectId) throws FIReCertificateException,
                                                                            FIReConnectorUnknownUserException,
                                                                            FIReConnectorNetworkException,
                                                                            CertificateBlockedException,
    																		WeakRegistryException {
    	final String ownerId = prepareSubjectId(subjectId);

    	final GateWayAPI gatewayApi = getGateWayApi();

        final QueryCertificatesResult certs;
        try {
            certs = gatewayApi.queryCertificatesFiltered(ownerId,
                    ConstantsGateWay.OPERATION_SIGN);
        }
        catch (final SafeCertGateWayException e) {

            if (ClaveFirmaErrorManager.ERROR_CODE_UNKNOWN_USER.equals(e.getCode())) {
                throw new FIReConnectorUnknownUserException(e);
            }
            if (e.getCause() != null && e.getCause().getCause() instanceof java.net.ConnectException) {
                throw new FIReConnectorNetworkException(e);
            }

            throw new FIReCertificateException(
                    "Error " + e.getCode() + " en la llamada al servicio de custodia: " + e, e //$NON-NLS-1$ //$NON-NLS-2$
            );
        }
        if (certs == null || certs.getCertificates().size() == 0) {
            if (isSignCertificateBlocked(gatewayApi, ownerId)) {
            	throw new CertificateBlockedException("El certificado de firma esta bloqueado"); //$NON-NLS-1$
            }
            else if (isUserWeakRegistry(gatewayApi, ownerId)) {
            	throw new WeakRegistryException("El usuario realizo un registro debil y no puede tener certificados de firma"); //$NON-NLS-1$
            }
            return new X509Certificate[0];
        }

        final List<CertificateInfo> certsInfo = certs.getCertificates();
        final X509Certificate[] ret = new X509Certificate[certsInfo.size()];
        final CertificateFactory cf;
        try {
            cf = CertificateFactory.getInstance("X.509"); //$NON-NLS-1$
        }
        catch (final Exception e) {
            throw new FIReCertificateException(
                "Error obteniendo la factoria de certificados: " + e, e //$NON-NLS-1$
            );
        }
        for (int i = 0; i < certsInfo.size(); i++) {
            try {
                ret[i] = (X509Certificate) cf.generateCertificate(
            		new ByteArrayInputStream(certsInfo.get(i).getCertificate())
        		);
            }
            catch (final CertificateException e) {
                throw new FIReCertificateException(
                    "Error extrayendo un certificado de usuario: " + e, e //$NON-NLS-1$
                );
            }
        }
        return ret;
    }

    @Override
    public LoadResult loadDataToSign(final String subjectId,
                                          final String algorithm,
                                          final TriphaseData datas,
                                          final Certificate signCert) throws FIReCertificateException,
                                          									 FIReSignatureException,
                                                                             IOException,
                                                                             FIReConnectorUnknownUserException,
                                                                             FIReConnectorNetworkException {

        if (this.providerName == null || this.providerName.trim().length() == 0) {
        	throw new BadConfigurationException(
        			"No se ha proporcionado el nombre del proveedor en el fichero de configuracion"); //$NON-NLS-1$
        }

    	final DataToSign dts = new DataToSign();
        try {
            dts.setCertificate(signCert.getEncoded());
        }
        catch (final Exception e) {
            throw new FIReCertificateException(
                "Error estableciendo el certificado del firmante: " + e, e //$NON-NLS-1$
            );
        }
        if (this.redirectErrorUrl == null || this.redirectOkUrl == null) {
            throw new IllegalStateException(
                "No se indico las configuracion obligatoria de URL de redireccion" //$NON-NLS-1$
            );
        }
        dts.setRedirectError(this.redirectErrorUrl);
        dts.setRedirectOK(this.redirectOkUrl);

        final String digestAlgorithmName = AOSignConstants.getDigestAlgorithmName(algorithm);

        dts.setDigestAlgorithm(digestAlgorithmName);

        final MessageDigest md;
        try {
            md = MessageDigest.getInstance(digestAlgorithmName);
        }
        catch (final NoSuchAlgorithmException e) {
            throw new FIReSignatureException(
                "Error obteniendo el objeto de creacion de huellas digitales: " + e, e //$NON-NLS-1$
            );
        }

        final List<TriSign> preTrisSigns = datas.getTriSigns();

        final List<DocumentsToSign> docs = new ArrayList<>(
            datas.getSignsCount()
        );

        // Componemos los datos a firmar a partir de las prefirmas y de ellas tambien
        // extraemos la informacion que le debemos asociar. Ademas, para evitar los problemas
        // con las contrafirmas debido a que todas tendrian el mismo id, lo modificamos para
        // el segundo y subsiguientes.
        for (final TriSign preTrisSign : preTrisSigns) {
            final DocumentsToSign doc = new DocumentsToSign();
            doc.setData(md.digest(preTrisSign.getPresign()));
            doc.setIdData(preTrisSign.getId());
            final DocInfo docInfo = preTrisSign.getDocInfo();
            doc.setNameDocument(docInfo.getName());
            doc.setTitleDocument(docInfo.getTitle());
            docs.add(doc);
        }

        dts.setDocuments(docs);

        // Configura el nombre de proveedor con el que se identifica el
        // componente central frente al servicio de custodia de certificados
        List<ParameterAux> params = null;
        if (this.providerName != null) {
        	params = new ArrayList<>();
        	final ParameterAux paramAux = new ParameterAux();
        	paramAux.setKey(PARAM_PROVIDER_NAME);
        	paramAux.setData(this.providerName);
        	params.add(paramAux);
        }

        // Configura el nombre del procedimiento si se indico
        if (this.procedureName != null) {
        	if (params == null) {
        		params = new ArrayList<>();
        	}
        	final ParameterAux paramAux = new ParameterAux();
        	paramAux.setKey(PARAM_PROCEDURE_NAME);
        	paramAux.setData(this.procedureName);
        	params.add(paramAux);
        }

    	final String ownerId = prepareSubjectId(subjectId);

        final StartTransactionResult str;
        try {
            str = getGateWayApi().startTransaction(ownerId, dts, params != null ? params.toArray(new ParameterAux[params.size()]): null);
        }
        catch (final SafeCertGateWayException e) {
            if (ClaveFirmaErrorManager.ERROR_CODE_UNKNOWN_USER.equals(e.getCode())) {
                throw new FIReConnectorUnknownUserException(e);
            }
            if (EX_MSG_INVALID_PROVIDER_NAME.equals(e.getMessage().trim())) {
                throw new BadConfigurationException("El formato del ProviderName no es valido: " + EX_MSG_INVALID_PROVIDER_NAME, e); //$NON-NLS-1$
            }
            if (e.getCause() != null && e.getCause().getCause() instanceof java.net.ConnectException) {
                throw new FIReConnectorNetworkException(e);
            }
            throw new FIReSignatureException(
                "Error iniciando la transaccion de firma contra el sistema: " + e, e //$NON-NLS-1$
            );
        }

        return new LoadResult(str.getIdTransaction(), str.getRedirect(), datas);
    }

    @Override
    public Map<String, byte[]> sign(final String transactionId) throws FIReSignatureException {

        if (transactionId == null) {
            throw new IllegalArgumentException(
                "El identificador de transaccion no puede ser nulo" //$NON-NLS-1$
            );
        }

        final GateWayAPI gatewayApi = getGateWayApi();

        final DataTransactionResult dtr;
        try {
            dtr = gatewayApi.dataTransaction(transactionId);
        }
        catch (final Exception e) {
        	// Mostramos el codigo de error interno
        	if (e instanceof SafeCertGateWayException) {
        		LOGGER.warning("Error en la firma. Codigo emitido por la GISS: " + ((SafeCertGateWayException) e).getCode()); //$NON-NLS-1$
        	}
            throw new FIReSignatureException(
                "No se ha podido recuperar la transaccion de firma: " + e, e //$NON-NLS-1$
            );
        }

        final List<SignsInfo> ssi = dtr.getSigns();
        final Map<String, byte[]> ret = new ConcurrentHashMap<>(ssi.size());
        for (final SignsInfo si : ssi) {
        	ret.put(si.getIdData(), si.getSign());
        }

        return ret;
    }

    @Override
    public void endSign(final String transactionId) {
    	try {
            final EndTransactionResult ets = new GateWayAPI().endTransaction(transactionId);
            LOGGER.fine(
                "Transaccion de firma cerrada: " + ets.getDescription() //$NON-NLS-1$
            );
        }
        catch (final SafeCertGateWayException e) {
            LOGGER.warning(
                "No se ha podido cerrar la transaccion de firma: " + e //$NON-NLS-1$
            );
        }
    }

	@Override
	public GenerateCertificateResult generateCertificate(final String subjectId) throws FIReCertificateException, WeakRegistryException {

		final StartOperationInfo opInfo = new StartOperationInfo();
		opInfo.setRedirectError(this.redirectErrorUrl);
		opInfo.setRedirectOK(this.redirectOkUrl);
		opInfo.setOperationName(StartOpTransactionCtes.ISSUE_CERTIFICATE);

		final ParameterAux param = new ParameterAux();
		param.setKey(StartOpTransactionCtes.CERTIFICATE_TYPE);
		param.setData(StartOpTransactionCtes.CERTIFICATE_TYPE_SIGN);

		final ParameterAux[] paramsList = new ParameterAux[1];
		paramsList[0] = param;

    	final String ownerId = prepareSubjectId(subjectId);

		StartOpTransactionResult intermediateResult;
		try {
			intermediateResult = getGateWayApi().startOpTransaction(ownerId, opInfo, paramsList);
		} catch (final SafeCertGateWayException e) {

			if (ClaveFirmaErrorManager.ERROR_CODE_WEAK_REGISTRY.equals(e.getCode())) {
				throw new WeakRegistryException("El usuario realizo un registro debil y no puede tener certificados de firma", e); //$NON-NLS-1$
			}

			if (ClaveFirmaErrorManager.isCertAvailable(e)) {
				throw new FIReCertificateAvailableException(e);
			}

			throw new FIReCertificateException("No se pudo generar el nuevo certificado de firma", e); //$NON-NLS-1$
		}

		return new GenerateCertificateResult(
				intermediateResult.getIdTransaction(),
				intermediateResult.getRedirect());
	}

	@Override
	public byte[] recoverCertificate(final String transactionId) throws FIReCertificateException {

		// Retomamos el control
		DataTransactionResult result;
		try {
			result = getGateWayApi().dataTransaction(transactionId);
		} catch (final SafeCertGateWayException e) {
			throw new FIReCertificateException("No se pudo recuperar el resultado de la generacion de certificado", e); //$NON-NLS-1$
		}

		final String resultOp = result.getStateTransaction().getResult();
		if (!NEW_CERT_SUCCESS_RESULT.equalsIgnoreCase(resultOp)) {
			throw new FIReCertificateException("Error al generar el nuevo certificado: " + resultOp); //$NON-NLS-1$
		}

		return result.getCertificate();
	}

	/**
	 * Proporciona una instancia del API de conexi&oacute;n con el servicio
	 * de custodia de Cl@ve Firma.
	 * @return Instancia del API de conexi&oacute;n.
	 */
	private GateWayAPI getGateWayApi() {

        final GateWayAPI gatewayApi = new GateWayAPI();
       	gatewayApi.setConfig(this.providerConfig);
        return gatewayApi;
	}

	/**
	 * Comprueba si el usuario tiene alg&uacute;n certificado de firma bloqueado.
	 * @param gatewayApi API de conexi&oacute;n con el servicio de custodia.
	 * @param ownerId Identificador del usuario.
	 * @return {@code true} si el certificado de firma del usuario est&aacute; bloqueado, {@code false}
	 * en caso contrario.
	 * @throws FIReCertificateException Cuando no se pueden recuperar los certificados del usuario
	 * o se produce un error durante su an&aacute;lisis.
	 */
	private static boolean isSignCertificateBlocked(final GateWayAPI gatewayApi, final String ownerId)
			throws FIReCertificateException {

		final ListOwnerCertificateInfo[] certsInfo;
		try {
			certsInfo = gatewayApi.listOwnerCertificates(ownerId).getCertificates();
		} catch (final SafeCertGateWayException e) {
            throw new FIReCertificateException(
                    "Error al recuperar todos los certificados del usuario del servicio de custodia: " + e, e //$NON-NLS-1$
            );
		}

		// Creo una factoria de certificados para poder analizarlos
		final CertificateFactory cf;
		try {
			cf = CertificateFactory.getInstance("X.509"); //$NON-NLS-1$
		}
		catch (final Exception e) {
			throw new FIReCertificateException(
					"Error obteniendo la factoria de certificados: " + e, e //$NON-NLS-1$
					);
		}

		// Si el certificado esta bloqueado, comprobamos si es de firma y, si lo es, indicamos que
		// el certificado de firma esta bloqueado
		for (final ListOwnerCertificateInfo certInfo : certsInfo) {
			if (certInfo.getState() != null && certInfo.getState().getStateCode() == CERT_STATE_CODE_BLOCKED) {
				final byte[] certEncodedB64 = certInfo.getContent();
				X509Certificate cert;
				try {
					cert = (X509Certificate) cf.generateCertificate(
							new ByteArrayInputStream(Base64.decode(certEncodedB64, 0, certEncodedB64.length, false)));
				}
				catch (final Exception e) {
					throw new FIReCertificateException("No se pudo decodificar un certificado del usuario: e", e); //$NON-NLS-1$
				}
				if (cert.getKeyUsage()[1] == true) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Comprueba si el usuario realiz&oacute; un registro d&eacute;bil.
	 * @param gatewayApi API de conexi&oacute;n con el servicio de custodia.
	 * @param ownerId Identificador del usuario.
	 * @return {@code true} si el usuario realiz&oacute; un registro d&eacute;bil, {@code false}
	 * en caso contrario.
	 * @throws FIReCertificateException Cuando no se puede comprobar el motivo por el que el usuario
	 * no puede recuperar sus certificados.
	 */
	private static boolean isUserWeakRegistry(final GateWayAPI gatewayApi, final String ownerId)
			throws FIReCertificateException {

		final StartOperationInfo opInfo = new StartOperationInfo();
		opInfo.setRedirectError(FAKE_URL);
		opInfo.setRedirectOK(FAKE_URL);
		opInfo.setOperationName(StartOpTransactionCtes.ISSUE_CERTIFICATE);

		final ParameterAux param = new ParameterAux();
		param.setKey(StartOpTransactionCtes.CERTIFICATE_TYPE);
		param.setData(StartOpTransactionCtes.CERTIFICATE_TYPE_SIGN);

		final ParameterAux[] paramsList = new ParameterAux[1];
		paramsList[0] = param;
		StartOpTransactionResult intermediateResult;
		try {
			intermediateResult = gatewayApi.startOpTransaction(ownerId, opInfo, paramsList);
		} catch (final SafeCertGateWayException e) {
			if (ClaveFirmaErrorManager.ERROR_CODE_WEAK_REGISTRY.equals(e.getCode())) {
				return true;
			}
			LOGGER.warning("Error al comprobar si el usuario realizo un registro debil. Codigo de error: " + e.getCode()); //$NON-NLS-1$
            throw new FIReCertificateException(
                    "Error al comprobar si el usuario realizo un registro debil: " + e, e //$NON-NLS-1$
            );
		}
		// Si la transaccion se inicio correctamente, la intentamos cerrar para
		// que no se quede pendiente
		try {
			gatewayApi.endTransaction(intermediateResult.getIdTransaction());
		}
		catch (final Exception e) {
			LOGGER.warning("Ocurrio un error al cerrar la transaccion con el proveedor: " + e); //$NON-NLS-1$
			// No hacemos nada
		}

		return false;
	}

	@Override
	public boolean allowRequestNewCerts() {

		// Se permitira la emision de nuevos certificados salvo que se configure
		// expresamente el valor "false"

		boolean allowed = true;
		if (this.providerConfig != null) {
			final String allowedValue = this.providerConfig.getProperty(PARAM_ALLOW_REQUEST_NEW_CERT);
			if (allowedValue != null && Boolean.FALSE.toString().equalsIgnoreCase(allowedValue)) {
				allowed = false;
			}
		}
		return allowed;
	}

	/**
	 * Prepara el identificador de usuario proporcionado para el correcto procesamiento por parte
	 * de la GISS.
	 * @param subjectId Identificador del usuario.
	 * @return Identificador del usuario preparado para su env&iacute;o a la GISS.
	 */
	private static String prepareSubjectId(final String subjectId) {
		// Convertimos el identificador a mayusculas, porque la GISS parece guardar en
		// mayusculas la letra del DNI de los ciudadanos
		return subjectId != null ? subjectId.toUpperCase() : null;
	}
}
