package db;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;

/** Represents the relation table for RelRefN. */
final class RelRefNMeta {
	final String tableName; // the table name for this association NN table
	final String fromTableName; // the table name of the source class
	final String fromColName; // column name referencing to source id
	final String toTableName; // table name of target class
	final String toColName; // column name referencing to target id

	RelRefNMeta(final Class<? extends DbObject> fromCls, final String relName, final Class<? extends DbObject> toCls) {
		this.fromTableName = Db.tableNameForJavaClass(fromCls);
		this.fromColName = "fromId";
		this.toTableName = Db.tableNameForJavaClass(toCls);
		this.toColName = "toId";
		tableName = new StringBuilder(256).append(TABLE_NAME_PREFIX).append(fromTableName).append('_').append(relName)
				.toString();
	}

	void ensureTable(final Statement stmt, final DatabaseMetaData dbm) throws Throwable {
		final ResultSet rs = dbm.getTables(null, null, tableName, new String[] { "TABLE" });
		if (rs.next()) {
			rs.close();
			return;// ? check columns
		}
		rs.close();

		final StringBuilder sb = new StringBuilder(256);
		sb.append("create table ").append(tableName).append('(').append(fromColName).append(" int,").append(toColName)
				.append(" int)");
		final String sql = sb.toString();
		Db.log(sql);
		stmt.execute(sql);
	}

	void sql_addToTable(final StringBuilder sb, final int fromId, final int toId) {
		sb.append("insert into ").append(tableName).append(" values(").append(fromId).append(',').append(toId)
				.append(')');
	}

	void sql_deleteFromTable(final StringBuilder sb, final int fromId, final int toId) {
		sb.append("delete from ").append(tableName).append(" where ").append(fromColName).append('=').append(fromId)
				.append(" and ").append(toColName).append('=').append(toId);

	}

	void sql_deleteAllFromTable(final StringBuilder sb, final int fromId) {
		sb.append("delete from ").append(tableName).append(" where ").append(fromColName).append('=').append(fromId);
	}

	void sql_deleteReferencesTo(final StringBuilder sb, final int id) {
		sb.append("delete from ").append(tableName).append(" where ").append(toColName).append('=').append(id);
	}

	void sql_createIndexOnFromColumn(final StringBuilder sb) {
		sb.append("create index ").append(getFromIxName()).append(" on ").append(tableName).append('(')
				.append(fromColName).append(')');
	}

	void sql_createIndexOnToColumn(final StringBuilder sb) {
		sb.append("create index ").append(getToIxName()).append(" on ").append(tableName).append('(').append(toColName)
				.append(')');
	}

	String getFromIxName() {
		return fromColName;
	}

	String getToIxName() {
		return toColName;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(this.tableName);
		return super.toString();
	}

	static ResultSet getAllRefsTables(DatabaseMetaData dbm) throws Throwable {
		return dbm.getTables(null, null, TABLE_NAME_PREFIX + "%", new String[] { "TABLE" });
	}

	private static String TABLE_NAME_PREFIX = "Refs_";
}
