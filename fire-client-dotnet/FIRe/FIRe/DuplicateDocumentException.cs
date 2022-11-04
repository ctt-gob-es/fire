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
    /// <summary>
    /// Indica cuando se intenta agregar a un lote un documento con un identificador que ya se utilizo
    /// anteriormente en este lote.
    /// </summary>
    class DuplicateDocumentException : HttpOperationException
    {
        /// <summary>
        /// Se crea la excepción.
        /// </summary>
        public DuplicateDocumentException() : base()
        {
        }

        /// <summary>
        /// Se crea la excepción.
        /// </summary>
        /// <param name="code">Código del error.</param>
        /// <param name="msg">Descripcion del error.</param>
        public DuplicateDocumentException(int code, string msg) : base(code, msg)
        {
        }

        /// <summary>
        /// Se crea la excepción.
        /// </summary>
        /// <param name="msg">Descripcion del error.</param>
        /// <param name="e">Causa del error</param>
        public DuplicateDocumentException(string msg, Exception e) : base(msg, e)
        {
        }
    }
}
