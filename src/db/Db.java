package db;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

/** The database. */
public final class Db {
	private static final ThreadLocal<DbTransaction> tn = new ThreadLocal<DbTransaction>();

	/** Enables the log(...) method. */
	public static boolean enable_log = true;

	/** Prints the string to System.out */
	public static void log(String s) {
		if (!enable_log)
			return;
		System.out.println(s);
	}

	private static Db inst;

	public static long pooled_connection_max_age_in_ms = 10 * 60 * 1000;
//	public static long pooled_connection_max_age_in_ms = 10 * 1000;

	/** Called once to create the singleton instance. */
	public static void initInstance() throws Throwable {
		Db.log("dbo: init instance");
		inst = new Db();
		inst.register(DbObject.class);
	}

	/** @return instance created at initInstance(). */
	public static Db instance() {
		return inst;
	}

	/**
	 * Initiates thread local for currentTransaction(). The transaction can be
	 * retrieved anywhere in the thread using Db.currentTransaction().
	 * 
	 * @return the created transaction
	 */
	public static DbTransaction initCurrentTransaction() { // ? so ugly
		Db.log("dbo: init transaction on " + Thread.currentThread());
		PooledConnection pc;
		synchronized (inst.conpool) {
			while (inst.conpool.isEmpty()) { // spurious interrupt might happen
				try {
					inst.conpool.wait();
				} catch (InterruptedException e) {
					log(e);
				}
			}
			pc = inst.conpool.removeFirst();
		}
		if (pc.getAgeInMs() > Db.pooled_connection_max_age_in_ms) {
			try {
				pc.getConnection().close();
			} catch (Throwable t) {
				log(t);
			}
			while (true) {
				try {
					final Connection c = Db.instance().createJdbcConnection();
					final PooledConnection pc2 = new PooledConnection(c);
					final DbTransaction t = new DbTransaction(pc2);
					tn.set(t);
					return t;
				} catch (Throwable e) {
					log(e);
				}
			}
		}
		while (true) {
			try {
				final DbTransaction t = new DbTransaction(pc);
				tn.set(t);
				return t;
			} catch (Throwable e) {
				log(e);
			}
			// something went wrong initiating transaction
			try {
				final Connection c = Db.instance().createJdbcConnection();
				final PooledConnection pc2 = new PooledConnection(c);
				final DbTransaction t = new DbTransaction(pc2);
				tn.set(t);
				return t;
			} catch (Throwable e) {
				log(e);
			}
		}
	}

	/**
	 * @return current transaction that was initiated in the thread local by
	 *         initCurrentTransaction().
	 */
	public static DbTransaction currentTransaction() {
//		Db.log("dbo: get current transaction on " + Thread.currentThread());
		return tn.get();
	}

	/**
	 * Removes the transaction from the thread local and the JDBC connection is
	 * returned to the pool.
	 */
	public static void deinitCurrentTransaction() { // ? so ugly
		Db.log("dbo: deinit transaction on " + Thread.currentThread());
		final DbTransaction tx = tn.get();
		boolean keep = true;
		if (!tx.rollbacked) {
			try {
				tx.commit();
			} catch (Throwable t) {
				keep = false;
				log(t);
			}
		}
		try {
			tx.stmt.close();
		} catch (Throwable t) {
			keep = false;
			log(t);
		}

		// make sure statement is closed here. should be. // ? stmt.isClosed() is not in
		// java 1.5
//		final boolean stmtIsClosed;
//		try {
//			stmtIsClosed = t.stmt.isClosed();
//		} catch (Throwable e) {
//			throw new RuntimeException(e);
//		}
//		if (!stmtIsClosed)
//			throw new RuntimeException(
//					"Statement should be closed here. DbTransaction.finishTransaction() not called?");
		if (keep) {
			synchronized (inst.conpool) {
				inst.conpool.addFirst(tx.pooledCon);
				inst.conpool.notify();
			}
			tn.remove();
			return;
		}
		// this connection threw exception while deinit. make a new one
		final Connection c = Db.instance().createJdbcConnection();
		final PooledConnection pc = new PooledConnection(c);
		synchronized (inst.conpool) {
			inst.conpool.addFirst(pc);
			inst.conpool.notify();
		}
		tn.remove();
	}

	public static synchronized void log(Throwable t) {
		while (t.getCause() != null)
			t = t.getCause();
		System.err.println(stacktraceline(t));
	}

	public static String stacktrace(final Throwable e) {
		final StringWriter sw = new StringWriter();
		final PrintWriter out = new PrintWriter(sw);
		e.printStackTrace(out);
		out.close();
		return sw.toString();
	}

	public static String stacktraceline(final Throwable e) {
		return stacktrace(e).replace('\n', ' ').replace('\r', ' ').replaceAll("\\s+", " ").replaceAll(" at ", " @ ");
	}

	// --- -- - -- -- - - --- - -- -- - -- --- - - - -- - - -- - -- -- -- - -- - - -
	private final LinkedList<PooledConnection> conpool = new LinkedList<PooledConnection>();
	private final ArrayList<DbClass> dbclasses = new ArrayList<DbClass>();
	private final HashMap<Class<? extends DbObject>, DbClass> clsToDbClsMap = new HashMap<Class<? extends DbObject>, DbClass>();
	final ArrayList<RelRefNMeta> relRefNMeta = new ArrayList<RelRefNMeta>();

	/** If true undeclared columns are deleted. */
	public boolean enable_delete_unused_columns = true;

	/** If true undeclared indexes are deleted. */
	public boolean enable_drop_undeclared_indexes = true;

	/**
	 * Object delete triggers the updating of referring columns to null. Racing
	 * conditions may occur.
	 */
	public boolean enable_update_referring_tables = true;

	/**
	 * Objects retrieved from the database are cached. This ensures that get(...)
	 * returns the same instance of a previously retrieved object.
	 */
	public boolean enable_cache = true;

	private String jdbcUrl;
	private String jdbcUser;
	private String jdbcPasswd;

	/** Registers DbObject class to be persisted. */
	public void register(final Class<? extends DbObject> cls) throws Throwable {
		final DbClass dbcls = new DbClass(cls);
		dbclasses.add(dbcls);
		clsToDbClsMap.put(cls, dbcls);
	}

	/**
	 * Initiates the registered classes and connects to the database.
	 * 
	 * @param url      JDBC URL to the database
	 * @param user
	 * @param password
	 * @param ncons    number of connections in the pool
	 */
	public void init(final String url, final String user, final String password, final int ncons) throws Throwable {
		jdbcUrl = url;
		jdbcUser = user;
		jdbcPasswd = password;
		Db.log("--- - - - ---- - - - - - -- -- --- -- --- ---- -- -- - - -");
		Db.log("connection: " + url);
		Db.log("      user: " + user);
		Db.log("  password: " + (password == null ? "[none]" : "[not displayed]"));
		final Connection con = createJdbcConnection();

		final DatabaseMetaData dbm = con.getMetaData();
		Db.log("    driver: " + dbm.getDriverName() + " " + dbm.getDriverVersion());
		Db.log("    server: " + dbm.getDatabaseProductName() + " " + dbm.getDatabaseProductVersion());
		Db.log("--- - - - ---- - - - - - -- -- --- -- --- ---- -- -- - - -");

		// recursively populate lists: allFields, allRelations, allIndexes
		for (final DbClass c : dbclasses) {
			c.init();
		}

		// allow DbClass relations to add necessary fields and indexes, even to other
		// DbClasses
		for (final DbClass c : dbclasses) {
			for (final DbRelation r : c.allRelations)
				r.init(c);
		}

		// allow indexes to initiate using fully initiated relations.
		for (final DbClass c : dbclasses) {
			for (final Index ix : c.allIndexes)
				ix.init(c);
		}

		// print summary
		for (final DbClass c : dbclasses) {
			Db.log(c.toString());
		}

		// DbClasses, fields and indexes are now ready for create/modify

		Db.log("--- - - - ---- - - - - - -- -- --- -- --- ---- -- -- - - -");

		ensureTablesAndIndexes(con, dbm);

		Db.log("--- - - - ---- - - - - - -- -- --- -- --- ---- -- -- - - -");

		printDbMetaInfo(dbm);

		con.close();

		// create connection pool
		for (int i = 0; i < ncons; i++) {
			final Connection c = createJdbcConnection();
			final PooledConnection pc = new PooledConnection(c);
			conpool.add(pc);
		}

		Db.log("--- - - - ---- - - - - - -- -- --- -- --- ---- -- -- - - -");
	}

	/** @return a JDBC connection. */
	public Connection createJdbcConnection() {
		Connection c = null;
		while (true) {
			try {
				c = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPasswd);
				c.setAutoCommit(false);
				return c;
			} catch (Throwable t) {
				try {
					System.err.println("dbo: cannot create connection waiting.");
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					log(e);
				}
			}
		}
	}

	private void printDbMetaInfo(final DatabaseMetaData dbm) throws SQLException {
		// output tables, columns, indexes
		final ResultSet rstbls = dbm.getTables(null, null, null, new String[] { "TABLE" });
		while (rstbls.next()) {
			final String tblname = rstbls.getString("TABLE_NAME");
			Db.log("[" + tblname + "]");
			ResultSet rscols = dbm.getColumns(null, null, tblname, null);
			while (rscols.next()) {
				String columnName = rscols.getString("COLUMN_NAME");
				String datatype = rscols.getString("TYPE_NAME");
				String defval = rscols.getString("COLUMN_DEF");
				final StringBuilder sb = new StringBuilder();
				sb.append("    ").append(columnName).append(' ').append(datatype);
				if (defval != null) {
					sb.append(" '").append(defval).append('\'');
				}
				Db.log(sb.toString());
			}
			rscols.close();

			final ResultSet rsix = dbm.getIndexInfo(null, null, tblname, false, false);
			while (rsix.next()) {
				Db.log("  index " + rsix.getString("INDEX_NAME") + " on " + rsix.getString("COLUMN_NAME"));
			}
			rsix.close();
			Db.log("");
		}
		rstbls.close();
	}

	private void ensureTablesAndIndexes(final Connection con, final DatabaseMetaData dbm) throws Throwable {
		// ensure RefN tables exist and match to definition
		final Statement stmt = con.createStatement();

		for (final DbClass dbcls : dbclasses) {
			if (Modifier.isAbstract(dbcls.javaClass.getModifiers()))
				continue;
			dbcls.ensureTable(stmt, dbm);
		}

		// ensure RefN tables exist and match to definition
		for (final RelRefNMeta rrm : relRefNMeta) {
			rrm.ensureTable(stmt, dbm);
		}

		// all tables exist

		// relations might need to create index directly with Statement or add to
		// allIndexes to other classes
		for (final DbClass dbcls : dbclasses) {
			for (final DbRelation dbrel : dbcls.allRelations) {
				dbrel.ensureIndexes(stmt, dbm);
			}
		}

		// ensure indexes exist and match definition
		for (final DbClass dbcls : dbclasses) {
			for (final Index ix : dbcls.allIndexes) {
				ix.ensureIndex(stmt, dbm);
			}
		}

		if (enable_drop_undeclared_indexes) {
			// drop undeclared indexes
			for (final DbClass dbcls : dbclasses) {
				dbcls.dropUndeclaredIndexes(stmt, dbm);
			}

			// drop unused RefN tables
			final ArrayList<String> refsTbls = new ArrayList<String>();
			final ResultSet rstbls = RelRefNMeta.getAllRefsTables(dbm);
			while (rstbls.next()) {
				final String tbl = rstbls.getString("TABLE_NAME");
				refsTbls.add(tbl);
			}

			for (final RelRefNMeta rrm : relRefNMeta) {
				refsTbls.remove(rrm.tableName);
			}

			for (final String s : refsTbls) {
				final StringBuilder sb = new StringBuilder(128);
				sb.append("drop table ").append(s);
				final String sql = sb.toString();
				Db.log(sql);
				stmt.execute(sql);
			}
		}

		stmt.close();
	}

	/**
	 * Deletes and recreates all tables and indexes. Used by testing framework.
	 */
	public void reset() {
		Db.log("*** reseting database");
		Connection con = null;
		try {
			con = createJdbcConnection();
			final Statement stmt = con.createStatement();
			final StringBuilder sb = new StringBuilder();
			for (final DbClass dbc : dbclasses) {
				if (Modifier.isAbstract(dbc.javaClass.getModifiers()))
					continue;
				sb.setLength(0);
				sb.append("drop table ").append(dbc.tableName);
				final String sql = sb.toString();
				Db.log(sql);
				stmt.execute(sql);
			}
			for (final RelRefNMeta rnm : relRefNMeta) {
				sb.setLength(0);
				sb.append("drop table ").append(rnm.tableName);
				final String sql = sb.toString();
				Db.log(sql);
				stmt.execute(sql);
			}
			stmt.close();

			ensureTablesAndIndexes(con, con.getMetaData());
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			try {
				if (con != null)
					con.close();
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}

	/** Sets singleton instance to null and closes the connections in the pool. */
	public void shutdown() {
		Db.log("dbo: shutdown");
		Db.inst = null;
		synchronized (conpool) {
			for (final PooledConnection pc : conpool) {
				try {
					pc.getConnection().close();
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
			conpool.clear();
		}
	}

	static String tableNameForJavaClass(final Class<? extends DbObject> cls) {
		final String tblnm = cls.getName().substring(cls.getName().lastIndexOf('.') + 1);// ? package name
//		final String tblnm = cls.getName().replace('.', '_');
//		final String tblnm = cls.getName();
		return tblnm;
	}

	DbClass dbClassForJavaClass(final Class<?> c) {
		return clsToDbClsMap.get(c);
	}

//	public List<DbClass> getDbClasses() {
//		return dbclasses;
//	}

	/** @return the {@link DbClass} for the Java class */
	public DbClass getDbClassForJavaClass(final Class<? extends DbObject> cls) {
		return clsToDbClsMap.get(cls);
	}
}
