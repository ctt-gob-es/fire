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

namespace FIRe
{
    /// <summary>Clase para la codificación de datos binarios en cadenas Base64 y viceversa.</summary>
   public static class Base64
    {
        /// <summary>Codifica una cadena de texto plano en base64</summary>
        /// <param name="plainText">Cadena a codificar.</param>
        /// <returns>Cadena base64.</returns>
        public static string Base64Encode(string plainText)
        {
            var plainTextBytes = System.Text.Encoding.UTF8.GetBytes(plainText);
            return System.Convert.ToBase64String(plainTextBytes);
        }

        /// <summary>Decodifica una cadena base64 en un texto plano.</summary>
        /// <param name="base64EncodedData">Cadena a decodificar.</param>
        /// <returns>Cadena decodificada.</returns>
        public static string Base64Decode(string base64EncodedData)
        {
            var base64EncodedBytes = System.Convert.FromBase64String(base64EncodedData);
            return System.Text.Encoding.UTF8.GetString(base64EncodedBytes);
        }

       
    }
}
