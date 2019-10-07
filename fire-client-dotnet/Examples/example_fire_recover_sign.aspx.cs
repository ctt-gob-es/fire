using FIRe;
using System;
using System.Collections.Generic;

public partial class example_fire_recover_sign : System.Web.UI.Page
{
    protected void Page_Load(object sender, EventArgs e)
    {

        Dictionary<string, string> serviceConfig = new Dictionary<string, string>();
        serviceConfig.Add("fire_service", "https://servidorcentral:8443/fire-signature/fireService");
        serviceConfig.Add("admit_all_certs", "true");
        serviceConfig.Add("ssl_client_pkcs12", "C:/Users/carlos.gamuci/Documents/FIRe/Ficheros_Despliegue/client_ssl_new.p12");
        serviceConfig.Add("ssl_client_pass", "12341234");

        // Funcion del API de Clave Firma para cargar los datos a firmar
        FireTransactionResult signature;
        string transactionId = "6db855d1-5985-482a-bdb1-ea03ef8e04f4";
        string appId = "B244E473466F";
        try
        {
            
            signature = new FireClient(appId, serviceConfig).recoverSign( // Identificador de la aplicacion (dada de alta previamente en el sistema)
                transactionId,  // Identificador de transaccion recuperado en la operacion createBatch()
                "00001",        // Identificador del usuario
                null            // Formato longevo
            );
            
            /*
            signature = FireApi.recoverSign(appId, // Identificador de la aplicacion (dada de alta previamente en el sistema)
                transactionId,  // Identificador de transaccion recuperado en la operacion createBatch()
                null            // Formato longevo
            );
            */
        }
        catch (Exception ex)
        {
            SignatureB64.Text = ex.ToString();
            return;
        }

        // Mostramos los datos obtenidos
        Provider.Text = signature.getProviderName();
        CertB64.Text = System.Convert.ToBase64String(signature.getSigningCert().GetRawCertData());
        SignatureB64.Text = System.Convert.ToBase64String(signature.getResult());
    }

    /// <summary>Codifica en base64</summary>
    /// <param name="plainText">string a codificar.</param>
    /// <returns>string codificado en base 64 </returns>
    private static string Base64Encode(string plainText)
    {
        var plainTextBytes = System.Text.Encoding.UTF8.GetBytes(plainText);
        return System.Convert.ToBase64String(plainTextBytes);
    }
}