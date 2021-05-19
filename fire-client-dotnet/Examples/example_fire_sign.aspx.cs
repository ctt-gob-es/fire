using FIRe;
using System;
using System.Collections.Generic;

public partial class example_fire_sign : System.Web.UI.Page
{
    protected void Page_Load(object sender, EventArgs e)
    {

        string appId = "B244E473466F";

        //Dictionary<String, String> serviceConfig = new Dictionary<string, string>();
        //serviceConfig.Add("fire_service", "https://servidorcentral:8443/fire-signature/fireService");
        //serviceConfig.Add("admit_all_certs", "true");
        //serviceConfig.Add("ssl_client_pkcs12", "C:/Users/carlos.gamuci/Documents/FIRe/Ficheros_Despliegue/client_ssl_new.p12");
        //serviceConfig.Add("ssl_client_pass", "12341234");

        string extraParams = "mode=implicit\nfilters=keyusage.nonrepudiation:true;nonexpired:";
        string extraParamsB64 = Base64Encode(extraParams);

        string dataB64 = Base64Encode("Hola Mundo!!");
        //string dataB64 = Base64Encode("<xml><hola>¡¡Hola Mundo con eñe!!</hola></xml>");

        string conf = "redirectOkUrl=http://www.google.es\n" +	// URL a la que llegara si el usuario se autentica correctamente
                      "redirectErrorUrl=http://www.ibm.com";        // URL a la que llegara si ocurre algun error o el usuario no se autentica correctamente
        string confB64 = Base64Encode(conf);
        
        // Funcion del API de Clave Firma para cargar los datos a firmar
        FireLoadResult loadResult;
        try
        {
            //loadResult = new FireClient(appId, serviceConfig).sign( // Identificador de la aplicacion (dada de alta previamente en el sistema)
            loadResult = new FireClient(appId).sign( // Identificador de la aplicacion (dada de alta previamente en el sistema)
            //loadResult = FireApi.sign(appId,          // Identificador de la aplicacion (dada de alta previamente en el sistema)
                "00001",        // Identificador del usuario
                "sign",         // Operacion criptografica (sign, cosign o countersign)
                "CAdES",        // Formato de firma (CAdES, XAdES, PAdES...)
                "SHA512withRSA",  // Algoritmo de firma (Actualmente solo se permite SHA1withRSA)
                extraParamsB64, // Configuracion del formato de firma en base 64. El equivalente al extraParams del MiniApplet de @firma
                dataB64,        // Datos a firmar
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
        RedirectURL.Text = loadResult.getRedirectUrl();
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