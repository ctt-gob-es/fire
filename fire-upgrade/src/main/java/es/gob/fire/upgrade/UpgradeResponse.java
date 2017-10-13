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
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.ws.security.util.Base64;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/** Respuesta de una consulta de mejora contra la plataforma Afirma. */
class UpgradeResponse {

    private static final String SUCCESS = "Success"; //$NON-NLS-1$

    private String major = null;
    private String minor = null;
    private String desc = null;

    private String signatureForm = null;

    private String signature = null;

    private static final String SIGNATURE_OBJECT_START = "<dss:SignatureObject>"; //$NON-NLS-1$
    private static final String SIGNATURE_OBJECT_END = "</dss:SignatureObject>"; //$NON-NLS-1$

    UpgradeResponse(final byte[] xml) throws ParserConfigurationException,
            SAXException, IOException {

        if (xml == null) {
            throw new IllegalArgumentException(
                    "El XML de entrada no puede ser nulo" //$NON-NLS-1$
            );
        }

        final String inXml = new String(xml);

        if (inXml.contains(SIGNATURE_OBJECT_START)) {
            final String updatedSigNode = inXml.substring(
                    inXml.indexOf(SIGNATURE_OBJECT_START)
                            + SIGNATURE_OBJECT_START.length(),
                    inXml.indexOf(SIGNATURE_OBJECT_END));

            // Condicion especifica para firmas XAdES Enveloping/Detached
            if (updatedSigNode.endsWith("Signature>")) { //$NON-NLS-1$
                setSignature(Base64.encode(updatedSigNode.getBytes()));
            }
        }

        final SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        final SAXParser saxParser = spf.newSAXParser();
        final XMLReader xmlReader = saxParser.getXMLReader();
        xmlReader.setContentHandler(new CustomHandler(this));
        xmlReader.parse(new InputSource(new ByteArrayInputStream(xml)));
    }

    /**
     * Indica si la respuesta ha sido correcta o hay alg&uacute;n error.
     *
     * @return <code>true</code> si la respuesta ha sido correcta,
     *         <code>false</code> si hay alg&uacute;n error,
     */
    boolean isOk() {
        return SUCCESS.equals(getMajorCode());
    }

    /**
     * Obtiene la firma mejorada.
     *
     * @return Firma mejorada.
     * @throws IOException
     *             Si falla la descodificaci&oacute;n del Base64 enviado por la
     *             plataforma.
     */
    byte[] getUpgradedSignature() throws IOException {
        return Base64.decode(this.signature);
    }

    void setSignature(final String s) {
        this.signature = s;
    }

    /**
     * Obtiene el c&oacute;digo secundario de resultado.
     *
     * @return C&oacute;digo secundario de resultado.
     */
    String getMinorCode() {
        return this.minor;
    }

    void setMinorCode(final String mc) {
        this.minor = mc;
    }

    /**
     * Obtiene el c&oacute;digo principal de resultado.
     *
     * @return C&oacute;digo principal de resultado.
     */
    String getMajorCode() {
        return this.major;
    }

    void setMajorCode(final String mc) {
        this.major = mc;
    }

    /**
     * Obtiene la descripci&oacute;n de la respuesta.
     *
     * @return Descripci&oacute;n de la respuesta.
     */
    String getDescription() {
        return this.desc;
    }

    void setDescription(final String d) {
        this.desc = d;
    }

    /**
     * Obtiene el formato al que se ha actualizado la firma.
     *
     * @return Formato actualizado de la respuesta.
     */
    String getSignatureForm() {
        return this.signatureForm;
    }

    void setSignatureForm(final String s) {
        this.signatureForm = s;
    }

    private static class CustomHandler extends DefaultHandler {

        private static final String TAG_RESULT_MAJOR = "ResultMajor"; //$NON-NLS-1$
        private static final String TAG_RESULT_MINOR = "ResultMinor"; //$NON-NLS-1$
        private static final String TAG_RESULT_MESSAGE = "ResultMessage"; //$NON-NLS-1$
        private static final String TAG_BASE64_SIGNATURE = "Base64Signature"; //$NON-NLS-1$
        private static final String TAG_BASE64_XML = "Base64XML"; //$NON-NLS-1$
        private static final String TAG_SIGNATURE_FORM = "SignatureForm"; //$NON-NLS-1$

        private final UpgradeResponse verifyResponse;

        private boolean overResultMajor = false;
        private boolean overResultMinor = false;
        private boolean overResultMessage = false;
        private boolean overBase64Signature = false;
        private boolean overBase64Xml = false;
        private boolean overSignatureForm = false;

        CustomHandler(final UpgradeResponse vr) {
            this.verifyResponse = vr;
        }

        @Override
        public void startElement(final String namespaceURI,
                final String localName, final String qName,
                final Attributes atts) throws SAXException {
            if (TAG_RESULT_MAJOR.equals(localName)) {
                this.overResultMajor = true;
                this.overResultMinor = false;
                this.overResultMessage = false;
                this.overBase64Signature = false;
                this.overBase64Xml = false;
                this.overSignatureForm = false;
            } else if (TAG_RESULT_MESSAGE.equals(localName)) {
                this.overResultMajor = false;
                this.overResultMinor = false;
                this.overResultMessage = true;
                this.overBase64Signature = false;
                this.overBase64Xml = false;
                this.overSignatureForm = false;
            } else if (TAG_BASE64_SIGNATURE.equals(localName)) {
                this.overResultMajor = false;
                this.overResultMinor = false;
                this.overResultMessage = false;
                this.overBase64Signature = true;
                this.overBase64Xml = false;
                this.overSignatureForm = false;
            } else if (TAG_RESULT_MINOR.equals(localName)) {
                this.overResultMajor = false;
                this.overResultMinor = true;
                this.overResultMessage = false;
                this.overBase64Signature = false;
                this.overBase64Xml = false;
                this.overSignatureForm = false;
            } else if (TAG_BASE64_XML.equals(localName)) {
                this.overResultMajor = false;
                this.overResultMinor = false;
                this.overResultMessage = false;
                this.overBase64Signature = false;
                this.overBase64Xml = true;
                this.overSignatureForm = false;
            } else if (TAG_SIGNATURE_FORM.equals(localName)) {
                this.overResultMajor = false;
                this.overResultMinor = false;
                this.overResultMessage = false;
                this.overBase64Signature = false;
                this.overBase64Xml = false;
                this.overSignatureForm = true;
            }
        }

        @Override
        public void characters(final char[] ch, final int start,
                final int length) {

            final String value = new String(ch, start, length).trim();

            if (this.overResultMajor) {
                this.verifyResponse.setMajorCode(DssServicesUtils
                        .cleanDssResult(value));
                this.overResultMajor = false;
            } else if (this.overResultMessage) {
                this.verifyResponse.setDescription(value);
                this.overResultMessage = false;
            } else if (this.overBase64Signature) {
                this.verifyResponse.setSignature(value);
                this.overBase64Signature = false;
            } else if (this.overResultMinor) {
                this.verifyResponse.setMinorCode(value);
                this.overResultMinor = false;
            } else if (this.overBase64Xml) {
                this.verifyResponse.setSignature(value);
                this.overBase64Xml = false;
            } else if (this.overSignatureForm) {
                this.verifyResponse.setSignatureForm(value);
                this.overSignatureForm = false;
            }
        }
    }

}
