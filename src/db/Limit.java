package db;

/** Parameter to get(...) limiting the result list. */
public final class Limit {
	final private int offset;
	final private int rowCount;

	public Limit(final int offset, final int rowCount) {
		this.offset = offset;
		this.rowCount = rowCount;
	}

	void sql_appendToQuery(final StringBuilder sb) {
		sb.append("limit ").append(offset).append(',').append(rowCount).append(' ');
	}
}
