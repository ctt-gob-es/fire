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
    /// Certificado bloqueado.
    /// </summary>
    public class HttpCertificateBlockedException : HttpOperationException
    {
        /// <summary>
        /// Certificado bloqueado.
        /// </summary>
	    public HttpCertificateBlockedException():base() {
        }
        /// <summary>
        /// Certificado bloqueado.
        /// </summary>
        /// <param name="msg">Menaje</param>
        /// <param name="e">Excepcion</param>
        public HttpCertificateBlockedException(string msg, Exception e): base(msg, e)   {
        }
    }
}
