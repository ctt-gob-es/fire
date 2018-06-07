/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.client;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import es.gob.fire.client.HttpsConnection.Method;

/**
 * Cliente para el acceso al componente central de FIRe. Mediante esta clase se pueden
 * ejecutar todas las operaciones soportadas por FIRe.<br/>
 * Para la configuraci&oacute;n de la conexi&oacute;n con FIRe es necesario que se
 * proporcionen ciertos par&aacute;metros a trav&eacute;s de un objeto de propiedades
 * o que estas est&eacute;n establecidas en el fichero de configuraci&oacute;n
 * <i>client_config.properties</i>, localizado en el directorio de configuraci&oacute;n
 * establecido mediante la propiedades del sistema <i>fire.config.path</i> o en el
 * <i>classpath</i> del servicio.
 * Las propiedades a configurar son:
 * <ul>
 * <li><b>fireUrl</b>: URL del servicio del componente central. Esta URL debe
 * proporcion&aacute;rsela el administrador del servicio.</li>
 * <li><b>javax.net.ssl.keyStore</b>: Ruta local absoluta al almac&eacute;n de claves
 * para la autenticaci&oacute;n SSL cliente contra el componente central. En este
 * almac&eacute;n debe encontrarse el certificado y la clave privada con la que se
 * habilit&oacute; el acceso de la aplicaci&oacute;n al componente central.</li>
 * <li><b>javax.net.ssl.keyStorePassword</b>: Contrase&ntilde;a del almac&eacute;n de
 * claves con el certificado de autenticaci&oacute;n SSL cliente.</li>
 * <li><b>javax.net.ssl.keyStoreType</b>: Tipo de almac&eacute;n de claves con el certificado
 * de autenticaci&oacute;n SSL cliente. Se pueden utilizar los valores "JKS" (almac&eacute;n
 * de Java) o "PKCS12" (almac&eacute;n PKCS#12/PFX).</li>
 * <li><b>javax.net.ssl.trustStore</b>: Ruta local absoluta al almac&eacute;n de certificados
 * de confianza en el que se encuentra la CA de los certificados SSL de los servidores a los
 * que se va a acceder. Si se desea aceptar cualquier certificado, independientemente de su
 * emisor, se puede establecer el valor <i>all</i>.</li>
 * <li><b>javax.net.ssl.trustStorePassword</b>: Contrase&ntilde;a del almac&eacute;n de
 * confianza. No se utiliza esta propiedad si se configur&oacute; el valor <i>all</i> en
 * la propiedad <i>javax.net.ssl.trustStorePassword</i>.</li>
 * <li><b>javax.net.ssl.trustStoreType</b>: Tipo de almac&eacute;n de confianza. Se pueden
 * utilizar los valores "JKS" (almac&eacute;n de Java) o "PKCS12" (almac&eacute;n PKCS#12/PFX).
 * No se utiliza esta propiedad si se configur&oacute; el valor <i>all</i> en la propiedad
 * <i>javax.net.ssl.trustStorePassword</i>.</li>
 * </ul>
 */
public class FireClient {

    private static final String PROPERTY_KEY_SERVICE_URL = "fireUrl"; //$NON-NLS-1$

    private static final String TAG_VALUE_APP_ID = "$$APPID$$"; //$NON-NLS-1$
    private static final String TAG_VALUE_OPERATION = "$$OPERATION$$"; //$NON-NLS-1$

    private static final String TAG_VALUE_CONFIG = "$$CONFIG$$"; //$NON-NLS-1$
    private static final String TAG_VALUE_ALGORITHM = "$$ALGORITHM$$"; //$NON-NLS-1$
    private static final String TAG_VALUE_SUBJECT_ID = "$$SUBJECTID$$"; //$NON-NLS-1$
    private static final String TAG_VALUE_EXTRA_PARAM = "$$EXTRAPARAMS$$"; //$NON-NLS-1$
    private static final String TAG_VALUE_CRYPTO_OPERATION = "$$CRYPTOOPERATION$$"; //$NON-NLS-1$
    private static final String TAG_VALUE_FORMAT = "$$FORMAT$$"; //$NON-NLS-1$
    private static final String TAG_VALUE_DATA = "$$DATA$$"; //$NON-NLS-1$
    private static final String TAG_VALUE_TRANSACTION = "$$TRANSACTION$$"; //$NON-NLS-1$
    private static final String TAG_VALUE_UPGRADE = "$$UPGRADE$$"; //$NON-NLS-1$
    private static final String TAG_VALUE_DOCUMENT_ID = "$$DOCID$$"; //$NON-NLS-1$
    private static final String TAG_VALUE_STOP_ON_ERROR = "$$STOPERROR$$"; //$NON-NLS-1$

    private static final String URL_PARAMETERS_BASE =
            "op=" + TAG_VALUE_OPERATION + //$NON-NLS-1$
            "&appid=" + TAG_VALUE_APP_ID + //$NON-NLS-1$
    		"&subjectid=" + TAG_VALUE_SUBJECT_ID; //$NON-NLS-1$

    private static final String URL_PARAMETERS_SIGN =
    		"&cop=" + TAG_VALUE_CRYPTO_OPERATION + //$NON-NLS-1$
    		"&algorithm=" + TAG_VALUE_ALGORITHM + //$NON-NLS-1$
            "&format=" + TAG_VALUE_FORMAT + //$NON-NLS-1$
    		"&properties=" + TAG_VALUE_EXTRA_PARAM + //$NON-NLS-1$
            "&dat=" + TAG_VALUE_DATA + //$NON-NLS-1$
    		"&config=" + TAG_VALUE_CONFIG; //$NON-NLS-1$

    private static final String URL_PARAMETERS_RECOVER_SIGNATURE =
    		"&transactionid=" + TAG_VALUE_TRANSACTION + //$NON-NLS-1$
    		"&upgrade=" + TAG_VALUE_UPGRADE; //$NON-NLS-1$

    private static final String URL_PARAMETERS_RECOVER_SIGNATURE_RESULT =
    		"&transactionid=" + TAG_VALUE_TRANSACTION; //$NON-NLS-1$

    private static final String URL_PARAMETERS_CREATE_BATCH =
            "&config=" + TAG_VALUE_CONFIG + //$NON-NLS-1$
            "&algorithm=" + TAG_VALUE_ALGORITHM + //$NON-NLS-1$
            "&properties=" + TAG_VALUE_EXTRA_PARAM + //$NON-NLS-1$
            "&cop=" + TAG_VALUE_CRYPTO_OPERATION + //$NON-NLS-1$
            "&format=" + TAG_VALUE_FORMAT + //$NON-NLS-1$
    		"&upgrade=" + TAG_VALUE_UPGRADE; //$NON-NLS-1$

    private static final String URL_PARAMETERS_ADD_BATCH_DOCUMENT =
    		"&transactionid=" + TAG_VALUE_TRANSACTION + //$NON-NLS-1$
    		"&docid=" + TAG_VALUE_DOCUMENT_ID + //$NON-NLS-1$
    		"&config=" + TAG_VALUE_CONFIG + //$NON-NLS-1$
    		"&dat=" + TAG_VALUE_DATA; //$NON-NLS-1$

    private static final String URL_PARAMETERS_ADD_BATCH_DOCUMENT_WITH_CONFIG =
    		"&transactionid=" + TAG_VALUE_TRANSACTION + //$NON-NLS-1$
    		"&docid=" + TAG_VALUE_DOCUMENT_ID + //$NON-NLS-1$
    		"&config=" + TAG_VALUE_CONFIG + //$NON-NLS-1$
    		"&dat=" + TAG_VALUE_DATA + //$NON-NLS-1$
    		"&cop=" + TAG_VALUE_CRYPTO_OPERATION + //$NON-NLS-1$
            "&format=" + TAG_VALUE_FORMAT + //$NON-NLS-1$
            "&properties=" + TAG_VALUE_EXTRA_PARAM + //$NON-NLS-1$
    		"&upgrade=" + TAG_VALUE_UPGRADE; //$NON-NLS-1$

    private static final String URL_PARAMETERS_SIGN_BATCH =
    		"&transactionid=" + TAG_VALUE_TRANSACTION + //$NON-NLS-1$
            "&stoponerror=" + TAG_VALUE_STOP_ON_ERROR; //$NON-NLS-1$

    private static final String URL_PARAMETERS_RECOVER_BATCH =
    		"&transactionid=" + TAG_VALUE_TRANSACTION; //$NON-NLS-1$

    private static final String URL_PARAMETERS_RECOVER_BATCH_STATE =
    		"&transactionid=" + TAG_VALUE_TRANSACTION; //$NON-NLS-1$

    private static final String URL_PARAMETERS_RECOVER_SIGN_BATCH =
    		"&transactionid=" + TAG_VALUE_TRANSACTION + //$NON-NLS-1$
    		"&docid=" + TAG_VALUE_DOCUMENT_ID; //$NON-NLS-1$

    private static final String URL_PARAMETERS_RECOVER_ERROR =
    		"&transactionid=" + TAG_VALUE_TRANSACTION; //$NON-NLS-1$

    private static final Logger LOGGER = Logger.getLogger(FireClient.class.getName());

    private final String appId;

    private final String serviceUrl;

    private HttpsConnection conn;

	/**
	 * Construye el cliente de FIRe cargando la configuraci&oacute;n para la
	 * conexi&oacute;n con el componente central del fichero
	 * <i>client_config.properties</i> del directorio configurado a trav&eacute;s
	 * de propiedades del sistema o del <i>classpath</i>.<br/>
	 * Las propiedades necesarias de la configuraci&oacute;n se detallan en el
	 * Javadoc de la clase ({@link FireClient}).
	 * @param appId Identificador de aplicaci&oacute;n.
	 * @throws ClientConfigFilesNotFoundException Cuando no se encuentra el fichero
	 * de configuraci&oacute;n.
	 */
	public FireClient(final String appId) throws ClientConfigFilesNotFoundException {
		this(appId, ConfigManager.loadConfig(), null);
	}

	/**
	 * Construye el cliente de FIRe a partir de la configuraci&oacute;n proporcionada.<br/>
	 * Las propiedades necesarias de la configuraci&oacute;n se detallan en el
	 * Javadoc de la clase ({@link FireClient}).
	 * @param appId Identificador de aplicaci&oacute;n.
	 * @param config Configuraci&oacute;n para la conexion con el componente central.<br/>
	 * Las propiedades necesarias de la configuraci&oacute;n se detallan en el
	 * Javadoc de la clase ({@link FireClient}).
	 */
	public FireClient(final String appId, final Properties config) {
		this(appId, config, null);
	}

	/**
	 * Construye el cliente de FIRe cargando la configuraci&oacute;n para la
	 * conexi&oacute;n con el componente central del fichero
	 * <i>client_config.properties</i> del directorio configurado a trav&eacute;s
	 * de propiedades del sistema o del <i>classpath</i>.<br/>
	 * Las propiedades necesarias de la configuraci&oacute;n se detallan en el
	 * Javadoc de la clase ({@link FireClient}).
	 * @param appId Identificador de aplicaci&oacute;n.
	 * @param decipher Objeto para el descifrado de las contrase&ntilde;as definidas en el
	 * objeto de propiedades. Si se pasa {@code null}, se entender&aacute;a que las
	 * contrase&ntildes;s est&aacute;n en claro. Las contrase&ntilde;as cifradas deben
	 * tener la forma {@ciphered: PASSWORD_CIFRADA_EN_BASE64 }.
	 * @throws ClientConfigFilesNotFoundException Cuando no se encuentra el fichero
	 * de configuraci&oacute;n.
	 */
	public FireClient(final String appId, final PasswordDecipher decipher) throws ClientConfigFilesNotFoundException {
		this(appId, ConfigManager.loadConfig(), decipher);
	}

	/**
	 * Construye el cliente de FIRe a partir de la configuraci&oacute;n proporcionada.
	 * @param appId Identificador de aplicaci&oacute;n.
	 * @param config Configuraci&oacute;n para la conexion con el componente central.<br/>
	 * Las propiedades necesarias de la configuraci&oacute;n se detallan en el
	 * Javadoc de la clase ({@link FireClient}).
	 * @param decipher Objeto para el descifrado de las contrase&ntilde;as definidas en el
	 * objeto de propiedades. Si se pasa {@code null}, se entender&aacute;a que las
	 * contrase&ntildes;s est&aacute;n en claro. Las contrase&ntilde;as cifradas deben
	 * tener la forma {@ciphered: PASSWORD_CIFRADA_EN_BASE64 }.
	 */
	public FireClient(final String appId, final Properties config, final PasswordDecipher decipher) {

    	this.appId = appId;
    	if (this.appId == null) {
            throw new NullPointerException(
                    "No se ha configurado un identificador de aplicacion" //$NON-NLS-1$
            );
        }

    	this.serviceUrl = config.getProperty(PROPERTY_KEY_SERVICE_URL);
        if (this.serviceUrl == null) {
            throw new IllegalStateException(
                    "No se ha configurado la URL del servicio de FIRe" //$NON-NLS-1$
            );
        }

        LOGGER.info("Se usara el siguiente servicio de acceso a FIRe: " + this.serviceUrl); //$NON-NLS-1$

        try {
			this.conn = HttpsConnection.getConnection(config, decipher);
		} catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Error en la configuracion de la comunicacion con el componente centralizado: " + e, e); //$NON-NLS-1$
			throw new SecurityException("Error en la configuracion de la comunicacion con el componente centralizado", e); //$NON-NLS-1$
		}
	}

    /**
     * Carga datos para ser posteriormente firmados.
     * @param subjectId
     *            Identificador del titular de la clave de firma.
     * @param op
     *            Tipo de operaci&oacute;n a realizar.
     * @param ft
     *            Formato de la operaci&oacute;n.
     * @param algth
     *            Algoritmo de firma.
     * @param prop
     *            Propiedades extra a a&ntilde;adir a la firma (puede ser
     *            <code>null</code>).
     * @param d
     *            Datos a firmar, cofirmar o contrafirmar.
     * @param config
     *            Configuraci&oacute;n a indicar al servicio remoto (dependiente
     *            de la implementaci&oacute;n).
     * @return Resultado de la carga de los datos para la firma.
     * @throws IOException
     *             Si hay problemas en la llamada al servicio de red.
     * @throws HttpNetworkException
     * 				Cuando se produce un error de red.
     * @throws HttpForbiddenException
     * 				Cuando se deniega el acceso al componente central.
     * @throws HttpOperationException Error gen&eacute;rico en la operaci&oacute;n de firma.
     */
    public SignOperationResult sign(
    		final String subjectId,
            final SignProcessConstants.SignatureOperation op,
            final SignProcessConstants.SignatureFormat ft,
            final SignProcessConstants.SignatureAlgorithm algth,
            final Properties prop, final byte[] d,
            final Properties config)
            throws IOException, HttpNetworkException, HttpForbiddenException,
            HttpOperationException {

    	if (op == null) {
            throw new IllegalArgumentException(
                    "El tipo de operacion de firma a realizar no puede ser nulo" //$NON-NLS-1$
            );
        }
        if (ft == null) {
            throw new IllegalArgumentException(
                    "El formato de firma no puede ser nulo" //$NON-NLS-1$
            );
        }
        if (algth == null) {
            throw new IllegalArgumentException(
                    "El algoritmo de firma no puede ser nulo" //$NON-NLS-1$
            );
        }
        if (d == null) {
            throw new IllegalArgumentException(
                    "Los datos a firmar/multifirmar no pueden ser nulos" //$NON-NLS-1$
            );
        }
        if (subjectId == null || "".equals(subjectId)) { //$NON-NLS-1$
            throw new IllegalArgumentException(
                    "El identificador del titular no puede ser nulo" //$NON-NLS-1$
            );
        }

        final String dataB64 = Base64.encode(d, true);
        final String extraParamsB64 = Utils.properties2Base64(prop, true);

        return sign(subjectId, op.toString(), ft.toString(),
                algth.toString(), extraParamsB64, dataB64, config);
    }

    /**
     * Inicia una operaci&oacute;n de firma.
     * @param subjectId
     *            Identificador del titular del certificado de firma.
     * @param op
     *            Tipo de operaci&oacute;n a realizar: sign, cosign o
     *            countersign.
     * @param ft
     *            Formato de la operaci&oacute;n.
     * @param algth
     *            Algoritmo de firma.
     * @param propB64
     *            Propiedades extra a a&ntilde;adir a la firma. Se establece
     *            en Base64 y puede ser <code>null</code>.
     * @param dataB64
     *            Datos a firmar en Base64.
     * @param config
     *            Configuraci&oacute;n a indicar al servicio remoto (dependiente
     *            de la implementaci&oacute;n).
     * @return Resultado de la carga de los datos para la firma.
     * @throws IllegalArgumentException
     * 				Si se proporciona nulo o vac&iacute;o alg&uacute;n
     * 				par&aacute;metro obligatorio.
     * @throws IOException
     * 				Si hay problemas en la llamada al servicio de red.
     * @throws HttpNetworkException
     * 				Cuando se produce un error de red.
     * @throws HttpForbiddenException
     * 				Cuando se deniega el acceso al componente central.
     * @throws HttpOperationException
     * 				Error gen&eacute;rico en la operaci&oacute;n de firma.
     */
    public SignOperationResult sign(
    		final String subjectId, final String op, final String ft,
    		final String algth, final String propB64,
    		final String dataB64, final Properties config)
            throws IOException, HttpNetworkException, HttpForbiddenException,
            HttpOperationException {

    	if (op == null) {
            throw new IllegalArgumentException(
                    "El tipo de operacion de firma a realizar no puede ser nulo" //$NON-NLS-1$
            );
        }
        if (ft == null) {
            throw new IllegalArgumentException(
                    "El formato de firma no puede ser nulo" //$NON-NLS-1$
            );
        }
        if (algth == null) {
            throw new IllegalArgumentException(
                    "El algoritmo de firma no puede ser nulo" //$NON-NLS-1$
            );
        }
        if (dataB64 == null) {
            throw new IllegalArgumentException(
                    "Los datos a firmar no pueden ser nulos" //$NON-NLS-1$
            );
        }
        if (subjectId == null || "".equals(subjectId)) { //$NON-NLS-1$
            throw new IllegalArgumentException(
                    "El identificador del titular no puede ser nulo" //$NON-NLS-1$
            );
        }

        final String urlParameters =
        		URL_PARAMETERS_BASE
        		.replace(TAG_VALUE_APP_ID, this.appId)
        		.replace(TAG_VALUE_SUBJECT_ID, subjectId)
        		.replace(TAG_VALUE_OPERATION, FIReServiceOperation.SIGN.getId()) +
        		URL_PARAMETERS_SIGN
        		.replace(TAG_VALUE_CRYPTO_OPERATION, op)
                .replace(TAG_VALUE_FORMAT, ft)
                .replace(TAG_VALUE_ALGORITHM, algth)
                .replace(TAG_VALUE_EXTRA_PARAM, doBase64UrlSafe(propB64))
                .replace(TAG_VALUE_DATA, doBase64UrlSafe(dataB64))
                .replace(TAG_VALUE_CONFIG, Utils.properties2Base64(config, true));

        final byte[] responseJSON;
        try {
        	responseJSON = this.conn.readUrl(
                            this.serviceUrl, urlParameters, Method.POST);
        } catch (final HttpError e) {
            LOGGER.severe("Error en la llamada al servicio de firma: " + //$NON-NLS-1$
            			e.getResponseDescription());
            switch (e.getResponseCode()) {
            case HttpURLConnection.HTTP_FORBIDDEN:
                throw new HttpForbiddenException(e);
            case HttpURLConnection.HTTP_CLIENT_TIMEOUT:
                throw new HttpNetworkException(e);
            default:
                throw new HttpOperationException(e.getResponseDescription(), e);
            }
        }

        return SignOperationResult.parse(responseJSON);
    }

    /**
     * Firma unos datos haciendo uso del servicio de red de firma en la nube.
     *
     * @param transactionId
     *            Identificador de la transacci&oacute;n.
     * @param subjectId
     * 			  Identificador del usuario que realiza la transacci&oacute;n.
     * @param upgrade
     *            Formato al que queremos mejorar la firma (puede ser
     *            <code>null</code>).
     * @return Firma realizada en servidor.
     * @throws IllegalArgumentException
     * 			   Si se proporciona a nulo el identificados de transacci&oacute;n
     * 			   o el de usuario.
     * @throws IOException
     *             Si hay problemas en la llamada al servicio de red.
     * @throws HttpNetworkException
     * 				Cuando se produce un error de red.
     * @throws HttpForbiddenException
     * 				Cuando se deniega el acceso al componente central.
     * @throws HttpOperationException
     * 			   Si se produjo un error durante la operaci&oacute;n de firma.
     * @throws InvalidTransactionException
     * 			   Cuando la transacci&oacute;n no existe o est&aacute; caducada.
     */
    public TransactionResult recoverSignResult(final String transactionId, final String subjectId, final String upgrade)
    				throws IOException, HttpNetworkException, HttpForbiddenException,
    				HttpOperationException, InvalidTransactionException {

        if (transactionId == null || transactionId.isEmpty()) {
            throw new IllegalArgumentException(
                    "El id de la transaccion no puede ser nulo" //$NON-NLS-1$
            );
        }
        if (subjectId == null || subjectId.isEmpty()) {
            throw new IllegalArgumentException(
                    "El identificador del titular no puede ser nulo" //$NON-NLS-1$
            );
        }

        String urlParameters =
        		URL_PARAMETERS_BASE
        		.replace(TAG_VALUE_APP_ID, this.appId)
        		.replace(TAG_VALUE_SUBJECT_ID, subjectId)
        		.replace(TAG_VALUE_OPERATION, FIReServiceOperation.RECOVER_SIGN.getId()) +
        		URL_PARAMETERS_RECOVER_SIGNATURE
                .replace(TAG_VALUE_TRANSACTION, transactionId);

        // Si se ha indicado un formato de upgrade, lo actualizamos; si no, lo eliminamos de la URL
        if (upgrade != null && !upgrade.isEmpty()) {
        	urlParameters = urlParameters.replace(TAG_VALUE_UPGRADE, upgrade);
        }
        else {
        	urlParameters = urlParameters.replace("&upgrade=" + TAG_VALUE_UPGRADE , ""); //$NON-NLS-1$ //$NON-NLS-2$
        }

        TransactionResult result;
        try {
        	result = TransactionResult.parse(TransactionResult.RESULT_TYPE_SIGN, this.conn.readUrl(this.serviceUrl, urlParameters, Method.GET));
        } catch (final HttpError e) {
        	LOGGER.severe("Error en la llamada al servicio de recuperacion del resultado de la operacion de firma: " + e.getResponseDescription()); //$NON-NLS-1$
        	if (e.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN) {
        		throw new HttpForbiddenException(e);
        	} else if (e.getResponseCode() == HttpURLConnection.HTTP_CLIENT_TIMEOUT) {
        		throw new HttpNetworkException(e);
        	} else if (e.getResponseCode() == HttpCustomErrors.INVALID_TRANSACTION.getErrorCode()) {
        		throw new InvalidTransactionException(HttpCustomErrors.INVALID_TRANSACTION.getErrorDescription(), e);
        	} else if (e.getResponseCode() == HttpCustomErrors.SIGN_ERROR.getErrorCode()) {
        		throw new HttpOperationException(HttpCustomErrors.SIGN_ERROR.getErrorDescription(), e);
        	} else if (e.getResponseCode() == HttpCustomErrors.POSTSIGN_ERROR.getErrorCode()) {
        		throw new HttpOperationException(HttpCustomErrors.POSTSIGN_ERROR.getErrorDescription(), e);
        	} else if (e.getResponseCode() == HttpCustomErrors.UPGRADING_ERROR.getErrorCode()) {
        		throw new HttpOperationException(HttpCustomErrors.UPGRADING_ERROR.getErrorDescription(), e);
        	} else if (e.getResponseCode() == HttpCustomErrors.SAVING_ERROR.getErrorCode()) {
        		throw new HttpOperationException(HttpCustomErrors.SAVING_ERROR.getErrorDescription(), e);
        	} else {
        		throw new HttpOperationException(e.getMessage(), e);
        	}
        }
        catch (final Exception e) {
        	LOGGER.log(
        			Level.SEVERE,
        			"Error en la comunicacion con el servicio de recuperacion de firma", //$NON-NLS-1$
        			e);
        	throw new IOException(e);
        }

        // Si el resultado es un error o si ya contiene la firma, lo devolvemos
        if (result.getErrorCode() != 0 || result.getResult() != null) {
        	return result;
        }

        // Si no tenemos la firma, hacemos una nueva llamada para descargarla
        urlParameters =
        		URL_PARAMETERS_BASE
        		.replace(TAG_VALUE_APP_ID, this.appId)
        		.replace(TAG_VALUE_SUBJECT_ID, subjectId)
        		.replace(TAG_VALUE_OPERATION, FIReServiceOperation.RECOVER_SIGN_RESULT.getId()) +
        		URL_PARAMETERS_RECOVER_SIGNATURE_RESULT
                .replace(TAG_VALUE_TRANSACTION, transactionId);

        byte[] signature;
        try {
        	 signature = this.conn.readUrl(this.serviceUrl, urlParameters, Method.GET);
        } catch (final HttpError e) {
        	LOGGER.severe("Error en la llamada al servicio de recuperacion de firma: " + e.getResponseDescription()); //$NON-NLS-1$
        	if (e.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN) {
        		throw new HttpForbiddenException(e);
        	} else if (e.getResponseCode() == HttpURLConnection.HTTP_CLIENT_TIMEOUT) {
        		throw new HttpNetworkException(e);
        	} else if (e.getResponseCode() == HttpCustomErrors.INVALID_TRANSACTION.getErrorCode()) {
        		throw new InvalidTransactionException(HttpCustomErrors.INVALID_TRANSACTION.getErrorDescription(), e);
        	} else {
        		throw new HttpOperationException(e.getMessage(), e);
        	}
        }
        catch (final Exception e) {
        	LOGGER.log(
        			Level.SEVERE,
        			"Error en la comunicacion con el servicio de recuperacion de firma", //$NON-NLS-1$
        			e);
        	throw new IOException(e);
        }

        result.setResult(signature);

        return result;
    }

    /**
     * Inicia un proceso de firma de lote de documentos. Este m&eacute;todo crea el lote
     * sin documentos y obtiene el ID de transaccion asociado para permitir agregar
     * documentos al lote.
     * @param subjectId Id del usuario propietario del certificado de firma.
     * @param op Operaci&oacute;n a realizar sobre los documentos.
     * @param ft Formato de firma.
     * @param algth Algoritmo de firma.
     * @param propB64 Configuraci&oacute;n adicional del formato de firma.
     * @param upgrade Formato al que actualizar las firmas.
     * @param config Configuraci&oacute;n de la operaci&oacute;n (URLs de destino,
     * nombre del procedimiento,...)
     * @return Resultado de la creaci&oacute;n del lote con el Id de
     * transacci&oacute;n asociado.
     * @throws IOException Cuando no se puede conectar con el servicio.
     * @throws HttpForbiddenException Cuando no se tiene acceso al servicio remoto.
     * @throws HttpNetworkException Cuando ocurre un error de red.
     * @throws HttpOperationException Cuando ocurre un error durante la ejecuci&oacute;n.
     */
    public CreateBatchResult createBatchProcess(
    		final String subjectId, final String op, final String ft,
    		final String algth, final String propB64, final String upgrade, final Properties config)
            throws IOException, HttpForbiddenException, HttpNetworkException,
            	HttpOperationException {

    	if (op == null) {
            throw new IllegalArgumentException(
                    "El tipo de operacion de firma a realizar no puede ser nulo" //$NON-NLS-1$
            );
        }
        if (ft == null) {
            throw new IllegalArgumentException(
                    "El formato de firma no puede ser nulo" //$NON-NLS-1$
            );
        }
        if (algth == null) {
            throw new IllegalArgumentException(
                    "El algoritmo de firma no puede ser nulo" //$NON-NLS-1$
            );
        }
        if (subjectId == null || "".equals(subjectId)) { //$NON-NLS-1$
            throw new IllegalArgumentException(
                    "El identificador del titular no puede ser nulo" //$NON-NLS-1$
            );
        }

        String urlParameters =
        		URL_PARAMETERS_BASE
        		.replace(TAG_VALUE_APP_ID, this.appId)
        		.replace(TAG_VALUE_SUBJECT_ID, subjectId)
        		.replace(TAG_VALUE_OPERATION, FIReServiceOperation.CREATE_BATCH.getId()) +
        		URL_PARAMETERS_CREATE_BATCH
                .replace(TAG_VALUE_CRYPTO_OPERATION, op)
                .replace(TAG_VALUE_FORMAT, ft)
                .replace(TAG_VALUE_ALGORITHM, algth)
                .replace(TAG_VALUE_EXTRA_PARAM, doBase64UrlSafe(propB64))
                .replace(TAG_VALUE_CONFIG, Utils.properties2Base64(config, true));

        // Si se ha indicado un formato de upgrade, lo actualizamos; si no, lo eliminamos de la URL
        if (upgrade != null && !upgrade.isEmpty()) {
        	urlParameters = urlParameters.replace(TAG_VALUE_UPGRADE, upgrade);
        }
        else {
        	urlParameters = urlParameters.replace("&upgrade=" + TAG_VALUE_UPGRADE , ""); //$NON-NLS-1$ //$NON-NLS-2$
        }

        final byte[] responseJSON;
        try {
        	responseJSON = this.conn.readUrl(
                            this.serviceUrl, urlParameters, Method.POST);
        } catch (final HttpError e) {
            LOGGER.severe(
                            "Error en la llamada al servicio de firma: " + e.getResponseDescription() //$NON-NLS-1$
                    );
            switch (e.getResponseCode()) {
            case HttpURLConnection.HTTP_FORBIDDEN:
                throw new HttpForbiddenException(e);
            case HttpURLConnection.HTTP_CLIENT_TIMEOUT:
                throw new HttpNetworkException(e);
            default:
                throw new HttpOperationException(e.getResponseDescription(), e);
            }
        }

        return CreateBatchResult.parse(responseJSON);
    }

    /**
     * Agrega un documento a un lote previamente creado para que se firme con
     * la configuraci&oacute;n establecida al crear el lote.
     * @param transactionId Identificador de la transacci&oacute;n del lote.
     * @param subjectId Identificador del usuario que realiza la transacci&oacute;n.
     * @param documentId Identificador &uacute;nico del documento que se adjunta al lote.
     * @param document Datos a firmar como parte del lote.
     * @param config Conjunto de propiedades adicionales que se podr&aacute;n establecer
     * para configurar la gesti&oacute;n del documento.
     * @throws IOException Cuando no se puede conectar con el servicio.
     * @throws HttpForbiddenException Cuando no se tiene acceso al servicio remoto.
     * @throws HttpNetworkException Cuando ocurre un error de red.
     * @throws NumDocumentsExceededException Cuando se intentan agregar m&aacute;s documentos
     * de los permitidos al lote.
     * @throws DuplicateDocumentException Cuando se el identificador de documento ya se
     * us&oacute; para otro documento del lote.
     * @throws HttpOperationException Cuando ocurre un error durante la ejecuci&oacute;n.
     * @throws InvalidTransactionException Cuando la transacci&oacute;n no existe o
     * est&aacute; caducada.
     */
    public void addDocumentToBatch(final String transactionId, final String subjectId,
    		final String documentId, final byte[] document, final Properties config)
    				throws IOException, HttpForbiddenException, HttpNetworkException,
    				NumDocumentsExceededException, DuplicateDocumentException,
    				InvalidTransactionException, HttpOperationException {

        if (transactionId == null) {
            throw new InvalidTransactionException(
                    "El id de la transaccion no puede ser nulo" //$NON-NLS-1$
            );
        }
        if (subjectId == null || "".equals(subjectId)) { //$NON-NLS-1$
            throw new IllegalArgumentException(
                    "El identificador del titular no puede ser nulo" //$NON-NLS-1$
            );
        }
        if (documentId == null) {
            throw new IllegalArgumentException(
                    "El id de documento no puede ser nulo" //$NON-NLS-1$
            );
        }
        if (document == null) {
            LOGGER.info("Los datos se obtendran del servidor a partir del documentId"); //$NON-NLS-1$
        }

        final String urlParameters =
        		URL_PARAMETERS_BASE
        		.replace(TAG_VALUE_APP_ID, this.appId)
        		.replace(TAG_VALUE_SUBJECT_ID, subjectId)
        		.replace(TAG_VALUE_OPERATION, FIReServiceOperation.ADD_DOCUMENT_TO_BATCH.getId()) +
        		URL_PARAMETERS_ADD_BATCH_DOCUMENT
                .replace(TAG_VALUE_TRANSACTION, transactionId)
        		.replace(TAG_VALUE_DOCUMENT_ID, documentId)
        		.replace(TAG_VALUE_DATA, document != null ? Base64.encode(document, true) : "") //$NON-NLS-1$
                .replace(TAG_VALUE_CONFIG, Utils.properties2Base64(config, true));

        // Llamamos al servicio que, siempre que funcione, devolvera true. Si falla devolvera
        // una excepcion de tipo HttpError
        try {
        	this.conn.readUrl(this.serviceUrl, urlParameters, Method.POST);
        } catch (final HttpError e) {
        	LOGGER.severe("Error en la llamada al servicio de carga de documentos en un lote con configuracion por defecto: " + e.getResponseDescription()); //$NON-NLS-1$
        	// Aplicacion no permitida
        	if (e.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN) {
        		throw new HttpForbiddenException(e);
        	// Problema de red
        	} else if (e.getResponseCode() == HttpURLConnection.HTTP_CLIENT_TIMEOUT) {
        		throw new HttpNetworkException(e);
        	// Se excedio el numero maximo de documentos permitidos en un lote
        	} else if (e.getResponseCode() == HttpCustomErrors.NUM_DOCUMENTS_EXCEEDED.getErrorCode()) {
        		throw new NumDocumentsExceededException(HttpCustomErrors.NUM_DOCUMENTS_EXCEEDED.getErrorDescription(), e);
        	// Se intento agregar un documento con un identificador que ya existe
        	} else if (e.getResponseCode() == HttpCustomErrors.DUPLICATE_DOCUMENT.getErrorCode()) {
        		throw new DuplicateDocumentException(HttpCustomErrors.DUPLICATE_DOCUMENT.getErrorDescription(), e);
        	// Se excedio el numero maximo de documentos permitidos en un lote
    		} else if (e.getResponseCode() == HttpCustomErrors.INVALID_TRANSACTION.getErrorCode()) {
    			throw new InvalidTransactionException(HttpCustomErrors.INVALID_TRANSACTION.getErrorDescription(), e);
    		} else {
        		throw new HttpOperationException(e.getResponseDescription(), e);
        	}
        }
        catch (final Exception e) {
        	LOGGER.log(
        			Level.SEVERE,
        			"Error en la comunicacion con el servicio de recuperacion de firma", //$NON-NLS-1$
        			e);
        	throw new IOException(e);
        }
    }

    /**
     * Agrega un documento a un lote previamente creado indicando las opciones de firma que
     * se deben aplicar sobre ella. Estas opciones tienen preferencia con respecto a las indicadas
     * al crear el lote. Las opciones se aplican al completo. Es decir, omitir alguna de ellas,
     * por ejemplo, implicar&iacute;a un error, ya que no se hereda ninguna de la configuraci&oacute;n
     * de la creaci&oacute;n del lote.
     * @param transactionId Identificador de la transacci&oacute;n del lote.
     * @param subjectId Identificador del usuario que realiza la transacci&oacute;n.
     * @param documentId Identificador &uacute;nico del documento que se adjunta al lote.
     * @param document Datos a firmar como parte del lote.
     * @param config Conjunto de propiedades adicionales que se podr&aacute;n establecer
     * para configurar la gesti&oacute;n del documento.
     * @param op Operaci&oacute;n criptogr&aacute;fica a realizar (firma, cofirma...).
     * @param ft Formato de firma.
     * @param propB64 Configuraci&oacute;n adicional del formato de firma.
     * @param upgrade Nombre del formato actualizado para la mejora de la firma antes de recuperarla.
     * @throws IOException Cuando no se puede conectar con el servicio.
     * @throws HttpForbiddenException Cuando no se tiene acceso al servicio remoto.
     * @throws HttpNetworkException Cuando ocurre un error de red.
     * @throws HttpOperationException Cuando ocurre un error durante la ejecuci&oacute;n.
     * @throws NumDocumentsExceededException Cuando se intentan agregar m&aacute;s documentos
     * de los permitidos al lote.
     * @throws InvalidTransactionException
     * 			   Cuando la transacci&oacute;n no existe o est&aacute; caducada.
     * @throws DuplicateDocumentException
     * 				Cuando se el identificador de documento ya se us&oacute; para otro documento del lote.
     */
    public void addDocumentToBatch(final String transactionId, final String subjectId,
    		final String documentId, final byte[] document,
    		final Properties config, final String op, final String ft,
    		final String propB64, final String upgrade)
    		throws IOException, HttpForbiddenException, HttpNetworkException, HttpOperationException,
    			NumDocumentsExceededException, InvalidTransactionException, DuplicateDocumentException {

        if (transactionId == null) {
            throw new InvalidTransactionException(
                    "El id de la transaccion no puede ser nulo" //$NON-NLS-1$
            );
        }
        if (subjectId == null || "".equals(subjectId)) { //$NON-NLS-1$
            throw new IllegalArgumentException(
                    "El identificador del titular no puede ser nulo" //$NON-NLS-1$
            );
        }
        if (documentId == null) {
            throw new IllegalArgumentException(
                    "El id de documento no puede ser nulo" //$NON-NLS-1$
            );
        }
        if (document == null) {
        	LOGGER.info("Los datos se obtendran del servidor a partir del documentId"); //$NON-NLS-1$
        }

        String urlParameters =
        		URL_PARAMETERS_BASE
        		.replace(TAG_VALUE_APP_ID, this.appId)
        		.replace(TAG_VALUE_SUBJECT_ID, subjectId)
        		.replace(TAG_VALUE_OPERATION, FIReServiceOperation.ADD_DOCUMENT_TO_BATCH.getId()) +
        		URL_PARAMETERS_ADD_BATCH_DOCUMENT_WITH_CONFIG
                .replace(TAG_VALUE_TRANSACTION, transactionId)
        		.replace(TAG_VALUE_DOCUMENT_ID, documentId)
        		.replace(TAG_VALUE_DATA, document != null ? Base64.encode(document, true) : "") //$NON-NLS-1$
        		.replace(TAG_VALUE_CRYPTO_OPERATION, op)
                .replace(TAG_VALUE_FORMAT, ft)
                .replace(TAG_VALUE_EXTRA_PARAM, doBase64UrlSafe(propB64))
                .replace(TAG_VALUE_CONFIG, Utils.properties2Base64(config, true));

        // Si se ha indicado un formato de upgrade, lo actualizamos; si no, lo eliminamos de la URL
        if (upgrade != null && !upgrade.isEmpty()) {
        	urlParameters = urlParameters.replace(TAG_VALUE_UPGRADE, upgrade);
        }
        else {
        	urlParameters = urlParameters.replace("&upgrade=" + TAG_VALUE_UPGRADE , ""); //$NON-NLS-1$ //$NON-NLS-2$
        }

        try {
        	this.conn.readUrl(this.serviceUrl, urlParameters, Method.POST);
        } catch (final HttpError e) {
        	LOGGER.severe("Error en la llamada al servicio de carga de documentos en un lote con configuracion propia: " + e.getResponseDescription()); //$NON-NLS-1$
        	// Aplicacion no permitida
        	if (e.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN) {
        		throw new HttpForbiddenException(e);
        	// Problema de red
        	} else if (e.getResponseCode() == HttpURLConnection.HTTP_CLIENT_TIMEOUT) {
        		throw new HttpNetworkException(e);
        	// Se excedio el numero maximo de documentos permitidos en un lote
        	} else if (e.getResponseCode() == HttpCustomErrors.NUM_DOCUMENTS_EXCEEDED.getErrorCode()) {
        		throw new NumDocumentsExceededException(HttpCustomErrors.NUM_DOCUMENTS_EXCEEDED.getErrorDescription(), e);
        	// Se intento agregar un documento con un identificador que ya existe
        	} else if (e.getResponseCode() == HttpCustomErrors.DUPLICATE_DOCUMENT.getErrorCode()) {
        		throw new DuplicateDocumentException(HttpCustomErrors.DUPLICATE_DOCUMENT.getErrorDescription(), e);
        	// Se excedio el numero maximo de documentos permitidos en un lote
    		} else if (e.getResponseCode() == HttpCustomErrors.INVALID_TRANSACTION.getErrorCode()) {
    			throw new InvalidTransactionException(HttpCustomErrors.INVALID_TRANSACTION.getErrorDescription(), e);
    		} else {
        		throw new HttpOperationException(e.getResponseDescription(), e);
        	}
        }
        catch (final Exception e) {
        	LOGGER.log(
        			Level.SEVERE,
        			"Error en la comunicacion con el servicio de recuperacion de firma", //$NON-NLS-1$
        			e);
        	throw new IOException(e);
        }
    }

    /**
     * Ejecuta el proceso de firma sobre todos los documentos de un lote previamente creado.
     * @param transactionId Identificador de la transacci&oacute;n devuelta por la
     * operaci&oacute;n de creaci&oacute;n del lote.
     * @param subjectId Identificador del usuario que realiza la transacci&oacute;n.
     * @param stopOnError Indica si se debe detener el proceso de firma al fallar una de las firmas.
     * @return Objeto con la URL de redirecci&oacute;n para la firma del lote.
     * @throws IOException Cuando no se puede conectar con el servicio.
     * @throws HttpForbiddenException Cuando no se tiene acceso al servicio remoto.
     * @throws HttpNetworkException Cuando ocurre un error de red.
     * @throws HttpOperationException Cuando ocurre un error durante la ejecuci&oacute;n.
     * @throws InvalidTransactionException
     * 			   Cuando la transacci&oacute;n no existe o est&aacute; caducada.
     */
    public SignOperationResult signBatch(final String transactionId, final String subjectId, final boolean stopOnError)
    		throws IOException, HttpForbiddenException, HttpNetworkException, HttpOperationException,
    			InvalidTransactionException {

        if (transactionId == null) {
            throw new InvalidTransactionException(
                    "El id de la transaccion no puede ser nulo" //$NON-NLS-1$
            );
        }
        if (subjectId == null || "".equals(subjectId)) { //$NON-NLS-1$
            throw new IllegalArgumentException(
                    "El identificador del titular no puede ser nulo" //$NON-NLS-1$
            );
        }

        final String urlParameters =
        		URL_PARAMETERS_BASE
        		.replace(TAG_VALUE_APP_ID, this.appId)
        		.replace(TAG_VALUE_SUBJECT_ID, subjectId)
        		.replace(TAG_VALUE_OPERATION, FIReServiceOperation.SIGN_BATCH.getId()) +
        		URL_PARAMETERS_SIGN_BATCH
                .replace(TAG_VALUE_TRANSACTION, transactionId)
        		.replace(TAG_VALUE_STOP_ON_ERROR, Boolean.toString(stopOnError));

        final byte[] responseJSON;
        try {
        	responseJSON = this.conn.readUrl(this.serviceUrl, urlParameters, Method.GET);
        } catch (final HttpError e) {
        	if (e.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN) {
        		throw new HttpForbiddenException(e);
        	} else if (e.getResponseCode() == HttpURLConnection.HTTP_CLIENT_TIMEOUT) {
        		throw new HttpNetworkException(e);
        	} else if (e.getResponseCode() == HttpCustomErrors.INVALID_TRANSACTION.getErrorCode()) {
        		throw new InvalidTransactionException(HttpCustomErrors.INVALID_TRANSACTION.getErrorDescription(), e);
        	} else if (e.getResponseCode() == HttpCustomErrors.BATCH_NO_DOCUMENT.getErrorCode()) {
        		throw new HttpOperationException(HttpCustomErrors.BATCH_NO_DOCUMENT.getErrorDescription(), e);
        	} else {
        		throw new HttpOperationException(e.getResponseDescription(), e);
        	}
        }
        catch (final Exception e) {
        	LOGGER.log(
        			Level.SEVERE,
        			"Error en la comunicacion con el servicio de recuperacion de firma", //$NON-NLS-1$
        			e);
        	throw new IOException(e);
        }

        return SignOperationResult.parse(responseJSON);
    }

    /**
     * Recupera el resultado de una operaci&oacute;n de firma de un lote de documentos.
     * @param transactionId Identificador de la transacci&oacute;n del lote.
     * @param subjectId Identificador del usuario que realiza la transacci&oacute;n.
     * @return Objeto con el XML resultado de la firma del lote. Se indica c&oacute;mo
     * termin&oacute; la firma de cada documento del lote, pero no se incluye la propia firma.
     * @throws IOException Cuando no se puede conectar con el servicio o la respuesta
     * no est&aacute; bien formada.
     * @throws HttpForbiddenException Cuando no se tiene acceso al servicio remoto.
     * @throws HttpNetworkException Cuando ocurre un error de red.
     * @throws HttpOperationException Cuando ocurre un error durante la ejecuci&oacute;n.
     * @throws InvalidTransactionException
     * 			   Cuando la transacci&oacute;n no existe o est&aacute; caducada.
     */
    public BatchResult recoverBatchResult(final String transactionId, final String subjectId)
    		throws IOException, HttpForbiddenException, HttpNetworkException, HttpOperationException,
    		InvalidTransactionException {

    	if (transactionId == null) {
    		throw new InvalidTransactionException(
    				"El id de la transaccion no puede ser nulo" //$NON-NLS-1$
    				);
    	}
        if (subjectId == null || "".equals(subjectId)) { //$NON-NLS-1$
            throw new IllegalArgumentException(
                    "El identificador del titular no puede ser nulo" //$NON-NLS-1$
            );
        }

    	final String urlParameters =
    			URL_PARAMETERS_BASE
    			.replace(TAG_VALUE_APP_ID, this.appId)
    			.replace(TAG_VALUE_SUBJECT_ID, subjectId)
    			.replace(TAG_VALUE_OPERATION, FIReServiceOperation.RECOVER_BATCH.getId()) +
    			URL_PARAMETERS_RECOVER_BATCH
    			.replace(TAG_VALUE_TRANSACTION, transactionId);

    	byte[] batchResult = null;
    	try {
    		batchResult = this.conn.readUrl(this.serviceUrl, urlParameters, Method.GET);
    	} catch (final HttpError e) {
    		LOGGER.severe("Error en la llamada al servicio de recuperacion del resultado de firma de lote: " + e.getResponseDescription()); //$NON-NLS-1$
        	if (e.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN) {
        		throw new HttpForbiddenException(e);
        	} else if (e.getResponseCode() == HttpURLConnection.HTTP_CLIENT_TIMEOUT) {
        		throw new HttpNetworkException(e);
        	} else if (e.getResponseCode() == HttpCustomErrors.INVALID_TRANSACTION.getErrorCode()) {
        		throw new InvalidTransactionException(HttpCustomErrors.INVALID_TRANSACTION.getErrorDescription(), e);
        	} else if (e.getResponseCode() == HttpCustomErrors.SIGN_ERROR.getErrorCode()) {
        		throw new HttpOperationException(HttpCustomErrors.SIGN_ERROR.getErrorDescription(), e);
        	} else {
        		throw new HttpOperationException(e.getResponseDescription(), e);
        	}
    	}
    	catch (final Exception e) {
    		LOGGER.log(
    				Level.SEVERE,
    				"Error en la comunicacion con el servicio de recuperacion de firma", //$NON-NLS-1$
    				e);
    		throw new IOException("Error en la comunicacion con el servicio de recuperacion de firma", e); //$NON-NLS-1$
    	}

    	try {
    		return BatchResult.parse(batchResult);
    	}
    	catch (final Exception e) {
    		LOGGER.severe("La respuesta de la firma del lote no esta bien formada. Inicio del resultado: " + //$NON-NLS-1$
    						new String(batchResult, 0, batchResult.length < 50 ? batchResult.length : 50) +
    						"\n" + e); //$NON-NLS-1$
    		throw new IOException("La respuesta de la firma del lote no esta bien formada", e); //$NON-NLS-1$
    	}
    }

    /**
     * Recupera el porcentaje actual de progreso de la firma de un lote de firma.
     * @param transactionId Identificador de la transacci&oacute;n del lote.
     * @param subjectId Identificador del usuario que realiza la transacci&oacute;n.
     * @return Valor decimal con el avance de progreso del lote en donde cero es
     * sin empezar y uno es terminado. Por ejemplo, si se obtiene el valor �0�3�
     * es que ha procesado ya el 30% del lote.
     * @throws IOException Cuando no se puede conectar con el servicio.
     * @throws HttpForbiddenException Cuando no se tiene acceso al servicio remoto.
     * @throws HttpNetworkException Cuando ocurre un error de red.
     * @throws HttpOperationException Cuando ocurre un error durante la ejecuci&oacute;n.
     * @throws InvalidTransactionException Cuando la transacci&oacute;n no existe o
     * est&aacute; caducada.
     */
    public float recoverBatchResultState(final String transactionId, final String subjectId)
    		throws IOException, HttpForbiddenException, HttpNetworkException, HttpOperationException,
    		InvalidTransactionException {

    	if (transactionId == null) {
    		throw new InvalidTransactionException(
    				"El id de la transaccion no puede ser nulo" //$NON-NLS-1$
    				);
    	}
    	if (subjectId == null) {
    		throw new InvalidTransactionException(
    				"El id de usuario no puede ser nulo" //$NON-NLS-1$
    				);
    	}

    	final String urlParameters =
    			URL_PARAMETERS_BASE
    			.replace(TAG_VALUE_APP_ID, this.appId)
    			.replace(TAG_VALUE_SUBJECT_ID, subjectId)
    			.replace(TAG_VALUE_OPERATION, FIReServiceOperation.RECOVER_BATCH_STATE.getId()) +
    			URL_PARAMETERS_RECOVER_BATCH_STATE
    			.replace(TAG_VALUE_TRANSACTION, transactionId);

    	try {
    		return Float.parseFloat(new String(this.conn.readUrl(this.serviceUrl, urlParameters, Method.GET)));
    	} catch (final HttpError e) {
    		LOGGER.severe("Error en la consulta al servicio de recuperacion del progreso de un lote de firma: " + e.getResponseDescription()); //$NON-NLS-1$
        	if (e.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN) {
        		throw new HttpForbiddenException(e);
        	} else if (e.getResponseCode() == HttpURLConnection.HTTP_CLIENT_TIMEOUT) {
        		throw new HttpNetworkException(e);
        	} else if (e.getResponseCode() == HttpCustomErrors.INVALID_TRANSACTION.getErrorCode()) {
        		throw new InvalidTransactionException(HttpCustomErrors.INVALID_TRANSACTION.getErrorDescription(), e);
        	} else {
        		throw new HttpOperationException(e.getResponseDescription(), e);
        	}
    	}
    	catch (final Exception e) {
    		LOGGER.log(
    				Level.SEVERE,
    				"Error en la comunicacion con el servicio de recuperacion del estado de un lote de firma", //$NON-NLS-1$
    				e);
    		throw new IOException(e);
    	}
    }

    /**
     * Recupera una firma obtenida como parte del proceso de firma de lote.
     * @param transactionId Identificador de la transacci&oacute;n del lote.
     * @param subjectId Identificador del usuario que realiza la transacci&oacute;n.
     * @param docId Identificador del documento de cuya firma queremos recuperar.
     * @return Objeto con la firma del documento.
     * @throws IOException Cuando no se puede conectar con el servicio.
     * @throws HttpForbiddenException Cuando no se tiene acceso al servicio remoto.
     * @throws HttpNetworkException Cuando ocurre un error de red.
     * @throws HttpOperationException Cuando ocurre un error durante la ejecuci&oacute;n.
     * @throws InvalidTransactionException Cuando la transacci&oacute;n no existe o
     * est&aacute; caducada.
     * @throws InvalidBatchDocumentException Cuando se indica el identificador de un documento
     * que no existe en el lote o que no se firm&oacute; correctamente.
     * @throws BatchNoSignedException Cuando se solicita recuperar una firma del lote antes de
     * firmarlo.
     */
    public TransactionResult recoverBatchSign(final String transactionId, final String subjectId, final String docId)
    		throws IOException, HttpForbiddenException, HttpNetworkException, HttpOperationException,
    		InvalidTransactionException, InvalidBatchDocumentException, BatchNoSignedException {

    	if (transactionId == null) {
    		throw new InvalidTransactionException(
    				"El id de la transaccion no puede ser nulo" //$NON-NLS-1$
    				);
    	}
        if (subjectId == null || "".equals(subjectId)) { //$NON-NLS-1$
            throw new IllegalArgumentException(
                    "El identificador del titular no puede ser nulo" //$NON-NLS-1$
            );
        }

    	final String urlParameters =
    			URL_PARAMETERS_BASE
    			.replace(TAG_VALUE_APP_ID, this.appId)
    			.replace(TAG_VALUE_SUBJECT_ID, subjectId)
    			.replace(TAG_VALUE_OPERATION, FIReServiceOperation.RECOVER_BATCH_SIGN.getId()) +
    			URL_PARAMETERS_RECOVER_SIGN_BATCH
    			.replace(TAG_VALUE_TRANSACTION, transactionId)
    			.replace(TAG_VALUE_DOCUMENT_ID, docId);

    	try {
    		return TransactionResult.parse(TransactionResult.RESULT_TYPE_BATCH_SIGN, this.conn.readUrl(this.serviceUrl, urlParameters, Method.GET));
    	} catch (final HttpError e) {
    		LOGGER.severe("Error en la llamada al servicio de recuperacion de firma de lote: " + e.getResponseDescription()); //$NON-NLS-1$
        	if (e.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN) {
        		throw new HttpForbiddenException(e);
        	} else if (e.getResponseCode() == HttpURLConnection.HTTP_CLIENT_TIMEOUT) {
        		throw new HttpNetworkException(e);
        	} else if (e.getResponseCode() == HttpCustomErrors.INVALID_TRANSACTION.getErrorCode()) {
        		throw new InvalidTransactionException(HttpCustomErrors.INVALID_TRANSACTION.getErrorDescription(), e);
        	} else if (e.getResponseCode() == HttpCustomErrors.BATCH_NO_SIGNED.getErrorCode()) {
        		throw new BatchNoSignedException(HttpCustomErrors.BATCH_NO_SIGNED.getErrorDescription(), e);
        	} else if (e.getResponseCode() == HttpCustomErrors.BATCH_DOCUMENT_FAILED.getErrorCode()) {
        		throw new InvalidBatchDocumentException(HttpCustomErrors.BATCH_DOCUMENT_FAILED.getErrorDescription(), e);
        	} else if (e.getResponseCode() == HttpCustomErrors.INVALID_BATCH_DOCUMENT.getErrorCode()) {
        		throw new InvalidBatchDocumentException(HttpCustomErrors.INVALID_BATCH_DOCUMENT.getErrorDescription(), e);
        	} else {
        		throw new HttpOperationException(e.getResponseDescription(), e);
        	}
    	}
    	catch (final Exception e) {
    		LOGGER.log(
    				Level.SEVERE,
    				"Error en la comunicacion con el servicio de recuperacion de firma de lote", //$NON-NLS-1$
    				e);
    		throw new IOException(e);
    	}
    }

    /**
     * Recupera una el resultado de error asociado a una transacci&oacute;n.
     * @param transactionId Identificador de la transacci&oacute;n del lote.
     * @param subjectId Identificador del usuario que realiza la transacci&oacute;n.
     * @return Resultado de la transacci&oacute;n con el error recibido.
     * @throws IOException Cuando no se puede conectar con el servicio.
     * @throws HttpForbiddenException Cuando no se tiene acceso al servicio remoto.
     * @throws HttpNetworkException Cuando ocurre un error de red.
     * @throws HttpOperationException Cuando ocurre un error durante la ejecuci&oacute;n.
     * @throws InvalidTransactionException Cuando la transacci&oacute;n no existe o
     * est&aacute; caducada.
     */
    public TransactionResult recoverErrorResult(final String transactionId, final String subjectId)
    		throws IOException, HttpForbiddenException, HttpNetworkException, HttpOperationException,
    		InvalidTransactionException {

    	if (transactionId == null) {
    		throw new InvalidTransactionException(
    				"El id de la transaccion no puede ser nulo" //$NON-NLS-1$
    				);
    	}
        if (subjectId == null || "".equals(subjectId)) { //$NON-NLS-1$
            throw new IllegalArgumentException(
                    "El identificador del titular no puede ser nulo" //$NON-NLS-1$
            );
        }

    	final String urlParameters =
    			URL_PARAMETERS_BASE
    			.replace(TAG_VALUE_APP_ID, this.appId)
    			.replace(TAG_VALUE_SUBJECT_ID, subjectId)
    			.replace(TAG_VALUE_OPERATION, FIReServiceOperation.RECOVER_ERROR.getId()) +
    			URL_PARAMETERS_RECOVER_ERROR
    			.replace(TAG_VALUE_TRANSACTION, transactionId);

    	try {
    		return TransactionResult.parse(TransactionResult.RESULT_TYPE_ERROR, this.conn.readUrl(this.serviceUrl, urlParameters, Method.GET));
    	} catch (final HttpError e) {
    		LOGGER.severe("Error en la llamada al servicio de recuperacion de error: " + e.getResponseDescription()); //$NON-NLS-1$
    		if (e.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN) {
        		throw new HttpForbiddenException(e);
        	} else if (e.getResponseCode() == HttpURLConnection.HTTP_CLIENT_TIMEOUT) {
        		throw new HttpNetworkException(e);
        	} else if (e.getResponseCode() == HttpCustomErrors.INVALID_TRANSACTION.getErrorCode()) {
        		throw new InvalidTransactionException(HttpCustomErrors.INVALID_TRANSACTION.getErrorDescription(), e);
        	} else {
        		throw new HttpOperationException(e.getResponseDescription(), e);
        	}
    	}
    	catch (final Exception e) {
    		LOGGER.log(
    				Level.SEVERE,
    				"Error en la comunicacion con el servicio de recuperacion de error", //$NON-NLS-1$
    				e);
    		throw new IOException(e);
    	}
    }

    /**
     * Transforma un Base64 normal en un Base64 URL SAFE.
     * @param base64 Cadena Base64.
     * @return Cadena de texto Base64 URL Safe o cadena vac&iacute;a
     * si la entrada era {@code null}.
     */
    private static String doBase64UrlSafe(final String base64) {
    	if (base64 == null) {
    		return ""; //$NON-NLS-1$
    	}
        return base64.replace('+', '-').replace('/', '_');
    }
}
