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
using System.Linq;
using System.Text;
using System.Web.Script.Serialization;

namespace FIRe
{
    /// <summary>Clase para el almacén del identificador de la transacción.</summary>
    public class FireTransactionIdResult
    {
        private readonly string transactionId;

        /// <summary>Crea el resultado de una operación de carga de datos a firmar a partir de su defición JSON.</summary>
        /// <param name="json">Definición del resultado de una operación de carga de datos a firmar.</param>
        /// <exception cref="ArgumentException">Cuando el formato del JSON no es el esperado.</exception>
        public FireTransactionIdResult(string json) { 
            if (json == null) {
			    throw new ArgumentException(
				    "El JSON de definicion no puede ser nulo" 
			    );
		    }
            Dictionary<string, string> jsonObject = GetJson(json);

            string id = jsonObject["transactionid"]; 
		

		    if (id == null || "".Equals(id)) { 
			    throw new ArgumentException(
				    "Es obligatorio que el JSON contenga el identificador de la transacci&oacute;n" 
			    );
		    }
            
		    this.transactionId = id;

        }

        /// <summary>
        ///  Devuelve un conjunto de propiedades extraídas de un JSON.
        /// </summary>
        /// <param name="JSON">Cadena en formato JSON que se desea analizar.</param>
        /// <returns></returns>
        private static Dictionary<string, string> GetJson(string JSON)
        {
            var json_serializer = new JavaScriptSerializer();
            return json_serializer.Deserialize<Dictionary<string, string>>(JSON);
        }

        /// <summary> Obtiene el identificador de la transacción de firma.</summary>
        /// <returns>Identificador de la transacción de firma</returns>
        public string getTransactionId() {
		    return this.transactionId;
	    }

        /// <summary>Genera una cadena JSON con las propiedades del resultado de la carga de datos.</summary>
        /// <returns>JSON con las propiedades del objeto.</returns>
	    public override string ToString() {
		    StringBuilder sb = new StringBuilder("{\n  \"transacionid\":\""); 
		    sb.Append(getTransactionId());
            sb.Append("\"\n}"); 
		    return sb.ToString();
	    } 
    }
}
