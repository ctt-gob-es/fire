using FIRe;
using System;

public partial class example_fire_recover_sign : System.Web.UI.Page
{
    protected void Page_Load(object sender, EventArgs e)
    {
        // Funcion del API de Clave Firma para cargar los datos a firmar
        FireTransactionResult signature;
        string transactionId = "2b996a27-3622-4923-903b-f390cce98cfc";
        string appId = "B244E473466F";
        try
        {
            
            signature = new FireClient(appId).recoverSign( // Identificador de la aplicacion (dada de alta previamente en el sistema)
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
            SignatureB64.Text = ex.Message;
            return;
        }

        // Mostramos los datos obtenidos
        Provider.Text = signature.getProviderName();
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