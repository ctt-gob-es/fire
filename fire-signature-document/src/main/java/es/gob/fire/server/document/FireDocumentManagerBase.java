package es.gob.fire.server.document;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Properties;

/**
 * Clase que los gestores de documentos deben extender para soportar todos los tipos de
 * operaciones soportadas por los gestores de documentos de FIRe.
 */
public abstract class FireDocumentManagerBase implements FireAsyncDocumentManager {

	@Override
	public boolean needConfiguration() {
		// Por defecto, los gestores necesitaran configuracion externa
		return true;
	}

	@Override
	public void init(final Properties config) throws IOException {
		// No hace nada
	}

	@Override
	public byte[] getDocument(final byte[] docId, final String appId, final String format,
			final Properties extraParams) throws IOException {
		return getDocument(docId, null, appId, format, extraParams);
	}

	/** Obtiene un documento para firmarlo.
	 * Si no es posible recuperar el fichero se debe lanzar una excepci&oacute;n. El mensaje se
	 * recibir&aacute; como parte del mensaje de error en el cliente de firma.
	 * @param docId Identificador del documento original no firmado.
	 * @param trId Identificador de transacci&ooacute;n.
	 * @param appId Identificador de la aplicaci&oacute;n que solicita la firma.
	 * @param format Formato de firma.
	 * @param extraParams Par&aacute;metros para la configuraci&oacute;n de la firma. Podr&iacute;a
	 * ser {@code null}.
	 * @return Documento (en binario)
	 * @throws IOException Cuando ocurre alg&uacute;n problema con la recuperaci&oacute;n. */
	public abstract byte[] getDocument(byte[] docId, String trId, String appId, String format,
			Properties extraParams) throws IOException;


	@Override
	public byte[] storeDocument(final byte[] docId, final String appId, final byte[] data,
			final X509Certificate cert, final String format, final Properties extraParams)
					throws IOException {
		return storeDocument(docId, null, appId, data, cert, format, null, extraParams);
	}

	/** Almacena un documento firmado.
	 * Si no es posible almacenar el fichero se lanza una excepci&oacute;n. El valor devuelto se
	 * recibir&aacute; como resultado de la operaci&oacute; de firma.
	 * @param docId Identificador del documento original no firmado.
	 * @param trId Identificador de transacci&oacute;n.
	 * @param appId Identificador de la aplicaci&oacute;n que solicita la firma.
	 * @param data Datos firmados.
	 * @param cert Certificado de firma. <b>IMPORTANTE:</b> El Cliente @firma 1.5 y anteriores no
	 * permiten obtener el certificado de firma en la firma de lotes, as&iacute; que en los casos
	 * de firma de lotes con certificado local este par&aacute;metro ser&aacute; nulo.
	 * @param format Formato de firma.
	 * @param upgradeFormat Formato longevo de la firma proporcionada.
	 * @param extraParams Par&aacute;metros para la configuraci&oacute;n de la firma.
	 * @return Resultado que obtendr&aacute; la aplicaci&oacute;n cliente. En caso de usarse
	 * cadenas de texto, se deben codificar en UTF-8.
	 * @throws IOException Cuando ocurre alg&uacute;n problema con el guardado. */
	public abstract byte[] storeDocument(byte[] docId, String trId, String appId, byte[] data,
			X509Certificate cert, String format, String upgradeFormat, Properties extraParams)
					throws IOException;

	@Override
	public void registryAsyncOperation(final String asyncId, final Date extimatedDate,
			final String appId, final byte[] docId, final byte[] signature,
			final X509Certificate cert, final String format, final String upgradeFormat,
			final Properties extraParams) throws IOException {
		// No hacemos nada
	}

	@Override
	public byte[] storeAsyncDocument(final String asyncId, final String appId,
			final byte[] signature, final String upgradeFormat) throws IOException {
		return signature;
	}
}
