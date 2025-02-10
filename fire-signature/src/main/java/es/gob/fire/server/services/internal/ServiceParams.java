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

/**
 * Clase con los identificadores de los distintos parametros que se utilizan en la
 * ejecuci&oacute;n de las operaciones de Clave Firma.
 * @author Carlos Gamuci
 *
 */
public class ServiceParams {

	/** Par&aacute;metro para indicar en las peticiones HTTP el identificador de operaci&oacute;n */
	public static final String HTTP_PARAM_OPERATION = "op"; //$NON-NLS-1$
	/** Par&aacute;metro para indicar en las peticiones HTTP el ID de la aplicaci&oacute;n. */
	public static final String HTTP_PARAM_APPLICATION_ID = "appid"; //$NON-NLS-1$
	/** Par&aacute;metro para indicar en las peticiones HTTP el ID del usuario. */
	public static final String HTTP_PARAM_SUBJECT_ID = "subjectid"; //$NON-NLS-1$
	/** Par&aacute;metro para indicar en las peticiones HTTP el ID del usuario. */
	public static final String HTTP_PARAM_SUBJECT_REF = "subjectref"; //$NON-NLS-1$
	/** Par&aacute;metro para indicar en las peticiones HTTP el ID de la transacci&oacute;n. */
    public static final String HTTP_PARAM_TRANSACTION_ID = "transactionid"; //$NON-NLS-1$
    /** Par&aacute;metro para indicar en las peticiones HTTP el ID del documento. */
    public static final String HTTP_PARAM_DOCUMENT_ID = "docid"; //$NON-NLS-1$
    /** Par&aacute;metro para indicar en las peticiones HTTP los datos. */
    public static final String HTTP_PARAM_DATA = "dat"; //$NON-NLS-1$
    /** Par&aacute;metro para indicar en las peticiones HTTP el certificado de firma. */
    public static final String HTTP_PARAM_CERT = "cert"; //$NON-NLS-1$
    /** Par&aacute;metro para indicar en las peticiones HTTP el origen del certificado. */
    public static final String HTTP_PARAM_CERT_ORIGIN = "certorigin"; //$NON-NLS-1$
    /** Par&aacute;metro para indicar en las peticiones HTTP si se configur&oacute; el origen del certificado desde la aplicaci&oacute;n cliente. */
    public static final String HTTP_PARAM_CERT_ORIGIN_FORCED = "origforced"; //$NON-NLS-1$
    /** Par&aacute;metro para indicar en las peticiones HTTP la operacion de firma (firma, cofirma,...) */
    public static final String HTTP_PARAM_CRYPTO_OPERATION = "cop"; //$NON-NLS-1$
    /** Par&aacute;metro para indicar en las peticiones HTTP el algoritmo de firma. */
    public static final String HTTP_PARAM_ALGORITHM = "algorithm"; //$NON-NLS-1$
    /** Par&aacute;metro para indicar en las peticiones HTTP el formato de firma. */
    public static final String HTTP_PARAM_FORMAT = "format"; //$NON-NLS-1$
    /** Par&aacute;metro para indicar en las peticiones HTTP la configuraci&oacute;n adicional de la firma. */
    public static final String HTTP_PARAM_EXTRA_PARAM = "properties"; //$NON-NLS-1$
    /** Par&aacute;metro para indicar en las peticiones HTTP el documento de propiedades para la redirecci&oacute;n del usuario. */
    public static final String HTTP_PARAM_CONFIG = "config"; //$NON-NLS-1$
    /** Par&aacute;metro para indicar en las peticiones HTTP el formato al que actualizar la firma. */
    public static final String HTTP_PARAM_UPGRADE = "upgrade"; //$NON-NLS-1$
    /** Par&aacute;metro para indicar en las peticiones HTTP si se permite una actualizaci&oacute;n parcial de la firma. */
    public static final String HTTP_PARAM_ALLOW_PARTIAL_UPGRADE = "partialupgrade"; //$NON-NLS-1$
    /** Par&aacute;metro para indicar en las peticiones HTTP que se detenga la firma batch en caso de error. */
    public static final String HTTP_PARAM_BATCH_STOP_ON_ERROR = "stoponerror"; //$NON-NLS-1$
    /** Par&aacute;metro para indicar en las peticiones HTTP el resultado de la firma de lote con el Cliente @firma. */
    public static final String HTTP_PARAM_AFIRMA_BATCH_RESULT = "afirmabatchresult"; //$NON-NLS-1$
    /** Par&aacute;metro para indicar en las peticiones HTTP indicar si la operacion es una firma de lote o no. */
    public static final String HTTP_PARAM_IS_BATCH_OPERATION = "isbatchop"; //$NON-NLS-1$
    /** Par&aacute;metro para indicar en las peticiones HTTP que se debe restringir el uso de la firma con certificado local. */
    public static final String HTTP_PARAM_LOCAL_RESTRINGED = "locrest"; //$NON-NLS-1$
    /** Par&aacute;metro para indicar en las peticiones HTTP la URL a la que redirigir en caso de error. */
    public static final String HTTP_PARAM_ERROR_URL = "errorurl"; //$NON-NLS-1$
    /** Par&aacute;metro upara indicar en las peticiones HTTP el tipo de error obtenido. */
    public static final String HTTP_PARAM_ERROR_TYPE = "errortype"; //$NON-NLS-1$
    /** Par&aacute;metro para indicar en las peticiones HTTP el mensaje de error obtenido. */
    public static final String HTTP_PARAM_ERROR_MESSAGE = "errormsg"; //$NON-NLS-1$
    /** Par&aacute;metro para indicar en las peticiones HTTP un nombre de p&aacute;gina a la que redirigir. */
    public static final String HTTP_PARAM_PAGE = "page"; //$NON-NLS-1$
    /** Par&aacute;metro para indicar en las peticiones HTTP si es necesaria la autenticaci&oacute;n del usuario. */
    public static final String HTTP_PARAM_NEED_AUTH_USER = "auth"; //$NON-NLS-1$

    /** Atributo usado en el env&iacute;o de datos HTTP con el certificado de firma. */
    public static final String HTTP_ATTR_CERT = "cert"; //$NON-NLS-1$
    /** Atributo usado en el env&iacute;o de datos HTTP con la URL a la que redirigir en caso de error. */
    public static final String HTTP_ATTR_ERROR_URL = "errorurl"; //$NON-NLS-1$

    /** Par&aacute;metro para guardar en sesi&oacute;n el ID de aplicaci&oacute;n. */
    public static final String SESSION_PARAM_APPLICATION_ID = "appid"; //$NON-NLS-1$
    /** Par&aacute;metro para guardar en sesi&oacute;n el nombre de aplicaci&oacute;n. */
    public static final String SESSION_PARAM_APPLICATION_NAME = "appidname"; //$NON-NLS-1$
    /** Par&aacute;metro para guardar en sesi&oacute;n el t&iacute;tulo asignado a la aplicaci&oacute;n. */
    public static final String SESSION_PARAM_APPLICATION_TITLE = "appname"; //$NON-NLS-1$
    /** Par&aacute;metro para guardar en sesi&oacute;n el ID de usuario. */
    public static final String SESSION_PARAM_SUBJECT_ID = "subjectid"; //$NON-NLS-1$
    /** Par&aacute;metro para guardar en sesi&oacute;n la referencia del usuario (utilizada en lugar del ID en las URL). */
    public static final String SESSION_PARAM_SUBJECT_REF = "subjectref"; //$NON-NLS-1$
    /** Par&aacute;metro para guardar en sesi&oacute;n el ID de transacci&oacute;n. */
    public static final String SESSION_PARAM_TRANSACTION_ID = "transactionid"; //$NON-NLS-1$
    /** Par&aacute;metro para guardar en sesi&oacute;n el ID del tipo de operaci&oacute;n. */
    public static final String SESSION_PARAM_TRANSACTION_TYPE = "transactiontype"; //$NON-NLS-1$
    /** Par&aacute;metro para guardar en sesi&oacute;n el certificado de firma en Base64. */
    public static final String SESSION_PARAM_CERT = "cert"; //$NON-NLS-1$
    /** Par&aacute;metro para guardar en sesi&oacute;n si debe omitirse la selecci&oacute;n de certificado por parte del usuario cuando sea posible. */
    public static final String SESSION_PARAM_SKIP_CERT_SELECTION = "skipcert"; //$NON-NLS-1$
    /** Par&aacute;metro para guardar en sesi&oacute;n el listado de proveedores disponibles para el usuario. */
    public static final String SESSION_PARAM_PROVIDERS = "providers"; //$NON-NLS-1$
    /** Par&aacute;metro para guardar en sesi&oacute;n el origen del certificado de firma. */
    public static final String SESSION_PARAM_CERT_ORIGIN = "certorigin"; //$NON-NLS-1$
    /** Par&aacute;metro para guardar en sesi&oacute;n si el origen del certificado se forz&oacute; desde la aplicaci&oacute;n. */
    public static final String SESSION_PARAM_CERT_ORIGIN_FORCED = "origforced"; //$NON-NLS-1$
    /** Par&aacute;metro para guardar en sesi&oacute;n la operacion de firma (firma, cofirma,...) */
    public static final String SESSION_PARAM_CRYPTO_OPERATION = "cop"; //$NON-NLS-1$
    /** Par&aacute;metro para guardar en sesi&oacute;n el algoritmo de firma. */
    public static final String SESSION_PARAM_ALGORITHM = "algorithm"; //$NON-NLS-1$
    /** Par&aacute;metro para guardar en sesi&oacute;n el formato de firma. */
    public static final String SESSION_PARAM_FORMAT = "format"; //$NON-NLS-1$
    /** Par&aacute;metro para guardar en sesi&oacute;n las propiedades para la configuraci&oacute;n de la firma de base 64. */
    public static final String SESSION_PARAM_EXTRA_PARAM = "properties"; //$NON-NLS-1$
    /** Par&aacute;metro para guardar en sesi&oacute;n el listado de filtros para la firma con certificado local. */
    public static final String SESSION_PARAM_FILTERS = "filters"; //$NON-NLS-1$
    /** Par&aacute;metro para guardar en sesi&oacute;n con el formato avanzado de actualizaci&oacute;n de la firma. */
    public static final String SESSION_PARAM_UPGRADE = "upgrade"; //$NON-NLS-1$
    /** Par&aacute;metro para guardar en sesi&oacute;n la configuracion adicional para la mejora de la firma. */
    public static final String SESSION_PARAM_UPGRADE_CONFIG = "upgradeconfig"; //$NON-NLS-1$
    /** Par&aacute;metro para guardar en sesi&oacute;n el formato de firma configurado */
    public static final String SESSION_PARAM_FORMAT_CONFIG = "format_config"; //$NON-NLS-1$
    /** Par&aacute;metro para guardar en sesi&oacute;n el documento de propiedades para la redirecci&oacute;n del usuario. */
    public static final String SESSION_PARAM_CONNECTION_CONFIG = "config"; //$NON-NLS-1$
    /** Par&aacute;metro para guardar en sesi&oacute;n el valor parcial de firma trif&aacute;sica. */
    public static final String SESSION_PARAM_TRIPHASE_DATA = "tri"; //$NON-NLS-1$
    /** Par&aacute;metro para guardar en sesi&oacute;n el identificador de sesion remota. */
    public static final String SESSION_PARAM_REMOTE_TRANSACTION_ID = "remoteid"; //$NON-NLS-1$
    /** Par&aacute;metro para guardar en sesi&oacute;n que se detenga la firma batch en caso de error. */
    public static final String SESSION_PARAM_BATCH_STOP_ON_ERROR = "stoponerror"; //$NON-NLS-1$
    /** Par&aacute;metro para guardar en sesi&oacute;n el estado parcial de las firmas en la firma de lote. */
    public static final String SESSION_PARAM_BATCH_RESULT = "batchresult"; //$NON-NLS-1$
    /** Par&aacute;metro para guardar en sesi&oacute;n si ya se ha firmado el lote actual. */
    public static final String SESSION_PARAM_BATCH_SIGNED = "batchsigned"; //$NON-NLS-1$
    /** Par&aacute;metro para guardar en sesi&oacute;n si ya se ha recuperado el resultado del lote actual. */
    public static final String SESSION_PARAM_BATCH_RECOVERED = "batchrecovered"; //$NON-NLS-1$
    /** Par&aacute;metro para guardar en sesi&oacute;n el numero de firmas pendientes de un lote. */
    public static final String SESSION_PARAM_BATCH_PENDING_SIGNS = "batchpending"; //$NON-NLS-1$
    /** Par&aacute;metro para guardar en sesi&oacute;n el ID de transacci&oacute;n del servicio de backend. */
    public static final String SESSION_PARAM_GENERATE_TRANSACTION_ID = "generatetrid"; //$NON-NLS-1$
    /** Par&aacute;metro para guardar en sesi&oacute;n el momento en milisegundos en el que caduca una sesi&oacute;n. */
    public static final String SESSION_PARAM_TIMEOUT = "timeout"; //$NON-NLS-1$
    /** Par&aacute;metro para guardar en sesi&oacute;n el identificador del documento. */
    public static final String SESSION_PARAM_DOC_ID = "docid"; //$NON-NLS-1$
    /** Par&aacute;metro para guardar en sesi&oacute;n el gestor de documentos. */
    public static final String SESSION_PARAM_DOCUMENT_MANAGER = "docmanager"; //$NON-NLS-1$
    /** Par&aacute;metro para guardar en sesi&oacute;n el valor bandera que indica si se redirigi&oacute; a la pasarela de autorizaci&oacute;n para firmar. */
    public static final String SESSION_PARAM_REDIRECTED_SIGN = "gatewayredirectedtosign"; //$NON-NLS-1$
    /** Par&aacute;metro para guardar en sesi&oacute;n el valor bandera que indica si se redirigi&oacute; a la pasarela de autorizaci&oacute;n para autenticar al usuario. */
    public static final String SESSION_PARAM_REDIRECTED_LOGIN = "gatewayredirectedtologin"; //$NON-NLS-1$
    /** Par&aacute;metro para guardar en sesi&oacute;n el valor que indica cu&aacute;l ha sido la anterior operaci&oacute;n realizada. */
    public static final String SESSION_PARAM_PREVIOUS_OPERATION = "prevop"; //$NON-NLS-1$
    /** Par&aacute;metro para guardar en sesi&oacute;n con el tipo de error obtenido. */
    public static final String SESSION_PARAM_ERROR_TYPE = "errortype"; //$NON-NLS-1$
    /** Par&aacute;metro para guardar en sesi&oacute;n con el mensaje de error obtenido. */
	public static final String SESSION_PARAM_ERROR_MESSAGE = "errormsg"; //$NON-NLS-1$
	/** Par&aacute;metro para guardar en sesi&oacute;n con el navegador usado del cliente. */
	public static final String SESSION_PARAM_BROWSER = "browser"; //$NON-NLS-1$
	/** Par&aacute;metro para guardar en sesi&oacute;n el tama&ntilde;o de los datos a firmar en sesi&oacute;n */
	public static final String SESSION_PARAM_DOCSIZE = "docsize"; //$NON-NLS-1$
	/** Par&aacute;metro para guardar en sesi&oacute;n el tama&ntilde;o de los datos de la transacci&oacute;n en sesi&oacute;n */
	public static final String SESSION_PARAM_TRANSACTION_SIZE = "transactionsize"; //$NON-NLS-1$

    /** Valor del parametro de actualizacion que determina que la firma debe validarse. */
    public static final String UPGRADE_VERIFY = "verify"; //$NON-NLS-1$

    public static final String TRANSACTION_TYPE_SIGN = "1"; //$NON-NLS-1$
    public static final String TRANSACTION_TYPE_BATCH = "2"; //$NON-NLS-1$

}
