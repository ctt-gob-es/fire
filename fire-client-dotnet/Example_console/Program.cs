using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

using FIRe;

namespace Example_Console
{
    class Program
    {
        static void Main(string[] args)
        {
            if (args[0] == "sign")
            {

                string extraParams = "filters=keyusage.nonrepudiation:true\nformat = XAdES Enveloped\napplySystemDate = false";
                string extraParamsB64 = Base64Encode(extraParams);

                //string dataB64 = Base64Encode("Hola Mundo!!");
                string dataB64 = Base64Encode("<xml><hola>¡¡Hola Mundo con eñe!!</hola></xml>");

                string conf = "redirectOkUrl=http://www.google.es\n" +  // URL a la que llegara si el usuario se autentica correctamente
                              "redirectErrorUrl=http://www.ibm.com";        // URL a la que llegara si ocurre algun error o el usuario no se autentica correctamente
                string confB64 = Base64Encode(conf);

                // Funcion del API de Clave Firma para cargar los datos a firmar
                FireLoadResult loadResult;
                try
                {
                    //loadResult = new FireClient("A418C37E84BA", serviceConfig).sign( // Identificador de la aplicacion (dada de alta previamente en el sistema)
                    loadResult = new FireClient("A418C37E84BA").sign(
                        "00001",        // Identificador del usuario
                        "sign",         // Operacion criptografica (sign, cosign o countersign)
                        "XAdES",        // Formato de firma (CAdES, XAdES, PAdES...)
                        "SHA1withRSA",  // Algoritmo de firma (Actualmente solo se permite SHA1withRSA)
                        extraParamsB64, // Configuracion del formato de firma en base 64. El equivalente al extraParams del MiniApplet de @firma
                        dataB64,        // Datos a firmar
                        confB64         // Configuracion del servicio en base 64 (se incluyen las URL a las que redirigir en caso de exito y error)
                    );
                }
                catch (Exception ex)
                {
                    Console.WriteLine("Error: " + ex.Message);
                    return;
                }

                Console.WriteLine("TransactionId: " + loadResult.getTransactionId());
                Console.WriteLine("RedirectUrl: " + loadResult.getRedirectUrl());
            }
            else if (args[0] == "recover")
            {
                // Funcion del API de Clave Firma para cargar los datos a firmar
                FireTransactionResult signature;
                string transactionId = args[1];
                try
                {
                    signature = new FireClient("A418C37E84BA").recoverSign(
                        transactionId,
                        "00001",
                        null
                    );
                }
                catch (Exception ex)
                {
                    Console.WriteLine("Error: " + ex.Message);
                    return;
                }
                Console.WriteLine(signature.getResult());

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
}
