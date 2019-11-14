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

	/** Par&aacute;metro usado en el env&iacute;o de datos HTTP con el identificador de operacion */
	public static final String HTTP_PARAM_OPERATION = "op"; //$NON-NLS-1$
	/** Par&aacute;metro usado en el env&iacute;o de datos HTTP con el ID de la aplicaci&oacute;n. */
	public static final String HTTP_PARAM_APPLICATION_ID = "appid"; //$NON-NLS-1$
	/** Par&aacute;metro usado en el env&iacute;o de datos HTTP con el ID del usuario. */
	public static final String HTTP_PARAM_SUBJECT_ID = "subjectid"; //$NON-NLS-1$
	/** Par&aacute;metro usado en el env&iacute;o de datos HTTP con el ID de la transacci&oacute;n. */
    public static final String HTTP_PARAM_TRANSACTION_ID = "transactionid"; //$NON-NLS-1$
    /** Par&aacute;metro usado en el env&iacute;o de datos HTTP con el ID del documento. */
    public static final String HTTP_PARAM_DOCUMENT_ID = "docid"; //$NON-NLS-1$
    /** Par&aacute;metro usado en el env&iacute;o de datos HTTP con los datos. */
    public static final String HTTP_PARAM_DATA = "dat"; //$NON-NLS-1$
    /** Par&aacute;metro usado en el env&iacute;o de datos HTTP con el certificado de firma. */
    public static final String HTTP_PARAM_CERT = "cert"; //$NON-NLS-1$
    /** Par&aacute;metro usado en el env&iacute;o de datos HTTP con el origen del certificado. */
    public static final String HTTP_PARAM_CERT_ORIGIN = "certorigin"; //$NON-NLS-1$
    /** Par&aacute;metro usado en el env&iacute;o de datos HTTP que indica si se configur&oacute; el origen del certificado desde la aplicaci&oacute;n cliente. */
    public static final String HTTP_PARAM_CERT_ORIGIN_FORCED = "origforced"; //$NON-NLS-1$
    /** Par&aacute;metro usado en el env&iacute;o de datos HTTP indicando si se ha comprobado
     * que el usuario no esta registrado en el servicio de cuestodia en la nube. */
    public static final String HTTP_PARAM_USER_NOT_REGISTERED = "reg"; //$NON-NLS-1$
    /** Par&aacute;metro usado en el env&iacute;o de datos HTTP con la operacion de firma (firma, cofirma,...) */
    public static final String HTTP_PARAM_CRYPTO_OPERATION = "cop"; //$NON-NLS-1$
    /** Par&aacute;metro usado en el env&iacute;o de datos HTTP con el algoritmo de firma. */
    public static final String HTTP_PARAM_ALGORITHM = "algorithm"; //$NON-NLS-1$
    /** Par&aacute;metro usado en el env&iacute;o de datos HTTP con el formato de firma. */
    public static final String HTTP_PARAM_FORMAT = "format"; //$NON-NLS-1$
    /** Par&aacute;metro usado en el env&iacute;o de datos HTTP con la configuracion adicional de la firma. */
    public static final String HTTP_PARAM_EXTRA_PARAM = "properties"; //$NON-NLS-1$
    /** Par&aacute;metro usado en el env&iacute;o de datos HTTP con el documento de propiedades para la redirecci&oacute;n del usuario. */
    public static final String HTTP_PARAM_CONFIG = "config"; //$NON-NLS-1$
    /** Par&aacute;metro usado en el env&iacute;o de datos HTTP con el formato al que actualizar la firma. */
    public static final String HTTP_PARAM_UPGRADE = "upgrade"; //$NON-NLS-1$
    /** Par&aacute;metro usado en el env&iacute;o de datos HTTP para indicar si se pertite una actualizacion parcial de la firma. */
    public static final String HTTP_PARAM_ALLOW_PARTIAL_UPGRADE = "partialupgrade"; //$NON-NLS-1$
    /** Par&aacute;metro usado en el env&iacute;o de datos HTTP que indica que se detenga la firma batch en caso de error. */
    public static final String HTTP_PARAM_BATCH_STOP_ON_ERROR = "stoponerror"; //$NON-NLS-1$
    /** Par&aacute;metro usado en el env&iacute;o de datos HTTP con el resultado de la firma de lote con el Cliente @firma. */
    public static final String HTTP_PARAM_AFIRMA_BATCH_RESULT = "afirmabatchresult"; //$NON-NLS-1$
    /** Par&aacute;metro usado en el env&iacute;o de datos HTTP para indicar si la operacion es una firma de lote o no. */
    public static final String HTTP_PARAM_IS_BATCH_OPERATION = "isbatchop"; //$NON-NLS-1$
    /** Par&aacute;metro usado en el env&iacute;o de datos HTTP para indicar que se debe restringir el uso de la firma con certificado local. */
    public static final String HTTP_PARAM_LOCAL_RESTRINGED = "locrest"; //$NON-NLS-1$
    /** Par&aacute;metro usado en el env&iacute;o de datos HTTP con la URL a la que redirigir en caso de error. */
    public static final String HTTP_PARAM_ERROR_URL = "errorurl"; //$NON-NLS-1$
    /** Par&aacute;metro usado en el env&iacute;o de datos HTTP con el tipo de error obtenido. */
    public static final String HTTP_PARAM_ERROR_TYPE = "errortype"; //$NON-NLS-1$
    /** Par&aacute;metro usado en el env&iacute;o de datos HTTP con el mensaje de error obtenido. */
    public static final String HTTP_PARAM_ERROR_MESSAGE = "errormsg"; //$NON-NLS-1$


    /** Par&aacute;metro usado para el guardado de datos en sesi&oacute;n con el ID de operaci&oacute;n. */
    public static final String SESSION_PARAM_OPERATION = "op"; //$NON-NLS-1$
    /** Par&aacute;metro usado para el guardado de datos en sesi&oacute;n con el ID de aplicaci&oacute;n. */
    public static final String SESSION_PARAM_APPLICATION_ID = "appid"; //$NON-NLS-1$
    /** Par&aacute;metro usado para el guardado de datos en sesi&oacute;n con el nombre de aplicaci&oacute;n. */
    public static final String SESSION_PARAM_APPLICATION_NAME = "appidname"; //$NON-NLS-1$
    /** Par&aacute;metro usado para el guardado de datos en sesi&oacute;n con el t&iacute;tulo asignado a la aplicaci&oacute;n. */
    public static final String SESSION_PARAM_APPLICATION_TITLE = "appname"; //$NON-NLS-1$
    /** Par&aacute;metro usado para el guardado de datos en sesi&oacute;n con el ID de usuario. */
    public static final String SESSION_PARAM_SUBJECT_ID = "subjectid"; //$NON-NLS-1$
    /** Par&aacute;metro usado para el guardado de datos en sesi&oacute;n con el ID de transacci&oacute;n. */
    public static final String SESSION_PARAM_TRANSACTION_ID = "transactionid"; //$NON-NLS-1$
    /** Par&aacute;metro usado para el guardado de datos en sesi&oacute;n con el ID del tipo de operaci&oacute;n. */
    public static final String SESSION_PARAM_TRANSACTION_TYPE = "transactiontype"; //$NON-NLS-1$
    /** Par&aacute;metro usado para el guardado de datos en sesi&oacute;n con el certificado de firma en Base64. */
    public static final String SESSION_PARAM_CERT = "cert"; //$NON-NLS-1$
    /** Par&aacute;metro usado para el guardado de datos en sesi&oacute;n con el listado de proveedores disponibles para el usuario. */
    public static final String SESSION_PARAM_PROVIDERS = "providers"; //$NON-NLS-1$
    /** Par&aacute;metro usado para el guardado de datos en sesi&oacute;n con el origen del certificado de firma. */
    public static final String SESSION_PARAM_CERT_ORIGIN = "certorigin"; //$NON-NLS-1$
    /** Par&aacute;metro usado para el guardado de datos en sesi&oacute;n que indica si el origen del certificado se forz&oacute; desde la aplicaci&oacute;n. */
    public static final String SESSION_PARAM_CERT_ORIGIN_FORCED = "origforced"; //$NON-NLS-1$
    /** Par&aacute;metro usado para el guardado de datos en sesi&oacute;n con la operacion de firma (firma, cofirma,...) */
    public static final String SESSION_PARAM_CRYPTO_OPERATION = "cop"; //$NON-NLS-1$
    /** Par&aacute;metro usado para el guardado de datos en sesi&oacute;n con el algoritmo de firma. */
    public static final String SESSION_PARAM_ALGORITHM = "algorithm"; //$NON-NLS-1$
    /** Par&aacute;metro usado para el guardado de datos en sesi&oacute;n con el formato de firma. */
    public static final String SESSION_PARAM_FORMAT = "format"; //$NON-NLS-1$
    /** Par&aacute;metro usado para el guardado de datos en sesi&oacute;n con las propiedades para la configuraci&oacute;n de la firma de base 64. */
    public static final String SESSION_PARAM_EXTRA_PARAM = "properties"; //$NON-NLS-1$
    /** Par&aacute;metro usado para el guardado de datos en sesi&oacute;n con el listado de filtros para la firma con certificado local. */
    public static final String SESSION_PARAM_FILTERS = "filters"; //$NON-NLS-1$
    /** Par&aacute;metro usado para el guardado de datos en sesi&oacute;n con el formato avanzado de actualizaci&oacute;n de la firma. */
    public static final String SESSION_PARAM_UPGRADE = "upgrade"; //$NON-NLS-1$
    /** Par&aacute;metro usado para el guardado de datos en sesi&oacute;n con la configuracion adicional para la mejora de la firma. */
    public static final String SESSION_PARAM_UPGRADE_CONFIG = "upgradeconfig"; //$NON-NLS-1$


    /** Par&aacute;metro usado para el guardado de datos en sesi&oacute;n con el formato de firma configurado */
    public static final String SESSION_PARAM_FORMAT_CONFIG = "format_config"; //$NON-NLS-1$
    /** Par&aacute;metro usado para el guardado de datos en sesi&oacute;n con el documento de propiedades para la redirecci&oacute;n del usuario. */
    public static final String SESSION_PARAM_CONNECTION_CONFIG = "config"; //$NON-NLS-1$
    /** Par&aacute;metro usado para el guardado de datos en sesi&oacute;n con el */
    public static final String SESSION_PARAM_TRIPHASE_DATA = "tri"; //$NON-NLS-1$
    /** Par&aacute;metro usado para el guardado de datos en sesi&oacute;n con el */
    public static final String SESSION_PARAM_REMOTE_TRANSACTION_ID = "remoteid"; //$NON-NLS-1$
    /** Par&aacute;metro usado para el guardado de datos en sesi&oacute;n que indica que se detenga la firma batch en caso de error. */
    public static final String SESSION_PARAM_BATCH_STOP_ON_ERROR = "stoponerror"; //$NON-NLS-1$
    /** Par&aacute;metro usado para el guardado del estado parcial de las firmas en la firma de lote. */
    public static final String SESSION_PARAM_BATCH_RESULT = "batchresult"; //$NON-NLS-1$
    /** Par&aacute;metro usado para el guardado de datos en sesi&oacute;n que indica que ya se ha firmado el lote actual. */
    public static final String SESSION_PARAM_BATCH_SIGNED = "batchsigned"; //$NON-NLS-1$
    /** Par&aacute;metro usado para el guardado del numero de firmas pendientes de un lote. */
    public static final String SESSION_PARAM_BATCH_PENDING_SIGNS = "batchpending"; //$NON-NLS-1$
    /** Par&aacute;metro usado para el guardado de datos en sesi&oacute;n con el ID de transacci&oacute;n del servicio de backend. */
    public static final String SESSION_PARAM_GENERATE_TRANSACTION_ID = "generatetrid"; //$NON-NLS-1$
    /** Par&aacute;metro usado para el guardado del momento en milisegundos en el que caduca una sesi&oacute;n. */
    public static final String SESSION_PARAM_TIMEOUT = "timeout"; //$NON-NLS-1$
    /** Par&aacute;metro usado para el guardado del identificador del documento. */
    public static final String SESSION_PARAM_DOC_ID = "docid"; //$NON-NLS-1$
    /** Par&aacute;metro usado para el guardado del gestor de documentos. */
    public static final String SESSION_PARAM_DOCUMENT_MANAGER = "docmanager"; //$NON-NLS-1$
    /** Par&aacute;metro usado para el guardado del valor bandera que indica si se redirigi&oacute; a la pasarela de autorizaci&oacute;n. */
    public static final String SESSION_PARAM_REDIRECTED = "gatewayredirected"; //$NON-NLS-1$
    /** Par&aacute;metro usado para el guardado del valor que indica cu&aacute;l ha sido la anterior operaci&oacute;n realizada. */
    public static final String SESSION_PARAM_PREVIOUS_OPERATION = "prevop"; //$NON-NLS-1$
    /** Par&aacute;metro usado para el guardado de datos en sesi&oacute;n con el tipo de error obtenido. */
    public static final String SESSION_PARAM_ERROR_TYPE = "errortype"; //$NON-NLS-1$
    /** Par&aacute;metro usado para el guardado de datos en sesi&oacute;n con el mensaje de error obtenido. */
	public static final String SESSION_PARAM_ERROR_MESSAGE = "errormsg"; //$NON-NLS-1$

	 /** Par&aacute;metro usado para el guardado de datos en sesi&oacute;n con el navegador usado del cliente. */
	public static final String SESSION_PARAM_BROWSER = "browser"; //$NON-NLS-1$

	 /** Par&aacute;metro usado para el guardado del tama&ntilde;o de los datos a firmar en sesi&oacute;n */
	public static final String SESSION_PARAM_DOCSIZE = "docsize"; //$NON-NLS-1$

    /** Valor del parametro de actualizacion que determina que la firma debe validarse. */
    public static final String UPGRADE_VERIFY = "verify"; //$NON-NLS-1$

    /** Identificador de operaci&oacute;n de firma de lotes. */
    public static final String OPERATION_BATCH = "batch"; //$NON-NLS-1$

    public static final String TRANSACTION_TYPE_SIGN = "1"; //$NON-NLS-1$
    public static final String TRANSACTION_TYPE_BATCH = "2"; //$NON-NLS-1$

}
