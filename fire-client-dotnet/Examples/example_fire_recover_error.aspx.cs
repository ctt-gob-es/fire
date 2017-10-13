using FIRe;
using System;

public partial class example_fire_recover_error : System.Web.UI.Page
{
    protected void Page_Load(object sender, EventArgs e)
    {
        string dataB64 = Base64Encode("Hola Mundo!!");

        // Funcion del API de Clave Firma para cargar los datos a firmar
        FireTransactionResult signature;
        string transactionId = "6a8ee913-20b5-4fd7-94d2-4b9dc19b0849";
        try
        {
            signature = new FireClient("A418C37E84BA").recoverError( // Identificador de la aplicacion (dada de alta previamente en el sistema)
                transactionId,  // Identificador de transaccion
                "00001"         // Identificador del usuario
            );
        }
        catch (Exception ex)
        {
            Error.Text = ex.Message;
            return;
        }

        // Mostramos los datos obtenidos
        Error.Text = signature.getErrorCode() + " - " + signature.getErrorMessage();
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