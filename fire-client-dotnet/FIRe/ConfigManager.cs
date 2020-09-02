/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */

using Microsoft.Win32;

namespace FIRe
{
    /// <summary>Clase para la gestor de la configuración del componente distribuido.</summary>
   public static class ConfigManager
    {

        // Valores de acceso al registro para la configuracion del servicio
        private static readonly string REGISTRY_KEY_CLAVEFIRMA = "SOFTWARE\\ClaveFirma";
        private static readonly string REGISTRY_VALUE_LIST_CERTS_SERVICE = "certificates_service";
        private static readonly string REGISTRY_VALUE_GENERATE_CERT_SERVICE = "generate_cert_service";
        private static readonly string REGISTRY_VALUE_RECOVER_CERT_SERVICE = "recover_cert_service";
        private static readonly string REGISTRY_VALUE_LOAD_SERVICE_VALUE = "load_service";
        private static readonly string REGISTRY_VALUE_SIGN_SERVICE = "sign_service";
        private static readonly string REGISTRY_KEY_FIRE = "SOFTWARE\\FIRe";
        private static readonly string REGISTRY_VALUE_FIRE_SERVICE = "fire_service";
        private static readonly string REGISTRY_VALUE_ADMIT_ALL_CERTS = "admit_all_certs";
        private static readonly string REGISTRY_VALUE_SSL_CLIENT_PKCS12 = "ssl_client_pkcs12";
        private static readonly string REGISTRY_VALUE_SSL_CLIENT_PASS = "ssl_client_pass";
        private static readonly string REGISTRY_VALUE_SSL_CLIENT_ALIAS = "ssl_client_alias";
        
        /// <summary>
        /// Obtiene del registro la URL para hacer uso de los servicios de clavefirma v2 (FIRe).
        /// </summary>
        /// <returns>URL configurada en el registro.</returns>
        public static string getFireService()
        {
            return getFireRegistryKey(REGISTRY_VALUE_FIRE_SERVICE);
        }

        /// <summary>
        /// Obtiene del registro la URL para listar los certificados de firma.
        /// </summary>
        /// <returns>URL configurada en el registro.</returns>
        public static string getUrlListCertsService()
        {
            return getLegacyRegistryKey(REGISTRY_VALUE_LIST_CERTS_SERVICE);
        }

        /// <summary>
        /// Obtiene del registro la URL para generar un nuevo certificado de firma.
        /// </summary>
        /// <returns>URL configurada en el registro.</returns>
        public static string getUrlGenerateCertService()
        {
            return getLegacyRegistryKey(REGISTRY_VALUE_GENERATE_CERT_SERVICE);
        }

        /// <summary>
        /// Obtiene del registro la URL para recuperar el nuevo certificado de firma.
        /// </summary>
        /// <returns>URL configurada en el registro.</returns>
        public static string getUrlRecoverCertService()
        {
            return getLegacyRegistryKey(REGISTRY_VALUE_RECOVER_CERT_SERVICE);
        }

        /// <summary>
        /// Obtiene del registro la URL para cargar datos para firmar.
        /// </summary>
        /// <returns>URL configurada en el registro.</returns>
        public static string getUrlLoadService()
        {
            return getLegacyRegistryKey(REGISTRY_VALUE_LOAD_SERVICE_VALUE);
        }

        /// <summary>
        /// Obtiene del registro la URL para firmar.
        /// </summary>
        /// <returns>URL configurada en el registro.</returns>
        public static string getUrlSignService()
        {
            return getLegacyRegistryKey(REGISTRY_VALUE_SIGN_SERVICE);
        }

        /// <summary>
        /// Obtiene del registro si se debe admitir cualquier certificado SSL servidor.
        /// </summary>
        /// <returns>true si no se deben realizar comprobaciones sobre el certificado SSL servidor,
        /// false en caso contrario.</returns>
        public static string getSSLAdmitAllCerts()
        {
            return getLegacyRegistryKey(REGISTRY_VALUE_ADMIT_ALL_CERTS);
        }

        /// <summary>
        /// Obtiene del registro la ruta del almacén de claves del certificado SSL cliente.
        /// </summary>
        /// <returns>Ruta del almacén PKCS#12 con el certificado y clave SSL cliente.</returns>
        public static string getSSLClientPkcs12()
        {
            return getLegacyRegistryKey(REGISTRY_VALUE_SSL_CLIENT_PKCS12);
        }

        /// <summary>
        /// Obtiene del registro la contraseña del almacén del certificado cliente SSL.
        /// </summary>
        /// <returns>Contraseña del almacén o <code>null</code> si no se configuró.</returns>
        public static string getSSLClientPass()
        {
            return getLegacyRegistryKey(REGISTRY_VALUE_SSL_CLIENT_PASS);
        }

        /// <summary>
        /// Obtiene del registro el alias  del certificado cliente SSL.
        /// </summary>
        /// <returns>Alias del certificado o <code>null</code> si no se configuró.</returns>
        public static string getSSLClientAlias()
        {
            return getLegacyRegistryKey(REGISTRY_VALUE_SSL_CLIENT_ALIAS);
        }

        /// <summary>
        /// Obtiene un valor del registro de Windows.
        /// </summary>
        /// <param name="key">Clave de registro</param>
        /// /// <param name="valueName">Nombre del valor de registro</param>
        /// <returns>Valor del registro o nulo si no se indicó.</returns>
        private static string getRegistryKey(string key, string valueName)
        {
            RegistryKey masterKey = Registry.CurrentUser.OpenSubKey(key);
            if (masterKey == null)
            {
                return null;
            }
            string value = (string)masterKey.GetValue(valueName);

            masterKey.Close();

            return value;
        }

        /// <summary>
        /// Obtiene un valor de configuración de FIRe.
        /// </summary>
        /// <param name="valueName">Nombre del valor de registro</param>
        /// <returns>Valor del registro o nulo si no se encontró configurado.</returns>
        private static string getFireRegistryKey(string valueName)
        {
            return getRegistryKey(REGISTRY_KEY_FIRE, valueName);
        }

        /// <summary>
        /// Obtiene un valor de configuración antiguo de FIRe. Estos se buscaran primero
        /// en la nueva clave de registro de FIRe y, si no se encuentra, en las claves de registro
        /// que se utilizaban anteriormente.
        /// </summary>
        /// <param name="valueName">Nombre del valor de registro</param>
        /// <returns>Valor del registro o nulo si no se encontró configurado.</returns>
        private static string getLegacyRegistryKey(string valueName)
        {
            string value = getFireRegistryKey(valueName);
            if (string.IsNullOrEmpty(value))
            {
                value = getRegistryKey(REGISTRY_KEY_CLAVEFIRMA, valueName);
            }
            return value;
        }
}
}
