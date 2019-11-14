
using System.Security.Cryptography.X509Certificates;

namespace FIRe
{
    /// <summary>Clase que contiene la información recabada de la firma de un documento.</summary>
    internal class TransactionResultJson
    {
        /// <summary>
        /// Resultado de la operacion de firma.
        /// </summary>
        public ResultJson Result { get; set; }
    }

    /// <summary>Clase que contiene la información recabada de la firma de un documento.</summary>
    internal class ResultJson
    {
        /// <summary>
        /// Estado del resultado.
        /// </summary>
        public int State { get; set; }
        /// <summary>
        /// Nombre del proveedor.
        /// </summary>
        public string Prov { get; set; }
        /// <summary>
        /// Certificado de firma.
        /// </summary>
        public string Cert { get; set; }
        /// <summary>
        /// Periodo de gracia.
        /// </summary>
        public GracePeriodJson Grace { get; set; }
        /// <summary>
        /// Formato al que se ha actualizado la firma.
        /// </summary>
        public string Upgrade { get; set; }
        /// <summary>
        /// Código de error.
        /// </summary>
        public int Ercod { get; set; }
        /// <summary>
        /// Mensaje de error.
        /// </summary>
        public string Ermsg { get; set; }
    }

    /// <summary>Clase con el periodo de gracia recogido del JSON.</summary>
    internal class GracePeriodJson
    {
        /// <summary>
        /// Identificador con el que recuperar la firma actualizada.
        /// </summary>
        public string Id { get; set; }
        /// <summary>
        /// Fecha en la que deberia estar disponible la firma actualizada, expresada en forma del
        /// número de milisegundos transcurridos desde el 1 de Enero de 1970.
        /// </summary>
        public long Date { get; set; }
    }
}
