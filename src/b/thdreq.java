package b;
import java.nio.channels.*;
import java.util.*;
final class thdreq extends Thread{
	static final Collection<thdreq>all=new LinkedList<thdreq>();
	private static int seq;
	req r;
	thdreq(final req r){
		super("t"+Integer.toString(seq++));
		this.r=r;
		synchronized(all){all.add(this);}//?!
		start();
	}
	public void run(){
		//synchronized(all){all.add(this);}//?!
		final long t0=System.currentTimeMillis();
		proc();
		while(b.thread_pool){
			final LinkedList<req>ls=b.pendingreqls();
			synchronized(ls){
				thdwatch.freethds++;
				while((r=ls.poll())==null)try{ls.wait();}catch(InterruptedException ok){}
				thdwatch.freethds--;
			}
			proc();
			final long dt=System.currentTimeMillis()- t0;
			if(dt>b.thread_pool_lftm)break;
			//if(all.size()>htp.thread_pool_size)break;
		}
		synchronized(all){all.remove(this);}
	}
	private void proc(){try{
		if(r.is_sock()){r.sock_thread_run();return;}
		thdwatch.pages++;
		if(r.is_waiting_run_page())r.run_page();
		else if(r.is_waiting_run_page_content())r.run_page_content();
		else throw new IllegalStateException();
		if(r.is_sock())return;
		if(r.is_transfer()){r.selkey.interestOps(SelectionKey.OP_WRITE);r.selkey.selector().wakeup();return;}
		if(!r.is_connection_keepalive()){r.close();	return;	}
		//? bug? if r.buf_len!=0
//		r.parse();
		r.selkey.interestOps(SelectionKey.OP_READ);
		r.selkey.selector().wakeup();
	}catch(Throwable e){
		r.close();b.log(e);}}//? logtofile
}
