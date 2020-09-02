using FIRe;
using System;

public partial class example_generatecert : System.Web.UI.Page
{
    protected void Page_Load(object sender, EventArgs e)
    {
        // Identificador de la aplicacion (dada de alta previamente en el sistema)
        //string appId = "0DC38D5E5D68"; // Preproduccion
        string appId = "B244E473466F";   // Local

        string subjectId = "00002";     // DNI de la persona

        string conf = "redirectOkUrl=http://www.google.es\n" +	// URL a la que llegara si el usuario se autentica correctamente
                      "redirectErrorUrl=http://www.ibm.com";    // URL a la que llegara si ocurre algun error o el usuario no se autentica correctamente
        string confB64 = Base64Encode(conf);

        string provider = "clavefirmatest";     // Proveedor de firma en la nube

        // Funcion del API de Clave Firma para cargar los datos a firmar
        GenerateCertificateResult generateResult;
        try
        {
            generateResult = HttpGenerateCertificate.generateCertificate(
                appId,          // Identificador de la aplicacion (dada de alta previamente en el sistema)
                subjectId,      // DNI de la persona
                confB64,        // Configuracion del servicio en base 64 (se incluyen las URL a las que redirigir en caso de exito y error)
                provider        // Proveedor de firma en la nube
            );

            // Mostramos los datos obtenidos
            TransactionId.Text = generateResult.getTransactionId();
            RedirectionURL.Text = generateResult.getRedirectUrl();
        }
        catch (HttpCertificateAvailableException)
        {
            ErrorMsg.Text = "El usuario ya tiene certificados generados";
        }
        catch (HttpWeakRegistryException)
        {
            ErrorMsg.Text = "El usuario realizó un registro débil y no puede tener certificados de firma";
        }
        catch (HttpNoUserException)
        {
            ErrorMsg.Text = "El usuario seleccionado no existe";
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