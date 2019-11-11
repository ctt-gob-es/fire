using FIRe;
using System;

public partial class example_fire_recover_error : System.Web.UI.Page
{
    protected void Page_Load(object sender, EventArgs e)
    {
        string dataB64 = Base64Encode("Hola Mundo!!");

        // Funcion del API de Clave Firma para cargar los datos a firmar
        FireTransactionResult signature;
        string appId = "B244E473466F";
        string transactionId = "47bc7696-7199-4e46-86e9-0a23225262f3";
        try
        {
            signature = new FireClient(appId).recoverError( // Identificador de la aplicacion (dada de alta previamente en el sistema)
                transactionId,  // Identificador de transaccion
                "00001"         // Identificador del usuario
            );

            /*
            signature = FireApi.recoverError(
                appId, // Identificador de la aplicacion (dada de alta previamente en el sistema)
                transactionId   // Identificador de transaccion
            );
            */
        }
        catch (Exception ex)
        {
            ErrorMsg.Text = ex.Message;
            return;
        }

        // Mostramos los datos obtenidos
        Provider.Text = signature.ProviderName;
        ErrorMsg.Text = signature.ErrorCode + " - " + signature.ErrorMessage;
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