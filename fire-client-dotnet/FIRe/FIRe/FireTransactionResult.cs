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
using System.Security.Cryptography.X509Certificates;
using System.Web.Script.Serialization;

namespace FIRe
{
    /// <summary>Clase para el almacén del resultado de una operación de carga de datos para firmar.</summary>
    public class FireTransactionResult
    {
        /// <summary>Especifica que la transacción finalizó correctamente.</summary>
        public static readonly int STATE_OK = 0;
        /// <summary>Especifica que la transacción no pudo finalizar debido a un error.</summary>
        public static readonly int STATE_ERROR = -1;
        /// <summary>Especifica que la transaccion aun no ha finalizado y se debera pedir el resultamos
		/// mas adelante.</summary>
        public static readonly int STATE_PENDING = 1;
        /// <summary>Especifica que la transacción ha finalizado pero que el resultado puede
        /// diferir de lo solicitado por la aplicación. Por ejemplo, puede haberse
        /// solicitado una firma ES-A y recibirse una ES-T.</summary>
        public static readonly int STATE_PARTIAL = 2;

        // Prefijo de la estructura que almacena la informacion sobre la operacion realizada
        private static readonly string JSON_RESULT_PREFIX = "{\"result\":"; //$NON-NLS-1$ //$NON-NLS-2$

        /// <summary>Tipo de resultado obtenido.</summary>
        public int State { get; }
        /// <summary>Nombre del proveedor de firma.</summary>
        public string ProviderName { get; }
        /// <summary>Certificado utilizado para firmar.</summary>
        public X509Certificate SigningCert { get; }
        /// <summary>Periodo de gracia que esperar antes de obtener un resultado.</summary>
        public GracePeriod GracePeriod { get; }
        /// <summary>Formato al que se ha actualizado la firma.</summary>
        public string UpgradeFormat { get; }
        /// <summary>Código del error obtenido.</summary>
        public string ErrorCode { get; }
        /// <summary>Mensaje del error obtenido.</summary>
        public string ErrorMessage { get; }
        /// <summary>Resultado generado por la transacción.</summary>
        public byte[] Result { get; set; }

        /// <summary>Crea el resultado de una operación de carga de datos a firmar a partir de su defición JSON.</summary>
        /// <param name="bytes">Definición del resultado de una operación de firma.</param>
        /// <exception cref="ArgumentException">Cuando el formato del JSON no es el esperado.</exception>
        public FireTransactionResult(byte[] bytes)
        {
            if (bytes == null)
            {
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
                    TransactionResultJson signResult = DeserializedSignResult(System.Text.Encoding.UTF8.GetString(bytes));
                    this.State = signResult.Result.State;
                    if (signResult.Result.Ercod != 0)
                    {
                        this.ErrorCode = signResult.Result.Ercod.ToString();
                    }
                    if (signResult.Result.Ermsg != null)
                    {
                        this.ErrorMessage = signResult.Result.Ermsg;
                    }
                    if (signResult.Result.Prov != null)
                    {
                        this.ProviderName = signResult.Result.Prov;
                    }
                    if (signResult.Result.Cert != null)
                    {
                        this.SigningCert = new X509Certificate(System.Convert.FromBase64String(signResult.Result.Cert));
                    }
                    if (signResult.Result.Grace != null)
                    {
                        // Transformamos los milisegundos Java a una fecha .Net
                        GracePeriodJson graceJson = signResult.Result.Grace;
                        DateTime graceDate = new DateTime(new DateTime(1970, 1, 1).Ticks + graceJson.Date * 10000);
                        this.GracePeriod = new GracePeriod(graceJson.Id, graceDate);
                    }
                    if (signResult.Result.Upgrade != null)
                    {
                        this.UpgradeFormat = signResult.Result.Upgrade;
                    }
                }
                catch (Exception e)
                {
                    throw new FormatException("El servicio respondio con un JSON no valido: " + System.Text.Encoding.UTF8.GetString(bytes), e);
                }
            }
            else
            {
                this.Result = bytes;
            }
        }

        /// <summary>
        ///  Deserializa una estructura JSON para obtener de ella un objeto de tipo TransactionResultJson.
        /// </summary>
        /// <param name="JSON">Cadena en formato JSON que se desea analizar.</param>
        /// <returns></returns>
        private static TransactionResultJson DeserializedSignResult(string JSON)
        {
            var json_serializer = new JavaScriptSerializer();
            return json_serializer.Deserialize<TransactionResultJson>(JSON);
        }

        
    }

    /// <summary>Clase con el periodo de gracia asignado para la obtencion de la firma actualizada.</summary>
    public class GracePeriod
    {
        /// <summary>
        /// Construye el periodo de gracia a partir del identificador.
        /// </summary>
        public GracePeriod(string Id, DateTime Date)
        {
            this.Id = Id;
            this.Date = Date;
        }

        /// <summary>
        /// Identificador con el que recuperar la firma actualizada.
        /// </summary>
        public string Id { get; set; }
        /// <summary>
        /// Fecha en la que deberia estar disponible la firma actualizada.
        /// </summary>
        public DateTime Date { get; set; }
    }
}
