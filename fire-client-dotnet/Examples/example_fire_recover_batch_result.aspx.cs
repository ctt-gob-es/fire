using FIRe;
using System;
using System.Globalization;

public partial class example_fire_recover_batch_result : System.Web.UI.Page
{
    protected void Page_Load(object sender, EventArgs e)
    {
        // Funcion del API de Clave Firma para cargar los datos a firmar
        FireBatchResult batchResult;
        string appId = "B244E473466F";
        string transactionId = "5e06f0bb-b812-40fb-87af-76770feed597";
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

        Result1.Text =  "Id: " + batchResult.batch[0].id +
                        "<br>Resultado: " + batchResult.batch[0].ok +
                        "<br>Detalle: " + GetDetailText(batchResult.batch[0]);

        Result2.Text =  "Id: " + batchResult.batch[1].id +
                        "<br>Resultado: " + batchResult.batch[1].ok +
                        "<br>Detalle: " + GetDetailText(batchResult.batch[1]);
    }

    /// <summary>Codifica en base64</summary>
    /// <param name="plainText">string a codificar.</param>
    /// <returns>string codificado en base 64 </returns>
    private static string Base64Encode(string plainText)
    {
        var plainTextBytes = System.Text.Encoding.UTF8.GetBytes(plainText);
        return System.Convert.ToBase64String(plainTextBytes);
    }

    /// <summary>Devuelve el texto con el detalle de la operación o, si existía, con la información
    /// del periodo de gracia remitido.</summary>
    /// <param name="result">Resultado de una firma del lote.</param>
    /// <returns>Información adicional de la firma.</returns>
    private string GetDetailText(FireSingleResult result)
    {
        string detail;
        if (result.gracePeriod != null)
        {
            detail = "ID periodo de gracia: " + result.gracePeriod.Id +
                     "  -  Fecha recogida: " + result.gracePeriod.Date.ToString("r", CultureInfo.CreateSpecificCulture("es-ES"));
        }
        else
        {
            detail = result.dt;
        }
        return detail;
    }
}