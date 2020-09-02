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
using Microsoft.Win32;

namespace FIRe
{
    /// <summary>
    /// Clase para la carga de datos a firmar. Esta clase obtiene la URL del
    /// servicio al que debe atacar para la carga de datos a través del registro.
    /// </summary>
    public class HttpLoadProcess
    {
        private static readonly String TAG_NAME_APP_ID = "$$APPID$$";
        private static readonly String TAG_NAME_CLAVEFIRMA_CONFIG = "$$CONFIG$$";
        private static readonly String TAG_NAME_ALGORITHM = "$$ALGORITHM$$";
        private static readonly String TAG_NAME_SUBJECT_ID = "$$SUBJECTID$$";
        private static readonly String TAG_NAME_CERT = "$$CERTIFICATE$$";
        private static readonly String TAG_NAME_EXTRA_PARAM = "$$EXTRAPARAMS$$";
        private static readonly String TAG_NAME_OPERATION = "$$SUBOPERATION$$";
        private static readonly String TAG_NAME_FORMAT = "$$FORMAT$$";
        private static readonly String TAG_NAME_DATA = "$$DATA$$";

        private static readonly String URL_PARAMETERS =
            "appId=" + TAG_NAME_APP_ID +
            "&config=" + TAG_NAME_CLAVEFIRMA_CONFIG +
            "&algorithm=" + TAG_NAME_ALGORITHM +
            "&subjectId=" + TAG_NAME_SUBJECT_ID +
            "&cert=" + TAG_NAME_CERT +
            "&properties=" + TAG_NAME_EXTRA_PARAM +
            "&operation=" + TAG_NAME_OPERATION +
            "&format=" + TAG_NAME_FORMAT +
            "&dat=" + TAG_NAME_DATA;

        /// <summary>Carga datos para ser posteriormente firmados usando el proveedor por defecto.</summary>
        /// <param name="appId">Identificador de la aplicación.</param>
        /// <param name="subjectId">Identificador del titular de la clave de firma.</param>
        /// <param name="op">Tipo de operación a realizar: firma, cofirma o contrafirma</param>
        /// <param name="ft">Formato de la operación.</param>
        /// <param name="algth">Algoritmo de firma.</param>
        /// <param name="propB64">Propiedades extra a añadir a la firma (puede ser <code>null</code>).</param>
        /// <param name="certB64">Certificado de usuario para realizar la firma.</param>
        /// <param name="dataB64">Datos a firmar.</param>
        /// <param name="confB64">Configuración a indicar al servicio remoto (dependiente de la implementación).</param>
        /// <returns>Resultado de la carga.</returns>
        /// <exception cref="ArgumentException">Cuando se proporciona un parámetro no válido.</exception>
        /// <exception cref="HttpForbiddenException">Cuando falla la autenticación con el componente central.</exception>
        /// <exception cref="HttpNetworkException">Cuando se produce un error de conexión con el componente central.</exception>
        /// <exception cref="HttpOperationException">Cuando se produce un error interno del servidor.</exception>
        /// <exception cref="ConfigureException">Cuando no se encuentra configurada la URL del servicio.</exception>
        public static LoadResult loadData(
                                            String appId,
                                            String subjectId,
                                            String op,
                                            String ft,
                                            String algth,
                                            String propB64,
                                            String certB64,
                                            String dataB64,
                                            String confB64)
        {
            return loadData(appId, subjectId, op, ft, algth, propB64, certB64, dataB64, confB64, null);
        }

        /// <summary>Carga datos para ser posteriormente firmados con un proveedor de firma en la nube.</summary>
        /// <param name="appId">Identificador de la aplicación.</param>
        /// <param name="subjectId">Identificador del titular de la clave de firma.</param>
        /// <param name="op">Tipo de operación a realizar: firma, cofirma o contrafirma</param>
        /// <param name="ft">Formato de la operación.</param>
        /// <param name="algth">Algoritmo de firma.</param>
        /// <param name="propB64">Propiedades extra a añadir a la firma (puede ser <code>null</code>).</param>
        /// <param name="certB64">Certificado de usuario para realizar la firma.</param>
        /// <param name="dataB64">Datos a firmar.</param>
        /// <param name="confB64">Configuración a indicar al servicio remoto (dependiente de la implementación).</param>
        /// <param name="providerName">Nombre del proveedor de firma en la nube a utilizar.</param>
        /// <returns>Resultado de la carga.</returns>
        /// <exception cref="ArgumentException">Cuando se proporciona un parámetro no válido.</exception>
        /// <exception cref="HttpForbiddenException">Cuando falla la autenticación con el componente central.</exception>
        /// <exception cref="HttpNetworkException">Cuando se produce un error de conexión con el componente central.</exception>
        /// <exception cref="HttpOperationException">Cuando se produce un error interno del servidor.</exception>
        /// <exception cref="ConfigureException">Cuando no se encuentra configurada la URL del servicio.</exception>
        public static LoadResult loadData(
                                        string appId,
                                        string subjectId,
                                        string op,
                                        string ft,
                                        string algth,
                                        string propB64,
                                        string certB64,
                                        string dataB64,
                                        string confB64,
                                        string providerName)
        {

            if (string.IsNullOrEmpty(subjectId))
            {
                throw new ArgumentException(
                    "El identificador del usuario no puede ser nulo"
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
            if (string.IsNullOrEmpty(certB64))
            {
                throw new ArgumentException(
                    "El certificado del firmante no puede ser nulo"
                );
            }
            if (string.IsNullOrEmpty(dataB64))
            {
                throw new ArgumentException(
                    "Los datos a firmar no pueden ser nulos"
                );
            }

            string url = ConfigManager.getUrlLoadService();
            string urlParameters = URL_PARAMETERS
                .Replace(TAG_NAME_APP_ID, appId)
                .Replace(TAG_NAME_SUBJECT_ID, subjectId)
                .Replace(TAG_NAME_OPERATION, op)
                .Replace(TAG_NAME_FORMAT, ft)
                .Replace(TAG_NAME_ALGORITHM, algth)
                .Replace(TAG_NAME_EXTRA_PARAM, propB64 != null ? propB64.Replace('+', '-').Replace('/', '_') : "")
                .Replace(TAG_NAME_CERT, certB64.Replace('+', '-').Replace('/', '_'))
                .Replace(TAG_NAME_DATA, dataB64.Replace('+', '-').Replace('/', '_'))
                .Replace(TAG_NAME_CLAVEFIRMA_CONFIG, confB64 != null ? confB64.Replace('+', '-').Replace('/', '_') : "");

            if (!string.IsNullOrEmpty(providerName))
            {
                urlParameters += "&certorigin=" + providerName;
            }

            string responseJSON = getResponseToPostPetition(url, urlParameters);
            return new LoadResult(responseJSON);
        }

        /// <summary>
        /// Realiza una peticion POST y devuelve un byte array con la respuesta.
        /// </summary>
        /// <param name="url">URL a la que realizar la petición.</param>
        /// <param name="urlParameters">Parámetros que se envían en la petición.</param>
        /// <returns>JSON en un String con la respuesta del servidor</returns>
        /// <exception cref="ArgumentException">Cuando se proporciona un parámetro no válido.</exception>
        /// <exception cref="HttpForbiddenException">Cuando falla la autenticación con el componente central.</exception>
        /// <exception cref="HttpNetworkException">Cuando se produce un error de conexión con el componente central.</exception>
        /// <exception cref="HttpOperationException">Cuando se produce un error interno del servidor.</exception>
        /// <exception cref="ConfigureException">Cuando no se encuentra configurada la URL del servicio.</exception>
        private static string getResponseToPostPetition(string url, string urlParameters)
        {
            try
            {
                // Generamos la respuesta del servidor
                HttpWebResponse response = ConnectionManager.connectByPost(url, urlParameters, null);
                // Recibimos el stream de la respuesta
                Stream dataStream = response.GetResponseStream();
                // Abrimos el stream
                StreamReader reader = new StreamReader(dataStream);
                // Leemos la respuesta del servidor
                string responseFromServer = reader.ReadToEnd();
                // Cerramos los stream que tenemos abierto
                dataStream.Close();
                reader.Close();
                response.Close();
                return responseFromServer;
            }
            catch (WebException e)
            {
                HttpWebResponse r = (HttpWebResponse)e.Response;

                if ((r != null && r.StatusCode == HttpStatusCode.Forbidden) || e.GetBaseException() is AuthenticationException)
                {
                    throw new HttpForbiddenException("Error durante la autenticacion de la aplicacion", e);
                }
                else if (r != null && r.StatusCode.ToString() == HttpCustomErrors.NO_USER)
                {
                    throw new HttpNoUserException("El usuario no esta dado de alta en el sistema", e);
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
