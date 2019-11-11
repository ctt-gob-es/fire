using FIRe;
using System;

public partial class example_fire_create_batch : System.Web.UI.Page
{
    protected void Page_Load(object sender, EventArgs e)
    {
        string appId = "B244E473466F";
        string conf = "redirectOkUrl=http://www.google.es\n" +	// URL a la que llegara si el usuario se autentica correctamente
                      "redirectErrorUrl=http://www.ibm.com";        // URL a la que llegara si ocurre algun error o el usuario no se autentica correctamente
        string confB64 = Base64Encode(conf);
        string extraParams = "mode=implicit";
        string extraParamsB64 = Base64Encode(extraParams);
        string upgradeFormat = null;

        // Funcion del API de Clave Firma para cargar los datos a firmar
        FireTransactionIdResult loadResult;
        try
        {
            loadResult = new FireClient(appId).createBatchProcess( // Identificador de la aplicacion (dada de alta previamente en el sistema)
                "00001",        // DNI de la persona
                "sign",         // Operacion criptografica (sign, cosign o countersign)
                "CAdES",        // Formato de firma (CAdES, XAdES, PAdES...)
                "SHA1withRSA",  // Algoritmo de firma (Actualmente solo se permite SHA1withRSA)
                extraParamsB64, // Configuracion del formato de firma en base 64 (propiedades). El equivalente al extraParams del MiniApplet de @firma
                upgradeFormat,  // Actualizacion
                confB64         // Configuracion del servicio en base 64 (se incluyen las URL a las que redirigir en caso de exito y error)
            );
        }
        catch (Exception ex)
        {
            TransactionId.Text = ex.Message;
            return;
        }

        // Mostramos los datos obtenidos
        TransactionId.Text = loadResult.getTransactionId();
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