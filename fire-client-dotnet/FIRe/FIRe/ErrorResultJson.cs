/* Copyright (C) 2022 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 04/11/2022
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */

namespace FIRe
{
    /// <summary>
    /// Clase para la serialización/deserialización de un error trasladado por el servicio.
    /// </summary>
    internal class ErrorResultJson
    {
        /// <summary>
        /// Código de error.
        /// </summary>
        public int c { get; set; }
        
        /// <summary>
        /// Mensaje de error.
        /// </summary>
        public string m { get; set; }
    }
}
