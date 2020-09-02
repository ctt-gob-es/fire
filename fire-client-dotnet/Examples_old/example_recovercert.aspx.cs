using FIRe;
using System;
using System.Security.Cryptography.X509Certificates;

public partial class example_recovercert : System.Web.UI.Page
{
    protected void Page_Load(object sender, EventArgs e)
    {
        // Identificador de la aplicacion (dada de alta previamente en el sistema)
        //string appId = "0DC38D5E5D68"; // Preproduccion
        string appId = "B244E473466F";   // Local

        string transactionId = "98c60d94-99d9-400b-819a-0339cb2050b0";     // DNI de la persona

        string provider = "clavefirmatest";     // Proveedor de firma en la nube

        X509Certificate cert;
        try
        {
            cert = HttpGenerateCertificate.recoverCertificate(
                appId,          // Identificador de la aplicacion (dada de alta previamente en el sistema)
                transactionId,  // Identificador de transaccion recuperado en la operacion generateCertificate()
                provider
            );
        }
        catch (Exception ex)
        {
            Cert1B64.Text = "Error durante la operacion: " + ex.Message;
            return;
        }
        X509Certificate2 cert2 = new X509Certificate2(cert);

        // Recorremos el listado de certificados
        Cert1B64.Text = System.Convert.ToBase64String(cert.GetRawCertData());

        Cert1SubjectCN.Text = cert2.GetNameInfo(X509NameType.SimpleName, false);
        Cert1IssuerCN.Text = cert2.GetNameInfo(X509NameType.SimpleName, true);

        Cert1ExpirationDate.Text = cert2.GetExpirationDateString();
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