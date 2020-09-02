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