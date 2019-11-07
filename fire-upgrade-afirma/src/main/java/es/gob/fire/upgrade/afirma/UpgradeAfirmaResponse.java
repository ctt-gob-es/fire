/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.upgrade.afirma;

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
class UpgradeAfirmaResponse {

    private static final String SUCCESS = "Success"; //$NON-NLS-1$

    private static final String PENDING = "Pending"; //$NON-NLS-1$

    private String major = null;
    private String minor = null;
    private String desc = null;

    private String signatureForm = null;

    private String signature = null;

    private String responseId = null;

    private String responseTime = null;

    private static final String SIGNATURE_OBJECT_START = "<dss:SignatureObject>"; //$NON-NLS-1$
    private static final String SIGNATURE_OBJECT_END = "</dss:SignatureObject>"; //$NON-NLS-1$

    UpgradeAfirmaResponse(final byte[] xml) throws ParserConfigurationException,
            SAXException, IOException {

        if (xml == null) {
            throw new IllegalArgumentException(
                    "El XML de entrada no puede ser nulo" //$NON-NLS-1$
            );
        }

        final String inXml = new String(xml);

        int signaturePos = inXml.indexOf(SIGNATURE_OBJECT_START);
        if (signaturePos > -1) {
        	signaturePos += SIGNATURE_OBJECT_START.length();
            final String updatedSigNode = inXml.substring(
                    signaturePos,
                    inXml.indexOf(SIGNATURE_OBJECT_END, signaturePos));

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
     * Indica si se ha obtenido la firma actualizada.
     *
     * @return <code>true</code> si se ha obtenido la firma,
     *         <code>false</code> en caso contrario.
     */
    boolean isOk() {
        return SUCCESS.equals(getMajorCode());
    }

    /**
     * Indica si la respuesta esta pendiente de que se respete un periodo de gracia.
     *
     * @return <code>true</code> si la respuesta esta pendiente,
     *         <code>false</code> en caso contrario.
     */
    boolean isPending() {
        return PENDING.equals(getMajorCode());
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

    /**
     * Obtiene el Token para recoger la firma tras el periodo de gracia.
     * @return Token para recoger la firma.
     */
    String getResponseId() {
		return this.responseId;
	}

    void setResponseId(final String responseId) {
		this.responseId = responseId;
	}

    /**
     * Obtiene la fecha a partir de la que recoger la firma.
     * @return Fecha a partir de la que recoger la firma.
     */
    String getResponseTime() {
		return this.responseTime;
	}

    public void setResponseTime(final String responseTime) {
		this.responseTime = responseTime;
	}

    private static class CustomHandler extends DefaultHandler {

    	enum Tag {
    		RESULT_MAJOR("ResultMajor"), //$NON-NLS-1$
    		RESULT_MINOR("ResultMinor"), //$NON-NLS-1$
    		RESULT_MESSAGE("ResultMessage"), //$NON-NLS-1$
    		BASE64_SIGNATURE("Base64Signature"), //$NON-NLS-1$
    		BASE64_XML("Base64XML"), //$NON-NLS-1$
    		SIGNATURE_FORM("SignatureForm"), //$NON-NLS-1$
    		RESPONSE_ID("ResponseID"), //$NON-NLS-1$
    		RESPONSE_TIME("ResponseTime"); //$NON-NLS-1$

    		private String nodeName;

    		Tag(final String nodeName) {
    			this.nodeName = nodeName;
    		}

    		public static Tag parseTag(final String tagName) {
    			for (final Tag tag : values()) {
    				if (tag.nodeName.equals(tagName)) {
    					return tag;
    				}
    			}
    			return null;
    		}
    	}

        private final UpgradeAfirmaResponse verifyResponse;

        private Tag currentTag = null;

        CustomHandler(final UpgradeAfirmaResponse vr) {
            this.verifyResponse = vr;
        }

        @Override
        public void startElement(final String namespaceURI,
                final String localName, final String qName,
                final Attributes atts) throws SAXException {
        	this.currentTag = Tag.parseTag(localName);
        }

        @Override
        public void characters(final char[] ch, final int start,
                final int length) {

        	if (this.currentTag == null) {
        		return;
        	}

            final String value = new String(ch, start, length).trim();

            switch (this.currentTag) {
			case RESULT_MAJOR:
				this.verifyResponse.setMajorCode(DssServicesUtils.cleanDssResult(value));
				break;
			case RESULT_MINOR:
				this.verifyResponse.setMinorCode(value);
				break;
			case RESULT_MESSAGE:
				this.verifyResponse.setDescription(value);
				break;
			case BASE64_SIGNATURE:
			case BASE64_XML:
				this.verifyResponse.setSignature(value);
				break;
			case SIGNATURE_FORM:
				this.verifyResponse.setSignatureForm(value);
				break;
			case RESPONSE_ID:
				this.verifyResponse.setResponseId(value);
				break;
			case RESPONSE_TIME:
				this.verifyResponse.setResponseTime(value);
				break;
			default:
				break;
			}
        }
    }

}
