package b;

import java.nio.channels.SelectionKey;
import java.util.LinkedList;

/** Request running thread. */
final class thdreq extends Thread {
	static final LinkedList<thdreq> all_request_threads = new LinkedList<thdreq>();
	private static int seq;
	req r;

	thdreq(final req r) {
		super("t" + Integer.toString(seq++));
		this.r = r;
		synchronized (all_request_threads) {
			all_request_threads.add(this);
		} // ?!
		start();
	}

	@Override
	public void run() {
		// synchronized(all){all.add(this);}//?!
		final long t0 = System.currentTimeMillis();
		process_request();
		while (b.thread_pool) {
			synchronized (b.pending_requests_list()) {
				thdwatch.freethds++;
				while ((r = b.pending_requests_list().poll()) == null) {
					try {
						b.pending_requests_list().wait();
					} catch (final InterruptedException ok) {
					}
				}
				thdwatch.freethds--;
			}
			process_request();
			// if thread is older than dt let it finish
			final long dt = System.currentTimeMillis() - t0;
			if (dt > b.thread_pool_lifetime_ms) {
				break;
				// if(all.size()>htp.thread_pool_size)break;
			}
		}
		synchronized (all_request_threads) {
			all_request_threads.remove(this);
		}
	}

	private void process_request() {
		try {
			if (r.is_sock()) {
				r.websock.process();
				return;
			}
			thdwatch.pages++;
			if (!r.is_waiting_run_page())
				throw new IllegalStateException();
			r.run_page();
			// the state of the page may have changed to socket
			if (r.is_sock())
				return;
			if (r.is_transfer()) { // ? can the state of a threaded request be this?
				r.selection_key.interestOps(SelectionKey.OP_WRITE);
				r.selection_key.selector().wakeup();
				return;
			}
			if (!r.is_connection_keepalive()) {
				r.close();
				return;
			}
			// ? this is dubious. what if req has ba_rem left to do. does not support
			// request chaining of pages.
			r.selection_key.interestOps(SelectionKey.OP_READ);
			r.selection_key.selector().wakeup();
		} catch (final Throwable e) {
			b.log(e);
			r.close();
		}
	}
}
