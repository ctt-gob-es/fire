using FIRe;
using System;

public partial class example_fire_recover_batch_result : System.Web.UI.Page
{
    protected void Page_Load(object sender, EventArgs e)
    {
        // Funcion del API de Clave Firma para cargar los datos a firmar
        FireBatchResult batchResult;
        string appId = "B244E473466F";
        string transactionId = "1bdbb621-fa77-4dea-8ff8-602400fd033b";
        try
        {
            batchResult = new FireClient(appId).recoverBatchResult( // Identificador de la aplicacion (dada de alta previamente en el sistema)
                transactionId,   // Identificador de transaccion recuperado en la operacion createBatch()
                "00001"        // Identificador del usuario
            );
/*
            batchResult = FireApi.recoverBatchResult(
                appId,          // Identificador de la aplicacion (dada de alta previamente en el sistema)
                transactionId   // Identificador de transaccion recuperado en la operacion createBatch()
            );
*/
        }
        catch (Exception ex)
        {
            Result1.Text = ex.Message;
            return;
        }

        // Mostramos los datos obtenidos
        ProviderName.Text = batchResult.prov;
        CertB64.Text = System.Convert.ToBase64String(batchResult.cert.GetRawCertData());
        Result1.Text = batchResult.batch[0].id + " - " + batchResult.batch[0].ok + " - " + batchResult.batch[0].dt;
        Result2.Text = batchResult.batch[1].id + " - " + batchResult.batch[1].ok + " - " + batchResult.batch[1].dt;
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