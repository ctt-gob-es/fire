package es.gob.fire.server.services.batch;

import es.gob.fire.server.services.batch.SingleSignConstants.AsyncCipherAlgorithm;
import es.gob.fire.server.services.batch.SingleSignConstants.DigestAlgorithm;

/** Algoritmo de firma. */
public class SignatureAlgorithm {

	final DigestAlgorithm digestAlgo;
	AsyncCipherAlgorithm cipherAlgo;

	public SignatureAlgorithm(final DigestAlgorithm digestAlgo, final AsyncCipherAlgorithm cipherAlgo) {
		this.digestAlgo = digestAlgo;
		this.cipherAlgo = cipherAlgo;
	}

	public String getName() {
		return this.digestAlgo.getName() + "with" + this.cipherAlgo.getName(); //$NON-NLS-1$
	}

	@Override
	public String toString() {
		return getName();
	}
}
