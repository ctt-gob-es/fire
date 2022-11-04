using FIRe;
using System;

public partial class example_fire_recover_batch_sign : System.Web.UI.Page
{
    protected void Page_Load(object sender, EventArgs e)
    {
        string dataB64 = Base64Encode("Hola Mundo!!");

        // Funcion del API de Clave Firma para cargar los datos a firmar
        string appId = "B244E473466F"; // Identificador de la aplicacion (dada de alta previamente en el sistema)
        string transactionId = "844b2698-6595-4698-a9c1-7682b7182fa7";
        FireClient fireClient = new FireClient(appId);

        FireTransactionResult signature1;
        try
        {
            signature1 = fireClient.recoverBatchSign(
                transactionId,  // Identificador de transaccion recuperado en la operacion createBatch()
                "00001",        // Identificador del usuario
                "1"             // Identificador del documento del que se quiere obtener la firma
            );

/*
            signature1 = FireApi.recoverBatchSign(
                appId,          // Identificador de la aplicacion (dada de alta previamente en el sistema)
                transactionId,  // Identificador de transaccion recuperado en la operacion createBatch()
                "1"             // Identificador del documento del que se quiere obtener la firma
            );
*/
            // Mostramos los datos obtenidos
            DocumentSignature1.Text = System.Convert.ToBase64String(signature1.Result);
        }
        catch (Exception ex)
        {
            DocumentSignature1.Text = ex.Message;
        }

        FireTransactionResult signature2;
        try
        {
            signature2 = fireClient.recoverBatchSign(
                transactionId,  // Identificador de transaccion recuperado en la operacion createBatch()
                "00001",        // Identificador del usuario
                "2"             // Identificador del documento del que se quiere obtener la firma
            );
/*
            signature2 = FireApi.recoverBatchSign(
                appId,          // Identificador de la aplicacion (dada de alta previamente en el sistema)
                transactionId,  // Identificador de transaccion recuperado en la operacion createBatch()
                "2"             // Identificador del documento del que se quiere obtener la firma
            );
*/

            // Mostramos los datos obtenidos
            DocumentSignature2.Text = System.Convert.ToBase64String(signature2.Result);
        }
        catch (Exception ex)
        {
            DocumentSignature2.Text = ex.Message;
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