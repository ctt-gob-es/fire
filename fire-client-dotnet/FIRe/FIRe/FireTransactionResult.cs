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
using System.Collections.Generic;
using System.Web.Script.Serialization;

namespace FIRe
{
    /// <summary>Clase para el almacén del resultado de una operación de carga de datos para firmar.</summary>
    public class FireTransactionResult
    {

        // Especifica que la transacci&oacute;n finaliz&oacute; correctamente.
        private static readonly int STATE_OK = 0;
        // Especifica que la transacci&oacute;n no pudo finalizar debido a un error.
        private static readonly int STATE_ERROR = -1;

        // Prefijo que antecede al codigo de error cuando este se produjo durante la operacion.
        private static readonly string ERROR_PREFIX = "ERR-";
        // Nombre del campo principal del JSON con la informacion de la firma
        private static readonly string JSON_ATTR_RESULT = "result"; //$NON-NLS-1$
        // Prefijo de la estructura que almacena la informacion sobre la operacion realizada
        private static readonly string JSON_RESULT_PREFIX = "{\"" + JSON_ATTR_RESULT + "\":"; //$NON-NLS-1$ //$NON-NLS-2$
        // Sufijo que se indica a continuacion de un codigo de error.
        private static readonly string ERROR_SUFIX = ":";

        private readonly int state;
        private String providerName { get; set; }
        private readonly String errorCode;
        private readonly String errorMessage;
        private byte[] result { get; set; }

        /// <summary>Crea el resultado de una operación de carga de datos a firmar a partir de su defición JSON.</summary>
        /// <param name="bytes">Definición del resultado de una operación de firma.</param>
        /// <exception cref="ArgumentException">Cuando el formato del JSON no es el esperado.</exception>
        public FireTransactionResult(byte[] bytes) { 
            if (bytes == null) {
			    throw new ArgumentException(
				    "El resultado de la firma no puede ser nulo" 
			    );
		    }

			this.state = STATE_OK;

            // Comprobamos el inicio de la respuesta para saber si recibimos la informacion
            // de la operacion o el binario resultante
            byte[] prefix = null;
            if (bytes != null && bytes.Length > JSON_RESULT_PREFIX.Length + 2)
            {
                prefix = new byte[JSON_RESULT_PREFIX.Length];
                Array.Copy(bytes, 0, prefix, 0, prefix.Length);
            }


            // Si los datos empiezan por un prefijo concreto, es la informacion de la operacion
            if (prefix != null && prefix.Equals(JSON_RESULT_PREFIX))
            {
                try
                {
                    FireSignResult signResult = deserializedSignResult(System.Text.Encoding.UTF8.GetString(bytes));
                    if (signResult.ercod != 0)
                    {
                        this.errorCode = signResult.ercod.ToString();
                    }
                    if (signResult.ermsg != null)
                    {
                        this.errorMessage = signResult.ermsg;
                    }
                    if (signResult.prov != null)
                    {
                        this.providerName = signResult.prov;
                    }
                }
                catch(Exception)
                {
                    this.state = STATE_ERROR;
                }
            }
            else
            {
                this.result = bytes;
            }
        }
        
        /// <summary>
        ///  Deserializa una estructura JSON para obtener de ella un objeto de tipo FireSignResult.
        /// </summary>
        /// <param name="JSON">Cadena en formato JSON que se desea analizar.</param>
        /// <returns></returns>
        private static FireSignResult deserializedSignResult(string JSON)
        {
            var json_serializer = new JavaScriptSerializer();
            return json_serializer.Deserialize<FireSignResult>(JSON);
        }

        /// <summary> Obtiene el resultado de la transacción de firma.</summary>
        /// <returns>Identificador de la transacción de firma</returns>
        public byte[] getResult()
        {
		    return this.result;
	    }

        /// <summary> Establece el resultado de la transacción de firma.</summary>
        /// <param name="data">Datos resultantes de la transacción.</returns>
        public void setResult(byte[] data)
        {
            this.result = data;
        }

        /// <summary>Obtiene el código de error durante la firma.</summary>
        /// <returns>Código de error.</returns>
	    public String getErrorCode()
        {
		    return this.errorCode;
	    }

        /// <summary>Obtiene el mensaje de error durante la firma.</summary>
        /// <returns>Mensaje de error.</returns>
        public String getErrorMessage()
        {
            return this.errorMessage;
        }
    }

    /// <summary>Clase que contiene la información recabada de la firma de un documento.</summary>
    public class FireSignResult
    {
        /// <summary>
        /// Nombre del proveedor.
        /// </summary>
        public string prov { get; set; }
        /// <summary>
        /// Código de error.
        /// </summary>
        public int ercod { get; set; }
        /// <summary>
        /// Mensaje de error.
        /// </summary>
        public string ermsg { get; set; }
    }
}
