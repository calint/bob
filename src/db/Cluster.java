package db;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
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
	public static boolean enable_log = true;
	public static boolean enable_log_sql = false;
	public static int server_port = 8889;
	/** Counter used to synchronize. */
	private static int activeThreads;
	private static final ArrayList<Client> clients = new ArrayList<Client>();
	/** Synchronization object. */
	private static Object sem = new Object();
	/** Current SQL executed by the cluster */
	private static String sql;

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
		if (args.length < 4) {
			System.out.println("Usage: java db.ClusterNIO <ip:port file> <dbname> <user> <password>");
			return;
		}
		// connect to cluster members
		log("database '" + args[1] + "' user '" + args[2] + "' password [not displayed]");
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
			final Client ct = new Client(line, args[1], args[2], args[3]);
			clients.add(ct);
			log("  connecting to database at " + ct.address);
			ct.connectToDatabase();
		}
		bfr.close();
		log("connected to databases.");

		final int nclients = clients.size();
		log("waiting for " + nclients + " client" + (nclients > 1 ? "s" : "") + " to connect.");
		ServerSocketChannel ssc = ServerSocketChannel.open();
		ssc.configureBlocking(false);
		ssc.bind(new InetSocketAddress(server_port));

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
					sc.setOption(StandardSocketOptions.TCP_NODELAY, true);
					InetSocketAddress sa = (InetSocketAddress) sc.getRemoteAddress();
					Client ct = findClientByAddress(sa.getHostString());
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
		// register client channels for read
		final byte[] ba_nl = "\n".getBytes();
		for (Client ct : clients) {
			ct.socketChannel.register(selector, SelectionKey.OP_READ, ct);
			final int c = ct.socketChannel.write(ByteBuffer.wrap(ba_nl));
			if (c != ba_nl.length)
				throw new RuntimeException("Could not write full message to client.");
			ct.thread.start();
		}
		while (true) {
			selector.select();
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

	static int execSql(final String sql) {
//		for (Client ct : clients) {
//			ct.thread.sql = sql;
//		}
		Cluster.sql = sql;
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

	private static Client findClientByAddress(String address) {
		for (Client ct : clients) {
			if (ct.address.equals(address))
				return ct;
		}
		throw new RuntimeException("client with address '" + address + "' is not registered.");
	}

	private final static class Client {
		private String address;
		private ByteBuffer bb = ByteBuffer.allocate(64 * 1024);
		private ByteBuffer bb_nl = ByteBuffer.wrap("\n".getBytes());
		private Connection connection;
		private String dbname;
		private String password;
		private StringBuilder sb = new StringBuilder(64 * 1024);
		private SocketChannel socketChannel;
		private Statement statement;
		private ClientThread thread;
		private String user;

		public Client(String address, String dbname, String user, String password) {
			this.address = address;
			this.dbname = dbname;
			this.user = user;
			this.password = password;
			this.thread = new ClientThread(this, address);
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
			final String cs = "jdbc:mysql://" + address + ":3306/" + dbname
					+ "?verifyServerCertificate=false&useSSL=true&ssl-mode=REQUIRED";
			while (true) {
				try {
					connection = DriverManager.getConnection(cs, user, password);
					statement = connection.createStatement();
//					log("connected to database at " + address);
					break;
				} catch (Throwable t) {
					try {
						System.err.println("cannot connect to database at " + address + ". waiting.");
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
					while (!stopped && prevSql == sql) {
						try {
							sem.wait();
						} catch (InterruptedException ok) {
						}
					}
				}
				if (stopped) // thread might be flagged for stop after interrupt
					break;
				prevSql = sql;
				autogeneratedId = 0;
				Cluster.log_sql(this + ": " + sql);
				try {
					if (sql.startsWith("insert ")) {
						client.statement.execute(sql, Statement.RETURN_GENERATED_KEYS);
						final ResultSet rs = client.statement.getGeneratedKeys();
						if (rs.next()) {
							autogeneratedId = rs.getInt(1);
							rs.close();
						} else
							throw new RuntimeException("expected generated id");
					} else {
						client.statement.execute(sql);
					}
				} catch (SQLException e) {
					// close client
					log(e);
					client.close();
					synchronized(clients) {
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
