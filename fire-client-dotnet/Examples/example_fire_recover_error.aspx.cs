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
        string transactionId = "1bdbb621-fa77-4dea-8ff8-602400fd033b";
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
        Provider.Text = signature.getProviderName();
        ErrorMsg.Text = signature.getErrorCode() + " - " + signature.getErrorMessage();
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