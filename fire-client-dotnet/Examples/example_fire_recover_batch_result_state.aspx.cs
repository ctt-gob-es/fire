using FIRe;
using System;

public partial class example_fire_recover_batch_result_state : System.Web.UI.Page
{
    protected void Page_Load(object sender, EventArgs e)
    {
        string dataB64 = Base64Encode("Hola Mundo!!");

        // Funcion del API de Clave Firma para cargar los datos a firmar
        float batchResult;
        string transactionId = "98dff98d-ce09-430c-8b86-6ff1e451ff5a";
        try
        {
            batchResult = new FireClient("A418C37E84BA").recoverBatchResultState( // Identificador de la aplicacion (dada de alta previamente en el sistema)
                transactionId,  // Identificador de transaccion recuperado en la operacion createBatch()
                "00001"         // Identificador del usuario
            );
        }
        catch (Exception ex)
        {
            ProgressBatch.Text = ex.Message;
            return;
        }

        // Mostramos los datos obtenidos
        ProgressBatch.Text = batchResult + "";

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