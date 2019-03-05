package es.gob.fire.statistics.entity;

public final class TransactionTotal {

	private long dataSize;

	private long total;

	public TransactionTotal(final long dataSize, final long total) {
		this.dataSize = dataSize;
		this.total = total;
	}

	public long getDataSize() {
		return this.dataSize;
	}

	public void setDataSize(final long dataSize) {
		this.dataSize = dataSize;
	}

	public long getTotal() {
		return this.total;
	}

	public void setTotal(final long total) {
		this.total = total;
	}
}
