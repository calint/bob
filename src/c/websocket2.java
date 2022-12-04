package c;

import java.nio.ByteBuffer;
import java.util.Map;

import b.req;
import b.threadedsock;
import b.websock;

final public class websocket2 extends websock implements threadedsock {
	private static int nthreads;
	private boolean is_closed;
	private String session_id;

	private class thd extends Thread {
		thd() {
			super("websocket " + (++nthreads));
		}

		@Override
		public void run() {
			int i = 1;
			b.b.pl("thread " + Integer.toHexString(this.hashCode()) + " " + session_id + ": start");
			while (!websocket2.this.is_closed) {
				try {
					websocket2.this.send("ping " + i);
				} catch (Throwable t) {
					b.b.log(t);
				}
				i++;
				try {
					Thread.sleep(5000);
				} catch (InterruptedException ignored) {
				}
			}
			b.b.pl("thread " + Integer.toHexString(this.hashCode()) + ": exit");
		}
	}

	synchronized final @Override protected void on_opened(final Map<String, String> headers) throws Throwable {
		session_id = req.get_session_id_from_headers(headers);
		System.out.println("websocket " + Integer.toHexString(hashCode()) + ": onopen (session " + session_id + ")");
		new thd().start();
	}

	@Override
	protected void on_message(ByteBuffer bb) throws Throwable {
		System.out.println("websocket " + Integer.toHexString(hashCode()) + ": onmessage size:" + bb.remaining());
//		send(new String(bb.array(), bb.position(), bb.remaining()));
//		return;
//		if(bb.remaining()==0) {
//			send("");
//			return;
//		}
		String msg = new String(bb.array(), bb.position(), bb.remaining());
//		Timestamp ts = new Timestamp(System.currentTimeMillis());
//		String s = ts.toString() + " " + req.get().ip() + " " + msg;
		send(msg);
//		byte[]b=new byte[8*1024*1024];
//		for(int i=0;i<b.length;i++)
//			b[i]='0';
//		String s2=new String(b);
//		send(s2);
//		send(s);
	}

	synchronized final @Override protected void on_closed() throws Throwable {
		System.out.println("websocket " + Integer.toHexString(hashCode()) + ": onclosed");
		is_closed = true;
	}
}
