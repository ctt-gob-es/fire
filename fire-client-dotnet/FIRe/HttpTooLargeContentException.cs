/* Copyright (C) 2025 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 16/01/2025
 */

using System;

namespace FIRe
{
    /// <summary>El tamaño de la petición excedía el máximo permitido.</summary>
    public class HttpTooLargeContentException : HttpOperationException
    {


        // Mensaje de error por defecto.
        private static readonly string DEFAUL_ERROR_MSG = "El contenido de la peticion era demasiado grande"; //$NON-NLS-1$

        /// <summary>
        /// Se crea la excepción.
        /// </summary>
        public HttpTooLargeContentException() : base()
        {
        }

        /// <summary>
        /// Se crea la excepción.
        /// </summary>
        /// <param name="code">Código del error.</param>
        public HttpTooLargeContentException(int code) : base(code, DEFAUL_ERROR_MSG)
        {
        }

        /// <summary>
        /// Se crea la excepción.
        /// </summary>
        /// <param name="code">Código del error.</param>
        /// <param name="msg">Descripcion del error.</param>
        public HttpTooLargeContentException(int code, string msg) : base(code, msg)
        {
        }

        /// <summary>
        /// Se crea la excepción.
        /// </summary>
        /// <param name="msg">Descripcion del error.</param>
        /// <param name="e">Causa del error</param>
        public HttpTooLargeContentException(string msg, Exception e) : base(msg, e)
        {
        }
    }
}
