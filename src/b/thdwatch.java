package b;
import java.io.*;
import java.lang.reflect.*;
final public class thdwatch extends Thread{
	public static long ms;
	public static long mem;
	public static long input;
	public static long output;
	public static long freethds;
	public static long que;
	public static long sessions;
	public static long iokeys;
	public static long iosel;
	public static long ioevent;
	public static long iocon;
	public static long ioread;
	public static long iowrite;
	public static long reqs;
	public static long cachef;
	public static long cacheu;
	public static long files;
	public static long pages;
	public static long posts;
	public static long socks;
	public static long eagain;
	public static long _cachef;
	public static long _threads;
	public static long _memfree;
	public static int _prevry=100;
	public static long _t0=System.currentTimeMillis();
	private static PrintStream _out=System.out;
	private long _t;
	static boolean _stop;
	final static Field[]_fields=thdwatch.class.getDeclaredFields();
	public thdwatch(){super("watch");}
	final static String _pad="       ";
	public void run(){
		while(!_stop)try{
			if(!b.thd_watch)
				break;
			sleep(b.thd_watch_sleep_in_ms);
			ms=System.currentTimeMillis()-_t0;
			if(ms-_t>b.thd_watch_report_every_ms){
				_t=ms;
				_out.println("\n\n");
				b.stats_to(_out);
				_out.println();
				print_fieldnames_to(_out,"\n");
			}
			_threads=thdreq.all.size();
			final Runtime rt=Runtime.getRuntime();
			_memfree=rt.freeMemory();
			mem=rt.totalMemory()-_memfree;//? doesnotmatchjprofiler
			que=b.pendingreqls().size();
			print_fields_to(_out,"\r");
		}catch(final Throwable t){t.printStackTrace();}
	}
	public static void print_fieldnames_to(final OutputStream os,final String eol)throws IOException{
		for(final Field f:_fields){
			String s=f.getName();
			if (s.startsWith("_"))continue;
			if (s.length()>_pad.length())s=s.substring(0, _pad.length());
			os.write(_pad.substring(0,_pad.length()-s.length()).getBytes());
			os.write(s.getBytes());
			os.write(" ".getBytes());
		}
		os.write(eol.getBytes());
	}
	public static void reset(){//? freethdsgetsderanged
		for(final Field f:_fields){
			final String s=f.getName();
			if(s.startsWith("_"))continue;
			if(f.getType()!=long.class)continue;
			try{f.set(null,Long.valueOf(0));}catch(final Throwable t){throw new Error(t);}
		}
	}
	public static void print_fields_to(final OutputStream os,final String eol)throws IllegalAccessException,IOException{
		for(final Field f:_fields){
			String s=f.getName();
			if(s.startsWith("_"))continue;
			s=f.get(null).toString();
			if(s.length()>_pad.length())s=s.substring(0,_pad.length());
			os.write(_pad.substring(0,_pad.length()-s.length()).getBytes());
			os.write(s.getBytes());
			os.write(" ".getBytes());
		}
		os.write(eol.getBytes());
	}
	public static void print_fields2_to(final osnl os,final byte[]ba_eol,final byte[]ba_eor,final String pad)throws Throwable{
		for(final Field f:_fields){
			String s=f.getName();
			if(s.startsWith("_"))continue;
			if(s.length()>pad.length())s=s.substring(0,pad.length());
			os.write(pad.substring(0,pad.length()-s.length()).getBytes());
			os.write(s.getBytes());
			os.write(": ".getBytes());
			s=f.get(null).toString();
			os.write(s.getBytes());
			os.write(ba_eol);
		}
		os.write(ba_eor);		
	}
}
