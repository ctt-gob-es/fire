using FIRe;
using System;

public partial class example_fire_add_document_batch : System.Web.UI.Page
{
    protected void Page_Load(object sender, EventArgs e)
    {
        
        string conf = "redirectOkUrl=http://www.google.es\n" +	// URL a la que llegara si el usuario se autentica correctamente
                      "redirectErrorUrl=http://www.ibm.com";        // URL a la que llegara si ocurre algun error o el usuario no se autentica correctamente
        string confB64 = Base64Encode(conf);
        string dataB64 = Base64Encode("Hola Mundo!!");
        string appId = "196647C3A40B"; // Identificador de la aplicacion (dada de alta previamente en el sistema)
        string transactionId = "3b5a7509-dd2d-4571-a6f8-8746c87b1eda";

        FireClient client;

        try
        {
            client = new FireClient(appId);
        }
        catch (Exception ex)
        {
            FileId1.Text = "No se pudo cargar el cliente de FIRe: " + ex.Message;
            FileId2.Text = "No se pudo cargar el cliente de FIRe: " + ex.Message;
            return;
        }

        try
        {
            client.addDocumentToBatch(
                transactionId,  // Identificador de transaccion
                "00001",        // Identificador del usuario
                "1",            // Identificador del documento
                dataB64,        // Documento a incluir
                confB64         // Configuracion del servicio en base 64 (se incluyen las URL a las que redirigir en caso de exito y error)
            );
/*
            FireApi.addDocumentToBatch(
                appId,          // Identificador de aplicacion
                transactionId,  // Identificador de transaccion
                "1",            // Identificador del documento
                dataB64,        // Documento a incluir
                confB64         // Configuracion del servicio en base 64 (se incluyen las URL a las que redirigir en caso de exito y error)
            );
*/
            FileId1.Text = "1";
        }
        catch (Exception ex)
        {
            FileId1.Text = ex.Message;
        }

        string extraParams = "mode=implicit\nfilters=keyusage.nonrepudiation:true;nonexpired:";
        string extraParamsB64 = Base64Encode(extraParams);

        try
        {
            client.addDocumentToBatch(
                transactionId,  // Identificador de transaccion
                "00001",        // Identificador del usuario
                "2",            // Identificador del documento
                dataB64,        // Documento a incluir
                "sign",         // Operacion criptografica (sign, cosign o countersign)
                "XAdES",        // Formato de firma (CAdES, XAdES, PAdES...)
                extraParamsB64, // Configuracion del formato de firma en base 64 (propiedades). El equivalente al extraParams de Autofirma
                "ES-A",         // Formato longevo
                confB64         // Configuracion del servicio en base 64 (se incluyen las URL a las que redirigir en caso de exito y error)
            );
/*
            FireApi.addDocumentToBatch(
                appId,
                transactionId,  // Identificador de transaccion
                "2",            // Identificador del documento
                dataB64,        // Documento a incluir
                confB64         // Configuracion del servicio en base 64 (se incluyen las URL a las que redirigir en caso de exito y error)
            );
*/

            FileId2.Text = "2";
        }
        catch (Exception ex)
        {
            FileId2.Text = ex.Message;
        }


        try
        {
            client.addDocumentToBatch(
                transactionId,  // Identificador de transaccion
                "00001",        // Identificador del usuario
                "3",            // Identificador del documento
                "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",        // Documento a incluir
                "countersign",         // Operacion criptografica (sign, cosign o countersign)
                "CAdES",        // Formato de firma (CAdES, XAdES, PAdES...)
                extraParamsB64, // Configuracion del formato de firma en base 64 (propiedades). El equivalente al extraParams de Autofirma
                null,         // Formato longevo
                confB64         // Configuracion del servicio en base 64 (se incluyen las URL a las que redirigir en caso de exito y error)
            );

            FileId3.Text = "3";
        }
        catch (Exception ex)
        {
            FileId3.Text = ex.Message;
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