/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */

using Microsoft.Win32;
using System;
using System.Collections.Generic;
using System.IO;
using System.Net;
using System.Security.Authentication;
using System.Security.Cryptography.X509Certificates;
using System.Text;
using System.Web.Script.Serialization;


namespace FIRe
{
    /// <summary>
    /// Clase para la solicitud del listado de certificados de un usuario. Esta clase obtiene la URL
    /// del servicio al que debe atacar a través del valor de registro
    /// "CURRENT_USER/SOFTWARE/ClaveFirma/certificates_service".
    /// </summary>
    public static class HttpCertificateList
    {
        // Valores de acceso al registro para la configuracion del servicio

        private static readonly String CERT_JSON_PARAM = "certificates";
        
        private static readonly String ID_TAG_APP = "$$APPID$$";

        private static readonly String ID_TAG_SUBJECT = "$$SUBJECTID$$";

        private static readonly String URL_PARAMETERS =
            "appId=" + ID_TAG_APP +
            "&subjectId=" + ID_TAG_SUBJECT;

        /// <summary>
        ///  Recuperar los certificados del sistema.
        /// </summary>
        /// <param name="appId">Identificador de la aplicación.</param>
        /// <param name="subjectId">Identificador del titular de la clave de firma.</param>
        /// <returns>Listado de certificados</returns>
        /// <exception cref="ArgumentException">Cuando se proporciona un parámetro no válido.</exception>
        /// <exception cref="HttpForbiddenException">Cuando falla la autenticación con el componente central.</exception>
        /// <exception cref="HttpNetworkException">Cuando se produce un error de conexión con el componente central.</exception>
        /// <exception cref="HttpNoUserException">Cuando el usuario no está dado de alta en el sistema.</exception>
        /// <exception cref="HttpCertificateBlockedException">Cuando los certificados del usuario estén bloqueados.</exception>
        /// <exception cref="HttpWeakRegistryException">Cuando el usuario realizó un registro débil y no puede tener certificados de firma.</exception>
        /// <exception cref="HttpOperationException">Cuando se produce un error interno del servidor.</exception>
        /// <exception cref="ConfigureException">Cuando no se encuentra configurada la URL del servicio.</exception>
        public static List<X509Certificate> getList(String appId, String subjectId)
        {
            
            if (string.IsNullOrEmpty(subjectId))
            {
                throw new ArgumentException(
                    "El identificador del usuario no puede ser nulo"
                );
            }

            // Generamos la URL
            string url = ConfigManager.getUrlListCertsService();
            string urlParameters = URL_PARAMETERS
                .Replace(ID_TAG_SUBJECT, subjectId)
                .Replace(ID_TAG_APP, appId);

            List<X509Certificate> certificates = new List<X509Certificate>();
            try
            {
                // generamos la respuesta del servidor
                HttpWebResponse response = ConnectionManager.connectByGet(url, urlParameters, null);
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
                // pasamos la respuesta del servidor a un diccionario <string, string[]>
                Dictionary<string, string[]> certificatesJson = getJson(responseFromServer);
                // comprobamos que esta bien formado el JSON
                if (!certificatesJson.ContainsKey(CERT_JSON_PARAM))
                {
                    throw new HttpOperationException("El json devuelto por el servidor no contiene la clave " + CERT_JSON_PARAM);
                }

                foreach (string[] key in certificatesJson.Values)
                {
                    foreach (string value in key)
                    {
                        certificates.Add(new X509Certificate(new UTF8Encoding(true).GetBytes(value)));
                    }
                }
                return certificates;

            }
            catch (WebException e)
            {
                if (e.GetBaseException() is AuthenticationException)
                {
                    throw new HttpForbiddenException("Error durante la autenticacion de la aplicacion", e);
                }
                HttpWebResponse r = (HttpWebResponse)e.Response;
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
                        // Certificado de firma esta bloqueado
                        case HttpCustomErrors.CERTIFICATE_BLOCKED:
                            throw new HttpCertificateBlockedException("Usuario con certificados bloqueados", e);
                        // El usuario realizo un registro debil y no puede tener certificados de firma
                        case HttpCustomErrors.WEAK_REGISTRY:
                            throw new HttpWeakRegistryException("El usuario realizo un registro debil y no puede tener certificados de firma", e);
                        // El usuario no tiene certificados
                        case HttpCustomErrors.NO_CERTS:
                            return certificates;
                    }
                }
                throw new HttpNetworkException(e.Message, e);
            }
            catch (ProtocolViolationException e)
            {
                throw new HttpNetworkException("Ha ocurrido un problema con el protocolo: " + e.Message, e);
            }
            catch (InvalidOperationException e)
            {
                throw new HttpNetworkException("Operacion no permitida: " + e.Message, e);
            }
            catch (NotSupportedException e)
            {
                throw new HttpNetworkException("Operacion no soportada: " + e.Message, e);
            }
            catch (Exception e)
            {
                throw new HttpOperationException("Error interno del servidor: " + e.Message, e);
            }
        }

        /// <summary>
        ///  Devuelve las propiedades extraidas del JSON proporcionado.
        /// </summary>
        /// <param name="JSON">Cadena en formato JSON.</param>
        /// <returns>Conjunto de propiedades y sus valores.</returns>
        private static Dictionary<string, string[]> getJson(string JSON){
            var json_serializer = new JavaScriptSerializer();
            return json_serializer.Deserialize<Dictionary<string, string[]>>(JSON);      
        }
    }
}
