/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.upgrade.afirma.ws;

import java.security.cert.X509Certificate;

import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.message.WSSecSignature;
import org.w3c.dom.Document;

/**
 * WS-Security signature class for ECDSA keys.
 * Extends WSSecSignature and delegates BST/signature creation to the superclass.
 */
public class WSSecSignatureEC extends WSSecSignature {

	/**
	 * The user's X.509 certificate used for signing.
	 */
	private X509Certificate userCert;

	/**
	 * Default constructor for {@link WSSecSignatureEC}.
	 * Initializes a new instance of the class.
	 */
	public WSSecSignatureEC() {
	    super();
	}

	/**
	 * Configures this object to use an EC certificate for signing.
	 * Must be called before {@link #prepare(...)}.
	 *
	 * @param cert The X.509 certificate to use.
	 * @param certUser The username (optional).
	 * @param certPassword The password (optional).
	 */
    public void configureECforBinarySecurityToken(final X509Certificate cert, final String certUser, final String certPassword) {
        this.userCert = cert;
        // indicate the algorithm for certificates with EC
        super.setSignatureAlgorithm("http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha256"); //$NON-NLS-1$
        // store in parent so getSigningCerts() can return it
        super.setX509Certificate(cert);
        // ensure username/password cleared when using cert-based signing
        super.setUserInfo(certUser, certPassword);
    }

    /**
     * Override build<Document,Crypto,WSSecHeader> to ensure the cert is set
     * (if provided) then delegate to the parent implementation.
     */
    @Override
    public Document build(final Document doc, final Crypto cr, final WSSecHeader secHeader) throws WSSecurityException {
        // If userCert was set earlier via setUserCert, it's already in the parent (setX509Certificate).
        // Optionally: ensure it again (harmless).
        if (this.userCert != null) {
            super.setX509Certificate(this.userCert);
        }

        // Delegate to WSSecSignature implementation that handles references, BST, computeSignature, etc.
        return super.build(doc, cr, secHeader);
    }
}
