package b;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.StandardSocketOptions;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TimeZone;
import db.Db;
final public class b{
	public final static String strenc="utf-8";
	public final static String q=" ڀ ";
	public final static String a=" ํ ";
	public final static int K=1024;
	public final static int M=K*K;
	public final static long G=K*M;
	public final static long T=K*G;
	public final static long P=K*T;
	public final static String pathsep="/";
	public static @conf String hello="public domain server #1";
	public static String id=""+Integer.toHexString((int)Math.floor(Math.random()*Short.MAX_VALUE));
	public static @conf String root_dir=".";
	public static @conf(reboot=true) String server_port="8888";
//	public static @conf boolean print_requests=false;
//	public static @conf boolean print_reply_headers=false;
//	public static @conf boolean print_replies=false;
	public static @conf boolean try_file=true;
	public static @conf boolean try_rc=true;
	public static @conf(reboot=true,note="requires reboot to turn on") boolean thd_watch=false;
	public static @conf @unit(name="ms") int thd_watch_sleep_in_ms=10000;
	public static @conf @unit(name="ms") int thd_watch_report_every_ms=60000;
	public static @conf(reboot=true) boolean thread_pool=true;
	public static @conf int thread_pool_size=16;
	public static @conf @unit(name="ms") long thread_pool_lftm=60*1000;
//	public static @conf boolean cache_uris=true;
	public static @conf boolean cache_files=true;
	public static @conf(reboot=true) int cache_files_hashlen=K;
	public static @conf @unit(name="B") int cache_files_maxsize=64*K;
	public static @conf @unit(name="ms") long cache_files_validate_dt=1000;
//	public static @conf boolean allow_partial_content_from_cache=true;
	public static @conf @unit(name="B") int transfer_file_write_size=256*K;
	public static @conf @unit(name="B") int io_buf_B=64*K;
//	public static @conf @unit(name="B")int chunk_B=4*K;
	public static @conf @unit(name="B") int chunk_B=16*K;
	public static @conf @unit(name="B") int reqinbuf_B=16*K;
//	public static @conf @unit(name="B")int reqinbuf_B=4*K;
//	public static @conf @unit(name="B")int reqinbuf_B=16;
//	public static @conf @unit(name="B")int reqinbuf_B=1;
	public static @conf String default_directory_file="index.html";
//	public static @conf String default_package_class="$";
	public static @conf boolean gc_before_stats=false;
//	public static @conf final String webobjpkg="a.";
	public static @conf String datetimefmtstr="yyyy-MM-dd HH:mm:ss.sss";
	public static @conf String resources_etag="\"v1\"";
//	public static HashSet<String>resources_in_b=new HashSet<String>(Arrays.asList("x.js","x.css","favicon.ico"));
//	public static @conf boolean resources_enable_any_path=false;
	public static @conf boolean enable_upload=true;
	public static @conf int max_pending_connections=20000;// when overrun causes SYN flood warning
	public static @conf boolean tcpnodelay=true;
	public static @conf boolean print_conf_at_startup=true;
	public static @conf boolean print_stats_at_startup=true;
	public static @conf boolean firewall_on=true;
	public static @conf boolean firewall_paths_on=true;
	public static @conf boolean log_client_disconnects=false;
	public static @conf String sessions_dir="u";

	public static PrintStream out=System.out;
	public static PrintStream err=System.err;

	public static String bapp_class="bob.app";
	public static String bapp_jdbc_host="localhost";
	public static String bapp_jdbc_db="dbo";
	public static String bapp_jdbc_user="user";
	public static String bapp_jdbc_password="password";
	public static int bapp_jdbc_ncons=10;
	public static boolean bapp_cluster_mode=false;
	public static String bapp_cluster_ip="127.0.0.1";
	public static int bapp_cluster_port=8889;
	public static bapp bapp=null;

	private final static HashMap<String,Class<?>> path_to_class_map=new HashMap<String,Class<?>>();
	private final static HashMap<String,String> path_to_resource_map=new HashMap<String,String>();
	private final static LinkedList<req> pending_req=new LinkedList<req>();
	private final static HashMap<String,byte[]> file_suffix_to_content_type_map=new HashMap<String,byte[]>();
	public static void main(final String[] args) throws Throwable{
		out.println(hello);
		id=InetAddress.getLocalHost().getHostName();
		if(!class_init(b.class,args))
			return;
		if(print_conf_at_startup){
			print_hr(out,64);
			class_init(b.class,new String[]{"-1"});
			print_hr(out,64);
		}
		if(print_stats_at_startup)
			stats_to(out);
		b.pl("");

		set_path_to_resource("/x.js","/b/x.js");
		set_path_to_resource("/x.css","/b/x.css");
		set_path_to_resource("/favicon.ico","/b/favicon.ico");
		set_file_suffix_to_content_type("js","application/javascript");

		// initiate db
		Db.initInstance();
		Db.cluster_on=bapp_cluster_mode;
		Db.register(session.class);
		Db.register(sessionobj.class);
		// initiate application
		if(b.bapp_class!=null){
			b.bapp=(bapp)Class.forName(b.bapp_class).getConstructor().newInstance();
			b.bapp.init();
		}
		Db.init("jdbc:mysql://"+bapp_jdbc_host+":3306/"+bapp_jdbc_db+"?verifyServerCertificate=false&useSSL=true&ssl-mode=REQUIRED",bapp_jdbc_user,bapp_jdbc_password,bapp_jdbc_ncons,bapp_cluster_ip,bapp_cluster_port);

		final ServerSocketChannel ssc=ServerSocketChannel.open();
		ssc.configureBlocking(false);
		final InetSocketAddress isa=new InetSocketAddress(Integer.parseInt(server_port));
		final ServerSocket ss=ssc.socket();
		ss.bind(isa,max_pending_connections);
		req.init_static();
		if(thd_watch)
			new thdwatch().start();
		b.pl("port open: "+b.server_port);
		final Selector sel=Selector.open();
		ssc.register(sel,SelectionKey.OP_ACCEPT);
		Runtime.getRuntime().addShutdownHook(new jvmsdh());
		while(true)
			try{
//			sel.select(1000);
				sel.select();
				thdwatch.iokeys=sel.keys().size();
				final Iterator<SelectionKey> it=sel.selectedKeys().iterator();
				if(!it.hasNext())
					continue;
				thdwatch.iosel++;
				while(it.hasNext()){
					thdwatch.ioevent++;
					final SelectionKey sk=it.next();
					it.remove();
					if(sk.isAcceptable()){
						thdwatch.iocon++;
						final req r=new req();
						r.socket_channel=ssc.accept();
//						final InetSocketAddress sa=(InetSocketAddress)r.socket_channel.getRemoteAddress();
//						System.out.println("accepted: "+sa.getAddress().getHostAddress());
						r.socket_channel.configureBlocking(false);

//						r.socket_channel.socket().setReceiveBufferSize(1);
//						r.socket_channel.socket().setSendBufferSize(1);

						if(tcpnodelay) // todo for performance in benchmarks. remove in production.
							r.socket_channel.setOption(StandardSocketOptions.TCP_NODELAY,true);
						r.selection_key=r.socket_channel.register(sel,0,r);
						process(r);
						continue;
					}
					sk.interestOps(0); // ? why?
					final req r=(req)sk.attachment();
					if(sk.isReadable()){
						thdwatch.ioread++;
						process(r);
						continue;
					}
					if(sk.isWritable()){
						thdwatch.iowrite++;
						process(r);
						continue;
					}
					throw new IllegalStateException();
				}
			}catch(final Throwable e){
				log(e);
			}
	}
	private static void process(final req r) throws Throwable{
		if(r.is_sock()){
			if(r.websock.is_threaded()){
//				r.set_waiting_sock_thread_read();
				b.thread(r);
				return;
			}
			r.websock.process();
			return;
		}
		r.process();
	}
//	private static void write(final req r) throws Throwable{
//		if(r.is_sock()){
//			if(r.websock.is_threaded()){
//				r.set_waiting_sock_thread_write();
//				b.thread(r);
//				return;
//			}
//			r.websock.process();
//			return;
//		}
//
//		if(r.is_waiting_write()){ // is oschunked blocked waiting for write?
//			synchronized(r){
//				r.notify();
//			}
//			return;
//		}
//		
//		r.process();
//	}
	static void thread(final req r){
		if(!b.thread_pool){
			new thdreq(r);
			return;
		}
		if(thdreq.all_request_threads.size()<thread_pool_size){
			new thdreq(r);
			return;
		}
		synchronized(pending_req){
			pending_req.addLast(r);
			pending_req.notify();
		}
	}
	/** Called during initiation stage. */
	public static void set_path_to_class(final String path,final Class<?> cls){
		path_to_class_map.put(path,cls);
	}
	/** Called by req. */
	public static Class<?> get_class_for_path(final String path){
		return path_to_class_map.get(path);
	}
	/** Called during initiation stage. */
	public static void set_path_to_resource(final String path,final String resource){
		path_to_resource_map.put(path,resource);
	}
	/** Called by req. */
	public static String get_resource_for_path(final String path){
		return path_to_resource_map.get(path);
	}
	/** Called during initiation stage. */
	public static void set_file_suffix_to_content_type(final String file_suffix,final String content_type){
		file_suffix_to_content_type_map.put(file_suffix,content_type.getBytes());
	}
	/** Called by req. */
	public static byte[] get_content_type_for_file_suffix(final String file_suffix){
		return file_suffix_to_content_type_map.get(file_suffix);
	}
	private static void print_hr(final OutputStream os,final int width_in_chars) throws IOException{ // ? only used in
																										// b.main,
																										// remove?
//		for(int i=0;i<width_in_chars;i++)
//			os.write((byte)(Math.random()<.5?'~':' '));
		float prob=1;
		float dprob_di=prob/width_in_chars;
		for(int i=0;i<width_in_chars;i++){
			os.write((byte)(Math.random()<prob?'~':' '));
			prob-=dprob_di;
		}
		os.write((byte)'\n');
	}
	public static int cp(final InputStream in,final OutputStream out,final sts sts) throws Throwable{
		final byte[] buf=new byte[io_buf_B];
		int n=0;
		while(true){
			final int count=in.read(buf);
			if(count<=0)
				break;
			out.write(buf,0,count);
			n+=count;
			if(sts!=null)
				sts.sts_set(Long.toString(n));
		}
		return n;
	}
	public static int cp(final Reader in,final Writer out,final sts sts) throws Throwable{
		final char[] buf=new char[io_buf_B];
		int n=0;
		while(true){
			final int count=in.read(buf);
			if(count<=0)
				break;
			out.write(buf,0,count);
			n+=count;
			if(sts!=null)
				sts.sts_set(Long.toString(n));
		}
		return n;
	}
	public static synchronized void log(Throwable t){
		while(t.getCause()!=null)
			t=t.getCause();
		if(!log_client_disconnects){
			if(t instanceof java.nio.channels.CancelledKeyException)
				return;
			if(t instanceof java.nio.channels.ClosedChannelException)
				return;
			if(t instanceof java.net.SocketException){
				final String msg=t.getMessage();
				if("Connection reset".equals(msg))
					return;
			}
			if(t instanceof java.io.IOException){
				final String msg=t.getMessage();
				if("Broken pipe".equals(msg))
					return;
				if("Connection reset by peer".equals(msg))
					return;
				if("An existing connection was forcibly closed by the remote host".equals(msg))
					return;
			}
		}
		err.println(b.stacktraceline(t));
	}
	public static path path(){
		return new path(new File(root_dir),true);
	}
	public static path path(final String path){
		ensure_path_ok(path);
		final path p=new path(new File(root_dir,path));// ? dont inst path yet
		final String uri=p.uri();
		if(firewall_paths_on)
			firewall_ensure_path_access(uri);
		return p;
	}
	static void firewall_ensure_path_access(final String uri){
	}
//	static path path_ommit_firewall_check(final String path){
//		ensure_path_ok(path);
//		return new path(new File(root_dir,path));
//	}
	private static void ensure_path_ok(final String path) throws Error{
		if(path.contains(".."))
			throw new Error("illegalpath "+path+": containing '..'");
	}
	static LinkedList<req> pending_requests_list(){
		return pending_req;
	}

	private static long stats_last_t_ms;
	private static long stats_last_io_B;
	public static void stats_to(final OutputStream out) throws Throwable{
		final long t_ms=System.currentTimeMillis();
		final long dt_ms=t_ms-stats_last_t_ms;
		stats_last_t_ms=t_ms;
		final long total_io_B=thdwatch.input+thdwatch.output;
		final long dB=total_io_B-stats_last_io_B;
		stats_last_io_B=total_io_B;
		final float dBdt_s=dt_ms==0?0:dB*1000/dt_ms;
		final int throughput_qty;
		final String throughput_unit;
		if(dBdt_s==0){
			throughput_qty=0;
			throughput_unit="";
		}else if(dBdt_s>M){
			throughput_qty=(int)(dBdt_s/M+0.5f);
			throughput_unit=" MB/s";
		}else if(dBdt_s>K){
			throughput_qty=(int)(dBdt_s/K+0.5f);
			throughput_unit=" KB/s";
		}else{
			throughput_qty=(int)(dBdt_s);
			throughput_unit=" B/s";
		}
		final PrintStream ps=new PrintStream(out);
		ps.println(hello);
//		for(final NetworkInterface ni:Collections.list(NetworkInterface.getNetworkInterfaces())){
//			final String nm=ni.getName();
//			if(nm.startsWith("lo"))
//				continue;
//			ps.println("              url: ");
//			for(final InetAddress ia:Collections.list(ni.getInetAddresses())){
//				final String s=ia.getHostAddress();
//				if(!s.matches("\\d+\\.\\d+\\.\\d+\\.\\d+"))
//					continue;
//				p("http://");
//				p(s);
//				if(!server_port.equals("80")){
//					p(":");
//					p(server_port);
//				}
//				p("/");
//				break;
//			}
//			p("\n");
//		}
		ps.println("               id: "+id);
		ps.println("             time: "+tolastmodstr(t_ms));
		ps.println("             port: "+server_port);
		ps.println("            input: "+(thdwatch.input>>10)+" KB");
		ps.println("           output: "+(thdwatch.output>>10)+" KB");
		ps.println("       throughput: "+throughput_qty+throughput_unit);
//		ps.println("         sessions: "+session.all().size());
		ps.println("        downloads: "+new File(root_dir).getCanonicalPath());
		ps.println("     sessions dir: "+new File(sessions_dir).getCanonicalPath());
		ps.println("     cached files: "+(req.file_and_resource_cache_size_B()>>10)+" KB");
//		ps.println("      cached uris: "+(req.cacheu_size()>>10)+" KB");
		ps.println("        classpath: "+System.getProperty("java.class.path"));
		final Runtime rt=Runtime.getRuntime();
		if(gc_before_stats)
			rt.gc();
		final long m1=rt.totalMemory();
		final long m2=rt.freeMemory();
		ps.println("         ram used: "+((m1-m2)>>10)+" KB");
		ps.println("         ram free: "+(m2>>10)+" KB");
		ps.println("          threads: "+thdreq.all_request_threads.size());
		ps.println("            cores: "+Runtime.getRuntime().availableProcessors());
//		ps.println("            cloud: "+cloud_bees);
	}
	public static int rndint(final int from,final int tonotincl){
		return (int)(Math.random()*(tonotincl-from)+from);
	}
	public static String stacktrace(final Throwable e){
		final StringWriter sw=new StringWriter();
		final PrintWriter out=new PrintWriter(sw);
		e.printStackTrace(out);
		out.close();
		return sw.toString();
	}
	public static String stacktraceline(final Throwable e){
		return stacktrace(e).replace('\n',' ').replace('\r',' ').replaceAll("\\s+"," ").replaceAll(" at "," @ ");
	}
	public static String tolastmodstr(final long t){
		final SimpleDateFormat sdf=new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		return sdf.format(new Date(t));
	}
	public static String urldecode(final String s){
		try{
			return URLDecoder.decode(s,strenc);
		}catch(UnsupportedEncodingException e){
			throw new Error(e);
		}
	}
	public static String urlencode(final String s){
		try{
			return URLEncoder.encode(s,strenc);
		}catch(UnsupportedEncodingException e){
			throw new Error(e);
		}
	}
	public static String tostr(final Object object,final String def){
		return object==null?def:object.toString();
	}
	public static byte[] tobytes(final String v){
		try{
			return v.getBytes(strenc);
		}catch(UnsupportedEncodingException e){
			throw new Error(e);
		}
	}
	public static boolean isempty(final String s){
		return s==null||s.length()==0;
	}
	public static String isempty(final String s,final String def){
		if(isempty(s)){
			if(def==null)
				return "";
			else
				return def;
		}
		return s;
	}
//	public static long get_session_bits_for_sessionid(final String sesid){// ? dubious function
//		//? file(system){sha1(sessionid),bits}
//		if("".equals(sesid))return 0;
//		return 1;
//	}
	public static void class_printopts(final Class<?> cls) throws IllegalArgumentException,IllegalAccessException{
		for(final Field f:cls.getFields()){
			final Object o=f.get(null);
			out.print(f.getName());
			out.print("=");
			String type=f.getType().getName();
			if(type.startsWith("java.lang."))
				type=type.substring("java.lang.".length());
			if(type.startsWith("java.util."))
				type=type.substring("java.util.".length());
			final boolean isstr=type.equals("String");
			final boolean isbool=type.equals("boolean");
			final boolean isint=type.equals("int");
			final boolean islong=type.equals("long");
			final boolean print_type=!(isstr||isbool||isint||islong);
			if(isstr)
				out.print("\"");
			if(print_type){
				out.print(type);
				out.print("(");
			}
			out.print(o==null?"":o.toString().replaceAll("\\n","\\\\n"));
//			if(islong)out.print("L");
			if(isstr)
				out.print("\"");
			if(print_type){
				out.print(")");
			}
			out.println();
		}
	}
	public static boolean class_init(final Class<?> cls,final String[] args) throws SecurityException,NoSuchFieldException,IllegalArgumentException,IllegalAccessException{
		if(args==null||args.length==0)
			return true;
		if("-1".equals(args[0])){
			class_printopts(cls);
			return false;
		}
		for(int i=0;i<args.length;i+=2){
			final String fldnm=args[i];
			final Field fld=cls.getField(fldnm);
			final String val=args[i+1];
			pl("conf "+fldnm+"="+val);
			final Class<?> fldcls=fld.getType();
			if(fldcls.isAssignableFrom(String.class))
				fld.set(null,val);
			else if(fldcls.isAssignableFrom(int.class))
				fld.set(null,Integer.parseInt(val));
			else if(fldcls.isAssignableFrom(boolean.class))
				fld.set(null,"1".equals(val)||"true".equals(val)||"yes".equals(val)||"y".equals(val)?Boolean.TRUE:Boolean.FALSE);
			else if(fldcls.isAssignableFrom(long.class))
				fld.set(null,Long.parseLong(val));
		}
		return true;
	}
	static enum op{
		read,write,noop
	}
	public static void cp(final InputStream in,final Writer out) throws Throwable{
		cp(new InputStreamReader(in,strenc),out,null);
	}
	public static void pl(final String s){
		out.println(s);
	}
	public static void p(final String s){
		out.print(s);
	}

	public static @Retention(RetentionPolicy.RUNTIME) @interface unit{
		String name() default "";
	}
	public static @Retention(RetentionPolicy.RUNTIME) @interface conf{
		String note() default "";
		boolean reboot() default false;
	}
//	public static @Retention(RetentionPolicy.RUNTIME)@interface conf_reboot{String note()default"";}
//	public static @Retention(RetentionPolicy.RUNTIME) @interface ref{
//	}

	public static @Retention(RetentionPolicy.RUNTIME) @interface acl{
		long create() default 0;
//		long list()default 0;
//		long peek()default 0;
//		long view()default 0;
//		long append()default 0;
//		long edit()default 0;
//		long rename()default 0;
//		long delete()default 0;
	}
//	public static interface client{bits acl_bits();}
//	public static interface bits{
//		boolean hasany(final bits b);
//		boolean hasall(final bits b);
//		int to_int();
//		long to_long();
//	}

//	static void acl_ensure_create(final a e){
//		final Class<? extends a>ecls=e.getClass();
//		final acl a=ecls.getAnnotation(acl.class);
//		if(a==null)return;
//		final long bits_c=a.create();
//		final req r=req.get();
//		final session ses=r.session();
//		if(ses.bits_hasany(bits_c))return;
//		throw new SecurityException("cannot create item of type "+ecls+" due to acl\n any:  0b"+Long.toBinaryString(ses.bits())+" vs 0b"+Long.toBinaryString(bits_c));
//	}
//	static void acl_ensure_post(final a e){
//		final Class<? extends a>ecls=e.getClass();
//		final acl a=ecls.getAnnotation(acl.class);
//		if(a==null)return;
//		final long bits_c=a.create();
//		final req r=req.get();
//		final session ses=r.session();
//		if(ses.bits_hasany(bits_c))return;
//		throw new SecurityException("cannot post to item of type "+ecls+" due to acl\n any:  0b"+Long.toBinaryString(ses.bits())+" vs 0b"+Long.toBinaryString(bits_c));
//	}
	public static void firewall_assert_access(final a e){
		final Class<? extends a> cls=e.getClass();
		if(cls.equals(a.class))
			return;
		final String clsnm=cls.getName();
//		final int i=clsnm.lastIndexOf('.');
//		final String pkgnm=i==-1?"":clsnm.substring(0,i);
//		if(pkgnm.endsWith(".a")&&!req.get().session().bits_hasall(2))throw new Error("firewalled1");
		if(clsnm.startsWith("a.localhost.")&&!req.get().ip().toString().equals("/0:0:0:0:0:0:0:1"))
			throw new Error("firewalled2");
	}
	public static String file_to_uri(final File f){// ? cleanup
		final String u1=f.getPath();
		if(!u1.startsWith(root_dir))
			throw new SecurityException("path "+u1+" not in root "+root_dir);
		final String u4=u1.substring(root_dir.length());
		final String u2=u4.replace(File.pathSeparatorChar,'/');
		final String u3=u2.replace(' ','+');
		return u3;
	}
}
