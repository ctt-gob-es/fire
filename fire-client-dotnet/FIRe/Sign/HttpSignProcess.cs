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
using Microsoft.Win32;


namespace FIRe
{
    /// <summary> Cliente del servicio de firma electrónica.</summary>
    /// La operación a realizar podrá ser firma, cofirma o contrafirma.
    /// Esta clase obtiene la URL del servicio de firma a través del registro.
    public static class HttpSignProcess
    {
        // Parametros que necesitamos de la URL.
        private static readonly String APP_ID = "%APPID%";
        private static readonly String TRANSACTION = "%TRANSACTION%";
	    private static readonly String OP = "%OP%";
	    private static readonly String ALG = "%ALG%"; 
	    private static readonly String FORMAT = "%FT%"; 
	    private static readonly String CERT = "%CERT%";
	    private static readonly String DATA = "%DATA%"; 
	    private static readonly String TRIPHASE_DATA = "%TDATA%";
        
        private static readonly String URL_PARAMETERS =
            "appId=" + APP_ID +
            "&transactionid=" + TRANSACTION +
            "&operation=" + OP +
            "&algorithm=" + ALG +
            "&format=" + FORMAT +
            "&cert=" + CERT +
            "&data=" + DATA +
            "&tri=" + TRIPHASE_DATA;

        /// <summary>
        /// Firma unos datos haciendo uso del servicio de red de firma en la nube.
        /// </summary>
        /// <param name="appId">Identificador de la aplicación.</param>
        /// <param name="transactionId">Identificador de la transacción.</param>
        /// <param name="op">Tipo de operaciónn a realizar: "sign", "cosign" o "countersign".</param>
        /// <param name="ft">Formato de la operación: "XAdES", "PAdES", etc.</param>
        /// <param name="algth">Algoritmo de firma.</param>
        /// <param name="propB64">Propiedades extra a añadir a la firma (puede ser <code>null</code>).</param>
        /// <param name="cert">Certificado de usuario para realizar la firma.</param>
        /// <param name="dataB64"> Datos a firmar en base64.</param>
        /// <param name="tdB64">Datos de la operación trifásica en base64.</param>
        /// <param name="upgrade">Formato al que queremos mejorar la firma (puede ser <code>null</code>).</param>
        /// <returns>Firma realizada en servidor.</returns>
        /// <exception cref="ArgumentException">Cuando se proporciona un parámetro no válido.</exception>
        /// <exception cref="HttpForbiddenException">Cuando falla la autenticación con el componente central.</exception>
        /// <exception cref="HttpNetworkException">Cuando se produce un error de conexión con el componente central.</exception>
        /// <exception cref="HttpOperationException">Cuando se produce un error interno del servidor.</exception>
        /// <exception cref="ConfigureException">Cuando no se encuentra configurada la URL del servicio.</exception>
        public static byte[] sign(
                              String appId,
                              String transactionId,
							  String op,
			                  String ft,
			                  String algth,
			                  String propB64,
			                  String cert,
			                  String dataB64,
			                  String tdB64,
			                  String upgrade) {

            if (string.IsNullOrEmpty(transactionId))
            {
			    throw new ArgumentException(
				    "El id de la transaccion no puede ser nulo"
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
            if (string.IsNullOrEmpty(tdB64))
            {
                throw new ArgumentException(
				    "Los datos de la operacion trifasica no pueden ser nulos"
			    );
		    }
            if (string.IsNullOrEmpty(cert))
            {
                throw new ArgumentException(
				    "El certificado del firmante no puede ser nulo"
			    );
		    }

            string url = ConfigManager.getUrlSignService();
            string urlParameters = URL_PARAMETERS
                .Replace(APP_ID, appId)
                .Replace(TRANSACTION, transactionId)
                .Replace(OP, op)
                .Replace(ALG, algth)
                .Replace(FORMAT, ft)
                .Replace(CERT, cert.Replace('+', '-').Replace('/', '_'))
                .Replace(DATA, dataB64.Replace('+', '-').Replace('/', '_'))
                .Replace(TRIPHASE_DATA, tdB64.Replace('+', '-').Replace('/', '_'));

            if (!string.IsNullOrEmpty(upgrade))
            {
                urlParameters += "&upgrade=" + upgrade; //$NON-NLS-1$
            }
            if (!string.IsNullOrEmpty(propB64))
            {
                urlParameters += "&properties=" + propB64.Replace('+', '-').Replace('/', '_'); //$NON-NLS-1$
            }

            //  realizamos la peticion post al servicio y recibimos los datos de la peticion
            return getResponseToPostPetition(url, urlParameters);        
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
    }
}
