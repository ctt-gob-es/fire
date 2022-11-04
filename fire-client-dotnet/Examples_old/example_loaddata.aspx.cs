using FIRe;
using System;

public partial class example_loaddata : System.Web.UI.Page
{
    protected void Page_Load(object sender, EventArgs e)
    {

        // Identificador de la aplicacion (dada de alta previamente en el sistema)
        //string appId = "0DC38D5E5D68"; // Preproduccion
        string appId = "B244E473466F";   // Local

        string subjectId = "00001";     // DNI de la persona

        string dataB64 = Base64Encode("Hola Mundo!!");
        string conf = "redirectOkUrl=http://www.google.es\n" +	// URL a la que llegara si el usuario se autentica correctamente
                      "redirectErrorUrl=http://www.ibm.com";        // URL a la que llegara si ocurre algun error o el usuario no se autentica correctamente
        string confB64 = Base64Encode(conf);

        string provider = "clavefirmatest";     // Proveedor de firma en la nube

        // Funcion del API de Clave Firma para cargar los datos a firmar
        LoadResult loadResult;
        try
        {
            loadResult = HttpLoadProcess.loadData(
                appId,          // Identificador de la aplicacion (dada de alta previamente en el sistema)
                subjectId,      // DNI de la persona
                "sign",         // Operacion criptografica (sign, cosign o countersign)
                "CAdES",        // Formato de firma (CAdES, XAdES, PAdES...)
                "SHA256withRSA",// Algoritmo de firma (Actualmente solo se permite SHA1withRSA)
                null,           // Configuracion del formato de firma en base 64. El equivalente al extraParams del MiniApplet de @firma
                                // Certificado de firma
                "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",
                dataB64,        // Datos a firmar
                confB64,        // Configuracion del servicio en base 64 (se incluyen las URL a las que redirigir en caso de exito y error)
                provider
            );
        }
        catch (Exception ex)
        {
            TransactionId.Text = ex.Message;
            return;
        }

        // Mostramos los datos obtenidos
        TransactionId.Text = loadResult.getTransactionId();
        RedirectionURL.Text = loadResult.getRedirectUrl();
        TriphaseData.Text = loadResult.getTriphaseData();
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