/* Copyright (C) 2017 [Gobierno de Espana]
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

namespace FIRe
{
    /// <summary> Cliente del servicio de firma electrónica.</summary>
    /// La operación a realizar podrá ser firma, cofirma o contrafirma.
    /// Esta clase obtiene la URL del servicio de firma a través del registro.
    public static class FireApi
    {
        // Parametros que necesitamos de la URL.
        private static readonly String APP_ID = "%APPID%";
        private static readonly String SUBJECTID = "%SUBJECTID%";
        private static readonly String TRANSACTION = "%TRANSACTION%";
        private static readonly String COP = "%COP%";
        private static readonly String OP = "%OP%";
	    private static readonly String ALG = "%ALG%";
	    private static readonly String FORMAT = "%FT%";
	    private static readonly String DATA = "%DATA%";
	    private static readonly String CONF = "%CONF%";
        private static readonly String UPGRADE = "%UPGRADE%";
        private static readonly string PARTIAL = "%ALLOWPARTIAL%";
        private static readonly String PROP = "%PROP%";
        private static readonly String DOCID = "%DOCID%";
        private static readonly String STOPONERROR = "%STOPONERROR%";

        private static readonly String URL_PARAMETERS_SIGN =
            "appid=" + APP_ID +
            "&subjectid=" + SUBJECTID +
            "&cop=" + COP +
            "&op=" + OP +
            "&algorithm=" + ALG +
            "&format=" + FORMAT +
            "&properties=" + PROP + 
            "&dat=" + DATA +
            "&config=" + CONF;

        /// <summary>
        /// Firma unos datos haciendo uso del servicio de red de firma en la nube.
        /// </summary>
        /// <param name="appId">Identificador de la aplicación.</param>
        /// <param name="subjectId">Identificador del firmante.</param>
        /// <param name="op">Tipo de operaciónn a realizar: "sign", "cosign" o "countersign".</param>
        /// <param name="ft">Formato de la operación: "XAdES", "PAdES", etc.</param>
        /// <param name="algth">Algoritmo de firma.</param>
        /// <param name="propB64">Propiedades extra a añadir a la firma (puede ser <code>null</code>).</param>
        /// <param name="dataB64"> Datos a firmar en base64.</param>
        /// <param name="confB64">Parámetros de la configuración de la firma.</param>
        /// <returns>Firma realizada en servidor.</returns>
        /// <exception cref="ArgumentException">Cuando se proporciona un parámetro no válido.</exception>
        /// <exception cref="HttpForbiddenException">Cuando falla la autenticación con el componente central.</exception>
        /// <exception cref="HttpNetworkException">Cuando se produce un error de conexión con el componente central.</exception>
        /// <exception cref="HttpOperationException">Cuando se produce un error interno del servidor.</exception>
        /// <exception cref="ConfigureException">Cuando no se encuentra configurada la URL del servicio.</exception>
        public static FireLoadResult sign(
                              String appId,
                              String subjectId,
							  String op,
			                  String ft,
			                  String algth,
			                  String propB64,
			                  String dataB64,
			                  String confB64) {
            
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
            
            string url = ConfigManager.getFireService();
            
            string urlParameters = URL_PARAMETERS_SIGN
                .Replace(APP_ID, appId)
                .Replace(SUBJECTID, subjectId)
                .Replace(COP, op)
                .Replace(OP, "1") // El tipo de operacion solicitada es SIGN (1)
                .Replace(ALG, algth)
                .Replace(FORMAT, ft)
                .Replace(PROP, string.IsNullOrEmpty(propB64) ? "" : propB64.Replace('+', '-').Replace('/', '_'))
                .Replace(DATA, dataB64.Replace('+', '-').Replace('/', '_'))
                .Replace(CONF, confB64.Replace('+', '-').Replace('/', '_'));
            //  realizamos la peticion post al servicio y recibimos los datos de la peticion
            byte[] bytes = getResponseToPostPetition(url, urlParameters);
            // Mostramos los datos obtenidos
            return new FireLoadResult(System.Text.Encoding.UTF8.GetString(bytes));
        }

        private static readonly String URL_PARAMETERS_RECOVER_SIGN =
            "appid=" + APP_ID +
            "&transactionid=" + TRANSACTION +
            "&upgrade=" + UPGRADE +
            "&op=" + OP;

        private static readonly string URL_PARAMETERS_RECOVER_SIGN_RESULT =
            "appid=" + APP_ID +
            "&transactionid=" + TRANSACTION +
            "&op=" + OP;

        /// <summary>
        /// Recupera la firma de los datos haciendo uso del servicio de red de firma en la nube.
        /// </summary>
        /// <param name="appId">Identificador de la aplicación.</param>
        /// <param name="transactionId">Identificador de la transacción.</param>
        /// <param name="upgrade">Formato al que queremos mejorar la firma (puede ser null).</param>
        /// <returns>Firma realizada en servidor.</returns>
        /// <exception cref="ArgumentException">Cuando se proporciona un parámetro no válido.</exception>
        /// <exception cref="HttpForbiddenException">Cuando falla la autenticación con el componente central.</exception>
        /// <exception cref="HttpNetworkException">Cuando se produce un error de conexión con el componente central.</exception>
        /// <exception cref="HttpOperationException">Cuando se produce un error interno del servidor.</exception>
        /// <exception cref="ConfigureException">Cuando no se encuentra configurada la URL del servicio.</exception>
        public static FireTransactionResult recoverSign(
                              String appId,
                              String transactionId,
                              String upgrade)
        {

            if (string.IsNullOrEmpty(transactionId))
            {
                throw new ArgumentException(
                    "El id de la transaccion no puede ser nulo"
                );
            }

            string url = ConfigManager.getFireService();
            string urlParameters = URL_PARAMETERS_RECOVER_SIGN
                .Replace(APP_ID, appId)
                .Replace(TRANSACTION, transactionId)
                .Replace(OP, "2"); // El tipo de operacion solicitada es RECOVER_SIGN (2)

            // Si se ha indicado un formato de upgrade, lo actualizamos; si no, lo eliminamos de la URL
            if (upgrade != null && upgrade != "")
            {
                urlParameters = urlParameters.Replace(UPGRADE, upgrade);
            }
            else
            {
                urlParameters = urlParameters.Replace("&upgrade=" + UPGRADE, ""); //$NON-NLS-1$ //$NON-NLS-2$
            }

            //  realizamos la peticion post al servicio y recibimos los datos de la peticion
            byte[] bytes = getResponseToPostPetition(url, urlParameters);

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

            // Si el resultado es un error o si ya contiene la firma, lo devolvemos
            if (result.ErrorCode != null || result.Result != null)
            {
                return result;
            }

            // Si no, hacemos una nueva llamada para recuperarla
            urlParameters = URL_PARAMETERS_RECOVER_SIGN_RESULT
                .Replace(APP_ID, appId)
                .Replace(TRANSACTION, transactionId)
                .Replace(OP, "11"); // El tipo de operacion solicitada es RECOVER_SIGN_RESULT (11)

            //  realizamos la peticion post al servicio y recibimos los datos de la peticion
            bytes = getResponseToPostPetition(url, urlParameters);
            result.Result = bytes;

            return result;
        }

        /// <summary>
        /// Recupera el error tras la firma de los datos haciendo uso del servicio de red de firma en la nube.
        /// </summary>
        /// <param name="appId">Identificador de la aplicación.</param>
        /// <param name="transactionId">Identificador de la transacción.</param>
        /// <returns>Firma realizada en servidor.</returns>
        /// <exception cref="ArgumentException">Cuando se proporciona un parámetro no válido.</exception>
        /// <exception cref="HttpForbiddenException">Cuando falla la autenticación con el componente central.</exception>
        /// <exception cref="HttpNetworkException">Cuando se produce un error de conexión con el componente central.</exception>
        /// <exception cref="HttpOperationException">Cuando se produce un error interno del servidor.</exception>
        /// <exception cref="ConfigureException">Cuando no se encuentra configurada la URL del servicio.</exception>
        public static FireTransactionResult recoverError(
                              String appId,
                              String transactionId)
        {

            if (string.IsNullOrEmpty(transactionId))
            {
                throw new ArgumentException(
                    "El id de la transaccion no puede ser nulo"
                );
            }

            string url = ConfigManager.getFireService();
            string urlParameters = URL_PARAMETERS_RECOVER_SIGN
                .Replace(APP_ID, appId)
                .Replace(TRANSACTION, transactionId)
                .Replace(OP, "99"); // El tipo de operacion solicitada es RECOVER_ERROR (99)

            //  realizamos la peticion post al servicio y recibimos los datos de la peticion
            byte[] bytes = getResponseToPostPetition(url, urlParameters);
            
            // Devolvemos la respuesta
            try {
                return new FireTransactionResult(bytes);
            }
            catch (Exception e)
            {
                throw new HttpOperationException("La respuesta del servicio no tiene un formato valido", e);
            }
        }

        private static readonly String URL_PARAMETERS_CREATE_BATCH =
            "appid=" + APP_ID +
            "&subjectid=" + SUBJECTID +
            "&cop=" + COP +
            "&op=" + OP +
            "&algorithm=" + ALG +
            "&format=" + FORMAT +
            "&properties=" + PROP +
            "&upgrade=" + UPGRADE +
            "&config=" + CONF;

        /// <summary>
        /// Crea un batch de documentos para posteriormente realizar la firma por lotes.
        /// </summary>
        /// <param name="appId">Identificador de la aplicación.</param>
        /// <param name="subjectId">Identificador del firmante.</param>
        /// <param name="op">Tipo de operaciónn a realizar: "sign", "cosign" o "countersign".</param>
        /// <param name="ft">Formato de la operación: "XAdES", "PAdES", etc.</param>
        /// <param name="algth">Algoritmo de firma.</param>
        /// <param name="propB64">Propiedades extra a añadir a la firma (puede ser <code>null</code>).</param>
        /// <param name="upgrade"> Parámetros de actualización.</param>
        /// <param name="confB64">Parámetros de la configuración de la firma.</param>
        /// <returns>Referencia al proceso batch.</returns>
        /// <exception cref="ArgumentException">Cuando se proporciona un parámetro no válido.</exception>
        /// <exception cref="HttpForbiddenException">Cuando falla la autenticación con el componente central.</exception>
        /// <exception cref="HttpNetworkException">Cuando se produce un error de conexión con el componente central.</exception>
        /// <exception cref="HttpOperationException">Cuando se produce un error interno del servidor.</exception>
        /// <exception cref="ConfigureException">Cuando no se encuentra configurada la URL del servicio.</exception>
        public static FireTransactionIdResult createBatchProcess(
                              String appId,
                              String subjectId,
                              String op,
                              String ft,
                              String algth,
                              String propB64,
                              String upgrade,
                              String confB64)
        {

            if (string.IsNullOrEmpty(subjectId))
            {
                throw new ArgumentException(
                    "El id de usuario no puede ser nulo"
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
            if (string.IsNullOrEmpty(confB64))
            {
                throw new ArgumentException(
                    "Los datos de configuracion no pueden ser nulos"
                );
            }

            string url = ConfigManager.getFireService();

            string urlParameters = URL_PARAMETERS_CREATE_BATCH
                .Replace(APP_ID, appId)
                .Replace(SUBJECTID, subjectId)
                .Replace(COP, op)
                .Replace(OP, "5") // El tipo de operacion solicitada es CREATE_BATCH (5)
                .Replace(ALG, algth)
                .Replace(FORMAT, ft)
                .Replace(PROP, string.IsNullOrEmpty(propB64) ? "" : propB64.Replace('+', '-').Replace('/', '_'))
                .Replace(CONF, confB64.Replace('+', '-').Replace('/', '_'));

            // Si se ha indicado un formato de upgrade, lo actualizamos; si no, lo eliminamos de la URL
            if (string.IsNullOrEmpty(upgrade)) {
                urlParameters = urlParameters.Replace("&upgrade=" + UPGRADE, "");
            }
            else {
                urlParameters = urlParameters.Replace(UPGRADE, upgrade);
            }

            //  realizamos la peticion post al servicio y recibimos los datos de la peticion
            byte[] bytes = getResponseToPostPetition(url, urlParameters);
            // Mostramos los datos obtenidos
            return new FireTransactionIdResult(System.Text.Encoding.UTF8.GetString(bytes));
        }

        private static readonly String URL_PARAMETERS_ADD_DOCUMENT_BATCH =
            "appid=" + APP_ID +
            "&transactionid=" + TRANSACTION +
            "&op=" + OP +
            "&dat=" + DATA +
            "&docid=" + DOCID +
            "&config=" + CONF;

        /// <summary>
        /// Incluye un documento en el batch para realizar una firma por lotes.
        /// </summary>
        /// <param name="appId">Identificador de la aplicación.</param>
        /// <param name="transactionId">Identificador de la transacción.</param>
        /// <param name="documentId">Identificador del documento a incluir.</param>
        /// <param name="documentB64">Documento en base 64.</param>
        /// <param name="confB64">Parámetros de la configuración de la firma.</param>
        /// <exception cref="ArgumentException">Cuando se proporciona un parámetro no válido.</exception>
        /// <exception cref="HttpForbiddenException">Cuando falla la autenticación con el componente central.</exception>
        /// <exception cref="HttpNetworkException">Cuando se produce un error de conexión con el componente central.</exception>
        /// <exception cref="HttpOperationException">Cuando se produce un error interno del servidor.</exception>
        /// <exception cref="ConfigureException">Cuando no se encuentra configurada la URL del servicio.</exception>
        public static void addDocumentToBatch(
                              String appId,
                              String transactionId,
                              String documentId,
                              String documentB64,
                              String confB64)
        {

            if (string.IsNullOrEmpty(transactionId))
            {
                throw new ArgumentException(
                    "El id de la transaccion no puede ser nulo"
                );
            }
            if (string.IsNullOrEmpty(documentId))
            {
                throw new ArgumentException(
                    "El identificador del documento no puede ser nulo"
                );
            }
            if (string.IsNullOrEmpty(documentB64))
            {
                throw new ArgumentException(
                    "El documento a incluir no puede ser nulo"
                );
            }

            string url = ConfigManager.getFireService();

            string urlParameters = URL_PARAMETERS_ADD_DOCUMENT_BATCH
                .Replace(APP_ID, appId)
                .Replace(TRANSACTION, transactionId)
                .Replace(OP, "6") // El tipo de operacion solicitada es ADD_DOCUMENT_TO_BATCH (6)
                .Replace(DOCID, documentId)
                .Replace(DATA, documentB64.Replace('+', '-').Replace('/', '_'))
                .Replace(CONF, string.IsNullOrEmpty(confB64) ? "" : confB64.Replace('+', '-').Replace('/', '_'));

            //  realizamos la peticion post al servicio y recibimos los datos de la peticion
            getResponseToPostPetition(url, urlParameters);
        }

        private static readonly String URL_PARAMETERS_ADD_CUSTOM_DOCUMENT_BATCH =
            "appid=" + APP_ID +
            "&transactionid=" + TRANSACTION +
            "&op=" + OP +
            "&dat=" + DATA +
            "&docid=" + DOCID +
            "&cop=" + COP +
            "&format=" + FORMAT +
            "&properties=" + PROP +
            "&upgrade=" + UPGRADE +
            "&config=" + CONF;

        /// <summary>
        /// Incluye en el batch un documento con una configuración de firma propia para realizar una firma por lotes.
        /// </summary>
        /// <param name="appId">Identificador de la aplicación.</param>
        /// <param name="transactionId">Identificador de la transacción.</param>
        /// <param name="documentId">Identificador del documento a incluir.</param>
        /// <param name="documentB64">Documento en base 64.</param>
        /// <param name="op">Tipo de operaciónn a realizar: "sign", "cosign" o "countersign".</param>
        /// <param name="ft">Formato de la operación: "XAdES", "PAdES", etc.</param>
        /// <param name="propB64">Propiedades extra a añadir a la firma (puede ser <code>null</code>).</param>
        /// <param name="upgrade"> Parámetros de actualización.</param>
        /// <param name="confB64">Parámetros de la configuración de la firma.</param>
        /// <exception cref="ArgumentException">Cuando se proporciona un parámetro no válido.</exception>
        /// <exception cref="HttpForbiddenException">Cuando falla la autenticación con el componente central.</exception>
        /// <exception cref="HttpNetworkException">Cuando se produce un error de conexión con el componente central.</exception>
        /// <exception cref="HttpOperationException">Cuando se produce un error interno del servidor.</exception>
        /// <exception cref="ConfigureException">Cuando no se encuentra configurada la URL del servicio.</exception>
        public static void addDocumentToBatch(
                              String appId,
                              String transactionId,
                              String documentId,
                              String documentB64,
                              String op,
                              String ft,
                              String propB64,
                              String upgrade,
                              String confB64)
        {

            if (string.IsNullOrEmpty(transactionId))
            {
                throw new ArgumentException(
                    "El id de la transaccion no puede ser nulo"
                );
            }
            if (string.IsNullOrEmpty(documentId))
            {
                throw new ArgumentException(
                    "El identificador del documento no puede ser nulo"
                );
            }
            if (string.IsNullOrEmpty(documentB64))
            {
                throw new ArgumentException(
                    "El documento a incluir no puede ser nulo"
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
            string url = ConfigManager.getFireService();

            string urlParameters = URL_PARAMETERS_ADD_CUSTOM_DOCUMENT_BATCH
                .Replace(APP_ID, appId)
                .Replace(TRANSACTION, transactionId)
                .Replace(OP, "6") // El tipo de operacion solicitada es ADD_DOCUMENT_TO_BATCH (6)
                .Replace(DOCID, documentId)
                .Replace(DATA, documentB64.Replace('+', '-').Replace('/', '_'))
                .Replace(COP, op)
                .Replace(FORMAT, ft)
                .Replace(PROP, string.IsNullOrEmpty(propB64) ? "" : propB64.Replace('+', '-').Replace('/', '_'))
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
            getResponseToPostPetition(url, urlParameters);
        }

        private static readonly String URL_PARAMETERS_SIGN_BATCH =
            "appid=" + APP_ID +
            "&transactionid=" + TRANSACTION +
            "&op=" + OP +
            "&stoponerror=" + STOPONERROR;

        /// <summary>
        /// Realiza una firma por lotes.
        /// </summary>
        /// <param name="appId">Identificador de la aplicación.</param>
        /// <param name="transactionId">Identificador de la transacción.</param>
        /// <param name="stopOnError">Indicador de si debe detenerse al producirse un error en la firma.</param>
        /// <returns>URL de redirección.</returns>
        /// <exception cref="ArgumentException">Cuando se proporciona un parámetro no válido.</exception>
        /// <exception cref="HttpForbiddenException">Cuando falla la autenticación con el componente central.</exception>
        /// <exception cref="HttpNetworkException">Cuando se produce un error de conexión con el componente central.</exception>
        /// <exception cref="HttpOperationException">Cuando se produce un error interno del servidor.</exception>
        /// <exception cref="ConfigureException">Cuando no se encuentra configurada la URL del servicio.</exception>
        public static FireLoadResult signBatch(
                              String appId,
                              String transactionId,
                              bool stopOnError)
        {

            if (string.IsNullOrEmpty(transactionId))
            {
                throw new ArgumentException(
                    "El id de la transaccion no puede ser nulo"
                );
            }

            string url = ConfigManager.getFireService();

            string urlParameters = URL_PARAMETERS_SIGN_BATCH
                .Replace(APP_ID, appId)
                .Replace(TRANSACTION, transactionId)
                .Replace(OP, "7") // El tipo de operacion solicitada es SIGN_BATCH (7)
                .Replace(STOPONERROR, stopOnError.ToString());

            //  realizamos la peticion post al servicio y recibimos los datos de la peticion
            byte[] bytes = getResponseToPostPetition(url, urlParameters);
            // Mostramos los datos obtenidos
            return new FireLoadResult(System.Text.Encoding.UTF8.GetString(bytes));
        }


        private static readonly String URL_PARAMETERS_RECOVER_BATCH_RESULT =
            "appid=" + APP_ID +
            "&transactionid=" + TRANSACTION +
            "&op=" + OP;

        /// <summary>
        /// Recupera la firma de los datos haciendo uso del servicio de red de firma en la nube.
        /// </summary>
        /// <param name="appId">Identificador de la aplicación.</param>
        /// <param name="transactionId">Identificador de la transacción.</param>
        /// <returns>Firma realizada en servidor.</returns>
        /// <exception cref="ArgumentException">Cuando se proporciona un parámetro no válido.</exception>
        /// <exception cref="HttpForbiddenException">Cuando falla la autenticación con el componente central.</exception>
        /// <exception cref="HttpNetworkException">Cuando se produce un error de conexión con el componente central.</exception>
        /// <exception cref="HttpOperationException">Cuando se produce un error interno del servidor.</exception>
        /// <exception cref="ConfigureException">Cuando no se encuentra configurada la URL del servicio.</exception>
        public static FireBatchResult recoverBatchResult(
                              String appId,
                              String transactionId)
        {

            if (string.IsNullOrEmpty(transactionId))
            {
                throw new ArgumentException(
                    "El id de la transaccion no puede ser nulo"
                );
            }

            string url = ConfigManager.getFireService();
            string urlParameters = URL_PARAMETERS_RECOVER_BATCH_RESULT
                .Replace(APP_ID, appId)
                .Replace(TRANSACTION, transactionId)
                .Replace(OP, "8"); // El tipo de operacion solicitada es RECOVER_BATCH (8)

            //  realizamos la peticion post al servicio y recibimos los datos de la peticion
            byte[] bytes = getResponseToPostPetition(url, urlParameters);
            BatchResultJson batchResult = getJson(System.Text.Encoding.UTF8.GetString(bytes));
            
            return FireBatchResult.Parse(batchResult);
        }

        /// <summary>
        /// Recupera el progreso del proceso de firma de los datos del proceso batch.
        /// </summary>
        /// <param name="appId">Identificador de la aplicación.</param>
        /// <param name="transactionId">Identificador de la transacción.</param>
        /// <returns>Progreso de la firma.</returns>
        /// <exception cref="ArgumentException">Cuando se proporciona un parámetro no válido.</exception>
        /// <exception cref="HttpForbiddenException">Cuando falla la autenticación con el componente central.</exception>
        /// <exception cref="HttpNetworkException">Cuando se produce un error de conexión con el componente central.</exception>
        /// <exception cref="HttpOperationException">Cuando se produce un error interno del servidor.</exception>
        /// <exception cref="ConfigureException">Cuando no se encuentra configurada la URL del servicio.</exception>
        public static float recoverBatchResultState(
                              String appId,
                              String transactionId)
        {

            if (string.IsNullOrEmpty(transactionId))
            {
                throw new ArgumentException(
                    "El id de la transaccion no puede ser nulo"
                );
            }

            // La peticion de recuperacion del estado y del resultado es igual salvo por el ID de operacion
            string url = ConfigManager.getFireService();
            string urlParameters = URL_PARAMETERS_RECOVER_BATCH_RESULT
                .Replace(APP_ID, appId)
                .Replace(TRANSACTION, transactionId)
                .Replace(OP, "9"); // El tipo de operacion solicitada es RECOVER_BATCH_STATE (9)

            //  realizamos la peticion post al servicio y recibimos los datos de la peticion
            byte[] bytes = getResponseToPostPetition(url, urlParameters);
            
            // Mostramos los datos obtenidos
            return float.Parse(System.Text.Encoding.UTF8.GetString(bytes), System.Globalization.CultureInfo.InvariantCulture);
        }

        private static readonly String URL_PARAMETERS_RECOVER_BATCH_SIGN =
            "appid=" + APP_ID +
            "&transactionid=" + TRANSACTION +
            "&docid=" + DOCID +
            "&op=" + OP;

        /// <summary>
        /// Recupera la firma de un documento del proceso batch haciendo uso del servicio de red de firma en la nube.
        /// </summary>
        /// <param name="appId">Identificador de la aplicación.</param>
        /// <param name="transactionId">Identificador de la transacción.</param>
        /// <param name="docId">Identificador del documento seleccionado.</param>
        /// <returns>Firma del documento realizada en servidor.</returns>
        /// <exception cref="ArgumentException">Cuando se proporciona un parámetro no válido.</exception>
        /// <exception cref="HttpForbiddenException">Cuando falla la autenticación con el componente central.</exception>
        /// <exception cref="HttpNetworkException">Cuando se produce un error de conexión con el componente central.</exception>
        /// <exception cref="HttpOperationException">Cuando se produce un error interno del servidor.</exception>
        /// <exception cref="ConfigureException">Cuando no se encuentra configurada la URL del servicio.</exception>
        public static FireTransactionResult recoverBatchSign(
                              String appId,
                              String transactionId,
                              String docId)
        {

            if (string.IsNullOrEmpty(transactionId))
            {
                throw new ArgumentException(
                    "El id de la transaccion no puede ser nulo"
                );
            }
            if (string.IsNullOrEmpty(docId))
            {
                throw new ArgumentException(
                    "El identificador del documento no puede ser nulo"
                );
            }

            string url = ConfigManager.getFireService();
            string urlParameters = URL_PARAMETERS_RECOVER_BATCH_SIGN
                .Replace(APP_ID, appId)
                .Replace(DOCID, docId)
                .Replace(TRANSACTION, transactionId)
                .Replace(OP, "10"); // El tipo de operacion solicitada es RECOVER_BATCH_SIGN (10)

            //  realizamos la peticion post al servicio y recibimos los datos de la peticion
            byte[] bytes = getResponseToPostPetition(url, urlParameters);
            
            // Devolvemos la respuesta
            try {
                return new FireTransactionResult(bytes);
            }
            catch (Exception e)
            {
                throw new HttpOperationException("La respuesta del servicio no tiene un formato valido", e);
            }
        }

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


        /// <summary>
        /// Recupera una firma enviada a generar anteriormente y para la que se solicitó esperar un periodo de gracia.
        /// </summary>
        /// <param name="appId">Identificador de la aplicación.</param>
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
        public static FireTransactionResult recoverAsyncSign(
                              string appId,
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

            string url = ConfigManager.getFireService();
            string urlParameters = URL_PARAMETERS_RECOVER_ASYNC_SIGN
                .Replace(APP_ID, appId)
                .Replace(DOCID, docId)
                .Replace(OP, "70")
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
            byte[] bytes = getResponseToPostPetition(url, urlParameters);

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
                .Replace(APP_ID, appId)
                .Replace(DOCID, docId)
                .Replace(OP, "71"); // El tipo de operacion solicitada es RECOVER_ASYNC_SIGN_RESULT

            //  Realizamos la peticion al servicio y recibimos los datos de la peticion
            bytes = getResponseToPostPetition(url, urlParameters);
            result.Result = bytes;

            return result;
        }

        /// <summary>
        /// Realiza una peticion POST y devuelve un byte array con la respuesta.
        /// </summary>
        /// <param name="url">URL a la que realizar la petición.</param>
        /// <param name="urlParameters">Parámetros que se envían en la petición.</param>
        /// <returns>Respuesta de la llamada a la URL indicada.</returns>
		/// <exception cref="ArgumentException">Cuando se proporciona un parámetro no válido.</exception>
        /// <exception cref="HttpForbiddenException">Cuando falla la autenticación con el componente central.</exception>
        /// <exception cref="HttpNetworkException">Cuando se produce un error de conexión con el componente central.</exception>
        /// <exception cref="HttpOperationException">Cuando se produce un error interno del servidor.</exception>
        /// <exception cref="ConfigureException">Cuando no se encuentra configurada la URL del servicio.</exception>
        private static byte[] getResponseToPostPetition(string url, string urlParameters)
        {
            try
            {
                // generamos la respuesta del servidor
                HttpWebResponse response = ConnectionManager.connectByPost(url, urlParameters, null);
                // recibimos el stream de la respuesta
                Stream dataStream = response.GetResponseStream();
                MemoryStream ms = new MemoryStream();
                dataStream.CopyTo(ms);
                byte[] bytes = ms.ToArray();
                // Cerramos los streams
                response.Close();
                ms.Close();
                dataStream.Close();

                return bytes;
            }
            catch (WebException e)
            {
                HttpWebResponse r = (HttpWebResponse)e.Response;
                if ((r != null && r.StatusCode == HttpStatusCode.Forbidden) || e.GetBaseException() is AuthenticationException)
                {
                    throw new HttpForbiddenException("Error durante la autenticacion de la aplicacion", e);
                }
                else if (r != null && r.StatusCode == HttpStatusCode.InternalServerError)
                {
                    throw new HttpForbiddenException("Error durante la operacion de firma", e);
                }
                throw new HttpNetworkException("Error al realizar la conexion: " + e.Message, e);
            }
            catch (ProtocolViolationException e)
            {
                throw new HttpNetworkException("Ha ocurrido un problema con el protocolo :" + e.Message, e);
            }
            catch (InvalidOperationException e)
            {
                throw new HttpNetworkException("Operacion no permitida :" + e.Message, e);
            }
            catch (IOException e)
            {
                throw new HttpOperationException("Error al leer la respuesta del servidor: " + e.Message, e);
            }
            catch (Exception e) {
                throw new HttpOperationException(e.Message, e);
            }
        }

        /// <summary>
        ///  Devuelve un conjunto de propiedades extraídas de un JSON.
        /// </summary>
        /// <param name="JSON">Cadena en formato JSON que se desea analizar.</param>
        /// <returns>Objeto con el resultado de la firma del lote.</returns>
        private static BatchResultJson getJson(string JSON)
        {
            var json_serializer = new JavaScriptSerializer();
            return json_serializer.Deserialize<BatchResultJson>(JSON);
        }
    }
}
