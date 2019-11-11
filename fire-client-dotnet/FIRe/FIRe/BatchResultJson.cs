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

namespace FIRe
{
    /// <summary>
    /// Clase para la serialización/deserialización del resultado de una operación de firma por lotes.
    /// </summary>
    internal class BatchResultJson
    {
        /// <summary>
        /// Listado de resultados de firma en proceso batch.
        /// </summary>
        public List<BatchSingleResultJson> batch { get; set; }
        
        /// <summary>
        /// Nombre del proveedor de firma utilizado.
        /// </summary>
        public string prov { get; set; }

        /// <summary>
        /// Certificado de firma utilizado.
        /// </summary>
        public string cert { get; set; }
    }

    /// <summary>Clase para el almacén del resultado de una operación de firma de un documento.</summary>
    internal class BatchSingleResultJson
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

        /// <summary>
        /// Periodo de gracia que es necesario conceder a la firma antes de recuperarla.
        /// </summary>
        public GracePeriodJson grace { get; set; }
    }
}
