using FIRe;
using System;

public partial class example_fire_recover_error : System.Web.UI.Page
{
    protected void Page_Load(object sender, EventArgs e)
    {
        string dataB64 = Base64Encode("Hola Mundo!!");

        // Funcion del API de Clave Firma para cargar los datos a firmar
        FireTransactionResult signature;
        string transactionId = "bba8106e-5259-4cff-9a1b-cd9d36d4d527";
        try
        {
            signature = new FireClient("A418C37E84BA").recoverError( // Identificador de la aplicacion (dada de alta previamente en el sistema)
                transactionId,  // Identificador de transaccion
                "00001"         // Identificador del usuario
            );

            /*
            signature = FireApi.recoverError(
                "A418C37E84BA", // Identificador de la aplicacion (dada de alta previamente en el sistema)
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