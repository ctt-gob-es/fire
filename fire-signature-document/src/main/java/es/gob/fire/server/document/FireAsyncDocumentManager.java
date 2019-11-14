package es.gob.fire.server.document;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Properties;

/**
 * Interfaz que deben implementar los gestores de documentos de FIRe que deban ser compatibles
 * con la actualizaci&oacute;n as&iacute;ncrona de firmas. Esta clase dispone de los
 * m&eacute;todos para registrar que se ha solicitado la espera de un periodo de gracia antes
 * de la obtenci&oacute;n de la firma y para almacenar la firma actualizada. La l&oacute;gica
 * de reintento, as&iacute; como la l&oacute;gica para la asociar el documento que se envi&oacute;
 * a actualizar y la configuraci&oacute;n de firma con el c&oacute;digo de actualizaci&oacute;n
 * deber&aacute; realizarlo la clase que implementa esta interfaz. El reintento de
 * recuperaci&oacute;n de la firma actualizada podr&aacute; realizarlo la aplicaci&oacute;n que
 * la solicit&oacute; (en cuyo caso habr&aacute; que devolverle el identificador de la
 * operaci&oacute;n as&iacute;ncrona y la fecha estimada de disponibilidad) o el propio gestor
 * de documentos.
 */
public interface FireAsyncDocumentManager extends FIReDocumentManager {

	/**
	 * Registra que se ha solicitado la actualizaci&oacute;n de una firma y que la plataforma
	 * de actualizaci&oacute;n le ha solicitado la espera de un periodo de gracia. Este
	 * m&eacute;todo se llamar&aacute; tanto cuando se solicite la espera del periodo de gracia
	 * como cuando se actualice ese periodo despu&eacute;s de nuevos reintentos.
	 * @param asyncId Identificador para la solicitud de la firma as&iacute;ncrona.
	 * @param estimatedDate Fecha estimada en la que deber&acute;
	 * @param appId Identificador de la aplicaci&oacute;n que solicita la firma.
	 * @param docId Identificador del documento original no firmado. En reintentos posteriores
	 * se proporcionar&aacute; {@code null}.
	 * @param signature Firma generada todav&iacute;a sin actualizar. En reintentos posteriores
	 * se proporcionar&aacute; {@code null}.
	 * @param cert Certificado de firma. <b>IMPORTANTE:</b> El Cliente @firma 1.5 y anteriores no
	 * permiten obtener el certificado de firma en la firma de lotes, as&iacute; que en los casos
	 * de firma de lotes con certificado local este par&aacute;metro ser&aacute; nulo.
	 * @param format Formato de firma. En reintentos posteriores se proporcionar&aacute;
	 * {@code null}.
	 * @param upgradeFormat Formato al que se ha solicitado actualizar la firma. En reintentos
	 * posteriores de se proporcionar&aacute; {@code null}.
	 * @param extraParams Par&aacute;metros para la configuraci&oacute;n de la firma. En reintentos
	 * posteriores se proporcionar&aacute; {@code null}.
	 * @throws IOException Cuando falla el registro de la operaci&oacute;n.
	 */
	void registryAsyncOperation(String asyncId, Date estimatedDate, String appId, byte[] docId,
			byte[] signature, X509Certificate cert, String format, String upgradeFormat,
			Properties extraParams)
					throws IOException;

	/**
	 * Registra el documento recuperado as&iacute;ncronamente.
	 * @param asyncId Identificador de la firma as&iacute;ncrona recuperada.
	 * @param appId Identificador de la aplicaci&oacute;n que solicita la firma.
	 * @param signature Firma recuperada (ya actualizada).
	 * @param upgradeFormat Formato al que se ha actualizado la firma.
	 * @throws IOException Cuando falla el guardado de la firma.
	 */
	byte[] storeAsyncDocument(String asyncId, String appId, byte[] signature,
			String upgradeFormat) throws IOException;
}
