package db;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;

/** Experimental cluster hub. */
public final class Cluster {
	/** true if sql statements to cluster members are executed in parallel */
	public static boolean execute_in_parallel = true;
	public static long connection_refresh_intervall_ms = 60 * 60 * 1000;
	public static boolean enable_log = true;
	public static boolean enable_log_sql = false;
	public static int server_port = 8889;
	// public static long connectionRefreshIntervallMs = 10 * 1000;
	/** Counter used to synchronize. */
	private static int activeThreads;
	private static final ArrayList<Client> clients = new ArrayList<Client>();
	/** Timestamp for when the connections where created. */
	private static long connections_last_refresh_ms;
	/** Synchronization object. */
	private static Object sem = new Object();
	/** Current SQL executed by the cluster */
	private static String current_sql;

	public static String dbname;
	public static String user;
	public static String password;

	/** Prints the string to System.out */
	public static void log(String s) {
		if (!enable_log)
			return;
		System.out.println(s);
	}

	public static void log(Throwable t) {
		while (t.getCause() != null)
			t = t.getCause();
		System.err.println(stacktraceline(t));
	}

	public static void log_sql(String s) {
		if (!enable_log_sql)
			return;
		System.out.println(s);
	}

	public static void main(String[] args) throws Throwable {
		Class.forName("com.mysql.jdbc.Driver"); // ! java 1.5
		if (args.length < 4) {
			System.out.println("Usage: java db.ClusterNIO <ip:port file> <dbname> <user> <password>");
			return;
		}
		dbname = args[1];
		user = args[2];
		password = args[3];
		// connect to cluster members
		log("database '" + dbname + "' user '" + user + "' password [not displayed]");
		log("reading members ip file '" + args[0] + "'");
		final FileReader fr = new FileReader(args[0]);
		final BufferedReader bfr = new BufferedReader(fr);
		String line;
		while (true) {
			line = bfr.readLine();
			if (line == null)
				break;
			line = line.trim();
			if (line.length() == 0)
				continue;
			if (line.startsWith("#"))
				continue;
			final Client ct = new Client(line);
			clients.add(ct);
			log("  connecting to database at " + ct.address);
			ct.connectToDatabase();
		}
		bfr.close();
		log("connected to databases.");
		connections_last_refresh_ms = System.currentTimeMillis();

		final int nclients = clients.size();
		log("waiting for " + nclients + " client" + (nclients > 1 ? "s" : "") + " to connect.");
		ServerSocketChannel ssc = ServerSocketChannel.open();
		ssc.configureBlocking(false);
		final InetSocketAddress isa = new InetSocketAddress(server_port);
		ssc.socket().bind(isa);

		Selector selector = Selector.open();
		SelectionKey ssk = ssc.register(selector, SelectionKey.OP_ACCEPT);
		// wait for clients to connect
		int client_count = 0;
		while (true) {
			selector.select();
			Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
			while (keys.hasNext()) {
				SelectionKey sk = keys.next();
				if (sk.isAcceptable()) {
					SocketChannel sc = ssc.accept();
					sc.configureBlocking(false);
					sc.socket().setTcpNoDelay(true);
//					sc.setOption(StandardSocketOptions.TCP_NODELAY, true);
//					InetSocketAddress sa = (InetSocketAddress) sc.getRemoteAddress();
//					Client ct = findClientByAddress(sa.getHostString());
					final String host = sc.socket().getInetAddress().getHostAddress();
					Client ct = findClientByAddress(host);
					ct.socketChannel = sc;
					keys.remove();
					client_count++;
					log("  client " + ct.address + " connected (" + client_count + " of " + nclients + ")");
					continue;
				} else {
					throw new RuntimeException("expected selection key to be accept");
				}
			}
			if (client_count == nclients)
				break;
		}
		ssk.cancel(); // done with accepting connections
		ssc.close();

		log("starting cluster.");
		// register client channels for read and give go ahead
		final byte[] ba_nl = "\n".getBytes();
		for (Client ct : clients) {
			if (execute_in_parallel)
				ct.thread.start();
			ct.socketChannel.register(selector, SelectionKey.OP_READ, ct);
			final int c = ct.socketChannel.write(ByteBuffer.wrap(ba_nl));
			if (c != ba_nl.length)
				throw new RuntimeException("Could not write full message to client.");
		}
		while (true) {
			selector.select(10 * 1000); // unblock every 10th second
			refreshConnectionsIfNecessary();
			Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
			while (keys.hasNext()) {
				SelectionKey sk = keys.next();
				Client ct = (Client) sk.attachment();
				if (sk.isReadable()) {
					try {
						ct.process();
					} catch (Throwable t) {
						ct.close();
						System.out.println("disconnected: " + ct.address);
						clients.remove(ct);
						if (!t.getMessage().startsWith("client disconnected"))
							log(t);
					}
				} else {
					throw new RuntimeException("expected selection key to be read");
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

	public static String stacktraceline(final Throwable e) {
		return stacktrace(e).replace('\n', ' ').replace('\r', ' ').replaceAll("\\s+", " ").replaceAll(" at ", " @ ");
	}

	private static int execSql(final String sql) {
		if (!execute_in_parallel)
			return execSql_serial(sql);

		current_sql = sql;
		// notify all threads to execute sql
		synchronized (sem) {
			activeThreads = clients.size();
			sem.notifyAll();
		}
		// wait for the threads to finish
		synchronized (sem) {
			while (activeThreads != 0) {
				try {
					sem.wait();
				} catch (InterruptedException ok) {
				}
			}
		}
		if (sql.startsWith("insert ")) {
			final int n = clients.size();
			if (n == 0)
				throw new RuntimeException("no threads");
			int prev = clients.get(0).thread.autogeneratedId;
			for (int i = 0; i < n; i++) {
				final int id = clients.get(i).thread.autogeneratedId;
				if (id != prev)
					throw new RuntimeException(
							"autogenerated ids do not match " + prev + " vs " + id + " after sql " + sql);
				prev = id;
			}
			return prev;
		} else {
			return 0;
		}
	}

	private static int execSql_serial(String sql) {
		ArrayList<Client> broken_clients = null;
		int autogenerated_id = 0;
		if (sql.startsWith("insert ")) {
			final int nclients = clients.size();
			ArrayList<Integer> autogenerated_ids = new ArrayList<Integer>(nclients);
			for (Client ct : clients) {
				try {
					ct.statement.execute(sql, Statement.RETURN_GENERATED_KEYS);
					final ResultSet rs = ct.statement.getGeneratedKeys();
					if (rs.next()) {
						autogenerated_ids.add(rs.getInt(1));
						rs.close();
					} else
						throw new RuntimeException("expected generated id");
				} catch (SQLException e) {
					log(e);
					if (broken_clients == null) {
						broken_clients = new ArrayList<Client>();
					}
					broken_clients.add(ct);
				}
			}
			if (autogenerated_ids.size() == 0)
				throw new RuntimeException("autogenerated ids is empty");
			int previd = autogenerated_ids.get(0);
			for (int i = 1; i < nclients; i++) {
				final int id = autogenerated_ids.get(1);
				if (previd != id)
					throw new RuntimeException("generated ids do not match " + id + " vs " + previd + " after " + sql);
				previd = id;
			}
			autogenerated_id = previd;
		} else {
			for (Client ct : clients) {
				try {
					ct.statement.execute(sql);
				} catch (SQLException e) {
					log(e);
					if (broken_clients == null) {
						broken_clients = new ArrayList<Client>();
					}
					broken_clients.add(ct);
				}
			}
		}
		if (broken_clients != null) {
			for (Client ct : broken_clients) {
				ct.close();
				clients.remove(ct);
			}
		}
		return autogenerated_id;
	}

	private static Client findClientByAddress(String address) {
		for (Client ct : clients) {
			if (ct.address.equals(address))
				return ct;
		}
		throw new RuntimeException("client with address '" + address + "' is not registered.");
	}

	private static void refreshConnectionsIfNecessary() {
		final long t1 = System.currentTimeMillis();
		final long dt = t1 - connections_last_refresh_ms;
		if (dt < connection_refresh_intervall_ms)
			return;
		log("refreshing connections.");
		connections_last_refresh_ms = t1;
		ArrayList<Client> brokenClients = null;
		for (Client ct : clients) {
			try {
				ct.refreshConnection();
			} catch (SQLException e) {
				if (brokenClients == null) {
					brokenClients = new ArrayList<Client>();
				}
				brokenClients.add(ct);
				continue;
			}
		}
		if (brokenClients == null)
			return;
		for (Client ct : brokenClients) {
			ct.close();
			clients.remove(ct);
		}
	}

	private final static class Client {
		private String address;
		private ByteBuffer bb = ByteBuffer.allocate(64 * 1024);
		private ByteBuffer bb_nl = ByteBuffer.wrap("\n".getBytes());
		private Connection connection;
		private StringBuilder sb = new StringBuilder(64 * 1024);
		private SocketChannel socketChannel;
		private Statement statement;
		private ClientThread thread;

		public Client(String address) {
			this.address = address;
			if (execute_in_parallel) {
				this.thread = new ClientThread(this, address);
			}
		}

		public void close() {
			thread.stopped = true;
			thread.interrupt();
			try {
				socketChannel.close();
			} catch (IOException e) {
				log(e);
			}
			try {
				connection.close();
			} catch (SQLException e) {
				log(e);
			}
		}

		public void connectToDatabase() {
			final String cs = Db.getJdbcConnectionString(address, dbname, user, password);
			while (true) {
				try {
					connection = DriverManager.getConnection(cs);
					statement = connection.createStatement();
//					log("connected to database at " + address);
					break;
				} catch (SQLException t) {
					try {
						System.err.println("cannot connect to database at " + address + ". waiting.");
						log(t);
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						log(e);
					}
				}
			}
		}

		public void process() throws Throwable {
			bb.clear();
			int read = socketChannel.read(bb);
			if (read == -1) {
				throw new RuntimeException("client disconnected " + address);
			}
			bb.flip();
			byte ch = bb.get(bb.limit() - 1);
			if (ch == '\n') { // if last character is \n then the read is done
				sb.append(new String(bb.array(), bb.position(), bb.limit() - 1));
				String sql = sb.toString();
				final int id = execSql(sql);
				if (id != 0) {
					ByteBuffer bb = ByteBuffer.wrap((id + "\n").getBytes());
					socketChannel.write(bb);
					if (bb.remaining() != 0) {
						throw new RuntimeException("could not fully write buffer");
					}
				} else {
					bb_nl.clear();
					socketChannel.write(bb_nl);
					if (bb_nl.remaining() != 0) {
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
			} catch (SQLException e) {
				log(e);
			}
			final String cs = Db.getJdbcConnectionString(address, dbname, user, password);
			connection = DriverManager.getConnection(cs);
			statement = connection.createStatement();
		}
	}

	private final static class ClientThread extends Thread {
		private int autogeneratedId;
		private Client client;
		private String prevSql;
		private boolean stopped;

		public ClientThread(Client client, String name) {
			super(name);
			this.client = client;
		}

		@Override
		public void run() {
			while (true) {
				if (stopped)
					break;
				// wait for new sql or stopped
				synchronized (sem) {
					while (!stopped && prevSql == current_sql) {
						try {
							sem.wait();
						} catch (InterruptedException ok) {
						}
					}
				}
				if (stopped) // thread might be flagged for stop after interrupt
					break;
				prevSql = current_sql;
				autogeneratedId = 0;
				Cluster.log_sql(this + ": " + current_sql);
				try {
					if (current_sql.startsWith("insert ")) {
						client.statement.execute(current_sql, Statement.RETURN_GENERATED_KEYS);
						final ResultSet rs = client.statement.getGeneratedKeys();
						if (rs.next()) {
							autogeneratedId = rs.getInt(1);
							rs.close();
						} else
							throw new RuntimeException("expected generated id");
					} else {
						client.statement.execute(current_sql);
					}
				} catch (SQLException e) {
					// close client
					log(e);
					client.close();
					synchronized (clients) {
						clients.remove(client);
					}
					// last thread done notifies the executor to continue
					synchronized (sem) {
						activeThreads--;
						if (activeThreads == 0) {
							sem.notify();
						}
					}
					return;
				}
				// last thread done notifies the executor to continue
				synchronized (sem) {
					activeThreads--;
					if (activeThreads == 0) {
						sem.notify();
					}
				}
			}
		}
	}
}
