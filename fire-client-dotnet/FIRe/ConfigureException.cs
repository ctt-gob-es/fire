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
    /// Se utiliza para identificar un error en la configuración de la biblioteca.
    /// </summary>
    public class ConfigureException : HttpOperationException
    {
        /// <summary>
        /// Excepción sin parámetros que llama al constructor de la clase padre
        /// </summary>
	    public ConfigureException():base() {	    
	    }
        /// <summary>
        /// Excepción con un parámetro que llama al constructor de la clase padre
        /// </summary>
        /// <param name="msg">Mensaje de la excepción</param>
        public ConfigureException(String msg) : base(msg) {         
        }
    }
}
