/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */

namespace FIRe
{
    /// <summary>Clase con los tipos de error HTTP propios de la aplicación.</summary>
   public static class HttpCustomErrors
    {
        /// Identifica los casos en los que un usuario no tiene certificados del tipo indicado.
        public const string NO_CERTS = "522";
        /// El usuario no esta dado de alta en el sistema.
        public const string NO_USER = "523";
        /// El usuario no esta dado de alta en el sistema. 
        public const string CERTIFICATE_BLOCKED = "524";
        /// Error que identifica que el usuario ya dispone de tantos certificados como puede tener del tipo indicado. 
        public const string CERTIFICATE_AVAILABLE = "525";
        /// Se excede el límite de documentos establecido (comúnmente, el tamaño de un lote). 
        public const string NUM_DOCUMENTS_EXCEEDED = "526";
        /// Se indica un identificador de documento que ya está dado de alta en el lote de firma.
        public const string DUPLICATE_DOCUMENT = "527";
        /// La transaccion indicada no es valida o ya ha caducado. 
        public const string INVALID_TRANSACTION = "528";
        /// Error devuelto por el servicio de custodia al realizar la operación de firma. 
        public const string SIGN_ERROR = "529";
        /// Error devuelto por el servicio de custodia al realizar la operación de firma. 
        public const string POSTSIGN_ERROR = "530";
        /// Error devuelto por el servicio de custodia al realizar la operación de firma. 
        public const string WEAK_REGISTRY = "531";
        /// Error devuelto por el servicio de custodia al realizar la actualización de la firma.
        public const string UPGRADING_ERROR = "532";
        /// Error devuelto al no poder guardar la firma en servidor a través del gestor de documentos.
        public const string SAVING_ERROR = "533";
        /// Error devuelto cuando se solicita recuperar una firma de un lote sin haberlo firmado antes.
        public const string BATCH_NO_SIGNED = "534";
        /// Error devuelto cuando se solicita recuperar una firma de un lote sin haberlo firmado antes.
        public const string INVALID_BATCH_DOCUMENT = "535";
        /// Error devuelto cuando se solicita recuperar una firma de un lote sin haberlo firmado antes.
        public const string BATCH_DOCUMENT_FAILED = "536";
        /// Error devuelto cuando se solicita firmar un lote sin documentos.
        public const string BATCH_NO_DOCUMENTS = "537";
        /// Error devuelto cuando se detecta que la firma generada no es válida.
        public const string INVALID_SIGNATURE_ERROR = "538";
        /// Error devuelto cuando se solicita el uso de un gestor que no existe o que no le está permitido a la aplicación.
        public const string INVALID_DOCUMENT_MANAGER = "539";
    }
}
