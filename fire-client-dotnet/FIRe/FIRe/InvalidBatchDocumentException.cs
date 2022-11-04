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

namespace FIRe
{
    /// <summary>
    /// La firma del documento no se generó correctamente o se indicó un documento no válido.
    /// </summary>
    class InvalidBatchDocumentException : HttpOperationException
    {
        /// <summary>
        /// Se crea la excepción.
        /// </summary>
        public InvalidBatchDocumentException() : base()
        {
        }

        /// <summary>
        /// Se crea la excepción.
        /// </summary>
        /// <param name="code">Código del error.</param>
        /// <param name="msg">Descripcion del error.</param>
        public InvalidBatchDocumentException(int code, string msg) : base(code, msg)
        {
        }

        /// <summary>
        /// Se crea la excepción.
        /// </summary>
        /// <param name="msg">Descripcion del error.</param>
        /// <param name="e">Causa del error</param>
        public InvalidBatchDocumentException(string msg, Exception e) : base(msg, e)
        {
        }
    }
}
