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

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Respuesta de una consulta de mejora contra la plataforma Afirma.
 *
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s.
 */
public class VerifyResponse {

    private static final String SUCCESS = "Success"; //$NON-NLS-1$

    private String major = null;
    private String minor = null;
    private String desc = null;

    VerifyResponse(final byte[] xml) throws ParserConfigurationException,
            SAXException, IOException {

        if (xml == null) {
            throw new IllegalArgumentException(
                    "El XML de entrada no puede ser nulo" //$NON-NLS-1$
            );
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
    public boolean isOk() {
        return SUCCESS.equals(getMajorCode());
    }

    /**
     * Obtiene el c&oacute;digo secundario de resultado.
     *
     * @return C&oacute;digo secundario de resultado.
     */
    public String getMinorCode() {
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
    public String getMajorCode() {
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
    public String getDescription() {
        return this.desc;
    }

    void setDescription(final String d) {
        this.desc = d;
    }

    private static class CustomHandler extends DefaultHandler {

        private static final String TAG_RESULT_MAJOR = "ResultMajor"; //$NON-NLS-1$
        private static final String TAG_RESULT_MINOR = "ResultMinor"; //$NON-NLS-1$
        private static final String TAG_RESULT_MESSAGE = "ResultMessage"; //$NON-NLS-1$

        private final VerifyResponse verifyResponse;

        private boolean overResultMajor = false;
        private boolean overResultMinor = false;
        private boolean overResultMessage = false;

        CustomHandler(final VerifyResponse vr) {
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
            } else if (TAG_RESULT_MESSAGE.equals(localName)) {
                this.overResultMajor = false;
                this.overResultMinor = false;
                this.overResultMessage = true;
            } else if (TAG_RESULT_MINOR.equals(localName)) {
                this.overResultMajor = false;
                this.overResultMinor = true;
                this.overResultMessage = false;
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
            } else if (this.overResultMinor) {
                this.verifyResponse.setMinorCode(value);
                this.overResultMinor = false;
            }
        }
    }

}
