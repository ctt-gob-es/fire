package es.gob.fire.server.services.internal;

import java.util.Map;

import es.gob.afirma.core.signers.TriphaseData;
import es.gob.afirma.core.signers.TriphaseData.TriSign;
import es.gob.fire.server.connector.FIReSignatureException;
import es.gob.fire.server.services.FIReTriHelper;
import es.gob.fire.server.services.FIReTriSignIdProcessor;

/**
 * Clase para la composici&oacute;n y recuperaci&oacute;n de la firma iniciada
 * como parte de un proceso de firma con un proveedor de firma en la nube.
 */
public class CloudPostSignBatchRecover implements PostSignBatchRecover {

	private final String docId;
	private final String algorithm;
	private final SignBatchConfig signConfig;
	private final Map<String, byte[]> pkcs1s;
	private final TriphaseData partialTd;
	private final BatchResult batchResult;
	private final LogTransactionFormatter logF;

	public CloudPostSignBatchRecover(final String docId, final String algorithm,
			final SignBatchConfig signConfig, final Map<String, byte[]> pkcs1s,
			final TriphaseData partialTd, final BatchResult batchResult,
			final LogTransactionFormatter logF) {
		this.docId = docId;
		this.algorithm = algorithm;
		this.signConfig = signConfig;
		this.pkcs1s = pkcs1s;
		this.partialTd = partialTd;
		this.batchResult = batchResult;
		this.logF = logF;
	}

	@Override
	public byte[] recoverSign() throws BatchRecoverException {

		final String docFilename = this.batchResult.getDocumentReference(this.docId);

    	final byte[] data;
        try {
        	data = TempDocumentsManager.retrieveDocument(docFilename);
        }
        catch (final Exception e) {
        	throw new BatchRecoverException("No se encuentran los datos a firmar", //$NON-NLS-1$
        			e, BatchResult.DATA_NOT_FOUND);
		}

        // De la informacion trifasica que tenemos, extraemos lo correspondiente
    	// a la firma de este documento. Tenemos cuidado de deshacer los cambios en los
    	// ID que pudieran haberse hecho para evitar problemas con los ID repetidos
    	final TriphaseData currentTd = new TriphaseData();
        for (final TriSign triSign : this.partialTd.getTriSigns()) {
        	if (this.docId.equals(FIReTriSignIdProcessor.unmake(triSign.getId()))) {
        		currentTd.addSignOperation(triSign);
        	}
        }

        // Insertamos los PKCS#1 en la sesion trifasica
        for (final String key : this.pkcs1s.keySet()) {
           	FIReTriHelper.addPkcs1ToTriSign(this.pkcs1s.get(key), key, currentTd);
        }

        // Realizamos la postfirma para calcular la firma
    	byte[] signature;
    	try {
    		signature = FIReTriHelper.getPostSign(
    				this.signConfig.getCryptoOperation(),
    				this.signConfig.getFormat(),
    				this.algorithm,
    				this.signConfig.getExtraParams(),
    				this.batchResult.getSigningCertificate(),
    				data,
    				currentTd,
    				this.logF);
    	}
    	catch (final FIReSignatureException e) {
    		throw new BatchRecoverException(String.format(
    				"Error durante la postfirma. Verifique el codigo de operacion (%1s) y el formato (%2s)", //$NON-NLS-1$
					this.signConfig.getCryptoOperation(), this.signConfig.getFormat()), e,
    				BatchResult.POSTSIGN_ERROR);
    	}

		return signature;
	}

}
