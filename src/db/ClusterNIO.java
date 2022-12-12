package db;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;

/** Experimental cluster hub. */
public class ClusterNIO {
	public static boolean enable_log = true;
	public static boolean enable_log_sql = false;
	public static int server_port = 8889;
	private static final ArrayList<Client> clients = new ArrayList<Client>();

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
		log("reading config: " + args[0]);
		final FileReader fr = new FileReader(args[0]);
		final BufferedReader bfr = new BufferedReader(fr);
		String line;
		log("creating clients '" + args[1] + "' user '" + args[2] + "' password '" + args[3] + "'");
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
			ct.connectToDatabase();
			clients.add(ct);
//			clusterMembers.add(line);
		}
		bfr.close();
		log("all clients connected to databases.");

		runServer();
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

	static void close(SocketChannel sc) {
		try {
			InetSocketAddress sa = (InetSocketAddress) sc.getRemoteAddress();
			System.out.println("disconnected: " + sa.getHostString());
			sc.close();
		} catch (IOException e) {
			log(e);
		}
	}

	static void execClusterSql(final String sql) throws Throwable {
		log_sql(sql);
		final ArrayList<Client> broken_clients = new ArrayList<Client>();
		for (final Client ct : clients) { // ! thread
			try {
				ct.statement.execute(sql);
			} catch (Throwable t) {
				broken_clients.add(ct);
			}
		}
		for (Client c : broken_clients) {
			log("connection broke. removing: " + c.address);
			clients.remove(c);
		}
	}

	static int execClusterSqlInsert(final String sql) throws Throwable {
		final ArrayList<Integer> ints = new ArrayList<Integer>(clients.size());
		final ArrayList<Client> broken_clients = new ArrayList<Client>();
		log_sql(sql);
		for (final Client ct : clients) { // ! thread
			try {
				ct.statement.execute(sql, Statement.RETURN_GENERATED_KEYS);
				final ResultSet rs = ct.statement.getGeneratedKeys();
				if (rs.next()) {
					ints.add(rs.getInt(1));
					rs.close();
				} else
					throw new RuntimeException("expected generated id");
			} catch (Throwable t) {
				broken_clients.add(ct);
			}
		}
		for (Client ct : broken_clients) {
			log("connection broke. removing: " + ct.address);
			clients.remove(ct);
		}
		if (ints.isEmpty()) {
			throw new RuntimeException("expected generated ids list is empty");
		}
		// check that it is the same id
		int prev = ints.get(0);
		final int n = ints.size();
		for (int j = 1; j < n; j++) {
			final int id = ints.get(j);
			if (id != prev)
				throw new RuntimeException("expected generated ids to be same. got: " + ints);
			prev = id;
		}
		return prev;
	}

	private static void runServer() throws IOException, ClosedChannelException {
		log("waiting for " + clients.size() + " clients");
		ServerSocketChannel ssc = ServerSocketChannel.open();
		ssc.configureBlocking(false);
		ssc.bind(new InetSocketAddress(server_port));

		Selector selector = Selector.open();
		SelectionKey ssk = ssc.register(selector, SelectionKey.OP_ACCEPT);
		// wait for clients to connect
		int client_count = 0;
		final int nclients = clients.size();
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
					log("accepted: " + ct.address + " (" + client_count + " of " + nclients + ")");
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

		log("clients connected. starting cluster.");
		// register client channels for read
		final byte[] ba_nl = "\n".getBytes();
		for (Client ct : clients) {
			ct.socketChannel.register(selector, SelectionKey.OP_READ, ct);
			final int c = ct.socketChannel.write(ByteBuffer.wrap(ba_nl));
			if (c != ba_nl.length)
				throw new RuntimeException("Could not write full message to client.");

		}
		while (true) {
			selector.select();
			Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
			while (keys.hasNext()) {
				SelectionKey sk = keys.next();
				Client cs = (Client) sk.attachment();
				if (sk.isReadable()) {
					try {
						cs.process(sk);
					} catch (Throwable t) {
						close((SocketChannel) sk.channel());
						log(t);
					}
				} else {
					throw new RuntimeException("expected selection key to be read");
				}
				keys.remove();
			}
		}

	}

	private static Client findClientByAddress(String hostString) {
		for (Client ct : clients) {
			if (ct.address.equals(hostString))
				return ct;
		}
		throw new RuntimeException("client with address '" + hostString + "' is not registered.");
	}

	private final static class Client {
		ByteBuffer bb = ByteBuffer.allocate(64 * 1024);
		ByteBuffer bb_nl = ByteBuffer.wrap("\n".getBytes());
		StringBuilder sb = new StringBuilder(1024);
		Connection connection;
		Statement statement;
		SocketChannel socketChannel;
		String address;
		String dbname;
		String user;
		String password;

		public Client(String address, String dbname, String user, String password) {
			this.address = address;
			this.dbname = dbname;
			this.user = user;
			this.password = password;
			log("client: " + address);
		}

		public void process(SelectionKey sk) throws Throwable {
			SocketChannel sc = (SocketChannel) sk.channel();
			bb.clear();
			int read = sc.read(bb);
			if (read == -1) {
				close(sc);
				return;
			}
			bb.flip();
			byte ch = bb.get(bb.limit() - 1);
			if (ch == '\n') { // if last character is \n then the read is done
				sb.append(new String(bb.array(), bb.position(), bb.limit() - 1));
				String sql = sb.toString();
				if (sql.startsWith("insert ")) {
					int id = execClusterSqlInsert(sql);
					ByteBuffer bb = ByteBuffer.wrap((id + "\n").getBytes());
					sc.write(bb);
					if (bb.remaining() != 0) {
						throw new RuntimeException("could not fully write buffer");
					}
					sb.setLength(0);
				} else {
					execClusterSql(sql);
					bb_nl.clear();
					sc.write(bb_nl);
					if (bb_nl.remaining() != 0) {
						throw new RuntimeException("could not fully write buffer");
					}
					sb.setLength(0);
				}
				return;
			}
			sb.append(new String(bb.array(), bb.position(), bb.limit()));
		}

		public void connectToDatabase() {
			log("connecting to: " + address);
			final String cs = "jdbc:mysql://" + address + ":3306/" + dbname
					+ "?verifyServerCertificate=false&useSSL=true&ssl-mode=REQUIRED";
			while (true) {
				try {
					connection = DriverManager.getConnection(cs, user, password);
					statement = connection.createStatement();
					break;
				} catch (Throwable t) {
					try {
						System.err.println("cannot connect to database at address " + address + ". waiting.");
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						log(e);
					}
				}
			}

		}
	}

}
