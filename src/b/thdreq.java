// reviewed: 2024-08-05
// reviewed: 2024-08-13
package b;

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
        }
        start();
    }

    @Override
    public void run() {
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
            }
        }
        synchronized (all_request_threads) {
            all_request_threads.remove(this);
        }
    }

    private void process_request() {
        try {
            if (r.is_websock()) {
                r.websock.process();
                return;
            }
            thdwatch.pages++;
            r.run_page();
        } catch (final Throwable e) {
            b.log(e);
            r.close();
        }
    }
}
