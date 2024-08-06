// reviewed: 2024-08-05
package db;

/** Parameter to get(...) limiting the result list. */
public final class Limit {

    private final int offset;
    private final int rowCount;

    public Limit(final int offset, final int rowCount) {
        this.offset = offset;
        this.rowCount = rowCount;
    }

    void sql_appendToQuery(final StringBuilder sb) {
        sb.append("limit ").append(offset).append(',').append(rowCount);
    }

}
