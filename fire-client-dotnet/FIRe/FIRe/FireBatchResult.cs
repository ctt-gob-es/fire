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
    /// <summary>Clase para el almacén del resultado de una operación de firma por lotes.</summary>
    public class FireBatchResult
    {
        /// <summary>
        /// Listado de resultados de firma en proceso batch.
        /// </summary>
        public List<FireSingleResult> batch { get; set; }
    }

    /// <summary>Clase para el almacén del resultado de una operación de firma de un documento.</summary>
    public class FireSingleResult
    {
        /// <summary>
        /// Identificador del documento.
        /// </summary>
        public string id { get; set; }
        /// <summary>
        /// Indicador de éxito de la firma.
        /// </summary>
        public bool ok { get; set; }
        /// <summary>
        /// Resultado de la firma.
        /// </summary>
        public string dt { get; set; }
    }
}
