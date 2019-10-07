using FIRe;
using System;

public partial class example_fire_recover_batch_sign : System.Web.UI.Page
{
    protected void Page_Load(object sender, EventArgs e)
    {
        string dataB64 = Base64Encode("Hola Mundo!!");

        // Funcion del API de Clave Firma para cargar los datos a firmar
        string appId = "B244E473466F";
        string transactionId = "1bdbb621-fa77-4dea-8ff8-602400fd033b";
        FireClient fireClient = new FireClient(appId);

        FireTransactionResult signature1;
        try
        {
            signature1 = fireClient.recoverBatchSign( // Identificador de la aplicacion (dada de alta previamente en el sistema)
                transactionId,  // Identificador de transaccion recuperado en la operacion createBatch()
                "00001",        // Identificador del usuario
                "1"             // Identificador del documento del que se quiere obtener la firma
            );

            // Mostramos los datos obtenidos
            DocumentSignature1.Text = System.Convert.ToBase64String(signature1.getResult());
        }
        catch (Exception ex)
        {
            DocumentSignature1.Text = ex.Message;
        }

        FireTransactionResult signature2;
        try
        {
            signature2 = fireClient.recoverBatchSign( // Identificador de la aplicacion (dada de alta previamente en el sistema)
                transactionId,  // Identificador de transaccion recuperado en la operacion createBatch()
                "00001",        // Identificador del usuario
                "2"             // Identificador del documento del que se quiere obtener la firma
            );

            // Mostramos los datos obtenidos
            DocumentSignature2.Text = System.Convert.ToBase64String(signature2.getResult());
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