//
// reviewed: 2024-08-05
//           2025-04-28
//
package db;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;

/** Association one-to-many. */
public final class RelRefN extends DbRelation {

    RelRefNMeta rrm;

    public RelRefN(final Class<? extends DbObject> toCls) {
        super(toCls);
    }

    @Override
    protected void init(final DbClass c) {
        rrm = new RelRefNMeta(c.javaClass, name, toCls);
        Db.relRefNMeta.add(rrm);
        final DbClass toDbCls = Db.dbClassForJavaClass(toCls);
        if (toDbCls == null) {
            throw new RuntimeException(
                    "class " + toCls + " not found. Has Db.register(" + toCls.getName() + ".class) been called?");
        }
        toDbCls.referringRefN.add(this);
    }

    public void add(final DbObject ths, final DbObject o) {
        add(ths.id(), o.getId());
    }

    public void add(final DbObject ths, final int toId) {
        add(ths.id(), toId);
    }

    /** @param thsId source object id. */
    public void add(final int thsId, final int toId) {
        final StringBuilder sb = new StringBuilder(256); // ? magic number
        rrm.appendSqlAddToTable(sb, thsId, toId);
        if (!Db.clusterOn) {
            Db.currentTransaction().execSql(sb.toString());
        } else {
            Db.execClusterSqlInsert(sb.toString());
        }
    }

    /** @param thsId source object id. */
    public DbObjects get(final int thsId) {
        final Query q = new Query(cls, thsId).and(this);
        return new DbObjects(null, toCls, q, null);
    }

    public DbObjects get(final DbObject ths) {
        return get(ths.id());
    }

    public void remove(final DbObject ths, final DbObject o) {
        remove(ths.id(), o.id());
    }

    public void remove(final DbObject ths, final int toId) {
        remove(ths.id(), toId);
    }

    /** @param thsId source object id. */
    public void remove(final int thsId, final int toId) {
        final StringBuilder sb = new StringBuilder(256); // ? magic number
        rrm.appendSqlDeleteFromTable(sb, thsId, toId);
        if (!Db.clusterOn) {
            Db.currentTransaction().execSql(sb.toString());
        } else {
            Db.execClusterSql(sb.toString());
        }
    }

    public void removeAll(final DbObject ths) {
        removeAll(ths.id());
    }

    /** @param thsId source object id. */
    public void removeAll(final int thsId) {
        final StringBuilder sb = new StringBuilder(256); // ? magic number
        rrm.appendSqlDeleteAllFromTable(sb, thsId);
        if (!Db.clusterOn) {
            Db.currentTransaction().execSql(sb.toString());
        } else {
            Db.execClusterSql(sb.toString());
        }
    }

    void deleteReferencesTo(final int id) {
        final StringBuilder sb = new StringBuilder(256); // ? magic number
        rrm.appendSqlDeleteReferencesTo(sb, id);
        if (!Db.clusterOn) {
            Db.currentTransaction().execSql(sb.toString());
        } else {
            Db.execClusterSql(sb.toString());
        }
    }

    @Override
    protected void ensureIndexes(final Statement stmt, final DatabaseMetaData dbm) throws Throwable {
        final String fromIxName = rrm.getFromIxName();
        final String toIxName = rrm.getToIxName();

        final HashSet<String> lookingForIndexNames = new HashSet<String>();
        lookingForIndexNames.add(fromIxName);
        lookingForIndexNames.add(toIxName);

        final ResultSet rs = dbm.getIndexInfo(null, null, rrm.tableName, false, false);
        while (rs.next()) {
            final String indexName = rs.getString("INDEX_NAME");
            lookingForIndexNames.remove(indexName);
        }
        rs.close();

        if (lookingForIndexNames.isEmpty()) {
            return;
        }

        if (lookingForIndexNames.contains(fromIxName)) {
            final StringBuilder sb = new StringBuilder(128); // ? magic number
            rrm.appendSqlCreateIndexOnFromColumn(sb);
            final String sql = sb.toString();
            Db.logSql(sql);
            stmt.execute(sql);
        }

        if (lookingForIndexNames.contains(toIxName)) {
            final StringBuilder sb = new StringBuilder(128); // ? magic number
            rrm.appendSqlCreateIndexOnToColumn(sb);
            final String sql = sb.toString();
            Db.logSql(sql);
            stmt.execute(sql);
        }
    }

    @Override
    protected void cascadeDelete(final DbObject ths) {
        removeAll(ths.id());
    }

}
