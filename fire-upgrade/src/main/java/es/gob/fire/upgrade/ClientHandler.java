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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.SOAPPart;
import org.apache.axis.handlers.BasicHandler;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.components.crypto.CryptoFactory;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.message.WSSecSignature;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** Gestor del Cliente AXIS para la Plataforma @firma con seguridad basada en
 * certificados. */
public final class ClientHandler extends BasicHandler {

    private static final Logger LOGGER = Logger.getLogger(ClientHandler.class.getName());

    private static final long serialVersionUID = 1L;

    private final Properties configuration;

    /**
     * Crea un gestor del Cliente AXIS para la Plataforma @firma con seguridad
     * basada en certificados.
     *
     * @param config
     *            Configuraci&oacute;n a aplicar.
     * @throws AxisFault
     *             Si la configuraci&oacute;n no es v&aacute;lida.
     */
    ClientHandler(final Properties config) throws AxisFault {
        if (config == null) {
            this.configuration = null;
            AxisFault.makeFault(new InvalidParameterException(
                    "Fichero de configuracion de propiedades nulo")); //$NON-NLS-1$
            return;
        }
        this.configuration = config;
    }

    @Override
    public void invoke(final MessageContext msgContext) throws AxisFault {

        try {

            // Obtencion del documento XML que representa la peticion SOAP
            final SOAPMessage msg = msgContext.getCurrentMessage();

            final Document doc = ((org.apache.axis.message.SOAPEnvelope) msg
                    .getSOAPPart().getEnvelope()).getAsDocument();

            // Securizacion de la peticion SOAP segun la opcion de seguridad
            // configurada
            final SOAPMessage secMsg = createBinarySecurityToken(doc);

            // Establecemos el mensaje asegurado
            ((SOAPPart) msgContext.getRequestMessage().getSOAPPart())
                    .setCurrentMessage(secMsg.getSOAPPart().getEnvelope(),
                            SOAPPart.FORM_SOAPENVELOPE);

        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "Error en la preparacion del mensaje para la invocacion SOAP: " + e, e); //$NON-NLS-1$
            AxisFault.makeFault(e);
        }
    }

    /**
     * Asegura, mediante la etiqueta <code>BinarySecurityToken</code> y firma,
     * una petici&oacute;n SOAP no asegurada.
     *
     * @param soapEnvelopeRequest
     *            Documento xml que representa la petici&oacute;n SOAP sin
     *            securizar.
     * @return Mensaje SOAP que contiene la petici&oacute;n SOAP de entrada
     *         asegurada mediante la etiqueta <code>BinarySecurityToken</code>.
     * @throws TransformerConfigurationException
	 * @throws TransformerException
	 * @throws TransformerFactoryConfigurationError
	 * @throws IOException
	 * @throws SOAPException
     *         */
    private SOAPMessage createBinarySecurityToken(
            final Document soapEnvelopeRequest)
            throws TransformerConfigurationException, TransformerException,
            TransformerFactoryConfigurationError, IOException, SOAPException {
        // Insercion del tag wsse:Security y BinarySecurityToken
        final WSSecHeader wsSecHeader = new WSSecHeader(null, false);

        final String keystoreCertAlias = this.configuration
                .getProperty("security.keystore.cert.alias"); //$NON-NLS-1$
        final String keystoreCertPassword = this.configuration
                .getProperty("security.keystore.cert.password"); //$NON-NLS-1$

        final Crypto crypto = CryptoFactory
                .getInstance(
                        "org.apache.ws.security.components.crypto.Merlin", //$NON-NLS-1$
                        ClientHandler.initializateCryptoProperties(
                                this.configuration
                                        .getProperty("security.keystore.type"), //$NON-NLS-1$
                                this.configuration
                                        .getProperty("security.keystore.password"), //$NON-NLS-1$
                                keystoreCertAlias,
                                keystoreCertPassword,
                                this.configuration
                                        .getProperty("security.keystore.location") //$NON-NLS-1$
                        ));

        // Indicacion para que inserte el tag BinarySecurityToken
        final WSSecSignature wsSecSignature = new WSSecSignature();
        wsSecSignature.setKeyIdentifierType(WSConstants.BST_DIRECT_REFERENCE);
        wsSecSignature.setUserInfo(keystoreCertAlias, keystoreCertPassword);
        wsSecHeader.insertSecurityHeader(soapEnvelopeRequest);
        wsSecSignature.prepare(soapEnvelopeRequest, crypto, wsSecHeader);

        // Modificacion y firma de la peticion
        final Document secSOAPReqDoc = wsSecSignature.build(
                soapEnvelopeRequest, crypto, wsSecHeader);
        final Element element = secSOAPReqDoc.getDocumentElement();

        // Transformacion del elemento DOM a String
        final DOMSource source = new DOMSource(element);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final StreamResult streamResult = new StreamResult(baos);
        TransformerFactory.newInstance().newTransformer()
                .transform(source, streamResult);
        final byte[] secSOAPReq = baos.toByteArray();

        // Creacion de un nuevo mensaje SOAP a partir del mensaje SOAP
        // securizado formado
        final MessageFactory mf = new org.apache.axis.soap.MessageFactoryImpl();
        return mf.createMessage(null,
                new ByteArrayInputStream(secSOAPReq));
    }

    /**
     * Establece el conjunto de propiedades con el que ser&aacute; inicializado
     * el gestor criptogr&aacute;fico de WSS4J.
     *
     * @return Conjunto de propiedades con el que ser&aacute; inicializado el
     *         gestor criptogr&aacute;fico de WSS4J.
     */
    private static Properties initializateCryptoProperties(
            final String keystoreType, final String keystorePassword,
            final String keystoreCertAlias, final String keystoreCertPassword,
            final String keystoreLocation) {
        final Properties res = new Properties();
        res.setProperty(
                "org.apache.ws.security.crypto.provider", "org.apache.ws.security.components.crypto.Merlin"); //$NON-NLS-1$ //$NON-NLS-2$
        res.setProperty(
                "org.apache.ws.security.crypto.merlin.keystore.type", keystoreType); //$NON-NLS-1$
        res.setProperty(
                "org.apache.ws.security.crypto.merlin.keystore.password", keystorePassword); //$NON-NLS-1$
        res.setProperty(
                "org.apache.ws.security.crypto.merlin.keystore.alias", keystoreCertAlias); //$NON-NLS-1$
        res.setProperty(
                "org.apache.ws.security.crypto.merlin.alias.password", keystoreCertPassword); //$NON-NLS-1$
        res.setProperty(
                "org.apache.ws.security.crypto.merlin.file", keystoreLocation); //$NON-NLS-1$
        return res;
    }
}