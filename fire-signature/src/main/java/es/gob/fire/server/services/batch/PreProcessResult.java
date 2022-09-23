package es.gob.fire.server.services.batch;

import es.gob.afirma.core.signers.TriphaseData;

/**
 * Resultado de la prefirma de una firma individual, que puede ser la prefirma generada
 * o el error resultante.
 */
public class PreProcessResult {

	private final ResultSingleSign signResult;

	private final TriphaseData presign;

	public PreProcessResult(final TriphaseData presign) {
		this.signResult = null;
		this.presign = presign;
	}

	public PreProcessResult(final ResultSingleSign result) {
		this.signResult = result;
		this.presign = null;
	}

	public TriphaseData getPresign() {
		return this.presign;
	}

	public ResultSingleSign getSignResult() {
		return this.signResult;
	}
}
