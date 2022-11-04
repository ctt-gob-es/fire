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
using System.IO;
using System.Security.Authentication;
using System.Security.Cryptography;
using System.Security.Cryptography.X509Certificates;

namespace FIRe
{
    /// <summary>
    /// Clase para la generación y recuperación de certificados de firma. Esta clase obtiene la URL
    /// del servicio de generación de certificado y recuperación del certificado generado del registro.
    /// </summary>
    public class HttpGenerateCertificate
    {
        private static readonly String TAG_NAME_APP_ID = "$$APPID$$";
        private static readonly String TAG_NAME_CLAVEFIRMA_CONFIG = "$$CONFIG$$";
        private static readonly String TAG_NAME_SUBJECT_ID = "$$SUBJECTID$$";
        private static readonly String TAG_NAME_TRANSACTION_ID = "$$TRANSACTION$$";

        private static readonly String URL_GENERATE_PARAMENTERS =
            "appId=" + TAG_NAME_APP_ID +
            "&subjectId=" + TAG_NAME_SUBJECT_ID +
            "&config=" + TAG_NAME_CLAVEFIRMA_CONFIG;

        private static readonly String URL_RECOVER_PARAMENTERS =
            "appId=" + TAG_NAME_APP_ID +
            "&transactionId=" + TAG_NAME_TRANSACTION_ID;


        /// <summary>Genera un nuevo certificado de firma para el proveedor de firma en la nube por defecto.</summary>
        /// <param name="appId">Identificador de la aplicación.</param>
        /// <param name="subjectId">Identificador del titular al que generar el certificado.</param>
        /// <param name="confB64">Configuración a indicar al servicio remoto (dependiente de la implementación).</param>
        /// <returns>Resultado del proceso de generación.</returns>
        /// <exception cref="ArgumentException">Cuando se proporciona un parámetro no válido o la repuesta del servidor
        /// no es correcta.</exception>
        /// <exception cref="HttpForbiddenException">Cuando falla la autenticación con el componente central.</exception>
        /// <exception cref="HttpNetworkException">Cuando se produce un error de conexión con el componente central.</exception>
        /// <exception cref="HttpCertificateAvailableException">Cuando se solicita crear una certificado para un usuario que ya tiene.</exception>
        /// <exception cref="HttpNoUserException">Cuando el usuario no está dado de alta en el sistema.</exception>
        /// <exception cref="HttpWeakRegistryException">Cuando el usuario realizó un registro débil y no puede tener certificados de firma.</exception>
        /// <exception cref="HttpOperationException">Cuando se produce un error interno del servidor.</exception>
        /// <exception cref="ConfigureException">Cuando no se encuentra configurada la URL del servicio.</exception>
        public static GenerateCertificateResult generateCertificate(
                                            string appId,
                                            string subjectId,
                                            string confB64)
        {
            return generateCertificate(appId, subjectId, confB64, null);
        }
            /// <summary>Genera un nuevo certificado de firma para un proveedor de firma en la nube.</summary>
            /// <param name="appId">Identificador de la aplicación.</param>
            /// <param name="subjectId">Identificador del titular al que generar el certificado.</param>
            /// <param name="confB64">Configuración a indicar al servicio remoto (dependiente de la implementación).</param>
            /// <param name="providerName">Nombre del proveedor de firma en la nube.</param>
            /// <returns>Resultado del proceso de generación.</returns>
            /// <exception cref="ArgumentException">Cuando se proporciona un parámetro no válido o la repuesta del servidor
            /// no es correcta.</exception>
            /// <exception cref="HttpForbiddenException">Cuando falla la autenticación con el componente central.</exception>
            /// <exception cref="HttpNetworkException">Cuando se produce un error de conexión con el componente central.</exception>
            /// <exception cref="HttpCertificateAvailableException">Cuando se solicita crear una certificado para un usuario que ya tiene.</exception>
            /// <exception cref="HttpNoUserException">Cuando el usuario no está dado de alta en el sistema.</exception>
            /// <exception cref="HttpWeakRegistryException">Cuando el usuario realizó un registro débil y no puede tener certificados de firma.</exception>
            /// <exception cref="HttpOperationException">Cuando se produce un error interno del servidor.</exception>
            /// <exception cref="ConfigureException">Cuando no se encuentra configurada la URL del servicio.</exception>
            public static GenerateCertificateResult generateCertificate(
                                            string appId,
                                            string subjectId,
                                            string confB64,
                                            string providerName)
        {

            if (string.IsNullOrEmpty(subjectId))
            {
                throw new ArgumentException(
                    "El identificador del usuario no puede ser nulo"
                );
            }

            string url = ConfigManager.getUrlGenerateCertService();
            string urlParameters = URL_GENERATE_PARAMENTERS
                .Replace(TAG_NAME_APP_ID, appId)
                .Replace(TAG_NAME_SUBJECT_ID, subjectId)
                .Replace(TAG_NAME_CLAVEFIRMA_CONFIG, confB64 != null ? confB64.Replace('+', '-').Replace('/', '_') : "");

            if (!string.IsNullOrEmpty(providerName))
            {
                urlParameters += "&certorigin=" + providerName;
            }

            string responseJSON = getResponseToPostPetition(url, urlParameters);
            return new GenerateCertificateResult(responseJSON);
        }

        /// <summary>Carga datos para ser posteriormente firmados con le proveedor de firma en la nube por defecto.</summary>
        /// <param name="appId">Identificador de la aplicación.</param>
        /// <param name="transactionId">Identificador de la transacción en la que se generó el certificado.</param>
        /// <returns>Resultado de la carga.</returns>
        /// <exception cref="ArgumentException">Cuando se proporciona un parámetro no válido.</exception>
        /// <exception cref="HttpForbiddenException">Cuando falla la autenticación con el componente central.</exception>
        /// <exception cref="HttpNetworkException">Cuando se produce un error de conexión con el componente central.</exception>
        /// <exception cref="HttpOperationException">Cuando se produce un error interno del servidor.</exception>
        /// <exception cref="CryptographicException">Cuando no se puede decodificar el certificado recuperado.</exception>
		/// <exception cref="ConfigureException">Cuando no se encuentra configurada la URL del servicio.</exception>
        public static X509Certificate recoverCertificate(
                                            string appId,
                                            string transactionId)
        {
            return recoverCertificate(appId, transactionId, null);
        }
        /// <summary>Carga datos para ser posteriormente firmados con un proveedor de firma en la nube.</summary>
        /// <param name="appId">Identificador de la aplicación.</param>
        /// <param name="transactionId">Identificador de la transacción en la que se generó el certificado.</param>
        /// <param name="providerName">Nombre del proveedor de firma en la nube.</param>
        /// <returns>Resultado de la carga.</returns>
        /// <exception cref="ArgumentException">Cuando se proporciona un parámetro no válido.</exception>
        /// <exception cref="HttpForbiddenException">Cuando falla la autenticación con el componente central.</exception>
        /// <exception cref="HttpNetworkException">Cuando se produce un error de conexión con el componente central.</exception>
        /// <exception cref="HttpOperationException">Cuando se produce un error interno del servidor.</exception>
        /// <exception cref="CryptographicException">Cuando no se puede decodificar el certificado recuperado.</exception>
        /// <exception cref="ConfigureException">Cuando no se encuentra configurada la URL del servicio.</exception>
        public static X509Certificate recoverCertificate(
                                        string appId,
                                        string transactionId,
                                        string providerName)
        {

            if (string.IsNullOrEmpty(transactionId))
            {
                throw new ArgumentException(
                    "El identificador de transaccion no puede ser nulo"
                );
            }

            string url = ConfigManager.getUrlRecoverCertService();
            string urlParameters = URL_RECOVER_PARAMENTERS
                .Replace(TAG_NAME_APP_ID, appId)
                .Replace(TAG_NAME_TRANSACTION_ID, transactionId);

            if (!string.IsNullOrEmpty(providerName))
            {
                urlParameters += "&certorigin=" + providerName;
            }

            var certEncoded = getResponseToGetPetition(url, urlParameters);

            return new X509Certificate(certEncoded);
        }
        
        /// <summary>
        /// Realiza una peticion POST y devuelve una cadena con el JSON de la respuesta.
        /// </summary>
        /// <param name="url">URL a la que realizar la petición.</param>
        /// <param name="urlParameters">Parámetros que se envían en la petición.</param>
        /// <returns>JSON en un String con la respuesta del servidor</returns>
        /// <exception cref="HttpForbiddenException">Cuando falla la autenticación con el componente central.</exception>
        /// <exception cref="HttpNetworkException">Cuando se produce un error de conexión con el componente central.</exception>
        /// <exception cref="HttpOperationException">Cuando se produce un error interno del servidor.</exception>
        /// <exception cref="HttpCertificateAvailableException">Cuando se solicita crear una certificado para un usuario que ya tiene.</exception>
        /// <exception cref="HttpNoUserException">Cuando el usuario no está dado de alta en el sistema.</exception>
        /// <exception cref="HttpWeakRegistryException">Cuando el usuario realizó un registro débil y no puede tener certificados de firma.</exception>
        private static string getResponseToPostPetition(string url, string urlParameters)
        {
            try
            {
                // generamos la respuesta del servidor
                HttpWebResponse response = ConnectionManager.ConnectByPost(url, urlParameters, null);
                // recibimos el stream de la respuesta
                Stream dataStream = response.GetResponseStream();
                // abrimos el stream
                StreamReader reader = new StreamReader(dataStream);
                // leemos la respuesta del servidor
                string responseFromServer = reader.ReadToEnd();
                // Cerramos los stream que tenemos abierto
                dataStream.Close();
                reader.Close();
                response.Close();
                return responseFromServer;
            }
            catch (WebException e)
            {
                if (e.GetBaseException() is AuthenticationException)
                {
                    throw new HttpForbiddenException("Error durante la autenticacion de la aplicacion", e);
                }
                HttpWebResponse r = (HttpWebResponse) e.Response;
                if (r != null)
                {
                    if (r.StatusCode == HttpStatusCode.Forbidden)
                    {
                        throw new HttpForbiddenException("Error durante la autenticacion de la aplicacion", e);
                    }
                    if (r.StatusCode == HttpStatusCode.RequestTimeout)
                    {
                        throw new HttpNetworkException("Error en la conexion", e);
                    }

                    switch (r.StatusCode.ToString())
                    {
                        // Usuario no valido
                        case HttpCustomErrors.NO_USER:
                            throw new HttpNoUserException("Usuario no valido", e);
                        // El usuario realizo un registro debil y no puede tener certificados de firma
                        case HttpCustomErrors.WEAK_REGISTRY:
                            throw new HttpWeakRegistryException("El usuario realizo un registro debil y no puede tener certificados de firma", e);
                        // El usuario ya tiene certificados de firma vigentes y no puede crear nuevos
                        case HttpCustomErrors.CERTIFICATE_AVAILABLE:
                            throw new HttpCertificateAvailableException("El usuario ya tiene certificados de firma vigentes", e);
                    }
                }
                throw new HttpNetworkException(e.Message, e);
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
            catch (Exception e)
            {
                throw new HttpOperationException(e.Message, e);
            }

        }

        /// <summary>
        /// Realiza una peticion GET y devuelve un byte array con la respuesta.
        /// </summary>
        /// <param name="url">URL a la que realizar la petición.</param>
        /// <param name="urlParameters">Parámetros que se envían en la petición.</param>
        /// <returns>Certificado codificado.</returns>
        /// <exception cref="HttpOperationException">Cuando falla la autenticación con el componente central.</exception>
        /// <exception cref="HttpNetworkException">Cuando se produce un error de conexión con el componente central.</exception>
        /// <exception cref="HttpOperationException">Cuando se produce un error interno del servidor.</exception>
        /// 
        private static byte[] getResponseToGetPetition(string url, string urlParameters)
        {
            try
            {
                HttpWebResponse response = ConnectionManager.connectByGet(url, urlParameters, null);
                // recibimos el stream de la respuesta
                Stream dataStream = response.GetResponseStream();
                MemoryStream ms = new MemoryStream();
                dataStream.CopyTo(ms);
                byte[] bytes = ms.ToArray();
                // Cerramos los stream que tenemos abierto
                dataStream.Close();
                ms.Close();
                response.Close();
                
                return bytes;
            }
            catch (WebException e)
            {
                HttpWebResponse r = (HttpWebResponse)e.Response;
                if ((r != null && r.StatusCode == HttpStatusCode.Forbidden) || e.GetBaseException() is AuthenticationException)
                {
                    throw new HttpOperationException("Error durante la autenticacion de la aplicacion", e);
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
            catch (Exception e)
            {
                throw new HttpOperationException(e.Message, e);
            }

        }
    }
}
