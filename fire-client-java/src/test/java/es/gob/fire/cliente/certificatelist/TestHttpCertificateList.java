/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.cliente.certificatelist;

import java.security.cert.X509Certificate;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import es.gob.clavefirma.client.certificatelist.HttpCertificateList;

/** Pruebas de listado de certificados. */
public class TestHttpCertificateList {

    /** Prueba de listado de certificados.
     * @throws Exception En cualquier error. */
    @SuppressWarnings("static-method")
    @Test
    //@Ignore
    public void testCertificateRetrieval() throws Exception {
        final List<X509Certificate> list = HttpCertificateList.getList("spt", "00001"); //$NON-NLS-1$ //$NON-NLS-2$
        System.out.println("Numero de certificados obtenidos: " + list.size()); //$NON-NLS-1$
        for (final X509Certificate cert : list) {
            System.out.println(" -- Certificado : " + cert.getSubjectDN()); //$NON-NLS-1$
        }
    }

    /** Prueba de listado de certificados de prueba.
     * @throws Exception En cualquier error. */
    @SuppressWarnings("static-method")
    @Test
    @Ignore
    public void testCertificateRetrievalTest() throws Exception {
        final List<X509Certificate> list = HttpCertificateList.getList("1", "00001"); //$NON-NLS-1$ //$NON-NLS-2$
        System.out.println("Numero de certificados obtenidos: " + list.size()); //$NON-NLS-1$
        for (final X509Certificate cert : list) {
            System.out.println(" -- Certificado : " + cert.getSubjectDN()); //$NON-NLS-1$
        }
    }

}
