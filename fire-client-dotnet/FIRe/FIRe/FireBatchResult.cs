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
using System.Security.Cryptography.X509Certificates;

namespace FIRe
{
    /// <summary>Clase para el almacén del resultado de una operación de firma por lotes.</summary>
    public class FireBatchResult
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
        public X509Certificate cert { get; set; }

        /// <summary>
        ///  Obtiene el resultado de la firma del lote a partir del objeto con la respuesta del
        ///  servicio que devolvió dicho resultado.
        /// </summary>
        /// <param name="json">Objeto con el JSON de respuesta serializado.</param>
        /// <returns>Objeto con el resultado de la firma del lote.</returns>
        internal static FireBatchResult Parse(BatchResultJson json)
        {
            FireBatchResult result = new FireBatchResult();
            result.prov = json.prov;
            if (json.cert != null)
            {
                result.cert = new X509Certificate(System.Convert.FromBase64String(json.cert));
            }
            result.batch = new List<FireSingleResult>();
            foreach (var singleBatch in json.batch)
            {
                FireSingleResult singleSignature = new FireSingleResult();
                singleSignature.id = singleBatch.id;
                singleSignature.ok = singleBatch.ok;
                singleSignature.dt = singleBatch.dt;
                if (singleBatch.grace != null)
                {
                    // Transformamos los milisegundos Java a una fecha .Net
                    DateTime graceDate = new DateTime(new DateTime(1970, 1, 1).Ticks + singleBatch.grace.Date * 10000);
                    singleSignature.gracePeriod = new GracePeriod(singleBatch.grace.Id, graceDate);
                }
                result.batch.Add(singleSignature);
            }
            return result;
        }
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

        /// <summary>
        /// Periodo de gracia que es necesario conceder a la firma antes de recuperarla.
        /// </summary>
        public GracePeriod gracePeriod { get; set; }
    }
}
