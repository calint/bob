//
// reviewed: 2024-08-05
//           2025-04-28
//
package db;

import java.util.List;

/** Aggregation one-to-many. */
public final class RelAggN extends DbRelation {

    public RelAggN(final Class<? extends DbObject> toCls) {
        super(toCls);
    }

    @Override
    protected void init(final DbClass dbCls) {
        relFld = new FldRel();
        relFld.cls = toCls;
        relFld.name = dbCls.tableName + "_" + name;
        final DbClass toDbCls = Db.dbClassForJavaClass(toCls);
        relFld.tableName = toDbCls.tableName;
        toDbCls.allFields.add(relFld);

        // add an index to target class
        final Index ix = new Index(relFld);
        ix.cls = toCls;
        ix.name = relFld.name;
        ix.tableName = relFld.tableName;
        toDbCls.allIndexes.add(ix);
    }

    /** @param thsId source object id. */
    public DbObject create(final int thsId) {
        final DbObject o = Db.currentTransaction().create(toCls);
        relFld.setId(o, thsId);
        return o;
    }

    public DbObject create(final DbObject ths) {
        return create(ths.id());
    }

    /** @param thsId source object id. */
    public DbObjects get(final int thsId) {
        final Query q = new Query(cls, thsId).and(this);
        return new DbObjects(null, toCls, q, null);
    }

    public DbObjects get(final DbObject ths) {
        return get(ths.id());
    }

    public void delete(final DbObject ths, final int toId) {
        delete(ths.id(), toId);
    }

    /** @param thsId source object id. */
    public void delete(final int thsId, final int toId) {
        final DbTransaction tn = Db.currentTransaction();
        final DbClass dbClsTo = Db.dbClassForJavaClass(toCls);
        if (dbClsTo.cascadeDelete) {
            final DbObject o = tn.get(toCls, toId);
            delete(thsId, o);
            return;
        }

        // target class does not need to cascade delete thus just delete them from
        // target table
        tn.flush();
        tn.removeReferencesToObject(dbClsTo, toId);

        final StringBuilder sb = new StringBuilder(128); // ? magic number
        sb.append("delete from ").append(dbClsTo.tableName).append(" where ").append(DbObject.id.name).append("=")
                .append(toId).append(" and ").append(relFld.name).append("=").append(thsId);

        if (!Db.clusterOn) {
            tn.execSql(sb.toString());
        } else {
            Db.execClusterSql(sb.toString());
        }

        if (tn.cacheEnabled) {
            tn.cache.remove(toCls, toId);
        }
    }

    public void delete(final DbObject ths, final DbObject o) {
        delete(ths.id(), o);
    }

    /** @param thsId source object id. */
    public void delete(final int thsId, final DbObject o) {
        if (relFld.getId(o) != thsId) {
            throw new RuntimeException(cls.getName() + "[" + thsId + "] does not contain " + toCls.getName() + "["
                    + o.id() + "] in relation '" + name + "'");
        }
        Db.currentTransaction().delete(o);
    }

    public void deleteAll(final DbObject ths) {
        deleteAll(ths.id());
    }

    /** @param thsId source object id. */
    public void deleteAll(final int thsId) {
        cascadeDelete(thsId);
    }

    @Override
    protected void cascadeDelete(final DbObject ths) {
        cascadeDelete(ths.id());
    }

    private void cascadeDelete(final int thsId) {
        final DbTransaction tn = Db.currentTransaction();
        final DbClass dbClsTo = Db.dbClassForJavaClass(toCls);

        // if target class needs to cascade deletes or it contains RelRefN (associated
        // table entries) or database setting is to set null on tables that refer to
        // this instance (dangling references) and it has tables referring to this
        if (dbClsTo.cascadeDelete || !dbClsTo.referringRefN.isEmpty()
                || (Db.enableUpdateReferringTables && !dbClsTo.referringRef.isEmpty())) {
            final List<DbObject> ls = get(thsId).toList();
            for (final DbObject o : ls) {
                tn.delete(o);
            }
            return;
        }

        // target class does not need to cascade delete thus just delete them from
        // target table
        tn.flush();

        final StringBuilder sb = new StringBuilder(128);
        sb.append("delete from ").append(dbClsTo.tableName).append(" where ").append(relFld.name).append("=")
                .append(thsId);

        if (!Db.clusterOn) {
            tn.execSql(sb.toString());
        } else {
            Db.execClusterSql(sb.toString());
        }

        // note: objects are potentially in the cache but are not removed because those
        // objects will not be accessed.
    }

}
