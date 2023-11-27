using FIRe;
using System;

public partial class example_fire_recover_error : System.Web.UI.Page
{
    protected void Page_Load(object sender, EventArgs e)
    {
        string dataB64 = Base64Encode("Hola Mundo!!");

        // Funcion del API de Clave Firma para cargar los datos a firmar
        FireTransactionResult signature;
        string appId = "196647C3A40B";
        string transactionId = "2602747f-2dbc-466f-9815-3187a7fb1249";
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