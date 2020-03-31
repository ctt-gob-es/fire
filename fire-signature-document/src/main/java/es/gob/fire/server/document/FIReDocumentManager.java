/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.document;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.Properties;

/** Interfaz para la recuperaci&oacute;n de documentos desde un servidor o repositorio documental.
 * <u>ES OBLIGATORIO</u>, que las clases que implementen esta interfaz no definan un constructor
 * o que este sea el contructor p&uacute;blico por defecto.<br>
 * Si se encuentra en el directorio de configuraci&oacute;n o en el <i>classpath</i> un fichero
 * con el nombre "docmanager.NOMBRE_DOCUMENT_MANAGER.properties", se cargar&aacute; como fichero
 * de propiedades y se ejecutar&aacute; el m&eacute;rodo init() de la case antes de realizar cualquier
 * llamada a los m&eacute;todos getDocument y storeDocument. Si no se encuentra este fichero, no se
 * realizara ninguna llamada al metodo init. */
public interface FIReDocumentManager {

	/**
	 * Inicializa el objeto con las propiedades de un fichero de configuraci&oacute;n.
	 * @param config Configuraci&oacute;n que aplicar a todas las operaciones del
	 * DocumentManager o {@code null} si no se pudo cargar la configuraci&oacute;n.
	 * @throws IOException Si ocurre un error durante la inicializaci&oacute;n.
	 */
	void init(Properties config) throws IOException;

	/** Obtiene un documento para firmarlo.
	 * Si no es posible recuperar el fichero se debe lanzar una excepci&oacute;n. El mensaje se
	 * recibir&aacute; como parte del mensaje de error en el cliente de firma.
	 * @param docId Identificador del documento original no firmado.
	 * @param appId Identificador de la aplicaci&oacute;n que solicita la firma.
	 * @param format Formato de firma.
	 * @param extraParams Par&aacute;metros para la configuraci&oacute;n de la firma. Podr&iacute;a
	 * ser {@code null}.
	 * @return Documento (en binario)
	 * @throws IOException Cuando ocurre alg&uacute;n problema con la recuperaci&oacute;n. */
	byte[] getDocument(byte[] docId, String appId, String format, Properties extraParams)
			throws IOException;

	/** Almacena un documento firmado.
	 * Si no es posible almacenar el fichero se lanza una excepci&oacute;n. El valor devuelto se
	 * recibir&aacute; como resultado de la operaci&oacute; de firma.
	 * @param docId Identificador del documento original no firmado.
	 * @param appId Identificador de la aplicaci&oacute;n que solicita la firma.
	 * @param data Datos firmados.
	 * @param cert Certificado de firma. <b>IMPORTANTE:</b> El Cliente @firma 1.5 y anteriores no
	 * permiten obtener el certificado de firma en la firma de lotes, as&iacute; que en los casos
	 * de firma de lotes con certificado local este par&aacute;metro ser&aacute; nulo.
	 * @param format Formato de firma.
	 * @param extraParams Par&aacute;metros para la configuraci&oacute;n de la firma.
	 * @return Resultado que obtendr&aacute; la aplicaci&oacute;n cliente. En caso de usarse
	 * cadenas de texto, se deben codificar en UTF-8.
	 * @throws IOException Cuando ocurre alg&uacute;n problema con el guardado. */
	byte[] storeDocument(byte[] docId, String appId, byte[] data, X509Certificate cert,
			String format, Properties extraParams) throws IOException;
}
