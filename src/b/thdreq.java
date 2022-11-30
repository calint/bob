package b;
import java.nio.channels.*;
import java.util.*;
final class thdreq extends Thread{
	static final Collection<thdreq>all_request_threads=new LinkedList<thdreq>();
	private static int seq;
	req r;
	thdreq(final req r){
		super("t"+Integer.toString(seq++));
		this.r=r;
		synchronized(all_request_threads){all_request_threads.add(this);}//?!
		start();
	}
	public void run(){
		//synchronized(all){all.add(this);}//?!
		final long t0=System.currentTimeMillis();
		process_request();
		while(b.thread_pool){
			synchronized(b.pending_requests_list()){
				thdwatch.freethds++;
				while((r=b.pending_requests_list().poll())==null)try{b.pending_requests_list().wait();}catch(InterruptedException ok){}
				thdwatch.freethds--;
			}
			process_request();
			// if thread is older than dt let it finish
			final long dt=System.currentTimeMillis()-t0;
			if(dt>b.thread_pool_lftm)break;
			//if(all.size()>htp.thread_pool_size)break;
		}
		synchronized(all_request_threads){all_request_threads.remove(this);}
	}
	private void process_request(){try{
		if(r.is_sock()){r.sock_thread_run();return;}
		thdwatch.pages++;
		if(r.is_waiting_run_page())r.run_page();
		else if(r.is_waiting_run_page_content())r.run_page_content();
		else throw new IllegalStateException();
		if(r.is_sock())return;
		if(r.is_transfer()){r.selection_key.interestOps(SelectionKey.OP_WRITE);r.selection_key.selector().wakeup();return;}
		if(!r.is_connection_keepalive()){r.close();return;}
		//? bug? if r.buf_len!=0
//		r.parse();
		r.selection_key.interestOps(SelectionKey.OP_READ);
		r.selection_key.selector().wakeup();
	}catch(Throwable e){
		r.close();
		b.log(e);
	}}
}
