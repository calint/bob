//
// reviewed: 2024-08-05
//           2025-04-28
//
package db;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;

/** Experimental cluster hub. */
public final class Cluster {

    /** True if sql statements to cluster nodes are executed in parallel. */
    public static boolean executeInParallel = true;

    /** Close and re-open connections to databases interval. */
    public static long connectionRefreshIntervalMs = 60 * 60 * 1000;

    public static boolean enableLog = true;

    /** Log SQL executed on databases. */
    public static boolean enableLogSql = false;

    public static int serverPort = 8889;

    private final static ArrayList<Client> clients = new ArrayList<Client>();

    /** Timestamp for when the connections where last refreshed. */
    private static long connectionsLastRefreshMs;

    /** Synchronization object. */
    private static Object monitor = new Object();

    /** Counter used when synchronizing. */
    private static int activeThreads;

    /** Current SQL executed by the cluster */
    private static String currentSql;

    private static String dbName;

    private static String dbUser;

    private static String dbPassword;

    /** Prints the string to System.out. */
    public static void log(final String s) {
        if (!enableLog) {
            return;
        }
        System.out.println(s);
    }

    public static void log(Throwable t) {
        while (t.getCause() != null) {
            t = t.getCause();
        }
        System.err.println(stacktraceToLine(t));
    }

    public static void logSql(final String s) {
        if (!enableLogSql) {
            return;
        }
        System.out.println(s);
    }

    public static void main(final String[] args) throws Throwable {
        final Driver driver = (Driver) Class.forName("com.mysql.jdbc.Driver").getConstructor().newInstance();
        // note: necessary in Java 1.5
        DriverManager.registerDriver(driver);

        if (args.length < 4) {
            System.out.println("Usage: " + Cluster.class.getName() + " <ip:port file> <database> <user> <password>");
            return;
        }

        dbName = args[1];
        dbUser = args[2];
        dbPassword = args[3];

        // connect to cluster nodes
        log("database '" + dbName + "' user '" + dbUser + "' password [not displayed]");
        log("reading nodes ip file '" + args[0] + "'");
        final FileReader fr = new FileReader(args[0]);
        final BufferedReader bfr = new BufferedReader(fr);
        while (true) {
            String line = bfr.readLine();
            if (line == null) {
                break;
            }
            line = line.trim();
            if (line.length() == 0 || line.startsWith("#")) {
                continue;
            }
            final Client ct = new Client(line);
            clients.add(ct);
        }
        bfr.close();

        final int nclients = clients.size();
        log("  " + nclients + " node" + (nclients > 1 ? "s" : "") + ".");
        log("connecting to databases.");
        int i = 0;
        for (final Client ct : clients) {
            i++;
            log("  " + ct.address + " (" + i + " of " + nclients + ")");
            ct.connectToDatabase();
        }
        log("connected to databases.");
        connectionsLastRefreshMs = System.currentTimeMillis();

        log("waiting for " + nclients + " node" + (nclients > 1 ? "s" : "") + " to connect.");

        final ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        final InetSocketAddress isa = new InetSocketAddress(serverPort);
        ssc.socket().bind(isa);

        final Selector selector = Selector.open();
        final SelectionKey ssk = ssc.register(selector, SelectionKey.OP_ACCEPT);

        // wait for clients to connect
        int client_count = 0;
        while (true) {
            selector.select();
            final Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
            while (keys.hasNext()) {
                final SelectionKey sk = keys.next();
                if (sk.isAcceptable()) {
                    final SocketChannel sc = ssc.accept();
                    sc.configureBlocking(false);
                    sc.socket().setTcpNoDelay(true);
                    final String host = sc.socket().getInetAddress().getHostAddress();
                    final Client ct = findClientByAddress(host);
                    ct.socketChannel = sc;
                    keys.remove();
                    client_count++;
                    log("  " + ct.address + " (" + client_count + " of " + nclients + ")");
                    continue;
                }
                throw new RuntimeException("expected selection key to be accept");
            }
            if (client_count == nclients) {
                break;
            }
        }
        ssk.cancel(); // done with accepting connections
        ssc.close();

        log("starting cluster.");
        // register client channels for read and give go ahead
        if (executeInParallel) {
            for (final Client ct : clients) {
                ct.thread.start();
            }
        }

        // ? potential racing. wait for all the threads to be in the semaphore before
        // starting

        // register for read
        for (final Client ct : clients) {
            ct.socketChannel.register(selector, SelectionKey.OP_READ, ct);
        }

        // give go ahead
        final byte[] baNewLine = "\n".getBytes();
        for (final Client ct : clients) {
            final int c = ct.socketChannel.write(ByteBuffer.wrap(baNewLine));
            if (c != baNewLine.length) {
                throw new RuntimeException("Could not write full message to client.");
            }
        }

        // processing
        while (true) {
            selector.select(10 * 1000); // unblock every 10th second
            refreshConnectionsIfNecessary();
            final Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
            while (keys.hasNext()) {
                final SelectionKey sk = keys.next();
                final Client ct = (Client) sk.attachment();
                if (!sk.isReadable()) {
                    throw new RuntimeException("expected selection key to be read");
                }
                try {
                    ct.process();
                } catch (final Throwable t) {
                    ct.close();
                    clients.remove(ct);
                    if (!t.getMessage().startsWith("client disconnected")) {
                        log(t);
                    }
                }
                keys.remove();
            }
        }
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

    /** @return Generated id when insert statement or 0. */
    private static int execSql(final String sql) {
        if (!executeInParallel) {
            return executeSqlInSerial(sql);
        }

        // set new SQL to execute, notify all clients and wait for all to be done
        synchronized (monitor) {
            currentSql = sql;
            activeThreads = clients.size();
            // notify all threads to execute sql
            monitor.notifyAll();
            // wait for last thread to finish
            while (activeThreads != 0) {
                try {
                    monitor.wait();
                } catch (final InterruptedException ok) {
                }
            }
        }

        if (!sql.startsWith("insert ")) {
            return 0;
        }
        // extract and check generated id
        final int n = clients.size();
        if (n == 0) {
            throw new RuntimeException("no threads");
        }
        int prev = clients.get(0).thread.autogeneratedId;
        // check that all clients generated the same id
        for (int i = 1; i < n; i++) {// 1 because first id already extracted
            final int id = clients.get(i).thread.autogeneratedId;
            if (id != prev) {
                throw new RuntimeException(
                        "autogenerated ids do not match " + prev + " vs " + id + " after sql " + sql);
            }
            prev = id;
        }
        return prev;
    }

    private static int executeSqlInSerial(final String sql) {
        ArrayList<Client> broken_clients = null;
        int autogenerated_id = 0;
        if (sql.startsWith("insert ")) {
            final int nclients = clients.size();
            final ArrayList<Integer> autogenerated_ids = new ArrayList<Integer>(nclients);
            for (final Client ct : clients) {
                try {
                    ct.statement.execute(sql, Statement.RETURN_GENERATED_KEYS);
                    final ResultSet rs = ct.statement.getGeneratedKeys();
                    if (!rs.next()) {
                        throw new RuntimeException("expected generated id");
                    }
                    autogenerated_ids.add(rs.getInt(1));
                    rs.close();
                } catch (final Throwable e) {
                    log(e);
                    if (broken_clients == null) {
                        broken_clients = new ArrayList<Client>();
                    }
                    broken_clients.add(ct);
                }
            }
            if (autogenerated_ids.size() == 0) {
                throw new RuntimeException("autogenerated ids is empty");
            }
            int previd = autogenerated_ids.get(0);
            // check that all clients generated the same id
            for (int i = 1; i < nclients; i++) {// 1 because first id already extracted
                final int id = autogenerated_ids.get(1);
                if (previd != id) {
                    throw new RuntimeException("generated ids do not match " + id + " vs " + previd + " after " + sql);
                }
                previd = id;
            }
            autogenerated_id = previd;
        } else {
            // not insert statement
            for (final Client ct : clients) {
                try {
                    ct.statement.execute(sql);
                } catch (final Throwable e) {
                    log(e);
                    if (broken_clients == null) {
                        broken_clients = new ArrayList<Client>();
                    }
                    broken_clients.add(ct);
                }
            }
        }
        if (broken_clients != null) {
            for (final Client ct : broken_clients) {
                ct.close();
                clients.remove(ct);
            }
        }
        return autogenerated_id;
    }

    private static Client findClientByAddress(final String address) {
        for (final Client ct : clients) {
            if (ct.address.equals(address)) {
                return ct;
            }
        }
        throw new RuntimeException("client with address '" + address + "' is not registered.");
    }

    private static void refreshConnectionsIfNecessary() {
        final long t1 = System.currentTimeMillis();
        final long dt = t1 - connectionsLastRefreshMs;
        if (dt < connectionRefreshIntervalMs) {
            return;
        }
        connectionsLastRefreshMs = t1;
        ArrayList<Client> brokenClients = null;
        for (final Client ct : clients) {
            try {
                ct.refreshConnection();
            } catch (final Throwable e) {
                if (brokenClients == null) {
                    brokenClients = new ArrayList<Client>();
                }
                brokenClients.add(ct);
                continue;
            }
        }
        final long dt1 = System.currentTimeMillis() - connectionsLastRefreshMs;
        log("refreshed connections in " + dt1 + " ms");
        if (brokenClients == null) {
            return;
        }
        for (final Client ct : brokenClients) {
            ct.close();
            clients.remove(ct);
        }
    }

    private final static class Client {
        private final String address;
        private final ByteBuffer bb = ByteBuffer.allocate(64 * 1024);
        private final ByteBuffer bbNewLine = ByteBuffer.wrap("\n".getBytes());
        private final StringBuilder sb = new StringBuilder(64 * 1024);
        private final ClientThread thread;
        private Connection connection;
        private SocketChannel socketChannel;
        private Statement statement;

        public Client(final String address) {
            this.address = address;
            if (executeInParallel) {
                thread = new ClientThread(this, address);
            } else {
                thread = null;
            }
        }

        public void close() {
            thread.stopped = true;
            thread.interrupt();
            try {
                socketChannel.close();
            } catch (final Throwable e) {
                log(e);
            }
            try {
                connection.close();
            } catch (final Throwable e) {
                log(e);
            }
            System.out.println("disconnected: " + address);
        }

        public void connectToDatabase() {
            // ? do this in the constructor and make fields final
            final String cs = Db.getJdbcConnectionString(address, dbName, dbUser, dbPassword);
            while (true) {
                try {
                    connection = DriverManager.getConnection(cs);
                    statement = connection.createStatement();
                    break;
                } catch (final Throwable t) {
                    try {
                        System.err.println("cannot connect to database at " + address + ". waiting.");
                        log(t);
                        Thread.sleep(5000);
                    } catch (final InterruptedException e) {
                        log(e);
                    }
                }
            }
        }

        public void process() throws Throwable {
            bb.clear();
            final int read = socketChannel.read(bb);
            if (read == -1) {
                throw new RuntimeException("client disconnected " + address);
            }
            bb.flip();
            final byte ch = bb.get(bb.limit() - 1);
            if (ch == '\n') {
                // if last character is \n then the read is done
                sb.append(new String(bb.array(), bb.position(), bb.limit() - 1));
                final String sql = sb.toString();
                final int id = execSql(sql);
                if (id != 0) {
                    final ByteBuffer bb = ByteBuffer.wrap((id + "\n").getBytes());
                    socketChannel.write(bb);
                    if (bb.remaining() != 0) {
                        throw new RuntimeException("could not fully write buffer");
                    }
                } else {
                    bbNewLine.clear();
                    socketChannel.write(bbNewLine);
                    if (bbNewLine.remaining() != 0) {
                        throw new RuntimeException("could not fully write buffer");
                    }
                }
                sb.setLength(0);
                return;
            }
            sb.append(new String(bb.array(), bb.position(), bb.limit()));
        }

        public void refreshConnection() throws SQLException {
            try {
                connection.close();
            } catch (final Throwable e) {
                log(e);
            }
            final String cs = Db.getJdbcConnectionString(address, dbName, dbUser, dbPassword);
            connection = DriverManager.getConnection(cs);
            statement = connection.createStatement();
        }
    }

    private final static class ClientThread extends Thread {
        // ? reduce context of thread in case Java memory model flushes too much
        private int autogeneratedId;
        private final Client client;
        private String prevSql;
        private boolean stopped;

        public ClientThread(final Client client, final String name) {
            super(name);
            this.client = client;
        }

        @Override
        public void run() {
            while (true) {
                if (stopped) {
                    break;
                }
                // wait for new sql or stopped
                synchronized (monitor) {
                    while (!stopped && prevSql == currentSql) {
                        try {
                            monitor.wait();
                        } catch (final InterruptedException ok) {
                        }
                    }
                }
                if (stopped) {
                    // thread might be flagged for stop and then interrupted
                    break;
                }
                prevSql = currentSql;
                autogeneratedId = 0;
                try {
                    if (currentSql.startsWith("insert ")) {
                        client.statement.execute(currentSql, Statement.RETURN_GENERATED_KEYS);
                        final ResultSet rs = client.statement.getGeneratedKeys();
                        if (!rs.next()) {
                            throw new RuntimeException("expected generated id");
                        }
                        autogeneratedId = rs.getInt(1);
                        rs.close();
                    } else {
                        // not insert statement
                        client.statement.execute(currentSql);
                    }
                } catch (final Throwable e) {
                    // close client
                    log(e);
                    client.close();
                    synchronized (clients) {
                        clients.remove(client);
                    }
                    // last thread done notifies the executor to continue
                    synchronized (monitor) {
                        activeThreads--;
                        if (activeThreads == 0) {
                            monitor.notify();
                        }
                    }
                    return;
                }
                // last thread done notifies the executor to continue
                synchronized (monitor) {
                    activeThreads--;
                    if (activeThreads == 0) {
                        monitor.notify();
                    }
                }
            }
        }
    }

}
