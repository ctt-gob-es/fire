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
        // Sufijo que se indica a continuacion de un codigo de error.
        private static readonly string ERROR_SUFIX = ":";

        private readonly int state;
        private readonly String errorCode;
        private readonly String errorMessage;
        private readonly byte[] result;

        /// <summary>Crea el resultado de una operación de carga de datos a firmar a partir de su defición JSON.</summary>
        /// <param name="result">Definición del resultado de una operación de firma.</param>
        /// <exception cref="ArgumentException">Cuando el formato del JSON no es el esperado.</exception>
        public FireTransactionResult(byte[] result) { 
            if (result == null) {
			    throw new ArgumentException(
				    "El resultado de la firma no puede ser nulo" 
			    );
		    }

			this.state = STATE_OK;
            
            // Comprobamos si se ha producido un error no recuperable
            if (result.Length > 6 && ERROR_PREFIX == System.Text.Encoding.Default.GetString(new byte[] { result[0], result[1], result[2], result[3] }))
            {
                // Comprobamos los primeros caracteres para corroborar que se trata de un error (cabecera "ERR-" seguida de un numero y despues ':') 
                for (int i = 5; i < Math.Min(result.Length, 11) && this.state == STATE_OK; i++)
                {
                    if (result[i] == ':')
                    {
                        this.state = STATE_ERROR;
                    }
                }
            }

            // En caso de error, habremos recibido el codigo y el mensaje
            if (this.state == STATE_ERROR) {
                string stringResult = System.Text.Encoding.Default.GetString(result);
                this.errorCode = stringResult.Substring(ERROR_PREFIX.Length, stringResult.IndexOf(ERROR_SUFIX) - ERROR_PREFIX.Length);
				this.errorMessage = stringResult.Substring(stringResult.IndexOf(ERROR_SUFIX) + 1);
            }
			// En caso de exito habremos recibido directamente el resultado
			else {
				this.result = result;
            }

        }
        
        /// <summary>
        ///  Devuelve un conjunto de propiedades extraídas de un JSON.
        /// </summary>
        /// <param name="JSON">Cadena en formato JSON que se desea analizar.</param>
        /// <returns></returns>
        private static Dictionary<string, string> getJson(string JSON)
        {
            var json_serializer = new JavaScriptSerializer();
            return json_serializer.Deserialize<Dictionary<string, string>>(JSON);
        }


	    /// <summary> Obtiene el resultado de la transacción de firma.</summary>
	    /// <returns>Identificador de la transacción de firma</returns>
         public byte[] getResult() {
		    return this.result;
	    }

        /// <summary>Obtiene el código de error durante la firma.</summary>
        /// <returns>Código de error.</returns>
	    public String getErrorCode() {
		    return this.errorCode;
	    }

        /// <summary>Obtiene el mensaje de error durante la firma.</summary>
        /// <returns>Mensaje de error.</returns>
        public String getErrorMessage()
        {
            return this.errorMessage;
        }
    }
}
