/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.services;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import es.gob.afirma.core.misc.Base64;
import es.gob.afirma.core.signers.AOSignConstants;
import es.gob.afirma.core.signers.CounterSignTarget;
import es.gob.afirma.core.signers.ExtraParamsProcessor;
import es.gob.afirma.core.signers.TriphaseData;
import es.gob.afirma.core.signers.TriphaseData.TriSign;
import es.gob.afirma.triphase.signer.processors.CAdESASiCSTriPhasePreProcessor;
import es.gob.afirma.triphase.signer.processors.CAdESTriPhasePreProcessor;
import es.gob.afirma.triphase.signer.processors.FacturaETriPhasePreProcessor;
import es.gob.afirma.triphase.signer.processors.PAdESTriPhasePreProcessor;
import es.gob.afirma.triphase.signer.processors.TriPhasePreProcessor;
import es.gob.afirma.triphase.signer.processors.XAdESASiCSTriPhasePreProcessor;
import es.gob.afirma.triphase.signer.processors.XAdESTriPhasePreProcessor;
import es.gob.fire.server.connector.DocInfo;
import es.gob.fire.server.connector.FIReSignatureException;
import es.gob.fire.server.services.internal.BatchDocument;
import es.gob.fire.server.services.internal.BatchResult;
import es.gob.fire.server.services.internal.Pkcs1TriPhasePreProcessor;
import es.gob.fire.server.services.internal.SignBatchConfig;
import es.gob.fire.signature.ConfigManager;

/**
 * Clase auxiliar que proporciona individualmente las operaciones de prefirma y postfirma.
 */
public final class FIReTriHelper {

    private static final String PARAM_VALUE_SUB_OPERATION_SIGN = "sign"; //$NON-NLS-1$
    private static final String PARAM_VALUE_SUB_OPERATION_COSIGN = "cosign"; //$NON-NLS-1$
    private static final String PARAM_VALUE_SUB_OPERATION_COUNTERSIGN = "countersign"; //$NON-NLS-1$

    /** Nombre del par&aacute;metro que identifica los nodos que deben contrafirmarse. */
    private static final String PARAM_NAME_TARGET_TYPE = "target"; //$NON-NLS-1$

    /** Nombre de la propiedad para almac&eacute;n de firmas PKCS#1. */
    private static final String PROPERTY_NAME_PKCS1_SIGN = "PK1"; //$NON-NLS-1$

    /** ExtraParam de Afirma con el que indicar el identificador de un documento. */
    private static final String AFIRMA_EXTRAPARAM_DOC_ID = "SignatureId"; //$NON-NLS-1$


	private static final boolean INSTALL_XMLDSIG_PROVIDER = ConfigManager.isAlternativeXmlDSigActive();

    private FIReTriHelper() {
        // No instanciable
    }

    /**
     * Ejecuta una operaci&oacute;n de prefirma dentro de un proceso de firma
     * trif&aacute;sica.
     * @param criptoOperation Operaci&oacute;n de firma a realizar ("sign", "cosign" o "countersign").
     * @param format Formato de firma.
     * @param algorithm Algoritmo de firma.
     * @param extraParams Configuraci&oacute;n de firma.
     * @param signerCert Certificado con el que se debe firmar.
     * @param docBytes Datos que se firman/multifirman.
     * @return Informaci&oacute;n de prefirma generada.
     * @throws FIReSignatureException Cuando ocurre un error durante la operaci&oacute;n.
     * @throws IOException Cuando ocurre un error al componer la estructura con la
     * informaci&oacute;n de la prefirma.
     */
    public final static TriphaseData getPreSign(final String criptoOperation,
                                         final String format,
                                         final String algorithm,
                                         final Properties extraParams,
                                         final X509Certificate signerCert,
                                         final byte[] docBytes) throws FIReSignatureException, IOException {

        // Instanciamos el preprocesador adecuado
        final TriPhasePreProcessor prep = getTriPhasePreProcessor(format);

        // Extraemos las propiedades propias de la configuracion de Cl@ve Firma
        final DocInfo docInfo = DocInfo.extractDocInfo(extraParams);

        // Expandimos las propiedades que lo necesites (como las de politica de firma)
        Properties expandedParams = extraParams;
        if (expandedParams != null) {
        	try {
        		expandedParams = ExtraParamsProcessor.expandProperties(expandedParams, docBytes, format);
        	} catch (final Exception e) {
        		Logger.getLogger("es.gob.afirma").warning("No se ha podido expandir la politica de firma: " + e); //$NON-NLS-1$ //$NON-NLS-2$
        	}
        }

        TriphaseData preRes;
        if (PARAM_VALUE_SUB_OPERATION_SIGN.equalsIgnoreCase(criptoOperation)) {
            try {
                preRes = prep.preProcessPreSign(
            		docBytes,
            		algorithm,
                    new X509Certificate[] { signerCert },
                    expandedParams
                );
            }
            catch (final Exception e) {
                throw new FIReSignatureException(
            		"Error en la prefirma: " + e, e //$NON-NLS-1$
                );
            }
        }
        else if (PARAM_VALUE_SUB_OPERATION_COSIGN.equalsIgnoreCase(criptoOperation)) {
            try {
                preRes = prep.preProcessPreCoSign(
            		docBytes,
            		algorithm,
                    new X509Certificate[] { signerCert },
                    expandedParams
                );
            }
            catch (final Exception e) {
                throw new FIReSignatureException(
                    "Error en la precofirma: " + e,  e //$NON-NLS-1$
                );
            }
        }
        else if (PARAM_VALUE_SUB_OPERATION_COUNTERSIGN.equalsIgnoreCase(criptoOperation)) {

            CounterSignTarget target = CounterSignTarget.LEAFS;
            if (expandedParams != null && expandedParams.containsKey(PARAM_NAME_TARGET_TYPE)) {
                final String targetValue = expandedParams.getProperty(PARAM_NAME_TARGET_TYPE).trim();
                if (CounterSignTarget.TREE.toString().equalsIgnoreCase(targetValue)) {
                    target = CounterSignTarget.TREE;
                }
            }

            try {
                preRes = prep.preProcessPreCounterSign(
            		docBytes,
                    algorithm,
                    new X509Certificate[] { signerCert },
                    expandedParams,
                    target
                );
            }
            catch (final Exception e) {
                throw new FIReSignatureException(
                    "Error en la precontrafirma: " + e, e //$NON-NLS-1$
                );
            }

            // El core del cliente @firma devuelve todas las firmas de una contrafirma con el mismo
            // ID para despues poder asociarlas. Como algunos proveedores en la nube no permiten que
            // se proporcionen varios datos con el mismo ID, los modificaremos para que sean distintos
            // y, posteriormente, desharemos el cambio para ejecutar la postfirma
            preRes = FIReTriSignIdProcessor.make(preRes);
        }
        else {
            throw new FIReSignatureException(
                "No se reconoce el codigo de sub-operacion: " + criptoOperation //$NON-NLS-1$
            );
        }
        // Asignamos a la firma la informacion del documento que firma para permitir que
        // posteriormente el conector la procese
        DocInfo.addDocInfoToSign(preRes.getSign(0), docInfo);

        return preRes;
    }

    /**
     * Ejecuta una operaci&oacute;n de prefirma dentro de un proceso de firma
     * trif&aacute;sica.
     * @param criptoOperation Operaci&oacute;n de firma a realizar ("sign", "cosign" o "countersign") por defecto.
     * @param format Formato de firma por defecto.
     * @param algorithm Algoritmo de firma por defecto.
     * @param extraParams Configuraci&oacute;n de firma por defecto.
     * @param signerCert Certificado con el que se debe firmar.
     * @param documents Datos que se firman/multifirman.
     * @param stopOnError Indica que se debe dejar de procesar las peticiones de firma en
     * 					  el momento de encontrar un error.
     * @return Informaci&oacute;n de prefirma generada.
     * @throws FIReSignatureException Cuando ocurre un error durante la operaci&oacute;n.
     * @throws IOException Cuando ocurre un error al componer la estructura con la
     * informaci&oacute;n de la prefirma.
     */
    public final static TriphaseData getPreSign(final String criptoOperation,
                                         final String format,
                                         final String algorithm,
                                         final Properties extraParams,
                                         final X509Certificate signerCert,
                                         final List<BatchDocument> documents,
                                         final boolean stopOnError) throws FIReSignatureException, IOException {

        final TriphaseData batchTriPhaseData = new TriphaseData();

        boolean stopOperation = false;
        for (final BatchDocument doc : documents) {

        	// Si se debe parar la operacion (debido a un error
        	// producido en alguna firma anterior), se marca todo como no procesado
        	if (stopOperation) {
        		doc.setBatchResult(BatchResult.NO_PROCESSED);
        		continue;
        	}

        	// Nos saltamos los casos en los que no se pudieran recuperar los datos
        	if (doc.getData() == null) {
        		continue;
        	}

        	final SignBatchConfig signConfig = doc.getConfig();
        	final String cop = signConfig != null ? signConfig.getCryptoOperation() : criptoOperation;
        	final String frmt = signConfig != null ? signConfig.getFormat() : format;
        	final Properties params = signConfig != null ? signConfig.getExtraParams() : extraParams;

        	// Instanciamos el preprocesador adecuado
        	final TriPhasePreProcessor prep;
            try {
            	prep = getTriPhasePreProcessor(frmt);
            }
            catch (final FIReSignatureException e) {
    			if (stopOnError) {
    				stopOperation = true;
				}
        		doc.setBatchResult(BatchResult.PRESIGN_ERROR);
    			continue;
            }

        	// Expandimos la configuracion teniendo en cuenta que puede variar
        	// el resultado segun los datos a los que aplica
        	Properties expandedParams = null;
        	if (params != null) {
        		try {
        			expandedParams = ExtraParamsProcessor.expandProperties((Properties) params.clone(), doc.getData(), frmt);
        		} catch (final Exception e) {
        			Logger.getLogger("es.gob.afirma").warning("No se ha podido expandir la politica de firma: " + e); //$NON-NLS-1$ //$NON-NLS-2$
        			expandedParams = new Properties();
        		}
        	}
        	else {
        		expandedParams = new Properties();
        	}

        	// Agregamos a la configuracion el parametro que hace que las firmas trifasicas
        	// establezcan un ID concreto para todas las prefirmas que las compongan
        	if (doc.getId() != null) {
        		expandedParams.setProperty(AFIRMA_EXTRAPARAM_DOC_ID, doc.getId());
        	}

        	TriphaseData preRes;
        	if (PARAM_VALUE_SUB_OPERATION_SIGN.equalsIgnoreCase(cop)) {
        		try {
        			preRes = prep.preProcessPreSign(
        					doc.getData(),
        					algorithm,
        					new X509Certificate[] { signerCert },
        					expandedParams
        					);
        		}
        		catch (final Exception e) {
            		if (stopOnError) {
            			stopOperation = true;
					}
            		doc.setBatchResult(BatchResult.PRESIGN_ERROR);
        			continue;
        		}
        	}
        	else if (PARAM_VALUE_SUB_OPERATION_COSIGN.equalsIgnoreCase(cop)) {
        		try {
        			preRes = prep.preProcessPreCoSign(
        					doc.getData(),
        					algorithm,
        					new X509Certificate[] { signerCert },
        					expandedParams
        					);
        		}
        		catch (final Exception e) {
        			if (stopOnError) {
        				stopOperation = true;
					}
            		doc.setBatchResult(BatchResult.PRESIGN_ERROR);
        			continue;
        		}
        	}
        	else if (PARAM_VALUE_SUB_OPERATION_COUNTERSIGN.equalsIgnoreCase(cop)) {

        		CounterSignTarget target = CounterSignTarget.LEAFS;
        		if (expandedParams.containsKey(PARAM_NAME_TARGET_TYPE)) {
        			final String targetValue = expandedParams.getProperty(PARAM_NAME_TARGET_TYPE).trim();
        			if (CounterSignTarget.TREE.toString().equalsIgnoreCase(targetValue)) {
        				target = CounterSignTarget.TREE;
        			}
        		}

        		try {
        			preRes = prep.preProcessPreCounterSign(
        					doc.getData(),
        					algorithm,
        					new X509Certificate[] { signerCert },
        					expandedParams,
        					target
        					);
        		}
        		catch (final Exception e) {
        			if (stopOnError) {
        				stopOperation = true;
					}
            		doc.setBatchResult(BatchResult.PRESIGN_ERROR);
        			continue;
        		}

        		// El core del cliente @firma devuelve todas las firmas de una contrafirma con el mismo
                // ID para despues poder asociarlas. Como esto no esta permitido por algunos proveedores,
                // modificaremos los ID para garantizar que son distintos y, antes de ejecutar la postfirma,
                // desharemos el cambio
        		preRes = FIReTriSignIdProcessor.make(preRes);
        	}
        	else {
        		if (stopOnError) {
        			stopOperation = true;
        		}
        		doc.setBatchResult(BatchResult.INVALID_SIGNATURE_OPERATION);
    			continue;
        	}

        	// Agregamos todas las firmas individuales al lote global.
        	// Le agregamos la informacion del documento, a su primera prefirma
        	boolean first = true;
        	for (final TriSign triSign : preRes.getTriSigns()) {
        		// Le agregamos la informacion del documento, a su primera prefirma
        		if (first && doc.getDocInfo() != null) {
        			DocInfo.addDocInfoToSign(triSign, doc.getDocInfo());
        		}
        		first = false;
        		batchTriPhaseData.addSignOperation(triSign);
        	}
        }

        return batchTriPhaseData;
    }

    /**
     * Agrega un PKCS#1 a la informaci&oacute;n ya disponible de una operaci&oacute;n
     * trif&aacute;sica.
     * @param pkcs1 PKCS#1 a agregar.
     * @param signId Identificador de la firma concreta a la que pertenece el PKCS#1.
     * @param td Conjunto de informaci&oacute;n de firma trif&aacute;sica a la que se agrega el PKCS#1.
     */
    public static void addPkcs1ToTriSign(final byte[] pkcs1, final String signId,
            final TriphaseData td) {
        if (pkcs1 == null || pkcs1.length < 1) {
            throw new IllegalArgumentException(
                    "La firma PKCS#1 no puede ser nula ni vacia"); //$NON-NLS-1$
        }
        if (signId == null || signId.isEmpty()) {
            throw new IllegalArgumentException(
                    "El identificador de firma no puede ser nulo ni vacio"); //$NON-NLS-1$
        }
        if (td == null) {
            throw new IllegalArgumentException(
                    "Los datos de sesion trifasica no pueden ser nulos"); //$NON-NLS-1$
        }

        for (final TriSign triSign : td.getTriSigns()) {
        	if (signId.equals(triSign.getId())) {
        		triSign.addProperty(PROPERTY_NAME_PKCS1_SIGN, Base64.encode(pkcs1));
        		break;
        	}
        }
    }

    /**
     * Ejecuta una operaci&oacute;n de postfirma dentro de un proceso de firma
     * trif&aacute;sica para una firma trif&aacute;sica concreta.
     * @param criptoOperation Operaci&oacute;n de firma a realizar ("sign", "cosign" o "countersign").
     * @param format Formato de firma.
     * @param algorithm Algoritmo de firma.
     * @param extraParams Configuraci&oacute;n de firma.
     * @param signerCert Certificado con el que se debe firmar.
     * @param docBytes Datos que se firman/multifirman.
     * @param triphaseData Conjunto de datos de la firma trif&aacute;sico obtenido de la
     * ejecuci&oacute;n de la prefirma y a&ntilde;adido de la firma.
     * @return Firma electr&oacute;nica resultante.
     * @throws FIReSignatureException Cuando ocurre un error durante la operaci&oacute;n.
     */
    public final static byte[] getPostSign(final String criptoOperation,
                                    final String format,
                                    final String algorithm,
                                    final Properties extraParams,
                                    final X509Certificate signerCert,
                                    final byte[] docBytes,
                                    final TriphaseData triphaseData) throws FIReSignatureException {

        // Instanciamos el preprocesador adecuado
        final TriPhasePreProcessor prep = getTriPhasePreProcessor(format);

        Properties expandedParams = extraParams;
        if (expandedParams != null) {
        	try {
        		expandedParams = ExtraParamsProcessor.expandProperties(expandedParams, docBytes, format);
        	} catch (final Exception e) {
        		Logger.getLogger("es.gob.afirma").warning("No se ha podido expandir la politica de firma: " + e); //$NON-NLS-1$ //$NON-NLS-2$
        	}
        }

        if (PARAM_VALUE_SUB_OPERATION_SIGN.equalsIgnoreCase(criptoOperation)) {
            try {
                return prep.preProcessPostSign(
            		docBytes,
            		algorithm,
                    new X509Certificate[] { signerCert },
                    expandedParams,
                    triphaseData
                );
            }
            catch (final Exception e) {
                throw new FIReSignatureException(
            		"Error en la postfirma: " + e, e //$NON-NLS-1$
                );
            }
        }
        else if (PARAM_VALUE_SUB_OPERATION_COSIGN.equalsIgnoreCase(criptoOperation)) {
            try {
                return prep.preProcessPostCoSign(
            		docBytes,
            		algorithm,
                    new X509Certificate[] { signerCert },
                    expandedParams,
                    triphaseData
                );
            }
            catch (final Exception e) {
                throw new FIReSignatureException(
                    "Error en la postcofirma: " + e, e //$NON-NLS-1$
                );
            }
        }
        else if (PARAM_VALUE_SUB_OPERATION_COUNTERSIGN.equalsIgnoreCase(criptoOperation)) {

            CounterSignTarget target = CounterSignTarget.LEAFS;
            if (expandedParams != null && expandedParams.containsKey(PARAM_NAME_TARGET_TYPE)) {
                final String targetValue = expandedParams.getProperty(PARAM_NAME_TARGET_TYPE).trim();
                if (CounterSignTarget.TREE.toString().equalsIgnoreCase(targetValue)) {
                    target = CounterSignTarget.TREE;
                }
            }

            // La postfirma se invoca deshaciendo previamente el cambio que garantizaba que
            // los ID de las firmas eran distintos
            try {
                return prep.preProcessPostCounterSign(
            		docBytes,
                    algorithm,
                    new X509Certificate[] { signerCert },
                    expandedParams,
                    FIReTriSignIdProcessor.unmake(triphaseData),
                    target
                );
            }
            catch (final Exception e) {
                throw new FIReSignatureException(
                    "Error en la postcontrafirma: " + e, e //$NON-NLS-1$
                );
            }
        }
        else {
            throw new FIReSignatureException(
                "No se reconoce el tipo de operacion de firma: " + criptoOperation //$NON-NLS-1$
            );
        }
    }

    /**
     * Obtiene el procesador encargado de gestionar firmas trifasicas asociado
     * a un formato de firma concreto.
     * @param format Formato de firma.
     * @return Procesador.
     * @throws FIReSignatureException Cuando el formato indicado no est&aacute;
     * soportado.
     */
    private static TriPhasePreProcessor getTriPhasePreProcessor(final String format) throws FIReSignatureException {
    	final TriPhasePreProcessor prep;
    	if (AOSignConstants.SIGN_FORMAT_PADES.equalsIgnoreCase(format)
    			|| AOSignConstants.SIGN_FORMAT_PADES_TRI.equalsIgnoreCase(format)) {
    		prep = new PAdESTriPhasePreProcessor();
    	}
    	else if (AOSignConstants.SIGN_FORMAT_CADES.equalsIgnoreCase(format)
    			|| AOSignConstants.SIGN_FORMAT_CADES_TRI.equalsIgnoreCase(format)) {
    		prep = new CAdESTriPhasePreProcessor();
    	}
    	else if (AOSignConstants.SIGN_FORMAT_CADES_ASIC_S.equalsIgnoreCase(format)
    			|| AOSignConstants.SIGN_FORMAT_CADES_ASIC_S_TRI.equalsIgnoreCase(format)) {
    		prep = new CAdESASiCSTriPhasePreProcessor();
    	}
    	else if (AOSignConstants.SIGN_FORMAT_XADES.equalsIgnoreCase(format)
    			|| AOSignConstants.SIGN_FORMAT_XADES_TRI.equalsIgnoreCase(format)) {
    		prep = new XAdESTriPhasePreProcessor(INSTALL_XMLDSIG_PROVIDER);
    	}
    	else if (AOSignConstants.SIGN_FORMAT_FACTURAE.equalsIgnoreCase(format)
    			|| AOSignConstants.SIGN_FORMAT_FACTURAE_TRI.equalsIgnoreCase(format)) {
    		prep = new FacturaETriPhasePreProcessor(INSTALL_XMLDSIG_PROVIDER);
    	}
    	else if (AOSignConstants.SIGN_FORMAT_XADES_ASIC_S.equalsIgnoreCase(format)
    			|| AOSignConstants.SIGN_FORMAT_XADES_ASIC_S_TRI.equalsIgnoreCase(format)) {
    		prep = new XAdESASiCSTriPhasePreProcessor(INSTALL_XMLDSIG_PROVIDER);
    	}
    	else if (AOSignConstants.SIGN_FORMAT_PKCS1.equalsIgnoreCase(format) ||
    			AOSignConstants.SIGN_FORMAT_PKCS1_TRI.equalsIgnoreCase(format)) {
    		prep = new Pkcs1TriPhasePreProcessor();
    	}
    	else {
    		throw new FIReSignatureException(
    				"No se soporta el formato " + format //$NON-NLS-1$
    				);
    	}
    	return prep;
    }

    /**
     * Permite obtener el nombre del formato trif&aacute;sico asociado a un formato monof&aacute;sico.
     * Si el formato no se soporta o es nulo, se devolvera el mismo valor de entrada.
     * @param format Nombre de formato de firma.
     * @return Nombre del formato trif&aacute;sico o la propia entrada si no se soporta.
     */
    public static String getTriPhaseFormat(final String format) {

    	if (AOSignConstants.SIGN_FORMAT_PADES.equalsIgnoreCase(format)) {
    		return AOSignConstants.SIGN_FORMAT_PADES_TRI;
    	}
    	else if (AOSignConstants.SIGN_FORMAT_CADES.equalsIgnoreCase(format)) {
        	return AOSignConstants.SIGN_FORMAT_CADES_TRI;
    	}
    	else if (AOSignConstants.SIGN_FORMAT_CADES_ASIC_S.equalsIgnoreCase(format)) {
        	return AOSignConstants.SIGN_FORMAT_CADES_ASIC_S_TRI;
    	}
    	else if (AOSignConstants.SIGN_FORMAT_FACTURAE.equalsIgnoreCase(format)) {
        	return AOSignConstants.SIGN_FORMAT_FACTURAE_TRI;
    	}
    	else if (AOSignConstants.SIGN_FORMAT_XADES.equalsIgnoreCase(format)) {
        	return AOSignConstants.SIGN_FORMAT_XADES_TRI;
    	}
    	else if (AOSignConstants.SIGN_FORMAT_XADES_ASIC_S.equalsIgnoreCase(format)) {
        	return AOSignConstants.SIGN_FORMAT_XADES_ASIC_S_TRI;
    	}
    	else if (AOSignConstants.SIGN_FORMAT_PKCS1.equalsIgnoreCase(format)) {
        	return AOSignConstants.SIGN_FORMAT_PKCS1_TRI;
    	}
    	return format;
    }
}
