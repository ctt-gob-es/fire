/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.upgrade;

import org.apache.ws.security.util.Base64;

/**
 * Utilidades para verificaci&oacute;n de certificados.
 *
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s.
 */
final class VerifyUtils {

    private static final String TAG_CERT = "$$CERT$$"; //$NON-NLS-1$
    private static final String TAG_APPNAME = "$$APPNAME$$"; //$NON-NLS-1$

    private static final String XML_TEMPLATE =
    		"<dss:VerifyRequest Profile='urn:afirma:dss:1.0:profile:XSS' xmlns:ds='http://www.w3.org/2000/09/xmldsig#' xmlns:dss='urn:oasis:names:tc:dss:1.0:core:schema' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='urn:oasis:names:tc:dss:1.0:core:schema http://docs.oasis-open.org/dss/v1.0/oasis-dss-core-schema-v1.0-os.xsd'>\n" + //$NON-NLS-1$
            "  <dss:SignatureObject>\n" + //$NON-NLS-1$
            "    <dss:Other>\n" + //$NON-NLS-1$
            "      <ds:X509Data>\n" + //$NON-NLS-1$
            "        <ds:X509Certificate>\n" + //$NON-NLS-1$
            "          " //$NON-NLS-1$
            + TAG_CERT
            + "\n" + //$NON-NLS-1$
            "        </ds:X509Certificate>\n" //$NON-NLS-1$
            +
            "      </ds:X509Data>\n" //$NON-NLS-1$
            +
            "    </dss:Other>\n" //$NON-NLS-1$
            +
            "  </dss:SignatureObject>\n" //$NON-NLS-1$
            +
            "  <dss:OptionalInputs>\n" //$NON-NLS-1$
            +
            "    <dss:ClaimedIdentity>\n" //$NON-NLS-1$
            +
            "      <dss:Name>\n" //$NON-NLS-1$
            +
            "        " //$NON-NLS-1$
            + TAG_APPNAME
            + "\n" + //$NON-NLS-1$
            "      </dss:Name>\n" //$NON-NLS-1$
            +
            "    </dss:ClaimedIdentity>\n" //$NON-NLS-1$
            +
            "    <vr:ReturnVerificationReport xmlns:vr=\"urn:oasis:names:tc:dss:1.0:profiles:verificationreport:schema#\">\n" //$NON-NLS-1$
            +
            "      <vr:CheckOptions>\n" //$NON-NLS-1$
            +
            "        <vr:CheckCertificateStatus>true</vr:CheckCertificateStatus>\n" //$NON-NLS-1$
            +
            "      </vr:CheckOptions>\n" + //$NON-NLS-1$
            "    </vr:ReturnVerificationReport>\n" + //$NON-NLS-1$
            "    <afxp:IgnoreGracePeriod xmlns:afxp=\"urn:afirma:dss:1.0:profile:XSS:schema\"/>\n" + //$NON-NLS-1$
            "  </dss:OptionalInputs>\n" + //$NON-NLS-1$
            "  <dss:SignatureObject></dss:SignatureObject>\n" + //$NON-NLS-1$
            "</dss:VerifyRequest>"; //$NON-NLS-1$

    private VerifyUtils() {
        // No instanciable
    }

    static String createCertVerifyDss(final byte[] cert, final String appName) {
        if (cert == null) {
            throw new IllegalArgumentException(
                    "El certificado no puede ser nulo"); //$NON-NLS-1$
        }
        if (appName == null || "".equals(appName)) { //$NON-NLS-1$
            throw new IllegalArgumentException(
                    "El nombre de aplicacion de la plataforma Afirma no puede ser nulo ni vacio" //$NON-NLS-1$
            );
        }
        return XML_TEMPLATE.replace(TAG_CERT, Base64.encode(cert)).replace(
                TAG_APPNAME, appName);
    }

}
