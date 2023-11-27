using FIRe;
using System;

public partial class example_fire_sign_batch : System.Web.UI.Page
{
    protected void Page_Load(object sender, EventArgs e)
    {
        string dataB64 = Base64Encode("Hola Mundo!!");

        // Funcion del API de Clave Firma para cargar los datos a firmar
        FireLoadResult result;
        string appId = "196647C3A40B"; // Identificador de la aplicacion (dada de alta previamente en el sistema)
        string transactionId = "844b2698-6595-4698-a9c1-7682b7182fa7";
        try
        {
            result = new FireClient(appId).signBatch(
                transactionId,  // Identificador de transaccion generado en la funcion createBatch()
                "00001",        // Identificador del usuario
                false           // Indica si debe detenerse al encontrar un error
            );

/*
            result = FireApi.signBatch(
                "196647C3A40B", // Identificador de la aplicacion (dada de alta previamente en el sistema)
                transactionId,  // Identificador de transaccion generado en la funcion createBatch()
                false           // Indica si debe detenerse al encontrar un error
            );
*/
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