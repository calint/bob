package db;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
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

import b.b;

/** Experimental cluster hub. */
public class ClusterNIO {
	public static boolean enable_log = true;
	public static boolean enable_log_sql = false;
	public static int server_port = 8889;
	private static final ArrayList<Connection> cons = new ArrayList<Connection>();
	private static final ArrayList<Statement> stmts = new ArrayList<Statement>();

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
			Connection c = null;
			while (true) {
				try {
					c = DriverManager.getConnection(cs, args[2], args[3]);
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
			cons.add(c);
			stmts.add(c.createStatement());
//			clusterMembers.add(line);
		}
		bfr.close();
		log("connected to cluster databases");

		runServer();
	}

	private static void runServer() throws IOException, ClosedChannelException {
		log("waiting for " + cons.size() + " clients");
		ServerSocketChannel ssc = ServerSocketChannel.open();
		ssc.configureBlocking(false);
		ssc.socket().bind(new InetSocketAddress(server_port));
		Selector selector = Selector.open();
		ssc.register(selector, SelectionKey.OP_ACCEPT);
		while (true) {
			selector.select();
			Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
			while (keys.hasNext()) {
				SelectionKey sk = (SelectionKey) keys.next();
				if (sk.isAcceptable()) {
					SocketChannel sc = ssc.accept();
					log("accept: " + sc.getRemoteAddress());
					sc.configureBlocking(false);
					sc.setOption(StandardSocketOptions.TCP_NODELAY, true);
					sc.register(sk.selector(), SelectionKey.OP_READ, new ClientState());
					keys.remove();
					continue;
				}
				SocketChannel sc = (SocketChannel) sk.channel();
				ClientState cs = (ClientState) sk.attachment();
				try {
					cs.process(sk);
				} catch (Throwable t) {
					close(sc);
					t.printStackTrace();
				}
				keys.remove();
			}
		}
	}

	final static class ClientState {
		ByteBuffer bb = ByteBuffer.allocate(64 * 1024);
		StringBuilder sb = new StringBuilder(1024);
		ByteBuffer bb_nl = ByteBuffer.wrap("\n".getBytes());

		public void process(SelectionKey selectionKey) throws Throwable {
			SocketChannel sc = (SocketChannel) selectionKey.channel();
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

	static void close(SocketChannel sc) {
		Socket s = sc.socket();
		SocketAddress sa = s.getRemoteSocketAddress();
		System.out.println("exception while processing: " + sa);
		try {
			sc.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static int execClusterSqlInsert(final String sql) throws Throwable {
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

	public static void execClusterSql(final String sql) throws Throwable {
		log_sql(sql);
		for (final Statement s : stmts) { // ! thread
			s.execute(sql);
		}
	}

	/** Prints the string to System.out */
	public static void log(String s) {
		if (!enable_log)
			return;
		System.out.println(s);
	}

	public static void log(Throwable t) {
		while (t.getCause() != null)
			t = t.getCause();
		System.err.println(b.stacktraceline(t));
	}

	public static void log_sql(String s) {
		if (!enable_log_sql)
			return;
		System.out.println(s);
	}

	public static String stacktraceline(final Throwable e) {
		return stacktrace(e).replace('\n', ' ').replace('\r', ' ').replaceAll("\\s+", " ").replaceAll(" at ", " @ ");
	}

	public static String stacktrace(final Throwable e) {
		final StringWriter sw = new StringWriter();
		final PrintWriter out = new PrintWriter(sw);
		e.printStackTrace(out);
		out.close();
		return sw.toString();
	}

}
