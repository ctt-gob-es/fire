/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.connector;

import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Properties;

import es.gob.afirma.core.signers.TriphaseData;

/** Interfaz contra un proveedor de custodia de certificados y operaciones de firma PKCS#1. */
public interface FIReConnector {


	/** Inicializa el conector.
     * @param config Propiedades obtenidas del fichero de configuraci&oacute;n del conector.
     * En caso de que el fichero no exista o no se pueda cargar, no habr&aacute; propiedades
     * definidas. */
	void init(Properties config);

    /** Inicializa una transacci&oacute;n.
     * @param config Par&aacute;metros de configuraci&oacute;n. */
    void initOperation(Properties config);


    /** Obtiene los certificados de firma del proveedor que el usuario tiene emitidos.
     * @param subjectId Identificador del titular de los certificados.
     * @return Certificados de firma del usuario. Si el usuario no tiene certificados
     * se devolver&aacute; un listado vac&iacute;o.
     * @throws FIReCertificateException Si hay errores durante la obtenci&oacute;n.
     * @throws FIReConnectorUnknownUserException Si el usuario no est&aacute; dado de alta en el servicio.
     * @throws FIReConnectorNetworkException Si hay problemas de conectividad de red.
     * @throws CertificateBlockedException Si se detecta que los certificados de firma est&aacute;n bloqueados.
     * @throws WeakRegistryException Si el usuario realiz&oacute; un registro d&eacute;bil y no puede tener certificados de firma. */
    X509Certificate[] getCertificates(String subjectId) throws FIReCertificateException,
                                                                     FIReConnectorUnknownUserException,
                                                                     FIReConnectorNetworkException,
                                                                     CertificateBlockedException,
                                                                     WeakRegistryException;


    /** Carga los datos a firmar.
     * @param subjectId Identificador del titular de la clave de firma.
     * @param algorithm Algoritmo de firma.
     * @param td Datos a firmar en forma de sesi&oacute;n trif&aacute;sica.
     * @param signCert Certificado con el que se desean firmar los datos.
     * @return Resultado de la carga.
     * @throws FIReCertificateException Si hay problemas con el tratamiento de los certificados del
     *                                   firmante.
     * @throws FIReSignatureException Si hay problemas durante la carga.
     * @throws IOException Si hay errores en el tratamiento de datos.
     * @throws FIReConnectorUnknownUserException Si el usuario no est&aacute; dado de alta en el sistema.
     * @throws FIReConnectorNetworkException Si hay problemas de conectividad de red. */
    LoadResult loadDataToSign(final String subjectId,
    		                       final String algorithm,
                                   final TriphaseData td,
                                   final Certificate signCert) throws FIReCertificateException,
                                   									  FIReSignatureException,
                                                                      IOException,
                                                                      FIReConnectorUnknownUserException,
                                                                      FIReConnectorNetworkException;


    /** Completa una transacci&oacute;n de firma obteniendo los resultados.
     * @param transactionId Identificador de la transacci&oacute;n.
     * @return Resultados de las firmas, indexados por su identificador.
     * @throws FIReSignatureException Si hay problemas en el proceso.
     * @throws FIReConnectorUnknownUserException Si el usuario no est&aacute; dado de alta en el sistema. */
    Map<String, byte[]> sign(String transactionId) throws FIReSignatureException, FIReConnectorUnknownUserException;

    /**
     * Informa del fin de la operaci&oacute;n de firma.
     * @param transactionId Identificador de la transacci&oacute;n a la que corresponde la firma. */
    void endSign(final String transactionId);

    /** Genera un nuevo certificado de firma para el usuario.
     * @param subjectId Identificador del usuario.
     * @return Resultado de la generacion con el ID de transacci&oacute;n y la URL para la redirecci&oacute;n en caso de &eacute;xito.
     * @throws FIReCertificateAvailableException Si no se puede generar un certificado para ese usuario porque ha alcanzado el l&iacute;mite.
     * @throws FIReCertificateException Si ocurre un error durante la generaci&oacute;n del certificado.
     * @throws FIReConnectorNetworkException Si ocurre un error en la comunicaci&oacute;n con el backend.
     * @throws FIReConnectorUnknownUserException Si el usuario no est&aacute; dado de alta en el sistema.
     * @throws WeakRegistryException Si el usuario realiz&oacute; un registro d&eacute;bil y no puede tener certificados de firma. */
    GenerateCertificateResult generateCertificate(String subjectId) throws FIReCertificateAvailableException, FIReCertificateException, FIReConnectorNetworkException, FIReConnectorUnknownUserException, WeakRegistryException;


    /** Recupera un certificado de firma reci&eacute;n generado.
     * @param transactionId Identificador de la transacci&oacute;n.
     * @return Certificado reci&eacute;n generado.
     * @throws FIReCertificateException Si ocurre un error durante la generaci&oacute;n del certificado.
     * @throws FIReConnectorNetworkException Si ocurre un error en la comunicaci&oacute;n con el backend. */
    byte[] recoverCertificate(final String transactionId) throws FIReCertificateException, FIReConnectorNetworkException;
}
