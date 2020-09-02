using FIRe;
using System;
using System.Collections.Generic;
using System.Security.Cryptography.X509Certificates;


public partial class example_getlist : System.Web.UI.Page
{
    
    protected void Page_Load(object sender, EventArgs e)
    {
        // Identificador de la aplicacion (dada de alta previamente en el sistema)
        //string appId = "0DC38D5E5D68"; // Preproduccion
        string appId = "B244E473466F";   // Local

        string subjectId = "00001";     // DNI de la persona

        string provider = "clavefirmatest";     // Proveedor de firma en la nube

        // Funcion del API de Clave Firma para listar certificados
        List<X509Certificate> certs;
        try
        {
            certs = HttpCertificateList.getList(appId, subjectId, provider);
        }
        catch (HttpCertificateBlockedException)
        {
            Cert1B64.Text = "Los certificados del usuario están bloqueados";
            return;
        }
        catch (HttpWeakRegistryException)
        {
            Cert1B64.Text = "El usuario realizó un registro débil y no puede tener certificados de firma";
            return;
        }
        catch (HttpNoUserException)
        {
            Cert1B64.Text = "El usuario no esta dado de alta en el sistema";
            return;
        }
        catch (Exception ex)
        {
            Cert1B64.Text = "Error durante la operacion: " + ex;
            return;
        }

        if (certs == null || certs.Count == 0)
        {
            Cert1B64.Text = "No se ha encontrado ningun certificado";
        }
        else
        {
            // Recogemos el contenido del primer certificado
            X509Certificate2 cert = new X509Certificate2(certs[0]);

            Cert1B64.Text = System.Convert.ToBase64String(cert.GetRawCertData());

            Cert1SubjectCN.Text = cert.GetNameInfo(X509NameType.SimpleName, false);
            Cert1IssuerCN.Text = cert.GetNameInfo(X509NameType.SimpleName, true);

            Cert1ExpirationDate.Text = cert.GetExpirationDateString();
        }
        
    }
}