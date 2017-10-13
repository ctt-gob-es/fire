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
        string transactionId = "2c78060f-68fa-410c-9469-bdcc92bc4ff7";

        FireClient client;

        try
        {
            client = new FireClient("A418C37E84BA"); // Identificador de la aplicacion (dada de alta previamente en el sistema)
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
                "00001",        // DNI de la persona,
                "1",            // Identificador del documento
                dataB64,        // Documento a incluir
                confB64         // Configuracion del servicio en base 64 (se incluyen las URL a las que redirigir en caso de exito y error)
            );
            FileId1.Text = "1";
        }
        catch (Exception ex)
        {
            FileId1.Text = ex.Message;
        }

        try
        {
            client.addDocumentToBatch(
                transactionId,  // Identificador de transaccion
                "00001",        // Identificador del usuario
                "2",            // Identificador del documento
                dataB64,        // Documento a incluir
                "sign",         // Operacion criptografica (sign, cosign o countersign)
                "CAdES",        // Formato de firma (CAdES, XAdES, PAdES...)
                null,           // Configuracion del formato de firma en base 64 (propiedades). El equivalente al extraParams del MiniApplet de @firma
                null,
                confB64         // Configuracion del servicio en base 64 (se incluyen las URL a las que redirigir en caso de exito y error)
            );

            FileId2.Text = "2";
        }
        catch (Exception ex)
        {
            FileId2.Text = ex.Message;
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