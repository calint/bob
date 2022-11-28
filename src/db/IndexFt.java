package db;

import java.sql.SQLException;
import java.sql.Statement;

/** Full text index of column(s). */
public final class IndexFt extends Index {
	public IndexFt(final DbField... fld) {
		super(fld);
	}

	@Override
	protected void createIndex(final Statement stmt) throws SQLException {
		final StringBuilder sb = new StringBuilder(128);
		sb.append("create fulltext index ").append(name).append(" on ").append(tableName).append('(');
		for (final DbField f : fields) {
			sb.append(f.name).append(',');
		}
		sb.setLength(sb.length() - 1);
		sb.append(')');

		final String sql = sb.toString();
		Db.log(sql);
		stmt.execute(sql);
	}
}
