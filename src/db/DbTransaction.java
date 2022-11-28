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
	final Connection con;
	final Statement stmt;
	final HashSet<DbObject> dirtyObjects = new HashSet<DbObject>();
	final Cache cache = new Cache();
	public boolean cache_enabled = true;

	static final class Cache {
		final HashMap<Class<? extends DbObject>, HashMap<Integer, DbObject>> clsToIdObjMap = new HashMap<Class<? extends DbObject>, HashMap<Integer, DbObject>>();

		void put(final DbObject o) {
			HashMap<Integer, DbObject> idToObjMap = clsToIdObjMap.get(o.getClass());
			if (idToObjMap == null) {
				idToObjMap = new HashMap<Integer, DbObject>();
				clsToIdObjMap.put(o.getClass(), idToObjMap);
			}
			idToObjMap.put(o.id(), o);
		}

		DbObject get(final Class<? extends DbObject> cls, final int id) {
			HashMap<Integer, DbObject> idToObjMap = clsToIdObjMap.get(cls);
			if (idToObjMap == null)
				return null;
			final DbObject o = idToObjMap.get(id);
			return o;
		}

		void remove(final DbObject o) {
			final HashMap<Integer, DbObject> idToObjMap = clsToIdObjMap.get(o.getClass());
			if (idToObjMap == null)
				return;
			idToObjMap.remove(o.id());
		}

		void clear() {
			clsToIdObjMap.clear();
		}
	}

	DbTransaction(final Connection c) throws Throwable {
		con = c;
		stmt = c.createStatement();
		cache_enabled = Db.instance().enable_cache;
	}

	public Statement getJdbcStatement() {
		return stmt;
	}

	public Connection getJdbcConnetion() {
		return con;
	}

	/** creates a DbObject */
	public DbObject create(final Class<? extends DbObject> cls) {
		try {
			final DbObject o = cls.getConstructor().newInstance();

			final StringBuilder sb = new StringBuilder(256);
			sb.append("insert into ").append(Db.tableNameForJavaClass(cls)).append(" values()");

			final String sql = sb.toString();
			Db.log(sql);
			stmt.execute(sql, Statement.RETURN_GENERATED_KEYS);
			final ResultSet rs = stmt.getGeneratedKeys();
			if (rs.next()) {
				final int id = rs.getInt(1);
				o.fieldValues.put(DbObject.id, id);
			} else
				throw new RuntimeException("expected generated id");
			rs.close();

			// init default values
			final DbClass dbcls = Db.instance().dbClassForJavaClass(cls);
			for (final DbField f : dbcls.allFields) {
				f.putDefaultValue(o.fieldValues);
			}

			if (cache_enabled)
				cache.put(o);

			return o;
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	public void delete(final DbObject o) {
		flush();
		
		final DbClass dbcls = Db.instance().dbClassForJavaClass(o.getClass());
		if (dbcls.cascadeDelete) {
			for (final DbRelation r : dbcls.allRelations) {
				if (r.cascadeDeleteNeeded()) {
					r.cascadeDelete(o);
				}
			}
		}

		final int id = o.id();

		// delete orphans
		for (final RelRefN r : dbcls.referingRefN) {
			r.deleteReferencesTo(id);
		}

		// update referring fields to null
		if (Db.instance().enable_update_referring_tables) {
			for (final RelRef r : dbcls.referingRef) {
				final StringBuilder sb = new StringBuilder(256);
				sb.append("update ").append(r.tableName).append(" set ").append(r.name).append("=null")
						.append(" where ").append(r.name).append('=').append(id);
				execSql(sb);
			}
		}

		// delete this
		final StringBuilder sb = new StringBuilder(256);
		sb.append("delete from ").append(dbcls.tableName).append(" where id=").append(id);
		execSql(sb);
		dirtyObjects.remove(o);
		if (cache_enabled)
			cache.remove(o);
	}

	public List<DbObject> get(final Class<? extends DbObject> cls, final Query qry, final Order ord, final Limit lmt) {
		flush(); // update database before query

		final Query.TableAliasMap tam = new Query.TableAliasMap();
		final StringBuilder sbwhere = new StringBuilder(128);
		if (qry != null) {
			sbwhere.append("where ");
			qry.sql_build(sbwhere, tam);
		}

		final DbClass dbcls = Db.instance().dbClassForJavaClass(cls);
		final StringBuilder sb = new StringBuilder(256);
		sb.append("select ").append(tam.getAliasForTableName(dbcls.tableName)).append(".* from ");
		tam.sql_appendSelectFromTables(sb);
		sb.append(" ");

		if (sbwhere.length() != 0)
			sb.append(sbwhere);

		if (ord != null)
			ord.sql_appendToQuery(sb, tam);

		if (lmt != null)
			lmt.sql_appendToQuery(sb);

		sb.setLength(sb.length() - 1);

		final ArrayList<DbObject> ls = new ArrayList<DbObject>(128); // ? magic number
		try {
			final Constructor<? extends DbObject> ctor = cls.getConstructor();
			final String sql = sb.toString();
			Db.log(sql);
			final ResultSet rs = stmt.executeQuery(sql);
			if (cache_enabled) {
				while (rs.next()) {
					final DbObject cachedObj = cache.get(cls, rs.getInt(1));// ? assumes id index
					if (cachedObj != null) {
						ls.add(cachedObj);
						continue;
					}
					final DbObject o = ctor.newInstance();
					readResultSetToDbObject(o, dbcls, rs);
					cache.put(o);
					ls.add(o);
				}
			} else {
				while (rs.next()) {
					final DbObject o = ctor.newInstance();
					readResultSetToDbObject(o, dbcls, rs);
					ls.add(o);
				}
			}
		} catch (final Throwable t) {
			throw new RuntimeException(t);
		}
		return ls;
	}

	private void readResultSetToDbObject(final DbObject o, final DbClass cls, final ResultSet rs) throws Throwable {
		int i = 1;
		for (final DbField f : cls.allFields) {
			final Object v = rs.getObject(i);
			o.fieldValues.put(f, v);
			i++;
		}
	}

	public int getCount(final Class<? extends DbObject> cls, final Query qry) {
		flush(); // update database before query

		final StringBuilder sb = new StringBuilder(256);
		sb.append("select count(*) from ");

		final Query.TableAliasMap tam = new Query.TableAliasMap();

		if (qry != null) {
			final StringBuilder sbwhere = new StringBuilder(128);
			qry.sql_build(sbwhere, tam);
			tam.sql_appendSelectFromTables(sb);
			sb.append(" where ");
			sb.append(sbwhere);
			sb.setLength(sb.length() - 1);
		} else {
			final DbClass dbcls = Db.instance().dbClassForJavaClass(cls);
			sb.append(dbcls.tableName);
		}

		final String sql = sb.toString();
		Db.log(sql);
		try {
			final ResultSet rs = stmt.executeQuery(sql);
			if (!rs.next())
				throw new RuntimeException("expected result from " + sql);
			return rs.getInt(1);
		} catch (final Throwable t) {
			throw new RuntimeException(t);
		}
	}

	/** writes changed objects to database, clears cache, commits */
	public void commit() throws Throwable {
		flush();
		if (cache_enabled) // will keep memory usage down at batch imports
			cache.clear();
		con.commit();
	}

	/** called when done with the transaction */
	public void finishTransaction() throws Throwable {
		commit();
		stmt.close();
	}

	/** writes changed objects to database */
	private void flush() { // ? public?
		if (dirtyObjects.isEmpty())
			return;
		Db.log("*** flushing " + dirtyObjects.size() + " objects");
		try {
			for (final DbObject o : dirtyObjects) {
				updateDbFromDbObject(o);
			}
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}

		dirtyObjects.clear();
		Db.log("*** done flushing");
	}

	private void updateDbFromDbObject(final DbObject o) throws Throwable {
		final StringBuilder sb = new StringBuilder(256);
		sb.append("update ").append(Db.tableNameForJavaClass(o.getClass())).append(" set ");
		for (final DbField f : o.dirtyFields) {
			sb.append(f.name).append('=');
			f.sql_updateValue(sb, o);
			sb.append(',');
		}
		sb.setLength(sb.length() - 1);
		sb.append(" where id=").append(o.id());
		execSql(sb);
		o.dirtyFields.clear();
	}

	public void rollback() {
		Db.log("*** rollback transaction");
		if (cache_enabled)
			cache.clear();
		try {
			con.rollback();
			stmt.close();
			Db.log("*** rollback done");
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	void execSql(final StringBuilder sb) {
		final String sql = sb.toString();
		Db.log(sql);
		try {
			stmt.execute(sql);
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	@Override
	public String toString() {
		return "dirtyObjects=" + dirtyObjects;
	}
}
