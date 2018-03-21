using FIRe;
using System;

public partial class example_fire_sign_batch : System.Web.UI.Page
{
    protected void Page_Load(object sender, EventArgs e)
    {
        string dataB64 = Base64Encode("Hola Mundo!!");

        // Funcion del API de Clave Firma para cargar los datos a firmar
        FireLoadResult result;
        string transactionId = "d9ff68d2-71cb-47ea-87b9-497b66c3b3e7";
        try
        {
            result = new FireClient("B244E473466F").signBatch( // Identificador de la aplicacion (dada de alta previamente en el sistema)
                transactionId,  // Identificador de transaccion generado en la funcion createBatch()
                "00001",        // Identificador del usuario
                false           // Indica si debe detenerse al encontrar un error
            );
        }
        catch (Exception ex)
        {
            TransactionId.Text = ex.Message;
            return;
        }

        // Mostramos los datos obtenidos
        TransactionId.Text = result.getTransactionId();
        RedirectURL.Text = result.getRedirectUrl();
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