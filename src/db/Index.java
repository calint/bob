package db;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/** Index of column(s). */
public class Index {
	/** The class that declared the index. */
	Class<? extends DbObject> cls;
	/** The name of the field that declared the index. */
	String name;
	/** The table name for the declaring class. */
	String tableName;
	/** The fields in this index. */
	final ArrayList<DbField> fields = new ArrayList<DbField>();

	public Index(final DbField... flds) {
		for (final DbField f : flds) {
			fields.add(f);
		}
	}

	/** Called after DbClasses and DbRelations have been initialized. */
	void init(final DbClass c) {
	}

	protected void ensureIndex(final Statement stmt, final DatabaseMetaData dbm) throws Throwable {
		final ResultSet rs = dbm.getIndexInfo(null, null, tableName, false, false);
		boolean found = false;
		while (rs.next()) {
			final String indexName = rs.getString("INDEX_NAME");
			if (!indexName.equals(name)) {
				continue;
			}
			found = true;
			ensureColumnsInIndex(stmt, dbm);
			break;
		}
		rs.close();
		if (found == true)
			return;

		createIndex(stmt);
	}

	protected void ensureColumnsInIndex(final Statement stmt, final DatabaseMetaData dbm) throws Throwable {
		// get columns in index
		final ResultSet rs = dbm.getIndexInfo(null, null, tableName, false, false);
		final ArrayList<String> cols = new ArrayList<String>();
		while (rs.next()) {
			final String indexName = rs.getString("INDEX_NAME");
			if (!indexName.equals(name))
				continue;
			final String columnName = rs.getString("COLUMN_NAME");
			cols.add(columnName);
		}
		rs.close();

		// check if declared columns match existing columns
		boolean done = true;
		for (final DbField f : fields) {
			if (!cols.contains(f.name)) {
				done = false;
				break;
			}
			cols.remove(f.name);
		}
		if (cols.isEmpty() && done)
			return;

		// declared index does not match index in db. recreate index
		dropIndex(stmt);
		createIndex(stmt);
	}

	protected void createIndex(final Statement stmt) throws SQLException {
		final StringBuilder sb = new StringBuilder(128);
		sb.append("create index ").append(name).append(" on ").append(tableName).append('(');
		for (final DbField f : fields) {
			sb.append(f.name).append(',');
		}
		sb.setLength(sb.length() - 1);
		sb.append(')');

		final String sql = sb.toString();
		Db.log(sql);
		stmt.execute(sql);
	}

	protected void dropIndex(final Statement stmt) throws SQLException {
		// columns in index do not match declaration, delete and create
		final StringBuilder sb = new StringBuilder(128);
		sb.append("drop index ").append(name).append(" on ").append(tableName);

		final String sql = sb.toString();
		Db.log(sql);
		stmt.execute(sql);
	}

	@Override
	public String toString() {
		return name;
	}
}
