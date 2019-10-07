/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */

using System.Collections.Generic;
using System.Security.Cryptography.X509Certificates;

namespace FIRe
{
    /// <summary>
    /// Clase para la serialización/deserialización del resultado de una operación de firma por lotes.
    /// </summary>
    public class FireBatchResultJson
    {
        /// <summary>
        /// Listado de resultados de firma en proceso batch.
        /// </summary>
        public List<FireSingleResult> batch { get; set; }
        
        /// <summary>
        /// Nombre del proveedor de firma utilizado.
        /// </summary>
        public string prov { get; set; }

        /// <summary>
        /// Certificado de firma utilizado.
        /// </summary>
        public string cert { get; set; }
    }
}
