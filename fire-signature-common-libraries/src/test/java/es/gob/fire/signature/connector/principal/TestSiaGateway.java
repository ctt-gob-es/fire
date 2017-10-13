/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.signature.connector.principal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Properties;

import org.junit.Test;

import com.openlandsw.rss.gateway.CertificateInfo;
import com.openlandsw.rss.gateway.GateWayAPI;
import com.openlandsw.rss.gateway.ListOwnerCertificateInfo;
import com.openlandsw.rss.gateway.QueryCertificatesResult;
import com.openlandsw.rss.gateway.RSSListOwnerCertificatesResult;
import com.openlandsw.rss.gateway.constants.ConstantsGateWay;
import com.openlandsw.rss.gateway.exception.SafeCertGateWayException;

import es.gob.afirma.core.misc.AOUtil;
import es.gob.afirma.core.misc.Base64;
import es.gob.fire.signature.connector.FIReCertificateException;
import es.gob.fire.signature.connector.FIReConnectorNetworkException;
import es.gob.fire.signature.connector.FIReConnectorUnknownUserException;

/**
 *  Clase para las pruebas del componente de firma remota de la GISS.
 */
public class TestSiaGateway {

	private static final String UNKOWN_USER_EX_MSG = "El identificador del titular no existe en SafeCert."; //$NON-NLS-1$

	private static final int ACTIVES = 1;
	private static final int SIGNATURE = 2;
	private static final int ALL = 3;

	/**
	 * Comprueba la operaci&oacute;n de solicitud de certificados.
	 * @throws Exception Cuando ocurre cualquier error.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testQueryCertificates() throws Exception {

		final Properties gatewayConfig = new Properties();
		gatewayConfig.setProperty("URL_GATEWAY", "https://clave-dninbrtws.dev.seg-social.gob.es:452/rss-gateway/HESS/OperationGateWayRSS"); //$NON-NLS-1$ //$NON-NLS-2$
		gatewayConfig.setProperty("AUTH_STORE", "C:/Users/carlos/Documents/ClaveFirma/Pruebas/SafeCert_GateWay_@Firma_Desarrollo.p12"); //$NON-NLS-1$ //$NON-NLS-2$
		gatewayConfig.setProperty("AUTH_STORE_PASS", "GISSusr010"); //$NON-NLS-1$ //$NON-NLS-2$

		final GateWayAPI api = new GateWayAPI();
		api.setConfig(gatewayConfig);

		final String subjectId = "52020202K"; //$NON-NLS-1$

//		System.out.println("Certificados activos de " + subjectId + ":"); //$NON-NLS-1$ //$NON-NLS-2$
//		listCertificates(api, subjectId, ACTIVES);
//		System.out.println("------------------------------------");

		System.out.println("Todos los certificados de " + subjectId + ":"); //$NON-NLS-1$ //$NON-NLS-2$
		listCertificates(api, subjectId, ALL);
		System.out.println("------------------------------------"); //$NON-NLS-1$
	}

	private static void listCertificates(final GateWayAPI api, final String subjectId, final int type) throws Exception {

		byte[][] certContents = null;
        try {
        	if (type == ACTIVES) {
        		certContents = getCertificates(api.queryCertificates(subjectId));
        	}
        	else if (type == SIGNATURE) {
        		certContents = getCertificates(api.queryCertificatesFiltered(subjectId, ConstantsGateWay.OPERATION_SIGN));
        	}
        	else if (type == ALL) {
        		certContents = getCertificates(api.listOwnerCertificates(subjectId));
        	}
        	else {
        		throw new Exception("Tipo de certificados incorrecto"); //$NON-NLS-1$
        	}

        }
        catch (final SafeCertGateWayException e) {
            if (UNKOWN_USER_EX_MSG.equals(e.getMessage().trim())) {
                throw new FIReConnectorUnknownUserException(e);
            }
            if (e.getCause() != null
                    && e.getCause().getCause() instanceof java.net.ConnectException) {
                throw new FIReConnectorNetworkException(e);
            }
            throw new FIReCertificateException(
                    "Error en la llamada al servicio de custodia: " + e, e //$NON-NLS-1$
            );
        }

        final CertificateFactory cf;
        try {
            cf = CertificateFactory.getInstance("X.509"); //$NON-NLS-1$
        }
        catch (final Exception e) {
            throw new FIReCertificateException(
                "Error obteniendo la factoria de certificados: " + e, e //$NON-NLS-1$
            );
        }
        for (int i = 0; i < certContents.length; i++) {
            try {
                System.out.println(AOUtil.getCN((X509Certificate) cf.generateCertificate(
            		new ByteArrayInputStream(certContents[i])
        		)));
            }
            catch (final CertificateException e) {
                throw new FIReCertificateException(
                    "Error extrayendo un certificado de usuario: " + e, e //$NON-NLS-1$
                );
            }
        }
	}

	private static byte[][] getCertificates(final QueryCertificatesResult result) throws NoCertificatesException {

		if (result == null) {
            throw new NoCertificatesException("No se han obtenido certificados"); //$NON-NLS-1$
        }
        final List<CertificateInfo> certsInfo = result.getCertificates();
        final byte[][] certs = new byte[certsInfo.size()][];
        for (int i = 0; i < certsInfo.size(); i++) {
        	certs[i] = certsInfo.get(i).getCertificate();
        }
        return certs;
	}

	private static byte[][] getCertificates(final RSSListOwnerCertificatesResult result) throws NoCertificatesException, IOException {

		if (result == null) {
            throw new NoCertificatesException("No se han obtenido certificados"); //$NON-NLS-1$
        }
        final ListOwnerCertificateInfo[] certsInfo = result.getCertificates();
        final byte[][] certs = new byte[certsInfo.length][];
        for (int i = 0; i < certsInfo.length; i++) {
        	System.out.println("-----------------------------------------------"); //$NON-NLS-1$
        	System.out.println("Identificador: " + certsInfo[i].getIdentifier()); //$NON-NLS-1$
        	System.out.println("Estado stateCode: " + certsInfo[i].getState().stateCode); //$NON-NLS-1$
        	System.out.println("Estado useLocked: " + certsInfo[i].getState().useLocked); //$NON-NLS-1$
        	System.out.println("Estado description: " + certsInfo[i].getState().stateDescription); //$NON-NLS-1$
        	System.out.println("PKCS11Indo LabelKey: " + certsInfo[i].getpKCS11Info()); //$NON-NLS-1$
        	System.out.println("GenerationDescription: " + certsInfo[i].getGenerationDescription()); //$NON-NLS-1$
        	System.out.println("Repository: " + certsInfo[i].getRepository()); //$NON-NLS-1$

        	final byte[] certEncodedB64 = certsInfo[i].getContent();
        	certs[i] = Base64.decode(certEncodedB64, 0, certEncodedB64.length, false);
        	System.out.println("-----------------------------------------------"); //$NON-NLS-1$
        }
        return certs;
	}
}
