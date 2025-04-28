//
// reviewed: 2024-08-05
//           2025-04-28
//
package db;

/** Aggregation one-to-one. */
public final class RelAgg extends DbRelation {

    public RelAgg(final Class<? extends DbObject> toCls) {
        super(toCls);
    }

    @Override
    protected void init(final DbClass dbCls) {
        relFld = new FldRel();
        relFld.cls = cls; // to class
        final DbClass dbc = Db.getDbClassForJavaClass(cls);
        relFld.tableName = dbc.tableName;
        relFld.name = name;
        dbCls.allFields.add(relFld);
    }

    /** @returns 0 if id is null */
    public int getId(final DbObject ths) {
        final Object objId = ths.fieldValues[relFld.slotNbr];
        if (objId == null) {
            return 0;
        }
        return (Integer) objId;
    }

    public DbObject get(final DbObject ths, final boolean createIfNone) {
        final DbTransaction tn = Db.currentTransaction();
        final int id = getId(ths);
        if (id == 0) {
            if (createIfNone) {
                final DbObject o = tn.create(toCls);
                relFld.setId(ths, o.id());
                return o;
            }
            return null;
        }
        return tn.get(toCls, id);
    }

    public void delete(final DbObject ths) {
        cascadeDelete(ths);
        relFld.setId(ths, 0);
    }

    @Override
    protected void cascadeDelete(final DbObject ths) {
        final int toId = getId(ths);
        if (toId == 0) {
            return;
        }
        final DbTransaction tn = Db.currentTransaction();
        final DbClass dbClsTo = Db.dbClassForJavaClass(toCls);
        if (dbClsTo.cascadeDelete) {
            final DbObject o = get(ths, false);
            if (o != null) {
                // ? how to handle dangling ref
                tn.delete(o);
            }
            return;
        }

        // no cascade needed. just delete row in target table
        tn.flush();
        tn.removeReferencesToObject(dbClsTo, toId);

        final StringBuilder sb = new StringBuilder(128); // ? magic number
        sb.append("delete from ").append(dbClsTo.tableName).append(" where ").append(DbObject.id.name).append("=")
                .append(toId);
        if (!Db.clusterOn) {
            tn.execSql(sb.toString());
        } else {
            Db.execClusterSql(sb.toString());
        }

        if (tn.cacheEnabled) {
            tn.cache.remove(toCls, toId);
        }
    }

}
