using FIRe;
using System;
using System.Collections.Generic;
using System.Globalization;

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
        string transactionId = "d17ddc97-6a71-491a-8e38-c3455bdf4bff";
        string appId = "B244E473466F";
        string upgradeFormat = "ES-T";
        try
        {
            signature = new FireClient(appId, serviceConfig).recoverSign( // Identificador de la aplicacion (dada de alta previamente en el sistema)
                transactionId,  // Identificador de transaccion recuperado en la operacion createBatch()
                "00001",        // Identificador del usuario
                upgradeFormat   // Formato longevo
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
        Provider.Text = signature.ProviderName;
        CertB64.Text = System.Convert.ToBase64String(signature.SigningCert.GetRawCertData());
        if (signature.ErrorCode != null)
        {
            SignatureB64.Text = "Error " + signature.ErrorCode + ": " + signature.ErrorMessage;
        }
        else if (signature.Result != null)
        {
            SignatureB64.Text = System.Convert.ToBase64String(signature.Result);
        }
        else if (signature.GracePeriod != null)
        {
            SignatureB64.Text = "ID periodo de gracia: " + signature.GracePeriod.Id +
                                "<br>Fecha recogida: " + signature.GracePeriod.Date.ToString("r", CultureInfo.CreateSpecificCulture("es-ES"));
        }
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