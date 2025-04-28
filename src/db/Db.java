// reviewed: 2024-08-05
//           2025-04-28
package db;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Modifier;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/** The database. */
public final class Db {

    private static final ThreadLocal<DbTransaction> currentTransaction = new ThreadLocal<DbTransaction>();

    /** True if in cluster. */
    public static boolean clusterOn = false;

    /** Address to cluster writer. */
    public static String clusterIp = "127.0.0.1";

    public static int clusterPort = 8889;

    /** Default is null (InnoDB). */
    public static String engine = "myisam";

    /** Default is false. */
    public static boolean autocommit = true;

    /** Enables the log(...) method. */
    public static boolean enableLog = true;

    /** Enables the log_sql(...) method. */
    public static boolean enableLogSql = true;

    /** Prints the string to System.out. */
    public static void log(final String s) {
        if (!enableLog) {
            return;
        }
        System.out.println(s);
    }

    /** Prints the string to System.out. */
    public static void logSql(final String s) {
        if (!enableLogSql) {
            return;
        }
        System.out.println(s);
    }

    public static long pooledConnectionMaxAgeMs = 10 * 60 * 1000;

    /** Called once to create the singleton instance. */
    public static void init() throws Throwable {
        final Driver driver = (Driver) Class.forName("com.mysql.jdbc.Driver").getConstructor().newInstance();
        // note: for java 1.5 compatibility
        DriverManager.registerDriver(driver);
        Db.log("dbo: init instance");
        register(DbObject.class);
    }

    /**
     * Initiates thread local for currentTransaction(). The transaction can be
     * retrieved anywhere in the thread using Db.currentTransaction().
     *
     * @return The created transaction.
     */
    public static DbTransaction initCurrentTransaction() { // ? so ugly
        if (currentTransaction.get() != null) {
            throw new RuntimeException("transaction already initiated on this thread.");
        }
        PooledConnection pc;
        // get pooled connection
        synchronized (connectionPool) {
            while (connectionPool.isEmpty()) {
                // spurious interrupt might happen
                try {
                    connectionPool.wait();
                } catch (final InterruptedException e) {
                    log(e);
                }
            }
            pc = connectionPool.removeFirst();
        }
        if (pc.getAgeInMs() > Db.pooledConnectionMaxAgeMs) {
            // connection exceeded life time. create a new connection.
            try {
                pc.getConnection().close();
            } catch (final Throwable t) {
                log(t);
            }
            while (true) {
                try {
                    final Connection c = Db.createJdbcConnection();
                    final PooledConnection p = new PooledConnection(c);
                    final DbTransaction t = new DbTransaction(p);
                    currentTransaction.set(t);
                    return t;
                } catch (final Throwable e) {
                    log(e);
                }
            }
        }
        // create transaction using pooled connection
        while (true) {
            try {
                final DbTransaction t = new DbTransaction(pc);
                currentTransaction.set(t);
                return t;
            } catch (final Throwable e) {
                log(e);
            }
            // something went wrong initiating transaction
            // create a new connection
            try {
                final Connection c = createJdbcConnection();
                final PooledConnection p = new PooledConnection(c);
                final DbTransaction t = new DbTransaction(p);
                currentTransaction.set(t);
                return t;
            } catch (final Throwable e) {
                log(e);
            }
        }
    }

    /**
     * @return Current transaction that was initiated in the thread local by
     *         initCurrentTransaction().
     */
    public static DbTransaction currentTransaction() {
        // Db.log("dbo: get current transaction on " + Thread.currentThread());
        final DbTransaction t = currentTransaction.get();
        if (t == null) {
            throw new RuntimeException("No transaction in the thread. Has Db.initCurrentTransaction() been done?");
        }
        return t;
    }

    /**
     * Removes the transaction from the thread local and the JDBC connection is
     * returned to the pool.
     */
    public static void deinitCurrentTransaction() { // ? so ugly
        final DbTransaction tn = currentTransaction.get();
        if (tn == null) {
            throw new RuntimeException("Transaction not initiated on this thread.");
        }
        boolean connectionIsOk = true;
        if (!tn.isRolledBack) {
            try {
                tn.commit();
            } catch (final Throwable t) {
                connectionIsOk = false;
                log(t);
            }
        }
        currentTransaction.remove();
        if (connectionIsOk) {
            synchronized (connectionPool) {
                connectionPool.addFirst(tn.pooledCon);
                connectionPool.notify();
            }
            return;
        }
        // this connection threw exception while de-init. make a new one
        final Connection c = createJdbcConnection();
        final PooledConnection p = new PooledConnection(c);
        synchronized (connectionPool) {
            connectionPool.addFirst(p);
            connectionPool.notify();
        }
    }

    public static void log(Throwable t) {
        while (t.getCause() != null) {
            t = t.getCause();
        }
        System.err.println(stacktraceToLine(t));
    }

    public static String stacktrace(final Throwable e) {
        final StringWriter sw = new StringWriter();
        final PrintWriter out = new PrintWriter(sw);
        e.printStackTrace(out);
        out.close();
        return sw.toString();
    }

    public static String stacktraceToLine(final Throwable e) {
        return stacktrace(e).replace('\n', ' ').replace('\r', ' ').replaceAll("\\s+", " ").replace(" at ", " @ ");
    }

    // --- -- - -- -- - - --- - -- -- - -- --- - - - -- - - -- - -- -- -- - -- - - -
    private static final LinkedList<PooledConnection> connectionPool = new LinkedList<PooledConnection>();
    private static final ArrayList<DbClass> dbClasses = new ArrayList<DbClass>();
    private static final HashMap<Class<? extends DbObject>, DbClass> clsToDbClsMap = new HashMap<Class<? extends DbObject>, DbClass>();
    static final ArrayList<RelRefNMeta> relRefNMeta = new ArrayList<RelRefNMeta>();

    /** If true unused columns are deleted. */
    public static boolean enableDeleteUnusedColumns = true;

    /** If true unused indexes are deleted. */
    public static boolean enableDropUndeclaredIndexes = true;

    /**
     * Object delete triggers the updating of referring columns to null. Racing
     * conditions may occur.
     */
    public static boolean enableUpdateReferringTables = true;

    /**
     * Objects retrieved from the database are cached. This ensures that get(...)
     * returns the same instance of a previously retrieved object.
     */
    public static boolean enableCache = true;

    private static String jdbcConnectionString;

    /** Registers DbObject class to be persisted. */
    public static void register(final Class<? extends DbObject> cls) throws Throwable {
        final DbClass dc = new DbClass(cls);
        dbClasses.add(dc);
        clsToDbClsMap.put(cls, dc);
    }

    /**
     * Initiates the registered classes and connects to the database.
     *
     * @param host       Server host.
     * @param dbName     Database name.
     * @param user
     * @param password
     * @param ncons      Number of connections in the pool.
     * @param clusterIp  null if none.
     * @param clust_port 0 if none.
     */
    public static void start(final String host, final String dbName, final String user, final String password,
            final int ncons, final String clusterIp, final int clusterPort) throws Throwable {
        jdbcConnectionString = getJdbcConnectionString(host, dbName, user, password);
        Db.clusterIp = clusterIp;
        Db.clusterPort = clusterPort;

        Db.log("--- - - - ---- - - - - - -- -- --- -- --- ---- -- -- - - -");
        Db.log("   cluster: " + clusterOn);
        Db.log("connection: " + jdbcConnectionString);
        // Db.log(" user: " + user);
        // Db.log(" password: " + (password == null ? "[none]" : "[not displayed]"));
        final Connection con = createJdbcConnection();

        final DatabaseMetaData dbm = con.getMetaData();
        Db.log("    driver: " + dbm.getDriverName() + " " + dbm.getDriverVersion());
        Db.log("    server: " + dbm.getDatabaseProductName() + " " + dbm.getDatabaseProductVersion());
        Db.log("--- - - - ---- - - - - - -- -- --- -- --- ---- -- -- - - -");

        // recursively populate lists: allFields, allRelations, allIndexes
        for (final DbClass c : dbClasses) {
            c.init();
        }

        // allow DbClass relations to add necessary fields and indexes, even to other
        // DbClasses
        for (final DbClass c : dbClasses) {
            for (final DbRelation r : c.allRelations) {
                r.init(c);
            }
        }

        // initiate DbField indexes in the data list
        for (final DbClass c : dbClasses) {
            c.initDbFields();
        }

        // allow indexes to initiate using fully initiated relations
        for (final DbClass c : dbClasses) {
            for (final Index ix : c.allIndexes) {
                ix.init(c);
            }
        }

        // print summary
        for (final DbClass c : dbClasses) {
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
            connectionPool.add(pc);
        }

        Db.log("--- - - - ---- - - - - - -- -- --- -- --- ---- -- -- - - -");

        if (!clusterOn) {
            return;
        }

        while (true) {
            log("connecting to cluster " + clusterIp + ":" + clusterPort);
            try {
                clusterSocket = new Socket(clusterIp, clusterPort);
                clusterSocket.setTcpNoDelay(true);
                clusterSocketReader = new BufferedReader(new InputStreamReader(clusterSocket.getInputStream()));
                clusterSocketOs = new BufferedOutputStream(clusterSocket.getOutputStream(), 1024 * 1);
                log("connected. waiting for cluster to give go ahead.");
                final String ack = clusterSocketReader.readLine();
                if (ack == null) {
                    throw new RuntimeException("cluster disconnected. re-trying");
                }
                if (ack.length() == 0) {
                    log("cluster started.");
                    break;
                }
                throw new RuntimeException("unknown reply from cluster.");
            } catch (final Throwable t) {
                try {
                    Thread.sleep(1000);
                } catch (final InterruptedException e) {
                    log(e);
                }
            }
        }
    }

    static Socket clusterSocket;
    static BufferedReader clusterSocketReader;
    static BufferedOutputStream clusterSocketOs;
    private static byte[] baNl = "\n".getBytes();

    public static synchronized int execClusterSqlInsert(final String sql) {
        try {
            clusterSocketOs.write(sql.getBytes());
            clusterSocketOs.write(baNl);
            clusterSocketOs.flush();
            final String idStr = clusterSocketReader.readLine();
            if (idStr == null) {
                throw new RuntimeException("lost connection to cluster");
            }
            return Integer.parseInt(idStr);
        } catch (final Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static synchronized void execClusterSql(final String sql) {
        try {
            clusterSocketOs.write(sql.getBytes());
            clusterSocketOs.write(baNl);
            clusterSocketOs.flush();
            final String ack = clusterSocketReader.readLine();
            if (ack == null) {
                throw new RuntimeException("lost connection to cluster");
            }
            if (ack.length() != 0) {
                throw new RuntimeException("unknown reply: {" + ack + "}");
            }
        } catch (final Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * @return A new JDBC connection. The method will block until a connection has
     *         been created.
     */
    public static Connection createJdbcConnection() {
        Connection c = null;
        while (true) {
            try {
                c = DriverManager.getConnection(jdbcConnectionString);
                if (!clusterOn && !autocommit) {
                    c.setAutoCommit(false);
                }
                return c;
            } catch (final Throwable t) {
                try {
                    System.err.println("dbo: cannot create connection. waiting. " + stacktraceToLine(t));
                    Thread.sleep(1000);
                } catch (final InterruptedException e) {
                    log(e);
                }
            }
        }
    }

    private static void printDbMetaInfo(final DatabaseMetaData dbm) throws SQLException {
        // output tables, columns, indexes
        final ResultSet rsTbls = dbm.getTables(null, null, null, new String[] { "TABLE" });
        while (rsTbls.next()) {
            final String tblName = rsTbls.getString("TABLE_NAME");
            Db.log("[" + tblName + "]");
            final ResultSet rsCols = dbm.getColumns(null, null, tblName, null);
            while (rsCols.next()) {
                final String columnName = rsCols.getString("COLUMN_NAME");
                final String dataType = rsCols.getString("TYPE_NAME");
                final String defValue = rsCols.getString("COLUMN_DEF");
                final StringBuilder sb = new StringBuilder();
                sb.append("    ").append(columnName).append(' ').append(dataType);
                if (defValue != null) {
                    sb.append(" '").append(defValue).append('\'');
                }
                Db.log(sb.toString());
            }
            rsCols.close();

            final ResultSet rsIx = dbm.getIndexInfo(null, null, tblName, false, false);
            while (rsIx.next()) {
                Db.log("  index " + rsIx.getString("INDEX_NAME") + " on " + rsIx.getString("COLUMN_NAME"));
            }
            rsIx.close();
            Db.log("");
        }
        rsTbls.close();
    }

    private static void ensureTablesAndIndexes(final Connection con, final DatabaseMetaData dbm) throws Throwable {
        final Statement stmt = con.createStatement();

        // ensure tables exist and match definition
        for (final DbClass dbCls : dbClasses) {
            if (Modifier.isAbstract(dbCls.javaClass.getModifiers())) {
                continue;
            }
            dbCls.ensureTable(stmt, dbm);
        }

        // ensure RefN tables exist and match definition
        for (final RelRefNMeta rrm : relRefNMeta) {
            rrm.ensureTable(stmt, dbm);
        }

        // all tables exist

        // relations might need to create index directly with Statement or add to
        // allIndexes to other classes
        for (final DbClass dbCls : dbClasses) {
            for (final DbRelation dbRel : dbCls.allRelations) {
                dbRel.ensureIndexes(stmt, dbm);
            }
        }

        // ensure indexes exist and match definition
        for (final DbClass dbCls : dbClasses) {
            for (final Index ix : dbCls.allIndexes) {
                ix.ensureIndex(stmt, dbm);
            }
        }

        if (enableDropUndeclaredIndexes) {
            // drop undeclared indexes
            for (final DbClass dbCls : dbClasses) {
                dbCls.dropUndeclaredIndexes(stmt, dbm);
            }

            // drop unused RefN tables
            final ArrayList<String> refsTbls = new ArrayList<String>();
            final ResultSet rsTbls = RelRefNMeta.getAllRefsTables(dbm);
            while (rsTbls.next()) {
                final String tbl = rsTbls.getString("TABLE_NAME");
                refsTbls.add(tbl);
            }

            for (final RelRefNMeta rrm : relRefNMeta) {
                refsTbls.remove(rrm.tableName);
            }

            for (final String s : refsTbls) {
                final StringBuilder sb = new StringBuilder(128);
                sb.append("drop table ").append(s);
                final String sql = sb.toString();
                Db.logSql(sql);
                stmt.execute(sql);
            }
        }

        stmt.close();
    }

    /**
     * Deletes and recreates all tables and indexes. Used by testing framework.
     */
    public static void reset() {
        Db.log("*** reseting database");
        Connection con = null;
        try {
            con = createJdbcConnection();
            final Statement stmt = con.createStatement();
            final StringBuilder sb = new StringBuilder();
            for (final DbClass dbc : dbClasses) {
                if (Modifier.isAbstract(dbc.javaClass.getModifiers())) {
                    continue;
                }
                sb.setLength(0);
                sb.append("drop table ").append(dbc.tableName);
                final String sql = sb.toString();
                Db.logSql(sql);
                stmt.execute(sql);
            }
            for (final RelRefNMeta rnm : relRefNMeta) {
                sb.setLength(0);
                sb.append("drop table ").append(rnm.tableName);
                final String sql = sb.toString();
                Db.logSql(sql);
                stmt.execute(sql);
            }
            stmt.close();

            ensureTablesAndIndexes(con, con.getMetaData());
        } catch (final Throwable t) {
            log(t);
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (final Throwable t) {
                log(t);
            }
        }
    }

    /** Closes the connections in the pool. */
    public static void shutdown() {
        Db.log("dbo: shutdown");
        synchronized (connectionPool) {
            for (final PooledConnection pc : connectionPool) {
                try {
                    pc.getConnection().close();
                } catch (final Throwable t) {
                    log(t);
                }
            }
            connectionPool.clear();
        }
    }

    static String tableNameForJavaClass(final Class<? extends DbObject> cls) {
        return cls.getName().replace('.', '_');
    }

    static DbClass dbClassForJavaClass(final Class<?> c) {
        return clsToDbClsMap.get(c);
    }

    /** @return The {@link DbClass} for the Java class. */
    public static DbClass getDbClassForJavaClass(final Class<? extends DbObject> cls) {
        return clsToDbClsMap.get(cls);
    }

    public static List<DbClass> getDbClasses() {
        return dbClasses;
    }

    public static int getConnectionPoolSize() {
        synchronized (connectionPool) {
            return connectionPool.size();
        }
    }

    public static String getJdbcConnectionString(final String address, final String dbName, final String user,
            final String password) {
        return "jdbc:mysql://" + address + ":3306/" + dbName + "?user=" + user + "&password=" + password
                + "&useSSL=false&allowPublicKeyRetrieval=true";
    }

}
