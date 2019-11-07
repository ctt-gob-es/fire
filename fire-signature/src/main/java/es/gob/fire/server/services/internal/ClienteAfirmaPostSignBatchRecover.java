package es.gob.fire.server.services.internal;

/**
 * Clase para la recuperaci&oacute;n de la firma generada en un proceso de firma de
 * lote con el Cliente @firma.
 */
public class ClienteAfirmaPostSignBatchRecover implements PostSignBatchRecover {

	private final String docId;
	private final BatchResult batchResult;

	public ClienteAfirmaPostSignBatchRecover(final String docId, final BatchResult batchResult) {
		this.docId = docId;
		this.batchResult = batchResult;
	}

	@Override
	public byte[] recoverSign() throws BatchRecoverException {

		final String docFilename = this.batchResult.getDocumentReference(this.docId);

    	byte[] signature;
        try {
        	signature = TempDocumentsManager.retrieveDocument(docFilename);
        }
        catch (final Exception e) {
        	throw new BatchRecoverException("No se encuentra la firma", //$NON-NLS-1$
        			e, BatchResult.DATA_NOT_FOUND);
		}
		return signature;
	}
}
