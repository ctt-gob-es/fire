package es.gob.log.consumer;

public class LogErrors {

	private  String msgError = null;
	private  int numError = 0;

	public LogErrors(final String msgError, final int numError) {
		super();
		this.msgError = msgError;
		this.numError = numError;
	}
	public  final String getMsgError() {
		return this.msgError;
	}
	public  final void setMsgError(final String msgError) {
		this.msgError = msgError;
	}
	public  final int getNumError() {
		return this.numError;
	}
	public  final void setNumError(final int numError) {
		this.numError = numError;
	}


}
