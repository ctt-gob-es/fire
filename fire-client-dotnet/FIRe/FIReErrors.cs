/* Copyright (C) 2022 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 04/11/2022
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */

namespace FIRe
{
    /// <summary>Clase con los tipos de error HTTP propios de la aplicación.</summary>
   public static class FIReErrors
    {
        /// Error en la lectura de los parámetros de entrada.
        public const int READING_PARAMETERS = 1;
        /// No se ha indicado el identificador de la aplicación.
        public const int PARAMETER_APP_ID_NEEDED = 2;
        /// No se ha indicado la operación a realizar.
        public const int PARAMETER_OPERATION_NEEDED = 3;
        /// Se ha indicado un id de operación no soportado.
        public const int PARAMETER_OPERATION_NOT_SUPPORTED = 5;
        /// No se ha indicado el certificado de autenticación.
        public const int PARAMETER_AUTHENTICATION_CERTIFICATE_NEEDED = 6;
        /// Se ha indicado un certificado de autenticación mal formado.
        public const int PARAMETER_AUTHENTICATION_CERTIFICATE_INVALID = 7;
        /// No se ha indicado el identificador de usuario.
        public const int PARAMETER_USER_ID_NEEDED = 8;
        /// No se ha indicado el algoritmo de firma.
        public const int PARAMETER_SIGNATURE_ALGORITHM_NEEDED = 9;
        /// No se ha indicado la operación de firma.
        public const int PARAMETER_SIGNATURE_OPERATION_NEEDED = 10;
        /// No se ha indicado el formato de firma.
        public const int PARAMETER_SIGNATURE_FORMAT_NEEDED = 11;
        /// No se han indicado los datos que firmar.
        public const int PARAMETER_DATA_TO_SIGN_NEEDED = 12;
        /// Se han indicado datos a firmar mal codificados.
        public const int PARAMETER_DATA_TO_SIGN_INVALID = 13;
        /// No se han encontrado los datos a firmar.
        public const int PARAMETER_DATA_TO_SIGN_NOT_FOUND = 14;
        /// No se ha indicado la configuración de transacción.
        public const int PARAMETER_CONFIG_TRANSACTION_NEEDED = 15;
        /// Se ha indicado una configuración de transacción mal formada.
        public const int PARAMETER_CONFIG_TRANSACTION_INVALID = 16;
        /// No se ha indicado la URL de redirección en caso de error en la configuración de transacción.
        public const int PARAMETER_URL_ERROR_REDIRECION_NEEDED = 17;
        /// No se ha indicado el identificador de transacción.
        public const int PARAMETER_TRANSACTION_ID_NEEDED = 18;
        /// Se han indicado propiedades de configuración de fima mal formadas.
        public const int PARAMETER_SIGNATURE_PARAMS_INVALID = 20;
        /// El proveedor no tiene dado de alta al usuario indicado
        public const int UNKNOWN_USER = 21;
        /// El usuario ya dispone de un certificado del tipo que se está solicitando generar.
        public const int CERTIFICATE_DUPLICATED = 22;
        /// Error al obtener los certificados del usuario o al generar uno nuevo.
        public const int CERTIFICATE_ERROR = 23;
        /// El usuario no puede poseer certificados de firma por haber realizado un registro no fehaciente.
        public const int CERTIFICATE_WEAK_REGISTRY = 24;
        /// Error durante la firma.
        public const int SIGNING = 26;
        /// No se seleccionó un proveedor de firma.
        public const int PROVIDER_NOT_SELECTED = 27;
        /// La firma generada no es válida.
        public const int INVALID_SIGNATURE = 31;
        /// Error durante la actualización de firma.
        public const int UPGRADING_SIGNATURE = 32;
        /// No se ha indicado el identificador de los datos asíncronos.
        public const int PARAMETER_ASYNC_ID_NEEDED = 34;
        /// Gestor de documentos no válido.
        public const int PARAMETER_DOCUMENT_MANAGER_INVALID = 35;
        /// Los certificados del usuario están bloqueados.
        public const int CERTIFICATE_BLOCKED = 38;
        /// El usuario no dispone de certificados y el proveedor no le permite generarlos en este momento.
        public const int CERTIFICATE_NO_CERTS = 39;
        /// El identificador de documento ya existe en el lote.
        public const int BATCH_DUPLICATE_DOCUMENT = 42;
        /// Se ha excedido el número máximo de documentos permitidos en el lote.
        public const int BATCH_NUM_DOCUMENTS_EXCEEDED = 43;
        /// Se intenta firmar un lote sin documentos.
        public const int BATCH_NO_DOCUMENTS = 44;
        /// No se ha indicado el identificador del documento del lote.
        public const int PARAMETER_DOCUMENT_ID_NEEDED = 48;
        /// No se ha firmado previamente el lote.
        public const int BATCH_NO_SIGNED = 49;
        /// Error al firmar el lote.
        public const int BATCH_SIGNING = 50;
        /// La firma se recuperó anteriormente.
        public const int BATCH_RECOVERED = 51;
        /// Se requiere esperar un periodo de gracia para recuperar el documento.
        public const int BATCH_DOCUMENT_GRACE_PERIOD = 52;
        /// El documento no estaba en el lote.
        public const int BATCH_INVALID_DOCUMENT = 53;
        /// El resultado del lote se recuperó anteriormente.
        public const int BATCH_RESULT_RECOVERED = 54;

        /// Error interno del servidor.
        public const int INTERNAL_ERROR = 500;
        /// Petición rechazada.
        public const int FORBIDDEN = 501;
        /// No se proporcionaron los parámetros de autenticación o no son correctos.
        public const int UNAUTHORIZED = 502;
        /// La transacción no se ha inicializado o ha caducado.
        public const int INVALID_TRANSACTION = 503;
        /// Error detectado después de llamar a la pasarela externa para autenticar al usuario.
        public const int EXTERNAL_SERVICE_ERROR_TO_LOGIN = 504;
        /// Error detectado después de llamar a la pasarela externa para firmar.
        public const int EXTERNAL_SERVICE_ERROR_TO_SIGN = 505;
        /// Operación cancelada.
        public const int OPERATION_CANCELED = 507;
        /// El proveedor de firma devolvió un error.
        public const int PROVIDER_ERROR = 508;
        /// No se pudo conectar con el proveedor de firma.
        public const int PROVIDER_INACCESIBLE_SERVICE = 510;
    }
}
