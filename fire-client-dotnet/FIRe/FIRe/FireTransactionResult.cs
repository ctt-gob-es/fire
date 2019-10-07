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
using System.Web.Script.Serialization;
using System.Security.Cryptography.X509Certificates;
using System.Windows.Forms;

namespace FIRe
{
    /// <summary>Clase para el almacén del resultado de una operación de carga de datos para firmar.</summary>
    public class FireTransactionResult
    {

        // Especifica que la transacci&oacute;n finaliz&oacute; correctamente.
        private static readonly int STATE_OK = 0;
        // Especifica que la transacci&oacute;n no pudo finalizar debido a un error.
        private static readonly int STATE_ERROR = -1;
        
        // Nombre del campo principal del JSON con la informacion de la firma
        private static readonly string JSON_ATTR_RESULT = "result"; //$NON-NLS-1$
        // Prefijo de la estructura que almacena la informacion sobre la operacion realizada
        private static readonly string JSON_RESULT_PREFIX = "{\"" + JSON_ATTR_RESULT + "\":"; //$NON-NLS-1$ //$NON-NLS-2$

        private readonly int State;
        private readonly string ProviderName;
        private readonly X509Certificate SigningCert;
        private readonly string ErrorCode;
        private readonly string ErrorMessage;
        private byte[] Result;

        /// <summary>Crea el resultado de una operación de carga de datos a firmar a partir de su defición JSON.</summary>
        /// <param name="bytes">Definición del resultado de una operación de firma.</param>
        /// <exception cref="ArgumentException">Cuando el formato del JSON no es el esperado.</exception>
        public FireTransactionResult(byte[] bytes) { 
            if (bytes == null) {
			    throw new ArgumentException(
				    "El resultado de la firma no puede ser nulo" 
			    );
		    }
            
            this.State = STATE_OK;

            // Comprobamos el inicio de la respuesta para saber si recibimos la informacion
            // de la operacion o el binario resultante
            byte[] prefix = null;
            if (bytes != null && bytes.Length > JSON_RESULT_PREFIX.Length + 2)
            {
                prefix = new byte[JSON_RESULT_PREFIX.Length];
                Array.Copy(bytes, 0, prefix, 0, prefix.Length);
            }
            
            // Si los datos empiezan por un prefijo concreto, es la informacion de la operacion
            if (prefix != null && System.Text.Encoding.UTF8.GetString(prefix).Equals(JSON_RESULT_PREFIX))
            {
                try
                {
                    FireSignResult signResult = DeserializedSignResult(System.Text.Encoding.UTF8.GetString(bytes));

                    if (signResult.GetErrorCode() != 0)
                    {
                        this.ErrorCode = signResult.GetErrorCode().ToString();
                        this.State = STATE_ERROR;
                    }
                    if (signResult.GetErrorMessage() != null)
                    {
                        this.ErrorMessage = signResult.GetErrorMessage();
                    }
                    if (signResult.GetProviderName() != null)
                    {
                        this.ProviderName = signResult.GetProviderName();
                    }
                    if (signResult.GetSigningCert() != null)
                    {
                        this.SigningCert = signResult.GetSigningCert();
                    }
                }
                catch(Exception e)
                {
                    throw new FormatException("El servicio respondio con un JSON no valido: " + System.Text.Encoding.UTF8.GetString(bytes), e);
                }
            }
            else
            {
                this.Result = bytes;
            }
        }

        /// <summary>Obtiene el tipo de resultado obtenido.</summary>
        /// <returns>Tipo de resultado obtenido.</returns>
        public int getState()
        {
            return this.State;
        }

        /// <summary>Obtiene el nombre del proveedor con el que se realizó la operación.</summary>
        /// <returns>Nombre del proveedor.</returns>
        public string getProviderName()
        {
            return this.ProviderName;
        }

        /// <summary>Obtiene el certificado con el que se realizó la operación.</summary>
        /// <returns>Certificado.</returns>
        public X509Certificate getSigningCert()
        {
            return this.SigningCert;
        }

        /// <summary>Obtiene el código del error obtenido al ejecurar la transacción.</summary>
        /// <returns>Código del error obtenido.</returns>
        public string getErrorCode()
        {
            return this.ErrorCode;
        }

        /// <summary>Obtiene el mensaje del error obtenido al ejecurar la transacción.</summary>
        /// <returns>Mensaje del error obtenido.</returns>
        public string getErrorMessage()
        {
            return this.ErrorMessage;
        }

        /// <summary>Recupera el resultado generado por la transacción.</summary>
        /// <returns>Resultado generado por la transacción.</returns>
        public byte[] getResult()
        {
            return this.Result;
        }

        /// <summary>Establece el resultado generado por la transacción.</summary>
        /// <param name="result">Resultado generado por la transacción.</param>
        public void setResult(byte[] result)
        {
            this.Result = result;
        }

        /// <summary>
        ///  Deserializa una estructura JSON para obtener de ella un objeto de tipo FireSignResult.
        /// </summary>
        /// <param name="JSON">Cadena en formato JSON que se desea analizar.</param>
        /// <returns></returns>
        private static FireSignResult DeserializedSignResult(string JSON)
        {
            var json_serializer = new JavaScriptSerializer();
            return json_serializer.Deserialize<FireSignResult>(JSON);
        }
    }

    /// <summary>Clase que contiene la información recabada de la firma de un documento.</summary>
    public class FireSignResult
    {
        /// <summary>
        /// Resultado de la operacion de firma.
        /// </summary>
        public FireSignResultData Result { get; set; }

        /// <summary>
        /// Recupera el nombre del proveedor utilizado en la operación.
        /// </summary>
        /// <returns>Nombre del proveedor.</returns>
        public string GetProviderName()
        {
            return this.Result.Prov;
        }

        /// <summary>
        /// Recupera el certificado utilizado en la operación.
        /// </summary>
        /// <returns>Certificado utilizado.</returns>
        public X509Certificate GetSigningCert()
        {
            if (this.Result.Cert == null)
            {
                return null;
            }
            return new X509Certificate(System.Convert.FromBase64String(this.Result.Cert));
        }

        /// <summary>
        /// Recupera el código del error que se produjese durante la operación.
        /// </summary>
        /// <returns>Código de error o null si no se produjo ningún error.</returns>
        public int GetErrorCode()
        {
            return this.Result.Ercod;
        }

        /// <summary>
        /// Recupera el mensaje del error que se produjese durante la operación.
        /// </summary>
        /// <returns>Mensaje de error o null si no se produjo ningún error.</returns>
        public string GetErrorMessage()
        {
            return this.Result.Ermsg;
        }
    }

    /// <summary>Clase que contiene la información recabada de la firma de un documento.</summary>
    public class FireSignResultData
    {
        /// <summary>
        /// Nombre del proveedor.
        /// </summary>
        public string Prov { get; set; }
        /// <summary>
        /// Certificado de firma.
        /// </summary>
        public string Cert { get; set; }
        /// <summary>
        /// Código de error.
        /// </summary>
        public int Ercod { get; set; }
        /// <summary>
        /// Mensaje de error.
        /// </summary>
        public string Ermsg { get; set; }
    }

}
