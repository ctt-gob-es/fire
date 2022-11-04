using FIRe;
using System;

public partial class example_sign : System.Web.UI.Page
{
    protected void Page_Load(object sender, EventArgs e)
    {
        // Identificador de la aplicacion (dada de alta previamente en el sistema)
        //string appId = "0DC38D5E5D68"; // Preproduccion
        string appId = "B244E473466F";   // Local

        string transactionId = "ef35b1ab-1aa4-438b-899a-4c619d3353f9";   // Identificador de la transaccion

        string dataB64 = Base64Encode("Hola Mundo!!");

        string provider = "clavefirmatest";     // Proveedor de firma en la nube

        // Funcion del API de Clave Firma para cargar los datos a firmar
        byte[] signature;
        try
        {
            signature = HttpSignProcess.sign(
                appId,          // Identificador de la aplicacion (dada de alta previamente en el sistema)
                transactionId,  // Identificador de transaccion recuperado en la operacion loadData()
                "sign",         // Operacion criptografica (sign, cosign o countersign)
                "CAdES",        // Formato de firma (CAdES, XAdES, PAdES...)
                "SHA256withRSA",  // Algoritmo de firma (Actualmente solo se permite SHA1withRSA)
                null,           // Configuracion del formato de firma en base 64. El equivalente al extraParams del MiniApplet de @firma
                                // Certificado de firma
                "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",
                dataB64,       // Datos a firmar
                               // Resultado parcial de firma trifasica obtenido en la operacion loadData()
                "PHhtbD4KIDxmaXJtYXM+CiAgPGZpcm1hIElkPSIzNThjYTI4Ny1hOTljLTRjMjMtOWU2NC0wNmUzNDllNDFmMzYiPgogICA8cGFyYW0gbj0iUFJFIj5NWUlDUmpBWUJna3Foa2lHOXcwQkNRTXhDd1lKS29aSWh2Y05BUWNCTUJ3R0NTcUdTSWIzRFFFSkJURVBGdzB5TWpFeE1EUXhNekl3TlRSYU1Da0dDeXFHU0liM0RRRUpFQUlFTVJvd0dBd0xWR1Y0ZEc4Z2NHeGhibThHQ1NxR1NJYjNEUUVIQVRBdkJna3Foa2lHOXcwQkNRUXhJZ1FnOHc2VlRFVFdycmg3SDU0Y3crL3YxTnkvSkpEL1ZNcFFPdVhZT3JrZnVHd3dnZ0d1QmdzcWhraUc5dzBCQ1JBQ0x6R0NBWjB3Z2dHWk1JR0tNSUdIQkNDcDJhWitrc1pzVFE3aVc4S09Gd3B1emVFUnJlempERTZlZzFBMUFMR0NwakJqTUUra1RUQkxNUXN3Q1FZRFZRUUdFd0pGVXpFUk1BOEdBMVVFQ2d3SVJrNU5WQzFTUTAweERqQU1CZ05WQkFzTUJVTmxjbVZ6TVJrd0Z3WURWUVFEREJCQlF5QkdUazFVSUZWemRXRnlhVzl6QWhCSTVLWEtPOUVWU1YraitGUVUwQytvTUlJQkNEQ0IrZ1lLS3dZQkJBR3NaZ01LQVRDQjZ6QXBCZ2dyQmdFRkJRY0NBUllkYUhSMGNEb3ZMM2QzZHk1alpYSjBMbVp1YlhRdVpYTXZaSEJqY3k4d2diMEdDQ3NHQVFVRkJ3SUNNSUd3RElHdFEyVnlkR2xtYVdOaFpHOGdZM1ZoYkdsbWFXTmhaRzhnWkdVZ1ptbHliV0VnWld4bFkzUnl3N051YVdOaExpQlRkV3BsZEc4Z1lTQnNZWE1nWTI5dVpHbGphVzl1WlhNZ1pHVWdkWE52SUdWNGNIVmxjM1JoY3lCbGJpQnNZU0JFVUVNZ1pHVWdiR0VnUms1TlZDMVNRMDBnWTI5dUlFNUpSam9nVVRJNE1qWXdNRFF0U2lBb1F5OUtiM0puWlNCS2RXRnVJREV3TmkweU9EQXdPUzFOWVdSeWFXUXRSWE53WWNPeFlTa3dDUVlIQkFDTDdFQUJBQT09PC9wYXJhbT4KICAgPHBhcmFtIG49Ik5FRURfUFJFIj50cnVlPC9wYXJhbT4KICA8L2Zpcm1hPgogPC9maXJtYXM+CjwveG1sPg==",
                null,            // Formato para el upgrade de firma (ES-T, ES-C...)
                provider
            );
        }
        catch (Exception ex)
        {
            SignatureB64.Text = ex.Message;
            return;
        }

        // Mostramos los datos obtenidos
        SignatureB64.Text = System.Convert.ToBase64String(signature);
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