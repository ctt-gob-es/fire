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
    /// Error durante la ejecución de la operación.
    /// </summary>
    public class HttpOperationException : Exception
    {
        /// <summary>
        /// Nombre del proveedor de firma utilizado.
        /// </summary>
        public int Code { get; set; }

        /// <summary>
        /// Excepción sin parámetros que llama al constructor de la clase padre
        /// </summary>
	    public HttpOperationException():base() {
            this.Code = 0;
	    }
        /// <summary>
        /// Excepción con un parámetros que llama al constructor de la clase padre
        /// </summary>
        /// <param name="msg">Mensaje de la excepción</param>
        public HttpOperationException(String msg) : base(msg) {
            this.Code = 0;
        }
        /// <summary>
        /// Excepción con un parámetros que llama al constructor de la clase padre
        /// </summary>
        /// <param name="code">Código de error.</param>
        /// <param name="msg">Mensaje de la excepción.</param>
        public HttpOperationException(int code, String msg) : base(msg)
        {
            this.Code = code;
        }
        /// <summary>
        ///  Excepción con dos parámetros que llama al constructor de la clase padre
        /// </summary>
        /// <param name="responseDescription">Mensaje de la excepción</param>
        /// <param name="e">Exception</param>
        public HttpOperationException(String responseDescription, Exception e):base( responseDescription,  e) {
            this.Code = 0;
	    }
         
    }
}
