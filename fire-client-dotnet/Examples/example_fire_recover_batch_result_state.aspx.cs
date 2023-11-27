using FIRe;
using System;

public partial class example_fire_recover_batch_result_state : System.Web.UI.Page
{
    protected void Page_Load(object sender, EventArgs e)
    {
        // Funcion del API de Clave Firma para cargar los datos a firmar
        float batchResult;
        string appId = "196647C3A40B"; // Identificador de la aplicacion (dada de alta previamente en el sistema)
        string transactionId = "e839c903-0dae-4ff9-9b52-d90f70069ce9";
        try
        {
            batchResult = new FireClient(appId).recoverBatchResultState(
                transactionId,  // Identificador de transaccion recuperado en la operacion createBatch()
                "00001"         // Identificador del usuario
            );
/*
            batchResult = FireApi.recoverBatchResultState(
                appId,          // Identificador de la aplicacion (dada de alta previamente en el sistema)
                transactionId  // Identificador de transaccion recuperado en la operacion createBatch()
            );
*/
        }
        catch (Exception ex)
        {
            ProgressBatch.Text = ex.Message;
            return;
        }

        // Mostramos los datos obtenidos
        ProgressBatch.Text = "" + batchResult;

    }
}