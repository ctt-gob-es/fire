package es.gob.fire.services.statistics.entity;

public class ApplicationSize {

	private Long size = new Long(0L);
	private String id_Transaction;
	private String application;


	public final Long getSize() {
		return this.size;
	}
	public final void setSize(final Long size) {
		this.size = size;
	}
	public final String getId_Transaction() {
		return this.id_Transaction;
	}
	public final void setId_Transaction(final String id_Transaction) {
		this.id_Transaction = id_Transaction;
	}
	public final String getApplication() {
		return this.application;
	}
	public final void setApplication(final String application) {
		this.application = application;
	}


}
