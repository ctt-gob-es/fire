﻿/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */

using System;
using System.Net;
using System.Security.Authentication;
using System.IO;
using System.Web.Script.Serialization;
using System.Collections.Generic;

namespace FIRe
{
    /// <summary> Cliente del servicio de firma electrónica.</summary>
    /// Permite firmar/multifirmar un único documento o sobre un lote completo.
    /// Esta clase puede recibir la configuración para la conexión con el componente central
    /// u obtener la URL del servicio de firma a través del registro
    public class FireClient
    {
        // Parametros que necesitamos de la URL.
        private static readonly string APP_ID = "%APPID%";
        private static readonly string SUBJECTID = "%SUBJECTID%";
        private static readonly string TRANSACTION = "%TRANSACTION%";
        private static readonly string COP = "%COP%";
        private static readonly string OP = "%OP%";
	    private static readonly string ALG = "%ALG%";
	    private static readonly string FORMAT = "%FT%";
	    private static readonly string DATA = "%DATA%";
	    private static readonly string CONF = "%CONF%";
        private static readonly string UPGRADE = "%UPGRADE%";
        private static readonly string PARTIAL = "%ALLOWPARTIAL%";
        private static readonly string PROP = "%PROP%";
        private static readonly string DOCID = "%DOCID%";
        private static readonly string STOPONERROR = "%STOPONERROR%";

        private static readonly string OP_CODE_SIGN = "1";
        private static readonly string OP_CODE_RECOVER_SIGN = "2";
        private static readonly string OP_CODE_CREATE_BATCH = "5";
        private static readonly string OP_CODE_ADD_DOCUMENT_TO_BATCH = "6";
        private static readonly string OP_CODE_SIGN_BATCH = "7";
        private static readonly string OP_CODE_RECOVER_BATCH = "8";
        private static readonly string OP_CODE_RECOVER_BATCH_STATE = "9";
        private static readonly string OP_CODE_RECOVER_BATCH_SIGN = "10";
        private static readonly string OP_CODE_RECOVER_SIGN_RESULT = "11";
        private static readonly string OP_CODE_RECOVER_ASYNC_SIGN = "70";
        private static readonly string OP_CODE_RECOVER_ASYNC_SIGN_RESULT = "71";
        private static readonly string OP_CODE_RECOVER_ERROR = "99";

        private static readonly string URL_PARAMETERS_SIGN =
            "&op=" + OP +
            "&appid=" + APP_ID +
            "&subjectid=" + SUBJECTID +
            "&cop=" + COP +
            "&algorithm=" + ALG +
            "&format=" + FORMAT +
            "&properties=" + PROP +
            "&dat=" + DATA +
            "&config=" + CONF;

        private static readonly string URL_PARAMETERS_RECOVER_SIGN =
            "appid=" + APP_ID +
            "&transactionid=" + TRANSACTION +
            "&subjectid=" + SUBJECTID +
            "&upgrade=" + UPGRADE +
            "&config=" + CONF +
            "&op=" + OP;

        private static readonly string URL_PARAMETERS_RECOVER_SIGN_RESULT =
            "appid=" + APP_ID +
            "&transactionid=" + TRANSACTION +
            "&subjectid=" + SUBJECTID +
            "&op=" + OP;

        private static readonly string URL_PARAMETERS_RECOVER_ERROR =
            "op=" + OP +
            "&appid=" + APP_ID +
            "&subjectid=" + SUBJECTID +
            "&transactionid=" + TRANSACTION;

        private static readonly string URL_PARAMETERS_CREATE_BATCH =
                    "appid=" + APP_ID +
                    "&subjectid=" + SUBJECTID +
                    "&cop=" + COP +
                    "&op=" + OP +
                    "&algorithm=" + ALG +
                    "&format=" + FORMAT +
                    "&properties=" + PROP +
                    "&upgrade=" + UPGRADE +
                    "&config=" + CONF;

        private static readonly string URL_PARAMETERS_ADD_DOCUMENT_BATCH =
            "op=" + OP +
            "&appid=" + APP_ID +
            "&subjectid=" + SUBJECTID +
            "&transactionid=" + TRANSACTION +
            "&docid=" + DOCID +
            "&dat=" + DATA +
            "&config=" + CONF;

        private static readonly string URL_PARAMETERS_ADD_CUSTOM_DOCUMENT_BATCH =
            "op=" + OP +
            "&appid=" + APP_ID +
            "&subjectid=" + SUBJECTID +
            "&transactionid=" + TRANSACTION +
            "&docid=" + DOCID +
            "&cop=" + COP +
            "&format=" + FORMAT +
            "&properties=" + PROP +
            "&upgrade=" + UPGRADE +
            "&dat=" + DATA +
            "&config=" + CONF;

        private static readonly string URL_PARAMETERS_SIGN_BATCH =
            "op=" + OP +
            "&appid=" + APP_ID +
            "&subjectid=" + SUBJECTID +
            "&transactionid=" + TRANSACTION +
            "&stoponerror=" + STOPONERROR;

        private static readonly string URL_PARAMETERS_RECOVER_BATCH_RESULT =
                   "op=" + OP +
                   "&appid=" + APP_ID +
                   "&subjectid=" + SUBJECTID +
                   "&transactionid=" + TRANSACTION +
                   "&op=" + OP;

        private static readonly string URL_PARAMETERS_RECOVER_BATCH_STATE =
            "op=" + OP +
            "&appid=" + APP_ID +
            "&subjectid=" + SUBJECTID +
            "&transactionid=" + TRANSACTION;

        private static readonly string URL_PARAMETERS_RECOVER_BATCH_SIGN =
            "op=" + OP +
            "&appid=" + APP_ID +
            "&subjectid=" + SUBJECTID +
            "&transactionid=" + TRANSACTION +
            "&docid=" + DOCID;

        private static readonly string URL_PARAMETERS_RECOVER_ASYNC_SIGN =
            "op=" + OP +
            "&appid=" + APP_ID +
            "&docid=" + DOCID +
            "&upgrade=" + UPGRADE +
            "&config=" + CONF +
            "&partial=" + PARTIAL;

        private static readonly string URL_PARAMETERS_RECOVER_ASYNC_SIGN_RESULT =
            "op=" + OP +
            "&appid=" + APP_ID +
            "&docid=" + DOCID;

        private readonly string appId;
        private readonly FireConfig config;

        /// <summary>
        /// Crea el cliente de FIRe indicandole el identificador de la aplicación actual.
        /// La configuracion necesaria para conectar con el componente central se tomará
        /// del registro.
        /// </summary>
        /// <param name="appId">Identificador de la aplicación.</param>
        /// <exception cref="ConfigureException">Cuando no se encuentra configurada la URL del servicio.</exception>
        public FireClient(string appId)
        {
            this.appId = appId ?? "";
            this.config = new FireConfig();
        }

        /// <summary>
        /// Crea el cliente de FIRe indicandole el identificador de la aplicación actual
        /// y la configuracion necesaria para conectar con el componente central. Los parametros
        /// no configurados a traves de este diccionario se tomarán del registro del sistema.
        /// </summary>
        /// <param name="appId">Identificador de la aplicación.</param>
        /// <param name="config">Configuración de la conexión con el componente central.</param>
        /// <exception cref="ConfigureException">Cuando no se encuentra configurada la URL del servicio.</exception>
        public FireClient(string appId, Dictionary<String, String> config)
        {
            this.appId = appId ?? "";
            this.config = new FireConfig(config);
        }

        /// <summary>
        /// Envía datos a firmar al servicio de firma.
        /// </summary>
        /// <param name="subjectId">Identificador del firmante.</param>
        /// <param name="op">Tipo de operaciónn a realizar: "sign", "cosign" o "countersign".</param>
        /// <param name="ft">Formato de la operación: "XAdES", "PAdES", etc.</param>
        /// <param name="algth">Algoritmo de firma.</param>
        /// <param name="propB64">Propiedades extra a añadir a la firma (puede ser <code>null</code>) codificadas en base 64.</param>
        /// <param name="dataB64"> Datos a firmar en base64.</param>
        /// <param name="confB64">Parámetros de la configuración de la firma codificados en base 64.</param>
        /// <returns>Firma realizada en servidor.</returns>
        /// <exception cref="ArgumentException">Cuando se proporciona nulo o vacío un parámetro obligatorio.</exception>
        /// <exception cref="HttpForbiddenException">Cuando falla la autenticación con el componente central.</exception>
        /// <exception cref="HttpNetworkException">Cuando se produce un error de conexión con el componente central.</exception>
        /// <exception cref="HttpOperationException">Cuando se produce un error interno del servidor.</exception>
        public FireLoadResult sign(
                              string subjectId,
                              string op,
			                  string ft,
			                  string algth,
			                  string propB64,
			                  string dataB64,
			                  string confB64) {
            
            if (string.IsNullOrEmpty(subjectId))
            {
			    throw new ArgumentException(
                    "El identificador del titular no puede ser nulo"
                );
		    }
            if (string.IsNullOrEmpty(op))
            {
                throw new ArgumentException(
				    "El tipo de operacion de firma a realizar no puede ser nulo"
			    );
		    }
            if (string.IsNullOrEmpty(ft))
            {
                throw new ArgumentException(
				    "El formato de firma no puede ser nulo"
			    );
		    }
            if (string.IsNullOrEmpty(algth))
            {
                throw new ArgumentException(
				    "El algoritmo de firma no puede ser nulo"
			    );
		    }
            if (string.IsNullOrEmpty(dataB64))
            {
                throw new ArgumentException(
				    "Los datos a firmar no pueden ser nulos"
			    );
		    }
            if (string.IsNullOrEmpty(confB64))
            {
                throw new ArgumentException(
				    "Los datos de configuracion no pueden ser nulos"
			    );
		    }
            
            string url = this.config.getFireService();
            
            string urlParameters = URL_PARAMETERS_SIGN
                .Replace(OP, OP_CODE_SIGN) // El tipo de operacion solicitada es SIGN
                .Replace(APP_ID, appId)
                .Replace(SUBJECTID, subjectId)
                .Replace(COP, op)
                .Replace(ALG, algth)
                .Replace(FORMAT, ft)
                .Replace(PROP, string.IsNullOrEmpty(propB64) ? "" : propB64.Replace('+', '-').Replace('/', '_'))
                .Replace(DATA, dataB64.Replace('+', '-').Replace('/', '_'))
                .Replace(CONF, confB64.Replace('+', '-').Replace('/', '_'));

            //  realizamos la peticion post al servicio y recibimos los datos de la peticion
            byte[] bytes = GetResponseToPostPetition(url, urlParameters, this.config.getConfig());

            // Mostramos los datos obtenidos
            return new FireLoadResult(System.Text.Encoding.UTF8.GetString(bytes));
        }

        /// <summary>
        /// Recupera la firma de los datos enviados a firmar anteriormente.
        /// </summary>
        /// <param name="transactionId">Identificador de la transacción.</param>
        /// <param name="subjectId">Identificador del usuario propietario de los certificados de firma.</param>
        /// <param name="upgrade">Formato al que queremos mejorar la firma (puede ser null).</param>
        /// <returns>Firma realizada en servidor.</returns>
        /// <exception cref="ArgumentException">Cuando se proporciona nulo o vacío un parámetro obligatorio.</exception>
        /// <exception cref="HttpForbiddenException">Cuando falla la autenticación con el componente central.</exception>
        /// <exception cref="HttpNetworkException">Cuando se produce un error de conexión con el componente central.</exception>
        /// <exception cref="InvalidTransactionException">Cuando se intenta operar sobre una transaccion inexistente o ya caducada.</exception>
        /// <exception cref="HttpOperationException">Cuando se produce un error generico durante la operación.</exception>
        public FireTransactionResult recoverSign(string transactionId, string subjectId, string upgrade)
        {
            return recoverSign(transactionId, subjectId, upgrade, null);
        }

        /// <summary>
        /// Recupera la firma de los datos enviados a firmar anteriormente.
        /// </summary>
        /// <param name="transactionId">Identificador de la transacción.</param>
        /// <param name="subjectId">Identificador del usuario propietario de los certificados de firma.</param>
        /// <param name="upgrade">Formato al que queremos mejorar la firma (puede ser null).</param>
        /// <param name="upgradeConfigB64">Configuración adicional para la plataforma de
        ///         actualización y validación en forma de properties codificado en base64.</param>
        /// <returns>Firma realizada en servidor.</returns>
        /// <exception cref="ArgumentException">Cuando se proporciona nulo o vacío un parámetro obligatorio.</exception>
        /// <exception cref="HttpForbiddenException">Cuando falla la autenticación con el componente central.</exception>
        /// <exception cref="HttpNetworkException">Cuando se produce un error de conexión con el componente central.</exception>
        /// <exception cref="InvalidTransactionException">Cuando se intenta operar sobre una transaccion inexistente o ya caducada.</exception>
        /// <exception cref="HttpOperationException">Cuando se produce un error generico durante la operación.</exception>
        public FireTransactionResult recoverSign(
                              string transactionId,
                              string subjectId,
                              string upgrade,
                              string upgradeConfigB64
                              )
        {

            if (string.IsNullOrEmpty(transactionId))
            {
                throw new ArgumentException(
                    "El id de la transaccion no puede ser nulo"
                );
            }
            if (string.IsNullOrEmpty(subjectId))
            {
                throw new ArgumentException(
                    "El identificador del titular no puede ser nulo"
                );
            }

            string url = this.config.getFireService();
            string urlParameters = URL_PARAMETERS_RECOVER_SIGN
                .Replace(APP_ID, this.appId)
                .Replace(TRANSACTION, transactionId)
                .Replace(SUBJECTID, subjectId)
                .Replace(OP, OP_CODE_RECOVER_SIGN); // El tipo de operacion solicitada es RECOVER_SIGN

            // Si se ha indicado un formato de upgrade, lo actualizamos; si no, lo eliminamos de la URL
            if (!string.IsNullOrEmpty(upgrade))
            {
                urlParameters = urlParameters.Replace(UPGRADE, upgrade);
            }
            else
            {
                urlParameters = urlParameters.Replace("&upgrade=" + UPGRADE, "");
            }
            if (!string.IsNullOrEmpty(upgradeConfigB64))
            {
                urlParameters = urlParameters.Replace(CONF, upgradeConfigB64.Replace('+', '-').Replace('/', '_'));
            }
            else
            {
                urlParameters = urlParameters.Replace("&config=" + CONF, "");
            }

            //  realizamos la peticion post al servicio y recibimos los datos de la peticion
            byte[] bytes = GetResponseToPostPetition(url, urlParameters, this.config.getConfig());

            // Identificamos los datos obtenidos
            FireTransactionResult result;
            try
            {
                result = new FireTransactionResult(bytes);
            }
            catch (Exception e)
            {
                throw new HttpOperationException("La respuesta del servicio no tiene un formato valido", e);
            }

            // Si el resultado es un error, se indica un periodo de gracia o si ya contiene la firma, lo devolvemos
            if (result.ErrorCode != null || result.GracePeriod != null || result.Result != null)
            {
                return result;
            }

            // Si no, hacemos una nueva llamada para recuperarla
            urlParameters = URL_PARAMETERS_RECOVER_SIGN_RESULT
                .Replace(APP_ID, this.appId)
                .Replace(TRANSACTION, transactionId)
                .Replace(SUBJECTID, subjectId)
                .Replace(OP, OP_CODE_RECOVER_SIGN_RESULT); // El tipo de operacion solicitada es RECOVER_SIGN_RESULT

            //  realizamos la peticion post al servicio y recibimos los datos de la peticion
            bytes = GetResponseToPostPetition(url, urlParameters, this.config.getConfig());
            result.Result = bytes;

            return result;
        }

        /// <summary>
        /// Recupera el error tras la firma de los datos haciendo uso del servicio de red de firma en la nube.
        /// </summary>
        /// <param name="transactionId">Identificador de la transacción.</param>
        /// <param name="subjectId">Identificador del usuario propietario de los certificados de firma.</param>
        /// <returns>Firma realizada en servidor.</returns>
        /// <exception cref="ArgumentException">Cuando se proporciona nulo o vacío un parámetro obligatorio.</exception>
        /// <exception cref="HttpForbiddenException">Cuando falla la autenticación con el componente central.</exception>
        /// <exception cref="HttpNetworkException">Cuando se produce un error de conexión con el componente central.</exception>
        /// <exception cref="HttpOperationException">Cuando se produce un error interno del servidor.</exception>
        /// <exception cref="InvalidTransactionException">Cuando se intenta operar sobre una transaccion inexistente o ya caducada.</exception>
        public FireTransactionResult recoverError(
                              string transactionId,
                              string subjectId)
        {

            if (string.IsNullOrEmpty(transactionId))
            {
                throw new ArgumentException(
                    "El id de la transaccion no puede ser nulo"
                );
            }
            if (string.IsNullOrEmpty(subjectId))
            {
                throw new ArgumentException(
                    "El identificador del titular no puede ser nulo"
                );
            }

            string url = this.config.getFireService();
            string urlParameters = URL_PARAMETERS_RECOVER_ERROR
                .Replace(OP, OP_CODE_RECOVER_ERROR) // El tipo de operacion solicitada es RECOVER_ERROR (99)
                .Replace(APP_ID, this.appId)
                .Replace(SUBJECTID, subjectId)
                .Replace(TRANSACTION, transactionId);

            //  Realizamos la peticion post al servicio y recibimos los datos de la peticion
            byte[] bytes = GetResponseToPostPetition(url, urlParameters, this.config.getConfig());

            try {
                return new FireTransactionResult(bytes);
            }
            catch (Exception e)
            {
                throw new HttpOperationException("La respuesta del servicio no tiene un formato valido", e);
            }
        }

        
        /// <summary>
        /// Crea un batch de documentos para posteriormente realizar la firma por lotes.
        /// </summary>
        /// <param name="subjectId">Identificador del firmante.</param>
        /// <param name="op">Tipo de operaciónn a realizar: "sign", "cosign" o "countersign".</param>
        /// <param name="ft">Formato de la operación: "XAdES", "PAdES", etc.</param>
        /// <param name="algth">Algoritmo de firma.</param>
        /// <param name="propB64">Propiedades extra a añadir a la firma (puede ser <code>null</code>).</param>
        /// <param name="upgrade"> Parámetros de actualización.</param>
        /// <param name="confB64">Parámetros de la configuración de la firma.</param>
        /// <returns>Referencia al proceso batch.</returns>
        /// <exception cref="ArgumentException">Cuando se proporciona nulo o vacío un parámetro obligatorio.</exception>
        /// <exception cref="HttpForbiddenException">Cuando falla la autenticación con el componente central.</exception>
        /// <exception cref="HttpNetworkException">Cuando se produce un error de conexión con el componente central.</exception>
        /// <exception cref="HttpOperationException">Cuando se produce un error interno del servidor.</exception>
        public FireTransactionIdResult createBatchProcess(
                              string subjectId,
                              string op,
                              string ft,
                              string algth,
                              string propB64,
                              string upgrade,
                              string confB64)
        {

            if (String.IsNullOrEmpty(subjectId))
            {
                throw new ArgumentException(
                    "El identificador del titular no puede ser nulo"
                );
            }
            if (String.IsNullOrEmpty(op))
            {
                throw new ArgumentException(
                    "El tipo de operacion de firma a realizar no puede ser nulo"
                );
            }
            if (String.IsNullOrEmpty(ft))
            {
                throw new ArgumentException(
                    "El formato de firma no puede ser nulo"
                );
            }
            if (String.IsNullOrEmpty(algth))
            {
                throw new ArgumentException(
                    "El algoritmo de firma no puede ser nulo"
                );
            }
            if (String.IsNullOrEmpty(confB64))
            {
                throw new ArgumentException(
                    "Los datos de configuracion no pueden ser nulos"
                );
            }

            string url = this.config.getFireService();

            string urlParameters = URL_PARAMETERS_CREATE_BATCH
                .Replace(OP, OP_CODE_CREATE_BATCH) // El tipo de operacion solicitada es CREATE_BATCH
                .Replace(APP_ID, this.appId)
                .Replace(SUBJECTID, subjectId)
                .Replace(COP, op)
                .Replace(ALG, algth)
                .Replace(FORMAT, ft)
                .Replace(PROP, string.IsNullOrEmpty(propB64) ? "" : propB64.Replace('+', '-').Replace('/', '_'))
                .Replace(CONF, confB64.Replace('+', '-').Replace('/', '_'));

            // Si se ha indicado un formato de upgrade, lo actualizamos; si no, lo eliminamos de la URL
            if (String.IsNullOrEmpty(upgrade)) {
                urlParameters = urlParameters.Replace("&upgrade=" + UPGRADE, "");
            }
            else {
                urlParameters = urlParameters.Replace(UPGRADE, upgrade);
            }

            //  realizamos la peticion post al servicio y recibimos los datos de la peticion
            byte[] bytes = GetResponseToPostPetition(url, urlParameters, this.config.getConfig());
            // Mostramos los datos obtenidos
            return new FireTransactionIdResult(System.Text.Encoding.UTF8.GetString(bytes));
        }
        
        /// <summary>
        /// Incluye un documento en el batch para realizar una firma por lotes.
        /// </summary>
        /// <param name="transactionId">Identificador de la transacción.</param>
        /// <param name="subjectId">Identificador del usuario propietario de los certificados de firma.</param>
        /// <param name="documentId">Identificador del documento a incluir.</param>
        /// <param name="documentB64">Documento en base 64.</param>
        /// <param name="confB64">Parámetros de la configuración de la firma.</param>
        /// <exception cref="ArgumentException">Cuando se proporciona nulo o vacío un parámetro obligatorio.</exception>
        /// <exception cref="HttpForbiddenException">Cuando falla la autenticación con el componente central.</exception>
        /// <exception cref="HttpNetworkException">Cuando se produce un error de conexión con el componente central.</exception>
        /// <exception cref="NumDocumentsExceededException">Cuando se intentan agregar más documentos de los permitidos al lote.</exception>
        /// <exception cref="DuplicateDocumentException">Cuando se el identificador de documento ya se usó para otro documento del lote.</exception>
        /// <exception cref="InvalidTransactionException">Cuando se intenta operar sobre una transaccion inexistente o ya caducada.</exception>
        /// <exception cref="HttpOperationException">Cuando se produce un error interno del servidor.</exception>
        public void addDocumentToBatch(
                              string transactionId,
                              string subjectId,
                              string documentId,
                              string documentB64,
                              string confB64)
        {

            if (string.IsNullOrEmpty(transactionId))
            {
                throw new ArgumentException(
                    "El id de la transaccion no puede ser nulo"
                );
            }
            if (string.IsNullOrEmpty(subjectId))
            {
                throw new ArgumentException(
                    "El identificador del titular no puede ser nulo"
                );
            }
            if (string.IsNullOrEmpty(documentId))
            {
                throw new ArgumentException(
                    "El identificador del documento no puede ser nulo"
                );
            }

            string url = this.config.getFireService();

            string urlParameters = URL_PARAMETERS_ADD_DOCUMENT_BATCH
                .Replace(OP, OP_CODE_ADD_DOCUMENT_TO_BATCH) // El tipo de operacion solicitada es ADD_DOCUMENT_TO_BATCH
                .Replace(APP_ID, this.appId)
                .Replace(SUBJECTID, subjectId)
                .Replace(TRANSACTION, transactionId)
                .Replace(DOCID, documentId)
                .Replace(DATA, documentB64 != null ? documentB64.Replace('+', '-').Replace('/', '_') : "")
                .Replace(CONF, string.IsNullOrEmpty(confB64) ? "" : confB64.Replace('+', '-').Replace('/', '_'));

            //  realizamos la peticion post al servicio y recibimos los datos de la peticion
            GetResponseToPostPetition(url, urlParameters, this.config.getConfig());
        }

        /// <summary>
        /// Incluye en el batch un documento con una configuración de firma propia para realizar una firma por lotes.
        /// </summary>
        /// <param name="transactionId">Identificador de la transacción.</param>
        /// <param name="subjectId">Identificador del usuario propietario de los certificados de firma.</param>
        /// <param name="documentId">Identificador del documento a incluir.</param>
        /// <param name="documentB64">Documento en base 64.</param>
        /// <param name="op">Tipo de operaciónn a realizar: "sign", "cosign" o "countersign".</param>
        /// <param name="ft">Formato de la operación: "XAdES", "PAdES", etc.</param>
        /// <param name="propB64">Propiedades extra a añadir a la firma (puede ser <code>null</code>).</param>
        /// <param name="upgrade"> Parámetros de actualización.</param>
        /// <param name="confB64">Parámetros de la configuración de la firma.</param>
        /// <exception cref="ArgumentException">Cuando se proporciona nulo o vacío un parámetro obligatorio.</exception>
        /// <exception cref="HttpForbiddenException">Cuando falla la autenticación con el componente central.</exception>
        /// <exception cref="HttpNetworkException">Cuando se produce un error de conexión con el componente central.</exception>
        /// <exception cref="InvalidTransactionException">Cuando se intenta operar sobre una transaccion inexistente o ya caducada.</exception>
        /// <exception cref="NumDocumentsExceededException">Cuando se intentan agregar más documentos de los permitidos al lote.</exception>
        /// <exception cref="DuplicateDocumentException">Cuando se el identificador de documento ya se usó para otro documento del lote.</exception>
        /// <exception cref="HttpOperationException">Cuando se produce un error interno del servidor.</exception>
        public void addDocumentToBatch(
                              string transactionId,
                              string subjectId,
                              string documentId,
                              string documentB64,
                              string op,
                              string ft,
                              string propB64,
                              string upgrade,
                              string confB64)
        {

            if (string.IsNullOrEmpty(transactionId))
            {
                throw new ArgumentException(
                    "El id de la transaccion no puede ser nulo"
                );
            }
            if (string.IsNullOrEmpty(subjectId))
            {
                throw new ArgumentException(
                    "El identificador del titular no puede ser nulo"
                );
            }
            if (string.IsNullOrEmpty(documentId))
            {
                throw new ArgumentException(
                    "El identificador del documento no puede ser nulo"
                );
            }
            if (string.IsNullOrEmpty(op))
            {
                throw new ArgumentException(
                    "El tipo de operacion de firma a realizar no puede ser nulo"
                );
            }
            if (string.IsNullOrEmpty(ft))
            {
                throw new ArgumentException(
                    "El formato de firma no puede ser nulo"
                );
            }
            string url = this.config.getFireService();

            string urlParameters = URL_PARAMETERS_ADD_CUSTOM_DOCUMENT_BATCH
                .Replace(OP, "6") // El tipo de operacion solicitada es ADD_DOCUMENT_TO_BATCH (6)
                .Replace(APP_ID, this.appId)
                .Replace(SUBJECTID, subjectId)
                .Replace(TRANSACTION, transactionId)
                .Replace(DOCID, documentId)
                .Replace(COP, op)
                .Replace(FORMAT, ft)
                .Replace(PROP, string.IsNullOrEmpty(propB64) ? "" : propB64.Replace('+', '-').Replace('/', '_'))
                .Replace(DATA, documentB64 != null ? documentB64.Replace('+', '-').Replace('/', '_') : "")
                .Replace(CONF, string.IsNullOrEmpty(confB64) ? "" : confB64.Replace('+', '-').Replace('/', '_'));
            
            // Si se ha indicado un formato de upgrade, lo actualizamos; si no, lo eliminamos de la URL
            if (string.IsNullOrEmpty(upgrade))
            {
                urlParameters = urlParameters.Replace("&upgrade=" + UPGRADE, "");
            }
            else
            {
                urlParameters = urlParameters.Replace(UPGRADE, upgrade);
            }

            //  realizamos la peticion post al servicio y recibimos los datos de la peticion
            GetResponseToPostPetition(url, urlParameters, this.config.getConfig());
        }

        /// <summary>
        /// Realiza una firma por lotes.
        /// </summary>
        /// <param name="transactionId">Identificador de la transacción.</param>
        /// <param name="subjectId">Identificador del usuario propietario de los certificados de firma.</param>
        /// <param name="stopOnError">Indicador de si debe detenerse al producirse un error en la firma.</param>
        /// <returns>URL de redirección.</returns>
        /// <exception cref="ArgumentException">Cuando se proporciona nulo o vacío un parámetro obligatorio.</exception>
        /// <exception cref="HttpForbiddenException">Cuando falla la autenticación con el componente central.</exception>
        /// <exception cref="HttpNetworkException">Cuando se produce un error de conexión con el componente central.</exception>
        /// <exception cref="InvalidTransactionException">Cuando se intenta operar sobre una transaccion inexistente o ya caducada.</exception>
        /// <exception cref="HttpOperationException">Cuando se produce un error interno del servidor.</exception>
        public FireLoadResult signBatch(
                              string transactionId,
                              string subjectId,
                              bool stopOnError)
        {

            if (string.IsNullOrEmpty(transactionId))
            {
                throw new ArgumentException(
                    "El id de la transaccion no puede ser nulo"
                );
            }
            if (string.IsNullOrEmpty(subjectId))
            {
                throw new ArgumentException(
                    "El identificador del titular no puede ser nulo"
                );
            }

            string url = this.config.getFireService();

            string urlParameters = URL_PARAMETERS_SIGN_BATCH
                .Replace(APP_ID, this.appId)
                .Replace(TRANSACTION, transactionId)
                .Replace(SUBJECTID, subjectId)
                .Replace(OP, OP_CODE_SIGN_BATCH) // El tipo de operacion solicitada es SIGN_BATCH
                .Replace(STOPONERROR, stopOnError.ToString());

            //  realizamos la peticion post al servicio y recibimos los datos de la peticion
            byte[] bytes = GetResponseToPostPetition(url, urlParameters, this.config.getConfig());
            // Mostramos los datos obtenidos
            return new FireLoadResult(System.Text.Encoding.UTF8.GetString(bytes));
        }

        /// <summary>
        /// Recupera la firma de los datos haciendo uso del servicio de red de firma en la nube.
        /// </summary>
        /// <param name="transactionId">Identificador de la transacción.</param>
        /// <param name="subjectId">Identificador del usuario propietario de los certificados de firma.</param>
        /// <returns>Firma realizada en servidor.</returns>
        /// <exception cref="ArgumentException">Cuando se proporciona nulo o vacío un parámetro obligatorio.</exception>
        /// <exception cref="HttpForbiddenException">Cuando falla la autenticación con el componente central.</exception>
        /// <exception cref="HttpNetworkException">Cuando se produce un error de conexión con el componente central.</exception>
        /// <exception cref="InvalidTransactionException">Cuando se intenta operar sobre una transaccion inexistente o ya caducada.</exception>
        /// <exception cref="HttpOperationException">Cuando se produce un error interno del servidor.</exception>
        public FireBatchResult recoverBatchResult(
                              string transactionId,
                              string subjectId)
        {

            if (string.IsNullOrEmpty(transactionId))
            {
                throw new ArgumentException(
                    "El id de la transaccion no puede ser nulo"
                );
            }
            if (string.IsNullOrEmpty(subjectId))
            {
                throw new ArgumentException(
                    "El identificador del titular no puede ser nulo"
                );
            }

            string url = this.config.getFireService();
            string urlParameters = URL_PARAMETERS_RECOVER_BATCH_RESULT
                .Replace(APP_ID, this.appId)
                .Replace(TRANSACTION, transactionId)
                .Replace(SUBJECTID, subjectId)
                .Replace(OP, OP_CODE_RECOVER_BATCH); // El tipo de operacion solicitada es RECOVER_BATCH

            //  Realizamos la peticion post al servicio y recibimos los datos de la peticion
            byte[] bytes = GetResponseToPostPetition(url, urlParameters, this.config.getConfig());
            FireBatchResult batchResult;
            try
            {
                // Componemos el objeto de resultado con la respuesta del servicio
                BatchResultJson batchResultJson = DeserializedBatchResult(System.Text.Encoding.UTF8.GetString(bytes));
                batchResult = FireBatchResult.Parse(batchResultJson);
            }
            catch (Exception e)
            {
                throw new HttpOperationException("El servicio respondio con un JSON no valido: " + System.Text.Encoding.UTF8.GetString(bytes), e);
            }
            return batchResult;
        }

        /// <summary>
        /// Recupera el progreso del proceso de firma de los datos del proceso batch.
        /// </summary>
        /// <param name="transactionId">Identificador de la transacción.</param>
        /// <param name="subjectId">Identificador del usuario propietario de los certificados de firma.</param>
        /// <returns>Progreso de la firma.</returns>
        /// <exception cref="ArgumentException">Cuando se proporciona nulo o vacío un parámetro obligatorio.</exception>
        /// <exception cref="HttpForbiddenException">Cuando falla la autenticación con el componente central.</exception>
        /// <exception cref="HttpNetworkException">Cuando se produce un error de conexión con el componente central.</exception>
        /// <exception cref="InvalidTransactionException">Cuando se intenta operar sobre una transaccion inexistente o ya caducada.</exception>
        /// <exception cref="HttpOperationException">Cuando se produce un error interno del servidor.</exception>
        public float recoverBatchResultState(
                              string transactionId,
                              string subjectId)
        {

            if (string.IsNullOrEmpty(transactionId))
            {
                throw new ArgumentException(
                    "El id de la transaccion no puede ser nulo"
                );
            }
            if (string.IsNullOrEmpty(subjectId))
            {
                throw new ArgumentException(
                    "El identificador del titular no puede ser nulo"
                );
            }

            string url = this.config.getFireService();
            string urlParameters = URL_PARAMETERS_RECOVER_BATCH_STATE
                .Replace(APP_ID, this.appId)
                .Replace(TRANSACTION, transactionId)
                .Replace(SUBJECTID, subjectId)
                .Replace(OP, OP_CODE_RECOVER_BATCH_STATE); // El tipo de operacion solicitada es RECOVER_BATCH_STATE

            //  realizamos la peticion post al servicio y recibimos los datos de la peticion
            byte[] bytes = GetResponseToPostPetition(url, urlParameters, this.config.getConfig());

            // Mostramos los datos obtenidos
            return float.Parse(System.Text.Encoding.UTF8.GetString(bytes), System.Globalization.CultureInfo.InvariantCulture);
        }

        /// <summary>
        /// Recupera la firma de un documento del proceso batch haciendo uso del servicio de red de firma en la nube.
        /// </summary>
        /// <param name="transactionId">Identificador de la transacción.</param>
        /// <param name="subjectId">Identificador del usuario propietario de los certificados de firma.</param>
        /// <param name="docId">Identificador del documento seleccionado.</param>
        /// <returns>Firma del documento realizada en servidor.</returns>
        /// <exception cref="ArgumentException">Cuando se proporciona nulo o vacío un parámetro obligatorio.</exception>
        /// <exception cref="HttpForbiddenException">Cuando falla la autenticación con el componente central.</exception>
        /// <exception cref="HttpNetworkException">Cuando se produce un error de conexión con el componente central.</exception>
        /// <exception cref="InvalidTransactionException">Cuando se intenta operar sobre una transaccion inexistente o ya caducada.</exception>
        /// <exception cref="InvalidBatchDocumentException">Cuando se indica el identificador de un documento que no existe en el lote o que no se firmó correctamente.</exception>
        /// <exception cref="BatchNoSignedException">Cuando se solicita recuperar una firma del lote antes de firmar el propio lote.</exception>
        /// <exception cref="HttpOperationException">Cuando se produce un error interno del servidor.</exception>
        public FireTransactionResult recoverBatchSign(
                              string transactionId,
                              string subjectId,
                              string docId)
        {

            if (string.IsNullOrEmpty(transactionId))
            {
                throw new ArgumentException(
                    "El id de la transaccion no puede ser nulo"
                );
            }
            if (string.IsNullOrEmpty(subjectId))
            {
                throw new ArgumentException(
                    "El identificador del titular no puede ser nulo"
                );
            }
            if (string.IsNullOrEmpty(docId))
            {
                throw new ArgumentException(
                    "El identificador del documento no puede ser nulo"
                );
            }

            string url = this.config.getFireService();
            string urlParameters = URL_PARAMETERS_RECOVER_BATCH_SIGN
                .Replace(APP_ID, this.appId)
                .Replace(DOCID, docId)
                .Replace(TRANSACTION, transactionId)
                .Replace(SUBJECTID, subjectId)
                .Replace(OP, OP_CODE_RECOVER_BATCH_SIGN); // El tipo de operacion solicitada es RECOVER_BATCH_SIGN

            //  realizamos la peticion post al servicio y recibimos los datos de la peticion
            byte[] bytes = GetResponseToPostPetition(url, urlParameters, this.config.getConfig());
            try
            {
                return new FireTransactionResult(bytes);
            }
            catch (Exception e)
            {
                throw new HttpOperationException("La respuesta del servicio no tiene un formato valido", e);
            }
        }

        /// <summary>
        /// Recupera una firma enviada a generar anteriormente y para la que se solicitó esperar un periodo de gracia.
        /// </summary>
        /// <param name="docId">Identificador de documento recibido de la  usuario propietario de los certificados de firma.</param>
        /// <param name="upgrade">Formato al que solicitamos actualizar la firma. Si se indica, se comprobará que la
        /// firma devuelta esté en el formato indicado.</param>
        /// <param name="confB64">Properties codificado en base 64 con configuración adicional para la plataforma de
        /// actualización de firma. </param>
        /// <param name="allowPartial">Indica si se debe devolver la firma incluso si no se ha actualizado al formato
        /// solicitado.</param>
        /// <returns>Resultado con la firma recuperada o un nuevo periodo de gracia.</returns>
        /// <exception cref="ArgumentException">Cuando se proporciona nulo o vacío un parámetro obligatorio.</exception>
        /// <exception cref="HttpForbiddenException">Cuando falla la autenticación con el componente central.</exception>
        /// <exception cref="HttpNetworkException">Cuando se produce un error de conexión con el componente central.</exception>
        /// <exception cref="HttpOperationException">Cuando se produce un error durante la operación.</exception>
        public FireTransactionResult recoverAsyncSign(
                              string docId,
                              string upgrade,
                              string confB64,
                              bool allowPartial
                              )
        {

            if (string.IsNullOrEmpty(docId))
            {
                throw new ArgumentException(
                    "El identificador del documento firmado no puede ser nulo"
                );
            }

            string url = this.config.getFireService();
            string urlParameters = URL_PARAMETERS_RECOVER_ASYNC_SIGN
                .Replace(APP_ID, this.appId)
                .Replace(DOCID, docId)
                .Replace(OP, OP_CODE_RECOVER_ASYNC_SIGN)
                .Replace(PARTIAL, allowPartial.ToString());

            // Establecemos el formato de update y la configuracion para el validador si se han establecido.
            // Si no, los eliminamos de la URL
            if (!string.IsNullOrEmpty(upgrade))
            {
                urlParameters = urlParameters.Replace(UPGRADE, upgrade);
            }
            else
            {
                urlParameters = urlParameters.Replace("&upgrade=" + UPGRADE, "");
            }
            if (!string.IsNullOrEmpty(confB64))
            {
                urlParameters = urlParameters.Replace(CONF, confB64.Replace('+', '-').Replace('/', '_'));
            }
            else
            {
                urlParameters = urlParameters.Replace("&config=" + CONF, "");
            }

            //  realizamos la peticion post al servicio y recibimos los datos de la peticion
            byte[] bytes = GetResponseToPostPetition(url, urlParameters, this.config.getConfig());

            // Identificamos los datos obtenidos
            FireTransactionResult result;
            try
            {
                result = new FireTransactionResult(bytes);
            }
            catch (Exception e)
            {
                throw new HttpOperationException("La respuesta del servicio no tiene un formato valido", e);
            }

            // Si el resultado es un error, se indica un periodo de gracia o si ya contiene la firma, lo devolvemos
            if (result.ErrorCode != null || result.GracePeriod != null || result.Result != null)
            {
                return result;
            }

            // Si no, hacemos una nueva llamada para recuperarla
            urlParameters = URL_PARAMETERS_RECOVER_ASYNC_SIGN_RESULT
                .Replace(APP_ID, this.appId)
                .Replace(DOCID, docId)
                .Replace(OP, OP_CODE_RECOVER_ASYNC_SIGN_RESULT); // El tipo de operacion solicitada es RECOVER_ASYNC_SIGN_RESULT

            //  Realizamos la peticion al servicio y recibimos los datos de la peticion
            bytes = GetResponseToPostPetition(url, urlParameters, this.config.getConfig());
            result.Result = bytes;

            return result;
        }

        /// <summary>
        /// Realiza una peticion POST y devuelve un byte array con la respuesta.
        /// </summary>
        /// <param name="url">URL a la que realizar la petición.</param>
        /// <param name="urlParameters">Parámetros que se envían en la petición.</param>
        /// <param name="config">Configuración para la conexión de red.</param>
        /// <returns>Respuesta de la llamada a la URL indicada.</returns>
        /// <exception cref="ArgumentException">Cuando se proporciona un parámetro no válido.</exception>
        /// <exception cref="HttpForbiddenException">Cuando falla la autenticación con el componente central.</exception>
        /// <exception cref="HttpNetworkException">Cuando se produce un error de conexión con el componente central.</exception>
        /// <exception cref="HttpOperationException">Cuando se produce un error interno del servidor.</exception>
        /// <exception cref="HttpNoUserException">Cuando el usuario no esta dado de alta en el proveedor indicado.</exception>
        /// <exception cref="HttpCertificateBlockedException">Cuando el certificado el usuario está caducado.</exception>
        /// <exception cref="HttpWeakRegistryException">Cuando el usuario realizó un registro no fehaciente en el proveedor.</exception>
        /// <exception cref="NumDocumentsExceededException">Cuando se intentan agregar a un lote más documentos de los permitidos.</exception>
        /// <exception cref="DuplicateDocumentException">Cuando se intenta agregar a un lote un documento con el mismo identificador que otro utilizado anteriormente.</exception>
        /// <exception cref="BatchNoSignedException">Cuando se intenta recuperar la firma de un documento de un lote antes de firmar el propio lote.</exception>
        /// <exception cref="InvalidBatchDocumentException">Cuando se solicita la firma de un documento de un lote que no existe o que no se firmó correctamente.</exception>
        /// <exception cref="InvalidTransactionException">Cuando se intenta operar sobre una transaccion inexistente o ya caducada.</exception>
        private static byte[] GetResponseToPostPetition(string url, string urlParameters, Dictionary<String, String> config)
        {
            HttpWebResponse response = null;
            Stream dataStream = null;
            MemoryStream ms = null;
            try
            {
                // generamos la respuesta del servidor
                response = ConnectionManager.ConnectByPost(url, urlParameters, config);
                // recibimos el stream de la respuesta
                dataStream = response.GetResponseStream();
                ms = new MemoryStream();
                dataStream.CopyTo(ms);
                byte[] bytes = ms.ToArray();

                return bytes;
            }
            catch (WebException e)
            {
                HttpWebResponse r = (HttpWebResponse) e.Response;
                
                if (e.GetBaseException() is AuthenticationException)
                {
                    throw new HttpForbiddenException("Error durante la autenticacion de la aplicacion", e);
                }
                if (r == null)
                {
                    throw new HttpNetworkException("Error al realizar la conexion: " + e.Message, e);
                }

                string contentType = r.ContentType;
                if (!string.IsNullOrEmpty(contentType)
                    && contentType.StartsWith("application/json", StringComparison.OrdinalIgnoreCase))
                {
                    ErrorResultJson errorResult;
                    try
                    {
                        dataStream = r.GetResponseStream();
                        ms = new MemoryStream();
                        dataStream.CopyTo(ms);
                        byte[] bytes = ms.ToArray();

                        System.Text.Encoding encoding = !string.IsNullOrEmpty(r.ContentEncoding)
                            ? System.Text.Encoding.GetEncoding(r.ContentEncoding)
                            : System.Text.Encoding.UTF8;

                        string json = encoding.GetString(bytes);

                        var json_serializer = new JavaScriptSerializer();
                        errorResult = json_serializer.Deserialize<ErrorResultJson>(json);
                    }
                    catch (Exception e2)
                    {

                        throw new HttpOperationException("Error. ContenType: " + r.ContentType + ". Encoding: " + r.ContentEncoding + ". Length: " + r.ContentLength + ". Excepcion: " + e2.ToString(), e2);

                        //throw new HttpOperationException("No se pudo decodificar la respuesta de error del servicio: " + e2.ToString(), e2);
                    }

                    int code = errorResult.c;
                    string message = errorResult.m;
                    if (FIReErrors.FORBIDDEN == code || FIReErrors.UNAUTHORIZED == code)
                    {
                        throw new HttpForbiddenException(code, message);
                    }
                    else if (FIReErrors.UNKNOWN_USER == code)
                    {
                        throw new HttpNoUserException(code, message);
                    }
                    else if (FIReErrors.INVALID_TRANSACTION == code)
                    {
                        throw new InvalidTransactionException(code, message);
                    }
                    else if (FIReErrors.CERTIFICATE_BLOCKED == code)
                    {
                        throw new HttpCertificateBlockedException(code, message);
                    }
                    else if (FIReErrors.CERTIFICATE_WEAK_REGISTRY == code)
                    {
                        throw new HttpWeakRegistryException(code, message);
                    }
                    else if (FIReErrors.BATCH_DUPLICATE_DOCUMENT == code)
                    {
                        throw new DuplicateDocumentException(code, message);
                    }
                    else if (FIReErrors.BATCH_INVALID_DOCUMENT == code)
                    {
                        throw new InvalidBatchDocumentException(code, message);
                    }
                    else if (FIReErrors.BATCH_NUM_DOCUMENTS_EXCEEDED == code)
                    {
                        throw new NumDocumentsExceededException(code, message);
                    }
                    else if (FIReErrors.BATCH_NO_SIGNED == code)
                    {
                        throw new BatchNoSignedException(code, message);
                    }
                    else
                    {
                        // El codigo de error no revela lo ocurrido, pero quizas se pueda determinar por otras vias
                        if (r.StatusCode == HttpStatusCode.RequestEntityTooLarge)
                        {
                            throw new HttpTooLargeContentException(FIReErrors.TOO_LARGE_CONTENT);
                        }
                        throw new HttpOperationException(code, message);
                    }
                }

                if (r.StatusCode == HttpStatusCode.Forbidden)
                {
                    throw new HttpForbiddenException("Error HTTP " + r.StatusCode, e);
                }
                else if (r.StatusCode == HttpStatusCode.RequestTimeout)
                {
                    throw new HttpNetworkException("Error HTTP " + r.StatusCode, e);
                }
                throw new HttpOperationException("Error desconocido en la llamada a FIRe: Error HTTP " + r.StatusCode, e);

            }
            catch (ProtocolViolationException e)
            {
                throw new HttpNetworkException("Ha ocurrido un problema con el protocolo:" + e.Message, e);
            }
            catch (InvalidOperationException e)
            {
                throw new HttpNetworkException("Operacion HTTP no permitida por el servicio:" + e.Message, e);
            }
            catch (IOException e)
            {
                throw new HttpOperationException("Error al leer la respuesta del servidor: " + e.Message, e);
            }
            catch (Exception e) {
                throw new HttpOperationException(e.Message, e);
            }
            finally
            {
                // Cerramos los streams
                if (ms != null) { ms.Close(); }
                if (dataStream != null) { dataStream.Close(); }
                if (response != null) response.Close();
            }
        }

        /// <summary>
        ///  Deserializa una estructura JSON para obtener de ella un objeto de tipo FireBatchResult.
        /// </summary>
        /// <param name="JSON">Cadena en formato JSON que se desea analizar.</param>
        /// <returns></returns>
        private static BatchResultJson DeserializedBatchResult(string JSON)
        {
            var json_serializer = new JavaScriptSerializer();
            return json_serializer.Deserialize<BatchResultJson>(JSON);
        }
    }

}
