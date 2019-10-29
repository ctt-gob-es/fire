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
using System.Diagnostics;
using System.Net;
using System.Net.Security;
using System.Security.Cryptography.X509Certificates;
using System.Text;

namespace FIRe
{
    /// <summary>Clase para la codificación de datos binarios en cadenas Base64 y viceversa.</summary>
   public static class ConnectionManager
    {

        // Valores de acceso al registro para la configuracion del servicio
        private static readonly string REGISTRY_KEY_CLAVEFIRMA = "SOFTWARE\\ClaveFirma";
        private static readonly string REGISTRY_KEY_FIRE = "SOFTWARE\\FIRe";
        private static readonly string REGISTRY_VALUE_ADMIT_ALL_CERTS = "admit_all_certs";
        private static readonly string REGISTRY_VALUE_SSL_CLIENT_PKCS12 = "ssl_client_pkcs12";
        private static readonly string REGISTRY_VALUE_SSL_CLIENT_PASS = "ssl_client_pass";
        private static readonly string REGISTRY_VALUE_SSL_CLIENT_ALIAS = "ssl_client_alias";
        
        private static readonly string URL_PARAMENTERS_SEPARATOR = "?";

        private static TraceSource log = new TraceSource("es.gob.fire");

        private static bool currentAdmitAllCert = false;

        /// <summary>Realiza una llamada HTTP/HTTPS vía GET y devuelve el resultado.</summary>
        /// <param name="url">URL a la que realizar la petición.</param>
        /// <param name="urlParameters">Parámetros que se envían en la petición.</param>
        /// <param name="config">Configuración para la conexión de red.</param>
        /// <returns>Respuesta de la llamada.</returns>
        public static HttpWebResponse connectByGet(string url, string urlParameters, Dictionary<String, String> config)
        {
            init(config);

            HttpWebRequest request;
            if (string.IsNullOrEmpty(url))
            {
                request = (HttpWebRequest)WebRequest.Create(url);
            }
            else
            {
                StringBuilder buffer = new StringBuilder(url, url.Length + urlParameters.Length + 1);
                buffer.Append(URL_PARAMENTERS_SEPARATOR);
                buffer.Append(urlParameters);

                request = (HttpWebRequest)WebRequest.Create(buffer.ToString());
            }
            request.Method = "GET";
            request.ContentType = "application/x-www-form-urlencoded";
            setClientCerts(request, config);
            
            return (HttpWebResponse)request.GetResponse();
        }

        /// <summary>Realiza una llamada HTTP/HTTPS vía POST y devuelve el resultado.</summary>
        /// <param name="url">URL a la que realizar la petición.</param>
        /// <param name="urlParameters">Parámetros que se envían en la petición.</param>
        /// <param name="config">Configuración para la conexión de red.</param>
        /// <returns>Respuesta de la llamada.</returns>
        public static HttpWebResponse connectByPost(string url, string urlParameters, Dictionary<String, String> config)
        {
            init(config);

            HttpWebRequest request = (HttpWebRequest)WebRequest.Create(url);
            request.Method = "POST";
            request.ContentType = "application/x-www-form-urlencoded";
            setClientCerts(request, config);
            
            if (!string.IsNullOrEmpty(urlParameters))
            {
                var data = Encoding.ASCII.GetBytes(urlParameters);
                request.ContentLength = data.Length;
                using (var stream = request.GetRequestStream())
                {
                    stream.Write(data, 0, data.Length);
                }
            }

            return (HttpWebResponse)request.GetResponse();
        }

        private static void init(Dictionary<String, String> config)
        {
            bool admitAllCert = wantAdmitAllCert(config);
            if (admitAllCert == currentAdmitAllCert)
            {
                return;
            }

            if (admitAllCert)
            {
                ServicePointManager.ServerCertificateValidationCallback =
                    delegate (object s, X509Certificate certificate, X509Chain chain, SslPolicyErrors sslPolicyErrors)
                    { return true; };
            }
            else
            {
                ServicePointManager.ServerCertificateValidationCallback = null;
            }
            currentAdmitAllCert = admitAllCert;
        }

        /// <summary>Indica si se debe admitir cualquier certificado SSL servidor.</summary>
        /// <returns>true cuando se deba admitir cualquier certificado SSL servidor, false en caso contrario.</returns>
        private static bool wantAdmitAllCert(Dictionary<String, String> config)
        {
            try
            {
                string admitAll = recoverConfigValue(REGISTRY_VALUE_ADMIT_ALL_CERTS, config);
                return bool.Parse(admitAll);
            }
            catch (Exception e)
            {
                log.TraceData(TraceEventType.Warning, 1, "No se ha podido identificar si admitir todos los certificados SSL. Por defecto, no se admitiran: " + e.ToString());
                return false;
            }
        }

        private static void setClientCerts(HttpWebRequest request, Dictionary<String, String> config)
        {
            string sslClientPkcs12 = null;
            string sslClientPass = null;
            string sslClientAlias = null;
            try
            {
                sslClientPkcs12 = recoverConfigValue(REGISTRY_VALUE_SSL_CLIENT_PKCS12, config);
                sslClientPass = recoverConfigValue(REGISTRY_VALUE_SSL_CLIENT_PASS, config);
                sslClientAlias = recoverConfigValue(REGISTRY_VALUE_SSL_CLIENT_ALIAS, config);
            }
            catch (Exception e)
            {
                log.TraceData(TraceEventType.Warning, 1, "No se ha encontrado un certificado cliente SSL configurado en el registro: " + e.ToString());
                return;
            }

            if (!string.IsNullOrEmpty(sslClientPkcs12) && !string.IsNullOrEmpty(sslClientPass))
            {
                try
                {
                    X509Certificate2Collection certCollection = new X509Certificate2Collection();
                    certCollection.Import(@sslClientPkcs12, sslClientPass, X509KeyStorageFlags.MachineKeySet);

                    X509Certificate2 sslClientCert = null;
                    if (sslClientAlias != null)
                    {
                        foreach (var certx in certCollection)
                        {
                            if (sslClientAlias.Equals(certx.FriendlyName))
                            {
                                sslClientCert = certx;
                            }
                        }
                    }
                    
                    if (sslClientCert == null)
                    {
                        if (sslClientAlias != null)
                        {
                            log.TraceData(TraceEventType.Warning, 1, "El certificado con el alias indicado no esta en el almacen. Se usara el primer certificado");
                        }
                        sslClientCert = certCollection[0];
                    }

                    request.ClientCertificates.Add(sslClientCert);
                }
                catch (Exception e)
                {
                    log.TraceData(TraceEventType.Warning, 1, "Error al configurar el certificado de cliente SSL: " + e.ToString());
                }
            }
        }

        private static String recoverConfigValue(String key, Dictionary<String, String> config)
        {
            return config != null && config.ContainsKey(key) && !string.IsNullOrEmpty(config[key]) ? config[key] : getRegistryKey(key);
        }

        private static string getRegistryKey(string valueName)
        {
            RegistryKey masterKey = Registry.CurrentUser.OpenSubKey(REGISTRY_KEY_FIRE);
            if (masterKey == null)
            {
                masterKey = Registry.CurrentUser.OpenSubKey(REGISTRY_KEY_CLAVEFIRMA);
                if (masterKey == null)
                {
                    throw new ConfigureException("No se ha encontrado la clave de registro '" + REGISTRY_KEY_FIRE);
                }
            }
            string value = (string)masterKey.GetValue(valueName);

            masterKey.Close();

            if (string.IsNullOrEmpty(value))
            {
                throw new ConfigureException("No se ha encontrado el valor de registro " + REGISTRY_KEY_FIRE + "\\" + valueName);
            }

            return value;
        }
    }
}
