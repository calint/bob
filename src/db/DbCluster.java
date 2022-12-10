package db;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import b.b;

public class DbCluster{
	public static boolean enable_log=true;
	public static boolean enable_log_sql=true;
	public static int server_port=8889;
	private static final ArrayList<Connection> clusterConnections=new ArrayList<Connection>();
	private static final ArrayList<Statement> clusterStatements=new ArrayList<Statement>();
	private static final ArrayList<String> clusterMembers=new ArrayList<String>();
	private static final ArrayList<ClientThread> clientThreads=new ArrayList<ClientThread>();

	public static void main(String[] args) throws Throwable{
		if(args.length<4){
			System.out.println("Usage: java db.DbCluster <ip:port file> <dbname> <user> <password>");
			return;
		}
		// connect to cluster members
		log("reading config: "+args[0]);
		final FileReader fr=new FileReader(args[0]);
		final BufferedReader bfr=new BufferedReader(fr);
		String line;
		log("opening connections to database '"+args[1]+"' user '"+args[2]+"' password '"+args[3]+"'");
		while(true){
			line=bfr.readLine();
			if(line==null)
				break;
			line=line.trim();
			if(line.length()==0)
				continue;
			if(line.startsWith("#"))
				continue;
			log("connecting to: "+line);
			final String cs="jdbc:mysql://"+line+"/"+args[1]+"?verifyServerCertificate=false&useSSL=true&ssl-mode=REQUIRED";
			Connection c=null;
			while(true){
				try{
					c=DriverManager.getConnection(cs,args[2],args[3]);
					break;
				}catch(Throwable t){
					try{
						System.err.println("dbo: cannot create connection. waiting. "+b.stacktraceline(t));
						Thread.sleep(1000);
					}catch(InterruptedException e){
						log(e);
					}
				}
			}
			clusterConnections.add(c);
			clusterStatements.add(c.createStatement());
			clusterMembers.add(line);
		}
		bfr.close();
		log("connected to cluster databases");
		log("listening for "+clusterConnections.size()+" clients");

		// open server socket
		final ServerSocket serverSocket=new ServerSocket(server_port);
		// wait for connections from cluster members
		int nclients=0;
		while(true){
			final Socket socket=serverSocket.accept();
			socket.setTcpNoDelay(true);
			log("accept: "+socket.getRemoteSocketAddress());
			clientThreads.add(new ClientThread(socket));
			nclients++;
			if(nclients==clusterConnections.size())
				break;
		}

		log("all clients are connected. starting threads.");
		// start threads and join
		for(ClientThread t:clientThreads){
			t.start();
		}
		for(ClientThread t:clientThreads){
			t.join();
		}

		serverSocket.close();
	}

	final static class ClientThread extends Thread{
		private static byte[] ba_nl="\n".getBytes();
		Socket socket;
		InputStream is;
		BufferedOutputStream os;
		BufferedReader br;
		public ClientThread(Socket socket) throws Throwable{
			this.socket=socket;
			is=socket.getInputStream();
			os=new BufferedOutputStream(socket.getOutputStream(),32);
			br=new BufferedReader(new InputStreamReader(is),1024);
		}
		@Override public void run(){
			try{
				while(true){
					final String sql=br.readLine();
					if(sql.startsWith("insert ")){
						final int id=execClusterSqlInsert(sql);
						os.write((id+"\n").getBytes());
						os.flush();
						continue;
					}
					execClusterSql(sql);
					os.write(ba_nl);
					os.flush();
				}
			}catch(Throwable t){
				t.printStackTrace();
			}
		}
	}

	public static synchronized int execClusterSqlInsert(final String sql) throws Throwable{
		final ArrayList<Integer> ints=new ArrayList<Integer>(clusterStatements.size());
		int i=0;
		log_sql(sql);
		for(final Statement s:clusterStatements){
			i++;
			log_sql("send to "+i);
			s.execute(sql,Statement.RETURN_GENERATED_KEYS);
			final ResultSet rs=s.getGeneratedKeys();
			if(rs.next()){
				ints.add(rs.getInt(1));
				rs.close();
			}else
				throw new RuntimeException("expected generated id");
		}

		// check that it is the same id
		int prev=ints.get(0);
		final int n=ints.size();
		for(int j=1;j<n;j++){
			final int id=ints.get(j);
			if(id!=prev)
				throw new RuntimeException("expected generated ids to be same. got: "+ints);
			prev=id;
		}
		return prev;
	}

	public static synchronized void execClusterSql(final String sql){
		int i=0;
		log_sql(sql);
		for(final Statement s:clusterStatements){
			i++;
			log("send to "+i);
			try{
				s.execute(sql);
			}catch(Throwable t){
				throw new RuntimeException(t);
			}
		}
	}

	/** Prints the string to System.out */
	public static void log(String s){
		if(!enable_log)
			return;
		System.out.println(s);
	}

	public static void log(Throwable t){
		while(t.getCause()!=null)
			t=t.getCause();
		System.err.println(b.stacktraceline(t));
	}

	public static void log_sql(String s){
		if(!enable_log_sql)
			return;
		System.out.println(s);
	}

	public static String stacktraceline(final Throwable e){
		return stacktrace(e).replace('\n',' ').replace('\r',' ').replaceAll("\\s+"," ").replaceAll(" at "," @ ");
	}

	public static String stacktrace(final Throwable e){
		final StringWriter sw=new StringWriter();
		final PrintWriter out=new PrintWriter(sw);
		e.printStackTrace(out);
		out.close();
		return sw.toString();
	}

}
