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

namespace FIRe
{
    /// <summary>
    /// Clase en la que almacenar la configuración de la aplicación. Cuando no se proporciona expresamente,
    /// se cargan los valores desde el registro de Windows. Los valores configurables, tanto a través de
    /// diccionario como del registro son:
    /// <list type="bullet">
    /// <item><term>fire_service:</term><description>URL del servicio del componente central.</description></item>
    /// <item><term>admit_all_certs:</term><description>Indica si se debe aceptar que la conexión SSL esté cifrada con cualquier
    /// certificado SSL (<code>true</code>) o que sólo con alguno cuya CA esté dada de alta en el almacén
    /// de certificados de confianza del sistema.</description></item>
    /// <item><term>ssl_client_pkcs12:</term><description>Ruta absoluta del almacén con el certificado de autenticación SSL cliente.</description></item>
    /// <item><term>ssl_client_pass:</term><description>Contraseña del almacén con el certificado de autenticación SSL cliente.</description></item>
    /// <item><term>ssl_client_alias:</term><description>Alias del certificado que se debe usar para la autenticación. Si no se indica, se usará el primero que se encuentre en el almacén.</description></item>
    /// </list>
    /// Los valores del registro de Windows se leen de las clave: <code>HKEY_CURRENT_USER\Software\FIRe</code>
    /// </summary>
    class FireConfig
    {
        private static readonly string KEY_FIRE_SERVICE = "fire_service";
        private static readonly string KEY_ADMIT_ALL_CERTS = "admit_all_certs";
        private static readonly string KEY_SSL_CLIENT_PKCS12 = "ssl_client_pkcs12";
        private static readonly string KEY_SSL_CLIENT_PASS = "ssl_client_pass";
        private static readonly string KEY_SSL_CLIENT_ALIAS = "ssl_client_alias";
        
        private readonly Dictionary<string, string> config;

        /// <summary>
        /// Construye el objeto de configuración cargando los valores desde el registro de Windows.
        /// </summary>
        /// <exception cref="ConfigureException">Cuando no se encuentra configurada la URL del servicio del
        /// componente central en el registro de Windows.</exception>
        public FireConfig()
        {
            if (String.IsNullOrEmpty(ConfigManager.getFireService()))
            {
                throw new ConfigureException("No se ha configurado en registro la URL del servicio del componente central");
            }

            this.config = new Dictionary<string, string>();
            this.config.Add(KEY_FIRE_SERVICE, ConfigManager.getFireService());
            this.config.Add(KEY_ADMIT_ALL_CERTS, ConfigManager.getSSLAdmitAllCerts());
            this.config.Add(KEY_SSL_CLIENT_PKCS12, ConfigManager.getSSLClientPkcs12());
            this.config.Add(KEY_SSL_CLIENT_PASS, ConfigManager.getSSLClientPass());
            this.config.Add(KEY_SSL_CLIENT_ALIAS, ConfigManager.getSSLClientAlias());
        }

        /// <summary>
        /// Construye el objeto de configuración cargando los valores proporcionados y, si no se han pasado, los
        /// valores del registro de Windows.
        /// </summary>
        /// <exception cref="ConfigureException">Cuando no se encuentra configurada la URL del servicio del
        /// componente central ni en el diccionario proporcionado ni en el registro de Windows.</exception>
        public FireConfig(Dictionary<string, string> config)
        {
            if ((config == null || !config.ContainsKey(KEY_FIRE_SERVICE) || String.IsNullOrEmpty(config[KEY_FIRE_SERVICE])) &&
                String.IsNullOrEmpty(ConfigManager.getFireService()))
            {
                throw new ConfigureException("No se ha proporcionado ni configurado en registro la URL del servicio del componente central");
            }

            this.config = new Dictionary<string, string>();
            initConfigKey(this.config, KEY_FIRE_SERVICE, config, ConfigManager.getFireService());
            initConfigKey(this.config, KEY_ADMIT_ALL_CERTS, config, ConfigManager.getSSLAdmitAllCerts());
            initConfigKey(this.config, KEY_SSL_CLIENT_PKCS12, config, ConfigManager.getSSLClientPkcs12());
            initConfigKey(this.config, KEY_SSL_CLIENT_PASS, config, ConfigManager.getSSLClientPass());
            initConfigKey(this.config, KEY_SSL_CLIENT_ALIAS, config, ConfigManager.getSSLClientAlias());
        }

        /// <summary>
        /// Inicializa una de las variables de la configuración del sistema con el valor
        /// configurado en un diccionario o un valor por defecto si no existia el anterior.
        /// </summary>
        /// <param name="config">Objeto en el que almacenar la configuración.</param>
        /// <param name="key">Clave del parámetro que se desea configurar.</param>
        /// <param name="defaultConfig">Objeto con la configuración por defecto.</param>
        /// <param name="defaultValue">Valor por defecto.</param>
        private static void initConfigKey(Dictionary<string, string> config, string key,
            Dictionary<string, string> defaultConfig, string defaultValue)
        {

            if (defaultConfig.ContainsKey(key))
            {
                config.Add(key, defaultConfig[key]);
            }
            else if (defaultValue != null)
            {
                config.Add(key, defaultValue);
            }
        }

        /// <summary>
        /// Obtiene del registro la URL para hacer uso de los servicios de clavefirma v2 (FIRe).
        /// </summary>
        /// <returns>URL configurada en el registro.</returns>
        public string getFireService()
        {
            return this.config.ContainsKey(KEY_FIRE_SERVICE) ? this.config[KEY_FIRE_SERVICE] : null;
        }

        /// <summary>
        /// Obtiene si se debe admitir cualquier certificado SSL servidor.
        /// </summary>
        /// <returns>true si no se deben realizar comprobaciones sobre el certificado SSL servidor,
        /// false en caso contrario.</returns>
        public string getSSLAdmitAllCerts()
        {
            return this.config.ContainsKey(KEY_ADMIT_ALL_CERTS) ? this.config[KEY_ADMIT_ALL_CERTS] : null;
        }

        /// <summary>
        /// Obtiene la ruta del almacén de claves del certificado SSL cliente.
        /// </summary>
        /// <returns>Ruta del almacén PKCS#12 con el certificado y clave SSL cliente.</returns>
        public string getSSLClientPkcs12()
        {
            return this.config.ContainsKey(KEY_SSL_CLIENT_PKCS12) ? this.config[KEY_SSL_CLIENT_PKCS12] : null;
        }

        /// <summary>
        /// Obtiene la contraseña del almacén del certificado cliente SSL.
        /// </summary>
        /// <returns>Contraseña del almacén.</returns>
        public string getSSLClientPass()
        {
            return this.config.ContainsKey(KEY_SSL_CLIENT_PASS) ? this.config[KEY_SSL_CLIENT_PASS] : null;
        }

        /// <summary>
        /// Obtiene el alias del certificado cliente SSL.
        /// </summary>
        /// <returns>Alias del certificado cliente SSL.</returns>
        public string getSSLClientAlias()
        {
            return this.config.ContainsKey(KEY_SSL_CLIENT_ALIAS) ? this.config[KEY_SSL_CLIENT_ALIAS] : null;
        }

        public Dictionary<string, string> getConfig()
        {
            return this.config;
        }
    }
}
