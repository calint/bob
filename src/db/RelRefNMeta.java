// reviewed: 2024-08-05
//           2025-04-28
package db;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;

/** Represents the relation table for RelRefN. */
final class RelRefNMeta {

    public static final String TABLE_NAME_PREFIX = "Refs_";
    public static final String COLUMN_NAME_FROM = "fromId"; // column name referencing to source id
    public static final String COLUMN_NAME_TO = "toId"; // column name referencing to target id

    final String tableName; // the table name for this association NN table
    final String fromTableName; // the table name of the source class
    final String toTableName; // table name of target class

    RelRefNMeta(final Class<? extends DbObject> fromCls, final String relName, final Class<? extends DbObject> toCls) {
        fromTableName = Db.tableNameForJavaClass(fromCls);
        toTableName = Db.tableNameForJavaClass(toCls);
        tableName = new StringBuilder(256).append(TABLE_NAME_PREFIX).append(fromTableName).append('_').append(relName)
                .toString(); // ? magic number
    }

    void ensureTable(final Statement stmt, final DatabaseMetaData dbm) throws Throwable {
        final ResultSet rs = dbm.getTables(null, null, tableName, new String[] { "TABLE" });
        if (rs.next()) {
            rs.close();
            // ? check columns
            return;
        }
        rs.close();

        // note: primary key added due to warning from mysql regarding replication
        // performance in cluster
        // ? ADD CONSTRAINT PK PRIMARY KEY (fromId,toId);
        final StringBuilder sb = new StringBuilder(256);
        sb.append("create table ").append(tableName).append("(id int primary key auto_increment").append(',')
                .append(COLUMN_NAME_FROM).append(" int,").append(COLUMN_NAME_TO).append(" int)");
        if (Db.engine != null) {
            sb.append("engine=").append(Db.engine);
        }
        final String sql = sb.toString();
        Db.logSql(sql);
        stmt.execute(sql);
    }

    void appendSqlAddToTable(final StringBuilder sb, final int fromId, final int toId) {
        sb.append("insert into ").append(tableName).append(" values(null,").append(fromId).append(',').append(toId)
                .append(')');
    }

    void appendSqlDeleteFromTable(final StringBuilder sb, final int fromId, final int toId) {
        sb.append("delete from ").append(tableName).append(" where ").append(COLUMN_NAME_FROM).append('=')
                .append(fromId).append(" and ").append(COLUMN_NAME_TO).append('=').append(toId);

    }

    void appendSqlDeleteFromTable(final StringBuilder sb, final int fromId) {
        sb.append("delete from ").append(tableName).append(" where ").append(COLUMN_NAME_FROM).append('=')
                .append(fromId);
    }

    void appendSqlDeleteAllFromTable(final StringBuilder sb, final int fromId) {
        sb.append("delete from ").append(tableName).append(" where ").append(COLUMN_NAME_FROM).append('=')
                .append(fromId);
    }

    void appendSqlDeleteReferencesTo(final StringBuilder sb, final int id) {
        sb.append("delete from ").append(tableName).append(" where ").append(COLUMN_NAME_TO).append('=').append(id);
    }

    void appendSqlCreateIndexOnFromColumn(final StringBuilder sb) {
        sb.append("create index ").append(getFromIxName()).append(" on ").append(tableName).append('(')
                .append(COLUMN_NAME_FROM).append(')');
    }

    void appendSqlCreateIndexOnToColumn(final StringBuilder sb) {
        sb.append("create index ").append(getToIxName()).append(" on ").append(tableName).append('(')
                .append(COLUMN_NAME_TO).append(')');
    }

    String getFromIxName() {
        return COLUMN_NAME_FROM;
    }

    String getToIxName() {
        return COLUMN_NAME_TO;
    }

    @Override
    public String toString() {
        return tableName;
    }

    static ResultSet getAllRefsTables(final DatabaseMetaData dbm) throws Throwable {
        return dbm.getTables(null, null, TABLE_NAME_PREFIX + "%", new String[] { "TABLE" });
    }

}
