package es.gob.fire.server.services.internal;

public interface PostSignBatchRecover {

	byte[] recoverSign() throws BatchRecoverException;
}
