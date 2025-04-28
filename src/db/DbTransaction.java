// reviewed: 2024-08-05
//           2025-04-28
package db;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/** Transaction used to create, get, count and delete objects. */
public final class DbTransaction {

    final PooledConnection pooledCon; // ? circular reference ok?
    final Connection con;
    final Statement stmt;
    final HashSet<DbObject> dirtyObjects = new HashSet<DbObject>();
    public boolean cacheEnabled = true;
    final Cache cache = new Cache();
    boolean isRolledBack = false;

    final static class Cache {
        final HashMap<Class<? extends DbObject>, HashMap<Integer, DbObject>> clsToIdObjMap = new HashMap<Class<? extends DbObject>, HashMap<Integer, DbObject>>();

        void put(final DbObject o) {
            HashMap<Integer, DbObject> idToObjMap = clsToIdObjMap.get(o.getClass());
            if (idToObjMap == null) {
                idToObjMap = new HashMap<Integer, DbObject>();
                clsToIdObjMap.put(o.getClass(), idToObjMap);
            }
            idToObjMap.put(o.id(), o);
        }

        DbObject get(final Class<?> cls, final int id) {
            final HashMap<Integer, DbObject> idToObjMap = clsToIdObjMap.get(cls);
            if (idToObjMap == null) {
                return null;
            }
            return idToObjMap.get(id);
        }

        void remove(final DbObject o) {
            remove(o.getClass(), o.id());
        }

        void remove(final Class<? extends DbObject> cls, final int id) {
            final HashMap<Integer, DbObject> idToObjMap = clsToIdObjMap.get(cls);
            if (idToObjMap == null) {
                // ? is this an exception?
                return;
            }
            idToObjMap.remove(id);
        }

        void clear() {
            clsToIdObjMap.clear();
        }
    }

    DbTransaction(final PooledConnection pc) throws Throwable {
        pooledCon = pc;
        con = pc.getConnection();
        stmt = con.createStatement();
        cacheEnabled = Db.enableCache;
    }

    public Statement getJdbcStatement() {
        return stmt;
    }

    public Connection getJdbcConnetion() {
        return con;
    }

    /** Creates a DbObject */
    public DbObject create(final Class<? extends DbObject> cls) {
        try {
            final DbObject obj = cls.getConstructor().newInstance();
            final DbClass dbCls = Db.dbClassForJavaClass(cls);
            obj.fieldValues = new Object[dbCls.allFields.size()];

            // set default values
            for (final DbField f : dbCls.allFields) {
                obj.fieldValues[f.slotNbr] = f.getDefaultValue();
            }

            final StringBuilder sb = new StringBuilder(256);
            sb.append("insert into ").append(Db.tableNameForJavaClass(cls)).append(" values()");

            final String sql = sb.toString();
            if (!Db.clusterOn) {
                Db.logSql(sql);
                stmt.execute(sql, Statement.RETURN_GENERATED_KEYS);
                final ResultSet rs = stmt.getGeneratedKeys();
                if (!rs.next()) {
                    throw new RuntimeException("expected generated id");
                }
                final int id = rs.getInt(1);
                obj.fieldValues[DbObject.id.slotNbr] = id;
                rs.close();
            } else {
                final int id = Db.execClusterSqlInsert(sql);
                obj.fieldValues[DbObject.id.slotNbr] = id;
            }

            if (cacheEnabled) {
                cache.put(obj);
            }

            obj.onCreate();

            return obj;
        } catch (final Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public void delete(final DbObject o) {
        flush();

        final DbClass dbCls = Db.dbClassForJavaClass(o.getClass());
        if (dbCls.cascadeDelete) {
            for (final DbRelation r : dbCls.allRelations) {
                if (r.cascadeDeleteNeeded()) {
                    r.cascadeDelete(o);
                }
            }
        }

        final int id = o.id();

        removeReferencesToObject(dbCls, id);

        // delete this
        final StringBuilder sb = new StringBuilder(256);
        sb.append("delete from ").append(dbCls.tableName).append(" where id=").append(id);
        if (!Db.clusterOn) {
            execSql(sb.toString());
        } else {
            Db.execClusterSql(sb.toString());
        }

        dirtyObjects.remove(o);
        if (cacheEnabled) {
            cache.remove(o);
        }
    }

    void removeReferencesToObject(final DbClass dbCls, final int id) {
        // delete orphans
        for (final RelRefN r : dbCls.referringRefN) {
            r.deleteReferencesTo(id);
        }

        // update referring fields to null
        if (Db.enableUpdateReferringTables) {
            for (final RelRef r : dbCls.referringRef) {
                final StringBuilder sb = new StringBuilder(256);
                sb.append("update ").append(r.tableName).append(" set ").append(r.name).append("=null")
                        .append(" where ").append(r.name).append('=').append(id);
                if (!Db.clusterOn) {
                    execSql(sb.toString());
                } else {
                    Db.execClusterSql(sb.toString());
                }
            }
        }
    }

    /**
     * Get object by id.
     * 
     * @return The object or null if not found.
     */
    public DbObject get(final Class<? extends DbObject> cls, final int id) {
        final List<DbObject> ls = get(cls, new Query(cls, id), null, null);
        if (ls.isEmpty()) {
            return null;
        }
        return ls.get(0);
    }

    /**
     * Get object by id using string. Convenience method that parses the string to
     * integer.
     *
     * @return Null if id is null or object of type cls with id parsed to integer.
     */
    public DbObject get(final Class<? extends DbObject> cls, final String id) {
        if (id == null) {
            return null;
        }
        return get(cls, Integer.parseInt(id));
    }

    public List<DbObject> get(final Class<?> cls, final Query qry, final Order ord, final Limit lmt) {
        flush(); // update database before query

        final Query.TableAliasMap tam = new Query.TableAliasMap();
        final StringBuilder sbWhere = new StringBuilder(128);
        if (qry != null) {
            qry.appendSql(sbWhere, tam);
        }

        final DbClass dbCls = Db.dbClassForJavaClass(cls);
        final StringBuilder sb = new StringBuilder(256);
        sb.append("select ").append(tam.getAliasForTableName(dbCls.tableName)).append(".* from ");
        tam.appendSqlSelectFromTables(sb);

        if (sbWhere.length() != 0) {
            sb.append(" where ");
            sb.append(sbWhere);
        }

        if (ord != null && !ord.isEmpty()) {
            sb.append(" ");
            ord.appendSqlQuery(sb, tam);
        }

        if (lmt != null) {
            sb.append(" ");
            lmt.sql_appendToQuery(sb);
        }

        final ArrayList<DbObject> ls = new ArrayList<DbObject>(128); // ? magic number, use limit if available
        try {
            final Constructor<?> ctor = cls.getConstructor();
            final String sql = sb.toString();
            Db.logSql(sql);
            final ResultSet rs = stmt.executeQuery(sql);
            if (cacheEnabled) {
                while (rs.next()) {
                    final DbObject cachedObj = cache.get(cls, rs.getInt(1));
                    if (cachedObj != null) {
                        ls.add(cachedObj);
                        continue;
                    }
                    final DbObject o = (DbObject) ctor.newInstance();
                    o.fieldValues = new Object[dbCls.allFields.size()];
                    readResultSetToDbObject(o, dbCls, rs, 1);
                    // note: offset 1 because result set results start at 1
                    cache.put(o);
                    ls.add(o);
                }
            } else {
                while (rs.next()) {
                    final DbObject o = (DbObject) ctor.newInstance();
                    o.fieldValues = new Object[dbCls.allFields.size()];
                    readResultSetToDbObject(o, dbCls, rs, 1);
                    // note: offset 1 because result set results start at 1
                    ls.add(o);
                }
            }
        } catch (final Throwable t) {
            throw new RuntimeException(t);
        }
        return ls;
    }

    public List<DbObject[]> get(final Class<?>[] classes, final Query qry, final Order ord, final Limit lmt) {
        flush(); // update database before query

        final Query.TableAliasMap tam = new Query.TableAliasMap();
        final StringBuilder sbWhere = new StringBuilder(128);
        if (qry != null) {
            qry.appendSql(sbWhere, tam);
        }

        final int n = classes.length;
        if (n < 1) {
            throw new RuntimeException("classes array is empty");
        }
        final DbClass[] dbClasses = new DbClass[n];
        for (int i = 0; i < n; i++) {
            dbClasses[i] = Db.dbClassForJavaClass(classes[i]);
        }

        final StringBuilder sb = new StringBuilder(256);
        sb.append("select ");
        for (int i = 0; i < n; i++) {
            final DbClass c = dbClasses[i];
            sb.append(tam.getAliasForTableName(c.tableName));
            sb.append(".*,");
        }
        sb.setLength(sb.length() - 1); // remove the last ','
        sb.append(" from ");
        tam.appendSqlSelectFromTables(sb);

        if (sbWhere.length() != 0) {
            sb.append(" where ");
            sb.append(sbWhere);
        }

        if (ord != null && !ord.isEmpty()) {
            sb.append(" ");
            ord.appendSqlQuery(sb, tam);
        }

        if (lmt != null) {
            sb.append(" ");
            lmt.sql_appendToQuery(sb);
        }

        final ArrayList<DbObject[]> ls = new ArrayList<DbObject[]>(128); // ? magic number, use limit if available
        try {
            final Constructor<?>[] ctors = new Constructor<?>[n];
            for (int i = 0; i < n; i++) {
                ctors[i] = classes[i].getConstructor();
            }

            final int[] classFieldCount = new int[n];
            for (int i = 0; i < n; i++) {
                classFieldCount[i] = dbClasses[i].allFields.size();
            }

            final String sql = sb.toString();
            Db.logSql(sql);
            final ResultSet rs = stmt.executeQuery(sql);
            if (cacheEnabled) {
                while (rs.next()) {
                    final DbObject[] objs = new DbObject[n];
                    for (int i = 0, j = 1; i < n; i++) { // read the objects from the result using column offset j
                        final DbObject cachedObj = cache.get(classes[i], rs.getInt(j));
                        if (cachedObj != null) {
                            objs[i] = cachedObj;
                            j += classFieldCount[i]; // jump to next object in result set
                            continue;
                        }
                        final DbObject o = (DbObject) ctors[i].newInstance();
                        o.fieldValues = new Object[dbClasses[i].allFields.size()];
                        readResultSetToDbObject(o, dbClasses[i], rs, j);
                        objs[i] = o;
                        j += classFieldCount[i]; // jump to next object in result set
                        cache.put(o);
                    }
                    ls.add(objs);
                }
            } else {
                while (rs.next()) {
                    final DbObject[] objs = new DbObject[n];
                    for (int i = 0, j = 1; i < n; i++) {
                        final DbObject o = (DbObject) ctors[i].newInstance();
                        o.fieldValues = new Object[dbClasses[i].allFields.size()];
                        readResultSetToDbObject(o, dbClasses[i], rs, j);
                        objs[i] = o;
                        j += classFieldCount[i]; // jump to next object in result set
                    }
                    ls.add(objs);
                }
            }
        } catch (final Throwable t) {
            throw new RuntimeException(t);
        }
        return ls;
    }

    private void readResultSetToDbObject(final DbObject o, final DbClass cls, final ResultSet rs, final int offset)
            throws Throwable {
        int i = offset;
        for (final DbField f : cls.allFields) {
            final Object v = rs.getObject(i);
            o.fieldValues[f.slotNbr] = v;
            i++;
        }
    }

    public int getCount(final Class<? extends DbObject> cls, final Query qry) {
        flush(); // update database before query

        final StringBuilder sb = new StringBuilder(256);
        sb.append("select count(*) from ");

        final Query.TableAliasMap tam = new Query.TableAliasMap();

        if (qry != null) {
            final StringBuilder sbWhere = new StringBuilder(128);
            qry.appendSql(sbWhere, tam); // build first for tam to know which tables to include
            final StringBuilder sbFrom = new StringBuilder(128);
            tam.appendSqlSelectFromTables(sbFrom);
            if (sbFrom.length() == 0) {
                // the query might have been empty. append table of class
                final DbClass dbCls = Db.dbClassForJavaClass(cls);
                sb.append(dbCls.tableName);
            } else {
                sb.append(sbFrom);
            }
            if (sbWhere.length() != 0) {
                sb.append(" where ");
                sb.append(sbWhere);
            }
        } else {
            final DbClass dbCls = Db.dbClassForJavaClass(cls);
            sb.append(dbCls.tableName);
        }

        final String sql = sb.toString();
        Db.logSql(sql);
        try {
            final ResultSet rs = stmt.executeQuery(sql);
            if (!rs.next()) {
                throw new RuntimeException("expected result from " + sql);
            }
            return rs.getInt(1);
        } catch (final Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /** Writes changed objects to database, clears cache, commits. */
    public void commit() throws Throwable {
        flush();
        if (cacheEnabled) { // will keep memory usage down at batch imports
            cache.clear();
        }
        if (Db.clusterOn || Db.autocommit) {
            return;
        }
        con.commit();
    }

    public void rollback() {
        isRolledBack = true;
        if (cacheEnabled) {
            cache.clear();
        }
        if (Db.clusterOn || Db.autocommit) {
            return;
        }
        try {
            con.rollback();
        } catch (final Throwable t) {
            throw new RuntimeException(t);// ? this can be ignored?
        }
    }

    /** Writes changed objects to database. */
    public void flush() {
        if (dirtyObjects.isEmpty()) {
            return;
        }
        try {
            for (final DbObject o : dirtyObjects) {
                updateDbFromDbObject(o);
            }
        } catch (final Throwable t) {
            throw new RuntimeException(t);
        }

        dirtyObjects.clear();
    }

    private void updateDbFromDbObject(final DbObject o) throws Throwable {
        final StringBuilder sb = new StringBuilder(256);
        sb.append("update ").append(Db.tableNameForJavaClass(o.getClass())).append(" set ");
        for (final DbField f : o.dirtyFields) {
            sb.append(f.name).append('=');
            f.appendSqlUpdateValue(sb, o);
            sb.append(',');
        }
        sb.setLength(sb.length() - 1); // remove last ','
        sb.append(" where id=").append(o.id());
        if (!Db.clusterOn) {
            execSql(sb.toString());
        } else {
            Db.execClusterSql(sb.toString());
        }
        o.dirtyFields.clear();
    }

    void execSql(final String sql) {
        Db.logSql(sql);
        try {
            stmt.execute(sql);
        } catch (final Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Override
    public String toString() {
        return "dirtyObjects=" + dirtyObjects;
    }

}
