package db;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
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
	private static final ArrayList<Connection> cons = new ArrayList<Connection>();
	private static ArrayList<SocketChannel> socketChannels = new ArrayList<SocketChannel>();
	private static final ArrayList<Statement> stmts = new ArrayList<Statement>();

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
		log("opening connections to database '" + args[1] + "' user '" + args[2] + "' password '" + args[3] + "'");
		while (true) {
			line = bfr.readLine();
			if (line == null)
				break;
			line = line.trim();
			if (line.length() == 0)
				continue;
			if (line.startsWith("#"))
				continue;
			log("connecting to: " + line);
			final String cs = "jdbc:mysql://" + line + "/" + args[1]
					+ "?verifyServerCertificate=false&useSSL=true&ssl-mode=REQUIRED";
			while (true) {
				try {
					Connection c = DriverManager.getConnection(cs, args[2], args[3]);
					cons.add(c);
					stmts.add(c.createStatement());
					break;
				} catch (Throwable t) {
					try {
						System.err.println("dbcluster: cannot connect to " + line + ". waiting.");
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						log(e);
					}
				}
			}
//			clusterMembers.add(line);
		}
		bfr.close();
		log("connected to cluster databases.");

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
		for (final Statement s : stmts) { // ! thread
			s.execute(sql);
		}
	}

	static int execClusterSqlInsert(final String sql) throws Throwable {
		final ArrayList<Integer> ints = new ArrayList<Integer>(stmts.size());
		log_sql(sql);
		for (final Statement s : stmts) { // ? thread
			s.execute(sql, Statement.RETURN_GENERATED_KEYS);
			final ResultSet rs = s.getGeneratedKeys();
			if (rs.next()) {
				ints.add(rs.getInt(1));
				rs.close();
			} else
				throw new RuntimeException("expected generated id");
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
		log("waiting for " + cons.size() + " clients");
		ServerSocketChannel ssc = ServerSocketChannel.open();
		ssc.configureBlocking(false);
		ssc.bind(new InetSocketAddress(server_port));

		Selector selector = Selector.open();
		SelectionKey ssk = ssc.register(selector, SelectionKey.OP_ACCEPT);
		// wait for clients to connect
		int client_count = 0;
		final int nclients = cons.size();
		while (true) {
			selector.select();
			Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
			while (keys.hasNext()) {
				SelectionKey sk = keys.next();
				if (sk.isAcceptable()) {
					SocketChannel sc = ssc.accept();
					sc.configureBlocking(false);
					sc.setOption(StandardSocketOptions.TCP_NODELAY, true);
					socketChannels.add(sc);
					keys.remove();
					client_count++;
					InetSocketAddress sa = (InetSocketAddress) sc.getRemoteAddress();
					log("accepted: " + sa.getHostString() + " (" + client_count + " of " + nclients + ")");
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
		for (SocketChannel sc : socketChannels) {
			sc.register(selector, SelectionKey.OP_READ, new ClientState());
			final int c = sc.write(ByteBuffer.wrap(ba_nl));
			if (c != ba_nl.length)
				throw new RuntimeException("Could not write full message to client.");
		}
		while (true) {
			selector.select();
			Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
			while (keys.hasNext()) {
				SelectionKey sk = keys.next();
				ClientState cs = (ClientState) sk.attachment();
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

	private final static class ClientState {
		ByteBuffer bb = ByteBuffer.allocate(64 * 1024);
		ByteBuffer bb_nl = ByteBuffer.wrap("\n".getBytes());
		StringBuilder sb = new StringBuilder(1024);

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
	}

}
